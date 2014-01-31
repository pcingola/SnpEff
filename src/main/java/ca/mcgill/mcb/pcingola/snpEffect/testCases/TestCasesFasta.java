package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.FastaFileIterator;

/**
 * Test case for FASTA file parsing
 * 
 * @author pcingola
 */
public class TestCasesFasta extends TestCase {

	public TestCasesFasta() {
		super();
	}

	/**
	 * Fasta file iterator should not crash if first line is empty
	 */
	public void test_01() {
		String fastaFileName = "tests/emptyLine.fa";
		FastaFileIterator ffi = new FastaFileIterator(fastaFileName);
		for (String seq : ffi) {
			System.out.println("SeqName: " + ffi.getName() + "\tSize: " + seq.length());
		}
	}
}
