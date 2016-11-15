package org.snpeff.genBank;

import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.util.Gpr;

/**
 * A class representing the same data as a GenBank file (a 'GB' file)
 *
 * References: http://www.ncbi.nlm.nih.gov/Sitemap/samplerecord
 *
 * @author pablocingolani
 */
public class GenBank extends Features {

	public static final int FEATURE_NAME_FIELD_LEN = 20;

	public GenBank(LineFileIterator lineFileIterator) {
		super(lineFileIterator);
	}

	/**
	 * Create a Genbank record from a 'GB' file
	 */
	public GenBank(String fileName) {
		super(fileName);
	}

	/**
	 * Has this line a new feature?
	 */
	@Override
	protected boolean isNewFeature(String line) {
		// Extract feature string
		int end = Math.min(FEATURE_NAME_FIELD_LEN, line.length());
		String featName = line.substring(0, end).trim();

		// Emtpy? Not a new feature
		if (featName.isEmpty()) return false;

		// Is it a number? => Not a new feature
		featName = featName.split(" ")[0];
		if (Gpr.parseIntSafe(featName) > 0) return false;

		return true;
	}

	/**
	 * Parse a feature line
	 */
	protected void parseFieldLine(String name, String valueOri, int fieldLineNum, int fileLineNum) {
		String value = valueOri.trim();

		if (name.equals("LOCUS")) {
			String subfields[] = value.split(" ");
			locusName = subfields[0];
			if (subfields.length > 1) sequenceLength = Gpr.parseIntSafe(subfields[1]);
			if (subfields.length > 2) moleculeType = subfields[2];
			if (subfields.length > 3) shape = subfields[3];
			if (subfields.length > 4) division = subfields[4];
			if (subfields.length > 5) date = subfields[5];
		} else if (name.equals("DEFINITION")) {
			definition += value;
		} else if (name.equals("ACCESSION")) {
			accession += value;
		} else if (name.equals("VERSION")) {
			version += value;
		} else if (name.equals("KEYWORDS")) {
			keywords += value;
		} else if (name.equals("SOURCE")) {
			source += value;
		} else if (name.equals("REFERENCE")) {
			if (fieldLineNum == 0) references.add(new StringBuffer());
			references.get(references.size() - 1).append(value + "\n");
		} else if (name.equals("FEATURES")) {
			if (featuresStartLine < 0) featuresStartLine = fileLineNum;
			if (fieldLineNum > 0) featuresStr.append(valueOri + "\n"); // We need all spaces preserved for this field
		} else if (name.equals("ORIGIN")) {
			String seq[] = value.split(" ", 2);

			// First line might be empty
			if (seq.length > 1) {
				String s = seq[1].replaceAll("\\s", ""); // Remove all spaces
				sequence.append(s);
			}
		} else if (debug) System.err.println("Ignored feature '" + name + "'");;
	}

	/**
	 * Load and parse the contents of a data file
	 */
	@Override
	public void readFile() {
		int fieldLineNum = 0;
		String name = null;
		String value = "";
		if (debug) Gpr.debug("NAME: " + name + "\tvalue: " + value);

		// Read file
		for (String line : lineFileIterator) {
			// End of current 'chromosome'?
			if (line.startsWith("//")) break;

			value = line;

			// Field start
			if (!line.startsWith(" ")) {
				String kv[] = line.split(" ", 2);
				name = kv[0];
				value = (kv.length > 1 ? kv[1] : "");
				if (debug) Gpr.debug("Line: " + line + "\n\tNAME: " + name + "\tvalue: " + value);
				fieldLineNum = 0;
			}

			// Parse field
			if (name != null) {
				parseFieldLine(name, value, fieldLineNum, lineFileIterator.getLineNum());
				fieldLineNum++;
			}
		}

		// All features are loaded. We can parse them now
		parseFeatures();
	}
}