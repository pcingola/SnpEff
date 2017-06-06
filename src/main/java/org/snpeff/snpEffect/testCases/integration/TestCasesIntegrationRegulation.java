package org.snpeff.snpEffect.testCases.integration;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesIntegrationRegulation {

	boolean debug = false;
	boolean verbose = false || debug;

	public TestCasesIntegrationRegulation() {
		super();
	}

	/**
	 * Create and load a regulation track
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		String genome = "testHg3775Chr22";
		String vcfFileName = "tests/integration/regulation/test_regulatory_01.vcf";
		String cellType = "HepG2";
		String dbFileName = "data/" + genome + "/regulation_" + cellType + ".bin";

		//---
		// Make sure database file does not exists
		//---
		if (verbose) System.err.println("Deleting file '" + dbFileName + "'");
		File f = (new File(dbFileName));
		f.delete();

		//---
		// Build regulatory region database
		//---
		String argsBuild[] = { "build", "-onlyReg", "-noLog", genome };
		SnpEff snpeffBuild = new SnpEff(argsBuild);
		snpeffBuild.setDebug(debug);
		snpeffBuild.setVerbose(verbose);
		snpeffBuild.setSupressOutput(!verbose);
		boolean ok = snpeffBuild.run();
		Assert.assertTrue("Error building regulatory regions", ok);

		//---
		// Annotate using the regulatory region database we've just created
		//---
		String argsRun[] = { genome, vcfFileName };
		SnpEffCmdEff snpeffRun = new SnpEffCmdEff();
		snpeffRun.parseArgs(argsRun);
		snpeffRun.setDebug(debug);
		snpeffRun.setVerbose(verbose);
		snpeffRun.setSupressOutput(!verbose);
		snpeffRun.addRegulationTrack(cellType);

		List<VcfEntry> vcfEntries = snpeffRun.run(true);
		ok = false;
		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff + "\t\t" + veff.getFeatureType());
				ok |= veff.getFeatureType().equals("REGULATION&H3K27me3:HepG2");
			}
		}

		Assert.assertTrue("Error annotating regulatory regions", ok);

	}
}
