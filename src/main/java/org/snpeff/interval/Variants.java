package org.snpeff.interval;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

/**
 * A set of variants that act within a haplotype
 * @author pcingola
 */
public class Variants implements Iterable<Variant> {

	List<Variant> variants; // Variants sorted by genomic coordinates

	public Variants() {
		variants = new ArrayList<>();
	}

	public boolean add(Variant arg0) {
		return variants.add(arg0);
	}

	public Variant getFirst() {
		return variants.get(0);
	}

	@Override
	public Iterator<Variant> iterator() {
		return variants.iterator();
	}

	public int size() {
		return variants.size();
	}

	public Stream<Variant> stream() {
		return variants.stream();
	}

	@Override
	public String toString() {
		if (variants.isEmpty()) return "";
		return stream() //
				.map(v -> v.toString()) // 
				.reduce("", (s1, s2) -> s1 + " + " + s2) // Add strings
				.substring(3) // Remove first '+' sign
		;
	}

	public boolean withinOneCodon(Transcript tr) {
		return false;
	}
}
