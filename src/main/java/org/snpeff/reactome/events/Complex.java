package org.snpeff.reactome.events;

import java.util.HashSet;
import java.util.Iterator;

import org.snpeff.reactome.Entity;
import org.snpeff.util.Gpr;

/**
 * A Reactome complex (a bunch of molecules or complexes
 * 
 * @author pcingola
 *
 */
public class Complex extends Reaction implements Iterable<Entity> {

	public Complex(int id, String name) {
		super(id, name);
	}

	public void add(Entity e) {
		addInput(e);
	}

	public boolean contains(Object o) {
		return inputs.containsKey(o);
	}

	public boolean isEmpty() {
		return inputs.isEmpty();
	}

	@Override
	public Iterator<Entity> iterator() {
		return inputs.keySet().iterator();
	}

	public int size() {
		return inputs.size();
	}

	@Override
	public String toString(int tabs, HashSet<Entity> done) {
		done.add(this);

		StringBuilder sb = new StringBuilder();
		sb.append(Gpr.tabs(tabs) + toStringSimple() + "\n");

		for (Entity e : this)
			sb.append(e.toString(tabs + 1, done) + "\n");

		return sb.toString();
	}

}
