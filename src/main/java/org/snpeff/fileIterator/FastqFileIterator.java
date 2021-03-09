package org.snpeff.fileIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

import org.snpeff.fastq.Fastq;
import org.snpeff.fastq.FastqBuilder;
import org.snpeff.fastq.FastqVariant;
import org.snpeff.util.Log;

/**
 * Opens a fastq file and iterates over all fastq sequences in the file
 * Unlike BioJava's version, this one does NOT load all sequences in
 * memory. Thus it allows to process much larger files
 *
 * @author pcingola
 */
public class FastqFileIterator implements Iterable<Fastq>, Iterator<Fastq> {

	private static enum State {
		DESCRIPTION, // Description parser state.
		SEQUENCE, // Sequence parser state.
		REPEAT_DESCRIPTION, // Repeat description parser state.
		QUALITY, // Quality score parser state.
		COMPLETE; // Complete parser state.
	}

	public static boolean debug = false;

	FastqBuilder fastqBuilder;
	BufferedReader reader;
	boolean seqReady = false;
	String line;
	int lineNum;

	public FastqFileIterator(InputStream inStream, FastqVariant variant) {
		fastqBuilder = new FastqBuilder();
		fastqBuilder.withVariant(variant);
		line = null;
		lineNum = 0;
		reader = null;
		reader = new BufferedReader(new InputStreamReader(inStream));
	}

	public FastqFileIterator(String fastqFileName) {
		fastqBuilder = new FastqBuilder();
		fastqBuilder.withVariant(FastqVariant.FASTQ_SANGER);
		line = null;
		lineNum = 0;
		reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fastqFileName))));
		} catch (FileNotFoundException e) {
		}
	}

	public FastqFileIterator(String fastqFileName, FastqVariant variant) {
		fastqBuilder = new FastqBuilder();
		fastqBuilder.withVariant(variant);
		line = null;
		lineNum = 0;
		reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(fastqFileName))));
		} catch (FileNotFoundException e) {
		}
	}

	/**
	 * Close file
	 */
	public void close() {
		try {
			if (reader != null) reader.close();
		} catch (IOException e) {
		}

		reader = null;
		line = null;
		lineNum = 0;
		seqReady = false;
	}

	public int getLineNum() {
		return lineNum;
	}

	@Override
	public boolean hasNext() {
		if (reader == null) return false;// No input stream?

		if (!seqReady) {
			seqReady = readSeq(); // Try reading a sequence.

			// End of file or any problem? => Close file
			if (seqReady == false) close();
		}

		return seqReady;
	}

	@Override
	public Iterator<Fastq> iterator() {
		return this;
	}

	@Override
	public Fastq next() {
		if (hasNext()) {
			seqReady = false;
			Fastq fastq = fastqBuilder.build();
			return fastq;
		}
		return null;
	}

	/**
	 * Read a sequence from the file
	 * @return
	 */
	boolean readSeq() {
		try {
			State state = State.DESCRIPTION;
			while (reader.ready()) {
				if (line == null) {
					line = reader.readLine(); // Read a line (only if needed)
					lineNum++;
				}

				if (debug) Log.debug("State:" + state + "\tLine " + lineNum + ": " + line);
				switch (state) {
				case DESCRIPTION:
					if (line.startsWith("@")) {
						fastqBuilder.withDescription(line.trim());
						state = State.SEQUENCE;
					}
					break;
				case SEQUENCE:
					fastqBuilder.withSequence(line.trim());
					state = State.REPEAT_DESCRIPTION;
					break;
				case REPEAT_DESCRIPTION:
					if (line.startsWith("+")) state = State.QUALITY;
					else fastqBuilder.appendSequence(line.trim());
					break;
				case QUALITY:
					fastqBuilder.withQuality(line.trim());
					state = State.COMPLETE;
					break;
				case COMPLETE:
					if (line.startsWith("@")) {
						state = State.COMPLETE;
						return true; // We finished reading this sequence
					} else fastqBuilder.appendQuality(line.trim());
					break;
				default:
					throw new RuntimeException("Unkown state '" + state + "'");
				}

				line = null;
			}

			if (state == State.COMPLETE) return true;

		} catch (IOException e) {
			return false;
		}

		// Not finished reading a sequence
		return false;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented");
	}

}
