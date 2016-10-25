package org.snpeff.nextProt;

import java.util.List;

import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;
import org.w3c.dom.Node;

/**
 * Parse NetxProt XML file (version 2)
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtParserV2 extends NextProtParser {

	protected String NODE_NAME_ANNOTATION_CATEGORY;
	protected String ATTR_NAME_POSITION;

	public NextProtParserV2(Config config) {
		super(config);
	}

	@Override
	protected void defineNextProtXmlTerms() {
		super.defineNextProtXmlTerms();

		// Define NextProt XML terms
		NODE_NAME_PROTEIN = "entry";

		NODE_NAME_GENE = "genomic-mapping";
		NODE_NAME_TRANSCRIPT = "transcript-mapping";
		NODE_NAME_ANNOTATION_LIST = "annotation-list";
		NODE_NAME_ANNOTATION_CATEGORY = "annotation-category";
		NODE_NAME_POSITION = "location";
		NODE_NAME_CVNAME = "cv-term";
		NODE_NAME_SEQUENCE = "isoform-sequence";
		//
		ATTR_NAME_UNIQUE_NAME = "accession";
		ATTR_NAME_DATABASE = "database";
		ATTR_NAME_ACCESSION = "accession";
		ATTR_NAME_FIRST = "begin";
		ATTR_NAME_LAST = "end";
		ATTR_NAME_POSITION = "position";
		ATTR_NAME_ISOFORM_REF = "accession";
		ATTR_VALUE_ENSEMBL = "Ensembl";
	}

	/**
	 * Get AA end position from position node
	 */
	@Override
	protected int getAaEnd(Node posNode) {
		Node endNode = findOneNode(posNode, ATTR_NAME_LAST, null, null, null);
		String last = getAttribute(endNode, ATTR_NAME_POSITION);
		int aaEnd = Gpr.parseIntSafe(last) - 1;
		return aaEnd;
	}

	/**
	 * Get AA start position from position node
	 */
	@Override
	protected int getAaStart(Node posNode) {
		Node beginNode = findOneNode(posNode, ATTR_NAME_FIRST, null, null, null);
		String first = getAttribute(beginNode, ATTR_NAME_POSITION);
		int aaStart = Gpr.parseIntSafe(first) - 1;
		return aaStart;
	}

	@Override
	List<Node> getAnnotationCategories(Node node) {
		List<Node> annListNodes = findNodes(node, NODE_NAME_ANNOTATION_CATEGORY, null, null, null);
		return annListNodes;
	}

	@Override
	protected String getIsoformRefFromPos(Node posNode) {
		Node isoAnn = posNode.getParentNode();
		String isoformRef = getAttribute(isoAnn, ATTR_NAME_ISOFORM_REF);
		return isoformRef;
	}

	@Override
	String getUniqueNameSequence(Node seqNode) {
		String seqUniqName = getAttribute(seqNode, ATTR_NAME_UNIQUE_NAME);
		return seqUniqName;
	}

	@Override
	String getUniqueNameTranscript(Node trNode) {
		Node isoMap = trNode.getParentNode().getParentNode();
		String trUniqName = getAttribute(isoMap, ATTR_NAME_UNIQUE_NAME);
		return trUniqName;
	}

}
