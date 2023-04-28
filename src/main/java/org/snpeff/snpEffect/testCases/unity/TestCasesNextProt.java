package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.Variant;
import org.snpeff.nextProt.LocationTargetIsoform;
import org.snpeff.nextProt.LocationTargetIsoformInteraction;
import org.snpeff.nextProt.NextProtMarkerFactory;
import org.snpeff.nextProt.NextProtXmlAnnotation;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.util.Log;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCasesNextProt {

    public static boolean verbose = false;

    @Test
    public void test_01_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 0
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 0, 0);

        assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt) nextProtMarkers.get(0);

        // Check nextProt marker
        assertEquals(1000, nextProt.getStart());
        assertEquals(1002, nextProt.getEndClosed());
        assertEquals("nextprot_name", nextProt.getName());
        assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_02_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 1
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 1, 1);

        assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt) nextProtMarkers.get(0);

        // Check nextProt marker
        assertEquals(1003, nextProt.getStart());
        assertEquals(1005, nextProt.getEndClosed());
        assertEquals("nextprot_name", nextProt.getName());
        assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_03_nextprotFactory() {
        // Test: Create a NextProt marker for AA across intron
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 16, 16);

        assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);

        // Check nextProt markers
        assertEquals(1048, nextProt0.getStart());
        assertEquals(1049, nextProt0.getEndClosed());
        assertEquals(1100, nextProt1.getStart());
        assertEquals(1100, nextProt1.getEndClosed());
    }

    @Test
    public void test_04_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 0 (reverse strand transcript)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 0, 0);

        assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt) nextProtMarkers.get(0);

        // Check nextProt marker
        assertEquals(1947, nextProt.getStart());
        assertEquals(1949, nextProt.getEndClosed());
        assertEquals("nextprot_name", nextProt.getName());
        assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_05_nextprotFactory() {
        // Test: Create a NextProt marker for AA number 1 (reverse strand transcript)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 1, 1);

        assertEquals(1, nextProtMarkers.size());
        NextProt nextProt = (NextProt) nextProtMarkers.get(0);

        // Check nextProt marker
        assertEquals(1944, nextProt.getStart());
        assertEquals(1946, nextProt.getEndClosed());
        assertEquals("nextprot_name", nextProt.getName());
        assertEquals("nextprot_accession_1", nextProt.getId());
    }

    @Test
    public void test_06_nextprotFactory() {
        // Test: Create a NextProt marker for AA across intron (reverse strand)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, "nextprot_accession_1", "nextprot_name", 16, 16);

        assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);

        // Check nextProt markers
        assertEquals(1849, nextProt0.getStart());
        assertEquals(1849, nextProt0.getEndClosed());
        assertEquals(1900, nextProt1.getStart());
        assertEquals(1901, nextProt1.getEndClosed());
    }

    @Test
    public void test_07_nextprotFactory() {
        // Test: Create a NextProt marker for 20 AAs across intron
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoform location = new LocationTargetIsoform(testGenome.tr.getId(), 0, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, annotation, location);

        assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);

        // Check nextProt markers
        assertEquals(1000, nextProt0.getStart());
        assertEquals(1049, nextProt0.getEndClosed());
        assertEquals(1100, nextProt1.getStart());
        assertEquals(1112, nextProt1.getEndClosed());
    }

    @Test
    public void test_08_nextprotFactory() {
        // Test: Create a NextProt marker for 20 AAs across intron (uses NextProtXmlAnnotation and LocationTargetIsoform)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoform location = new LocationTargetIsoform(testGenome.tr.getId(), 0, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, annotation, location);

        assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);

        // Check nextProt markers
        assertEquals(1837, nextProt0.getStart());
        assertEquals(1849, nextProt0.getEndClosed());
        assertEquals(1900, nextProt1.getStart());
        assertEquals(1949, nextProt1.getEndClosed());
    }

    @Test
    public void test_09_nextprotFactory() {
        // Test: Create a NextProt marker for 20 AAs across intron, transcript in reverse strand (uses NextProtXmlAnnotation and LocationTargetIsoform)
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoform location = new LocationTargetIsoform(testGenome.tr.getId(), 0, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, annotation, location);

        assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);

        // Check nextProt markers
        assertEquals(1000, nextProt0.getStart());
        assertEquals(1049, nextProt0.getEndClosed());
        assertEquals(1100, nextProt1.getStart());
        assertEquals(1112, nextProt1.getEndClosed());
    }

    @Test
    public void test_10_nextprotFactory() {
        // Test: Create a NextProt interaction marker (uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(), 10, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, annotation, location);

        assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);

        // Check nextProt markers
        assertEquals(1030, nextProt0.getStart());
        assertEquals(1032, nextProt0.getEndClosed());
        assertEquals(1110, nextProt1.getStart()); //
        assertEquals(1112, nextProt1.getEndClosed());
    }

    @Test
    public void test_11_nextprotFactory() {
        // Test: Create a NextProt interaction marker in a reverse strand(uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(), 10, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, annotation, location);

        assertEquals(2, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);

        // Check nextProt markers
        assertEquals(1837, nextProt0.getStart());
        assertEquals(1839, nextProt0.getEndClosed());
        assertEquals(1917, nextProt1.getStart());
        assertEquals(1919, nextProt1.getEndClosed());
    }

    @Test
    public void test_12_nextprotFactory() {
        // Test: Create a NextProt interaction marker with one AA that spans across an intron (uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(false);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(), 16, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, annotation, location);

        assertEquals(3, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);
        NextProt nextProt2 = (NextProt) nextProtMarkers.get(2);

        // Check nextProt markers
        assertEquals(1048, nextProt0.getStart());
        assertEquals(1049, nextProt0.getEndClosed());
        assertEquals(1100, nextProt1.getStart());
        assertEquals(1100, nextProt1.getEndClosed());
        assertEquals(1110, nextProt2.getStart());
        assertEquals(1112, nextProt2.getEndClosed());
    }

    @Test
    public void test_13_nextprotFactory() {
        // Test: Create a NextProt interaction marker with one AA that spans across an intron, reverse strand (uses NextProtXmlAnnotation and LocationTargetIsoformInteraction)
        Log.debug("Test");
        var testGenome = new TestGenome(true);
        NextProtMarkerFactory factory = new NextProtMarkerFactory(testGenome.config);

        NextProtXmlAnnotation annotation = new NextProtXmlAnnotation(null, "category");
        LocationTargetIsoformInteraction location = new LocationTargetIsoformInteraction(testGenome.tr.getId(), 16, 20);
        Markers nextProtMarkers = factory.nextProt(testGenome.tr, annotation, location);

        assertEquals(3, nextProtMarkers.size());
        nextProtMarkers.sort();
        NextProt nextProt0 = (NextProt) nextProtMarkers.get(0);
        NextProt nextProt1 = (NextProt) nextProtMarkers.get(1);
        NextProt nextProt2 = (NextProt) nextProtMarkers.get(2);

        // Check nextProt markers
        assertEquals(1837, nextProt0.getStart());
        assertEquals(1839, nextProt0.getEndClosed());
        assertEquals(1849, nextProt1.getStart());
        assertEquals(1849, nextProt1.getEndClosed());
        assertEquals(1900, nextProt2.getStart());
        assertEquals(1901, nextProt2.getEndClosed());
    }

    @Test
    public void test_14() {
        // Test annotation non-synonymous variant + "NextProt highly conserved"
        // => Nextprot.EffectImpact = HIGH;
        Log.debug("Test");
        // Create a genome
        var testGenome = new TestGenome(false);
        // Add a highly conserved 'nextprot' effect
        var pos = testGenome.tr.getStart() + 3;
        NextProt nextProt = new NextProt(testGenome.tr, pos, pos, "nextprot_1", "test_nexprot_effect");
        nextProt.setHighlyConservedAaSequence(true);
        testGenome.add(nextProt);
        // Create a variant
        Variant variant = new Variant(testGenome.chr, pos, "C", "A"); // Non-synonimous variant ('P' to 'T')
        // Predict effect and check results
        testGenome.checkEffect(variant, EffectType.NEXT_PROT, VariantEffect.EffectImpact.HIGH, 1);
    }

    @Test
    public void test_15() {
        // Test annotation synonymous variant + "NextProt highly conserved"
        // => Nextprot.EffectImpact = LOW;
        Log.debug("Test");
        // Create a genome
        var testGenome = new TestGenome(false);
        // Add a highly conserved 'nextprot' effect
        var pos = testGenome.tr.getStart() + 5;
        NextProt nextProt = new NextProt(testGenome.tr, pos, pos, "nextprot_1", "test_nexprot_effect");
        nextProt.setHighlyConservedAaSequence(true);
        testGenome.add(nextProt);
        // Create a variant
        Variant variant = new Variant(testGenome.chr, pos, "C", "A"); // synonimous variant ('P' to 'P')
        // Predict effect and check results
        testGenome.checkEffect(variant, EffectType.NEXT_PROT, VariantEffect.EffectImpact.LOW, 1);
    }

    @Test
    public void test_16() {
        // Test annotation non-synonymous variant + "NextProt NOT highly conserved"
        // => Nextprot.EffectImpact = LOW;
        Log.debug("Test");
        // Create a genome
        var testGenome = new TestGenome(false);
        // Add a highly conserved 'nextprot' effect
        var pos = testGenome.tr.getStart() + 3;
        NextProt nextProt = new NextProt(testGenome.tr, pos, pos, "nextprot_1", "test_nexprot_effect");
        nextProt.setHighlyConservedAaSequence(false);
        testGenome.add(nextProt);
        // Create a variant
        Variant variant = new Variant(testGenome.chr, pos, "C", "A"); // Non-synonimous variant ('P' to 'T')
        // Predict effect and check results
        testGenome.checkEffect(variant, EffectType.NEXT_PROT, VariantEffect.EffectImpact.LOW, 1);
    }

    @Test
    public void test_17() {
        // Test annotation synonymous variant + "NextProt NOT highly conserved"
        // => Nextprot.EffectImpact = MODIFIER;
        Log.debug("Test");
        // Create a genome
        var testGenome = new TestGenome(false);
        // Add a highly conserved 'nextprot' effect
        var pos = testGenome.tr.getStart() + 5;
        NextProt nextProt = new NextProt(testGenome.tr, pos, pos, "nextprot_1", "test_nexprot_effect");
        nextProt.setHighlyConservedAaSequence(false);
        testGenome.add(nextProt);
        // Create a variant
        Variant variant = new Variant(testGenome.chr, pos, "C", "A"); // synonimous variant ('P' to 'P')
        // Predict effect and check results
        testGenome.checkEffect(variant, EffectType.NEXT_PROT, VariantEffect.EffectImpact.MODIFIER, 1);
    }

    @Test
    public void test_18() {
        // Test annotation DEL variant + "NextProt highly conserved"
        // => Nextprot.EffectImpact = HIGH;
        Log.debug("Test");
        // Create a genome
        var testGenome = new TestGenome(false);
        // Add a highly conserved 'nextprot' effect
        var pos = testGenome.tr.getStart() + 3;
        NextProt nextProt = new NextProt(testGenome.tr, pos, pos, "nextprot_1", "test_nexprot_effect");
        nextProt.setHighlyConservedAaSequence(true);
        testGenome.add(nextProt);
        // Create a variant
        Variant variant = new Variant(testGenome.chr, pos, "C", ""); // Deletion
        // Predict effect and check results
        testGenome.checkEffect(variant, EffectType.NEXT_PROT, VariantEffect.EffectImpact.HIGH, 1);
    }

    @Test
    public void test_19() {
        // Test annotation DEL variant + "NextProt highly conserved"
        // => Nextprot.EffectImpact = HIGH;
        Log.debug("Test");
        // Create a genome
        var testGenome = new TestGenome(false);
        // Add a highly conserved 'nextprot' effect
        var pos = testGenome.tr.getStart() + 3;
        NextProt nextProt = new NextProt(testGenome.tr, pos, pos, "nextprot_1", "test_nexprot_effect");
        nextProt.setHighlyConservedAaSequence(false);
        testGenome.add(nextProt);
        // Create a variant
        Variant variant = new Variant(testGenome.chr, pos, "C", ""); // Deletion
        // Predict effect and check results
        testGenome.checkEffect(variant, EffectType.NEXT_PROT, VariantEffect.EffectImpact.HIGH, 1);
    }

}
