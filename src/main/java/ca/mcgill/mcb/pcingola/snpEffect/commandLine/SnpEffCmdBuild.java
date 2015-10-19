package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.io.File;
import java.util.Collection;

import ca.mcgill.mcb.pcingola.RegulationConsensusMultipleBed;
import ca.mcgill.mcb.pcingola.RegulationFileConsensus;
import ca.mcgill.mcb.pcingola.codons.FindRareAaIntervals;
import ca.mcgill.mcb.pcingola.fileIterator.MotifFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.RegulationFileIterator;
import ca.mcgill.mcb.pcingola.fileIterator.RegulationGffFileIterator;
import ca.mcgill.mcb.pcingola.interval.ExonSpliceCharacterizer;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.interval.RareAminoAcid;
import ca.mcgill.mcb.pcingola.motif.Jaspar;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactory;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryEmbl;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGenBank;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGenesFile;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGff2;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGff3;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryGtf22;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryKnownGene;
import ca.mcgill.mcb.pcingola.snpEffect.factory.SnpEffPredictorFactoryRefSeq;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Command line program: Build database
 *
 * @author pcingola
 */
public class SnpEffCmdBuild extends SnpEff {

	GeneDatabaseFormat geneDatabaseFormat; // Database format (only used if 'buildDb' is active)
	boolean storeAlignments; // Store alignments (used for some test cases)
	boolean storeSequences = true; // Store full sequences
	String cellType = null;
	SnpEffCmdProtein snpEffCmdProtein;
	SnpEffCmdCds snpEffCmdCds;

	public SnpEffCmdBuild() {
		super();
		geneDatabaseFormat = null; // GeneDatabaseFormat.GTF22; // Database format (only used if 'buildDb' is active)
	}

	/**
	 * Check if database is OK
	 */
	void checkDb() {
		//---
		// Check using CDS file
		//---
		String cdsFile = config.getFileNameCds();
		if (Gpr.canRead(cdsFile)) {
			// Use FASTA format
			if (verbose) Timer.showStdErr("CDS check (FASTA file): '" + cdsFile + "'\n");
			snpEffCmdCds = new SnpEffCmdCds(config);
			snpEffCmdCds.setVerbose(verbose);
			snpEffCmdCds.setDebug(debug);
			snpEffCmdCds.setStoreAlignments(storeAlignments);
			snpEffCmdCds.run();
		} else if (debug) Timer.showStdErr("\tOptional file '" + cdsFile + "' not found, nothing done.");

		//---
		// Check using proteins file
		//---
		String protFile = null;
		if (geneDatabaseFormat == GeneDatabaseFormat.GENBANK) {
			// GenBank format
			protFile = config.getBaseFileNameGenes() + SnpEffPredictorFactoryGenBank.EXTENSION_GENBANK;
		} else if (geneDatabaseFormat == GeneDatabaseFormat.EMBL) {
			// EMBL format
			protFile = config.getBaseFileNameGenes() + SnpEffPredictorFactoryEmbl.EXTENSION_EMBL;
		} else {
			// Protein fasta file
			protFile = config.getFileNameProteins();
		}

		if (Gpr.canRead(protFile)) {
			if (verbose) Timer.showStdErr("Protein check file: '" + protFile + "'\n");
			snpEffCmdProtein = new SnpEffCmdProtein(config, protFile);
			snpEffCmdProtein.setVerbose(verbose);
			snpEffCmdProtein.setDebug(debug);
			snpEffCmdProtein.setStoreAlignments(storeAlignments);
			snpEffCmdProtein.run();
		} else if (debug) Timer.showStdErr("\tOptional file '" + protFile + "' not found, nothing done.");

	}

	/**
	 * Create SnpEffectPredictor
	 */
	SnpEffectPredictor createSnpEffPredictor() {
		if (geneDatabaseFormat == null) geneDatabaseFormat = guessGenesFormat();

		// Create factory
		SnpEffPredictorFactory factory = null;
		if (geneDatabaseFormat == GeneDatabaseFormat.GTF22) factory = new SnpEffPredictorFactoryGtf22(config);
		else if (geneDatabaseFormat == GeneDatabaseFormat.GFF3) factory = new SnpEffPredictorFactoryGff3(config);
		else if (geneDatabaseFormat == GeneDatabaseFormat.GFF2) factory = new SnpEffPredictorFactoryGff2(config);
		else if (geneDatabaseFormat == GeneDatabaseFormat.REFSEQ) factory = new SnpEffPredictorFactoryRefSeq(config);
		else if (geneDatabaseFormat == GeneDatabaseFormat.KNOWN_GENES) factory = new SnpEffPredictorFactoryKnownGene(config);
		else if (geneDatabaseFormat == GeneDatabaseFormat.GENBANK) factory = new SnpEffPredictorFactoryGenBank(config);
		else if (geneDatabaseFormat == GeneDatabaseFormat.EMBL) factory = new SnpEffPredictorFactoryEmbl(config);
		else if (geneDatabaseFormat == GeneDatabaseFormat.BIOMART) factory = new SnpEffPredictorFactoryGenesFile(config);
		else throw new RuntimeException("Unimplemented format " + geneDatabaseFormat);

		// Create SnpEffPredictor
		factory.setVerbose(verbose);
		factory.setDebug(debug);
		factory.setStoreSequences(storeSequences);
		return factory.create();
	}

	/**
	 * Does either 'path' or 'path'+'.gz' exist?
	 */
	protected boolean fileExists(String path) {
		return Gpr.exists(path) || Gpr.exists(path + ".gz");
	}

	public SnpEffCmdCds getSnpEffCmdCds() {
		return snpEffCmdCds;
	}

	public SnpEffCmdProtein getSnpEffCmdProtein() {
		return snpEffCmdProtein;
	}

	/**
	 * Try to guess database format by checking which file type is present
	 */
	protected GeneDatabaseFormat guessGenesFormat() {
		String genesBase = config.getBaseFileNameGenes();

		if (fileExists(genesBase + ".gtf")) return GeneDatabaseFormat.GTF22;
		if (fileExists(genesBase + ".gff")) return GeneDatabaseFormat.GFF3;
		if (fileExists(genesBase + ".gff2")) return GeneDatabaseFormat.GFF2;
		if (fileExists(genesBase + ".gbk")) return GeneDatabaseFormat.GENBANK;
		if (fileExists(genesBase + ".embl")) return GeneDatabaseFormat.EMBL;
		if (fileExists(genesBase + ".refseq")) return GeneDatabaseFormat.REFSEQ;
		if (fileExists(genesBase + ".kg")) return GeneDatabaseFormat.KNOWN_GENES;
		if (fileExists(genesBase + ".biomart")) return GeneDatabaseFormat.BIOMART;

		if (geneDatabaseFormat == null) fatalError("Cannot guess input database format for genome '" + genomeVer + "'. No genes file found '" + genesBase + ".*'");

		return null;
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// It is an argument?
			if (isOpt(arg)) {

				switch (arg.toLowerCase()) {
				case "-gff3":
					geneDatabaseFormat = GeneDatabaseFormat.GFF3;
					break;

				case "-gff2":
					geneDatabaseFormat = GeneDatabaseFormat.GFF2;
					break;

				case "-gtf22":
					geneDatabaseFormat = GeneDatabaseFormat.GTF22;
					break;

				case "-refseq":
					geneDatabaseFormat = GeneDatabaseFormat.REFSEQ;
					break;

				case "-genbank":
					geneDatabaseFormat = GeneDatabaseFormat.GENBANK;
					break;

				case "-knowngenes":
					geneDatabaseFormat = GeneDatabaseFormat.KNOWN_GENES;
					break;

				case "-embl":
					geneDatabaseFormat = GeneDatabaseFormat.EMBL;
					break;

				case "-txt":
					geneDatabaseFormat = GeneDatabaseFormat.BIOMART;
					break;

				case "-storeseqs":
					storeSequences = true;
					break;

				case "-nostoreseqs":
					storeSequences = false;
					break;

				case "-onlyreg":
					onlyRegulation = true;
					break;

				case "-celltype":
					if ((i + 1) < args.length) cellType = args[++i];
					else usage("Missing 'cellType' argument");
					break;

				default:
					usage("Unknown option '" + arg + "'");
				}

			} else if (genomeVer.length() <= 0) genomeVer = arg;
			else usage("Unknown parameter '" + arg + "'");
		}

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");
	}

	/**
	 * Calculate and add annotations for rare amino acids
	 */
	void rareAa(SnpEffectPredictor snpEffectPredictor) {
		if (verbose) Timer.showStdErr("[Optional] Rare amino acid annotations");
		String proteinsFile = config.getFileNameProteins();

		try {
			// Find a list of 'rare' amino acids
			FindRareAaIntervals findRare = new FindRareAaIntervals(snpEffectPredictor.getGenome());
			findRare.setVerbose(verbose);
			Collection<RareAminoAcid> raas = findRare.findRareAa(proteinsFile);

			// Add them all
			for (RareAminoAcid raa : raas) {
				if (verbose) System.err.println("\tAdding: " + raa);
				snpEffectPredictor.add(raa);
			}

			if (verbose) Timer.showStdErr("Done.");
		} catch (Throwable t) {
			// If file does not exists, no problem
			if (verbose) Timer.showStdErr("Warning: Cannot read optional protein sequence file '" + proteinsFile + "', nothing done.");
			if (debug) t.printStackTrace();
		}
	}

	/**
	 * Read regulatory elements from multiple BED files
	 */
	void readRegulationBed() {
		if (verbose) Timer.showStdErr("[Optional] Reading regulation elements: BED ");

		String inDir = config.getDirRegulationBed();
		String outDir = config.getDirDataVersion();

		// Is the directory present?
		File dir = new File(inDir);
		if (!dir.exists() || !dir.isDirectory()) {
			if (verbose) Timer.showStdErr("Cannot find optional regulation dir '" + inDir + "', nothing done.");
			return;
		}

		RegulationConsensusMultipleBed regBeds = new RegulationConsensusMultipleBed(inDir, outDir);
		regBeds.setVerbose(verbose);
		regBeds.setCellType(cellType);
		regBeds.run();
	}

	/**
	 * Read regulation elements (only GFF3 file supported)
	 */
	void readRegulationGff() {
		if (verbose) Timer.showStdErr("[Optional] Reading regulation elements: GFF");
		String regulationFileName = config.getBaseFileNameRegulation() + ".gff";

		// If file does not exists, no problem
		if (!Gpr.canRead(regulationFileName)) {
			if (verbose) Timer.showStdErr("Warning: Cannot read optional regulation file '" + regulationFileName + "', nothing done.");
			return;
		}

		// Open the regulation file and create a consensus
		RegulationFileIterator regulationFileIterator = new RegulationGffFileIterator(regulationFileName);
		RegulationFileConsensus regulationGffConsensus = new RegulationFileConsensus(verbose);
		regulationGffConsensus.readFile(regulationFileIterator); // Read info from file
		regulationGffConsensus.save(config.getDirDataVersion()); // Save database
		if (verbose) Timer.showStdErr("Done.");
	}

	/**
	 * Read regulation motif files
	 */
	void readRegulationMotif() {
		if (verbose) Timer.showStdErr("[Optional] Reading motifs: GFF");
		String motifFileName = config.getBaseFileNameMotif() + ".gff";
		String motifBinFileName = config.getBaseFileNameMotif() + ".bin";
		String pwmsFileName = config.getDirDataVersion() + "/pwms.bin";

		if (!Gpr.exists(pwmsFileName)) {
			if (verbose) Timer.showStdErr("Warning: Cannot open PWMs file " + pwmsFileName + ". Nothing done");
			return;
		}

		try {
			// Load all PWMs
			if (verbose) Timer.showStdErr("\tLoading PWMs from : " + pwmsFileName);
			Jaspar jaspar = new Jaspar();
			jaspar.load(pwmsFileName);

			// Open the regulation file and create a consensus
			if (verbose) Timer.showStdErr("\tLoading motifs from : " + motifFileName);
			MotifFileIterator motifFileIterator = new MotifFileIterator(motifFileName, config.getGenome(), jaspar);
			Markers motifs = new Markers();
			for (Motif motif : motifFileIterator)
				motifs.add(motif);
			if (verbose) Timer.showStdErr("\tLoadded motifs: " + motifs.size());

			if (verbose) Timer.showStdErr("\tSaving motifs to: " + motifBinFileName);
			motifs.save(motifBinFileName);
		} catch (Throwable t) {
			// If file does not exists, no problem
			if (verbose) Timer.showStdErr("Warning: Cannot read optional motif file '" + motifFileName + "', nothing done.");
			if (debug) t.printStackTrace();
		}
	}

	/**
	 * Build database
	 */
	@Override
	public boolean run() {
		if (verbose) Timer.showStdErr("Building database for '" + genomeVer + "'");
		loadConfig(); // Read configuration file

		// Create SnpEffectPredictor
		if (!onlyRegulation) {
			SnpEffectPredictor snpEffectPredictor = createSnpEffPredictor();
			config.setSnpEffectPredictor(snpEffectPredictor);

			// Characterize exons (if possible)
			ExonSpliceCharacterizer exonSpliceCharacterizer = new ExonSpliceCharacterizer(snpEffectPredictor.getGenome());
			exonSpliceCharacterizer.setVerbose(verbose);
			exonSpliceCharacterizer.characterize();

			// Add read rare codons annotations, if possible
			rareAa(snpEffectPredictor);

			// Check database
			checkDb();

			// Save database
			if (verbose) Timer.showStdErr("Saving database");
			snpEffectPredictor.save(config);
		}

		// Read regulation elements
		if (cellType == null) readRegulationGff(); // CellType specific is meant for BED files.
		readRegulationBed();
		readRegulationMotif();

		if (verbose) Timer.showStdErr("Done");

		return true;
	}

	public void setStoreAlignments(boolean storeAlignments) {
		this.storeAlignments = storeAlignments;
	}

	/**
	 * Show 'usage;' message and exit with an error code '-1'
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff build [options] genome_version");
		System.err.println("\nBuild DB options:");
		System.err.println("\nDatabase format option (default: Auto detect):");
		System.err.println("\t-embl                        : Use Embl format.");
		System.err.println("\t-genbank                     : Use GenBank format.");
		System.err.println("\t-gff2                        : Use GFF2 format (obsolete).");
		System.err.println("\t-gff3                        : Use GFF3 format.");
		System.err.println("\t-gtf22                       : Use GTF 2.2 format.");
		System.err.println("\t-knowngenes                  : Use KnownGenes table from UCSC.");
		System.err.println("\t-refseq                      : Use RefSeq table from UCSC.");
		System.err.println("\nDatabase build options:");
		System.err.println("\t-cellType <type>             : Only build regulation tracks for cellType <type>.");
		System.err.println("\t-noStoreSeqs                 : Do not store sequence in binary files. Default: " + !storeSequences);
		System.err.println("\t-onlyReg                     : Only build regulation tracks.");
		System.err.println("\t-storeSeqs                   : Store sequence in binary files. Default: " + storeSequences);
		System.err.println("\t-txt                         : Use TXT format (obsolete).");

		usageGeneric();

		System.exit(-1);
	}
}
