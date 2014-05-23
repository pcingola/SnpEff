package ca.mcgill.mcb.pcingola.stats;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;

/**
 * Count for each 'type' and 'gene'.
 * Tries to avoid multiple counting by comparing to latest seqChanges.
 * WARNING: This strategy does not work if changeEffect are out of order.
 * 
 * @author pcingola
 */
@SuppressWarnings("serial")
public class GeneCountByTypeTable implements Iterable<Gene>, Serializable {

	public static int GENE_CPG_NUM_BINS = 30;
	public static boolean debug = false;

	int prevSeqChangePos = -1;
	HashSet<String> latestTypes;
	HashSet<Gene> genes;
	HashMap<String, CountByType> countersByType;

	public GeneCountByTypeTable() {
		genes = new HashSet<Gene>();
		countersByType = new HashMap<String, CountByType>();
		latestTypes = new HashSet<String>();
	}

	/**
	 * How many changes of this type?
	 * @param type
	 * @return
	 */
	public CountByType getCounter(String type) {
		CountByType counter = countersByType.get(type);

		// Lazy init counters
		if (counter == null) {
			counter = new CountByType();
			countersByType.put(type, counter);
		}

		return counter;
	}

	/**
	 * Get a sorted list of genes 
	 */
	public List<Gene> getGeneList() {
		ArrayList<Gene> geneList = new ArrayList<Gene>();
		geneList.addAll(genes);
		Collections.sort(geneList, new Comparator<Gene>() {

			@Override
			public int compare(Gene o1, Gene o2) {
				return o1.getId().compareTo(o2.getId());
			}
		});
		return geneList;
	}

	/**
	 * Get a sorted list of types 
	 */
	public List<String> getTypeList() {
		ArrayList<String> typeList = new ArrayList<String>();
		typeList.addAll(countersByType.keySet());
		Collections.sort(typeList);
		return typeList;
	}

	@Override
	public Iterator<Gene> iterator() {
		return genes.iterator();
	}

	/**
	 * Sample this <gene, marker, type, seqChange> tuple to update statistics
	 * @param gene
	 * @param marker
	 * @param seqChange
	 */
	public void sample(Gene gene, Marker marker, String type, ChangeEffect changeEffect) {
		Variant seqChange = changeEffect.getSeqChange();

		// Different seqChange? Clean the cache
		if (prevSeqChangePos != seqChange.getStart()) {
			latestTypes.clear();
			prevSeqChangePos = seqChange.getStart();
		}

		// Make sure we don't count the same type twice 
		if (latestTypes.contains(type)) return;
		latestTypes.add(type); // Update values

		//---
		// Count
		//---
		CountByType counter = getCounter(type);
		// Calculate the size of the intersection
		int start = Math.max(seqChange.getStart(), marker.getStart());
		int end = Math.min(seqChange.getEnd(), marker.getEnd());
		int size = end - start + 1;
		if (size > 0) {
			counter.inc(gene.getId(), size); // Increment counters
			//			// Add score (if any)
			//			if (!Double.isNaN(seqChange.getScore())) counter.addScore(gene.getId(), seqChange.getScore());
		}

		genes.add(gene); // Add gene to hash
	}
}
