package org.snpeff.reactome.events;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.snpeff.reactome.Entity;

/**
 * A Reactome pathway
 * 
 * @author pcingola
 *
 */
public class Pathway extends Reaction implements Iterable<Event> {

	HashMap<Event, Integer> events; // Count number of events

	public Pathway(int id, String name) {
		super(id, name);
		events = new HashMap<Event, Integer>();
	}

	public void add(Event e) {
		if (e == null) return;

		if (!events.containsKey(e)) events.put(e, 1);
		events.put(e, events.get(e) + 1);
	}

	public boolean contains(Object o) {
		return events.containsKey(o);
	}

	public boolean isEmpty() {
		return events.isEmpty();
	}

	@Override
	public Iterator<Event> iterator() {
		return events.keySet().iterator();
	}

	public int size() {
		return events.size();
	}

	@Override
	public String toString() {
		return toString(0, new HashSet<Entity>());
	}

	@Override
	public String toString(int tabs, HashSet<Entity> done) {
		done.add(this);

		StringBuilder sb = new StringBuilder();
		sb.append(super.toString(tabs, done));

		for (Event e : this)
			sb.append(e.toString(tabs + 1, done) + "\n");

		return sb.toString();
	}

}
