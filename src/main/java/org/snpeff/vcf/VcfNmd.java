package org.snpeff.vcf;

import org.snpeff.interval.Gene;

/**
 * An 'NMD' entry in a vcf line
 *
 * @author pablocingolani
 */
public class VcfNmd extends VcfLof {

	/**
	 * Convert from field name to field number
	 */
	public static int fieldNum(String name) {
		int fieldNum = 0;

		if (name.equals("NMD.GENE")) return fieldNum;
		fieldNum++;

		if (name.equals("NMD.GENEID")) return fieldNum;
		fieldNum++;

		if (name.equals("NMD.NUMTR")) return fieldNum;
		fieldNum++;

		if (name.equals("NMD.PERC")) return fieldNum;
		fieldNum++;

		return -1;
	}

	public VcfNmd(Gene gene, double percentAffected) {
		super(gene, percentAffected);
	}

	public VcfNmd(String nmdStr) {
		super(nmdStr);
	}

	public VcfNmd(String geneName, String geneId, int numTranscripts, double percentAffected) {
		super(geneName, geneId, numTranscripts, percentAffected);
	}

	public VcfNmd(VcfEntry vcfEntry, String nmdStr) {
		super(vcfEntry, nmdStr);
	}

}
