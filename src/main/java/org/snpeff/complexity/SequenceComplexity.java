package org.snpeff.complexity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

/**
 * Measures the complexity of a sequence
 * 
 * Ideally we'd like to measure the Kolmogorov complexity of the sequence. Unfortunately
 * it is not a computable function (see http://en.wikipedia.org/wiki/Kolmogorov_complexity)
 * 
 * @author pcingola
 *
 */
public class SequenceComplexity {

	HashMap<Integer, Integer> minCompressedLength; // Compressed length of a trivial string length 'N'

	public SequenceComplexity() {
		minCompressedLength = new HashMap<Integer, Integer>();
	}

	/**
	 * Measure string's complexity
	 */
	public int complexity(String seq) {
		int len = seq.length();
		int comppressLen = compressedLength(seq) - minCompressedLength(len);
		// Complexity cannot be more than sequence's length
		int complexity = Math.min(comppressLen, len);
		return complexity;
	}

	/**
	 * Measure string's length after compression
	 */
	int compressedLength(String seq) {
		ByteArrayOutputStream os = new ByteArrayOutputStream(seq.length());
		try {
			GZIPOutputStream gz = new GZIPOutputStream(os);
			gz.write(seq.getBytes());
			gz.close();
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		return os.toByteArray().length;
	}

	/**
	 * Calculate minimum compressed length of a string length 'n'
	 * I.e. create a trivial string of length 'n',compress it and return length
	 * 
	 * @param n
	 * @return
	 */
	int minCompressedLength(int n) {
		Integer mcl = minCompressedLength.get(n);

		// Not cached? => Calculate it
		if( mcl == null ) {
			// Create a trivial string length 'n'
			StringBuilder sb = new StringBuilder();
			for( int i = 0; i < n; i++ )
				sb.append('a');

			// Calculate compressed length
			mcl = compressedLength(sb.toString());

			minCompressedLength.put(n, mcl); // Cache it
		}

		return mcl;
	}

	/**
	 * Measure string's complexity ratio. 
	 * I.e. complexity(seq) / seq.length
	 */
	public double ratio(String seq) {
		int measure = complexity(seq);
		double ratio = ((double) measure) / ((double) seq.length());
		return ratio;

	}
}
