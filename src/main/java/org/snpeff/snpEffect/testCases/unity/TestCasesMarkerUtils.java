package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.*;
import org.snpeff.util.Log;
import org.snpeff.util.Tuple;

import java.util.HashSet;
import java.util.Map;
import java.util.Random;

public class TestCasesMarkerUtils {

    int maxLen = 100;
    int minLen = 1;
    int maxGap = 100;
    int minGap = 50;
    boolean verbose = false;
    boolean compareCdsTestsEnable = false;
    Random rand;
    Genome genome;

    public TestCasesMarkerUtils() {
        super();
        initRand();
        String genomeName = "testCaseHg";
        genome = new Genome(genomeName);

        // Create chromosomes
        for (int i = 1; i < 22; i++)
            genome.add(new Chromosome(genome, 0, 0, "" + i));
        genome.add(new Chromosome(genome, 0, 0, "X"));
        genome.add(new Chromosome(genome, 0, 0, "Y"));
    }

    /**
     * Create a list of markers
     *
     * @return Two collections of markers (in a tuple), the original one and the collapsed one
     */
    Tuple<Markers, Markers> createMarkers(Chromosome chr, int numMarkers) {
        Markers markers = new Markers();
        Markers markersCollapsed = new Markers();
        int start = 0, startPrev = -1, end = 0, gap = 0;

        Marker m = null, mcol = null;
        for (int i = 0; i < numMarkers; i++) {
            // Interval size
            int size = rand.nextInt(maxLen) + minLen;
            end = start + size;
            if (startPrev < 0) startPrev = start;

            // Create marker
            m = new Marker(chr, start, end, false, "");
            markers.add(m);

            // Next interval
            gap = rand.nextInt(maxGap);
            mcol = new Marker(chr, startPrev, end, false, "");
            if (gap <= minGap) {
                gap = 1;
            } else {
                markersCollapsed.add(mcol);
                mcol = null;
                startPrev = -1;
            }

            start = end + gap;
        }
        if (mcol != null) markersCollapsed.add(mcol);

        return new Tuple<>(markers, markersCollapsed);
    }

    void initRand() {
        rand = new Random(20121115);
    }

    /**
     * Show a collection of sorted intervals as a string
     */
    String markers2string(Markers markers) {
        StringBuilder sb = new StringBuilder();

        int prevEnd = -1;
        markers.sort(false, false);
        for (Marker m : markers) {
            if (prevEnd >= 0) {
                for (int i = prevEnd; i < m.getStart(); i++)
                    sb.append('-');
            }

            for (int i = m.getStart(); i <= m.getEndClosed(); i++)
                sb.append('M');

            prevEnd = m.getEndClosed() + 1;
        }

        return sb.toString();
    }

    /**
     * Test for collapsing markers with zero gaps
     */
    @Test
    public void test_collapseZeroGap() {
        Log.debug("Test");
        initRand();
        int numMarkers = 20;
        Chromosome chr = genome.getChromosome("1");

        for (int num = 1; num < 1000; num++) {
            // Create markers
            Tuple<Markers, Markers> tupleMarkers = createMarkers(chr, numMarkers);
            Markers markersOri = tupleMarkers.first;
            Markers markersCollapsedOri = tupleMarkers.second;

            //---
            // Compare created markers
            //---
            String mStr = markers2string(markersOri);
            String mColOriStr = markers2string(markersCollapsedOri);
            if (verbose)
                Log.debug("Iteration : " + num + "\n\tMarkers           : " + mStr + "\n\tMarkers collapsed : " + mColOriStr);

            // Are generated intervasl OK?
            if (!mStr.equals(mColOriStr)) {
                System.err.println("Markers : ");
                for (Marker m : markersOri)
                    System.err.println(m);

                System.err.println("Markers collapsed: ");
                for (Marker m : markersCollapsedOri)
                    System.err.println(m);

                throw new RuntimeException("Error creating markers! Markers and collapsed marker do not match!\n\t" + mStr + "\n\t" + mColOriStr);
            }

            //---
            // Compare to Markers.collapseZeroGap
            //---

            // Collapse
            Map<Marker, Marker> collapse = MarkerUtil.collapseZeroGap(markersOri);
            // Get unique markers
            HashSet<Marker> collapsed = new HashSet<>(collapse.values());

            Markers markers = new Markers();
            markers.addAll(collapsed);
            String mColStr = markers2string(markers); // Create string

            // Are generated intervasl OK?
            if (!mColStr.equals(mStr)) {
                Log.debug("Error checing markers! Markers and collapsed marker do not match!\n\t" + mStr + "\n\t" + mColStr);

                System.err.println("Markers : ");
                for (Marker m : markersOri)
                    System.err.println(m);

                System.err.println("Markers collapsed: ");
                markers = new Markers();
                markers.addAll(collapse.keySet());
                Markers keySorted = markers.sort(false, false);
                for (Marker mkey : keySorted)
                    System.err.println(mkey + "\t->\t" + collapse.get(mkey));

                throw new RuntimeException("Error checing markers! Markers and collapsed marker do not match!\n\t" + mStr + "\n\t" + mColStr);
            }
        }
    }
}
