package org.snpeff.fastq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.snpeff.snpEffect.commandLine.CommandLine;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Split a fastq into N files
 *
 * @author pablocingolani
 */
public class FastqSplit implements CommandLine {

	public static final long K = 1024L;
	public static final long M = K * K;
	public static final long G = K * M;
	public static final long T = K * G;
	public static final long P = K * T;

	public static final int BUFFER_SIZE = (int) (10 * M);

	boolean verbose = false;
	byte buffer[];
	String fastqFile;
	String dirName, baseName, ext;
	int numSplits;
	File file;
	RandomAccessFile raf;
	ArrayList<String> splitFileNames;
	String args[];

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		FastqSplit fbp = new FastqSplit();
		fbp.parseArgs(args);
		fbp.run();
	}

	public FastqSplit() {
		buffer = new byte[BUFFER_SIZE];
		splitFileNames = new ArrayList<String>();
	}

	/**
	 * Close files
	 */
	void close() {
		try {
			// Close if already closed yet.
			if (raf != null) {
				raf.close();
				raf = null;
			}
			file = null;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	void error(String message) {
		System.err.println("Error: " + message + "\n");
		System.exit(-1);
	}

	/**
	 * File size as a human readable string
	 * @param s
	 * @return
	 */
	String fileSizeStr(long s) {
		if (s < K) return String.format("%dbytes", s);
		if (s < M) return String.format("%.1fKb", (1.0 * s / K));
		if (s < G) return String.format("%.1fMb", (1.0 * s / M));
		if (s < T) return String.format("%.1fGb", (1.0 * s / G));
		if (s < P) return String.format("%.1fTb", (1.0 * s / T));
		return String.format("%.1fPb", (1.0 * s / P));
	}

	/**
	 * Find next '\n' in a buffer
	 * @param buffer
	 * @param idx
	 * @return
	 */
	int findNl(byte buffer[], int idx) {
		for (int i = idx; i < buffer.length; i++)
			if (buffer[i] == '\n') return i;

		return -1;
	}

	/**
	 * Find the first FASTQ record start at or after position 'pos'
	 * @param pos
	 * @return The first positions after 'pos' having a "\n@" character sequence. '-1' if not found
	 */
	long findRecordStart(long pos) {
		try {
			raf.seek(pos);

			int len = 0;
			for (long p = pos; (len = raf.read(buffer)) > 0; p += len) {
				for (int idx = 0; idx >= 0;) {
					// Find '\n@' in this buffer
					idx = findNl(buffer, idx);
					if (idx >= 0) {
						idx++;
						if ((idx < buffer.length) && (buffer[idx] == '@')) {
							// Is there a '@\n'? => This is probably a record start
							// Note: It may be a quality line that has the first base quality of 31 (i.e. '@' in phred-33 coding)
							if (isRecordStart(buffer, idx)) {
								long recordStart = p + idx;
								return recordStart;
							}
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return -1;
	}

	@Override
	public String[] getArgs() {
		return args;
	}

	public ArrayList<String> getSplitFileNames() {
		return splitFileNames;
	}

	/**
	 * Is this a FASTQ record start?
	 *
	 * @param buffer
	 * @param idx
	 * @return
	 */
	boolean isRecordStart(byte buffer[], int idx) {
		int next = findNl(buffer, idx); // Find next line
		if (next < 0) return false; // Not found? => Fail

		// Did we find a '\n@'? => The previous one was NOT a record start
		next++;
		if ((next < buffer.length) && (buffer[next] != '@')) return true;

		return false;
	}

	/**
	 * Open files
	 */
	void open() {
		// File parameters
		dirName = Gpr.dirName(fastqFile);
		ext = Gpr.extName(fastqFile);
		baseName = Gpr.baseName(fastqFile, "." + ext);

		// Open files
		try {
			file = new File(fastqFile);
			if (!file.canRead()) error("Cannot read file '" + fastqFile + "'");
			raf = new RandomAccessFile(file, "r");
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * Parse command line arguments
	 * @param args
	 */
	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		int i = 0;
		if (args[0].equals("-v")) {
			verbose = true;
			i++;
		}

		if ((args.length - i) != 2) usage(null);

		numSplits = Gpr.parseIntSafe(args[i++]);
		fastqFile = args[i++];

		if (numSplits <= 1) usage("Number of splits should be more than 1.");
	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		open(); // Open files

		// Split size
		long size = file.length();
		if (verbose) Log.info("Splitting file '" + fastqFile + "' into " + numSplits + " parts. File size: " + fileSizeStr(size) + " ( " + size + " bytes).");
		long step = size / numSplits;
		if (step < 0) error("Error: Split file size less than 1 byte!");

		// Create each split
		long start = 0, end = 0;
		for (int i = 0; i < numSplits; i++) {
			start = end; // Next byte
			end = (i + 1) * step;

			// Last split ends at file size or at record end
			if (i == (numSplits - 1)) end = size;
			else end = findRecordStart(end);

			// Perform the split
			split(i, start, end);
		}

		close();
		if (verbose) Log.info("Done.");
		return true;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Create a split of the file by dumping bytes from 'start' to 'end'.
	 * Note: Both start and end are included.
	 *
	 * @param splitNumber
	 * @param start
	 * @param end
	 */
	void split(int splitNumber, long start, long end) {
		// Split file name
		String splitFileName = String.format("%s/%s.%03d.%s", dirName, baseName, splitNumber, ext);
		if ((dirName == null) || dirName.isEmpty()) splitFileName = String.format("%s.%03d.%s", baseName, splitNumber, ext);
		if (verbose) Log.info("Split " + splitNumber + ":\t[ " + start + " , " + end + " ]\t=>\t" + splitFileName);

		try {
			// Open output file
			FileOutputStream out = new FileOutputStream(new File(splitFileName));

			// Seek to start
			raf.seek(start);

			int len = 0;
			for (long p = start; ((len = raf.read(buffer)) > 0) && (p < end); p += len) {
				if ((p + len) > end) len = (int) (end - p); // Adjust end
				out.write(buffer, 0, len); // Write to file
			}

			// Done, close output file
			out.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Usage message
	 * @param message
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("Usage: " + this.getClass().getSimpleName() + " [-v] numSplits file.fastq\nOptions:\n\t-v\t: Verbose\n");
		System.exit(-1);
	}

}
