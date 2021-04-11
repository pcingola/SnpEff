package org.snpeff.nextProt;

import java.util.List;
import java.util.Stack;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

// A location
class Location {
	int begin, end;

	Location() {
		this(-1, -1);
	}

	Location(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}
}

class LocationTargetIsoform extends Location {
	String accession;

	LocationTargetIsoform(String accession) {
		super();
		this.accession = accession;
	}
}

/**
 * Parse NetxProt XML file and build a database
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtHandler extends DefaultHandler {

	StringBuilder stringBuilder;
	Stack<String> stack;
	String nextProtAccession; // Latest nextprot accession
	String geneId, transcriptId, proteinId;
	List<Location> locations;
	Location location;

	public NextProtHandler() {
		stack = new Stack<>();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		stringBuilder.append(ch, start, length);
	}

	/**
	 * Parse XML's element end
	 */

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		//		Log.info("END qName: " + toStringStack() + "\n\tlen: " + stringBuilder.length() + "\n\t" + stringBuilder);
		String qNamePop = stack.pop();
		if (!qNamePop.equals(qName)) Log.info("POP does not match: '" + qNamePop + "' != '" + qName + "'");

		switch (qName) {
		case "annotation-category":
			location = null;
			locations = null;
			break;

		case "location":
			if (locations != null) locations.add(location);
			location = null;
			break;
		}
	}

	public void startAnnotationCategory(Attributes attributes) {
		switch (attributes.getValue("category")) {
		case "cross-link":
			break;
		}
	}

	/**
	 * Parse XML's element start
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		stringBuilder = new StringBuilder();
		stack.push(qName);

		String accession = attributes.getValue("accession");

		switch (qName) {
		case "entry":
			nextProtAccession = attributes.getValue("accession");
			Log.info("Entry: " + nextProtAccession);
			break;

		case "genomic-mapping":
			geneId = attributes.getValue("accession");
			break;

		case "transcript-mapping":
			transcriptId = attributes.getValue("accession");
			break;

		case "transcript-protein":
			proteinId = attributes.getValue("accession");
			break;

		case "annotation-category":
			startAnnotationCategory(attributes);
			break;

		case "location":
			if (location == null) location = new Location();
			break;

		case "begin":
			if (location != null) location.begin = Gpr.parseIntSafe(attributes.getValue("position"));
			break;

		case "end":
			if (location != null) location.end = Gpr.parseIntSafe(attributes.getValue("position"));
			break;

		case "target-isoform":
			location = new LocationTargetIsoform(accession);
		}
	}

	/**
	 * Convert attributes to a string
	 */
	String toString(Attributes attributes) {
		StringBuilder sb = new StringBuilder();
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
		StringBuilder sb = new StringBuilder();
		for (String s : stack)
			sb.append((s.length() > 0 ? " -> " : "") + s);
		return sb.toString();
	}
}
