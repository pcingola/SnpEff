package org.snpeff.spliceSites;

import java.util.ArrayList;
import java.util.List;

import org.snpeff.util.Log;

/**
 * ACGT tree
 *
 * @author pcingola
 */
public class AcgtTree {

	public static final char BASES[] = { 'A', 'C', 'G', 'T' };
	public static final double LOG2 = Math.log(2.0);
	public static final int FAKE_COUNTS = 1;
	public static final double MAX_ENTROPY = 2.0; // Maximum possible entropy for 4 symbols: - 4 * 1/4 * log2(1/4)

	String name;
	AcgtTree nodes[];
	int counts[];
	int totalCount;
	AcgtTree parent;

	public static int base2index(char base) {
		switch (Character.toUpperCase(base)) {
		case 'A':
			return 0;
		case 'C':
			return 1;
		case 'G':
			return 2;
		case 'T':
			return 3;
		}
		throw new RuntimeException("Unknown base '" + base + "'");
	}

	public AcgtTree() {
		name = "";
		nodes = new AcgtTree[4];
		counts = new int[4];
		parent = null; // Root of the tree
	}

	protected AcgtTree(String name, AcgtTree parent) {
		this.name = name;
		this.parent = parent;
		nodes = new AcgtTree[4];
		counts = new int[4];
	}

	public void add(String sequence) {
		if ((sequence == null) || sequence.isEmpty()) {
			totalCount++;
			return;
		}

		// Count for this node
		char base = sequence.charAt(0);
		inc(base);
		AcgtTree node = getOrCreate(base);

		// Recurse into tree
		node.add(sequence.substring(1));
	}

	/**
	 * Calculate the entropy
	 */
	public double entropy() {
		double entropy = 0;
		for (double inf : informationContent())
			entropy += inf;

		return entropy;
	}

	public List<Double> entropyAll(int thresholdCount) {
		ArrayList<Double> entropies = new ArrayList<Double>();
		entropyAll(thresholdCount, entropies);
		return entropies;
	}

	void entropyAll(int thresholdCount, ArrayList<Double> entropies) {
		if (totalCount >= thresholdCount) entropies.add(entropy());
		for (AcgtTree node : nodes)
			if (node != null) node.entropyAll(thresholdCount, entropies);
	}

	/**
	 * Find node names that are within the thresholds
	 */
	public List<String> findNodeNames(double thresholdEntropy, double thresholdP, int thresholdCount) {
		ArrayList<String> names = new ArrayList<String>();
		if (getTotalCount() == 0) return names;

		double p[] = p();
		for (int idx = 0; idx < 4; idx++) {
			AcgtTree n = nodes[idx];
			if (n != null) {
				if (((parent == null) || (parent.entropy() <= thresholdEntropy)) // Parent's entropy is low enough?
						&& (p[idx] >= thresholdP) // Probability is high enough?
						&& (counts[idx] >= thresholdCount) // Do we have enough counts?
				) {
					names.add(n.name);
				}

				names.addAll(n.findNodeNames(thresholdEntropy, thresholdP, thresholdCount));
			}
		}

		return names;
	}

	/**
	 * Get a node
	 */
	public AcgtTree get(char base) {
		return nodes[base2index(base)];
	}

	/**
	 * Get node indexed by this string
	 */
	public AcgtTree get(String bases) {
		if (bases.isEmpty()) return this;
		char base = bases.charAt(0);
		AcgtTree node = get(base);
		if (node == null) return null;
		return node.get(bases.substring(1));
	}

	/**
	 * Get a node (create it if it doesn't exist)
	 */
	public AcgtTree getOrCreate(char base) {
		AcgtTree node = get(base);
		if (node != null) return node;

		// Create node
		node = new AcgtTree(name + base, this);
		set(base, node);
		return node;
	}

	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * Increment counter for a base
	 */
	public void inc(char base) {
		counts[base2index(base)]++;
		totalCount++;
	}

	double[] informationContent() {
		double inf[] = new double[4];
		double p[] = p();
		for (int i = 0; i < 4; i++)
			inf[i] += -p[i] * Math.log(p[i]) / LOG2;

		return inf;
	}

	double[] p() {
		int tot = 0;
		for (int c : counts) {
			c += FAKE_COUNTS;
			tot += c;
		}

		double p[] = new double[4];
		int i = 0;
		for (int c : counts) {
			c += FAKE_COUNTS;
			p[i] = ((double) c) / tot;
			i++;
		}

		return p;
	}

	public List<Double> pAll(int thresholdCount) {
		ArrayList<Double> ps = new ArrayList<Double>();
		pAll(thresholdCount, ps);
		return ps;
	}

	protected void pAll(int thresholdCount, List<Double> ps) {
		for (int i = 0; i < nodes.length; i++) {
			AcgtTree node = nodes[i];
			double p[] = p();
			if (node != null) {
				if (counts[i] >= thresholdCount) ps.add(p[i]);
				node.pAll(thresholdCount, ps);
			}
		}
	}

	public double seqConservation() {
		return (MAX_ENTROPY - entropy()) / MAX_ENTROPY;
	}

	/**
	 * Set a node
	 */
	public void set(char base, AcgtTree n) {
		nodes[base2index(base)] = n;
	}

	@Override
	public String toString() {
		return toString("", 2.0, 1.0, 0);
	}

	public String toString(String tabs, double thresholdEntropy, double thresholdP, int thresholdCount) {
		if (getTotalCount() == 0) return "";

		StringBuilder sb = new StringBuilder();
		double p[] = p();
		for (int idx = 0; idx < 4; idx++) {
			char base = BASES[idx];
			AcgtTree n = nodes[idx];
			if (n != null) {
				sb.append(String.format("%s%s%s: %d\te:%4.3f\tp:%4.2f\n", tabs, name, base, counts[idx], n.entropy(), p[idx]));

				if (((n.entropy() <= thresholdEntropy) || (p[idx] >= thresholdP)) //
						&& (counts[idx] >= thresholdCount) //
				) {
					Log.debug("Name:" + n.name + "\tIdx:" + +idx + "\tEntropy: " + n.entropy() + "\tP:" + p[idx] + "\tCount:" + counts[idx]);
					sb.append(n.toString(tabs + "\t", thresholdEntropy, thresholdP, thresholdCount));
				}

			}
		}

		return sb.toString();
	}
}
