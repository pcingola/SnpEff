package org.snpeff.geneOntology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * A collection of GO terms
 *
 * @author Pablo Cingolani
 *
 */
public class GoTerms implements Iterable<GoTerm>, Serializable {

	public static boolean debug = false; // Debug mode for this class?
	public static boolean verbose = false; // Verbose mode for this class?
	static int warnCount = 0;

	String label; // Label for this set of nodes
	int maxRank;
	String nameSpace;
	HashMap<String, GoTerm> goTermsByGoTermAcc; // Go terms indexed by GoTermName (all of them for a given ontology)
	HashMap<String, Set<GoTerm>> goTermsBySymbolId; // Go terms indexed by geneId
	HashSet<String> interestingSymbolIdsSet; // Interesting symbols for this experiment
	HashMap<String, Integer> rankSymbolId;

	/**
	 * Default constructor
	 */
	public GoTerms() {
		goTermsByGoTermAcc = new HashMap<String, GoTerm>(); // All go terms are hashed here (by GoTermName)
		goTermsBySymbolId = new HashMap<String, Set<GoTerm>>(); // All go terms are hashed here (by symbolId)
		interestingSymbolIdsSet = new HashSet<String>();
		rankSymbolId = new HashMap<String, Integer>();
		maxRank = 0;
	}

	/**
	 * Constructor
	 * @param oboFile : Path to OBO description file
	 * @param nameSpace: Can be 'null' for "all namespaces"
	 * @param interestingGenesFile : Path to a file containing a list of 'interesting' genes (one geneName per line)
	 * @param geneAssocFile : A file containing lines like: "GOterm \t gene_product_id \t gene_name \n"
	 */
	public GoTerms(String oboFile, String nameSpace, String interestingGenesFile, String geneAssocFile, boolean removeObsolete, boolean useGeneId) {
		goTermsByGoTermAcc = new HashMap<String, GoTerm>(); // All go terms are hashed here (by GoTermName)
		goTermsBySymbolId = new HashMap<String, Set<GoTerm>>(); // All go terms are hashed here (by GeneId)
		interestingSymbolIdsSet = new HashSet<String>();
		this.nameSpace = nameSpace;
		maxRank = 0;

		// Read go <-> genes file
		if (oboFile != null) readOboFile(oboFile, removeObsolete);
		if (geneAssocFile != null) readGeneAssocFile(geneAssocFile, useGeneId);
		if (interestingGenesFile != null) readInterestingSymbolIdsFile(interestingGenesFile);
	}

	/**
	 * Add a GOTerm (if not already in this GOTerms)
	 * WARNING: Creates 'fake' symbolNames based on symbolIds. This method is used mostly for testing / debugging
	 */
	public GoTerm add(GoTerm goTerm) {
		goTermsByGoTermAcc.put(goTerm.getAcc(), goTerm);

		for (String symbolId : goTerm.getSymbolIdSet())
			addSymbolId(goTerm, symbolId);

		return goTerm;
	}

	/**
	 * Add a symbol as 'interesting' symbol (to every corresponding GOTerm in this set)
	 * @param symbolId : Symbol's ID number
	 */
	private void addInterestingSymbol(String symbolId, HashSet<String> noGoTermFound) {
		// Add to list of 'interesting symbol'
		interestingSymbolIdsSet.add(symbolId);

		// Find GOTerms associated with this 'interesting symbol' (and add this symbols as 'interesting')
		Set<GoTerm> goTerms = getGoTermsBySymbolId(symbolId);
		if (goTerms != null) {
			for (GoTerm gt : goTerms)
				gt.addInterestingSymbolId(symbolId);
		} else if (noGoTermFound != null) noGoTermFound.add(symbolId);
		else Log.debug("No GOTerms related to SymbolId:" + symbolId + " were found in this DAG (" + label + ")");
	}

	/**
	 * Add a symbol as 'interesting' symbol (to every corresponding GOTerm in this set)
	 * @param symbolName : Symbol's name
	 * @param rank : symbol's rank
	 * @param noGoTermFound : Add symbol here if there are no GOTerms associated with this symbol
	 * @return 0 if 'ok', 1 if symbol's ID is not found
	 */
	public void addInterestingSymbol(String symbolId, int rank, HashSet<String> noGoTermFound) {
		rankSymbolId.put(symbolId, rank); // Add symbol -> rank pair
		addInterestingSymbol(symbolId, noGoTermFound); // Add symbol
		if (maxRank < rank) maxRank = rank;
	}

	/**
	 * Add a symbolId (as well as all needed mappings)
	 *
	 * @param goTermAcc
	 * @param symbolId
	 * @param symbolName
	 * @param goTermType
	 * @param description
	 *
	 * @return true if OK, false on error (GOTerm 'goTermAcc' not found)
	 */
	public boolean addSymbolId(GoTerm goTerm, String symbolId) {
		// Add symbolId to GOTerm
		goTerm.addSymbolId(symbolId);

		// Add symbolId -> Set<GOTerm> mapping (a symbolId identifies a set of goTerms)
		Set<GoTerm> termsSet = goTermsBySymbolId.get(symbolId);
		if (termsSet == null) { // Need to create a new list?
			termsSet = new HashSet<GoTerm>();
			goTermsBySymbolId.put(symbolId, termsSet);
		}
		termsSet.add(goTerm);

		// OK
		return true;
	}

	/**
	 * Use symbols for chids in DAG
	 * For every GOTerm, each child's symbols are added to the term
	 * so that root term contains every symbol and every interestingSymbol
	 */
	public void addSymbolsFromChilds() {
		for (GoTerm gt : this)
			gt.addSymbolsFromChilds(gt);
	}

	/**
	 * Create a set with all the symbols
	 */
	public Set<String> allSymbols() {
		HashSet<String> syms = new HashSet<String>();
		for (GoTerm gt : this)
			syms.addAll(gt.getSymbolIdSet());
		return syms;
	}

	/**
	 * Checks that every symboolID is in the set (as 'interesting' symbols)
	 * @param interestingSymbolIds : A set of interesting symbols
	 * Throws an exception on error
	 */
	public void checkInterestingSymbolIds(Set<String> interestingSymbolIds) {
		HashSet<String> issc = new HashSet<String>(); // Interesting symbols (sanity check)

		if (debug) Log.debug("Checking symbols (" + interestingSymbolIds.size() + ") : " + interestingSymbolIds);

		// Add every interesting symbol in the DAG
		for (GoTerm gt : this)
			issc.addAll(gt.getInterestingSymbolIdSet());

		// Check that symbolIds contains issc
		if (!interestingSymbolIds.containsAll(issc)) throw new RuntimeException("Not every term in symbolIds is marked in DAG:" + label + " as an interesting symbol");

		// Check that every interesting symbol in DAG is from symbolIds
		if (!issc.containsAll(interestingSymbolIds)) throw new RuntimeException("Not every term marked as interesting in DAG " + label + " is from symbolIds\n\tInteresting symbols(" + issc.size() + "): " + issc + "\n\tsymbolIds(" + interestingSymbolIds.size() + "): " + interestingSymbolIds);

		// Are ranks being used? => Check them
		if ((rankSymbolId != null) && (rankSymbolId.keySet().size() > 0)) {
			int maxRank = interestingSymbolIds.size();

			// Check that every rank is being used
			int ranksUsed[] = new int[maxRank + 1];
			for (int i = 0; i < maxRank; i++)
				ranksUsed[i] = 0;

			// Check that every interestingSymbolId is ranked (if ranks are being used)
			for (String symbolId : interestingSymbolIds) {
				Integer rank = rankSymbolId.get(symbolId);
				if ((rank == null) || (rank <= 0) || (rank > maxRank)) throw new RuntimeException("Invalid rank for symbolId:" + symbolId + ", rank:" + rank + "(should be [1," + maxRank + "]");
				ranksUsed[rank]++;
			}

			for (int rank = 1; rank < maxRank; rank++)
				if (ranksUsed[rank] != 1) throw new RuntimeException("Rank number " + rank + " is used " + ranksUsed[rank] + " times (should be used exactly 1 time)");
		}
	}

	/**
	 * Produce a GOTerm based on a list of GOTerms and a 'mask'
	 *
	 * @param goTermList : A list of GOTerms
	 * @param activeSets : An integer (binary mask) that specifies weather a set in the list should be taken into account or not. The operation performed is:
	 *
	 * 		Intersection{ GOTerms where mask_bit == 1 } - Union{ GOTerms where mask_bit == 0 } )
	 *
	 * where the minus sign '-' is actually a 'set minus' operation. This operation is done for both sets
	 * in GOTerm (i.e. symbolIds and interestingSymbolIds)
	 *
	 * @return A GOTerm
	 */
	public GoTerm disjointSet(List<GoTerm> goTermList, int activeSets) {
		//---
		// Produce intersections (for each term in the list)
		//---
		GoTerm gtUnion = new GoTerm("UNION", null, null, null);
		GoTerm gtIntersect = new GoTerm("INTERSECTION", null, null, null);

		int i = 0;
		boolean firstIntersection = true;
		for (GoTerm goTerm : goTermList) {
			// Extract the i_th bit from 'activeSets'
			boolean biti = (activeSets & (1L << i)) > 0;

			if (biti) { // Is this bit is 1? => perform an intersection
				if (firstIntersection) { // Initialize intersection set (otherwise all intersections are empty)
					gtIntersect.union(goTerm);
					firstIntersection = false;
				} else {
					gtIntersect.intersection(goTerm);
					// Are we done? (if the intersection set is empty, it doesn't make any sense to continue
					if (gtIntersect.getTotalCount() <= 0) return gtIntersect;
				}
			} else gtUnion.union(goTerm);
			i++;
		}

		// Now extract the 'union' set from the intersection set (i.e. perform a 'set minus' operation)
		gtIntersect.setMinus(gtUnion);

		return gtIntersect;
	}

	/**
	 * Find a GoTerm or create it (if not found)
	 * @param symbolId
	 * @return
	 */
	GoTerm findOrCreate(String symbolId) {
		GoTerm gt = getGoTerm(symbolId);
		if (gt == null) gt = new GoTerm(symbolId, this, nameSpace, "");
		goTermsByGoTermAcc.put(gt.getAcc(), gt);
		return gt;
	}

	public GoTerm getGoTerm(String goTermAcc) {
		return goTermsByGoTermAcc.get(goTermAcc);
	}

	public HashMap<String, GoTerm> getGoTermsByGoTermAcc() {
		return goTermsByGoTermAcc;
	}

	public HashMap<String, Set<GoTerm>> getGoTermsBySymbolId() {
		return goTermsBySymbolId;
	}

	public Set<GoTerm> getGoTermsBySymbolId(String symbolId) {
		return goTermsBySymbolId.get(symbolId);
	}

	public HashSet<String> getInterestingSymbolIdsSet() {
		return interestingSymbolIdsSet;
	}

	public int getInterestingSymbolIdsSize() {
		return interestingSymbolIdsSet.size();
	}

	public String getLabel() {
		return label;
	}

	public int getMaxRank() {
		return maxRank;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	/**
	 * Get symbol's rank
	 * @param symbolId
	 * @return
	 */
	public int getRank(String symbolId) {
		Integer rank = rankSymbolId.get(symbolId);
		if (rank == null) return 0;
		return rank;
	}

	public HashMap<String, Integer> getRankSymbolId() {
		return rankSymbolId;
	}

	/**
	 * Iterate through each GOterm in this GOTerms
	 */
	@Override
	public Iterator<GoTerm> iterator() {
		return goTermsByGoTermAcc.values().iterator();
	}

	public Set<String> keySet() {
		return goTermsByGoTermAcc.keySet();
	}

	/**
	 * Calculate each node's level (in DAG)
	 * @return maximum level
	 */
	public int levels() {
		int maxLevel = 0;
		for (GoTerm gt : this)
			maxLevel = Math.max(maxLevel, gt.getLevel());
		return maxLevel;
	}

	/**
	 * Select a number of GOTerms
	 * @param numberToSelect
	 * @return
	 */
	public List<GoTerm> listTopTerms(int numberToSelect) {
		LinkedList<GoTerm> list = new LinkedList<GoTerm>();

		// Create a list of terms (to be ordered)
		LinkedList<GoTerm> ll = new LinkedList<GoTerm>();
		for (String go : goTermsByGoTermAcc.keySet())
			ll.add(goTermsByGoTermAcc.get(go));
		Collections.sort(ll);

		int i = 0;
		for (GoTerm goTerm : ll)
			if (i++ < numberToSelect) list.add(goTerm);
			else break;

		return list;
	}

	/**
	 * Calculate how many interesting symbol-IDs in are there in all these GOTerms
	 * @return Number of interesting symbols
	 */
	public int numberOfInterestingSymbols() {
		HashSet<String> intSym = new HashSet<String>();
		for (GoTerm gt : this)
			intSym.addAll(gt.getInterestingSymbolIdSet());
		return intSym.size();
	}

	/**
	 * Number of nodes in this DAG
	 * @return
	 */
	public int numberOfNodes() {
		return goTermsByGoTermAcc.keySet().size();
	}

	/**
	 * Calculate the number of nodes in that have at least one interesting symbol
	 * @return
	 */
	public int numberOfNodesWithOneInterestingSymbol() {
		int num = 0;
		for (GoTerm gt : this)
			if (gt.getInterestingSymbolIdsSize() >= 1) num++;
		return num;
	}

	/**
	 * Calculate the number of nodes in that have at least one annotated symbol
	 * @return
	 */
	public int numberOfNodesWithOneSymbol() {
		int num = 0;
		for (GoTerm gt : this)
			if (gt.getTotalCount() >= 1) num++;
		return num;
	}

	/**
	 * Calculate how many symbol-IDs in are there in all these GOTerms
	 * @return Number of interesting symbols
	 */
	public int numberOfSymbols() {
		HashSet<String> syms = new HashSet<String>();
		for (GoTerm gt : this)
			syms.addAll(gt.getSymbolIdSet());
		return syms.size();
	}

	/**
	 * Reads a file containing every gene (names and ids) associated GO terms
	 * @param goGenesFile : A file containing gene associations to GO terms
	 */
	public void readGeneAssocFile(String goGenesFile, boolean useGeneId) {
		try {
			System.err.println("Reading gene association file: '" + goGenesFile + "'");

			HashSet<String> notFound = new HashSet<String>();

			// Open file and initialize buffers
			BufferedReader inFile = Gpr.reader(goGenesFile);
			String line;
			int lineNum;

			// Read each line
			for (lineNum = 1; (line = inFile.readLine()) != null; lineNum++) {
				if (!line.startsWith("!")) {
					String items[] = line.split("\t");
					if (items.length > 4) {
						String geneName = useGeneId ? items[1] : items[2]; // Use geneID or geneName?
						String goTermAcc = items[4];

						// Add mappings
						GoTerm goTerm = getGoTerm(goTermAcc);
						if (goTerm == null) notFound.add(goTermAcc);
						else addSymbolId(goTerm, geneName);
					} else System.err.println("Ignoring line " + lineNum + ": '" + line + "'");
				}
			}

			// OK, finished
			inFile.close();

			// Show errors if any
			if (notFound.size() > 0) {
				LinkedList<String> ll = new LinkedList<String>(notFound);
				Collections.sort(ll);
				System.err.println("WARNING: Couldn't find some GOTerms while reading file '" + goGenesFile + "'\n\tNot found (" + notFound.size() + ") : " + ll);
			}

			if (verbose) Log.info("Finished reding GoGenes file '" + goGenesFile + "' : " + lineNum + " lines.");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Reads a file with a list of 'interesting' genes (one per line)
	 * @param fileName : Can be "-" for no-file
	 * @return
	 */
	public void readInterestingSymbolIdsFile(String fileName) {
		System.err.println("Reading 'interesting' genes from file: '" + fileName + "'");

		// First: Reset 'interesting' terms
		resetInterestingSymbolIds();
		HashSet<String> noGoTermFound = new HashSet<String>();
		HashSet<String> symbolNotFound = new HashSet<String>();

		if (fileName.equals("-")) return;

		// Read file
		int lineNum;
		try {
			try (
				// Open file and initialize buffers
				BufferedReader inFile = new BufferedReader(new FileReader(fileName))) {
				String line;

				// Read each line
				for (lineNum = 1; (line = inFile.readLine()) != null; lineNum++) {
					String symbolName = line.trim();
					addInterestingSymbol(symbolName, lineNum, noGoTermFound);
				}

				// Ok, finished
				inFile.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Any missing value?
		if ((symbolNotFound.size() > 0) || (noGoTermFound.size() > 0)) {
			LinkedList<String> ngtfs = new LinkedList<String>(noGoTermFound);
			Collections.sort(ngtfs);
			System.err.println("WARNING: There were some missing values while reading interesting symbolIds file: '" + fileName + "'" + "\n\tGenes's list size: " + lineNum + "\n\tFound: " + getInterestingSymbolIdsSize() + "\n\tNo GOTerm found for symbolIds (" + ngtfs.size() + "): " + ngtfs);
		}
	}

	/**
	 * Read an OBO file
	 *
	 * @param oboFile
	 * @param nameSpace
	 */
	public void readOboFile(String oboFile, boolean removeObsolete) {
		try {
			// Open file and initialize buffers
			BufferedReader inFile = Gpr.reader(oboFile);
			String line;
			int found = 0;
			String goTermAcc = null, isa = null;
			GoTerm goTerm = null;
			HashSet<String> goNotFound = new HashSet<String>();

			// Read each line
			while ((line = inFile.readLine()) != null) {
				// Get term's accesion
				if (line.startsWith("id: ")) {
					goTermAcc = line.substring(4);
					if (goTermAcc.startsWith("GO:")) goTerm = findOrCreate(goTermAcc);
					else goTerm = null;
				} else if (goTerm != null) {
					if (line.startsWith("namespace: ")) { // Read namespace
						String termNameSpace = line.substring(11);
						goTerm.setNameSpace(termNameSpace);
						// Different nameSpaces? => Ignore GOTerm
						if ((nameSpace != null) && (!nameSpace.equals(termNameSpace))) goTermAcc = null;
					} else if (line.startsWith("name: ")) { // GO name
						goTerm.setSescription(line.substring(6).trim());
					} else if (line.startsWith("is_obsolete: ") && removeObsolete) { // Is this an obsolete term? => ignore it
						removeGOTerm(goTermAcc);
					} else if ((goTermAcc != null) && (line.startsWith("is_a: "))) { // Add childs & parents as needed
						isa = line.substring(6, 16);
						GoTerm parent = findOrCreate(isa); // Add this as child
						parent.addChild(goTerm);
					}
				}
			}

			// OK, finished
			inFile.close();

			// Show errors (if any)
			if (goNotFound.size() > 0) {
				LinkedList<String> ll = new LinkedList<String>(goNotFound);
				Collections.sort(ll);
				System.err.println("WARNING: Some GO-Terms were not found while loading OBO file \'" + oboFile + "\':\n\tNot found: " + goNotFound.size() + "\n\tFound:" + found + "\n\tNot found GOTerms: " + ll);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Remove a GOTerm
	 */
	public void removeGOTerm(String goTermAcc) {
		goTermsByGoTermAcc.remove(goTermAcc);
	}

	/**
	 * Reset every 'interesting' symbolId (on every single GOTerm in this GOTerms)
	 */
	public void resetInterestingSymbolIds() {
		maxRank = 0;
		for (GoTerm gt : this)
			gt.resetInterestingSymbolIdSet();
	}

	public Set<GoTerm> rootNodes() {
		HashSet<GoTerm> roots = new HashSet<GoTerm>();

		for (GoTerm gt : this)
			roots.add(gt.rootNode());

		return roots;
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
		for (GoTerm gt : this) {
			// Save GOTerm that have at least 1 symbolId
			if (gt.getSymbolIdSet().size() > 0) {
				out.append(gt.getAcc() + "\t" + gt.getDescription() + "\t");

				// Add all symbols for this GOTerm
				for (String symbolId : gt.getSymbolIdSet())
					out.append(symbolId + "\t");
				out.append("\n");
			}
		}

		// Save it
		Gpr.toFile(fileName, out);
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuffer stb = new StringBuffer();

		// Create a list of terms (to be ordered)
		LinkedList<GoTerm> ll = new LinkedList<GoTerm>();
		for (String go : goTermsByGoTermAcc.keySet())
			ll.add(goTermsByGoTermAcc.get(go));
		Collections.sort(ll);

		// Append each term
		for (GoTerm goTerm : ll)
			stb.append(goTerm.toStringAll() + "\n");

		return stb.toString();
	}

	public Collection<GoTerm> values() {
		return goTermsByGoTermAcc.values();
	}

}
