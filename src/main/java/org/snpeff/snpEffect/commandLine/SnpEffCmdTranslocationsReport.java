package org.snpeff.snpEffect.commandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genes;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.interval.VariantBnd;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.stats.TranslocationReport;
import org.snpeff.svg.SvgTranslocation;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Create an SVG representation of a Marker
 */
public class SnpEffCmdTranslocationsReport extends SnpEff {

	public static final String REPORT_TEMPLATE = "translocations_report.ftl";
	public static final String DEFAULT_REPORT_HTML_FILE = "translocations_report.html";

	boolean onlyOneTranscript;
	boolean useLocalTemplate = false; // Use template from 'local' file instead of 'jar' (this is only used for development and debugging)
	int countTranslocations;
	String outPath = "";
	String reportFile = DEFAULT_REPORT_HTML_FILE;
	String vcfFileName = "";
	List<TranslocationReport> translocationReports;

	public SnpEffCmdTranslocationsReport() {
		super();
		nextProt = true;
	}

	/**
	 * Filter according to variant types
	 */
	boolean filterVariants(Variant var) {
		VariantType vt = var.getVariantType();
		return vt != VariantType.BND //
				&& vt != VariantType.DUP //
				&& vt != VariantType.DEL //
		;
	}

	/**
	 * Find gene from gene name
	 */
	Gene findGene(String gene) {
		SnpEffectPredictor sep = config.getSnpEffectPredictor();

		// Look up using geneId
		Gene g = sep.getGene(gene);
		if (g != null) return g;

		// Search using geneName
		Genes genes = sep.getGenome().getGenes();
		g = genes.getGeneByName(gene);
		if (g == null) Log.info("Gene '" + gene + "' not found. Skipping plot");

		return g;
	}

	/**
	 * Is this a translocation effect?
	 */
	boolean isTranslocation(EffectType effType) {
		return effType == EffectType.GENE_FUSION //
				|| effType == EffectType.GENE_FUSION_REVERESE //
				|| effType == EffectType.GENE_FUSION_HALF //
		;
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

				case "-report":
					if ((i + 1) < args.length) reportFile = args[++i];
					else usage("Missing -report argument");
					break;

				case "-uselocaltemplate": // Undocumented option (only used for development & debugging)
					useLocalTemplate = true;
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

	/**
	 * Create report including all translocations in VCF file
	 */
	void report() {
		translocationReports = new ArrayList<>();

		// Read VCF file (one line)
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (debug) System.out.println(ve);

			List<Variant> vars = ve.variants();
			if (vars.isEmpty()) continue;

			Variant var = vars.get(0);
			if (filterVariants(var)) continue;

			countTranslocations++;

			report(ve, var);
		}
	}

	/**
	 * Report a trnaslocation
	 */
	void report(VcfEntry ve, Variant var) {
		for (VcfEffect veff : ve.getVcfEffects()) {
			if (!isTranslocation(veff.getEffectType())) continue;

			if (debug) System.out.println("\t" + veff);
			String geneStr = veff.getGeneId();
			String genes[] = geneStr.split("&");

			if (verbose) Log.info("Plotting translocation: '" + veff + "'");

			if (genes.length < 2) continue;
			String geneName1 = genes[0];
			String geneName2 = genes[1];

			reportBnd(variantBnd(var), veff, geneName1, geneName2);
		}
	}

	/**
	 * Report a translocation (Gene level)
	 */
	String reportBnd(VariantBnd varBnd, VcfEffect veff, String gene1, String gene2) {
		if (debug) System.out.println("\tGenes: " + gene1 + "\t" + gene2);
		Gene g1 = findGene(gene1);
		Gene g2 = findGene(gene2);
		if (g1 == null || g2 == null) return "";

		StringBuilder sb = new StringBuilder();
		for (Transcript tr1 : g1)
			for (Transcript tr2 : g2) {
				sb.append(reportBnd(varBnd, veff, tr1, tr2));
				if (onlyOneTranscript) {
					if (verbose) Log.info("Plotting only one transcript pair");
					return sb.toString();
				}
			}

		return sb.toString();
	}

	/**
	 * Report a translocation (transcript level)
	 */
	TranslocationReport reportBnd(VariantBnd varBnd, VcfEffect veff, Transcript tr1, Transcript tr2) {
		if (debug) System.out.println("\tTranscripts: " + tr1.getId() + "\t" + tr2.getId());

		SvgTranslocation svgTranslocation = new SvgTranslocation(tr1, tr2, varBnd, config.getSnpEffectPredictor());

		// Plot translocation
		String svgPlot = svgTranslocation.toString();

		// Create and add translocation report
		TranslocationReport trRep = new TranslocationReport(varBnd, veff, tr1, tr2);
		trRep.setSvgPlot(svgPlot);
		translocationReports.add(trRep);

		// Save to separate files
		if (outPath != null && !outPath.isEmpty()) {
			String fileName = outPath + "/" //
					+ varBnd.getChromosomeName() + ":" + (varBnd.getStart() + 1) //
					+ "_" + varBnd.getEndPoint().getChromosomeName() + ":" + (varBnd.getEndPoint().getStart() + 1) //
					+ "-" + tr2.getId() //
					+ ".html";
			Gpr.toFile(fileName, svgPlot);
			if (verbose) Log.info("Saved to file " + fileName);
		}

		return trRep;
	}

	@Override
	public boolean run() {
		loadConfig();
		loadDb();
		report();
		summary(REPORT_TEMPLATE, reportFile, false);
		if (verbose) Log.info("Done.");
		return true;
	}

	public void setOnlyOneTranscript(boolean onlyOneTranscript) {
		this.onlyOneTranscript = onlyOneTranscript;
	}

	public void setVcfFileName(String vcfFileName) {
		this.vcfFileName = vcfFileName;
	}

	boolean summary(String templateFile, String outputFile, boolean noCommas) {
		try {
			if (verbose) Log.info("Creating report file '" + outputFile + "'");
			// Configure FreeMaker
			Configuration cfg = new Configuration();

			// Specify the data source where the template files come from
			if (useLocalTemplate) cfg.setDirectoryForTemplateLoading(new File("./templates/")); // Use local 'template' directory
			else cfg.setClassForTemplateLoading(SnpEffCmdEff.class, "/"); // Use current directory in JAR file

			cfg.setObjectWrapper(new DefaultObjectWrapper()); // Specify how templates will see the data-model. This is an advanced topic...
			cfg.setLocale(java.util.Locale.US);
			if (noCommas) cfg.setNumberFormat("0.######");

			// Create the root hash (where data objects are)
			HashMap<String, Object> root = summaryCreateHash();

			// Get the template
			Template temp = cfg.getTemplate(templateFile);

			// Process the template
			Writer out = new OutputStreamWriter(new FileOutputStream(new File(outputFile)));
			temp.process(root, out);
			out.flush();
			out.close();
		} catch (IOException e) {
			Log.error(e, "Error creating summary: " + e.getMessage());
			return false;
		} catch (TemplateException e) {
			Log.error(e, "Error creating summary: " + e.getMessage());
			return false;
		}

		return true;
	}

	HashMap<String, Object> summaryCreateHash() {
		// Create the root hash (where data objects are)
		HashMap<String, Object> root = new HashMap<>();
		root.put("args", commandLineStr(true));
		root.put("countTranslocations", countTranslocations);
		root.put("date", String.format("%1$TY-%1$Tm-%1$Td %1$TH:%1$TM", new Date()));
		root.put("translocations", translocationReports);
		root.put("version", SnpEff.VERSION);
		return root;
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
		System.err.println("\t-onlyOneTr         : Report only one transcript (used for debugging).");
		System.err.println("\t-outPath <dir>     : Create output SVG files for each translocation in 'path' (set to empty to disable). Default '" + outPath + "'");
		System.err.println("\t-report <file>     : Output report file name. Default: " + reportFile);
		System.exit(-1);
	}

	/**
	 * Create a VariantBnd from a Variant (could be a <DUP> or <DEL>)
	 */
	VariantBnd variantBnd(Variant var) {
		// BND? Nothing to do
		if (var.isBnd()) return (VariantBnd) var;

		if (var.isDup() || var.isDel()) {
			// Create a BND variant
			return new VariantBnd(var.getChromosome(), var.getStart(), "N", "N", var.getChromosome(), var.getEndClosed(), false, false);
		}

		throw new RuntimeException("Unsupported variant type '" + var.getVariantType() + "'");
	}

}
