package org.snpeff.geneSets;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;

/**
 * Some statistics about a gene
 * 
 * @author pcingola
 */
public class GeneStats {

	String geneName;
	boolean proteinCoding = false;
	int geneLength = 0;
	int numberOfTranscripts = 0;
	int numberOfExonsTotal = 0;
	int numberOfExonsMax = 0;
	int cdsLengthTotal = 0;
	int cdsLengthMax = 0;
	int aaLengthTotal = 0;
	int aaLengthMax = 0;
	int aaLengthCount = 0;

	public GeneStats() {
	}

	public void add(Gene gene) {
		add(gene, false);
	}

	/**
	 * Add a gene, update statistics
	 * @param gene
	 */
	public void add(Gene gene, boolean useGeneId) {
		if (geneName == null) geneName = useGeneId ? gene.getId() : gene.getGeneName();

		proteinCoding |= gene.isProteinCoding();
		geneLength += gene.size();

		for (Transcript tr : gene) {
			// Skip transcripts that have errors
			if (tr.hasError()) continue;

			numberOfTranscripts++;

			numberOfExonsTotal += tr.numChilds();
			numberOfExonsMax = Math.max(numberOfExonsMax, tr.numChilds());

			int cdsLen = tr.cds().length();
			cdsLengthTotal += cdsLen;
			cdsLengthMax = Math.max(cdsLengthMax, cdsLen);

			// Only protein coding transcripts
			if (tr.isProteinCoding() && (cdsLen > 0)) {
				int aaLen = cdsLen / 3;
				aaLengthTotal += aaLen;
				aaLengthMax = Math.max(aaLengthMax, aaLen);
				aaLengthCount++;
			}
		}
	}

	public String title() {
		return "proteinCoding\tgeneLength\tnumberOfTranscripts\tnumberOfExonsAvg\tnumberOfExonsMax\tcdsLengthAvg\tcdsLengthMax\taaLengthAvg\taaLengthMax\taaLengthCount";
	}

	@Override
	public String toString() {
		double numberOfExonsAvg = 0;
		double cdsLengthAvg = 0;
		double aaLengthAvg = 0;

		if (numberOfTranscripts > 0) {
			numberOfExonsAvg = numberOfExonsTotal / ((double) numberOfTranscripts);
			cdsLengthAvg = cdsLengthTotal / ((double) numberOfTranscripts);
		}

		if (aaLengthCount > 0) aaLengthAvg = aaLengthTotal / ((double) aaLengthCount);

		return proteinCoding //
				+ "\t" + geneLength //
				+ "\t" + numberOfTranscripts //
				+ "\t" + numberOfExonsAvg //
				+ "\t" + numberOfExonsMax //
				+ "\t" + cdsLengthAvg //
				+ "\t" + cdsLengthMax //
				+ "\t" + aaLengthAvg //
				+ "\t" + aaLengthMax //
				+ "\t" + aaLengthCount //
		;
	}
}
