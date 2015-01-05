package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * VCF annotations test cases
 *
 * @author pcingola
 */
public class TestCasesVcfs {

	boolean verbose = false;
	boolean debug = false;
	boolean createOutputFile = false;

	/**
	 * Creates a test file
	 */
	public static void create1kgFile() throws IOException {
		String vcfFile = Gpr.HOME + "/snpEff/1kg.indels.vcf";
		String vcfOutFile = Gpr.HOME + "/workspace/SnpEff/tests/1kg.indels.vcf";

		StringBuilder outvcf = new StringBuilder();

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			StringBuilder sb = new StringBuilder();

			for (Variant sc : ve.variants()) {
				if (sb.length() > 0) sb.append(",");
				sb.append(sc.getReference() + "/" + sc.getAlt());
			}

			ve.addInfo("variant", sb.toString());
			outvcf.append(ve + "\n");
		}

		Gpr.toFile(vcfOutFile, outvcf);
	}

	public TestCasesVcfs() {
		super();
	}

	@Test
	public void test_17_vcf_bed_filter() {
		Gpr.debug("Test");
		String vcfFile = "tests/test_vcf_filter.vcf";
		String bedFile = "tests/test_vcf_filter.bed";

		String args[] = { "-classic", "-filterinterval", bedFile, "testHg3771Chr1", vcfFile };
		SnpEff snpeff = new SnpEff(args);

		// Create command and run
		SnpEffCmdEff effcmd = (SnpEffCmdEff) snpeff.snpEffCmd();
		effcmd.setVerbose(verbose);
		effcmd.setSupressOutput(!verbose);
		List<VcfEntry> vcfEntries = effcmd.run(true);

		// All VCF entries should be filtered out
		Gpr.debug("Vcf entries: " + vcfEntries.size());
		Assert.assertEquals(0, vcfEntries.size());

		// Nothing should be printed
		for (VcfEntry ve : vcfEntries)
			System.out.println(ve);
	}

	/**
	 * Annotating LOF / NMD using a geneName that contains spaces triggers
	 * an Exception (it shouldn't happen)
	 */
	@Test
	public void test_26_Annotating_LOF_Spaces() {
		String vcfFileName = "tests/vcf_genes_spaces.vcf";
		String genomeName = "test_ENSG00000158062_spaces";

		// Prepare a command line
		String args[] = { "-noLog", genomeName, vcfFileName };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setSupressOutput(!verbose);
		snpEff.setVerbose(verbose);
		snpEff.setDebug(debug);

		// This should run OK
		boolean ok = snpEff.run();
		Assert.assertTrue("SnpEff run failed!", ok);
	}

}
