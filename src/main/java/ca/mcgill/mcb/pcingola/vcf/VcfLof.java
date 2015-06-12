package ca.mcgill.mcb.pcingola.vcf;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * An 'LOF' entry in a vcf line
 *
 * @author pablocingolani
 */
public class VcfLof {

	String geneName;
	String geneId;
	int numTranscripts;
	double percentAffected;

	/**
	 * Convert from field name to field number
	 */
	public static int fieldNum(String name) {
		int fieldNum = 0;

		if (name.equals("LOF.GENE")) return fieldNum;
		fieldNum++;

		if (name.equals("LOF.GENEID")) return fieldNum;
		fieldNum++;

		if (name.equals("LOF.NUMTR")) return fieldNum;
		fieldNum++;

		if (name.equals("LOF.PERC")) return fieldNum;
		fieldNum++;

		return -1;
	}

	public VcfLof(Gene gene, double percentAffected) {
		geneName = gene.getGeneName();
		geneId = gene.getId();
		numTranscripts = gene.numChilds();
		this.percentAffected = percentAffected;
	}

	public VcfLof(String lofStr) {
		parse(lofStr);
	}

	public VcfLof(String geneName, String geneId, int numTranscripts, double percentAffected) {
		this.geneName = geneName;
		this.geneId = geneId;
		this.numTranscripts = numTranscripts;
		this.percentAffected = percentAffected;
	}

	public String getGeneId() {
		return geneId;
	}

	public String getGeneName() {
		return geneName;
	}

	public int getNumTranscripts() {
		return numTranscripts;
	}

	public double getPercentAffected() {
		return percentAffected;
	}

	void parse(String lof) {
		if (lof.startsWith("(")) lof = lof.substring(1);
		if (lof.endsWith(")")) lof = lof.substring(0, lof.length() - 1);

		String lofFields[] = lof.split("\\|");

		try {
			// Parse each sub field
			int index = 0;

			if ((lofFields.length > index) && !lofFields[index].isEmpty()) geneName = lofFields[index];
			index++;

			if ((lofFields.length > index) && !lofFields[index].isEmpty()) geneId = lofFields[index];
			index++;

			if ((lofFields.length > index) && !lofFields[index].isEmpty()) numTranscripts = Gpr.parseIntSafe(lofFields[index]);
			index++;

			if ((lofFields.length > index) && !lofFields[index].isEmpty()) percentAffected = Gpr.parseDoubleSafe(lofFields[index]);
			index++;

		} catch (Exception e) {
			String fields = "";
			for (int i = 0; i < lofFields.length; i++)
				fields += "\t" + i + " : '" + lofFields[i] + "'\n";
			throw new RuntimeException("Error parsing: '" + lof + "'\n" + fields, e);
		}
	}

	@Override
	public String toString() {
		return String.format("(%s|%s|%d|%.2f)" //
				, VcfEntry.vcfInfoSafe(geneName) //
				, VcfEntry.vcfInfoSafe(geneId) //
				, numTranscripts //
				, percentAffected //
				);
	}
}
