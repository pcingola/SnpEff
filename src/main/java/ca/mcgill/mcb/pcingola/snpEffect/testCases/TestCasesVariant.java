package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.VariantFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VariantTxtFileIterator;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.interval.codonChange.CodonChange;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGtf22;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * 
 * Test cases used:
 * 
   Transcript: ENST00000250823
  
		Y   16167997    UPSTREAM
		Y   16168096    UPSTREAM
		Y   16168097    5PRIME_UTR
		Y   16168169    5PRIME_UTR
		Y   16168170    EXON
		Y   16168271    EXON
		Y   16168272    INTRON
		Y   16168463    INTRON
		Y   16168464    EXON
		Y   16168739    EXON
		Y   16168740    3PRIME_UTR
		Y   16168838    3PRIME_UTR
		Y   16168839    DOWNSTREAM
		Y   16168940    DOWNSTREAM

		Note: Coordinates in the following diagram are 16160000 + X
		
		7997       8096               8170       8271               8464       8739               8839       8938
		|-----UP------||-----5'------||-----EX------||-----IN------||-----EX------||-----3'------||-----DO------|
		7997           8097       8169               8272       8463               8740       8838

 * 
 * @author pcingola
 */
public class TestCasesVariant extends TestCase {

	boolean verbose = false;
	boolean createOutputFile = false;
	Random rand;
	Config config;
	Genome genome;

	public TestCasesVariant() {
		super();
		initRand();
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
	}

	/** 
	 * Compare each result. If one matches, we consider it OK
	 * @param transcriptId
	 * @param variant
	 * @param changeEffects
	 * @param useSimple
	 * @param resultsSoFar
	 * @return
	 */
	boolean anyResultMatches(String transcriptId, Variant variant, VariantEffects changeEffects, boolean useShort) {
		for (VariantEffect chEff : changeEffects) {
			String resStr = chEff.toStringSimple(useShort);

			Transcript tr = chEff.getTranscript();
			if (tr != null) {
				if ((transcriptId == null) || (transcriptId.equals(tr.getId()))) {
					if (resStr.indexOf(variant.getId()) >= 0) return true; // Matches one result in this transcript
				}
			} else if (resStr.indexOf(variant.getId()) >= 0) return true; // Matches any result (out of a transcript)
		}
		return false;
	}

	void initRand() {
		rand = new Random(20100629);
	}

	void initSnpEffPredictor() {
		initSnpEffPredictor("testCase");
	}

	void initSnpEffPredictor(String genomeName) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		config.setTreatAllAsProteinCoding(true); // For historical reasons we set this one to 'true'....
		genome = config.getGenome();
		config.getSnpEffectPredictor().buildForest();
	}

	/**
	 * Parse a variant file and return a list
	 * 
	 * @param variantFile
	 * @return
	 */
	public List<Variant> parseSnpEffectFile(String variantFile) {
		ArrayList<Variant> variants = new ArrayList<Variant>();

		VariantTxtFileIterator variantFileIterator = new VariantTxtFileIterator(variantFile, config.getGenome());
		for (Variant sc : variantFileIterator)
			variants.add(sc);

		Collections.sort(variants);
		return variants;
	}

	/**
	 * Calculate snp effect for a list of snps
	 * @param snpEffFile
	 */
	public void snpEffect(List<Variant> variantList, String transcriptId, boolean useShort, boolean negate) {
		int num = 1;
		// Predict each variant
		for (Variant variant : variantList) {
			// Get results for each snp
			VariantEffects results = config.getSnpEffectPredictor().variantEffect(variant);

			String msg = "";
			msg += "Number : " + num + "\n";
			msg += "\tExpecting   : " + (negate ? "NOT " : "") + "'" + variant.getId() + "'\n";
			msg += "\tvariant   : " + variant + "\n";
			msg += "\tResultsList :\n";
			for (VariantEffect res : results)
				msg += "\t" + res + "\n";

			if (verbose) System.out.println(msg);

			// Compare each result. If one matches, we consider it OK
			// StringBuilder resultsSoFar = new StringBuilder();
			boolean ok = anyResultMatches(transcriptId, variant, results, useShort);
			ok = negate ^ ok; // Negate? (i.e. when we are looking for effects that should NOT be matched)

			if (!ok) {
				if (createOutputFile) {
					for (VariantEffect res : results) {
						Variant sc = res.getVariant();
						System.out.println(sc.getChromosomeName() //
								+ "\t" + (sc.getStart() + 1) //
								+ "\t" + sc.getReference() //
								+ "\t" + sc.getChange() //
								+ "\t+\t0\t0" //
								+ "\t" + res.effect(true, true, true, false) //
						);
					}
				} else {
					Gpr.debug(msg);
					throw new RuntimeException(msg);
				}
			}
			num++;
		}
	}

	/**
	 * Read snps from a file and compare them to 'out' SnpEffect predictor.
	 * Make sure at least one effect matched the 'id' in the input TXT file
	 */
	public void snpEffect(String snpEffFile, String transcriptId, boolean useShort) {
		List<Variant> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, false); // Predict each snp
	}

	/**
	 * Read snps from a file and compare them to 'out' SnpEffect predictor.
	 * Make sure NOT A SINGLE effect matched the 'id' in the input TXT file, i.e. the opposite of snpEffect...) method.
	 */
	public void snpEffectNegate(String snpEffFile, String transcriptId, boolean useShort) {
		List<Variant> snplist = parseSnpEffectFile(snpEffFile); // Read SNPs from file
		snpEffect(snplist, transcriptId, useShort, true); // Predict each snp
	}

	/**
	 * CDS test (CDS = CoDing Sequences)
	 * Build CDS form exon sequences
	 */
	public void test_08() {
		initSnpEffPredictor();

		// Read CDS (hg37, chromosome Y) from a file and store them indexed by transcript ID
		HashMap<String, String> cdsByTrId = new HashMap<String, String>();
		String cdsY = Gpr.readFile("./tests/cds_hg37_chrY.txt");
		String lines[] = cdsY.split("\n");
		for (String line : lines) {
			String recs[] = line.split("\t");
			cdsByTrId.put(recs[0], recs[1]);
		}

		// Calculate CDS from chromosome Y and compare
		int totalOk = 0;
		for (Gene gint : config.getGenome().getGenes()) {
			for (Transcript tint : gint) {
				String seqOri = cdsByTrId.get(tint.getId());

				if (seqOri != null) {
					String seq = tint.cds();
					// Compare CDS sequences
					if (!seqOri.equalsIgnoreCase(seq)) throw new RuntimeException("CDS do not match:\nTranscipt:" + tint.getId() + " " + tint.isStrandMinus() + "\n\t" + seq + "\n\t" + seqOri + "\n");
					else {
						if (verbose) System.out.println("CDS compare:\n\t" + seqOri + "\n\t" + seq);
						totalOk++;
					}
				}
			}
		}
		if (totalOk == 0) throw new RuntimeException("No sequences compared!");
	}

	/**
	 * Test SNP effect predictor for a transcript
	 */
	public void test_09() {
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + ".out", trId, true);
	}

	/**
	 * Test SNP effect predictor: Test UTR distances, Up/Downstream distances
	 */
	public void test_11() {
		initSnpEffPredictor();
		CodonChange.showCodonChange = false;
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + "_all.out", trId, false);
	}

	/**
	 * Test SNP effect predictor: Test Splice sites
	 */
	public void test_12() {
		initSnpEffPredictor();
		snpEffect("tests/splice_site.out", null, false);
	}

	/**
	 * Test SNP effect predictor: Test Splice sites (make sure they are only 2 bases long)
	 */
	public void test_12_2() {
		initSnpEffPredictor();
		snpEffect("tests/splice_site_2.out", null, false);
	}

	/**
	 * Test SNP effect predictor: Test start codon gained
	 */
	public void test_19() {
		initSnpEffPredictor();
		String trId = "ENST00000439108";
		snpEffect("tests/" + trId + ".snps", trId, false);
	}

	/**
	 * Test SNP effect predictor: Test start codon gained (reverse strand)
	 */
	public void test_20() {
		initSnpEffPredictor();
		String trId = "ENST00000382673";
		snpEffect("tests/" + trId + ".snps", trId, false);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_21() {
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + "_InDels.out", trId, false);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_21_2() {
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + "_InDels_2.out", trId, false);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_21_3() {
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + "_InDels_3.out", trId, true);
	}

	/**
	 * Read file test: Should throw an exception (chromosome not found)
	 */
	public void test_22() {
		initSnpEffPredictor();

		VariantFileIterator snpFileIterator;
		snpFileIterator = new VariantTxtFileIterator("tests/chr_not_found.out", config.getGenome());
		snpFileIterator.setIgnoreChromosomeErrors(false);

		boolean trown = false;
		try {
			// Read all SNPs from file. Note: This should throw an exception "Chromosome not found"
			for (Variant variant : snpFileIterator) {
				Gpr.debug(variant);
			}
		} catch (RuntimeException e) {
			trown = true;
			String expectedMessage = "ERROR: Chromosome 'chrZ' not found! File 'tests/chr_not_found.out', line 1";
			if (e.getMessage().equals(expectedMessage)) ; // OK
			else throw new RuntimeException("This is not the exception I was expecting!\n\tExpected message: '" + expectedMessage + "'\n\tMessage: '" + e.getMessage() + "'", e);
		}

		// If no exception => error
		if (!trown) throw new RuntimeException("This should have thown an exception 'Chromosome not found!' but it didn't");
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_23_MNP_on_exon_edge() {
		initSnpEffPredictor();
		String trId = "ENST00000250823";
		snpEffect("tests/" + trId + "_mnp_out_of_exon.txt", trId, true);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_24_delete_exon_utr() {
		initSnpEffPredictor();
		snpEffect("tests/delete_exon_utr.txt", null, true);
	}

	public void test_25_exon_bases() {
		System.out.println("Loading config file");
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		System.out.println("Loading fasta file");
		String fastaFile = "tests/testCase.fa";
		String seq = GprSeq.fastaSimpleRead(fastaFile);

		// Test all bases in all exons
		int countOk = 0, countErr = 0;
		for (Gene gint : config.getGenome().getGenes()) {
			for (Transcript tr : gint) {
				System.out.println("Transcript: " + tr.getId());
				List<Exon> exons = tr.sortedStrand();
				for (Exon exon : exons) {
					for (int i = exon.getStart(); i <= exon.getEnd(); i++) {
						String base = seq.substring(i, i + 1);
						String exonBase = exon.basesAt(i - exon.getStart(), 1);

						if (base.equalsIgnoreCase(exonBase)) {
							countOk++;
						} else {
							countErr++;
							String msg = "ERROR:\tPosition: " + i + "\tExpected: " + base + "\tGot: " + exonBase;
							Gpr.debug(msg);
							throw new RuntimeException(msg);
						}
					}
				}
			}
		}

		System.out.println("Count OK: " + countOk + "\tCount Err: " + countErr);
	}

	/**
	 * Test SNP effect predictor for a transcript (Insertions)
	 */
	public void test_26_chr15_78909452() {
		initSnpEffPredictor("testHg3761Chr15");
		snpEffect("tests/chr15_78909452.txt", null, true);
	}

	/**
	 * Splice site: Bug reported by Wang, Xusheng 
	 */
	public void test_28_Splice_mm37_ENSMUSG00000005763() {
		//---
		// Build snpEffect
		//---
		String gtfFile = "tests/ENSMUSG00000005763.gtf";
		String genome = "testMm37.61";

		config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGtf22 fgtf22 = new SnpEffPredictorFactoryGtf22(config);
		fgtf22.setFileName(gtfFile);
		fgtf22.setReadSequences(false); // Don't read sequences
		SnpEffectPredictor snpEffectPredictor = fgtf22.create();
		config.setSnpEffectPredictor(snpEffectPredictor);

		// Set chromosome size (so that we don't get an exception)
		for (Chromosome chr : config.getGenome())
			chr.setEnd(1000000000);

		//---
		// Calculate effect
		//---
		snpEffectPredictor.buildForest();
		snpEffect("tests/ENSMUSG00000005763.out", null, true);
	}

	/**
	 * Test effect when hits a gene, but not any transcript within a gene. 
	 * This is an extremely weird case, might be an annotation problem.
	 */
	public void test_29_Intergenic_in_Gene() {
		initSnpEffPredictor("testHg3763Chr20");
		snpEffect("tests/warren.eff.missing.chr20.txt", null, true);
	}

	/**
	 * Rare Amino acid
	 */
	public void test_30_RareAa() {
		initSnpEffPredictor("testHg3765Chr22");
		snpEffect("tests/rareAa.txt", null, true);
	}

	/**
	 * MT chromo
	 */
	public void test_31_CodonTable() {
		initSnpEffPredictor("testHg3767Chr21Mt");
		snpEffect("tests/mt.txt", null, true);
	}

	/**
	 * Start gained
	 */
	public void test_32_StartGained() {
		initSnpEffPredictor("testHg3769Chr12");
		snpEffect("tests/start_gained_test.txt", null, true);
	}

	/**
	 * Not start gained
	 */
	public void test_33_StartGained_NOT() {
		initSnpEffPredictor("testHg3769Chr12");
		snpEffectNegate("tests/start_gained_NOT_test.txt", null, true);
	}

	/**
	 * Start gained
	 */
	public void test_34_StartGained() {
		initSnpEffPredictor("testHg3766Chr1");
		snpEffect("tests/start_gained_test_2.txt", null, true);
	}

	/**
	 * Not start gained
	 */
	public void test_35_StartGained_NOT() {
		initSnpEffPredictor("testHg3766Chr1");
		snpEffectNegate("tests/start_gained_NOT_test_2.txt", null, true);
	}

	/**
	 * Make sure all variant effects have appropriate impacts
	 */
	public void test_36_EffectImpact() {
		Chromosome chr = new Chromosome(null, 0, 1, "1");
		Variant var = new Variant(chr, 1, "A", "C");
		var.setChangeType(VariantType.SNP);

		System.out.println(var);
		for (EffectType eff : EffectType.values()) {
			VariantEffect varEff = new VariantEffect(var);
			varEff.setEffectType(eff);
			System.out.println(var.isVariant() + "\t" + eff + "\t" + varEff.getEffectImpact());
		}
	}

}
