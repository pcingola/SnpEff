package org.snpeff.stats;

/**
 * A simple class that counts...
 * 
 * Note: This is probably the best piece of software I'll ever write! :-P
 * 
 * @author pcingola
 */
public class Counter {

	public long count = 0;

	public long add(long add) {
		count += add;
		return count;
	}

	public long dec() {
		return --count;
	}

	public long get() {
		return count;
	}

	public long getCount() {
		return count;
	}

	public long inc() {
		return ++count;
	}

	public void reset() {
		count = 0;
	}

	public void set(long newCount) {
		count = newCount;
	}

	public long sub(long sub) {
		count -= sub;
		return count;
	}

	@Override
	public String toString() {
		return Long.toString(count);
	}
}
