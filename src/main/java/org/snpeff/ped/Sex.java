package org.snpeff.ped;

import java.util.Random;

public enum Sex {

	Male, Female, Unknown;

	/**
	 * Random sex
	 * @return
	 */
	public static Sex rand(Random random) {
		if( random.nextDouble() >= 0.5 ) return Sex.Female;
		return Sex.Male;
	}

	/**
	 * Return the opposite sex
	 * @param sex
	 * @return
	 */
	public Sex opposite() {
		return this == Sex.Male ? Sex.Female : Sex.Male;
	}
}
