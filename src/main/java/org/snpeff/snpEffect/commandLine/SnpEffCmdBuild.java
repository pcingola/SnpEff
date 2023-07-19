package org.snpeff.snpEffect.commandLine;

import org.snpeff.RegulationConsensusMultipleBed;
import org.snpeff.RegulationFileConsensus;
import org.snpeff.RegulationFileSplitBytType;
import org.snpeff.SnpEff;
import org.snpeff.codons.FindRareAaIntervals;
import org.snpeff.fileIterator.MotifFileIterator;
import org.snpeff.fileIterator.RegulationFileIterator;
import org.snpeff.fileIterator.RegulationGffFileIterator;
import org.snpeff.interval.ExonSpliceCharacterizer;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Motif;
import org.snpeff.interval.RareAminoAcid;
import org.snpeff.motif.Jaspar;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.factory.*;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * Command line program: Build database
 *
 * @author pcingola
 */
public class SnpEffCmdBuild extends SnpEff {

    GeneDatabaseFormat geneDatabaseFormat; // Database format (only used if 'buildDb' is active)
    boolean checkNumOk = true; // Require some transcripts to be checked (count checked transcripts should be more than zero)
    boolean checkCds = true; // Try to check CDS sequences
    boolean checkProtein = true; // Try to check protein sequences
    boolean storeAlignments; // Store alignments (only used for some test cases)
    boolean storeSequences = true; // Store full sequences
    boolean regSortedByType = false;
    String cellType = null;
    double maxErrorRate = -1;
    SnpEffCmdProtein snpEffCmdProtein;
    SnpEffCmdCds snpEffCmdCds;

    public SnpEffCmdBuild() {
        super();
        geneDatabaseFormat = null; // GeneDatabaseFormat.GTF22; // Database format (only used if 'buildDb' is active)
    }

    /**
     * Check if database is OK, by comparing to CDS or Protein sequences
     *
     * @return: true if at least one check (cds or protein) is successful
     */
    boolean checkDb(SnpEffPredictorFactory snpEffectPredictorFactory) {
        boolean ok = true;
        if(checkCds) ok &= checkDbCds(snpEffectPredictorFactory);
        if(checkProtein) ok &= checkDbProtein(snpEffectPredictorFactory);
        return ok;
    }

    /**
     * Check if database is OK, by comparing to CDS sequences
     *
     * @return: true the comparisson error rate is lower than maxErrorRate
     */
    boolean checkDbCds(SnpEffPredictorFactory snpEffectPredictorFactory) {
        var okCds = false;
        String cdsFile = config.getFileNameCds();
        if (geneDatabaseFormat == GeneDatabaseFormat.GENBANK) {
            // GenBank format
            if (verbose) Log.info("CDS check: GenBank file format, skipping\n");
            okCds = true;
        } else if (Gpr.canRead(cdsFile)) {
            // Use FASTA format
            if (verbose) Log.info("CDS check (FASTA file): '" + cdsFile + "'\n");
            snpEffCmdCds = new SnpEffCmdCds(config);
            snpEffCmdCds.setVerbose(verbose);
            snpEffCmdCds.setDebug(debug);
            snpEffCmdCds.setStoreAlignments(storeAlignments);
            snpEffCmdCds.setCheckNumOk(checkNumOk);
            if( maxErrorRate >= 0) snpEffCmdCds.setMaxErrorRate(maxErrorRate);
            okCds = snpEffCmdCds.run();
            if (!okCds && verbose) Log.info("\tCDS sequences comparison failed!");
        } else Log.error("CDS check file '" + cdsFile + "' not found.");
        return okCds;
    }


    /**
     * Check if database is OK, by comparing to Protein sequences
     *
     * @return: true if at least one check (cds or protein) is successful
     */
    boolean checkDbProtein(SnpEffPredictorFactory snpEffectPredictorFactory) {
        String protFile = null;
        Map<String, String> proteinByTrId = snpEffectPredictorFactory.getProteinByTrId(); // Some factories provide the transcritpId -> protein_sequence mapping
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

        var okProtein = false;
        if (Gpr.canRead(protFile)) {
            if (verbose) Log.info("Protein check file: '" + protFile + "'\n");
            snpEffCmdProtein = new SnpEffCmdProtein(config, protFile);
            snpEffCmdProtein.setVerbose(verbose);
            snpEffCmdProtein.setDebug(debug);
            snpEffCmdProtein.setStoreAlignments(storeAlignments);
            snpEffCmdProtein.setCheckNumOk(checkNumOk);
            snpEffCmdProtein.setProteinByTrId(proteinByTrId);
            if( maxErrorRate >= 0) snpEffCmdProtein.setMaxErrorRate(maxErrorRate);
            okProtein = snpEffCmdProtein.run();
            if (!okProtein) {
                if (verbose) Log.info("\tProtein sequences comparison failed!");
                return false;
            }
        } else Log.error("Protein check file '" + protFile + "' not found.");

        return okProtein;
    }

    /**
     * Create SnpEffectPredictor
     */
    SnpEffPredictorFactory createSnpEffPredictorFactory() {
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
        else if (geneDatabaseFormat == GeneDatabaseFormat.BIOMART)
            factory = new SnpEffPredictorFactoryGenesFile(config);
        else throw new RuntimeException("Unimplemented format " + geneDatabaseFormat);

        // Create SnpEffPredictor
        factory.setVerbose(verbose);
        factory.setDebug(debug);
        factory.setStoreSequences(storeSequences);
        return factory;
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

        if (geneDatabaseFormat == null)
            Log.fatalError("Cannot guess input database format for genome '" + genomeVer + "'. No genes file found '" + genesBase + ".*'");

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

                    case "-nocheckcds":
                        checkCds = false;
                        break;

                    case "-nocheckprotein":
                        checkProtein = false;
                        break;

                    case "-nostoreseqs":
                        storeSequences = false;
                        break;

                    case "-onlyreg":
                        onlyRegulation = true;
                        break;

                    case "-celltype":
                        if ((i + 1) < args.length) cellType = args[++i];
                        else usage("Missing 'regType' argument");
                        break;

                    case "-regsortedbytype":
                        regSortedByType = true;
                        break;

                    case "-maxerrorrate":
                        if ((i + 1) < args.length) maxErrorRate = Gpr.parseDoubleSafe(args[++i]);
                        else usage("Missing 'maxErrorPercentage' argument");
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
        if (verbose) Log.info("[Optional] Rare amino acid annotations");
        String proteinsFile = config.getFileNameProteins();

        try {
            // Find a list of 'rare' amino acids
            FindRareAaIntervals findRare = new FindRareAaIntervals(snpEffectPredictor.getGenome());
            findRare.setVerbose(verbose);
            Collection<RareAminoAcid> raas = findRare.findRareAa(proteinsFile);

            // Add them all
            for (RareAminoAcid raa : raas) {
                if (verbose) Log.info("\tAdding: " + raa);
                snpEffectPredictor.add(raa);
            }

            if (verbose) Log.info("Done.");
        } catch (Throwable t) {
            // If file does not exists, no problem
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_FILE_NOT_FOUND, "Rare Amino Acid analysis: Cannot read protein sequence file '" + proteinsFile + "', nothing done.");
            if (debug) t.printStackTrace();
        }
    }

    /**
     * Read regulatory elements from multiple BED files
     */
    void readRegulationBed() {
        if (verbose) Log.info("[Optional] Reading regulation elements: BED ");

        String inDir = config.getDirRegulationBed();
        String outDir = config.getDirDataGenomeVersion();

        // Is the directory present?
        File dir = new File(inDir);
        if (!dir.exists() || !dir.isDirectory()) {
            if (verbose) Log.info("Cannot find optional regulation dir '" + inDir + "', nothing done.");
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
        if (verbose) Log.info("[Optional] Reading regulation elements: GFF");
        String regulationFileName = config.getBaseFileNameRegulation() + ".gff";

        // If file does not exists, no problem
        if (!Gpr.canRead(regulationFileName)) {
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_FILE_NOT_FOUND, "Cannot read optional regulation file '" + regulationFileName + "', nothing done.");
            return;
        }

        // Split large GFF files into smaller ones
        RegulationFileIterator regulationFileIterator = new RegulationGffFileIterator(regulationFileName);
        RegulationFileSplitBytType regSplit = new RegulationFileSplitBytType();
        regSplit.setVerbose(verbose);
        regSplit.splitFile(regulationFileIterator, config.getDirDataGenomeVersion());

        // Create database for each individual GFF file
        for (String regFileName : regSplit.getRegFileNames()) {
            // Open the regulation file and create a consensus
            regulationFileIterator = new RegulationGffFileIterator(regFileName);
            RegulationFileConsensus regulationGffConsensus = new RegulationFileConsensus();
            regulationGffConsensus.setVerbose(verbose);
            regulationGffConsensus.setOutputDir(config.getDirDataGenomeVersion());
            regulationGffConsensus.readFile(regulationFileIterator); // Read info from file
            regulationGffConsensus.save(); // Save database
        }
        if (verbose) Log.info("Done.");
    }

    /**
     * Read regulation motif files
     */
    void readRegulationMotif() {
        if (verbose) Log.info("[Optional] Reading motifs: GFF");
        String motifFileName = config.getBaseFileNameMotif() + ".gff";
        String motifBinFileName = config.getBaseFileNameMotif() + ".bin";
        String pwmsFileName = config.getDirDataGenomeVersion() + "/pwms.bin";

        if (!Gpr.exists(pwmsFileName)) {
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_FILE_NOT_FOUND, "Cannot open PWMs file " + pwmsFileName + ". Nothing done");
            return;
        }

        try {
            // Load all PWMs
            if (verbose) Log.info("\tLoading PWMs from : " + pwmsFileName);
            Jaspar jaspar = new Jaspar();
            jaspar.load(pwmsFileName);

            // Open the regulation file and create a consensus
            if (verbose) Log.info("\tLoading motifs from : " + motifFileName);
            MotifFileIterator motifFileIterator = new MotifFileIterator(motifFileName, config.getGenome(), jaspar);
            Markers motifs = new Markers();
            for (Motif motif : motifFileIterator)
                motifs.add(motif);
            if (verbose) Log.info("\tLoadded motifs: " + motifs.size());

            if (verbose) Log.info("\tSaving motifs to: " + motifBinFileName);
            motifs.save(motifBinFileName);
        } catch (Throwable t) {
            // If file does not exists, no problem
            if (verbose)
                Log.warning(ErrorWarningType.WARNING_FILE_NOT_FOUND, "Cannot read optional motif file '" + motifFileName + "', nothing done.");
            if (debug) t.printStackTrace();
        }
    }

    /**
     * Build database
     */
    @Override
    public boolean run() {
        if (verbose) Log.info("Building database for '" + genomeVer + "'");
        loadConfig(); // Read configuration file

        // Create SnpEffectPredictor
        if (!onlyRegulation) {
            SnpEffPredictorFactory snpEffectPredictorFactory = createSnpEffPredictorFactory();
            SnpEffectPredictor snpEffectPredictor = snpEffectPredictorFactory.create();
            config.setSnpEffectPredictor(snpEffectPredictor);

            // Characterize exons (if possible)
            ExonSpliceCharacterizer exonSpliceCharacterizer = new ExonSpliceCharacterizer(snpEffectPredictor.getGenome());
            exonSpliceCharacterizer.setVerbose(verbose);
            exonSpliceCharacterizer.characterize();

            // Add read rare codons annotations, if possible
            rareAa(snpEffectPredictor);

            // Check database
            var okCheck = checkDb(snpEffectPredictorFactory);
            if (!okCheck) {
                Log.error("Database check failed.");
                return false;
            }

            // Save database
            if (verbose) Log.info("Saving database");
            snpEffectPredictor.save(config);
        }

        // Read regulation elements
        if (cellType == null) readRegulationGff(); // CellType specific is meant for BED files.
        readRegulationBed();
        readRegulationMotif();

        if (verbose) Log.info("Done");

        return true;
    }

    public void setCheckNumOk(boolean checkNumOk) {
        this.checkNumOk = checkNumOk;
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
        System.err.println("\t-maxErrorRate <num>          : Maximum allowed error rate (number between 0.0 and 1.0). Default: 0.05");
        System.err.println("\t-noCheckCds                  : Skip CDS sequences check.");
        System.err.println("\t-noCheckProtein              : Skip Protein sequences check.");
        System.err.println("\t-noStoreSeqs                 : Do not store sequence in binary files. Default: " + !storeSequences);
        System.err.println("\t-onlyReg                     : Only build regulation tracks.");
        System.err.println("\t-regSortedByType             : The 'regulation.gff' file is sorted by 'regulation type' instead of sorted by chromosome:pos. Default: " + regSortedByType);
        System.err.println("\t-storeSeqs                   : Store sequence in binary files. Default: " + storeSequences);

        usageGeneric();

        System.exit(-1);
    }
}
