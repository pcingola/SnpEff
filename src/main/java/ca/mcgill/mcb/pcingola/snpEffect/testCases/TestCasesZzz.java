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
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	public static boolean debug = false;

	public static void createDelFile(String genomeName, String outFile, double prob) throws IOException {
		Config config = new Config(genomeName, Gpr.HOME + "/snpEff/" + Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		Random rand = new Random(20140129);
		StringBuilder out = new StringBuilder();

		int count = 0;
		for (Gene g : config.getGenome().getGenes()) {
			for (Transcript tr : g) {
				for (Exon e : tr) {
					for (int i = e.getStart(); i < e.getEnd(); i++) {
						if (rand.nextDouble() < prob) {

							// Deletion length
							int delLen = rand.nextInt(10) + 2;
							if (i + delLen > e.getEnd()) delLen = e.getEnd() - i;

							if (delLen >= 2) {
								int idx = i - e.getStart();

								String ref = e.basesAt(idx, delLen);
								String alt = ref.substring(0, 1);

								int pos = i + 1;
								String line = e.getChromosomeName() + "\t" + pos + "\t.\t" + ref + "\t" + alt + "\t.\t.\tAC=1\tGT\t0/1";
								System.out.println(line);
								out.append(line + "\n");
								count++;
							}
						}
					}
				}
			}
		}

		System.err.println("Count:" + count);
		System.out.println("Output file: " + outFile);
		Gpr.toFile(outFile, out);
	}

	public static void createInsFile(String genomeName, String outFile, double prob) throws IOException {
		Config config = new Config(genomeName, Gpr.HOME + "/snpEff/" + Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		Random rand = new Random(20140129);
		StringBuilder out = new StringBuilder();

		int count = 0;
		for (Gene g : config.getGenome().getGenes()) {
			for (Transcript tr : g) {
				for (Exon e : tr) {
					for (int i = e.getStart(); i < e.getEnd(); i++) {
						if (rand.nextDouble() < prob) {

							// Insertion length
							int insLen = rand.nextInt(10) + 1;
							if (i + insLen > e.getEnd()) insLen = e.getEnd() - i;

							int idx = i - e.getStart();

							String ref = e.basesAt(idx, 1);
							String alt = ref + GprSeq.randSequence(rand, insLen);

							int pos = i + 1;
							String line = e.getChromosomeName() + "\t" + pos + "\t.\t" + ref + "\t" + alt + "\t.\t.\tAC=1\tGT\t0/1";
							System.out.println(line);
							out.append(line + "\n");
							count++;
						}
					}
				}
			}
		}

		System.err.println("Count:" + count);
		System.out.println("Output file: " + outFile);
		Gpr.toFile(outFile, out);
	}

	public static void createMnpFile(String genomeName, String outFile, double prob) throws IOException {
		Config config = new Config(genomeName, Gpr.HOME + "/snpEff/" + Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();

		Random rand = new Random(20140129);
		StringBuilder out = new StringBuilder();

		int count = 0;
		for (Gene g : config.getGenome().getGenes()) {
			for (Transcript tr : g) {
				for (Exon e : tr) {
					for (int i = e.getStart(); i < e.getEnd(); i++) {
						if (rand.nextDouble() < prob) {

							// Deletion length
							int mnpLen = rand.nextInt(10) + 1;
							if (i + mnpLen > e.getEnd()) mnpLen = e.getEnd() - i;

							if (mnpLen >= 2) {
								int idx = i - e.getStart();

								String ref = e.basesAt(idx, mnpLen);
								String alt = GprSeq.randSequence(rand, mnpLen);

								// Make sure thay are not equal
								while (ref.equals(alt))
									alt = GprSeq.randSequence(rand, mnpLen);

								int pos = i + 1;
								String line = e.getChromosomeName() + "\t" + pos + "\t.\t" + ref + "\t" + alt + "\t.\t.\tAC=1\tGT\t0/1";
								System.out.println(line);
								out.append(line + "\n");
								count++;
							}
						}
					}
				}
			}
		}

		System.err.println("Count:" + count);
		System.out.println("Output file: " + outFile);
		Gpr.toFile(outFile, out);
	}

	/**
	 * Create a file to send to ENSEMBL's VEP.
	 * Used for benchmarking
	 * 
	 * @throws IOException
	 */
	public static void createSnpFile(String genomeName, String outFile) throws IOException {
		Config config = new Config(genomeName, Gpr.HOME + "/snpEff/" + Config.DEFAULT_CONFIG_FILE);
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
		System.out.println("Output file: " + outFile);
		Gpr.toFile(outFile, out);
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
				if (so.equals("feature_elongation")) so = null; // This one is useless, if the variant is an insertion, it obviously causes an elongation
				else if (so.equals("feature_truncation")) so = null; // This one is useless, if the variant is an insertion, it obviously causes an elongation

				if (so != null) vepSos.add(so);
			}

			// Get effects for transcript 'trId'
			HashSet<String> effSos = new HashSet<String>();
			List<VcfEffect> veffs = ve.parseEffects();
			for (VcfEffect veff : veffs) {
				if (veff.getTranscriptId().equals(trId)) {
					String eff = veff.getEffString();
					if (eff.equals("5_prime_UTR_premature_start_codon_gain_variant")) eff = "5_prime_UTR_variant"; // OK. I consider these the same
					for (String e : eff.split("\\+"))
						effSos.add(e);
				}
			}

			// Make sure both sets are equal
			boolean error = false;
			if (debug) error = !(effSos.containsAll(vepSos) && vepSos.containsAll(effSos));
			else error = !effSos.containsAll(vepSos);

			if (error) {
				String msg = "\n" + ve + "\n\tSnpEff: ";
				for (String e : effSos)
					msg += e + " ";

				msg += "\n\tVep   : ";
				for (String e : vepSos)
					msg += e + " ";

				msg += "\n\tMarker   : " + ve.getChromosomeName() + ":" + ve.getStart() + "-" + ve.getEnd();
				Gpr.debug(msg);
				if (!debug) throw new RuntimeException(msg);
			}

		}
	}

	public void test_06_Vep() throws IOException {
		// createMnpFile("testENST00000268124", "./tests/testENST00000268124.Mnp.ORI.06.vcf", 0.15);
		compareVepSO("testENST00000268124", "tests/testENST00000268124.Mnp.06.vcf", "ENST00000268124");
	}

}
