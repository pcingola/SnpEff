package org.snpeff.snpEffect.testCases.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.Hgvs;
import org.snpeff.snpEffect.LossOfFunction;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffect.ErrorWarningType;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.commandLine.SnpEffCmdBuild;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryEmbl;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryGenBank;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryGff3;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryGtf22;
import org.snpeff.snpEffect.factory.SnpEffPredictorFactoryRefSeq;
import org.snpeff.util.Diff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Base class: Provides common methods used for testing
 */
public class TestCasesIntegrationBase {

	public static int SHOW_EVERY = 10;

	public boolean debug = false;
	public boolean verbose = false || debug;

	protected boolean ignoreErrors = false;
	protected boolean shiftHgvs; // Do or do not shift variants according to HGVS notation (for test cases that were created before the feature was implemented)

	public TestCasesIntegrationBase() {
		super();
		init();
	}

	/**
	 * Apply a variant to a transcript
	 */
	public Transcript appyTranscript(String genome, String trId, String vcfFileName) {
		// Load database
		SnpEffectPredictor sep = loadSnpEffectPredictor(genome, false);

		// Find transcript
		Transcript tr = sep.getGenome().getGenes().findTranscript(trId);
		if (tr == null) throw new RuntimeException("Could not find transcript ID '" + trId + "'");

		// Apply first variant
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			for (Variant var : ve.variants()) {
				Transcript trNew = tr.apply(var);
				if (debug) Gpr.debug(trNew);
				return trNew;
			}
		}

		throw new RuntimeException("Could not apply any variant!");
	}

	/**
	 * Build a genome
	 */
	public SnpEffectPredictor build(String genome) {
		String args[] = { "build", genome };
		SnpEff snpeff = new SnpEff(args);
		snpeff.setVerbose(verbose);
		SnpEffCmdBuild snpeffBuild = (SnpEffCmdBuild) snpeff.cmd();
		snpeffBuild.run();
		return snpeffBuild.getConfig().getSnpEffectPredictor();
	}

	/**
	 * Build a genome from a RefSeq file and compare results to 'expected' results
	 */
	public SnpEffectPredictor buildAndCompare(String genome, String refSeqFile, String fastaFile, String resultFile, boolean hideProtein) {
		String expectedResult = Gpr.readFile(resultFile).trim();

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryRefSeq factory = new SnpEffPredictorFactoryRefSeq(config);
		factory.setFileName(refSeqFile);
		factory.setVerbose(verbose);

		// Set fasta file (or don't read sequences)
		if (fastaFile != null) factory.setFastaFile(fastaFile);
		else factory.setReadSequences(false);

		SnpEffectPredictor sep = factory.create();

		// Compare result
		String result = showTranscripts(sep.getGenome(), hideProtein).trim();
		if (verbose) System.out.println(result);
		Assert.assertEquals(Gpr.noSpaces(expectedResult), Gpr.noSpaces(result));

		return sep;
	}

	/**
	 * Build a genome from a embl file and compare results to 'expected' results
	 */
	public SnpEffectPredictor buildEmbl(String genome, String emblFile) {
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryEmbl sepEmbl = new SnpEffPredictorFactoryEmbl(config, emblFile);
		SnpEffectPredictor sep = sepEmbl.create();
		return sep;
	}

	/**
	 * Build a genome from a genbank file and compare results to 'expected' results
	 */
	public SnpEffectPredictor buildGeneBank(String genome, String genBankFile) {
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGenBank sepfg = new SnpEffPredictorFactoryGenBank(config, genBankFile);
		sepfg.setVerbose(verbose);
		SnpEffectPredictor sep = sepfg.create();
		return sep;
	}

	/**
	 * Build a genome from a GFF3 file and compare results to 'expected' results
	 */
	public SnpEffectPredictor buildGff3AndCompare(String genome, String gff3File, String resultFile, boolean readSeqs, boolean createRandSequences) {

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGff3 fgff3 = new SnpEffPredictorFactoryGff3(config);
		fgff3.setVerbose(verbose);
		if (gff3File != null) fgff3.setFileName(gff3File);
		fgff3.setReadSequences(readSeqs);
		fgff3.setCreateRandSequences(createRandSequences);
		fgff3.setRandom(new Random(20140410)); // Note: we want consistent results in our test cases, so we always initialize the random generator in the same way

		SnpEffectPredictor sep = fgff3.create();

		if (resultFile != null) {
			// Compare result
			String result = showTranscripts(sep.getGenome()).trim();
			String expectedResult = Gpr.readFile(resultFile).trim();

			// Remove spaces and compare
			String erNs = Gpr.noSpaces(expectedResult);
			String rNs = Gpr.noSpaces(result);
			if (verbose || !erNs.equals(rNs)) {
				System.out.println("Result:\n----------\n" + result + "\n----------\n");
				System.out.println("Expected (" + resultFile + "):\n----------\n" + expectedResult + "\n----------\n");
				System.out.println(new Diff(expectedResult, result));
			}
			Assert.assertEquals(Gpr.noSpaces(expectedResult), Gpr.noSpaces(result));
		}

		return sep;
	}

	/**
	 * Build a genome from a GTF file and compare results to 'expected' results
	 */
	public void buildGtfAndCompare(String genome, String gtf22, String fastaFile, String resultFile) {
		String expectedResult = Gpr.readFile(resultFile).trim();

		// Build
		Config config = new Config(genome, Config.DEFAULT_CONFIG_FILE);
		SnpEffPredictorFactoryGtf22 fgtf22 = new SnpEffPredictorFactoryGtf22(config);
		fgtf22.setFileName(gtf22);
		fgtf22.setVerbose(verbose);

		// Set fasta file (or don't read sequences)
		if (fastaFile != null) fgtf22.setFastaFile(fastaFile);
		else fgtf22.setReadSequences(false);

		SnpEffectPredictor sep = fgtf22.create();

		// Compare result
		String result = showTranscripts(sep.getGenome(), true).trim();
		if (verbose) System.out.println(result);
		Assert.assertEquals(Gpr.noSpaces(expectedResult), Gpr.noSpaces(result));
	}

	Marker cdsMarker(Transcript tr) {
		int start = tr.isStrandPlus() ? tr.getCdsStart() : tr.getCdsEnd();
		int end = tr.isStrandPlus() ? tr.getCdsEnd() : tr.getCdsStart();
		return new Marker(tr.getParent(), start, end, false, "");
	}

	void checkAnnotations(SnpEffectPredictor sep, String chr, int pos, String ref, String alt, String hgvsP, String hgvsC, String eff) {
		Genome genome = sep.getGenome();
		Variant var = new Variant(genome.getChromosome(chr), pos, ref, alt, "");
		VariantEffects varEffs = sep.variantEffect(var);
		for (VariantEffect varEff : varEffs) {
			VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
			if (verbose) System.out.println("\t" + vcfEff);

			Assert.assertEquals(hgvsP, vcfEff.getHgvsProt());
			Assert.assertEquals(hgvsC, vcfEff.getHgvsDna());
			Assert.assertEquals(eff, vcfEff.getEffectsStrSo());
		}
	}

	/**
	 * Check HGVS annotations
	 */
	public void checkHgvs(String genome, String vcfFile, int minCheck) {
		List<VcfEntry> list = snpEffect(genome, vcfFile, null);

		int countCheck = 0;
		for (VcfEntry ve : list) {
			if (verbose) System.out.println(ve);

			String transcriptId = ve.getInfo("TR");
			if (verbose) System.out.println("\tLooking for transcript '" + transcriptId + "'");
			for (VcfEffect veff : ve.getVcfEffects()) {

				if (veff.getTranscriptId().equals(transcriptId)) {
					if (verbose) {
						System.out.println("\t" + veff);
						System.out.println("\t\tHGVS.p: " + veff.getHgvsP() + "\t\tHGVS.c: " + veff.getHgvsC());
					}

					// Compare against expected result
					String expectedHgvsC = ve.getInfo("HGVSC");
					if (expectedHgvsC != null) {
						String actualHgvsC = veff.getHgvsC();
						Assert.assertEquals("HGVS.c mismatch", expectedHgvsC, actualHgvsC);
						countCheck++;
					}

					String expectedHgvsP = ve.getInfo("HGVSP");
					if (expectedHgvsP != null) {
						String actualHgvsP = veff.getHgvsP();
						Assert.assertEquals("HGVS.p mismatch", expectedHgvsP, actualHgvsP);
						countCheck++;
					}
				}
			}
		}

		if (verbose) System.out.println("Total checked: " + countCheck);
		Assert.assertTrue("Too few variants checked: " + countCheck, countCheck >= minCheck);
	}

	void checkMotif(String genomeVer, String vcfFile, String effectDetails, EffectImpact impact, boolean useAnn) {
		String args[] = { "-classic", "-motif", "-ud", "0", genomeVer, vcfFile };
		String argsAnn[] = { "-ud", "0", genomeVer, vcfFile };
		if (useAnn) args = argsAnn;

		SnpEff cmd = new SnpEff(args);

		// Run
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Check results
		int numNextProt = 0;
		for (VcfEntry ve : vcfEntries) {
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff.getVcfFieldString());

				// Is it motif?
				if (veff.getEffectType() == EffectType.MOTIF) {

					boolean ok = false;
					if (useAnn) {
						// Motif ID and impact match?
						ok = effectDetails.equals(veff.getFeatureId()) && (impact == veff.getImpact());
					} else {
						// Motif ID and impact match?
						ok = effectDetails.equals(veff.getEffectDetails()) && (impact == veff.getImpact());
					}

					if (ok) numNextProt++;
				}
			}
		}

		Assert.assertEquals(1, numNextProt);
	}

	void checkNextProt(String genomeVer, String vcfFile, String effectDetails, EffectImpact impact, boolean useAnn) {
		String args[] = { "-classic", "-nextProt", genomeVer, vcfFile };
		String argsAnn[] = { genomeVer, vcfFile };
		if (useAnn) args = argsAnn;

		SnpEff cmd = new SnpEff(args);
		cmd.setVerbose(verbose);
		cmd.setSupressOutput(!verbose);

		// Run
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		List<VcfEntry> vcfEntries = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Check results
		int numNextProt = 0;
		for (VcfEntry ve : vcfEntries) {
			for (VcfEffect veff : ve.getVcfEffects()) {

				if ((veff.hasEffectType(EffectType.NEXT_PROT)) // Is it nextProt?
						&& (impact == veff.getImpact())) // Is impact OK?
				{
					// Are details OK?
					boolean match = false;
					if (!useAnn && effectDetails.equals(veff.getEffectDetails())) match = true;
					if (useAnn && effectDetails.equals(veff.getFeatureType())) match = true;

					if (match) numNextProt++;
				}

				if (verbose) //
					System.out.println("\t" + veff //
							+ "\n\t\tEffect            : " + veff.getVcfFieldString() //
							+ "\n\t\tEffect type       : " + veff.getEffectType() //
							+ "\n\t\tEffect details    : '" + veff.getEffectDetails() + "'" //
							+ "\n\t\tEffect impact     : '" + veff.getImpact() + "'" //
							+ "\n\t\tExpected details  : '" + effectDetails + "'" //
							+ "\n\t\tExpected impact   : '" + impact + "'" //
							+ "\n\t\tCount matches     : " + numNextProt //
							+ "\thasEffectType : " + veff.hasEffectType(EffectType.NEXT_PROT) //
							+ "\tmatch details : " + effectDetails.equals(veff.getEffectDetails()) //
							+ "\tmatch impact: " + (impact == veff.getImpact()) //
					);
			}
		}

		Assert.assertEquals(1, numNextProt);
	}

	/**
	 * Check that NMD works for a given transcript
	 */
	void checkNmd(Config config, Gene gene, Transcript tr) {
		int pos = 0;
		boolean isNmd[] = new boolean[tr.cds().length()];
		HashSet<Exon> codingExons = new HashSet<>();

		StringBuilder nmdStr = new StringBuilder();
		StringBuilder nmdStrSimple = new StringBuilder();
		for (Exon exon : tr.sortedStrand()) {
			int step = exon.isStrandPlus() ? 1 : -1;
			int from = exon.isStrandPlus() ? exon.getStart() : exon.getEnd();

			for (int expos = from; (exon.getStart() <= expos) && (expos <= exon.getEnd()); expos += step) {
				// Not in UTR? => Test
				if (!tr.isUtr(expos)) {
					codingExons.add(exon);

					// Create a seqChange
					//					SeqChange seqChange = new SeqChange(tr.getChromosome(), expos, expos, "");
					Variant variant = new Variant(tr.getChromosome(), expos, "A", "C"); // Create a seqChange

					// Create a STOP_GAIN effect
					VariantEffect variantEffect = new VariantEffect(variant);
					variantEffect.set(exon, EffectType.STOP_GAINED, EffectType.STOP_GAINED.effectImpact(), "");
					LinkedList<VariantEffect> changeEffects = new LinkedList<>();
					changeEffects.add(variantEffect);

					// Create a LOF object and analyze the effect
					LossOfFunction lof = new LossOfFunction(config, changeEffects);
					isNmd[pos] = lof.isNmd();

					nmdStr.append(isNmd[pos] ? '+' : '.');
					nmdStrSimple.append(isNmd[pos] ? '+' : '.');
					pos++;
				} else nmdStr.append('U');
			}
			nmdStr.append('\t');
			nmdStrSimple.append('\t');
		}

		// Show string
		if (verbose) System.err.println(nmdStr);
		if (debug) System.err.println("\tCoding Exons:" + codingExons.size());

		//---
		// Check that NMP prediction is 'correct'
		//---
		// We need a splice event in the coding part
		if (codingExons.size() > 1) {
			// Use the 'simple' string to check
			StringBuilder sb = new StringBuilder();
			String ex[] = nmdStrSimple.toString().split("\t");
			for (int i = 0; i < (ex.length - 1); i++)
				sb.append(ex[i]);

			// Check that last 50 bases are '.'
			String simpleNoLast = sb.toString();
			int lastNmd = Math.max(0, simpleNoLast.length() - LossOfFunction.MND_BASES_BEFORE_LAST_JUNCTION);
			String points = simpleNoLast.substring(lastNmd) + ex[ex.length - 1];
			String plus = simpleNoLast.substring(0, lastNmd);

			if (debug) System.err.println("\tPoints: " + points + "\n\tPlus :" + plus);

			// Check
			Assert.assertEquals(0, points.replace('.', ' ').trim().length());
			Assert.assertEquals(0, plus.replace('+', ' ').trim().length());
		}
	}

	public void checkNoChange(String args[]) {
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = snpeff.run(true);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				EffectImpact imp = veff.getImpact();
				if (verbose) System.out.println("\t" + imp + "\t" + veff);
				Assert.assertEquals(EffectImpact.MODIFIER, imp);
			}
		}
	}

	/**
	 * Run a predictor and check if the expected warnings appear
	 */
	public void checkTranscriptError(String args[], ErrorWarningType warningType) {
		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff snpeff = (SnpEffCmdEff) cmd.cmd();
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = snpeff.run(true);

		boolean hasWarning = false;
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				EffectImpact imp = veff.getImpact();
				if (verbose) System.out.println("\t" + imp + "\t" + veff);

				// Check if the warning type we expect is there
				if (veff.getErrorsWarning() != null) hasWarning |= veff.getErrorsWarning().indexOf(warningType.toString()) >= 0;
			}
		}

		Assert.assertEquals(true, hasWarning);
	}

	public void compareHgvs(String genome, String vcfFileName) {
		compareHgvs(genome, vcfFileName, true);
	}

	public void compareHgvs(String genome, String vcfFileName, boolean compareProt) {
		// Create SnpEff
		String args[] = { genome, vcfFileName };
		SnpEffCmdEff snpeff = new SnpEffCmdEff();
		snpeff.parseArgs(args);
		snpeff.setDebug(debug);
		snpeff.setVerbose(verbose);
		snpeff.setSupressOutput(!verbose);
		snpeff.setUpDownStreamLength(0);
		snpeff.setShiftHgvs(shiftHgvs);
		snpeff.setFormatVersion(EffFormatVersion.FORMAT_EFF_4);

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		Set<String> trNotFoundSet = new HashSet<>();

		// Make sure entries are annotated as expected
		int countOkC = 0, countErrC = 0, countOkP = 0, countErrP = 0, countTrFound = 0;
		for (VcfEntry ve : results) {
			// Extract expected HGVS values
			String hgvsCexp = ve.getInfo("HGVS_C") != null ? ve.getInfo("HGVS_C") : "";
			String trIdC = Hgvs.parseTranscript(hgvsCexp);
			hgvsCexp = Hgvs.removeTranscript(hgvsCexp);

			String hgvsPexp = "";
			String trIdP = "";
			if (compareProt) {
				hgvsPexp = ve.getInfo("HGVS_P") != null ? ve.getInfo("HGVS_P") : "";
				trIdP = Hgvs.parseTranscript(hgvsPexp);
				hgvsPexp = Hgvs.removeTranscript(hgvsPexp);
			}

			if (verbose) {
				System.out.println(ve);
				if (trIdC != null) System.out.println("\tExpected HGVS_C: " + trIdC + ":" + hgvsCexp);
				if (trIdP != null) System.out.println("\tExpected HGVS_P: " + trIdP + ":" + hgvsPexp + "\n");
			}

			// Check all effects
			boolean okC = false, okP = false, trFound = false;
			for (VcfEffect veff : ve.getVcfEffects()) {
				// Parse calculated HGVS values
				String trId = veff.getTranscriptId();
				String hgvsCactual = veff.getHgvsDna() != null ? veff.getHgvsDna() : "";
				String hgvsPactual = veff.getHgvsProt() != null ? veff.getHgvsProt() : "";

				// Compare results for HGVS_DNA
				boolean foundC = false, foundP = false;
				if (trId != null && trId.equals(trIdC)) {
					trFound = true;
					if (!hgvsCexp.equals(hgvsCactual)) {
						if (!ignoreErrors) Assert.assertEquals(hgvsCexp, hgvsCactual);
						countErrC++;
					} else {
						okC = foundC = true;
						countOkC++;
					}
				}

				// Compare results for HGVS_PROT
				if (compareProt && trId != null && trId.equals(trIdP)) {
					if (!hgvsPexp.equals(hgvsPactual)) {
						if (!ignoreErrors) Assert.assertEquals(hgvsPexp, hgvsPactual);
						countErrP++;
					} else {
						okP = foundP = true;
						countOkP++;
					}
				}

				if (verbose) {
					System.out.println("\t" + veff //
							+ "\n\t\tEFF    : " + veff.getEffectsStr() //
							+ "\n\t\tHGVS_C : " + trId + ":" + hgvsCactual + "\t\tExpected: " + trIdC + ":" + hgvsCexp + "\t" + (foundC ? "OK" : "NO") //
							+ (compareProt ? "\n\t\tHGVS_P : " + trId + ":" + hgvsPactual + "\t\tExpected: " + trIdP + ":" + hgvsPexp + "\t" + (foundP ? "OK" : "NO") : "") //
							+ "\n");
				}

			}

			if (!trFound) {
				System.out.println("Transcript '" + trIdC + "' not found.");
				countTrFound++;
				trNotFoundSet.add(trIdC);
			}

			if (!ignoreErrors) {
				Assert.assertTrue("HGVS (DNA) not found: '" + hgvsCexp + "'", okC);
				if (!hgvsPexp.isEmpty()) Assert.assertTrue("HGVS (Protein) not found: '" + hgvsPexp + "'", okP);
			} else {
				// Show errors
				if (!okC) System.err.println("HGVS (DNA) not found : '" + hgvsCexp + "', vcf entry:\t" + ve);
				if (compareProt && !okP) System.err.println("HGVS (Prot) not found: '" + hgvsPexp + "', vcf entry:\t" + ve);
			}
		}

		if (verbose || ignoreErrors) {
			System.out.println("Count OKs   :\tHGVS (DNA): " + countOkC + "\tHGVS (Protein): " + countOkP);
			System.out.println("Count Errors:\tHGVS (DNA): " + countErrC + "\tHGVS (Protein): " + countErrP);
			System.out.println("Transcripts not found:\t" + countTrFound + ", unique: " + trNotFoundSet.size() + "\n" + trNotFoundSet);
		}
	}

	/**
	 * Compare with results from ENSEMBL's VEP on transcript ENST00000268124
	 */
	public void compareVep(String genome, String vcf, String trId) {
		String args[] = { "-classic", genome, vcf };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setSupressOutput(!verbose);

		List<VcfEntry> vcfEnties = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		for (VcfEntry ve : vcfEnties) {

			StringBuilder msg = new StringBuilder();

			// Check effects
			boolean ok = false;
			for (VcfEffect veff : ve.getVcfEffects()) {
				// Find transcript
				if (veff.getTranscriptId() != null && veff.getTranscriptId().equals(trId)) {
					// Check that reported effect is the same
					String vep = ve.getInfo("EFF_V");
					String eff = veff.getEffectType().toString();

					if (vep.equals(eff)) ok = true;
					else {
						if (vep.equals("CODON_INSERTION") && eff.equals("CODON_CHANGE_PLUS_CODON_INSERTION")) ok = true; // OK. I consider these the same
						else if (vep.equals("STOP_GAINED,CODON_INSERTION") && eff.equals("STOP_GAINED")) ok = true; // OK. I consider these the same
						else if (eff.equals("SPLICE_SITE_REGION")) ok = true; // OK. I'm not checking these
						else {
							String line = "\n" + ve + "\n\tSnpEff:" + veff + "\n\tVEP   :" + ve.getInfo("EFF_V") + "\t" + ve.getInfo("AA") + "\t" + ve.getInfo("CODON") + "\n";
							msg.append(line);
						}
					}
				}
			}

			if (!ok) throw new RuntimeException(msg.toString());
		}
	}

	/**
	 * Benchmarking: Compare with results from ENSEMBL's VEP
	 */
	public void compareVepSO(String genome, String vcf, String trId) {
		String args[] = { "-classic", "-sequenceOntology", genome, vcf };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		List<VcfEntry> vcfEnties = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		for (VcfEntry ve : vcfEnties) {
			// Create a set of found variants
			HashSet<String> vepSos = new HashSet<>();
			String vepSo = ve.getInfo("SO");
			for (String so : vepSo.split(",")) {
				if (so.equals("feature_elongation")) so = null; // This one is useless, if the variant is an insertion, it obviously causes an elongation
				else if (so.equals("feature_truncation")) so = null; // This one is useless, if the variant is an insertion, it obviously causes an elongation

				if (so != null) vepSos.add(so);
			}

			// Get effects for transcript 'trId'
			HashSet<String> effSos = new HashSet<>();
			List<VcfEffect> veffs = ve.getVcfEffects();
			for (VcfEffect veff : veffs) {
				if (veff.getTranscriptId().equals(trId)) {
					String effs = veff.getEffString();

					for (String eff : effs.split("\\" + EffFormatVersion.EFFECT_TYPE_SEPARATOR_OLD)) {
						// OK. I consider these the same
						if (eff.equals("5_prime_UTR_premature_start_codon_gain_variant")) eff = "5_prime_UTR_variant";
						if (eff.equals("disruptive_inframe_insertion")) eff = "inframe_insertion";
						if (eff.equals("conservative_inframe_insertion")) eff = "inframe_insertion";
						if (eff.equals("start_lost")) eff = "initiator_codon_variant";
						effSos.add(eff);
					}
				}
			}

			// Make sure both sets are equal
			boolean error = !effSos.containsAll(vepSos);

			if (error) {
				String msg = "\n" + ve;
				msg += "\n\tSnpEff    : ";
				for (String e : effSos)
					msg += e + " ";

				msg += "\n\tVEP       : ";
				for (String e : vepSos)
					msg += e + " ";

				msg += "\n\tMarker    : " + ve.getChromosomeName() + ":" + ve.getStart() + "-" + ve.getEnd();
				Gpr.debug(msg);
				throw new RuntimeException(msg);
			}

		}
	}

	/**
	 * Find a marker that intersects variant
	 */
	Marker findMarker(SnpEffectPredictor sep, Variant variant, EffectType effectType, Transcript tr, Marker markerFilter) {
		Markers markers = sep.queryDeep(variant);

		for (Marker m : markers) {
			Marker mfilter = null;
			if (markerFilter != null) mfilter = (Marker) m.findParent(markerFilter.getClass());

			Transcript mtr = (Transcript) m.findParent(Transcript.class);

			if (debug) Gpr.debug("\tLooking for '" + effectType + "' in '" + (markerFilter != null ? markerFilter.getId() : "NULL") //
					+ "', class: " + (markerFilter != null ? markerFilter.getClass().getSimpleName() : "") //
					+ "\t\tFound: '" + m.getType() + "', mfilter: " + (mfilter != null ? mfilter.getId() : "NULL") //
					+ ", parent: " + m.getParent().getClass().getSimpleName() //
			);

			if ((m.getType() == effectType) && (mfilter != null) && (mtr != null)) {
				if (markerFilter != null) {
					// Id filter?
					if (mfilter.getId().equals(markerFilter.getId())) return m;
				} else if (tr != null) {
					// Transcript filter?
					if (mtr.getId().equals(tr.getId())) return m;
				} else return m; // No exon reference? => just return this
			}
		}

		throw new RuntimeException("Cannot find '" + effectType + "' " + (markerFilter != null ? "for exon " + markerFilter.getId() : "") + ", seqChange: " + variant);
	}

	public void init() {
		// Nothing done
	}

	/**
	 * Load predictor and create interval forest
	 */
	public SnpEffectPredictor loadSnpEffectPredictor(String genome, boolean build) {
		Config config = new Config(genome);
		SnpEffectPredictor sep = SnpEffectPredictor.load(config);
		sep.createGenomicRegions();
		if (build) sep.buildForest();
		return sep;
	}

	public String showTranscripts(Genome genome) {
		return showTranscripts(genome, false);
	}

	/**
	 * Show a genome in a 'standard' way
	 */
	public String showTranscripts(Genome genome, boolean hideProtein) {
		StringBuilder sb = new StringBuilder();

		// Genome
		sb.append(genome.getVersion() + "\n");

		// Chromosomes
		for (Chromosome chr : genome)
			sb.append(chr + "\n");

		// Genes
		ArrayList<Gene> genes = new ArrayList<>();

		// Sort genes
		for (Gene gene : genome.getGenes())
			genes.add(gene);
		Collections.sort(genes);

		// We don't compare protein codding in this test
		// Show genes
		for (Gene gene : genes) {

			if (hideProtein) { // Don't show protein information
				for (Transcript tr : gene.sortedStrand())
					tr.setProteinCoding(false);
			}

			sb.append(gene);

			for (Transcript tr : gene.sortedStrand()) {
				sb.append("\t\tCDS '" + tr.getId() + "': " + tr.cds() + "\n");
			}
		}

		return sb.toString();
	}

	/**
	 * Calculate snp effect for an input VCF file
	 */
	public List<VcfEntry> snpEffect(String genome, String vcfFile, String otherArgs[]) {
		return snpEffect(genome, vcfFile, otherArgs, EffFormatVersion.FORMAT_EFF_4);
	}

	/**
	 * Calculate snp effect for an input VCF file
	 */
	public List<VcfEntry> snpEffect(String genome, String vcfFile, String otherArgs[], EffFormatVersion effFormatVersion) {
		// Arguments
		ArrayList<String> args = new ArrayList<>();
		if (otherArgs != null) {
			for (String a : otherArgs)
				args.add(a);
		}
		args.add(genome);
		args.add(vcfFile);

		SnpEff cmd = new SnpEff(args.toArray(new String[0]));
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);
		if (effFormatVersion != null) cmdEff.setFormatVersion(effFormatVersion);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		return list;
	}

	/**
	 * Calculate snp effect for a list of snps using cancer samples
	 */
	public void snpEffectCancer(String vcfFile, String txtFile, String aaHgsv, String genotype) {
		// Create command
		String argsVcf[] = { "-classic", "-cancer", "-hgvs", "testHg3766Chr1", vcfFile };
		String argsTxt[] = { "-classic", "-cancer", "-cancerSamples", txtFile, "-hgvs", "testHg3766Chr1", vcfFile };
		String args[] = (txtFile == null ? argsVcf : argsTxt);

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.cmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		Assert.assertTrue("Errors while executing SnpEff", cmdEff.getTotalErrs() <= 0);

		// Find AA change for a genotype
		boolean found = false;
		for (VcfEntry vcfEntry : list) {
			if (debug) System.err.println(vcfEntry);
			for (VcfEffect eff : vcfEntry.getVcfEffects()) {
				if (debug) System.err.println("\t" + eff + "\n\t\tAA : " + eff.getAa() + "\n\t\tGenotype: " + eff.getGenotype());
				if (genotype.equals(eff.getGenotype())) {
					Assert.assertEquals(aaHgsv, eff.getAa());
					found = true;
				}
			}
		}

		// Not found? Error
		if (!found) throw new RuntimeException("Genotype '" + genotype + "' not found.");
	}

	/**
	 * Create change effects
	 */
	LinkedList<VariantEffect> variantEffects(Variant variant, EffectType effectType, Marker marker) {
		VariantEffect changeEffect = new VariantEffect(variant);
		changeEffect.set(marker, effectType, effectType.effectImpact(), "");
		LinkedList<VariantEffect> changeEffects = new LinkedList<>();
		changeEffects.add(changeEffect);
		return changeEffects;

	}

}
