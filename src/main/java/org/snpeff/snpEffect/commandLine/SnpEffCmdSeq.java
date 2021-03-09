package org.snpeff.snpEffect.commandLine;

import java.util.ArrayList;

import org.snpeff.SnpEff;
import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.interval.Genome;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Command line program: Show a transcript or a gene
 *
 * @author pcingola
 */
public class SnpEffCmdSeq extends SnpEff {

	boolean reverseWc = false;
	ArrayList<String> sequences = new ArrayList<String>();

	public SnpEffCmdSeq() {
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

				switch (arg.toLowerCase()) {
				case "-r":
					reverseWc = true;
					break;

				default:
					usage("Unknown option '" + arg + "'");
				}
			} else if (genomeVer.length() <= 0) genomeVer = arg;
			else sequences.add(arg);
		}

		if (genomeVer == null || genomeVer.isEmpty()) usage("Missing genome");
		if (sequences.isEmpty()) usage("Missing sequences");

		// Do not log this command
		log = false;
	}

	/**
	 * Run according to command line options
	 */
	@Override
	public boolean run() {
		loadConfig();

		// Find codon table
		Genome genome = config.getGenome();
		CodonTable codonTable = CodonTables.getInstance().getTable(genome);
		if (codonTable == null) {
			Log.fatalError("Could not find codon table for genome '" + genome.getId() + "'");
		}

		// Translate sequences
		for (String seq : sequences) {
			System.out.println("Sequence                   : " + seq);

			if (reverseWc) {
				seq = GprSeq.reverseWc(seq);
				System.out.println("Complement                 : " + seq);
			}

			String aa = codonTable.aa(seq);
			String aa3 = codonTable.aaThreeLetterCode(aa);

			StringBuilder aasp = new StringBuilder();
			for (char c : aa.toCharArray())
				aasp.append(" " + c + " ");

			System.out.println("Protein (3-Letter)         : " + aa3);
			System.out.println("Protein (1-Letter-space)   : " + aasp);
			System.out.println("Protein (1-Letter)         : " + aa + "\n");
		}

		return true;
	}

	/**
	 * Show 'usage' message and exit with an error code '-1'
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff seq [-r] genome seq_1 seq_2 ... seq_N");
		System.err.println("\t-r : Reverse-Watson-Cricks complement.");
		System.exit(-1);
	}
}
