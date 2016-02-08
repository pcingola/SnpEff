package org.snpeff.geneOntology;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * An instance of a GO term (a node in the DAG)
 * 
 * @author Pablo Cingolani
 *
 */
public class GoTerm implements Comparable<GoTerm>, Iterable<String>, Serializable {

	public static boolean debug = false;
	private static final long serialVersionUID = 1L;

	GoTerms goTerms; // GoTerms (this node belongs to a DAG)
	String nameSpace; // name space (e.g. molecular_function)
	String acc; // Go term's accession (e.g. GO:0001234)
	String description; // Detailed description
	int level; // Node's level in DAG
	int maxRank; // Maximum rank in this go term
	int rankedSymbols; // How many of the symbols are ranked
	long rankSum; // Rank sum in this GOTerm
	HashSet<GoTerm> childs; // GO terms' accession that are 'child' (in the directed acyclic graph)
	HashSet<GoTerm> parents; // GO terms' accession that are 'parent' (in the directed acyclic graph)
	HashSet<String> symbolIdSet;// Symbol IDs set in this GO term
	HashSet<String> interestingSymbolIdSet; // Interesting Symbols-Ids in this GO term

	/**
	 * Create a new GOTerm by joining all GOTerms in the list
	 * @param goTermList
	 */
	public GoTerm(List<GoTerm> goTermList, GoTerms goTerms) {
		this.goTerms = goTerms;

		level = -1;
		maxRank = Integer.MIN_VALUE; // Mark it a not calculated
		rankSum = Integer.MIN_VALUE; // Mark it a not calculated
		rankedSymbols = Integer.MIN_VALUE; // Mark it a not calculated

		// Initialize up genes set
		interestingSymbolIdSet = new HashSet<String>();
		symbolIdSet = new HashSet<String>();

		// DAG structure
		childs = new HashSet<GoTerm>();
		parents = new HashSet<GoTerm>();

		// For each GOTerm in the list: join them
		String accAll = "";
		nameSpace = null;
		for( GoTerm goTerm : goTermList ) {
			// Add symbols and interestingSymbols to new Set
			interestingSymbolIdSet.addAll(goTerm.interestingSymbolIdSet);
			symbolIdSet.addAll(goTerm.symbolIdSet);

			// Append names
			accAll += goTerm.getAcc() + " ";
			if( nameSpace == null ) nameSpace = goTerm.nameSpace;
		}

		// Names...
		acc = accAll;
		description = accAll;
	}

	public GoTerm(String acc, GoTerms goTerms, String nameSpace, String description) {
		this.goTerms = goTerms;
		this.acc = acc;
		this.nameSpace = nameSpace;
		this.description = description;
		level = -1;
		maxRank = Integer.MIN_VALUE; // Mark it a not calculated
		rankSum = Integer.MIN_VALUE; // Mark it a not calculated
		rankedSymbols = Integer.MIN_VALUE; // Mark it a not calculated

		// Fill up genes set
		interestingSymbolIdSet = new HashSet<String>();
		symbolIdSet = new HashSet<String>();

		// DAG structure
		childs = new HashSet<GoTerm>();
		parents = new HashSet<GoTerm>();
	}

	/**
	 * Add a goTermId as a child of this GOTerm
	 * Also adds 'this' as parent of 'goTerm'
	 * @param goTermAcc
	 */
	public void addChild(GoTerm childGoTerm) {
		childs.add(childGoTerm);
		childGoTerm.addParent(this);
	}

	/**
	 * Add one gene to interestingGenesSet
	 * @param symbolId
	 */
	public void addInterestingSymbolId(String symbolId) {
		// Add interesting gene
		if( symbolIdSet.contains(symbolId) ) interestingSymbolIdSet.add(symbolId);
		else throw new RuntimeException("Symbol '" + symbolId + "' cannod be added as 'interesting' to goTerm '" + this + "' (it does not belong to the set)");
	}

	/**
	 * Add a goTermId as a parent of this GOTerm
	 * @param goTermAcc
	 */
	private void addParent(GoTerm goTerm) {
		parents.add(goTerm);
	}

	/**
	 * Add one gene to genesSet
	 * @param symbolId
	 */
	public void addSymbolId(String symbolId) {
		symbolIdSet.add(symbolId);
	}

	/**
	 * Add all symbols from childs to goTerm
	 */
	public void addSymbolsFromChilds(GoTerm goTerm) {
		// Add symbols
		for( String symbolId : symbolIdSet )
			goTerm.addSymbolId(symbolId);

		// Add interesting symbols
		for( String symbolId : interestingSymbolIdSet )
			goTerm.addInterestingSymbolId(symbolId);

		// Recurse
		for( GoTerm child : childs )
			child.addSymbolsFromChilds(goTerm);
	}

	@Override
	public int compareTo(GoTerm goTerm) {
		return acc.compareTo(goTerm.acc);
	}

	public String getAcc() {
		return acc;
	}

	public HashSet<GoTerm> getChilds() {
		return childs;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Return first child in the list
	 * @return First child (or null if there are no children)
	 */
	public GoTerm getFirstChild() {
		return childs.iterator().next();
	}

	/**
	 * Return first parent in the list
	 * @return First parent (or null if there are no parents)
	 */
	public GoTerm getFirstParent() {
		return parents.iterator().next();
	}

	public GoTerms getGoTerms() {
		return goTerms;
	}

	public HashSet<String> getInterestingSymbolIdSet() {
		return interestingSymbolIdSet;
	}

	/**
	 * Number of 'interesting' symbols
	 * @return
	 */
	public int getInterestingSymbolIdsSize() {
		return interestingSymbolIdSet.size();
	}

	/**
	 * Calculate node's level
	 * @return
	 */
	public int getLevel() {
		// Level not calculated yet?
		if( level < 0 ) {
			level = 0;
			// Level = max(parent's level) + 1;
			for( GoTerm parent : parents )
				level = Math.max(level, parent.getLevel() + 1);
		}
		return level;
	}

	public int getMaxRank() {
		return maxRank;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public HashSet<GoTerm> getParents() {
		return parents;
	}

	public int getRankedSymbols() {
		return rankedSymbols;
	}

	/**
	 * Number of symbols in this node (total = interesting + not-interesting)
	 * @return
	 */
	public Set<String> getSymbolIdSet() {
		return symbolIdSet;
	}

	/**
	 * Number of symbols in this node (total = interesting + not-interesting)
	 * @return
	 */
	public int getTotalCount() {
		return symbolIdSet.size();
	}

	/**
	 * Intersects this term with 'goTerm' (intersects 'symbolIdSet' and 'interestingSymbolIdSet').
	 * 
	 * @param goTerm : GOTerm to intercept
	 */
	public void intersection(GoTerm goTerm) {
		if( symbolIdSet != null ) symbolIdSet.retainAll(goTerm.symbolIdSet);
		if( interestingSymbolIdSet != null ) interestingSymbolIdSet.retainAll(goTerm.interestingSymbolIdSet);
	}

	/**
	 * Is this GOTerm a leave?
	 * @return
	 */
	public boolean isLeave() {
		if( (childs == null) || (childs.size() <= 0) ) return true;
		return false;
	}

	@Override
	public Iterator<String> iterator() {
		return symbolIdSet.iterator();
	}

	/**
	 * Reset 'interesting' symbols
	 */
	void resetInterestingSymbolIdSet() {
		if( interestingSymbolIdSet.size() > 0 ) interestingSymbolIdSet = new HashSet<String>();
		maxRank = Integer.MIN_VALUE; // Mark it a not calculated
		rankSum = Integer.MIN_VALUE; // Mark it a not calculated
		rankedSymbols = Integer.MIN_VALUE; // Mark it a not calculated
	}

	/**
	 * Get the root node
	 * @return
	 */
	public GoTerm rootNode() {
		if( (parents == null) || parents.isEmpty() ) return this;
		return parents.iterator().next().rootNode();
	}

	public void setAcc(String go) {
		acc = go;
	}

	public void setChilds(HashSet<GoTerm> childs) {
		this.childs = childs;
	}

	public void setGenesSet(HashSet<String> genesSet) {
		symbolIdSet = genesSet;
	}

	public void setGoTerms(GoTerms goTerms) {
		this.goTerms = goTerms;
	}

	public void setInterestingSymbolIdSet(HashSet<String> interestingSymbolIdSet) {
		this.interestingSymbolIdSet = interestingSymbolIdSet;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setMaxRank(int maxRank) {
		this.maxRank = maxRank;
	}

	/**
	 * Perform a 'set minus' between this term and 'goTerm' (set minus for 'symbolIdSet' and 'interestingSymbolIdSet').
	 * @param goTerm : GOTerm to use for 'set minus' operation
	 */
	public void setMinus(GoTerm goTerm) {
		if( symbolIdSet != null ) symbolIdSet.removeAll(goTerm.symbolIdSet);
		if( interestingSymbolIdSet != null ) interestingSymbolIdSet.removeAll(goTerm.interestingSymbolIdSet);
	}

	public void setNameSpace(String goTermType) {
		nameSpace = goTermType;
	}

	public void setParents(HashSet<GoTerm> parents) {
		this.parents = parents;
	}

	public void setRankedSymbols(int rankedSymbols) {
		this.rankedSymbols = rankedSymbols;
	}

	public void setRankSum(int rankSum) {
		this.rankSum = rankSum;
	}

	public void setSescription(String description) {
		this.description = description;
	}

	public void setSymbolIdSet(HashSet<String> symbolIdSet) {
		this.symbolIdSet = symbolIdSet;
	}

	@Override
	public String toString() {
		return acc;
	}

	public String toStringAll() {
		return toStringAll(true);
	}

	public String toStringAll(boolean showSymbols) {
		StringBuffer ig = new StringBuffer();
		StringBuffer ids = new StringBuffer();

		// Show 'interesting' genes first
		LinkedList<String> is = new LinkedList<String>(interestingSymbolIdSet);
		Collections.sort(is);
		for( String id : is )
			ig.append(id + " ");

		// Show 'other' genes
		LinkedList<String> ss = new LinkedList<String>(symbolIdSet);
		Collections.sort(ss);
		for( String id : ss )
			if( !interestingSymbolIdSet.contains(id) ) ids.append(id + " ");

		// Sort 'childs'
		LinkedList<GoTerm> schilds = new LinkedList<GoTerm>(childs);
		Collections.sort(schilds);

		// Sort 'parents'
		LinkedList<GoTerm> sparents = new LinkedList<GoTerm>(parents);
		Collections.sort(sparents);

		// Prepare output string
		String out = acc + "\tlev:" + level + "\tinteresting:" + getInterestingSymbolIdsSize() + "\ttotal:" + getTotalCount();
		out += "\t" + nameSpace + "\t" + description;
		if( showSymbols ) out += "\t[ " + ig + "]\t" + ids;
		out += "\tChilds: " + schilds + "\tParents: " + sparents;

		return out;
	}

	/**
	 * Union this term with 'goTerm' (union for 'symbolIdSet' and 'interestingSymbolIdSet').
	 * 
	 * @param goTerm : GOTerm to use for union
	 */
	public void union(GoTerm goTerm) {
		if( symbolIdSet != null ) symbolIdSet.addAll(goTerm.symbolIdSet);
		if( interestingSymbolIdSet != null ) interestingSymbolIdSet.addAll(goTerm.interestingSymbolIdSet);
	}
}
