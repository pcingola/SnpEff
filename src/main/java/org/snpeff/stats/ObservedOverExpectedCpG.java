package org.snpeff.stats;

/**
 * Observed over expected values (o/e) of CpG in a sequence 
 * 
 * @author pcingola
 *
 */
public class ObservedOverExpectedCpG extends ObservedOverExpected {

	@Override
	public double expected(String sequence) {
		return sequence.length() / 16.0; // P[ base(i) = C ] * P[ base(i+1) = G] is 1/16
	}

	/**
	 * Count the number of CpG in this sequence
	 * @param sequence
	 * @return
	 */
	@Override
	public int observed(String sequence) {
		char seq[] = sequence.toUpperCase().toCharArray();
		int cpg = 0;
		for( int pos = 0; pos < (seq.length - 1); pos++ )
			if( (seq[pos] == 'C') && (seq[pos + 1] == 'G') ) cpg++;

		return cpg;
	}

}
