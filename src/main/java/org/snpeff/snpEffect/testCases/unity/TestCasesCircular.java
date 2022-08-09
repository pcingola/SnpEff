package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.*;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


/**
 * Test cases for circular genomes
 */
public class TestCasesCircular {

    EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN;

    boolean debug = false;
    boolean verbose = false || debug;

    @Test
    public void test_circular_01() {
        Log.debug("Test");

        Genome genome = new Genome("test");
        Chromosome chr = new Chromosome(genome, 0, 999, "1");
        boolean strandMinus = false;
        Gene gene = new Gene(chr, 0, chr.getEndClosed(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
        Transcript tr = new Transcript(gene, 0, chr.getEndClosed(), strandMinus, "tr1");
        tr.add(new Cds(tr, 800, 850, strandMinus, "cds1"));
        tr.add(new Cds(tr, 900, 950, strandMinus, "cds2"));
        tr.add(new Cds(tr, 10, 50, strandMinus, "cds3"));

        CircularCorrection cc = new CircularCorrection(tr);
        cc.setCorrectLargeGap(true);
        cc.setDebug(debug);
        cc.correct();

        List<Cds> cdss = tr.getCds();
        Marker cds1 = cdss.get(0);
        Marker cds2 = cdss.get(1);
        Marker cds3 = cdss.get(2);

        assertEquals(-200, cds1.getStart(), "CDS 1 start does not match");
        assertEquals(-100, cds2.getStart(), "CDS 2 start does not match");
        assertEquals(10, cds3.getStart(), "CDS 3 start does not match");
    }

    @Test
    public void test_circular_02() {
        Log.debug("Test");

        Genome genome = new Genome("test");
        Chromosome chr = new Chromosome(genome, 0, 999, "1");
        boolean strandMinus = true;
        Gene gene = new Gene(chr, 0, chr.getEndClosed(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
        Transcript tr = new Transcript(gene, 0, chr.getEndClosed(), strandMinus, "tr1");

        tr.add(new Cds(tr, 200, 250, strandMinus, "ex1"));
        tr.add(new Cds(tr, 100, 150, strandMinus, "ex2"));
        tr.add(new Cds(tr, 900, 950, strandMinus, "ex3"));

        CircularCorrection cc = new CircularCorrection(tr);
        cc.setCorrectLargeGap(true);
        cc.setDebug(debug);
        cc.correct();

        List<Cds> cdss = tr.getCds();
        Marker cds1 = cdss.get(0);
        Marker cds2 = cdss.get(1);
        Marker cds3 = cdss.get(2);

        assertEquals(-100, cds1.getStart(), "Exon 3 start does not match");
        assertEquals(100, cds2.getStart(), "Exon 2 start does not match");
        assertEquals(200, cds3.getStart(), "Exon 1 start does not match");
    }

    @Test
    public void test_circular_03() {
        Log.debug("Test");

        Genome genome = new Genome("test");
        Chromosome chr = new Chromosome(genome, 0, 999, "1");
        boolean strandMinus = true;
        Gene gene = new Gene(chr, 0, chr.getEndClosed(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
        Transcript tr = new Transcript(gene, 0, chr.getEndClosed(), strandMinus, "tr1");

        tr.add(new Cds(tr, 200, 250, strandMinus, "ex1"));
        tr.add(new Cds(tr, 100, 150, strandMinus, "ex2"));
        tr.add(new Cds(tr, -100, -50, strandMinus, "ex3"));

        CircularCorrection cc = new CircularCorrection(tr);
        cc.setDebug(debug);
        cc.correct();
        assertFalse(cc.isCorrected(), "No correction expexted for this transcript");
    }

    @Test
    public void test_circular_04() {
        Log.debug("Test");

        Genome genome = new Genome("test");
        Chromosome chr = new Chromosome(genome, 0, 999, "1");
        boolean strandMinus = true;
        Gene gene = new Gene(chr, 0, chr.getEndClosed(), strandMinus, "geneId1", "geneName1", BioType.protein_coding);
        Transcript tr = new Transcript(gene, 0, chr.getEndClosed(), strandMinus, "tr1");

        tr.add(new Cds(tr, 800, 850, true, "ex1"));
        tr.add(new Cds(tr, 900, 950, true, "ex2"));
        tr.add(new Cds(tr, 1010, 1050, true, "ex3"));

        CircularCorrection cc = new CircularCorrection(tr);
        cc.setDebug(debug);
        cc.correct();

        List<Cds> cdss = tr.getCds();
        Marker cds1 = cdss.get(0);
        Marker cds2 = cdss.get(1);
        Marker cds3 = cdss.get(2);

        assertEquals(-200, cds1.getStart(), "Exon 1 start does not match");
        assertEquals(-100, cds2.getStart(), "Exon 2 start does not match");
        assertEquals(10, cds3.getStart(), "Exon 3 start does not match");
    }
}
