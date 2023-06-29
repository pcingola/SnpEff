package org.snpeff.interval;

import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An interval intended as a mark
 *
 * @author pcingola
 *
 */
public class Gtf2Marker extends GffMarker {

	private static final long serialVersionUID = 5416962964309837838L;
	static final String TAG_KEY = "tag";
	static final String ATTRIBUTE_PATTERN_REGEX = "\\s*(\\S+)\\s+\"(.*?)\"\\s*;";
	static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(ATTRIBUTE_PATTERN_REGEX);

	public Gtf2Marker() {
		super();
	}

	public Gtf2Marker(Genome genome, String line) {
		super(genome, line);
	}

	public Gtf2Marker(Marker parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
	}

	/** Does the key have mutiple values */
	public boolean isMultipleValues(String key) {
		return key.equals(TAG_KEY);  // Only the 'tag' key is allowed multiple value
	}

	/**
	 * Parse attributes
	 */
	@Override
	protected void parseAttributes(String attrStr) {
		keyValues = new HashMap<>();
		keys = new HashSet<String>();

		if (attrStr.length() > 0) {
			Matcher matcher = ATTRIBUTE_PATTERN.matcher(attrStr);
			while (matcher.find()) {
				if (matcher.groupCount() >= 2) {
					String key = matcher.group(1).toLowerCase();
					String value = matcher.group(2);
					add(key, value);
				}
			}
		}
	}

}
