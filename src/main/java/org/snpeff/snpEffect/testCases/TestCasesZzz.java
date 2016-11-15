package org.snpeff.snpEffect.testCases;

import org.junit.Test;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;

import junit.framework.Assert;

/**
 * Test cases in preparation or debugging 
 */
public class TestCasesZzz {

	EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

	boolean debug = false;
	boolean verbose = false || debug;

	@Test
	public void test_11_circular_01() {
		Gpr.debug("Test");

		Markers markers = new Markers();
		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");

		markers.add(new Marker(chr, 800, 850, false, "ex1"));
		markers.add(new Marker(chr, 900, 950, false, "ex2"));
		markers.add(new Marker(chr, 10, 50, false, "ex3"));

		Markers corr = markers.circularCorrect();

		Marker ex1 = corr.get(0);
		Marker ex2 = corr.get(1);
		Marker ex3 = corr.get(2);

		Assert.assertEquals("Exon 1 start does not match", -200, ex1.getStart());
		Assert.assertEquals("Exon 2 start does not match", -100, ex2.getStart());
		Assert.assertEquals("Exon 3 start does not match", 10, ex3.getStart());
	}

	@Test
	public void test_11_circular_02() {
		Gpr.debug("Test");

		Markers markers = new Markers();
		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");

		markers.add(new Marker(chr, 200, 250, true, "ex1"));
		markers.add(new Marker(chr, 100, 150, true, "ex2"));
		markers.add(new Marker(chr, 900, 950, true, "ex3"));

		Markers corr = markers.circularCorrect();
		Gpr.debug(corr);

		Marker ex1 = corr.get(0);
		Marker ex2 = corr.get(1);
		Marker ex3 = corr.get(2);

		Assert.assertEquals("Exon 1 start does not match", 200, ex1.getStart());
		Assert.assertEquals("Exon 2 start does not match", 100, ex2.getStart());
		Assert.assertEquals("Exon 3 start does not match", -100, ex3.getStart());
	}

	@Test
	public void test_11_circular_03() {
		Gpr.debug("Test");

		Markers markers = new Markers();
		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");

		markers.add(new Marker(chr, 200, 250, true, "ex1"));
		markers.add(new Marker(chr, 100, 150, true, "ex2"));
		markers.add(new Marker(chr, -100, -50, true, "ex3"));

		Markers corr = markers.circularCorrect();
		Gpr.debug(corr);

		Marker ex1 = corr.get(0);
		Marker ex2 = corr.get(1);
		Marker ex3 = corr.get(2);

		Assert.assertEquals("Exon 1 start does not match", 200, ex1.getStart());
		Assert.assertEquals("Exon 2 start does not match", 100, ex2.getStart());
		Assert.assertEquals("Exon 3 start does not match", -100, ex3.getStart());
	}

	@Test
	public void test_11_circular_04() {
		Gpr.debug("Test");

		Markers markers = new Markers();
		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");

		markers.add(new Marker(chr, 800, 850, true, "ex1"));
		markers.add(new Marker(chr, 900, 950, true, "ex2"));
		markers.add(new Marker(chr, 1010, 1050, true, "ex3"));

		Markers corr = markers.circularCorrect();
		Gpr.debug(corr);

		Marker ex1 = corr.get(0);
		Marker ex2 = corr.get(1);
		Marker ex3 = corr.get(2);

		Assert.assertEquals("Exon 1 start does not match", -200, ex1.getStart());
		Assert.assertEquals("Exon 2 start does not match", -100, ex2.getStart());
		Assert.assertEquals("Exon 3 start does not match", 10, ex3.getStart());
	}
}
