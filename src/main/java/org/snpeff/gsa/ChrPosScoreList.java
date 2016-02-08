package org.snpeff.gsa;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

import org.snpeff.interval.Chromosome;

/**
 * A list of <chromosome, position, scores> 
 * 
 * @author pcingola
 */
public class ChrPosScoreList {

	ArrayList<Chromosome> chromosomes;
	TIntArrayList starts;
	TIntArrayList ends;
	TDoubleArrayList scores;

	public ChrPosScoreList() {
		chromosomes = new ArrayList<Chromosome>();
		starts = new TIntArrayList();
		ends = new TIntArrayList();
		scores = new TDoubleArrayList();
	}

	public void add(Chromosome chr, int start, int end, double score) {
		chromosomes.add(chr);
		starts.add(start);
		ends.add(start);
		scores.add(score);
	}

	public Chromosome getChromosome(int index) {
		return chromosomes.get(index);
	}

	public String getChromosomeName(int index) {
		return chromosomes.get(index).getId();
	}

	public int getEnd(int index) {
		return ends.get(index);
	}

	public double getScore(int index) {
		return scores.get(index);
	}

	public int getStart(int index) {
		return starts.get(index);
	}

	public int size() {
		return chromosomes.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < size(); i++)
			sb.append(getChromosomeName(i) + "\t" + getStart(i) + "\t" + getEnd(i) + "\t" + getScore(i) + "\n");

		return sb.toString();
	}

}
