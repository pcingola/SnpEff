package ca.mcgill.mcb.pcingola.pdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ca.mcgill.mcb.pcingola.collections.AutoHashMap;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Map IDs
 *
 * @author pcingola
 */
public class IdMapper {

	boolean verbose;
	int count;

	AutoHashMap<String, ArrayList<IdMapperEntry>> byTrId, byPdbId;
	HashSet<IdMapperEntry> entries;

	/**
	 * Remove version form transcript ID
	 */
	public static String transcriptIdNoVersion(String trId) {
		int idx = trId.lastIndexOf('.');
		if (idx < 0) return trId;
		return trId.substring(0, idx);
	}

	public static Set<String> transcriptIds(List<IdMapperEntry> ids) {
		if (ids == null) return null;

		// Unique names
		HashSet<String> set = new HashSet<String>();
		for (IdMapperEntry ime : ids)
			set.add(ime.trId);

		return set;
	}

	public IdMapper() {
		ArrayList<IdMapperEntry> emptyList = new ArrayList<IdMapperEntry>();
		byTrId = new AutoHashMap<String, ArrayList<IdMapperEntry>>(emptyList);
		byPdbId = new AutoHashMap<String, ArrayList<IdMapperEntry>>(emptyList);;
		entries = new HashSet<>();
	}

	public void add(IdMapperEntry ime) {
		if (ime.trId != null) byTrId.getOrCreate(ime.trId).add(ime);
		if (ime.pdbId != null) byPdbId.getOrCreate(ime.pdbId).add(ime);
		entries.add(ime);
	}

	public void addAll(Collection<IdMapperEntry> imes) {
		imes.stream().forEach(ime -> add(ime));
	}

	//	/**
	//	 * Pick "best" entries
	//	 */
	//	public Collection<IdMapperEntry> best(DistanceResults distanceResults) {
	//		// Sort best entries by geneId
	//		Map<String, List<IdMapperEntry>> map = entries.stream() //
	//				.sorted((im1, im2) -> compareToBetterMap(distanceResults, im1, im2)) //
	//				.collect(Collectors.groupingBy(im -> im.geneId)) //
	//				;
	//
	//		// Show
	//		ArrayList<IdMapperEntry> best = new ArrayList<>();
	//		map.keySet().forEach(k -> {
	//			best.add(map.get(k).get(0)); // Add first entry to 'best'
	//			System.err.println("map\t" + k);
	//			map.get(k).forEach(im -> System.err.println("\t\t" + im + "\tAA_contacts: " + contacts(distanceResults, im)));
	//		} //
	//		);
	//
	//		return best;
	//	}

	/**
	 * Comparator: Get "best" mappings first
	 */
	public int compareToBetterMap(DistanceResults distanceResults, IdMapperEntry im1, IdMapperEntry im2) {
		//		int cmp = im1.geneId.compareTo(im2.geneId);
		//		if (cmp != 0) return cmp;

		int cmp = im2.pdbAaLen - im1.pdbAaLen; // Longer PDB AA sequence first
		if (cmp != 0) return cmp;

		cmp = contacts(distanceResults, im2) - contacts(distanceResults, im1); // Compare number of AA 'in contact' (more is better)
		if (cmp != 0) return cmp;

		cmp = im2.trAaLen - im1.trAaLen; // Longer transcript AA sequence first
		if (cmp != 0) return cmp;

		cmp = im1.trId.compareTo(im2.trId);
		if (cmp != 0) return cmp;

		cmp = im1.pdbId.compareTo(im2.pdbId);
		if (cmp != 0) return cmp;

		cmp = im1.pdbChainId.compareTo(im2.pdbChainId);
		return cmp;
	}

	/**
	 * Number of AA in contact
	 */
	int contacts(DistanceResults distanceResults, IdMapperEntry ime) {
		return distanceResults.contacts(ime.pdbId, ime.pdbChainId);
	}

	public List<IdMapperEntry> getByPdbId(String id) {
		return byPdbId.get(id);
	}

	public List<IdMapperEntry> getByPdbId(String id, String chainId) {
		if (byPdbId.get(id) == null) return null;
		return byPdbId.get(id) //
				.stream() //
				.filter(ime -> chainId.equals(ime.pdbChainId)) //
				.collect(Collectors.toList());
	}

	public List<IdMapperEntry> getByTrId(String id) {
		return byTrId.get(id);
	}

	public Collection<IdMapperEntry> getEntries() {
		return entries;
	}

	public void load(String fileName) {
		if (verbose) Timer.showStdErr("Loading IDs from file: " + fileName);
		count = 0;
		String lines[] = Gpr.readFile(fileName).split("\n");
		for (String line : lines)
			parseLine(line);

		if (verbose) Timer.showStdErr("Done. Total entries added: " + count);
	}

	/**
	 * Parse a line and add it to this map
	 */
	void parseLine(String line) {
		String fields[] = line.split("\t");

		if (fields.length > 1 && !fields[1].isEmpty()) {
			IdMapperEntry ime = new IdMapperEntry(fields[0], fields[1]);
			add(ime);
			count++;
		}

		if (fields.length > 2 && !fields[2].isEmpty()) {
			IdMapperEntry ime = new IdMapperEntry(fields[0], fields[2]);
			add(ime);
			count++;
		}

	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		return entries.stream().sorted().map(im -> im.toString()).collect(Collectors.joining("\n"));
	}
}
