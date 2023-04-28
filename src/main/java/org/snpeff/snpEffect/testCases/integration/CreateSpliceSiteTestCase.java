package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

public class CreateSpliceSiteTestCase {

	StringBuilder out = new StringBuilder();

	public static void main(String[] args) {
		CreateSpliceSiteTestCase zzz = new CreateSpliceSiteTestCase();
		zzz.run();
	}

	void out(Exon exon, int from, int to, EffectType type) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i <= to; i++)
			sb.append(exon.getChromosomeName() + "\t" + (i + 1) + "\tA\tC\t+\t0\t0\t" + type + "\n");
		System.out.print(sb);
		out.append(sb.toString());
	}

	/**
	 * @param args
	 */
	public void run() {
		Log.debug("Loading config files");
		Config config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		for (Gene gint : config.getGenome().getGenes()) {
			for (Transcript tr : gint) {
				List<Exon> exons = tr.sortedStrand();

				for (Exon eint : exons) {
					// Create Splice site test for each exon

					if (eint.size() > SpliceSite.CORE_SPLICE_SITE_SIZE) {
						// Positive strand donor & acceptor sites
						if (gint.isStrandPlus()) {
							// Acceptor splice site: before exon start, but not before first exon
							if (eint.getRank() > 1) out(eint, eint.getStart() - SpliceSite.CORE_SPLICE_SITE_SIZE, eint.getStart() - 1, EffectType.SPLICE_SITE_ACCEPTOR);
							else out(eint, eint.getStart() - SpliceSite.CORE_SPLICE_SITE_SIZE, eint.getStart() - 1, EffectType.UPSTREAM);

							// Donor splice site: after exon end, but not after last exon
							if (eint.getRank() < exons.size()) out(eint, eint.getEndClosed() + 1, eint.getEndClosed() + SpliceSite.CORE_SPLICE_SITE_SIZE, EffectType.SPLICE_SITE_DONOR);
							else out(eint, eint.getEndClosed() + 1, eint.getEndClosed() + SpliceSite.CORE_SPLICE_SITE_SIZE, EffectType.DOWNSTREAM);
						} else { // Negative strand donor & acceptor sites

							// Acceptor splice site: before exon start (since it's minus strand, it's actually after end), but not before first exon
							if (eint.getRank() > 1) out(eint, eint.getEndClosed() + 1, eint.getEndClosed() + SpliceSite.CORE_SPLICE_SITE_SIZE, EffectType.SPLICE_SITE_ACCEPTOR);
							else out(eint, eint.getEndClosed() + 1, eint.getEndClosed() + SpliceSite.CORE_SPLICE_SITE_SIZE, EffectType.UPSTREAM);

							// Donor splice site: after exon end (since it's minus strand, it's actually before start), but not after last exon
							if (eint.getRank() < exons.size()) out(eint, eint.getStart() - SpliceSite.CORE_SPLICE_SITE_SIZE, eint.getStart() - 1, EffectType.SPLICE_SITE_DONOR);
							else out(eint, eint.getStart() - SpliceSite.CORE_SPLICE_SITE_SIZE, eint.getStart() - 1, EffectType.DOWNSTREAM);
						}
					}
				}
			}
		}

		Gpr.toFile("/tmp/CreateSpliceSiteTestCase.txt", out.toString());
	}

}
