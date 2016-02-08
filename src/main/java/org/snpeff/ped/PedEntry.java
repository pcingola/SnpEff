package org.snpeff.ped;

import java.util.Collection;
import java.util.Iterator;

/**
 * An entry in a PED table.
 * I.e. a line in a PED file (PLINK)
 * 
 * @author pcingola
 */
public class PedEntry extends TfamEntry implements Iterable<PedGenotype> {

	PlinkMap plinkMap;
	String genotypes[];

	public PedEntry(PlinkMap plinkMap, String line) {
		super(line);
		this.plinkMap = plinkMap;
	}

	public PedEntry(PlinkMap plinkMap, String familyId, String id, String fatherId, String motherId, Sex sex, double phenotype, String genotypes[]) {
		super(familyId, id, fatherId, motherId, sex, phenotype);
		this.plinkMap = plinkMap;
		this.genotypes = genotypes;
	}

	/**
	 * Number of phenotypes available 
	 * @return
	 */
	public int countGenotypes() {
		int count = 0;
		for (PedGenotype gen : this)
			if (gen.isValid()) count++;
		return count;
	}

	String genoStr(String geno) {
		if (geno.equals("x") || geno.equals("0")) return "";
		return geno;
	}

	/**
	 * Get genotype
	 * WARNING: Empty string means that no genotpye is available
	 * 
	 * @param idx
	 * @return
	 */
	public PedGenotype getGenotype(int idx) {
		String geno[] = new String[2];
		geno[0] = genoStr(genotypes[idx * 2]);
		geno[1] = genoStr(genotypes[idx * 2 + 1]);
		return new PedGenotype(geno, plinkMap.getChrName(idx), plinkMap.getPosition(idx));
	}

	/**
	 * Get phenotype by String ID
	 * @param idStr
	 * @return
	 */
	public PedGenotype getGenotype(String idStr) {
		Integer idxInt = plinkMap.getGenotypeNames(idStr);
		if (idxInt == null) return null;
		return getGenotype(idxInt);
	}

	/**
	 * Get all genotype names 
	 * WARNING: the returned string collection is unsorted!
	 * @return
	 */
	public Collection<String> getGenotypeNames() {
		return plinkMap.getGenotypeNames();
	}

	public String[] getGenotypes() {
		return genotypes;
	}

	@Override
	public Iterator<PedGenotype> iterator() {
		return new Iterator<PedGenotype>() {

			int i = 0;
			int max = size();

			@Override
			public boolean hasNext() {
				return i < max;
			}

			@Override
			public PedGenotype next() {
				return getGenotype(i++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Unsupported! Go away and die!");
			}
		};
	}

	@Override
	protected void parse(String line) {
		String fields[] = line.split("\\s", -1);
		parse(fields);
	}

	@Override
	protected int parse(String fields[]) {
		int fieldNum = super.parse(fields);

		// Phenotypes
		String genotypes[] = new String[fields.length - fieldNum];
		for (int j = 0; fieldNum < fields.length; fieldNum++, j++)
			genotypes[j] = fields[fieldNum];

		return fieldNum;
	}

	/**
	 * Number of phenotypes
	 * @return
	 */
	public int size() {
		return genotypes.length / 2;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(super.toString() + "\t");

		int len = genotypes.length / 2;
		for (int i = 0; i < len; i += 2)
			sb.append(genotypes[i] + "/" + genotypes[i + 1] + "\t");

		return sb.toString();
	}
}
