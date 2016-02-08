package org.snpeff.stats;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.util.GprSeq;

/**
 * Observed over expected values (o/e) ratios 
 * E.g.: CpG dinucleotides in a sequence 
 * 
 * @author pcingola
 *
 */
public abstract class ObservedOverExpected {

	/**
	 * Expected number of sequences (average between plus and minus strand)
	 * @param e : Exon
	 * @return
	 */
	public double expected(Exon e) {
		String seq = e.getSequence();
		double eplus = expected(seq);
		double eminus = expected(GprSeq.reverseWc(seq));
		return (eplus + eminus) / 2;
	}

	/**
	 * Expected number of sequences (average between plus and minus strand)
	 * @param t : Transcript
	 * @return
	 */
	public double expected(Gene g) {
		double eplus = 0;
		double eminus = 0;
		for( Transcript t : g ) {
			for( Exon e : t ) {
				String seq = e.getSequence();
				eplus += expected(seq);
				eminus += expected(GprSeq.reverseWc(seq));
			}
		}
		return (eplus + eminus) / 2;
	}

	public abstract double expected(String sequence);

	/**
	 * Expected number of sequences (average between plus and minus strand)
	 * @param t : Transcript
	 * @return
	 */
	public double expected(Transcript t) {
		double eplus = 0;
		double eminus = 0;
		for( Exon e : t ) {
			String seq = e.getSequence();
			eplus += expected(seq);
			eminus += expected(GprSeq.reverseWc(seq));
		}
		return (eplus + eminus) / 2;
	}

	/**
	 * Observed sequence (average between plus and minus strand)
	 * @param e : Exon
	 * @return
	 */
	public int observed(Exon e) {
		String seq = e.getSequence();
		int oplus = observed(seq);
		int ominus = observed(GprSeq.reverseWc(seq));
		return (oplus + ominus) / 2;
	}

	/**
	 * Observed sequence (average between plus and minus strand)
	 * @param g : Gene
	 * @return
	 */
	public int observed(Gene g) {
		int oplus = 0;
		int ominus = 0;
		for( Transcript t : g ) {
			for( Exon e : t ) {
				String seq = e.getSequence();
				oplus += observed(seq);
				ominus += observed(GprSeq.reverseWc(seq));
			}
		}
		return (oplus + ominus) / 2;
	}

	public abstract int observed(String sequence);

	/**
	 * Observed sequence (average between plus and minus strand)
	 * @param t : Transcript
	 * @return
	 */
	public int observed(Transcript t) {
		int oplus = 0;
		int ominus = 0;
		for( Exon e : t ) {
			String seq = e.getSequence();
			oplus += observed(seq);
			ominus += observed(GprSeq.reverseWc(seq));
		}
		return (oplus + ominus) / 2;
	}

	/**
	 * Count the number of CHG in this sequence
	 * @param sequence
	 * @return
	 */
	public int observedChg(String sequence) {
		int count = 0;

		// Count CHH in this sequence
		char bases[] = sequence.toUpperCase().toCharArray();
		for( int i = 0; i < bases.length - 2; i++ )
			if( (bases[i] == 'C') && (bases[i + 1] != 'G') && (bases[i + 2] == 'G') ) count++;

		return count;
	}

	/**
	 * Observed over expected ratio
	 * @param Exon
	 * @return
	 */
	public double oe(Exon e) {
		return observed(e) / expected(e);
	}

	/**
	 * Observed over expected ratio
	 * @param Gene
	 * @return
	 */
	public double oe(Gene g) {
		double exp = expected(g);
		return exp > 0 ? observed(g) / exp : 0;
	}

	/**
	 * Observed over expected ratio
	 * @param sequence
	 * @return
	 */
	public double oe(String sequence) {
		double exp = expected(sequence);
		return exp > 0 ? observed(sequence) / exp : 0;
	}

	/**
	 * Observed over expected ratio
	 * @param Transcript
	 * @return
	 */
	public double oe(Transcript t) {
		double exp = expected(t);
		return exp > 0 ? observed(t) / exp : 0;
	}

}
