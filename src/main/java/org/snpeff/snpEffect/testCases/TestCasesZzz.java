package org.snpeff.snpEffect.testCases;

import org.junit.Test;
import org.snpeff.fileIterator.VariantFileIterator;
import org.snpeff.fileIterator.VariantTxtFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.testCases.integration.CompareEffects;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;

/**
 * Test case for structural variants: Translocation (fusions)
 * 
 * We create two genes (one transcript each). Each gene is in one different chromosome 
 * 
 * Transcripts:
 * 1:10-90, strand: +, id:tr1, Protein
 *      Exons:
 *      1:10-30 'exon1', rank: 1, frame: ., sequence: tatttgtatgaggatttgagt
 *      1:40-90 'exon2', rank: 2, frame: ., sequence: tactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 *      CDS     :   tatttgtatgaggatttgagttactcagtgctgggcaatcccttagctgtcgcgccgcttaccctactattc
 *      Protein :   YLYEDLSYSVLGNPLAVAPLTLLF
 *
 * 2:110-190, strand: +, id:tr2, Protein
 *      Exons:
 *      2:110-125 'exon3', rank: 1, frame: ., sequence: gttaatgggatttcac
 *      2:150-190 'exon4', rank: 2, frame: ., sequence: atgggaacggagtgtcgacagcaccttatggggagctatat
 *      CDS     :   gttaatgggatttcacatgggaacggagtgtcgacagcaccttatggggagctatat
 *      Protein :   VNGISHGNGVSTAPYGELY */
public class TestCasesZzz {

	EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

	boolean debug = false;
	boolean verbose = false || debug;
	long randSeed = 20100629;
	String genomeName = "testCase";

	/**
	 * Read file test: Should throw an exception (chromosome not found)
	 */
	@Test
	public void test_22() {
		Gpr.debug("Test");
		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);

		VariantFileIterator snpFileIterator;
		snpFileIterator = new VariantTxtFileIterator("tests/chr_not_found.out", comp.getConfig().getGenome());
		snpFileIterator.setIgnoreChromosomeErrors(false);
		snpFileIterator.setCreateChromos(false);

		boolean trown = false;
		try {
			// Read all SNPs from file. Note: This should throw an exception "Chromosome not found"
			for (Variant variant : snpFileIterator) {
				Gpr.debug(variant);
			}
		} catch (RuntimeException e) {
			trown = true;
			String expectedMessage = "ERROR: Chromosome 'chrZ' not found! File 'tests/chr_not_found.out', line 1";
			if (e.getMessage().equals(expectedMessage)) ; // OK
			else throw new RuntimeException("This is not the exception I was expecting!\n\tExpected message: '" + expectedMessage + "'\n\tMessage: '" + e.getMessage() + "'", e);
		}

		// If no exception => error
		if (!trown) throw new RuntimeException("This should have thown an exception 'Chromosome not found!' but it didn't");
	}

}
