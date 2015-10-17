package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.Assert;

/**
 * VCF annotations test cases
 *
 * @author pcingola
 */
public class TestCasesIntegrationVcfs {

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

	public TestCasesIntegrationVcfs() {
		super();
	}

	@Test
	public void test_01_vcf_bed_filter() {
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
	public void test_02_Annotating_LOF_Spaces() {
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

	/**
	 * Non-variant VCF entries should be skipped (i.e. no annotation should be added)
	 */
	@Test
	public void test_03_do_not_annotate_non_variants() {
		String vcfFileName = "tests/test_non_variants.vcf";
		String genomeName = "testHg3775Chr1";

		// Prepare a command line
		String args[] = { "-noLog", genomeName, vcfFileName };
		SnpEff snpEff = new SnpEff(args);
		snpEff.setSupressOutput(!verbose);
		snpEff.setVerbose(verbose);
		snpEff.setDebug(debug);

		// Run command
		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.snpEffCmd();
		List<VcfEntry> vcfEntries = seff.run(true);
		Assert.assertFalse("SnpEff run failed, returned an empty list", vcfEntries.isEmpty());

		// Check output
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			if (ve.hasInfo(EffFormatVersion.VCF_INFO_ANN_NAME) || ve.hasInfo(EffFormatVersion.VCF_INFO_EFF_NAME)) //
				throw new RuntimeException("Effect field should not be annotated on non-variant entries!\n" + ve);

		}
	}

}
