package org.snpeff.geneSets;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.geneOntology.GoTerm;
import org.snpeff.geneOntology.GoTerms;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * A collection of GeneSets
 *
 * Genes have associated "experimental values"
 *
 * @author Pablo Cingolani
 */
public class GeneSets implements Iterable<GeneSet>, Serializable {

	private static final long serialVersionUID = -359594418467719013L;
	public static boolean debug = false; // Debug mode for this class?
	public static double LOG2 = Math.log(2); // We use this constant often
	public static long PRINT_SOMETHING_TIME = 5000; // Print something every X seconds
	static int warnCount = 0;

	boolean verbose = false; // Verbose mode for this class?
	boolean doNotAddIfNotInGeneSet = false; // Do not add genes that don't belong to geneset
	String label; // Label, or name, for this GeneSet (e.g. "mSigDb.C2", or "GO")
	HashSet<String> genes; // All genes in this experiment
	HashMap<String, GeneSet> geneSetsByName; // Gene sets indexed by GeneSet.name
	HashMap<String, HashSet<GeneSet>> geneSetsByGene; // Gene sets indexed by gene name
	HashSet<String> interestingGenes; // Interesting genes in this experiment
	HashMap<String, Double> valueByGene;

	/**
	 * Create gene sets form GoTerms
	 * @param goTerms : GoTerms to use
	 */
	public static GeneSets factory(GoTerms goTerms) {
		GeneSets geneSets = new GeneSets();

		for (GoTerm gt : goTerms) {
			// Create gene set
			GeneSet geneSet = new GeneSet(gt.getAcc(), gt.getDescription(), geneSets);

			// Add all genes
			for (String id : gt)
				geneSet.addGene(id);

			// Add to Gene Sets
			geneSets.add(geneSet);
		}

		return geneSets;
	}

	/**
	 * Default constructor
	 */
	public GeneSets() {
		init();
	}

	public GeneSets(GeneSets geneSets) {
		init();
		copy(geneSets);
	}

	public GeneSets(String msigDb) {
		init();
		loadMSigDb(msigDb, false);
	}

	/**
	 * Add a gene set
	 * @param geneSetName
	 * @param geneSet
	 */
	public void add(GeneSet geneSet) {
		// Add
		geneSetsByName.put(geneSet.getName().toUpperCase(), geneSet);

		// Add all genes
		for (String gene : geneSet)
			add(gene, geneSet);

		geneSet.setGeneSets(this);
	}

	/**
	 * Add a gene and aliases
	 */
	public boolean add(String gene) {
		return genes.add(gene);
	}

	/**
	 * Add a gene and it's corresponding gene set
	 * @param gene
	 * @param geneSet
	 * @return
	 */
	public boolean add(String gene, GeneSet geneSet) {
		HashSet<GeneSet> listgs = geneSetsByGene.get(gene);
		if (listgs == null) {
			listgs = new HashSet<GeneSet>();
			geneSetsByGene.put(gene, listgs);
		}
		listgs.add(geneSet);

		return genes.add(gene);
	}

	/**
	 * Add a symbol as 'interesting' gene (to every corresponding GeneSet in this collection)
	 * @returns : true if it was added OK, false on error.
	 */
	public boolean addInteresting(String gene) {
		boolean ok = true;

		// Sanity check
		if (!genes.contains(gene)) {
			if (debug) System.err.println("WARNING: Trying to add ranked gene. Gene  '" + gene + "' does not exist in GeneSets. " + (doNotAddIfNotInGeneSet ? "Ignored." : "Added anyway."));
			ok = false;
			if (doNotAddIfNotInGeneSet) return ok;
			add(gene);
		}

		interestingGenes.add(gene);
		return ok;
	}

	/**
	 * Checks that every symboolID is in the set (as 'interesting' genes)
	 * @param intGenes : A set of interesting genes
	 * Throws an exception on error
	 */
	public void checkInterestingGenes(Set<String> intGenes) {
		if (debug) Log.debug("Checking genes (" + intGenes.size() + ") : " + intGenes);

		// Check that genes contains interestingGenes
		if (!intGenes.containsAll(interestingGenes)) { throw new RuntimeException("Not every gene in :" + label + " as an interesting symbol"); }

		// Check that every interesting symbol in DAG is from genes
		if (!interestingGenes.containsAll(intGenes)) { throw new RuntimeException("Not every gene marked as interesting in " + label + " is from intGenes\n\tInteresting genes(" + interestingGenes.size() + "): " + interestingGenes + "\n\tintGenes(" + intGenes.size() + "): " + intGenes); }
	}

	/**
	 * Copy all data from geneSets
	 * @param geneSets
	 */
	protected void copy(GeneSets geneSets) {
		interestingGenes.addAll(geneSets.interestingGenes);
		genes.addAll(geneSets.genes);
		valueByGene.putAll(geneSets.valueByGene);
		geneSetsByName.putAll(geneSets.geneSetsByName);
		geneSetsByGene.putAll(geneSets.geneSetsByGene);

		// Set 'this' as the new GeneSets
		for (GeneSet gs : geneSetsByName.values())
			gs.setGeneSets(this);
	}

	/**
	 * Produce a GeneSet based on a list of GeneSets and a 'mask'
	 *
	 * @param geneSetList : A list of GeneSets
	 * @param activeSets : An integer (binary mask) that specifies weather a set in the list should be taken into account or not. The operation performed is:
	 *
	 * 		Intersection{ GeneSets where mask_bit == 1 } - Union{ GeneSets where mask_bit == 0 } )
	 *
	 * where the minus sign '-' is actually a 'set minus' operation. This operation is done for both sets
	 * in GeneSet (i.e. genes and interestingGenes)
	 *
	 * @return A GeneSet
	 */
	public GeneSet disjointSet(List<GeneSet> geneSetList, int activeSets) {
		//---
		// Produce intersections (for each term in the list)
		//---
		GeneSet gtUnion = new GeneSet("UNION", "UNION", null);
		GeneSet gtIntersect = new GeneSet("INTERSECTION", "INTERSECTION", null);

		int i = 0;
		boolean firstIntersection = true;
		for (GeneSet geneSet : geneSetList) {
			// Extract the i_th bit from 'activeSets'
			boolean biti = (activeSets & (1L << i)) > 0;

			if (biti) { // Is this bit is 1? => perform an intersection

				if (firstIntersection) { // Initialize intersection set (otherwise all intersections are empty)
					gtIntersect.union(geneSet);
					firstIntersection = false;
				} else {
					gtIntersect.intersection(geneSet);
					// Are we done? (if the intersection set is empty, it doesn't make any sense to continue
					if (gtIntersect.getGeneCount() <= 0) return gtIntersect;
				}
			} else gtUnion.union(geneSet);

			i++;
		}

		// Now extract the 'union' set from the intersection set (i.e. perform a 'set minus' operation)
		gtIntersect.setMinus(gtUnion);

		return gtIntersect;
	}

	/**
	 * Iterate through each GeneSet in this GeneSets
	 */
	public List<GeneSet> geneSetsSorted() {
		LinkedList<GeneSet> ll = new LinkedList<GeneSet>(geneSetsByName.values());
		Collections.sort(ll);
		return ll;
	}

	/**
	 * Gene sets sorted by size (if same size, sort by name).
	 * @param reverse : Reverse size sorting (does not affect name sorting)
	 * @return
	 */
	public List<GeneSet> geneSetsSortedSize(final boolean reverse) {
		ArrayList<GeneSet> ll = new ArrayList<GeneSet>(geneSetsByName.values());
		Collections.sort(ll, new Comparator<GeneSet>() {

			@Override
			public int compare(GeneSet gs1, GeneSet gs2) {
				// Compare by size
				int diff = gs1.size() - gs2.size();
				if (diff != 0) return (reverse ? -diff : diff);

				// Same size? Sozr by name
				return gs1.getName().compareTo(gs2.getName());
			}
		});
		return ll;
	}

	/**
	 * How many genes do we have?
	 * @return
	 */
	public int getGeneCount() {
		if (genes == null) { return 0; }
		return genes.size();
	}

	/**
	 * Get all genes in this set
	 * @return
	 */
	public Set<String> getGenes() {
		return genes;
	}

	/**
	 * Get a gene set named 'geneSetName'
	 * @param geneSetName
	 * @return
	 */
	public GeneSet getGeneSet(String geneSetName) {
		return geneSetsByName.get(geneSetName.toUpperCase());
	}

	/**
	 * Get number of gene sets
	 * @return
	 */
	public int getGeneSetCount() {
		if (geneSetsByName == null) { return 0; }
		return geneSetsByName.size();
	}

	/**
	 * All gene sets that this gene belongs to
	 * @param gene
	 * @return
	 */
	public HashSet<GeneSet> getGeneSetsByGene(String gene) {
		return geneSetsByGene.get(gene);
	}

	public HashMap<String, GeneSet> getGeneSetsByName() {
		return geneSetsByName;
	}

	public HashSet<String> getInterestingGenes() {
		return interestingGenes;
	}

	public int getInterestingGenesCount() {
		return interestingGenes.size();
	}

	public String getLabel() {
		return label;
	}

	/**
	 * Get experimental value
	 * @param gene
	 * @return
	 */
	public double getValue(String gene) {
		Double val = valueByGene.get(gene);
		if (val == null) return 0;
		return val;
	}

	public HashMap<String, Double> getValueByGene() {
		return valueByGene;
	}

	public boolean hasGene(String geneId) {
		return genes.contains(geneId);
	}

	public boolean hasValue(String gene) {
		return valueByGene.containsKey(gene);
	}

	void init() {
		interestingGenes = new HashSet<String>();
		valueByGene = new HashMap<String, Double>();
		geneSetsByName = new HashMap<String, GeneSet>();
		genes = new HashSet<String>();
		geneSetsByGene = new HashMap<String, HashSet<GeneSet>>();
	}

	public boolean isInteresting(String geneName) {
		return interestingGenes.contains(geneName);
	}

	public boolean isRanked() {
		return false;
	}

	/**
	 * Is this gene set used? I.e. is there at least one gene 'used'? (e.g. interesting or ranked)
	 * @param gs
	 * @return
	 */
	protected boolean isUsed(GeneSet gs) {
		for (String gene : gs) {
			if (isUsed(gene)) return true;
		}
		return false;
	}

	protected boolean isUsed(String geneName) {
		return isInteresting(geneName);
	}

	/**
	 * Iterate through each GeneSet in this GeneSets
	 */
	@Override
	public Iterator<GeneSet> iterator() {
		return geneSetsByName.values().iterator();
	}

	/**
	 * Iterate through each GeneSet in this GeneSets
	 */
	public Iterator<GeneSet> iteratorSorted() {
		return geneSetsSorted().iterator();

	}

	public Set<String> keySet() {
		return geneSetsByName.keySet();
	}

	/**
	 * Select a number of GeneSets
	 * @param numberToSelect
	 * @return
	 */
	public List<GeneSet> listTopTerms(int numberToSelect) {
		LinkedList<GeneSet> list = new LinkedList<GeneSet>();

		// Create a list of terms (to be ordered)
		int i = 0;
		LinkedList<GeneSet> ll = new LinkedList<GeneSet>();
		for (String geneSetName : keySet())
			ll.add(getGeneSet(geneSetName));

		Collections.sort(ll);

		for (GeneSet geneSet : ll)
			if (i++ < numberToSelect) list.add(geneSet);

		return list;
	}

	/**
	 * Reads a file with a list of genes and experimental values.
	 * Format: "gene \t value \n"
	 * @param fileName
	 * @return A list of genes not found
	 */
	public List<String> loadExperimentalValues(String fileName, boolean maskException) {
		LinkedList<String> notFound = new LinkedList<String>();

		if (verbose) Log.info("Reading 'ranked' genes from file: '" + fileName + "'");

		// First: Initialize
		reset();

		//---
		// Read genes and values
		//---

		// Read file
		try {
			// Open file and initialize buffers
			BufferedReader inFile = new BufferedReader(new FileReader(fileName));
			String line;

			// Read each line and add it to 'valueByGene'
			while ((line = inFile.readLine()) != null) {
				// line = line.trim();
				if (!line.startsWith("#")) { // Skip comments

					String fields[] = line.split("\t");
					String gene = fields[0];
					if (!gene.isEmpty()) {
						Double value = Double.parseDouble(fields[1]);
						setValue(gene, value);

						// Is this gene in this gene set collection
						if (!genes.contains(gene)) notFound.add(gene);
					}
				}
			}

			// OK, finished
			inFile.close();
		} catch (IOException e) {
			if (maskException) { return null; }
			throw new RuntimeException(e);
		}

		return notFound;
	}

	/**
	 * Read an MSigDBfile and add every Gene set (do not add relationships between nodes in DAG)
	 * @param gmtFile
	 * @param geneSetType
	 */
	public boolean loadMSigDb(String gmtFile, boolean maskException) {
		try {
			if (verbose) Log.info("Reading gene sets file: '" + gmtFile + "'");

			genes = new HashSet<String>(); // Reset genes
			geneSetsByName = new HashMap<String, GeneSet>(); // and genesets by name

			// Open file and initialize buffers
			LineFileIterator lfi = new LineFileIterator(gmtFile);

			// Read each line
			for (String line : lfi) {
				line = line.trim();
				if (!line.startsWith("#")) { // Skip comments

					String fields[] = line.split("\t");

					// Parse name & description
					String geneSetName = fields[0];
					String description = fields[1];

					// Sanity check: Does gene set already exist?
					if (getGeneSet(geneSetName) != null) Log.debug("Error: File '" + gmtFile + "' line " + lfi.getLineNum() + ". Gene set name '" + geneSetName + "' duplicated.");

					// Create geneSet and all genes
					GeneSet gs = new GeneSet(geneSetName, description, this);
					for (int i = 2; i < fields.length; i++)
						gs.addGene(fields[i]);

					// Add gene set to collection
					add(gs);
				}
			}

			// OK, finished
			if (verbose) Log.info("GeneSets added: " + geneSetsByName.size());

		} catch (Exception e) {
			if (maskException) return false;
			throw new RuntimeException(e);
		}

		return true;
	}

	public void remove(GeneSet geneSet) {
		if (geneSet == null) return;
		geneSetsByName.remove(geneSet.getName());
	}

	/**
	 * Remove a GeneSet
	 */
	public void removeGeneSet(String geneSetName) {
		remove(getGeneSet(geneSetName));
	}

	/**
	 * Remove unused gene sets
	 */
	public void removeUnusedSets() {
		// Is it unused? => Remove
		LinkedList<GeneSet> todelete = new LinkedList<GeneSet>();
		for (GeneSet gs : this) {
			if (!isUsed(gs)) todelete.add(gs);
		}

		// Remove gene sets
		for (GeneSet gs : todelete)
			remove(gs);

		Log.debug("Removind unused gene sets:" //
				+ "\n\t\tTotal removed: " + todelete.size() //
				+ "\n\t\tRemaining: " + geneSetsByName.size() //
		);
	}

	/**
	 * Reset every 'interesting' gene or ranked gene (on every single GeneSet in this GeneSets)
	 */
	public void reset() {
		interestingGenes = new HashSet<String>();
		valueByGene = new HashMap<String, Double>();

		for (GeneSet gt : this) {
			gt.reset();
		}
	}

	/**
	 * Save gene sets file for GSEA analysis
	 * Format specification: http://www.broad.mit.edu/cancer/software/gsea/wiki/index.php/Data_formats#GMT:_Gene_Matrix_Transposed_file_format_.28.2A.gmt.29
	 *
	 * @param fileName
	 */
	public void saveGseaGeneSets(String fileName) {
		// Create a string with all the data
		StringBuffer out = new StringBuffer();
		for (GeneSet gt : this) // Save GeneSet that have at least 1 gene
		{
			if (gt.getGenes().size() > 0) {
				out.append(gt.getName() + "\t" + gt.getName() + "\t");

				// Add all genes for this GeneSet
				for (String gene : gt.getGenes())
					out.append("gene_" + gene + "\t");
				out.append("\n");
			}
		}

		// Save it
		Gpr.toFile(fileName, out);
	}

	public void setDoNotAddIfNotInGeneSet(boolean doNotAddIfNotInGeneSet) {
		this.doNotAddIfNotInGeneSet = doNotAddIfNotInGeneSet;
	}

	public void setGeneSetByName(HashMap<String, GeneSet> geneSets) {
		geneSetsByName = geneSets;
	}

	public void setInterestingGenes(HashSet<String> interestingGenesIdSet) {
		interestingGenes = interestingGenesIdSet;
	}

	/**
	 * Set experimental value for this gene
	 * @param geneId
	 * @param value
	 */
	public void setValue(String geneId, double value) {
		valueByGene.put(geneId, value);
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		for (GeneSet gs : geneSetsSorted())
			sb.append(gs.toStringAll() + "\n");

		return sb.toString();
	}

	public Collection<GeneSet> values() {
		return geneSetsByName.values();
	}
}
