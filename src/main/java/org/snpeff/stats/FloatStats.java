package org.snpeff.stats;

/**
 * A simple class that does some basic statistics on double numbers
 * 
 * @author pcingola
 */
public class FloatStats {

	double sum = 0;
	int count = 0;
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;

	public double avg() {
		return sum / count;
	}

	public int getCount() {
		return count;
	}

	public double getMax() {
		return max;
	}

	public double getMin() {
		return min;
	}

	public double getSum() {
		return sum;
	}

	public void sample(double d) {
		sum += d;
		count++;
		max = Math.max(max, d);
		min = Math.min(max, d);
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean title) {
		if( title ) { return "Count: " + count //
				+ "\tSum: " + sum //
				+ "\tAvg: " + avg() //
				+ "\tMin: " + min //
				+ "\tMax: " + max //
		; }

		return count //
				+ "\t" + sum //
				+ "\t" + avg() //
				+ "\t" + min //
				+ "\t" + max //
		;

	}

}
