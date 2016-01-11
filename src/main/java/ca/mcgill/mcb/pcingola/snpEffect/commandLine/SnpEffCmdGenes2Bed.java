package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.HashSet;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Simple test program
 * @author pcingola
 */
public class SnpEffCmdGenes2Bed extends SnpEff {

	HashSet<String> geneIds;
	String fileName = null;
	boolean onlyProteinCoding;
	boolean showExons;
	int expandUpstreamDownstream = 0;

	public static void main(String[] args) {
		SnpEffCmdGenes2Bed conf2down = new SnpEffCmdGenes2Bed();
		conf2down.parseArgs(args);
		conf2down.run();
	}

	public SnpEffCmdGenes2Bed() {
		super();
		geneIds = new HashSet<String>();
	}

	void loadGenes() {
		// Parse file
		if (fileName != null) {
			if (verbose) Timer.showStdErr("Loading genes list from file '" + fileName + "'");

			String lines[] = Gpr.readFile(fileName).split("\n");
			if (lines.length <= 0) throw new RuntimeException("Cannot read file '" + fileName + "'");

			for (String line : lines) {
				String id = line.trim();
				if (!id.isEmpty()) geneIds.add(id);
			}
		}
	}

	@Override
	public void parseArgs(String[] args) {
		if (args.length < 1) usage(null);

		// Parse command line arguments
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (isOpt(arg)) {
				switch (arg.toLowerCase()) {
				case "-e":
					// Show exons for all transcripts
					showExons = true;
					break;

				case "-f":
					// List in a file?
					if ((i + 1) < args.length) fileName = args[++i];
					else usage("Option '-f' without file argument");
					break;

				case "-pc":
					// Use only protein coding genes
					onlyProteinCoding = true;
					break;

				case "-ud":
					// Expand upstream & downstream
					if ((i + 1) < args.length) expandUpstreamDownstream = Gpr.parseIntSafe(args[++i]);
					else usage("Option '-ud' without file argument");
					break;

				default:
					usage("Unknown commnand line option '" + arg + "'");
				}
			} else if ((genomeVer == null) || genomeVer.isEmpty()) {
				// Genome version
				genomeVer = args[i];
			} else geneIds.add(args[i]);
		}
	}

	@Override
	public boolean run() {
		loadGenes();
		boolean isEmpty = (geneIds.size() <= 0);
		if (verbose) {
			Timer.showStdErr("Number of gene IDs to look up: " + geneIds.size());
			if (isEmpty) Timer.showStdErr("Empty list of IDs. Using all genes.");
		}

		// Load config & database
		loadConfig();
		loadDb();

		// Show title
		if (showExons) System.out.println("#chr\tstart\tend\tgeneName;geneId;transcriptId;exonRank");
		else System.out.println("#chr\tstart\tend\tgeneName;geneId");

		// Find genes
		Genome genome = config.getGenome();
		if (verbose) Timer.showStdErr("Finding genes.");
		int found = 0, filtered = 0;
		for (Gene g : genome.getGenesSortedPos()) {
			// Is gene.id or gene.name in geneSet? => Show it
			if (isEmpty //
					|| geneIds.contains(g.getId()) // Is the geneId in the list?
					|| geneIds.contains(g.getGeneName()) // Id the geneName in the list?
			) {
				found++;

				// Show or filter?
				if (!onlyProteinCoding || g.isProteinCoding()) show(g);
				else filtered++;
			}
		}

		if (verbose) {
			Timer.showStdErr("Done\n\tFound      : " + found + " / " + geneIds.size() //
					+ (filtered > 0 ? "\n\tFiltered out : " + filtered + " / " + found : "") //
			);
		}

		return true;
	}

	/**
	 * Show either a gene or all exons for all transcripts within a gene 
	 */
	void show(Gene g) {
		// Expand interval
		int start = g.getStart() - expandUpstreamDownstream;
		int end = g.getEnd() + 1 + expandUpstreamDownstream;

		System.out.println(g.getChromosomeName() //
				+ "\t" + start //
				+ "\t" + end //
				+ "\t" + g.getGeneName() //
				+ ";" + g.getId() //
		);

		// Show exon information as well
		if (showExons) {
			for (Transcript tr : g) {
				for (Exon ex : tr) {
					start = ex.getStart() - expandUpstreamDownstream;
					end = ex.getEnd() + 1 + expandUpstreamDownstream;

					System.out.println(ex.getChromosomeName() //
							+ "\t" + start //
							+ "\t" + end //
							+ "\t" + g.getGeneName() //
							+ ";" + g.getId() //
							+ ";" + tr.getId() //
							+ ";" + ex.getRank() //
					);
				}
			}
		}
	}

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("Usage: " + SnpEffCmdGenes2Bed.class.getSimpleName() + " genomeVer [-f genes.txt | geneList]}");
		System.err.println("Options: ");
		System.err.println("\t-e             : Show exons.");
		System.err.println("\t-f <file.txt>  : A TXT file having one gene ID (or name) per line.");
		System.err.println("\t-pc            : Use only protein coding genes.");
		System.err.println("\t-ud <num>      : Expand gene interval upstream and downstream by 'num' bases.");
		System.err.println("\tgeneList       : A list of gene IDs or names. One per command line argument: geneId_1 geneId_2 geneId_3 ... geneId_N");
		System.exit(-1);
	}
}
