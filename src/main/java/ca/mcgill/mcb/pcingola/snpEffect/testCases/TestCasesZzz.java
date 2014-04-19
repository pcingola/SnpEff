package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	public void test_00_chrOrder() {
		Genome genome = new Genome("test");

		Chromosome chrA = new Chromosome(genome, 0, 1, 1, "chr1");
		Chromosome chrB = new Chromosome(genome, 0, 1, 1, "scaffold0001");

		// Order: A < B < C
		Assert.assertTrue(chrA.compareTo(chrB) < 0);
	}
}
