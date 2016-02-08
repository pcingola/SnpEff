package org.snpeff.probablility.bootstrap;

import java.util.Collection;
import java.util.HashMap;

/**
 * Resample statistic
 * 
 * @author pablocingolani
 */
public class ReSampleMap extends ReSampleInt {

	HashMap<String, Integer> scoreByName; // All name-score relations

	public ReSampleMap(HashMap<String, Integer> scoresByName, int sampleSize) {
		super(null, sampleSize);
		scoreByName = scoresByName;

		// Initialize scores array
		int i = 0;
		scores = new int[scoresByName.size()];
		for( Integer score : scoresByName.values() )
			scores[i++] = score;
	}

	public int score(Collection<String> names) {
		int sum = 0;
		for( String name : names ) {
			if( scoreByName.get(name) == null ) throw new RuntimeException("Error: Entry '" + name + "' not found!");
			sum += scoreByName.get(name);
		}
		return sum;
	}
}
