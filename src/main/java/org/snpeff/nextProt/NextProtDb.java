package org.snpeff.nextProt;

import java.io.File;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Log;
import org.w3c.dom.Node;

/**
 * Parse NetxProt XML file and build a database
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtDb {

	public static final double HIGHLY_CONSERVED_AA_PERCENT = 0.99;

	public static final int HIGHLY_CONSERVED_AA_COUNT = 30;

	boolean debug;
	boolean verbose;
	String trIdFile;
	String xmlDirName;
	Markers markers;
	Config config;
	Genome genome;
	int aaErrors;

	public NextProtDb(String xmlDirName, Config config) {
		this.config = config;
		this.xmlDirName = xmlDirName;
		markers = new Markers();
	}

	/**
	 * Guess NextProt XML version
	 */
	int nextProtXmlVersion(Node doc) {
		Node nextProtNode = doc.getFirstChild();
		String nextProtNodeName = nextProtNode.getNodeName();

		if (nextProtNodeName.equals("nextprotExport")) return 1;
		if (nextProtNodeName.equals("nextprot-export")) return 2;
		throw new RuntimeException("Unrecognized nextprot version: Node name '" + nextProtNodeName + "'");
	}

	/**
	 * Parse XML file
	 */
	public boolean parse() {
		genome = config.getGenome();
		if (verbose) Log.info("done");

		// Parse all XML files in directory
		if (verbose) Log.info("Reading NextProt files from directory '" + xmlDirName + "'");
		String files[] = (new File(xmlDirName)).list();
		if (files != null) {
			for (String xmlFileName : files) {
				if (verbose) Log.info("\tNextProt file '" + xmlFileName + "'");
				if (xmlFileName.endsWith(".xml.gz") || xmlFileName.endsWith(".xml")) {
					String path = xmlDirName + "/" + xmlFileName;
					parse(path);
				}
			}
		} else Log.fatalError("No XML files found in directory '" + xmlDirName + "'");

		return true;
	}

	void parse(Node doc) {
		int xmlVersion = nextProtXmlVersion(doc);
		if (verbose) Log.info("NextProt XML version:" + xmlVersion);

		NextProtParser nextProtParser;
		switch (xmlVersion) {
		case 1:
			nextProtParser = new NextProtParser(config);
			break;

		case 2:
			nextProtParser = new NextProtParserV2(config);
			break;

		default:
			throw new RuntimeException("Unknown NextProt XML version " + xmlVersion);
		}

		nextProtParser.setVerbose(verbose);
		nextProtParser.setDebug(debug);
		nextProtParser.setTrIdFile(trIdFile);
		nextProtParser.parse(doc);
		markers.add(nextProtParser.getMarkers());
	}

	/**
	 * Parse an XML file
	 */
	void parse(String xmlFileName) {
		try {
			// Load document
			if (verbose) Log.info("Reading file:" + xmlFileName);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			SAXParser saxParser = factory.newSAXParser();
			File file = new File(xmlFileName);
			saxParser.parse(file, new NextProtHandler()); // specify handler
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Save nextprot markers as databases
	 */
	public void saveDatabase() {
		String nextProtBinFile = config.getDirDataGenomeVersion() + "/nextProt.bin";
		if (verbose) Log.info("Saving database to file '" + nextProtBinFile + "'");

		// Add chromosomes
		HashSet<Chromosome> chromos = new HashSet<>();
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

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setTrIdFile(String trIdFile) {
		this.trIdFile = trIdFile;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	String vcfSafe(String str) {
		return str.trim().replaceAll("(,|;|=| |\t)+", "_");
	}

}
