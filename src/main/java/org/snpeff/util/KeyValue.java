package org.snpeff.util;

/**
 * A "key = value" pair
 * @author pablocingolani
 */
@SuppressWarnings("rawtypes")
public class KeyValue<A, B> implements Comparable<KeyValue<A, B>> {
	public final A key;
	public final B value;

	public KeyValue(A key, B value) {
		super();
		this.key = key;
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int compareTo(KeyValue<A, B> o) {
		int cmp = 0;

		if (key instanceof Comparable) {
			cmp = ((Comparable) key).compareTo(o.key);
			if (cmp != 0) return cmp;
		}

		if (value instanceof Comparable) {
			cmp = ((Comparable) value).compareTo(o.value);
			if (cmp != 0) return cmp;
		}

		return cmp;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof KeyValue) {
			KeyValue otherPair = (KeyValue) other;
			return ((this.key == otherPair.key || (this.key != null && otherPair.key != null && this.key.equals(otherPair.key))) && (this.value == otherPair.value || (this.value != null && otherPair.value != null && this.value.equals(otherPair.value))));
		}
		return false;
	}

	public A getKey() {
		return key;
	}

	public B getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		int hashKey = key != null ? key.hashCode() : 0;
		int hashValue = value != null ? value.hashCode() : 0;
		return (hashKey + hashValue) * hashValue + hashKey;
	}

	@Override
	public String toString() {
		return key + " = '" + value + "'";
	}
}