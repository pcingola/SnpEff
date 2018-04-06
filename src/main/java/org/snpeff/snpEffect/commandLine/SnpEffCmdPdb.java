package org.snpeff.snpEffect.commandLine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

import org.biojava.nbio.structure.AminoAcid;
import org.biojava.nbio.structure.Atom;
import org.biojava.nbio.structure.Calc;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.EntityInfo;
import org.biojava.nbio.structure.DBRef;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.GroupType;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.io.PDBFileReader;
import org.snpeff.SnpEff;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.pdb.DistanceResult;
import org.snpeff.pdb.IdMapper;
import org.snpeff.pdb.IdMapperEntry;
import org.snpeff.pdb.PdbFile;
import org.snpeff.util.Gpr;
import org.snpeff.util.Timer;

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
	public static final String DEFAULT_ID_MAP_FILE = DEFAULT_PDB_DIR + "/idMap_pdbId_ensemblId_refseqId.txt.gz";
	public static final String DEFAULT_INTERACT_FILE = DEFAULT_PDB_DIR + "/pdbCompoundLines.txt";
	public static final String PDB_EXT = ".ent";
	public static final String PDB_EXT_GZ = ".ent.gz";
	public static final String[] PDB_EXTS = { PDB_EXT_GZ, PDB_EXT };
	public static final String PROTEIN_INTERACTION_FILE = "interactions.bin";

	public static final String UNIPROT_DATABASE = "UNP";

	public static final double DEFAULT_DISTANCE_THRESHOLD = 3.0; // Maximum distance to be considered 'in contact'
	public static final double DEFAULT_MAX_MISMATCH_RATE = 0.1;
	public static final int DEFAULT_PDB_MIN_AA_SEPARATION = 20; // Number of AA of distance within a sequence to consider them for distance analysis
	public static final String DEFAULT_PDB_ORGANISM_COMMON = "HUMAN"; // PDB organism
	public static final String DEFAULT_PDB_ORGANISM_SCIENTIFIC = "HOMO SAPIENS";
	public static final double DEFAULT_PDB_RESOLUTION = 3.0; // PDB file resolution (in Angstrom)

	public static final ArrayList<DistanceResult> EMPTY_DISTANCES = new ArrayList<>();

	String idMapFile = DEFAULT_ID_MAP_FILE;
	String interactListFile = DEFAULT_ID_MAP_FILE;
	String pdbDir = DEFAULT_PDB_DIR;
	String pdbOrganismCommon = DEFAULT_PDB_ORGANISM_COMMON; // PDB organism "common name"
	String pdbOrganismScientific = DEFAULT_PDB_ORGANISM_SCIENTIFIC; // PDB organism "scientific name"
	double pdbResolution = DEFAULT_PDB_RESOLUTION; // PDB file resolution (in Angstrom)
	double maxMismatchRate = DEFAULT_MAX_MISMATCH_RATE;
	double distanceThreshold = DEFAULT_DISTANCE_THRESHOLD;
	double distanceThresholdNon = Double.POSITIVE_INFINITY; // Distance threshold for 'not in contact'
	int aaMinSeparation = DEFAULT_PDB_MIN_AA_SEPARATION;
	int countFilesPass, countMapError, countMapOk;
	IdMapper idMapper;
	IdMapper idMapperConfirmed;
	PDBFileReader pdbreader;
	Set<String> confirmedPdbChainsMappings; // A Set of all 'pbdId:chainId' mappings that have been confirmed with the reference genome
	Map<String, Transcript> trancriptById; // Transcripts by id (trId without version number)
	Collection<String> pdbFileNames; // PDB files to porcess
	BufferedWriter outpufFile;
	Set<String> saved;
	List<DistanceResult> distanceResults;

	public SnpEffCmdPdb() {
	}

	/**
	 * Get AA sequence
	 */
	String aaSequence(Chain chain) {
		// AA sequence
		List<AminoAcid> aas = aminoAcids(chain);
		StringBuilder sb = new StringBuilder();

		for (AminoAcid aa1 : aas)
			sb.append(aa1.getAminoType());

		return sb.toString();
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
	 * Get NIPROT IDs from PDB structure
	 */
	Map<String, String> chainUniprotIds(Structure pdbStruct) {
		Map<String, String> chain2uniproId = new HashMap<String, String>();
		for (DBRef dbref : pdbStruct.getDBRefs()) {
			if (debug) Gpr.debug("PDB_DBREF\tchain:" + dbref.getChainName() + "\tdb: " + dbref.getDatabase() + "\tID: " + dbref.getDbAccession());
			if (dbref.getDatabase().equals(UNIPROT_DATABASE)) chain2uniproId.put(dbref.getChainName(), dbref.getDbAccession());
		}
		return chain2uniproId;
	}

	/**
	 * Check that protein sequences match between a PDB 'chain' and the refrence genome
	 * Return a list of maps that are confirmed (i.e. AA sequence matches between transcript and PDB)
	 * Note: Only part of the sequence usually matches (PDB chains shorter than the transcript)
	 */
	List<IdMapperEntry> checkSequencePdbGenome(Structure pdbStruct, Chain chain, String trId, List<IdMapperEntry> idmapsOri) {
		List<IdMapperEntry> idmapsNew = new ArrayList<>();
		String pdbId = pdbStruct.getPDBCode();

		// Does chain pass filter criteria?
		if (!filterPdbChain(chain)) return idmapsNew;

		// Transcript
		Transcript tr = trancriptById.get(trId);
		String prot = tr.protein();
		if (debug) System.err.println("\tTranscript ID: " + tr.getId() + "\tProtein [" + prot.length() + "]: " + prot);

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
			if (debug) Gpr.debug("\tChain: " + chain.getId() + "\terror: " + err + "\t" + sb);

			if (err < maxMismatchRate) {
				if (debug) Gpr.debug("\tMapping OK    :\t" + trId + "\terror: " + err);

				int trAaLen = tr.protein().length();
				int pdbAaLen = chain.getAtomGroups(GroupType.AMINOACID).size();

				for (IdMapperEntry idm : idmapsOri) {
					if (trId.equals(idm.trId) && pdbId.equals(idm.pdbId)) {
						idmapsNew.add(idm.cloneAndSet(chain.getId(), pdbAaLen, trAaLen));
						break;
					}
				}
			} else if (debug) Gpr.debug("\tMapping ERROR :\t" + trId + "\terror: " + err);
		}

		return idmapsNew;
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

		// Compare each chain in the PDB structure
		for (Chain chain : pdbStruct.getChains())
			idmapsNew.addAll(checkSequencePdbGenome(pdbStruct, chain, trId, idmapsOri));

		// Show all confirmed mappings
		if (debug) {
			for (IdMapperEntry ime : idmapsNew)
				Gpr.debug(ime);
		}

		return idmapsNew;
	}

	void closeOuptut() {
		try {
			if (outpufFile != null) outpufFile.close();
			outpufFile = null;
			saved = null;
		} catch (IOException e) {
			throw new RuntimeEOFException("Error closing output file", e);
		}
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

	void deleteOuptut(String outputPdbFile) {
		File of = new File(outputPdbFile);
		of.delete();
	}

	/**
	 * Minimum distance between all atoms in two amino acids
	 */
	double distanceMin(AminoAcid aa1, AminoAcid aa2) {
		double distMin = Double.POSITIVE_INFINITY;

		for (Atom atom1 : aa1.getAtoms())
			for (Atom atom2 : aa2.getAtoms()) {
				double dist = Calc.getDistance(atom1, atom2);
				distMin = Math.min(distMin, dist);
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
	 * Return true if the PDB structure passes the filter criteria
	 * I.e.: Resolution less or equal to desire one and species matches
	 */
	boolean filterPdb(Structure pdbStruct) {
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
                // note: Compound is replaced by EntityInfo in biojava 5.x
                for (EntityInfo entityInfo : chain.getStructure().getEntityInfos()) {
                        if (contains(entityInfo.getOrganismCommon(), pdbOrganismCommon) ||
                            contains(entityInfo.getOrganismScientific(), pdbOrganismScientific)) {
                                return true;
                        }
                }
                return false;
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

	/**
	 * Analyze interacting sites in a pdb structure
	 */
	List<DistanceResult> findInteractingCompound(Structure pdbStruct, Chain chain1, Chain chain2, String trId1, String trId2) {
		ArrayList<DistanceResult> results = new ArrayList<>();

		Transcript tr1 = getTranscript(trId1);
		Transcript tr2 = getTranscript(trId2);
		List<AminoAcid> aas1 = aminoAcids(chain1);
		List<AminoAcid> aas2 = aminoAcids(chain2);

		// Find between chain interactions
		for (AminoAcid aa1 : aas1) {
			for (AminoAcid aa2 : aas2) {
				double dmin = distanceMin(aa1, aa2);
				if (select(dmin)) {
					DistanceResult dres = new DistanceResult(aa1, aa2, tr1, tr2, dmin);
					if (dres.hasValidCoords()) {
						results.add(dres);
						countMapOk++;
						if (debug) Gpr.debug(((dmin <= distanceThreshold) ? "AA_IN_CONTACT\t" : "AA_NOT_IN_CONTACT\t") + dres);
					} else {
						countMapError++;
					}
				}
			}
		}

		return results;
	}

	/**
	 * Find interacting AA within s same chain (i.e. two amino acids in close proximity within the same chain)
	 */
	List<DistanceResult> findInteractingSingle(Chain chain, Transcript tr) {
		ArrayList<DistanceResult> results = new ArrayList<>();
		List<AminoAcid> aas = aminoAcids(chain);

		for (int i = 0; i < aas.size(); i++) {
			int minj = i + aaMinSeparation;

			for (int j = minj; j < aas.size(); j++) {
				AminoAcid aa1 = aas.get(i);
				AminoAcid aa2 = aas.get(j);
				double d = distanceMin(aa1, aa2);

				if (select(d)) {
					DistanceResult dres = new DistanceResult(aa1, aa2, tr, tr, d);
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
	List<DistanceResult> findInteractingSingle(Structure structure, Transcript tr) {
		ArrayList<DistanceResult> results = new ArrayList<>();

		// Distance
		for (Chain chain : structure.getChains())
			if (filterPdbChain(chain)) {
				results.addAll(findInteractingSingle(chain, tr));
			}

		return results;
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

		if (!dir.isDirectory()) throw new RuntimeException("No such directory '" + dir + "'");

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
				for (IdMapperEntry ime : idEntries)
					sb.append("\t\t" + ime + "\n");
				sb.append("\tTranscripts:\t" + trIds + "\n");
			}
			Gpr.debug(sb);
		}

		return trIds;
	}

	public List<DistanceResult> getDistanceResults() {
		return distanceResults;
	}

	Transcript getTranscript(String trId) {
		return trancriptById.get(IdMapper.transcriptIdNoVersion(trId));
	}

	/**
	 * Filter IdMaps for a specific chain
	 */
	List<IdMapperEntry> idMapChain(Structure pdbStruct, Chain chain, List<IdMapperEntry> idMaps) {
		List<IdMapperEntry> idMapChain = new ArrayList<>();
		for (IdMapperEntry idmap : idMaps) {
			if (idmap.pdbId.equals(pdbStruct.getPDBCode()) //
					&& idmap.pdbChainId.equals(chain.getId()) //
			) {
				idMapChain.add(idmap);
			}
		}

		return idMapChain;
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

	boolean isCompound(Structure pdbStruct) {
		List<EntityInfo> compounds = pdbStruct.getEntityInfos();
		return compounds != null && !compounds.isEmpty();
	}

	public void loadIdMapper() {
		if (verbose) Timer.showStdErr("Loading id maps " + idMapFile);
		idMapper = new IdMapper();
		idMapper.setVerbose(verbose);
		idMapper.load(idMapFile);
	}

	/**
	 * Open output file
	 */
	void openOuptut(String outputPdbFile) {
		try {
			if (verbose) Timer.showStdErr("Saving results to database file '" + outputPdbFile + "'");
			outpufFile = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(new File(outputPdbFile)))));
			saved = new HashSet<String>();
		} catch (IOException e) {
			throw new RuntimeEOFException("Error opening output file '" + outputPdbFile + "'", e);
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

				case "-interactlist":
					if ((i + 1) < args.length) idMapFile = args[++i];
					else usage("Missing parameter in '-interactList'");
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

				case "-orgscientific":
					if ((i + 1) < args.length) pdbOrganismScientific = args[++i].toUpperCase();
					else usage("Missing parameter in '-orgScientific'");
					break;

				case "-pdbdir":
					if ((i + 1) < args.length) pdbDir = args[++i];
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

		for (String pdbFileName : pdbFileNames)
			pdbAnalysis(pdbFileName);

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
		// Get Pdb ID from file name
		String pdbId = fileName2PdbId(pdbFileName);

		// Find transcript IDs
		Set<String> trIds = findTranscriptIds(pdbId);
		if (trIds == null || trIds.isEmpty()) {
			if (debug) Gpr.debug("No transcript IDs found for PDB entry '" + pdbId + "'");
			return;
		}

		// Read PDB structure
		Structure pdbStruct = readPdbFile(pdbFileName);
		if (pdbStruct == null || !filterPdb(pdbStruct)) return; // Passes filter?

		// Single protein analysis
		pdbAnalysisSingle(pdbStruct, trIds);

		// Compound protein analysis
		if (isCompound(pdbStruct)) pdbAnalysisCompound(pdbStruct, trIds);
	}

	/**
	 * Interaction analysis of PDB compounds (co-crystalized molecules)
	 */
	void pdbAnalysisCompound(Structure pdbStruct, Set<String> trIds) {
		countFilesPass++;
		List<IdMapperEntry> idMapConfirmed = checkSequencePdbGenome(pdbStruct, trIds);
		if (idMapConfirmed == null || idMapConfirmed.isEmpty()) return;

		// Get uniprot references
		Map<String, String> chain2uniproId = chainUniprotIds(pdbStruct);

		// Analyze distance between amino acids in different chains
		for (Chain chain1 : pdbStruct.getChains()) {

			String chainId1 = chain1.getId();
			List<IdMapperEntry> idMapChain1 = idMapChain(pdbStruct, chain1, idMapConfirmed);
			if (idMapChain1.isEmpty()) {
				if (debug) Gpr.debug("Empty maps for chain '" + chainId1 + "'");
				continue;
			}

			for (Chain chain2 : pdbStruct.getChains()) {
				String chainId2 = chain2.getId();
				if (chainId1.compareTo(chainId2) >= 0) continue; // Only calculate once

				// Compare UNIPROT IDs
				String uniprot1 = chain2uniproId.get(chainId1);
				String uniprot2 = chain2uniproId.get(chainId2);
				if (uniprot1 != null && uniprot2 != null && uniprot1.equals(uniprot2)) {
					if (debug) Gpr.debug("Filtering out two chains with same UNIPROT ID: '" + uniprot1);
					continue;
				}

				List<IdMapperEntry> idMapChain2 = idMapChain(pdbStruct, chain2, idMapConfirmed);
				if (idMapChain2.isEmpty()) {
					if (debug) Gpr.debug("Empty maps for chain '" + chainId2 + "'");
					continue;
				}

				// Find interactions for each transcript pair
				for (IdMapperEntry im1 : idMapChain1) {
					for (IdMapperEntry im2 : idMapChain2) {
						// Don't analyze same transcript (this is done in pdbAnalysisCompoundSingle)
						if (!im1.trId.equals(im2.trId)) {
							List<DistanceResult> dres = findInteractingCompound(pdbStruct, chain1, chain2, im1.trId, im2.trId);
							save(dres);
						}
					}
				}
			}
		}
	}

	/**
	 * Interaction analysis of PDB molecules (within molecule interactions)
	 */
	void pdbAnalysisSingle(Structure pdbStruct, Set<String> trIds) {
		// Check that entries map to the genome
		countFilesPass++;
		List<IdMapperEntry> idMapConfirmed = checkSequencePdbGenome(pdbStruct, trIds);
		if (idMapConfirmed == null || idMapConfirmed.isEmpty()) return;

		// Calculate distances
		for (IdMapperEntry idmap : idMapConfirmed) {
			// Get full transcript ID including version (version numbers are removed in the IdMap)
			Transcript tr = getTranscript(idmap.trId);
			List<DistanceResult> dres = findInteractingSingle(pdbStruct, tr);
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
		loadIdMapper(); // Load ID map table
		loadConfig(); // Read config file

		// Open output file (save to database)
		// Note: We do this before opening the database, because it removes
		// old interaction files (we don't want to try to load stale or old
		// interactions).
		String outputPdbFile = config.getDirDataGenomeVersion() + "/" + PROTEIN_INTERACTION_FILE;
		deleteOuptut(outputPdbFile);

		loadDb(); // Load database

		// Run analysis
		openOuptut(outputPdbFile);
		pdb();

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
	void save(List<DistanceResult> distResults) {
		for (DistanceResult d : distResults)
			try {

				String dstr = d.toString();

				if (!saved.contains(dstr)) {
					outpufFile.write(dstr + "\n");
					saved.add(dstr);
					if (distanceResults != null) distanceResults.add(d);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	}

	/**
	 * Should this pair of amino acids be selected?
	 */
	boolean select(double d) {
		if (!Double.isInfinite(distanceThreshold)) return d <= distanceThreshold; // Amino acids in close distance
		if (!Double.isInfinite(distanceThresholdNon)) return d > distanceThresholdNon;// Amino acids far apart
		throw new RuntimeException("Neither distance is finite!");
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
		System.err.println("\t-idMap <file>                   : ID map file (i.e. file containing mapping from PDB ID to transcript ID). Default: " + idMapFile);
		System.err.println("\t-interactList <file>            : A file containing protein-protein interations (from PDB co-srystalzed structures). Default: " + interactListFile);
		System.err.println("\t-maxDist <number>               : Maximum distance in Angtrom for any atom in a pair of amino acids to be considered 'in contact'. Default: " + distanceThreshold);
		System.err.println("\t-maxErr <number>                : Maximum amino acid sequence differece between PDB file and genome. Default: " + maxMismatchRate);
		System.err.println("\t-org <name>                     : Organism 'common name'. Default: " + pdbOrganismCommon);
		System.err.println("\t-orgScientific <name>           : Organism 'scientific name'. Default: " + pdbOrganismScientific);
		System.err.println("\t-pdbDir <path>                  : Path to PDB files (files in all sub-dirs are scanned).");
		System.err.println("\t-res <number>                   : Maximum PDB file resolution. Default: " + pdbResolution);

		usageGenericAndDb();

		System.exit(-1);
	}


        /**
         * Return true if <code>s1</code> is not null and contains <code>s2</code>.
         *
         * @param s1 string
         * @param s2 string
         * @return true if <code>s1</code> is not null and contains <code>s2</code>
         */
        private static boolean contains(String s1, String s2) {
                return s1 != null && s1.indexOf(s2) >= 0;
        }
}
