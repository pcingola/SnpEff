package org.snpeff.snpEffect;

import org.snpeff.SnpEff;
import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class Config implements Serializable, Iterable<String> {

    public static final String DEFAULT_CONFIG_FILE = "snpEff.config";
    public static final String DEFAULT_DATA_DIR = "./data";
    // Keys in properties file
    public static final String KEY_BUNDLE_SUFIX = ".bundle";
    public static final String KEY_CODON_PREFIX = "codon.";
    public static final String KEY_CODONTABLE_SUFIX = ".codonTable";
    public static final String KEY_COORDINATES = "coordinates";
    public static final String KEY_DATA_DIR = "data.dir";
    public static final String KEY_DATABASE_LOCAL = "database";
    public static final String KEY_DATABASE_REPOSITORY = "database.repository";
    public static final String KEY_DATABASE_REPOSITORY_KEY = "database.repositoryKey";
    public static final String KEY_DBNSFP_FIELDS = "dbnsfp.fields";
    public static final String KEY_GENOME_SUFIX = ".genome";
    public static final String KEY_LOF_IGNORE_PROTEIN_CODING_AFTER = "lof.ignoreProteinCodingAfter";
    public static final String KEY_LOF_IGNORE_PROTEIN_CODING_BEFORE = "lof.ignoreProteinCodingBefore";
    public static final String KEY_LOF_DELETE_PROTEIN_CODING_BASES = "lof.deleteProteinCodingBases";
    public static final String KEY_REFERENCE_SUFIX = ".reference";
    public static final String KEY_VERSIONS_URL = "versions.url";

    // Database versions compatibility
    //
    //     SnpEff version | Compatible
    //     ---------------+-----------
    //                5.1 | 5.0
    //     ---------------+-----------
    //                5.2 | 5.1, 5.0
    //     ---------------+-----------
    public static final String[] COMPATIBLE_WITH_5_2 = {"5.0", "5.1"};
    public static final String[] COMPATIBLE_WITH_5_1 = {"5.0"};
    public static final Map<String, String[]> DATABASE_COMPATIBLE_VERSIONS = Map.of("5.1", COMPATIBLE_WITH_5_1, "5.2", COMPATIBLE_WITH_5_2);

    private static final long serialVersionUID = 877453207407217465L;

    public static String GENOMES_DIR = "genomes"; // Directory has one genomes information (FASTA files)
    public static String DEFAULT_COORDINATES = "GRCh37";

    private static Config configInstance = null; // Config is some kind of singleton because we want to make it accessible from everywhere

    boolean debug = false; // Debug mode?
    boolean verbose = false; // Verbose
    boolean quiet = false; // Quiet
    boolean treatAllAsProteinCoding; // Calculate effect only in coding genes. Default is true for testing and debugging reasons (command line default is 'false')
    boolean onlyRegulation; // Only use regulation features
    boolean errorOnMissingChromo; // Error if chromosome is missing
    boolean errorChromoHit; // Error if chromosome is not hit in a query
    boolean expandIub = true; // Expand IUB codes in variants?
    boolean hgvs = true; // Use HGVS notation?
    boolean hgvsShift = true; // Shift variants according to HGVS notation (towards the most 3prime possible coordinate)
    boolean hgvsOneLetterAa = false; // Use HGVS 1 letter amino acid in HGVS notation?
    boolean hgvsOld = false;
    boolean hgvsTrId = false; // Use HGVS transcript ID in HGVS notation?
    double lofIgnoreProteinCodingAfter;
    double lofIgnoreProteinCodingBefore;
    double lofDeleteProteinCodingBases;
    String configFileName = "";
    String configFileCanonicaPath;
    String configDirPath = ""; // Configuration file directory
    String dataDir; // Directory containing all databases and genomes
    String genomeVersion;
    Properties properties;
    Genome genome;
    HashMap<String, Genome> genomeById;
    HashMap<String, String> referenceById;
    HashMap<String, String> nameById;
    HashMap<String, String> bundleByGenomeId;
    SnpEffectPredictor snpEffectPredictor;
    String databaseRepository = "";
    String databaseRepositoryKey = "";
    String versionsUrl = "";

    /**
     * This constructor is used in test cases
     */
    public Config(Genome genome) {
        this.genomeVersion = genome.getVersion();
        this.genome = genome;
        configInstance = this;
    }

    /**
     * Create a config (uses DEFAULT_CONFIG_FILE)
     */
    public Config(String genomeVersion) {
        init(genomeVersion, DEFAULT_CONFIG_FILE, null, null);
    }

    /**
     * Create a configuration from 'configFileName'
     */
    public Config(String genomeVersion, String configFileName) {
        init(genomeVersion, configFileName, null, null);
    }

    /**
     * Create a configuration from 'configFileName'
     */
    public Config(String genomeVersion, String configFileName, String dataDir, Map<String, String> override) {
        init(genomeVersion, configFileName, dataDir, override);
    }

    public Config(String genomeVersion, String configFileName, String dataDir, Map<String, String> override, boolean verbose) {
        this.verbose = verbose;
        init(genomeVersion, configFileName, dataDir, override);
    }

    public static Config get() {
        return configInstance;
    }

    public static Config reset() {
        return configInstance = null;
    }

    /**
     * Convert a path to an absolute (i.e. canonical) path
     */
    String canonicalDir(String dirName) {
        // Parse data dir
        if (dirName.startsWith("~/"))
            dirName = Gpr.HOME + "/" + dirName.substring(2); // Relative to 'home' dir? (starts with '~/')
        else if (dirName.startsWith("~"))
            dirName = Gpr.HOME + "/" + dirName.substring(1); // Relative to 'home' dir? (starts with '~')
        else if (!dirName.startsWith("/")) dirName = configDirPath + "/" + dirName; // Not an absolute path?

        // Remove all trailing slashes
        while (dirName.endsWith("/"))
            dirName = dirName.substring(0, dirName.length() - 1); // make sure path doesn't end with '/' (some OS can have problems with "//" in paths)

        return dirName;
    }

    /**
     * Extract and create codon tables
     */
    void createCodonTables(String genomeId, Properties properties) {
        // Read codon tables
        for (Object k : properties.keySet()) {
            String key = k.toString().trim();
            if (key.startsWith(KEY_CODON_PREFIX)) {
                String name = key.substring(KEY_CODON_PREFIX.length());
                String table = properties.getProperty(key);
                if (debug) Log.debug("Reading codon table '" + name + "'");
                CodonTable codonTable = new CodonTable(name, table);
                CodonTables.getInstance().add(codonTable);
            }
        }

        // Assign codon tables for different genome+chromosome
        for (Object key : properties.keySet()) {
            String keyStr = key.toString();
            if (keyStr.endsWith(KEY_CODONTABLE_SUFIX) && keyStr.startsWith(genomeId + ".")) {
                // Everything between gneomeName and ".codonTable" is assumed to be chromosome name
                int chrNameEnd = keyStr.length() - KEY_CODONTABLE_SUFIX.length();
                int chrNameStart = genomeId.length() + 1;
                int chrNameLen = chrNameEnd - chrNameStart;
                String chromo = null;
                if (chrNameLen > 0) chromo = keyStr.substring(chrNameStart, chrNameEnd);

                // Find codon table
                String codonTableName = properties.getProperty(key.toString());
                CodonTable codonTable = CodonTables.getInstance().getTable(codonTableName);
                if (codonTable == null)
                    throw new RuntimeException("Error parsing property '" + key + "'. No such codon table '" + codonTableName + "'");

                // Find genome
                Genome gen = getGenome(genomeId);
                if (gen == null)
                    throw new RuntimeException("Error parsing property '" + key + "'. No such genome '" + genomeId + "'");

                if (chromo != null) {
                    // Find chromosome
                    Chromosome chr = gen.getOrCreateChromosome(chromo);

                    if( verbose ) Log.info("Codon table '" + codonTableName + "' assigned to chromosome '" + chr.getChromosomeName() + "'");

                    // Everything seems to be OK, go on
                    CodonTables.getInstance().set(genomeById.get(genomeId), chr, codonTable);
                } else {
                    // Set genome-wide chromosome table
                    Log.info("Codon table '" + codonTableName + "' for genome '" + genomeId + "'");
                    CodonTables.getInstance().set(genomeById.get(genomeId), codonTable);
                }
            }
        }
    }

    /**
     * Get a list of URLs that can be used to download a database
     * Use compatible versions from 'getDatabaseCompatibilityVersions()'
     */
    public List<URL> downloadUrl(String genomeVer) {
        List<String> versions = getDatabaseCompatibilityVersions();
        List<URL> urls = new LinkedList<>();
        for (String version : versions) {
            urls.add(downloadUrl(genomeVer, version));
        }
        return urls;
    }

    /**
     * Build the URL for downloading a database file
     * <p>
     * Format  : DatabaseRepository / v VERSION / snpEff_v VERSION _ genomeVersion .zip
     * Example : http://downloads.sourceforge.net/project/snpeff/databases/v2_0_3/snpEff_v2_0_3_EF3.64.zip
     */
    public URL downloadUrl(String genomeVer, String version) {
        try {
            // Replace '.' by '_'
            version = version.replace('.', '_');

            String urlRoot = getDatabaseRepository();
            if (urlRoot == null || urlRoot.isEmpty())
                throw new RuntimeException("Cannot find database repository. Missing '" + KEY_DATABASE_REPOSITORY + "' property in config file?");

            StringBuilder urlsb = new StringBuilder();
            urlsb.append(urlRoot);
            if (urlsb.charAt(urlRoot.length() - 1) != '/') urlsb.append("/");

            // It is in a bundle?
            String bundleName = getBundleName(genomeVer);
            if (bundleName != null) urlsb.append("v" + version + "/snpEff_v" + version + "_" + bundleName + ".zip");
            else urlsb.append("v" + version + "/snpEff_v" + version + "_" + genomeVer + ".zip");

            // Is there a key?
            if (databaseRepositoryKey != null && !databaseRepositoryKey.isEmpty()) urlsb.append(databaseRepositoryKey);

            return new URL(urlsb.toString());
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * Find config file in different locations
     * @param configFileName
     * @return Canonical path to config file
     */
    String findConfigFile(String configFile) {
        try {
            // Build error message
            File confFile = new File(configFile);
            if (verbose) Log.info("Looking for config file: '" + confFile.getCanonicalPath() + "'");
            if( confFile.exists() && confFile.canRead() ) return confFile.getCanonicalPath();

            // Absolute path? Nothing else to do...
            if (confFile.isAbsolute())
                throw new RuntimeException("Cannot find config file '" + confFile.getCanonicalPath() + "'");

            // Try reading from current execution directory
            String confPath = getRelativeConfigPath() + "/" + configFile;
            confFile = new File(confPath);
            if( confFile.exists() && confFile.canRead() ) return confFile.getCanonicalPath();

            throw new RuntimeException("Cannot find config file '" + configFile + "'\n");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find config file '" + configFile + "'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Genes file path (no extension)
     */
    public String getBaseFileNameGenes() {
        return dataDir + "/" + genome.getVersion() + "/genes";
    }

    public String getBaseFileNameMotif() {
        return getDirDataGenomeVersion() + "/motif";
    }

    /**
     * Regulation file (GFF format)
     */
    public String getBaseFileNameRegulation() {
        return getDirDataGenomeVersion() + "/regulation";
    }

    public String getBaseFileNameSequence() {
        return getDirData() + "/" + genome.getVersion() + "/sequence";
    }

    /**
     * Is this genome packed in a bundle?
     */
    public String getBundleName(String genomeVer) {
        return bundleByGenomeId.get(genomeVer);
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public String getConfigFileCanonicaPath() {
        return configFileCanonicaPath;
    }

    public String getCoordinates() {
        String coords = getString(genomeVersion + "." + Config.KEY_COORDINATES);
        if (coords != null) return coords;

        // Not found? Try genome name without version (e.g. 'GRCh38.85' => 'GRCh38')
        coords = genomeVersion.split("\\.")[0];
        return coords.isEmpty() ? DEFAULT_COORDINATES : coords;
    }

    /**
     * Return a list of databases that are compatible with this SnpEff version number
     */
    public List<String> getDatabaseCompatibilityVersions() {
        // A list with at least this version number
        List<String> dcv = new LinkedList<>();
        dcv.add(SnpEff.VERSION_MAJOR);

        // Retrieve more compatible versions
        var moreVersions = DATABASE_COMPATIBLE_VERSIONS.get(SnpEff.VERSION_MAJOR);
        if (moreVersions != null) {
            for (String v : moreVersions) dcv.add(v);
        }

        return dcv;
    }

    /**
     * Database local file for a specific database, such as 'dbSnp', 'ClinVar', etc.
     */
    public String getDatabaseLocal(String dbName) {
        String dbLocal = properties.getProperty(getDatabaseLocalKey(dbName), "");
        if (dbLocal.isEmpty()) return "";
        return canonicalDir(dbLocal);
    }

    public String getDatabaseLocalKey(String dbName) {
        String coordinates = getCoordinates();
        if (coordinates == null)
            throw new RuntimeException("Cannot find coordinates config entry for genome '" + genomeVersion + "'");

        return KEY_DATABASE_LOCAL + "." + dbName + "." + coordinates;
    }

    public String getDatabaseRepository() {
        return databaseRepository;
    }

    /**
     * Database repository for a specific database, such as 'dbSnp', 'ClinVar', etc.
     */
    public String getDatabaseRepository(String dbName) {
        String coordinates = getCoordinates();
        if (coordinates == null)
            throw new RuntimeException("Cannot find coordinates config entry for genome '" + genomeVersion + "'");
        return properties.getProperty(KEY_DATABASE_REPOSITORY + "." + dbName + "." + coordinates, "");
    }

    public URL getDatabaseRepositoryUrl(String dbName) {
        String repo = getDatabaseRepository(dbName);
        if (repo == null || repo.isEmpty()) return null;
        try {
            return new URL(repo);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public String getDbNsfpFields() {
        String coordinates = getCoordinates();
        if (coordinates == null) {
            if (verbose) Log.info("Cannot find coordinates config entry for genome '" + genomeVersion + "'");
            return null;
        }

        return properties.getProperty(KEY_DBNSFP_FIELDS + "." + coordinates, "");
    }

    /**
     * Main data directory
     */
    public String getDirData() {
        return dataDir;
    }

    /**
     * Data dir for a specific genome version (i.e. where the database is)
     */
    public String getDirDataGenomeVersion() {
        return dataDir + "/" + genome.getVersion();
    }

    /**
     * Main dir
     */
    public String getDirMain() {
        File dir = new File(dataDir);
        return dir.getParent();
    }

    /**
     * Directory where regulation 'BED' files are
     */
    public String getDirRegulationBed() {
        return getDirDataGenomeVersion() + "/regulation.bed/";
    }

    /**
     * Get a property as a double
     */
    public double getDouble(String propertyName, double defaultValue) {
        String val = getString(propertyName);
        if (val == null) return defaultValue;
        return Gpr.parseDoubleSafe(val);
    }

    /**
     * Filenames for reference sequence (fasta files)
     */
    public List<String> getFileListGenomeFasta() {
        ArrayList<String> files = new ArrayList<>();
        files.add(getDirData() + "/genomes/" + genome.getVersion() + ".fa");
        files.add(getDirData() + "/" + genome.getVersion() + "/sequences.fa");
        return files;
    }

    public String getFileNameCds() {
        return getDirDataGenomeVersion() + "/cds.fa";
    }

    /**
     * Filename for reference sequence (fasta file)
     * Scans the list of files 'getFileListGenomeFasta()' and finds the first file that exists
     */
    public String getFileNameGenomeFasta() {
        for (String f : getFileListGenomeFasta()) {
            if ((new File(f)).exists()) return f;
            if ((new File(f + ".gz")).exists()) return f;
        }
        return null;
    }

    public String getFileNameProteins() {
        return getDirDataGenomeVersion() + "/protein.fa";
    }

    public String getFileNameSequence() {
        return getBaseFileNameSequence() + ".bin";
    }

    public String getFileNameSequence(String chr) {
        String chrNameSafe = Gpr.sanityzeFileName(chr);
        return getBaseFileNameSequence() + "." + chrNameSafe + ".bin";
    }

    public String getFileSnpEffectPredictor() {
        return getDirData() + "/" + genome.getVersion() + "/snpEffectPredictor.bin";
    }

    public Genome getGenome() {
        return genome;
    }

    public Genome getGenome(String genomeId) {
        return genomeById.get(genomeId);
    }

    public String getGenomeVersion() {
        return genomeVersion;
    }

    public double getLofDeleteProteinCodingBases() {
        return lofDeleteProteinCodingBases;
    }

    public double getLofIgnoreProteinCodingAfter() {
        return lofIgnoreProteinCodingAfter;
    }

    public double getLofIgnoreProteinCodingBefore() {
        return lofIgnoreProteinCodingBefore;
    }

    /**
     * Get a property as a long
     */
    public long getLong(String propertyName, long defaultValue) {
        String val = getString(propertyName);
        if (val == null) return defaultValue;
        return Gpr.parseLongSafe(val);
    }

    public String getName(String genomeVersion) {
        return nameById.get(genomeVersion);
    }

    public String getProperty(String property) {
        if (!properties.containsKey(property)) {
            if( isDebug() ) Log.warning("Property '" + property + "' not found in config file '" + getConfigFileCanonicaPath() + "'");
            return null;
        } 
        return properties.getProperty(property);
    }

    /**
	 * Parse a comma separated property as a string array
	*/
	public String[] propertyToStringArray(String attr) {
		String value = properties.getProperty(attr);
		if (value == null) return new String[0];

		String values[] = value.split("[\\s+,]");
		LinkedList<String> list = new LinkedList<>();
		for (String val : values)
			if (val.length() > 0) list.add(val);

		return list.toArray(new String[0]);
	}

    public String getReference(String genomeVersion) {
        return referenceById.get(genomeVersion);
    }

    /**
     * Get the relative path to a config file
     */
    String getRelativeConfigPath() {
        URL url = Config.class.getProtectionDomain().getCodeSource().getLocation();
        try {
            File path = new File(url.toURI());
            return path.getParent();
        } catch (Exception e) {
            throw new RuntimeException("Cannot get path '" + url + "'", e);
        }
    }

    public SnpEffectPredictor getSnpEffectPredictor() {
        return snpEffectPredictor;
    }

    public void setSnpEffectPredictor(SnpEffectPredictor snpEffectPredictor) {
        this.snpEffectPredictor = snpEffectPredictor;
    }

    /**
     * Get a property as a string
     */
    public String getString(String propertyName) {
        return properties.getProperty(propertyName);
    }

    /**
     * Get a property as a string
     */
    public String getString(String propertyName, String defaultValue) {
        return properties.getProperty(propertyName, defaultValue);
    }

    public String getVersionsUrl() {
        return versionsUrl;
    }

    /**
     * Create a configuration from 'configFileName'
     */
    void init(String genomeVersion, String configFileName, String dataDir, Map<String, String> override) {
        treatAllAsProteinCoding = false;
        onlyRegulation = false;
        errorOnMissingChromo = true;
        errorChromoHit = true;
        this.configFileName = configFileName;
        this.genomeVersion = genomeVersion;
        this.dataDir = dataDir;

        configFileCanonicaPath = findConfigFile(configFileName);
        readConfig(configFileCanonicaPath, override); // Read config file and get a genome
        genome = genomeById.get(genomeVersion); // Set a genome
        if (!genomeVersion.isEmpty() && (genome == null))
            throw new RuntimeException("No such genome '" + genomeVersion + "' in config file '" + configFileCanonicaPath + "'");

        // Make this the current singleton instance
        configInstance = this;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isErrorChromoHit() {
        return errorChromoHit;
    }

    public void setErrorChromoHit(boolean errorChromoHit) {
        this.errorChromoHit = errorChromoHit;
    }

    public boolean isErrorOnMissingChromo() {
        return errorOnMissingChromo;
    }

    public void setErrorOnMissingChromo(boolean errorOnMissingChromo) {
        this.errorOnMissingChromo = errorOnMissingChromo;
    }

    public boolean isExpandIub() {
        return expandIub;
    }

    public void setExpandIub(boolean expandIub) {
        this.expandIub = expandIub;
    }

    public boolean isHgvs() {
        return hgvs;
    }

    public boolean isHgvs1LetterAA() {
        return hgvsOneLetterAa;
    }

    public boolean isHgvsOld() {
        return hgvsOld;
    }

    public void setHgvsOld(boolean hgvsDnaOld) {
        hgvsOld = hgvsDnaOld;
    }

    public boolean isHgvsShift() {
        return hgvsShift;
    }

    public void setHgvsShift(boolean hgvsShift) {
        this.hgvsShift = hgvsShift;
    }

    public boolean isHgvsTrId() {
        return hgvsTrId;
    }

    public void setHgvsTrId(boolean hgvsTrId) {
        this.hgvsTrId = hgvsTrId;
    }

    public boolean isOnlyRegulation() {
        return onlyRegulation;
    }

    public void setOnlyRegulation(boolean onlyRegulation) {
        this.onlyRegulation = onlyRegulation;
    }

    public boolean isQuiet() {
        return quiet;
    }

    public void setQuiet(boolean quiet) {
        this.quiet = quiet;
    }

    public boolean isTreatAllAsProteinCoding() {
        return treatAllAsProteinCoding;
    }

    public void setTreatAllAsProteinCoding(boolean treatAllAsProteinCoding) {
        this.treatAllAsProteinCoding = treatAllAsProteinCoding;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public Iterator<String> iterator() {
        return nameById.keySet().iterator();
    }

    public List<String> keys() {
        Set<Object> keyset = properties.keySet();
        ArrayList<String> l = new ArrayList<>(keyset.size());
        for (Object o : keyset)
            l.add(o.toString());
        return l;
    }

    /**
     * Load properties from configuration file
     *
     * @return true if success
     */
    boolean loadProperties(String configPath) {
        try {
            File confFile = new File(configPath);
            if (verbose) Log.info("Reading config file: " + confFile.getCanonicalPath());

            if (Gpr.canRead(configPath)) {
                // Load properties
                var freader = new FileReader(confFile);
                properties.load(freader);

                // Set config directory
                configDirPath = confFile.getCanonicalFile().getParent();

                freader.close();
                return true;
            }
        } catch (Exception e) {
            properties = null;
            configDirPath = "";
            throw new RuntimeException(e);
        }

        return false;
    }

    /**
     * Load a snpEff predictor
     * WARNING: 'genome' object get replaced upon loading a snpEffectPredictor (this is a dangerous side effect)
     */
    public SnpEffectPredictor loadSnpEffectPredictor() {
        snpEffectPredictor = SnpEffectPredictor.load(this);
        genome = snpEffectPredictor.genome; // WARNING: 'genome' object get replaced upon loading a snpEffectPredictor (this might have dangerous side effects)
        snpEffectPredictor.setDebug(debug);
        return snpEffectPredictor;
    }

    /**
     * Read configuration file and create all 'genomes'
     */
    private void readConfig(String configPath, Map<String, String> override) {
        //---
        // Read properties file
        //---
        readProperties(configPath, override);

        //---
        // Set attributes
        //---

        // Set data_dir if not overridden by constructor
        if (dataDir == null) dataDir = properties.getProperty(KEY_DATA_DIR, DEFAULT_DATA_DIR);
        dataDir = canonicalDir(dataDir); // Parse data dir

        // Repository
        databaseRepository = unquote(properties.getProperty(KEY_DATABASE_REPOSITORY, ""));
        databaseRepositoryKey = unquote(properties.getProperty(KEY_DATABASE_REPOSITORY_KEY, ""));

        //---
        // Find all genomes in this configuration file
        //---
        genomeById = new HashMap<>();
        referenceById = new HashMap<>();
        nameById = new HashMap<>();

        // Sorted keys
        ArrayList<String> keys = new ArrayList<>();
        for (Object k : properties.keySet())
            keys.add(k.toString());
        Collections.sort(keys);

        for (String key : keys) {
            if (key.endsWith(KEY_GENOME_SUFIX)) {
                String genVer = key.substring(0, key.length() - KEY_GENOME_SUFIX.length());

                // Add full name
                String name = properties.getProperty(genVer + KEY_GENOME_SUFIX);
                nameById.put(genVer, name);

                // Add reference
                String ref = properties.getProperty(genVer + KEY_REFERENCE_SUFIX);
                referenceById.put(genVer, ref);
            }
        }

        //---
        // Find all bundles
        //---
        bundleByGenomeId = new HashMap<>();
        for (String key : keys) {
            if (key.endsWith(KEY_BUNDLE_SUFIX)) {
                String bundleName = key.substring(0, key.length() - KEY_BUNDLE_SUFIX.length());
                String entries = properties.getProperty(key);
                for (String gen : entries.split("\\s+")) {
                    gen = gen.trim();
                    bundleByGenomeId.put(gen, bundleName);
                }
            }
        }

        // Genome specified?
        if (!genomeVersion.isEmpty()) {
            readGenomeConfig(genomeVersion, properties); // Read configuration file for genome version (if any)
            genome = new Genome(genomeVersion, this); // Create genome object
            genomeById.put(genomeVersion, genome);
            createCodonTables(genomeVersion, properties); // Codon tables
        }

        // Set properties
        setFromProperties();
    }

    /**
     * Read a config file for a given genome version (dataDir/genVer/snpEff.config)
     * Add all properties found to 'properties'
     */
    void readGenomeConfig(String genVer, Properties properties) {
        String genomePropsFileName = dataDir + "/" + genVer + "/snpEff.config";
        try {
            // Read properties file "data_dir/genomeVersion/snpEff.conf"
            // If the file exists, read all properties and add them to the main 'properties'
            Properties genomeProps = new Properties();
            genomeProps.load(new FileReader(new File(genomePropsFileName)));

            // Copy genomeProperties to 'properties'
            for (Object propKey : genomeProps.keySet()) {
                String pk = propKey.toString();
                String propVal = genomeProps.getProperty(pk);
                if (properties.getProperty(pk) == null) {
                    properties.setProperty(pk, propVal);
                } else System.err.println("Ignoring property '" + pk + "' in file '" + genomePropsFileName + "'");
            }
        } catch (Exception e) {
            if (debug)
                System.err.println("File '" + genomePropsFileName + "' not found"); // File does not exists? => OK
        }
    }

    /**
     * Read config file
     */
    void readProperties(String configPath, Map<String, String> override) {
        properties = new Properties();
        if (!loadProperties(configPath)) throw new RuntimeException("Cannot read config file '" + configPath + "'\n");

        if (override != null) {
            for (String key : override.keySet()) {
                properties.setProperty(key, override.get(key));
            }
        }
    }

    /**
     * Set from parameter properties
     */
    void setFromProperties() {
        versionsUrl = getString(KEY_VERSIONS_URL);
        lofIgnoreProteinCodingAfter = getDouble(KEY_LOF_IGNORE_PROTEIN_CODING_AFTER, LossOfFunction.DEFAULT_IGNORE_PROTEIN_CODING_AFTER);
        lofIgnoreProteinCodingBefore = getDouble(KEY_LOF_IGNORE_PROTEIN_CODING_BEFORE, LossOfFunction.DEFAULT_IGNORE_PROTEIN_CODING_BEFORE);
        lofDeleteProteinCodingBases = getDouble(KEY_LOF_DELETE_PROTEIN_CODING_BASES, LossOfFunction.DEFAULT_DELETE_PROTEIN_CODING_BASES);
    }

    public void setHgvsOneLetterAA(boolean hgvsOneLetterAa) {
        this.hgvsOneLetterAa = hgvsOneLetterAa;
    }

    public void setString(String propertyName, String value) {
        properties.setProperty(propertyName, value);
    }

    public void setUseHgvs(boolean useHgvs) {
        hgvs = useHgvs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        ArrayList<String> keys = new ArrayList<>();
        for (Object key : properties.keySet())
            keys.add(key.toString());
        Collections.sort(keys);

        for (String key : keys)
            sb.append("\t" + key + " = '" + properties.getProperty(key) + "'\n");

        for (String genVer : this) {
            String name = nameById.get(genVer).replace('_', ' ');
            String ref = referenceById.get(genVer);

            sb.append("\t" + genVer);
            sb.append("\t" + name);
            if (ref != null) sb.append("\t" + ref);
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Remove quotes from a string
     */
    private String unquote(String s) {
        if (s == null || s.length() < 2) return s;
        int l = s.length();
        if ((s.charAt(0) == '"' && s.charAt(l - 1) == '"') //
                || (s.charAt(0) == '\'' && s.charAt(l - 1) == '\'') //
        ) { //
            return s.substring(1, l - 1);
        }
        return s;
    }
}
