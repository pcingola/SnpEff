package org.snpeff.snpEffect.testCases.unity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.tree.IntervalTree;
import org.snpeff.interval.tree.Itree;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test case for interval tree structure
 */
public class TestCasesIntervalTree {

    public static int MAX_SMALL_MARKER_SIZE = 5;
    public static int NUM_LARGE_INTERVALS = 100;
    public static int NUM_SMALL_INTERVALS = 10 * NUM_LARGE_INTERVALS;
    public static int CHR_SIZE = 10 * 1000;

    protected boolean debug = false;
    protected boolean verbose = false || debug;
    protected Random rand;
    protected Genome genome;
    protected Chromosome chromosome;
    protected Markers markers;

    public TestCasesIntervalTree() {
        super();
    }

    @BeforeEach
    public void before() {
        int randSeed = 20151117;
        rand = new Random(randSeed);
        genome = new Genome();
        chromosome = new Chromosome(genome, 0, CHR_SIZE, "1");
        markers = createRandomMarkers();
    }

    /**
     * Perform a query using 'naive' lookup and interval forest.
     * Compare results and throw an exception if any difference exists
     */
    protected int compareQuery(Marker m, Itree intTree) {
        if (debug) Log.debug("Query: " + m);
        Markers resultsNaive = queryNaive(m);
        Markers resultsIntForest = intTree.query(m);

        if (debug) {
            Log.debug("Query: " + m + "\n\tResults: " + resultsIntForest.size());
            for (Marker res : resultsIntForest)
                System.err.println("\t" + res + (res.intersects(m) ? "" : "\tERROR!"));
        }

        // Compare all results
        String resultsNaiveStr = resultsNaive.sort().toString();
        String resultsIntForestStr = resultsIntForest.sort().toString();
        assertEquals(resultsNaiveStr, resultsIntForestStr, "Results differ for query '" + m + "'.");

        return resultsNaive.size();
    }

    protected Markers createRandomLargeMarkers(Chromosome chr, int num) {
        Markers markers = new Markers();

        for (int i = 0; i < num; i++) {
            int start = rand.nextInt(chr.size());
            int end = rand.nextInt(chr.size());

            if (end < start) {
                int tmp = end;
                end = start;
                start = tmp;
            }

            Marker m = new Marker(chr, start, end, false, "ID_" + i);
            markers.add(m);
        }

        return markers;
    }

    protected Markers createRandomMarkers() {
        Markers markers = new Markers();
        markers.addAll(createRandomLargeMarkers(chromosome, NUM_LARGE_INTERVALS));
        markers.addAll(createRandomSmallMarkers(chromosome, NUM_SMALL_INTERVALS));
        return markers;
    }

    protected Markers createRandomSmallMarkers(Chromosome chr, int num) {
        Markers markers = new Markers();

        for (int i = 0; i < num; i++) {
            int start = rand.nextInt(chr.size());
            int end = start + rand.nextInt(MAX_SMALL_MARKER_SIZE) + 1;

            if (end < start) {
                int tmp = end;
                end = start;
                start = tmp;
            }

            Marker m = new Marker(chr, start, end, false, "ID_" + i);
            markers.add(m);
        }

        return markers;
    }

    protected Itree newItree(Markers markers) {
        return new IntervalTree(markers);
    }

    /**
     * Naively find all intervals intersecting 'marker'
     */
    protected Markers queryNaive(Marker query) {
        Markers results = new Markers();

        // For each marker, fid if intersects query
        for (Marker m : markers)
            if (query.intersects(m)) results.add(m);

        return results;
    }

    /**
     * Test small intervals
     */
    @Test
    public void test_01() {
        Log.debug("Test");

        Itree intTree = newItree(markers);
        intTree.build();

        Markers queries = createRandomSmallMarkers(chromosome, 100000);

        int i = 0;
        int totalResults = 0;
        for (Marker m : queries) {
            totalResults += compareQuery(m, intTree);
            Gpr.showMark(i++, 100);
        }

        assertTrue(totalResults > 0, "Not a signle result found in all queries!");
        System.err.println("");
    }

    /**
     * Test large intervals
     */
    @Test
    public void test_02() {
        Log.debug("Test");

        Itree intForest = newItree(markers);
        intForest.build();

        Markers queries = createRandomLargeMarkers(chromosome, 10000);
        int i = 0;
        int totalResults = 0;
        for (Marker m : queries) {
            totalResults += compareQuery(m, intForest);
            Gpr.showMark(i++, 10);
        }

        assertTrue(totalResults > 0, "Not a signle result found in all queries!");
        System.err.println("");

    }
}
