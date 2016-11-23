package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Test;
import org.snpeff.interval.BioType;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.CircularCorrection;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.EffFormatVersion;

import junit.framework.Assert;

/**
 * Test cases for circular genomes
 */
public class TestCasesCircular {

	EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

	boolean debug = false;
	boolean verbose = false || debug;

	@Test
	public void test_circular_01() {
		Gpr.debug("Test");

		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");
		boolean strandMinus = false;
		Gene gene = new Gene(chr, 0, chr.getEnd(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
		Transcript tr = new Transcript(gene, 0, chr.getEnd(), strandMinus, "tr1");
		tr.add(new Cds(tr, 800, 850, strandMinus, "cds1"));
		tr.add(new Cds(tr, 900, 950, strandMinus, "cds2"));
		tr.add(new Cds(tr, 10, 50, strandMinus, "cds3"));

		CircularCorrection cc = new CircularCorrection(tr);
		cc.setDebug(debug);
		Transcript trCorr = cc.correct();

		List<Cds> cdss = trCorr.getCds();
		Marker cds1 = cdss.get(0);
		Marker cds2 = cdss.get(1);
		Marker cds3 = cdss.get(2);

		Assert.assertEquals("CDS 1 start does not match", -200, cds1.getStart());
		Assert.assertEquals("CDS 2 start does not match", -100, cds2.getStart());
		Assert.assertEquals("CDS 3 start does not match", 10, cds3.getStart());
	}

	@Test
	public void test_circular_02() {
		Gpr.debug("Test");

		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");
		boolean strandMinus = true;
		Gene gene = new Gene(chr, 0, chr.getEnd(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
		Transcript tr = new Transcript(gene, 0, chr.getEnd(), strandMinus, "tr1");

		tr.add(new Cds(tr, 200, 250, strandMinus, "ex1"));
		tr.add(new Cds(tr, 100, 150, strandMinus, "ex2"));
		tr.add(new Cds(tr, 900, 950, strandMinus, "ex3"));

		CircularCorrection cc = new CircularCorrection(tr);
		cc.setDebug(debug);
		Transcript trCorr = cc.correct();

		List<Cds> cdss = trCorr.getCds();
		Marker cds1 = cdss.get(0);
		Marker cds2 = cdss.get(1);
		Marker cds3 = cdss.get(2);

		Assert.assertEquals("Exon 3 start does not match", -100, cds1.getStart());
		Assert.assertEquals("Exon 2 start does not match", 100, cds2.getStart());
		Assert.assertEquals("Exon 1 start does not match", 200, cds3.getStart());
	}

	@Test
	public void test_circular_03() {
		Gpr.debug("Test");

		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");
		boolean strandMinus = true;
		Gene gene = new Gene(chr, 0, chr.getEnd(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
		Transcript tr = new Transcript(gene, 0, chr.getEnd(), strandMinus, "tr1");

		tr.add(new Cds(tr, 200, 250, strandMinus, "ex1"));
		tr.add(new Cds(tr, 100, 150, strandMinus, "ex2"));
		tr.add(new Cds(tr, -100, -50, strandMinus, "ex3"));

		CircularCorrection cc = new CircularCorrection(tr);
		cc.setDebug(debug);
		Transcript trCorr = cc.correct();
		Assert.assertNull("No correction expexted for this transcript", trCorr);
	}

	@Test
	public void test_circular_04() {
		Gpr.debug("Test");

		Genome genome = new Genome("test");
		Chromosome chr = new Chromosome(genome, 0, 999, "1");
		boolean strandMinus = true;
		Gene gene = new Gene(chr, 0, chr.getEnd(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
		Transcript tr = new Transcript(gene, 0, chr.getEnd(), strandMinus, "tr1");

		tr.add(new Cds(tr, 800, 850, true, "ex1"));
		tr.add(new Cds(tr, 900, 950, true, "ex2"));
		tr.add(new Cds(tr, 1010, 1050, true, "ex3"));

		CircularCorrection cc = new CircularCorrection(tr);
		cc.setDebug(debug);
		Transcript trCorr = cc.correct();

		List<Cds> cdss = trCorr.getCds();
		Marker cds1 = cdss.get(0);
		Marker cds2 = cdss.get(1);
		Marker cds3 = cdss.get(2);

		Assert.assertEquals("Exon 1 start does not match", -200, cds1.getStart());
		Assert.assertEquals("Exon 2 start does not match", -100, cds2.getStart());
		Assert.assertEquals("Exon 3 start does not match", 10, cds3.getStart());
	}
}
