package org.snpeff.nextProt;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

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
		trById = new HashMap<String, Transcript>();
		sequenceConservation = new NextProtSequenceConservation();
		addTranscripts();
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

	public void conservation() {
		sequenceConservation.analyzeSequenceConservation();
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
			Log.error("Amino acid coordinates error, transcript '" + tr.getId() + "', location " + location + ", for protein length " + aaSeqTr.length());
			return false;
		}

		// Compare protein sequences at 'location'
		int minLen = Math.min(aaSeqTr.length(), aaSeqIso.length());
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
	 * Create a marker
	 * @return A NextProt marker
	 */
	public Markers markers(NextProtXmlEntry entry, NextProtXmlIsoform isoform, NextProtXmlAnnotation annotation, Location location, String trId) {
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
		if (!isProteinMatch(tr, isoform, location)) {
			//			Log.debug("Cannot add annotation: " + annotation //
			//					+ "\nTranscript '" + tr.getId() + "' protein sequence does not match isform '" + isoform.getAccession() + "'at " + location + "\n" //
			//					+ GprSeq.showMismatch(tr.protein(), isoform.getSequence(), "\t") //
			//			);
			return null;
		}

		// Convert from AA number to genomic coordinates
		int aaStart = location.begin;
		int aaEnd = location.end;
		int start = tr.aaNumber2Pos(aaStart);
		int end = tr.aaNumber2Pos(aaEnd);
		var aaSubSeq = isoform.getSequence().substring(aaStart, aaEnd + 1);

		if (count++ % 1000 == 0) Log.debug("ANNOTATION: " + annotation.getCategory() + "\t" + trId + "\t" + aaStart + "\t" + aaEnd + "\t'" + aaSubSeq + "'");

		sequenceConservation.add(annotation.name(), aaSubSeq);

		// TODO: Specialized NextProt annotation
		NextProt nextProt = new NextProt(tr, start, end, annotation.category);

		// TODO: Marker needs to be split across exon junction

		return null;
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
