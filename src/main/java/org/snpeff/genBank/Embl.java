package org.snpeff.genBank;

import org.snpeff.fileIterator.LineFileIterator;

/**
 * A class representing the same data as an EMBL file
 *
 * References: http://www.ebi.ac.uk/embl/Documentation/User_manual/usrman.html
 *
 * @author pablocingolani
 */
public class Embl extends Features {

	public static final int FEATURE_NAME_LEN = 15;
	public static final int FEATURE_KEY_LEN = 5;

	/**
	 * Create a Genbank record from a 'GB' file
	 * @param fileName
	 */
	public Embl(LineFileIterator lineFileIterator) {
		super(lineFileIterator);
	}

	/**
	 * Create a Genbank record from a 'GB' file
	 * @param fileName
	 */
	public Embl(String fileName) {
		super(fileName);
	}

	/**
	 * Has this line a new feature?
	 * @param line
	 * @return
	 */
	@Override
	protected boolean isNewFeature(String line) {
		return !line.substring(0, FEATURE_NAME_LEN).trim().isEmpty(); // Feature name should be within the first 20 characters
	}

	/**
	 * Parse a feature line
	 *
	 * Line types:

			 ID - identification             (begins each entry; 1 per entry)
		     AC - accession number           (>=1 per entry)
		     PR - project identifier         (0 or 1 per entry)
		     DT - date                       (2 per entry)
		     DE - description                (>=1 per entry)
		     KW - keyword                    (>=1 per entry)
		     OS - organism species           (>=1 per entry)
		     OC - organism classification    (>=1 per entry)
		     OG - organelle                  (0 or 1 per entry)
		     RN - reference number           (>=1 per entry)
		     RC - reference comment          (>=0 per entry)
		     RP - reference positions        (>=1 per entry)
		     RX - reference cross-reference  (>=0 per entry)
		     RG - reference group            (>=0 per entry)
		     RA - reference author(s)        (>=0 per entry)
		     RT - reference title            (>=1 per entry)
		     RL - reference location         (>=1 per entry)
		     DR - database cross-reference   (>=0 per entry)
		     CC - comments or notes          (>=0 per entry)
		     AH - assembly header            (0 or 1 per entry)
		     AS - assembly information       (0 or >=1 per entry)
		     FH - feature table header       (2 per entry)
		     FT - feature table data         (>=2 per entry)
		     XX - spacer line                (many per entry)
		     SQ - sequence header            (1 per entry)
		     CO - contig/construct line      (0 or >=1 per entry)
		     bb - (blanks) sequence data     (>=1 per entry)
		     // - termination line           (ends each entry; 1 per entry)
	 *
	 */
	protected void parseFieldLine(String fkey, String valueOri, int fieldLineNum) {
		String value = valueOri.trim();

		if (fkey.equals("XX")) {
			// Nothing to do
		} else if (fkey.equals("ID")) {
			String subfields[] = value.split("[;\\s]");
			locusName = subfields[0];
			if (subfields.length > 1) shape = subfields[1];
			if (subfields.length > 2) moleculeType = subfields[2];
		} else if (fkey.equals("DT")) {
			date = value;
		} else if (fkey.equals("DT")) {
			date = value;
		} else if (fkey.equals("DE")) {
			definition += value;
		} else if (fkey.equals("AC")) {
			accession += value;
		} else if (fkey.equals("KW")) {
			keywords += value;
		} else if (fkey.equals("SQ")) {
			value = value.replaceAll("\\s", "");
			value = value.replaceAll("\\d", "");
			if (value.replaceAll("[acgtnACGTN]", "").isEmpty()) sequence.append(value); // Only accept {a,c,g,t,n}
			else if (sequence.length() > 0) System.err.println("Warning: Improper sequence value ignored:\n\t'" + valueOri + "'"); // Only first line has a summary of the information. Other lines should only have valid sequence characters
		} else if (fkey.equals("OS") | fkey.equals("OC")) {
			organism += value;
		} else if (fkey.equals("FT")) {
			if (featuresStartLine < 0) featuresStartLine = fieldLineNum;
			featuresStr.append(valueOri + "\n"); // We need all spaces preserved for this field
		} else if (fkey.equals("//")) {
			// End of file
		} else if (fkey.startsWith("R")) {
			if (fkey.equals("RN")) references.add(new StringBuffer());
			references.get(references.size() - 1).append(value + "\n");
		} else if (fkey.equals("FH") | fkey.equals("PR") | fkey.equals("DR") | fkey.equals("CC") | fkey.equals("AH") | fkey.equals("AS") | fkey.equals("SQ")) {
			// Ignore
		} else System.err.println("Unknown feature '" + fkey + "'");;
	}

	/**
	 * Load and parse the contents of a data file
	 * @param fileName
	 */
	@Override
	public void readFile() {
		String fkeyPrev = "";

		// Read file
		for (String line : lineFileIterator) {
			// End of current 'chromosome'?
			if (line.equals("//")) {
				break;
			}

			// Parse feature key
			String fkey = "";
			String value = "";

			if (line.length() < FEATURE_KEY_LEN) fkey = line;
			else {
				fkey = line.substring(0, FEATURE_KEY_LEN).trim();
				if (fkey.isEmpty()) fkey = fkeyPrev;
				value = line.substring(FEATURE_KEY_LEN);
			}

			// Parse line
			parseFieldLine(fkey, value, lineFileIterator.getLineNum());

			fkeyPrev = fkey;
		}

		// All features are loaded. We can parse them now
		parseFeatures();
	}

}