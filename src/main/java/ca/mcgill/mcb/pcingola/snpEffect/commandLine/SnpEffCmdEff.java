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
import ca.mcgill.mcb.pcingola.fileIterator.PileUpFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.SeqChangeFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.SeqChangeTxtFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.filter.ChangeEffectFilter;
import ca.mcgill.mcb.pcingola.filter.SeqChangeFilter;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.codonChange.CodonChange;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.motif.Jaspar;
import ca.mcgill.mcb.pcingola.motif.Pwm;
import ca.mcgill.mcb.pcingola.outputFormatter.BedAnnotationOutputFormatter;
import ca.mcgill.mcb.pcingola.outputFormatter.BedOutputFormatter;
import ca.mcgill.mcb.pcingola.outputFormatter.OutputFormatter;
import ca.mcgill.mcb.pcingola.outputFormatter.TxtOutputFormatter;
import ca.mcgill.mcb.pcingola.outputFormatter.VcfOutputFormatter;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.eff.MasterEff;
import ca.mcgill.mcb.pcingola.stats.ChangeEffectResutStats;
import ca.mcgill.mcb.pcingola.stats.CountByType;
import ca.mcgill.mcb.pcingola.stats.SeqChangeStats;
import ca.mcgill.mcb.pcingola.stats.VcfStats;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.util.Tuple;
import ca.mcgill.mcb.pcingola.vcf.PedigreeEnrty;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Command line program: Predict changes
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
	boolean canonical = false; // Use only canonical transcripts
	boolean supressOutput = false; // Only used for debugging purposes 
	boolean createSummary = true; // Do not create summary output file 
	boolean download = false; // Download genome, if not available 
	boolean useHgvs = false; // Use Hgvs notation
	boolean useLocalTemplate = false; // Use template from 'local' file instead of 'jar' (this is only used for development and debugging)
	boolean useSequenceOntolgy = false; // Use Sequence Ontolgy terms
	boolean useOicr = false; // Use OICR tag
	Boolean treatAllAsProteinCoding = null; // Only use coding genes. Default is 'null' which means 'auto'
	boolean chromoPlots = true; // Create mutations by chromosome plots?
	boolean onlyRegulation = false; // Only build regulation tracks
	boolean lossOfFunction = false; // Create loss of function LOF tag?
	boolean useGeneId = false; // Use gene ID instead of gene name (VCF output)
	boolean nextProt = false; // Annotate using NextProt database
	boolean motif = false; // Annotate using motifs
	boolean createCsvSummary = false; // Use a CSV as output summary
	int upDownStreamLength = SnpEffectPredictor.DEFAULT_UP_DOWN_LENGTH; // Upstream & downstream interval length
	int spliceSiteSize = SpliceSite.CORE_SPLICE_SITE_SIZE; // Splice site size default: 2 bases (canonical splice site)
	int totalErrs = 0;
	long countInputLines = 0, countVariants = 0, countEffects = 0, countVariantsFilteredOut = 0;
	String chrStr = "";
	String inputFile = ""; // Input file
	ArrayList<String> inputFiles;
	String summaryFile; // Summary output file
	String summaryGenesFile; // Gene table file
	String onlyTranscriptsFile = null; // Only use the transcripts in this file (Format: One transcript ID per line)
	String cancerSamples = null;
	SeqChangeFilter seqChangeFilter; // Filter seqChanges (before prediction)
	InputFormat inputFormat = InputFormat.VCF; // Format use in input files
	OutputFormat outputFormat = OutputFormat.VCF; // Output format
	ChangeEffectFilter changeEffectResutFilter; // Filter prediction results
	ArrayList<String> filterIntervalFiles;// Files used for filter intervals
	IntervalForest filterIntervals; // Filter only seqChanges that match these intervals
	ArrayList<String> customIntervalFiles; // Custom interval files (bed)
	SeqChangeStats seqChangeStats;
	ChangeEffectResutStats changeEffectResutStats;
	VcfStats vcfStats;
	HashSet<String> regulationTracks = new HashSet<String>();
	List<VcfEntry> vcfEntriesDebug = null; // Use for debugging or testing (in some test-cases)

	public SnpEffCmdEff() {
		super();
		upDownStreamLength = SnpEffectPredictor.DEFAULT_UP_DOWN_LENGTH; // Upstream & downstream interval length
		chrStr = ""; // Default: Don't show 'chr' before chromosome
		inputFile = ""; // seqChange input file
		seqChangeFilter = new SeqChangeFilter(); // Filter seqChanges (before prediction)
		changeEffectResutFilter = new ChangeEffectFilter(); // Filter prediction results
		filterIntervalFiles = new ArrayList<String>(); // Files used for filter intervals
		filterIntervals = new IntervalForest(); // Filter only seqChanges that match these intervals
		customIntervalFiles = new ArrayList<String>(); // Custom interval files
		summaryFile = DEFAULT_SUMMARY_FILE;
		summaryGenesFile = DEFAULT_SUMMARY_GENES_FILE;
	}

	/**
	 * Analyze which comparisons to make in cancer genomes
	 * @param vcfEntry
	 * @param pedigree
	 * @return
	 */
	Set<Tuple<Integer, Integer>> compareCancerGenotypes(VcfEntry vcfEntry, List<PedigreeEnrty> pedigree) {
		HashSet<Tuple<Integer, Integer>> comparisons = new HashSet<Tuple<Integer, Integer>>();

		// Find out which comparisons have to be analyzed
		for (PedigreeEnrty pe : pedigree) {
			if (pe.isDerived()) {
				VcfGenotype genOri = vcfEntry.getVcfGenotype(pe.getOriginalNum());
				VcfGenotype genDer = vcfEntry.getVcfGenotype(pe.getDerivedNum());

				int gd[] = genDer.getGenotype(); // Derived genotype
				int go[] = genOri.getGenotype(); // Original genotype

				if (genOri.isPhased() && genDer.isPhased()) {
					// Phased, we only have two possible comparissons
					// TODO: Check if this is correct for phased genotypes!
					for (int i = 0; i < 2; i++) {
						// Add comparissons
						// TODO: Decide if we want to keep "back to reference" analysis (i.e. gd[d] == 0)
						// if ((go[i] >= 0) && (gd[i] >= 0) // Both genotypes are non-missing?
						if ((go[i] > 0) && (gd[i] > 0) // Both genotypes are non-missing?
								&& (go[i] != 0) // Origin genotype is non-reference? (this is always analyzed in the default mode)
								&& (gd[i] != go[i]) // Both genotypes are different?
						) {
							Tuple<Integer, Integer> compare = new Tuple<Integer, Integer>(gd[i], go[i]);
							comparisons.add(compare);
						}
					}
				} else {
					// Phased, we only have two possible comparissons	
					for (int d = 0; d < gd.length; d++)
						for (int o = 0; o < go.length; o++) {
							// Add comparissons 
							// TODO: Decide if we want to keep "back to reference" analysis (i.e. gd[d] == 0)
							// if ((go[o] >= 0) && (gd[d] >= 0) // Both genotypes are non-missing?
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

	public ChangeEffectResutStats getChangeEffectResutStats() {
		return changeEffectResutStats;
	}

	public SeqChangeStats getSeqChangeStats() {
		return seqChangeStats;
	}

	/**
	 * Iterate on all inputs and calculate effects.
	 * Note: This is used for all input formats except VCF, which has a different iteration modality
	 * 
	 * @param outputFormatter
	 */
	void iterateSeqChange(String inputFile, OutputFormatter outputFormatter) {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		// Create an input file iterator
		SeqChangeFileIterator seqChangeFileIterator;
		if (inputFormat == InputFormat.PILEUP) seqChangeFileIterator = new PileUpFileIterator(inputFile, config.getGenome(), inOffset);
		else if (inputFormat == InputFormat.BED) seqChangeFileIterator = new BedFileIterator(inputFile, config.getGenome(), inOffset);
		else if (inputFormat == InputFormat.TXT) seqChangeFileIterator = new SeqChangeTxtFileIterator(inputFile, config.getGenome(), inOffset);
		else throw new RuntimeException("Cannot create SeqChange file iterator on input format '" + inputFormat + "'");

		//---
		// Iterate over input file
		//---
		for (SeqChange seqChange : seqChangeFileIterator) {
			try {
				countInputLines++;

				countVariants += seqChange.getChangeOptionCount();
				if (verbose && (countVariants % SHOW_EVERY == 0)) Timer.showStdErr("\t" + countVariants + " variants");

				// Does it pass the filter? => Analyze
				if ((seqChangeFilter == null) || seqChangeFilter.filter(seqChange)) {

					// Skip if there are filter intervals and they are not matched 
					if ((filterIntervals != null) && (filterIntervals.stab(seqChange).size() <= 0)) continue;

					// Perform basic statistics about this seqChange
					if (createSummary) seqChangeStats.sample(seqChange);

					// Calculate effects
					List<ChangeEffect> changeEffects = snpEffectPredictor.seqChangeEffect(seqChange);

					// Create new 'section'
					outputFormatter.startSection(seqChange);

					// Show results
					for (ChangeEffect changeEffect : changeEffects) {
						changeEffectResutStats.sample(changeEffect); // Perform basic statistics about this result
						outputFormatter.add(changeEffect);
						countEffects++;
					}

					// Finish up this section
					outputFormatter.printSection(seqChange);

				} else countVariantsFilteredOut += seqChange.getChangeOptionCount();
			} catch (Throwable t) {
				totalErrs++;
				error(t, "Error while processing variant (line " + seqChangeFileIterator.getLineNum() + ") :\n\t" + seqChange + "\n" + t);
			}
		}

		// Close file iterator (not really needed, but just in case)
		seqChangeFileIterator.close();
	}

	/**
	 * Iterate on all inputs (VCF) and calculate effects.
	 * Note: This is used only on input format VCF, which has a different iteration modality
	 * 
	 * TODO: Effect analysis should be in a separate class, so we can easily reuse it for single or mutli-threaded modes.
	 *       SnpEffCmdEff should only parse command line, and then invoke the other class (now everything is here, it's a mess)
	 * 
	 * @param outputFormatter
	 */
	void iterateVcf(String inputFile, OutputFormatter outputFormatter) {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		// Open VCF file
		VcfFileIterator vcfFile = new VcfFileIterator(inputFile, config.getGenome());
		vcfFile.setInOffset(inOffset); // May be there is a special inOffset (not likely to happen).

		boolean anyCancerSample = false;
		List<PedigreeEnrty> pedigree = null;
		CountByType errByType = new CountByType(), warnByType = new CountByType();

		for (VcfEntry vcfEntry : vcfFile) {
			boolean printed = false;

			try {
				countInputLines++;

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
				if ((filterIntervals != null) && (filterIntervals.query(vcfEntry).isEmpty())) continue;

				// Create new 'section'
				outputFormatter.startSection(vcfEntry);

				//---
				// Analyze all changes in this VCF entry
				// Note, this is the standard analysis. 
				// Next section deals with cancer: Somatic vs Germline comparisons 
				//---
				boolean impact = false; // Does this entry have an impact (other than MODIFIER)?
				List<SeqChange> seqChanges = vcfEntry.seqChanges();
				for (SeqChange seqChange : seqChanges) {
					countVariants += seqChange.getChangeOptionCount();
					if (verbose && (countVariants % SHOW_EVERY == 0)) Timer.showStdErr("\t" + countVariants + " variants");

					// Does it pass the filter? => Analyze
					if ((seqChangeFilter == null) || seqChangeFilter.filter(seqChange)) {
						// Perform basic statistics about this seqChange
						if (createSummary) seqChangeStats.sample(seqChange);

						// Calculate effects
						List<ChangeEffect> changeEffects = snpEffectPredictor.seqChangeEffect(seqChange);

						// Create new 'section'
						outputFormatter.startSection(seqChange);

						// Show results
						for (ChangeEffect changeEffect : changeEffects) {
							if (createSummary) changeEffectResutStats.sample(changeEffect); // Perform basic statistics about this result

							// Any errors or warnings?
							if (changeEffect.hasError()) errByType.inc(changeEffect.getError());
							if (changeEffect.hasWarning()) warnByType.inc(changeEffect.getWarning());

							// Does this entry have an impact (other than MODIFIER)?
							impact |= (changeEffect.getEffectImpact() != EffectImpact.MODIFIER);

							outputFormatter.add(changeEffect);
							countEffects++;
						}

						// Finish up this section
						outputFormatter.printSection(seqChange);

					} else countVariantsFilteredOut += seqChange.getChangeOptionCount();
				}

				//---
				// Do we analyze cancer samples?
				// Here we deal with Somatic vs Germline comparisons 
				//---
				if (anyCancerSample && impact && vcfEntry.isMultipleAlts()) {
					// Calculate all required comparissons
					Set<Tuple<Integer, Integer>> comparisons = compareCancerGenotypes(vcfEntry, pedigree);

					// Analyze each comparison
					for (Tuple<Integer, Integer> comp : comparisons) {
						// We have to compare comp.first vs comp.second
						int altGtNum = comp.first; // comp.first is 'derived' (our new ALT)
						int refGtNum = comp.second; // comp.second is 'original' (our new REF)

						SeqChange seqChangeRef = seqChanges.get(refGtNum - 1); // After applying this seqChange, we get the new 'reference'
						SeqChange seqChangeAlt = seqChanges.get(altGtNum - 1); // This our new 'seqChange'

						// Calculate effects
						List<ChangeEffect> changeEffects = snpEffectPredictor.seqChangeEffect(seqChangeAlt, seqChangeRef);

						// Create new 'section'
						outputFormatter.startSection(seqChangeAlt);

						// Show results (note, we don't add these to the statistics)
						for (ChangeEffect changeEffect : changeEffects)
							outputFormatter.add(changeEffect);

						// Finish up this section
						outputFormatter.printSection(seqChangeAlt);
					}
				}

				// Finish up this section
				outputFormatter.printSection(vcfEntry);
				printed = true;

			} catch (Throwable t) {
				totalErrs++;
				error(t, "Error while processing VCF entry (line " + vcfFile.getLineNum() + ") :\n\t" + vcfEntry + "\n" + t);
			} finally {
				if (!printed) outputFormatter.printSection(vcfEntry);
			}
		}

		// Close file iterator (not really needed, but just in case)
		vcfFile.close();

		// Show errors and warnings
		if (!errByType.isEmpty()) System.err.println("\nERRORS: Some errors were detected\nError type\tNumber of errors\n" + errByType + "\n");
		if (!warnByType.isEmpty()) System.err.println("\nWARNINGS: Some warning were detected\nWarning type\tNumber of warnings\n" + warnByType + "\n");
	}

	/**
	 * Multi-threaded iteration on VCF inputs and calculates effects.
	 * Note: This is used only on input format VCF, which has a different iteration modality
	 * 
	 * @param outputFormatter
	 */
	void iterateVcfMulti(String inputFile, final OutputFormatter outputFormatter) {
		if (verbose) Timer.showStdErr("Running multi-threaded mode (numThreads=" + numWorkers + ").");

		outputFormatter.setShowHeader(false); // Master process takes care of the header (instead of outputFormatter). Otherwise you get the header printed one time per worker.

		// We need final variables for the inner class
		final SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
		final VcfOutputFormatter vcfOutForm = (VcfOutputFormatter) outputFormatter;
		final SnpEffCmdEff snpEffCmdEff = this;

		// Open VCF file
		VcfFileIterator vcfFile = new VcfFileIterator(inputFile, config.getGenome());
		vcfFile.setInOffset(inOffset); // May be there is a special inOffset (not likely to happen).

		// Master factory 
		Props props = new Props(new UntypedActorFactory() {

			private static final long serialVersionUID = 1L;

			@Override
			public Actor create() {
				MasterEff master = new MasterEff(numWorkers, snpEffCmdEff, snpEffectPredictor, outputFormatter, filterIntervals, seqChangeFilter);
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
	 * @param inputFile
	 * @return
	 */
	String outputFile(String inputFile) {
		// Remove GZ extention
		String base = Gpr.baseName(inputFile, ".gz");

		// Remove extension according to input format
		switch (inputFormat) {
		case TXT:
			base = Gpr.baseName(inputFile, ".txt");
			break;
		case BED:
			base = Gpr.baseName(inputFile, ".bed");
			break;
		case VCF:
			base = Gpr.baseName(inputFile, ".vcf");
			break;
		case PILEUP:
			base = Gpr.baseName(inputFile, ".pileup");
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
		case TXT:
			outputFile += ".txt";
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
	 * @param args
	 */
	@Override
	public void parseArgs(String[] args) {
		boolean isFileList = false;
		this.args = args;

		for (int i = 0; i < args.length; i++) {

			String arg = args[i];

			// Is it a command line option?
			if (isOpt(arg)) {

				//---
				// Generic options.
				// Note: OPtions config, verbose, debug, quiet, etc. at parse by SnpEff (parent class) 
				//---
				if (arg.equals("-1")) inOffset = outOffset = 1;
				else if (arg.equals("-0")) inOffset = outOffset = 0;
				else if (arg.equals("-h") || arg.equalsIgnoreCase("-help")) {
					usage(null);
					System.exit(0);
				} else if (arg.equals("-t")) {
					multiThreaded = true;
					createSummary = false; // Implies '-noStats'
				} else if (arg.equalsIgnoreCase("-fileList")) isFileList = true;

				//---
				// Output options
				//---
				else if (arg.equals("-o")) {
					// Output format
					if ((i + 1) < args.length) {
						String outFor = args[++i].toUpperCase();

						if (outFor.equals("TXT")) {
							outputFormat = OutputFormat.TXT;
							outOffset = 1; // Implies '-1' since TXT coordinates are one-based
						} else if (outFor.equals("VCF")) {
							outputFormat = OutputFormat.VCF;
							outOffset = 1; // Implies '-1' since VCF coordinates are one-based
						} else if (outFor.equals("GATK")) {
							outputFormat = OutputFormat.GATK;
							outOffset = 1; // Implies '-1' since VCF coordinates are one-based
						} else if (outFor.equals("BED")) {
							outputFormat = OutputFormat.BED;
							outOffset = 0; // Implies '0' since BED coordinates are zero-based
						} else if (outFor.equals("BEDANN")) {
							outputFormat = OutputFormat.BEDANN;
							outOffset = 0; // Implies '0' since BED coordinates are zero-based
						} else usage("Unknown output file format '" + outFor + "'");
					}
				} else if ((arg.equals("-a") || arg.equalsIgnoreCase("-around"))) {
					if ((i + 1) < args.length) CodonChange.SHOW_CODONS_AROUND_CHANGE = Gpr.parseIntSafe(args[++i]);
					else usage("Option '-i' without config interval_file argument");
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
				} else if ((arg.equals("-of") || arg.equalsIgnoreCase("-outOffset"))) {
					if ((i + 1) < args.length) outOffset = Gpr.parseIntSafe(args[++i]);
				} else if (arg.equalsIgnoreCase("-chr")) chrStr = args[++i];
				else if (arg.equalsIgnoreCase("-useLocalTemplate")) useLocalTemplate = true; // Undocumented option (only used for development & debugging)
				else if (arg.equalsIgnoreCase("-noOut")) supressOutput = true; // Undocumented option (only used for development & debugging)
				else if (arg.equalsIgnoreCase("-noChromoPlots")) chromoPlots = false;
				//---
				// Annotation options
				//---
				else if (arg.equalsIgnoreCase("-treatAllAsProteinCoding")) {
					if ((i + 1) < args.length) {
						i++;
						if (args[i].equalsIgnoreCase("auto")) treatAllAsProteinCoding = null;
						else treatAllAsProteinCoding = Gpr.parseBoolSafe(args[i]);
					}
				} else if (arg.equalsIgnoreCase("-cancer")) cancer = true; // Perform cancer comparissons
				else if (arg.equalsIgnoreCase("-cancerSamples")) {
					if ((i + 1) < args.length) cancerSamples = args[++i]; // Read cancer samples from TXT files
					else usage("Missing -cancerSamples argument");
				} else if (arg.equalsIgnoreCase("-canon")) canonical = true; // Use canonical transcripts
				else if (arg.equalsIgnoreCase("-download")) download = true; // Download genome if not availble
				else if (arg.equalsIgnoreCase("-lof")) lossOfFunction = true; // Add LOF tag
				else if (arg.equalsIgnoreCase("-hgvs")) useHgvs = true; // Use HGVS notation
				else if (arg.equalsIgnoreCase("-geneId")) useGeneId = true; // Use gene ID instead of gene name
				else if (arg.equalsIgnoreCase("-sequenceOntolgy")) useSequenceOntolgy = true; // Use SO temrs
				else if (arg.equalsIgnoreCase("-oicr")) useOicr = true; // Use OICR tag
				else if (arg.equalsIgnoreCase("-onlyTr")) {
					if ((i + 1) < args.length) onlyTranscriptsFile = args[++i]; // Only use the transcripts in this file
				}
				//---
				// Input options
				//---
				else if (arg.equalsIgnoreCase("-interval")) {
					if ((i + 1) < args.length) customIntervalFiles.add(args[++i]);
					else usage("Option '-interval' without config interval_file argument");
				} else if ((arg.equals("-fi") || arg.equalsIgnoreCase("-filterInterval"))) {
					if ((i + 1) < args.length) filterIntervalFiles.add(args[++i]);
					else usage("Option '-fi' without config filter_interval_file argument");
				} else if (arg.equals("-i")) {
					// Input format
					if ((i + 1) < args.length) {
						String inFor = args[++i].toUpperCase();

						if (inFor.equals("TXT")) {
							inputFormat = InputFormat.TXT;
							outputFormat = OutputFormat.TXT;
							inOffset = 1; // Implies '-1' since TXT coordinates are one-based
						} else if (inFor.equals("PILEUP")) {
							inputFormat = InputFormat.PILEUP;
							outputFormat = OutputFormat.TXT;
							inOffset = 1; // Implies '-1' since PILEUP coordinates are one-based
						} else if (inFor.equals("VCF")) {
							inputFormat = InputFormat.VCF;
							outputFormat = OutputFormat.VCF;
							inOffset = 1; // Implies '-1' since VCF coordinates are one-based
						} else if (inFor.equals("BED")) {
							inputFormat = InputFormat.BED;
							outputFormat = OutputFormat.BED;
							inOffset = 0; // Implies '-0' since Bed coordinates are zero-based
						} else usage("Unknown input file format '" + inFor + "'");
					} else usage("Missing input format in command line option '-i'");
				} else if ((arg.equals("-if") || arg.equalsIgnoreCase("-inOffset"))) {
					if ((i + 1) < args.length) inOffset = Gpr.parseIntSafe(args[++i]);
				}
				//---
				// Regulation options
				//---
				else if (arg.equals("-onlyReg")) onlyRegulation = true;
				else if (arg.equals("-reg")) {
					if ((i + 1) < args.length) regulationTracks.add(args[++i]); // Add this track to the list
				}
				//---
				// NextProt database
				//---
				else if (arg.equalsIgnoreCase("-nextProt")) nextProt = true; // Use NextProt database
				else if (arg.equalsIgnoreCase("-motif")) motif = true; // Use motif database
				//---
				// Filters
				//---
				else if ((arg.equals("-minQ") || arg.equalsIgnoreCase("-minQuality"))) {
					if ((i + 1) < args.length) seqChangeFilter.setMinQuality(Gpr.parseIntSafe(args[++i]));
				} else if ((arg.equals("-maxQ") || arg.equalsIgnoreCase("-maxQuality"))) {
					if ((i + 1) < args.length) seqChangeFilter.setMaxQuality(Gpr.parseIntSafe(args[++i]));
				} else if ((arg.equals("-minC") || arg.equalsIgnoreCase("-minCoverage"))) {
					if ((i + 1) < args.length) seqChangeFilter.setMinCoverage(Gpr.parseIntSafe(args[++i]));
				} else if ((arg.equals("-maxC") || arg.equalsIgnoreCase("-maxCoverage"))) {
					if ((i + 1) < args.length) seqChangeFilter.setMaxCoverage(Gpr.parseIntSafe(args[++i]));
				} else if ((arg.equals("-ud") || arg.equalsIgnoreCase("-upDownStreamLen"))) {
					if ((i + 1) < args.length) upDownStreamLength = Gpr.parseIntSafe(args[++i]);
				} else if ((arg.equals("-ss") || arg.equalsIgnoreCase("-spliceSiteSize"))) {
					if ((i + 1) < args.length) spliceSiteSize = Gpr.parseIntSafe(args[++i]);
				} else if (arg.equals("-hom")) seqChangeFilter.setHeterozygous(false);
				else if (arg.equals("-het")) seqChangeFilter.setHeterozygous(true);
				else if (arg.equals("-snp")) seqChangeFilter.setChangeType(SeqChange.ChangeType.SNP);
				else if (arg.equals("-mnp")) seqChangeFilter.setChangeType(SeqChange.ChangeType.MNP);
				else if (arg.equals("-ins")) seqChangeFilter.setChangeType(SeqChange.ChangeType.INS);
				else if (arg.equals("-del")) seqChangeFilter.setChangeType(SeqChange.ChangeType.DEL);
				else if (arg.equalsIgnoreCase("-no-downstream")) changeEffectResutFilter.setDownstream(true);
				else if (arg.equalsIgnoreCase("-no-upstream")) changeEffectResutFilter.setUpstream(true);
				else if (arg.equalsIgnoreCase("-no-intergenic")) changeEffectResutFilter.setIntergenic(true);
				else if (arg.equalsIgnoreCase("-no-intron")) changeEffectResutFilter.setIntron(true);
				else if (arg.equalsIgnoreCase("-no-utr")) changeEffectResutFilter.setUtr(true);
				else if (arg.equalsIgnoreCase("-no")) {
					String filterOut = "";
					if ((i + 1) < args.length) filterOut = args[++i];

					String filterOutArray[] = filterOut.split(",");
					for (String filterStr : filterOutArray) {
						if (filterStr.equalsIgnoreCase("downstream")) changeEffectResutFilter.setDownstream(true);
						else if (filterStr.equalsIgnoreCase("upstream")) changeEffectResutFilter.setUpstream(true);
						else if (filterStr.equalsIgnoreCase("intergenic")) changeEffectResutFilter.setIntergenic(true);
						else if (filterStr.equalsIgnoreCase("intron")) changeEffectResutFilter.setIntron(true);
						else if (filterStr.equalsIgnoreCase("utr")) changeEffectResutFilter.setUtr(true);
						else if (filterStr.equalsIgnoreCase("None")) ; // OK, nothing to do
						else usage("Unknown filter option '" + filterStr + "'");
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
		if (multiThreaded && cancer) usage("Cancer analysis is currently not supported in multi-threaded mode.");
		if (multiThreaded && !isOutVcf) usage("Multi-threaded option is only supported when when output is in VCF format");
		if (multiThreaded && createSummary) usage("Multi-threaded option should be used with 'noStats'.");
	}

	/**
	 * Read a custom interval file
	 * @param intFile
	 */
	protected int readCustomIntFile(String intFile) {
		Markers markers = readMarkers(intFile);

		// Add all markers to predictor
		for (Marker m : markers)
			config.getSnpEffectPredictor().add(m);

		// Number added
		return markers.size();
	}

	/**
	 * Read a file after checking for some common error conditions
	 * @param fileName
	 * @return
	 */
	String readFile(String fileName) {
		File file = new File(fileName);
		if (!file.exists()) fatalError("No such file '" + fileName + "'");
		if (!file.canRead()) fatalError("Cannot open file '" + fileName + "'");
		return Gpr.readFile(fileName);
	}

	/**
	 * Read a filter custom interval file
	 * @param intFile
	 */
	int readFilterIntFile(String intFile) {
		Markers markers = readMarkers(intFile);
		for (Marker filterInterval : markers)
			filterIntervals.add(filterInterval);
		return markers.size();
	}

	/**
	 * Read regulation motif files
	 */
	void readMotif() {
		if (verbose) Timer.showStdErr("Loading Motifs and PWMs");

		//---
		// Sanity checks
		//---
		String pwmsFileName = config.getDirDataVersion() + "/pwms.bin";
		if (!Gpr.exists(pwmsFileName)) fatalError("Warning: Cannot open PWMs file " + pwmsFileName);

		String motifBinFileName = config.getBaseFileNameMotif() + ".bin";
		if (!Gpr.exists(motifBinFileName)) fatalError("Warning: Cannot open Motifs file " + motifBinFileName);

		//---
		// Load all PWMs
		//---
		if (verbose) Timer.showStdErr("\tLoading PWMs from : " + pwmsFileName);
		Jaspar jaspar = new Jaspar();
		jaspar.load(pwmsFileName);

		//---
		// Read motifs
		//---		
		if (verbose) Timer.showStdErr("\tLoading Motifs from file '" + motifBinFileName + "'");

		MarkerSerializer markerSerializer = new MarkerSerializer();
		Markers motifsDb = markerSerializer.load(motifBinFileName);

		// Add (only) motif markers. The original motifs has to be serialized with Chromosomes, Genomes and other markers (otherwise it could have not been saved)
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
		int countAddded = 0;
		for (Marker m : motifsDb)
			if (m instanceof Motif) {
				Motif motif = (Motif) m;

				// Connect motifs to their respective PWMs
				Pwm pwm = jaspar.getPwm(motif.getPwmId());
				if (pwm != null) {
					// Set PWM and add to snpEffPredictor
					motif.setPwm(pwm);
					snpEffectPredictor.add(motif);
					countAddded++;
				} else Timer.showStdErr("Cannot find PWM for motif '" + motif.getId() + "'");
			}

		if (verbose) Timer.showStdErr("\tMotif database: " + countAddded + " markers loaded.");
	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 * @param regTrack
	 */
	void readNextProt() {
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();

		//---
		// Read nextProt binary file
		//---
		String nextProtBinFile = config.getDirDataVersion() + "/nextProt.bin";
		if (verbose) Timer.showStdErr("Reading NextProt database from file '" + nextProtBinFile + "'");

		MarkerSerializer markerSerializer = new MarkerSerializer();
		Markers nextProtDb = markerSerializer.load(nextProtBinFile);

		// Create a collection of (only) NextProt markers. The original nextProtDb has Chromosomes, Genomes and other markers (otherwise it could have not been saved)
		ArrayList<NextProt> nextProts = new ArrayList<NextProt>(nextProtDb.size());
		for (Marker m : nextProtDb)
			if (m instanceof NextProt) nextProts.add((NextProt) m);

		if (verbose) Timer.showStdErr("NextProt database: " + nextProts.size() + " markers loaded.");

		//---
		// Connect nextProt annotations to transcripts and exons 
		//---
		if (verbose) Timer.showStdErr("Adding transcript info to NextProt markers.");

		// Create a list of all transcripts
		HashMap<String, Transcript> trs = new HashMap<String, Transcript>();
		for (Gene g : snpEffectPredictor.getGenome().getGenes())
			for (Transcript tr : g)
				trs.put(tr.getId(), tr);

		// Find the corresponding transcript for each nextProt marker 
		// WARNING: The transcripts might be filtered out by the user (e.g. '-cannon' command line option or user defined sets). 
		//          We only keep nextProt markers associated to found transcripts. All others are discarded (the user doesn't want that info).
		ArrayList<NextProt> nextProtsToAdd = new ArrayList<NextProt>();
		for (NextProt np : nextProts) {
			Transcript tr = trs.get(np.getTranscriptId());

			// Found transcript, now try to find an exon
			if (tr != null) {
				boolean assignedToExon = false;
				for (Exon ex : tr) {
					if (ex.intersects(np)) {
						NextProt npEx = (NextProt) np.clone(); // The nextProt marker might cover more than one Exon 
						npEx.setParent(ex);
						nextProtsToAdd.add(npEx);
						assignedToExon = true;
					}
				}

				// Not assigned to an exon? Add transcript info
				if (!assignedToExon) {
					np.setParent(tr); // Set this transcript as parent
					nextProtsToAdd.add(np);
				}
			}
		}

		//---
		// Add all nextProt marker to predictor
		//---
		for (NextProt np : nextProtsToAdd)
			snpEffectPredictor.add(np);

		// Note: We might end up with more markers than we loaded (just because they map to multiple exons (although it would be highly unusual)
		if (verbose) Timer.showStdErr("NextProt database: " + nextProtsToAdd.size() + " markers added.");
	}

	/**
	 * Read pedigree either from VCF header or from cancerSample file
	 * 
	 * @param vcfFile
	 * @return
	 */
	List<PedigreeEnrty> readPedigree(VcfFileIterator vcfFile) {
		List<PedigreeEnrty> pedigree = null;

		if (cancerSamples != null) {
			// Read from TXT file
			if (verbose) Timer.show("Reading cancer samples pedigree from file '" + cancerSamples + "'.");

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
			if (verbose) Timer.show("Reading cancer samples pedigree from VCF header.");
			pedigree = vcfFile.getVcfHeader().getPedigree();
		}

		if (verbose && ((pedigree == null) || pedigree.isEmpty())) Timer.show("Warngin: No cancer sample pedigree found.");
		return pedigree;
	}

	/**
	 * Read regulation track and update SnpEffectPredictor
	 * @param regTrack
	 */
	@SuppressWarnings("unchecked")
	void readRegulationTrack(String regTrack) {
		//---
		// Read file
		//---
		if (verbose) Timer.showStdErr("Reading regulation track '" + regTrack + "'");
		String regFile = config.getDirDataVersion() + "/regulation_" + regTrack + ".bin";
		ArrayList<Regulation> regulation = (ArrayList<Regulation>) Gpr.readFileSerializedGz(regFile);

		//---
		// Are all chromosomes available?
		//---
		Genome genome = config.getGenome();
		HashMap<String, Integer> chrs = new HashMap<String, Integer>();
		for (Regulation r : regulation) {
			String chr = r.getChromosomeName();
			int max = chrs.containsKey(chr) ? chrs.get(chr) : 0;
			max = Math.max(max, r.getEnd());
			chrs.put(chr, max);
		}

		// Add all chromos
		for (String chr : chrs.keySet())
			if (genome.getChromosome(chr) == null) genome.add(new Chromosome(genome, 0, chrs.get(chr), 1, chr));

		//---
		// Add all markers to predictor
		//---
		SnpEffectPredictor snpEffectPredictor = config.getSnpEffectPredictor();
		for (Regulation r : regulation)
			snpEffectPredictor.add(r);
	}

	@Override
	public HashMap<String, String> reportValues() {
		HashMap<String, String> report = super.reportValues();
		if (seqChangeStats != null) report.put("SeqChanges", seqChangeStats.getCount() + "");
		return report;
	}

	/**
	 * Run according to command line options
	 */
	@Override
	public boolean run() {
		run(false);
		return true;
	}

	/**
	 * Run according to command line options
	 */
	public List<VcfEntry> run(boolean createList) {
		//---
		// Prepare to run
		//---

		// Nothing to filter out => don't waste time
		if (!changeEffectResutFilter.anythingSet()) changeEffectResutFilter = null;

		filterIntervals = null;

		// Read config file
		readConfig();

		// Read database (or create a new one)
		if (onlyRegulation) {
			// Create predictor
			config.setSnpEffectPredictor(new SnpEffectPredictor(config.getGenome()));
			config.setOnlyRegulation(true);
			config.setErrorOnMissingChromo(false); // A chromosome might be missing (e.g. no regulation tracks available for 'MT')
			config.setErrorChromoHit(false); // A chromosome's length might be smaller than the real (it's calculated using regulation features, not real chromo data)
		} else {
			// Read
			if (verbose) Timer.showStdErr("Reading database for genome version '" + genomeVer + "' from file '" + config.getFileSnpEffectPredictor() + "' (this might take a while)");

			// Try to download database if it doesn't exists?
			if (download && !Gpr.canRead(config.getFileSnpEffectPredictor())) {
				if (verbose) Timer.showStdErr("Database not installed\n\tAttempting to download and install database '" + genomeVer + "'");

				// Run download command
				String downloadArgs[] = { genomeVer };
				SnpEffCmdDownload snpEffCmdDownload = new SnpEffCmdDownload();
				boolean ok = run(snpEffCmdDownload, downloadArgs, null);
				if (!ok) throw new RuntimeException("Genome download failed!");
				else if (verbose) Timer.showStdErr("Database installed.");
			}

			config.loadSnpEffectPredictor(); // Read snpEffect predictor
			if (verbose) Timer.showStdErr("done");
		}

		// Check if we can open the input file (no need to check if it is STDIN)
		if (!Gpr.canRead(inputFile)) usage("Cannot open input file '" + inputFile + "'");

		// Set 'treatAllAsProteinCoding'
		if (treatAllAsProteinCoding != null) config.setTreatAllAsProteinCoding(treatAllAsProteinCoding);
		else {
			// treatAllAsProteinCoding was set to 'auto'
			// I.e.: Use 'true' if there is protein coding info, otherwise use false.
			boolean tapc = !config.getGenome().hasCodingInfo();
			if (debug) Timer.showStdErr("Setting '-treatAllAsProteinCoding' to '" + tapc + "'");
			config.setTreatAllAsProteinCoding(tapc);
		}

		// Read custom interval files
		for (String intFile : customIntervalFiles) {
			if (verbose) Timer.showStdErr("Reading interval file '" + intFile + "'");
			int count = readCustomIntFile(intFile);
			if (verbose) Timer.showStdErr("done (" + count + " intervals loaded). ");
		}

		// Read filter interval files
		for (String filterIntFile : filterIntervalFiles) {
			if (filterIntervals == null) filterIntervals = new IntervalForest();
			if (verbose) Timer.showStdErr("Reading filter interval file '" + filterIntFile + "'");
			int count = readFilterIntFile(filterIntFile);
			if (verbose) Timer.showStdErr("done (" + count + " intervals loaded). ");
		}

		// Read regulation tracks
		for (String regTrack : regulationTracks)
			readRegulationTrack(regTrack);

		// Build interval forest for filter (if any)
		if (filterIntervals != null) {
			if (verbose) Timer.showStdErr("Building filter interval forest");
			filterIntervals.build();
			if (verbose) Timer.showStdErr("done.");
		}

		// Set upstream-downstream interval length
		config.getSnpEffectPredictor().setUpDownStreamLength(upDownStreamLength);

		// Set splice site size
		config.getSnpEffectPredictor().setSpliceSiteSize(spliceSiteSize);

		// Filter canonical transcripts
		if (canonical) {
			if (verbose) Timer.showStdErr("Filtering out non-canonical transcripts.");
			config.getSnpEffectPredictor().removeNonCanonical();

			if (debug) {
				// Show genes and transcript (which ones are considered 'cannonica')
				Timer.showStdErr("Canonical transcripts:\n\t\tgeneName\tgeneId\ttranscriptId\tcdsLength");
				for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes()) {
					for (Transcript t : g) {
						String cds = t.cds();
						int cdsLen = (cds != null ? cds.length() : 0);
						System.err.println("\t\t" + g.getGeneName() + "\t" + g.getId() + "\t" + t.getId() + "\t" + cdsLen);
					}
				}
			}
			if (verbose) Timer.showStdErr("done.");
		}

		// Use transcripts set form input file
		if (onlyTranscriptsFile != null) {
			// Load file
			String onlyTr = Gpr.readFile(onlyTranscriptsFile);
			HashSet<String> trIds = new HashSet<String>();
			for (String trId : onlyTr.split("\n"))
				trIds.add(trId.trim());

			// Remove transcripts
			if (verbose) Timer.showStdErr("Filtering out transcripts in file '" + onlyTranscriptsFile + "'. Total " + trIds.size() + " transcript IDs.");
			int removed = config.getSnpEffectPredictor().retainAllTranscripts(trIds);
			if (verbose) Timer.showStdErr("Done: " + removed + " transcripts removed.");
		}

		// Read nextProt database?
		if (nextProt) readNextProt();
		if (motif) readMotif();

		// Build tree
		if (verbose) Timer.showStdErr("Building interval forest");
		config.getSnpEffectPredictor().buildForest();
		if (verbose) Timer.showStdErr("done.");

		// Show some genome stats. Chromosome names are shown, a lot of people has problems with the correct chromosome names.
		if (verbose) {
			Timer.showStdErr("Genome stats :");
			System.err.println(config.getGenome());
		}

		// Store VCF results in a list?
		if (createList) vcfEntriesDebug = new ArrayList<VcfEntry>();

		// Predict
		if (verbose) Timer.showStdErr("Predicting variants");
		if (inputFiles == null) {
			// Single input file (normal operations)
			runAnalysis(inputFile, null);
		} else {
			// Multiple input files
			for (String inputFile : inputFiles) {
				String outputFile = outputFile(inputFile);
				if (verbose) Timer.showStdErr("Analyzing file" //
						+ "\n\tInput   : '" + inputFile + "'" //
						+ "\n\tOutput  : '" + outputFile + "'" //
						+ (createSummary ? "\n\tSummary : '" + summaryFile + "'" : "") //
				);
				runAnalysis(inputFile, outputFile);
			}
		}
		if (verbose) Timer.showStdErr("done.");

		return vcfEntriesDebug;
	}

	/**
	 * Calculate the effect of variants and show results
	 * @param snpEffFile
	 */
	public void runAnalysis(String inputFile, String outputFile) {
		// Reset all counters
		totalErrs = 0;
		countInputLines = countVariants = countEffects = countVariantsFilteredOut = 0;

		// Create 'stats' objects
		seqChangeStats = new SeqChangeStats(config.getGenome());
		changeEffectResutStats = new ChangeEffectResutStats(config.getGenome());
		changeEffectResutStats.setUseSequenceOntolgy(useSequenceOntolgy);
		vcfStats = new VcfStats();

		int totalErrs = 0;

		//---
		// Create output formatter
		//---
		OutputFormatter outputFormatter = null;
		switch (outputFormat) {
		case TXT:
			outputFormatter = new TxtOutputFormatter();
			break;
		case VCF:
			VcfOutputFormatter vof = new VcfOutputFormatter(vcfEntriesDebug);
			vof.setLossOfFunction(lossOfFunction);
			vof.setConfig(config);
			outputFormatter = vof;
			break;
		case GATK:
			outputFormatter = new VcfOutputFormatter(config.getGenome());
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
		outputFormatter.setChangeEffectResutFilter(changeEffectResutFilter);
		outputFormatter.setSupressOutput(supressOutput);
		outputFormatter.setOutOffset(outOffset);
		outputFormatter.setChrStr(chrStr);
		outputFormatter.setUseSequenceOntolgy(useSequenceOntolgy);
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
			iterateSeqChange(inputFile, outputFormatter);
		}
		outputFormatter.close();

		//---
		// Create reports
		//---
		if (createSummary && (summaryFile != null)) {
			// Creates a summary output file
			if (verbose) Timer.showStdErr("Creating summary file: " + summaryFile);
			if (createCsvSummary) summary(SUMMARY_CSV_TEMPLATE, summaryFile, true);
			else summary(SUMMARY_TEMPLATE, summaryFile, false);

			// Creates genes output file
			if (verbose) Timer.showStdErr("Creating genes file: " + summaryGenesFile);
			summary(SUMMARY_GENES_TEMPLATE, summaryGenesFile, true);
		}

		if (totalErrs > 0) System.err.println(totalErrs + " errors.");
	}

	/**
	 * Creates a summary output file (using freeMarker and a template)
	 */
	void summary(String templateFile, String outputFile, boolean noCommas) {
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
		} catch (TemplateException e) {
			error(e, "Error creating summary: " + e.getMessage());
		}
	}

	/**
	 * Create a hash with all variables needed for creating summary pages
	 * @return
	 */
	HashMap<String, Object> summaryCreateHash() {
		// Create the root hash (where data objects are)
		HashMap<String, Object> root = new HashMap<String, Object>();
		root.put("args", commandLineStr(createCsvSummary ? false : true));
		root.put("changeStats", changeEffectResutStats);
		root.put("chromoPlots", chromoPlots);
		root.put("countEffects", countEffects);
		root.put("countInputLines", countInputLines);
		root.put("countVariants", countVariants);
		root.put("countVariantsFilteredOut", countVariantsFilteredOut);
		root.put("date", String.format("%1$TY-%1$Tm-%1$Td %1$TH:%1$TM", new Date()));
		root.put("genesFile", Gpr.baseName(summaryGenesFile, ""));
		root.put("genome", config.getGenome());
		root.put("genomeVersion", genomeVer);
		root.put("seqChangeFilter", seqChangeFilter);
		root.put("seqStats", seqChangeStats);
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
		System.err.println("\nvariants_file                   : Default is STDIN");
		System.err.println("\n");
		System.err.println("\nOptions:");
		System.err.println("\t-a , -around                    : Show N codons and amino acids around change (only in coding regions). Default is " + CodonChange.SHOW_CODONS_AROUND_CHANGE + " codons.");
		System.err.println("\t-chr <string>                   : Prepend 'string' to chromosome name (e.g. 'chr1' instead of '1'). Only on TXT output.");
		System.err.println("\t-download                       : Download reference genome if not available. Default: " + download);
		System.err.println("\t-i <format>                     : Input format [ vcf, txt, pileup, bed ]. Default: VCF.");
		System.err.println("\t-fileList                       : Input actually contains a list of files to process.");
		System.err.println("\t-interval                       : Use a custom intervals in TXT/BED/BigBed/VCF/GFF file (you may use this option many times)");
		System.err.println("\t-o <format>                     : Ouput format [ txt, vcf, gatk, bed, bedAnn ]. Default: VCF.");
		System.err.println("\t-s,  -stats                     : Name of stats file (summary). Default is '" + DEFAULT_SUMMARY_FILE + "'");
		System.err.println("\t-t                              : Use multiple threads (implies '-noStats'). Default 'off'");
		System.err.println("\t-noStats                        : Do not create stats (summary) file");
		System.err.println("\t-csvStats                       : Create CSV summary file instead of HTML");
		System.err.println("\nSequence change filter options:");
		System.err.println("\t-del                            : Analyze deletions only");
		System.err.println("\t-ins                            : Analyze insertions only");
		System.err.println("\t-hom                            : Analyze homozygous variants only");
		System.err.println("\t-het                            : Analyze heterozygous variants only");
		System.err.println("\t-minQ X, -minQuality X          : Filter out variants with quality lower than X");
		System.err.println("\t-maxQ X, -maxQuality X          : Filter out variants with quality higher than X");
		System.err.println("\t-minC X, -minCoverage X         : Filter out variants with coverage lower than X");
		System.err.println("\t-maxC X, -maxCoverage X         : Filter out variants with coverage higher than X");
		System.err.println("\t-nmp                            : Only MNPs (multiple nucleotide polymorphisms)");
		System.err.println("\t-snp                            : Only SNPs (single nucleotide polymorphisms)");
		System.err.println("\nResults filter options:");
		System.err.println("\t-fi  <bedFile>                  : Only analyze changes that intersect with the intervals specified in this file (you may use this option many times)");
		System.err.println("\t-no-downstream                  : Do not show DOWNSTREAM changes");
		System.err.println("\t-no-intergenic                  : Do not show INTERGENIC changes");
		System.err.println("\t-no-intron                      : Do not show INTRON changes");
		System.err.println("\t-no-upstream                    : Do not show UPSTREAM changes");
		System.err.println("\t-no-utr                         : Do not show 5_PRIME_UTR or 3_PRIME_UTR changes");
		System.err.println("\nAnnotations options:");
		System.err.println("\t-cancer                         : Perform 'cancer' comparissons (Somatic vs Germline). Default: " + cancer);
		System.err.println("\t-cancerSamples <file>           : Two column TXT file defining 'oringinal \\t derived' samples.");
		System.err.println("\t-canon                          : Only use canonical transcripts.");
		System.err.println("\t-geneId                         : Use gene ID instead of gene name (VCF output). Default: " + useGeneId);
		System.err.println("\t-hgvs                           : Use HGVS annotations for amino acid sub-field. Default: " + useHgvs);
		System.err.println("\t-lof                            : Add loss of function (LOF) and Nonsense mediated decay (NMD) tags.");
		System.err.println("\t-motif                          : Annotate using motifs (requires Motif database).");
		System.err.println("\t-nextProt                       : Annotate using NextProt (requires NextProt database).");
		System.err.println("\t-reg <name>                     : Regulation track to use (this option can be used add several times).");
		System.err.println("\t-oicr                           : Add OICR tag in VCF file. Default: " + useOicr);
		System.err.println("\t-onlyReg                        : Only use regulation tracks.");
		System.err.println("\t-onlyTr <file.txt>              : Only use the transcripts in this file. Format: One transcript ID per line.");
		System.err.println("\t-sequenceOntolgy                : Use Sequence Ontolgy terms. Default: " + useSequenceOntolgy);
		System.err.println("\t-ss, -spliceSiteSize <int>      : Set size for splice sites (donor and acceptor) in bases. Default: " + spliceSiteSize);
		System.err.println("\t-ud, -upDownStreamLen <int>     : Set upstream downstream interval length (in bases)");
		System.err.println("\nGeneric options:");
		System.err.println("\t-0                              : File positions are zero-based (same as '-inOffset 0 -outOffset 0')");
		System.err.println("\t-1                              : File positions are one-based (same as '-inOffset 1 -outOffset 1')");
		System.err.println("\t-c , -config                    : Specify config file");
		System.err.println("\t-d , -debug                     : Debug mode (very verbose).");
		System.err.println("\t-dataDir <path>                 : Override data_dir parameter from config file.");
		System.err.println("\t-h , -help                      : Show this help and exit");
		System.err.println("\t-if, -inOffset                  : Offset input by a number of bases. E.g. '-inOffset 1' for one-based input files");
		System.err.println("\t-of, -outOffset                 : Offset output by a number of bases. E.g. '-outOffset 1' for one-based output files");
		System.err.println("\t-noLog                          : Do not report usage statistics to server");
		System.err.println("\t-q , -quiet                     : Quiet mode (do not show any messages or errors)");
		System.err.println("\t-v , -verbose                   : Verbose mode");
		System.exit(-1);
	}
}
