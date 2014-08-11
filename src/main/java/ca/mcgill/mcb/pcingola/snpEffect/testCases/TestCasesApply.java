package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.Random;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Test 'apply' method (apply variant to marker)
 *
 * @author pcingola
 */
public class TestCasesApply extends TestCase {

	public static boolean debug = false;
	public static boolean verbose = false || debug;
	public static int SHOW_EVERY = 10;

	public TestCasesApply() {
		super();
	}

	/**
	 * Test 'apply' on exons (test sequence changes)
	 * Only using SNPs variants
	 */
	@Test
	public void test_01_Exon_SNPs() {
		Gpr.debug("Test");
		Config config = new Config("testHg3765Chr22");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
		int count = 1;
		for (Gene g : genome.getGenes()) {

			if (g.isProteinCoding()) { // Only protein coding ones...
				if (verbose) System.out.println(g.getGeneName());

				// All transcripts
				for (Transcript t : g) {
					if (verbose) System.out.println("\t" + t.getId());

					// All exons
					for (Exon ex : t) {
						// Positive strand sequence
						String seq = ex.getSequence();
						seq = ex.isStrandPlus() ? seq : GprSeq.reverseWc(seq);

						// Skip some exons, otherwise test takes too much time
						if (random.nextInt(10) > 1) continue; // Randomly some exons
						if (ex.size() > 1000) continue; // Skip exon if too long

						if (verbose) System.out.println("\t\t" + ex.getId() + "\tStrand: " + ex.getStrand() + "\tSize: " + ex.size());

						// Change each base
						for (int i = ex.getStart(), idx = 0; i < ex.getEnd(); i++, idx++) {
							// Create a fake SNP. Random REF and ALT bases
							char ref = Character.toUpperCase(seq.charAt(idx));
							char alt = ref;
							do {
								alt = GprSeq.randBase(random);
							} while (ref == alt);

							// Resulting sequence
							String altStr = alt + "";
							String newSeq = seq.substring(0, idx) + altStr + seq.substring(idx + 1);
							newSeq = ex.isStrandPlus() ? newSeq : GprSeq.reverseWc(newSeq);
							newSeq = newSeq.toLowerCase();

							Variant variant = new Variant(t.getChromosome(), i, ref + "", alt + "", "");

							Exon exNew = ex.apply(variant);

							if (!exNew.getSequence().equals(newSeq)) throw new RuntimeException("Error:" //
									+ "\n\t\tVariant   : " + variant //
									+ "\n\t\tOriginal  : " + ex //
									+ "\n\t\tNew       : " + exNew //
									+ "\n\t\tNew seq   : " + newSeq //
							);

							Gpr.showMark(count++, 1000);
						}
					}
				}
			}
		}
		System.err.println("");
	}

	/**
	 * Test 'apply' on exons (test sequence changes)
	 * Only using insertion variants
	 */
	@Test
	public void test_02_Exon_INS() {
		Gpr.debug("Test");
		Config config = new Config("testHg3765Chr22");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
		int count = 1;
		for (Gene g : genome.getGenes()) {

			if (g.isProteinCoding()) { // Only protein coding ones...
				if (verbose) System.out.println(g.getGeneName());

				// All transcripts
				for (Transcript t : g) {
					if (verbose) System.out.println("\t" + t.getId());

					// All exons
					for (Exon ex : t) {
						// Positive strand sequence
						String seq = ex.getSequence();
						seq = ex.isStrandPlus() ? seq : GprSeq.reverseWc(seq);

						// Skip some exons, otherwise test takes too much time
						if (random.nextInt(10) > 1) continue; // Randomly some exons
						if (ex.size() > 1000) continue; // Skip exon if too long

						if (verbose) System.out.println("\t\t" + ex.getId() + "\tStrand: " + ex.getStrand() + "\tSize: " + ex.size());

						// Change each base
						for (int i = ex.getStart(), idx = 0; i < ex.getEnd(); i++, idx++) {
							// Create a fake INS.

							// Random ALT
							int insLen = 1 + random.nextInt(8);
							StringBuilder altsb = new StringBuilder();
							for (int j = 0; j < insLen; j++)
								altsb.append(GprSeq.randBase(random));

							// Resulting sequence
							String newSeq;
							if (idx > 0) newSeq = seq.substring(0, idx) + altsb.toString() + seq.substring(idx);
							else newSeq = altsb.toString() + seq;
							newSeq = ex.isStrandPlus() ? newSeq : GprSeq.reverseWc(newSeq);
							newSeq = newSeq.toLowerCase();

							Variant variant = new Variant(t.getChromosome(), i, "", altsb.toString(), "");
							if (debug) Gpr.debug("variant: " + variant.getVariantType() + "\t" + variant);

							Exon exNew = ex.apply(variant);

							if (!exNew.getSequence().equals(newSeq)) {
								String msg = "Error:" //
										+ "\n\t\tIndex               : " + idx //
										+ "\n\t\tVariant             : " + variant //
										+ "\n\t\tOriginal            : " + ex //
										+ "\n\t\tSequence (expected) : " + newSeq + "'" //
										+ "\n\t\tSequence            : " + exNew.getSequence() + "'" //
								;
								System.err.println(msg);
								throw new RuntimeException(msg);
							}

							Assert.assertEquals(newSeq, exNew.getSequence());

							Gpr.showMark(count++, 1000);
						}
					}
				}
			}
		}
		System.err.println("");
	}

	/**
	 * Test 'apply' on exons (test sequence changes)
	 * Only using deletions variants
	 */
	@Test
	public void test_03_Exon_DEL() {
		Gpr.debug("Test");
		Config config = new Config("testHg3765Chr22");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
		int count = 1;
		for (Gene g : genome.getGenes()) {

			if (g.isProteinCoding()) { // Only protein coding ones...
				if (verbose) System.out.println(g.getGeneName());

				// All transcripts
				for (Transcript t : g) {
					if (verbose) System.out.println("\t" + t.getId());

					// All exons
					for (Exon ex : t) {
						// Positive strand sequence
						String seq = ex.getSequence();
						seq = ex.isStrandPlus() ? seq : GprSeq.reverseWc(seq);

						// Skip some exons, otherwise test takes too much time
						if (random.nextInt(10) > 1) continue; // Randomly some exons
						if (ex.size() > 1000) continue; // Skip exon if too long

						if (verbose) System.out.println("\t\t" + ex.getId() + "\tStrand: " + ex.getStrand() + "\tSize: " + ex.size());

						// Change each base
						for (int i = ex.getStart(), idx = 0; i < ex.getEnd(); i++, idx++) {
							// Create a fake DEL:  Random REF (since it's a deletion, alt="")
							int delLen = 1 + random.nextInt(8);
							int end = idx + delLen;
							String ref = end < seq.length() ? seq.substring(idx, end) : seq.substring(idx);

							// Resulting sequence
							String newSeq = "";
							if (idx > 0) newSeq = seq.substring(0, idx);
							newSeq += (end < seq.length() ? seq.substring(end) : "");

							newSeq = ex.isStrandPlus() ? newSeq : GprSeq.reverseWc(newSeq);
							newSeq = newSeq.toLowerCase();

							Variant variant = new Variant(t.getChromosome(), i, ref, "", "");
							if (debug) Gpr.debug("variant: " + variant.getVariantType() + "\t" + variant);

							Exon exNew = ex.apply(variant);

							String newExSeq = (exNew != null ? exNew.getSequence() : "");
							Assert.assertEquals(newSeq, newExSeq);
							if (!newExSeq.equals(newSeq)) {
								String msg = "Error:" //
										+ "\n\t\tVariant   : " + variant //
										+ "\n\t\tOriginal  : " + ex //
										+ "\n\t\tNew       : " + exNew //
										+ "\n\t\tNew seq   : " + newSeq //
								;
								System.err.println(msg);
								throw new RuntimeException(msg);
							}

							Gpr.showMark(count++, 1000);

						}
					}
				}
			}
		}
		System.err.println("");

	}

	/**
	 * Test 'apply' on exons (test sequence changes)
	 * Only using deletions variants
	 */
	@Test
	public void test_04_Exon_MNP() {
		Gpr.debug("Test");
		Config config = new Config("testHg3765Chr22");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
		int count = 1;
		for (Gene g : genome.getGenes()) {

			if (g.isProteinCoding()) { // Only protein coding ones...
				if (verbose) System.out.println(g.getGeneName());

				// All transcripts
				for (Transcript t : g) {
					if (verbose) System.out.println("\t" + t.getId());

					// All exons
					for (Exon ex : t) {
						// Positive strand sequence
						String seq = ex.getSequence();
						seq = ex.isStrandPlus() ? seq : GprSeq.reverseWc(seq);

						// Skip some exons, otherwise test takes too much time
						if (random.nextInt(10) > 1) continue; // Randomly some exons
						if (ex.size() > 1000) continue; // Skip exon if too long

						if (verbose) System.out.println("\t\t" + ex.getId() + "\tStrand: " + ex.getStrand() + "\tSize: " + ex.size());

						// Change each base
						for (int i = ex.getStart(), idx = 0; i < ex.getEnd(); i++, idx++) {
							// Create a fake MNP. Random REF and ALT bases
							int len = random.nextInt(9) + 1;
							int end = idx + len;

							StringBuilder altsb, refsb;
							while (true) {
								altsb = new StringBuilder();
								refsb = new StringBuilder();
								for (int j = 0; j < len; j++) {
									if ((idx + j) < seq.length()) refsb.append(seq.charAt(idx + j));
									else refsb.append(GprSeq.randBase(random));
									altsb.append(GprSeq.randBase(random));
								}

								if (refsb.length() != altsb.length()) throw new RuntimeException("This should never happen!");

								// Are we satisfied with this MNP?
								String alt = altsb.toString().toUpperCase();
								String ref = refsb.toString().toUpperCase();
								if (!alt.equals(ref) && alt.charAt(0) != ref.charAt(0)) break;
							}

							// Resulting sequence
							String newSeq = "";
							if (idx > 0) newSeq = seq.substring(0, idx);
							int maxlen = Math.min(ex.getEnd() - i + 1, len);
							newSeq += altsb.substring(0, maxlen);
							newSeq += (end < seq.length() ? seq.substring(end) : "");

							newSeq = ex.isStrandPlus() ? newSeq : GprSeq.reverseWc(newSeq);
							newSeq = newSeq.toLowerCase();

							// Create variant and apply
							Variant variant = new Variant(t.getChromosome(), i, refsb.toString(), altsb.toString(), "");
							if (debug) Gpr.debug("variant: " + variant);
							Exon exNew = ex.apply(variant);

							// Check
							if (!exNew.getSequence().equals(newSeq)) {
								String msg = "Error:" //
										+ "\n\t\tVariant   : " + variant //
										+ "\n\t\tOriginal  : " + ex //
										+ "\n\t\tNew       : " + exNew //
										+ "\n\t\tNew seq   : " + newSeq //
								;
								System.err.println(msg);
								throw new RuntimeException(msg);
							}

							Gpr.showMark(count++, 1000);
						}
					}
				}
			}
		}
		System.err.println("");

	}
}
