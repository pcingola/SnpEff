package org.snpeff.ped;

import org.snpeff.util.GprSeq;

/**
 * A Simple genotype implementation for PED files
 * 
 * @author pcingola
 */
public class PedGenotype {

	String genotypes[];
	String chrName;
	int position;

	public PedGenotype(String genotypes[]) {
		this.genotypes = genotypes;
	}

	public PedGenotype(String genotypes[], String chrName, int position) {
		this.genotypes = genotypes;
		this.chrName = chrName;
		this.position = position;
	}

	public String get(int idx) {
		return genotypes[idx];
	}

	public String[] getGenotypes() {
		return genotypes;
	}

	public int getPosition() {
		return position;
	}

	/**
	 * Is 'genotype' in this one?
	 * @param genotype
	 * @return
	 */
	public boolean has(String genotype) {
		for (String gen : genotypes)
			if (gen.equals(genotype)) return true;
		return false;
	}

	/**
	 * Is this homozygous?
	 * @return
	 */
	public boolean isHomozygous() {
		for (int i = 1; i < genotypes.length; i++)
			if (genotypes[i] != genotypes[i - 1]) return false;
		return true;
	}

	public boolean isValid() {
		for (String g : genotypes)
			if (g.equalsIgnoreCase("x") || g.equalsIgnoreCase("0")) return false;
		return true;
	}

	public int size() {
		return genotypes.length;
	}

	@Override
	public String toString() {
		return toString(false);
	}

	public String toString(boolean wcComplement) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < genotypes.length; i++) {
			if (genotypes[i].isEmpty()) sb.append(".");
			else if (wcComplement) sb.append(GprSeq.wc(genotypes[i]));
			else sb.append(genotypes[i]);

			if (i < genotypes.length - 1) sb.append("/");
		}
		return sb.toString();
	}
}
