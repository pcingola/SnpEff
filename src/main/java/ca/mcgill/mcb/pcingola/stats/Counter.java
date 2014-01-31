package ca.mcgill.mcb.pcingola.stats;

/**
 * A simple class that counts...
 * 
 * Note: This is probably the best piece of software I'll ever write! :-P
 * 
 * @author pcingola
 */
public class Counter {

	public long count = 0;

	public void add(long add) {
		count += add;
	}

	public void dec() {
		count--;
	}

	public long get() {
		return count;
	}

	public long getCount() {
		return count;
	}

	public void inc() {
		count++;
	}

	public void reset() {
		count = 0;
	}

	public void set(long newCount) {
		count = newCount;
	}

	public void sub(long sub) {
		count -= sub;
	}

	@Override
	public String toString() {
		return Long.toString(count);
	}
}
