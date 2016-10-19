package org.snpeff.nextProt;

import java.util.List;

import org.snpeff.snpEffect.Config;
import org.snpeff.util.Timer;
import org.w3c.dom.Node;

/**
 * Parse NetxProt XML file (version 2)
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtParserV2 extends NextProtParser {

	public NextProtParserV2(Config config) {
		super(config);
	}

	/**
	 * Parse  XML document
	 */
	@Override
	public void parse(Node doc) {
		addTranscripts();

		if (verbose) Timer.showStdErr("Parsing XML data.");
		List<Node> nodeList = findNodes(doc.getChildNodes(), "biological-object", null, "bio-type", "protein");
		if (verbose) Timer.showStdErr("Found " + nodeList.size() + " protein nodes");

		// Parse each node
		for (Node node : nodeList)
			parseProteinNode(node);

		analyzeSequenceConservation();
	}

}
