package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.NeedlemanWunsch;
import ca.mcgill.mcb.pcingola.fileIterator.VcfRefAltAlign;

/**
 * test cases for Sequence alignment
 *
 * @author pcingola
 */
public class TestCasesAlign extends TestCase {

	boolean verbose = false;

	public void test_01() {
		String as[] = { "TTT", "TTTGTT", "GCG", "G" };
		String bs[] = { "TTTGTT", "TTT", "G", "GCG" };
		String res[] = { "-GTT", "+GTT", "+CG", "-CG" };
		int offset[] = { 3, 3, 1, 1 };

		for (int i = 0; i < as.length; i++) {
			String a = as[i];
			String b = bs[i];
			NeedlemanWunsch align = new NeedlemanWunsch(a, b);
			if (verbose) System.out.println("---------------------------------------- " + align.getClass().getSimpleName() + ": " + i + " ----------------------------------------");
			align.align();
			if (verbose) System.out.println("a    : '" + a + "'\nb    : '" + b + "'\nAlign: '" + align.getAlignment() + "'" + "\tOffset: " + align.getOffset() + "\n");

			Assert.assertEquals(res[i], align.getAlignment());
			Assert.assertEquals(offset[i], align.getOffset());
		}
	}

	public void test_02() {
		String as[] = { "TTT", "TTTGTT", "GCG", "G" };
		String bs[] = { "TTTGTT", "TTT", "G", "GCG" };
		String res[] = { "-GTT", "+GTT", "+CG", "-CG" };
		int offset[] = { 3, 3, 1, 1 };

		for (int i = 0; i < as.length; i++) {
			String a = as[i];
			String b = bs[i];
			VcfRefAltAlign align = new VcfRefAltAlign(a, b);
			if (verbose) System.out.println("---------------------------------------- " + align.getClass().getSimpleName() + ": " + i + " ----------------------------------------");
			align.align();
			if (verbose) System.out.println("a    : '" + a + "'\nb    : '" + b + "'\nAlign: '" + align.getAlignment() + "'" + "\tOffset: " + align.getOffset() + "\n");

			Assert.assertEquals(res[i], align.getAlignment());
			Assert.assertEquals(offset[i], align.getOffset());
		}
	}

}
