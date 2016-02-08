package org.snpeff.stats;

import java.util.List;

import org.snpeff.vcf.VcfEntry;

/**
 * Count Hom/Het per sample
 *
 *
 * From Pierre:
 * For multiple ALT, I suggest to count the  number of REF allele
 *		0/1 => ALT1
 *		0/2 => ALT1
 *		1/1 => ALT2
 *		2/2 => ALT2
 *		1/2 => ALT2
 *
 * @author pablocingolani
 */
public class HomHetStats implements SamplingStats<VcfEntry> {

	List<String> sampleNames;
	long countHomRef[];
	long countAlt1[];
	long countAlt2[];
	long countMissing[];

	public HomHetStats() {
	}

	@Override
	public boolean hasData() {
		return countHomRef != null;
	}

	/**
	 * Update Hom/Het counters
	 */
	@Override
	public void sample(VcfEntry vcfEntry) {
		if (sampleNames == null) sampleNames = vcfEntry.getVcfFileIterator().getSampleNames();

		// Is this a variant? (i.e. not the same as reference)
		if (!vcfEntry.isVariant()) return;

		// Do we need to initialize?
		byte gt[] = vcfEntry.getGenotypesScores();
		if (gt == null || gt.length < 1) return;

		if (countHomRef == null) {
			int size = gt.length;
			countHomRef = new long[size];
			countAlt1 = new long[size];
			countAlt2 = new long[size];
			countMissing = new long[size];
		}

		// For each sample (i.e. 'genotype' field)
		for (int i = 0; i < gt.length; i++) {
			switch (gt[i]) {
			case -1:
				countMissing[i]++;
				break;

			case 0:
				countHomRef[i]++;
				break;

			case 1:
				countAlt1[i]++;
				break;

			case 2:
				countAlt2[i]++;
				break;

			default:
				throw new RuntimeException("Unknown genotype code '" + gt[i] + "'");
			}
		}
	}

	/**
	 * Show results to stdout
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		if (!hasData()) {
			sb.append("No results available (empty input?)");
		} else {
			// Show title
			sb.append("Sample ,");
			if (sampleNames != null) {
				for (String sname : sampleNames)
					sb.append(sname + ",");
			}
			sb.append("Total");
			sb.append("\n");

			// Show transitions
			sb.append(toStringArray("Homozygous reference", countHomRef));
			sb.append(toStringArray("One ALT", countAlt1));
			sb.append(toStringArray("Two ALTs", countAlt2));
			sb.append(toStringArray("Missing", countMissing));
		}

		return sb.toString();
	}

	/**
	 * Format an array into a string
	 */
	String toStringArray(String title, long count[]) {
		StringBuilder sb = new StringBuilder();
		sb.append(title + ",");

		long total = 0;
		for (int i = 0; i < count.length; i++) {
			sb.append(count[i] + ",");
			total += count[i];
		}
		sb.append(total + "\n");

		return sb.toString();
	}
}
