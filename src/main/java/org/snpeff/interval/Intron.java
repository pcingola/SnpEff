package org.snpeff.interval;

import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffects;

import java.util.ArrayList;

/**
 * Intron
 *
 * @author pcingola
 */
public class Intron extends Marker {

    private static final long serialVersionUID = -8283322526157264389L;

    int rank; // Exon rank in transcript
    Exon exonBefore; // Exon before this intron
    Exon exonAfter; // Exon after this intron
    ArrayList<SpliceSite> spliceSites;

    public Intron() {
        super();
        type = EffectType.INTRON;
        exonAfter = exonBefore = null;
        spliceSites = new ArrayList<SpliceSite>();
    }

    public Intron(Transcript parent, int start, int end, boolean strandMinus, String id, Exon exonBefore, Exon exonAfter) {
        super(parent, start, end, strandMinus, id);
        type = EffectType.INTRON;
        this.exonAfter = exonAfter;
        this.exonBefore = exonBefore;
        spliceSites = new ArrayList<SpliceSite>();
    }

    /**
     * Add a splice site to the collection
     */
    public void add(SpliceSite ss) {
        spliceSites.add(ss);
    }

    @Override
    public Intron apply(Variant variant) {
        // Create new exon with updated coordinates
        Intron newIntron = (Intron) super.apply(variant);
        if (newIntron == null) return null;

        // Splice sites should be created using Transcript.createSpliceSites() method
        newIntron.reset();

        return newIntron;
    }

    @Override
    public Intron cloneShallow() {
        Intron clone = (Intron) super.cloneShallow();
        clone.rank = rank;
        return clone;
    }

    /**
     * Create a splice site acceptor of 'size' length
     * Acceptor site: 3' end of the intron
     */
    public SpliceSiteAcceptor createSpliceSiteAcceptor(int maxSpliceSiteSize) {
        maxSpliceSiteSize = Math.min(maxSpliceSiteSize, size()); // Cannot be larger than this intron
        if (maxSpliceSiteSize <= 0) return null;

        int ssstart, ssend;
        if (isStrandPlus()) {
            ssstart = getEndClosed() - (maxSpliceSiteSize - 1);
            ssend = getEndClosed();
        } else {
            ssstart = getStart();
            ssend = getStart() + (maxSpliceSiteSize - 1);
        }

        SpliceSiteAcceptor spliceSiteAcceptor = new SpliceSiteAcceptor(this, ssstart, ssend, strandMinus, id);
        add(spliceSiteAcceptor);

        return spliceSiteAcceptor;
    }

    /**
     * Create a splice site donor of 'maxSize' length
     * Donor site: 5' end of the intron
     */
    public SpliceSiteDonor createSpliceSiteDonor(int maxSpliceSiteSize) {
        maxSpliceSiteSize = Math.min(maxSpliceSiteSize, size()); // Cannot be larger than this intron
        if (maxSpliceSiteSize <= 0) return null;

        int ssstart, ssend;
        if (isStrandPlus()) {
            ssstart = getStart();
            ssend = getStart() + (maxSpliceSiteSize - 1);
        } else {
            ssstart = getEndClosed() - (maxSpliceSiteSize - 1);
            ssend = getEndClosed();
        }

        SpliceSiteDonor spliceSiteDonor = new SpliceSiteDonor(this, ssstart, ssend, strandMinus, id);
        add(spliceSiteDonor);

        return spliceSiteDonor;
    }

    /**
     * Create splice site region
     */
    public SpliceSiteRegion createSpliceSiteRegionEnd(int sizeMin, int sizeMax) {
        if (sizeMin < 0) return null;
        if (sizeMax > size()) sizeMax = size(); // Cannot be larger than this intron
        if (sizeMax <= sizeMin) return null; // Cannot be less than 'sizeMin' bases long

        SpliceSiteRegion spliceSiteRegionEnd = null;
        if (isStrandPlus()) spliceSiteRegionEnd = new SpliceSiteRegion(this, getEndClosed() - (sizeMax - 1), getEndClosed() - (sizeMin - 1), strandMinus, id);
        else spliceSiteRegionEnd = new SpliceSiteRegion(this, getStart() + sizeMin - 1, getStart() + sizeMax - 1, strandMinus, id);

        if (spliceSiteRegionEnd != null) add(spliceSiteRegionEnd);

        return spliceSiteRegionEnd;
    }

    /**
     * Create splice site region
     */
    public SpliceSiteRegion createSpliceSiteRegionStart(int sizeMin, int sizeMax) {
        if (sizeMin < 0) return null;
        if (sizeMax > size()) sizeMax = size(); // Cannot be larger than this intron
        if (sizeMax <= sizeMin) return null; // Cannot be less than 'sizeMin' bases long

        SpliceSiteRegion spliceSiteRegionStart = null;
        if (isStrandPlus()) spliceSiteRegionStart = new SpliceSiteRegion(this, getStart() + (sizeMin - 1), getStart() + (sizeMax - 1), strandMinus, id);
        else spliceSiteRegionStart = new SpliceSiteRegion(this, getEndClosed() - (sizeMax - 1), getEndClosed() - (sizeMin - 1), strandMinus, id);

        if (spliceSiteRegionStart != null) add(spliceSiteRegionStart);

        return spliceSiteRegionStart;
    }

    public Exon getExonAfter() {
        return exonAfter;
    }

    public Exon getExonBefore() {
        return exonBefore;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    public ArrayList<SpliceSite> getSpliceSites() {
        return spliceSites;
    }

    public String getSpliceType() {
        return (exonBefore != null ? exonBefore.getSpliceType() : "") //
                + "-" //
                + (exonAfter != null ? exonAfter.getSpliceType() : "") //
                ;
    }

    /**
     * Query all genomic regions that intersect 'marker'
     */
    @Override
    public Markers query(Marker marker) {
        Markers markers = new Markers();

        for (SpliceSite ss : spliceSites)
            if (ss.intersects(marker)) markers.add(ss);

        return markers;
    }

    public void reset() {
        spliceSites = new ArrayList<SpliceSite>();
    }

    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        // Note: We do not save splice sites
        super.serializeParse(markerSerializer);
    }

    /**
     * Create a string to serialize to a file
     */
    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        // Note: We do not save splice sites
        return super.serializeSave(markerSerializer);
    }

    @Override
    public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false;

        for (SpliceSite ss : spliceSites)
            if (ss.intersects(variant)) ss.variantEffect(variant, variantEffects);

        // Add intron part
        variantEffects.addEffectType(variant, this, EffectType.INTRON);

        return true;
    }

}
