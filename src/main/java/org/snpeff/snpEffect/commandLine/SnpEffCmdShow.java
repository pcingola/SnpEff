package org.snpeff.snpEffect.commandLine;

import java.util.ArrayList;
import java.util.HashMap;

import org.snpeff.SnpEff;
import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Gpr;

/**
 * Command line program: Show a transcript or a gene
 *
 * @author pcingola
 */
public class SnpEffCmdShow extends SnpEff {

	ArrayList<String> transcriptIds = new ArrayList<String>();

	public SnpEffCmdShow() {
		super();
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {

			String arg = args[i];

			// Argument starts with '-'?
			if (isOpt(arg)) {
				usage("Unknown option '" + arg + "'");
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

		System.out.println("Showing genes and transcripts using zero-based coordinates");

		//---
		// Map all transcript IDs
		//---
		HashMap<String, Transcript> trById = new HashMap<String, Transcript>();
		HashMap<String, Gene> geneById = new HashMap<String, Gene>();
		for (Gene g : config.getGenome().getGenes()) {
			geneById.put(g.getId(), g);
			for (Transcript tr : g)
				trById.put(tr.getId(), tr);
		}

		//---
		// Show all transcripts
		//---
		StringBuilder sb = new StringBuilder();
		for (String id : transcriptIds) {

			Gene gene = geneById.get(id);

			Transcript tr = trById.get(id);

			if (gene != null) showGene(gene, sb);
			else if (tr != null) showTranscript(tr, sb);
			else System.err.println("ID '" + id + "' not found.");

			// Save output (for debugging)
			if (debug) {
				String fileName = "showTr." + genomeVer + "." + trById + ".txt";
				System.err.println("Saving output to file '" + fileName + "'");
				Gpr.toFile("showtr.txt", sb.toString());
			}
		}

		return true;
	}

	void showGene(Gene g, StringBuilder sb) {
		System.out.println("Gene:\t" + g.toString(false));

		for (Transcript tr : g)
			showTranscript(tr, sb);
	}

	void showTranscript(Transcript tr, StringBuilder sb) {
		String trStr = tr.toString(true);
		String art = tr.toStringAsciiArt(true);

		CodonTable codonTable = tr.getChromosome().getCodonTable();

		System.out.println("Transcript (codon table: " + codonTable.getName() + " ) :\t" + trStr);
		System.out.println(Gpr.prependEachLine("\t\t", art) + "\n");

		sb.append(trStr + "\n");
		sb.append(art + "\n\n");
	}

	/**
	 * Show 'usage;' message and exit with an error code '-1'
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff show genome_version gene_1 ... gene_N ... trId_1 ... trId_N");
		System.exit(-1);
	}
}
