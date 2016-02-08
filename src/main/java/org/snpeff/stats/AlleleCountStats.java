package org.snpeff.stats;

import java.util.List;

import org.snpeff.vcf.VcfEntry;

/**
 * Count singletons and other allele counts per sample
 *
 * @author pablocingolani
 */
public class AlleleCountStats implements SamplingStats<VcfEntry> {

	public static final int MAX_MAC = 1000;

	List<String> sampleNames;
	int counters[][];

	public AlleleCountStats() {
	}

	@Override
	public boolean hasData() {
		return counters != null;
	}

	/**
	 * Update MAC counters
	 */
	@Override
	public void sample(VcfEntry vcfEntry) {
		if (sampleNames == null) {
			sampleNames = vcfEntry.getVcfFileIterator().getSampleNames();

			// Initialize counters
			int size = sampleNames.size();
			int sizeMac = Math.min(MAX_MAC, size);

			counters = new int[sizeMac][size];

			for (int i = 0; i < counters.length; i++)
				for (int j = 0; j < size; j++)
					counters[i][j] = 0;
		}

		// Is this a variant? (i.e. not the same as reference)
		if (!vcfEntry.isVariant()) return;

		// Ignore if there are no genotypes
		byte gt[] = vcfEntry.getGenotypesScores();
		if (gt == null || gt.length < 1) return;

		// Get minor allele count
		int mac = vcfEntry.mac();

		// Ignore negative MAC out of range
		if ((mac >= 0) && (mac < counters.length)) {
			int count[] = counters[mac];

			// For each sample count if this sample has the MAC
			for (int i = 0; i < gt.length; i++) {
				if (gt[i] > 0) count[i]++;
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
			for (int i = 0; i < counters.length; i++)
				sb.append(toStringArray("MAC=" + i, counters[i]));
		}

		return sb.toString();
	}

	/**
	 * Format an array into a string
	 */
	String toStringArray(String title, int count[]) {
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
