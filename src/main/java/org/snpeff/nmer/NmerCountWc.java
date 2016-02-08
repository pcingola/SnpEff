package org.snpeff.nmer;

/**
 * Create a counter that can count Nmers as well as their WC complements
 * That means that given an Nmer, the nmer and the Watson-Crick complement are counted the same.
 * 
 * How is done: Given an nmer
 *    - Calculate WC complement: nmerWc = WC(nmer)
 *    - Count the minimum of both nmer.count( min(nmer, nmerWc) ) 
 * 
 * It works with other counters (NmerCountBit, NmerCountByte, NmerCountInt)
 * 
 * @author pcingola
 */
public class NmerCountWc extends NmerCount {

	private static final long serialVersionUID = 1L;
	Nmer nmerWc, nmerMin;

	public NmerCountWc(int nmerSize) {
		super(nmerSize);
		nmerWc = new Nmer(nmerSize);
		nmerMin = new Nmer(nmerSize);
	}

	@Override
	public void count(Nmer nmer) {
		nmerWc.setNmer(nmer.wc()); // Calculate WC complement
		long nmerToCount = Math.min(nmer.getNmer(), nmerWc.getNmer()); // use the minimum
		nmerMin.setNmer(nmerToCount);
		count(nmerMin);
	}
}
