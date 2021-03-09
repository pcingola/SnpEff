package org.snpeff.snpEffect.testCases.unity;

import org.junit.Test;
import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.util.Log;

/**
 * Test case for FASTA file parsing
 *
 * @author pcingola
 */
public class TestCasesFasta extends TestCasesBase {

	public TestCasesFasta() {
		super();
	}

	/**
	 * Bug: Fasta file iterator should not crash if first line is empty
	 */
	@Test
	public void test_01() {
		Log.debug("Test");
		String fastaFileName = path("emptyLine.fa");
		FastaFileIterator ffi = new FastaFileIterator(fastaFileName);
		for (String seq : ffi) {
			if (verbose) System.out.println("SeqName: " + ffi.getName() + "\tSize: " + seq.length());
		}
	}
}
