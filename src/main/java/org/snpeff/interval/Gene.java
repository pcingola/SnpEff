package org.snpeff.interval;

import org.snpeff.interval.tree.IntervalTree;
import org.snpeff.interval.tree.Itree;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.*;
import org.snpeff.stats.ObservedOverExpectedCpG;
import org.snpeff.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

/**
 * Interval for a gene, as well as transcripts
 *
 * @author pcingola
 */
public class Gene extends IntervalAndSubIntervals<Transcript> implements Serializable {

    public static final String CIRCULAR_GENE_ID = "_circ";
    private static final long serialVersionUID = 8419206759034068147L;
    BioType bioType;
    String geneName;
    Itree intervalTreeGene;
    public Gene() {
        super();
        geneName = "";
        bioType = null;
        type = EffectType.GENE;
    }

    public Gene(Marker parent, int start, int end, boolean strandMinus, String id, String geneName, BioType bioType) {
        super(parent, start, end, strandMinus, id);
        this.geneName = geneName;
        this.bioType = bioType;
        type = EffectType.GENE;
    }

    /**
     * Add a gene dependent marker
     */
    public void addPerGene(Marker marker) {
        if (intervalTreeGene == null) intervalTreeGene = new IntervalTree();

        intervalTreeGene.add(marker);
    }

    /**
     * Adjust start, end and strand values
     *
     * @return true if any adjustment was done
     */
    public boolean adjust() {
        boolean changed = false;
        int strandSumGene = 0;

        int newStart = Integer.MAX_VALUE;
        int newEnd = Integer.MIN_VALUE;

        for (Transcript tr : this) {
            newStart = Math.min(newStart, tr.getStart());
            newEnd = Math.max(newEnd, tr.getEndClosed());

            for (Exon exon : tr.sortedStrand()) {
                newStart = Math.min(newStart, exon.getStart());
                newEnd = Math.max(newEnd, exon.getEndClosed());
                strandSumGene += exon.isStrandMinus() ? -1 : 1; // Some exons have incorrect strands, we use the strand indicated by most exons
            }

            for (Utr utr : tr.getUtrs()) {
                newStart = Math.min(newStart, utr.getStart());
                newEnd = Math.max(newEnd, utr.getEndClosed());
            }
        }

        // Change gene strand?
        boolean newStrandMinus = strandSumGene < 0;
        if (strandMinus != newStrandMinus) {
            strandMinus = newStrandMinus;
            changed = true;
        }

        if (newStart < newEnd) {
            // Change start?
            if (getStart() != newStart) {
                if (Config.get().isVerbose()) Log.warning(ErrorWarningType.WARNING_GENE_COORDINATES, "Gene '" + id + "' (name:'" + geneName + "'), adjusting start coordinate from " + getStart() + " to " + newStart);
                setStart(newStart);
                changed = true;
            }

            // Change end?
            if (getEndClosed() != newEnd) {
                if (Config.get().isVerbose()) Log.warning(ErrorWarningType.WARNING_GENE_COORDINATES, "Gene '" + id + "' (name:'" + geneName + "'), adjusting end coordinate from " + getEndClosed() + " to " + newEnd);
                setEndClosed(newEnd);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Build gene dependent interval tree
     */
    public void buildPerGene() {
        if (intervalTreeGene != null) intervalTreeGene.build();
    }

    /**
     * Get canonical transcript
     * Canonical transcripts are defined as the longest CDS of amongst the protein coding transcripts.
     * If none of the transcripts is protein coding, then it is the longest cDNA.
     */
    public Transcript canonical() {
        Transcript canonical = null;
        int canonicalLen = 0;

        if (isProteinCoding()) {
            // Find canonical transcript in protein coding gene (longest CDS)
            for (Transcript t : this) {
                int tlen = t.cds().length();

                // Compare coding length. If both lengths are equal, compare IDs
                if (t.isProteinCoding() //
                        && ((canonical == null) // No canonical selected so far? => Select this one
                        || (canonicalLen < tlen) // Longer? => Update
                        || ((canonicalLen == tlen) && (t.getId().compareTo(canonical.getId()) < 0)) // Same length? Compare IDs
                ) //
                ) {
                    canonical = t;
                    canonicalLen = tlen;
                }
            }
        } else {
            // Find canonical transcript in non-protein coding gene (longest mRNA)
            for (Transcript t : this) {
                int tlen = t.mRna().length();

                if (canonicalLen <= tlen //
                        && ((canonical == null) // No canonical selected so far? => Select this one
                        || (canonicalLen < tlen) // Longer? => Update
                        || ((canonicalLen == tlen) && (t.getId().compareTo(canonical.getId()) < 0)) // Same length? Compare IDs
                ) //
                ) {
                    canonical = t;
                    canonicalLen = tlen;
                }
            }
        }

        // Found canonical transcript? Set canonical flag
        if (canonical != null) canonical.setCanonical(true);

        return canonical;
    }

    /**
     * In a circular genome, a gene can have negative coordinates or crosses
     * over chromosome end. These genes are mirrored to the opposite end of
     * the chromosome so that they can be referenced by both circular
     * coordinates.
     */
    public Gene circularClone() {
        Gene newGene = (Gene) clone();

        // Change IDs
        newGene.setId(id + CIRCULAR_GENE_ID);
        for (Transcript tr : newGene) {
            tr.setId(tr.getId() + CIRCULAR_GENE_ID);
            for (Exon ex : tr)
                ex.setId(ex.getId() + CIRCULAR_GENE_ID);
        }

        // Shift coordinates
        Chromosome chr = getChromosome();

        int shift = 0;
        if (getStart() < 0) {
            shift = chr.size();
        } else if (getEndClosed() > chr.getEndClosed()) {
            shift = -chr.size();
        } else throw new RuntimeException("Sanity check: This should never happen!");

        newGene.shiftCoordinates(shift);
        if (Config.get().isVerbose()) {
            Log.info("Gene '" + id + "' spans across coordinate zero: Assuming circular chromosome, creating mirror gene at the end." //
                    + "\n\tGene        :" + toStr() //
                    + "\n\tNew gene    :" + newGene.toStr() //
                    + "\n\tChrsomosome :" + chr.toStr() //
            );
        }

        return newGene;
    }

    @Override
    public Gene cloneShallow() {
        Gene clone = (Gene) super.cloneShallow();

        clone.bioType = bioType;
        clone.geneName = geneName;

        return clone;
    }

    /**
     * Calculate CpG bias: number of CpG / expected[CpG]
     */
    public double cpgExonBias() {
        ObservedOverExpectedCpG oe = new ObservedOverExpectedCpG();
        return oe.oe(this);
    }

    /**
     * Filter transcripts by TSL
     */
    public void filterTranscriptSupportLevel(TranscriptSupportLevel maxTsl) {
        ArrayList<Transcript> toDelete = new ArrayList<>();

        // Mark transcripts for removal
        for (Transcript tr : this)
            if (!tr.hasTranscriptSupportLevelInfo() //
                    || (tr.getTranscriptSupportLevel().compareTo(maxTsl) > 0) //
            ) toDelete.add(tr);

        // Remove transcripts
        for (Transcript t : toDelete)
            remove(t);
    }

    public GeneType geneType() {
        if (bioType != null) {
            if (bioType.isProteinCoding()) return GeneType.CODING;
            return GeneType.NON_CODING;
        }

        return GeneType.UNKNOWN;
    }

    public BioType getBioType() {
        return bioType;
    }

    public void setBioType(BioType bioType) {
        this.bioType = bioType;
    }

    public String getGeneName() {
        return geneName;
    }

    /**
     * Is any of the transcripts protein coding?
     */
    public boolean isProteinCoding() {
        for (Transcript tr : this)
            if (tr.isProteinCoding()) return true;
        return false;
    }

    @Override
    protected boolean isShowWarningIfParentDoesNotInclude() {
        return false;
    }

    /**
     * Remove all transcripts in trIds
     *
     * @return : Number of transcripts removed
     */
    public int keepTranscripts(Set<String> trIds) {
        // Find transcripts in trIds
        ArrayList<Transcript> toDelete = new ArrayList<>();
        for (Transcript t : this) {
            String trId = t.getId();

            // Sometimes the provided list does not have version numbers
            // (e.g. NM_005157 instead of NM_005157.4)
            String trIdNoVersion = t.getId();
            int versionIdx = trIdNoVersion.indexOf('.');
            if (versionIdx > 0) trIdNoVersion = trIdNoVersion.substring(0, versionIdx);

            // Transcript not in the list? => Remove it
            if (!trIds.contains(trId) && !trIds.contains(trIdNoVersion)) toDelete.add(t);
        }

        // Remove them
        for (Transcript t : toDelete)
            remove(t);

        return toDelete.size();
    }

    /**
     * Keep only protein coding transcripts
     *
     * @return : Number of transcripts removed
     */
    public int keepTranscriptsProteinCoding() {
        // Find transcripts in trIds
        ArrayList<Transcript> toDelete = new ArrayList<>();
        for (Transcript t : this)
            if (!t.isProteinCoding()) toDelete.add(t);

        // Remove them
        for (Transcript t : toDelete)
            remove(t);

        return toDelete.size();
    }

    @Override
    public Markers markers() {
        Markers markers = new Markers();
        for (Transcript tr : this) {
            markers.add(tr);
            markers.add(tr.markers());
        }
        return markers;
    }

    /**
     * Remove all non-canonical transcripts
     */
    public void removeNonCanonical(String trId) {
        // User transcript 'trId' or find canonical transcript
        Transcript canonical;
        if (trId != null) {
            canonical = get(trId);
            if (canonical == null) throw new RuntimeException("Canonical transcript '" + trId + "' not found! Gene name: '" + getGeneName() + "', Gene ID: '" + getId() + "'");
        } else canonical = canonical();

        // Found canonical? => Remove all others
        if (canonical != null) {
            // Remove all other transcripts
            ArrayList<Transcript> toDelete = new ArrayList<>();
            toDelete.addAll(subIntervals());
            toDelete.remove(canonical); // Do not remove canonical transcript.

            // Remove all other transcripts
            for (Transcript t : toDelete)
                remove(t);
        }
    }

    /**
     * Remove unverified or corrected transcripts
     *
     * @return : True if ALL transcripts have been removed
     */
    public boolean removeUnverified() {
        // Mark unchecked transcripts for deletion
        ArrayList<Transcript> toDelete = new ArrayList<>();

        int countRemoved = 0;
        for (Transcript tr : this) {
            if (!tr.isChecked() || tr.isCorrected()) {
                toDelete.add(tr);
                countRemoved++;
            }
        }

        if (Config.get().isDebug()) Log.debug("Gene '', removing " + countRemoved + " / " + numChilds() + " unchecked transcript.");

        // Remove
        for (Transcript t : toDelete)
            remove(t);

        return numChilds() <= 0;
    }

    /**
     * Parse a line from a serialized file
     */
    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        super.serializeParse(markerSerializer);
        geneName = markerSerializer.getNextField();
        bioType = BioType.parse(markerSerializer.getNextField());
    }

    /**
     * Create a string to serialize to a file
     */
    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return super.serializeSave(markerSerializer) //
                + "\t" + geneName //
                + "\t" + bioType;
    }

    /**
     * Size of a genetic region for a given gene
     */
    public int sizeof(String type) {
        // Calculate size
        EffectType eff = EffectType.valueOf(type.toUpperCase());
        Markers all = new Markers();
        int len = 0;

        switch (eff) {
            case GENE:
                return size();

            case EXON:
                // Add all exons
                for (Transcript tr : this)
                    for (Exon ex : tr)
                        all.add(ex);
                break;

            case CDS:
                // Add all cds
                for (Transcript tr : this)
                    for (Cds cds : tr.getCds())
                        all.add(cds);
                break;

            case TRANSCRIPT:
                // Add all transcripts
                for (Transcript tr : this)
                    all.add(tr);
                break;

            case INTRON:
                return Math.max(0, sizeof("TRANSCRIPT") - sizeof("EXON"));

            case UTR_3_PRIME:
                // Add all Utr3prime
                for (Transcript tr : this)
                    for (Utr3prime utr : tr.get3primeUtrs())
                        all.add(utr);
                break;

            case UTR_5_PRIME:
                // Add all Utr3prime
                for (Transcript tr : this)
                    for (Utr5prime utr : tr.get5primeUtrs())
                        all.add(utr);
                break;

            case UPSTREAM:
                for (Transcript tr : this)
                    all.add(tr.getUpstream());

                break;

            case DOWNSTREAM:
                for (Transcript tr : this)
                    all.add(tr.getDownstream());
                break;

            case SPLICE_SITE_ACCEPTOR:
                // Add all exons
                for (Transcript tr : this)
                    for (Exon ex : tr)
                        for (SpliceSite ss : ex.getSpliceSites())
                            if (ss instanceof SpliceSiteAcceptor) all.add(ss);
                break;

            case SPLICE_SITE_BRANCH:
                // Add all exons
                for (Transcript tr : this)
                    for (SpliceSite ss : tr.spliceSites())
                        if (ss instanceof SpliceSiteBranch) all.add(ss);
                break;

            case SPLICE_SITE_DONOR:
                // Add all exons
                for (Transcript tr : this)
                    for (Exon ex : tr)
                        for (SpliceSite ss : ex.getSpliceSites())
                            if (ss instanceof SpliceSiteDonor) all.add(ss);
                break;

            case SPLICE_SITE_REGION:
                // Add all exons
                for (Transcript tr : this) {
                    for (Exon ex : tr) {
                        for (SpliceSite ss : ex.getSpliceSites())
                            if (ss instanceof SpliceSiteRegion) all.add(ss);
                    }

                    for (Intron intron : tr.introns()) {
                        for (SpliceSite ss : intron.getSpliceSites())
                            if (ss instanceof SpliceSiteRegion) all.add(ss);
                    }
                }

            case INTRAGENIC:
                // We have to perform a set minus operation between this gene and all the transcripts
                Markers gene = new Markers();
                gene.add(this);

                // Create transcripts
                Markers trans = new Markers();
                for (Transcript tr : this)
                    trans.add(tr);

                all = gene.minus(trans);
                break;

            case NONE:
                return 0;

            default:
                throw new RuntimeException("Unimplemented sizeof('" + type + "')");
        }

        // Merge and calculate total length
        Markers merged = all.merge();
        for (Marker m : merged)
            len += m.size();

        return len;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    public String toString(boolean showTr) {
        StringBuilder sb = new StringBuilder();
        sb.append(getChromosomeName() + ":" + getStart() + "-" + getEndClosed());
        sb.append(", strand:" + (strandMinus ? "-1" : "1"));
        if ((id != null) && (id.length() > 0)) sb.append(", id:" + id);
        if ((geneName != null) && (geneName.length() > 0)) sb.append(", name:" + geneName);
        if ((bioType != null) && (bioType != null)) sb.append(", bioType:" + bioType);

        sb.append("\n");

        if (showTr && numChilds() > 0) {
            sb.append("Transcipts:\n");
            for (Transcript tint : sorted())
                sb.append("\t" + tint + "\n");
        }

        return sb.toString();
    }

    /**
     * Get some details about the effect on this gene
     */
    @Override
    public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false; // Sanity check

        //---
        // Shift variant towards the most 3-prime end
        // This is done in order to comply with HGVS notation
        //---
        boolean shifted3prime = false;
        if (Config.get().isHgvsShift()) {
            // Keep track of the original variants, just in case it is changed
            Variant variantOri = variant;
            // Do we need to 'walk and roll'? I.e. align variant towards the most 3-prime
            // end of the transcript? Note that VCF request variants to be aligned towards
            // the 'leftmost' coordinate, so this re-alignment is only required for variants
            // within transcripts on the positive strand.
            if (!variant.isSnp() && Config.get().isHgvsShift() && isStrandPlus()) {
                // Get sequence information. Might have to load sequences from database
                variant = variant.realignLeft();

                // Created a new variant? => It was shifted towards the left (i.e. 3-prime)
                shifted3prime = (variant != variantOri);
            }
        }

        //---
        // Calculate effects
        //---

        // Calculate effect for each transcript
        boolean hitTranscript = false;
        for (Transcript tr : this) {
            // Apply sequence change to create new 'reference'?
            if (variant.isNonRef()) {
                Variant vref = ((VariantNonRef) variant).getVariantRef();
                tr = tr.apply(vref); // Note: Do we need to check on null transcript? e.g. huge deletion removing the whole transcript
            }

            // Calculate effects
            hitTranscript |= tr.intersects(variant);

            tr.variantEffect(variant, variantEffects);
        }

        // May be none of the transcripts are actually hit
        if (!hitTranscript) {
            variantEffects.add(variant, this, EffectType.INTRAGENIC, "");
            return true;
        }

        // Do we have 'per gene' information?
        variantEffectGene(variant, variantEffects);

        //---
        // Do we need to add INFO_SHIFT_3_PRIME warning message?
        //---
        if (shifted3prime) {
            for (VariantEffect ve : variantEffects) {
                // Is this effect using the shifted variant? => Mark as shifted
                if (ve.getVariant() == variant) ve.addErrorWarningInfo(ErrorWarningType.INFO_REALIGN_3_PRIME);
            }
        }

        return true;
    }

    /**
     * Add gene-specific annotations
     */
    protected void variantEffectGene(Variant variant, VariantEffects variantEffects) {
        if (intervalTreeGene == null) return;

        Markers res = intervalTreeGene.query(variant);
        for (Marker m : res)
            m.variantEffect(variant, variantEffects);
    }

    public enum GeneType {
        CODING, NON_CODING, UNKNOWN
    }
}
