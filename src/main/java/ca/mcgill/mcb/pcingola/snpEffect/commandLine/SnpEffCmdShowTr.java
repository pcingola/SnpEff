package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.ArrayList;
import java.util.HashMap;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Command line program: Show a transcript
 *
 * @author pcingola
 */
public class SnpEffCmdShowTr extends SnpEff {

	ArrayList<String> transcriptIds = new ArrayList<String>();

	public SnpEffCmdShowTr() {
		super();
	}

	/**
	 * Parse command line arguments
	 * @param args
	 */
	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {

			String arg = args[i];

			// Argument starts with '-'?
			if (isOpt(arg)) {
				usage("Unknow option '" + arg + "'");
			} else if (genomeVer.length() <= 0) genomeVer = arg;
			else transcriptIds.add(arg);
		}

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");
		if (transcriptIds.isEmpty()) usage("Missing transcript IDs");
	}

	/**
	 * Run according to command line options
	 */
	@Override
	public boolean run() {
		//---
		// Dump database
		//---
		loadConfig(); // Read config file
		loadDb();

		//---
		// Map all transcript IDs
		//---
		HashMap<String, Transcript> trById = new HashMap<String, Transcript>();
		for (Gene g : config.getGenome().getGenes())
			for (Transcript tr : g)
				trById.put(tr.getId(), tr);

		//---
		// Show all transcripts
		//---
		StringBuilder sb = new StringBuilder();
		for (String trid : transcriptIds) {
			Transcript tr = trById.get(trid);

			if (tr == null) System.err.println("Transcript '" + trid + "' not found.");
			else {
				System.out.println(tr);
				System.out.println(tr.toStringAsciiArt());
				System.out.println("");

				sb.append(tr + "\n");
				sb.append(tr.toStringAsciiArt() + "\n\n");
			}

			// Save output (for debugging)
			if (debug) {
				String fileName = "showTr." + genomeVer + "." + trById + ".txt";
				System.err.println("Saving output to file '" + fileName + "'");
				Gpr.toFile("showtr.txt", sb.toString());
			}
		}

		return true;
	}

	/**
	 * Show 'usage;' message and exit with an error code '-1'
	 * @param message
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff showTr genome_version trId_1 ... trId_N");
		System.exit(-1);
	}
}
