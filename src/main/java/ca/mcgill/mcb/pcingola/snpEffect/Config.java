package ca.mcgill.mcb.pcingola.snpEffect;

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
import java.util.Properties;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.codons.CodonTables;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.stats.CountByType;
import ca.mcgill.mcb.pcingola.util.Gpr;

public class Config implements Serializable, Iterable<String> {

	private static final long serialVersionUID = 877453207407217465L;

	public static final String DEFAULT_CONFIG_FILE = "snpEff.config";
	public static final String DEFAULT_DATA_DIR = "./data";
	public static String GENOMES_DIR = "genomes"; // Directory has one genomes information (FASTA files)
	public static int MAX_WARNING_COUNT = 20;

	// Keys in properties file
	public static final String KEY_CODON = "codon.";
	public static final String KEY_CODONTABLE = ".codonTable";
	public static final String KEY_DATA_DIR = "data.dir";
	public static final String KEY_DATABASE_LOCAL = "database.local";
	public static final String KEY_DATABASE_REPOSITORY = "database.repository";
	public static final String KEY_GENOME = ".genome";
	public static final String KEY_LOF_IGNORE_PROTEIN_CODING_AFTER = "lof.ignoreProteinCodingAfter";
	public static final String KEY_LOF_IGNORE_PROTEIN_CODING_BEFORE = "lof.ignoreProteinCodingBefore";
	public static final String KEY_LOF_DELETE_PROTEIN_CODING_BASES = "lof.deleteProteinCodingBases";
	public static final String KEY_REFERENCE = ".reference";
	public static final String KEY_VERSIONS_URL = "versions.url";

	private static Config configInstance = null; // Config is some kind of singleton because we want to make it accessible from everywhere

	boolean debug = false; // Debug mode?
	boolean verbose = false; // Verbose
	boolean treatAllAsProteinCoding; // Calculate effect only in coding genes. Default is true for testing and debugging reasons (command line default is 'false')
	boolean onlyRegulation; // Only use regulation features
	boolean errorOnMissingChromo; // Error if chromosome is missing
	boolean errorChromoHit; // Error if chromosome is not hit in a query
	double lofIgnoreProteinCodingAfter;
	double lofIgnoreProteinCodingBefore;
	double lofDeleteProteinCodingBases;
	String configDirPath = ""; // Configuration file directory
	String dataDir; // Directory containing all databases and genomes
	Properties properties;
	Genome genome;
	HashMap<String, Genome> genomeByVersion;
	HashMap<String, String> referenceByVersion;
	HashMap<String, String> nameByVersion;
	SnpEffectPredictor snpEffectPredictor;
	String databaseRepository = "";
	String versionsUrl = "";
	CountByType warningsCounter = new CountByType();

	public static Config get() {
		return configInstance;
	}

	public Config() {
		genome = new Genome();
		treatAllAsProteinCoding = false;
		onlyRegulation = false;
		errorOnMissingChromo = false;
		errorChromoHit = false;
		configInstance = this; // Make this instance globally available
	}

	/**
	 * Create a config (uses DEFAULT_CONFIG_FILE)
	 * @param genomeVersion
	 */
	public Config(String genomeVersion) {
		init(genomeVersion, DEFAULT_CONFIG_FILE, null);
	}

	/**
	 * Create a configuration from 'configFileName'
	 * @param genomeVersion
	 * @param configFileName
	 */
	public Config(String genomeVersion, String configFileName) {
		init(genomeVersion, configFileName, null);
	}

	/**
	 * Create a configuration from 'configFileName'
	 * @param genomeVersion
	 * @param configFileName
	 */
	public Config(String genomeVersion, String configFileName, String dataDir) {
		init(genomeVersion, configFileName, dataDir);
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
	 * @param genomeVersion
	 * @param properties
	 */
	void createCodonTables(String genomeVersion, Properties properties) {
		//---
		// Read codon tables
		//---
		for (Object key : properties.keySet()) {
			if (key.toString().startsWith(KEY_CODON)) {
				String name = key.toString().substring(KEY_CODON.length());
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
			if (keyStr.endsWith(KEY_CODONTABLE) && keyStr.startsWith(genomeVersion + ".")) {
				// Everything between gneomeName and ".codonTable" is assumed to be chromosome name
				int chrNameEnd = keyStr.length() - KEY_CODONTABLE.length();
				int chrNameStart = genomeVersion.length() + 1;
				int chrNameLen = chrNameEnd - chrNameStart;
				if (chrNameLen < 0) throw new RuntimeException("Error parsing config entry '" + keyStr + "'.\n\tExpected format: GENOME.CHROMOSOME.codonTable\n\tChromosome name not found!");
				String chromo = keyStr.substring(chrNameStart, chrNameEnd);

				String codonTableName = properties.getProperty(key.toString());
				CodonTable codonTable = CodonTables.getInstance().getTable(codonTableName);

				// Sanity checks
				if (genomeByVersion.get(genomeVersion) == null) throw new RuntimeException("Error parsing property '" + key + "'. No such genome '" + genomeVersion + "'");
				if (codonTable == null) throw new RuntimeException("Error parsing property '" + key + "'. No such codon table '" + codonTableName + "'");

				Chromosome chr = genomeByVersion.get(genomeVersion).getChromosome(chromo);
				if (chr == null) {
					// Create chromosome
					Genome genome = genomeByVersion.get(genomeVersion);
					chr = new Chromosome(genome, 0, 0, chromo);
					genome.add(chr);
				}

				// Everything seems to be OK, go on
				CodonTables.getInstance().add(genomeByVersion.get(genomeVersion), chr, codonTable);
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
			urlsb.append("v" + version + "/snpEff_v" + version + "_" + genomeVer + ".zip");

			return new URL(urlsb.toString());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Find the genome string in a 'codonTable' key string
	 */
	String findGenome(String codonKeyStr) {
		for (String genVer : genomeByVersion.keySet()) {
			if (codonKeyStr.startsWith(genVer + ".")) return genVer;
		}
		return null;
	}

	/**
	 * Genes file path (no extension)
	 * @return
	 */
	public String getBaseFileNameGenes() {
		return dataDir + "/" + genome.getVersion() + "/genes";
	}

	public String getBaseFileNameMotif() {
		return getDirDataVersion() + "/motif";
	}

	/**
	 * Regulation file (GFF format)
	 * @return
	 */
	public String getBaseFileNameRegulation() {
		return getDirDataVersion() + "/regulation";
	}

	/**
	 * Database local file for a specific database, such as 'dbSnp', 'ClinVar', etc.
	 */
	public String getDatabaseLocal(String dbName) {
		String dbLocal = properties.getProperty(KEY_DATABASE_LOCAL + "." + dbName, "");
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
		return properties.getProperty(KEY_DATABASE_REPOSITORY + "." + dbName, "");
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

	/**
	 * Main data directory
	 * @return
	 */
	public String getDirData() {
		return dataDir;
	}

	/**
	 * Data dir for a specific genome version (i.e. where the database is)
	 * @return
	 */
	public String getDirDataVersion() {
		return dataDir + "/" + genome.getVersion();
	}

	/**
	 * Main dir
	 * @return
	 */
	public String getDirMain() {
		File dir = new File(dataDir);
		return dir.getParent();
	}

	/**
	 * Directory where regulation 'BED' files are
	 * @return
	 */
	public String getDirRegulationBed() {
		return getDirDataVersion() + "/regulation.bed/";
	}

	/**
	 * Get a property as a double
	 * @param propertyName
	 * @return
	 */
	protected double getDouble(String propertyName, double defaultValue) {
		String val = getString(propertyName);
		if (val == null) return defaultValue;
		return Gpr.parseDoubleSafe(val);
	}

	/**
	 * Filenames for reference sequence (fasta files)
	 * @return
	 */
	public List<String> getFileListGenomeFasta() {
		ArrayList<String> files = new ArrayList<String>();
		files.add(getDirData() + "/genomes/" + genome.getVersion() + ".fa");
		files.add(getDirData() + "/" + genome.getVersion() + "/sequences.fa");
		return files;
	}

	public String getFileNameCds() {
		return getDirDataVersion() + "/cds.fa";
	}

	/**
	 * Filename for reference sequence (fasta file)
	 * Scans the list of files 'getFileListGenomeFasta()' and finds the first file that exists
	 * @return
	 */
	public String getFileNameGenomeFasta() {
		for (String f : getFileListGenomeFasta()) {
			if ((new File(f)).exists()) return f;
			if ((new File(f + ".gz")).exists()) return f;
		}
		return null;
	}

	public String getFileNameProteins() {
		return getDirDataVersion() + "/protein.fa";
	}

	public String getFileSnpEffectPredictor() {
		return getDirData() + "/" + genome.getVersion() + "/snpEffectPredictor.bin";
	}

	public Genome getGenome() {
		return genome;
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
	 * @param propertyName
	 * @return
	 */
	protected long getLong(String propertyName, long defaultValue) {
		String val = getString(propertyName);
		if (val == null) return defaultValue;
		return Gpr.parseLongSafe(val);
	}

	public String getName(String genomeVersion) {
		return nameByVersion.get(genomeVersion);
	}

	public String getReference(String genomeVersion) {
		return referenceByVersion.get(genomeVersion);
	}

	/**
	 * Get the relative path to a config file
	 * @return
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
	 * @param propertyName
	 * @return
	 */
	protected String getString(String propertyName) {
		return properties.getProperty(propertyName);
	}

	public String getVersionsUrl() {
		return versionsUrl;
	}

	/**
	 * Create a configuration from 'configFileName'
	 * @param genomeVersion
	 * @param configFileName
	 */
	void init(String genomeVersion, String configFileName, String dataDir) {
		treatAllAsProteinCoding = false;
		onlyRegulation = false;
		errorOnMissingChromo = true;
		errorChromoHit = true;
		this.dataDir = dataDir;

		readConfig(genomeVersion, configFileName); // Read config file and get a genome
		genome = genomeByVersion.get(genomeVersion); // Set a genome
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
		return nameByVersion.keySet().iterator();
	}

	/**
	 * Load a snpEff predictor
	 * WARNING: 'genome' object get replaced upon loading a snpEffectPredictor (this is a dangerous side effect)
	 */
	public SnpEffectPredictor loadSnpEffectPredictor() {
		snpEffectPredictor = SnpEffectPredictor.load(this);
		genome = snpEffectPredictor.genome; // WARNING: 'genome' object get replaced upon loading a snpEffectPredictor (this might have dangerous side effects)
		return snpEffectPredictor;
	}

	/**
	 * Read configuration file and create all 'genomes'
	 * @return
	 */
	private void readConfig(String genomeVersion, String configFileName) {
		//---
		// Read properties file
		//---
		configFileName = readProperties(configFileName);

		// Get config file directory
		configDirPath = "";
		try {
			File configDir = new File(configFileName).getAbsoluteFile().getParentFile();
			configDirPath = configDir.getCanonicalPath();
		} catch (Exception e) {
			// Nothing done
			configDirPath = "";
			e.printStackTrace();
		}

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
		genomeByVersion = new HashMap<String, Genome>();
		referenceByVersion = new HashMap<String, String>();
		nameByVersion = new HashMap<String, String>();

		// Sorted keys
		ArrayList<String> keys = new ArrayList<String>();
		for (Object k : properties.keySet())
			keys.add(k.toString());
		Collections.sort(keys);

		for (String key : keys) {
			if (key.endsWith(KEY_GENOME)) {
				String genVer = key.substring(0, key.length() - KEY_GENOME.length());

				// Add full namne
				String name = properties.getProperty(genVer + KEY_GENOME);
				nameByVersion.put(genVer, name);

				// Add reference
				String ref = properties.getProperty(genVer + KEY_REFERENCE);
				referenceByVersion.put(genVer, ref);
			} else {
				if (!key.endsWith(KEY_REFERENCE) //
						&& !key.endsWith(KEY_CODONTABLE) //
				) Gpr.debug("property{'" + key + "'} = '" + properties.getProperty(key) + "'");
			}
		}

		// Genome specified?
		if (!genomeVersion.isEmpty()) {
			// Read configuration file for genome version (if any)
			readGenomeConfig(genomeVersion, properties);

			// Codon tables
			createCodonTables(genomeVersion, properties);
		}

		// Set properties
		setFromProperties();
	}

	/**
	 * Read a config file for a given genome version (dataDir/genVer/snpEff.config)
	 * Add all properties found to 'properties'
	 *
	 * @param genVer
	 * @param properties
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

		Genome genome = new Genome(genVer, properties);
		genomeByVersion.put(genVer, genome);
	}

	/**
	 * Reads a properties file
	 * @param configFileName
	 * @return The path where config file is located
	 */
	String readProperties(String configFileName) {
		properties = new Properties();
		try {
			// Try to read properties file
			File confFile = new File(configFileName);
			if (Gpr.canRead(configFileName)) {
				properties.load(new FileReader(confFile));
				return configFileName;
			}

			// Build error message
			StringBuilder errMsg = new StringBuilder();
			errMsg.append("\tConfig file name : '" + configFileName + "'" + "\n\tFull path        : '" + confFile.getCanonicalPath() + "'" + "\n");

			// Absolute path? Nothing else to do...
			if (confFile.isAbsolute()) throw new RuntimeException("Cannot read config file!\n" + errMsg);

			// Try reading from relative dir
			configFileName = getRelativeConfigPath() + "/" + configFileName;
			confFile = new File(configFileName);
			if (Gpr.canRead(configFileName)) {
				properties.load(new FileReader(confFile));
				return configFileName;
			}
			errMsg.append("\n\tConfig file name : '" + configFileName + "'" + "\n\tFull path        : '" + confFile.getCanonicalPath() + "'" + "\n");

			throw new RuntimeException("Cannot read config file!\n" + errMsg);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Cannot find config file '" + configFileName + "'");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	public void setOnlyRegulation(boolean onlyRegulation) {
		this.onlyRegulation = onlyRegulation;
	}

	public void setSnpEffectPredictor(SnpEffectPredictor snpEffectPredictor) {
		this.snpEffectPredictor = snpEffectPredictor;
	}

	public void setTreatAllAsProteinCoding(boolean treatAllAsProteinCoding) {
		this.treatAllAsProteinCoding = treatAllAsProteinCoding;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (String genVer : this) {
			String name = nameByVersion.get(genVer).replace('_', ' ');
			String ref = referenceByVersion.get(genVer);

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
