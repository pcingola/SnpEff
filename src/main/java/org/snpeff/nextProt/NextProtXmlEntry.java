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

	List<NextProtXmlAnnotation> annotations;
	Identifier currentIdentifier;
	NextProtHandler handler;
	Set<Identifier> identifiers;
	Map<String, NextProtXmlIsoform> isoformsByAccession;

	public NextProtXmlEntry(String accession, NextProtHandler handler) {
		super(accession);
		isoformsByAccession = new HashMap<>();
		identifiers = new HashSet<>();
		annotations = new ArrayList<>();
		this.handler = handler;
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

	/**
	 * Create all Markers for this entry
	 */
	public void addMarkers(NextProtMarkerFactory markersFactory) {
		for (var a : annotations)
			a.addMarkers(markersFactory);
	}

	public List<NextProtXmlAnnotation> getAnnotations() {
		return annotations;
	}

	public NextProtHandler getHandler() {
		return handler;
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
