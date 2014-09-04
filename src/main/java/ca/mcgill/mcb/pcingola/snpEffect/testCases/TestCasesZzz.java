package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = true || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Parsing Genomic VCFs
	 * http://www.broadinstitute.org/gatk/guide/article?id=4017
	 */
	public void test_25_Genomic_VCF() {
		String vcfFileName = "tests/genomic_vcf.gvcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
		}
	}

}
