package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Test 'apply' method (apply seqChange to marker)
 * 
 * @author pcingola
 */
public class TestCasesApply extends TestCase {

	public static boolean debug = false;
	public static boolean verbose = false;
	public static int SHOW_EVERY = 10;

	public TestCasesApply() {
		super();
	}

	/**
	 * Test 'apply' on exons (test sequence changes) 
	 * Only using SNPs seqChanges
	 */
	@Test
	public void test_01_Exon_SNPs() {
		Config config = new Config("testHg3765Chr22");
		Timer.show("Loading predictor");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();
		Timer.show("Done");

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
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
							char ref = seq.charAt(idx);
							char alt;
							do {
								alt = GprSeq.randBase(random);
							} while (ref == alt);

							// Resulting sequence
							String altStr = alt + "";
							String newSeq = seq.substring(0, idx) + altStr + seq.substring(idx + 1);
							newSeq = ex.isStrandPlus() ? newSeq : GprSeq.reverseWc(newSeq);
							newSeq = newSeq.toLowerCase();

							SeqChange seqChange = new SeqChange(t.getChromosome(), i, ref + "", alt + "", 1, "", -1, -1);

							Exon exNew = ex.apply(seqChange);

							if (!exNew.getSequence().equals(newSeq)) throw new RuntimeException("Error:" //
									+ "\n\t\tSeqChange : " + seqChange //
									+ "\n\t\tOriginal  : " + ex //
									+ "\n\t\tNew       : " + exNew //
									+ "\n\t\tNew seq   : " + newSeq //
							);
						}
					}
				}
			}
		}
	}

	/**
	 * Test 'apply' on exons (test sequence changes) 
	 * Only using insertion seqChanges
	 */
	@Test
	public void test_02_Exon_INS() {
		Config config = new Config("testHg3765Chr22");
		Timer.show("Loading predictor");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();
		Timer.show("Done");

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
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

							// Random REF 
							char ref = seq.charAt(idx);

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

							SeqChange seqChange = new SeqChange(t.getChromosome(), i, ref + "", "+" + altsb.toString(), 1, "", -1, -1);
							if (debug) Gpr.debug("SeqChange: " + seqChange.getChangeType() + "\t" + seqChange);

							Exon exNew = ex.apply(seqChange);

							Assert.assertEquals(newSeq, exNew.getSequence());
							if (!exNew.getSequence().equals(newSeq)) {
								String msg = "Error:" //
										+ "\n\t\tSeqChange : " + seqChange //
										+ "\n\t\tOriginal  : " + ex //
										+ "\n\t\tNew       : " + exNew //
										+ "\n\t\tNew seq   : " + newSeq //
								;
								System.err.println(msg);
								throw new RuntimeException(msg);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Test 'apply' on exons (test sequence changes) 
	 * Only using deletions seqChanges
	 */
	@Test
	public void test_03_Exon_DEL() {
		Config config = new Config("testHg3765Chr22");
		Timer.show("Loading predictor");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();
		Timer.show("Done");

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
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
							String alt = end < seq.length() ? seq.substring(idx, end) : seq.substring(idx);

							// Resulting sequence
							String newSeq = "";
							if (idx > 0) newSeq = seq.substring(0, idx);
							newSeq += (end < seq.length() ? seq.substring(end) : "");

							newSeq = ex.isStrandPlus() ? newSeq : GprSeq.reverseWc(newSeq);
							newSeq = newSeq.toLowerCase();

							SeqChange seqChange = new SeqChange(t.getChromosome(), i, "", "-" + alt, 1, "", -1, -1);
							if (debug) Gpr.debug("SeqChange: " + seqChange.getChangeType() + "\t" + seqChange);

							Exon exNew = ex.apply(seqChange);

							String newExSeq = (exNew != null ? exNew.getSequence() : "");
							Assert.assertEquals(newSeq, newExSeq);
							if (!newExSeq.equals(newSeq)) {
								String msg = "Error:" //
										+ "\n\t\tSeqChange : " + seqChange //
										+ "\n\t\tOriginal  : " + ex //
										+ "\n\t\tNew       : " + exNew //
										+ "\n\t\tNew seq   : " + newSeq //
								;
								System.err.println(msg);
								throw new RuntimeException(msg);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Test 'apply' on exons (test sequence changes) 
	 * Only using deletions seqChanges
	 */
	@Test
	public void test_04_Exon_MNP() {
		Config config = new Config("testHg3765Chr22");
		Timer.show("Loading predictor");
		SnpEffectPredictor snpEffectPredictor = config.loadSnpEffectPredictor();
		Timer.show("Done");

		Random random = new Random(20130214);

		// All genes
		Genome genome = snpEffectPredictor.getGenome();
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
							StringBuilder altsb = new StringBuilder(), refsb = new StringBuilder();
							for (int j = 0; j < len; j++) {
								if ((idx + j) < seq.length()) refsb.append(seq.charAt(idx + j));
								else refsb.append(GprSeq.randBase(random));
								altsb.append(GprSeq.randBase(random));
							}
							if (refsb.length() != altsb.length()) throw new RuntimeException("This should never happen!");

							// Resulting sequence
							String newSeq = "";
							if (idx > 0) newSeq = seq.substring(0, idx);
							int maxlen = Math.min(ex.getEnd() - i + 1, len);
							newSeq += altsb.substring(0, maxlen);
							newSeq += (end < seq.length() ? seq.substring(end) : "");

							newSeq = ex.isStrandPlus() ? newSeq : GprSeq.reverseWc(newSeq);
							newSeq = newSeq.toLowerCase();

							// Create seqChange and apply 
							SeqChange seqChange = new SeqChange(t.getChromosome(), i, refsb.toString(), altsb.toString(), 1, "", -1, -1);
							if (debug) Gpr.debug("SeqChange: " + seqChange);
							Exon exNew = ex.apply(seqChange);

							// Check
							if (!exNew.getSequence().equals(newSeq)) {
								String msg = "Error:" //
										+ "\n\t\tSeqChange : " + seqChange //
										+ "\n\t\tOriginal  : " + ex //
										+ "\n\t\tNew       : " + exNew //
										+ "\n\t\tNew seq   : " + newSeq //
								;
								System.err.println(msg);
								throw new RuntimeException(msg);
							}
						}
					}
				}
			}
		}
	}
}
