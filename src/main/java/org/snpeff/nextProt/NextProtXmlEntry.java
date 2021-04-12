package org.snpeff.nextProt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;

class Identifier {
	String type, database, category, id;

	public Identifier(Attributes attributes) {
		type = attributes.getValue("type");
		database = attributes.getValue("database");
		category = attributes.getValue("category");
	}

	@Override
	public String toString() {
		return "Identifier type: '" + type + ", database: '" + database + "', id: '" + id + "'";
	}
}

/**
 * Mimics the 'entry' in a NextProt XML file
 *
 * @author Pablo Cingolani
 *
 */
public class NextProtXmlEntry extends NextProtXmlNode {

	Map<String, NextProtXmlIsoform> isoformsByAccession;
	Set<Identifier> identifiers;
	Identifier currentIdentifier;
	List<NextProtXmlAnnotation> annotations;

	public NextProtXmlEntry(String accession) {
		super(accession);
		isoformsByAccession = new HashMap<>();
		identifiers = new HashSet<>();
		annotations = new ArrayList<>();
	}

	/**
	 * Add an annotation
	 */
	public void add(NextProtXmlAnnotation ann) {
		annotations.add(ann);
	}

	/**
	 * Add an Isoform sequence
	 */
	public void addIsoformSequence(String isoformAccession, String sequence) {
		getOrCreateIsoform(isoformAccession).setSequence(sequence);
	}

	public List<NextProtXmlAnnotation> getAnnotations() {
		return annotations;
	}

	public Set<Identifier> getIdentifiers() {
		return identifiers;
	}

	/**
	 * Get or create an isoform
	 */
	public NextProtXmlIsoform getIsoform(String isoformAccession) {
		return isoformsByAccession.get(isoformAccession);
	}

	public Map<String, NextProtXmlIsoform> getIsoformsByAccession() {
		return isoformsByAccession;
	}

	public NextProtXmlIsoform getOrCreateIsoform(String isoformAccession) {
		var iso = isoformsByAccession.get(isoformAccession);
		if (iso == null) {
			iso = new NextProtXmlIsoform(isoformAccession);
			isoformsByAccession.put(isoformAccession, iso);
		}
		return iso;
	}

	/**
	 * Identifier XML tag closed
	 */
	void identifierEnd(String id) {
		currentIdentifier.id = id;
		identifiers.add(currentIdentifier);
		currentIdentifier = null;
	}

	/**
	 * Identifier XML tag started
	 */
	void identifierStart(Attributes attributes) {
		currentIdentifier = new Identifier(attributes);
	}

	/**
	 * Create all Markers for this entry
	 */
	public void markers(NextProtMarkerFactory markersFactory) {
		for (var a : annotations)
			a.markers(markersFactory);
	}

	@Override
	public String toString() {
		var sb = new StringBuilder();
		sb.append("Entry: '" + accession + "':\n");
		sb.append("\tIsoforms: " + isoformsByAccession.size() + "\n");
		for (String isoId : isoformsByAccession.keySet())
			sb.append("\t\t" + isoformsByAccession.get(isoId) + "\n");

		sb.append("\tIdentifiers: " + identifiers.size() + "\n");
		for (Identifier i : identifiers)
			sb.append("\t\t" + i + "\n");

		sb.append("\tAnnotations: " + annotations.size() + "\n");
		for (NextProtXmlAnnotation a : annotations)
			sb.append("\t\t" + a.toString("\t\t") + "\n");

		return sb.toString();
	}

}
