package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

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

import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import ca.mcgill.mcb.pcingola.akka.vcf.VcfWorkQueue;
import ca.mcgill.mcb.pcingola.fileIterator.BedFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VariantFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.filter.VariantEffectFilter;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.VariantNonRef;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.outputFormatter.BedAnnotationOutputFormatter;
import ca.mcgill.mcb.pcingola.outputFormatter.BedOutputFormatter;
import ca.mcgill.mcb.pcingola.outputFormatter.OutputFormatter;
import ca.mcgill.mcb.pcingola.outputFormatter.VcfOutputFormatter;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.eff.MasterEff;
import ca.mcgill.mcb.pcingola.stats.CountByType;
import ca.mcgill.mcb.pcingola.stats.VariantEffectStats;
import ca.mcgill.mcb.pcingola.stats.VariantStats;
import ca.mcgill.mcb.pcingola.stats.VcfStats;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.util.Tuple;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.PedigreeEnrty;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Command line program: Predict variant effects
 *
 * @author Pablo Cingolani
 */
public class SnpEffCmdEff extends SnpEff {

	public static final String SUMMARY_TEMPLATE = "snpEff_summary.ftl"; // Summary template file name
	public static final String SUMMARY_CSV_TEMPLATE = "snpEff_csv_summary.ftl"; // Summary template file name
	public static final String SUMMARY_GENES_TEMPLATE = "snpEff_genes.ftl"; // Genes template file name

	public static final String DEFAULT_SUMMARY_FILE = "snpEff_summary.html";
	public static final String DEFAULT_SUMMARY_CSV_FILE = "snpEff_summary.csv";
	public static final String DEFAULT_SUMMARY_GENES_FILE = "snpEff_genes.txt";

	public static final int SHOW_EVERY = 100000;

	boolean cancer = false; // Perform cancer comparisons
	boolean chromoPlots = true; // Create mutations by chromosome plots?
	boolean createCsvSummary = false; // Use a CSV as output summary
	boolean createSummary = true; // Do not create summary output file
	boolean lossOfFunction = true; // Create loss of function LOF tag?
	boolean useGeneId = false; // Use gene ID instead of gene name (VCF output)
	boolean useHgvs = true; // Use Hgvs notation
	boolean useLocalTemplate = false; // Use template from 'local' file instead of 'jar' (this is only used for development and debugging)
	boolean useOicr = false; // Use OICR tag
	boolean useSequenceOntology = true; // Use Sequence Ontology terms
	int totalErrs = 0;
	long countInputLines = 0, countVariants = 0, countEffects = 0; // , countVariantsFilteredOut = 0;
	String cancerSamples = null;
	String chrStr = "";
	String inputFile = ""; // Input file
	String summaryFile; // Summary output file
	String summaryGenesFile; // Gene table file
	InputFormat inputFormat = InputFormat.VCF; // Format use in input files
	OutputFormat outputFormat = OutputFormat.VCF; // Output format
	VariantEffectFilter variantEffectResutFilter; // Filter prediction results
	ArrayList<String> filterIntervalFiles;// Files used for filter intervals
	ArrayList<String> inputFiles;
	IntervalForest filterIntervals; // Filter only variants that match these intervals
	VariantStats variantStats;
	VariantEffectStats variantEffectStats;
	VcfStats vcfStats;
	List<VcfEntry> vcfEntriesDebug = null; // Use for debugging or testing (in some test-cases)
	EffFormatVersion formatVersion = EffFormatVersion.DEFAULT_FORMAT_VERSION;

	public SnpEffCmdEff() {
		super();
		chrStr = ""; // Default: Don't show 'chr' before chromosome
		inputFile = ""; // variant input file
		variantEffectResutFilter = new VariantEffectFilter(); // Filter prediction results
		filterIntervalFiles = new ArrayList<String>(); // Files used for filter intervals
		filterIntervals = new IntervalForest(); // Filter only variants that match these intervals
		summaryFile = DEFAULT_SUMMARY_FILE;
		summaryGenesFile = DEFAULT_SUMMARY_GENES_FILE;
	}

	/**
	 * Analyze which comparisons to make in cancer genomes
	 */
	Set<Tuple<Integer, Integer>> compareCancerGenotypes(VcfEntry vcfEntry, List<PedigreeEnrty> pedigree) {
		HashSet<Tuple<Integer, Integer>> comparisons = new HashSet<Tuple<Integer, Integer>>();

		// Find out which comparisons have to be analyzed
		for (PedigreeEnrty pe : pedigree) {
			if (pe.isDerived()) {
				int numOri = pe.getOriginalNum();
				int numDer = pe.getDerivedNum();
				VcfGenotype genOri = vcfEntry.getVcfGenotype(numOri);
				VcfGenotype genDer = vcfEntry.getVcfGenotype(numDer);

				int gd[] = genDer.getGenotype(); // Derived genotype
				int go[] = genOri.getGenotype(); // Original genotype

				// Skip if one of the genotypes is missing
				if (gd == null || go == null) continue;

				if (genOri.isPhased() && genDer.isPhased()) {
					// Phased, we only have two possible comparisons
					for (int i = 0; i < 2; i++) {
						// Add comparisons
						if ((go[i] > 0) && (gd[i] > 0) // Both genotypes are non-missing?
								&& (go[i] != 0) // Origin genotype is non-reference? (this is always analyzed in the default mode)
								&& (gd[i] != go[i]) // Both genotypes are different?
								) {
							Tuple<Integer, Integer> compare = new Tuple<Integer, Integer>(gd[i], go[i]);
							comparisons.add(compare);
						}
					}
				} else {
					// Phased, we only have two possible comparisons
					for (int d = 0; d < gd.length; d++)
						for (int o = 0; o < go.length; o++) {
							// Add comparisons
							if ((go[o] > 0) && (gd[d] > 0) // Both genotypes are non-missing?
									&& (go[o] != 0) // Origin genotype is non-reference? (this is always analyzed in the default mode)
									&& (gd[d] != go[o]) // Both genotypes are different?
									) {
								Tuple<Integer, Integer> compare = new Tuple<Integer, Integer>(gd[d], go[o]);
								comparisons.add(compare);
							}
						}
				}
			}
		}

		return comparisons;
	}

	public VariantEffectStats getChangeEffectResutStats() {
		return variantEffectStats;
	}

	public VariantStats getvariantStats() {
		return variantStats;
	}

	/**
	 * Iterate on all inputs and calculate effects.
	 * Note: This is used for all input formats except VCF, which has a different iteration modality
	 */
	void iteratevariant(String inputFile, OutputFormatter outputFormatter) {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		// Create an input file iterator
		VariantFileIterator variantFileIterator;
		if (inputFormat == InputFormat.BED) variantFileIterator = new BedFileIterator(inputFile, config.getGenome());
		else throw new RuntimeException("Cannot create variant file iterator on input format '" + inputFormat + "'");

		//---
		// Iterate over input file
		//---
		for (Variant variant : variantFileIterator) {
			try {
				countInputLines++;

				countVariants++;
				if (verbose && (countVariants % SHOW_EVERY == 0)) Timer.showStdErr("\t" + countVariants + " variants");

				// Does it pass the filter? => Analyze

				// Skip if there are filter intervals and they are not matched
				if ((filterIntervals != null) && (filterIntervals.stab(variant).size() <= 0)) continue;

				// Perform basic statistics about this variant
				if (createSummary) variantStats.sample(variant);

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
				error(t, "Error while processing variant (line " + variantFileIterator.getLineNum() + ") :\n\t" + variant + "\n" + t);
			}
		}

		// Close file iterator (not really needed, but just in case)
		variantFileIterator.close();
	}

	/**
	 * Iterate on all inputs (VCF) and calculate effects.
	 * Note: This is used only on input format VCF, which has a different iteration modality
	 */
	void iterateVcf(String inputFile, OutputFormatter outputFormatter) {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		// Open VCF file
		VcfFileIterator vcfFile = new VcfFileIterator(inputFile, config.getGenome());
		vcfFile.setDebug(debug);
		boolean anyCancerSample = false;
		List<PedigreeEnrty> pedigree = null;
		CountByType errByType = new CountByType(), warnByType = new CountByType();

		int countVcfEntries = 0;
		for (VcfEntry vcfEntry : vcfFile) {
			boolean printed = false;
			boolean filteredOut = false;

			try {
				countInputLines++;
				countVcfEntries++;

				// Find if there is a pedigree and if it has any 'derived' entry
				if (vcfFile.isHeadeSection()) {
					if (cancer) {
						pedigree = readPedigree(vcfFile);

						// Any 'derived' entry in this pedigree?
						if (pedigree != null) {
							for (PedigreeEnrty pe : pedigree)
								anyCancerSample |= pe.isDerived();
						}
					}
				}

				// Sample vcf entry
				if (createSummary) vcfStats.sample(vcfEntry);

				// Skip if there are filter intervals and they are not matched
				if ((filterIntervals != null) && (filterIntervals.query(vcfEntry).isEmpty())) {
					filteredOut = true;
					continue;
				}

				// Create new 'section'
				outputFormatter.startSection(vcfEntry);

				//---
				// Analyze all changes in this VCF entry
				// Note, this is the standard analysis.
				// Next section deals with cancer: Somatic vs Germline comparisons
				//---
				boolean impact = false; // Does this entry have an impact (other than MODIFIER)?
				List<Variant> variants = vcfEntry.variants();
				for (Variant variant : variants) {
					countVariants++;
					if (verbose && (countVariants % SHOW_EVERY == 0)) Timer.showStdErr("\t" + countVariants + " variants");

					// Perform basic statistics about this variant
					if (createSummary) variantStats.sample(variant);

					// Calculate effects
					VariantEffects variantEffects = snpEffectPredictor.variantEffect(variant);

					// Create new 'section'
					outputFormatter.startSection(variant);

					// Show results
					for (VariantEffect variantEffect : variantEffects) {
						if (createSummary) variantEffectStats.sample(variantEffect); // Perform basic statistics about this result

						// Any errors or warnings?
						if (variantEffect.hasError()) errByType.inc(variantEffect.getError());
						if (variantEffect.hasWarning()) warnByType.inc(variantEffect.getWarning());

						// Does this entry have an impact (other than MODIFIER)?
						impact |= (variantEffect.getEffectImpact() != EffectImpact.MODIFIER);

						outputFormatter.add(variantEffect);
						countEffects++;
					}

					// Finish up this section
					outputFormatter.printSection(variant);
				}

				//---
				// Do we analyze cancer samples?
				// Here we deal with Somatic vs Germline comparisons
				//---
				if (anyCancerSample && impact && vcfEntry.isMultiallelic()) {
					// Calculate all required comparisons
					Set<Tuple<Integer, Integer>> comparisons = compareCancerGenotypes(vcfEntry, pedigree);

					// Analyze each comparison
					for (Tuple<Integer, Integer> comp : comparisons) {
						// We have to compare comp.first vs comp.second
						int altGtNum = comp.first; // comp.first is 'derived' (our new ALT)
						int refGtNum = comp.second; // comp.second is 'original' (our new REF)

						Variant variantRef = variants.get(refGtNum - 1); // After applying this variant, we get the new 'reference'
						Variant variantAlt = variants.get(altGtNum - 1); // This our new 'variant'
						VariantNonRef varNonRef = new VariantNonRef(variantAlt, variantRef);

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
				}

				// Finish up this section
				outputFormatter.printSection(vcfEntry);

				printed = true;
			} catch (Throwable t) {
				totalErrs++;
				error(t, "Error while processing VCF entry (line " + vcfFile.getLineNum() + ") :\n\t" + vcfEntry + "\n" + t);
			} finally {
				if (!printed && !filteredOut) outputFormatter.printSection(vcfEntry);
			}
		}

		// Empty file? Show at least the header
		if (countVcfEntries == 0) outputFormatter.print(vcfFile.getVcfHeader().toString());

		// Close file iterator (not really needed, but just in case)
		vcfFile.close();

		// Show errors and warnings
		if (verbose) {
			if (!errByType.isEmpty()) System.err.println("\nERRORS: Some errors were detected\nError type\tNumber of errors\n" + errByType + "\n");
			if (!warnByType.isEmpty()) System.err.println("\nWARNINGS: Some warning were detected\nWarning type\tNumber of warnings\n" + warnByType + "\n");
		}
	}

	/**
	 * Multi-threaded iteration on VCF inputs and calculates effects.
	 * Note: This is used only on input format VCF, which has a different iteration modality
	 */
	void iterateVcfMulti(String inputFile, final OutputFormatter outputFormatter) {
		if (verbose) Timer.showStdErr("Running multi-threaded mode (numThreads=" + numWorkers + ").");

		outputFormatter.setShowHeader(false); // Master process takes care of the header (instead of outputFormatter). Otherwise you get the header printed one time per worker.

		// We need final variables for the inner class
		final SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
		final VcfOutputFormatter vcfOutForm = (VcfOutputFormatter) outputFormatter;
		final SnpEffCmdEff snpEffCmdEff = this;

		new VcfFileIterator(inputFile, config.getGenome());

		// Master factory
		Props props = new Props(new UntypedActorFactory() {

			private static final long serialVersionUID = 1L;

			@Override
			public Actor create() {
				MasterEff master = new MasterEff(numWorkers, snpEffCmdEff, snpEffectPredictor, outputFormatter, filterIntervals);
				master.setAddHeader(vcfOutForm.getNewHeaderLines().toArray(new String[0]));
				return master;
			}
		});

		// Create and run queue
		int batchSize = 10;
		VcfWorkQueue vcfWorkQueue = new VcfWorkQueue(inputFile, batchSize, -1, props);
		vcfWorkQueue.run(true);
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
		summaryFile = Gpr.dirName(inputFile) + "/" + base + (createCsvSummary ? "_summary.csv" : "_summary.html");
		summaryGenesFile = Gpr.dirName(inputFile) + "/" + base + "_genes.txt";

		return outputFile;
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		boolean isFileList = false;
		this.args = args;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// Is it a command line option?
			// Note: Generic options (such as config, verbose, debug, quiet, etc.) are parsed by SnpEff class
			//---
			if (isOpt(arg)) {
				if (arg.equalsIgnoreCase("-fileList")) isFileList = true;
				//---
				// Output options
				//---
				else if (arg.equals("-o")) {
					// Output format
					if ((i + 1) < args.length) {
						String outFor = args[++i].toUpperCase();

						// if (outFor.equals("TXT")) outputFormat = OutputFormat.TXT;
						if (outFor.equals("VCF")) outputFormat = OutputFormat.VCF;
						else if (outFor.equals("GATK")) {
							outputFormat = OutputFormat.GATK;
							useSequenceOntology = false;
							useHgvs = false;
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
						} else if (outFor.equals("TXT")) usage("Output format 'TXT' has been deprecated. Please use 'VCF' instead.\nYou can extract VCF fields to a TXT file using 'SnpSift extractFields' (http://snpeff.sourceforge.net/SnpSift.html#Extract).");
						else usage("Unknown output file format '" + outFor + "'");
					}
				} else if ((arg.equals("-s") || arg.equalsIgnoreCase("-stats"))) {
					if ((i + 1) < args.length) {
						summaryFile = args[++i];
						String base = Gpr.baseName(Gpr.baseName(summaryFile, ".html"), ".csv"); // Extension can be either HTML or CSV
						String dir = Gpr.dirName(summaryFile);
						summaryGenesFile = (dir != null ? dir + "/" : "") + base + ".genes.txt";
					}
				} else if (arg.equalsIgnoreCase("-noStats")) createSummary = false; // Do not create summary file. It can be much faster (e.g. when parsing VCF files with many samples)
				else if (arg.equalsIgnoreCase("-csvStats")) {
					createCsvSummary = true; // Create a CSV formatted summary file.
					if (summaryFile.equals(DEFAULT_SUMMARY_FILE)) summaryFile = DEFAULT_SUMMARY_CSV_FILE;
				} else if (arg.equalsIgnoreCase("-chr")) chrStr = args[++i];
				else if (arg.equalsIgnoreCase("-useLocalTemplate")) useLocalTemplate = true; // Undocumented option (only used for development & debugging)
				else if (arg.equalsIgnoreCase("-noChromoPlots")) chromoPlots = false;
				//---
				// Annotation options
				//---
				else if (arg.equalsIgnoreCase("-cancer")) cancer = true; // Perform cancer comparisons
				else if (arg.equalsIgnoreCase("-cancerSamples")) {
					if ((i + 1) < args.length) cancerSamples = args[++i]; // Read cancer samples from TXT files
					else usage("Missing -cancerSamples argument");
				} else if (arg.equalsIgnoreCase("-lof")) lossOfFunction = true; // Add LOF tag
				else if (arg.equalsIgnoreCase("-noLof")) lossOfFunction = false; // Do not add LOF tag
				else if (arg.equalsIgnoreCase("-hgvs")) useHgvs = true; // Use HGVS notation
				else if (arg.equalsIgnoreCase("-noHgvs")) useHgvs = false; // Do not use HGVS notation
				else if (arg.equalsIgnoreCase("-geneId")) useGeneId = true; // Use gene ID instead of gene name
				else if (arg.equalsIgnoreCase("-sequenceOntology")) useSequenceOntology = true; // Use SO temrs
				else if (arg.equalsIgnoreCase("-classic")) {
					useSequenceOntology = false;
					useHgvs = false;
					formatVersion = EffFormatVersion.FORMAT_EFF_4;
				} else if (arg.equalsIgnoreCase("-formatEff")) {
					formatVersion = EffFormatVersion.FORMAT_EFF_4;
				} else if (arg.equalsIgnoreCase("-oicr")) useOicr = true; // Use OICR tag
				//---
				// Input options
				//---
				else if ((arg.equals("-fi") || arg.equalsIgnoreCase("-filterInterval"))) {
					if ((i + 1) < args.length) filterIntervalFiles.add(args[++i]);
					else usage("Option '-fi' without config filter_interval_file argument");
				} else if (arg.equals("-i")) {
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
				}
				//---
				// Filters
				//---
				else if (arg.equalsIgnoreCase("-no-downstream")) variantEffectResutFilter.add(EffectType.DOWNSTREAM);
				else if (arg.equalsIgnoreCase("-no-upstream")) variantEffectResutFilter.add(EffectType.UPSTREAM);
				else if (arg.equalsIgnoreCase("-no-intergenic")) variantEffectResutFilter.add(EffectType.INTERGENIC);
				else if (arg.equalsIgnoreCase("-no-intron")) variantEffectResutFilter.add(EffectType.INTRON);
				else if (arg.equalsIgnoreCase("-no-utr")) {
					variantEffectResutFilter.add(EffectType.UTR_3_PRIME);
					variantEffectResutFilter.add(EffectType.UTR_3_DELETED);
					variantEffectResutFilter.add(EffectType.UTR_5_PRIME);
					variantEffectResutFilter.add(EffectType.UTR_5_DELETED);
				} else if (arg.equalsIgnoreCase("-no")) {
					String filterOut = "";
					if ((i + 1) < args.length) filterOut = args[++i];

					String filterOutArray[] = filterOut.split(",");
					for (String filterStr : filterOutArray) {
						if (filterStr.equalsIgnoreCase("utr")) {
							variantEffectResutFilter.add(EffectType.UTR_3_PRIME);
							variantEffectResutFilter.add(EffectType.UTR_3_DELETED);
							variantEffectResutFilter.add(EffectType.UTR_5_PRIME);
							variantEffectResutFilter.add(EffectType.UTR_5_DELETED);
						} else if (filterStr.equalsIgnoreCase("None")) ; // OK, nothing to do
						else variantEffectResutFilter.add(EffectType.valueOf(filterStr.toUpperCase()));
					}
				} else usage("Unknow option '" + arg + "'");
			} else if (genomeVer.isEmpty()) genomeVer = arg;
			else if (inputFile.isEmpty()) inputFile = arg;
			else usage("Unknow parameter '" + arg + "'");
		}

		//---
		// Sanity checks
		//---

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");

		// Check input file
		if (inputFile.isEmpty()) inputFile = "-"; // Use STDIN as default
		else if (!Gpr.canRead(inputFile)) usage("Cannot read input file '" + inputFile + "'");

		// Read input files from file list?
		if (isFileList) {
			inputFiles = new ArrayList<String>();
			for (String file : readFile(inputFile).split("\n"))
				inputFiles.add(file);
		}

		// Sanity checks for VCF output format
		boolean isOutVcf = (outputFormat == OutputFormat.VCF) || (outputFormat == OutputFormat.GATK);
		if (isOutVcf && (inputFormat != InputFormat.VCF)) usage("Output in VCF format is only supported when the input is also in VCF format");
		if (!isOutVcf && lossOfFunction) usage("Loss of function annotation is only supported when when output is in VCF format");
		if (!isOutVcf && cancer) usage("Canccer annotation is only supported when when output is in VCF format");

		// Sanity check for multi-threaded version
		if (multiThreaded) createSummary = false; // This is implied ( '-t' => '-noStats' )
		if (multiThreaded && cancer) usage("Cancer analysis is currently not supported in multi-threaded mode.");
		if (multiThreaded && !isOutVcf) usage("Multi-threaded option is only supported when when output is in VCF format");
		if (multiThreaded && createSummary) usage("Multi-threaded option should be used with 'noStats'.");
	}

	/**
	 * Read a file after checking for some common error conditions
	 */
	String readFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) fatalError("No such file '" + fileName + "'");
		if (!file.canRead()) fatalError("Cannot open file '" + fileName + "'");
		return Gpr.readFile(fileName);
	}

	/**
	 * Read a filter custom interval file
	 */
	int readFilterIntFile(String intFile) {
		Markers markers = loadMarkers(intFile);
		for (Marker filterInterval : markers)
			filterIntervals.add(filterInterval);
		return markers.size();
	}

	/**
	 * Read pedigree either from VCF header or from cancerSample file
	 */
	List<PedigreeEnrty> readPedigree(VcfFileIterator vcfFile) {
		List<PedigreeEnrty> pedigree = null;

		if (cancerSamples != null) {
			// Read from TXT file
			if (verbose) Timer.showStdErr("Reading cancer samples pedigree from file '" + cancerSamples + "'.");

			List<String> sampleNames = vcfFile.getVcfHeader().getSampleNames();
			pedigree = new ArrayList<PedigreeEnrty>();

			for (String line : Gpr.readFile(cancerSamples).split("\n")) {
				String recs[] = line.split("\\s", -1);
				String original = recs[0];
				String derived = recs[1];

				PedigreeEnrty pe = new PedigreeEnrty(original, derived);
				pe.sampleNumbers(sampleNames);

				pedigree.add(pe);
			}
		} else {
			// Read from VCF header
			if (verbose) Timer.showStdErr("Reading cancer samples pedigree from VCF header.");
			pedigree = vcfFile.getVcfHeader().getPedigree();
		}

		if (verbose && ((pedigree == null) || pedigree.isEmpty())) Timer.showStdErr("WARNING: No cancer sample pedigree found.");
		return pedigree;
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
		//---
		// Prepare to run
		//---

		// Nothing to filter out => don't waste time
		if (!variantEffectResutFilter.anythingSet()) variantEffectResutFilter = null;

		filterIntervals = null;

		loadConfig(); // Read config file
		loadDb(); // Load database

		// Set some configuraion options
		config.setShiftHgvs(useHgvs && shiftHgvs);

		// Check if we can open the input file (no need to check if it is STDIN)
		if (!Gpr.canRead(inputFile)) usage("Cannot open input file '" + inputFile + "'");

		// Read filter interval files
		for (String filterIntFile : filterIntervalFiles) {
			if (filterIntervals == null) filterIntervals = new IntervalForest();
			if (verbose) Timer.showStdErr("Reading filter interval file '" + filterIntFile + "'");
			int count = readFilterIntFile(filterIntFile);
			if (verbose) Timer.showStdErr("done (" + count + " intervals loaded). ");
		}

		// Build interval forest for filter (if any)
		if (filterIntervals != null) {
			if (verbose) Timer.showStdErr("Building filter interval forest");
			filterIntervals.build();
			if (verbose) Timer.showStdErr("done.");
		}

		// Store VCF results in a list?
		if (createList) vcfEntriesDebug = new ArrayList<VcfEntry>();

		// Predict
		boolean ok = true;
		if (verbose) Timer.showStdErr("Predicting variants");
		if (inputFiles == null) {
			// Single input file (normal operations)
			ok = runAnalysis(inputFile, null);
		} else {
			// Multiple input files
			for (String inputFile : inputFiles) {
				String outputFile = outputFile(inputFile);
				if (verbose) Timer.showStdErr("Analyzing file" //
						+ "\n\tInput   : '" + inputFile + "'" //
						+ "\n\tOutput  : '" + outputFile + "'" //
						+ (createSummary ? "\n\tSummary : '" + summaryFile + "'" : "") //
						);
				ok &= runAnalysis(inputFile, outputFile);
			}
		}
		if (verbose) Timer.showStdErr("done.");

		if (!ok) return null;
		if (vcfEntriesDebug == null) return new ArrayList<VcfEntry>();
		return vcfEntriesDebug;
	}

	/**
	 * Calculate the effect of variants and show results
	 */
	public boolean runAnalysis(String inputFile, String outputFile) {
		boolean ok = true;

		// Reset all counters
		totalErrs = 0;
		countInputLines = countVariants = countEffects = 0; // = countVariantsFilteredOut = 0;

		// Create 'stats' objects
		variantStats = new VariantStats(config.getGenome());
		variantEffectStats = new VariantEffectStats(config.getGenome());
		variantEffectStats.setUseSequenceOntology(useSequenceOntology);
		vcfStats = new VcfStats();

		int totalErrs = 0;

		//---
		// Create output formatter
		//---
		OutputFormatter outputFormatter = null;
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

		outputFormatter.setVersion(VERSION_NO_NAME);
		outputFormatter.setCommandLineStr(commandLineStr(false));
		outputFormatter.setChangeEffectResutFilter(variantEffectResutFilter);
		outputFormatter.setSupressOutput(suppressOutput);
		outputFormatter.setChrStr(chrStr);
		outputFormatter.setUseSequenceOntology(useSequenceOntology);
		outputFormatter.setUseOicr(useOicr);
		outputFormatter.setUseHgvs(useHgvs);
		outputFormatter.setUseGeneId(useGeneId);
		outputFormatter.setOutputFile(outputFile);

		//---
		// Iterate over all changes
		//---
		switch (inputFormat) {
		case VCF:
			if (multiThreaded) iterateVcfMulti(inputFile, outputFormatter);
			else iterateVcf(inputFile, outputFormatter);
			break;
		default:
			iteratevariant(inputFile, outputFormatter);
		}
		outputFormatter.close();

		//---
		// Create reports
		//---
		if (createSummary && (summaryFile != null)) {
			// Creates a summary output file
			if (verbose) Timer.showStdErr("Creating summary file: " + summaryFile);
			if (createCsvSummary) ok &= summary(SUMMARY_CSV_TEMPLATE, summaryFile, true);
			else ok &= summary(SUMMARY_TEMPLATE, summaryFile, false);

			// Creates genes output file
			if (verbose) Timer.showStdErr("Creating genes file: " + summaryGenesFile);
			ok &= summary(SUMMARY_GENES_TEMPLATE, summaryGenesFile, true);
		}

		if (totalErrs > 0) System.err.println(totalErrs + " errors.");
		return ok;
	}

	public void setFormatVersion(EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion;
	}

	/**
	 * Creates a summary output file (using freeMarker and a template)
	 */
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

	/**
	 * Create a hash with all variables needed for creating summary pages
	 * @return
	 */
	HashMap<String, Object> summaryCreateHash() {
		// Create the root hash (where data objects are)
		HashMap<String, Object> root = new HashMap<String, Object>();
		root.put("args", commandLineStr(createCsvSummary ? false : true));
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
		System.err.println("\t-classic                        : Use old style annotaions instead of Sequence Ontology and Hgvs.");
		System.err.println("\t-download                       : Download reference genome if not available. Default: " + download);
		System.err.println("\t-i <format>                     : Input format [ vcf, bed ]. Default: VCF.");
		System.err.println("\t-fileList                       : Input actually contains a list of files to process.");
		System.err.println("\t-o <format>                     : Ouput format [ vcf, gatk, bed, bedAnn ]. Default: VCF.");
		System.err.println("\t-s , -stats                     : Name of stats file (summary). Default is '" + DEFAULT_SUMMARY_FILE + "'");
		System.err.println("\t-noStats                        : Do not create stats (summary) file");
		System.err.println("\t-csvStats                       : Create CSV summary file instead of HTML");
		System.err.println("\nResults filter options:");
		System.err.println("\t-fi , -filterInterval  <file>   : Only analyze changes that intersect with the intervals specified in this file (you may use this option many times)");
		System.err.println("\t-no-downstream                  : Do not show DOWNSTREAM changes");
		System.err.println("\t-no-intergenic                  : Do not show INTERGENIC changes");
		System.err.println("\t-no-intron                      : Do not show INTRON changes");
		System.err.println("\t-no-upstream                    : Do not show UPSTREAM changes");
		System.err.println("\t-no-utr                         : Do not show 5_PRIME_UTR or 3_PRIME_UTR changes");
		System.err.println("\t-no EffectType                  : Do not show 'EffectType'. This option can be used several times.");
		System.err.println("\nAnnotations options:");
		System.err.println("\t-cancer                         : Perform 'cancer' comparisons (Somatic vs Germline). Default: " + cancer);
		System.err.println("\t-cancerSamples <file>           : Two column TXT file defining 'oringinal \\t derived' samples.");
		System.err.println("\t-formatEff                      : Use 'EFF' field compatible with older versions (instead of 'ANN').");
		System.err.println("\t-geneId                         : Use gene ID instead of gene name (VCF output). Default: " + useGeneId);
		System.err.println("\t-hgvs                           : Use HGVS annotations for amino acid sub-field. Default: " + useHgvs);
		System.err.println("\t-lof                            : Add loss of function (LOF) and Nonsense mediated decay (NMD) tags.");
		System.err.println("\t-noHgvs                         : Do not add HGVS annotations.");
		System.err.println("\t-noLof                          : Do not add LOF and NMD annotations.");
		System.err.println("\t-noShiftHgvs                    : Do not shift variants according to HGVS notation (most 3prime end).");
		System.err.println("\t-oicr                           : Add OICR tag in VCF file. Default: " + useOicr);
		System.err.println("\t-sequenceOntology               : Use Sequence Ontology terms. Default: " + useSequenceOntology);

		usageGenericAndDb();

		System.exit(-1);
	}

}
