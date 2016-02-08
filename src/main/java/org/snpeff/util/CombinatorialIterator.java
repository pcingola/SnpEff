package org.snpeff.util;

import java.util.Iterator;

/**
 * Generate all possible 'count' combinations
 *
 * @author pcingola
 */
public class CombinatorialIterator implements Iterable<int[]>, Iterator<int[]> {

	int next[];
	int max[];
	int min[];
	boolean inc, finished;

	public CombinatorialIterator(int size) {
		next = new int[size];
		max = new int[size];
		min = new int[size];
	}

	@Override
	public boolean hasNext() {
		if (inc) inc();
		return !finished;
	}

	public void inc() {
		inc = false;

		for (int i = 0; i < next.length; i++) {
			if (next[i] < max[i]) {
				next[i]++;
				return;
			} else {
				next[i] = min[i];
			}
		}

		finished = true;
	}

	@Override
	public Iterator<int[]> iterator() {
		reset();
		return this;
	}

	@Override
	public int[] next() {
		if (inc) inc();
		if (!hasNext()) return null;

		inc = true;
		return next;
	}

	@Override
	public void remove() {

	}

	void reset() {
		inc = false;
		finished = false;
		for (int i = 0; i < next.length; i++)
			next[i] = min[i];
	}

	public void set(int idx, int min, int max) {
		if (max <= min) throw new RuntimeException("Cannot initialize 'max' less or equal than 'min'. min = " + min + ", max = " + max);
		this.min[idx] = min;
		this.max[idx] = max;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int i = 0; i < next.length; i++)
			sb.append((i > 0 ? ", " : "") + next[i]);
		sb.append(" ]");

		return sb.toString();
	}

}
