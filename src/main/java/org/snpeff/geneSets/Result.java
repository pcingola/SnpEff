package org.snpeff.geneSets;

import java.util.ArrayList;
import java.util.List;

import org.apfloat.Apcomplex;
import org.apfloat.Apfloat;
import org.snpeff.util.Log;

/**
 * Store a result form a greedy search algorithm
 *
 * @author Pablo Cingolani
 */
public class Result implements Comparable<Result> {

	private List<GeneSet> geneSets;
	private Apfloat pValue;
	private List<Integer> geneSetCount;

	public Result() {
		geneSets = new ArrayList<GeneSet>();
		pValue = Apcomplex.ONE;
		geneSetCount = new ArrayList<Integer>();
	}

	public Result(GeneSet geneSet, Apfloat pValue, int geneSetCount) {
		geneSets = new ArrayList<GeneSet>();
		geneSets.add(geneSet);
		this.pValue = pValue;
		this.geneSetCount = new ArrayList<Integer>();
		this.geneSetCount.add(geneSetCount);
	}

	public Result(List<GeneSet> list, double pValue) {
		setGeneSets(list);
		this.pValue = new Apfloat(pValue);
		geneSetCount = new ArrayList<Integer>();
	}

	public Result(Result res) {
		setGeneSets(res.geneSets);
		setGeneSetCount(res.geneSetCount);
		pValue = res.pValue;
	}

	/**
	 * Add an item to the list of counts
	 * @param count
	 */
	public void addGeneSetCount(int count) {
		if (count > 0) geneSetCount.add(count);
	}

	@Override
	public int compareTo(Result res) {
		if ((pValue != null) && (res.pValue != null)) return pValue.compareTo(res.pValue);
		return 0;
	}

	public List<Integer> getGeneSetCount() {
		return geneSetCount;
	}

	public int getGeneSetCountLast() {
		if (geneSetCount.size() <= 0) return 0;

		// Set last element
		int index = geneSetCount.size() - 1;
		return geneSetCount.get(index);
	}

	public List<GeneSet> getGeneSets() {
		return geneSets;
	}

	public GeneSet getLatestGeneSet() {
		if ((geneSets == null) || (geneSets.size() == 0)) { return null; }
		return geneSets.get(geneSets.size() - 1);
	}

	public Apfloat getPvalue() {
		return pValue;
	}

	/**
	 * P-Value adjusted using
	 * @return
	 */
	public double getPvalueAdjusted() {
		if (geneSetCount.size() != geneSets.size()) {

			for (GeneSet gs : geneSets)
				Log.debug("Gene set: " + gs.getName());
			for (Integer c : geneSetCount)
				Log.debug("Gene set count: " + c);

			throw new RuntimeException("Incompatible gene count sizes." //
					+ "\n\tGeneSetCount.size : " + geneSetCount.size() //
					+ "\n\tGeneSets.size     : " + geneSets.size() //
			);
		}

		double adj = 1.0;
		for (int i = 0; i < geneSetCount.size(); i++)
			adj *= ((double) geneSetCount.get(i)) / (i + 1);

		return Math.min(1.0, pValue.doubleValue() * adj);
	}

	public double getPvalueDouble() {
		return pValue.doubleValue();
	}

	public boolean isEmpty() {
		return geneSets.isEmpty();
	}

	/**
	 * Ser a new list and pvalue
	 * @param geneSets
	 * @param pvalue
	 */
	public void set(List<GeneSet> geneSets, Apfloat pValue) {
		setGeneSets(geneSets);
		setPvalue(pValue);
	}

	public void setGeneSetCount(List<Integer> geneSetCount) {
		ArrayList<Integer> l = new ArrayList<Integer>();
		l.addAll(geneSetCount);
		this.geneSetCount = l;
	}

	public void setGeneSetCountLast(int count) {
		if (geneSetCount.size() <= 0) {
			geneSetCount.add(count);
			return;
		}

		// Set last element
		int index = geneSetCount.size() - 1;
		geneSetCount.set(index, count);
	}

	/**
	 * Assign geneSets
	 * @param newGeneSets
	 */
	public void setGeneSets(List<GeneSet> newGeneSets) {
		geneSets = new ArrayList<GeneSet>();
		geneSets.addAll(newGeneSets);
	}

	public void setPvalue(Apfloat pValue) {
		this.pValue = pValue;
	}

	public void setPvalue(double pValue) {
		this.pValue = new Apfloat(pValue);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Gene sets: " + geneSets.size());
		sb.append("\tpValue: " + pValue + "\t[");
		sb.append("\tpValue.adj: " + getPvalueAdjusted() + "\t[");
		sb.append("\t[");
		for (Integer count : geneSetCount)
			sb.append(" " + count);
		sb.append("]\t");

		for (GeneSet gs : geneSets)
			sb.append(" " + gs.getName() + "(" + gs.size() + ")");

		return sb.toString();
	}
}
