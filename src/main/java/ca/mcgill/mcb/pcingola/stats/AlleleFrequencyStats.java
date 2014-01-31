package ca.mcgill.mcb.pcingola.stats;

import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;

/**
 * Calculatenumber of alleles (number of singletons, doubletons, etc.)
 * 
 * @author pablocingolani
 */
public class AlleleFrequencyStats implements SamplingStats<VcfEntry> {

	static int GENOTYPE_SINGLE_ALT_CHANGE[] = { 1 };

	IntStats count;

	public AlleleFrequencyStats() {
		count = new IntStats();
	}

	public IntStats getCount() {
		return count;
	}

	/**
	 * Does this stat have any data
	 * @return
	 */
	@Override
	public boolean hasData() {
		return count.isValidData();
	}

	/**
	 * Update counters
	 */
	@Override
	public void sample(VcfEntry vcfEntry) {
		// Is this a variant? (i.e. not the same as reference)
		// Is this a SNP?
		if( !vcfEntry.isSnp() || !vcfEntry.isVariant() ) return;

		// Do we need to initialize?
		int genotypes = vcfEntry.getVcfGenotypes().size();
		String alts[] = vcfEntry.getAlts();
		int countGenotypes[] = new int[alts.length + 1];

		// Are there any genotype fields?
		if( genotypes > 0 ) {
			// For each sample (i.e. 'genotype' field)
			for( VcfGenotype vcfGenotype : vcfEntry ) {
				if( vcfGenotype.isVariant() ) {
					// Get genotypes
					int gens[] = vcfGenotype.getGenotype();

					// Missing all genotype information => assume single 'ALT' change
					if( gens == null ) gens = GENOTYPE_SINGLE_ALT_CHANGE;

					// For all genotypes
					for( int gen : gens )
						if( gen > 0 ) countGenotypes[gen]++; // Genotype '0' is the REF (i.e. no base change). If it's negative, then it is not available.
				}
			}

			// Process genotype counts
			for( int c : countGenotypes )
				if( c > 0 ) count.sample(c);
		} else {
			// Assume only one sample: REF -> ALTs
			count.sample(1);
		}
	}

	/**
	 * Show results to stdout
	 */
	@Override
	public String toString() {
		return count.toString();
	}

}
