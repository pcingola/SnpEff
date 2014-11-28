package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.binseq.DnaSequenceByte;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

public class TestCasesDnaSequenceByte extends TestCase {

	public static boolean verbose = false;

	public void test_01() {
		Gpr.debug("Test");
		Random random = new Random(20120907);
		for (int len = 1; len < 1000; len++) {
			for (int i = 0; i < 10; i++) {
				String seq = GprSeq.randSequence(random, len);
				DnaSequenceByte dna = new DnaSequenceByte(seq);

				if (verbose) System.out.println("Len: " + len + "\t" + seq + "\t" + dna);

				if (seq.equals(dna.toString())) throw new RuntimeException("Sequences do not match! Lnegth: " + len + "\n\t" + seq + "\n\t" + dna);
			}
		}
	}

}
