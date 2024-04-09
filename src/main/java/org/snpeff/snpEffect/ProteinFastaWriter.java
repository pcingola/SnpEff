package org.snpeff.snpEffect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.util.Log;

/**
 * Writes protein sequences to a FASTA file
 *
 * @author Pablo Cingolani
 */
public class ProteinFastaWriter {
	String fileName;
	BufferedWriter writer;
	Set<String> transcriptIDsReferenceDone; // Set of transcript IDs, whose protein sequence has been written
	Set<String> proteinSequenceDone; // Protein sequences that have has been written to the FASTA file
	boolean noRef; // Do not write protein sequences for reference transcripts
	boolean verbose;

	public ProteinFastaWriter(String fileName, boolean noRef, boolean verbose) {
		this.fileName = fileName;
		this.verbose = verbose;
		this.noRef = noRef;
		transcriptIDsReferenceDone = new HashSet<>();
		proteinSequenceDone = new HashSet<>();

		// Initialize protein fasta output file
		if ((new File(fileName)).delete() && verbose) {
			Log.warning("Deleted protein fasta output file '" + fileName + "'");
		}
		try {
			writer = new BufferedWriter(new FileWriter(fileName, true));
		} catch (IOException e) {
			throw new RuntimeException("Error trying to open Protein Fasta output file '" + fileName + "'", e);
		}
	}

	/** Close file */
	public void close() {
		if(writer != null) {
			try {
				writer.close();
			} catch (IOException e) {
				throw new RuntimeException("Error closing Protein FASTA output file '" + fileName + "'", e);
			}
		}
	}

	/** Cut protein sequence after first STOP codon */
	String proteinSequence(Transcript tr) {
		var sequence = tr.protein();
		CodonTable codonTable = tr.codonTable();
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < sequence.length(); i++) {
			char aa = sequence.charAt(i);
			if(codonTable.isStopAaSymbol(aa)) break;
			sb.append(aa);
		}
		return sb.toString();
	}

	/**
	 * Append ALT protein sequence to fasta protein file
	 */
	public void write(Variant var, VariantEffects variantEffects) {
		Set<String> doneTr = new HashSet<>(); // Transcript IDs that have already been processed for this specific variant Effects
		for (VariantEffect varEff : variantEffects) {
			Transcript tr = varEff.getTranscript();
			if (tr == null || doneTr.contains(tr.getId())) continue;

			// Calculate sequence after applying variant
			Transcript trAlt = tr.apply(var);

			// Build fasta entries and append to file
			StringBuilder sb = new StringBuilder();

			// Add protein sequence for transcript reference, if not already added in a previous entry (skip if 'noRef' is set)
			var proteinSequenceRef = proteinSequence(tr);
			if (!noRef && !transcriptIDsReferenceDone.contains(tr.getId())) {
				sb.append(">" + tr.getId() //
					+ (tr.getGene() != null ? ", gene: " + tr.getGene().getGeneName() : "") //
					+ (tr.getProteinId() != null ? ", protein_id: " + tr.getProteinId() : "") //
					+ ", reference" //
					+ "\n" //
					+ proteinSequenceRef //
					+ "\n" //
				);
			}
			proteinSequenceDone.add(proteinSequenceRef); // Always add the reference protein sequence so we avoid writing changes that have no effect on the refrence protein sequence

			// Add protein sequence for transcript variant, if the sequence has not already been added in a previous entry
			var proteinSequenceAlt = proteinSequence(trAlt);
			if( ! proteinSequenceDone.contains(proteinSequenceAlt) ) {
				sb.append(">" + tr.getId() //
						+ (tr.getGene() != null ? ", gene: " + tr.getGene().getGeneName() : "") //
						+ (tr.getProteinId() != null ? ", protein_id: " + tr.getProteinId() : "") //
						+ ", variant:q " //
						+ var.getChromosomeName() //
						+ ":" + (var.getStart() + 1) //
						+ "-" + (var.getEnd() + 1) //
						+ ", ref:'" + var.getReference() + "'" //
						+ ", alt:'" + var.getAlt() + "'" //
						+ ", HGVS.p: " + varEff.getHgvsProt() //
						+ "\n" //
						+ proteinSequenceAlt + "\n" //
				);
				proteinSequenceDone.add(proteinSequenceAlt);
			}

			// Write fasta entries
			try {
				writer.write(sb.toString());
			} catch (IOException e) {
				throw new RuntimeException("Error while trying to write to Protein Fasta file '" + fileName + "'", e);
			}

			// Update transcript IDs
			transcriptIDsReferenceDone.add(tr.getId());
			doneTr.add(tr.getId());
		}
	}
}
