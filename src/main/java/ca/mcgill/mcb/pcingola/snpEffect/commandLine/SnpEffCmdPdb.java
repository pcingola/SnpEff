package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.biojava.bio.structure.AminoAcid;
import org.biojava.bio.structure.Atom;
import org.biojava.bio.structure.Calc;
import org.biojava.bio.structure.Chain;
import org.biojava.bio.structure.Group;
import org.biojava.bio.structure.Structure;
import org.biojava.bio.structure.StructureException;
import org.biojava.bio.structure.io.PDBFileReader;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.pdb.DistanceResult;
import ca.mcgill.mcb.pcingola.pdb.IdMapper;
import ca.mcgill.mcb.pcingola.pdb.IdMapperEntry;
import ca.mcgill.mcb.pcingola.pdb.PdbFile;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import net.sf.samtools.util.RuntimeEOFException;

/**
 * PDB distance analysis
 *
 * References: http://biojava.org/wiki/BioJava:CookBook:PDB:read
 *
 * @author pcingola
 */
public class SnpEffCmdPdb extends SnpEff {

	public static final String DEFAULT_PDB_DIR = "db/pdb";
	public static final String DEFAULT_ID_MAP_FILE = DEFAULT_PDB_DIR + "/idMap_pdbId_ensemblId_refseqId.txt";
	public static final String PDB_EXT = ".ent";
	public static final String PDB_EXT_GZ = ".ent.gz";
	public static final String[] PDB_EXTS = { PDB_EXT_GZ, PDB_EXT };
	public static final String PROTEIN_INTERACTION_FILE = "interactions.bin";

	public static final double DEFAULT_DISTANCE_THRESHOLD = 3.0; // Maximum distance to be considered 'in contact'
	public static final double DEFAULT_MAX_MISMATCH_RATE = 0.1;
	public static final int DEFAULT_PDB_MIN_AA_SEPARATION = 20; // Number of AA of distance within a sequence to consider them for distance analysis
	public static final String DEFAULT_PDB_ORGANISM_COMMON = "HUMAN"; // PDB organism
	public static final double DEFAULT_PDB_RESOLUTION = 3.0; // PDB file resolution (in Angstrom)

	public static final ArrayList<DistanceResult> EMPTY_DISTANCES = new ArrayList<>();

	String idMapFile = DEFAULT_ID_MAP_FILE;
	String pdbDir = DEFAULT_PDB_DIR;
	String pdbOrganismCommon = DEFAULT_PDB_ORGANISM_COMMON; // PDB organism "common name"
	double pdbResolution = DEFAULT_PDB_RESOLUTION; // PDB file resolution (in Angstrom)
	double maxMismatchRate = DEFAULT_MAX_MISMATCH_RATE;
	double distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;
	double distanceThresholdNon = Double.POSITIVE_INFINITY; // Distance threshold for 'not in contact'
	int aaMinSeparation = DEFAULT_PDB_MIN_AA_SEPARATION;
	int countFilesPass, countMapError, countMapOk;
	IdMapper idMapper;
	IdMapper idMapperConfirmed;
	PDBFileReader pdbreader;
	Map<String, Transcript> trancriptById;
	Collection<String> pdbFileNames;
	BufferedWriter outpufFile;

	public SnpEffCmdPdb() {
	}

	/**
	 * Get all AAs in a chain
	 */
	List<AminoAcid> aminoAcids(Chain chain) {
		ArrayList<AminoAcid> aas = new ArrayList<AminoAcid>();
		for (Group group : chain.getAtomGroups())
			if (group instanceof AminoAcid) aas.add((AminoAcid) group);
		return aas;
	}

	/**
	 * Check that protein sequences form PDB (pdbFile) matches sequences from Genome
	 * Return a list of maps that are confirmed (i.e. AA sequence matches between transcript and PDB)
	 */
	List<IdMapperEntry> checkSequencePdbGenome(Structure pdbStruct, Set<String> trIds) {
		// Check idMaps. Only return those that match
		ArrayList<IdMapperEntry> list = new ArrayList<IdMapperEntry>();
		for (String trId : trIds)
			if (filterTranscript(trId)) {
				list.addAll(checkSequencePdbGenome(pdbStruct, trId));
			}

		return list;
	}

	/**
	 * Check that protein sequences match between PDB and Genome
	 * Return a list of maps that are confirmed (i.e. AA sequence matches between transcript and PDB)
	 * Note: Only part of the sequence usually matches (PDB chains shorter than the transcript)
	 */
	List<IdMapperEntry> checkSequencePdbGenome(Structure pdbStruct, String trId) {
		String pdbId = pdbStruct.getPDBCode();
		if (debug) System.err.println("\nChecking '" + trId + "'\t<->\t'" + pdbStruct.getPDBCode() + "'");
		List<IdMapperEntry> idmapsOri = idMapper.getByPdbId(pdbId);
		List<IdMapperEntry> idmapsNew = new ArrayList<>();

		// Transcript
		Transcript tr = trancriptById.get(trId);
		String prot = tr.protein();
		if (debug) System.err.println("\tTranscript ID: " + tr.getId() + "\tProtein [" + prot.length() + "]: " + prot);

		// Compare to PDB structure
		for (Chain chain : pdbStruct.getChains()) {
			// Chain passes criteria?
			if (!filterPdbChain(chain)) continue;

			// Compare sequence to each AA-Chain
			StringBuilder sb = new StringBuilder();
			int countMatch = 0, countMismatch = 0;

			// Count differences
			for (Group group : chain.getAtomGroups())
				if (group instanceof AminoAcid) {
					AminoAcid aa = (AminoAcid) group;
					int aaPos = aa.getResidueNumber().getSeqNum() - 1;
					if (aaPos < 0) continue; // I don't know why some PDB coordinates are negative...

					char aaLetter = aa.getChemComp().getOne_letter_code().charAt(0);
					if (prot.length() > aaPos) {
						char trAaLetter = prot.charAt(aaPos);
						if (aaLetter == trAaLetter) countMatch++;
						else countMismatch++;
					} else countMismatch++;
					sb.append(aa.getChemComp().getOne_letter_code());
				}

			// Only use mappings that have low error rate
			if (countMatch + countMismatch > 0) {
				double err = countMismatch / ((double) (countMatch + countMismatch));
				if (debug) Gpr.debug("\tChain: " + chain.getChainID() + "\terror: " + err + "\t" + sb);

				if (err < maxMismatchRate) {
					if (debug) Gpr.debug("\tMapping OK    :\t" + trId + "\terror: " + err);

					int trAaLen = tr.protein().length();
					int pdbAaLen = chain.getAtomGroups("amino").size();

					idmapsOri.stream() //
							.filter(idm -> trId.equals(idm.trId) && pdbId.equals(idm.pdbId)) //
							.findFirst() //
							.ifPresent(i -> idmapsNew.add(i.cloneAndSet(chain.getChainID(), pdbAaLen, trAaLen)));
				} else if (debug) Gpr.debug("\tMapping ERROR :\t" + trId + "\terror: " + err);
			}
		}

		// Show all confirmed mappings
		if (debug) idmapsNew.stream().forEach(i -> System.out.println(i));

		return idmapsNew;
	}

	void closeOuptut() {
		try {
			if (outpufFile != null) outpufFile.close();
			outpufFile = null;
		} catch (IOException e) {
			throw new RuntimeEOFException("Error closing output file", e);
		}
	}

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

	/**
	 * Distances within two amino acids within the same chain
	 */
	List<DistanceResult> distance(Chain chain, Transcript tr1, Transcript tr2) {
		ArrayList<DistanceResult> results = new ArrayList<>();
		List<AminoAcid> aas = aminoAcids(chain);

		for (int i = 0; i < aas.size(); i++) {
			int minj = i + aaMinSeparation;

			for (int j = minj; j < aas.size(); j++) {
				AminoAcid aa1 = aas.get(i);
				AminoAcid aa2 = aas.get(j);
				double d = distanceMin(aa1, aa2);

				if ((Double.isFinite(distanceThreshold) && d <= distanceThreshold) // Amino acids in close distance
						|| (Double.isFinite(distanceThresholdNon) && (d > distanceThresholdNon)) // Amino acids far apart
				) {
					DistanceResult dres = new DistanceResult(aa1, aa2, tr1, tr2, d);
					if (dres.hasValidCoords()) {
						results.add(dres);
						countMapOk++;
						if (debug) Gpr.debug(((d <= distanceThreshold) ? "AA_IN_CONTACT\t" : "AA_NOT_IN_CONTACT\t") + dres);
					} else {
						countMapError++;
					}
				}
			}
		}

		return results;
	}

	/**
	 * Distances within all chains in a structure
	 */
	List<DistanceResult> distance(Structure structure, Transcript tr1, Transcript tr2) {
		ArrayList<DistanceResult> results = new ArrayList<>();

		// Distance
		for (Chain chain : structure.getChains())
			if (filterPdbChain(chain)) {
				results.addAll(distance(chain, tr1, tr2));
			}

		return results;
	}

	/**
	 * Minimum distance between all atoms in two amino acids
	 */
	double distanceMin(AminoAcid aa1, AminoAcid aa2) {
		double distMin = Double.POSITIVE_INFINITY;

		for (Atom atom1 : aa1.getAtoms())
			for (Atom atom2 : aa2.getAtoms()) {
				try {
					double dist = Calc.getDistance(atom1, atom2);
					distMin = Math.min(distMin, dist);
				} catch (StructureException e) {
					throw new RuntimeException(e);
				}
			}

		return distMin;
	}

	/**
	 * Parse PDB id from PDB file name
	 */
	String fileName2PdbId(String pdbFileName) {
		String base = Gpr.baseName(pdbFileName);
		if (base.startsWith("pdb")) base = base.substring(3);
		base = Gpr.removeExt(base, PDB_EXTS);
		return base.toUpperCase();
	}

	/**
	 * Return true if the PDB structure passes the criteria
	 * I.e.: Resolution less or equal to desire one and species matches
	 */
	boolean filterPdb(Structure pdbStruct) {
		// Filter PDB structure
		// Within resolution limits? => Process
		double res = pdbStruct.getPDBHeader().getResolution();
		if (res > pdbResolution) {
			if (debug) Gpr.debug("PDB resolution is " + res + ", ignoring file");
			return false;
		}

		// Match organism (any chain)
		boolean ok = false;
		for (Chain chain : pdbStruct.getChains())
			ok |= filterPdbChain(chain);

		return ok;
	}

	/**
	 * Return true if the PDB's chain passes the criteria
	 * I.e.: Organism matches
	 */
	boolean filterPdbChain(Chain chain) {
		if (chain.getHeader() == null) return false;

		String orgs = chain.getHeader().getOrganismCommon();
		if (orgs == null) return false;

		// Multiple organisms?
		if (orgs.indexOf(' ') > 0) {
			for (String org : orgs.split("\\s"))
				if (org.equals(pdbOrganismCommon)) return true;

		}

		return orgs.equals(pdbOrganismCommon);
	}

	/**
	 * Return true if the transcript passes the criteria
	 * (i.e. the ID is present in 'trancriptById' map)
	 */
	boolean filterTranscript(String trId) {
		Transcript tr = trancriptById.get(trId);
		if (tr == null) {
			if (debug) Gpr.debug("Transcript '" + trId + "' not found in " + genomeVer + ".");
			return false;
		}

		return true;
	}

	Collection<String> findPdbFiles() {
		return findPdbFiles(new File(pdbDir));
	}

	/**
	 * Find all files (in any subdir) matching pdb entry extension
	 */
	Collection<String> findPdbFiles(File dir) {
		if (debug) Gpr.debug("Finding PDB files in directory: " + dir);
		List<String> list = new LinkedList<>();

		for (File f : dir.listFiles()) {
			String fileName = f.getName();
			if (f.isDirectory()) {
				list.addAll(findPdbFiles(f));
			} else if (f.isFile() && (fileName.endsWith(PDB_EXT) || fileName.endsWith(PDB_EXT_GZ))) {
				list.add(f.getAbsolutePath());
				if (debug) Gpr.debug("Found PDB file: " + f.getAbsolutePath());
			}
		}

		return list;
	}

	Set<String> findTranscriptIds(String pdbId) {
		// Get transcript IDs
		List<IdMapperEntry> idEntries = idMapper.getByPdbId(pdbId);
		Set<String> trIds = IdMapper.transcriptIds(idEntries);

		if (debug) {
			StringBuilder sb = new StringBuilder();
			sb.append("PDB ID: " + pdbId);
			sb.append("\tEntries:\n");
			if (idEntries != null) {
				idEntries.forEach(le -> sb.append("\t\t" + le + "\n"));
				sb.append("\tTranscripts:\t" + trIds + "\n");
			}
			Gpr.debug(sb);
		}

		return trIds;
	}

	/**
	 * Load all data
	 */
	public void initialize() {
		//---
		// Initialize SnpEff
		//---
		String argsSnpEff[] = { "eff", "-c", configFile, genomeVer };
		args = argsSnpEff;
		setGenomeVer(genomeVer);
		parseArgs(argsSnpEff);
		loadConfig();

		// Load SnpEff database
		if (genomeVer != null) {
			Timer.showStdErr("Loading SnpEff's database: " + genomeVer);
			loadDb();
			Timer.showStdErr("Done.");
		}

		// Initialize trancriptById
		trancriptById = new HashMap<>();
		for (Gene g : config.getSnpEffectPredictor().getGenome().getGenes())
			for (Transcript tr : g) {
				String id = tr.getId();
				if (id.indexOf('.') > 0) id = id.substring(0, id.indexOf('.')); // When using RefSeq transcripts, we don't store sub-version number

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

		//---
		// Initialize reader
		//---
		pdbreader = new PDBFileReader();
	}

	@Override
	public void load() {
		if (verbose) Timer.showStdErr("Loading id maps " + idMapFile);
		idMapper = new IdMapper();
		idMapper.setVerbose(verbose);
		idMapper.load(idMapFile);

		loadConfig(); // Read config file
		loadDb(); // Load database
	}

	/**
	 * Open output file
	 */
	void openOuptut(String fileName) {
		try {
			if (verbose) Timer.showStdErr("Saving results to database file '" + fileName + "'");
			outpufFile = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(fileName)))));
		} catch (IOException e) {
			throw new RuntimeEOFException("Error opening output file '" + fileName + "'", e);
		}
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args == null) return;

		this.args = args;

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			// Is it a command line option?
			// Note: Generic options (such as config, verbose, debug, quiet, etc.) are parsed by SnpEff class
			//---
			if (isOpt(arg)) {
				arg = arg.toLowerCase();

				switch (arg.toLowerCase()) {
				case "-aasep":
					if ((i + 1) < args.length) aaMinSeparation = Gpr.parseIntSafe(args[++i]);
					else usage("Missing parameter in '-aaSep'");
					break;

				case "-idmap":
					if ((i + 1) < args.length) idMapFile = args[++i];
					else usage("Missing parameter in '-idMap'");
					break;

				case "-maxdist":
					if ((i + 1) < args.length) distanceThreshold = Gpr.parseDoubleSafe(args[++i]);
					else usage("Missing parameter in '-maxDist'");
					break;

				case "-maxerr":
					if ((i + 1) < args.length) maxMismatchRate = Gpr.parseDoubleSafe(args[++i]);
					else usage("Missing parameter: '-maxErr'");
					break;

				case "-org":
					if ((i + 1) < args.length) pdbOrganismCommon = args[++i].toUpperCase();
					else usage("Missing parameter in '-org'");
					break;

				case "-pdbdir":
					if ((i + 1) < args.length) idMapFile = args[++i];
					else usage("Missing parameter in '-pdbDir'");
					break;

				case "-res":
					if ((i + 1) < args.length) pdbResolution = Gpr.parseDoubleSafe(args[++i]);
					else usage("Missing parameter: '-res'");
					break;

				default:
					usage("Unknown option '" + arg + "'");
				}
			} else if (genomeVer == null || genomeVer.isEmpty()) genomeVer = arg;
		}

		//---
		// Sanity checks
		//---

		// Check: Do we have all required parameters?
		if (genomeVer == null || genomeVer.isEmpty()) usage("Missing genomer_version parameter");

		if (distanceThreshold <= 0) usage("Max distance in '-maxdist' command line option must be a positive number");
		if (maxMismatchRate <= 0) usage("Max mismatch rate in '-maxErr' command line option must be a positive number");
		if (pdbResolution <= 0) usage("Resoluton in '-res' command line option must be a positive number");
		if (aaMinSeparation <= 0) usage("Minimum separation in '-aaSep' command line option must be a positive, integer number");
	}

	/**
	 * PDB analysis
	 */
	public void pdb() {
		// Find all pdb files
		if (verbose) Timer.showStdErr("Finding PDB files");
		pdbFileNames = findPdbFiles();

		// Create transcript map
		createTranscriptMap();

		// Open output file (save to database)
		String outFile = config.getDirDataVersion() + "/" + PROTEIN_INTERACTION_FILE;
		openOuptut(outFile);

		// Map IDs and confirm that amino acid sequence matches (within certain error rate)
		if (verbose) Timer.showStdErr("Analyzing PDB sequences");
		pdbAnalysis();
		closeOuptut();
	}

	/**
	 * Check that protein sequences form PDB matches sequences from Genome
	 * Return an IdMapped of confirmed entries (i.e. AA sequence matches between transcript and PDB)
	 */
	protected void pdbAnalysis() {
		if (verbose) Timer.showStdErr("Analyzing PDB files");

		pdbFileNames.stream().forEach(pf -> pdbAnalysis(pf));

		if (verbose) Timer.showStdErr("Done." //
				+ "\n\tNumber of PDB files : " + pdbFileNames.size() //
				+ "\n\tPDB files analyzed  : " + countFilesPass //
				+ "\n\tAA 'in contact'     : " + countMapOk //
				+ "\n\tMapping errors      : " + countMapError //
		);
	}

	/**
	 * Analyze a PDB file
	 */
	protected void pdbAnalysis(String pdbFileName) {
		// Find transcript IDs
		String pdbId = fileName2PdbId(pdbFileName);
		Set<String> trIds = findTranscriptIds(pdbId);
		if (trIds == null || trIds.isEmpty()) {
			if (debug) Gpr.debug("No transcript IDs found for PDB entry '" + pdbId + "'");
			return;
		}

		// Read PDB structure
		Structure pdbStruct = readPdbFile(pdbFileName);
		if (pdbStruct == null || !filterPdb(pdbStruct)) return; // Passes filter?

		// Check that entries map to the genome
		countFilesPass++;
		List<IdMapperEntry> idMapConfirmed = checkSequencePdbGenome(pdbStruct, trIds);
		if (idMapConfirmed == null || idMapConfirmed.isEmpty()) return;

		// Calculate distances
		for (IdMapperEntry idmap : idMapConfirmed) {
			// Get full transcript ID including version (version numbers are removed in the IdMap)
			Transcript tr = trancriptById.get(idmap.trId);
			List<DistanceResult> dres = distance(pdbStruct, tr, tr);
			save(dres);
		}
	}

	/**
	 * Read and parse PDB file
	 */
	public Structure readPdbFile(String pdbFileName) {
		try {
			PdbFile pdbreader = new PdbFile();
			if (verbose) Timer.showStdErr("Reading PDB file: " + pdbFileName);
			return pdbreader.getStructure(pdbFileName);
		} catch (IOException e) {
			if (verbose) e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean run() {
		load();
		pdb();
		return true;
	}

	void save(List<DistanceResult> distResults) {
		for (DistanceResult d : distResults)
			try {
				outpufFile.write(d.toString());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

	public void setDistanceThresholdNon(double distanceThresholdNon) {
		this.distanceThresholdNon = distanceThresholdNon;
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
		System.err.println("Usage: snpEff pdb [options] genome_version");
		System.err.println("\n");
		System.err.println("\nOptions:");
		System.err.println("\t-aaSep <number>                 : Minimum number of AA of separation within the sequence. Default: " + aaMinSeparation);
		System.err.println("\t-idMap <file>                   : ID map file (i.e. file containing mapping from PDB ID to transcript ID).");
		System.err.println("\t-maxDist <number>               : Maximum distance for a pair of amino acids to be considered 'in contact'. Default: " + distanceThreshold);
		System.err.println("\t-maxErr <number>                : Maximum amino acid sequence differece between PDB file and genome. Default: " + maxMismatchRate);
		System.err.println("\t-org <name>                     : Organism 'common name'. Default: " + pdbOrganismCommon);
		System.err.println("\t-pdbDir <path>                  : Path to PDB files (files in all sub-dirs are scanned).");
		System.err.println("\t-res <number>                   : Maximum PDB file resolution. Default: " + pdbResolution);

		usageGenericAndDb();

		System.exit(-1);
	}

}
