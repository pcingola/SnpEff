package org.snpeff.snpEffect.commandLine;

import java.util.HashSet;

import org.snpeff.SnpEff;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Simple test program
 * @author pcingola
 */
public class SnpEffCmdGenes2Bed extends SnpEff {

	HashSet<String> geneIds;
	String fileName = null;
	boolean onlyProteinCoding;
	boolean showCds;
	boolean showExons;
	boolean showIntrons;
	boolean showTranscripts;
	int expandUpstreamDownstream = 0;

	public static void main(String[] args) {
		SnpEffCmdGenes2Bed conf2down = new SnpEffCmdGenes2Bed();
		conf2down.parseArgs(args);
		conf2down.run();
	}

	public SnpEffCmdGenes2Bed() {
		super();
		geneIds = new HashSet<>();
	}

	void loadGenes() {
		// Parse file
		if (fileName != null) {
			if (verbose) Log.info("Loading genes list from file '" + fileName + "'");

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
				case "-cds":
					// Show exons in CDS
					showCds = true;
					break;

				case "-e":
					// Show exons for all transcripts
					showExons = true;
					break;

				case "-f":
					// List in a file?
					if ((i + 1) < args.length) fileName = args[++i];
					else usage("Option '-f' without file argument");
					break;

				case "-i":
					// Show introns for all transcripts
					showIntrons = true;
					break;

				case "-pc":
					// Use only protein coding genes
					onlyProteinCoding = true;
					break;

				case "-tr":
					// Show transcripts
					showTranscripts = true;
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

		int countMutex = 0;
		if (showCds) countMutex++;
		if (showExons) countMutex++;
		if (showTranscripts) countMutex++;
		if (countMutex > 1) usage("Options '-e', '-cds' and '-tr' are mutually exclusive");
	}

	@Override
	public boolean run() {
		loadGenes();
		if (verbose) {
			Log.info("Number of gene IDs to look up: " + geneIds.size());
			if (geneIds.isEmpty()) Log.info("Empty list of IDs. Using all genes.");
		}

		// Load config & database
		loadConfig();
		loadDb();

		// Show genes
		showGenes();

		return true;
	}

	/**
	 * Show either a gene or all exons for all transcripts within a gene
	 */
	void show(Gene g) {
		if (showCds) showCds(g);
		else if (showExons) showExons(g);
		else if (showIntrons) showIntrons(g);
		else if (showTranscripts) showTr(g);
		else showGene(g);
	}

	/**
	 * Show CDS coordinates
	 */
	void showCds(Gene g) {
		for (Transcript tr : g) {
			for (Exon ex : tr) {
				Cds cds = tr.findCds(ex); // Find corresponding CDS

				if (cds != null) {
					int start = cds.getStart() - expandUpstreamDownstream;
					int end = cds.getEndClosed() + 1 + expandUpstreamDownstream;

					System.out.println(cds.getChromosomeName() //
							+ "\t" + start //
							+ "\t" + end //
							+ "\t" + g.getGeneName() //
							+ ";" + g.getId() //
							+ ";" + tr.getId() //
							+ ";" + ex.getRank() //
							+ ";" + (ex.isStrandPlus() ? "+" : "-") //
					);
				}
			}
		}
	}

	/**
	 * Show exons coordinates
	 */
	void showExons(Gene g) {
		// Show exon for each transcript
		for (Transcript tr : g) {
			for (Exon ex : tr.sorted()) {
				int start = ex.getStart() - expandUpstreamDownstream;
				int end = ex.getEndClosed() + 1 + expandUpstreamDownstream;

				System.out.println(ex.getChromosomeName() //
						+ "\t" + start //
						+ "\t" + end //
						+ "\t" + g.getGeneName() //
						+ ";" + g.getId() //
						+ ";" + tr.getId() //
						+ ";" + ex.getRank() //
						+ ";" + (ex.isStrandPlus() ? "+" : "-") //
				);
			}
		}
	}

	/**
	 * Show a gene coordiantes
	 */
	void showGene(Gene g) {
		// Show gene
		int start = g.getStart() - expandUpstreamDownstream;
		int end = g.getEndClosed() + 1 + expandUpstreamDownstream;

		System.out.println(g.getChromosomeName() //
				+ "\t" + start //
				+ "\t" + end //
				+ "\t" + g.getGeneName() //
				+ ";" + g.getId() //
				+ ";" + (g.isStrandPlus() ? "+" : "-") //
		);
	}

	/**
	 * Show genes in BED format
	 */
	public void showGenes() {
		// Show title
		if (showCds) System.out.println("#chr\tstart\tend\tgeneName;geneId;transcriptId;exonRank;strand");
		else if (showExons) System.out.println("#chr\tstart\tend\tgeneName;geneId;transcriptId;exonRank;strand");
		else if (showIntrons) System.out.println("#chr\tstart\tend\tgeneName;geneId;transcriptId;exonRank;strand");
		else if (showTranscripts) System.out.println("#chr\tstart\tend\tgeneName;geneId;transcriptId;strand");
		else System.out.println("#chr\tstart\tend\tgeneName;geneId;strand");

		// Find genes
		Genome genome = config.getGenome();
		if (verbose) Log.info("Finding genes.");
		int found = 0, filtered = 0;
		for (Gene g : genome.getGenesSortedPos()) {
			// Is gene.id or gene.name in geneSet? => Show it
			if (geneIds.isEmpty() // Empty set means 'use all genes'
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
			Log.info("Done\n\tFound      : " + found + " / " + geneIds.size() //
					+ (filtered > 0 ? "\n\tFiltered out : " + filtered + " / " + found : "") //
			);
		}
	}

	/**
	 * Show exons coordinates
	 */
	void showIntrons(Gene g) {
		// Show exon for each transcript
		for (Transcript tr : g) {
			for (Intron intr : tr.introns()) {
				int start = intr.getStart() - expandUpstreamDownstream;
				int end = intr.getEndClosed() + 1 + expandUpstreamDownstream;

				System.out.println(intr.getChromosomeName() //
						+ "\t" + start //
						+ "\t" + end //
						+ "\t" + g.getGeneName() //
						+ ";" + g.getId() //
						+ ";" + tr.getId() //
						+ ";" + intr.getRank() //
						+ ";" + (intr.isStrandPlus() ? "+" : "-") //
				);
			}
		}
	}

	/**
	 * Show transcript coordinates
	 */
	void showTr(Gene g) {
		for (Transcript tr : g) {
			int start = tr.getStart() - expandUpstreamDownstream;
			int end = tr.getEndClosed() + 1 + expandUpstreamDownstream;

			System.out.println(tr.getChromosomeName() //
					+ "\t" + start //
					+ "\t" + end //
					+ "\t" + g.getGeneName() //
					+ ";" + g.getId() //
					+ ";" + tr.getId() //
					+ ";" + (tr.isStrandPlus() ? "+" : "-") //
			);
		}
	}

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("Usage: " + SnpEffCmdGenes2Bed.class.getSimpleName() + " genomeVer [-f genes.txt | geneList]}");
		System.err.println("Options: ");
		System.err.println("\t-cds           : Show coding exons (no UTRs).");
		System.err.println("\t-e             : Show exons for every transcript.");
		System.err.println("\t-f <file.txt>  : A TXT file having one gene ID (or name) per line.");
		System.err.println("\t-i             : Show introns for every transcript.");
		System.err.println("\t-pc            : Use only protein coding genes.");
		System.err.println("\t-tr            : Show transcript coordinates.");
		System.err.println("\t-ud <num>      : Expand gene interval upstream and downstream by 'num' bases.");
		System.err.println("\tgeneList       : A list of gene IDs or names. One per command line argument: geneId_1 geneId_2 geneId_3 ... geneId_N");
		System.exit(-1);
	}
}
