package org.snpeff.interval;

import org.snpeff.interval.codonChange.CodonChange;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.stats.ObservedOverExpectedCpG;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import java.util.*;

/**
 * Codon position
 *
 * @author pcingola
 */
class CodonPosition {

    public int codonNum = -1;
    public int codonIndex = -1;
}

/**
 * Interval for a transcript, as well as some other information: exons, utrs, cds, etc.
 *
 * @author pcingola
 */
public class Transcript extends IntervalAndSubIntervals<Exon> {

    private static final long serialVersionUID = -2665025617916107311L;

    boolean aaCheck; // Has this transcript been checked against a protein sequence?
    boolean canonical; // Is this a canonical transcript?
    boolean corrected; // Have coordinates been corrected? (e.g. frame correction)
    boolean dnaCheck; // Has this transcript been checked against a CDS/cDNA sequence?
    boolean proteinCoding; // Is this a protein-coding transcript?
    boolean ribosomalSlippage; // Ribosomal slippage causes changes in reading frames. This might be represented as negative length introns (overlapping exons).
    int cdsStart, cdsEnd; // CDS start and end coordinates. Note: If the transcript is in reverse strand, then cdsStart > cdsEnd
    int spliceSiteSize, spliceRegionExonSize, spliceRegionIntronMin, spliceRegionIntronMax; // Splice sizes
    int upDownLength; // Upstream and downstream size
    BioType bioType; // Transcript biotype
    String cds; // Coding sequence
    String mRna; // mRna sequence (includes 5'UTR and 3'UTR)
    String protein; // Protein sequence
    String version = ""; // Transcript version
    List<Utr> utrs; // UTRs
    List<Cds> cdss; // CDS information
    List<Intron> introns; // Intron markers
    Upstream upstream; // Upstream interval
    Downstream downstream; // Downstream interval
    Exon firstCodingExon; // First coding exon. I.e. where transcription start site (TSS) is.
    int[] cds2pos, aa2pos;
    TranscriptSupportLevel transcriptSupportLevel = null;

    public Transcript() {
        super();
        utrs = new ArrayList<>();
        cdss = new ArrayList<>();
        type = EffectType.TRANSCRIPT;
    }

    public Transcript(Gene gene, int start, int end, boolean strandMinus, String id) {
        super(gene, start, end, strandMinus, id);
        type = EffectType.TRANSCRIPT;
    }

    /**
     * Calculate chromosome position as function of Amino Acid number
     * Note that returns the chromosomal position of the first base for each Amino Acid
     * <p>
     * If you need the chromosomal position of each base
     *
     * @returns An array mapping 'pos[aaNumber] = chromosmalPos'
     */
    public synchronized int[] aaNumber2Pos() {
        if (aa2pos != null) return aa2pos;

        calcCdsStartEnd();

        aa2pos = new int[protein().length()];
        for (int i = 0; i < aa2pos.length; i++)
            aa2pos[i] = -1;

        int cdsMin = Math.min(cdsStart, cdsEnd);
        int cdsMax = Math.max(cdsStart, cdsEnd);

        // For each exon, add CDS position to array
        int aaNum = 0;
        int step = isStrandPlus() ? 1 : -1;
        int codonFrame = 0;
        for (Exon exon : sortedStrand()) {
            int min = isStrandPlus() ? exon.getStart() : exon.getEndClosed();

            int aaIdxStart = -1, aaIdxEnd = -1;

            for (int pos = min; exon.intersects(pos) && aaNum < aa2pos.length; pos += step) {
                // Is this within a CDS?
                if ((cdsMin <= pos) && (pos <= cdsMax)) {
                    // Update AA indexes for this exon
                    if (aaIdxStart < 0) aaIdxStart = aaNum;
                    aaIdxEnd = aaNum;

                    // First codon base? Add to map
                    if (codonFrame == 0) aa2pos[aaNum] = pos;

                    // Last base in this codon? Increment AA number
                    if (codonFrame == 2) aaNum++;

                    // Update codon base
                    codonFrame = (codonFrame + 1) % 3;
                }
            }

            // Update exons' AA indexes
            if (aaIdxStart >= 0) exon.setAaIdx(aaIdxStart, aaIdxEnd);
        }

        return aa2pos;
    }

    /**
     * Find a genomic position of the first base in a Amino Acid 'aaNum'
     */
    public int aaNumber2Pos(int aaNum) {
        var aa2pos = aaNumber2Pos();
        if (aaNum < 0 || aaNum > aa2pos.length) return -1;
        return aa2pos[aaNum];
    }

    /**
     * Add a CDS
     */
    public void add(Cds cdsInt) {
        cdss.add(cdsInt);
        cds = null;
    }

    /**
     * Add an intron
     */
    public void add(Intron intron) {
        if (introns == null) introns = new ArrayList<>();
        introns.add(intron);

        // Introns should be sorted by strand
        if (isStrandPlus()) Collections.sort(introns);
        else Collections.sort(introns, Collections.reverseOrder());
    }

    /**
     * Add a SpliceSite
     */
    public void add(SpliceSite spliceSite) {
        for (Exon ex : this)
            if (ex.intersects(spliceSite)) ex.add(spliceSite);

        for (Intron intr : introns())
            if (intr.intersects(spliceSite)) intr.add(spliceSite);
    }

    /**
     * Add a UTR
     */
    public void add(Utr utr) {
        utrs.add(utr);
        cds = null;
    }

    /**
     * Add missing UTRs. See utrFromCds() method.
     */
    boolean addMissingUtrs(Markers missingUtrs, boolean verbose) {
        missingUtrs.sort(false, isStrandMinus());

        // Get min/max CDS positions
        int minCds = Integer.MAX_VALUE;
        int maxCds = 0;
        for (Cds c : cdss) {
            minCds = Math.min(minCds, c.getStart());
            maxCds = Math.max(maxCds, c.getEndClosed());
        }

        if (verbose) {
            System.out.println("Transcript '" + id + "' has missing UTRs." //
                    + " Strand: " + (strandMinus ? "-" : "+") //
                    + " (minCds: " + minCds //
                    + " , maxCds: " + maxCds + "):" //
            );
        }

        // Add intervals
        boolean retVal = false;
        for (Marker mu : missingUtrs) {
            Exon exon = queryExon(mu);
            if (exon == null) throw new RuntimeException("Cannot find exon for UTR: " + mu);
            Utr toAdd = null;

            if (isStrandPlus()) {
                if (mu.getEndClosed() <= minCds)
                    toAdd = new Utr5prime(exon, mu.getStart(), mu.getEndClosed(), strandMinus, mu.getId());
                else if (mu.getStart() >= maxCds)
                    toAdd = new Utr3prime(exon, mu.getStart(), mu.getEndClosed(), strandMinus, mu.getId());
            } else {
                if (mu.getStart() >= maxCds)
                    toAdd = new Utr5prime(exon, mu.getStart(), mu.getEndClosed(), strandMinus, mu.getId());
                else if (mu.getEndClosed() <= minCds)
                    toAdd = new Utr3prime(exon, mu.getStart(), mu.getEndClosed(), strandMinus, mu.getId());
            }

            // OK?
            if (toAdd != null) {
                add(toAdd);
                if (verbose) Log.info("\tAdding " + toAdd);
                retVal = true;
            }
        }

        return retVal;
    }

    /**
     * Adjust transcript coordinates
     */
    public boolean adjust() {
        boolean changed = false;
        int strandSumTr = 0;
        int newStart = Integer.MAX_VALUE;
        int newEnd = Integer.MIN_VALUE;

        int countStrandPlus = 0, countStrandMinus = 0;
        for (Exon exon : sortedStrand()) {
            newStart = Math.min(newStart, exon.getStart());
            newEnd = Math.max(newEnd, exon.getEndClosed());

            // Common exon strand
            if (exon.isStrandPlus()) countStrandPlus++;
            else countStrandMinus++;
        }

        // UTRs
        for (Utr utr : getUtrs()) {
            newStart = Math.min(newStart, utr.getStart());
            newEnd = Math.max(newEnd, utr.getEndClosed());
        }

        // Sanity check
        strandSumTr = countStrandPlus - countStrandMinus; // Some exons have incorrect strands, we use the strand indicated by most exons
        boolean newStrandMinus = strandSumTr < 0;
        if ((countStrandPlus > 0) && (countStrandMinus > 0))
            Log.debug("Transcript '" + id + "' has " + countStrandPlus + " exons on the plus and " + countStrandMinus + " exons on the minus strand! This should never happen!");

        // Change transcript strand?
        if (strandMinus != newStrandMinus) {
            changed = true;
            setStrandMinus(newStrandMinus); // Change strand
        }

        // Changed? Update values
        if (newStart < Integer.MAX_VALUE && newEnd > Integer.MIN_VALUE) {
            // Change start?
            if (getStart() != newStart) {
                setStart(newStart);
                changed = true;
            }

            // Change end?
            if (getEndClosed() != newEnd) {
                setEndClosed(newEnd);
                changed = true;
            }
        }

        return changed;
    }

    /**
     * Create a new transcript after applying changes in variant
     * <p>
     * Note: If this transcript is unaffected, no new transcript is created (same transcript is returned)
     */
    @Override
    public Transcript apply(Variant variant) {
        // Variant after this marker: No effect
        if (!shouldApply(variant)) return this;

        // Create new transcript
        Transcript newTr = (Transcript) super.apply(variant);
        if (newTr == null) return null;

        // Add changed UTRs
        for (Utr utr : utrs) {
            Utr newUtr = (Utr) utr.apply(variant);
            if (newUtr != null) {
                Exon newExon = newTr.findExon(newUtr);

                if (newExon != null) {
                    newUtr.setParent(newExon);
                    newTr.utrs.add(newUtr);
                } else {
                    // This might happen when a duplication affecting part of an exon
                    // E.g. If the duplication affects the coding part and NOT the 3'UTR then the UTR doesn't have a
                    if (Config.get().isDebug())
                        Log.debug("WARNING: applying variant: Could not find 'new' parent exon for 'new' UTR" //
                                + "\n\t\tVariant           : " + variant + "\n" //
                                + "\n\t\tUTR        (ori) :" + utr + "\n" //
                                + "\n\t\tTranscript (ori) :" + this + "\n" //
                                + "\n\t\tUTR        (new) :" + newUtr + "\n" //
                                + "\n\t\tTranscript (new) :" + newTr //
                        );
                }
            }
        }

        // Rank exons: if a variant deletes one or more exons, ranks will change
        newTr.rankExons();

        // Up & Down stream
        newTr.createUpDownStream(upDownLength);

        // Introns
        newTr.introns();

        // Splice sites
        newTr.createSpliceSites(spliceSiteSize, spliceRegionExonSize, spliceRegionIntronMin, spliceRegionIntronMax);

        return newTr;
    }

    /**
     * Find base at genomic coordinate 'pos'
     */
    public String baseAt(int pos) {
        calcCdsStartEnd();
        Exon ex = findExon(pos);
        if (ex == null) return null;
        return ex.basesAt(pos - ex.getStart(), 1);
    }

    /**
     * Calculate distance from transcript start to a position
     * mRNA is roughly the same than cDNA. Strictly speaking mRNA
     * has a poly-A tail and 5'cap.
     */
    public synchronized int baseNumber2MRnaPos(int pos) {
        int count = 0;
        for (Exon eint : sortedStrand()) {
            if (eint.intersects(pos)) {
                // Intersect this exon? Calculate the number of bases from the beginning
                int dist = 0;
                if (isStrandPlus()) dist = pos - eint.getStart();
                else dist = eint.getEndClosed() - pos;

                // Sanity check
                if (dist < 0)
                    throw new RuntimeException("Negative distance for position " + pos + ". This should never happen!\n" + this);

                return count + dist;
            }

            count += eint.size();
        }
        return -1;
    }

    /**
     * Calculate base number in a CDS where 'pos' maps
     *
     * @param usePrevBaseIntron: When 'pos' is intronic this method returns:
     *                           - if( usePrevBaseIntron== false)  => The first base in the exon after 'pos' (i.e. first coding base after intron)
     *                           - if( usePrevBaseIntron== true)   => The last base in the  exon before 'pos'  (i.e. last coding base before intron)
     * @returns Base number or '-1' if it does not map to a coding base
     */
    public synchronized int baseNumberCds(int pos, boolean usePrevBaseIntron) {
        // Doesn't hit this transcript?
        if (!intersects(pos)) return -1;

        // Is it in UTR instead of CDS?
        if (isUtr(pos)) return -1;

        // Calculate cdsStart and cdsEnd (if not already done)
        calcCdsStartEnd();

        // All exons..
        int firstCdsBaseInExon = 0; // Where the exon maps to the CDS (i.e. which CDS base number does the first base in this exon maps to).
        for (Exon eint : sortedStrand()) {
            if (eint.intersects(pos)) {
                int cdsBaseInExon; // cdsBaseInExon: base number relative to the beginning of the coding part of this exon (i.e. excluding 5'UTRs)
                if (isStrandPlus()) cdsBaseInExon = pos - Math.max(eint.getStart(), cdsStart);
                else cdsBaseInExon = Math.min(eint.getEndClosed(), cdsStart) - pos;

                cdsBaseInExon = Math.max(0, cdsBaseInExon);

                return firstCdsBaseInExon + cdsBaseInExon;
            } else {
                // Before exon begins?
                if ((isStrandPlus() && (pos < eint.getStart())) // Before exon begins (positive strand)?
                        || (isStrandMinus() && (pos > eint.getEndClosed()))) // Before exon begins (negative strand)?
                    return firstCdsBaseInExon - (usePrevBaseIntron ? 1 : 0);
            }

            if (isStrandPlus())
                firstCdsBaseInExon += Math.max(0, eint.getEndClosed() - Math.max(eint.getStart(), cdsStart) + 1);
            else firstCdsBaseInExon += Math.max(0, Math.min(cdsStart, eint.getEndClosed()) - eint.getStart() + 1);
        }

        return firstCdsBaseInExon - 1;
    }

    /**
     * Return a codon that includes 'cdsBaseNumber'
     */
    public String baseNumberCds2Codon(int cdsBaseNumber) {
        int codonNum = cdsBaseNumber / CodonChange.CODON_SIZE;
        int min = codonNum * CodonChange.CODON_SIZE;
        int max = codonNum * CodonChange.CODON_SIZE + CodonChange.CODON_SIZE;
        if ((min >= 0) && (max <= cds().length())) return cds().substring(min, max).toUpperCase();
        return null;
    }

    /**
     * Calculate chromosome position as function of CDS number
     *
     * @returns An array mapping 'cds2pos[cdsBaseNumber] = chromosmalPos'
     */
    public synchronized int[] baseNumberCds2Pos() {
        if (cds2pos != null) return cds2pos;

        calcCdsStartEnd();

        cds2pos = new int[cds().length()];
        for (int i = 0; i < cds2pos.length; i++)
            cds2pos[i] = -1;

        int cdsMin = Math.min(cdsStart, cdsEnd);
        int cdsMax = Math.max(cdsStart, cdsEnd);

        // For each exon, add CDS position to array
        int cdsBaseNum = 0;
        for (Exon exon : sortedStrand()) {
            int min = isStrandPlus() ? exon.getStart() : exon.getEndClosed();
            int step = isStrandPlus() ? 1 : -1;
            for (int pos = min; exon.intersects(pos) && cdsBaseNum < cds2pos.length; pos += step)
                if ((cdsMin <= pos) && (pos <= cdsMax)) cds2pos[cdsBaseNum++] = pos;
        }

        return cds2pos;
    }

    public int baseNumberCds2Pos(int cdsBaseNum) {
        if (cds2pos == null) baseNumberCds2Pos();
        if (cdsBaseNum < 0 || cdsBaseNum >= cds2pos.length) return -1;
        return cds2pos[cdsBaseNum];
    }

    /**
     * Calculate CDS start and CDS end
     */
    synchronized void calcCdsStartEnd() {
        // Do we need to calculate these values?
        // Note: In circular genomes, one of cdsStart / cdsEnd might be less
        //       than zero (we must check both)
        if ((cdsStart < 0) && (cdsEnd < 0)) {
            // Calculate coding start (after 5 prime UTR)

            if (utrs.isEmpty()) {
                // No UTRs => Use all exons
                cdsStart = (isStrandPlus() ? getEndClosed() : getStart()); // cdsStart is the position of the first base in the CDS (i.e. the first base after all 5'UTR)
                cdsEnd = (isStrandPlus() ? getStart() : getEndClosed()); // cdsEnd is the position of the last base in the CDS (i.e. the first base before all 3'UTR)

                for (Exon ex : this) {
                    if (isStrandPlus()) {
                        cdsStart = Math.min(cdsStart, ex.getStart());
                        cdsEnd = Math.max(cdsEnd, ex.getEndClosed());
                    } else {
                        cdsStart = Math.max(cdsStart, ex.getEndClosed());
                        cdsEnd = Math.min(cdsEnd, ex.getStart());
                    }
                }
            } else {
                // We have to take into account UTRs
                cdsStart = (isStrandPlus() ? getStart() : getEndClosed()); // cdsStart is the position of the first base in the CDS (i.e. the first base after all 5'UTR)
                cdsEnd = (isStrandPlus() ? getEndClosed() : getStart()); // cdsEnd is the position of the last base in the CDS (i.e. the first base before all 3'UTR)
                int cdsStartNotExon = cdsStart;

                for (Utr utr : utrs) {
                    if (utr instanceof Utr5prime) {
                        if (isStrandPlus()) cdsStart = Math.max(cdsStart, utr.getEndClosed() + 1);
                        else cdsStart = Math.min(cdsStart, utr.getStart() - 1);
                    } else if (utr instanceof Utr3prime) {
                        if (isStrandPlus()) cdsEnd = Math.min(cdsEnd, utr.getStart() - 1);
                        else cdsEnd = Math.max(cdsEnd, utr.getEndClosed() + 1);
                    }
                }

                // Make sure cdsStart and cdsEnd lie within an exon
                if (isStrandPlus()) {
                    cdsStart = firstExonPositionAfter(cdsStart);
                    cdsEnd = lastExonPositionBefore(cdsEnd);
                } else {
                    cdsStart = lastExonPositionBefore(cdsStart);
                    cdsEnd = firstExonPositionAfter(cdsEnd);
                }

                // We were not able to find cdsStart & cdsEnd within exon limits.
                // Probably there is something wrong with the database and the transcript does
                // not have a single coding base (e.g. all of it is UTR).
                if (cdsStart < 0 || cdsEnd < 0) cdsStart = cdsEnd = cdsStartNotExon;
            }
        }
    }

    /**
     * Retrieve coding sequence
     */
    public synchronized String cds() {
        if (cds != null) return cds;

        // Concatenate all exons
        List<Exon> exons = sortedStrand();
        StringBuilder sequence = new StringBuilder();
        int utr5len = 0, utr3len = 0;

        // 5 prime UTR length
        for (Utr utr : get5primeUtrs())
            utr5len += utr.size();

        // Append all exon sequences
        boolean missingSequence = false;
        for (Exon exon : exons) {
            missingSequence |= !exon.hasSequence(); // If there is no sequence, we are in trouble
            sequence.append(exon.getSequence());
        }

        if (missingSequence) cds = ""; // One or more exons does not have sequence. Nothing to do
        else {
            // OK, all exons have sequences

            // 3 prime UTR length
            for (Utr utr : get3primeUtrs())
                utr3len += utr.size();

            // Cut 5 prime UTR and 3 prime UTR points
            int subEnd = sequence.length() - utr3len;

            if (utr5len > subEnd) cds = "";
            else cds = sequence.substring(utr5len, subEnd);
        }

        return cds;
    }

    /**
     * Create a marker of the coding region in this transcript
     */
    public Marker cdsMarker() {
        return isStrandPlus() //
                ? new Marker(this, getCdsStart(), getCdsEnd()) //
                : new Marker(this, getCdsEnd(), getCdsStart()) //
                ;
    }

    @Override
    public Transcript cloneShallow() {
        Transcript clone = (Transcript) super.cloneShallow();

        clone.proteinCoding = proteinCoding;
        clone.canonical = canonical;
        clone.bioType = bioType;
        clone.aaCheck = aaCheck;
        clone.dnaCheck = dnaCheck;
        clone.corrected = corrected;
        clone.ribosomalSlippage = ribosomalSlippage;

        return clone;
    }

    /**
     * Return an array of 3 genomic positions where amino acid number 'aaNum' maps
     *
     * @return aa2pos[0], aa2pos[1], aa2pos[2] are the coordinates (within the chromosome)
     * of the three bases conforming codon 'aaNum'. Any aa2pos[i] = -1 means that
     * it could a base in the codon could not be mapped.
     * <p>
     * Bases in the array are sorted by chromosome position, so aa2pos[0] < aa2pos[1] < aa2pos[2]
     */
    public int[] codonNumber2Pos(int codonNum) {
        if (cds2pos == null) baseNumberCds2Pos();

        // Initialize
        int[] codon = new int[3];
        int step = isStrandPlus() ? 1 : -1;
        int idxStart = isStrandPlus() ? 0 : 2;
        for (int i = idxStart, j = 3 * codonNum; (i < codon.length) && (i >= 0) && j < cds2pos.length; i += step, j++) {
            codon[i] = cds2pos[j];
        }

        return codon;
    }

    /**
     * Collapses exons having gaps of zero (i.e. exons that followed by other exons).
     * Does the same for CDSs and UTRs.
     *
     * @return true of any exon in the transcript was 'collapsed'
     */
    public boolean collapseZeroGap() {
        if (ribosomalSlippage)
            return false; // Overlapping exons are representing ribosomal slippage, so they are not annotations errors and must not be corrected.

        boolean ret = false;
        introns = null; // These need to be recalculated

        //---
        // Collapse Exons
        //---
        Markers markers = new Markers();
        markers.addAll(subIntervals());
        Map<Marker, Marker> collapse = MarkerUtil.collapseZeroGap(markers); // Create a map of collapsed exons

        // Replace exons
        for (Marker exon : collapse.keySet()) {
            Exon collapsedExon = (Exon) collapse.get(exon);

            // Is this exon to be replaced? (i.e. collapseZeroGap returns different coordinates)
            if (exon.size() != collapsedExon.size() //
                    || exon.getStart() != collapsedExon.getStart() //
                    || exon.getEndClosed() != collapsedExon.getEndClosed() //
            ) {
                ret = true;

                // Show debugging information
                if (Config.get().isDebug())
                    System.err.println("\t\t\tTranscript " + getId() + ": Collapsing exon " + exon.getId() + "\t[ " + exon.getStart() + " - " + exon.getEndClosed() + " ]\t=>\t[ " + collapsedExon.getStart() + " - " + collapsedExon.getEndClosed() + " ]");

                // Replace exon
                remove((Exon) exon);
                if (!containsId(collapsedExon.getId()))
                    add(collapsedExon); // Add collapsedExon. Make sure we don't add it twice (since many exons can be collapsed into one).

                // Change parent exon in UTRs
                for (Marker m : getUtrs()) {
                    Utr utr = (Utr) m;
                    if (utr.getParent() == exon) utr.setParent(collapsedExon);
                }
            }
        }

        //---
        // Collapse CDS
        //---
        collapse = MarkerUtil.collapseZeroGap(new Markers(cdss));
        cdss = new ArrayList<>(); // Re-create CDSs list
        Markers uniqCollapsedCds = new Markers(collapse.values()).unique(); // Create a set of unique CDSs and add them to CDSs list
        for (Marker cds : uniqCollapsedCds)
            cdss.add((Cds) cds);

        //---
        // Collapse UTRs
        //---
        collapse = MarkerUtil.collapseZeroGap(new Markers(utrs));
        Markers uniqCollapsedUtrs = new Markers(collapse.values()).unique(); // Create a set of unique UTRs, and add them to the list
        utrs = new ArrayList<>(); // Re-generate UTRs list
        for (Marker utr : uniqCollapsedUtrs)
            utrs.add((Utr) utr);

        return ret;
    }

    /**
     * Calculate CpG bias: number of CpG / expected[CpG]
     */
    public double cpgExonBias() {
        ObservedOverExpectedCpG oe = new ObservedOverExpectedCpG();
        return oe.oe(this);
    }

    /**
     * Count total CpG in this transcript's exons
     */
    public int cpgExons() {
        ObservedOverExpectedCpG oe = new ObservedOverExpectedCpG();
        return oe.observed(this);
    }

    /**
     * Find all splice sites.
     */
    public void createSpliceSites(int spliceSiteSize, int spliceRegionExonSize, int spliceRegionIntronMin, int spliceRegionIntronMax) {
        this.spliceSiteSize = spliceSiteSize;
        this.spliceRegionExonSize = spliceRegionExonSize;
        this.spliceRegionIntronMin = spliceRegionIntronMin;
        this.spliceRegionIntronMax = spliceRegionIntronMax;

        // Create spliceSiteRegion on the Exon side
        ArrayList<Exon> exons = (ArrayList<Exon>) sortedStrand();
        if (exons.size() > 0) {
            for (int i = 0; i < exons.size(); i++) { // Iterate like this to check rank
                Exon exon = exons.get(i);

                if (i > 0) exon.createSpliceSiteRegionStart(spliceRegionExonSize); // Splice site region at the start
                if (i < (exon.size() - 1))
                    exon.createSpliceSiteRegionEnd(spliceRegionExonSize); // Splice site region at the end

                // Sanity check
                int rank = i + 1;
                if (exon.getRank() != rank) {
                    String msg = "Rank numbers do not march: " + rank + " != " + exon.getRank() + "\n\tTranscript: " + this;
                    throw new RuntimeException(msg);
                }
            }
        }

        // Create spliceSite (donor/acceptor) and spliceSiteRegion on the Intron side
        List<Intron> introns = introns();
        if (introns != null) {
            for (int i = 0; i < introns.size(); i++) {
                Intron intron = introns.get(i);
                intron.createSpliceSiteAcceptor(spliceSiteSize); // Acceptor splice site
                intron.createSpliceSiteDonor(spliceSiteSize); // Acceptor splice site

                // Splice region
                intron.createSpliceSiteRegionStart(spliceRegionIntronMin, spliceRegionIntronMax);
                intron.createSpliceSiteRegionEnd(spliceRegionIntronMin, spliceRegionIntronMax);
            }
        }
    }

    /**
     * Creates a list of UP/DOWN stream regions (for each transcript)
     * Upstream (downstream) stream is defined as upDownLength before (after) transcript
     */
    public void createUpDownStream(int upDownLength) {
        this.upDownLength = upDownLength;

        Chromosome chr = getChromosome();
        int chrMin = chr.getStart(), chrMax = chr.getEndClosed();

        // Create up/down stream intervals and add them to the list
        int beforeStart = Math.max(getStart() - upDownLength, chrMin);
        int beforeEnd = Math.max(getStart() - 1, chrMin);
        int afterStart = Math.min(getEndClosed() + 1, chrMax);
        int afterEnd = Math.min(getEndClosed() + upDownLength, chrMax);

        if (isStrandPlus()) {
            if (beforeStart < beforeEnd) upstream = new Upstream(this, beforeStart, beforeEnd, false, id);
            if (afterStart < afterEnd) downstream = new Downstream(this, afterStart, afterEnd, false, id);
        } else {
            if (afterStart < afterEnd) upstream = new Upstream(this, afterStart, afterEnd, false, id);
            if (beforeStart < beforeEnd) downstream = new Downstream(this, beforeStart, beforeEnd, false, id);
        }
    }

    /**
     * Deletes redundant exons (i.e. exons that are totally included in other exons).
     * Does the same for CDSs.
     * Does the same for UTRs.
     */
    public boolean deleteRedundant() {
        boolean ret = false;
        introns = null; // These need to be recalculated

        //---
        // Delete redundant exons
        //---
        Map<Marker, Marker> includedIn = MarkerUtil.redundant(subIntervals());
        for (Marker exon : includedIn.keySet()) {
            ret = true;
            remove((Exon) exon);

            // Change parent exon in UTRs
            for (Marker m : getUtrs()) {
                Utr utr = (Utr) m;
                if (utr.getParent() == exon) utr.setParent(includedIn.get(exon));
            }
        }

        //---
        // Delete redundant CDS
        //---
        includedIn = MarkerUtil.redundant(cdss);
        for (Marker cds : includedIn.keySet())
            cdss.remove(cds);

        //---
        // Delete redundant UTRs
        //---
        includedIn = MarkerUtil.redundant(utrs);
        for (Marker utr : includedIn.keySet())
            utrs.remove(utr);

        return ret;
    }

    /**
     * Find a CDS that matches exactly the exon
     */
    public Cds findCds(Exon exon) {
        for (Cds cds : cdss)
            if (exon.includes(cds)) return cds;
        return null;
    }

    /**
     * Return the an exon that intersects 'pos'
     */
    public Exon findExon(int pos) {
        for (Exon exon : this)
            if (exon.intersects(pos)) return exon;
        return null;
    }

    /**
     * Return an exon intersecting 'marker' (first exon found)
     */
    public Exon findExon(Marker marker) {
        for (Exon exon : this)
            if (exon.intersects(marker)) return exon;
        return null;
    }

    /**
     * Return an intron overlapping position 'pos'
     */
    public Intron findIntron(int pos) {
        // Is 'pos' in intron?
        for (Intron intron : introns())
            if (intron.intersects(pos)) return intron;
        return null;
    }

    /**
     * Return the UTR that hits position 'pos'
     *
     * @return An UTR intersecting 'pos' (null if not found)
     */
    public Utr findUtr(int pos) {
        // Is it in UTR?
        for (Utr utr : utrs)
            if (utr.intersects(pos)) return utr;
        return null;
    }

    /**
     * Return the UTR that intersects 'marker' (null if not found)
     */
    public List<Utr> findUtrs(Marker marker) {
        List<Utr> utrs = new LinkedList<>();

        // Is it in UTR instead of CDS?
        for (Utr utr : utrs)
            if (utr.intersects(marker)) utrs.add(utr);

        return utrs.isEmpty() ? null : utrs;
    }

    /**
     * Find the first position after 'pos' within an exon
     */
    int firstExonPositionAfter(int pos) {
        for (Exon ex : sorted()) {
            if (pos <= ex.getStart()) return ex.getStart();
            if (pos <= ex.getEndClosed()) return pos;
        }

        Log.warning(ErrorWarningType.WARNING_EXON_NOT_FOUND, "WARNING: Cannot find first exonic position after " + pos + " for transcript '" + id + "'");
        return -1;
    }

    /**
     * Correct exons based on frame information.
     * <p>
     * E.g. if the frame information (form a genomic
     * database file, such as a GTF) does not
     * match the calculated frame, we correct exon's
     * boundaries to make them match.
     * <p>
     * This is performed in two stages:
     * i) First exon is corrected by adding a fake 5'UTR
     * ii) Other exons are corrected by changing the start (or end) coordinates.
     */
    public synchronized boolean frameCorrection() {
        // Copy frame information form CDSs to Exons (if missing)
        frameFromCds();

        // First exon is corrected by adding a fake 5'UTR
        boolean changedFirst = frameCorrectionFirstCodingExon();

        // Other exons are corrected by changing the start (or end) coordinates.
        // boolean changedNonFirst = false;
        boolean changedNonFirst = frameCorrectionNonFirstCodingExon();

        boolean changed = changedFirst || changedNonFirst;

        // We have to reset cached CDS data if anything changed
        if (changed) {
            resetCache();
            corrected = true;
        }

        // Return true if there was any adjustment
        return changed;
    }

    /**
     * Fix transcripts having non-zero frames in first exon
     * <p>
     * Transcripts whose first exon has a non-zero frame indicate problems.
     * We add a 'fake' UTR5 to compensate for reading frame.
     */
    synchronized boolean frameCorrectionFirstCodingExon() {
        List<Exon> exons = sortedStrand();

        // No exons? Nothing to do
        if ((exons == null) || exons.isEmpty()) return false;

        Exon exonFirst = getFirstCodingExon(); // Get first exon
        if (exonFirst == null) return false;
        if (exonFirst.getFrame() <= 0) return false; // Frame OK (or missing), nothing to do

        // First exon is not zero? => Create a UTR5 prime to compensate
        Utr5prime utr5 = null;
        int frame = exonFirst.getFrame();

        if (isStrandPlus()) {
            int end = exonFirst.getStart() + (frame - 1);
            utr5 = new Utr5prime(exonFirst, exonFirst.getStart(), end, isStrandMinus(), exonFirst.getId());
        } else {
            int start = exonFirst.getEndClosed() - (frame - 1);
            utr5 = new Utr5prime(exonFirst, start, exonFirst.getEndClosed(), isStrandMinus(), exonFirst.getId());
        }

        if (Config.get().isDebug())
            Log.debug("Frame correction for first coding exon: Added 5'UTR to compensate frame=" + frame + ", new UTR: " + utr5);
        // Reset frame, since it was already corrected
        exonFirst.setFrame(0);
        Cds cds = findCds(exonFirst);
        if (cds != null) cds.frameCorrection(cds.getFrame());

        // Add UTR5'
        add(utr5);

        // Update counter
        return true;
    }

    /**
     * Correct exons according to frame information
     */
    synchronized boolean frameCorrectionNonFirstCodingExon() {
        boolean corrected = false;

        // Concatenate all exons to create a CDS
        List<Exon> exons = sortedStrand();
        StringBuilder sequence = new StringBuilder();

        // We don't need to correct if there is no sequence!
        // Note: The problem only exists due to sequence frame
        //       If there is no sequence we cannot do functional predictions anyway
        for (Exon exon : exons)
            if (!exon.hasSequence()) return false;

        // Create an interval spanning all 5'UTRs
        int utr5Start = Integer.MAX_VALUE, utr5End = -1;
        for (Utr utr : get5primeUtrs()) {
            utr.size();
            utr5Start = Math.min(utr5Start, utr.getStart());
            utr5End = Math.max(utr5End, utr.getEndClosed());
        }
        Marker utr5 = utr5End >= 0 ? new Marker(this, utr5Start, utr5End, strandMinus, "") : null;

        // Append all exon sequences
        for (Exon exon : exons) {
            String seq = "";
            int utrOverlap = 0;
            boolean utrIncluded = false;

            // Check if exon overlaps UTR
            if (utr5 != null && utr5.includes(exon)) {
                // The whole exon is included => No sequence change
                seq = "";
                utrIncluded = true;
            } else {
                // Add sequence
                seq = exon.getSequence();
                if (utr5 != null && utr5.intersects(exon)) {
                    utrOverlap = utr5.intersectSize(exon);
                    if (utrOverlap > 0) {
                        if (utrOverlap < seq.length()) seq = seq.substring(utrOverlap);
                        else seq = "";
                    }
                }
            }

            //---
            // Frame check
            //---
            if (exon.getFrame() < 0) {
                // Nothing to do (assume current frame is right)
            } else if (utrIncluded) {
                // Exon does not code, frame information is meaningless
                if (exon.getFrame() >= 0) exon.setFrame(-1);
            } else {
                // Calculate frame
                // We use GFF style frame calculation
                // References: http://mblab.wustl.edu/GTF22.html
                int frameReal = FrameType.GFF.frameFromLength(sequence.length());

                // Does calculated frame match?
                if (frameReal != exon.getFrame()) {
                    if (utrOverlap > 0) {
                        throw new RuntimeException("Fatal Error: First exon needs correction: This should never happen!"//
                                + "\n\tThis method is supposed to be called AFTER method"//
                                + "\n\tSnpEffPredictorFactory.frameCorrectionFirstCodingExon(), which"//
                                + "\n\tshould have taken care of this problem." //
                                + "\n\t" + this //
                        );
                    }

                    if (Config.get().isDebug()) {
                        Log.debug("Frame correction: " //
                                + "Transcript '" + getId() + "'" //
                                + " " + toStr() //
                                + ", exon rank: " + exon.getRank() //
                                + ", expected frame: " + frameReal //
                                + ", exon frame: " + exon.getFrame() //
                                + ", sequence len: " + sequence.length() //
                        );
                    }

                    // Find matching CDS
                    Cds cdsToCorrect = findCds(exon);

                    // Correct exon until we get the expected frame
                    for (boolean ok = true; ok && frameReal != exon.getFrame(); ) {
                        // Correct both Exon and CDS
                        ok &= exon.frameCorrection(1);
                        if (cdsToCorrect != null) cdsToCorrect.frameCorrection(1);
                        corrected = true;
                    }

                    // Get new exon's sequence
                    seq = exon.getSequence();
                }
            }

            // Append sequence
            sequence.append(seq);
        }

        return corrected;
    }

    /**
     * Copy frame info from CDSs into Exons
     */
    void frameFromCds() {
        for (Exon ex : this) {
            // No frame info? => try to find matching CDS
            if (ex.getFrame() < 0) {
                // Check a CDS that matches an exon
                for (Cds cds : getCds()) {
                    // CDS matches the exon coordinates? => Copy frame info
                    if (isStrandPlus() && (ex.getStart() == cds.getStart())) {
                        ex.setFrame(cds.getFrame());
                        break;
                    } else if (isStrandMinus() && (ex.getEndClosed() == cds.getEndClosed())) {
                        ex.setFrame(cds.getFrame());
                        break;
                    }
                }
            }
        }

    }

    /**
     * Create a list of 3 prime UTRs
     */
    public List<Utr3prime> get3primeUtrs() {
        ArrayList<Utr3prime> list = new ArrayList<>();
        for (Utr utr : utrs)
            if (utr instanceof Utr3prime) list.add((Utr3prime) utr);
        return list;
    }

    public List<Utr3prime> get3primeUtrsSorted() {
        List<Utr3prime> list = get3primeUtrs();
        Collections.sort(list);
        return list;
    }

    /**
     * Create a list of 5 prime UTRs
     */
    public List<Utr5prime> get5primeUtrs() {
        ArrayList<Utr5prime> list = new ArrayList<>();
        for (Utr utr : utrs)
            if (utr instanceof Utr5prime) list.add((Utr5prime) utr);
        return list;
    }

    public List<Utr5prime> get5primeUtrsSorted() {
        List<Utr5prime> list = get5primeUtrs();
        Collections.sort(list);
        return list;
    }

    public BioType getBioType() {
        return bioType;
    }

    public void setBioType(BioType bioType) {
        this.bioType = bioType;
    }

    /**
     * Get all CDSs
     */
    public List<Cds> getCds() {
        return cdss;
    }

    public int getCdsEnd() {
        calcCdsStartEnd();
        return cdsEnd;
    }

    public int getCdsStart() {
        calcCdsStartEnd();
        return cdsStart;
    }

    public Downstream getDownstream() {
        return downstream;
    }

    /**
     * A more intuitive name for 'subintervals'
     */
    public Collection<Exon> getExons() {
        return subIntervals();
    }

    /**
     * Get first coding exon
     */
    public synchronized Exon getFirstCodingExon() {
        if (firstCodingExon == null) {
            // Get transcription start position
            long cstart = getCdsStart();

            // Pick exon intersecting cdsStart (TSS)
            for (Exon exon : sortedStrand())
                if (exon.intersects(cstart)) firstCodingExon = exon;

            // Sanity check
            if (firstCodingExon == null) {
                Gene g = (Gene) getParent();
                Log.warning(ErrorWarningType.WARNING_EXON_NOT_FOUND, "Cannot find first coding exon for transcript '" + getId() + "', gene name '" + (g != null ? g.getGeneName() : "") + "', gene ID '" + (g != null ? g.getId() : "") + "'");
            }
        }
        return firstCodingExon;
    }

    public Gene getGene() {
        return (Gene) findParent(Gene.class);
    }

    public TranscriptSupportLevel getTranscriptSupportLevel() {
        return transcriptSupportLevel;
    }

    public void setTranscriptSupportLevel(TranscriptSupportLevel transcriptSupportLevel) {
        this.transcriptSupportLevel = transcriptSupportLevel;
    }

    /**
     * Create a TSS marker
     */
    public Marker getTss() {
        calcCdsStartEnd();
        Marker tss = new Marker(this, getStart() + (isStrandPlus() ? 0 : -1), getStart() + (isStrandPlus() ? 1 : 0), false, "TSS_" + id);
        return tss;
    }

    public Upstream getUpstream() {
        return upstream;
    }

    /**
     * Get all UTRs
     */
    public List<Utr> getUtrs() {
        return utrs;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean hasCds() {
        return cdss != null && !cdss.isEmpty();
    }

    /**
     * Does this transcript have any errors?
     */
    public boolean hasError() {
        return isErrorProteinLength() || isErrorStartCodon() || isErrorStopCodonsInCds();
    }

    /**
     * Does this transcript have any errors?
     */
    public boolean hasErrorOrWarning() {
        return hasError() || hasWarning();
    }

    public boolean hasTranscriptSupportLevelInfo() {
        return (transcriptSupportLevel != null) && (transcriptSupportLevel != TranscriptSupportLevel.TSL_NA);
    }

    /**
     * Does this transcript have any errors?
     */
    public boolean hasWarning() {
        return isWarningStopCodon() // All possible warnings
                ;
    }

    /**
     * Get all introns (lazy init)
     */
    public synchronized List<Intron> introns() {
        if (introns == null) {
            introns = new ArrayList<>();

            Exon exBefore = null;
            for (Exon ex : sortedStrand()) {
                if (exBefore != null) {
                    // Create intron
                    Intron intron;
                    int rank = introns.size() + 1;

                    // Find intron start and end
                    int start, end;
                    if (isStrandPlus()) {
                        start = exBefore.getEndClosed() + 1;
                        end = ex.getStart() - 1;
                    } else {
                        start = ex.getEndClosed() + 1;
                        end = exBefore.getStart() - 1;
                    }

                    int size = end - start + 1;
                    if (size > 0) {
                        // Add intron to list
                        intron = new Intron(this, start, end, strandMinus, id + "_intron_" + rank, exBefore, ex);
                        intron.setRank(rank);
                        introns.add(intron);
                    }
                }

                exBefore = ex;
            }
        }
        return introns;
    }

    public boolean isAaCheck() {
        return aaCheck;
    }

    public void setAaCheck(boolean aaCheck) {
        this.aaCheck = aaCheck;
    }

    @Override
    protected boolean isAdjustIfParentDoesNotInclude(Marker parent) {
        return true;
    }

    public boolean isCanonical() {
        return canonical;
    }

    public void setCanonical(boolean canonical) {
        this.canonical = canonical;
    }

    /**
     * Is this variant in the CDS part of this transcript?
     */
    boolean isCds(Variant variant) {
        calcCdsStartEnd();

        int cs = cdsStart;
        int ce = cdsEnd;

        if (isStrandMinus()) {
            cs = cdsEnd;
            ce = cdsStart;
        }

        return (variant.getEndClosed() >= cs) && (variant.getStart() <= ce);
    }

    /**
     * Has this transcript been checked against CDS/DNA/AA sequences?
     */
    public boolean isChecked() {
        return aaCheck || dnaCheck;
    }

    public boolean isCorrected() {
        return corrected;
    }

    public boolean isDnaCheck() {
        return dnaCheck;
    }

    public void setDnaCheck(boolean dnaCheck) {
        this.dnaCheck = dnaCheck;
    }

    public boolean isDownstream(int pos) {
        return downstream != null && downstream.intersects(pos);
    }

    /**
     * Check if coding length is multiple of 3 in protein coding transcripts
     *
     * @return true on Error
     */
    public boolean isErrorProteinLength() {
        if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;
        return (cds().length() % 3) != 0;
    }

    /**
     * Is the first codon a START codon?
     */
    public boolean isErrorStartCodon() {
        if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;

        // Not even one codon in this protein? Error
        String cds = cds();
        if (cds.length() < 3) return true;

        String codon = cds.substring(0, 3);
        return !codonTable().isStart(codon);
    }

    /**
     * Check if protein sequence has STOP codons in the middle of the coding sequence
     *
     * @return true on Error
     */
    public boolean isErrorStopCodonsInCds() {
        if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;

        // Get protein sequence
        String prot = protein();
        if (prot == null) return false;

        // Any STOP codon before the end?
        char[] bases = prot.toCharArray();
        int max = bases.length - 1;
        int countErrs = 0;
        for (int i = 0; i < max; i++)
            if (bases[i] == '*') {
                countErrs++;
                // We allow up to one STOP codon because it can be a RARE_AMINO_ACID which is coded as a STOP codon.
                // More than one STOP codon is not "normal", so it's probably an error in the genomic annotations (e.g. ENSEMBL or UCSC)
                if (countErrs > 1) return true;
            }

        // OK
        return false;
    }

    public boolean isIntron(int pos) {
        return findIntron(pos) != null;
    }

    public boolean isProteinCoding() {
        return proteinCoding;
    }

    public void setProteinCoding(boolean proteinCoding) {
        this.proteinCoding = proteinCoding;
    }

    public boolean isRibosomalSlippage() {
        return ribosomalSlippage;
    }

    public void setRibosomalSlippage(boolean ribosomalSlippage) {
        this.ribosomalSlippage = ribosomalSlippage;
    }

    public boolean isUpstream(int pos) {
        return upstream != null && upstream.intersects(pos);
    }

    public boolean isUtr(int pos) {
        return findUtr(pos) != null;
    }

    public boolean isUtr(Marker marker) {
        return findUtrs(marker) != null;
    }

    public boolean isUtr3(int pos) {
        Utr utr = findUtr(pos);
        return utr != null && utr instanceof Utr3prime;
    }

    public boolean isUtr5(int pos) {
        Utr utr = findUtr(pos);
        return utr != null && utr instanceof Utr5prime;
    }

    /**
     * Is the last codon a STOP codon?
     */
    public boolean isWarningStopCodon() {
        if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;

        // Not even one codon in this protein? Error
        String cds = cds();
        if (cds.length() < 3) return true;

        String codon = cds.substring(cds.length() - 3);
        return !codonTable().isStop(codon);
    }

    /**
     * Find the last position before 'pos' within an exon
     */
    int lastExonPositionBefore(int pos) {
        int last = -1;
        for (Exon ex : sorted()) {
            if (pos < ex.getStart()) {
                // Nothing found?
                if (last < 0) {
                    System.err.println("WARNING: Cannot find last exonic position before " + pos + " for transcript '" + id + "'");
                    return -1;
                }
                return last;
            } else if (pos <= ex.getEndClosed()) return pos;
            last = ex.getEndClosed();
        }

        if (last < 0)
            System.err.println("WARNING: Cannot find last exonic position before " + pos + " for transcript '" + id + "'");
        return pos;
    }

    /**
     * A list of all markers in this transcript
     */
    @Override
    public Markers markers() {
        Markers markers = new Markers();
        markers.addAll(subIntervals());
        markers.addAll(utrs);
        markers.addAll(cdss);
        markers.add(upstream);
        markers.add(downstream);
        markers.addAll(introns());
        return markers;
    }

    /**
     * Retrieve coding sequence AND the UTRs (mRNA = 5'UTR + CDS + 3'UTR)
     * I.e. Concatenate all exon sequences
     */
    public synchronized String mRna() {
        if (mRna != null) return mRna;

        List<Exon> exons = sortedStrand();

        // Concatenate all exons
        StringBuilder sequence = new StringBuilder();
        for (Exon ex : exons)
            sequence.append(ex.getSequence());

        mRna = sequence.toString();
        return mRna;
    }

    /**
     * Protein sequence (amino acid sequence produced by this transcripts)
     */
    public String protein() {
        if (protein == null) {
            if (!(Config.get() != null && Config.get().isTreatAllAsProteinCoding()) && !isProteinCoding()) protein = "";
            else protein = codonTable().aa(cds(), true);
        }
        return protein;
    }

    /**
     * Query all genomic regions that intersect 'marker'
     */
    @Override
    public Markers query(Marker marker) {
        Set<Marker> results = new HashSet<>();

        // Add exons
        for (Exon ex : this)
            if (ex.intersects(marker)) {
                results.add(ex);

                // Query deeper
                for (Marker ee : ex.query(marker))
                    results.add(ee);
            }

        // Ad UTRs
        for (Utr u : utrs)
            if (u.intersects(marker)) results.add(u);

        // Add CDSs
        for (Cds m : cdss)
            if (m.intersects(marker)) results.add(m);

        // Add introns
        for (Intron intr : introns())
            if (intr.intersects(marker)) {
                results.add(intr);

                // Query deeper
                for (Marker ee : intr.query(marker))
                    results.add(ee);
            }

        // Get results into markers
        Markers markers = new Markers();
        markers.addAll(results);

        return markers;
    }

    /**
     * Return the first exon that intersects 'interval' (null if not found)
     */
    public Exon queryExon(Marker interval) {
        for (Exon ei : this)
            if (ei.intersects(interval)) return ei;
        return null;
    }

    /**
     * Assign ranks to exons
     */
    public boolean rankExons() {
        boolean changed = false;
        int rank = 1;
        for (Exon exon : sortedStrand()) {
            if (rank != exon.getRank()) {
                exon.setRank(rank);
                changed = true;
            }
            rank++;
        }
        return changed;
    }

    @Override
    public void reset() {
        super.reset();

        utrs = new ArrayList<>();
        cdss = new ArrayList<>();
        introns = null;
        upstream = null;
        downstream = null;
        resetCache();
    }

    public void resetCache() {
        cdsStart = -1;
        cdsEnd = -1;
        firstCodingExon = null;
        cds = null;
        cds2pos = null;
        aa2pos = null;
        mRna = null;
        protein = null;
    }

    public void resetExons() {
        super.reset();
        resetCache();
    }

    /**
     * Perfom some baseic chekcs, return error type, if any
     */
    public ErrorWarningType sanityCheck(Variant variant) {
        if (isErrorStopCodonsInCds()) return ErrorWarningType.WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS;
        if (isErrorProteinLength()) return ErrorWarningType.WARNING_TRANSCRIPT_INCOMPLETE;
        if (isErrorStartCodon()) return ErrorWarningType.WARNING_TRANSCRIPT_NO_START_CODON;
        if (isWarningStopCodon()) return ErrorWarningType.WARNING_TRANSCRIPT_NO_STOP_CODON;
        return null;
    }

    /**
     * Parse a line from a serialized file
     */
    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        super.serializeParse(markerSerializer);
        bioType = BioType.parse(markerSerializer.getNextField());
        proteinCoding = markerSerializer.getNextFieldBoolean();
        dnaCheck = markerSerializer.getNextFieldBoolean();
        aaCheck = markerSerializer.getNextFieldBoolean();
        corrected = markerSerializer.getNextFieldBoolean();
        ribosomalSlippage = markerSerializer.getNextFieldBoolean();
        transcriptSupportLevel = TranscriptSupportLevel.parse(markerSerializer.getNextField());
        version = markerSerializer.getNextField();

        upstream = (Upstream) markerSerializer.getNextFieldMarker();
        downstream = (Downstream) markerSerializer.getNextFieldMarker();

        for (Marker m : markerSerializer.getNextFieldMarkers())
            utrs.add((Utr) m);

        for (Marker m : markerSerializer.getNextFieldMarkers())
            cdss.add((Cds) m);
    }

    /**
     * Create a string to serialize to a file
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return super.serializeSave(markerSerializer) //
                + "\t" + bioType //
                + "\t" + proteinCoding //
                + "\t" + dnaCheck //
                + "\t" + aaCheck //
                + "\t" + corrected //
                + "\t" + ribosomalSlippage //
                + "\t" + (transcriptSupportLevel == null ? "" : transcriptSupportLevel.toString()) //
                + "\t" + version //
                + "\t" + markerSerializer.save(upstream) //
                + "\t" + markerSerializer.save(downstream) //
                + "\t" + markerSerializer.save((Iterable) utrs)//
                + "\t" + markerSerializer.save((Iterable) cdss)//
                ;
    }

    public void sortCds() {
        Collections.sort(cdss);
        resetCache();
    }

    public List<SpliceSite> spliceSites() {
        List<SpliceSite> sslist = new ArrayList<>();

        for (Exon ex : this)
            sslist.addAll(ex.getSpliceSites());

        for (Intron intr : introns())
            sslist.addAll(intr.getSpliceSites());

        return sslist;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    public String toString(boolean full) {
        StringBuilder sb = new StringBuilder();

        sb.append(getChromosomeName() + ":" + getStart() + "-" + getEndClosed());
        sb.append(", strand: " + (isStrandPlus() ? "+" : "-"));
        if ((id != null) && (id.length() > 0)) sb.append(", id:" + id);
        if ((bioType != null) && (bioType != null)) sb.append(", bioType:" + bioType);
        if (isProteinCoding()) sb.append(", Protein");
        if (isAaCheck()) sb.append(", AA check");
        if (isDnaCheck()) sb.append(", DNA check");

        if (numChilds() > 0) {
            sb.append("\n");
            for (Utr utr : get5primeUtrsSorted())
                sb.append("\t\t5'UTR   :\t" + utr + "\n");

            sb.append("\t\tExons:\n");
            for (Exon exon : sorted())
                sb.append("\t\t" + exon + "\n");

            for (Utr utr : get3primeUtrsSorted())
                sb.append("\t\t3'UTR   :\t" + utr + "\n");

            // We may show CDS
            if (isProteinCoding()) {
                sb.append("\t\tCDS     :\t" + cds() + "\n");
                sb.append("\t\tProtein :\t" + protein() + "\n");
            }
        }

        if (full) {
            // Show errors or warnings
            String warn = "";
            if (isErrorStopCodonsInCds()) warn += ErrorWarningType.WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS + " ";
            if (isErrorProteinLength()) warn += ErrorWarningType.WARNING_TRANSCRIPT_INCOMPLETE + " ";
            if (isErrorStartCodon()) warn += ErrorWarningType.WARNING_TRANSCRIPT_NO_START_CODON + " ";
            if (isWarningStopCodon()) warn += ErrorWarningType.WARNING_TRANSCRIPT_NO_STOP_CODON + " ";
            if (!warn.isEmpty()) sb.append("\tWarnings  :" + warn);

            // Sequence checks
            sb.append("\t\tCDS check : " + (isDnaCheck() ? "OK" : "Failed (or missing)") + "\n");
            sb.append("\t\tAA check  : " + (isAaCheck() ? "OK" : "Failed (or missing)") + "\n");
        }

        return sb.toString();
    }

    /**
     * Show a transcript as an ASCII Art
     */
    public String toStringAsciiArt(boolean full) {
        //---
        // ASCII art for transcript
        //---
        char[] art = new char[size()];
        for (int i = getStart(), j = 0; i <= getEndClosed(); i++, j++) {
            Utr utr = findUtr(i);
            if (utr != null) art[j] = utr.isUtr5prime() ? '5' : '3';
            else {
                Exon exon = findExon(i);
                if (exon != null) {
                    art[j] = exon.isStrandPlus() ? '>' : '<';
                } else art[j] = '-';
            }
        }

        // Only 'basic' ASCII art?
        if (!full) return new String(art);

        //---
        // DNA Sequence
        //---
        StringBuilder seq = new StringBuilder();
        for (int i = getStart(); i <= getEndClosed(); i++) {
            Exon exon = findExon(i);
            if (exon != null) {
                String s = exon.getSequence().toLowerCase();
                if (exon.isStrandMinus()) s = GprSeq.reverseWc(s);

                s = GprSeq.padN(s, exon.size());
                seq.append(s);
                i += s.length() - 1;
            } else seq.append('.');
        }

        //---
        // AA Sequence and frame
        //---
        StringBuilder aa = new StringBuilder();
        StringBuilder frameSb = new StringBuilder();
        StringBuilder pos1sb = new StringBuilder();
        StringBuilder pos10sb = new StringBuilder();
        StringBuilder pos100sb = new StringBuilder();
        StringBuilder pos1000sb = new StringBuilder();

        int pos = getStart();
        if (isProteinCoding()) {
            char[] codon = new char[3];
            int step = isStrandPlus() ? 1 : -1;
            int frame = 0;
            for (int i = (isStrandPlus() ? 0 : art.length - 1), j = 0; (i >= 0) && (i < art.length); i += step) {
                if (art[i] == '3' || art[i] == '5') {
                    // 5'UTR or 3'UTR
                    aa.append(' ');
                    frameSb.append(' ');
                } else {
                    char b = seq.charAt(i);
                    if (b == 'a' || b == 'c' || b == 'g' || b == 't') {
                        // Coding sequence
                        codon[j++] = b;
                        if (j >= 3) {
                            j = 0;
                            String cod = new String(codon);
                            if (isStrandMinus())
                                cod = GprSeq.wc(cod); // Bases are already reversed, we only need WC complement

                            boolean translateStart = (aa.length() <= 0); // First codon? Translate start coddon accordingly
                            aa.append(" " + codonTable().aa(cod, translateStart) + " ");
                        }

                        // Update frame
                        frameSb.append(frame);
                        frame = (frame + 1) % 3;

                    } else {
                        // Intron
                        aa.append(' ');
                        frameSb.append(' ');
                    }
                }

                // Position
                pos1sb.append(pos % 10);

                if (pos % 10 == 0) pos10sb.append((pos % 100) / 10);
                else pos10sb.append(' ');

                if (pos % 100 == 0) pos100sb.append((pos % 1000) / 100);
                else pos100sb.append(' ');

                if (pos % 1000 == 0) pos1000sb.append((pos % 10000) / 1000);
                else pos1000sb.append(' ');

                pos++;
            }
        }

        String aaStr = isStrandPlus() ? aa.toString() : aa.reverse().toString();
        String frameStr = isStrandPlus() ? frameSb.toString() : frameSb.reverse().toString();

        //---
        // Coordinates
        //---

        // Create 'vertical lines'
        StringBuilder lines = new StringBuilder();
        if (isStrandPlus()) lines.append(' ');
        int prev = getStart();
        for (Exon ex : this.sorted()) {
            lines.append(Gpr.repeat(' ', ex.getStart() - prev - 1) + "|");
            prev = ex.getStart();

            lines.append(Gpr.repeat(' ', ex.getEndClosed() - prev - 1) + "|");
            prev = ex.getEndClosed();
        }

        StringBuilder coords = new StringBuilder();
        coords.append(lines + "\n");

        ArrayList<Exon> exons = new ArrayList<>();
        exons.addAll(subIntervals());
        Collections.sort(exons, new IntervalComparatorByStart(true)); // Sort by reverse position

        int n, len;
        for (Exon ex : exons) {
            // Right-side coordinate
            n = ex.getEndClosed();
            len = n - getStart() + 1;
            coords.append((len > 0 ? lines.subSequence(0, len) : "") + "^" + n + "\n");

            // Left-side coordinate
            n = ex.getStart();
            len = n - getStart() + 1;
            coords.append((len > 0 ? lines.subSequence(0, len) : "") + "^" + n + "\n");
        }

        // Result
        return "" //
                + (pos1000sb.toString().trim().isEmpty() ? "" : "\n" + pos1000sb) //
                + (pos100sb.toString().trim().isEmpty() ? "" : "\n" + pos100sb) //
                + (pos10sb.toString().trim().isEmpty() ? "" : "\n" + pos10sb) //
                + "\n" + pos1sb //
                + "\n" + seq //
                + (isProteinCoding() ? "\n" + aaStr + "\n" + frameStr : "") //
                + "\n" + new String(art) //
                + "\n" + coords //
                ;
    }

    /**
     * Calculate UTR regions from CDSs
     */
    public boolean utrFromCds(boolean verbose) {
        if (cdss.size() <= 0) return false; // Cannot do this if we don't have CDS information

        // All exons minus all UTRs and CDS should give us the missing UTRs
        Markers exons = new Markers();
        Markers minus = new Markers();

        // Add all exons
        for (Exon e : this)
            exons.add(e);

        // Add all UTRs and CDSs to the 'minus' set
        for (Utr uint : getUtrs())
            minus.add(uint);

        for (Cds cint : cdss)
            minus.add(cint);

        Markers missingUtrs = exons.minus(minus); // Perform interval minus
        if (!missingUtrs.isEmpty())
            return addMissingUtrs(missingUtrs, verbose); // Anything left? => There was a missing UTR
        return false;
    }

    /**
     * Get some details about the effect on this transcript
     */
    @Override
    public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false; // Sanity check

        // Large structural variant including the whole transcript?
        if (variant.includes(this) && variant.isStructural()) {
            CodonChange codonChange = CodonChange.factory(variant, this, variantEffects);
            codonChange.codonChange();
            return true;
        }

        //---
        // Structural variants may affect more than one exon
        //---
        boolean mayAffectSeveralExons = variant.isStructural() || variant.isMixed() || variant.isMnp();
        if (mayAffectSeveralExons) {
            int countExon = 0;
            for (Exon ex : this)
                if (ex.intersects(variant)) countExon++;

            // More than one exon?
            if (countExon > 1) {
                CodonChange codonChange = CodonChange.factory(variant, this, variantEffects);
                codonChange.codonChange();
                return true;
            }
        }

        //---
        // Does it hit an exon?
        // Note: This only adds spliceSites effects, for detailed codon
        //       changes effects we use 'CodonChange' class
        //---
        boolean exonAnnotated = false;
        for (Exon ex : this)
            if (ex.intersects(variant)) {
                exonAnnotated |= ex.variantEffect(variant, variantEffects);
            }

        //---
        // Hits a UTR region?
        //---
        boolean included = false;
        for (Utr utr : utrs)
            if (utr.intersects(variant)) {
                // Calculate the effect
                utr.variantEffect(variant, variantEffects);
                included |= utr.includes(variant); // Is this variant fully included in the UTR?
            }
        if (included) return true; // Variant fully included in the UTR? => We are done.

        //---
        // Does it hit an intron?
        //---
        for (Intron intron : introns())
            if (intron.intersects(variant)) {
                intron.variantEffect(variant, variantEffects);
                included |= intron.includes(variant); // Is this variant fully included in this intron?
            }
        if (included) return true; // Variant fully included? => We are done.

        //---
        // No annotations from exons? => Add transcript
        //---
        if (!exonAnnotated) {
            variantEffects.add(variant, this, EffectType.TRANSCRIPT, "");
            return true;
        }

        return exonAnnotated;
    }

}
