package org.snpeff.stats;

import java.util.List;

import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;

/**
 * Calculate Ts/Tv rations per sample (transitions vs transversions)
 *
 * @author pablocingolani
 */
public class TsTvStats implements SamplingStats<VcfEntry> {

	static int GENOTYPE_SINGLE_ALT_CHANGE[] = { 1 };

	List<String> sampleNames;
	long countTs[];
	long countTv[];

	public TsTvStats() {
	}

	public long getTransitions() {
		if (countTs == null) return 0;
		long sum = 0;
		for (int i = 0; i < countTs.length; i++)
			sum += countTs[i];
		return sum;
	}

	public long getTransversions() {
		if (countTv == null) return 0;

		long sum = 0;
		for (int i = 0; i < countTv.length; i++)
			sum += countTv[i];
		return sum;
	}

	/**
	 * Transitions / transverions ratio
	 *
	 * WARNING: I removed the '2.0' factor because it mostly confused people.
	 * I clarify that the ratio is a 'raw' ratio in the summary page
	 *
	 * ------------------------------------------------------------------------
	 * Comments that follow are out-dated. I leave it here just for reference.
	 *
	 * Note: Why is there a '2' in the ratio and not just "number of transitions / number of transverions"?
	 *
	 * From Casey Bergman (Manchester Univ.)
	 * 		Ts:Tv ratio is a ratio of rates, not observed events. Imagine observing 100 sites with
	 * 		transitions and 100 sites with transversions. Your method would say that the Ts:Tv rate
	 * 		ratio is 1. But since there are 4 possible Tv mutation types and only 2 possible Ts
	 * 		mutation types, in this example there is actually a 2-fold higher rate of Ts mutations
	 * 		that Tv mutations per site. Thus, the Ts:Tv (rate) ratio is 2:1
	 *
	 * References:
	 * 		http://www.mun.ca/biology/scarr/Transitions_vs_Transversions.html
	 * 		http://biostar.stackexchange.com/questions/4759/ti-tv-ratio-confirms-snp-discovery-is-this-a-general-rule/
	 *
	 * @return
	 */
	public double getTsTvRatio() {
		double ts = getTransitions();
		double tv = getTransversions();
		return tv > 0 ? ts / tv : 0;
	}

	/**
	 * Does this stat have any data
	 * @return
	 */
	@Override
	public boolean hasData() {
		return countTs != null;
	}

	/**
	 * Is this a transition?
	 * @param ref : Reference base (upper case)
	 * @param alt : Alternative base (upper case)
	 * @return
	 */
	public boolean isTransition(String ref, String alt) {
		if (ref.equals("A") && alt.equals("G")) return true;
		if (ref.equals("C") && alt.equals("T")) return true;
		if (ref.equals("G") && alt.equals("A")) return true;
		if (ref.equals("T") && alt.equals("C")) return true;
		return false;
	}

	/**
	 * Is this a transversion?
	 * @param ref : Reference base (upper case)
	 * @param alt : Alternative base (upper case)
	 * @return
	 */
	public boolean isTranversion(String ref, String alt) {
		if (ref.equals("A") && (alt.equals("C") || alt.equals("T"))) return true;
		if (ref.equals("C") && (alt.equals("A") || alt.equals("G"))) return true;
		if (ref.equals("G") && (alt.equals("C") || alt.equals("T"))) return true;
		if (ref.equals("T") && (alt.equals("A") || alt.equals("G"))) return true;
		return false;
	}

	/**
	 * Update Ts and Tv counters
	 * Only for SNPs
	 */
	@Override
	public void sample(VcfEntry vcfEntry) {
		if (sampleNames == null) sampleNames = vcfEntry.getVcfFileIterator().getSampleNames();

		// Is this a variant? (i.e. not the same as reference)
		// Is this a SNP?
		if (!vcfEntry.isVariant() || !vcfEntry.isSingleSnp()) return;

		// Do we need to initialize?
		int genotypes = vcfEntry.getVcfGenotypes().size();
		if (countTs == null) {
			int size = Math.max(genotypes, 1); // At least must be one
			countTs = new long[size];
			countTv = new long[size];
		}

		if (genotypes > 0) {
			// Calculate for each sample
			int sampleNum = 0;

			// For each sample (i.e. 'genotype' field)
			for (VcfGenotype vcfGenotype : vcfEntry) {

				if (sampleNum >= countTs.length) {
					Log.debug("WARNING: VCF entry has more genotype fields than expected (expected: " + countTs.length + ", number of genotypes: " + sampleNum + ").\n" + vcfEntry);
				} else if (vcfGenotype.isVariant()) {
					String alts[] = vcfEntry.getAlts();
					int gens[] = vcfGenotype.getGenotype();

					// Missing genotype information => assume single 'ALT' change
					if (gens == null) gens = GENOTYPE_SINGLE_ALT_CHANGE;

					// For all genotypes
					for (int gen : gens) {
						// Genotype '0' is the REF (i.e. no base change). If it's negative, then it is not available.
						if (gen > 0) {
							String ref = vcfEntry.getRef();
							String alt = alts[gen - 1];

							// Count Ts / Tv per sample (i.e. per genotype field)
							if (isTransition(ref, alt)) countTs[sampleNum]++;
							else if (isTranversion(ref, alt)) countTv[sampleNum]++;
						}
					}
				}

				sampleNum++;
			}
		} else {
			// Assume only one sample: REF -> ALTs
			String ref = vcfEntry.getRef();
			for (String alt : vcfEntry.getAlts()) {
				if (!ref.equals(alt)) { // Is it a variant?
					if (isTransition(ref, alt)) countTs[0]++;
					else if (isTranversion(ref, alt)) countTv[0]++;
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

			// Show transitions
			long sumTs = 0;
			sb.append("Transitions ,");
			for (int i = 0; i < countTs.length; i++) {
				sb.append(countTs[i] + ",");
				sumTs += countTs[i];
			}
			sb.append(sumTs);
			sb.append("\n");

			// Show transversions
			long sumTv = 0;
			sb.append("Transversions ,");
			for (int i = 0; i < countTv.length; i++) {
				sb.append(countTv[i] + ",");
				sumTv += countTv[i];
			}
			sb.append(sumTv);
			sb.append("\n");

			// Show transversions
			sb.append("Ts/Tv ,");
			for (int i = 0; i < countTv.length; i++) {
				double tstv = ((double) countTs[i]) / ((double) countTv[i]);
				sb.append(String.format("%.3f,", tstv));
			}
			double tstv = ((double) sumTs) / ((double) sumTv);
			sb.append(String.format("%.3f", tstv));
			sb.append("\n");
		}

		return sb.toString();
	}

}
