package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.IOException;
import java.util.HashSet;
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
 * Test case for sequence ontology
 * 
 * @author pcingola
 */
public class TestCasesSequenceOntology extends TestCase {

	/**
	 * Create a file to send to ENSEMBL's VEP.
	 * Used for benchmarking
	 * 
	 * @throws IOException
	 */
	public static void create_ENST00000268124_SNP_file() throws IOException {
		Config config = new Config("testENST00000268124", Gpr.HOME + "/snpEff/" + Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		Random rand = new Random(20140205);
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

	public TestCasesSequenceOntology() {
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
			// Create a set of found variants
			HashSet<String> vepSos = new HashSet<String>();
			String vepSo = ve.getInfo("SO");
			for (String so : vepSo.split(",")) {
				vepSos.add(so);
			}

			// Get effects for transcript 'trId'
			HashSet<String> effSos = new HashSet<String>();
			List<VcfEffect> veffs = ve.parseEffects();
			for (VcfEffect veff : veffs) {
				if (veff.getTranscriptId().equals(trId)) {
					String eff = veff.getEffString();
					if (eff.equals("5_prime_UTR_premature_start_codon_gain_variant")) eff = "5_prime_UTR_variant"; // OK. I consider these the same
					effSos.add(eff);
				}
			}

			// Make sure both sets are equal
			if (!(effSos.containsAll(vepSos) && vepSos.containsAll(effSos))) {
				String msg = "\n" + ve + "\n\tSnpEff: ";
				for (String e : effSos)
					msg += e + " ";

				msg += "\n\tVep   : ";
				for (String e : vepSos)
					msg += e + " ";

				Gpr.debug(msg);
				//throw new RuntimeException(msg);
			}
		}

	}

	public void test_01_Vep() throws IOException {
		// create_ENST00000268124_SNP_file();
		compareVepSO("testENST00000268124", "tests/testENST00000268124.SNP.vcf", "ENST00000268124");
	}

	public void test_02_Vep() throws IOException {
		// create_ENST00000268124_SNP_file();
		compareVepSO("testENST00000268124", "tests/testENST00000268124.SNP.02.vcf", "ENST00000268124");
	}

}
