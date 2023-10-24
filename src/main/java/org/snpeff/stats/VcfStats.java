package org.snpeff.stats;

import org.snpeff.vcf.VcfEntry;

/**
 * VCF statistics: These are statisticas at VCF line (i.e. VCFEntry) level, so these are usually across all samples in a VCF line
 *
 * @author pcingola
 */
public class VcfStats implements SamplingStats<VcfEntry> {

	IntStats qualityStats;
	TsTvStats tsTvStats;
	TsTvStats tsTvStatsKnown;
	GenotypeStats genotypeStats;
	int countMultiallelic = 0;

	public VcfStats() {
		qualityStats = new IntStats();
		tsTvStats = new TsTvStats();
		tsTvStatsKnown = new TsTvStats();
		genotypeStats = new GenotypeStats();
	}

	public int getCountMultiallelic() {
		return countMultiallelic;
	}

	public GenotypeStats getGenotypeStats() {
		return genotypeStats;
	}

	public IntStats getQualityStats() {
		return qualityStats;
	}

	public String getQualityStatsHistoUrl() {
		return qualityStats.toStringPlot("Quality histogram", "Quality", true);
	}

	public TsTvStats getTsTvStats() {
		return tsTvStats;
	}

	public TsTvStats getTsTvStatsKnown() {
		return tsTvStatsKnown;
	}

	@Override
	public boolean hasData() {
		return tsTvStats.hasData() || genotypeStats.hasData();
	}

	@Override
	public void sample(VcfEntry vcfEntry) {
		// Does it have an ID? => it is a 'known' variant.
		if (!vcfEntry.getId().isEmpty()) tsTvStatsKnown.sample(vcfEntry);
		tsTvStats.sample(vcfEntry);

		// Quality
		if (vcfEntry.hasQuality()) qualityStats.sample((int) vcfEntry.getQuality());

		// Genotype stats
		genotypeStats.sample(vcfEntry);

		if (vcfEntry.isMultiallelic()) countMultiallelic++;
	}
}
