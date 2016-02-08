package org.snpeff.stats;

/**
 * Observed over expected values (o/e) of CHH in a sequence 
 * 
 * @author pcingola
 *
 */
public class ObservedOverExpectedCHH extends ObservedOverExpected {

	@Override
	public double expected(String sequence) {
		return sequence.length() * 0.140625; // P[ base(i) = C ] * P[ base(i+1) != G] * P[ base(i+2) != G] = 1/4 * 3/4 * 3/4 = 0.140625
	}

	/**
	 * Count the number of CHH in this sequence
	 * @param sequence
	 * @return
	 */
	@Override
	public int observed(String sequence) {
		int count = 0;

		// Count CHH in this sequence
		char bases[] = sequence.toUpperCase().toCharArray();
		for( int i = 0; i < bases.length - 2; i++ )
			if( (bases[i] == 'C') && (bases[i + 1] != 'G') && (bases[i + 2] != 'G') ) count++;

		return count;
	}

}
