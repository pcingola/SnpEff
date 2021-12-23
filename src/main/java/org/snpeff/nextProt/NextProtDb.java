package org.snpeff.nextProt;

import org.apache.commons.io.input.ReaderInputStream;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.util.Locale;

/**
 * Parse NetxProt XML file and build a database
 * <p>
 * http://www.nextprot.org/
 * <p>
 * <p>
 * How this works:
 * - NextProtDb
 *      Main class to parse a directory with NextPort XML files.
 *      Each XML file is quite large (several GB compressed) so it cannot be
 *      fully loaded and parse in memory on a "standard laptop"
 *      Once all markers are created (by the marker factory) the database
 *      is serialized to a file
 * <p>
 * <p>
 * - NextProtMarkerFactory
 *      Factory that creates all NextProt Markers.
 *      This object is shared across all XML handlers
 * <p>
 * - NextProtHandler
 *      This is a handler used to extract the relevant data from an XML file.
 *      One handler is created for each XML file, all handlers share the NextProtMarkerFactory
 * <p>
 * - NextProtSequenceConservation
 *      The markers are analyzed for sequence conservation patterns.
 *      If the marker type has high conservation, this is set in the NextProtMarker
 *      to keep in mind when predicting effects. For instance, a marker that has 100%
 *      conservation would be highly affected by a non-synonymous variant.
 * <p>
 * - NextProtXmlNode
 *      This is the basic "XML" node for the information we need to extract to create the Markers
 * <p>
 * - NextProtXmlIsoform:
 *      Specified an isoform protein / transcript
 * <p>
 * - NextProtXmlAnnotation:
 *      An annotation is a controlled vocabulary term (CvTerm) associated with one or more Locations in an Isoform.
 *      Example: "For transcript TR_1234, amino acid 25 is a phosphorylation site"
 *      In this case we have:
 *          "TR_1234": The Isoform
 *          "amino acid 25": The Location in the Isoform
 *          "phosphorylation site": The controlled vocabulary term
 * <p>
 * - NextProtXmlEntry:
 *      A set of annotations
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
        String[] files = (new File(xmlDirName)).list();
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
        StringBuilder sb = new StringBuilder();
        var missingCats = handler.getMissingCategories();
        for (String cat : missingCats.keysRanked(true))
            sb.append("\t" + missingCats.get(cat) + "\t" + cat + "\n");
        if (sb.length() > 0) Log.warning("Missing categories:\n" + sb);

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

            // Create an input stream that can handle compressed files
            var isGzipped = xmlFileName.toLowerCase(Locale.ROOT).endsWith(".gz");
            var reader = Gpr.reader(xmlFileName, isGzipped);
            var inStream = new ReaderInputStream(reader);

            handler = new NextProtHandler(markersFactory);
            saxParser.parse(inStream, handler); // specify handler
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
