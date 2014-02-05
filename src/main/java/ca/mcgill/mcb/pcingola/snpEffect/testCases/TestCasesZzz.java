package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	/**
	 * Create a file to send to ENSEMBL's VEP.
	 * Used for benchmarking
	 * 
	 * @throws IOException
	 */
	public static void create_ENST00000268124_SNP_file() throws IOException {
		Config config = new Config("testENST00000268124", Gpr.HOME + "/snpEff/" + Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		Random rand = new Random(20140129);
		StringBuilder out = new StringBuilder();

		int count = 0;
		for (Gene g : config.getGenome().getGenes()) {
			for (Transcript tr : g) {
				for (Exon e : tr) {
					for (int i = e.getStart(); i < e.getEnd(); i++) {
						if (rand.nextDouble() < 0.15) {

							// Insertion length
							int idx = i - e.getStart();

							// Find 'REF'
							String ref = e.basesAt(idx, 1);

							// Create 'ALT'
							String alt = ref;
							while (alt.equals(ref))
								alt = GprSeq.randSequence(rand, 1);

							// Output in 'VCF' format
							String line = e.getChromosomeName() + "\t" + (i + 1) + "\t.\t" + ref + "\t" + alt + "\t.\t.\tAC=1\tGT\t0/1";
							System.out.println(line);
							out.append(line + "\n");
							count++;
						}
					}
				}
			}
		}

		System.err.println("Count:" + count);
		String outFile = "./tests/testENST00000268124.SNP.ORI.vcf";
		System.out.println("Output file: " + outFile);
		Gpr.toFile(outFile, out);
	}

	public TestCasesZzz() {
		super();
	}

	/**
	 * Benchmarking: Compare with results from ENSEMBL's VEP 
	 */
	public void compareVepSO(String genome, String vcf, String trId) {
		String args[] = { "-sequenceOntolgy", genome, vcf };

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();

		List<VcfEntry> vcfEnties = cmdEff.run(true);
		for (VcfEntry ve : vcfEnties) {

			// Get first effect (there should be only one)
			List<VcfEffect> veffs = ve.parseEffects();
			VcfEffect veff = null;
			for (VcfEffect v : veffs)
				if (v.getTranscriptId().equals(trId)) veff = v;

			//---
			// Check that reported effect is the same
			//---
			String vepSo = ve.getInfo("SO");
			String eff = veff.getEffString();
			if (!vepSo.equals(eff)) {
				//				if (vepSo.equals("CODON_INSERTION") && eff.equals("CODON_CHANGE_PLUS_CODON_INSERTION")) ; // OK. I consider these the same
				//				else if (vepSo.equals("STOP_GAINED,CODON_INSERTION") && eff.equals("STOP_GAINED")) ; // OK. I consider these the same
				//				else {
				String msg = "\n" + ve + "\n\tSnpEff:" + veff.getEffectString() + "\n\tVEP   :" + ve.getInfo("SO") + "\t" + ve.getInfo("AA") + "\t" + ve.getInfo("CODON");
				Gpr.debug(msg);
				throw new RuntimeException(msg);
				//				}
			}

			//---
			// Check that AA is the same
			//---
			String aa = veff.getAa();
			String vepaa = ve.getInfo("AA");
			if (aa == null && vepaa.equals("-")) ; // OK, test passed
			else {
				String aas[] = aa.split("[0-9]+");
				String aav = aas[0] + (aas.length > 1 ? "/" + aas[1] : "");

				// Convert from 'Q/QLV' to '-/LV'
				String aav2 = "";
				if ((aas[0].length() > 1) && (aas[1].startsWith(aas[0]))) aav2 = "-/" + aas[1].substring(1);
				if ((aas[0].length() > 1) && (aas[1].endsWith(aas[0]))) aav2 = "-/" + aas[1].substring(0, aas[1].length() - 1);

				if (aav.equals(vepaa)) ; // OK, test passed
				else if (aav2.equals(vepaa)) ; // OK, test passed
				else if (aav.endsWith("?") && vepaa.equals("-")) ; // OK, test passed
				else {
					Gpr.debug(aa + " (" + aav + ")\t" + vepaa + "\t");
				}
			}

		}
	}

	public void test_05_Vep() throws IOException {
		// create_ENST00000268124_SNP_file();
		compareVepSO("testENST00000268124", "tests/testENST00000268124.SNP.vcf", "ENST00000268124");
	}

}
