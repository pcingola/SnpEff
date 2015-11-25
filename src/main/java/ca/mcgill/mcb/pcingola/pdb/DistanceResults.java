package ca.mcgill.mcb.pcingola.pdb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;

import ca.mcgill.mcb.pcingola.stats.CountByType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * A collection of DistanceResult
 */
public class DistanceResults extends ArrayList<DistanceResult> {

	private static final long serialVersionUID = 1L;

	HashMap<String, DistanceResult> byKey;
	CountByType contactsByPdbId;

	public DistanceResults() {
		super();
	}

	/**
	 * Add an element only if the 'key' is unique
	 */
	public void addIfUniq(DistanceResult d, String key) {
		if (byKey == null) byKey = new HashMap<>();
		if (!byKey.containsKey(key)) {
			add(d);
			byKey.put(key, d);
		}
	}

	/**
	 * Add all elements from collection
	 */
	public void addMins() {
		byKey.values().stream().sorted((d1, d2) -> d1.compareByPos(d2)).forEach(d -> add(d));
	}

	/**
	 * Collect elements, keep 'min' by key
	 */
	public void collectMin(DistanceResult d, String key) {
		if (byKey == null) byKey = new HashMap<>();
		if (!byKey.containsKey(key)) byKey.put(key, d);
		else {
			DistanceResult dold = byKey.get(key);
			if (dold.equalPos(d) && d.compareByPos(dold) < 0) // Same position? Keep smallest one
				byKey.put(key, d);
		}
	}

	/**
	 * Number of contacts by pdbID and chain
	 */
	public int contacts(String pdbId, String pdbChainId) {
		// Create?
		if (contactsByPdbId == null) {
			contactsByPdbId = new CountByType();
			this.forEach(d -> contactsByPdbId.inc(d.pdbId + ":" + d.pdbChainId));
		}

		return (int) contactsByPdbId.get(pdbId + ":" + pdbChainId);
	}

	/**
	 * Load from file
	 */
	public void load(String fileName) {
		for (String line : Gpr.readFile(fileName).split("\n"))
			add(new DistanceResult(line));
	}

	@Override
	public String toString() {
		return stream().map(d -> d.toString()).collect(Collectors.joining("\n"));
	}

}
