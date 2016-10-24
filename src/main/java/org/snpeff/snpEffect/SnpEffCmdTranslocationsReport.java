package org.snpeff.snpEffect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
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
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.svg.SvgTranslocation;
import org.snpeff.util.Gpr;
import org.snpeff.util.Timer;
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

	public static final String REPORT_TEMPLATE = "transcript_report.ftl";
	public static final String DEFAULT_REPORT_HTML_FILE = "transcript_report.html";

	boolean onlyOneTranscript;
	boolean useLocalTemplate = false; // Use template from 'local' file instead of 'jar' (this is only used for development and debugging)
	String outPath = ".";
	String reportFile = DEFAULT_REPORT_HTML_FILE;
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
		summary(REPORT_TEMPLATE, reportFile, false);
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
			error(e, "Error creating summary: " + e.getMessage());
			return false;
		} catch (TemplateException e) {
			error(e, "Error creating summary: " + e.getMessage());
			return false;
		}

		return true;
	}

	HashMap<String, Object> summaryCreateHash() {
		// Create the root hash (where data objects are)
		HashMap<String, Object> root = new HashMap<>();
		//		root.put("args", commandLineStr(createSummaryCsv ? false : true));
		//		root.put("changeStats", variantEffectStats);
		//		root.put("chromoPlots", chromoPlots);
		//		root.put("countEffects", countEffects);
		//		root.put("countInputLines", countInputLines);
		//		root.put("countVariants", countVariants);
		//		root.put("date", String.format("%1$TY-%1$Tm-%1$Td %1$TH:%1$TM", new Date()));
		//		root.put("genesFile", Gpr.baseName(summaryGenesFile, ""));
		//		root.put("genome", config.getGenome());
		//		root.put("genomeVersion", genomeVer);
		//		root.put("variantEffectResutFilter", variantEffectResutFilter);
		//		root.put("variantStats", variantStats);
		//		root.put("snpEffectPredictor", config.getSnpEffectPredictor());
		//		root.put("vcfStats", vcfStats);
		root.put("version", SnpEff.VERSION); // Version used

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
		System.err.println("\t-outPath <dir>     : Create output files in 'path' (set to empty to disable). Default '.'");
		System.err.println("\t-onlyOneTr         : Report only one transcript.");
		System.err.println("\t-report <file>     : Output report file name. Default: " + reportFile);
	}

}
