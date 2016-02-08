package org.snpeff.util;

/**
 * Tuple: A pair of objects
 * @author pablocingolani
 *
 * @param <A>
 * @param <B>
 */
@SuppressWarnings("rawtypes")
public class Tuple<A, B> {
	public final A first;
	public final B second;

	public Tuple(A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Tuple) {
			Tuple otherPair = (Tuple) other;
			return ((this.first == otherPair.first || (this.first != null && otherPair.first != null && this.first.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null && otherPair.second != null && this.second.equals(otherPair.second))));
		}
		return false;
	}

	public A getFirst() {
		return first;
	}

	public B getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	@Override
	public String toString() {
		return "(" + first + ", " + second + ")";
	}
}