package org.snpeff.gtex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Load data from GTEx files.
 *
 * References: http://www.broadinstitute.org/gtex/
 *
 * @author pcingola
 */
public class Gtex implements Iterable<GtexExperiment> {

	public static final String NA_VALUE = "NA";

	boolean verbose = false;
	HashMap<String, GtexExperiment> experiments;
	HashMap<String, Integer> geneId2Index;
	String geneIds[];

	public Gtex() {
		experiments = new HashMap<String, GtexExperiment>();
		geneId2Index = new HashMap<String, Integer>();
	}

	public Collection<String> getGeneIds() {
		return geneId2Index.keySet();
	}

	public int getIndex(String geneId) {
		return geneId2Index.get(geneId);
	}

	@Override
	public Iterator<GtexExperiment> iterator() {
		return experiments.values().iterator();
	}

	public void load(String samples, String data) {
		loadSamples(samples); // Sample definition file
		loadData(data); // Data file
	}

	/**
	 * Load data: Experimental (sometimes normalized) values
	 * @param data
	 */
	protected void loadData(String data) {
		if (verbose) Log.info("Loaded GTEx data from file " + data);

		if (!Gpr.canRead(data)) throw new RuntimeException("Cannot find data file '" + data + "'");

		geneId2Index = new HashMap<String, Integer>();

		// Open file
		LineFileIterator lfi = new LineFileIterator(data);
		String exIds[] = null;
		int index = 0;
		for (String line : lfi) {
			String fields[] = line.split("\t", -1); // Parse fields

			if (lfi.getLineNum() <= 1) {
				// Parse title
				exIds = fields; // Sort experiment IDs

				// For some reason the IDs in one file are like 'GTEX-N7MS-0007-SM-2D7W1' and in the other file are like 'GTEX.N7MS.0007.SM.2D7W1'
				for (int i = 0; i < exIds.length; i++)
					exIds[i] = exIds[i].replace('.', '-');
			} else {
				// Parse gene ID
				String geneId = fields[0];
				geneId = geneId.split("\\.")[0]; // Remove trailing ".X" form geneID, e.g.:   "ENSG00000228463.3" --> "ENSG00000228463"

				// Add to map(ID => index)
				geneId2Index.put(geneId, index);

				// Add data for each experiment
				for (int i = 2; i < fields.length; i++) {
					// Get appropriate experiment
					String exId = exIds[i];
					GtexExperiment gtexExperiment = experiments.get(exId);

					// Sanity check
					if (gtexExperiment == null) throw new RuntimeException("Cannot find experiment '" + exId + "'");
					if (gtexExperiment.size() != index) throw new RuntimeException("Index doesn't match: " + gtexExperiment.size() + "!=" + index);

					// Parse data value
					double value = Double.NaN;
					if (!fields[i].equals(NA_VALUE)) value = Gpr.parseDoubleSafe(fields[i]);

					// Add value to experiment
					gtexExperiment.add(value);
				}
				index++;
			}
		}

		if (verbose) Log.info("Done. Loaded " + experiments.size() + " experiments.");
	}

	/**
	 * Load samples names
	 * @param samples
	 */
	protected void loadSamples(String samples) {
		if (verbose) Log.info("Loaded GTEx experiments from file " + samples);

		if (!Gpr.canRead(samples)) throw new RuntimeException("Cannot find samples file '" + samples + "'");

		experiments = new HashMap<String, GtexExperiment>();
		LineFileIterator lfi = new LineFileIterator(samples);
		for (String line : lfi) {
			if (lfi.getLineNum() <= 1) continue; // Ignore title

			GtexExperiment gtexExperiment = new GtexExperiment(this, line);
			experiments.put(gtexExperiment.getId(), gtexExperiment);
		}

		if (verbose) Log.info("Done. Loaded " + experiments.size() + " experiments.");
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (GtexExperiment gex : experiments.values())
			sb.append(gex + "\n");
		return sb.toString();
	}
}
