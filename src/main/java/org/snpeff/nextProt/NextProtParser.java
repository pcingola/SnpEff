package org.snpeff.nextProt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.snpeff.codons.CodonTables;
import org.snpeff.collections.AutoHashMap;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.stats.CountByType;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;
import org.snpeff.util.Timer;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Parse NetxProt XML file and build a database
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtParser {

	public static final double HIGHLY_CONSERVED_AA_PERCENT = 0.99;

	public static final int HIGHLY_CONSERVED_AA_COUNT = 30;

	// We don't care about these categories
	public static final String CATAGORY_BLACK_LIST_STR[] = { "" //
			, "expression-info" //
			, "mature-protein" //
			, "mature protein" //
			, "mutagenesis site" //
			, "mutagenesis-site" //
			, "mutagenesis" //
			, "pdb-mapping" //
			, "peptide-mapping" //
			, "retained intron" //
			, "sequence conflict" //
			, "sequence-conflict" //
			, "sequence variant" //
			, "sequence-variant" //
			, "srm-peptide-mapping" //
			, "variant" //
	};

	protected String NODE_NAME_PROTEIN;
	protected String NODE_NAME_GENE;
	protected String NODE_NAME_TRANSCRIPT;
	protected String NODE_NAME_ANNOTATION;
	protected String NODE_NAME_ANNOTATION_LIST;
	protected String NODE_NAME_POSITION;
	protected String NODE_NAME_PROPERTY;
	protected String NODE_NAME_DESCRIPTION;
	protected String NODE_NAME_CVNAME;
	protected String NODE_NAME_SEQUENCE;
	protected String NODE_NAME_XREF;

	protected String ATTR_NAME_UNIQUE_NAME;
	protected String ATTR_NAME_DATABASE;
	protected String ATTR_NAME_ACCESSION;
	protected String ATTR_NAME_ANNOTATION_LIST;
	protected String ATTR_NAME_CATAGORY;
	protected String ATTR_NAME_FIRST;
	protected String ATTR_NAME_LAST;
	protected String ATTR_NAME_ISOFORM_REF;
	protected String ATTR_NAME_PROPERTY_NAME;
	protected String ATTR_NAME_VALUE;

	protected String ATTR_VALUE_ENSEMBL;
	protected String ATTR_VALUE_REFSEQ;
	protected String ATTR_VALUE_NUCLEOTIDE_SEQUENCE_ID;

	boolean debug;
	boolean verbose;
	String trIdFile;
	HashSet<String> categoryBlackList;
	HashMap<String, String> trIdByUniqueName;
	HashMap<String, String> trIdMap;
	HashMap<String, String> sequenceByUniqueName;
	AutoHashMap<String, CountByType> countAaSequenceByType;
	HashMap<String, Transcript> trById;
	HashSet<String> proteinDifferences = new HashSet<>();
	HashSet<String> proteinOk = new HashSet<>();
	Markers markers;
	Config config;
	Genome genome;
	int aaErrors;

	public NextProtParser(Config config) {
		this.config = config;
		genome = config.getGenome();
		trIdByUniqueName = new HashMap<>();
		sequenceByUniqueName = new HashMap<>();
		countAaSequenceByType = new AutoHashMap<>(new CountByType());
		trById = new HashMap<>();
		markers = new Markers();

		defineNextProtXmlTerms();

		// Create and populate black list
		categoryBlackList = new HashSet<>();
		for (String cat : CATAGORY_BLACK_LIST_STR)
			categoryBlackList.add(cat);
	}

	void addTr(Transcript tr) {
		String trId = tr.getId();
		trById.put(trId, tr);

		// Find another transcript ID in the map
		String id = trIdMap.get(trId);
		if (id != null) trById.put(id, tr);

		// Remove transcript version (if any)
		if (trId.indexOf('.') > 0) {
			trId = trId.split("\\.")[0];
			id = trIdMap.get(trId);
			if (id != null) trById.put(id, tr);
		}
	}

	void addTranscripts() {
		// Read transcript ids
		readTrIdMap();

		// Build transcript map
		for (Gene gene : config.getSnpEffectPredictor().getGenome().getGenes())
			for (Transcript tr : gene)
				addTr(tr);
	}

	/**
	 * Show annotations counters in a table
	 */
	void analyzeSequenceConservation() {
		if (verbose) Log.info("Sequence conservation analysis."//
				+ "\n\tAA sequence length  : " + 1 //
				+ "\n\tMin AA count        : " + HIGHLY_CONSERVED_AA_COUNT //
				+ "\n\tMin AA conservation : " + HIGHLY_CONSERVED_AA_PERCENT //
		);

		ArrayList<String> keys = new ArrayList<>();
		keys.addAll(countAaSequenceByType.keySet());
		Collections.sort(keys);

		// Show title
		StringBuilder title = new StringBuilder();
		for (char aa : GprSeq.AMINO_ACIDS)
			title.append(aa + "\t");
		title.append("\t" + title);
		if (verbose) Log.info("Amino acid regions:\n\tTotal\tMax count\tAvg len\tConservation\tCatergory\tControlled Vocabulary\t" + title + "\tOther AA sequences:");

		// Show AA counts for each 'key'
		for (String key : keys) {
			long seqLen = 0, totalSeqs = 0, maxCount = 0;
			CountByType cbt = countAaSequenceByType.get(key);
			long total = cbt.sum();

			boolean highlyConservedAaSequence = false;

			StringBuilder sb = new StringBuilder();

			// For each single amino acid "sequence"
			for (char aa : GprSeq.AMINO_ACIDS) {
				long count = cbt.get("" + aa);
				if (count > 0) {
					seqLen += 1 * count;
					totalSeqs += count;
					maxCount = Math.max(maxCount, count);

					sb.append(count);
					double perc = ((double) count) / total;

					// We estimate that if most AA are the same, then changing this AA can cause a high impact in protein coding
					if ((perc > HIGHLY_CONSERVED_AA_PERCENT) && (total >= HIGHLY_CONSERVED_AA_COUNT)) highlyConservedAaSequence = true;
				}
				sb.append("\t");
			}

			// Sequences of more than one AA
			for (String aas : cbt.keySet()) {
				long count = cbt.get(aas);
				double perc = ((double) count) / total;
				if (aas.length() > 1) {
					seqLen += aas.length() * count;
					totalSeqs += count;
					maxCount = Math.max(maxCount, count);

					sb.append(String.format("\t" + aas + ":" + count));
					if ((perc > HIGHLY_CONSERVED_AA_PERCENT) && (total >= HIGHLY_CONSERVED_AA_COUNT)) highlyConservedAaSequence = true;
				}
			}

			long avgLen = seqLen / totalSeqs;

			// Show line
			if (verbose) Log.info( //
					"\t" + total //
							+ "\t" + maxCount //
							+ "\t" + avgLen //
							+ "\t" + (highlyConservedAaSequence ? "High" : "") //
							+ "\t" + key //
							+ "\t" + sb //
			);

			// Mark highly conserved
			if (highlyConservedAaSequence) {
				int count = 0;
				for (Marker m : markers) {
					NextProt nextProt = (NextProt) m;
					if (m.getId().equals(key)) {
						nextProt.setHighlyConservedAaSequence(true);
						count++;
					}
				}

				if (verbose) Log.info("NextProt " + count + " markers type '" + key + "' marked as highly conserved AA sequence");
			}
		}
	}

	/**
	 * Add annotations
	 */
	void countAaSequence(String category, String contrVoc, String description, String sequence) {
		String key = key(category, contrVoc, description);
		CountByType cbt = countAaSequenceByType.getOrCreate(key);
		cbt.inc(sequence);
	}

	protected void defineNextProtXmlTerms() {
		// Define NextProt XML terms
		NODE_NAME_PROTEIN = "protein";
		NODE_NAME_GENE = "gene";
		NODE_NAME_TRANSCRIPT = "transcript";
		NODE_NAME_ANNOTATION = "annotation";
		NODE_NAME_ANNOTATION_LIST = "annotationList";
		NODE_NAME_POSITION = "position";
		NODE_NAME_PROPERTY = "property";
		NODE_NAME_DESCRIPTION = "description";
		NODE_NAME_CVNAME = "cvName";
		NODE_NAME_SEQUENCE = "sequence";
		NODE_NAME_XREF = "xref";

		ATTR_NAME_UNIQUE_NAME = "uniqueName";
		ATTR_NAME_DATABASE = "database";
		ATTR_NAME_ACCESSION = "accession";
		ATTR_NAME_ANNOTATION_LIST = "annotationList";
		ATTR_NAME_CATAGORY = "category";
		ATTR_NAME_FIRST = "first";
		ATTR_NAME_LAST = "last";
		ATTR_NAME_ISOFORM_REF = "isoformRef";
		ATTR_NAME_PROPERTY_NAME = "propertyName";
		ATTR_NAME_VALUE = "value";

		ATTR_VALUE_ENSEMBL = "Ensembl";
		ATTR_VALUE_REFSEQ = "RefSeq";
		ATTR_VALUE_NUCLEOTIDE_SEQUENCE_ID = "'nucleotide sequence ID";
	}

	/**
	 * Show an error message and exit
	 */
	protected void fatalError(String message) {
		System.err.println("Fatal error: " + message);
		System.exit(-1);
	}

	/**
	 * Parse a node
	 */
	ArrayList<Node> findNodes(Node node, String nodeName, String nodeValue, String attrName, String attrValue) {
		ArrayList<Node> resulstsList = new ArrayList<>();

		while (node != null) {
			boolean found = false;
			short type = node.getNodeType();

			//---
			// Get name & value
			//---
			String name = node.getNodeName();
			String value = node.getNodeValue();
			if (value != null) value = value.replace('\n', ' ').trim();

			//---
			// Get attributes
			//---
			StringBuilder attrSb = new StringBuilder();
			if ((attrName != null) || (attrValue != null)) { // Are attributes required? (don't parse if they are not needed
				NamedNodeMap map = node.getAttributes();
				if (map != null) {
					for (int i = 0; i < map.getLength(); i++) {
						Node attr = map.item(i);
						if (attrSb.length() > 0) attrSb.append(", ");

						String aname = attr.getNodeName();
						String aval = attr.getNodeValue();
						attrSb.append(aname + "=" + aval);

						if (((nodeName == null) || ((name != null) && name.equals(nodeName))) //
								&& ((nodeValue == null) || ((value != null) && value.equals(nodeValue))) //
								&& ((attrName == null) || ((aname != null) && attrName.equals(aname))) //
								&& ((attrValue == null) || ((aval != null) && attrValue.equals(aval))) //
						) found = true;
					}
				}
			} else {
				if (((nodeName == null) || ((name != null) && name.equals(nodeName))) //
						&& ((nodeValue == null) || ((value != null) && value.equals(nodeValue))) //
				) {
					found = true;
				}
			}

			if (found) resulstsList.add(node);

			//---
			// Show node
			//---
			switch (type) {
			case Node.ELEMENT_NODE:
				NodeList nodeList = node.getChildNodes();
				resulstsList.addAll(findNodes(nodeList, nodeName, nodeValue, attrName, attrValue));
				node = node.getNextSibling();
				break;
			case Node.TEXT_NODE:
				node = null;
				break;
			case Node.CDATA_SECTION_NODE:
				node = null;
				break;
			default:
				node = null;
			}
		}

		return resulstsList;
	}

	/**
	 * Parse a node list
	 */
	List<Node> findNodes(NodeList nodeList, String nodeName, String nodeValue, String attrName, String attrValue) {
		ArrayList<Node> resulstsList = new ArrayList<>();

		for (int temp = 0; temp < nodeList.getLength(); temp++) {
			Node node = nodeList.item(temp);
			resulstsList.addAll(findNodes(node, nodeName, nodeValue, attrName, attrValue));
		}

		return resulstsList;
	}

	/**
	 * Find only one node
	 */
	Node findOneNode(Node node, String nodeName, String nodeValue, String attrName, String attrValue) {
		ArrayList<Node> resulstsList = findNodes(node, nodeName, nodeValue, attrName, attrValue);
		if (resulstsList.isEmpty()) return null;
		return resulstsList.get(0);
	}

	/**
	 * Find sequences for a node
	 */
	void findSequences(Node node) {
		// Get sequences
		List<Node> seqNodes = findNodes(node, NODE_NAME_SEQUENCE, null, null, null);
		for (Node seq : seqNodes) {
			String seqStr = getText(seq);
			String uniq = getUniqueNameSequence(seq);
			sequenceByUniqueName.put(uniq, seqStr);
		}
	}

	/**
	 * Get uniqueId -> EnsemblId mapping for transcripts
	 * @return true if any was found
	 */
	boolean findTrIds(Node node) {
		boolean added = false;

		// Find Ensembl transcript ID
		List<Node> ensemblTrIds = findNodes(node, NODE_NAME_TRANSCRIPT, null, ATTR_NAME_DATABASE, ATTR_VALUE_ENSEMBL);
		for (Node trNode : ensemblTrIds) {
			// Get Ensembl ID
			String trId = getAttribute(trNode, ATTR_NAME_ACCESSION);

			// Get Unique ID
			String trUniqName = getUniqueNameTranscript(trNode);

			// Add to map
			trIdByUniqueName.put(trUniqName, trId);
			added = true;
		}

		return added;
	}

	/**
	 * Get AA end position from position node
	 */
	protected int getAaEnd(Node posNode) {
		String last = getAttribute(posNode, ATTR_NAME_LAST);
		int aaEnd = Gpr.parseIntSafe(last) - 1;
		return aaEnd;
	}

	/**
	 * Get AA start position from position node
	 */
	protected int getAaStart(Node posNode) {
		String first = getAttribute(posNode, ATTR_NAME_FIRST);
		int aaStart = Gpr.parseIntSafe(first) - 1;
		return aaStart;
	}

	/**
	 * Get annotation description from annotation node
	 */
	String getAnnDescription(Node annNode) {
		Node descr = findOneNode(annNode, NODE_NAME_DESCRIPTION, null, null, null);
		String description = getText(descr);
		if (description == null) description = "";
		else if (description.indexOf(';') > 0) description = description.substring(0, description.indexOf(';')); // Cut after semicolon
		return description;
	}

	List<Node> getAnnotationCategories(Node node) {
		List<Node> annListNodes = findNodes(node, NODE_NAME_ANNOTATION_LIST, null, null, null);
		return annListNodes;
	}

	/**
	 * Get an attribute from a node
	 */
	String getAttribute(Node node, String attrName) {
		if (node == null) return null;

		NamedNodeMap map = node.getAttributes();
		if (map == null) return null;

		Node attrNode = map.getNamedItem(attrName);
		if (attrNode == null) return null;

		return attrNode.getNodeValue();
	}

	/**
	 * Get controlled vocabulary term from annotation node
	 */
	String getControlledVocubulary(Node annNode) {
		// Controlled vocabulary
		Node cv = findOneNode(annNode, NODE_NAME_CVNAME, null, null, null);
		String contrVoc = getText(cv);
		if (contrVoc == null) contrVoc = "";

		contrVoc.indexOf(';');
		String cvs[] = contrVoc.split(";", 2);
		if (cvs.length > 1) return cvs[0];
		return contrVoc;
	}

	/**
	 * Get Ensembl gene ID
	 */
	String getGeneId(Node node, String uniqueName) {
		Node geneNode = findOneNode(node, NODE_NAME_GENE, null, ATTR_NAME_DATABASE, ATTR_VALUE_ENSEMBL);
		return getAttribute(geneNode, ATTR_NAME_ACCESSION);
	}

	protected String getIsoformRefFromPos(Node posNode) {
		Node isoAnn = posNode.getParentNode().getParentNode();
		String isoformRef = getAttribute(isoAnn, ATTR_NAME_ISOFORM_REF);
		return isoformRef;
	}

	public Markers getMarkers() {
		return markers;
	}

	/**
	 * Get text form a node
	 */
	String getText(Node n) {
		if (n == null) return null;
		return n.getTextContent().replace('\n', ' ').trim();
	}

	String getUniqueNameSequence(Node seqNode) {
		Node iso = seqNode.getParentNode();
		String seqUniqName = getAttribute(iso, ATTR_NAME_UNIQUE_NAME);
		return seqUniqName;
	}

	String getUniqueNameTranscript(Node trNode) {
		Node isoMap = trNode.getParentNode();
		String trUniqName = getAttribute(isoMap, ATTR_NAME_UNIQUE_NAME);
		return trUniqName;
	}

	/**
	 * Create a key
	 */
	String key(String category, String contrVoc, String description) {
		category = vcfSafe(category);

		if ((description == null) || description.isEmpty()) description = contrVoc;
		description = vcfSafe(description);

		if (description.isEmpty()) return category;
		return category + ":" + description;
	}

	/**
	 * Get node type as a string
	 */
	String nodeType(short type) {
		switch (type) {
		case Node.ATTRIBUTE_NODE:
			return "ATTRIBUTE_NODE";
		case Node.CDATA_SECTION_NODE:
			return "CDATA_SECTION_NODE";
		case Node.COMMENT_NODE:
			return "COMMENT_NODE";
		case Node.DOCUMENT_FRAGMENT_NODE:
			return "DOCUMENT_FRAGMENT_NODE";
		case Node.DOCUMENT_NODE:
			return "DOCUMENT_NODE";
		case Node.DOCUMENT_POSITION_CONTAINED_BY:
			return "DOCUMENT_POSITION_CONTAINED_BY";
		case Node.DOCUMENT_TYPE_NODE:
			return "DOCUMENT_TYPE_NODE";
		case Node.ELEMENT_NODE:
			return "ELEMENT_NODE";
		case Node.ENTITY_NODE:
			return "ENTITY_NODE";
		case Node.ENTITY_REFERENCE_NODE:
			return "ENTITY_REFERENCE_NODE";
		case Node.NOTATION_NODE:
			return "NOTATION_NODE";
		case Node.PROCESSING_INSTRUCTION_NODE:
			return "PROCESSING_INSTRUCTION_NODE";
		case Node.TEXT_NODE:
			return "TEXT_NODE";
		default:
			throw new RuntimeException("Unknown");
		}
	}

	/**
	 * Parse  XML document
	 */
	public void parse(Node doc) {
		addTranscripts();

		if (verbose) Log.info("Parsing XML data.");
		List<Node> nodeList = findNodes(doc.getChildNodes(), NODE_NAME_PROTEIN, null, null, null);
		if (verbose) Log.info("Found " + nodeList.size() + " protein nodes");

		// Parse each node
		for (Node node : nodeList) {
			if (debug) Log.debug("Processing protein node: " + toString(node));
			parseProteinNode(node);
		}

		analyzeSequenceConservation();
	}

	/**
	 * Parse a protein node
	 */
	void parseAnnotation(Node ann, String geneId, String category) {
		if (debug) Log.debug("\t\tAnnotation: " + toString(ann) + "\tCategory: " + category);
		String description = getAnnDescription(ann);
		String contrVoc = getControlledVocubulary(ann);

		// Search annotations
		List<Node> posNodes = findNodes(ann, NODE_NAME_POSITION, null, null, null);
		for (Node pos : posNodes) {
			if (debug) Log.debug("\t\t\tPosition: " + toString(pos));
			// Get first & last position
			int aaStart = getAaStart(pos);
			int aaEnd = getAaStart(pos);
			if (aaStart < 0 || aaEnd < 0) continue;
			int len = aaEnd - aaStart + 1;

			// Get ID
			String isoformRef = getIsoformRefFromPos(pos);

			// Find sequence
			String sequence = sequenceByUniqueName.get(isoformRef);
			String subSeq = "";
			if ((sequence != null) //
					&& (aaStart >= 0) //
					&& (aaEnd >= aaStart) //
					&& (aaEnd < sequence.length()) //
			) subSeq = sequence.substring(aaStart, aaEnd + 1);

			// Check transcript
			TranscriptData trData = transcriptData(isoformRef, aaStart, aaEnd, sequence, subSeq);

			// Create nextProt markers
			if (!trData.ok || (len <= 0)) continue;

			// Create marker
			String id = key(category, contrVoc, description);
			NextProt nextProt = new NextProt(trData.tr, trData.chrPosStart, trData.chrPosEnd, id);
			markers.add(nextProt);
			if (debug) Log.debug("Added NextProt entry:" + nextProt //
					+ "\n\tgeneId:" + geneId //
					+ "\n\tisoformRef:" + isoformRef //
					+ "\n\ttrId:" + trData.tr.getId() //
					+ "\n\tcategory:'" + category + "'" //
					+ "\n\tdescription:'" + description + "'" //
					+ "\n\tcontrolled_vocabulary:" + contrVoc //
					+ "\n\taaStart:" + aaStart//
					+ "\n\taaEnd:" + aaEnd //
					+ "\n\taaLen:" + len //
					+ "\n\tchr:" + trData.chrName //
					+ "\n\tstart:" + trData.chrPosStart //
					+ "\n\tend:" + trData.chrPosEnd //
					+ "\n\tsubSeq:" + subSeq //
					+ "\n\tcodon:" + trData.codon //
					+ "\n\taa:" + trData.aa//
			);

			countAaSequence(category, contrVoc, description, subSeq);
		}
	}

	/**
	 * Parse "<annotations>" XML mark
	 */
	void parseAnnotations(Node node, String geneId) {
		// Find all <annotationList> XML marks
		List<Node> annListNodes = getAnnotationCategories(node);

		// For each <annotationList> set of nodes
		for (Node annListNode : annListNodes) {
			// Get annotationList category
			String category = getAttribute(annListNode, ATTR_NAME_CATAGORY);

			if (!categoryBlackList.contains(category)) {
				List<Node> annNodes = findNodes(annListNode, NODE_NAME_ANNOTATION, null, null, null);

				// Analyze the ones not in the blacklist
				for (Node ann : annNodes)
					parseAnnotation(ann, geneId, category);
			}
		}
	}

	/**
	 * Parse a protein node
	 */
	void parseProteinNode(Node node) {
		String uniqueName = getAttribute(node, ATTR_NAME_UNIQUE_NAME);
		if (debug) Log.debug("Parsing protein node: " + uniqueName);

		// Find Ensembl gene ID
		String geneId = getGeneId(node, uniqueName);
		if (geneId != null) {
			if (debug) Log.debug("\tFound matching gene ID: " + geneId);
			// Get transcript IDs
			if (findTrIds(node)) {
				findSequences(node); // Find sequences
				parseAnnotations(node, geneId); // Parse annotation list (XML <annotationList> mark)
			}
		}
	}

	/**
	 * Read transcript file
	 */
	void readTrIdMap() {
		trIdMap = new HashMap<>();
		if (trIdFile == null) return;
		if (verbose) Log.info("Reading transcripts file '" + trIdFile + "'");

		String lines[] = Gpr.readFile(trIdFile).split("\n");
		for (String line : lines) {
			String ids[] = line.split("\t");
			if (ids.length > 1) {
				String ensemblId = ids[0].trim();
				String refSeqId = ids[1].trim();
				trIdMap.put(refSeqId, ensemblId);
			}
		}
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setTrIdFile(String trIdFile) {
		this.trIdFile = trIdFile;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Show a node as a string
	 */
	String toString(Node node) {
		StringBuilder sb = new StringBuilder();

		//---
		// Get name & value
		//---
		String name = node.getNodeName();
		String value = node.getNodeValue();
		if (value != null) value = value.replace('\n', ' ').trim();
		sb.append(name);

		//---
		// Get attributes
		//---
		NamedNodeMap map = node.getAttributes();
		if (map != null) {
			sb.append("( ");
			for (int i = 0; i < map.getLength(); i++) {
				Node attr = map.item(i);
				String aname = attr.getNodeName();
				String aval = attr.getNodeValue();

				if (i > 0) sb.append(", ");
				sb.append(aname + "='" + aval + "'");
			}
			sb.append(" )");
		}

		if (value != null) sb.append(" = '" + value + "'\n");

		return sb.toString();
	}

	/**
	 * Gather data from transcript
	 */
	TranscriptData transcriptData(String isoformRef, int aaStart, int aaEnd, String sequence, String subSeq) {
		String trId = trIdByUniqueName.get(isoformRef);
		TranscriptData trData = new TranscriptData();
		if (trId != null) {
			Transcript tr = trById.get(trId);
			if (tr != null) {
				trData.tr = tr;
				String protein = tr.protein();

				// Remove trailing stop codon ('*')
				if (!protein.isEmpty() && (protein.charAt(protein.length() - 1) == '*')) protein = protein.substring(0, protein.length() - 1);

				// Sanity check: Do protein sequences match?
				if (protein.equals(sequence)) {
					proteinOk.add(trId);

					if ((aaStart >= 0) && (aaEnd >= aaStart)) {
						// Try to map to chromosome position
						int cdsBase2Pos[] = tr.baseNumberCds2Pos();
						int codonStart = aaStart * 3;
						int codonEnd = (aaEnd + 1) * 3 - 1;

						if (codonStart < cdsBase2Pos.length && codonEnd < cdsBase2Pos.length) {
							if (tr.isStrandPlus()) {
								trData.chrPosStart = cdsBase2Pos[codonStart];
								trData.chrPosEnd = cdsBase2Pos[codonEnd];
							} else {
								trData.chrPosStart = cdsBase2Pos[codonEnd];
								trData.chrPosEnd = cdsBase2Pos[codonStart];
							}

							trData.chrName = tr.getChromosomeName();

							// More sanity checks
							trData.codon = tr.cds().substring(codonStart, codonEnd + 1);
							trData.aa = CodonTables.getInstance().aa(trData.codon, genome, trData.chrName);
							if (!subSeq.equals(trData.aa) && verbose) {
								Log.info("WARNING: AA differ: " //
										+ "\tUniqueName : " + isoformRef //
										+ "\tEnsembl ID : " + trId //
										+ "\tEnsembl  AA: '" + trData.aa + "'"//
										+ "\tNextProt AA: '" + subSeq + "'"//
										+ "\n");
							} else trData.ok = true; // All sanity checks passed
						}
					}
				} else {
					if (!proteinDifferences.contains(trId) && verbose) Log.info("WARNING: Protein sequences differ: " //
							+ "\tUniqueName" + isoformRef //
							+ "\tEnsembl ID: " + trId //
							+ "\n\tEnsembl  (" + protein.length() + "): " + protein //
							+ "\n\tNextProt (" + sequence.length() + "): " + sequence //
							+ "\n");
					proteinDifferences.add(trId);
				}
			}
		}

		return trData;
	}

	String vcfSafe(String str) {
		return str.trim().replaceAll("(,|;|=| |\t)+", "_");
	}
}

class TranscriptData {
	public boolean ok = false;
	public Transcript tr = null;
	public int chrPosStart = -1, chrPosEnd = -1;
	public String chrName = "", codon = "", aa = "";

	@Override
	public String toString() {
		return chrName + ":" + chrPosStart + "-" + chrPosEnd //
				+ ", codon: '" + codon + "'" //
				+ ", aa: '" + aa + "'" //
		;
	}
}
