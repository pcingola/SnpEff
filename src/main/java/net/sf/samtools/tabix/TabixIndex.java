package net.sf.samtools.tabix;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.snpeff.util.Log;

/**
 * Tabix Index (i.e. the structure stored in *.tbi file)
 *
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
public class TabixIndex {

	public static final int TAD_LIDX_SHIFT = 14; // Minimum bin size is 2^TAD_LIDX_SHIFT = 2^14 = 16KB

	boolean debug;
	private HashMap<Integer, TPair64[]> binningIndex; // Binning index
	private long[] linearIndex; // Linear index

	public static String binInfo(int binNumber) {
		int binLevel = (int) Math.floor((Math.log(7 * binNumber + 1) / (3 * Math.log(2.0))));
		int binSize = 1 << (29 - 3 * binLevel);
		int offsetLevel = ((1 << (3 * binLevel)) - 1) / 7;

		int start = (binNumber - offsetLevel) * binSize;
		int end = (binNumber + 1 - offsetLevel) * binSize;

		return "bin: " + binNumber //
				+ ", level: " + binLevel //
				+ ", size: " + binSize //
				+ ", offset: " + offsetLevel //
				+ ", interval: [ " + start + " , " + end + " )" //
		;
	}

	public TabixIndex() {
		binningIndex = new HashMap<Integer, TPair64[]>();
	}

	public TPair64[] get(int binNum) {
		return binningIndex.get(binNum);
	}

	public long minOffset(int beg) {
		// Minimum offset within file
		// Linear index has the offset of the smallest start coordinate that
		// overlaps the each 16KB window (i.e. all possible lowest level bins)
		if (linearIndex.length > 0) {
			int begTad = beg >> TAD_LIDX_SHIFT;
			if (begTad >= linearIndex.length) return linearIndex[linearIndex.length - 1]; // Pick last position in linear index
			else return linearIndex[begTad]; // Use linear index
		}

		return 0;
	}

	public void put(int bin, TPair64[] chunks) {
		binningIndex.put(bin, chunks);
	}

	public void readIndex(InputStream is) throws IOException {
		int numBins = TabixReader.readInt(is);
		if (debug) Log.debug("Number of bins: " + numBins);

		// Load each bin
		for (int j = 0; j < numBins; ++j) {
			int binNumber = TabixReader.readInt(is); // Bin number
			int numChunks = TabixReader.readInt(is); // How many 'chunks' in this bin?

			TPair64[] chunks = new TPair64[numChunks];
			if (debug) Log.debug("\t" + binInfo(binNumber) + "\tnumChunks: " + numChunks);

			for (int chunNum = 0; chunNum < chunks.length; ++chunNum) {
				TPair64 tp = new TPair64();
				tp.readIndex(is);
				chunks[chunNum] = tp;
				if (debug) Log.debug("\t\tchunk[" + chunNum + "]: " + chunks[chunNum]);
			}

			put(binNumber, chunks);
		}

		// Load linear index
		int linearIndexLen = TabixReader.readInt(is);
		long[] linearIndex = new long[linearIndexLen];
		for (int tid = 0; tid < linearIndex.length; ++tid) {
			linearIndex[tid] = TabixReader.readLong(is);
			if (debug) Log.debug("\tlinearIndex[" + tid + "] :" + linearIndex[tid]);
		}
		setLinearIndex(linearIndex);

	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setLinearIndex(long[] linearIndex) {
		this.linearIndex = linearIndex;
	}

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
