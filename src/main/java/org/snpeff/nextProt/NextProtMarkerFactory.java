package org.snpeff.nextProt;

import org.snpeff.interval.*;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Creates Markers from nextprot XML annotations
 *
 * @author Pablo Cingolani
 */
public class NextProtMarkerFactory {
    int count;
    Config config;
    Genome genome;
    Markers markers;
    NextProtSequenceConservation sequenceConservation;
    Map<String, Transcript> trById;
    boolean verbose;

    public NextProtMarkerFactory(Config config) {
        this.config = config;
        genome = config.getGenome();
        trById = new HashMap<>();
        sequenceConservation = new NextProtSequenceConservation();
        markers = new Markers();
        addTranscripts();
        verbose = Config.get().isVerbose();
    }

    /**
     * Create markers and add them
     *
     * @return New markers created
     */
    public Markers addMarkers(NextProtXmlEntry entry, NextProtXmlIsoform isoform, NextProtXmlAnnotation annotation, Location location, String trId) {
        if (isoform.getSequence() == null) {
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Isoform '" + isoform.getAccession() + "' has no sequence");
            return null;
        }

        // Find protein coding transcript
        var tr = trById.get(trId);
        if (tr == null) {
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + trId + "' not found");
            return null;
        }

        // Sanity check: Compare protein sequence
        if (!isProteinMatch(tr, isoform, location)) return null;

        if (verbose && count++ % 1000 == 0)
            Log.info("ANNOTATION: " + annotation.name() + "\t" + trId + "\t" + location.begin + "\t" + location.end);

        // Analysis of sequence conservation
        addConservation(isoform, annotation, location, tr);

        // Create and add all nextProt markers
        markers.add(nextProt(tr, annotation, location));

        return markers;
    }

    /**
     * Add annotation for conservation analysis
     */
    void addConservation(NextProtXmlIsoform isoform, NextProtXmlAnnotation annotation, Location location, Transcript tr) {
        int aaStart = location.begin;
        int aaEnd = location.end;
        int start = tr.aaNumber2Pos(aaStart);
        int end = tr.aaNumber2Pos(aaEnd);

        if (location.isInteraction()) {
            // In this case we need to add both sides of the interaction separately
            var seq = isoform.getSequence();
            var aaSubSeq1 = seq.substring(aaStart, aaStart + 1);
            var aaSubSeq2 = seq.substring(aaEnd, aaEnd + 1);
            var name = annotation.name();
            sequenceConservation.add(name, aaSubSeq1);
            sequenceConservation.add(name, aaSubSeq2);
        } else if (location.isIsoform()) {
            // Add the sequences of AAs in the [start, end] interval
            var aaSubSeq = isoform.getSequence().substring(aaStart, aaEnd + 1);
            var name = annotation.name();
            sequenceConservation.add(name, aaSubSeq);
        }
    }

    /**
     * Add a transcript by ID mapping
     */
    void addTr(Transcript tr) {
        var trId = tr.getId();
        trById.put(trId, tr);

        // Remove transcript version (if any)
        if (trId.indexOf('.') > 0) {
            trId = trId.split("\\.")[0];
            trById.put(trId, tr);
        }
    }

    /**
     * Build transcript map
     */
    void addTranscripts() {
        for (Gene gene : genome.getGenes())
            for (Transcript tr : gene)
                addTr(tr);
    }

    /**
     * Sequence conservations analysis.
     * Tag highly conserved NextProt markers
     */
    public void conservation() {
        sequenceConservation.analyzeSequenceConservation(markers);
    }

    public Markers getMarkers() {
        return markers;
    }

    /**
     * Are the AA sequences from transcript and Isoform equal?
     */
    boolean isProteinMatch(Transcript tr, NextProtXmlIsoform isoform, Location location) {
        // Check transcript protein sequence
        if (!tr.isProteinCoding()) {
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + tr.getId() + "' is not protein coding");
            return false;
        }

        var aaSeqTr = tr.protein();
        if (aaSeqTr == null || aaSeqTr.isBlank()) {
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Could not find protein sequence for transcript '" + tr.getId() + "'");
            return false;
        }

        aaSeqTr = proteinSequenceCleanup(aaSeqTr);
        if (aaSeqTr.isBlank()) {
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Empty protein sequence after cleanup,  transcript '" + tr.getId() + "'");
            return false; // Nothing left after cleanup?
        }

        // Check isoform sequence
        var aaSeqIso = isoform.getSequence();
        if (aaSeqIso == null || aaSeqIso.isBlank()) return false;

        // Check that 'location' is within transcript
        int aaStart = location.begin;
        int aaEnd = location.end;
        if (aaStart > aaEnd || aaStart < 0 || aaEnd >= aaSeqTr.length()) {
            if (verbose)
                Log.warning("Amino acid coordinates error, transcript '" + tr.getId() + "', location [" + location.begin + ", " + location.end + "], for protein length " + aaSeqTr.length());
            return false;
        }

        // Compare protein sequences at 'location'
        aaSeqIso = aaSeqIso.substring(aaStart, aaEnd).toUpperCase();
        aaSeqTr = aaSeqTr.substring(aaStart, aaEnd).toUpperCase();
        if (!aaSeqIso.equals(aaSeqTr)) {
            if (verbose)
                Log.warning("Transcript '" + tr.getId() + "' protein sequence does not match isform '" + isoform.getAccession() + "' at [" + aaStart + ", " + aaEnd + "]\n" //
                        + GprSeq.showMismatch(tr.protein(), isoform.getSequence(), "\t") //
                );
            return false;
        }

        return true;
    }

    /**
     * Create a list of NextProt markers according to this annotation
     */
    public Markers nextProt(Transcript tr, NextProtXmlAnnotation annotation, Location location) {
        if (location.isInteraction()) {
            // Interaction, we need to add two (sets or) markers, one on each side of the interaction (i.e. location.begin and location.end)
            Markers nextprotMarkers = new Markers();
            nextprotMarkers.add(nextProt(tr, annotation.accession, annotation.name(), location.begin, location.begin));
            nextprotMarkers.add(nextProt(tr, annotation.accession, annotation.name(), location.end, location.end));
            return nextprotMarkers;
        } else {
            return nextProt(tr, annotation.accession, annotation.name(), location.begin, location.end);
        }
    }

    /**
     * Create a single NextProt marker
     */
    public Markers nextProt(Transcript tr, String accession, String name, int aaStart, int aaEnd) {
        int start = -1, end = -1;

        // Find the start and end coordiantes from AA numbers
        if (tr.isStrandPlus()) { // Plus strand
            start = tr.codonNumber2Pos(aaStart)[0]; // Start codon's left-most base
            end = tr.codonNumber2Pos(aaEnd)[2]; // End codon's right-most base
        } else { // Minus strand
            start = tr.codonNumber2Pos(aaEnd)[0]; // End codon's left-most base
            end = tr.codonNumber2Pos(aaStart)[2]; // Start codon's right-most base
        }
        // Create an interval
        Marker marker = new Marker(tr.getChromosome(), start, end);

        // The interval could span multiple exons, create one marker for each exon it intersects
        Markers exons = new Markers();
        exons.addAll(tr.getExons());
        var exonsIntersected = exons.intersect(marker);
        Markers nextProtMarkers = new Markers();
        // Create one nextProt marker for each intersection with a different exon
        for (Marker m : exonsIntersected)
            nextProtMarkers.add(new NextProt(tr, m.getStart(), m.getEndClosed(), accession, name));
        return nextProtMarkers;
    }

    /**
     * Clean up protein sequence:  Remove trailing stop or unknown codons ('*' / '?')
     */
    String proteinSequenceCleanup(String protein) {
        boolean change;
        do {
            change = false;
            var lastAa = (!protein.isEmpty() ? protein.charAt(protein.length() - 1) : ' ');

            if (lastAa == '*' || lastAa == '?') {
                protein = protein.substring(0, protein.length() - 1);
                change = true;
            }
        } while (change);

        return protein;
    }

    /**
     * Save nextprot markers as databases
     */
    public void saveDatabase() {
        String nextProtBinFile = config.getDirDataGenomeVersion() + "/nextProt.bin";
        if (config.isVerbose()) Log.info("Saving database to file '" + nextProtBinFile + "'");

        // Add chromosomes
        HashSet<Chromosome> chromos = new HashSet<>();
        for (Marker m : markers)
            chromos.add(m.getChromosome());

        // Create a set of all markers to be saved
        Markers markersToSave = new Markers();
        markersToSave.add(genome);
        for (Chromosome chr : chromos)
            markersToSave.add(chr);
        for (Marker m : markers)
            markersToSave.add(m);

        // Save
        markersToSave.save(nextProtBinFile);
    }
}
