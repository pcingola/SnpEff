package org.snpeff.nextProt;

import java.util.HashSet;
import java.util.Set;

import org.xml.sax.Attributes;

/**
 * Mimics the 'isoform-mapping' in a NextProt XML file
 *
 * @author Pablo Cingolani
 *
 */
public class NextProtXmlIsoform extends NextProtXmlNode {

	String sequence;
	Set<String> transcriptIds; // Transcript IDs this entry maps to
	Set<String> proteinIds; // Protein IDs this entry maps to

	public NextProtXmlIsoform(String accession) {
		super(accession);
		transcriptIds = new HashSet<String>();
		proteinIds = new HashSet<String>();
	}

	/**
	 * Add a transcript mapping
	 */
	public void addProteinMapping(Attributes attributes) {
		var accession = attributes.getValue("accession");
		proteinIds.add(accession);
	}

	/**
	 * Add a protein mapping
	 */
	public void addTranscriptMapping(Attributes attributes) {
		var accession = attributes.getValue("accession");
		transcriptIds.add(accession);
	}

	public Set<String> getProteinIds() {
		return proteinIds;
	}

	public String getSequence() {
		return sequence;
	}

	public Set<String> getTranscriptIds() {
		return transcriptIds;
	}

	/**
	 * Add an Isoform sequence
	 */
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	@Override
	public String toString() {
		return "Isoform '" + accession + "'" //
				+ ", transcripts: " + transcriptIds //
				+ ", proteins: " + proteinIds //
				+ ", sequence: " + sequence //
		;
	}

}
