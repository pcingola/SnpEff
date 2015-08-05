package ca.mcgill.mcb.pcingola.interval;

import java.io.Serializable;

/**
 * A genomic interval.
 * Note: Intervals are assumed to be zero-based and inclusive
 *       i.e. an interval including the first base up to base X would
 *       be [0,X] NOT [1,X]
 *
 * @author pcingola
 */
public class Interval implements Comparable<Interval>, Serializable, Cloneable {

	private static final long serialVersionUID = -3547434510230920403L;

	protected int start, end;
	protected boolean strandMinus;
	protected String id = ""; // Interval's ID (e.g. gene name, transcript ID)
	protected String chromosomeNameOri; // Original chromosome name (e.g. literal form a file)
	protected Interval parent;

	protected Interval() {
		start = -1;
		end = -1;
		id = null;
		strandMinus = false;
		parent = null;
	}

	public Interval(Interval parent, int start, int end, boolean strandMinus, String id) {
		// Sanity checks
		if (end < start) throw new RuntimeException("Interval error: end before start." //
				+ "\n\tClass        : " + getClass().getSimpleName() //
				+ "\n\tStart        : " + start //
				+ "\n\tEnd          : " + end //
				+ "\n\tID           : " + id //
				+ "\n\tParent class : " + (parent != null ? parent.getClass().getSimpleName() : "") //
				+ "\n\tParent       : " + parent //
				);

		this.start = start;
		this.end = end;
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
		if ((parent != null) && (parent instanceof Marker)) return ((Marker) parent).findParent(clazz);
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

	public Interval getParent() {
		return parent;
	}

	public int getStart() {
		return start;
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
	 * Return true if this intersects '[iStart, iEnd]'
	 */
	public boolean intersects(int iStart, int iEnd) {
		return (iEnd >= start) && (iStart <= end);
	}

	/**
	 * Return true if this intersects 'interval'
	 */
	public boolean intersects(Interval interval) {
		return (interval.getEnd() >= start) && (interval.getStart() <= end);
	}

	/**
	 * @return  true if this interval contains point (inclusive)
	 */
	public boolean intersects(long point) {
		return (start <= point) && (point <= end);
	}

	/**
	 * Do the intervals intersect?
	 * @return  return true if this intersects 'interval'
	 */
	public boolean intersects(Marker interval) {
		if (!interval.getChromosomeName().equals(getChromosomeName())) return false;
		return (interval.getEnd() >= start) && (interval.getStart() <= end);
	}

	/**
	 * How much do intervals intersect?
	 * @return  number of bases these intervals intersect
	 */
	public int intersectSize(Marker interval) {
		if (!interval.getChromosomeName().equals(getChromosomeName())) return 0;

		int start = Math.max(this.start, interval.getStart());
		int end = Math.min(this.end, interval.getEnd());

		if (end < start) return 0;
		return (end - start) + 1;
	}

	public boolean isSameChromo(Marker interval) {
		return interval.getChromosomeName().equals(getChromosomeName());
	}

	public boolean isStrandMinus() {
		return strandMinus;
	}

	public boolean isStrandPlus() {
		return !strandMinus;
	}

	public boolean isValid() {
		return start <= end;
	}

	public void setChromosomeNameOri(String chromosomeNameOri) {
		this.chromosomeNameOri = chromosomeNameOri;
	}

	public void setEnd(int end) {
		if (end < start) throw new RuntimeException("Trying to set end before start:\n\tstart: " + start + "\n\tend : " + end + "\n\t" + this);
		if (end < 0) throw new RuntimeException("Trying to set negative 'end' coordinate:\n\t: " + start + "\n\tend : " + end + "\n\t" + this);
		this.end = end;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParent(Interval parent) {
		this.parent = parent;
	}

	public void setStart(int start) {
		if (start < 0) throw new RuntimeException("Trying to set negative 'start' coordinate:\n\t: " + start + "\n\tend : " + end + "\n\t" + this);
		this.start = start;
	}

	public void setStrandMinus(boolean strand) {
		strandMinus = strand;
	}

	public void shiftCoordinates(int shift) {
		start += shift;
		end += shift;
	}

	public int size() {
		return end - start + 1;
	}

	@Override
	public String toString() {
		return start + "-" + end //
				+ ((id != null) && (id.length() > 0) ? " '" + id + "'" : "");
	}

	/**
	 * Show it as an ASCII art
	 */
	public String toStringAsciiArt(int maxLen) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < maxLen; i++) {
			if ((i >= start) && (i <= end)) sb.append('-');
			else sb.append(' ');
		}

		return sb.toString();
	}

}
