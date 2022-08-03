package org.snpeff.snpEffect.commandLine;

import org.snpeff.SnpEff;
import org.snpeff.pdb.DistanceResult;
import org.snpeff.pdb.ProteinInteractions;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.util.List;

/**
 * PDB distance analysis
 * <p>
 * References: http://biojava.org/wiki/BioJava:CookBook:PDB:read
 *
 * @author pcingola
 */
public class SnpEffCmdPdb extends SnpEff {

    ProteinInteractions proteinInteractions;

    public SnpEffCmdPdb() {
    }

    /**
     * Get distance results. This method is used to implement test cases
     */
    public List<DistanceResult> getDistanceResults() {
        return proteinInteractions.getDistanceResults();
    }

    /**
     * Initialize SnpEff. Load all data
     */
    public void initialize() {
        String[] argsSnpEff = {"eff", "-c", configFile, genomeVer};
        args = argsSnpEff;
        setGenomeVer(genomeVer);
        parseArgs(argsSnpEff);
        loadConfig();

        // Load SnpEff database
        if (genomeVer != null) {
            Log.info("Loading SnpEff's database: " + genomeVer);
            loadDb();
            Log.info("Done.");
        }
    }

    /**
     * Parse command line arguments
     */
    @Override
    public void parseArgs(String[] args) {
        proteinInteractions = new ProteinInteractions();
        proteinInteractions.setVerbose(verbose);
        proteinInteractions.setDebug(debug);

        if (args == null) return;
        this.args = args;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            // Is it a command line option?
            // Note: Generic options (such as config, verbose, debug, quiet, etc.) are parsed by SnpEff class
            if (isOpt(arg)) {
                arg = arg.toLowerCase();

                switch (arg.toLowerCase()) {
                    case "-aasep":
                        if ((i + 1) < args.length) proteinInteractions.setAaMinSeparation(Gpr.parseIntSafe(args[++i]));
                        else usage("Missing parameter in '-aaSep'");
                        break;

                    case "-idmap":
                        if ((i + 1) < args.length) proteinInteractions.setIdMapFile(args[++i]);
                        else usage("Missing parameter in '-idMap'");
                        break;

                    case "-maxdist":
                        if ((i + 1) < args.length) proteinInteractions.setDistanceThreshold(Gpr.parseDoubleSafe(args[++i]));
                        else usage("Missing parameter in '-maxDist'");
                        break;

                    case "-maxerr":
                        if ((i + 1) < args.length) proteinInteractions.setMaxMismatchRate(Gpr.parseDoubleSafe(args[++i]));
                        else usage("Missing parameter: '-maxErr'");
                        break;

                    case "-org":
                        if ((i + 1) < args.length) proteinInteractions.setPdbOrganismCommon(args[++i].toUpperCase());
                        else usage("Missing parameter in '-org'");
                        break;

                    case "-orgscientific":
                        if ((i + 1) < args.length) proteinInteractions.setPdbOrganismScientific(args[++i].toUpperCase());
                        else usage("Missing parameter in '-orgScientific'");
                        break;

                    case "-pdbdir":
                        if ((i + 1) < args.length) proteinInteractions.setPdbDir(args[++i]);
                        else usage("Missing parameter in '-pdbDir'");
                        break;

                    case "-res":
                        if ((i + 1) < args.length) proteinInteractions.setPdbResolution(Gpr.parseDoubleSafe(args[++i]));
                        else usage("Missing parameter: '-res'");
                        break;

                    default:
                        usage("Unknown option '" + arg + "'");
                }
            } else if (genomeVer == null || genomeVer.isEmpty()) {
                genomeVer = arg;
                proteinInteractions.setGenomeVer(genomeVer);
            }
        }


        // Sanity check. Do we have all required parameters?
        var check = proteinInteractions.checkParams();
        if (check != null) usage(check);
    }


    @Override
    public boolean run() {
        return run(false);
    }

    public boolean run(boolean storeResults) {
        loadConfig(); // Read config file
        proteinInteractions.initialize(config);
        loadDb();
        proteinInteractions.run(storeResults);
        return true;
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
        System.err.println("Usage: snpEff pdb [options] genome_version");
        System.err.println("\n");
        System.err.println("\nOptions:");
        System.err.println("\t-aaSep <number>                 : Minimum number of AA of separation within the sequence. Default: " + proteinInteractions.getAaMinSeparation());
        System.err.println("\t-idMap <file>                   : ID map file (i.e. file containing mapping from PDB ID to transcript ID). Default: " + proteinInteractions.getIdMapFile());
        System.err.println("\t-maxDist <number>               : Maximum distance in Angstrom for any atom in a pair of amino acids to be considered 'in contact'. Default: " + proteinInteractions.getDistanceThreshold());
        System.err.println("\t-maxErr <number>                : Maximum amino acid sequence difference between PDB file and genome. Default: " + proteinInteractions.getMaxMismatchRate());
        System.err.println("\t-org <name>                     : Organism 'common name'. Default: " + proteinInteractions.getPdbOrganismCommon());
        System.err.println("\t-orgScientific <name>           : Organism 'scientific name'. Default: " + proteinInteractions.getPdbOrganismScientific());
        System.err.println("\t-pdbDir <path>                  : Path to PDB files (files in all sub-dirs are scanned).");
        System.err.println("\t-res <number>                   : Maximum PDB file resolution. Default: " + proteinInteractions.getPdbResolution());

        usageGenericAndDb();

        System.exit(-1);
    }
}
