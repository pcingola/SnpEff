package org.snpeff.genotypes;

import java.io.Serializable;

import org.snpeff.vcf.VcfGenotype;

/**
 * A vector of genotypes in a 'compact' structure
 * 
 * Note: Genotypes 0/0, 0/1, 1/0, 1/1 are stored in 2 bits.
 * WARNIGN: Other genotypes are ignored or silently converted to 0/0
 *  
 * @author pcingola
 */
public class GenotypeVector implements Serializable {

	private static final long serialVersionUID = 4734574592894281057L;

	public static final byte mask[] = { 3, 3, 12, 12, 48, 48, (byte) 192, (byte) 192 };
	public static final byte reverseMask[] = { ~3, ~3, ~12, ~12, ~48, ~48, ~(byte) 192, ~(byte) 192 };
	int size; // Size in elements (genotypes)
	byte genotype[];

	public GenotypeVector(int size) {
		this.size = size;
		genotype = new byte[pos2byte(size) + 1];

		for (int i = 0; i < genotype.length; i++)
			genotype[i] = 0;
	}

	public int get(int sampleNum) {
		int idx = pos2byte(sampleNum);
		int bitIdx = pos2bit(sampleNum);
		return (genotype[idx] >> bitIdx) & 0x3;
	}

	/**
	 * Bit number to use
	 */
	int pos2bit(int pos) {
		return (pos & 0x03) << 1;
	}

	/**
	 * Bit mask
	 */
	int pos2bitMask(int pos) {
		return mask[(pos & 0x03)];
	}

	/**
	 * Convert position to byte number
	 */
	int pos2byte(int pos) {
		return pos / 4;
	}

	/**
	 * Set genotype code
	 * 
	 * Codes {0, 1, 2, 3} => Genotypes { 0/0, 0/1, 1/0, 1/1 }
	 * 
	 * 
	 * @param sampleNum
	 * @param code
	 */
	public void set(int sampleNum, int code) {
		int idx = pos2byte(sampleNum);
		int bitIdx = pos2bit(sampleNum);
		genotype[idx] = (byte) ((genotype[idx] & reverseMask[bitIdx]) | ((0x3 & code) << pos2bit(sampleNum)));
	}

	/**
	 * Set genotype
	 * @param sampleNum
	 * @param vg
	 */
	public void set(int sampleNum, VcfGenotype vg) {
		int code = vg.getGenotypeCode();
		if (code < 0) code = 0;
		set(sampleNum, code);
	}

	public int size() {
		return size;
	}
}
