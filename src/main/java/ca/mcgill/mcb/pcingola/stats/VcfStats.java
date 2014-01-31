package ca.mcgill.mcb.pcingola.stats;

import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * VCF statistics: This are usually multi-sample statistics
 * 
 * @author pcingola
 */
public class VcfStats implements SamplingStats<VcfEntry> {

	TsTvStats tsTvStats;
	TsTvStats tsTvStatsKnown;
	AlleleFrequencyStats alleleFrequencyStats;
	AlleleFrequencyStats alleleFrequencyStatsKnown;

	public VcfStats() {
		tsTvStats = new TsTvStats();
		tsTvStatsKnown = new TsTvStats();
		alleleFrequencyStats = new AlleleFrequencyStats();
		alleleFrequencyStatsKnown = new AlleleFrequencyStats();
	}

	public AlleleFrequencyStats getAlleleFrequencyStats() {
		return alleleFrequencyStats;
	}

	public AlleleFrequencyStats getAlleleFrequencyStatsKnown() {
		return alleleFrequencyStatsKnown;
	}

	public TsTvStats getTsTvStats() {
		return tsTvStats;
	}

	public TsTvStats getTsTvStatsKnown() {
		return tsTvStatsKnown;
	}

	public String getAlleleFrequencyHistoUrl() {
		return alleleFrequencyStats.getCount().toStringPlot("Alleles frequencies", "Alleles", true);
	}

	@Override
	public boolean hasData() {
		return tsTvStats.hasData() || alleleFrequencyStats.hasData();
	}

	@Override
	public void sample(VcfEntry vcfEntry) {
		// Does it have an ID? => it is a 'known' variant.
		if( !vcfEntry.getId().isEmpty() ) {
			tsTvStatsKnown.sample(vcfEntry);
			alleleFrequencyStatsKnown.sample(vcfEntry);
		}

		tsTvStats.sample(vcfEntry);
		alleleFrequencyStats.sample(vcfEntry);
	}
}
