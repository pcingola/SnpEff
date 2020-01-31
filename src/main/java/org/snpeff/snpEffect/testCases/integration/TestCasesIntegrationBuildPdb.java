package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;
import org.snpeff.pdb.DistanceResult;
import org.snpeff.snpEffect.commandLine.SnpEffCmdPdb;
import org.snpeff.util.Gpr;

import junit.framework.Assert;

/**
 * Test cases for annotation of protein interaction loci
 */
public class TestCasesIntegrationBuildPdb extends TestCasesIntegrationBase {

	public TestCasesIntegrationBuildPdb() {
	}

	/**
	 * Interaction within protein using PDB entry '1A12'
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		// Command line arguments
		String genome = "testHg19Pdb";
		String pdbDir = path("pdb");
		String idmap = path("pdb") + "/idMap_pdbId_ensemblId_refseqId.txt.gz";
		String args[] = { "-pdbDir", pdbDir, "-idmap", idmap, genome };

		// Create command
		SnpEffCmdPdb cmd = new SnpEffCmdPdb();
		cmd.setVerbose(verbose);
		cmd.setDebug(debug);
		cmd.parseArgs(args);
		cmd.run(true);
		List<DistanceResult> distanceResults = cmd.getDistanceResults();

		// Check results for a specific interaction
		boolean ok = false;
		for (DistanceResult dr : distanceResults) {
			ok |= dr.pdbId.equals("1A12") && dr.aaPos1 == 24 && dr.aaPos2 == 135;
			if (verbose) Gpr.debug("INTERACTION:\t" + dr);
		}

		Assert.assertTrue("Interaction not found!", ok);
	}

	/**
	 * Interaction between two proteins
	 * PDB entry 4OVU should have an interaction between chains 'A'
	 * and 'B' (Min distance :2.45 Angstrom)
	 *
	 *  	AA.pos	AA		chr:pos			transcript
	 *  	22		E		3:178916679		NM_006218.2
	 *  	533		R		5:67591006		NM_181523.2
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");

		// Command line arguments
		String genome = "testHg19Pdb";
		String pdbDir = path("pdb");
		String idmap = path("pdb") + "/idMap_pdbId_ensemblId_refseqId.txt.gz";
		String args[] = { "-pdbDir", pdbDir, "-idmap", idmap, genome };

		// Create command
		SnpEffCmdPdb cmd = new SnpEffCmdPdb();
		cmd.setVerbose(verbose);
		cmd.setDebug(debug);
		cmd.parseArgs(args);
		cmd.run(true);
		List<DistanceResult> distanceResults = cmd.getDistanceResults();

		// Check results
		boolean ok = false;
		for (DistanceResult dr : distanceResults) {
			ok |= dr.pdbId.equals("4OVU") //
					&& dr.aaPos1 == 22 //
					&& dr.aaPos2 == 533 //
					&& dr.trId1.equals("NM_006218.2") //
					&& dr.trId2.equals("NM_181523.2") //
			;
			if (verbose) Gpr.debug(dr);
		}

		Assert.assertTrue("Interaction not found!", ok);
	}

}
