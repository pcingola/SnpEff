package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test Splice sites variants
 *
 * @author pcingola
 */
public class TestCasesSplice extends TestCasesBase {

	public static int N = 1000;

	public TestCasesSplice() {
		super();
	}

	@Override
	protected void init() {
		super.init();
		randSeed = 20141205;
	}

	@Test
	public void test_01() {
		Gpr.debug("Test");
		CodonTable codonTable = genome.codonTable();

		// Test N times
		//	- Create a random gene transcript, exons
		//	- Change each base in the exon
		//	- Calculate effect
		for (int i = 0; i < N; i++) {
			initSnpEffPredictor();
			if (debug) System.out.println("Splice Test iteration: " + i + "\n" + transcript);
			else if (verbose) System.out.println("Splice Test iteration: " + i + "\t" + transcript.getStrand() + "\t" + transcript.cds());
			else Gpr.showMark(i + 1, 1);

			for (Exon exon : transcript.sortedStrand()) {
				throw new RuntimeException("UnImplemented!");
			}
		}

		System.err.println("");
	}

}
