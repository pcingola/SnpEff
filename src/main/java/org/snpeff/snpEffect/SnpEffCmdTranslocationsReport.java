package org.snpeff.snpEffect;

import java.util.List;

import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genes;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.interval.VariantBnd;
import org.snpeff.svg.SvgTranslocation;
import org.snpeff.util.Gpr;
import org.snpeff.util.Timer;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Create an SVG representation of a Marker
 */
public class SnpEffCmdTranslocationsReport extends SnpEff {

	boolean onlyOneTranscript;
	String outPath = ".";
	String vcfFileName = "";

	public SnpEffCmdTranslocationsReport() {
		super();
		nextProt = true;
	}

	Gene findGene(String gene) {
		SnpEffectPredictor sep = config.getSnpEffectPredictor();

		// Look up using geneId
		Gene g = sep.getGene(gene);
		if (g != null) return g;

		// Search using geneName
		Genes genes = sep.getGenome().getGenes();
		g = genes.getGeneByName(gene);
		if (g == null) Timer.showStdErr("Gene '" + gene + "' not found. Skipping plot");

		return g;
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args == null) return;

		this.args = args;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			//---
			// Is it a command line option?
			// Note: Generic options (such as config, verbose, debug, quiet, etc.) are parsed by SnpEff class
			//---
			if (isOpt(arg)) {
				switch (arg.toLowerCase()) {
				case "-onlyonetr":
					onlyOneTranscript = true;
					break;

				case "-outpath":
					if ((i + 1) < args.length) outPath = args[++i];
					else usage("Missing -outPath argument");
					break;

				default:
					usage("Unknown option '" + arg + "'");
				}
			} else if (genomeVer.isEmpty()) genomeVer = arg;
			else if (vcfFileName.isEmpty()) vcfFileName = arg;
			else usage("Unknown parameter '" + arg + "'");
		}

		// Check: Do we have all required parameters?
		if (genomeVer == null || genomeVer.isEmpty()) usage("Missing genomer_version parameter");

		// Check input file
		if (vcfFileName.isEmpty()) vcfFileName = "-"; // Use STDIN as default
		else if (!Gpr.canRead(vcfFileName)) usage("Cannot read input file '" + vcfFileName + "'");
	}

	void report() {
		// Read VCF file (one line)
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (debug) System.out.println(ve);

			List<Variant> vars = ve.variants();
			if (vars.isEmpty()) continue;

			Variant var = vars.get(0);
			if (var.getVariantType() != VariantType.BND) continue;

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (debug) System.out.println("\t" + veff);
				String geneStr = veff.getGeneId();
				String genes[] = geneStr.split("&");

				if (verbose) Timer.showStdErr("Plotting translocation: '" + veff + "'");

				if (genes.length < 2) continue;
				String geneName1 = genes[0];
				String geneName2 = genes[1];
				report((VariantBnd) var, geneName1, geneName2);

			}
		}
	}

	/**
	 * Report a translocation (Gene level)
	 */
	String report(VariantBnd var, String gene1, String gene2) {
		if (debug) System.out.println("\tGenes: " + gene1 + "\t" + gene2);
		Gene g1 = findGene(gene1);
		Gene g2 = findGene(gene2);
		if (g1 == null || g2 == null) return "";

		StringBuilder sb = new StringBuilder();
		for (Transcript tr1 : g1)
			for (Transcript tr2 : g2) {
				sb.append(report(var, tr1, tr2));
				if (onlyOneTranscript) {
					if (verbose) Timer.showStdErr("Plotting only one transcript pair");
					return sb.toString();
				}
			}

		return sb.toString();
	}

	/**
	 * Report a translocation (transcript level)
	 */
	String report(VariantBnd var, Transcript tr1, Transcript tr2) {
		if (debug) System.out.println("\tTranscripts: " + tr1.getId() + "\t" + tr2.getId());
		SvgTranslocation svgTranslocation = new SvgTranslocation(tr1, tr2, var, config.getSnpEffectPredictor());
		StringBuilder sb = new StringBuilder();

		// Plot translocation
		sb.append(svgTranslocation.toString());

		// Save to file
		if (outPath != null && !outPath.isEmpty()) {
			String fileName = outPath + "/" //
					+ var.getChromosomeName() + ":" + (var.getStart() + 1) //
					+ "_" + var.getEndPoint().getChromosomeName() + ":" + (var.getEndPoint().getStart() + 1) //
					+ "-" + tr2.getId() //
					+ ".html";
			Gpr.toFile(fileName, sb);
			if (verbose) Timer.showStdErr("Saved to file " + fileName);
		} else {
			System.out.println(sb);

		}

		return sb.toString();
	}

	@Override
	public boolean run() {
		loadConfig();
		loadDb();
		report();
		return true;
	}

	public void setOnlyOneTranscript(boolean onlyOneTranscript) {
		this.onlyOneTranscript = onlyOneTranscript;
	}

	public void setVcfFileName(String vcfFileName) {
		this.vcfFileName = vcfFileName;
	}

	/**
	 * Show 'usage;' message and exit with an error code '-1'
	 */
	@Override
	public void usage(String message) {
		if (message != null) {
			System.err.println("Error        :\t" + message);
			System.err.println("Command line :\t" + commandLineStr(false) + "\n");
		}

		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff translocReport [options] genome_version input.vcf");
		System.err.println("\n");
		System.err.println("\nOptions:");
		System.err.println("\t-outPath <string>  : Create output files in 'path' (set to empty to disable). Default '.'");
		System.err.println("\t-onlyOneTr         : Report only one transcript.");
	}

}
