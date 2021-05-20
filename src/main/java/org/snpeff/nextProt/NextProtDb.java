package org.snpeff.nextProt;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.snpeff.snpEffect.Config;
import org.snpeff.util.Log;

/**
 * Parse NetxProt XML file and build a database
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class NextProtDb {

	boolean debug;
	boolean verbose;
	String xmlDirName;
	Config config;
	NextProtMarkerFactory markersFactory;
	NextProtHandler handler;

	public NextProtDb(String xmlDirName, Config config) {
		this.config = config;
		this.xmlDirName = xmlDirName;
		this.markersFactory = new NextProtMarkerFactory(config);
	}

	/**
	 * Parse all XML files in a directory
	 */
	public boolean parse() {
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

		// Conservation analysis
		markersFactory.conservation();

		// Show missing categories
		Log.info("Missing categories");
		var missingCats = handler.getMissingCategories();
		for (String cat : missingCats.keysRanked(true)) {
			Log.info("\t" + missingCats.get(cat) + "\t" + cat);
		}

		return true;
	}

	/**
	 * Parse a single NextProt XML file
	 */
	void parse(String xmlFileName) {
		try {
			// Load document
			if (verbose) Log.info("Reading file:" + xmlFileName);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setValidating(true);
			SAXParser saxParser = factory.newSAXParser();
			File file = new File(xmlFileName);

			handler = new NextProtHandler(markersFactory);
			saxParser.parse(file, handler); // specify handler
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void saveDatabase() {
		markersFactory.saveDatabase();
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
