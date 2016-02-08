package org.snpeff.stats;

/**
 * A simple class that calculates average of integer numbers
 * 
 * @author pcingola
 */
public class AverageInt {

	int count = 0;
	long sum = 0;

	public void add(double add) {
		sum += add;
		count++;
	}

	public double getAvg() {
		return count > 0 ? ((double) sum) / ((double) count) : 0.0;
	}

	public int getCount() {
		return count;
	}

	public long getSum() {
		return sum;
	}

	public void reset() {
		count = 0;
		sum = 0;
	}

	@Override
	public String toString() {
		return Long.toString(count);
	}
}
