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
    Map<String, Transcript> trById;
    NextProtSequenceConservation sequenceConservation;

    public NextProtMarkerFactory(Config config) {
        this.config = config;
        genome = config.getGenome();
        trById = new HashMap<>();
        sequenceConservation = new NextProtSequenceConservation();
        markers = new Markers();
        addTranscripts();
    }

    /**
     * Create markers and add them
     *
     * @return New markers created
     */
    public Markers addMarkers(NextProtXmlEntry entry, NextProtXmlIsoform isoform, NextProtXmlAnnotation annotation, Location location, String trId) {
        if (isoform.getSequence() == null) {
            Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Isoform '" + isoform.getAccession() + "' has no sequence");
            return null;
        }

        // Find protein coding transcript
        var tr = trById.get(trId);
        if (tr == null) {
            Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + trId + "' not found");
            return null;
        }

        // Sanity check: Compare protein sequence
        if (!isProteinMatch(tr, isoform, location)) return null;

        if (count++ % 1000 == 0)
            Log.debug("ANNOTATION: " + annotation.name() + "\t" + trId + "\t" + location.begin + "\t" + location.end);

        // Analysis of sequence conservation
        addConservation(isoform, annotation, location, tr);

        // Add NextProt annotation
        Markers newmarkers = createNextProt(tr, annotation, location);

        // Add all new markers
        markers.add(newmarkers);

        return newmarkers;
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

    /**
     * Create a list of NextProt markers according to this annotation
     */
    Markers createNextProt(Transcript tr, NextProtXmlAnnotation annotation, Location location) {
        // Get chromosome location
        int start = -1, end = -1;
        if (tr.isStrandPlus()) {
            start = tr.aaNumber2Pos(location.begin);
            end = tr.aaNumber2Pos(location.end + 1) - 1;
        } else {
            end = tr.aaNumber2Pos(location.begin);
            start = tr.aaNumber2Pos(location.end + 1) - 1;
        }

        // TODO: Marker needs to be split across exon junction
        Markers newmarkers = new Markers();

        if (location.isInteraction()) {
            // Interaction
            NextProt nextProtStart = new NextProt(tr, start, start, annotation.accession, annotation.name());
            NextProt nextProtEnd = new NextProt(tr, end, end, annotation.accession, annotation.name());
            newmarkers.add(nextProtStart);
            newmarkers.add(nextProtEnd);
        } else {
            // Convert from AA number to genomic coordinates
            NextProt nextProt = new NextProt(tr, start, end, annotation.accession, annotation.name());
            newmarkers.add(nextProt);
        }

        return newmarkers;
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
            Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + tr.getId() + "' is not protein coding");
            return false;
        }

        var aaSeqTr = tr.protein();
        if (aaSeqTr == null || aaSeqTr.isBlank()) {
            Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Could not find protein sequence for transcript '" + tr.getId() + "'");
            return false;
        }

        aaSeqTr = proteinSequenceCleanup(aaSeqTr);
        if (aaSeqTr.isBlank()) {
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
            Log.error("Amino acid coordinates error, transcript '" + tr.getId() + "', location [" + location.begin + ", " + location.end + "], for protein length " + aaSeqTr.length());
            return false;
        }

        // Compare protein sequences at 'location'
        aaSeqIso = aaSeqIso.substring(aaStart, aaEnd).toUpperCase();
        aaSeqTr = aaSeqTr.substring(aaStart, aaEnd).toUpperCase();
        if (!aaSeqIso.equals(aaSeqTr)) {
            Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, //
                    "Transcript '" + tr.getId() + "' protein sequence does not match isform '" + isoform.getAccession() + "' at [" + aaStart + ", " + aaEnd + "]\n" //
                            + GprSeq.showMismatch(tr.protein(), isoform.getSequence(), "\t") //
            );
            return false;
        }

        return true;
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
