package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.LinkedList;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import net.sf.samtools.util.RuntimeEOFException;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.LossOfFunction;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Loss of Function prediction
 *
 * @author pcingola
 */
public class TestCasesLof extends TestCase {

	public static boolean debug = false;
	public static final int NUM_DEL_TEST = 10; // number of random test per transcript

	Config config;
	Random random = new Random(20121030);

	public TestCasesLof() {
		super();
	}

	Marker cdsMarker(Transcript tr) {
		int start = tr.isStrandPlus() ? tr.getCdsStart() : tr.getCdsEnd();
		int end = tr.isStrandPlus() ? tr.getCdsEnd() : tr.getCdsStart();
		return new Marker(tr.getParent(), start, end, false, "");
	}

	/**
	 * Create change effects
	 */
	LinkedList<VariantEffect> changeEffects(Variant variant, EffectType effectType, Marker marker) {
		VariantEffect changeEffect = new VariantEffect(variant);
		changeEffect.set(marker, effectType, effectType.effectImpact(), "");
		LinkedList<VariantEffect> changeEffects = new LinkedList<VariantEffect>();
		changeEffects.add(changeEffect);
		return changeEffects;

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
	 * @param tr
	 */
	void checkLofExonDeleted(Transcript tr) {
		// First coding exont
		checkLofExonDeletedFirstExon(tr);

		// More than half protein is lost
		checkLofExonDeletedHalf(tr);
	}

	/**
	 * First coding exon produces a LOF
	 * @param tr
	 */
	void checkLofExonDeletedFirstExon(Transcript tr) {
		Exon ex = tr.getFirstCodingExon();
		Variant seqChange = new Variant(tr.getChromosome(), ex.getStart(), "AC", "A");
		seqChange.setStart(ex.getStart());
		seqChange.setEnd(ex.getEnd());
		seqChange.setVariantType(VariantType.DEL);
		if (debug) Gpr.debug("SeqChange:" + seqChange);
		LinkedList<VariantEffect> changeEffects = changeEffects(seqChange, EffectType.EXON_DELETED, ex);

		// Calculate LOF
		LossOfFunction lof = new LossOfFunction(config, changeEffects);
		boolean islof = lof.isLof();
		Assert.assertEquals(true, islof);
	}

	/**
	 * If more than half protein is lost => LOF
	 * @param tr
	 */
	void checkLofExonDeletedHalf(Transcript tr) {
		// Calculate coding part of the transcript
		Marker cds = cdsMarker(tr);

		for (int i = 0; i < NUM_DEL_TEST; i++) {
			// Create a random seqChange
			int delStart = random.nextInt(tr.size() - 1) + tr.getStart();
			int delEnd = random.nextInt(tr.getEnd() - delStart) + delStart + 1;
			Variant seqChange = new Variant(tr.getChromosome(), delStart, "AC", "A");
			seqChange.setStart(delStart);
			seqChange.setEnd(delEnd);
			if (debug) Gpr.debug("SeqChange:" + seqChange);
			seqChange.setVariantType(VariantType.DEL);

			// How many coding bases are affected?
			Marker codingDel = cds.intersect(seqChange);
			if (codingDel != null) { // Does it intersect?
				int numBases = 0;
				for (Exon ex : tr)
					numBases += codingDel.intersectSize(ex);

				// Percent of coding bases?
				double perc = numBases / ((double) tr.cds().length());
				boolean delIsLof = perc > LossOfFunction.DEFAULT_DELETE_PROTEIN_CODING_BASES;

				// Calculate LOF
				LinkedList<VariantEffect> changeEffects = changeEffects(seqChange, EffectType.TRANSCRIPT, tr); // Notice that we don't care what type of effect is, so we just use 'TRANSCRIPT'
				LossOfFunction lof = new LossOfFunction(config, changeEffects);
				boolean islof = lof.isLof();
				Assert.assertEquals(delIsLof, islof);
			}
		}
	}

	/**
	 * Frame shifts are LOF
	 * @param tr
	 */
	void checkLofFrameShift(Transcript tr) {
		Marker cds = cdsMarker(tr);

		int codingBase = 0;

		for (Exon ex : tr.sortedStrand()) {
			int start = tr.isStrandPlus() ? ex.getStart() : ex.getEnd();
			int step = tr.isStrandPlus() ? 1 : -1;

			// All exonic positions
			for (int pos = start; ex.intersects(pos); pos += step) {
				// Create a seqChange
				Variant seqChange;
				boolean ins = random.nextBoolean(); // Randomly choose INS or DEL
				if (ins) seqChange = new Variant(tr.getChromosome(), pos, "A", "AC");
				else seqChange = new Variant(tr.getChromosome(), pos, "AC", "A");
				seqChange.setVariantType(ins ? VariantType.INS : VariantType.DEL);
				if (debug) Gpr.debug("SeqChange:" + seqChange);

				// Create change effect
				LinkedList<VariantEffect> changeEffects = changeEffects(seqChange, EffectType.FRAME_SHIFT, ex);
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
				Assert.assertEquals(isFsLof, islof);
			}
		}
	}

	void checkLofSplice(Transcript tr) {
		// All transcripts in exon
		int exonNum = 0;
		for (Exon ex : tr.sortedStrand()) {
			checkSpliceDonor(tr, ex, exonNum);
			checkSpliceAcceptor(tr, ex, exonNum);
			exonNum++;
		}
	}

	/**
	 * Check that START_LOST is LOF
	 * @param tr
	 */
	void checkLofStartLost(Transcript tr) {
		// Find start codon position
		int pos = tr.getCdsStart();
		Variant seqChange = new Variant(tr.getChromosome(), pos, "A", "C"); // Create a seqChange
		if (debug) Gpr.debug("SeqChange:" + seqChange);

		// Finr exon
		Exon exon = null;
		for (Exon ex : tr)
			if (ex.intersects(pos)) exon = ex;
		if (exon == null) throw new RuntimeEOFException("Cannot find first exon for transcript " + tr.getId());

		// Create a LOF object and analyze the effect
		LinkedList<VariantEffect> changeEffects = changeEffects(seqChange, EffectType.START_LOST, exon);
		LossOfFunction lof = new LossOfFunction(config, changeEffects);
		boolean islof = lof.isLof();
		Assert.assertEquals(true, islof);
	}

	/**
	 * Check that Core Splice Site acceptors are considered LOF
	 * @param tr
	 * @param ex
	 * @param exonNum
	 */
	void checkSpliceAcceptor(Transcript tr, Exon ex, int exonNum) {
		int step = tr.isStrandPlus() ? -1 : +1;
		int intronNum = exonNum - 1; // We care about the intron before

		if (ex.getRank() > 1) {
			// Position
			int posDonor = tr.isStrandPlus() ? ex.getStart() : ex.getEnd();
			posDonor += step;

			// Splice site size
			int maxSize = Math.min(tr.intronSize(intronNum), SpliceSite.CORE_SPLICE_SITE_SIZE);
			if (debug) Gpr.debug("Intron size: " + tr.intronSize(intronNum));
			if (maxSize <= 0) throw new RuntimeEOFException("Max splice size is " + maxSize);

			//---
			// For all position on splice site donor positions, make sure it is LOF
			//---
			for (int pos = posDonor, i = 0; i < maxSize; i++, pos += step) {
				Variant seqChange = new Variant(tr.getChromosome(), pos, "A", "C"); // Create a seqChange
				Marker marker = findMarker(seqChange, EffectType.SPLICE_SITE_ACCEPTOR, null, ex);
				LinkedList<VariantEffect> changeEffects = changeEffects(seqChange, EffectType.SPLICE_SITE_ACCEPTOR, marker); // Create a SPLICE_SITE_ACCEPTOR effect
				if (debug) Gpr.debug("SeqChange:" + seqChange);

				// Create a LOF object and analyze the effect
				LossOfFunction lof = new LossOfFunction(config, changeEffects);
				boolean islof = lof.isLof();
				Assert.assertEquals(true, islof);
			}
		}

	}

	/**
	 * Check that Core Splice Donor acceptors are considered LOF
	 * @param tr
	 * @param ex
	 * @param exonNum
	 */
	void checkSpliceDonor(Transcript tr, Exon ex, int exonNum) {
		int step = tr.isStrandPlus() ? 1 : -1;
		int maxRank = tr.numChilds();
		int intronNum = exonNum; // We care about the intron before

		if (ex.getRank() < maxRank) {
			// Position
			int posDonor = tr.isStrandPlus() ? ex.getEnd() : ex.getStart();
			posDonor += step;

			// Splice site size
			int maxSize = Math.min(tr.intronSize(intronNum), SpliceSite.CORE_SPLICE_SITE_SIZE);
			if (debug) Gpr.debug("Intron size: " + tr.intronSize(intronNum));
			if (maxSize <= 0) throw new RuntimeEOFException("Max splice size is " + maxSize);

			//---
			// For all position on splice site donor positions, make sure it is LOF
			//---
			for (int pos = posDonor, i = 0; i < maxSize; i++, pos += step) {
				Variant seqChange = new Variant(tr.getChromosome(), pos, "A", "C"); // Create a seqChange
				Marker marker = findMarker(seqChange, EffectType.SPLICE_SITE_DONOR, null, ex);
				LinkedList<VariantEffect> changeEffects = changeEffects(seqChange, EffectType.SPLICE_SITE_DONOR, marker); // Create a SPLICE_DONOR effect
				if (debug) Gpr.debug("SeqChange:" + seqChange);

				// Create a LOF object and analyze the effect
				LossOfFunction lof = new LossOfFunction(config, changeEffects);
				boolean islof = lof.isLof();
				Assert.assertEquals(true, islof);
			}
		}

	}

	/**
	 * Find a marker that intersects seqChange
	 * @return
	 */
	Marker findMarker(Variant seqChange, EffectType effectType, Transcript tr, Exon exon) {
		Markers markers = config.getSnpEffectPredictor().query(seqChange);
		for (Marker m : markers) {
			Exon mex = (Exon) m.findParent(Exon.class);
			Transcript mtr = (Transcript) m.findParent(Transcript.class);

			if ((m.getType() == effectType) && (mex != null) && (mtr != null)) {
				if (exon != null) {
					// Exon filter?
					if (mex.getId().equals(exon.getId())) return m;
				} else if (tr != null) {
					// Transcript filter?
					if (mtr.getId().equals(tr.getId())) return m;
				} else return m; // No exon reference? => just return this
			}
		}

		throw new RuntimeEOFException("Cannot find '" + effectType + "' " + (exon != null ? "for exon " + exon.getId() : "") + ", seqChange: " + seqChange);
	}

	public void test_01() {
		Gpr.debug("Test");
		// Load database
		String genomeVer = "testHg3766Chr1";
		Gpr.debug("Loading database '" + genomeVer + "'");
		config = new Config(genomeVer, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		Gpr.debug("Building forest");
		config.setTreatAllAsProteinCoding(true); // For historical reasons...
		config.getSnpEffectPredictor().buildForest();

		// For each gene, transcript, check that NMD works
		Gpr.debug("Testing");
		int i = 1;
		for (Gene gene : config.getGenome().getGenes()) {
			Gpr.showMark(i++, 1);
			for (Transcript tr : gene) {
				if (debug) System.err.println(tr);
				checkLof(tr);
			}
		}
	}
}
