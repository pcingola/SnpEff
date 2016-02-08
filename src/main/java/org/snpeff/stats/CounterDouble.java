package org.snpeff.stats;

/**
 * A simple class that counts...
 * 
 * Note: This is probably the best piece of software I'll ever write! :-P
 * 
 * @author pcingola
 */
public class CounterDouble {

	public double count = 0;

	public void add(double d) {
		count += d;
	}

	public void add(long add) {
		count += add;
	}

	public void dec() {
		count--;
	}

	public double get() {
		return count;
	}

	public double getCount() {
		return count;
	}

	public void inc() {
		count++;
	}

	public void max(double d) {
		count = Math.max(count, d);
	}

	public void min(double d) {
		count = Math.min(count, d);
	}

	public void reset() {
		count = 0;
	}

	public void set(double newCount) {
		count = newCount;
	}

	public void sub(double d) {
		count -= d;
	}

	public void sub(long sub) {
		count -= sub;
	}

	@Override
	public String toString() {
		return Double.toString(count);
	}
}
