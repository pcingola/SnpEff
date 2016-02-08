package org.snpeff.snpEffect;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.snpeff.stats.IntStats;
import org.snpeff.util.Gpr;

/**
 * Histogram of integer numbers
 * @author pcingola
 */
public class IntHisto {

	public static void main(String[] args) {
		IntStats intStats = new IntStats();

		// Default input is STDIN
		InputStream in = System.in;

		// Input from file?
		if( args.length > 0 ) try {
			in = new FileInputStream(new File(args[0]));
		} catch(FileNotFoundException e1) {
			throw new RuntimeException("Error opening file '" + args[0] + "'");
		}

		// Read each line
		BufferedReader stdin = new BufferedReader(new InputStreamReader(in));
		try {
			String line;
			while((line = stdin.readLine()) != null) {
				double d = Gpr.parseDoubleSafe(line); // Parse as 'double', then convert to int
				intStats.sample((int) d); // Do stats
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		// Show stats
		System.out.println(intStats.toString());
		System.out.println(intStats.toStringPlot("Histogram", "", true));
	}
}
