package org.snpeff.snpEffect.testCases.unity;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.CytoBands;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;

/**
 * Test case for cytobands
 *
 */
public class TestCasesCytoBands {

	boolean debug = false;
	boolean verbose = false || debug;

	/**
	 * Test that we can load cytobands
	 */
	@Test
	public void test01() {
		Gpr.debug("Test");

		String genomeVer = "testHg19Chr22";
		Config config = new Config(genomeVer);
		config.setVerbose(verbose);
		config.setDebug(debug);

		Genome genome = config.getGenome();
		CytoBands cytoBands = genome.getCytoBands();
		Assert.assertFalse("No cytobands found!", cytoBands.isEmpty());
	}

	/**
	 * Query cytobands
	 */
	@Test
	public void test02() {
		Gpr.debug("Test");

		String genomeVer = "testHg19Chr22";
		Config config = new Config(genomeVer);
		config.setVerbose(verbose);
		config.setDebug(debug);

		Genome genome = config.getGenome();
		CytoBands cytoBands = genome.getCytoBands();

		Chromosome chr = genome.getOrCreateChromosome("22");
		int pos = 4800000;
		Marker m = new Marker(chr, pos, pos);
		Markers cbs = cytoBands.query(m);

		if (verbose) {
			System.out.println("Resutls: ");
			for (Marker cb : cbs)
				System.out.println(cb);
		}

		Assert.assertTrue("Should find one cytoband for query: " + m, cbs.size() == 1);
		Assert.assertEquals("Expected cytoband 'p12' not found: " + m, "p12", cbs.get(0).getId());

	}

}
