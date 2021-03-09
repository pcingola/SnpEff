package org.snpeff.ped;

import java.util.Collection;
import java.util.HashMap;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * PLINK MAP file
 *
 * References: http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml
 *
 * @author pcingola
 */
public class PlinkMap {

	public static boolean debug = false;

	HashMap<String, Integer> id2LineNumber;
	String chrNames[];
	int positions[];
	String ids[];

	public PlinkMap() {
		id2LineNumber = new HashMap<String, Integer>();
	}

	public PlinkMap(String mapFileName) {
		read(mapFileName);
	}

	public String getChrName(int idx) {
		return chrNames[idx];
	}

	public Collection<String> getGenotypeNames() {
		return id2LineNumber.keySet();
	}

	public Integer getGenotypeNames(String idStr) {
		return id2LineNumber.get(idStr);
	}

	public String getId(int idx) {
		return ids[idx];
	}

	public int getPosition(int idx) {
		return positions[idx];
	}

	/**
	 * Reads MAP file
	 *
	 * MAP file format (http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml)
	 *
	 *  Space separated or tab columns:
	 *		chromosome (1-22, X, Y or 0 if unplaced)
	 *		rs# or snp identifier
	 *		Genetic distance (morgans)
	 *		Base-pair position (bp units)
	 *
	 *
	 * @param dataFileName
	 */
	protected void read(String mapFileName) {
		// Read the whole file and split lines
		String cols = Gpr.readFile(mapFileName);
		String lines[] = cols.split("\n");

		// Initialize data
		positions = new int[lines.length];
		chrNames = new String[lines.length];
		ids = new String[lines.length];
		id2LineNumber = new HashMap<String, Integer>();

		int lineNum = 0;
		for (String line : lines) {
			String fields[] = line.split("\\s");

			chrNames[lineNum] = fields[0];
			ids[lineNum] = fields[1];
			positions[lineNum] = Gpr.parseIntSafe(fields[fields.length - 1]);

			String id = ids[lineNum];

			if (!id.isEmpty()) {
				// Is it duplicate?
				if (id2LineNumber.containsKey(id)) throw new RuntimeException("Duplicate ID '" + id + "'. File '" + mapFileName + "', line '" + (lineNum + 1) + "'");
				id2LineNumber.put(id, lineNum);
				if (debug) Log.debug("genotypeNames.put(" + id + ", " + lineNum + ")");
			}
			lineNum++;
		}
	}

	public int size() {
		return positions.length;
	}

}
