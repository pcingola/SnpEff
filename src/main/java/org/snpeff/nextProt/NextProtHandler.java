package org.snpeff.nextProt;

import java.util.Stack;

import org.snpeff.stats.CountByType;
import org.snpeff.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler used in XML parsing for NextProt database
 *
 * It keeps track of the tags and saves state data to create Markers using NextProtMarkerFactory
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtHandler extends DefaultHandler {

	NextProtXmlAnnotation annotation; // Current annotation
	String annotationCategory; // Annotation category
	CvTerm cvTerm; // Controlled vocabulary term
	NextProtXmlEntry entry; // Current NextProt entry
	String isoformAccession; // Latest iso-form sequence accession
	NextProtMarkerFactory markersFactory;
	CountByType missingCategories;
	Stack<String> stack; // Stack of XML entries
	StringBuilder text; // Latest XML entry text

	public NextProtHandler(NextProtMarkerFactory markersFactory) {
		this.markersFactory = markersFactory;
		stack = new Stack<>();
		missingCategories = new CountByType();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		text.append(ch, start, length);
	}

	public void countMissingCategory(String category) {
		missingCategories.inc(category);
	}

	/**
	 * Parse XML's element end
	 */

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		var qNamePop = stack.pop();
		if (!qNamePop.equals(qName)) Log.info("Stack does not match: '" + qNamePop + "' != '" + qName + "'");

		switch (qName) {
		case "annotation":
			if (annotation != null && !annotation.isEmpty()) entry.add(annotation);
			annotation = null;
			break;

		case "annotation-category":
			annotationCategory = null;
			break;

		case "cv-term":
			if (annotation != null && !annotation.hasCvTerm() && cvTerm != null && level().equals("annotation")) {
				cvTerm.description = text.toString();
				annotation.setCvTerm(cvTerm);
			}
			cvTerm = null;
			break;

		case "description":
			if (annotation != null && level().equals("annotation") && annotation.getDescription() == null) annotation.setDescription(text.toString());
			break;

		case "entry":
			if (entry != null) entry.addMarkers(markersFactory);
			entry = null;
			break;

		case "identifier":
			entry.identifierEnd(text.toString());
			break;

		case "isoform-mapping":
			isoformAccession = null;
			break;

		case "isoform-sequence":
			entry.addIsoformSequence(isoformAccession, text.toString());
			isoformAccession = null;
			break;

		case "location":
			if (annotation != null) annotation.locationEnd();
			break;
		}
	}

	public CountByType getMissingCategories() {
		return missingCategories;
	}

	String level() {
		return stack.lastElement();
	}

	/**
	 * Parse XML's element start
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		text = new StringBuilder();
		stack.push(qName);

		var accession = attributes.getValue("accession");

		switch (qName) {
		case "annotation":
			if (entry != null) annotation = new NextProtXmlAnnotation(entry, annotationCategory);
			break;

		case "annotation-category":
			annotationCategory = attributes.getValue("category");
			break;

		case "begin":
			if (annotation != null) annotation.locationBeginPos(attributes);
			break;

		case "cv-term":
			if (annotation != null) cvTerm = new CvTerm(attributes);
			break;

		case "end":
			if (annotation != null) annotation.locationEndPos(attributes);
			break;

		case "entry":
			entry = new NextProtXmlEntry(accession, this);
			break;

		case "identifier":
			entry.identifierStart(attributes);
			break;

		case "isoform-mapping":
		case "isoform-sequence":
			isoformAccession = accession;
			break;

		case "location":
			if (annotation != null) annotation.locationStart(attributes);
			break;

		case "target-isoform":
			if (annotation != null) annotation.locationIsoformStart(accession);
			break;

		case "transcript-mapping":
			entry.getOrCreateIsoform(isoformAccession).addTranscriptMapping(attributes);
			break;

		case "transcript-protein":
			entry.getIsoform(isoformAccession).addProteinMapping(attributes);
			break;

		}
	}

	/**
	 * Convert attributes to a string
	 */
	String toString(Attributes attributes) {
		var sb = new StringBuilder();
		for (int i = 0; i < attributes.getLength(); i++) {
			String aname = attributes.getQName(i);
			String aval = attributes.getValue(i);
			String atype = attributes.getType(i);
			sb.append(aname + " (" + atype + ") = '" + aval + "', ");
		}
		return sb.toString();
	}

	/**
	 * Convert stack to a string
	 */
	String toStringStack() {
		var sb = new StringBuilder();
		for (String s : stack)
			sb.append((s.length() > 0 ? " -> " : "") + s);
		return sb.toString();
	}
}
