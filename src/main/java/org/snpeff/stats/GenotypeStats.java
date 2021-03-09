package org.snpeff.stats;

import java.util.ArrayList;
import java.util.List;

import org.snpeff.stats.plot.GoogleHistogram;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;

/**
 * Calculate statistics on genotype
 *
 * @author pablocingolani
 */
public class GenotypeStats implements SamplingStats<VcfEntry> {

	static int GENOTYPE_SINGLE_ALT_CHANGE[] = { 1 };
	static final String UNIQUE_SAMPLE_NAME = "No_sample_name_found";

	List<String> sampleNames;
	IntStats alleleCount;
	IntStats alleleFrequency;
	int countHom[];
	int countHet[];
	int countRef[];
	int countMissing[];

	public GenotypeStats() {
		alleleCount = new IntStats();
		alleleFrequency = new IntStats();
	}

	String countBySampleBarChartUrl(int count[], String title, String xlabel, String ylabel) {
		if (countMissing == null) return "";

		int[] sampleNum = new int[countMissing.length];
		for (int i = 0; i < countMissing.length; i++)
			sampleNum[i] = i;

		GoogleHistogram barChart = new GoogleHistogram(sampleNum, count, title, xlabel, ylabel);
		return barChart.toURLString();
	}

	public IntStats getAlleleCount() {
		return alleleCount;
	}

	public String getAlleleCountHistoUrl() {
		return alleleCount.toStringPlot("Allele Count", "Allele Count", true);
	}

	public IntStats getAlleleFrequency() {
		return alleleFrequency;
	}

	public String getAlleleFrequencyHistoUrl() {
		return alleleFrequency.toStringPlot("Allele Frequency %", "Alleles frequency", true);
	}

	public String getHetBySampleUrl() {
		return countBySampleBarChartUrl(countHet, "Heterozygous genotypes", "Sample number", "Heterozygous count");
	}

	public String getHomBySampleUrl() {
		return countBySampleBarChartUrl(countHom, "Homozygous (ALT) genotypes", "Sample number", "Homozygous count");
	}

	public String getHomHetTable() {
		if (sampleNames == null) return "";

		StringBuilder sb = new StringBuilder();

		sb.append("Sample_names ");
		for (String s : sampleNames)
			sb.append(", " + s);
		sb.append("\n");

		sb.append("Reference ");
		for (int i = 0; i < countRef.length; i++)
			sb.append(", " + countRef[i]);
		sb.append("\n");

		sb.append("Het ");
		for (int i = 0; i < countHet.length; i++)
			sb.append(", " + countHet[i]);
		sb.append("\n");

		sb.append("Hom ");
		for (int i = 0; i < countHom.length; i++)
			sb.append(", " + countHom[i]);
		sb.append("\n");

		sb.append("Missing ");
		for (int i = 0; i < countMissing.length; i++)
			sb.append(", " + countMissing[i]);
		sb.append("\n");

		return sb.toString();
	}

	public String getMissingBySampleUrl() {
		return countBySampleBarChartUrl(countMissing, "Missing genotypes", "Sample number", "Missing count");
	}

	@Override
	public boolean hasData() {
		return true;
	}

	/**
	 * Update counters
	 */
	@Override
	public void sample(VcfEntry vcfEntry) {
		if (sampleNames == null) {
			sampleNames = vcfEntry.getVcfFileIterator().getSampleNames();

			if (sampleNames == null) {
				sampleNames = new ArrayList<String>();
				sampleNames.add(UNIQUE_SAMPLE_NAME);
			}

			// Get length
			int len = sampleNames.size();
			int lenGts = vcfEntry.getVcfGenotypes() != null ? vcfEntry.getVcfGenotypes().size() : 0;
			len = Math.max(len, lenGts);

			countRef = new int[len];
			countHet = new int[len];
			countHom = new int[len];
			countMissing = new int[len];
		}

		// Is this a variant? (i.e. not the same as reference)
		if (!vcfEntry.isVariant()) return;

		// Are there any genotype fields?
		if (!vcfEntry.getVcfGenotypes().isEmpty()) {
			int ac = 0, totalAc = 0; // Allele count
			int gtNum = 0;
			for (VcfGenotype vcfGenotype : vcfEntry) {
				if (gtNum >= countHet.length) {
					Log.debug("WARNING: VCF entry has more genotype fields than expected (expected: " + countHet.length + ", number of genotypes: " + gtNum + ").\n" + vcfEntry);
				} else {
					int code = vcfGenotype.getGenotypeCode();

					if (code > 0) {
						ac += code;
						totalAc += 2;
						if (code == 1) countHet[gtNum]++;
						else if (code == 2) countHom[gtNum]++;
					} else if (code == 0) {
						totalAc += 2;
						countRef[gtNum]++; // Reference genotype
					} else {
						countMissing[gtNum]++; // Negative code means missing
					}
				}

				gtNum++;
			}

			// Allele count
			alleleCount.sample(ac);

			// Allele frequency
			double af = ((double) ac) / ((double) totalAc);
			int afInt = (int) (100.0 * af);
			alleleFrequency.sample(afInt);
		} else {
			// Assume only one sample: REF -> ALTs
			alleleCount.sample(1);
		}
	}

	/**
	 * Show results to stdout
	 */
	@Override
	public String toString() {
		return alleleCount.toString();
	}

}
