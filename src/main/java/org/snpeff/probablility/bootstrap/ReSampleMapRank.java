package org.snpeff.probablility.bootstrap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Re-sample statistic using ranks of scores (scores are double)
 * 
 * @author pablocingolani
 */
public class ReSampleMapRank extends ReSampleInt {

	HashMap<String, Double> scoreByName; // All name-score relations
	HashMap<String, Integer> rankByName;

	public ReSampleMapRank(HashMap<String, Double> scoresByName, int sampleSize) {
		super(null, sampleSize);
		scoreByName = scoresByName;

		// Create a list ordered by score
		ArrayList<String> names = new ArrayList<String>();
		names.addAll(scoresByName.keySet());
		Collections.sort(names, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {

				return scoreByName.get(arg0).compareTo(scoreByName.get(arg1));
			}
		});

		// Populate rank list
		rankByName = new HashMap<String, Integer>();
		scores = new int[names.size()];
		double latest = Double.NaN;
		int rank = 0;
		for( int i = 0; i < names.size(); i++ ) {
			String name = names.get(i);

			double score = scoresByName.get(name);
			if( latest != score ) rank++;
			latest = score;

			rankByName.put(name, rank);
			scores[i] = rank;
		}
	}

	public int score(Collection<String> names) {
		int sum = 0;
		for( String name : names ) {
			if( rankByName.get(name) == null ) throw new RuntimeException("Error: Entry '" + name + "' not found!");
			sum += rankByName.get(name);
		}
		return sum;
	}
}
