package ca.mcgill.mcb.pcingola.interval;

import java.util.HashMap;

/**
 * Convert chromosome names to simple names
 * @author pcingola
 */
public class ChromosomeSimpleName {

	public static final String CHROMO_PREFIX[] = { "chromosome", "chromo", "chr" }; //, "group", "scaffold", "contig", "supercontig", "supercont", "0" }; // Must be lower case (see method)
	private static ChromosomeSimpleName instance = new ChromosomeSimpleName();

	private final HashMap<String, String> map;

	/**
	 * Get a simple name for the chromosome
	 * @param chrName
	 * @return
	 */
	public static String get(String chrName) {
		return instance.simpleNameCache(chrName);
	}

	private ChromosomeSimpleName() {
		map = new HashMap<String, String>();
	}

	/**
	 * Simplify chromosome name
	 * @param chr
	 * @return
	 */
	protected String simpleName(String chr) {
		if (chr == null) return "";
		chr = chr.trim();

		// Remove any prefix string until no change is made
		String chrPrev = "";
		do {
			chrPrev = chr;

			// Remove all common prefixes
			for (String prefix : CHROMO_PREFIX) {
				String chName = chr.toLowerCase();

				if (chName.startsWith(prefix + ":")) chr = chr.substring(prefix.length() + 1);
				else if (chName.startsWith(prefix + "_")) chr = chr.substring(prefix.length() + 1);
				else if (chName.startsWith(prefix + "-")) chr = chr.substring(prefix.length() + 1);
				else if (chName.startsWith(prefix)) chr = chr.substring(prefix.length());
				else if (chName.startsWith("0")) chr = chr.substring(1);
			}
		} while (!chr.equals(chrPrev));

		return chr;
	}

	/**
	 * Query cache before simplifying name
	 * @param chrName
	 * @return
	 */
	protected String simpleNameCache(String chrName) {
		String chr = map.get(chrName);
		if (chr == null) {
			chr = simpleName(chrName);
			map.put(chrName, chr);
		}
		return chr;
	}

}
