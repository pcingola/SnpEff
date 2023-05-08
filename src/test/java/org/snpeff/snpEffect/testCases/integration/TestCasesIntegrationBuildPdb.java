package org.snpeff.snpEffect.testCases.integration;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.snpeff.pdb.DistanceResult;
import org.snpeff.snpEffect.commandLine.SnpEffCmdPdb;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for annotation of protein interaction loci
 */
public class TestCasesIntegrationBuildPdb extends TestCasesIntegrationBase {

	/**
	 * Interaction within protein using PDB entry '1A12' (Uniprot 'P18754')
	 *
	 * PDB Entry https://www.rcsb.org/structure/1a12
	 * Uniprot entry: https://www.uniprot.org/uniprotkb/P18754/entry
	 */
	@Test
	public void test_01() {
		Log.debug("Test");

		verbose = true;
		// Command line arguments
		String genome = "testHg19Pdb";
		String pdbDir = path("pdb");
		String idmap = path("pdb") + "/idMap_pdbId_refseqId.txt.gz";
		Log.debug("idMap: " + idmap);
		String args[] = {"-pdbDir", pdbDir, "-idmap", idmap, genome};

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
			ok |= dr.proteinId.equals("1A12") && dr.aaPos1 == 24 && dr.aaPos2 == 135;

			if(verbose && dr.proteinId.equals("1A12") && dr.aaPos1 == 24 && dr.aaPos2 == 135) Log.debug("FOUND:\t" + dr);
			if (debug) Log.debug("INTERACTION:\t" + dr);
		}

		assertTrue(ok, "Interaction not found!");
	}

	/**
	 * Interaction between two proteins
	 * PDB entry 4OVU should have an interaction between chains 'A'
	 * and 'B' (Min distance :2.45 Angstrom)
	 * <p>
	 * AA.pos	AA		chr:pos			transcript
	 * 22		E		3:178916679		NM_006218.2
	 * 533		R		5:67591006		NM_181523.2
	 */
	@Test
	public void test_02() {
		Log.debug("Test");

		// Command line arguments
		String genome = "testHg19Pdb";
		String pdbDir = path("pdb");
		String idmap = path("pdb") + "/idMap_pdbId_refseqId.txt.gz";
		String args[] = {"-pdbDir", pdbDir, "-idmap", idmap, genome};

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
			ok |= dr.proteinId.equals("4OVU") //
					&& dr.aaPos1 == 22 //
					&& dr.aaPos2 == 533 //
					&& dr.trId1.equals("NM_006218.2") //
					&& dr.trId2.equals("NM_181523.2") //
			;
			if (verbose) Log.debug(dr);
		}

		assertTrue(ok, "Interaction not found!");
	}

	/**
	 * Interaction within protein intreaction using Uniprot entry 'P18754' from AlphaFold predictions
	 */
	@Test
	public void test_03_build_alphafold() {
		Log.debug("Test");

		// Command line arguments
		String genome = "testHg19Pdb";
		String pdbDir = path("pdb");
		String idmap = path("pdb") + "/idMap_uniprotId_refSeqId.txt";
		String args[] = {"-pdbDir", pdbDir, "-idmap", idmap, genome};

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
			ok |= dr.proteinId.equals("P18754") && dr.aaPos1 == 24 && dr.aaPos2 == 135;
			if (debug) Log.debug("INTERACTION:\t" + dr);
		}

		assertTrue(ok, "Interaction not found!");
	}

	public void test_03_ion() {
		// Test annotation of Ion interation
		throw new RuntimeException("UNIMPLEMENTED");
	}

	public void test_04_non_covalent_bond() {
		// Test annotation of Non-covalent bond (interaction?)
		throw new RuntimeException("UNIMPLEMENTED");
	}

	public void test_05_covalent_bond() {
		// Test annotation of covalent bond (interation?)
		throw new RuntimeException("UNIMPLEMENTED");
	}

	public void test_06_ligand() {
		// Test annotation of ligand
		throw new RuntimeException("UNIMPLEMENTED");
	}

	public void test_07_small_molecues() {
		// Test annotation of smalle molecules
		throw new RuntimeException("UNIMPLEMENTED");
	}

	public void test_06_interaction_5A_range() {
		// Test annotation of interaction at 5A range
		throw new RuntimeException("UNIMPLEMENTED");
	}

}