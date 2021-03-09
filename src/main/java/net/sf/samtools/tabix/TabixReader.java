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
import java.util.Arrays;
import java.util.HashMap;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Variant;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import htsjdk.samtools.util.BlockCompressedInputStream;

public class TabixReader implements Iterable<String> {

	private static int MAX_BIN = 37450; // Maximum possible number of bins

	boolean debug = false;
	private String fileName;
	private BlockCompressedInputStream fileInputStream;
	private int mPreset;
	private int mSc;
	private int mBc;
	private int mEc;
	private int mMeta;
	private int mSkip;
	private String[] sequenceNames;
	private HashMap<String, Integer> sequenceName2tid;
	private TabixIndex[] tabixIndexes;
	private TabixIterator tabixIterator;
	protected boolean showHeader;

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
				TabixIterator iter = tr.query(args[1]); // get the iterator
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

	public static long readLong(final InputStream is) throws IOException {
		byte[] buf = new byte[8];
		is.read(buf);
		return ByteBuffer.wrap(buf).order(ByteOrder.LITTLE_ENDIAN).getLong();
	}

	/**
	 * Bins span different sizes depending on their levels: Bins Size (sl)
	 * ------------------------ 0 512Mb 2^29 1-8 64Mb 2^26 9-72 8Mb 2^23 73-584 1Mb
	 * 2^20 585-4680 128kb 2^17 4681-37449 16kb 2^14
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

	public TabixReader(String fileName) throws IOException {
		this(fileName, false);
	}

	public TabixReader(String fileName, boolean debug) throws IOException {
		this.fileName = fileName;
		this.debug = debug;
		fileInputStream = new BlockCompressedInputStream(new File(fileName));
		readIndex();
	}

	protected int chr2tid(String chr) {
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

	public long getFilePointer() {
		return fileInputStream.getFilePointer();
	}

	public int getmBc() {
		return mBc;
	}

	public int getmEc() {
		return mEc;
	}

	public int getmMeta() {
		return mMeta;
	}

	public int getmPreset() {
		return mPreset;
	}

	public int getmSc() {
		return mSc;
	}

	@Override
	public TabixIterator iterator() {
		if (tabixIterator != null) return tabixIterator;

		// No query performed yet: create one
		TPair64 off[] = new TPair64[1];
		off[0] = new TPair64(0, Long.MAX_VALUE);
		tabixIterator = new TabixIterator(this, -1, 0, Integer.MAX_VALUE, off);
		tabixIterator.setShowHeader(showHeader); // Do we want header lines?

		return tabixIterator;
	}

	/**
	 * Parse a region in the format of "chr1", "chr1:100" or "chr1:100-1000"
	 *
	 * @param reg
	 *            Region string
	 * @return An array where the three elements are [ sequence_id, region_begin,
	 *         region_end] On failure, sequence_id==-1.
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
	};

	/**
	 * Query for a given interval:
	 *
	 * @param tid
	 *            : Chromosome number
	 * @param beg
	 *            : Interval start
	 * @param end
	 *            : Interval end
	 * @return
	 */
	private TabixIterator query(final int tid, final int beg, final int end) {
		TPair64[] chunksOff, chunks;
		long minFileOffset;

		if (tid < 0) return null;

		// Get index for chromosome 'tid'
		TabixIndex idx = tabixIndexes[tid];
		int[] bins = new int[MAX_BIN];
		int i, l, numChunks;

		// Fill up 'bins' with the bin numbers that are intersected by [beg, end)
		// interval.
		int numBins = reg2bins(beg, end, bins);

		// Minimum offset within file
		minFileOffset = idx.minOffset(beg);
		if (debug) Log.debug("minFileOffset: " + minFileOffset);

		// Add chunk lengths for all blocks within the interval
		for (i = numChunks = 0; i < numBins; ++i) {
			chunks = idx.get(bins[i]);
			if (chunks != null) {
				numChunks += chunks.length;
				if (debug) {
					StringBuilder sb = new StringBuilder();
					for (TPair64 tp : chunks)
						sb.append("\t\t" + tp + "\n");
					Log.debug("\tnumChunks: " + numChunks + "\n" + sb);
				}
			}
		}

		// Zero chunks? Then there is nothing in the index matching the interval
		if (numChunks == 0) return null;

		// Collect all chunks having their end coordinated after min_off
		chunksOff = new TPair64[numChunks];
		for (i = numChunks = 0; i < numBins; ++i) {
			chunks = idx.get(bins[i]);
			if (chunks != null) {
				for (int j = 0; j < chunks.length; ++j) {
					// Chunk ends after minFileOffset? => Create a copy and add it to the array
					// If chunk is before minFileOffset, so it is filtered out
					if (TPair64.less64(minFileOffset, chunks[j].v)) {
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
			if (TPair64.less64(chunksOff[l].v, chunksOff[i].v)) {
				++l;
				chunksOff[l].u = chunksOff[i].u;
				chunksOff[l].v = chunksOff[i].v;
			}
		}
		numChunks = l + 1;

		// Resolve overlaps between adjacent blocks; this may happen due to the merge in
		// indexing
		for (i = 1; i < numChunks; ++i)
			if (!TPair64.less64(chunksOff[i - 1].v, chunksOff[i].u)) chunksOff[i - 1].v = chunksOff[i].u;

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

		return new TabixIterator(this, tid, beg, end, mergedChunks);
	}

	/**
	 * Return an iterator for the interval in this query Format: "chr:star-end"
	 */
	public TabixIterator query(String reg) {
		// Parse to an array: [tid, begin, end]. Note 'tid' means chromosome ID
		int[] x = parseReg(reg);
		tabixIterator = query(x[0], x[1], x[2]);
		return tabixIterator;
	}

	public TabixIterator query(Variant variant) {
		int tid = chr2tid(variant.getChromosomeName());

		// Tabix uses zero-based, half-open coordinates whereas
		// marker has zero-based, closed coordinates.
		int start = variant.getStart();
		int end = variant.getEnd() + 1;
		if (variant.isIns()) start--;

		TabixIterator tabixIterator = query(tid, start, end);
		if (tabixIterator != null) tabixIterator.setDebug(debug);
		if (debug) Log.debug("Query: " + variant + "\ttabixIterator: " + tabixIterator);
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
	 * @param fp
	 *            File pointer
	 */
	public void readIndex(File fp) throws IOException {
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

		if (debug) Log.debug("Tabix index:" //
				+ "\n\tNumber of sequences: " + numSeqs //
				+ "\n\tmPreset: " + mPreset //
				+ "\n\tmSc    : " + mSc //
				+ "\n\tmBc    : " + mBc //
				+ "\n\tmEc    : " + mEc //
				+ "\n\tmMeta  : " + mMeta //
				+ "\n\tmSkip  : " + mSkip //
		);

		// Read sequence dictionary
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
				if (debug) Log.debug("sequenceNames[" + tid + "] = s: '" + chrName + "'");
				sequenceName2tid.put(chrName, tid);
				sequenceName2tid.put(Chromosome.simpleName(chrName), tid); // Add simple name as well

				sequenceNames[tid++] = chrName;
				j = i + 1;
			}
		}

		// Read the index for each chromosome (sequenceName)
		// Note: Not all bins are stored
		tabixIndexes = new TabixIndex[sequenceNames.length];
		for (int seqNum = 0; seqNum < sequenceNames.length; ++seqNum) {
			TabixIndex tabixIndex = new TabixIndex();
			tabixIndex.setDebug(debug);
			tabixIndex.readIndex(is);
			tabixIndexes[seqNum] = tabixIndex;

			if (debug) {
				String txtfile = fileName + ".tabixIndex_" + sequenceNames[seqNum] + ".txt";
				Log.debug("Writing to file " + txtfile);
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
		StringBuffer buf = new StringBuffer();
		int c;
		while ((c = fileInputStream.read()) >= 0 && c != '\n')
			buf.append((char) c);
		if (c < 0) return null;
		return buf.toString();
	}

	void seek(long pos) throws IOException {
		if (debug) Log.debug("seek(" + pos + ")");
		fileInputStream.seek(pos);
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tabixIndexes.length; i++)
			sb.append("TabixIndex[" + i + "]:\t" + tabixIndexes[i] + "\n");
		return sb.toString();
	}
}
