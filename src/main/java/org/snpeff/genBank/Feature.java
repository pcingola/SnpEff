package org.snpeff.genBank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snpeff.util.Gpr;

/**
 * A feature in a GenBank or EMBL file
 *
 * @author pablocingolani
 */
public class Feature implements Iterable<FeatureCoordinates> {

	public enum Type {
		SOURCE, ID, CDS, GENE, MRNA, TRNA, RRNA, MISC_RNA, REPEAT_UNIT, REPEAT_REGION, MISC_FEATURE, UTR_3, UTR_5, MAT_PEPTIDE;

		/**
		 * Parse a string into a Feature.Type
		 */
		public static Feature.Type parse(String typeStr) {
			typeStr = typeStr.toUpperCase();
			typeStr = typeStr.replaceAll("[^A-Za-z0-9]", "_");

			// Some equivalences
			if (typeStr.equals("5_UTR")) return UTR_5;
			if (typeStr.equals("3_UTR")) return UTR_3;
			if (typeStr.equals("SQ")) return SOURCE;

			try {
				return Feature.Type.valueOf(typeStr);
			} catch (Exception e) {
				return null;
			}
		}
	}

	static final String FEATURE_REGEX = "/([^=/\\s]*)(=?[^=\\n]*)";
	static final Pattern FEATURE_PATTERN = Pattern.compile(FEATURE_REGEX);;
	public static final String COMPLEMENT_STRING = "complement";

	Type type;
	int start, end;
	int lineNum;
	HashMap<String, String> qualifiers;
	boolean complement;
	List<FeatureCoordinates> featureCoordinates;

	public Feature(Type type, String def) {
		this.type = type;
		qualifiers = new HashMap<>();
		start = -1;
		end = -1;
		complement = false;
		parse(def);
	}

	public Feature(Type type, String def, int start, int end, boolean complement, int lineNum) {
		this.type = type;
		qualifiers = new HashMap<>();
		this.complement = complement;

		// Assign start & end
		if (end < start) { // Order reversed? Swap them
			int tmp = end;
			end = start;
			start = tmp;
		}
		this.start = start;
		this.end = end;
		this.lineNum = lineNum;

		// Parse
		parse(def);

		// Sanity check
		if (start < 0) throw new RuntimeException("Feature starts with negative coordinates!\n\t" + this);
	}

	public void add(FeatureCoordinates fc) {
		if (featureCoordinates == null) featureCoordinates = new LinkedList<>();
		featureCoordinates.add(fc);
	}

	/**
	 * Get a qualifier by name
	 */
	public String get(String name) {
		return qualifiers.get(name);
	}

	/**
	 * Get translated amino acid sequence
	 */
	public String getAasequence() {
		return get("translation");
	}

	public int getEnd() {
		return end;
	}

	public String getGeneId() {
		// Try ID
		String geneId = null;

		if (type == Type.GENE) geneId = get("id");
		if (geneId != null) return geneId;

		// Try 'locus'...
		geneId = get("locus_tag");
		if (geneId != null) return geneId;

		// Try 'db_xref'...
		geneId = get("db_xref");
		if (geneId != null) return geneId;

		return null;
	}

	/**
	 * Get gene name from feature
	 */
	public String getGeneName() {
		// Try 'gene'...
		String geneName = get("gene");
		if (geneName != null) return geneName;

		// Try 'gene'...
		geneName = get("gene_synonym");
		if (geneName != null) return geneName;

		return getGeneId();
	}

	/**
	 * Create an ID based on a feature
	 */
	public String getMaturePeptideId() {
		String trId = get("transcript_id");
		if (trId != null) return trId;

		// Try 'protein'...
		trId = get("protein_id");
		if (trId != null) return trId;

		trId = get("product");
		if (trId != null) return trId.replaceAll("\\s", "_");

		// Try 'locus'...
		trId = get("locus_tag");
		if (trId != null) return trId;

		return "tr_line_" + lineNum;
	}

	public int getStart() {
		return start;
	}

	/**
	 * Create a transcript ID based on a feature
	 */
	public String getTranscriptId() {
		// Try transcript ID
		String trId = get("transcript_id");
		if (trId != null) return trId;

		// Try 'locus'...
		trId = get("locus_tag");
		if (trId != null) return trId;

		// Try 'protein'...
		trId = get("protein_id");
		if (trId != null) return trId;

		// Try 'db_xref'...
		trId = get("db_xref");
		if (trId != null) return trId;

		trId = get("product");
		if (trId != null) return trId.replaceAll("\\s", "_");

		return "tr_line_" + lineNum;
	}

	public Type getType() {
		return type;
	}

	public boolean hasMultipleCoordinates() {
		return featureCoordinates != null;
	}

	public boolean isComplement() {
		return complement;
	}

	public boolean isRibosomalSlippage() {
		return get("ribosomal_slippage") != null;
	}

	@Override
	public Iterator<FeatureCoordinates> iterator() {
		return featureCoordinates.iterator();
	}

	/**
	 * Parse definition
	 */
	void parse(String def) {
		int firstLine = def.indexOf("\n");

		// Parse location (first line), if required
		if ((start < 0) && (end < 0)) {
			String loc = def.substring(0, firstLine);
			parseLocation(loc);
		}

		//---
		// Parse all other features
		//---
		def = def.substring(firstLine + 1);
		Matcher matcher = FEATURE_PATTERN.matcher(def);
		while (matcher.find()) {
			if (matcher.groupCount() >= 2) {
				String key = matcher.group(1).toLowerCase();
				String value = matcher.group(2);

				if (value == null) value = "";
				if (value.startsWith("=")) value = value.substring(1); // Remove leading "=" sign
				if (value.startsWith("\"") && value.endsWith("\"")) value = value.substring(1, value.length() - 1); // Remove surrounding quotes

				qualifiers.put(key, value.trim());
			}
		}
	}

	/**
	 * Parse location
	 */
	void parseLocation(String loc) {
		loc = loc.replaceAll("[<>()]", "");
		if (loc.startsWith("complement")) {
			complement = true;
			loc = loc.substring(COMPLEMENT_STRING.length());
		}

		String se[] = loc.split("[\\.]+");
		if (se.length > 1) {
			start = Gpr.parseIntSafe(se[0]);
			end = Gpr.parseIntSafe(se[1]);
		}
	}

	/**
	 * Remove surrounding quotes from a string
	 */
	String removeQuotes(String s) {
		if (s.startsWith("\"")) s = s.substring(1);
		if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
		return s;
	}

	@Override
	public String toString() {
		String format = "\t%-20s: \"%s\"\n";
		StringBuilder sb = new StringBuilder();

		sb.append("Feature (line " + lineNum + "): '" + type //
				+ "' [ " + start + ", " + end + " ]\t" //
				+ (complement ? "complement" : "") //
				+ "\n" //
		);

		// More coordinates?
		if (featureCoordinates != null) {
			sb.append(String.format(format, "coordinates", "join"));
			for (FeatureCoordinates fc : featureCoordinates)
				sb.append(String.format(format, "", fc));
		}

		for (Entry<String, String> e : qualifiers.entrySet())
			sb.append(String.format(format, e.getKey(), e.getValue()));

		return sb.toString();
	}
}
