package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.stats.CountByType;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Tast class
 * @author pablocingolani
 */
public class ZzzExonLengthByRank {

	/**
	 * Main
	 * @param args
	 */
	public static void main(String[] args) {
		// String gen = "testHg3763Chr20";
		String gen = "hg19";
		ZzzExonLengthByRank zzz = new ZzzExonLengthByRank();
		zzz.run(gen);
	}

	void run(String gen) {
		// Load data
		Timer.showStdErr("Loading..");
		Config config = new Config(gen);
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();

		// Analyze all exons
		CountByType countByRank = new CountByType();
		CountByType lenByRank = new CountByType();
		for (Gene g : snpEffectPredictor.getGenome().getGenes()) {
			System.err.println(g.getGeneName());

			for (Transcript tr : g) {
				if (tr.isProteinCoding()) {
					for (Exon e : tr.sortedStrand()) {
						if (tr.isUtr(e.getStart()) || tr.isUtr(e.getEnd())) {
							// UTRs ignored
						} else {
							String rank = "" + e.getRank();
							countByRank.inc(rank);
							lenByRank.inc(rank, e.size());
						}
					}
				}
			}
		}

		for (String key : countByRank.keySet())
			System.out.println(key + "\t" + countByRank.get(key) + "\t" + lenByRank.get(key));
	}

}
