package ca.mcgill.mcb.pcingola.stats;

import java.util.List;

import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;

/**
 * Calculate Ts/Tv rations per sample (transitions vs transversions)
 * 
 * @author pablocingolani
 */
public class TsTvStats implements SamplingStats<VcfEntry> {

	static int GENOTYPE_SINGLE_ALT_CHANGE[] = { 1 };

	List<String> sampleNames;
	Boolean homozygous;
	long countTs[];
	long countTv[];

	public TsTvStats() {
		homozygous = null;
	}

	public TsTvStats(Boolean homozygous) {
		this.homozygous = homozygous;
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
		if (ref.equals("G") && alt.equals("A")) return true;
		if (ref.equals("C") && alt.equals("T")) return true;
		if (ref.equals("T") && alt.equals("C")) return true;
		return false;
	}

	/**
	 * Update Ts and Tv counters
	 */
	@Override
	public void sample(VcfEntry vcfEntry) {
		if (sampleNames == null) sampleNames = vcfEntry.getVcfFileIterator().getSampleNames();

		// Is this a variant? (i.e. not the same as reference)
		// Is this a SNP?
		if (!vcfEntry.isVariant() || !vcfEntry.isSnp()) return;

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
				if (vcfGenotype.isVariant()) {
					// When homozygous is null, accept any. Otherwise, filter accordingly
					if ((homozygous == null) || (vcfGenotype.isHomozygous() == homozygous)) {

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
								else countTv[sampleNum]++;
							}
						}
					}
				}

				sampleNum++;
			}
		} else {
			// Assume only one sample: REF -> ALTs
			boolean homo = vcfEntry.getAlts().length <= 1; // This is homozygous if there is only one ALT

			if ((homozygous == null) || (homo == homozygous)) {
				String ref = vcfEntry.getRef();
				for (String alt : vcfEntry.getAlts()) {

					if (!ref.equals(alt)) { // Is it a variant?
						if (isTransition(ref, alt)) countTs[0]++;
						else countTv[0]++;
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
