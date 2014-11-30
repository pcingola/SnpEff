package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.NeedlemanWunsch;
import ca.mcgill.mcb.pcingola.fileIterator.VcfRefAltAlign;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * test cases for Sequence alignment
 *
 * @author pcingola
 */
public class TestCasesAlign {

	boolean verbose = false;

	@Test
	public void test_01() {
		Gpr.debug("Test");
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

	@Test
	public void test_02() {
		Gpr.debug("Test");
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
