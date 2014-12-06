package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.Hgvs;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBase;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCasesBase {

	boolean ignoreErrors = false;

	public TestCasesZzz() {
		super();
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

		// Run & get result (single line)
		List<VcfEntry> results = snpeff.run(true);
		Set<String> trNotFoundSet = new HashSet<String>();

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
			for (VcfEffect veff : ve.parseEffects()) {
				// Parse calculated HGVS values
				String trId = veff.getTranscriptId();
				String hgvsCactual = veff.getHgvsDna() != null ? veff.getHgvsDna() : "";
				String hgvsPactual = veff.getHgvsProt() != null ? veff.getHgvsProt() : "";
				if (verbose) System.out.println("\t" + veff //
						+ "\n\t\tEFF    : " + veff.getEffectsStr() //
						+ "\n\t\tHGVS_C : " + trId + ":" + hgvsCactual + "\t\tExpected: " + trIdC + ":" + hgvsCexp //
						+ (compareProt ? "\n\t\tHGVS_P : " + trId + ":" + hgvsPactual + "\t\tExpected: " + trIdP + ":" + hgvsPexp : "") //
						+ "\n");

				// Compare results for HGVS_DNA
				if (trId != null && trId.equals(trIdC)) {
					trFound = true;
					if (!hgvsCexp.equals(hgvsCactual)) {
						if (!ignoreErrors) Assert.assertEquals(hgvsCexp, hgvsCactual);
						countErrC++;
					} else {
						okC = true;
						countOkC++;
					}
				}

				// Compare results for HGVS_PROT
				if (compareProt && trId != null && trId.equals(trIdP)) {
					if (!hgvsPexp.equals(hgvsPactual)) {
						if (!ignoreErrors) Assert.assertEquals(hgvsPexp, hgvsPactual);
						countErrP++;
					} else {
						okP = true;
						countOkP++;
					}
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

	@Override
	protected void init() {
		super.init();
		shiftHgvs = true;
	}

	//	@Test
	//	public void test_hgvs_md() {
	//		Gpr.debug("Test");
	//		verbose = true;
	//		String genome = "testHg19Chr1";
	//		String vcf = "tests/hgvs_md.chr1.vcf";
	//		compareHgvs(genome, vcf, false);
	//	}
	//
	//	@Test
	//	public void test_hgvs_md_2() {
	//		Gpr.debug("Test");
	//		String genome = "testHg19Chr13";
	//		String vcf = "tests/hgvs_md.chr13.vcf";
	//		compareHgvs(genome, vcf);
	//	}
	//
	//	@Test
	//	public void test_hgvs_md_3() {
	//		Gpr.debug("Test");
	//
	//		ignoreErrors = true;
	//
	//		String genome = "testHg19Chr17";
	//		String vcf = "tests/hgvs_md.chr17.vcf";
	//		compareHgvs(genome, vcf, false);
	//	}

	@Test
	public void test_zzz() {
		Gpr.debug("Test");

		ignoreErrors = true;

		String genome = "testHg19Chr17";
		String vcf = "tests/zzz.vcf";
		compareHgvs(genome, vcf, false);
	}

}
