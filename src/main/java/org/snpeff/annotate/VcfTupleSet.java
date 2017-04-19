package org.snpeff.annotate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A set of Vcf tuples
 * 
 * @author pcingola
 */
public class VcfTupleSet implements Iterable<VcfTuple> {

	Set<VcfTuple> tupleSet;

	public VcfTupleSet() {
		tupleSet = new HashSet<>();
	}

	public boolean add(VcfTuple arg0) {
		return tupleSet.add(arg0);
	}

	public boolean addAll(Collection<? extends VcfTuple> arg0) {
		return tupleSet.addAll(arg0);
	}

	public boolean isEmpty() {
		return tupleSet.isEmpty();
	}

	@Override
	public Iterator<VcfTuple> iterator() {
		return tupleSet.iterator();
	}

	public boolean remove(Object arg0) {
		return tupleSet.remove(arg0);
	}

	public int size() {
		return tupleSet.size();
	}

	public Stream<VcfTuple> stream() {
		return tupleSet.stream();
	}

}
