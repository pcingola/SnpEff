package org.snpeff.nextProt;

import java.util.Stack;

import org.snpeff.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse NetxProt XML file and build a database
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtHandler extends DefaultHandler {

	StringBuilder text; // Latest XML entry text
	Stack<String> stack; // Stack of XML entries
	NextProtXmlEntry entry; // Current nextprot entry
	String isoformAccession; // Latest isoform sequence accesssion
	NextProtXmlAnnotation annotation; // Current annotation
	NextProtMarkerFactory markersFactory;

	public NextProtHandler(NextProtMarkerFactory markersFactory) {
		this.markersFactory = markersFactory;
		stack = new Stack<>();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		text.append(ch, start, length);
	}

	/**
	 * Parse XML's element end
	 */

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		var qNamePop = stack.pop();
		if (!qNamePop.equals(qName)) Log.info("Stack does not match: '" + qNamePop + "' != '" + qName + "'");

		switch (qName) {
		case "annotation-category":
			if (!annotation.isEmpty()) entry.add(annotation);
			annotation = null;
			break;

		case "entry":
			entry.markers(markersFactory);
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

	/**
	 * Parse XML's element start
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		text = new StringBuilder();
		stack.push(qName);

		var accession = attributes.getValue("accession");

		switch (qName) {
		case "annotation-category":
			var category = attributes.getValue("category");
			annotation = new NextProtXmlAnnotation(entry, category);
			break;

		case "begin":
			if (annotation != null) annotation.locationBeginPos(attributes);
			break;

		case "end":
			if (annotation != null) annotation.locationEndPos(attributes);
			break;

		case "entry":
			entry = new NextProtXmlEntry(accession);
			break;

		case "identifier":
			entry.identifierStart(attributes);
			break;

		case "isoform-mapping":
		case "isoform-sequence":
			isoformAccession = accession;
			break;

		case "location":
			if (annotation != null) annotation.locationStart();
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
