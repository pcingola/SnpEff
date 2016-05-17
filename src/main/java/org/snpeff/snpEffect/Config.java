package org.snpeff.snpEffect;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.snpeff.SnpEff;
import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.stats.CountByType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Timer;

public class Config implements Serializable, Iterable<String> {

	private static final long serialVersionUID = 877453207407217465L;

	public static final String DEFAULT_CONFIG_FILE = "snpEff.config";
	public static final String DEFAULT_DATA_DIR = "./data";
	public static String GENOMES_DIR = "genomes"; // Directory has one genomes information (FASTA files)
	public static int MAX_WARNING_COUNT = 20;

	// Keys in properties file
	public static final String KEY_BUNDLE_SUFIX = ".bundle";
	public static final String KEY_CODON_PREFIX = "codon.";
	public static final String KEY_CODONTABLE_SUFIX = ".codonTable";
	public static final String KEY_COORDINATES = "coordinates";
	public static final String KEY_DATA_DIR = "data.dir";
	public static final String KEY_DATABASE_LOCAL = "database";
	public static final String KEY_DATABASE_REPOSITORY = "database.repository";
	public static final String KEY_DBNSFP_FIELDS = "dbnsfp.fields";
	public static final String KEY_GENOME_SUFIX = ".genome";
	public static final String KEY_LOF_IGNORE_PROTEIN_CODING_AFTER = "lof.ignoreProteinCodingAfter";
	public static final String KEY_LOF_IGNORE_PROTEIN_CODING_BEFORE = "lof.ignoreProteinCodingBefore";
	public static final String KEY_LOF_DELETE_PROTEIN_CODING_BASES = "lof.deleteProteinCodingBases";
	public static final String KEY_REFERENCE_SUFIX = ".reference";
	public static final String KEY_VERSIONS_URL = "versions.url";

	private static Config configInstance = null; // Config is some kind of singleton because we want to make it accessible from everywhere

	boolean debug = false; // Debug mode?
	boolean verbose = false; // Verbose
	boolean treatAllAsProteinCoding; // Calculate effect only in coding genes. Default is true for testing and debugging reasons (command line default is 'false')
	boolean onlyRegulation; // Only use regulation features
	boolean errorOnMissingChromo; // Error if chromosome is missing
	boolean errorChromoHit; // Error if chromosome is not hit in a query
	boolean hgvs = true; // Use HGVS notation?
	boolean hgvsShift = true; // Shift variants according to HGVS notation (towards the most 3prime possible coordinate)
	boolean hgvsOneLetterAa = false; // Use HGVS 1 letter amino acid in HGVS notation?
	boolean hgvsDnaOld = false;
	boolean hgvsTrId = false; // Use HGVS transcript ID in HGVS notation?
	double lofIgnoreProteinCodingAfter;
	double lofIgnoreProteinCodingBefore;
	double lofDeleteProteinCodingBases;
	String configFileName = "";
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
	String versionsUrl = "";
	CountByType warningsCounter = new CountByType();

	public static Config get() {
		return configInstance;
	}

	public static Config reset() {
		return configInstance = null;
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

	/**
	 * Convert a path to an absolute (i.e. canonical) path
	 */
	String canonicalDir(String dirName) {
		// Parse data dir
		if (dirName.startsWith("~/")) dirName = Gpr.HOME + "/" + dirName.substring(2); // Relative to 'home' dir? (starts with '~/')
		else if (dirName.startsWith("~")) dirName = Gpr.HOME + "/" + dirName.substring(1); // Relative to 'home' dir? (starts with '~')
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
		//---
		// Read codon tables
		//---
		for (Object key : properties.keySet()) {
			if (key.toString().startsWith(KEY_CODON_PREFIX)) {
				String name = key.toString().substring(KEY_CODON_PREFIX.length());
				String table = properties.getProperty(key.toString());
				CodonTable codonTable = new CodonTable(name, table);
				CodonTables.getInstance().add(codonTable);
			}
		}

		//---
		// Assign codon tables for different genome+chromosome
		//---
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
				if (codonTable == null) throw new RuntimeException("Error parsing property '" + key + "'. No such codon table '" + codonTableName + "'");

				// Find genome
				Genome gen = getGenome(genomeId);
				if (gen == null) throw new RuntimeException("Error parsing property '" + key + "'. No such genome '" + genomeId + "'");

				if (chromo != null) {
					// Find chromosome
					Chromosome chr = gen.getOrCreateChromosome(chromo);

					// Everything seems to be OK, go on
					CodonTables.getInstance().set(genomeById.get(genomeId), chr, codonTable);
				} else {
					// Set genome-wide chromosome table
					CodonTables.getInstance().set(genomeById.get(genomeId), codonTable);
				}
			}
		}
	}

	/**
	 * Build the URL for downloading a database file
	 *
	 * Format  : DatabaseRepository / v VERSION / snpEff_v VERSION _ genomeVersion .zip
	 * Example : http://downloads.sourceforge.net/project/snpeff/databases/v2_0_3/snpEff_v2_0_3_EF3.64.zip
	 */
	public URL downloadUrl(String genomeVer) {
		try {
			String version = SnpEff.VERSION_MAJOR;

			// Replace '.' by '_'
			version = version.replace('.', '_');

			String urlRoot = getDatabaseRepository();
			if (urlRoot == null || urlRoot.isEmpty()) throw new RuntimeException("Cannot find database repository. Missing '" + KEY_DATABASE_REPOSITORY + "' property in config file?");

			StringBuilder urlsb = new StringBuilder();
			urlsb.append(urlRoot);
			if (urlsb.charAt(urlRoot.length() - 1) != '/') urlsb.append("/");

			// It is in a bundle?
			String bundleName = getBundleName(genomeVer);
			if (bundleName != null) urlsb.append("v" + version + "/snpEff_v" + version + "_" + bundleName + ".zip");
			else urlsb.append("v" + version + "/snpEff_v" + version + "_" + genomeVer + ".zip");

			return new URL(urlsb.toString());
		} catch (MalformedURLException e) {
			return null;
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

	public String getCoordinates() {
		return getString(genomeVersion + "." + Config.KEY_COORDINATES);
	}

	/**
	 * Database local file for a specific database, such as 'dbSnp', 'ClinVar', etc.
	 */
	public String getDatabaseLocal(String dbName) {
		String coordinates = getCoordinates();
		if (coordinates == null) throw new RuntimeException("Cannot find coordinates config entry for genome '" + genomeVersion + "'");

		String dbLocal = properties.getProperty(KEY_DATABASE_LOCAL + "." + dbName + "." + coordinates, "");

		if (dbLocal.isEmpty()) return "";
		return canonicalDir(dbLocal);
	}

	public String getDatabaseRepository() {
		return databaseRepository;
	}

	/**
	 * Database repository for a specific database, such as 'dbSnp', 'ClinVar', etc.
	 */
	public String getDatabaseRepository(String dbName) {
		String coordinates = getCoordinates();
		if (coordinates == null) throw new RuntimeException("Cannot find coordinates config entry for genome '" + genomeVersion + "'");
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
			if (verbose) System.err.println("Cannot find coordinates config entry for genome '" + genomeVersion + "'");
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
		ArrayList<String> files = new ArrayList<String>();
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

		readConfig(configFileName, override); // Read config file and get a genome
		genome = genomeById.get(genomeVersion); // Set a genome
		if (!genomeVersion.isEmpty() && (genome == null)) throw new RuntimeException("No such genome '" + genomeVersion + "'");
		configInstance = this;
	}

	public boolean isDebug() {
		return debug;
	}

	public boolean isErrorChromoHit() {
		return errorChromoHit;
	}

	public boolean isErrorOnMissingChromo() {
		return errorOnMissingChromo;
	}

	public boolean isHgvs() {
		return hgvs;
	}

	public boolean isHgvs1LetterAA() {
		return hgvsOneLetterAa;
	}

	public boolean isHgvsDnaOld() {
		return hgvsDnaOld;
	}

	public boolean isHgvsShift() {
		return hgvsShift;
	}

	public boolean isHgvsTrId() {
		return hgvsTrId;
	}

	public boolean isOnlyRegulation() {
		return onlyRegulation;
	}

	public boolean isTreatAllAsProteinCoding() {
		return treatAllAsProteinCoding;
	}

	public boolean isVerbose() {
		return verbose;
	}

	@Override
	public Iterator<String> iterator() {
		return nameById.keySet().iterator();
	}

	/**
	 * Load properties from configuration file
	 * @return true if success
	 */
	boolean loadProperties(String configFileName) {
		try {
			File confFile = new File(configFileName);
			if (verbose) Timer.showStdErr("Reading config file: " + confFile.getCanonicalPath());

			if (Gpr.canRead(configFileName)) {
				// Load properties
				properties.load(new FileReader(confFile));

				// Set config directory
				configDirPath = confFile.getCanonicalFile().getParent();

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
	private void readConfig(String configFileName, Map<String, String> override) {
		//---
		// Read properties file
		//---
		configFileName = readProperties(configFileName, override);

		//---
		// Set attributes
		//---

		// Set data_dir if not overridden by constructor
		if (dataDir == null) dataDir = properties.getProperty(KEY_DATA_DIR, DEFAULT_DATA_DIR);
		dataDir = canonicalDir(dataDir); // Parse data dir

		// Repository
		databaseRepository = properties.getProperty(KEY_DATABASE_REPOSITORY, "");

		//---
		// Find all genomes in this configuration file
		//---
		genomeById = new HashMap<String, Genome>();
		referenceById = new HashMap<String, String>();
		nameById = new HashMap<String, String>();

		// Sorted keys
		ArrayList<String> keys = new ArrayList<String>();
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
		bundleByGenomeId = new HashMap<String, String>();
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
			if (debug) System.err.println("File '" + genomePropsFileName + "' not found"); // File does not exists? => OK
		}

		genome = new Genome(genVer, properties);
		genomeById.put(genVer, genome);
	}

	/**
	 * Reads a properties file
	 * @return The path where config file is located
	 */
	String readProperties(String configFileName) {
		properties = new Properties();
		try {
			// Build error message
			File confFile = new File(configFileName);
			if (loadProperties(configFileName)) return configFileName;

			// Absolute path? Nothing else to do...
			if (confFile.isAbsolute()) throw new RuntimeException("Cannot read config file '" + confFile.getCanonicalPath() + "'");

			// Try reading from current execution directory
			String confPath = getRelativeConfigPath() + "/" + configFileName;
			confFile = new File(confPath);
			if (loadProperties(confPath)) return confPath;

			throw new RuntimeException("Cannot read config file '" + configFileName + "'\n");
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot find config file '" + configFileName + "'");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Read config file
	 */
	String readProperties(String configFileName, Map<String, String> override) {
		String configFile = readProperties(configFileName);

		if (override != null) {
			for (String key : override.keySet()) {
				properties.setProperty(key, override.get(key));
			}
		}

		return configFile;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setErrorChromoHit(boolean errorChromoHit) {
		this.errorChromoHit = errorChromoHit;
	}

	public void setErrorOnMissingChromo(boolean errorOnMissingChromo) {
		this.errorOnMissingChromo = errorOnMissingChromo;
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

	public void setHgvsDnaOld(boolean hgvsDnaOld) {
		this.hgvsDnaOld = hgvsDnaOld;
	}

	public void setHgvsOneLetterAA(boolean hgvsOneLetterAa) {
		this.hgvsOneLetterAa = hgvsOneLetterAa;
	}

	public void setHgvsShift(boolean hgvsShift) {
		this.hgvsShift = hgvsShift;
	}

	public void setHgvsTrId(boolean hgvsTrId) {
		this.hgvsTrId = hgvsTrId;
	}

	public void setOnlyRegulation(boolean onlyRegulation) {
		this.onlyRegulation = onlyRegulation;
	}

	public void setSnpEffectPredictor(SnpEffectPredictor snpEffectPredictor) {
		this.snpEffectPredictor = snpEffectPredictor;
	}

	public void setString(String propertyName, String value) {
		properties.setProperty(propertyName, value);
	}

	public void setTreatAllAsProteinCoding(boolean treatAllAsProteinCoding) {
		this.treatAllAsProteinCoding = treatAllAsProteinCoding;
	}

	public void setUseHgvs(boolean useHgvs) {
		hgvs = useHgvs;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

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
	 * Show a warning message and exit
	 */
	public void warning(String warningType, String details) {
		long count = warningsCounter.inc(warningType);

		if (debug || count < MAX_WARNING_COUNT) {
			if (debug) Gpr.debug(warningType + details, 1);
			else System.err.println(warningType + details);
		} else if (count <= MAX_WARNING_COUNT) {
			String msg = "Too many '" + warningType + "' warnings, no further warnings will be shown:\n" + warningType + details;
			if (debug) Gpr.debug(msg, 1);
			else System.err.println(msg);
		}
	}
}
