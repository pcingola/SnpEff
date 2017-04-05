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

import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.filter.VariantEffectFilter;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantNonRef;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.outputFormatter.VcfOutputFormatter;
import org.snpeff.snpEffect.Config;
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
import org.snpeff.util.Timer;
import org.snpeff.util.Tuple;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.PedigreeEnrty;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * Command line program: Predict variant effects
 *
 * @author Pablo Cingolani
 */
public class AnnotateVcf implements VcfAnnotator {

	public static final int SHOW_EVERY = 10 * 1000;

	public static final String SUMMARY_CSV_TEMPLATE = "snpEff_csv_summary.ftl"; // Summary template file name
	public static final String SUMMARY_GENES_TEMPLATE = "snpEff_genes.ftl"; // Genes template file name
	public static final String SUMMARY_TEMPLATE = "snpEff_summary.ftl"; // Summary template file name

	Timer annotateTimer;
	boolean anyCancerSample;
	boolean cancer = false; // Perform cancer comparisons
	String cancerSamples = null;
	boolean chromoPlots = true; // Create mutations by chromosome plots?
	String chrStr = "";
	String commandLineStr, commandLineStrReport;
	Config config; // Configuration
	long countEffects = 0;
	long countInputLines = 0;
	long countVariants = 0;
	int countVcfEntries = 0;
	boolean createSummaryCsv = false;
	boolean createSummaryHtml = true;
	boolean debug = false;
	CountByType errByType, warnByType;
	String fastaProt = null;
	ArrayList<String> filterIntervalFiles;// Files used for filter intervals
	IntervalForest filterIntervals; // Filter only variants that match these intervals
	EffFormatVersion formatVersion = EffFormatVersion.DEFAULT_FORMAT_VERSION;
	boolean gatk = false; // Use GATK compatibility mode
	String genomeVer; // Genome version
	protected boolean hgvs = true; // Use Hgvs notation
	protected boolean hgvsForce = false; // Use Hgvs notation even in classic mode?
	protected boolean hgvsOld = false; // Old notation style notation: E.g. 'c.G123T' instead of 'c.123G>T' and 'X' instead of '*'
	protected boolean hgvsOneLetterAa = false; // Use 1-letter AA codes in HGVS.p notation?
	protected boolean hgvsShift = true; // Shift variants towards the 3-prime end of the transcript
	protected boolean hgvsTrId = false; // Use full transcript version in HGVS notation?
	String inputFile = ""; // Input file
	ArrayList<String> inputFiles;
	boolean lossOfFunction = true; // Create loss of function LOF tag?
	List<PedigreeEnrty> pedigree;
	boolean quiet = false;
	SnpEffectPredictor snpEffectPredictor;
	String summaryFileCsv; // HTML Summary file name
	String summaryFileHtml; // CSV Summary file name
	String summaryGenesFile; // Gene table file
	boolean suppressOutput = false; // Only used for debugging purposes
	int totalErrs = 0;
	boolean useGeneId = false; // Use gene ID instead of gene name (VCF output)
	boolean useLocalTemplate = false; // Use template from 'local' file instead of 'jar' (this is only used for development and debugging)
	boolean useOicr = false; // Use OICR tag
	boolean useSequenceOntology = true; // Use Sequence Ontology terms
	VariantEffectFilter variantEffectResutFilter; // Filter prediction results
	VariantEffectStats variantEffectStats;
	VariantStats variantStats;
	List<VcfEntry> vcfEntriesDebug = null; // Use for debugging or testing (in some test-cases)
	VcfOutputFormatter vcfOutputFormatter = null;
	VcfStats vcfStats;
	boolean verbose = false;

	public AnnotateVcf() {
	}

	@Override
	public boolean addHeaders(VcfFileIterator vcfFile) {
		// Done in VcfOutputFormatter
		return false;
	}

	public void addReportValues(HashMap<String, String> report) {
		if (variantStats != null) report.put("variants", variantStats.getCount() + "");
	}

	protected void addVariantEffect(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		vcfOutputFormatter.add(variantEffect);
	}

	/**
	 * Annotate: Calculate the effect of variants and show results
	 */
	public boolean annotate(String inputFile, String outputFile) {
		annotateInit(outputFile); // Initialize
		VcfFileIterator vcf = annotateVcf(inputFile); // Annotate
		vcfOutputFormatter.close(); // Close output
		boolean err = annotateFinish(vcf); // Create reports and finish up
		return !err;
	}

	/**
	 * Annotate a VCF entry
	 */
	@Override
	public boolean annotate(VcfEntry vcfEntry) {
		boolean filteredOut = false;
		VcfFileIterator vcfFile = vcfEntry.getVcfFileIterator();

		try {
			countInputLines++;
			countVcfEntries++;

			// Find if there is a pedigree and if it has any 'derived' entry
			if (cancer && vcfFile.isHeadeSection()) {
				pedigree = readPedigree(vcfFile);

				// Any 'derived' entry in this pedigree?
				if (pedigree != null) {
					for (PedigreeEnrty pe : pedigree)
						anyCancerSample |= pe.isDerived();
				}
			}

			// Statistics for VcfEntry
			if (createSummaryHtml || createSummaryCsv) vcfStats.sample(vcfEntry);

			// Skip if there are filter intervals and they are not matched
			if ((filterIntervals != null) && (filterIntervals.query(vcfEntry).isEmpty())) {
				filteredOut = true;
				return false;
			}

			// Set VcfEntry
			currentVcfEntry(vcfEntry);

			//---
			// Analyze all changes in this VCF entry
			//---
			boolean impactLowOrHigher = false; // Does this entry have an impact (other than MODIFIER)?
			boolean impactModerateOrHigh = false; // Does this entry have a 'MODERATE' or 'HIGH' impact?
			List<Variant> variants = vcfEntry.variants();
			for (Variant variant : variants) {
				countVariants++;
				if (verbose && (countVariants % SHOW_EVERY == 0)) {
					int millisec = (int) annotateTimer.elapsed();
					int secs = millisec / 1000;
					if (secs > 0) {
						int varsPerSec = (int) (countVariants * 1000.0 / millisec);
						Timer.showStdErr("\t" + countVariants + " variants (" + varsPerSec + " variants per second), " + countVcfEntries + " VCF entries");
					}
				}

				// Calculate effects: By default do not annotate non-variant sites
				// Note, this is the standard analysis.
				// Next section deals with cancer: Somatic vs Germline comparisons
				if (variant.isVariant()) {
					// Perform basic statistics about this variant
					if (createSummaryHtml || createSummaryCsv) variantStats.sample(variant);

					VariantEffects variantEffects = snpEffectPredictor.variantEffect(variant);

					// Show results
					for (VariantEffect variantEffect : variantEffects) {
						if (createSummaryHtml || createSummaryCsv) variantEffectStats.sample(variantEffect); // Perform basic statistics about this result

						// Any errors or warnings?
						if (variantEffect.hasError()) errByType.inc(variantEffect.getError());
						if (variantEffect.hasWarning()) warnByType.inc(variantEffect.getWarning());

						// Does this entry have an impact (other than MODIFIER)?
						impactLowOrHigher |= (variantEffect.getEffectImpact() != EffectImpact.MODIFIER);
						impactModerateOrHigh |= (variantEffect.getEffectImpact() == EffectImpact.MODERATE) || (variantEffect.getEffectImpact() == EffectImpact.HIGH);

						addVariantEffect(vcfEntry, variant, variantEffect);
						countEffects++;
					}

					if (fastaProt != null && impactModerateOrHigh) {
						// Output protein changes to fasta file
						proteinAltSequence(variant, variantEffects);
					}
				}

				//---
				// Do we analyze cancer samples?
				// Here we deal with Somatic vs Germline comparisons
				//---
				if (anyCancerSample && impactLowOrHigher && vcfEntry.isMultiallelic()) {
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

						// Show results (note, we don't add these to the statistics)
						for (VariantEffect variantEffect : variantEffects)
							addVariantEffect(vcfEntry, varNonRef, variantEffect);
					}
				}
			}
		} catch (Throwable t) {
			totalErrs++;
			error(t, "Error while processing VCF entry (line " + vcfFile.getLineNum() + ") :\n\t" + vcfEntry + "\n" + t);
		} finally {
			if (filteredOut) return false;
			print(); // Print entry: Make sure we don't skip the entry when there is an exception
		}

		return true;
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
			if (verbose) Timer.showStdErr("Creating summary file: " + summaryFileCsv);
			ok &= summary(SUMMARY_CSV_TEMPLATE, summaryFileCsv, true);
		}
		if (createSummaryHtml) {
			if (verbose) Timer.showStdErr("Creating summary file: " + summaryFileHtml);
			ok &= summary(SUMMARY_TEMPLATE, summaryFileHtml, false);
		}

		// Creates genes output file
		if (createSummaryHtml || createSummaryCsv) {
			if (verbose) Timer.showStdErr("Creating genes file: " + summaryGenesFile);
			ok &= summary(SUMMARY_GENES_TEMPLATE, summaryGenesFile, true);
		}

		if (totalErrs > 0) System.err.println(totalErrs + " errors.");
		return !ok;
	}

	/**
	 * Calculate the effect of variants and show results
	 */
	protected void annotateInit(String outputFile) {
		if (snpEffectPredictor == null) snpEffectPredictor = config.getSnpEffectPredictor();

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
		Genome genome = snpEffectPredictor.getGenome();
		variantStats = new VariantStats(genome);
		variantEffectStats = new VariantEffectStats(genome);
		variantEffectStats.setUseSequenceOntology(useSequenceOntology);
		vcfStats = new VcfStats();

		if (fastaProt != null) {
			if ((new File(fastaProt)).delete() && verbose) {
				Timer.showStdErr("Deleted protein fasta output file '" + fastaProt + "'");
			}
		}

		//---
		// Create output formatter
		//---
		vcfOutputFormatter = null;
		VcfOutputFormatter vof = new VcfOutputFormatter(vcfEntriesDebug);
		if (gatk) {
			vof.setGatk(true);
		} else {
			vof.setFormatVersion(formatVersion);
			vof.setLossOfFunction(lossOfFunction);
			vof.setConfig(config);
		}
		vcfOutputFormatter = vof;
		vcfOutputFormatter.setVariantEffectResutFilter(variantEffectResutFilter);
		vcfOutputFormatter.setSupressOutput(suppressOutput);
		vcfOutputFormatter.setChrStr(chrStr);
		vcfOutputFormatter.setUseSequenceOntology(useSequenceOntology);
		vcfOutputFormatter.setUseOicr(useOicr);
		vcfOutputFormatter.setUseHgvs(hgvs);
		vcfOutputFormatter.setUseGeneId(useGeneId);
		vcfOutputFormatter.setOutputFile(outputFile);
		vcfOutputFormatter.setCommandLineStr(commandLineStr);
	}

	@Override
	public boolean annotateInit(VcfFileIterator vcfFile) {
		annotateInit((String) null);
		return false;
	}

	/**
	 * Iterate on all inputs (VCF) and calculate effects.
	 * Note: This is used only on input format VCF, which has a different iteration modality
	 */
	VcfFileIterator annotateVcf(String inputFile) {
		// Open VCF file
		VcfFileIterator vcfFile = new VcfFileIterator(inputFile, config.getGenome());
		vcfFile.setDebug(debug);

		// Iterate over VCF entries
		for (VcfEntry vcfEntry : vcfFile)
			annotate(vcfEntry);

		// Empty file? Show at least the header
		if (countVcfEntries == 0) {
			vcfOutputFormatter.print(vcfFile.getVcfHeader().toString());
		}

		// Show errors and warnings
		if (verbose) {
			if (!errByType.isEmpty()) System.err.println("\nERRORS: Some errors were detected\nError type\tNumber of errors\n" + errByType + "\n");
			if (!warnByType.isEmpty()) System.err.println("\nWARNINGS: Some warning were detected\nWarning type\tNumber of warnings\n" + warnByType + "\n");
		}

		return vcfFile;
	}

	/**
	 * Analyze which comparisons to make in cancer genomes
	 */
	Set<Tuple<Integer, Integer>> compareCancerGenotypes(VcfEntry vcfEntry, List<PedigreeEnrty> pedigree) {
		HashSet<Tuple<Integer, Integer>> comparisons = new HashSet<>();

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
							Tuple<Integer, Integer> compare = new Tuple<>(gd[i], go[i]);
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
								Tuple<Integer, Integer> compare = new Tuple<>(gd[d], go[o]);
								comparisons.add(compare);
							}
						}
				}
			}
		}

		return comparisons;
	}

	protected void currentVcfEntry(VcfEntry vcfEntry) {
		// Set VcfEntry
		vcfOutputFormatter.setVcfEntry(vcfEntry);
	}

	/**
	 * Show an error (if not 'quiet' mode)
	 */
	public void error(Throwable e, String message) {
		if (verbose && (e != null)) e.printStackTrace();
		if (!quiet) System.err.println("Error: " + message);
	}

	/**
	 * Show an error message and exit
	 */
	public void fatalError(String message) {
		System.err.println("Fatal error: " + message);
		System.exit(-1);
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
		String base = Gpr.baseName(inputFile, ".gz"); // Remove GZ extension
		base = Gpr.baseName(inputFile, ".vcf"); // Remove extension according to input format

		// Add extension according to output format
		String outputFile = Gpr.dirName(inputFile) + "/" + base + ".eff";
		outputFile += ".vcf";

		// Create summary file names
		if (createSummaryCsv) summaryFileCsv = Gpr.dirName(inputFile) + "/" + base + "_summary.csv";
		if (createSummaryHtml) summaryFileHtml = Gpr.dirName(inputFile) + "/" + base + "_summary.html";
		summaryGenesFile = Gpr.dirName(inputFile) + "/" + base + "_genes.txt";

		return outputFile;
	}

	protected void print() {
		// Finish up this section
		vcfOutputFormatter.print();
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
					+ "-" + (var.getEnd() + 1) //
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
	 * Read a filter custom interval file
	 */
	int readFilterIntFile(String intFile) {
		Markers markers = Markers.loadCustomMarkers(intFile);

		if (filterIntervals == null) filterIntervals = new IntervalForest();
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
			pedigree = new ArrayList<>();

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
			if (verbose) Timer.showStdErr("Pedigree: " + pedigree);
		}

		if (verbose && ((pedigree == null) || pedigree.isEmpty())) Timer.showStdErr("WARNING: No cancer sample pedigree found.");
		return pedigree;
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

		// Check if we can open the input file (no need to check if it is STDIN)
		if (!Gpr.canRead(inputFile)) Gpr.fatalError("Cannot open input file '" + inputFile + "'");

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
		if (createList) vcfEntriesDebug = new ArrayList<>();

		// Predict
		boolean ok = true;
		if (verbose) Timer.showStdErr("Predicting variants");
		if (inputFiles != null) {
			// Multiple input and output files
			for (String inputFile : inputFiles) {
				String outputFile = outputFile(inputFile);
				if (verbose) Timer.showStdErr("Analyzing file" //
						+ "\n\tInput         : '" + inputFile + "'" //
						+ "\n\tOutput        : '" + outputFile + "'" //
						+ (createSummaryHtml ? "\n\tSummary (HTML): '" + summaryFileHtml + "'" : "") //
						+ (createSummaryCsv ? "\n\tSummary (CSV) : '" + summaryFileCsv + "'" : "") //
				);
				ok &= annotate(inputFile, outputFile);
			}
		} else {
			// Single input file, output to STDOUT (typical usage)
			ok = annotate(inputFile, null);
		}
		if (verbose) Timer.showStdErr("done.");

		if (!ok) return null;
		if (vcfEntriesDebug == null) return new ArrayList<>();
		return vcfEntriesDebug;
	}

	public void set(SnpEffCmdEff cmdEff) {
		ValuesCopy vc = new ValuesCopy(cmdEff, this);
		vc.copy();
	}

	@Override
	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setFormatVersion(EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion;
	}

	public void setNoSummary() {
		createSummaryCsv = createSummaryHtml = false;
	}

	public void setSnpEffectPredictor(SnpEffectPredictor snpEffectPredictor) {
		this.snpEffectPredictor = snpEffectPredictor;
	}

	@Override
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
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
			else cfg.setClassForTemplateLoading(AnnotateVcf.class, "/"); // Use current directory in JAR file

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
	 */
	HashMap<String, Object> summaryCreateHash() {
		// Create the root hash (where data objects are)
		HashMap<String, Object> root = new HashMap<>();
		root.put("args", commandLineStrReport);
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

}
