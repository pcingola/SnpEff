package org.snpeff.interval;

import java.util.HashMap;

import org.snpeff.interval.Exon.ExonSpliceType;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.stats.CountByType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Characterize exons based on alternative splicing
 *
 * References: "Alternative splicing and evolution - diversification, exon definition and function"  (see Box 1)
 *
 * @author pablocingolani
 */
public class ExonSpliceCharacterizer {

	public static final int MAX_EXONS = 1000; // Do not characterize transcripts having more than this number of exons
	public static final int SHOW_EVERY = 1000;

	boolean verbose = false;
	Genome genome;
	HashMap<Exon, Exon.ExonSpliceType> typeByExon;
	CountByType countByType = new CountByType();

	public ExonSpliceCharacterizer(Genome genome) {
		this.genome = genome;
		typeByExon = new HashMap<Exon, Exon.ExonSpliceType>();
	}

	public ExonSpliceCharacterizer(String genomeVer) {
		Config config = new Config(genomeVer);
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();
		genome = snpEffectPredictor.getGenome();
		typeByExon = new HashMap<Exon, Exon.ExonSpliceType>();
	}

	/**
	 * Characterize all exons
	 */
	public CountByType characterize() {
		type();
		return countByType;
	}

	/**
	 * Count number of exons
	 */
	int countExons() {
		int count = 0;
		for (Gene g : genome.getGenes())
			for (Transcript tr : g)
				count += tr.numChilds();
		return count;
	}

	/**
	 * Does the marker intersect any exon in 'tr'?
	 * @param m
	 * @param tr
	 * @return
	 */
	boolean intersectsAnyExon(Marker m, Transcript tr) {
		for (Exon e : tr)
			if (m.intersects(e)) return true;
		return false;
	}

	/**
	 * Is thins an ALTERNATIVE_3SS exon?
	 * @param exon
	 * @param gene
	 * @return
	 */
	boolean isAlt3ss(Exon exon, Gene gene) {
		for (Transcript tr : gene)
			for (Exon e : tr) {
				if (exon.intersects(e)) {
					if (exon.isStrandPlus()) {
						// Same exon end, different exon start?
						if ((exon.getStart() != e.getStart()) && (exon.getEndClosed() == e.getEndClosed())) return true;
					} else {
						// Same exon end, different exon start? (negative strand)
						if ((exon.getStart() == e.getStart()) && (exon.getEndClosed() != e.getEndClosed())) return true;
					}
				}
			}

		return false;
	}

	/**
	 * Is thins an ALTERNATIVE_5SS exon?
	 * @param exon
	 * @param gene
	 * @return
	 */
	boolean isAlt5ss(Exon exon, Gene gene) {
		for (Transcript tr : gene)
			for (Exon e : tr) {
				if (exon.intersects(e)) {
					if (exon.isStrandPlus()) {
						// Same exon start, different exon end?
						if ((exon.getStart() == e.getStart()) && (exon.getEndClosed() != e.getEndClosed())) return true;
					} else {
						// Same exon start, different exon end? (negative strand)
						if ((exon.getStart() != e.getStart()) && (exon.getEndClosed() == e.getEndClosed())) return true;
					}
				}
			}

		return false;
	}

	/**
	 * Is this exon mutually exclusive with another exon?
	 * @param exon
	 * @param gene
	 * @return
	 */
	boolean isMutEx(Exon exon, Gene gene) {
		if (gene.numChilds() <= 1) return false;

		//---
		// Make a list of all unique exons
		//---
		String exonKey = key(exon);
		HashMap<String, Exon> uniqEx = new HashMap<String, Exon>();
		for (Transcript tr : gene)
			for (Exon e : tr) {
				String ekey = key(e);
				if (!exonKey.equals(ekey)) uniqEx.put(ekey, e);
			}

		//---
		// For each unique exon, compare if it is mutually exclusive with 'exon'
		//---
		Transcript exonTr = (Transcript) exon.getParent();
		for (Exon e : uniqEx.values()) {
			ExonSpliceType type = typeByExon.get(e);;
			if (type == Exon.ExonSpliceType.SKIPPED) { // Only check for these exons (avoid all ALT*)
				boolean xor = true;
				for (Transcript tr : gene) {
					if (exonTr.intersects(tr) && exon.intersects(tr) && e.intersects(exonTr)) { // Make sure both exons intersect both transcripts (otherwise they cannot be mutually exclusive)
						xor &= intersectsAnyExon(e, tr) ^ intersectsAnyExon(exon, tr);
					} else xor = false;
				}

				// XOR is true? => Mutually exclusive
				if (xor) return true;
			}
		}

		return false;
	}

	/**
	 * Create a simple hash key based on choromosomal position
	 * @param m
	 * @return
	 */
	String key(Marker m) {
		return m.getChromosomeName() + ":" + m.getStart() + "-" + m.getEndClosed();
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Mark exons types
	 */
	void type() {
		if (verbose) {
			Log.info("Caracterizing exons by splicing (stage 1) : ");
			System.out.print("\t");
		}

		// Find retained exons
		int numExon = 1;
		for (Gene g : genome.getGenes()) {

			// Count exons
			CountByType count = new CountByType();
			for (Transcript tr : g)
				for (Exon e : tr)
					count.inc(key(e));

			// Label exons
			int countTr = g.numChilds();
			for (Transcript tr : g) {
				for (Exon e : tr) {
					if (verbose) Gpr.showMark(numExon++, SHOW_EVERY, "\t");

					String eKey = key(e);
					int countEx = (int) count.get(eKey);

					// Is this exon maintained in all transcripts?
					if (countEx == countTr) type(e, Exon.ExonSpliceType.RETAINED);
					else {
						if (isAlt3ss(e, g)) type(e, Exon.ExonSpliceType.ALTTENATIVE_3SS);
						else if (isAlt5ss(e, g)) type(e, Exon.ExonSpliceType.ALTTENATIVE_5SS);
						else if (tr.numChilds() > 1) {
							if (e.getRank() == 1) type(e, Exon.ExonSpliceType.ALTTENATIVE_PROMOMOTER);
							else if (e.getRank() == tr.numChilds()) type(e, Exon.ExonSpliceType.ALTTENATIVE_POLY_A);
							else type(e, Exon.ExonSpliceType.SKIPPED);
						}
					}
				}
			}
		}

		if (verbose) {
			System.err.println("");
			Log.info("Caracterizing exons by splicing (stage 2) : ");
			System.out.print("\t");
		}

		// Now analyze if there are mutually exclusive exons
		numExon = 1;
		for (Gene g : genome.getGenes()) {
			for (Transcript tr : g) {
				if (tr.numChilds() < MAX_EXONS) {
					for (Exon e : tr) {
						if (verbose) Gpr.showMark(numExon++, SHOW_EVERY, "\t");
						ExonSpliceType type = typeByExon.get(e);
						if (type == ExonSpliceType.SKIPPED) { // Try to re-annotate only these
							if (isMutEx(e, g)) type(e, Exon.ExonSpliceType.MUTUALLY_EXCLUSIVE);
						}
					}
				} else {
					System.err.println("");
					Log.debug("WARNING: Gene '" + g.getId() + "', transcript '" + tr.getId() + "' has too many exons (" + tr.numChilds() + " exons). Skipped");
				}
			}
		}

		if (verbose) Log.info("done.");
	}

	/**
	 * Mark this exons as 'type'
	 * @param e
	 * @param type
	 */
	void type(Exon e, Exon.ExonSpliceType type) {
		e.spliceType = type;
		countByType.inc(type.toString());
		typeByExon.put(e, type);
	}
}
