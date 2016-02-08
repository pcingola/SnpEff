package org.snpeff.ped;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.util.Gpr;

/**
 * A pedigree of PedEntries
 * 
 * @author pcingola
 */
public class PedPedigree implements Iterable<TfamEntry>, Comparable<PedPedigree> {

	boolean verbose = false;
	HashMap<String, TfamEntry> tfamEntryById = new HashMap<String, TfamEntry>();
	List<TfamEntry> tfamEntries;
	PlinkMap plinkMap;

	public PedPedigree() {
		tfamEntryById = new HashMap<String, TfamEntry>();
		tfamEntries = new ArrayList<TfamEntry>();
	}

	public PedPedigree(String tfamFileName) {
		tfamEntryById = new HashMap<String, TfamEntry>();
		tfamEntries = new ArrayList<TfamEntry>();
		loadTfam(tfamFileName);
	}

	/**
	 * Add an entry to this pedigree
	 * @param tfamEntry
	 */
	public void add(TfamEntry tfamEntry) {
		tfamEntryById.put(tfamEntry.getId(), tfamEntry);
		tfamEntries.add(tfamEntry);
	}

	@Override
	public int compareTo(PedPedigree ped) {
		return getFamilyId().compareTo(ped.getFamilyId());
	}

	/**
	 * Split data into families
	 * @return
	 */
	public Collection<PedPedigree> families() {
		AutoHashMap<String, PedPedigree> families = new AutoHashMap<String, PedPedigree>(new PedPedigree());

		// Add each individual to its corresponding family
		for (TfamEntry ind : this) {
			String fid = ind.getFamilyId();
			PedPedigree ped = families.getOrCreate(fid);
			ped.add(ind);
		}

		return families.values();
	}

	public TfamEntry get(String id) {
		return tfamEntryById.get(id);
	}

	public String getFamilyId() {
		if (tfamEntries.isEmpty()) return "";
		return tfamEntries.get(0).getFamilyId();
	}

	public PlinkMap getPlinkMap() {
		return plinkMap;
	}

	/**
	 * Get a list of sample IDs
	 * @return
	 */
	public List<String> getSampleIds() {
		ArrayList<String> sampleIds = new ArrayList<String>();
		for (TfamEntry te : tfamEntries)
			sampleIds.add(te.id);
		return sampleIds;
	}

	@Override
	public Iterator<TfamEntry> iterator() {
		return tfamEntries.iterator();
	}

	public Set<String> keySet() {
		return tfamEntryById.keySet();
	}

	/**
	 * Load a pedigree from a PED and MAP file pair
	 * 
	 * @param pedFileName
	 */
	public void load(String pedFileName) {
		String pedBaseFileName = Gpr.removeExt(pedFileName);
		String mapFile = pedBaseFileName + ".map";

		PedFileIterator pedFile = new PedFileIterator(pedFileName, mapFile);

		// Load all entries for this family
		int count = 1;
		for (PedEntry pe : pedFile) {
			if (verbose) Gpr.showMarkStderr(count++, 1);
			add(pe);
		}

		plinkMap = pedFile.getPlinkMap();
	}

	/**
	 * Load a TFAM file
	 * @param tfamFileName
	 */
	public void loadTfam(String tfamFileName) {
		String tfamStr = Gpr.readFile(tfamFileName);
		if (tfamStr.isEmpty()) throw new RuntimeException("Cannot read file '" + tfamFileName + "'");

		for (String line : tfamStr.split("\n")) {
			if (line.startsWith("#")) continue; // Skip comments

			try {
				TfamEntry te = new TfamEntry(line);
				add(te);
			} catch (Throwable t) {
				throw new RuntimeException("Error parsing line from PED/TFAM file:\n\tFile name : '" + tfamFileName + "\n\tLine: '" + line + "'", t);
			};
		}
	}

	/**
	 * Save pedigree as a TFAM file
	 * @param fileName
	 */
	public void saveTfam(String fileName) {
		StringBuilder sb = new StringBuilder();
		for (TfamEntry te : this)
			sb.append(te + "\n");

		Gpr.toFile(fileName, sb.toString());
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public int size() {
		return tfamEntryById.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (TfamEntry te : this)
			sb.append(te.toString() + "\n");
		return sb.toString();
	}

	public Collection<TfamEntry> values() {
		return tfamEntryById.values();
	}
}
