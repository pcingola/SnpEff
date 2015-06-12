package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.FastaFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case for FASTA file parsing
 *
 * @author pcingola
 */
public class TestCasesFasta {

	boolean verbose = false;

	public TestCasesFasta() {
		super();
	}

	/**
	 * Bug: Fasta file iterator should not crash if first line is empty
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		String fastaFileName = "tests/emptyLine.fa";
		FastaFileIterator ffi = new FastaFileIterator(fastaFileName);
		for (String seq : ffi) {
			if (verbose) System.out.println("SeqName: " + ffi.getName() + "\tSize: " + seq.length());
		}
	}
}
