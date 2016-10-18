package org.snpeff.snpEffect.commandLine;

import org.snpeff.SnpEff;
import org.snpeff.nextProt.NextProtParser;
import org.snpeff.util.Timer;

/**
 * Parse NetxProt XML file and build a database
 *
 * http://www.nextprot.org/
 *
 * @author pablocingolani
 */
public class SnpEffCmdBuildNextProt extends SnpEff {

	String xmlDirName;

	public SnpEffCmdBuildNextProt() {
		super();
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
	 * Run main analysis
	 */
	@Override
	public boolean run() {
		// Initialzie
		loadConfig(); // Read config file
		loadDb();

		NextProtParser nextProtParser = new NextProtParser(xmlDirName, config);
		nextProtParser.parse(); // Parse XML files
		nextProtParser.saveDatabase(); // Save database

		if (verbose) Timer.showStdErr("Done!");
		return true;
	}

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error        :\t" + message);
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff buildNextProt [options] genome_version nextProt_XML_dir");
		System.exit(-1);
	}

}
