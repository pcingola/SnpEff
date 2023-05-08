package org.snpeff.snpEffect.testCases.integration;

import java.io.File;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case
 */
public class TestCasesIntegrationRegulation extends TestCasesIntegrationBase {

	public TestCasesIntegrationRegulation() {
		super();
	}

	/**
	 * Create and load a regulation track
	 */
	@Test
	public void test_01() {
		Log.debug("Test");

		String genome = "testHg3775Chr22";
		String vcfFileName = path("test_regulatory_01.vcf");
		String cellType = "HepG2";
		String dbFileName = "data/" + genome + "/regulation_" + cellType + ".bin";

		//---
		// Make sure database file does not exists
		//---
		if (verbose) Log.info("Deleting file '" + dbFileName + "'");
		File f = (new File(dbFileName));
		f.delete();

		//---
		// Build regulatory region database
		//---
		String[] argsBuild = { "build", "-onlyReg", "-noLog", genome };
		SnpEff snpeffBuild = new SnpEff(argsBuild);
		snpeffBuild.setDebug(debug);
		snpeffBuild.setVerbose(verbose);
		snpeffBuild.setSupressOutput(!verbose);
		boolean ok = snpeffBuild.run();
		assertTrue(ok, "Error building regulatory regions");

		//---
		// Annotate using the regulatory region database we've just created
		//---
		String[] argsRun = { genome, vcfFileName };
		SnpEffCmdEff snpeffRun = new SnpEffCmdEff();
		snpeffRun.parseArgs(argsRun);
		snpeffRun.setDebug(debug);
		snpeffRun.setVerbose(verbose);
		snpeffRun.setSupressOutput(!verbose);
		snpeffRun.addRegulationTrack(cellType);

		List<VcfEntry> vcfEntries = snpeffRun.run(true);
		ok = false;
		for (VcfEntry ve : vcfEntries) {
			if (verbose) Log.info(ve);
			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) Log.info("\t" + veff + "\t\t" + veff.getFeatureType());
				ok |= veff.getFeatureType().equals("REGULATION&H3K27me3:HepG2");
			}
		}

		assertTrue(ok, "Error annotating regulatory regions");

	}
}
