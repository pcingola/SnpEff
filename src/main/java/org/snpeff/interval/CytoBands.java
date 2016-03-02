package org.snpeff.interval;

import org.snpeff.fileIterator.BedFileIterator;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Timer;

/**
 * Cytband definitions
 * E.g.: http://hgdownload.soe.ucsc.edu/goldenPath/hg38/database/cytoBand.txt.gz
 *
 * @author pcingola
 */
public class CytoBands {

	public static final String DEFAULT_CYTOBAND_BED_FILE = "cytoBand.txt";

	boolean verbose;
	IntervalForest forest;

	public CytoBands(Config config) {
		verbose = config.isVerbose();
		forest = new IntervalForest();

		if (config.getCoordinates() != null) {
			String cytoBandFile = config.getDirDataGenomeVersion() + "/" + DEFAULT_CYTOBAND_BED_FILE;
			load(cytoBandFile);
		}
	}

	/**
	 * Load cytobands form BED interval
	 */
	void load(String bedFile) {
		if (verbose) Timer.showStdErr("Loading cytobands form file '" + bedFile + "'");
		BedFileIterator bed = new BedFileIterator(bedFile);

		int count = 0;
		for (Variant var : bed) {
			forest.add(var);
			count++;
		}

		if (verbose) {
			if (count <= 0) Timer.showStdErr("WARNING: Unable to load cytobands from file '" + bedFile + "'");
			else Timer.showStdErr("Loaded " + count + " cytoband intervals");
		}

		forest.build();
	}

}
