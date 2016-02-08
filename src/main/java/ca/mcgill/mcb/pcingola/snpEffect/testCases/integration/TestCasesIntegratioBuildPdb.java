package ca.mcgill.mcb.pcingola.snpEffect.testCases.integration;

import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.pdb.DistanceResult;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdPdb;
import ca.mcgill.mcb.pcingola.util.Gpr;
import junit.framework.Assert;

/**
 * Test cases for annotation of protein interaction loci
 */
public class TestCasesIntegratioBuildPdb extends TestCasesIntegrationBase {

	public TestCasesIntegratioBuildPdb() {
	}

	/**
	 * Interaction within protein
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");

		// Command line arguments
		String genome = "";
		String pdbDir = "test/pdb";
		String args[] = { "-pdbDir", pdbDir, genome };

		// Create command
		SnpEffCmdPdb cmd = new SnpEffCmdPdb();
		cmd.parseArgs(args);
		cmd.run(true);
		List<DistanceResult> distanceResults = cmd.getDistanceResults();

		// Check resoults
		boolean ok = false;
		for (DistanceResult dr : distanceResults) {
			Gpr.debug("TODO: Find interaction\t" + dr);
		}

		Assert.assertTrue("Interaction not found!", ok);

		throw new RuntimeException("CREATE!!!");
	}

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
		String genome = "";
		String pdbDir = "test/pdb";
		String args[] = { "-pdbDir", pdbDir, genome };

		// Create command
		SnpEffCmdPdb cmd = new SnpEffCmdPdb();
		cmd.parseArgs(args);
		cmd.run(true);
		List<DistanceResult> distanceResults = cmd.getDistanceResults();

		// Check resoults
		boolean ok = false;
		for (DistanceResult dr : distanceResults) {
			Gpr.debug("TODO: Find interaction\t" + dr);
		}

		Assert.assertTrue("Interaction not found!", ok);

		// TODO: Check comparisson
		throw new RuntimeException("CREATE!!!");
	}

	/**
	 * Interaction between two proteins
	 * TODO: Test case using 4OVU. 
	 * From email: "...PIK3R1 mutations was based on protein-protein 
	 * 				interactions (PDB:4OVU). It looked like D560 was 
	 * 				interacting with PIK3CA"
	 */
	@Test
	public void test_03() {
		Gpr.debug("Test");

		// Command line arguments
		String genome = "";
		String pdbDir = "test/pdb";
		String args[] = { "-pdbDir", pdbDir, genome };

		// Create command
		SnpEffCmdPdb cmd = new SnpEffCmdPdb();
		cmd.parseArgs(args);
		cmd.run(true);
		List<DistanceResult> distanceResults = cmd.getDistanceResults();

		// Check resoults
		boolean ok = false;
		for (DistanceResult dr : distanceResults) {
			Gpr.debug("TODO: Find interaction\t" + dr);
		}

		Assert.assertTrue("Interaction not found!", ok);

		// TODO: Check comparisson
		throw new RuntimeException("CREATE!!!");
	}

}
