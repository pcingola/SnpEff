package org.snpeff.pdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Map protein ID to transcript ID and vice versa
 *
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
		if (ime.proteinId != null) byPdbId.getOrCreate(ime.proteinId).add(ime);
		entries.add(ime);
	}

	public void addAll(Collection<IdMapperEntry> imes) {
		for (IdMapperEntry ime : imes)
			add(ime);
	}

	public List<IdMapperEntry> getByPdbId(String id) {
		return byPdbId.get(id);
	}

	public List<IdMapperEntry> getByPdbId(String id, String chainId) {
		if (byPdbId.get(id) == null) return null;

		List<IdMapperEntry> list = new LinkedList<>();
		for (IdMapperEntry ime : byPdbId.get(id))
			if (chainId.equals(ime.pdbChainId)) list.add(ime);

		return list;
	}

	public List<IdMapperEntry> getByTrId(String id) {
		return byTrId.get(id);
	}

	public Collection<IdMapperEntry> getEntries() {
		return entries;
	}

	public void load(String fileName) {
		if (verbose) Log.info("Loading IDs from file: " + fileName);
		count = 0;
		String lines[] = Gpr.readFile(fileName).split("\n");
		for (String line : lines)
			parseLine(line);

		if (verbose) Log.info("Done. Total entries added: " + count);
	}

	/**
	 * Parse a line and add it to this map
	 *
	 * File format:
	 *     proteinID \t transcriptID
	 *
	 * 	proteinId: The protein ID used in the protein files (e.g. pdbID, uniprotID, etc.)
	 * 	transcriptId: The transcript ID from the genomic file (e.g. ENSEMBL transcript iD, RefSeq ID, etc.)
	 */
	void parseLine(String line) {
		String fields[] = line.split("\t");

		if (fields.length > 1 && !fields[1].isEmpty()) {
			IdMapperEntry ime = new IdMapperEntry(fields[0], fields[1]);
			add(ime);
			count++;
		}
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (IdMapperEntry im : entries)
			sb.append(im + "\n");
		return sb.toString();
	}
}
