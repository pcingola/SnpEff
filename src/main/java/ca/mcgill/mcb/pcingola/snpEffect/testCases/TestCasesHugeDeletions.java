package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.TestCase;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case where VCF entries are huge (e.g. half chromosome deleted)
 * 
 * @author pcingola
 */
public class TestCasesHugeDeletions extends TestCase {

	boolean verbose = false;

	public TestCasesHugeDeletions() {
		super();
	}

	public void test_01() {
		String args[] = { "-noOut", "testHg3766Chr1", "./tests/huge_deletion_DEL.vcf" };

		SnpEffCmdEff snpEffCmdEff = new SnpEffCmdEff();
		snpEffCmdEff.parseArgs(args);
		snpEffCmdEff.setVerbose(verbose);

		List<VcfEntry> vcfEntries = snpEffCmdEff.run(true);

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			System.out.println(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getInfoStr());
			Assert.assertTrue(ve.getInfo("EFF").startsWith("CHROMOSOME_LARGE_DELETION(HIGH"));
		}
	}

	public void test_02() {
		String args[] = { "-noOut", "testHg3766Chr1", "./tests/huge_deletion.vcf.gz" };

		SnpEffCmdEff snpEffCmdEff = new SnpEffCmdEff();
		snpEffCmdEff.parseArgs(args);
		snpEffCmdEff.setVerbose(verbose);
		List<VcfEntry> vcfEntries = snpEffCmdEff.run(true);

		// Make sure these are "CHROMOSOME_LARGE_DELETION" type of variants
		for (VcfEntry ve : vcfEntries) {
			System.out.println(ve.getChromosomeName() + "\t" + ve.getStart() + "\t" + ve.getInfoStr());
			Assert.assertTrue(ve.getInfo("EFF").startsWith("CHROMOSOME_LARGE_DELETION(HIGH"));
		}
	}
}
