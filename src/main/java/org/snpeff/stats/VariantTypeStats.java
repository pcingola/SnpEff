package org.snpeff.stats;

import java.util.List;

import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.vcf.VcfEntry;

/**
 * Count variant types (SNP, MNP, INS, DEL)
 *
 * @author pablocingolani
 */
public class VariantTypeStats implements SamplingStats<VcfEntry> {

	public static final int MAX_MAC = 1000;

	List<String> sampleNames;
	int counterSnp[];
	int counterMnp[];
	int counterIns[];
	int counterDel[];
	int counterComplex[];
	int counterMultiallelic[];

	public VariantTypeStats() {
	}

	int[] getCounter(VariantType vt) {
		switch (vt) {
		case SNP:
			return counterSnp;

		case MNP:
			return counterMnp;

		case INS:
			return counterIns;

		case DEL:
			return counterDel;

		case MIXED:
			return counterComplex;

		default:
			return null;
		}
	}

	@Override
	public boolean hasData() {
		return counterSnp != null;
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
			counterSnp = new int[size];
			counterMnp = new int[size];
			counterIns = new int[size];
			counterDel = new int[size];
			counterComplex = new int[size];
			counterMultiallelic = new int[size];
		}

		// Is this a variant? (i.e. not the same as reference)
		if (!vcfEntry.isVariant()) return;

		// Ignore if there are no genotypes
		byte gt[] = vcfEntry.getGenotypesScores();
		if (gt == null || gt.length < 1) return;

		// Get counter for this variant type
		boolean isMultiallelic = vcfEntry.isMultiallelic();
		for (Variant var : vcfEntry.variants()) {
			int count[] = getCounter(var.getVariantType());

			// For each sample count non-ref genotypes
			if (count != null) {
				int altIdx = vcfEntry.getAltIndex(var.getAlt());

				if (altIdx > 0) {
					for (int i = 0; i < gt.length; i++) {
						if (gt[i] == altIdx) count[i]++;
						if (isMultiallelic && gt[i] == altIdx) counterMultiallelic[i]++;
					}
				}
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

			// Show Counters
			for (VariantType vt : VariantType.values())
				sb.append(toStringArray(vt.toString(), getCounter(vt)));

			sb.append(toStringArray("Multiallelic", counterMultiallelic));
		}

		return sb.toString();
	}

	/**
	 * Format an array into a string
	 */
	String toStringArray(String title, int count[]) {
		if (count == null) return "";

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
