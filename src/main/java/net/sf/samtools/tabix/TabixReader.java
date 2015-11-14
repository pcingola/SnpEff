package net.sf.samtools.tabix;

/* The MIT License

   Copyright (c) 2010 Broad Institute.

   Permission is hereby granted, free of charge, to any person obtaining
   a copy of this software and associated documentation files (the
   "Software"), to deal in the Software without restriction, including
   without limitation the rights to use, copy, modify, merge, publish,
   distribute, sublicense, and/or sell copies of the Software, and to
   permit persons to whom the Software is furnished to do so, subject to
   the following conditions:

   The above copyright notice and this permission notice shall be
   included in all copies or substantial portions of the Software.

   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
   BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
   ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
   CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
   SOFTWARE.
 */

/* Contact: Heng Li <hengli@broadinstitute.org> */

/* Minor changes by Pablo Cingolani (Jan-2014) */

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;
import net.sf.samtools.util.BlockCompressedInputStream;

public class TabixReader implements Iterable<String> {

	/**
	 * Iterate on a result from TabixReader.query()
	 */
	public class TabixIterator implements Iterator<String>, Iterable<String> {
		private int i;
		private int tid, beg, end;
		private TPair64[] off;
		private long curr_off;
		private boolean iseof;
		private String next = null;
		private boolean showHeader = false; // By default, do not return header lines

		public TabixIterator(final int _tid, final int _beg, final int _end, final TPair64[] _off) {
			i = -1;
			curr_off = 0;
			iseof = false;
			off = _off;
			tid = _tid;
			beg = _beg;
			end = _end;
		}

		@Override
		public boolean hasNext() {
			if (next == null) next = readNext(); // Try reading next item.
			return (next != null);
		}

		@Override
		public Iterator<String> iterator() {
			return this;
		}

		@Override
		public String next() {
			if (hasNext()) {
				String ret = next;
				next = null;
				return ret;
			}
			return null;

		}

		/**
		 * Read next line
		 */
		private String readNext() {
			try {
				if (iseof) {
					if (debug) Gpr.debug("readNext return: EOF");
					return null;
				}

				for (;;) {
					if (curr_off == 0 || !less64(curr_off, off[i].v)) { // then jump to the next chunk
						if (i == off.length - 1) {
							if (debug) Gpr.debug("readNext break: No more chunks");
							break; // no more chunks
						}
						if (i >= 0) assert (curr_off == off[i].v); // otherwise bug
						if (i < 0 || off[i].v != off[i + 1].u) { // not adjacent chunks; then seek
							long pos = off[i + 1].u;
							if (pos == latestIntvPos //
									&& (latestIntv != null) //
									&& ((latestIntv.tid != tid) || (latestIntv.beg >= end)) //
							) {
								Gpr.debug("readNext return: Cached interval starts before query end" //
										+ "\n\tFile position: " + latestIntvPos //
										+ "\n\tInterval      : " + latestIntv //
								);
								return null;
							} else {
								Gpr.debug("Cache miss\tpos: " + pos + ", latestIntvPos: " + latestIntvPos + ", latestInv: " + latestIntv + ", tid: " + tid + ", end: " + end);
							}

							seek(pos);
							curr_off = fileInputStream.getFilePointer();
							if (debug) Gpr.debug("readNext seek: " + off[i + 1].u + "\tcurr_off: " + curr_off);
						}
						++i;
					}

					String s;
					if ((s = readLine(fileInputStream)) != null) {
						// TIntv intv;
						char[] str = s.toCharArray();
						curr_off = fileInputStream.getFilePointer();
						if (str.length == 0) {
							if (debug) Gpr.debug("readNext continue, empty line");
							continue;
						}

						// Check header
						if (str[0] == mMeta) {
							if (!showHeader) {
								if (debug) Gpr.debug("readNext continue, header line: " + s);
								continue;
							}

							if (debug) Gpr.debug("readNext return, line: " + s);
							return s;
						}

						// Check range
						latestIntv = getIntv(s);
						latestIntvPos = curr_off;

						if (((tid >= 0) && (latestIntv.tid != tid)) || latestIntv.beg >= end) {
							// No need to proceed. Note: tid < 0 means any-chromosome (i.e. no-limits)
							if (debug) Gpr.debug("readNext break: Interval from file after query:" //
									+ "\n\tQuery        :\t" + "tid: " + tid + ", start: " + beg + ", end: " + end //
									+ "\n\tFile interval:\t" + latestIntv //
							);
							break;
						} else if (latestIntv.end > beg && latestIntv.beg < end) {
							if (debug) Gpr.debug("readNext return, line: " + s);
							return s; // overlap; return
						}
					} else {
						if (debug) Gpr.debug("readNext break: End of file");
						break; // end of file
					}
				}

				iseof = true;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			if (debug) Gpr.debug("readNext return, line: null");
			return null;
		}

		@Override
		public void remove() {
			throw new RuntimeException("Unimplemented!");
		}

		void seek(long pos) throws IOException {
			if (debug) Gpr.debug("seek(" + pos + ")");
			fileInputStream.seek(pos);
		}

		public void setShowHeader(boolean showHeader) {
			this.showHeader = showHeader;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("tid:" + tid + "\tbeg:" + beg + "\tend:" + end);
			for (TPair64 tp : off)
				sb.append("\t" + tp + "\n");
			return sb.toString();
		}
	}

	/**
	 * Tabix Index
	 * From the paper:
	 *
	 * Binnig index:
	 *      In Tabix, each bin k, 0 <= k <= 37449, represents a half-close-half-open interval
	 *
	 *          [ (k-ol) sl , (k-ol+1) sl )
	 *
	 *      , where
	 *      	'l' is the level of the bin             l = floor[ log2(7k + 1) / 3 ]
	 *      	'sl' is the size of the bin at level l  sl = 2^(29 - 3 l)
	 *			'ol': is the offset at l.               ol = (23 l - 1)/7
	 *
	 *      In this scheme, bins span different sizes depending on their levels:
	 *      	Level	Bins		 Size (sl)
	 *      	------------------------------
	 *      	0		0			512Mb	2^29
	 *      	1		1-8			 64Mb	2^26
	 *      	2		9-72		  8Mb	2^23
	 *      	3		73-584		  1Mb	2^20
	 *      	4		585-4680	128kb	2^17
	 *      	5		4681-37449	 16kb	2^14
	 *
	 * Linear index: In the linear index, we keep for each tiling 16kb window
	 * 		the virtual file offset of the leftmost record (i.e. having the
	 * 		smallest start coordinate) that overlaps the window. When we search
	 * 		for records overlapping a query interval, we will know from the
	 * 		index the leftmost record that possibly overlaps the query interval.
	 * 		Records having smaller coordinates than this leftmost record can be
	 * 		skipped and unsuccessful seek calls can be saved.
	 */
	private class TIndex {
		HashMap<Integer, TPair64[]> binningIndex; // Binning index
		long[] linearIndex; // Linear index

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			ArrayList<Integer> keys = new ArrayList<>();
			keys.addAll(binningIndex.keySet());
			Collections.sort(keys);

			sb.append("Binning index size:" + binningIndex.size() + "\n");
			for (Integer binNum : keys) {
				TPair64[] chunks = binningIndex.get(binNum);
				sb.append("\t" + binInfo(binNum) + "\n\tNumber of chunks:" + chunks.length + "\n");

				for (int i = 0; i < chunks.length; i++)
					sb.append("\t\tchunk " + i + "\t" + chunks[i] + "\n");
			}

			sb.append("Linear index size: " + linearIndex.length + "\n");
			for (int i = 0; i < linearIndex.length; i++)
				sb.append("\t" + i + "\t" + linearIndex[i] + "\n");

			return sb.toString();
		}

	}

	private class TIntv {
		int tid, beg, end;

		@Override
		public String toString() {
			return "tid: " + tid //
					+ ", start: " + beg //
					+ ", end: " + end //
					;
		}
	}

	/**
	 * Pair of 'long' (64 bits)
	 */
	private class TPair64 implements Comparable<TPair64> {
		long u, v;

		public TPair64(final long _u, final long _v) {
			u = _u;
			v = _v;
		}

		public TPair64(final TPair64 p) {
			u = p.u;
			v = p.v;
		}

		@Override
		public int compareTo(final TPair64 p) {
			return u == p.u ? 0 : ((u < p.u) ^ (u < 0) ^ (p.u < 0)) ? -1 : 1; // unsigned 64-bit comparison
		}

		@Override
		public String toString() {
			return "<" + u + "," + v + ">";
		}
	}

	private static int MAX_BIN = 37450; // Maximum possible number of bins
	private static int TAD_LIDX_SHIFT = 14; // Minimum bin size is 2^TAD_LIDX_SHIFT = 2^14 = 16KB

	boolean debug = false;
	private String fileName;
	private BlockCompressedInputStream fileInputStream;
	private int mPreset;
	private int mSc;
	private int mBc;
	private int mEc;
	private int mMeta;
	private int mSkip;;
	private String[] sequenceNames;;
	private HashMap<String, Integer> sequenceName2tid;
	private TIndex[] tabixIndexes;;
	private TabixIterator tabixIterator;
	protected boolean showHeader;
	long latestIntvPos = -1;
	TIntv latestIntv = null;

	public static String binInfo(int k) {
		int l = (int) Math.floor((Math.log(7 * k + 1) / (3 * Math.log(2.0))));
		int sl = 1 << (29 - 3 * l);
		int ol = ((1 << (3 * l)) - 1) / 7;

		int start = (k - ol) * sl;
		int end = (k + 1 - ol) * sl;

		return "bin: " + k //
				+ ", level: " + l //
				+ ", size: " + sl //
				+ ", offset: " + ol //
				+ ", interval: [ " + start + " , " + end + " )" //
				;
	}

	/**
	 * Unsigned 64-bit comparison
	 */
	private static boolean less64(final long u, final long v) {
		return (u < v) ^ (u < 0) ^ (v < 0);
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Usage: java -cp .:sam.jar TabixReader <in.gz> [region]");
			System.exit(1);
		}
		try {
			TabixReader tr = new TabixReader(args[0]);
			String s;
			if (args.length == 1) { // no region is specified; print the whole file
				while ((s = tr.readLine()) != null)
					System.out.println(s);
			} else { // a region is specified; random access
				TabixReader.TabixIterator iter = tr.query(args[1]); // get the iterator
				while (iter != null && (s = iter.next()) != null)
					System.out.println(s);
			}
		} catch (IOException e) {
		}
	}

	public static int readInt(final InputStream is) throws IOException {
		byte[] buf = new byte[4];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getInt();
	}

	public static String readLine(final InputStream is) throws IOException {
		StringBuffer buf = new StringBuffer();
		int c;
		while ((c = is.read()) >= 0 && c != '\n')
			buf.append((char) c);
		if (c < 0) return null;
		return buf.toString();
	}

	public static long readLong(final InputStream is) throws IOException {
		byte[] buf = new byte[8];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	/**
	 * Bins span different sizes depending on their levels:
	 *      	Bins		 Size (sl)
	 *      	------------------------
	 *      	0			512Mb	2^29
	 *      	1-8			 64Mb	2^26
	 *      	9-72		  8Mb	2^23
	 *      	73-584		  1Mb	2^20
	 *      	585-4680	128kb	2^17
	 *      	4681-37449	 16kb	2^14
	 */
	private static int reg2bins(final int beg, final int _end, final int[] list) {
		int i = 0, k, end = _end;
		if (beg >= end) return 0;

		// Max size = 2^29
		if (end >= 1 << 29) end = 1 << 29;
		--end;
		list[i++] = 0;

		// Bins 1
		for (k = 1 + (beg >> 26); k <= 1 + (end >> 26); ++k)
			list[i++] = k;

		for (k = 9 + (beg >> 23); k <= 9 + (end >> 23); ++k)
			list[i++] = k;

		for (k = 73 + (beg >> 20); k <= 73 + (end >> 20); ++k)
			list[i++] = k;

		for (k = 585 + (beg >> 17); k <= 585 + (end >> 17); ++k)
			list[i++] = k;

		for (k = 4681 + (beg >> 14); k <= 4681 + (end >> 14); ++k)
			list[i++] = k;

		return i;
	}

	/**
	 * The constructor
	 *
	 * @param fn File name of the data file
	 */
	public TabixReader(final String fn) throws IOException {
		fileName = fn;
		fileInputStream = new BlockCompressedInputStream(new File(fn));
		readIndex();
	}

	private int chr2tid(String chr) {
		if (sequenceName2tid.containsKey(chr)) return sequenceName2tid.get(chr);

		// Try simple chromosome names (no 'chr')
		chr = Chromosome.simpleName(chr);
		if (sequenceName2tid.containsKey(chr)) return sequenceName2tid.get(chr);

		else return -1;
	}

	public void close() {
		if (fileInputStream != null) {
			try {
				fileInputStream.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			fileInputStream = null;
		}
	}

	private TIntv getIntv(final String s) {
		TIntv intv = new TIntv();
		int col = 0, end = 0, beg = 0;
		while ((end = s.indexOf('\t', beg)) >= 0 || end == -1) {
			++col;
			if (col == mSc) {
				intv.tid = chr2tid(s.substring(beg, end));
			} else if (col == mBc) {
				intv.beg = intv.end = Integer.parseInt(s.substring(beg, end == -1 ? s.length() : end));
				if ((mPreset & 0x10000) != 0) ++intv.end;
				else--intv.beg;
				if (intv.beg < 0) intv.beg = 0;
				if (intv.end < 1) intv.end = 1;
			} else {
				// SAM supports are not tested yet
				if ((mPreset & 0xffff) == 0) { // generic
					if (col == mEc) intv.end = Integer.parseInt(s.substring(beg, end));
				} else if ((mPreset & 0xffff) == 1) { // SAM
					if (col == 6) { // CIGAR
						int l = 0, i, j;
						String cigar = s.substring(beg, end);
						for (i = j = 0; i < cigar.length(); ++i) {
							if (cigar.charAt(i) > '9') {
								int op = cigar.charAt(i);
								if (op == 'M' || op == 'D' || op == 'N') l += Integer.parseInt(cigar.substring(j, i));
							}
						}
						intv.end = intv.beg + l;
					}
				} else if ((mPreset & 0xffff) == 2) {
					// VCF
					String alt;
					alt = end >= 0 ? s.substring(beg, end) : s.substring(beg);
					if (col == 4) { // REF
						if (alt.length() > 0) intv.end = intv.beg + alt.length();
					} else if (col == 8) { // INFO
						int e_off = -1, i = alt.indexOf("END=");
						if (i == 0) e_off = 4;
						else if (i > 0) {
							i = alt.indexOf(";END=");
							if (i >= 0) e_off = i + 5;
						}
						if (e_off > 0) {
							i = alt.indexOf(";", e_off);
							intv.end = Integer.parseInt(i > e_off ? alt.substring(e_off, i) : alt.substring(e_off));
						}
					}
				}
			}
			if (end == -1) break;
			beg = end + 1;
		}
		return intv;
	}

	@Override
	public TabixIterator iterator() {
		if (tabixIterator != null) return tabixIterator;

		// No query performed yet: create one
		TPair64 off[] = new TPair64[1];
		off[0] = new TPair64(0, Long.MAX_VALUE);
		tabixIterator = new TabixIterator(-1, 0, Integer.MAX_VALUE, off);
		tabixIterator.setShowHeader(showHeader); // Do we want header lines?

		return tabixIterator;
	}

	/**
	 * Parse a region in the format of "chr1", "chr1:100" or "chr1:100-1000"
	 *
	 * @param reg Region string
	 * @return An array where the three elements are
	 * 			[ sequence_id, region_begin, region_end]
	 * 		   On failure, sequence_id==-1.
	 */
	public int[] parseReg(final String reg) {
		String chr;
		int colon, hyphen;
		int[] ret = new int[3];
		colon = reg.indexOf(':');
		hyphen = reg.indexOf('-');
		chr = colon >= 0 ? reg.substring(0, colon) : reg;
		ret[1] = colon >= 0 ? Integer.parseInt(reg.substring(colon + 1, hyphen >= 0 ? hyphen : reg.length())) - 1 : 0;
		ret[2] = hyphen >= 0 ? Integer.parseInt(reg.substring(hyphen + 1)) : 0x7fffffff;
		ret[0] = chr2tid(chr);
		return ret;
	}

	/**
	 * Query for a given interval:
	 * @param tid : Chromosome number
	 * @param beg : Interval start
	 * @param end : Interval end
	 * @return
	 */
	private TabixIterator query(final int tid, final int beg, final int end) {
		TPair64[] chunksOff, chunks;
		long minFileOffset;

		if (tid < 0) return null;

		// Get index for chromosome 'tid'
		TIndex idx = tabixIndexes[tid];
		int[] bins = new int[MAX_BIN];
		int i, l, numChunks;

		// Fill up 'bins' with the bin numbers that are intersected by [beg, end) interval.
		int numBins = reg2bins(beg, end, bins);

		// Minimum offset within file
		// Linear index has the offset of the smallest start coordinate that
		// overlaps the each 16KB window (i.e. all possible lowest level bins)
		if (idx.linearIndex.length > 0) {
			int begTad = beg >> TAD_LIDX_SHIFT;
			if (begTad >= idx.linearIndex.length) minFileOffset = idx.linearIndex[idx.linearIndex.length - 1]; // Pick last position in linear index
			else minFileOffset = idx.linearIndex[begTad]; // Use linear index
		} else minFileOffset = 0;

		// Add chunk lengths for all blocks within the interval
		for (i = numChunks = 0; i < numBins; ++i) {
			chunks = idx.binningIndex.get(bins[i]);
			if (chunks != null) numChunks += chunks.length;
		}

		// Zero chunks? Then there is nothing in the index matching the interval
		if (numChunks == 0) return null;

		// Collect all chunks having their end coordinated after min_off
		chunksOff = new TPair64[numChunks];
		for (i = numChunks = 0; i < numBins; ++i) {
			chunks = idx.binningIndex.get(bins[i]);
			if (chunks != null) {
				for (int j = 0; j < chunks.length; ++j) {
					// Chunk ends after minFileOffset? => Create a copy and add it to the array
					// If chunk is before minFileOffset, so it is filtered out
					if (less64(minFileOffset, chunks[j].v)) {
						chunksOff[numChunks++] = new TPair64(chunks[j]);
					}
				}
			}
		}

		// No chunks left? Nothing to do (index doesn't match query)
		if (numChunks == 0) return null;

		// Sort chunks (note, after 'numChunks' the array is null)
		Arrays.sort(chunksOff, 0, numChunks);

		// Resolve completely contained adjacent blocks
		for (i = 1, l = 0; i < numChunks; ++i) {
			if (less64(chunksOff[l].v, chunksOff[i].v)) {
				++l;
				chunksOff[l].u = chunksOff[i].u;
				chunksOff[l].v = chunksOff[i].v;
			}
		}
		numChunks = l + 1;

		// Resolve overlaps between adjacent blocks; this may happen due to the merge in indexing
		for (i = 1; i < numChunks; ++i)
			if (!less64(chunksOff[i - 1].v, chunksOff[i].u)) chunksOff[i - 1].v = chunksOff[i].u;

		// Merge adjacent blocks
		for (i = 1, l = 0; i < numChunks; ++i) {
			if (chunksOff[l].v >> 16 == chunksOff[i].u >> 16) chunksOff[l].v = chunksOff[i].v;
			else {
				++l;
				chunksOff[l].u = chunksOff[i].u;
				chunksOff[l].v = chunksOff[i].v;
			}
		}
		numChunks = l + 1;

		// Create new array and move merged chunks there
		TPair64[] mergedChunks = new TPair64[numChunks];
		for (i = 0; i < numChunks; ++i)
			mergedChunks[i] = new TPair64(chunksOff[i].u, chunksOff[i].v);

		// Create an iterator to read the chunks
		return new TabixReader.TabixIterator(tid, beg, end, mergedChunks);
	}

	/**
	 * Return an iterator for the interval in this query
	 * Format: "chr:star-end"
	 */
	public TabixIterator query(String reg) {
		// Parse to an array: [tid, begin, end]. Note 'tid' means chromosome ID
		int[] x = parseReg(reg);
		tabixIterator = query(x[0], x[1], x[2]);
		return tabixIterator;
	};

	/**
	 * Return an iterator for the interval in this query
	 */
	public TabixIterator query(Variant variant) {
		int tid = chr2tid(variant.getChromosomeName());

		// Tabix uses zero-based, half-open coordinates whereas
		// marker has zero-based, closed coordinates.
		int start = variant.getStart();
		int end = variant.getEnd() + 1;
		if (variant.isIns()) start--;

		TabixIterator tabixIterator = query(tid, start, end);
		if (debug) Gpr.debug("Query: " + variant + "\ttabixIterator: " + tabixIterator);
		return tabixIterator;
	}

	/**
	 * Read the Tabix index from the default file.
	 */
	public void readIndex() throws IOException {
		readIndex(new File(fileName + ".tbi"));
	}

	/**
	 * Read the Tabix index from a file
	 *
	 * @param fp File pointer
	 */
	public void readIndex(final File fp) throws IOException {
		if (fp == null) return;
		BlockCompressedInputStream is = new BlockCompressedInputStream(fp);
		byte[] buf = new byte[4];

		is.read(buf, 0, 4); // read "TBI\1"
		int numSeqs = readInt(is);

		sequenceNames = new String[numSeqs]; // # sequences
		sequenceName2tid = new HashMap<String, Integer>();
		mPreset = readInt(is);
		mSc = readInt(is);
		mBc = readInt(is);
		mEc = readInt(is);
		mMeta = readInt(is);
		mSkip = readInt(is);

		if (debug) Gpr.debug("Tabix index:" //
				+ "\n\tNumber of sequences: " + numSeqs //
				+ "\n\tmPreset: " + mPreset //
				+ "\n\tmSc    : " + mSc //
				+ "\n\tmBc    : " + mBc //
				+ "\n\tmEc    : " + mEc //
				+ "\n\tmMeta  : " + mMeta //
				+ "\n\tmSkip  : " + mSkip //
		);

		// Read sequence dictionary
		//int i, j, tid;
		int sequencesLength = readInt(is);
		buf = new byte[sequencesLength];
		is.read(buf);

		// Chromosome names (sequence name) are stored as '\0' delimited
		// string (C style). Each chromosome name is assigned a 'tid'
		// (an integer number) which is implied by the order in which
		// they are stored
		// The mappings 'tid -> sequenceName' and 'sequenceName -> tid' are
		// created here
		for (int i = 0, j = 0, tid = 0; i < buf.length; ++i) {
			if (buf[i] == 0) {
				byte[] b = new byte[i - j];
				System.arraycopy(buf, j, b, 0, b.length);
				String chrName = new String(b);

				// Add chromosome mapping
				if (debug) Gpr.debug("sequenceNames[" + tid + "] = s: '" + chrName + "'");
				sequenceName2tid.put(chrName, tid);
				sequenceName2tid.put(Chromosome.simpleName(chrName), tid); // Add simple name as well

				sequenceNames[tid++] = chrName;
				j = i + 1;
			}
		}

		// Read the index for each chromosome (sequenceName)
		// Note: Not all bins are stored
		tabixIndexes = new TIndex[sequenceNames.length];
		for (int seqNum = 0; seqNum < sequenceNames.length; ++seqNum) {
			// How many 'bins' in this TIndex?
			int numBins = readInt(is);
			if (debug) Gpr.debug("Sequence: " + sequenceNames[seqNum] + "\tbin number: " + seqNum + "\tn_bin: " + numBins);

			tabixIndexes[seqNum] = new TIndex();
			tabixIndexes[seqNum].binningIndex = new HashMap<Integer, TPair64[]>();

			// Load each bin
			for (int j = 0; j < numBins; ++j) {
				int bin = readInt(is); // Bin number
				int numChunks = readInt(is); // How many 'chunks' in this bin?

				TPair64[] chunks = new TPair64[numChunks];
				if (debug) Gpr.debug("\tbin: " + bin + "\tnumChunks: " + numChunks);

				for (int tid = 0; tid < chunks.length; ++tid) {
					long u = readLong(is);
					long v = readLong(is);
					chunks[tid] = new TPair64(u, v); // in C, this is inefficient
					if (debug) Gpr.debug("\t\tchunk[" + tid + "]: " + chunks[tid]);
				}
				tabixIndexes[seqNum].binningIndex.put(bin, chunks);
			}

			// Load linear index
			int linearIndexLen = readInt(is);
			tabixIndexes[seqNum].linearIndex = new long[linearIndexLen];
			for (int tid = 0; tid < tabixIndexes[seqNum].linearIndex.length; ++tid) {
				tabixIndexes[seqNum].linearIndex[tid] = readLong(is);
				if (debug) Gpr.debug("\tlinearIndex[" + tid + "] :" + tabixIndexes[seqNum].linearIndex[tid]);
			}

			if (debug) {
				//Gpr.debug("Index " + sequenceNames[seqNum] + " (tid=" + seqNum + "):\n" + tabixIndexes[seqNum]);
				String txtfile = fileName + "." + sequenceNames[seqNum] + ".txt";
				Gpr.debug("Writing to file " + txtfile);
				Gpr.toFile(txtfile, tabixIndexes[seqNum]);
			}
		}

		// Close
		is.close();
	}

	/**
	 * Read one line from the data file.
	 */
	public String readLine() throws IOException {
		return readLine(fileInputStream);
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}
}
