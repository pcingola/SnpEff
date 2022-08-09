package org.snpeff.snpEffect.commandLine;

import org.snpeff.SnpEff;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Utr;
import org.snpeff.interval.tree.Itree;
import org.snpeff.util.Log;

/**
 * Command line program: Build database
 *
 * @author pcingola
 */
public class SnpEffCmdDump extends SnpEff {

	public enum DumpFormat {
		DUMP, BED, TXT
	}

	DumpFormat dumpFormat = DumpFormat.DUMP;
	String chrStr = "";

	public SnpEffCmdDump() {
		super();
	}

	/**
	 * Parse command line arguments
	 *
	 * @param args
	 */
	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {

			// Argument starts with '-'?
			if (args[i].startsWith("-")) {
				if (args[i].equalsIgnoreCase("-chr")) chrStr = args[++i];
				else if (args[i].equals("-bed")) {
					dumpFormat = DumpFormat.BED;
					// inOffset = outOffset = 0;
				} else if (args[i].equals("-txt")) {
					dumpFormat = DumpFormat.TXT;
					// inOffset = outOffset = 1;
				} else usage("Unknown option '" + args[i] + "'");
			} else if (genomeVer.length() <= 0) genomeVer = args[i];
			else usage("Unknown parameter '" + args[i] + "'");
		}

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");
	}

	/**
	 * Show all intervals in BED format References:
	 * http://genome.ucsc.edu/FAQ/FAQformat.html#format1
	 */
	void print() {
		// Show title
		if (dumpFormat == DumpFormat.TXT) System.out.println("chr\tstart\tend\tstrand\ttype\tid\tgeneName\tgeneId\tnumberOfTranscripts\tcanonicalTranscriptLength\ttranscriptId\tcdsLength\tnumberOfExons\texonRank\texonSpliceType");

		for (Itree tree : config.getSnpEffectPredictor().getIntervalForest()) {
			for (Marker i : tree) {
				try {
					print(i);

					// Show gene specifics
					if (i instanceof Gene) {
						Gene g = (Gene) i;

						// Show transcripts: UTR and Exons
						for (Transcript t : g) {
							print(t);

							for (Cds c : t.getCds())
								print(c);

							for (Utr u : t.getUtrs())
								print(u);

							for (Exon e : t)
								print(e);

							for (Intron intron : t.introns())
								print(intron);
						}
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		}
	}

	/**
	 * Print a marker
	 */
	void print(Marker marker) {
		if (dumpFormat == DumpFormat.BED) printBed(marker);
		else if (dumpFormat == DumpFormat.TXT) printTxt(marker);
	}

	/**
	 * Show a marker in BED format
	 */
	void printBed(Marker marker) {
		String chr = chrStr + marker.getChromosome().getId();
		int start = marker.getStart(); // The starting position of the feature in the chromosome or scaffold. The first
										// base in a chromosome is numbered 0.
		int end = marker.getEndClosed() + 1; // The ending position of the feature in the chromosome or scaffold. The
										// chromEnd base is not included in the display of the feature.
		String name = marker.getClass().getSimpleName() + "_" + marker.getId();
		System.out.println(chr + "\t" + start + "\t" + end + "\t" + name);
	}

	/**
	 * Print as a TXT format
	 */
	void printTxt(Marker marker) {
		String chr = chrStr + marker.getChromosome().getId();
		int start = marker.getStart(); // The starting position of the feature in the chromosome or scaffold. The first
										// base in a chromosome is numbered 0.
		int end = marker.getEndClosed() + 1; // The ending position of the feature in the chromosome or scaffold. The
										// chromEnd base is not included in the display of the feature.

		StringBuilder info = new StringBuilder();
		info.append(chr);
		info.append("\t" + start);
		info.append("\t" + end);
		info.append("\t" + (marker.isStrandPlus() ? "+1" : "-1"));
		info.append("\t" + marker.getClass().getSimpleName());
		info.append("\t" + marker.getId());

		// Add gene info
		Gene gene = null;
		if (marker instanceof Gene) gene = (Gene) marker;
		else if (marker != null) gene = (Gene) marker.findParent(Gene.class);

		if (gene != null) {
			Transcript canonical = gene.canonical();
			info.append("\t" + gene.getGeneName() //
					+ "\t" + gene.getId() //
					+ "\t" + gene.numChilds() //
					+ "\t" + (canonical == null ? 0 : canonical.cds().length()) //
			);
		} else info.append("\t\t\t\t");

		// Add transcript info
		Transcript tr = null;
		if (marker instanceof Transcript) tr = (Transcript) marker;
		else if (marker != null) tr = (Transcript) marker.findParent(Transcript.class);

		if (tr != null) info.append("\t" + tr.getId() //
				+ "\t" + tr.cds().length() //
				+ "\t" + tr.numChilds() //
		);
		else info.append("\t\t\t");

		// Add exon info
		Exon exon = null;
		if (marker instanceof Exon) exon = (Exon) marker;
		else if (marker != null) exon = (Exon) marker.findParent(Exon.class);

		if (exon != null) info.append("\t" + exon.getRank() //
				+ "\t" + exon.getSpliceType() //
		);
		else info.append("\t\t");

		System.out.println(info);
	}

	/**
	 * Run according to command line options
	 */
	@Override
	public boolean run() {
		// ---
		// Dump database
		// ---
		loadConfig(); // Read config file
		loadDb();

		// Build forest
		if (verbose) Log.info("Building interval forest");
		config.getSnpEffectPredictor().buildForest();
		if (verbose) Log.info("Done.");

		// Dump database
		if (dumpFormat == DumpFormat.DUMP) config.getSnpEffectPredictor().print();
		else if ((dumpFormat == DumpFormat.BED) || (dumpFormat == DumpFormat.TXT)) print();
		else throw new RuntimeException("Unimplemented format '" + dumpFormat + "'");

		return true;
	}

	/**
	 * Show 'usage;' message and exit with an error code '-1'
	 *
	 * @param message
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff dump [options] genome_version");
		System.err.println("\t-bed                    : Dump in BED format (implies -0)");
		System.err.println("\t-chr <string>           : Prepend 'string' to chromosome name (e.g. 'chr1' instead of '1')");
		System.err.println("\t-txt                    : Dump as a TXT table (implies -1)");
		System.err.println("\nGeneric options:");
		System.err.println("\t-0                      : Output zero-based coordinates. ");
		System.err.println("\t-1                      : Output one-based coordinates");
		System.exit(-1);
	}
}
