package org.snpeff.genBank;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.util.Tuple;

/**
 * A class representing a set of features
 *
 * References: http://www.ebi.ac.uk/embl/Documentation/FT_definitions/feature_table.html
 *
 * @author pablocingolani
 */
public abstract class Features implements Iterable<Feature> {

	public static boolean debug = false;

	public static final int MAX_LEN_TO_SHOW = 200;
	public static final String COMPLEMENT = "complement";
	public static final String JOIN = "join";
	public static final String ORDER = "order";

	String locusName, moleculeType, shape, division, date;
	int sequenceLength;
	int featuresStartLine = -1;
	String definition = "";
	String accession = "";
	String version = "";
	String keywords = "";
	String source = "";
	String organism = "";
	StringBuffer featuresStr;
	StringBuffer sequence;
	ArrayList<Feature> features;
	ArrayList<StringBuffer> references;
	LineFileIterator lineFileIterator;

	/**
	 * Create features from a file
	 */
	public Features(LineFileIterator lineFileIterator) {
		references = new ArrayList<>();
		featuresStr = new StringBuffer();
		sequence = new StringBuffer();
		features = new ArrayList<>();
		this.lineFileIterator = lineFileIterator;
		readFile();
	}

	/**
	 * Create features from a file
	 */
	public Features(String fileName) {
		references = new ArrayList<>();
		featuresStr = new StringBuffer();
		sequence = new StringBuffer();
		features = new ArrayList<>();
		open(fileName);
		readFile();
	}

	/**
	 * Create and add a feature
	 */
	void addFeature(String typeStr, StringBuilder values, int lineNum) {
		Feature.Type type = Feature.Type.parse(typeStr);
		if (type == null) {
			if (debug) Log.debug("WARNING: Unknown feature '" + typeStr + "', not added.");
			return;
		}

		try {
			Feature newFeature = featureFactory(type, values.toString(), lineNum); // Create new feature
			features.add(newFeature); // Add features to list
		} catch (Exception e) {
			Log.debug("Error parsing feature type '" + typeStr + "' -> '" + type + "':\n" + values);
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create features from a 'type' and 'values'
	 */
	Feature featureFactory(Feature.Type type, String def, int lineNum) {
		boolean complement = false;

		// Get first line (location)
		int firstLine = def.indexOf("\n");

		String locStr = (firstLine >= 0) ? def.substring(0, firstLine) : def;

		// Get rid of 'join' and 'complement' strings
		Tuple<String, Boolean> stripped = strip(locStr);
		locStr = stripped.first;
		complement = stripped.second;

		// Split multiple locations?
		String locs[] = locStr.split(",");
		List<FeatureCoordinates> fcList = new LinkedList<>();
		int startMin = Integer.MAX_VALUE, endMax = 0;
		for (String loc : locs) {
			// Get rid of 'join' and 'complement' strings
			stripped = strip(loc);
			loc = stripped.first;
			complement |= stripped.second;

			// Remove other characters
			loc = loc.replaceAll("[<>()]", "");

			// Calculate start & end coordinates
			String startEnd[] = loc.split("[\\.]+");

			int start, end;
			if (startEnd.length == 2) {
				start = Gpr.parseIntSafe(startEnd[0]);
				end = Gpr.parseIntSafe(startEnd[1]);
			} else if (startEnd.length == 1) {
				start = Gpr.parseIntSafe(startEnd[0]);
				end = start;
			} else throw new RuntimeException("Cannot calculate start & end coordinates: '" + loc + "'");

			startMin = Math.min(startMin, start);
			endMax = Math.max(endMax, end);

			// Create feature
			fcList.add(new FeatureCoordinates(start, end, complement));
		}

		// Create feature
		Feature feature = new Feature(type, def, startMin, endMax, complement, lineNum);

		// Add all coordinates (if multiple)
		if (fcList.size() > 1) {
			for (FeatureCoordinates fc : fcList)
				feature.add(fc);
		}

		return feature;
	}

	public String getAccession() {
		return accession;
	}

	public String getDate() {
		return date;
	}

	public String getDefinition() {
		return definition;
	}

	public String getDivision() {
		return division;
	}

	public ArrayList<Feature> getFeatures() {
		return features;
	}

	public String getKeywords() {
		return keywords;
	}

	public String getLocusName() {
		return locusName;
	}

	public String getMoleculeType() {
		return moleculeType;
	}

	public String getOrganism() {
		return organism;
	}

	public ArrayList<StringBuffer> getReferences() {
		return references;
	}

	public String getSequence() {
		return sequence.toString();
	}

	public int getSequenceLength() {
		return sequenceLength;
	}

	public String getShape() {
		return shape;
	}

	public String getSource() {
		return source;
	}

	public String getVersion() {
		return version;
	}

	public boolean isEmpty() {
		return features.isEmpty();
	}

	/**
	 * Is there a new feature in this line?
	 */
	protected abstract boolean isNewFeature(String line);

	@Override
	public Iterator<Feature> iterator() {
		return features.iterator();
	}

	/**
	 * Open a file
	 */
	protected void open(String fileName) {
		if (!Gpr.canRead(fileName)) throw new RuntimeException("Cannot read file '" + fileName + "'");
		if (lineFileIterator != null) lineFileIterator.close();
		lineFileIterator = new LineFileIterator(fileName);
	}

	/**
	 * Parse features
	 */
	protected void parseFeatures() {
		// Empty?
		if (featuresStr.length() <= 0) return;

		String type = null;
		String value = "";
		StringBuilder values = new StringBuilder();
		int lineNum = featuresStartLine;
		for (String line : featuresStr.toString().split("\n")) {
			lineNum++;
			if (debug) Log.debug("Line:" + lineNum + "\tLine:" + line);

			// Feature start
			if (isNewFeature(line)) {
				String kv[] = line.trim().split(" ", 2);
				if (kv.length > 1) {
					// Previous feature data is available? => Add it
					if (type != null) addFeature(type, values, lineNum);

					// Parse new feature's name
					type = kv[0];
					value = kv[1].trim();
				} else {
					// New type
					type = line.trim();
					value = "";
				}

				// New values
				values = new StringBuilder();
			} else value = line.trim();

			// Append values to feature
			if (value.startsWith("/")) values.append("\n");
			values.append(value);
		}

		// Add last feature
		addFeature(type, values, lineNum);
	}

	/**
	 * Load and parse the contents of a data file previously opened by 'open()' method.
	 */
	protected abstract void readFile();

	/**
	 * Remove start string
	 */
	String removeStartStr(String str, String startStr) {
		if (str.startsWith(startStr)) return str.substring(startStr.length() + 1, str.length());
		return str;
	}

	/**
	 * Remove strings until we only have numbers
	 */
	Tuple<String, Boolean> strip(String loc) {
		String locPrev = "";
		boolean complement = false;

		while (!loc.equals(locPrev)) {
			locPrev = loc;

			// Is it a complement?
			if (loc.startsWith(COMPLEMENT)) {
				complement = true;
				loc = removeStartStr(loc, COMPLEMENT);
			}

			loc = removeStartStr(loc, JOIN); // Remove 'join'
			loc = removeStartStr(loc, ORDER); // Remove 'order'
		}

		return new Tuple<>(loc, complement);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name            : " + locusName + "\n");
		sb.append("Sequence length : " + sequence.length() + "\n");

		// Show references
		for (StringBuffer refsb : references) {
			sb.append("Reference       :\n");
			for (String l : refsb.toString().split("\n"))
				sb.append("                 " + l + "\n");
		}

		// Show (part of) sequence
		if (sequence.length() <= MAX_LEN_TO_SHOW) sb.append("Sequence        : " + sequence + "\n");
		else sb.append("Sequence        : " + sequence.substring(0, MAX_LEN_TO_SHOW) + "..." + "\n");

		// Show all features
		for (Feature f : features)
			sb.append(f);

		return sb.toString();
	}
}
