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
public class TestCasesIntegratioBuildPdb extends TestCasesIntegrationBase {

	public TestCasesIntegratioBuildPdb() {
	}

	//	/**
	//	 * Interaction within protein
	//	 */
	//	@Test
	//	public void test_01() {
	//		Gpr.debug("Test");
	//
	//		// Command line arguments
	//		String genome = "testHg19Pdb";
	//		String pdbDir = "tests/pdb";
	//		String args[] = { "-pdbDir", pdbDir, genome };
	//
	//		// Create command
	//		SnpEffCmdPdb cmd = new SnpEffCmdPdb();
	//		cmd.setVerbose(verbose);
	//		cmd.setDebug(debug);
	//		cmd.parseArgs(args);
	//		cmd.run(true);
	//		List<DistanceResult> distanceResults = cmd.getDistanceResults();
	//
	//		// Check results for a specific interaction
	//		boolean ok = false;
	//		for (DistanceResult dr : distanceResults) {
	//			ok |= dr.pdbId.equals("1A12") && dr.aaPos1 == 24 && dr.aaPos2 == 135;
	//		}
	//
	//		Assert.assertTrue("Interaction not found!", ok);
	//	}

	/**
	 * Interaction between two proteins
	 * TODO: Test case using 2G4D. Should find interaction
	 * between amino acid #441 of Senp1 and #60 of Sumo1 proteins
	 * See thesis, Fig 4.5
	 */
	@Test
	public void test_02() {
		Gpr.debug("Test");

		// Command line arguments
		String genome = "testHg19Pdb";
		String pdbDir = "tests/pdb";
		String args[] = { "-pdbDir", pdbDir, genome };

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
			System.out.println(dr);
		}

		Assert.assertTrue("Interaction not found!", ok);

		// TODO: Check comparisson
		throw new RuntimeException("CREATE!!!");
	}

	//	/**
	//	 * Interaction between two proteins
	//	 * TODO: Test case using 4OVU. 
	//	 * From email: "...PIK3R1 mutations was based on protein-protein 
	//	 * 				interactions (PDB:4OVU). It looked like D560 was 
	//	 * 				interacting with PIK3CA"
	//	 */
	//	@Test
	//	public void test_03() {
	//		Gpr.debug("Test");
	//
	//		// Command line arguments
	//		String genome = "testHg19Pdb";
	//		String pdbDir = "tests/pdb";
	//		String args[] = { "-pdbDir", pdbDir, genome };
	//
	//		// Create command
	//		SnpEffCmdPdb cmd = new SnpEffCmdPdb();
	//		cmd.setVerbose(verbose);
	//		cmd.setDebug(debug);
	//		cmd.parseArgs(args);
	//		cmd.run(true);
	//		List<DistanceResult> distanceResults = cmd.getDistanceResults();
	//
	//		// Check resoults
	//		boolean ok = false;
	//		for (DistanceResult dr : distanceResults) {
	//			Gpr.debug("TODO: Find interaction\t" + dr);
	//		}
	//
	//		Assert.assertTrue("Interaction not found!", ok);
	//
	//		// TODO: Check comparison
	//		throw new RuntimeException("CREATE!!!");
	//	}

}
