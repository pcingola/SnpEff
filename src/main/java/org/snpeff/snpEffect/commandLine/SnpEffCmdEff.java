package org.snpeff.snpEffect.commandLine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.snpeff.SnpEff;
import org.snpeff.fileIterator.BedFileIterator;
import org.snpeff.fileIterator.VariantFileIterator;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.filter.VariantEffectFilter;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantNonRef;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.outputFormatter.BedAnnotationOutputFormatter;
import org.snpeff.outputFormatter.BedOutputFormatter;
import org.snpeff.outputFormatter.OutputFormatter;
import org.snpeff.outputFormatter.VcfOutputFormatter;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.VcfAnnotator;
import org.snpeff.stats.CountByType;
import org.snpeff.stats.VariantEffectStats;
import org.snpeff.stats.VariantStats;
import org.snpeff.stats.VcfStats;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.util.Timer;
import org.snpeff.util.Tuple;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.Pedigree;
import org.snpeff.vcf.VcfEntry;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Command line program: Predict variant effects
 *
 * @author Pablo Cingolani
 */
public class SnpEffCmdEff extends SnpEff implements VcfAnnotator {

	public static final String TEMPLATES_DIR = "src/main/resources";
	public static final String SUMMARY_TEMPLATE = "snpEff_summary.ftl"; // Summary template file name
	public static final String SUMMARY_CSV_TEMPLATE = "snpEff_csv_summary.ftl"; // Summary template file name
	public static final String SUMMARY_GENES_TEMPLATE = "snpEff_genes.ftl"; // Genes template file name

	public static final String DEFAULT_SUMMARY_HTML_FILE = "snpEff_summary.html";
	public static final String DEFAULT_SUMMARY_CSV_FILE = "snpEff_summary.csv";
	public static final String DEFAULT_SUMMARY_GENES_FILE = "snpEff_genes.txt";

	public static final int SHOW_EVERY = 10 * 1000;

	boolean anyCancerSample;
	boolean cancer = false; // Perform cancer comparisons
	boolean chromoPlots = true; // Create mutations by chromosome plots?
	boolean createSummaryCsv = false;
	boolean createSummaryHtml = true;
	boolean lossOfFunction = true; // Create loss of function LOF tag?
	boolean useGeneId = false; // Use gene ID instead of gene name (VCF output)
	boolean useLocalTemplate = false; // Use template from 'local' file instead of 'jar' (this is only used for development and debugging)
	boolean useOicr = false; // Use OICR tag
	boolean useSequenceOntology = true; // Use Sequence Ontology terms
	int totalErrs = 0;
	int countVcfEntries = 0;
	long countInputLines = 0;
	long countVariants = 0;
	long countEffects = 0;
	String cancerSamples = null;
	String chrStr = "";
	String inputFile = ""; // Input file
	String fastaProt = null;
	String summaryFileCsv; // HTML Summary file name
	String summaryFileHtml; // CSV Summary file name
	String summaryGenesFile; // Gene table file
	InputFormat inputFormat = InputFormat.VCF; // Format use in input files
	OutputFormat outputFormat = OutputFormat.VCF; // Output format
	VariantEffectFilter variantEffectResutFilter; // Filter prediction results
	ArrayList<String> filterIntervalFiles;// Files used for filter intervals
	ArrayList<String> inputFiles;
	IntervalForest filterIntervals; // Filter only variants that match these intervals
	VariantStats variantStats;
	VariantEffectStats variantEffectStats;
	SnpEffectPredictor snpEffectPredictor;
	VcfStats vcfStats;
	List<VcfEntry> vcfEntriesDebug = null; // Use for debugging or testing (in some test-cases)
	EffFormatVersion formatVersion = EffFormatVersion.DEFAULT_FORMAT_VERSION;
	Pedigree pedigree;
	CountByType errByType, warnByType;
	OutputFormatter outputFormatter = null;
	Timer annotateTimer;

	public SnpEffCmdEff() {
		super();
		chrStr = ""; // Default: Don't show 'chr' before chromosome
		inputFile = ""; // variant input file
		variantEffectResutFilter = new VariantEffectFilter(); // Filter prediction results
		filterIntervalFiles = new ArrayList<>(); // Files used for filter intervals
		summaryFileHtml = DEFAULT_SUMMARY_HTML_FILE;
		summaryFileCsv = DEFAULT_SUMMARY_CSV_FILE;
		summaryGenesFile = DEFAULT_SUMMARY_GENES_FILE;
	}

	@Override
	public boolean addHeaders(VcfFileIterator vcfFile) {
		// This is done by VcfOutputFormatter, so there is nothing to do here.
		return false;
	}

	/**
	 * Annotate: Calculate the effect of variants and show results
	 */
	public boolean annotate(String inputFile, String outputFile) {
		// Initialize
		annotateInit(outputFile);
		VcfFileIterator vcf = null;

		// Iterate over input files
		switch (inputFormat) {
		case VCF:
			vcf = annotateVcf(inputFile);
			break;

		case BED:
			annotateBed(inputFile, outputFormatter);
			break;

		default:
			throw new RuntimeException("Cannot create variant file iterator on input format '" + inputFormat + "'");
		}
		outputFormatter.close();

		// Create reports and finish up
		boolean err = annotateFinish(vcf);

		return !err;
	}

	/**
	 * Annotate a VCF entry
	 */
	@Override
	public boolean annotate(VcfEntry vcfEntry) {
		boolean printed = false;
		boolean filteredOut = false;
		VcfFileIterator vcfFile = vcfEntry.getVcfFileIterator();

		try {
			countInputLines++;
			countVcfEntries++;

			// Find if there is a pedigree and if it has any 'derived' entry
			if (vcfFile.isHeadeSection()) {
				if (cancer) {
					pedigree = readPedigree(vcfFile);
					anyCancerSample = pedigree.anyDerived();
				}
			}

			// VCF entry statistics
			if (createSummaryHtml || createSummaryCsv) vcfStats.sample(vcfEntry);

			// Skip if there are filter intervals and they are not matched
			if ((filterIntervals != null) && (filterIntervals.query(vcfEntry).isEmpty())) {
				filteredOut = true;
				return false;
			}

			// Create new 'section'
			outputFormatter.startSection(vcfEntry);

			// ---
			// Analyze all changes in this VCF entry
			// Note, this is the standard analysis.
			// Next section deals with cancer: Somatic vs Germline comparisons
			// ---
			boolean impactLowOrHigher = false; // Does this entry have an impact (other than MODIFIER)?
			List<Variant> variants = vcfEntry.variants();
			for (Variant variant : variants) {
				// Show progress
				showProgress();

				// Annotate variant
				impactLowOrHigher |= annotateVariant(variant);
			}

			// Perform cancer annotations
			if (anyCancerSample && impactLowOrHigher) annotateVariantCancer(variants, vcfEntry);

			// Finish up this section
			outputFormatter.printSection(vcfEntry);

			printed = true;
		} catch (Throwable t) {
			totalErrs++;
			Log.error(t, "Error while processing VCF entry (line " + vcfFile.getLineNum() + ") :\n\t" + vcfEntry + "\n" + t);
		} finally {
			if (!printed && !filteredOut) outputFormatter.printSection(vcfEntry);
		}

		return true;
	}

	/**
	 * Iterate on all inputs and calculate effects. Note: This is used for all input
	 * formats except VCF, which has a different iteration modality
	 */
	void annotateBed(String inputFile, OutputFormatter outputFormatter) {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		// Create an input file iterator
		VariantFileIterator variantFileIterator = new BedFileIterator(inputFile, config.getGenome());

		// Iterate over input file
		for (Variant variant : variantFileIterator) {
			try {
				countInputLines++;

				countVariants++;
				if (verbose && (countVariants % SHOW_EVERY == 0)) Log.info("\t" + countVariants + " variants");

				// Skip if there are filter intervals and they are not matched
				if ((filterIntervals != null) && (filterIntervals.stab(variant).size() <= 0)) continue;

				// Perform basic statistics about this variant
				if (createSummaryHtml || createSummaryCsv) variantStats.sample(variant);

				// Calculate effects
				VariantEffects variantEffects = snpEffectPredictor.variantEffect(variant);

				// Create new 'section'
				outputFormatter.startSection(variant);

				// Show results
				for (VariantEffect variantEffect : variantEffects) {
					variantEffectStats.sample(variantEffect); // Perform basic statistics about this result
					outputFormatter.add(variantEffect);
					countEffects++;
				}

				// Finish up this section
				outputFormatter.printSection(variant);
			} catch (Throwable t) {
				totalErrs++;
				Log.error(t, "Error while processing variant (line " + variantFileIterator.getLineNum() + ") :\n\t" + variant + "\n" + t);
			}
		}

		// Close file iterator (not really needed, but just in case)
		variantFileIterator.close();
	}

	/**
	 * Finish annotations and create reports
	 */
	@Override
	public boolean annotateFinish(VcfFileIterator vcfFile) {
		boolean ok = true;

		if (vcfFile != null) vcfFile.close();

		// Creates a summary output file
		if (createSummaryCsv) {
			if (verbose) Log.info("Creating summary file: " + summaryFileCsv);
			ok &= summary(SUMMARY_CSV_TEMPLATE, summaryFileCsv, true);
		}
		if (createSummaryHtml) {
			if (verbose) Log.info("Creating summary file: " + summaryFileHtml);
			ok &= summary(SUMMARY_TEMPLATE, summaryFileHtml, false);
		}

		// Creates genes output file
		if (createSummaryHtml || createSummaryCsv) {
			if (verbose) Log.info("Creating genes file: " + summaryGenesFile);
			ok &= summary(SUMMARY_GENES_TEMPLATE, summaryGenesFile, true);
		}

		if (totalErrs > 0) System.err.println(totalErrs + " errors.");
		return !ok;
	}

	/**
	 * Calculate the effect of variants and show results
	 */
	protected void annotateInit(String outputFile) {
		snpEffectPredictor = config.getSnpEffectPredictor();

		// Reset all counters
		totalErrs = 0;
		countInputLines = countVariants = countEffects = 0; // = countVariantsFilteredOut = 0;
		anyCancerSample = false;
		pedigree = null;
		errByType = new CountByType();
		warnByType = new CountByType();
		countVcfEntries = 0;
		annotateTimer = new Timer();

		// Create 'stats' objects
		variantStats = new VariantStats(config.getGenome());
		variantEffectStats = new VariantEffectStats(config.getGenome());
		variantEffectStats.setUseSequenceOntology(useSequenceOntology);
		vcfStats = new VcfStats();

		if (fastaProt != null) {
			if ((new File(fastaProt)).delete() && verbose) {
				Log.info("Deleted protein fasta output file '" + fastaProt + "'");
			}
		}

		// Create output formatter
		outputFormatter = null;
		switch (outputFormat) {
		case VCF:
			VcfOutputFormatter vof = new VcfOutputFormatter(vcfEntriesDebug);
			vof.setFormatVersion(formatVersion);
			vof.setLossOfFunction(lossOfFunction);
			vof.setConfig(config);
			outputFormatter = vof;
			break;
		case GATK:
			outputFormatter = new VcfOutputFormatter(vcfEntriesDebug);
			((VcfOutputFormatter) outputFormatter).setGatk(true);
			break;
		case BED:
			outputFormatter = new BedOutputFormatter();
			break;
		case BEDANN:
			outputFormatter = new BedAnnotationOutputFormatter();
			break;
		default:
			throw new RuntimeException("Unknown output format '" + outputFormat + "'");
		}

		outputFormatter.setVersion(VERSION_AUTHOR);
		outputFormatter.setCommandLineStr(commandLineStr(false));
		outputFormatter.setVariantEffectResutFilter(variantEffectResutFilter);
		outputFormatter.setSupressOutput(suppressOutput);
		outputFormatter.setChrStr(chrStr);
		outputFormatter.setUseSequenceOntology(useSequenceOntology);
		outputFormatter.setUseOicr(useOicr);
		outputFormatter.setUseHgvs(hgvs);
		outputFormatter.setUseGeneId(useGeneId);
		outputFormatter.setOutputFile(outputFile);
	}

	@Override
	public boolean annotateInit(VcfFileIterator vcfFile) {
		if (inputFormat != InputFormat.VCF || outputFormat != OutputFormat.VCF) throw new RuntimeException();
		annotateInit((String) null);
		return false;
	}

	/**
	 * Annotate a single variant
	 *
	 * @param variant
	 * @return true if there is any impact 'Low' or higher
	 */
	boolean annotateVariant(Variant variant) {
		// Calculate effects: By default do not annotate non-variant sites
		if (!variant.isVariant()) return false;

		boolean impactModerateOrHigh = false; // Does this entry have a 'MODERATE' or 'HIGH' impact?
		boolean impactLowOrHigher = false; // Does this entry have an impact (other than MODIFIER)?

		// Perform basic statistics about this variant
		if (createSummaryHtml || createSummaryCsv) variantStats.sample(variant);

		VariantEffects variantEffects = snpEffectPredictor.variantEffect(variant);

		// Create new 'section'
		outputFormatter.startSection(variant);

		// Show results
		for (VariantEffect variantEffect : variantEffects) {
			if (createSummaryHtml || createSummaryCsv) variantEffectStats.sample(variantEffect); // Perform basic statistics about this result

			// Any errors or warnings?
			if (variantEffect.hasError()) errByType.inc(variantEffect.getError());
			if (variantEffect.hasWarning()) warnByType.inc(variantEffect.getWarning());

			// Does this entry have an impact (other than MODIFIER)?
			impactLowOrHigher |= (variantEffect.getEffectImpact() != EffectImpact.MODIFIER);
			impactModerateOrHigh |= (variantEffect.getEffectImpact() == EffectImpact.MODERATE) || (variantEffect.getEffectImpact() == EffectImpact.HIGH);

			outputFormatter.add(variantEffect);
			countEffects++;
		}

		// Finish up this section
		outputFormatter.printSection(variant);

		// Output protein changes to FASTA file
		if (fastaProt != null && impactModerateOrHigh) proteinAltSequence(variant, variantEffects);

		return impactLowOrHigher;
	}

	/**
	 * Compare two genotypes
	 */
	void annotateVariantCancer(List<Variant> variants, int altGtNum, int refGtNum) {
		VariantNonRef varNonRef = variantCancer(variants, altGtNum, refGtNum);

		// No net variation? Skip
		if (!varNonRef.isVariant()) return;

		// Calculate effects
		VariantEffects variantEffects = snpEffectPredictor.variantEffect(varNonRef);

		// Create new 'section'
		outputFormatter.startSection(varNonRef);

		// Show results (note, we don't add these to the statistics)
		for (VariantEffect variantEffect : variantEffects)
			outputFormatter.add(variantEffect);

		// Finish up this section
		outputFormatter.printSection(varNonRef);
	}

	/**
	 * Do we analyze cancer samples? Here we deal with Somatic vs Germline comparisons
	 */
	void annotateVariantCancer(List<Variant> variants, VcfEntry vcfEntry) {
		if (!shouldAnnotateVariantCancer(variants, vcfEntry)) return;

		// Calculate all required comparisons
		Set<Tuple<Integer, Integer>> comparisons = pedigree.compareCancerGenotypes(vcfEntry);

		// Analyze each comparison
		for (Tuple<Integer, Integer> comp : comparisons) {
			// We have to compare comp.first vs comp.second
			int altGtNum = comp.first; // comp.first is 'derived' (our new ALT)
			int refGtNum = comp.second; // comp.second is 'original' (our new REF)
			annotateVariantCancer(variants, altGtNum, refGtNum);
		}
	}

	/**
	 * Iterate on all inputs (VCF) and calculate effects. Note: This is used only on
	 * input format VCF, which has a different iteration modality
	 */
	VcfFileIterator annotateVcf(String inputFile) {
		// Open VCF file
		VcfFileIterator vcfFile = new VcfFileIterator(inputFile, config.getGenome());
		vcfFile.setDebug(debug);

		// Iterate over VCF entries
		if (multiThreaded) {
			// Multi-thread loop
			StreamSupport.stream(vcfFile.spliterator(), true).forEach(this::annotate);
		} else {
			// Single thread
			for (VcfEntry vcfEntry : vcfFile)
				annotate(vcfEntry);
		}

		// Empty file? Show at least the header
		if (countVcfEntries == 0) outputFormatter.print(vcfFile.getVcfHeader().toString());

		// Show errors and warnings
		if (verbose) {
			if (!errByType.isEmpty()) System.err.println("\nERRORS: Some errors were detected\nError type\tNumber of errors\n" + errByType + "\n");
			if (!warnByType.isEmpty()) System.err.println("\nWARNINGS: Some warning were detected\nWarning type\tNumber of warnings\n" + warnByType + "\n");
		}

		return vcfFile;
	}

	public VariantEffectStats getChangeEffectResutStats() {
		return variantEffectStats;
	}

	public int getTotalErrs() {
		return totalErrs;
	}

	public VariantStats getvariantStats() {
		return variantStats;
	}

	/**
	 * Create a suitable output file name
	 */
	String outputFile(String inputFile) {
		// Remove GZ extension
		String base = Gpr.baseName(inputFile, ".gz");

		// Remove extension according to input format
		switch (inputFormat) {
		case VCF:
			base = Gpr.baseName(inputFile, ".vcf");
			break;
		case BED:
			base = Gpr.baseName(inputFile, ".bed");
			break;
		default:
			throw new RuntimeException("Unimplemented option for input file type " + inputFormat);
		}

		String outputFile = Gpr.dirName(inputFile) + "/" + base + ".eff";

		// Add extension according to output format
		switch (outputFormat) {
		case BED:
		case BEDANN:
			outputFile += ".bed";
			break;
		case VCF:
		case GATK:
			outputFile += ".vcf";
			break;
		default:
			throw new RuntimeException("Unimplemented option for output file type " + outputFormat);
		}

		// Create summary file names
		if (createSummaryCsv) summaryFileCsv = Gpr.dirName(inputFile) + "/" + base + "_summary.csv";
		if (createSummaryHtml) summaryFileHtml = Gpr.dirName(inputFile) + "/" + base + "_summary.html";
		summaryGenesFile = Gpr.dirName(inputFile) + "/" + base + "_genes.txt";

		return outputFile;
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args == null) return;

		boolean isFileList = false;
		this.args = args;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// Is it a command line option?
			// Note: Generic options (such as config, verbose, debug, quiet, etc.) are parsed by SnpEff class
			if (isOpt(arg)) {
				if (arg.equalsIgnoreCase("-fileList")) isFileList = true;
				else {
					arg = arg.toLowerCase();

					switch (arg) {
					// ---
					// Output options
					// ---
					case "-chr":
						chrStr = args[++i];
						break;

					case "-csvstats":
						createSummaryCsv = true; // Create a CSV formatted summary file.
						if ((i + 1) < args.length) {
							summaryFileCsv = args[++i];
							String base = Gpr.baseName(summaryFileCsv, ".csv");
							String dir = Gpr.dirName(summaryFileCsv);
							summaryGenesFile = (dir != null ? dir + "/" : "") + base + ".genes.txt";
						} else usage("Missing parameter: CSV stats file name ");
						break;

					case "-fastaprot":
						if ((i + 1) < args.length) fastaProt = args[++i]; // Output protein sequences in fasta files
						else usage("Missing -cancerSamples argument");
						break;

					case "-nochromoplots":
						chromoPlots = false;
						break;

					case "-nostats":
						createSummaryHtml = createSummaryCsv = false;
						break;

					case "-o": // Output format
						if ((i + 1) < args.length) {
							String outFor = args[++i].toUpperCase();

							// if (outFor.equals("TXT")) outputFormat = OutputFormat.TXT;
							if (outFor.equals("VCF")) outputFormat = OutputFormat.VCF;
							else if (outFor.equals("GATK")) {
								outputFormat = OutputFormat.GATK;
								useSequenceOntology = false;
								hgvs = false;
								nextProt = false;
								motif = false;

								// GATK doesn't support SPLICE_REGION at the moment.
								// Set parameters to zero so that splcie regions are not created.
								spliceRegionExonSize = spliceRegionIntronMin = spliceRegionIntronMax = 0;
							} else if (outFor.equals("BED")) {
								outputFormat = OutputFormat.BED;
								lossOfFunction = false;
							} else if (outFor.equals("BEDANN")) {
								outputFormat = OutputFormat.BEDANN;
								lossOfFunction = false;
							} else if (outFor.equals("TXT")) usage("Output format 'TXT' has been deprecated. Please use 'VCF' instead.\nYou can extract VCF fields to a TXT file using 'SnpSift extractFields' (https://pcingola.github.io/SnpEff/SnpSift.html#Extract).");
							else usage("Unknown output file format '" + outFor + "'");
						}
						break;

					case "-s":
					case "-stats":
					case "-htmlstats":
						createSummaryHtml = true;
						if ((i + 1) < args.length) {
							summaryFileHtml = args[++i];
							String base = Gpr.baseName(summaryFileHtml, ".html");
							String dir = Gpr.dirName(summaryFileHtml);
							summaryGenesFile = (dir != null ? dir + "/" : "") + base + ".genes.txt";
						} else usage("Missing parameter: HTML stats file name ");
						break;

					case "-uselocaltemplate": // Undocumented option (only used for development & debugging)
						useLocalTemplate = true;
						break;

					// ---
					// Annotation options
					// ---
					case "-cancer":
						cancer = true; // Perform cancer comparisons
						break;

					case "-cancersamples":
						if ((i + 1) < args.length) cancerSamples = args[++i]; // Read cancer samples from TXT files
						else usage("Missing -cancerSamples argument");
						break;

					case "-classic":
						useSequenceOntology = false;
						formatVersion = EffFormatVersion.FORMAT_EFF_4;
						hgvs = hgvsForce;
						break;

					case "-formateff":
						formatVersion = EffFormatVersion.FORMAT_EFF_4;
						break;

					case "-geneid":
						useGeneId = true; // Use gene ID instead of gene name
						break;

					case "-lof":
						lossOfFunction = true; // Add LOF tag
						break;

					case "-nohgvs":
						hgvs = false; // Do not use HGVS notation
						hgvsShift = false;
						break;

					case "-noshifthgvs":
					case "-no_shift_hgvs":
						hgvsShift = false;
						break;

					case "-nolof":
						lossOfFunction = false; // Do not add LOF tag
						break;

					case "-oicr":
						useOicr = true; // Use OICR tag
						break;

					case "-sequenceontology":
						useSequenceOntology = true; // Use SO temrs
						break;

					// ---
					// Input options
					// ---
					case "-fi":
					case "-filterinterval":
						if ((i + 1) < args.length) filterIntervalFiles.add(args[++i]);
						else usage("Option '-fi' without config filter_interval_file argument");
						break;

					case "-i":
						// Input format
						if ((i + 1) < args.length) {
							String inFor = args[++i].toUpperCase();

							if (inFor.equals("VCF")) {
								inputFormat = InputFormat.VCF;
								outputFormat = OutputFormat.VCF;
							} else if (inFor.equals("BED")) {
								inputFormat = InputFormat.BED;
								outputFormat = OutputFormat.BED;
								lossOfFunction = false;
							} else if (inFor.equals("TXT")) usage("Input format 'TXT' has been deprecated. Please use 'VCF' instead.");
							else usage("Unknown input file format '" + inFor + "'");
						} else usage("Missing input format in command line option '-i'");
						break;

					// ---
					// Filters
					// ---
					case "-no-downstream":
						variantEffectResutFilter.add(EffectType.DOWNSTREAM);
						break;

					case "-no-upstream":
						variantEffectResutFilter.add(EffectType.UPSTREAM);
						break;

					case "-no-intergenic":
						variantEffectResutFilter.add(EffectType.INTERGENIC);
						break;

					case "-no-intron":
						variantEffectResutFilter.add(EffectType.INTRON);
						break;

					case "-no-utr":
						variantEffectResutFilter.add(EffectType.UTR_3_PRIME);
						variantEffectResutFilter.add(EffectType.UTR_3_DELETED);
						variantEffectResutFilter.add(EffectType.UTR_5_PRIME);
						variantEffectResutFilter.add(EffectType.UTR_5_DELETED);
						break;

					case "-no":
						String filterOut = "";
						if ((i + 1) < args.length) filterOut = args[++i];

						String filterOutArray[] = filterOut.split(",");
						for (String filterStr : filterOutArray) {
							if (filterStr.equalsIgnoreCase("utr")) {
								variantEffectResutFilter.add(EffectType.UTR_3_PRIME);
								variantEffectResutFilter.add(EffectType.UTR_3_DELETED);
								variantEffectResutFilter.add(EffectType.UTR_5_PRIME);
								variantEffectResutFilter.add(EffectType.UTR_5_DELETED);
							} else if (filterStr.equalsIgnoreCase("None")); // OK, nothing to do
							else variantEffectResutFilter.add(EffectType.valueOf(filterStr.toUpperCase()));
						}
						break;

					default:
						usage("Unknown option '" + arg + "'");
					}
				}
			} else if (genomeVer.isEmpty()) genomeVer = arg;
			else if (inputFile.isEmpty()) inputFile = arg;
			else usage("Unknown parameter '" + arg + "'");
		}

		// ---
		// Sanity checks
		// ---

		// Check: Do we have all required parameters?
		if (genomeVer == null || genomeVer.isEmpty()) usage("Missing genomer_version parameter");

		// Check input file
		if (inputFile.isEmpty()) inputFile = "-"; // Use STDIN as default
		else if (!Gpr.canRead(inputFile)) usage("Cannot read input file '" + inputFile + "'");

		// Read input files from file list?
		if (isFileList) {
			inputFiles = new ArrayList<>();
			for (String file : readFile(inputFile).split("\n"))
				inputFiles.add(file);
		}

		// Sanity checks for VCF output format
		boolean isOutVcf = (outputFormat == OutputFormat.VCF) || (outputFormat == OutputFormat.GATK);
		if (isOutVcf && (inputFormat != InputFormat.VCF)) usage("Output in VCF format is only supported when the input is also in VCF format");
		if (!isOutVcf && lossOfFunction) usage("Loss of function annotation is only supported when when output is in VCF format");
		if (!isOutVcf && cancer) usage("Canccer annotation is only supported when when output is in VCF format");

		// Sanity check for multi-threaded version
		if (multiThreaded) {
			createSummaryHtml = false; // This is implied ( '-t' => '-noStats' )
			createSummaryCsv = false;
		}
		if (multiThreaded && cancer) usage("Cancer analysis is currently not supported in multi-threaded mode.");
		if (multiThreaded && !isOutVcf) usage("Multi-threaded option is only supported when when output is in VCF format");
		if (multiThreaded && (createSummaryHtml || createSummaryCsv)) usage("Multi-threaded option should be used with 'noStats'.");
	}

	/**
	 * Append ALT protein sequence to 'fastaProt' file
	 */
	void proteinAltSequence(Variant var, VariantEffects variantEffects) {
		Set<Transcript> doneTr = new HashSet<>();
		for (VariantEffect varEff : variantEffects) {
			Transcript tr = varEff.getTranscript();
			if (tr == null || doneTr.contains(tr)) continue;

			// Calculate sequence after applying variant
			Transcript trAlt = tr.apply(var);

			// Build fasta entries and append to file
			StringBuilder sb = new StringBuilder();
			sb.append(">" + tr.getId() + " Ref\n" + tr.protein() + "\n");
			sb.append(">" + tr.getId() + " Variant " //
					+ var.getChromosomeName() //
					+ ":" + (var.getStart() + 1) //
					+ "-" + (var.getEndClosed() + 1) //
					+ " Ref:" + var.getReference() //
					+ " Alt:" + var.getAlt() //
					+ " HGVS.p:" + varEff.getHgvsProt() //
					+ "\n" //
					+ trAlt.protein() + "\n" //
			);
			Gpr.toFile(fastaProt, sb, true);

			doneTr.add(tr);
		}
	}

	/**
	 * Read a file after checking for some common error conditions
	 */
	String readFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) Log.fatalError("No such file '" + fileName + "'");
		if (!file.canRead()) Log.fatalError("Cannot open file '" + fileName + "'");
		return Gpr.readFile(fileName);
	}

	/**
	 * Read a filter custom interval file
	 */
	int readFilterIntFile(String intFile) {
		Markers markers = loadMarkers(intFile);

		if (filterIntervals == null) filterIntervals = new IntervalForest();
		for (Marker filterInterval : markers)
			filterIntervals.add(filterInterval);

		return markers.size();
	}

	/**
	 * Read pedigree either from VCF header or from cancerSample file
	 */
	Pedigree readPedigree(VcfFileIterator vcfFile) {
		if (cancerSamples != null) return new Pedigree(vcfFile, cancerSamples);
		return new Pedigree(vcfFile);
	}

	@Override
	public HashMap<String, String> reportValues() {
		HashMap<String, String> report = super.reportValues();
		if (variantStats != null) report.put("variants", variantStats.getCount() + "");
		return report;
	}

	/**
	 * Run according to command line options
	 */
	@Override
	public boolean run() {
		return run(false) != null;
	}

	/**
	 * Run according to command line options
	 */
	public List<VcfEntry> run(boolean createList) {
		// Prepare to run

		// Nothing to filter out => don't waste time
		if (!variantEffectResutFilter.anythingSet()) variantEffectResutFilter = null;

		filterIntervals = null;

		loadConfig(); // Read config file
		loadDb(); // Load database

		// Check if we can open the input file (no need to check if it is STDIN)
		if (!Gpr.canRead(inputFile)) usage("Cannot open input file '" + inputFile + "'");

		// Read filter interval files
		for (String filterIntFile : filterIntervalFiles) {
			if (filterIntervals == null) filterIntervals = new IntervalForest();
			if (verbose) Log.info("Reading filter interval file '" + filterIntFile + "'");
			int count = readFilterIntFile(filterIntFile);
			if (verbose) Log.info("done (" + count + " intervals loaded). ");
		}

		// Build interval forest for filter (if any)
		if (filterIntervals != null) {
			if (verbose) Log.info("Building filter interval forest");
			filterIntervals.build();
			if (verbose) Log.info("done.");
		}

		// Store VCF results in a list?
		if (createList) vcfEntriesDebug = new ArrayList<>();

		// Predict
		boolean ok = true;
		if (verbose) Log.info("Predicting variants");
		if (inputFiles == null) {
			// Single input file, output to STDOUT (typical usage)
			ok = annotate(inputFile, null);
		} else {
			// Multiple input and output files
			for (String inputFile : inputFiles) {
				String outputFile = outputFile(inputFile);
				if (verbose) Log.info("Analyzing file" //
						+ "\n\tInput         : '" + inputFile + "'" //
						+ "\n\tOutput        : '" + outputFile + "'" //
						+ (createSummaryHtml ? "\n\tSummary (HTML): '" + summaryFileHtml + "'" : "") //
						+ (createSummaryCsv ? "\n\tSummary (CSV) : '" + summaryFileCsv + "'" : "") //
				);
				ok &= annotate(inputFile, outputFile);
			}
		}
		if (verbose) Log.info("done.");

		if (!ok) return null;
		if (vcfEntriesDebug == null) return new ArrayList<>();
		return vcfEntriesDebug;
	}

	public void setFormatVersion(EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion;
	}

	/**
	 * Should we annotate cancer variants?
	 */
	boolean shouldAnnotateVariantCancer(List<Variant> variants, VcfEntry vcfEntry) {
		if (vcfEntry.isMultiallelic()) return true;
		// Bi-allelic are analyzed only if there are "back to REF" mutation
		return pedigree.anyBackToRef(vcfEntry);
	}

	/**
	 * Show annotation progress
	 */
	void showProgress() {
		countVariants++;
		if (verbose && (countVariants % SHOW_EVERY == 0)) {
			int millisec = ((int) annotateTimer.elapsed());
			int secs = millisec / 1000;
			if (secs > 0) {
				int varsPerSec = (int) (countVariants * 1000.0 / millisec);
				Log.info("\t" + countVariants + " variants (" + varsPerSec + " variants per second), " + countVcfEntries + " VCF entries");
			}
		}
	}

	/**
	 * Creates a summary output file (using freeMarker and a template)
	 */
	boolean summary(String templateFile, String outputFile, boolean noCommas) {
		try {
			// Configure FreeMaker
			Configuration cfg = new Configuration();

			// Specify the data source where the template files come from
			if (useLocalTemplate) cfg.setDirectoryForTemplateLoading(new File(TEMPLATES_DIR)); // Use local 'template' directory
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

	/**
	 * Create a hash with all variables needed for creating summary pages
	 */
	HashMap<String, Object> summaryCreateHash() {
		// Create the root hash (where data objects are)
		HashMap<String, Object> root = new HashMap<>();
		root.put("args", commandLineStr(createSummaryCsv ? false : true));
		root.put("changeStats", variantEffectStats);
		root.put("chromoPlots", chromoPlots);
		root.put("countEffects", countEffects);
		root.put("countInputLines", countInputLines);
		root.put("countVariants", countVariants);
		root.put("date", String.format("%1$TY-%1$Tm-%1$Td %1$TH:%1$TM", new Date()));
		root.put("genesFile", Gpr.baseName(summaryGenesFile, ""));
		root.put("genome", config.getGenome());
		root.put("genomeVersion", genomeVer);
		root.put("variantEffectResutFilter", variantEffectResutFilter);
		root.put("variantStats", variantStats);
		root.put("snpEffectPredictor", config.getSnpEffectPredictor());
		root.put("vcfStats", vcfStats);
		root.put("version", SnpEff.VERSION); // Version used

		return root;
	}

	/**
	 * Show 'usage;' message and exit with an error code '-1'
	 *
	 * @param message
	 */
	@Override
	public void usage(String message) {
		if (message != null) {
			System.err.println("Error        :\t" + message);
			System.err.println("Command line :\t" + commandLineStr(false) + "\n");
		}

		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff [eff] [options] genome_version [input_file]");
		System.err.println("\n");
		System.err.println("\tvariants_file                   : Default is STDIN");
		System.err.println("\n");
		System.err.println("\nOptions:");
		System.err.println("\t-chr <string>                   : Prepend 'string' to chromosome name (e.g. 'chr1' instead of '1'). Only on TXT output.");
		System.err.println("\t-classic                        : Use old style annotations instead of Sequence Ontology and Hgvs.");
		System.err.println("\t-csvStats <file>                : Create CSV summary file.");
		System.err.println("\t-download                       : Download reference genome if not available. Default: " + download);
		System.err.println("\t-i <format>                     : Input format [ vcf, bed ]. Default: VCF.");
		System.err.println("\t-fileList                       : Input actually contains a list of files to process.");
		System.err.println("\t-o <format>                     : Ouput format [ vcf, gatk, bed, bedAnn ]. Default: VCF.");
		System.err.println("\t-s , -stats, -htmlStats         : Create HTML summary file.  Default is '" + DEFAULT_SUMMARY_HTML_FILE + "'");
		System.err.println("\t-noStats                        : Do not create stats (summary) file");
		System.err.println("\nResults filter options:");
		System.err.println("\t-fi , -filterInterval  <file>   : Only analyze changes that intersect with the intervals specified in this file (you may use this option many times)");
		System.err.println("\t-no-downstream                  : Do not show DOWNSTREAM changes");
		System.err.println("\t-no-intergenic                  : Do not show INTERGENIC changes");
		System.err.println("\t-no-intron                      : Do not show INTRON changes");
		System.err.println("\t-no-upstream                    : Do not show UPSTREAM changes");
		System.err.println("\t-no-utr                         : Do not show 5_PRIME_UTR or 3_PRIME_UTR changes");
		System.err.println("\t-no <effectType>                : Do not show 'EffectType'. This option can be used several times.");
		System.err.println("\nAnnotations options:");
		System.err.println("\t-cancer                         : Perform 'cancer' comparisons (Somatic vs Germline). Default: " + cancer);
		System.err.println("\t-cancerSamples <file>           : Two column TXT file defining 'oringinal \\t derived' samples.");
		System.err.println("\t-fastaProt <file>               : Create an output file containing the resulting protein sequences.");
		System.err.println("\t-formatEff                      : Use 'EFF' field compatible with older versions (instead of 'ANN').");
		System.err.println("\t-geneId                         : Use gene ID instead of gene name (VCF output). Default: " + useGeneId);
		System.err.println("\t-hgvs                           : Use HGVS annotations for amino acid sub-field. Default: " + hgvs);
		System.err.println("\t-hgvsOld                        : Use old HGVS notation. Default: " + hgvsOld);
		System.err.println("\t-hgvs1LetterAa                  : Use one letter Amino acid codes in HGVS notation. Default: " + hgvsOneLetterAa);
		System.err.println("\t-hgvsTrId                       : Use transcript ID in HGVS notation. Default: " + hgvsTrId);
		System.err.println("\t-lof                            : Add loss of function (LOF) and Nonsense mediated decay (NMD) tags.");
		System.err.println("\t-noHgvs                         : Do not add HGVS annotations.");
		System.err.println("\t-noLof                          : Do not add LOF and NMD annotations.");
		System.err.println("\t-noShiftHgvs                    : Do not shift variants according to HGVS notation (most 3prime end).");
		System.err.println("\t-oicr                           : Add OICR tag in VCF file. Default: " + useOicr);
		System.err.println("\t-sequenceOntology               : Use Sequence Ontology terms. Default: " + useSequenceOntology);

		usageGenericAndDb();

		System.exit(-1);
	}

	/**
	 * Create a cancer variant using alt and ref genotypes
	 */
	VariantNonRef variantCancer(List<Variant> variants, int altGtNum, int refGtNum) {
		// Is this a "back to reference" variant?
		// Example:
		// Germline: 'A' -> 'T'
		// Somatic: 'T' -> 'A' (i.e. reverses the gerline mutation to the genome
		// reference)
		if (altGtNum == 0) {
			Variant variantRef = variants.get(refGtNum - 1); // After applying this variant, we get the new 'reference'
			Variant variantAlt = variantRef.reverse(); // This our new 'variant'. The effect of this variant is "back to
														// reference"
			return new VariantNonRef(variantAlt, variantRef);
		}

		Variant variantRef = variants.get(refGtNum - 1); // After applying this variant, we get the new 'reference'
		Variant variantAlt = variants.get(altGtNum - 1); // This our new 'variant'
		return new VariantNonRef(variantAlt, variantRef);
	}

}
