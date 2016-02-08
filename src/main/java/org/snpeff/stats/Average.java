package org.snpeff.stats;

/**
 * A simple class that calculates averages
 * 
 * @author pcingola
 */
public class Average {

	int count = 0;
	double sum = 0;

	public void add(double add) {
		sum += add;
		count++;
	}

	public double getAvg() {
		return count > 0 ? sum / count : 0.0;
	}

	public int getCount() {
		return count;
	}

	public double getSum() {
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
