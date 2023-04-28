package org.snpeff.snpEffect.testCases.integration;

import java.util.LinkedList;
import java.util.Random;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Marker;
import org.snpeff.interval.SpliceSite;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.LossOfFunction;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Test Loss of Function prediction
 *
 * @author pcingola
 */
public class TestCasesIntegrationLof extends TestCasesIntegrationBase {

	public static final int NUM_DEL_TEST = 10; // number of random test per transcript

	Config config;
	Random random = new Random(20121030);

	public TestCasesIntegrationLof() {
		super();
	}

	@AfterEach
	public void after() {
		config = null;
	}

	@Override
	Marker cdsMarker(Transcript tr) {
		int start = tr.isStrandPlus() ? tr.getCdsStart() : tr.getCdsEnd();
		int end = tr.isStrandPlus() ? tr.getCdsEnd() : tr.getCdsStart();
		return new Marker(tr.getParent(), start, end, false, "");
	}

	/**
	 * Check that LOF works for a given transcript
	 */
	void checkLof(Transcript tr) {
		// Don't check non-protein coding
		if (!tr.isProteinCoding()) return;

		checkLofSplice(tr);
		checkLofStartLost(tr);
		checkLofExonDeleted(tr);
		checkLofFrameShift(tr);
	}

	/**
	 * Check that exon deleted is LOF
	 */
	void checkLofExonDeleted(Transcript tr) {
		// First coding exont
		checkLofExonDeletedFirstExon(tr);

		// More than half protein is lost
		checkLofExonDeletedHalf(tr);
	}

	/**
	 * First coding exon produces a LOF
	 */
	void checkLofExonDeletedFirstExon(Transcript tr) {
		Exon ex = tr.getFirstCodingExon();
		Variant Variant = new Variant(tr.getChromosome(), ex.getStart(), "AC", "A");
		Variant.setStart(ex.getStart());
		Variant.setEndClosed(ex.getEndClosed());
		Variant.setVariantType(VariantType.DEL);
		if (verbose) Log.debug("Variant:" + Variant);
		LinkedList<VariantEffect> changeEffects = variantEffects(Variant, EffectType.EXON_DELETED, ex);

		// Calculate LOF
		LossOfFunction lof = new LossOfFunction(config, changeEffects);
		boolean islof = lof.isLof();
		assertTrue(islof);
	}

	/**
	 * If more than half protein is lost => LOF
	 */
	void checkLofExonDeletedHalf(Transcript tr) {
		// Calculate coding part of the transcript
		Marker cds = cdsMarker(tr);

		for (int i = 0; i < NUM_DEL_TEST; i++) {
			// Create a random Variant
			int delStart = random.nextInt(tr.size() - 1) + tr.getStart();
			int delEnd = random.nextInt(tr.getEndClosed() - delStart) + delStart + 1;
			Variant Variant = new Variant(tr.getChromosome(), delStart, "AC", "A");
			Variant.setStart(delStart);
			Variant.setEndClosed(delEnd);
			if (verbose) Log.debug("Variant:" + Variant);
			Variant.setVariantType(VariantType.DEL);

			// How many coding bases are affected?
			Marker codingDel = cds.intersect(Variant);
			if (codingDel != null) { // Does it intersect?
				int numBases = 0;
				for (Exon ex : tr)
					numBases += codingDel.intersectSize(ex);

				// Percent of coding bases?
				double perc = numBases / ((double) tr.cds().length());
				boolean delIsLof = perc > LossOfFunction.DEFAULT_DELETE_PROTEIN_CODING_BASES;

				// Calculate LOF
				LinkedList<VariantEffect> changeEffects = variantEffects(Variant, EffectType.TRANSCRIPT, tr); // Notice that we don't care what type of effect is, so we just use 'TRANSCRIPT'
				LossOfFunction lof = new LossOfFunction(config, changeEffects);
				boolean islof = lof.isLof();
				assertEquals(delIsLof, islof);
			}
		}
	}

	/**
	 * Frame shifts are LOF
	 */
	void checkLofFrameShift(Transcript tr) {
		Marker cds = cdsMarker(tr);

		int codingBase = 0;

		for (Exon ex : tr.sortedStrand()) {
			int start = tr.isStrandPlus() ? ex.getStart() : ex.getEndClosed();
			int step = tr.isStrandPlus() ? 1 : -1;

			// All exonic positions
			for (int pos = start; ex.intersects(pos); pos += step) {
				// Create a Variant
				Variant Variant;
				boolean ins = random.nextBoolean(); // Randomly choose INS or DEL
				if (ins) Variant = new Variant(tr.getChromosome(), pos, "A", "AC");
				else Variant = new Variant(tr.getChromosome(), pos, "AC", "A");
				Variant.setVariantType(ins ? VariantType.INS : VariantType.DEL);
				if (verbose) Log.debug("Variant:" + Variant);

				// Create change effect
				LinkedList<VariantEffect> changeEffects = variantEffects(Variant, EffectType.FRAME_SHIFT, ex);
				VariantEffect changeEffect = changeEffects.get(0);
				changeEffect.setCodons("", "", codingBase / 3, codingBase % 3); // Set codon affected
				int aaLen = changeEffect.getAaLength();

				// Should this be a LOF?
				boolean isFsLof = false;
				if (cds.intersects(pos)) {
					double perc = (codingBase / 3) / ((double) aaLen);
					isFsLof = (LossOfFunction.DEFAULT_IGNORE_PROTEIN_CODING_BEFORE <= perc) && (perc <= LossOfFunction.DEFAULT_IGNORE_PROTEIN_CODING_AFTER);
					codingBase++;
				}

				// Is LOF as expected?
				LossOfFunction lof = new LossOfFunction(config, changeEffects);
				boolean islof = lof.isLof();
				assertEquals(isFsLof, islof);
			}
		}
	}

	void checkLofSplice(Transcript tr) {
		// All transcripts in exon
		for (Intron intron : tr.introns()) {
			checkSpliceDonor(tr, intron);
			checkSpliceAcceptor(tr, intron);
		}
	}

	/**
	 * Check that START_LOST is LOF
	 */
	void checkLofStartLost(Transcript tr) {
		// Find start codon position
		int pos = tr.getCdsStart();
		Variant Variant = new Variant(tr.getChromosome(), pos, "A", "C"); // Create a Variant
		if (verbose) Log.debug("Variant:" + Variant);

		// Finr exon
		Exon exon = null;
		for (Exon ex : tr)
			if (ex.intersects(pos)) exon = ex;
		if (exon == null) throw new RuntimeException("Cannot find first exon for transcript " + tr.getId());

		// Create a LOF object and analyze the effect
		LinkedList<VariantEffect> changeEffects = variantEffects(Variant, EffectType.START_LOST, exon);
		LossOfFunction lof = new LossOfFunction(config, changeEffects);
		boolean islof = lof.isLof();
		assertEquals(true, islof);
	}

	/**
	 * Check that Core Splice Site acceptors are considered LOF
	 */
	void checkSpliceAcceptor(Transcript tr, Intron intron) {
		int step = tr.isStrandPlus() ? +1 : -1;

		if (intron.getRank() > 1) {
			// Position
			int posDonor = tr.isStrandPlus() ? intron.getEndClosed() : intron.getStart();

			// Splice site size
			int maxSize = Math.min(intron.size(), SpliceSite.CORE_SPLICE_SITE_SIZE);
			posDonor -= step * (maxSize - 1);
			if (verbose) Log.debug("Intron size: " + intron.size());
			if (maxSize <= 0) throw new RuntimeException("Max splice size is " + maxSize);

			//---
			// For all position on splice site donor positions, make sure it is LOF
			//---
			for (int pos = posDonor, i = 0; i < maxSize; i++, pos += step) {
				Variant Variant = new Variant(tr.getChromosome(), pos, "A", "C"); // Create a Variant
				Marker marker = findMarker(config.getSnpEffectPredictor(), Variant, EffectType.SPLICE_SITE_ACCEPTOR, null, intron);
				LinkedList<VariantEffect> changeEffects = variantEffects(Variant, EffectType.SPLICE_SITE_ACCEPTOR, marker); // Create a SPLICE_SITE_ACCEPTOR effect
				if (verbose) Log.debug("Variant:" + Variant);

				// Create a LOF object and analyze the effect
				LossOfFunction lof = new LossOfFunction(config, changeEffects);
				boolean islof = lof.isLof();
				assertEquals(true, islof);
			}
		}

	}

	/**
	 * Check that Core Splice Donor acceptors are considered LOF
	 */
	void checkSpliceDonor(Transcript tr, Intron intron) {
		int step = tr.isStrandPlus() ? 1 : -1;
		int maxRank = tr.numChilds();

		if (intron.getRank() < maxRank) {
			// Position
			int posDonor = tr.isStrandPlus() ? intron.getStart() : intron.getEndClosed();

			// Splice site size
			int maxSize = Math.min(intron.size(), SpliceSite.CORE_SPLICE_SITE_SIZE);
			if (verbose) Log.debug("Intron size: " + intron.size() + "\tmaxSize: " + maxSize);
			if (maxSize <= 0) throw new RuntimeException("Max splice size is non-positive: " + maxSize);

			//---
			// For all position on splice site donor positions, make sure it is LOF
			//---
			for (int pos = posDonor, i = 0; i < maxSize; i++, pos += step) {
				if (verbose) Log.debug("Position: " + tr.getChromosome().getId() + ":" + posDonor);
				Variant variant = new Variant(tr.getChromosome(), pos, "A", "C"); // Create a Variant
				Marker marker = findMarker(config.getSnpEffectPredictor(), variant, EffectType.SPLICE_SITE_DONOR, null, intron);
				LinkedList<VariantEffect> changeEffects = variantEffects(variant, EffectType.SPLICE_SITE_DONOR, marker); // Create a SPLICE_DONOR effect
				if (verbose) Log.debug("Variant:" + variant);

				// Create a LOF object and analyze the effect
				LossOfFunction lof = new LossOfFunction(config, changeEffects);
				boolean islof = lof.isLof();
				assertEquals(true, islof);
			}
		}

	}

	@Test
	public void test_01() {
		Log.debug("Test");

		// Load database
		String genomeVer = "testHg3766Chr1";
		config = new Config(genomeVer, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		config.setTreatAllAsProteinCoding(true); // For historical reasons...
		config.getSnpEffectPredictor().buildForest();

		// For each gene, transcript, check that NMD works
		int i = 1;
		for (Gene gene : config.getGenome().getGenes()) {
			Gpr.showMark(i++, 10);
			for (Transcript tr : gene) {
				if (verbose) Log.info(tr);
				checkLof(tr);
			}
		}
	}

	/**
	 * We should be able to annotate a BED file
	 */
	@Test
	public void test_02() {
		String[] args = { "testHg3775Chr22", "-noLog", "-i", "bed", path("test_lof_02.bed") };
		SnpEff snpeff = new SnpEff(args);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		boolean ok = snpeff.run();
		assertTrue(ok);
	}

	/**
	 * Create change effects
	 */
	@Override
	LinkedList<VariantEffect> variantEffects(Variant variant, EffectType effectType, Marker marker) {
		VariantEffect changeEffect = new VariantEffect(variant);
		changeEffect.set(marker, effectType, effectType.effectImpact(), "");
		LinkedList<VariantEffect> changeEffects = new LinkedList<>();
		changeEffects.add(changeEffect);
		return changeEffects;

	}

}
