package org.snpeff.interval;

import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.serializer.TxtSerializable;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An interval intended as a mark (i.e. "genomic annotation")
 * It is essentially an interval with an 'EffectType'
 *
 * @author pcingola
 */
public class Marker extends Interval implements TxtSerializable {

    private static final long serialVersionUID = 7878886900660027549L;
    protected EffectType type = EffectType.NONE;

    protected Marker() {
        super();
        type = EffectType.NONE;
    }

    public Marker(Marker parent, int start, int end) {
        this(parent, start, end, false, "");
    }

    public Marker(Marker parent, int start, int end, boolean strandMinus, String id) {
        super(parent, start, end, strandMinus, id);

        // Adjust parent if child is not included?
        if ((parent != null) && !parent.includes(this)) {
            String err = "";
            if (isShowWarningIfParentDoesNotInclude())
                err = "WARNING: " + this.getClass().getSimpleName() + " is not included in parent " + parent.getClass().getSimpleName() + ". " //
                        + "\t" + this.getClass().getSimpleName() + " '" + getId() + "'  [ " + getStart() + " , " + getEndClosed() + " ]" //
                        + "\t" + parent.getClass().getSimpleName() + " '" + parent.getId() + "' [ " + parent.getStart() + " , " + parent.getEndClosed() + " ]";

            // Adjust parent?
            if (isAdjustIfParentDoesNotInclude(parent)) {
                parent.adjust(this);
                if (isShowWarningIfParentDoesNotInclude())
                    err += "\t=> Adjusting " + parent.getClass().getSimpleName() + " '" + parent.getId() + "' to [ " + parent.getStart() + " , " + parent.getEndClosed() + " ]";
            }

            // Show an error?
            if (isShowWarningIfParentDoesNotInclude()) System.err.println(err);
        }
    }

    /**
     * Adjust [start,end] to include child
     */
    protected void adjust(Marker child) {
        setStart(Math.min(getStart(), child.getStart()));
        setEndClosed(Math.max(getEndClosed(), child.getEndClosed()));
    }

    /**
     * Apply a variant to a marker.
     * <p>
     * Calculate the result of a marker, such that
     * newMarker = marker.apply( variant )
     * variant = Diff( newMarker , marker ) // Differences in sequence
     * <p>
     * Note: This method may return:
     * - The same marker (shallow clone) when genetic coordinates remain unchanged
     * - 'null' if the whole marker is removed by the variant (e.g. a deletion spanning the whole marker)
     * <p>
     *
     * @return The marker result after applying variant
     */
    public Marker apply(Variant variant) {
        if (!shouldApply(variant)) return this;

        Marker newMarker = null;
        switch (variant.getVariantType()) {
            case SNP:
                newMarker = applySnp(variant);
                break;

            case MNP:
                newMarker = applyMnp(variant);
                break;

            case INS:
                newMarker = applyIns(variant);
                break;

            case DEL:
                newMarker = applyDel(variant);
                break;

            case DUP:
                newMarker = applyDup(variant);
                break;

            case MIXED:
                newMarker = applyMixed(variant);
                break;

            default:
                // We are not ready for mixed changes
                throw new RuntimeException("Variant type not supported: " + variant.getVariantType() + "\n\t" + variant);
        }

        // Always return a copy of the marker (if the variant is applied)
        if (newMarker == this) {
            Log.debug("Clone shallow while applying. This should not happen!");
            return cloneShallow();
        }

        return newMarker;
    }


    /**
     * Apply a Variant to a marker. Variant is a deletion
     */
    protected Marker applyDel(Variant variant) {
        Marker m = cloneShallow();

        if (variant.getEndClosed() < m.getStart()) {
            // Deletion before start: Adjust coordinates
            int lenChange = variant.lengthChange();
            m.shiftCoordinates(lenChange);
        } else if (variant.includes(m)) {
            // Deletion completely includes this marker => The whole marker deleted
            return null;
        } else if (m.includes(variant)) {
            // This marker completely includes the deletion, but deletion does not include
            // marker. Marker is shortened (i.e. only 'end' coordinate needs to be updated)
            m.shiftEnd(variant.lengthChange());
        } else {
            // Variant is partially included in this marker.
            // This is treated as three different type of deletions:
            //		1- One after the marker
            //		2- One inside the marker
            //		3- One before the marker
            // Note that type 1 and 3 cannot exists at the same time, otherwise the
            // deletion would fully include the marker (previous case)

            // Part 1: Deletion after the marker
            if (m.getEndClosed() < variant.getEndClosed()) {
                // Actually this does not affect the coordinates, so we don't care about this part
            }

            // Part 2: Deletion matching the marker (intersection)
            int istart = Math.max(variant.getStart(), m.getStart());
            int iend = Math.min(variant.getEndClosed(), m.getEndClosed());
            if (iend < istart) throw new RuntimeException("This should never happen!"); // Sanity check
            m.shiftEnd(-(iend - istart + 1)); // Update end coordinate

            // Part 3: Deletion before the marker
            if (variant.getStart() < m.getStart()) {
                // Update coordinates shifting the marker to the left
                int delta = m.getStart() - variant.getStart();
                m.shiftCoordinates(-delta);
            }
        }

        return m;
    }

    /**
     * Apply a Variant to a marker. Variant is a duplication
     */
    protected Marker applyDup(Variant variant) {
        Marker m = cloneShallow();

        if (variant.getEndClosed() < m.getStart()) {
            // Duplication before marker start? => Adjust both coordinates
            int lenChange = variant.lengthChange();
            m.shiftCoordinates(lenChange);
        } else if (variant.includes(m)) {
            // Duplication includes whole marker? => Adjust both coordinates
            int lenChange = variant.lengthChange();
            m.shiftCoordinates(lenChange);
        } else if (m.includes(variant)) {
            // Duplication included in marker? => Adjust end coordinate
            m.shiftEnd(variant.lengthChange());
        } else if (variant.intersects(m)) {
            // Duplication includes part of marker? => Adjust end
            m.shiftEnd(variant.intersect(m).size());
        } else {
            // Duplication after end, no effect on marker coordinates
        }

        return m;
    }

    /**
     * Apply a Variant to a marker. Variant is an insertion
     */
    protected Marker applyIns(Variant variant) {
        Marker m = cloneShallow();

        if (variant.getStart() < m.getStart()) {
            // Insertion point before marker start? => Adjust both coordinates
            int lenChange = variant.lengthChange();
            m.shiftCoordinates(lenChange);
        } else if (variant.getStart() <= m.getEndClosed()) {
            // Insertion point after start, but before end? => Adjust end coordinate
            m.shiftEnd(variant.lengthChange());
        } else {
            // Insertion point after end, no effect on marker coordinates
        }

        return m;
    }

    /**
     * Apply a mixed variant
     * Note: MIXED variant is interpreted as "MNP + InDel"
     */
    protected Marker applyMixed(Variant variant) {
        // Decompose variant into simple variants
        Variant[] variants = variant.decompose();

        // Apply each basic variant progressively
        Marker m = this;
        for (Variant var : variants) {
            m = m.apply(var);
            if (m == null) return null;
        }

        return m;
    }

    protected Marker applyMnp(Variant variant) {
        // Variant does not change length.
        // No effect when applying, because all coordinates remain the same.
        return cloneShallow();
    }

    protected Marker applySnp(Variant variant) {
        // Variant does not change length.
        // No effect when applying, because all coordinates remain the same.
        return cloneShallow();
    }


    @Override
    public Marker clone() {
        return (Marker) super.clone();
    }

    /**
     * Perform a shallow clone
     */
    @SuppressWarnings("rawtypes")
    public Marker cloneShallow() {
        try {
            // Create new object
            Constructor ctor = this.getClass().getConstructor();
            Marker clone = (Marker) ctor.newInstance();

            // Copy fields
            clone.chromosomeNameOri = chromosomeNameOri;
            clone.setEndClosed(getEndClosed());
            clone.id = id;
            clone.parent = parent;
            clone.setStart(getStart());
            clone.strandMinus = strandMinus;
            clone.type = type;

            return clone;
        } catch (Exception e) {
            throw new RuntimeException("Error performing shallow clone: ", e);
        }
    }

    /**
     * Get a suitable codon table
     */
    public CodonTable codonTable() {
        return CodonTables.getInstance().getTable(getGenome(), getChromosomeName());
    }

    /**
     * Compare by start and end
     */
    @Override
    public int compareTo(Interval i2) {
        int comp = compareToPos(i2);
        if (comp != 0) return comp;

        // Compare by ID
        if ((id == null) && (i2.getId() == null)) return 0;
        if ((id != null) && (i2.getId() == null)) return -1;
        if ((id == null) && (i2.getId() != null)) return 1;
        return id.compareTo(i2.getId());
    }

    /**
     * Compare genomic coordinates
     */
    public int compareToPos(Interval i2) {
        // Compare chromosome names
        Marker m2 = (Marker) i2;

        Chromosome chr1 = getChromosome();
        Chromosome chr2 = m2.getChromosome();

        if ((chr1 != null) && (chr2 != null)) {
            // Non-null: Compare chromosomes
            int compChromo = chr1.compareChromoName(chr2);
            if (compChromo != 0) return compChromo;
        } else if ((chr1 == null) && (chr2 != null)) return 1; // One chromosome is null
        else if ((chr1 != null) && (chr2 == null)) return -1;

        // Compare by start position
        if (getStart() > i2.getStart()) return 1;
        if (getStart() < i2.getStart()) return -1;

        // Compare by end position
        if (getEnd() > i2.getEnd()) return 1;
        if (getEnd() < i2.getEnd()) return -1;

        return 0;
    }

    /**
     * How far apart are these intervals?
     *
     * @return Distance or -1 if they are not comparable (i.e. different chromosomes)
     */
    public int distance(Marker interval) {
        if (!interval.getChromosomeName().equals(getChromosomeName())) return -1;

        if (intersects(interval)) return 0;

        if (getStart() > interval.getEndClosed()) return getStart() - interval.getEndClosed();
        if (interval.getStart() > getEndClosed()) return interval.getStart() - getEndClosed();

        throw new RuntimeException("This should never happen!");
    }

    /**
     * Distance from the beginning/end of a list of intervals, until this SNP
     * It count the number of bases in 'markers'
     */
    public int distanceBases(List<? extends Marker> markers, boolean fromEnd) {

        // Create a new list of sorted intervals
        ArrayList<Marker> markersSort = new ArrayList<>();
        markersSort.addAll(markers);
        if (fromEnd) Collections.sort(markersSort, new IntervalComparatorByEnd(true));
        else Collections.sort(markersSort, new IntervalComparatorByStart());

        // Calculate distance
        int len = 0, latest = -1;
        for (Marker m : markersSort) {

            // Initialize
            if (latest < 0) {
                if (fromEnd) latest = m.getEndClosed() + 1;
                else latest = m.getStart() - 1;
            }

            if (fromEnd) {
                if (intersects(m)) return len + (m.getEndClosed() - getStart());
                else if (getStart() > m.getEndClosed()) return len - 1 + (latest - getStart());

                latest = m.getStart();
            } else {
                if (intersects(m)) return len + (getStart() - m.getStart());
                else if (getStart() < m.getStart()) return len - 1 + (getStart() - latest);

                latest = m.getEndClosed();
            }

            len += m.size();
        }

        if (fromEnd) return len - 1 + (latest - getStart());
        return len - 1 + (getStart() - latest);
    }

    @Override
    public Marker getParent() {
        return (Marker) parent;
    }

    public EffectType getType() {
        return type;
    }

    public String idChain() {
        return idChain(";", ":", true);
    }

    public String idChain(String separatorBetween, String separatorWithin, boolean useGeneId) {
        return idChain(separatorBetween, separatorWithin, useGeneId, null);
    }

    /**
     * A list of all IDs and parent IDs until chromosome
     */
    public String idChain(String separatorBetween, String separatorWithin, boolean useGeneId, VariantEffect varEff) {
        StringBuilder sb = new StringBuilder();

        for (Marker m = this; (m != null) && !(m instanceof Chromosome) && !(m instanceof Genome); m = m.getParent()) {
            if (sb.length() > 0) sb.append(separatorBetween);

            switch (m.getType()) {
                case EXON:
                    Transcript tr = (Transcript) m.getParent();
                    Exon ex = (Exon) m;
                    sb.append(m.getClass().getSimpleName());
                    sb.append(separatorWithin + ex.getRank());
                    sb.append(separatorWithin + tr.numChilds());
                    sb.append(separatorWithin + ex.getSpliceType());
                    break;

                case INTRON:
                    Intron intron = (Intron) m;
                    tr = (Transcript) m.getParent();
                    sb.append(m.getClass().getSimpleName());
                    sb.append(separatorWithin + intron.getRank());
                    sb.append(separatorWithin + (tr.numChilds() - 1));
                    sb.append(separatorWithin + intron.getSpliceType());
                    break;

                case GENE:
                    Gene g = (Gene) m;
                    sb.append(m.getClass().getSimpleName());
                    sb.append(separatorWithin + (useGeneId ? m.getId() : g.getGeneName()));
                    sb.append(separatorWithin + g.getBioType());
                    break;

                case TRANSCRIPT:
                    tr = (Transcript) m;
                    sb.append(m.getClass().getSimpleName());
                    sb.append(separatorWithin + m.getId());
                    if (tr.getBioType() != null) sb.append(separatorWithin + tr.getBioType());
                    break;

                case DOWNSTREAM:
                    sb.append(m.getClass().getSimpleName());
                    if ((varEff != null) && (varEff.getVariant() != null)) {
                        Downstream downstream = (Downstream) m;
                        sb.append(separatorWithin + downstream.distanceToTr(varEff.getVariant()));
                    }
                    break;

                case UPSTREAM:
                    sb.append(m.getClass().getSimpleName());
                    if ((varEff != null) && (varEff.getVariant() != null)) {
                        Upstream upstream = (Upstream) m;
                        sb.append(separatorWithin + upstream.distanceToTr(varEff.getVariant()));
                    }
                    break;

                case CHROMOSOME:
                case INTERGENIC:
                    sb.append(m.getClass().getSimpleName());
                    sb.append(separatorWithin + m.getId());
                    break;

                default:
                    break;
            }
        }

        // Empty? Add type + ID
        if (sb.length() <= 0) sb.append(this.getClass().getSimpleName() + separatorWithin + getId());

        return sb.toString();
    }


    /**
     * Intersect of two markers
     *
     * @return A new marker which is the intersect of the two
     */
    public Marker intersect(Marker marker) {
        if (!getChromosomeName().equals(marker.getChromosomeName())) return null;

        int istart = Math.max(getStart(), marker.getStart());
        int iend = Math.min(getEndClosed(), marker.getEndClosed());
        if (iend < istart) return null;
        return new Marker(getParent(), istart, iend, strandMinus, "");
    }

    /**
     * Adjust parent if it does not include child?
     */
    protected boolean isAdjustIfParentDoesNotInclude(Marker parent) {
        return false;
    }

    /**
     * Deferred analysis markers must be analyzed after 'standard' ones because their impact depends on other results
     * For instance, a NextProt marker's impact would be different if the variant is synonymous or non-synonymous
     */
    public boolean isDeferredAnalysis() {
        return false;
    }

    /**
     * Show an error if parent does not include child?
     */
    protected boolean isShowWarningIfParentDoesNotInclude() {
        return false;
    }

    /**
     * Return the difference between two markers
     *
     * @param interval
     * @return A set of 'markers'. Note that the result can have zero, one or two markers
     */
    public Markers minus(Marker interval) {
        Markers ints = new Markers();
        if (intersects(interval)) {
            if ((interval.getStart() <= getStart()) && (getEndClosed() <= interval.getEndClosed())) {
                // 'this' is included in 'interval' => Nothing left
            } else if ((interval.getStart() <= getStart()) && (interval.getEndClosed() < getEndClosed())) {
                // 'interval' overlaps left part of 'this' => Include right part of 'this'
                ints.add(new Marker(getParent(), interval.getEndClosed() + 1, getEndClosed(), isStrandMinus(), getId()));
            } else if ((getStart() < interval.getStart()) && (getEndClosed() <= interval.getEndClosed())) {
                // 'interval' overlaps right part of 'this' => Include left part of 'this'
                ints.add(new Marker(getParent(), getStart(), interval.getStart() - 1, isStrandMinus(), getId()));
            } else if ((getStart() < interval.getStart()) && (interval.getEndClosed() < getEndClosed())) {
                // 'interval' overlaps middle of 'this' => Include left and right part of 'this'
                ints.add(new Marker(getParent(), getStart(), interval.getStart() - 1, isStrandMinus(), getId()));
                ints.add(new Marker(getParent(), interval.getEndClosed() + 1, getEndClosed(), isStrandMinus(), getId()));
            } else throw new RuntimeException("Interval intersection not analysed. This should nbever happen!");
        } else ints.add(this); // No intersection => Just add 'this' interval

        return ints;
    }

    /**
     * Return a collection of intervals that intersect this marker
     */
    public Markers query(IntervalForest intervalForest) {
        return intervalForest.query(this);
    }

    /**
     * Query all genomic regions that intersect 'marker' (this makes sense in Gene, Transcript, Exon, etc.)
     */
    public Markers query(Marker marker) {
        return null;
    }

    /**
     * Parse a line (form a file)
     * Format: "chromosome \t start \t end \t id \n"
     */
    public void readTxt(String line, int lineNum, Genome genome, int positionBase) {
        line = line.trim(); // Remove spaces

        // Ignore empty lines and comment lines
        if ((line.length() > 0) && (!line.startsWith("#"))) {
            // Parse line
            String[] fields = line.split("\\s+");

            // Is line OK?
            if (fields.length >= 3) {
                Chromosome chromo = genome.getChromosome(fields[0].trim());
                if (chromo == null)
                    System.err.println("WARNING: Chromosome '" + fields[0] + "' not found in genome '" + genome.getGenomeName() + "', version '" + genome.getVersion() + "'!\n\tLine: " + lineNum + "\t'" + line + "'");
                parent = chromo;
                setStart(Gpr.parseIntSafe(fields[1]) - positionBase);
                setEndClosed(Gpr.parseIntSafe(fields[2]) - positionBase);
                id = "";

                if (fields.length >= 4) {
                    // Join all ids using a space character (and remove all spaces)
                    for (int t = 3; t < fields.length; t++)
                        id += fields[t].trim() + " ";
                    id = id.trim();
                }
            } else
                throw new RuntimeException("Error line " + lineNum + " (number of fields is " + fields.length + "):\t" + line);
        }
    }

    /**
     * Parse a line from a serialized file
     */
    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        type = EffectType.valueOf(markerSerializer.getNextField());
        markerSerializer.getNextFieldInt();
        parent = new MarkerParentId(markerSerializer.getNextFieldInt()); // Create a 'fake' parent. It will be replaced after all objects are in memory.
        setStart(markerSerializer.getNextFieldInt());
        setEndClosed(markerSerializer.getNextFieldInt());
        id = markerSerializer.getNextField();
        strandMinus = markerSerializer.getNextFieldBoolean();
    }

    /**
     * Create a string to serialize to a file
     */
    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return type //
                + "\t" + markerSerializer.getIdByMarker(this) //
                + "\t" + (parent != null ? markerSerializer.getIdByMarker((Marker) parent) : -1) //
                + "\t" + getStart() //
                + "\t" + getEndClosed() //
                + "\t" + id //
                + "\t" + strandMinus //
                ;
    }

    /**
     * True if the variant should be applied to the marker
     * We need to modify the coordinates if the variant is "left" respect to the marker
     */
    public boolean shouldApply(Variant variant) {
        // Variant after this marker: No effect when applying (all coordinates remain the same)
        return variant.getStart() <= getEndClosed();
    }

    @Override
    public String toString() {
        return getChromosomeName() + "\t" + getStart() + "-" + getEndClosed() //
                + " " //
                + type //
                + ((id != null) && (id.length() > 0) ? " '" + id + "'" : "");
    }

    /**
     * Union of two markers
     *
     * @return A new marker which is the union of the two
     */
    public Marker union(Marker m) {
        if (!getChromosomeName().equals(m.getChromosomeName())) return null;

        int ustart = Math.min(getStart(), m.getStart());
        int uend = Math.max(getEndClosed(), m.getEndClosed());
        return new Marker(getParent(), ustart, uend, strandMinus, "");
    }

    /**
     * Calculate the effect of this variant
     */
    public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false;
        variantEffects.add(variant, this, type, "");
        return true;
    }

    /**
     * Calculate the effect of this variant
     *
     * @param variant :	Before analyzing results, we have to change markers using variantrRef
     *                to create a new reference 'on the fly'
     */
    public boolean variantEffectNonRef(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false; // Sanity check

        if (variant.isNonRef()) {
            Variant variantRef = ((VariantNonRef) variant).getVariantRef();
            Marker newMarker = apply(variantRef);

            // Has the marker been deleted?
            // Then there is no effect over this marker (it does not exist any more)
            if (newMarker == null) return false;

            return newMarker.variantEffect(variant, variantEffects);
        }

        return variantEffect(variant, variantEffects);
    }

}
