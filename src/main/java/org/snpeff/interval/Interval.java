package org.snpeff.interval;

import java.io.Serializable;

/**
 * A genomic interval.
 * <p>
 * Intervals are assumed to be zero-based and "half-open"
 * i.e. an interval includes the 'start' position and DOES NOT include the 'end' position
 * <p>
 * e.g.: an interval including the first base, up to base (and including) X would be [0, X+1)
 * <p>
 * Zero-length intervals: A zero length interval can be defined using start=end
 * e.g. [10, 10), since base 10 is not included, the interval is zero length
 * <p>
 * IMPORTANT: This changed in version 6.0. In previous version we used closed intervals
 *
 * @author pcingola
 */
public class Interval implements Comparable<Interval>, Serializable, Cloneable {

    private static final long serialVersionUID = -3547434510230920403L;
    protected boolean strandMinus;
    protected String id = ""; // Interval's ID (e.g. gene name, transcript ID)
    protected String chromosomeNameOri; // Original chromosome name (e.g. literal form a file)
    protected Interval parent;
    private int start, end; // This is a zero-based, half open interval

    protected Interval() {
        start = -1;
        end = -1;
        id = null;
        strandMinus = false;
        parent = null;
    }

    public Interval(Interval parent, int start, int end, boolean strandMinus, String id) {
        this.start = start;
        setEndClosed(end);
        this.id = id;
        this.strandMinus = strandMinus;
        this.parent = parent;
    }

    @Override
    public Interval clone() {
        try {
            return (Interval) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compare by start and end
     */
    @Override
    public int compareTo(Interval i2) {
        // Start
        if (start > i2.start) return 1;
        if (start < i2.start) return -1;

        // End
        if (end > i2.end) return 1;
        if (end < i2.end) return -1;

        return 0;
    }

    public boolean equals(Interval interval) {
        return compareTo(interval) == 0;
    }

    /**
     * Go up (parent) until we find an instance of 'clazz'
     */
    @SuppressWarnings("rawtypes")
    public Interval findParent(Class clazz) {
        if (this.getClass().equals(clazz)) return this;
        if ((parent != null) && (parent instanceof Marker)) return parent.findParent(clazz);
        return null;
    }

    public Chromosome getChromosome() {
        return (Chromosome) findParent(Chromosome.class);
    }

    /**
     * Find chromosome name
     */
    public String getChromosomeName() {
        Chromosome chromo = getChromosome();
        if (chromo != null) return chromo.getId();
        return "";
    }

    public String getChromosomeNameOri() {
        return chromosomeNameOri;
    }

    public void setChromosomeNameOri(String chromosomeNameOri) {
        this.chromosomeNameOri = chromosomeNameOri;
    }

    /**
     * Find chromosome and return it's number
     *
     * @return Chromosome number if found, -1 otherwise
     */
    public double getChromosomeNum() {
        Chromosome chromo = (Chromosome) findParent(Chromosome.class);
        if (chromo != null) return chromo.chromosomeNum;
        return -1;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    /**
     * Get 'end' coordinate, as if it was a closed interval
     * We use half-open coordinates (i.e. do not include 'end' coordiante), this method returns
     * the 'end' coordinate as if we were using closed coordinates.
     * <p>
     * IMPORTANT: This is used for refactoring the old version of the code that used closed coordinates
     */
    public int getEndClosed() {
        return end - 1;
    }

    /**
     * Set 'end' coordinate, as if it was a closed interval
     * We use half-open coordinates (i.e. do not include 'end' coordiante), this method sets
     * the 'end' coordinate as if we were using closed coordinates.
     * <p>
     * IMPORTANT: This is used for refactoring the old version of the code that used closed coordinates
     */
    public void setEndClosed(int end) {
        if (end < start)
            throw new RuntimeException("Trying to set end before start:\n\tstart: " + start + "\n\tend : " + end + "\n\t" + this);
        this.end = end + 1;
    }

    /**
     * Find genome
     */
    public Genome getGenome() {
        return (Genome) findParent(Genome.class);
    }

    /**
     * Find genome name
     */
    public String getGenomeName() {
        Genome genome = (Genome) findParent(Genome.class);
        if (genome != null) return genome.getId();
        return "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Interval getParent() {
        return parent;
    }

    public void setParent(Interval parent) {
        this.parent = parent;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getStrand() {
        return strandMinus ? "-" : "+";
    }

    @Override
    public int hashCode() {
        int hashCode = getChromosomeName().hashCode();
        hashCode = hashCode * 31 + start;
        hashCode = hashCode * 31 + end;
        hashCode = hashCode * 31 + (strandMinus ? -1 : 1);
        if (id != null) hashCode = hashCode * 31 + id.hashCode();
        return hashCode;
    }

    /**
     * Return true if this intersects '[iStart, iEnd)'
     * <p>
     * Examples:
     * These intervals intersect:
     * -----------[>>>>>>>>>>>>)----------
     * -------------------[>>>>>>>>)------
     * <p>
     * These intervals do not intersect:
     * ---[>>>>>>>>>>>)-------------------
     * -----------------[>>>>>>>>>>)------
     * <p>
     * These intervals do not intersect:
     * -------[>>>>>>>>>>)---------------
     * ------------------[>>>>>>>>>>>)--
     */
    public boolean intersects(int iStart, int iEnd) {
        return (iEnd > start) && (iStart < end);
    }

    /**
     * Return true if this intersects 'interval'
     */
    public boolean intersects(Interval interval) {
        return interval.getChromosomeName().equals(getChromosomeName()) && intersects(interval.getStart(), interval.getEnd());
    }

    /**
     * Return true if this interval contains point (inclusive)
     * <p>
     * Examples:
     * This point intersects:
     * ----------------X------------------
     * ----------[>>>>>>>>>>)-------------
     * This point intersects:
     * ----------X------------------------
     * ----------[>>>>>>>>>>)-------------
     * <p>
     * This point does NOT intersect:
     * ---------X-------------------------
     * ----------[>>>>>>>>>>)-------------
     * <p>
     * This point does NOT intersect:
     * ---------------------X-------------
     * ----------[>>>>>>>>>>)-------------
     */
    public boolean intersects(long point) {
        return (start <= point) && (point < end);
    }

    /**
     * Is 'interval' completely included in 'this'?
     *
     * @return return true if 'this' includes 'interval'
     */
    public boolean includes(Interval interval) {
        return interval.getChromosomeName().equals(getChromosomeName()) && (start <= interval.start) && (interval.end <= end);
    }

    /**
     * How much do intervals intersect?
     *
     * @return number of bases these intervals intersect
     */
    public int intersectSize(Marker interval) {
        if (!interval.getChromosomeName().equals(getChromosomeName())) return 0;

        int start = Math.max(getStart(), interval.getStart());
        int end = Math.min(getEndClosed(), interval.getEndClosed());

        if (end <= start) return 0;
        return end - start + 1;
    }

    /**
     * Is this interval part of a circular chromosome and it spans
     * the 'chromosome zero / chromosome end' line?
     */
    public boolean isCircular() {
        Chromosome chr = getChromosome();
        return start < 0 // Negative coordinates?
                || (start > end) // Start before end?
                || (end > chr.getEndClosed()) // Ends after chromosome end?
                ;
    }

    public boolean isSameChromo(Marker interval) {
        return interval.getChromosomeName().equals(getChromosomeName());
    }

    public boolean isStrandMinus() {
        return strandMinus;
    }

    public void setStrandMinus(boolean strand) {
        strandMinus = strand;
    }

    public boolean isStrandPlus() {
        return !strandMinus;
    }

    public boolean isValid() {
        return start <= end;
    }

    public void shiftCoordinates(int shift) {
        start += shift;
        end += shift;
    }

    public void shiftStart(int shift) {
        start += shift;
    }

    public void shiftEnd(int shift) {
        end += shift;
    }

    public int size() {
        return end - start;
    }

    /**
     * To string as a simple "chr:start-end" format
     */
    public String toStr() {
        return getClass().getSimpleName() //
                + "_" + getChromosomeName() //
                + ":" + (start + 1) //
                + "-" + (getEndClosed() + 1) //
                ;
    }

    @Override
    public String toString() {
        return start + "-" + getEndClosed() //
                + ((id != null) && (id.length() > 0) ? " '" + id + "'" : "");
    }

    /**
     * Show it as an ASCII art
     */
    public String toStringAsciiArt(int maxLen) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < maxLen; i++) {
            if ((i >= start) && (i < end)) sb.append('-');
            else sb.append(' ');
        }

        return sb.toString();
    }

    /**
     * To string as a simple "chr:start-end" format
     */
    public String toStrPos() {
        return getChromosomeName() + ":" + (start + 1) + "-" + (getEndClosed() + 1);
    }

}
