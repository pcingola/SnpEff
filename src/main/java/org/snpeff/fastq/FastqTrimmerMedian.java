package org.snpeff.fastq;

import java.util.Collections;
import java.util.LinkedList;

/**
 * Trim fastq sequence when median quality drops below a threshold
 * 
 * @author pcingola
 *
 */
public class FastqTrimmerMedian extends FastqTrimmer {

	int runningMedianLength; // How many bases do we consider for calculating the median 

	public FastqTrimmerMedian(int qualityThreshold, int minBases, int runningMedianLength) {
		super(qualityThreshold, minBases);
		this.runningMedianLength = runningMedianLength;
	}

	/**
	 * Calculate the running median
	 * @return
	 */
	int median(int qual[], int i) {
		// Create a list
		LinkedList<Integer> list = new LinkedList<Integer>();
		for( int k = 0, j = i - runningMedianLength + 1; k < runningMedianLength; k++, j++ )
			if( (j >= 0) && (j < qual.length) ) list.add(qual[j]);
		Collections.sort(list);

		int size = list.size(), med = 0;
		if( list.size() % 2 == 1 ) med = list.get(size / 2);
		else med = (list.get(size / 2 - 1) + list.get(size / 2)) / 2;

		return med;
	}

	/**
	 * Return index where the sequence should be trimmed
	 * First time quality median drops below 'qualityThreshold'
	 */
	@Override
	int trimIndex(Fastq fastq) {
		int qual[] = FastqTools.qualtityArray(fastq);

		for( int i = 0; i < qual.length; i++ ) {
			int median = median(qual, i); // Compute running median
			if( median < qualityThreshold ) return i; // Below threshold? => trim here
		}

		return qual.length;
	}
}
