package org.snpeff.gtex;

import java.util.LinkedList;
import java.util.List;

import org.snpeff.collections.AutoHashMap;

/**
 * Maps many IDs to many Names
 * I.e. a mapping id <-> name where neither is unique
 * 
 * @author pcingola
 */
public class IdMap {

	AutoHashMap<String, List<String>> id2names;
	AutoHashMap<String, List<String>> name2ids;

	public IdMap() {
		id2names = new AutoHashMap<String, List<String>>(new LinkedList<String>());
		name2ids = new AutoHashMap<String, List<String>>(new LinkedList<String>());
	}

	/**
	 * Add a mapping id <-> name
	 * @param id
	 * @param name
	 */
	public void add(String id, String name) {
		// Do not add nulls or empties
		if ((id == null) || id.isEmpty()) return;
		if ((name == null) || name.isEmpty()) return;

		id2names.getOrCreate(id).add(name);
		name2ids.getOrCreate(name).add(id);
	}

	/**
	 * Get all IDs corresponding to this name
	 * @param name
	 * @return
	 */
	public List<String> getIds(String name) {
		return name2ids.get(name);
	}

	/**
	 * Get all names corresponding to this Id
	 * @param name
	 * @return
	 */
	public List<String> getNames(String id) {
		return id2names.get(id);
	}

}
