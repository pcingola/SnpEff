package org.snpeff.snpEffect.testCases.unity;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.interval.*;
import org.snpeff.nextProt.*;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import java.util.Random;

class TestGenome {
    public Config config;
    public Genome genome;
    public Chromosome chr;
    public Gene gene;
    public Transcript tr;
    String genomeName;

    public TestGenome(boolean strandMinus) {
        Random random = new Random(20211222);
        genomeName = "test_genome";
        genome = new Genome(genomeName);
        chr = new Chromosome(genome, 1, 10000000, "chr1");
        gene = new Gene(chr, 1000, 2000, strandMinus, "gene1", "gene1", BioType.protein_coding);
        tr = new Transcript(gene, gene.getStart(), gene.getEnd(), strandMinus, "tr1");
        tr.setProteinCoding(true);
        for (int i = gene.getStart(); i < gene.getEnd(); i += 100) {
            Exon exon = new Exon(tr, i, i + 49, strandMinus, "exon" + i, i); // Exon is 50 bases (so that there are amino acids spanning across introns)
            exon.setSequence(GprSeq.randSequence(random, exon.size()));
            tr.add(exon);
        }

        config = new Config(genome);
    }
}

public class TestCasesNextProt {

    public static boolean verbose = false;

    @Test
    public void test_01_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 0
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers =  factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 0, 0);

        Assert.assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt)nextProtMarkers.get(0);

        // Check nextProt marker
        Assert.assertEquals(1000, nextProt.getStart());
        Assert.assertEquals(1002, nextProt.getEnd());
        Assert.assertEquals("nextprot_name", nextProt.getName());
        Assert.assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_02_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 1
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 1, 1);

        Assert.assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt)nextProtMarkers.get(0);

                // Check nextProt marker
        Assert.assertEquals(1003, nextProt.getStart());
        Assert.assertEquals(1005, nextProt.getEnd());
        Assert.assertEquals("nextprot_name", nextProt.getName());
        Assert.assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_03_nextprotFactory() {
        // Test: Create a NextProt marker for AA across intron
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 16, 16);

        Assert.assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);

        // Check nextProt markers
        Assert.assertEquals(1048, nextProt0.getStart());
        Assert.assertEquals(1049, nextProt0.getEnd());
        Assert.assertEquals(1100, nextProt1.getStart());
        Assert.assertEquals(1100, nextProt1.getEnd());
    }

    @Test
    public void test_04_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 0 (reverse strand transcript)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers =  factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 0, 0);

        Assert.assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt)nextProtMarkers.get(0);

        // Check nextProt marker
        Assert.assertEquals(1947, nextProt.getStart());
        Assert.assertEquals(1949, nextProt.getEnd());
        Assert.assertEquals("nextprot_name", nextProt.getName());
        Assert.assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_05_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 1 (reverse strand transcript)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 1, 1);

        Assert.assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt)nextProtMarkers.get(0);

        // Check nextProt marker
        Assert.assertEquals(1944, nextProt.getStart());
        Assert.assertEquals(1946, nextProt.getEnd());
        Assert.assertEquals("nextprot_name", nextProt.getName());
        Assert.assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_06_nextprotFactory() {
        // Test: Create a NextProt marker for AA across intron (reverse strand)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 16, 16);

        Assert.assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);

        // Check nextProt markers
        Assert.assertEquals(1849, nextProt0.getStart());
        Assert.assertEquals(1849, nextProt0.getEnd());
        Assert.assertEquals(1900, nextProt1.getStart());
        Assert.assertEquals(1901, nextProt1.getEnd());
    }

    @Test
    public void test_07_nextprotFactory() {
        // Test: Create a NextProt marker for 20 AAs across intron
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoform location = new LocationTargetIsoform(testGenome.tr.getId(),0, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr,  annotation,  location);

        Assert.assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);

        // Check nextProt markers
        Assert.assertEquals(1000, nextProt0.getStart());
        Assert.assertEquals(1049, nextProt0.getEnd());
        Assert.assertEquals(1100, nextProt1.getStart());
        Assert.assertEquals(1112, nextProt1.getEnd());
    }

    @Test
    public void test_08_nextprotFactory() {
        // Test: Create a NextProt marker for 20 AAs across intron (uses NextProtXmlAnnotation and LocationTargetIsoform)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoform location = new LocationTargetIsoform(testGenome.tr.getId(),0, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr,  annotation,  location);

        Assert.assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);

        // Check nextProt markers
        Assert.assertEquals(1837, nextProt0.getStart());
        Assert.assertEquals(1849, nextProt0.getEnd());
        Assert.assertEquals(1900, nextProt1.getStart());
        Assert.assertEquals(1949, nextProt1.getEnd());
    }

    @Test
    public void test_09_nextprotFactory() {
        // Test: Create a NextProt marker for 20 AAs across intron, transcript in reverse strand (uses NextProtXmlAnnotation and LocationTargetIsoform)
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoform location = new LocationTargetIsoform(testGenome.tr.getId(),0, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr,  annotation,  location);

        Assert.assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);

        // Check nextProt markers
        Assert.assertEquals(1000, nextProt0.getStart());
        Assert.assertEquals(1049, nextProt0.getEnd());
        Assert.assertEquals(1100, nextProt1.getStart());
        Assert.assertEquals(1112, nextProt1.getEnd());
    }

    @Test
    public void test_10_nextprotFactory() {
        // Test: Create a NextProt interaction marker (uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(),10, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr,  annotation,  location);

        Assert.assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);

        // Check nextProt markers
        Assert.assertEquals(1030, nextProt0.getStart());
        Assert.assertEquals(1032, nextProt0.getEnd());
        Assert.assertEquals(1110, nextProt1.getStart()); //
        Assert.assertEquals(1112, nextProt1.getEnd());
    }

    @Test
    public void test_11_nextprotFactory() {
        // Test: Create a NextProt interaction marker in a reverse strand(uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(),10, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr,  annotation,  location);

        Assert.assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);

        // Check nextProt markers
        Assert.assertEquals(1837, nextProt0.getStart());
        Assert.assertEquals(1839, nextProt0.getEnd());
        Assert.assertEquals(1917, nextProt1.getStart());
        Assert.assertEquals(1919, nextProt1.getEnd());
    }

    @Test
    public void test_12_nextprotFactory() {
        // Test: Create a NextProt interaction marker with one AA that spans across an intron (uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(),16, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr,  annotation,  location);

        Assert.assertEquals(3, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);
        NextProt nextProt2 = (NextProt)nextProtMarkers.get(2);

        // Check nextProt markers
        Assert.assertEquals(1048, nextProt0.getStart());
        Assert.assertEquals(1049, nextProt0.getEnd());
        Assert.assertEquals(1100, nextProt1.getStart());
        Assert.assertEquals(1100, nextProt1.getEnd());
        Assert.assertEquals(1110, nextProt2.getStart());
        Assert.assertEquals(1112, nextProt2.getEnd());
    }

    @Test
    public void test_13_nextprotFactory() {
        // Test: Create a NextProt interaction marker with one AA that spans across an intron, reverse strand (uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(),16, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr,  annotation,  location);

        Assert.assertEquals(3, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt)nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt)nextProtMarkers.get(1);
        NextProt nextProt2 = (NextProt)nextProtMarkers.get(2);

        // Check nextProt markers
        Assert.assertEquals(1837, nextProt0.getStart());
        Assert.assertEquals(1839, nextProt0.getEnd());
        Assert.assertEquals(1849, nextProt1.getStart());
        Assert.assertEquals(1849, nextProt1.getEnd());
        Assert.assertEquals(1900, nextProt2.getStart());
        Assert.assertEquals(1901, nextProt2.getEnd());
    }


}
