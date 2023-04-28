package org.snpeff.interval;

import org.snpeff.align.VariantRealign;
import org.snpeff.binseq.GenomicSequences;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.GprSeq;
import org.snpeff.util.IubString;

import java.util.LinkedList;
import java.util.List;

/**
 * A variant represents a change in a reference sequence
 * <p>
 * As of version 4.0, variants have no strand, i.e. variants
 * on the negative are NOT allowed because they just complicate the
 * code and bring no real benefit.
 *
 * @author pcingola
 */
public class Variant extends Marker {

    public static final int HUGE_DELETION_SIZE_THRESHOLD = 1000000; // Number of bases
    public static final double HUGE_DELETION_RATIO_THRESHOLD = 0.01; // Percentage of bases
    public static final Variant NO_VARIANT = new Variant(null, 0, 0, ""); // Not a variant (ref=alt)
    private static final long serialVersionUID = -2928105165111400441L;

    protected VariantType variantType; // Variant type
    protected String ref; // Reference (i.e. original bases in the genome)
    protected String alt; // Changed bases
    protected String genotype; // Genotype 'ALT' (e.g. A VCF entry may encode multiple ALTs).
    protected boolean imprecise = false; // Imprecise variant: coordinates are not exact (E.g. see section "Encoding Structural Variants in VCF" from VCF spec. 4.1)

    public Variant() {
        super();
        ref = alt = "";
        variantType = VariantType.INTERVAL;
    }

    /**
     * This constructor is used when we only have interval data (e.g. when reading a BED file)
     */
    public Variant(Marker parent, int start, int end, String id) {
        super(parent, start, end, false, id);
        ref = alt = "";
        variantType = VariantType.INTERVAL;
    }

    public Variant(Marker parent, int position, String referenceStr, String altStr) {
        this(parent, position, referenceStr, altStr, "");
    }

    public Variant(Marker parent, int position, String referenceStr, String altStr, String id) {
        super(parent, position, position, false, id);
        init(position, referenceStr, altStr, null, id);
    }

    /**
     * Create variants from ALT (which can be multiple values)
     */
    public static List<Variant> factory(Chromosome chromo, int start, String ref, String altStr, String id, boolean expand) {
        LinkedList<Variant> list = new LinkedList<>();

        // No alt? It's an interval
        if (altStr == null) {
            Variant var = new Variant(chromo, start, ref, null, id);
            list.add(var);
            return list;
        }

        // Split alts
        String[] alts;
        if (altStr.indexOf(',') >= 0) alts = altStr.split(",");
        else {
            alts = altStr.split("/");

            // Special case, two ALTs are the same
            if (alts.length == 2 && alts[0].equals(alts[1])) {
                Variant var = new Variant(chromo, start, ref, alts[0], id);
                list.add(var);
                return list;
            }
        }

        // Add each alt
        for (String alt : alts) {
            // Note: We use 'hasIUBMax()' instead of 'hasIUB()' because large InDels may
            // have tons of 'N' bases. In such cases, it is impractical (and useless) to
            // produce all possible combinations
            boolean refIub, altIub;
            if (!expand) {
                // Non-IUB expansion needed
                Variant var = new Variant(chromo, start, ref, alt, id);
                list.add(var);
            } else {
                refIub = IubString.hasIUBMax(ref);
                altIub = IubString.hasIUBMax(alt);

                // Expand all possible REF / ALT combinations
                if (!refIub && !altIub) {
                    // Non-IUB expansion needed
                    Variant var = new Variant(chromo, start, ref, alt, id);
                    list.add(var);
                } else if (altIub && !refIub) {
                    // ALT has IUB characters
                    IubString iubsAlt = new IubString(alt);
                    for (String seqAlt : iubsAlt) {
                        Variant var = new Variant(chromo, start, ref, seqAlt, id);
                        list.add(var);
                    }
                } else if (!altIub && refIub) {
                    // REF has IUB characters
                    IubString iubsRef = new IubString(ref);
                    for (String seqRef : iubsRef) {
                        Variant var = new Variant(chromo, start, seqRef, alt, id);
                        list.add(var);
                    }
                } else { // Condition: altIub && refIub
                    // Both REF and ALT have IUB characters
                    IubString iubsRef = new IubString(ref);
                    for (String seqRef : iubsRef) {
                        IubString iubsAlt = new IubString(alt);
                        for (String seqAlt : iubsAlt) {
                            Variant var = new Variant(chromo, start, seqRef, seqAlt, id);
                            list.add(var);
                        }
                    }
                }
            }
        }

        return list;
    }

    @Override
    public Variant clone() {
        return (Variant) super.clone();
    }

    @Override
    public Variant cloneShallow() {
        Variant clone = (Variant) super.cloneShallow();
        clone.variantType = variantType;
        clone.ref = ref;
        clone.alt = alt;
        clone.genotype = genotype;
        clone.imprecise = imprecise;
        return clone;
    }

    /**
     * Compare by start and end
     */
    @Override
    public int compareTo(Interval i2) {
        int comp = compareToPos(i2);
        if (comp != 0) return comp;

        Variant v2 = (Variant) i2;
        comp = ref.compareTo(v2.ref);
        if (comp != 0) return comp;

        comp = alt.compareTo(v2.alt);
        return comp;
    }

    /**
     * Decompose a variant into basic constituents
     * At the moment this only makes sense for MIXED variants which
     * are decomposed into two variants: MNP + InDel
     */
    public Variant[] decompose() {
        if (variantType != VariantType.MIXED)
            throw new RuntimeException("Cannot decompose variant type " + variantType + ":\n\t" + this);

        Variant varMnp, varInDel;
        if (ref.length() < alt.length()) {
            // MNP + INS
            varMnp = new Variant(getChromosome(), getStart(), ref, alt.substring(0, ref.length()), id + "_MNP");
            varInDel = new Variant(getChromosome(), getStart() + ref.length(), "", alt.substring(ref.length()), id + "_INS");
        } else {
            // MNP + DEL
            varMnp = new Variant(getChromosome(), getStart(), ref.substring(0, alt.length()), alt, id + "_MNP");
            varInDel = new Variant(getChromosome(), getStart() + alt.length(), ref.substring(alt.length()), "", id + "_DEL");
        }

        Variant[] variants = new Variant[2];
        variants[0] = varMnp;
        variants[1] = varInDel;

        return variants;
    }

    public String getAlt() {
        return alt;
    }

    public String getGenotype() {
        return genotype;
    }

    public void setGenotype(String genotype) {
        this.genotype = genotype;
    }

    public String getReference() {
        return ref;
    }

    public VariantType getVariantType() {
        return variantType;
    }

    public void setVariantType(VariantType variantType) {
        this.variantType = variantType;
    }

    @Override
    public int hashCode() {
        int hashCode = getChromosomeName().hashCode();
        hashCode = hashCode * 31 + getStart();
        hashCode = hashCode * 31 + getEnd();
        hashCode = hashCode * 31 + (strandMinus ? -1 : 1);
        hashCode = hashCode * 31 + id.hashCode();
        hashCode = hashCode * 31 + ref.hashCode();
        hashCode = hashCode * 31 + alt.hashCode();
        return hashCode;
    }

    void init(int position, String referenceStr, String altStr, VariantType variantType, String id) {
        if (altStr == null) {
            // Not a variant (this is an interval). Set ref = alt
            altStr = referenceStr;
            variantType = VariantType.INTERVAL;
        }

        ref = referenceStr.toUpperCase();
        alt = altStr.toUpperCase();

        // Sanity check
        if (altStr.indexOf(',') >= 0 || altStr.indexOf('/') >= 0)
            throw new RuntimeException("Variants with multiple ALTs are not allowed (ALT: '" + altStr + "')");

        // Remove leading char (we still have some test cases using old TXT format)
        if (ref.equals("*")) ref = "";

        if (alt.startsWith("+")) {
            // Insertion
            alt = ref + alt.substring(1);
        } else if (alt.startsWith("-")) {
            // Deletion
            ref = alt.substring(1);
            alt = "";
        } else if (alt.startsWith("=")) {
            // Mixed variant
            alt = altStr.substring(1);
        }

        // Calculate variant type
        if (variantType == null) {
            if (ref.equals(alt)) this.variantType = VariantType.INTERVAL;
            else if (ref.length() == 1 && alt.length() == 1) this.variantType = VariantType.SNP;
            else if (ref.length() == alt.length()) this.variantType = VariantType.MNP;
            else if (ref.length() < alt.length() && alt.startsWith(ref)) this.variantType = VariantType.INS;
            else if (ref.length() > alt.length() && ref.startsWith(alt)) this.variantType = VariantType.DEL;
            else this.variantType = VariantType.MIXED;
        } else this.variantType = variantType;

        // Start and end position
        // 	- Start is always the leftmost base
        //	- End is always the rightmost affected base in the reference genome
        setStart(position);
        if (isSnp()) {
            // These changes only affect one base in the reference genome
            setEndClosed(getStart());
        } else if (isIns()) {
            // Insertions are "zero" length
            setEnd(getStart());
        } else {
            // Cases isDel(), or isMnp(): Update 'end' position
            setEnd(getStart() + ref.length());
        }

        // Effect type
        type = EffectType.NONE;
        this.id = id;
    }

    public boolean isBnd() {
        return false;
    }

    public boolean isDel() {
        return (variantType == VariantType.DEL);
    }

    public boolean isDup() {
        return (variantType == VariantType.DUP);
    }

    public boolean isImprecise() {
        return imprecise;
    }

    public void setImprecise(boolean imprecise) {
        this.imprecise = imprecise;
    }

    public boolean isInDel() {
        return (variantType == VariantType.INS) || (variantType == VariantType.DEL);
    }

    public boolean isIns() {
        return variantType == VariantType.INS;
    }

    public boolean isInterval() {
        return variantType == VariantType.INTERVAL;
    }

    public boolean isInv() {
        return variantType == VariantType.INV;
    }

    public boolean isMixed() {
        return variantType == VariantType.MIXED;
    }

    public boolean isMnp() {
        return variantType == VariantType.MNP;
    }

    public boolean isNonRef() {
        return false;
    }

    @Override
    protected boolean isShowWarningIfParentDoesNotInclude() {
        return false;
    }

    public boolean isSnp() {
        return variantType == VariantType.SNP;
    }

    public boolean isStructural() {
        return isDel() || isInv() || isDup() || isBnd();
    }

    /**
     * Is this a huge structural variant?
     */
    public boolean isStructuralHuge() {
        if (!isStructural()) return false;

        // Chromosome might not exists (e.g. error in chromosome name or '-noGenome' option)
        Chromosome chr = getChromosome();

        // We only check if: a) that the chromosome exists and b) chromosome it is not small
        if (chr != null && chr.size() > HUGE_DELETION_SIZE_THRESHOLD) {
            double ratio = (chr.size() > 0 ? size() / ((double) chr.size()) : 0);
            return size() > HUGE_DELETION_SIZE_THRESHOLD || ratio > HUGE_DELETION_RATIO_THRESHOLD;
        }

        return size() > HUGE_DELETION_SIZE_THRESHOLD;
    }

    /**
     * Is this a change or is ALT actually the same as the reference
     */
    public boolean isVariant() {
        return variantType != VariantType.INTERVAL;
    }

    /**
     * Calculate the number of bases of change in length
     */
    public int lengthChange() {
        if (isSnp() || isMnp()) return 0;

        // This is a length changing Variant (i.e. Insertions, deletion, or mixed change)
        // Calculate the number of bases of change in length
        if (!ref.isEmpty() || !alt.isEmpty()) return alt.length() - ref.length();

        // Default to traditional approach for imprecise and structural variants
        return size();
    }

    /**
     * Return the change (always compared to 'referenceStrand')
     */
    public String netChange(boolean reverseStrand) {
        if (isDel()) return reverseStrand ? GprSeq.reverseWc(ref) : ref; // Deletions have empty 'alt'
        return reverseStrand ? GprSeq.reverseWc(alt) : alt; // Need reverse-WC?
    }

    /**
     * Only the part of the change that overlaps with a marker
     * Return the change (always in positive strand)
     */
    public String netChange(Marker marker) {
        String netChange = alt;
        if (isDel()) netChange = ref; // In deletions 'alt' is empty

        int removeBefore = marker.getStart() - getStart();
        if (removeBefore > 0) {
            if (removeBefore >= netChange.length()) return ""; // Nothing left
        } else removeBefore = 0;

        int removeAfter = getEndClosed() - marker.getEndClosed();
        if (removeAfter > 0) {
            if ((removeBefore + removeAfter) >= netChange.length()) return ""; // Nothing left
        } else removeAfter = 0;

        // Remove leading and trailing parts
        netChange = netChange.substring(removeBefore, netChange.length() - removeAfter);

        return netChange;
    }

    /**
     * Create a new variant realigning it towards the leftmost position
     */
    public Variant realignLeft() {
        GenomicSequences gs = getGenome().getGenomicSequences();
        if (gs == null) return this;

        VariantRealign vr = new VariantRealign(this);
        vr.setDebug(Config.get().isDebug());
        if (!vr.realign()) return this;
        return vr.getVariantRealigned();
    }

    /**
     * Reverse variant (e.g. back to reference in cancer samples)
     */
    public Variant reverse() {
        Variant clone = (Variant) super.cloneShallow();
        clone.variantType = variantType;
        clone.alt = ref;
        clone.ref = alt;
        clone.genotype = ref;
        clone.imprecise = imprecise;
        return clone;
    }

    @Override
    public String toString() {
        if ((ref == null || ref.isEmpty()) //
                && (alt == null || alt.isEmpty()) //
        ) return "chr" + getChromosomeName() + ":" + getStart() + "-" + getEndClosed() + "[" + variantType + "]";

        return "chr" + getChromosomeName() //
                + ":" + getStart() //
                + "_" + getReference() //
                + "/" + getAlt() //
                + ((id != null) && (id.length() > 0) ? " '" + id + "'" : "");
    }

    /**
     * Old format, used for some test cases
     */
    public String toStringOld() {
        if (isIns()) return getChromosomeName() + ":" + getStart() + "_*" + "/+" + getAlt();
        else if (isDel()) return getChromosomeName() + ":" + getStart() + "_*" + "/-" + getReference();
        return getChromosomeName() + ":" + getStart() + "_" + getReference() + "/" + getAlt();
    }

    public enum VariantType {
        SNP// Single nucleotide polymorphism (i.e. 1 base is changed)
        , MNP // Multiple nucleotide polymorphism (i.e. several bases are changed)
        , INS // Insertion (i.e. some bases added)
        , DEL // Deletion (some bases removed)
        , MIXED // A mixture of insertion, deletions, SNPs and or MNPs (a.k.a. substitution)
        , INV // Inversion (structural variant)
        , DUP // Duplication (structural variant)
        , BND // Break-ends (rearrangement)
        , INTERVAL
        // Just analyze interval hits. Not a variant (e.g. BED input format)
    }

}
