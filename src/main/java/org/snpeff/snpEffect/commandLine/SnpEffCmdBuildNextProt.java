package org.snpeff.snpEffect.commandLine;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.snpeff.SnpEff;
import org.snpeff.codons.CodonTables;
import org.snpeff.collections.AutoHashMap;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.Transcript;
import org.snpeff.stats.CountByType;
import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Timer;
import org.w3c.dom.Document;
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
public class SnpEffCmdBuildNextProt extends SnpEff {

	public static final double HIGHLY_CONSERVED_AA_PERCENT = 0.99;

	public static final int HIGHLY_CONSERVED_AA_COUNT = 30;;

	// We don't care about these categories
	public static final String CATAGORY_BLACK_LIST_STR[] = { "" //
			, "sequence variant" //
			, "sequence conflict" //
			, "mature protein" //
			, "mutagenesis site" //
			, "retained intron" //
	};

	public static final String NODE_NAME_PROTEIN = "protein";
	public static final String NODE_NAME_GENE = "gene";
	public static final String NODE_NAME_TRANSCRIPT = "transcript";
	public static final String NODE_NAME_ANNOTATION = "annotation";
	public static final String NODE_NAME_ANNOTATION_LIST = "annotationList";
	public static final String NODE_NAME_POSITION = "position";
	public static final String NODE_NAME_PROPERTY = "property";
	public static final String NODE_NAME_DESCRIPTION = "description";
	public static final String NODE_NAME_CVNAME = "cvName";
	public static final String NODE_NAME_SEQUENCE = "sequence";
	public static final String NODE_NAME_XREF = "xref";

	public static final String ATTR_NAME_UNIQUE_NAME = "uniqueName";
	public static final String ATTR_NAME_DATABASE = "database";
	public static final String ATTR_NAME_ACCESSION = "accession";
	public static final String ATTR_NAME_ANNOTATION_LIST = "annotationList";
	public static final String ATTR_NAME_CATAGORY = "category";
	public static final String ATTR_NAME_FIRST = "first";
	public static final String ATTR_NAME_LAST = "last";
	public static final String ATTR_NAME_ISOFORM_REF = "isoformRef";
	public static final String ATTR_NAME_PROPERTY_NAME = "propertyName";
	public static final String ATTR_NAME_VALUE = "value";

	public static final String ATTR_VALUE_ENSEMBL = "Ensembl";
	public static final String ATTR_VALUE_REFSEQ = "RefSeq";
	public static final String ATTR_VALUE_NUCLEOTIDE_SEQUENCE_ID = "'nucleotide sequence ID";

	public static final String NEXT_PROT_DB_DIR = Gpr.HOME + "/snpEff/db/nextProt/2012_06";

	String xmlDirName;
	HashSet<String> categoryBlackList;
	HashMap<String, String> trIdByUniqueName;
	HashMap<String, String> sequenceByUniqueName;
	AutoHashMap<String, CountByType> countAaSequenceByType;
	HashMap<String, Transcript> trById;
	HashSet<String> proteinDifferences = new HashSet<String>();
	HashSet<String> proteinOk = new HashSet<String>();
	Markers markers;
	Genome genome;
	int aaErrors;

	public SnpEffCmdBuildNextProt() {
		markers = new Markers();
		trIdByUniqueName = new HashMap<String, String>();
		sequenceByUniqueName = new HashMap<String, String>();
		countAaSequenceByType = new AutoHashMap<String, CountByType>(new CountByType());
		trById = new HashMap<String, Transcript>();

		// Create and populate black list
		categoryBlackList = new HashSet<String>();
		for (String cat : CATAGORY_BLACK_LIST_STR)
			categoryBlackList.add(cat);
	}

	/**
	 * Show annotations counters in a table
	 */
	void analyzeSequenceConservation() {
		if (verbose) Timer.showStdErr("Sequence conservation analysis."//
				+ "\n\tAA sequence length  : " + 1 //
				+ "\n\tMin AA count        : " + HIGHLY_CONSERVED_AA_COUNT //
				+ "\n\tMin AA conservation : " + HIGHLY_CONSERVED_AA_PERCENT //
		);

		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(countAaSequenceByType.keySet());
		Collections.sort(keys);

		// Show title
		StringBuilder title = new StringBuilder();
		for (char aa : GprSeq.AMINO_ACIDS)
			title.append(aa + "\t");
		title.append("\t" + title);
		if (verbose) System.out.println("Amino acid regions:\n\tTotal\tMax count\tAvg len\tConservation\tCatergory\tControlled Vocabulary\t" + title + "\tOther AA sequences:");

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
			if (verbose) System.out.println( //
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

				if (verbose) Timer.showStdErr("NextProt " + count + " markers type '" + key + "' marked as highly conserved AA sequence");
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

	/**
	 * Parse a node
	 */
	ArrayList<Node> findNodes(Node node, String nodeName, String nodeValue, String attrName, String attrValue) {
		ArrayList<Node> resulstsList = new ArrayList<Node>();

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
		ArrayList<Node> resulstsList = new ArrayList<Node>();

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
			Node iso = seq.getParentNode();
			String uniq = getAttribute(iso, ATTR_NAME_UNIQUE_NAME);
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
			Node isoMap = trNode.getParentNode();
			String trUniqName = getAttribute(isoMap, ATTR_NAME_UNIQUE_NAME);

			// Add to map
			trIdByUniqueName.put(trUniqName, trId);
			added = true;
		}

		return added;
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
	 * Get Ensembl gene ID
	 */
	String getGeneId(Node node, String uniqueName) {
		Node geneNode = findOneNode(node, NODE_NAME_GENE, null, ATTR_NAME_DATABASE, ATTR_VALUE_ENSEMBL);
		return getAttribute(geneNode, ATTR_NAME_ACCESSION);
	}

	/**
	 * Get text form a node
	 */
	String getText(Node n) {
		if (n == null) return null;
		return n.getTextContent().replace('\n', ' ').trim();
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
	 * Parse an XML file
	 */
	void parse(String xmlFileName) {
		try {
			//---
			// Load document
			//---
			if (verbose) Timer.showStdErr("Reading file:" + xmlFileName);
			File xmlFile = new File(xmlFileName);

			Document doc = null;
			if (xmlFileName.endsWith(".gz")) doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new GZIPInputStream(new FileInputStream(xmlFile)));
			else doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlFile);

			if (verbose) Timer.showStdErr("Normalizing XML document");
			doc.getDocumentElement().normalize();

			//---
			// Parse nodes
			//---
			if (verbose) Timer.showStdErr("Parsing XML data.");
			List<Node> nodeList = findNodes(doc.getChildNodes(), NODE_NAME_PROTEIN, null, null, null);
			if (verbose) Timer.showStdErr("Found " + nodeList.size() + " protein nodes");

			// Parse each node
			for (Node node : nodeList)
				parseProteinNode(node);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Parse a protein node
	 */
	void parseAnnotation(Node ann, String geneId, String category) {
		// Description
		Node descr = findOneNode(ann, NODE_NAME_DESCRIPTION, null, null, null);
		String description = getText(descr);
		if (description == null) description = "";
		else if (description.indexOf(';') > 0) description = description.substring(0, description.indexOf(';')); // Cut after semicolon

		// Controlled vocabulary
		Node cv = findOneNode(ann, NODE_NAME_CVNAME, null, null, null);
		String contrVoc = getText(cv);
		if (contrVoc == null) contrVoc = "";

		contrVoc.indexOf(';');
		String cvs[] = contrVoc.split(";", 2);
		String contrVoc2 = "";
		if (cvs.length > 1) {
			contrVoc = cvs[0];
			contrVoc2 = cvs[1];
		}

		// Search annotations
		List<Node> posNodes = findNodes(ann, NODE_NAME_POSITION, null, null, null);
		for (Node pos : posNodes) {

			// Get first & last position
			String first = getAttribute(pos, ATTR_NAME_FIRST);
			String last = getAttribute(pos, ATTR_NAME_LAST);
			int aaStart = Gpr.parseIntSafe(first) - 1;
			int aaEnd = Gpr.parseIntSafe(last) - 1;
			int len = aaEnd - aaStart + 1;

			// Get ID
			Node isoAnn = pos.getParentNode().getParentNode();
			String isoformRef = getAttribute(isoAnn, ATTR_NAME_ISOFORM_REF);

			// Find sequence
			String sequence = sequenceByUniqueName.get(isoformRef);
			String subSeq = "";
			if ((sequence != null) && (aaStart >= 0) && (aaEnd >= aaStart)) subSeq = sequence.substring(aaStart, aaEnd + 1);

			// Check transcript
			TranscriptData trData = transcriptData(isoformRef, aaStart, aaEnd, sequence, subSeq);

			// Create nextProt markers
			if (trData.ok && (len > 0)) {
				if (debug) System.out.println(geneId //
						+ "\t" + isoformRef //
						+ "\t" + trData.tr.getId() //
						+ "\t" + category //
						+ "\t" + description //
						+ "\t" + contrVoc //
						+ "\t" + contrVoc2 //
						+ "\t" + first //
						+ "\t" + last //
						+ "\t" + len //
						+ "\t" + trData.chrName //
						+ "\t" + trData.chrPosStart //
						+ "\t" + trData.chrPosEnd //
						+ "\t" + subSeq //
						+ "\t" + trData.codon //
						+ "\t" + trData.aa//
				);

				// Create marker
				String id = key(category, contrVoc, description);
				NextProt nextProt = new NextProt(trData.tr, trData.chrPosStart, trData.chrPosEnd, id);
				if (debug) Gpr.debug("Adding NextProt: " + nextProt);
				markers.add(nextProt);

				// if (subSeq.length() == 1) countAaSequence(category, contrVoc, description, subSeq);
				countAaSequence(category, contrVoc, description, subSeq);
			}
		}
	}

	/**
	 * Parse "<annotations>" XML mark
	 */
	void parseAnnotations(Node node, String geneId) {
		// Find all <annotationList> XML marks
		List<Node> annListNodes = findNodes(node, NODE_NAME_ANNOTATION_LIST, null, null, null);

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

	@Override
	public void parseArgs(String[] args) {
		this.args = args;

		if (args.length <= 0) usage(null);

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// Argument starts with '-'?
			if (isOpt(arg)) {
				// All of them are parsed in SnpEff
				usage("Unknonwn option '" + arg + "'");
			} else if ((genomeVer == null) || genomeVer.isEmpty()) genomeVer = args[i];
			else if ((xmlDirName == null) || xmlDirName.isEmpty()) xmlDirName = args[i];
		}

		// Sanity check
		if ((genomeVer == null) || genomeVer.isEmpty()) usage("Missing genome version");
		if ((xmlDirName == null) || xmlDirName.isEmpty()) usage("Missing nextProt XML dir");
	}

	/**
	 * Parse a protein node
	 */
	void parseProteinNode(Node node) {
		String uniqueName = getAttribute(node, ATTR_NAME_UNIQUE_NAME);
		if (debug) Timer.showStdErr("Parsing protein node: " + uniqueName);

		// Find Ensembl gene ID
		String geneId = getGeneId(node, uniqueName);
		if (geneId != null) {
			// Get transcript IDs
			if (findTrIds(node)) {
				findSequences(node); // Find sequences
				parseAnnotations(node, geneId); // Parse annotation list (XML <annotationList> mark)
			}
		}
	}

	/**
	 * Run main analysis
	 */
	@Override
	public boolean run() {
		// Initialzie
		loadConfig(); // Read config file
		loadDb();

		genome = config.getGenome();
		if (verbose) Timer.showStdErr("done");

		// Build transcript map
		for (Gene gene : config.getSnpEffectPredictor().getGenome().getGenes())
			for (Transcript tr : gene)
				trById.put(tr.getId(), tr);

		// Parse all XML files in directory
		if (verbose) Timer.showStdErr("Reading NextProt files from directory '" + xmlDirName + "'");
		String files[] = (new File(xmlDirName)).list();
		if (files != null) {
			for (String xmlFileName : files) {
				if (verbose) Timer.showStdErr("\tNextProt file '" + xmlFileName + "'");
				if (xmlFileName.endsWith(".xml.gz") || xmlFileName.endsWith(".xml")) {
					String path = xmlDirName + "/" + xmlFileName;
					parse(path);
				}
			}
		} else fatalError("No XML files found in directory '" + xmlDirName + "'");

		// Show stats
		if (verbose) Timer.showStdErr("Proteing sequences:" //
				+ "\n\tMatch       : " + proteinOk.size() //
				+ "\n\tDifferences : " + proteinDifferences.size() //
				+ "\n\tAA errros   : " + aaErrors //
		);

		analyzeSequenceConservation();

		// Save database
		save();

		if (verbose) Timer.showStdErr("Done!");
		return true;
	}

	/**
	 * Save nextprot markers
	 */
	void save() {
		String nextProtBinFile = config.getDirDataGenomeVersion() + "/nextProt.bin";
		if (verbose) Timer.showStdErr("Saving database to file '" + nextProtBinFile + "'");

		// Add chromosomes
		HashSet<Chromosome> chromos = new HashSet<Chromosome>();
		for (Marker m : markers)
			chromos.add(m.getChromosome());

		// Create a set of all markers to be saved
		Markers markersToSave = new Markers();
		markersToSave.add(genome);
		for (Chromosome chr : chromos)
			markersToSave.add(chr);
		for (Marker m : markers)
			markersToSave.add(m);

		// Save
		markersToSave.save(nextProtBinFile);
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
						if (!subSeq.equals(trData.aa) && verbose) Timer.showStdErr("WARNING: AA differ: " //
								+ "\tUniqueName" + isoformRef //
								+ "\tEnsembl ID: " + trId //
								+ "\tEnsembl  AA: " + trData.aa//
								+ "\tNextProt AA:" + subSeq//
								+ "\n");
						else trData.ok = true; // All sanity checks passed
					}
				} else {
					if (!proteinDifferences.contains(trId) && verbose) Timer.showStdErr("WARNING: Protein sequences differ: " //
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

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error        :\t" + message);
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff buildNextProt [options] genome_version nextProt_XML_dir");
		System.exit(-1);
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
}