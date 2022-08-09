package org.snpeff.interval;

import org.snpeff.binseq.DnaSequence;
import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Interval for the whole chromosome
 * If a SNP has no 'ChromosomeInterval' => it is outside the chromosome => Invalid
 *
 * @author pcingola
 */
public class Chromosome extends Marker {

    private static final long serialVersionUID = 1636197649250882952L;

    double chromosomeNum;
    DnaSequence sequence = null;
    boolean circular;

    public Chromosome() {
        super();
    }

    public Chromosome(Genome parent, int start, int end, String id) {
        super(null, start, end, false, id); // Parent = null to avoid sanity check (it will always fail for chromosomes)
        this.parent = parent;
        type = EffectType.CHROMOSOME;
        circular = false;
        setChromosomeName(id);
    }

    /**
     * Compare chromosome names
     */
    public static int compare(String chr1, String chr2) {
        // Try to compare numbers
        int chr1Num = number(chr1);
        int chr2Num = number(chr2);
        if (chr1Num > 0 && chr2Num > 0) return chr1Num - chr2Num;
        if (chr1Num > 0 && chr2Num <= 0) return -1;
        if (chr1Num <= 0 && chr2Num > 0) return 1;

        // Numbers did not work, compare strings
        return simpleName(chr1).compareTo(simpleName(chr2));
    }

    /**
     * Convert to chromosome number (return '0' if it cannot be converted)
     */
    public static int number(String chrName) {
        return Gpr.parseIntSafe(ChromosomeSimpleName.get(chrName));
    }

    /**
     * Simplify chromosome name
     */
    public static String simpleName(String chrName) {
        return ChromosomeSimpleName.get(chrName);
    }

    @Override
    public Chromosome cloneShallow() {
        Chromosome clone = (Chromosome) super.cloneShallow();
        clone.chromosomeNum = chromosomeNum;
        clone.circular = circular;
        return clone;
    }

    /**
     * Compare only chromosome's name
     */
    public int compareChromoName(Interval interval) {
        Chromosome i2 = (Chromosome) interval;

        // Both of them are non-numeric: Compare as string
        if ((chromosomeNum == 0) && (i2.chromosomeNum == 0)) return id.compareTo(i2.id);

        // One of them is a number? => the number goes first
        if ((chromosomeNum != 0) && (i2.chromosomeNum == 0)) return -1;
        if ((chromosomeNum == 0) && (i2.chromosomeNum != 0)) return 1;

        // Use numeric comparison
        if (chromosomeNum - i2.chromosomeNum < 0) return -1;
        if (chromosomeNum - i2.chromosomeNum > 0) return 1;
        return 0;
    }

    /**
     * Is this a circular chromosome? See if any exon has evidence of 'circular coordinates'
     */
    public boolean detectCircular() {
        String chr = getChromosomeName();
        int chrLen = size();
        for (Gene gene : getGenome().getGenes()) {
            // Different chromosome? Skip
            if (!gene.getChromosomeName().equalsIgnoreCase(chr)) continue;

            for (Transcript tr : gene) {
                for (Exon exon : tr) {
                    int ssStart = exon.getStart();
                    int ssEnd = exon.getEndClosed() + 1; // String.substring does not include the last character in the interval (so we have to add 1)

                    if ((ssStart >= 0) && (ssEnd <= chrLen)) {
                        // OK: Regular coordinates, nothing to do
                    } else if ((ssStart < 0) && (ssEnd > 0)) {
                        // Negative start coordinates? This is probably a circular genome
                        circular = true;
                        Log.warning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR, "Chromosome '" + chr + "' (len=" + size() + ") has exon with negative start coordinate: Marking as 'circular'. Exon:" + exon);
                        return circular;
                    } else if ((ssStart < 0) && (ssEnd < 0)) {
                        // Negative start coordinates? This is probably a circular genome
                        circular = true;
                        Log.warning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR, "Chromosome '" + chr + "' (len=" + size() + ") has exon with negative coordinates: Marking as 'circular'. Exon:" + exon);
                        return circular;
                    } else if (ssEnd > chrLen) {
                        // Exon ends is after chromosme length
                        circular = true;
                        Log.warning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR, "Chromosome '" + chr + "' (len=" + size() + ") has exon with end coordinate after chromosome end: Marking as 'circular'. Exon:" + exon);
                        return circular;
                    }
                }
            }
        }
        return circular;
    }

    public CodonTable getCodonTable() {
        return CodonTables.getInstance().getTable(getGenome(), getId());
    }

    public DnaSequence getDnaSequence() {
        return sequence;
    }

    public String getSequence() {
        return sequence.toString();
    }

    /**
     * Set sequence for this chromosome
     */
    public void setSequence(String sequenceStr) {
        sequence = new DnaSequence(sequenceStr, true);
        setLength(sequenceStr.length()); // Update chromosome length
    }

    @Override
    public boolean isCircular() {
        return circular;
    }

    public void setCircular(boolean circular) {
        this.circular = circular;
    }

    /**
     * Is this a mitochondrial chromosome?
     * Note: This is a wild guess just by looking at the name
     */
    public boolean isMt() {
        String iduc = id.toUpperCase();
        return iduc.equals("M") //
                || iduc.startsWith("MT") //
                || iduc.contains("MITO") //
                ;
    }

    @Override
    protected boolean isShowWarningIfParentDoesNotInclude() {
        return false;
    }

    /**
     * Parse a line from a serialized file
     */
    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        super.serializeParse(markerSerializer);
        setChromosomeName(id);
        circular = markerSerializer.getNextFieldBoolean();
    }

    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return super.serializeSave(markerSerializer) //
                + "\t" + circular //
                ;
    }

    /**
     * Set chromosome name
     * Note: Removes prefixes (such as 'chr') and parse numeric version.
     * E.g. 'chr2' becomes '2'. Also numeric '2' is assigned to 'chromosomeNum' to facilitate order by number (so that '2' is ordered before '21')
     */
    private void setChromosomeName(String chromo) {
        id = simpleName(chromo);
        chromosomeNum = Gpr.parseIntSafe(id); // Try to parse a numeric string
    }

    public void setLength(int len) {
        setEndClosed(len - 1); // Intervals are zero-based
    }

    @Override
    public String toString() {
        return getChromosomeName() + "\t" + getStart() + "-" + getEndClosed() //
                + " " //
                + type //
                + ((id != null) && (id.length() > 0) ? " '" + id + "'" : "") //
                + (circular ? " [Cicular]" : "") //
                ;
    }

}
