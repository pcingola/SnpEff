package org.snpeff.pdb;

import htsjdk.samtools.util.RuntimeEOFException;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Log;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 * This class reads a set of protein structure (PDB) files, analyzes
 * the structures, and creates a interactions file for
 * a SnpEff genome database (interactions.bin)
 *
 * @author pcingola
 */
public class ProteinInteractions {

    public static final String PROTEIN_INTERACTION_FILE = "interactions.bin";
    public static final String DEFAULT_PDB_DIR = "db/pdb";
    public static final double DEFAULT_DISTANCE_THRESHOLD = 3.0; // Maximum distance to be considered 'in contact'
    public static final double DEFAULT_MAX_MISMATCH_RATE = 0.05; // Maximum number of mismatches accepted in a protein chain
    public static final int DEFAULT_PDB_MIN_AA_SEPARATION = 20; // Number of AA of distance within a sequence to consider them for distance analysis
    public static final String DEFAULT_PDB_ORGANISM_COMMON = "HUMAN"; // PDB organism
    public static final String DEFAULT_PDB_ORGANISM_SCIENTIFIC = "HOMO SAPIENS";
    public static final double DEFAULT_PDB_RESOLUTION = 3.0; // PDB file resolution (in Angstrom)

    int aaMinSeparation = DEFAULT_PDB_MIN_AA_SEPARATION;
    Config config;
    boolean debug, verbose;
    List<DistanceResult> distanceResults;
    double distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;
    double distanceThresholdNon = Double.POSITIVE_INFINITY; // Distance threshold for 'not in contact'
    String idMapFile;
    int countFilesPass, countMapError, countMapOk;
    String genomeVer;
    IdMapper idMapper;
    double maxMismatchRate = DEFAULT_MAX_MISMATCH_RATE;
    BufferedWriter outpufDbFile;
    String outputDbFileName;
    String pdbDir = DEFAULT_PDB_DIR;
    Collection<String> pdbFileNames; // PDB files to porcess
    String pdbOrganismCommon = DEFAULT_PDB_ORGANISM_COMMON; // PDB organism "common name"
    String pdbOrganismScientific = DEFAULT_PDB_ORGANISM_SCIENTIFIC; // PDB organism "scientific name"
    PDBFileReader pdbReader;
    double pdbResolution = DEFAULT_PDB_RESOLUTION; // PDB file resolution (in Angstrom)
    Set<String> saved;
    Map<String, Transcript> trancriptById; // Transcripts by id (trId without version number)

    public ProteinInteractions() {
    }

    public String checkParams() {
        if (genomeVer == null || genomeVer.isEmpty()) return "Missing genomer version parameter";
        if(idMapFile==null || idMapFile.isEmpty()) return "Missing ID map file";
        if (distanceThreshold <= 0) return "Max distance in '-maxdist' command line option must be a positive number";
        if (maxMismatchRate <= 0)
            return "Max mismatch rate in '-maxErr' command line option must be a positive number";
        if (pdbResolution <= 0) return "Resoluton in '-res' command line option must be a positive number";
        if (aaMinSeparation <= 0)
            return "Minimum separation in '-aaSep' command line option must be a positive, integer number";
        return null;
    }

    /**
     * Map transcript ID to transcript
     */
    void createTranscriptMap() {
        // Create transcript map
        trancriptById = new HashMap<>();
        for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes()) {
            for (Transcript tr : g) {
                String trId = IdMapper.transcriptIdNoVersion(tr.getId());
                trancriptById.put(trId, tr);
            }
        }
    }

    void closeOuptutDb() {
        try {
            if (outpufDbFile != null) outpufDbFile.close();
            outpufDbFile = null;
            saved = null;
        } catch (IOException e) {
            throw new RuntimeException("Error closing output file", e);
        }
    }

    public void deleteOutputDb() {
        File of = new File(outputDbFileName);
        of.delete();
    }

    Collection<String> findPdbFiles() {
        return findPdbFiles(new File(pdbDir));
    }

    /**
     * Find all files (in any subdir) matching pdb entry extension
     */
    Collection<String> findPdbFiles(File dir) {
        if (debug) Log.debug("Finding PDB files in directory: " + dir);
        List<String> list = new LinkedList<>();

        if (!dir.isDirectory()) throw new RuntimeException("No such directory '" + dir + "'");

        for (File f : dir.listFiles()) {

            if (f.isDirectory()) {
                list.addAll(findPdbFiles(f));
            } else if (isPdbFile(f)) {
                list.add(f.getAbsolutePath());
                if (debug) Log.debug("Found PDB file: " + f.getAbsolutePath());
            }
        }

        return list;
    }

    /**
     * Return true if the transcript passes the criteria (i.e. the ID is present in 'trancriptById' map)
     */
    public boolean filterTranscript(String trId) {
        Transcript tr = trancriptById.get(trId);
        if (tr == null) {
            if (debug) Log.debug("Transcript '" + trId + "' not found in " + genomeVer + ".");
            return false;
        }

        return true;
    }

    public Set<String> findTranscriptIds(String pdbId) {
        // Get transcript IDs
        List<IdMapperEntry> idEntries = idMapper.getByPdbId(pdbId);
        Set<String> trIds = IdMapper.transcriptIds(idEntries);

        if (debug) {
            StringBuilder sb = new StringBuilder();
            sb.append("PDB ID: " + pdbId);
            sb.append("\tEntries:\n");
            if (idEntries != null) {
                for (IdMapperEntry ime : idEntries)
                    sb.append("\t\t" + ime + "\n");
                sb.append("\tTranscripts:\t" + trIds + "\n");
            }
            Log.debug(sb);
        }

        return trIds;
    }

    public int getAaMinSeparation() {
        return aaMinSeparation;
    }

    public void setAaMinSeparation(int aaMinSeparation) {
        this.aaMinSeparation = aaMinSeparation;
    }

    public List<IdMapperEntry> getByProteinId(String proteinId) {
        return idMapper.getByPdbId(proteinId);
    }

    public List<DistanceResult> getDistanceResults() {
        return distanceResults;
    }

    public double getDistanceThreshold() {
        return distanceThreshold;
    }

    public void setDistanceThreshold(double distanceThreshold) {
        this.distanceThreshold = distanceThreshold;
    }

    public double getDistanceThresholdNon() {
        return distanceThresholdNon;
    }

    public void setDistanceThresholdNon(double distanceThresholdNon) {
        this.distanceThresholdNon = distanceThresholdNon;
    }

    public String getIdMapFile() {
        return idMapFile;
    }

    public void setIdMapFile(String idMapFile) {
        this.idMapFile = idMapFile;
    }

    public double getMaxMismatchRate() {
        return maxMismatchRate;
    }

    public void setMaxMismatchRate(double maxMismatchRate) {
        this.maxMismatchRate = maxMismatchRate;
    }

    public String getPdbOrganismCommon() {
        return pdbOrganismCommon;
    }

    public void setPdbOrganismCommon(String pdbOrganismCommon) {
        this.pdbOrganismCommon = pdbOrganismCommon;
    }

    public String getPdbOrganismScientific() {
        return pdbOrganismScientific;
    }

    public void setPdbOrganismScientific(String pdbOrganismScientific) {
        this.pdbOrganismScientific = pdbOrganismScientific;
    }

    public double getPdbResolution() {
        return pdbResolution;
    }

    public void setPdbResolution(double pdbResolution) {
        this.pdbResolution = pdbResolution;
    }

    public Transcript getTranscript(String trId) {
        return trancriptById.get(IdMapper.transcriptIdNoVersion(trId));
    }

    public void incCountFilesPass() {
        countFilesPass++;
    }

    public void incCountMapOk() {
        countMapOk++;
    }

    public void incCountMapError() {
        countMapError++;
    }

    /**
     * Initialize class (deferred initialization)
     * Note: This is not done at construction because we don't have a 'Config' object ready (loaded) at the time
     */
    public void initialize(Config config) {
        this.config = config;
        outputDbFileName = config.getDirDataGenomeVersion() + "/" + PROTEIN_INTERACTION_FILE;
        deleteOutputDb(); // Delete output file. We do this before opening the database, because we don't want to try to load the old interactions (we are rebuilding them)
    }

    void initTranscriptById() {
        // Initialize trancriptById
        trancriptById = new HashMap<>();
        for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes())
            for (Transcript tr : g) {
                String id = tr.getId();
                if (id.indexOf('.') > 0)
                    id = id.substring(0, id.indexOf('.')); // We don't store version number

                if (trancriptById.containsKey(id)) {
                    // There is already a transcript?
                    // Favor shorter chromosome names. E.g.: 'chr6' is better than 'chr6_cox_hap2'
                    String chrPrev = trancriptById.get(id).getChromosomeName();
                    String chr = tr.getChromosomeName();

                    if (chr.length() < chrPrev.length()) trancriptById.put(id, tr);
                } else {
                    // Transcript not present: Add it
                    trancriptById.put(id, tr);
                }
            }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    boolean isPdbFile(File f) {
        String fileName = f.getName();
        return f.isFile() && ( //
                fileName.endsWith(PdbFile.PDB_ENT_EXT) //
                        || fileName.endsWith(PdbFile.PDB_ENT_EXT_GZ) //
                        || fileName.endsWith(PdbFile.PDB_EXT) //
                        || fileName.endsWith(PdbFile.PDB_EXT_GZ) //
        );
    }

    public void loadIdMapper() {
        if (verbose) Log.info("Loading id maps " + idMapFile);
        idMapper = new IdMapper();
        idMapper.setVerbose(verbose);
        idMapper.load(idMapFile);
    }

    /**
     * Open output file
     */
    void openOuptutDbFile() {
        try {
            if (verbose) Log.info("Saving results to database file '" + outputDbFileName + "'");
            outpufDbFile = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outputDbFileName))));
            saved = new HashSet<String>();
        } catch (IOException e) {
            throw new RuntimeEOFException("Error opening output file '" + outputDbFileName + "'", e);
        }
    }

    /**
     * PDB analysis
     */
    public void pdb() {
        // Find all pdb files
        if (verbose) Log.info("Finding PDB files");
        pdbFileNames = findPdbFiles();

        // Create transcript map
        createTranscriptMap();

        // Map IDs and confirm that amino acid sequence matches (within certain error rate)
        if (verbose) Log.info("Analyzing PDB sequences");
        pdbAnalysis();
    }

    /**
     * Check that protein sequences form PDB matches sequences from Genome Return an
     * IdMapped of confirmed entries (i.e. AA sequence matches between transcript
     * and PDB)
     */
    protected void pdbAnalysis() {
        if (verbose) Log.info("Analyzing PDB files");

        for (String pdbFileName : pdbFileNames) {
            try {
                PdbFile pdbFile = new PdbFile(this, pdbFileName);
                pdbFile.pdbAnalysis();
            } catch (IOException e) {
                Log.error("Error processing file '" + pdbFileName + "'. Message: " + e.getMessage());
            }
        }

        if (verbose) Log.info("Done." //
                + "\n\tNumber of PDB files : " + pdbFileNames.size() //
                + "\n\tPDB files analyzed  : " + countFilesPass //
                + "\n\tAA 'in contact'     : " + countMapOk //
                + "\n\tMapping errors      : " + countMapError //
        );
    }

    /**
     * Run analysis.
     */
    public boolean run() {
        loadIdMapper(); // Load ID map table
        initTranscriptById(); // Load transcript mapping
        pdbReader = new PDBFileReader(); // Initialize reader
        openOuptutDbFile(); // Open output (database) file
        pdb(); // Run analysis
        closeOuptutDb(); // Close output file
        return true;
    }

    public boolean run(boolean storeResults) {
        distanceResults = new ArrayList<>();
        run();
        return true;
    }

    /**
     * Save results
     */
    public void save(List<DistanceResult> distResults) {
        if (debug) Log.debug("Saving " + distResults.size() + " results");
        for (DistanceResult d : distResults)
            try {
                String dstr = d.toString();
                if (!saved.contains(dstr)) {
                    outpufDbFile.write(dstr + "\n");
                    saved.add(dstr);
                    if (distanceResults != null) distanceResults.add(d);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setGenomeVer(String genomeVer) {
        this.genomeVer = genomeVer;
    }

    public void setPdbDir(String pdbDir) {
        this.pdbDir = pdbDir;
    }
}
