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

	Config config;
	Genome genome;
	Markers markers;
	Map<String, Transcript> trById;

	public NextProtMarkerFactory(Config config) {
		this.config = config;
		genome = config.getGenome();
		trById = new HashMap<String, Transcript>();
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

	/**
	 * Are the AA sequences from transcript and Isoform equal?
	 */
	boolean isProteinMatch(Transcript tr, NextProtXmlIsoform isoform) {
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
		if (aaSeqTr.isBlank()) return false; // Nothing left after cleanup?

		var aaSeqIso = isoform.getSequence();
		if (aaSeqIso == null || aaSeqIso.isBlank()) return false;

		int minLen = Math.min(aaSeqTr.length(), aaSeqIso.length());
		aaSeqIso = aaSeqIso.substring(0, minLen);
		aaSeqTr = aaSeqTr.substring(0, minLen);

		var match = aaSeqIso.equals(aaSeqTr);
		if (!match) Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + tr.getId() + "' protein sequence does not match isform '" + isoform.getAccession() + "'\n" + GprSeq.showMismatch(tr.protein(), isoform.getSequence(), "\t"));
		return match;
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
		if (!isProteinMatch(tr, isoform)) {
			Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Transcript '" + tr.getId() + "' protein sequence does not match isform '" + isoform.getAccession() + "'\n\t" + tr.protein() + "\n\t" + isoform.getSequence());
			return null;
		}

		// Get AA coordinates
		var aaSeq = tr.protein();
		int aaStart = location.begin;
		int aaEnd = location.end;
		// Check AA coordinates
		if (aaStart > aaEnd || aaStart < 0 || aaEnd >= aaSeq.length()) {
			Log.error("Amino acid coordinates error, AA coordinates [" + aaStart + ", " + aaEnd + "] for protein length " + aaSeq.length());
			return null;
		}

		var aaSubSeq = aaSeq.substring(aaStart, aaEnd + 1);

		// Convert from AA number to genomic coordinates
		int start = tr.aaNumber2Pos(aaStart);
		int end = tr.aaNumber2Pos(aaEnd);
		Log.debug("ANNOTATION: " + annotation.getCategory() + "\t" + trId + "\t" + aaStart + "\t" + aaEnd + "\t'" + aaSubSeq + "'");

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
