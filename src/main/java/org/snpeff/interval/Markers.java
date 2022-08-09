package org.snpeff.interval;

import org.snpeff.fileIterator.BedFileIterator;
import org.snpeff.fileIterator.Gff3FileIterator;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.io.Serializable;
import java.util.*;

/**
 * A collection of markers
 *
 * @author pcingola
 */
public class Markers implements Serializable, Collection<Marker> {

    private static final long serialVersionUID = 259791388087691277L;
    protected ArrayList<Marker> markers;
    protected String name = "";

    public Markers() {
        markers = new ArrayList<>();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public Markers(Collection otherMarkers) {
        markers = new ArrayList<>();
        addAll(otherMarkers);
    }

    public Markers(Markers otherMarkers) {
        markers = new ArrayList<>();
        addAll(otherMarkers.getMarkers());
    }

    public Markers(String name) {
        this.name = name;
        markers = new ArrayList<>();
    }

    /**
     * Read markers from a file
     * Supported formats: BED, BigBed, VCF, TXT
     */
    public static Markers readMarkers(String fileName) {
        String flLower = fileName.toLowerCase();

        // Remove '.gz' if any
        if (flLower.endsWith(".gz")) flLower = Gpr.removeExt(flLower);

        // Load according to file type
        if (flLower.endsWith(".txt"))
            return new BedFileIterator(fileName, null).loadMarkers(); // TXT is assumed to be "chr \t start \t end"
        else if (flLower.endsWith(".bed")) return new BedFileIterator(fileName).loadMarkers();
        else if (flLower.endsWith(".vcf")) return new VcfFileIterator(fileName).loadMarkers();
        else if (flLower.endsWith(".gff")) return new Gff3FileIterator(fileName).loadMarkers();
            // else if (flLower.endsWith(".bb")) return new BigBedFileIterator(fileName).loadMarkers();
        else throw new RuntimeException("Unrecognized genomig interval file type '" + fileName + "'");
    }

    /**
     * Add an interval to the collection
     */
    @Override
    public boolean add(Marker marker) {
        return markers.add(marker);
    }

    /**
     * Add all intervals
     */
    public Markers add(Markers markersToAdd) {
        markers.addAll(markersToAdd.markers);
        return this;
    }

    /**
     * Add all markers in this collection
     */
    @Override
    public boolean addAll(Collection<? extends Marker> mm) {
        boolean changed = false;
        for (Marker m : mm)
            changed |= markers.add(m);
        return changed;
    }

    @Override
    public void clear() {
        markers.clear();
    }

    @Override
    public boolean contains(Object o) {
        return markers.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return markers.containsAll(c);
    }

    /**
     * Are all intervals equal?
     */
    public boolean equals(Markers intervals) {
        if (intervals == null) return false;
        if (size() != intervals.size()) return false;

        // Sort both collections
        sort(false, false);
        intervals.sort(false, false);

        // Compare all intervals
        Iterator<Marker> it1 = iterator();
        Iterator<Marker> it2 = intervals.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            Interval i1 = it1.next();
            Interval i2 = it2.next();
            if (!i1.equals(i2)) return false;
        }

        return true;
    }

    public Marker get(int i) {
        return markers.get(i);
    }

    public List<Marker> getMarkers() {
        return markers;
    }

    /**
     * Calculate the median point in this set of markers
     */
    public int getMedian() {
        // Add all start & end coordinates
        int i = 0;
        int[] points = new int[2 * size()];
        for (Interval interval : this) {
            points[i++] = interval.getStart();
            points[i++] = interval.getEndClosed();
        }

        // Calculate median by sorting and selecting middle element
        Arrays.sort(points);
        int middle = points.length / 2;
        return points[middle];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Perform the intersection of all overlapping sub-intervals
     * <p>
     * For each marker, calculate all overlapping markers and create a new marker that contains them all.
     * Return a set of those new markers.
     */
    public Markers intersect() {
        Markers intersectOfOverlaps = new Markers();
        IntervalForest forest = new IntervalForest(this);

        HashSet<Marker> done = new HashSet<>();
        for (Marker mi : this) {
            if (!done.contains(mi)) { // No added yet?
                Markers query = forest.query(mi);

                // Get intersect
                Marker intersect = new Marker(mi.getParent(), mi.getStart(), mi.getEndClosed(), mi.isStrandMinus(), "");
                done.add(mi);
                for (Marker m : query) {
                    if (intersect != null) {
                        if ((intersect.getStart() < m.getStart()) || (intersect.getEndClosed() > m.getEndClosed())) {
                            intersect = intersect.intersect(m);
                        }
                    }
                    done.add(m);
                }

                // Add union
                if (intersect != null) intersectOfOverlaps.add(intersect);
            }
        }

        return intersectOfOverlaps;
    }

    /**
     * Intersection between 'marker' and all sub-intervals
     */
    public Markers intersect(Marker marker) {
        Markers result = new Markers();
        for (Marker m : this) {
            var intersect = marker.intersect(m);
            if (intersect != null) result.add(intersect);
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return markers.isEmpty();
    }

    @Override
    public Iterator<Marker> iterator() {
        return markers.iterator();
    }

    public void load(String fileName) {
        load(fileName, null);
    }

    public void load(String fileName, Genome genome) {
        MarkerSerializer markerSerializer = new MarkerSerializer(genome);
        Markers markers = markerSerializer.load(fileName);
        add(markers);
    }

    /**
     * Merge overlapping intervals
     * This is the same as 'union()' method, but the algorithm is more efficient
     */
    public Markers merge() {
        // Intervals sorted by start
        Markers intsSorted = new Markers();
        intsSorted.add(this);
        intsSorted.sort();

        // Merge intervals
        Markers intsMerged = new Markers();
        String tag = "", chromoName = "";
        Chromosome chromo = null;
        int start = -1, end = -1;
        for (Marker i : intsSorted) {

            // Different chromosome? => Re-start
            Chromosome ichromo = i.getChromosome();
            String ichromoName = ichromo.getId();
            if (!chromoName.equals(ichromoName)) {
                // Save current interval (if a any)
                if ((start >= 0) && (end >= 0)) {
                    Marker im = new Marker(chromo, start, end, false, tag);
                    intsMerged.add(im);
                }

                chromoName = ichromoName;
                chromo = ichromo;
                start = end = -1;
                tag = "";
            }

            // Previous interval finished? => add it to list
            if (i.getStart() > end) {
                if ((start >= 0) && (end >= 0)) {
                    if (end < start) { // Sanity check
                        Log.debug("This should never happen!\tstart: " + start + "\tend:" + end);
                        for (Marker m : this)
                            System.err.println("\t" + m);
                    } else {
                        Marker im = new Marker(chromo, start, end, false, tag);
                        intsMerged.add(im);
                    }
                }
                start = end = -1;
                tag = "";
            }

            // Update interval 'start'
            if (start < 0) start = i.getStart();

            // Update 'end'
            end = Math.max(end, i.getEndClosed());

            // Update tag
            if (tag.length() <= 0) tag = i.id;
            else tag += " " + i.id;
        }

        if ((start >= 0) && (end >= 0)) {
            Marker im = new Marker(chromo, start, end, false, tag);
            intsMerged.add(im);
        }

        return intsMerged;
    }

    /**
     * Calculate 'set minus' using one interval
     *
     * @param interval
     * @return
     */
    public Markers minus(Marker interval) {
        Markers ints = new Markers();

        // Add all intervals in 'this'
        for (Marker i : this)
            if (i.intersects(interval)) {
                if ((interval.getStart() <= i.getStart()) && (i.getEndClosed() <= interval.getEndClosed())) {
                    // 'i' is included in 'interval' => Do not add 'i'
                } else if ((interval.getStart() <= i.getStart()) && (interval.getEndClosed() < i.getEndClosed())) {
                    // 'interval' overlaps left part of 'i' => Include right part of 'i'
                    ints.add(new Marker(i.getParent(), interval.getEndClosed() + 1, i.getEndClosed(), i.isStrandMinus(), i.getId()));
                } else if ((i.getStart() < interval.getStart()) && (i.getEndClosed() <= interval.getEndClosed())) {
                    // 'interval' overlaps right part of 'i' => Include left part of 'i'
                    ints.add(new Marker(i.getParent(), i.getStart(), interval.getStart() - 1, i.isStrandMinus(), i.getId()));
                } else if ((i.getStart() < interval.getStart()) && (interval.getEndClosed() < i.getEndClosed())) {
                    // 'interval' overlaps middle of 'i' => Include left and right part of 'i'
                    ints.add(new Marker(i.getParent(), i.getStart(), interval.getStart() - 1, i.isStrandMinus(), i.getId()));
                    ints.add(new Marker(i.getParent(), interval.getEndClosed() + 1, i.getEndClosed(), i.isStrandMinus(), i.getId()));
                } else throw new RuntimeException("Interval intersection not analysed. This should nbever happen!");
            } else ints.add(i); // No intersection => Just add interval

        return ints;
    }

    /**
     * Returns the result of this set minus 'intervals'
     * <p>
     * WARNING: This method should only be used for debugging (or in very small collections) since it is extremely inefficient.
     */
    public Markers minus(Markers intervals) {
        Markers result = new Markers();
        result.add(this);

        // Calculate 'set minus' for all 'intervals'
        for (Marker j : intervals)
            result = result.minus(j);

        return result;
    }

    /**
     * Return a random interval within this collection
     */
    public Interval rand() {
        int idx = (int) (Math.random() * markers.size());
        return markers.get(idx);
    }

    @Override
    public boolean remove(Object o) {
        return markers.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return markers.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return markers.retainAll(c);
    }

    /**
     * Save to a file using a serializer
     */
    public void save(String fileName) {
        // Nothing to save
        if (size() <= 0) return;

        // We must add genome and all chromosomes to the list (otherwise the serializer cannot save all references)
        Genome genome = markers.get(0).getGenome();

        // Add all chromosomes to a set (to make sure they are added only once)
        HashSet<Chromosome> chromos = new HashSet<>();
        for (Marker m : this)
            chromos.add(m.getChromosome());

        // Create a set of all markers to be saved
        Markers markersToSave = new Markers();

        // Add genome
        markersToSave.add(genome);

        // Add chromosomes
        for (Chromosome chr : chromos)
            markersToSave.add(chr);

        // Add markers
        for (Marker m : markers)
            markersToSave.add(m);

        // Save
        MarkerSerializer markerSerializer = new MarkerSerializer(genome);
        markerSerializer.save(fileName, markersToSave);
    }

    /**
     * Save to a file using a serializer
     * Only save one chromosome ('chr')
     * Note: This is used to save only markers related to one
     * chromosome (e.g. when saving GenomicSequences)
     */
    public void save(String fileName, String chr) {
        // Nothing to save
        if (size() <= 0) return;

        // Create a set of all markers to be saved
        Markers markersToSave = new Markers();

        // Add genome
        Genome genome = markers.get(0).getGenome();

        // Add only chromosome 'chr'
        markersToSave.add(genome.getChromosome(chr));

        // Add markers
        for (Marker m : markers)
            markersToSave.add(m);

        // Save
        MarkerSerializer markerSerializer = new MarkerSerializer(genome);
        markerSerializer.doNotSave(genome);
        markerSerializer.save(fileName, markersToSave);
    }

    @Override
    public int size() {
        return markers.size();
    }

    /**
     * Sort intervals
     */
    public Markers sort() {
        return sort(false, false);
    }

    /**
     * Sort intervals
     *
     * @param byEnd   : If true, sort by end. Otherwise sort by start
     * @param reverse : Reverse order
     */
    public Markers sort(boolean byEnd, boolean reverse) {
        if (byEnd) Collections.sort(markers, new IntervalComparatorByEnd(reverse));
        else Collections.sort(markers, new IntervalComparatorByStart(reverse));
        return this;
    }

    @Override
    public Marker[] toArray() {
        Marker[] markers = new Marker[size()];

        int i = 0;
        for (Marker m : this)
            markers[i++] = m;

        return markers;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return markers.toArray(a);
    }

    @Override
    public String toString() {
        int num = 1;
        StringBuilder sb = new StringBuilder();
        for (Marker i : this)
            sb.append("\t" + (num++) + ":" //
                    + "\t" + i.getChromosomeName() //
                    + "\t" + i.getStart() //
                    + "\t" + i.getEndClosed() //
                    + "\t" + i.getClass().getSimpleName() //
                    + "\t" + i.getId() //
                    + "\n");

        return sb.toString();
    }

    /**
     * Show all intervals as an ASCII art
     */
    public String toStringAsciiArt(int maxLen) {
        StringBuilder sb = new StringBuilder();

        // Separator
        String sep = "";
        for (int i = 0; i < maxLen; i++)
            sep += "=";

        // Show intervals
        String ch = "";
        for (Marker i : this) {
            if (!i.getChromosomeName().equals(ch)) {
                sb.append("|" + sep + "|\n");
                ch = i.getChromosomeName();
            }

            sb.append("|" + i.toStringAsciiArt(maxLen) + "|\t" + i.getChromosomeName() + ": [" + i.getStart() + " - " + i.getEndClosed() + "] ");
            if ((i.id != null) && (i.id.length() > 0)) sb.append("'" + i.id + "'"); // Show tag (if any)
            sb.append("\n");
        }
        sb.append("|" + sep + "|\n");

        return sb.toString();
    }

    public String toStringTxt() {
        StringBuilder sb = new StringBuilder();
        for (Marker i : this)
            sb.append(i.getChromosomeName() + "\t" + i.getStart() + "\t" + i.getEndClosed() + "\t" + i.getId() + "\n");
        return sb.toString();
    }

    /**
     * Perform the union of all overlapping intervals
     * <p>
     * For each marker, calculate all overlapping markers and create a new marker that contains them all.
     * Return a set of those new markers.
     */
    public Markers union() {
        Markers unionOfOverlaps = new Markers();

        IntervalForest forest = new IntervalForest(this);
        forest.build();

        HashSet<Marker> done = new HashSet<>();
        for (Marker mi : this) {
            if (!done.contains(mi)) { // No added yet?
                Markers query = forest.query(mi);

                // Get union
                // Marker union = new Marker(mi.getParent(), mi.getStart(), mi.getEnd(), mi.isStrandMinus(), "");
                Marker union = mi.clone();
                done.add(mi);
                for (Marker m : query) {
                    if ((union != null) && (union.getStart() > m.getStart()) || (union.getEndClosed() < m.getEndClosed()))
                        union = union.union(m);
                    done.add(m);
                }

                // Add union
                if (union != null) unionOfOverlaps.add(union);
            }
        }

        return unionOfOverlaps;
    }

    /**
     * Remove duplicated markers
     *
     * @return this object
     */
    public Markers unique() {
        HashSet<Marker> set = new HashSet<>();
        ArrayList<Marker> markersUnique = new ArrayList<>();
        for (Marker m : markers) {
            if (set.add(m)) markersUnique.add(m);
        }
        markers = markersUnique;
        return this;
    }

}
