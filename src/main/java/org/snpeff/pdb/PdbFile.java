package org.snpeff.pdb;

import org.biojava.nbio.core.util.InputStreamProvider;
import org.biojava.nbio.structure.*;
import org.biojava.nbio.structure.align.util.UserConfiguration;
import org.biojava.nbio.structure.io.FileParsingParameters;
import org.biojava.nbio.structure.io.PDBFileParser;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.snpeff.pdb.PdbUtil.aminoAcids;
import static org.snpeff.pdb.PdbUtil.distanceMin;

/**
 * A structure that reads PDB files
 * <p>
 * This code is similar to 'PDBFileReader' from BioJava, but the BioJava version
 * doesn't close file descriptors and eventually crashes when reading many files.
 *
 * @author pcingola
 */
public class PdbFile {

    public static final String PDB_ENT_EXT = ".pdb";
    public static final String PDB_ENT_EXT_GZ = ".pdb.gz";
    public static final String PDB_EXT = ".ent";
    public static final String PDB_EXT_GZ = ".ent.gz";
    public static final String[] PDB_EXTS = {PDB_ENT_EXT_GZ, PDB_ENT_EXT, PDB_EXT_GZ, PDB_EXT};
    public static final double PDB_RESOLUTION_UNSET = 99.0; // Resolution value set by PDB parser when the parameter is missing
    public static final String UNIPROT_DATABASE = "UNP";

    boolean debug, verbose;
    String fileName;
    File file;
    String proteinId;
    ProteinInteractions proteinInteractions;
    Structure pdbStructure;
    Set<String> trIds;

    public PdbFile(ProteinInteractions proteinInteractions, String fileName) {
        this.proteinInteractions = proteinInteractions;
        this.fileName = fileName;
        this.debug = proteinInteractions.isDebug();
        this.verbose = proteinInteractions.isVerbose();
    }

    /**
     * Return true if <code>s1</code> is not null and contains <code>s2</code>.
     */
    private static boolean contains(String s1, String s2) {
        return s1 != null && s1.contains(s2);
    }

    /**
     * Get Uniprot IDs from PDB structure
     */
    Map<String, String> chainUniprotIds(Structure pdbStruct) {
        Map<String, String> chain2uniproId = new HashMap<>();
        for (DBRef dbref : pdbStruct.getDBRefs()) {
            if (debug)
                Log.debug("PDB_DBREF\tchain:" + dbref.getChainName() + "\tdb: " + dbref.getDatabase() + "\tID: " + dbref.getDbAccession());
            if (dbref.getDatabase().equals(UNIPROT_DATABASE))
                chain2uniproId.put(dbref.getChainName(), dbref.getDbAccession());
        }
        return chain2uniproId;
    }

    /**
     * Check that protein sequences form PDB (pdbFile) matches sequences from Genome
     * Return a list of maps that are confirmed (i.e. AA sequence matches between
     * transcript and PDB)
     */
    List<IdMapperEntry> checkSequencePdbGenome() {
        // Check idMaps. Only return those that match
        ArrayList<IdMapperEntry> list = new ArrayList<>();
        for (String trId : trIds)
            if (proteinInteractions.filterTranscript(trId)) {
                list.addAll(checkSequencePdbGenome(trId));
            }

        return list;
    }

    /**
     * Check that protein sequences match between PDB and Genome Return a list of
     * maps that are confirmed (i.e. AA sequence matches between transcript and PDB)
     * Note: Only part of the sequence usually matches (PDB chains shorter than the
     * transcript)
     */
    List<IdMapperEntry> checkSequencePdbGenome(String trId) {
        if (debug) Log.debug("\nChecking '" + trId + "'\t<->\t'" + proteinId + "'");
        List<IdMapperEntry> idmapsOri = proteinInteractions.getByProteinId(proteinId);
        List<IdMapperEntry> idmapsNew = new ArrayList<>();

        // Compare each chain in the PDB structure
        for (Chain chain : pdbStructure.getChains())
            idmapsNew.addAll(checkSequencePdbGenome(chain, trId, idmapsOri));

        // Show all confirmed mappings
        if (debug) {
            for (IdMapperEntry ime : idmapsNew)
                Log.debug(ime);
        }

        return idmapsNew;
    }

    /**
     * Check that protein sequences match between a PDB 'chain' and the refrence
     * genome Return a list of maps that are confirmed (i.e. AA sequence matches
     * between transcript and PDB) Note: Only part of the sequence usually matches
     * (PDB chains shorter than the transcript)
     */
    List<IdMapperEntry> checkSequencePdbGenome(Chain chain, String trId, List<IdMapperEntry> idmapsOri) {
        List<IdMapperEntry> idmapsNew = new ArrayList<>();

        // Does chain pass filter criteria?
        if (!filterPdbChain(chain)) return idmapsNew;

        // Transcript
        Transcript tr = proteinInteractions.getTranscript(trId);
        String prot = tr.protein();
        if (debug) System.err.println("\tTranscript ID: " + tr.getId() + "\tProtein [" + prot.length() + "]: " + prot);

        // Compare sequence to each AA-Chain
        StringBuilder protSeq = new StringBuilder();
        StringBuilder trSeq = new StringBuilder();
        StringBuilder diff = new StringBuilder();
        int countMatch = 0, countMismatch = 0;

        // Count differences and create 'difference' string
        for (Group group : chain.getAtomGroups())
            if (group instanceof AminoAcid) {
                AminoAcid aa = (AminoAcid) group;
                int aaPos = aa.getResidueNumber().getSeqNum() - 1;
                if (aaPos < 0) continue; // I don't know why some PDB coordinates are negative...

                char aaLetter = aa.getChemComp().getOneLetterCode().charAt(0);
                if (prot.length() > aaPos) {
                    char trAaLetter = prot.charAt(aaPos);
                    trSeq.append(trAaLetter);
                    if (aaLetter == trAaLetter) {
                        countMatch++;
                        diff.append(' ');
                    } else {
                        countMismatch++;
                        diff.append('|');
                    }
                } else {
                    countMismatch++;
                }
                protSeq.append(aa.getChemComp().getOneLetterCode());
            }

        // Only use mappings that have low error rate
        if (countMatch + countMismatch > 0) {
            double err = countMismatch / ((double) (countMatch + countMismatch)); // Error rate: 1.0 means 100% difference (all AA in the chain differ from what we expect)
            if (debug) Log.debug("\tChain: " + chain.getId() + "\terror: " + err + "\n\t" //
                    + "protein    : " + protSeq + "\n\t" //
                    + " diff      : " + diff + "\n\t" //
                    + "transcript : " + trSeq //
            );

            if (err < proteinInteractions.getMaxMismatchRate()) {
                if (debug) Log.debug("\tMapping OK    :\t" + trId + "\terror: " + err);

                int trAaLen = tr.protein().length();
                int pdbAaLen = chain.getAtomGroups(GroupType.AMINOACID).size();

                for (IdMapperEntry idm : idmapsOri) {
                    if (trId.equals(idm.trId) && proteinId.equals(idm.proteinId)) {
                        idmapsNew.add(idm.cloneAndSet(chain.getId(), pdbAaLen, trAaLen));
                        break;
                    }
                }
            } else if (debug) Log.debug("\tMapping ERROR :\t" + trId + "\terror: " + err);
        }

        return idmapsNew;
    }

    /**
     * Parse ProteinId from PDB file name
     *
     * @returns: A string with a proteinId (PdbId or UniprotID) parsed fomr the file's name, or null if it cannot be parsed
     */
    public String fileName2ProteinId() {
        String base = Gpr.baseName(fileName);

        // PDB style file name, e.g. "pdb7daa.ent.gz"
        // The format is 'pdb' + PdbID + ".ent.gz"
        if (base.startsWith("pdb")) {
            base = base.substring(3);
            base = Gpr.removeExt(base, PDB_EXTS);
            return base.toUpperCase();
        }

        // ALphaFold style filename: AF-Q9Y6V0-F9-model_v2.pdb.gz
        // The format is 'AF-' + UniprotID + "-F" + foldNumber + "-model_v" + modelVersion + ".pdb.gz"
        if (base.startsWith("AF-")) {
            String[] segments = base.split("-");
            return segments[1].toUpperCase();
        }

        if (debug) Log.debug("Could not parse protein ID for file '" + fileName + "', base name '" + base + "'");
        return null;
    }


    /**
     * Return true if the PDB structure passes the filter criteria I.e.: Resolution
     * less or equal to desire one and species matches
     */
    boolean filterPdb() {
        // Within resolution limits? => Process
        double res = pdbStructure.getPDBHeader().getResolution();
        if (res > proteinInteractions.getPdbResolution() && res < PDB_RESOLUTION_UNSET) {
            if (debug) Log.debug("PDB resolution is " + res + ", ignoring file");
            return false;
        }

        // Match organism (any chain)
        boolean ok = false;
        for (Chain chain : pdbStructure.getChains())
            ok |= filterPdbChain(chain);

        return ok;
    }

    /**
     * Return true if the PDB's chain passes the criteria I.e.: Organism matches
     */
    boolean filterPdbChain(Chain chain) {
        // note: Compound is replaced by EntityInfo in biojava 5.x
        for (EntityInfo entityInfo : chain.getStructure().getEntityInfos()) {
            if (contains(entityInfo.getOrganismCommon(), proteinInteractions.getPdbOrganismCommon()) || contains(entityInfo.getOrganismScientific(), proteinInteractions.getPdbOrganismScientific())) {
                return true;
            }
        }
        return false;
    }


    /**
     * Analyze interacting sites in a pdb structure
     */
    List<DistanceResult> findInteractingCompound(Chain chain1, Chain chain2, String trId1, String trId2) {
        ArrayList<DistanceResult> results = new ArrayList<>();

        Transcript tr1 = proteinInteractions.getTranscript(trId1);
        Transcript tr2 = proteinInteractions.getTranscript(trId2);
        List<AminoAcid> aas1 = aminoAcids(chain1);
        List<AminoAcid> aas2 = aminoAcids(chain2);

        // Find between chain interactions
        for (AminoAcid aa1 : aas1) {
            for (AminoAcid aa2 : aas2) {
                double dmin = distanceMin(aa1, aa2);
                if (select(dmin)) {
                    DistanceResult dres = new DistanceResult(proteinId, aa1, aa2, tr1, tr2, dmin);
                    if (dres.hasValidCoords()) {
                        results.add(dres);
                        proteinInteractions.incCountMapOk();
                        if (debug)
                            Log.debug(((dmin <= proteinInteractions.getDistanceThreshold()) ? "AA_IN_CONTACT\t" : "AA_NOT_IN_CONTACT\t") + dres);
                    } else {
                        proteinInteractions.incCountMapError();
                    }
                }
            }
        }

        return results;
    }

    /**
     * Distances within all chains in a structure
     */
    List<DistanceResult> findInteractingSingle(Transcript tr) {
        ArrayList<DistanceResult> results = new ArrayList<>();

        // Distance
        for (Chain chain : pdbStructure.getChains())
            if (filterPdbChain(chain)) {
                results.addAll(findInteractingSingle(chain, tr));
            }

        return results;
    }


    /**
     * Find interacting AA within s same chain (i.e. two amino acids in close
     * proximity within the same chain)
     */
    List<DistanceResult> findInteractingSingle(Chain chain, Transcript tr) {
        ArrayList<DistanceResult> results = new ArrayList<>();
        List<AminoAcid> aas = aminoAcids(chain);

        for (int i = 0; i < aas.size(); i++) {
            int minj = i + proteinInteractions.getAaMinSeparation();

            for (int j = minj; j < aas.size(); j++) {
                AminoAcid aa1 = aas.get(i);
                AminoAcid aa2 = aas.get(j);
                double d = distanceMin(aa1, aa2);

                if (select(d)) {
                    DistanceResult dres = new DistanceResult(proteinId, aa1, aa2, tr, tr, d);
                    if (dres.hasValidCoords()) {
                        results.add(dres);
                        proteinInteractions.incCountMapOk();
                        if (debug)
                            Log.debug(((d <= proteinInteractions.getDistanceThreshold()) ? "AA_IN_CONTACT\t" : "AA_NOT_IN_CONTACT\t") + dres);
                    } else {
                        proteinInteractions.incCountMapError();
                    }
                }
            }
        }

        return results;
    }

    public String getProteinId() {
        Log.debug("DEBUG: " + pdbStructure.getPDBHeader().getPdbId());
        Log.debug("DEBUG: " + pdbStructure.getPDBHeader().getId());
        throw new RuntimeException("UNIMPLEMENTED");
    }

    /**
     * Filter IdMaps for a specific chain
     */
    List<IdMapperEntry> idMapChain(Chain chain, List<IdMapperEntry> idMaps) {
        List<IdMapperEntry> idMapChain = new ArrayList<>();
        for (IdMapperEntry idmap : idMaps) {
            if (idmap.proteinId.equals(proteinId) && idmap.pdbChainId.equals(chain.getId())) {
                idMapChain.add(idmap);
            }
        }

        return idMapChain;
    }

    boolean isCompound() {
        List<EntityInfo> compounds = pdbStructure.getEntityInfos();
        return compounds != null && !compounds.isEmpty();
    }


    /**
     * Opens filename, parses it and returns aStructure object .
     */
    public Structure load() throws IOException {
        if (verbose) Log.info("Reading PDB file: " + fileName);
        file = new File(fileName);

        // Set property to avoid "Illegal reflective access"
        //		WARNING: An illegal reflective access operation has occurred
        //		WARNING: Illegal reflective access by com.sun.xml.bind.v2.runtime.reflect.opt.Injector (file:/Users/kqrw311/.m2/repository/com/sun/xml/bind/jaxb-impl/2.3.0/jaxb-impl-2.3.0.jar) to method java.lang.ClassLoader.defineClass(java.lang.String,byte[],int,int)
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        // Setting system property so BioJava doesn't print 'PDB_DIR' missing warnings
        System.setProperty(UserConfiguration.PDB_DIR, file.getParent());

        InputStreamProvider isp = new InputStreamProvider();
        InputStream inStream = isp.getInputStream(file);
        PDBFileParser pdbpars = new PDBFileParser();
        FileParsingParameters params = new FileParsingParameters();
        pdbpars.setFileParsingParameters(params);

        // Read, parse and load file
        Structure structure = pdbpars.parsePDBFile(inStream);
        inStream.close();

        return structure;
    }

    /**
     * Analyze a PDB file
     */
    public void pdbAnalysis() throws IOException {
        // Get ProteinId from file name
        proteinId = fileName2ProteinId();
        if (proteinId == null) return;

        if (verbose) Log.info("Analysing PDB file '" + fileName + "', protein ID '" + proteinId + "'");

        // Find transcript IDs
        trIds = proteinInteractions.findTranscriptIds(proteinId);
        if (trIds == null || trIds.isEmpty()) {
            if (debug) Log.debug("No transcript IDs found for PDB entry '" + proteinId + "'");
            return;
        }

        // Read PDB structure
        pdbStructure = load();
        if (pdbStructure == null || !filterPdb()) return; // Passes filter?

        // Single protein analysis
        pdbAnalysisSingle();

        // Compound protein analysis
        if (isCompound()) pdbAnalysisCompound();
    }

    /**
     * Interaction analysis of PDB compounds (co-crystalized molecules)
     */
    void pdbAnalysisCompound() {
        proteinInteractions.incCountFilesPass();
        List<IdMapperEntry> idMapConfirmed = checkSequencePdbGenome();
        if (idMapConfirmed == null || idMapConfirmed.isEmpty()) return;

        // Get uniprot references
        Map<String, String> chain2uniproId = chainUniprotIds(pdbStructure);

        // Analyze distance between amino acids in different chains
        for (Chain chain1 : pdbStructure.getChains()) {

            String chainId1 = chain1.getId();
            List<IdMapperEntry> idMapChain1 = idMapChain(chain1, idMapConfirmed);
            if (idMapChain1.isEmpty()) {
                if (debug) Log.debug("Empty maps for chain '" + chainId1 + "'");
                continue;
            }

            for (Chain chain2 : pdbStructure.getChains()) {
                String chainId2 = chain2.getId();
                if (chainId1.compareTo(chainId2) >= 0) continue; // Only calculate once

                // Compare UNIPROT IDs
                String uniprot1 = chain2uniproId.get(chainId1);
                String uniprot2 = chain2uniproId.get(chainId2);
                if (uniprot1 != null && uniprot2 != null && uniprot1.equals(uniprot2)) {
                    if (debug) Log.debug("Filtering out two chains with same UNIPROT ID: '" + uniprot1);
                    continue;
                }

                List<IdMapperEntry> idMapChain2 = idMapChain(chain2, idMapConfirmed);
                if (idMapChain2.isEmpty()) {
                    if (debug) Log.debug("Empty maps for chain '" + chainId2 + "'");
                    continue;
                }

                // Find interactions for each transcript pair
                for (IdMapperEntry im1 : idMapChain1) {
                    for (IdMapperEntry im2 : idMapChain2) {
                        // Don't analyze same transcript (this is done in pdbAnalysisCompoundSingle)
                        if (!im1.trId.equals(im2.trId)) {
                            List<DistanceResult> dres = findInteractingCompound(chain1, chain2, im1.trId, im2.trId);
                            proteinInteractions.save(dres);
                        }
                    }
                }
            }
        }
    }

    /**
     * Interaction analysis of PDB molecules (within molecule interactions)
     */
    void pdbAnalysisSingle() {
        if (debug) Log.debug("Protein structure analysis for " + pdbStructure.getIdentifier());
        // Check that entries map to the genome
        proteinInteractions.incCountFilesPass();
        List<IdMapperEntry> idMapConfirmed = checkSequencePdbGenome();
        if (idMapConfirmed == null || idMapConfirmed.isEmpty()) return;

        // Calculate distances
        for (IdMapperEntry idmap : idMapConfirmed) {
            // Get full transcript ID including version (version numbers are removed in the IdMap)
            Transcript tr = proteinInteractions.getTranscript(idmap.trId);
            List<DistanceResult> dres = findInteractingSingle(tr);
            proteinInteractions.save(dres);
        }
    }

    /**
     * Should this pair of amino acids be selected?
     */
    boolean select(double d) {
        if (!Double.isInfinite(proteinInteractions.getDistanceThreshold()))
            return d <= proteinInteractions.getDistanceThreshold(); // Amino acids in close distance
        if (!Double.isInfinite(proteinInteractions.getDistanceThresholdNon()))
            return d > proteinInteractions.getDistanceThresholdNon();// Amino acids far apart
        throw new RuntimeException("Neither distance is finite!");
    }
}
