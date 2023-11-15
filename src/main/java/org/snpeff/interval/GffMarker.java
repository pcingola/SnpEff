package org.snpeff.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import org.snpeff.util.Gpr;
import org.snpeff.util.KeyValue;

/**
 * An interval intended as a mark
 *
 * @author pcingola
 *
 */
public class GffMarker extends Custom {

	private static final long serialVersionUID = -164502778854644537L;
	public static final String MULTIPLE_VALUES_SEPARATOR = ";";
	public static final String FIELD_BIOTYPE = "biotype";
	public static final String FIELD_DB_XREF = "db_xref";
	public static final String FIELD_EXON_ID = "exon_id";
	public static final String FIELD_GENE = "gene";
	public static final String FIELD_GENE_BIOTYPE = "gene_biotype";
	public static final String FIELD_GENE_ID = "gene_id";
	public static final String FIELD_GENE_NAME = "gene_name";
	public static final String FIELD_GENE_TYPE = "gene_type";
	public static final String FIELD_ID = "ID";
	public static final String FIELD_NAME = "name";
	public static final String FIELD_NAMEU = "Name";
	public static final String FIELD_PARENT = "Parent";
	public static final String FIELD_PROTEIN_ID = "protein_id";
	public static final String FIELD_TAG = "tag";
	public static final String FIELD_TRANSCRIPT_ID = "transcript_id";
	public static final String FIELD_TRANSCRIPT_TSL = "transcript_support_level";
	public static final String FIELD_TRANSCRIPT_VERSION = "transcript_version";

	String source;
	int frame;
	GffType gffType;
	String gffTypeStr;
	Map<String, String> keyValues;
	Set<String> keys;

	/**
	 * Can this line (form a GFF file) be parsed?
	 * Note: Returns false for headers, empty lines and lines
	 *       having less number of fields than expected
	 */
	public static boolean canParseLine(String line) {
		// Ignore empty lines and comment lines
		if (line == null || line.isEmpty() || line.startsWith("#")) return false;

		// Split fields
		String fields[] = line.split("\t");
		if (fields.length < 9) return false;

		return true;
	}

	public GffMarker() {
		super();
	}

	public GffMarker(Genome genome, String line) {
		super();
		parse(genome, line);
	}

	public GffMarker(Marker parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id, "");
	}

	/**
	 * Add key value pair
	 */
	public void add(String key, String value) {
		if (keyValues == null) keyValues = new HashMap<>();
		if (keys == null) keys = new HashSet<>();

		keys.add(key); // Store original key (to preserve capitalization in 'Custom' annotations)
		if(isMultipleValues(key)) {
			// Pack multiple values into a single tab-separated string
			if (keyValues.containsKey(key)) {
                String oldValue = keyValues.get(key);
                String newValue = (oldValue != null ? oldValue + MULTIPLE_VALUES_SEPARATOR + value : value);
                keyValues.put(key, newValue);
            } else {
                keyValues.put(key, value);
            }
		} else {
			keyValues.put(key.toLowerCase(), value);
		}
	}

	public String getAttr(String key) {
		return keyValues.get(key.toLowerCase());
	}

	public BioType getBiotype() {
		if (gffType == GffType.GENE) return getGeneBiotype();
		if (gffType == GffType.TRANSCRIPT) return getTranscriptBiotype();
		return getBiotypeGeneric();
	}

	protected String getBioType() {
		return getAttr(FIELD_BIOTYPE);
	}

	protected BioType getBiotypeGeneric() {
		BioType bioType = null;

		// Use generic biotype field
		if (hasAttr(FIELD_BIOTYPE)) return BioType.parse(getAttr(FIELD_BIOTYPE));

		// Use 'source' as bioType (Old ENSEMBL GTF files use this field)
		bioType = BioType.parse(source);
		if (bioType != null) return bioType;

		// One last effor to try to inferr it from the GFF's 'type' column
		return BioType.parse(gffTypeStr);
	}

	public int getFrame() {
		return frame;
	}

	public BioType getGeneBiotype() {
		// Note: Different data providers use different keys
		// E.g.: ENSEMBL uses "gene_biotype" and GenCode uses "gene_type"
		String keys[] = { FIELD_GENE_BIOTYPE, FIELD_GENE_TYPE, FIELD_BIOTYPE };

		for (String key : keys)
			if (hasAttr(key)) return BioType.parse(getAttr(key));

		return getBiotypeGeneric();
	}

	public String getGeneId() {
		if (hasAttr(FIELD_GENE_ID)) return getAttr(FIELD_GENE_ID);
		if (gffType == GffType.GENE) return id;
		if (gffType == GffType.TRANSCRIPT) return getGffParentId(true);
		return GffType.GENE + "_" + id;
	}

	public String getGeneName() {
		if (hasAttr(FIELD_GENE_NAME)) return getAttr(FIELD_GENE_NAME);
		if (gffType == GffType.GENE && hasAttr(FIELD_NAME)) return getAttr(FIELD_NAME);
		if (gffType == GffType.TRANSCRIPT) {
			String pid = getGffParentId(true);
			if (pid != null) return pid;
		}
		return id;
	}

	public String getGffParentId(boolean doNotRecurse) {
		if (hasAttr(FIELD_PARENT)) return getAttr(FIELD_PARENT);

		switch (gffType) {
		case TRANSCRIPT:
		case INTRON_CONSERVED:
			if (hasAttr(FIELD_GENE)) return getAttr(FIELD_GENE);
			return doNotRecurse ? null : getGeneId(); // Avoid infinite recursion

		case EXON:
		case CDS:
		case START_CODON:
		case STOP_CODON:
		case UTR3:
		case UTR5:
			return doNotRecurse ? null : getTranscriptId();

		default:
			return null;
		}
	}

	/**
	 * Parent can have multiple, comma separated entries
	 */
	public String[] getGffParentIds() {
		String ids = getAttr(FIELD_PARENT);
		if (ids != null) return ids.split(",");

		// Nothing found? Try to find parentId
		String pid = getGffParentId(false);
		if (pid == null) return null; // Nothing found? Give up

		// Pack parentId into a String[]
		String pids[] = { pid };
		return pids;
	}

	public GffType getGffType() {
		return gffType;
	}

	public String getProteinId() {
		return getAttr(FIELD_PROTEIN_ID);
	}

	public BioType getTranscriptBiotype() {
		// Note: Different data providers use different keys
		// E.g.: ENSEMBL uses "transcript_biotype" and GenCode uses "transcript_type"
		String keys[] = { "transcript_biotype", "transcript_type", "biotype" };

		for (String key : keys)
			if (hasAttr(key)) return BioType.parse(getAttr(key));

		return getBiotypeGeneric();
	}

	public String getTags() {
        return getAttr(FIELD_TAG);
    }

	public String getTranscriptTsl() {
        return getAttr(FIELD_TRANSCRIPT_TSL);
    }

	public String getTranscriptId() {
		if (hasAttr(FIELD_TRANSCRIPT_ID)) return getAttr(FIELD_TRANSCRIPT_ID);
		if (gffType == GffType.TRANSCRIPT) return id;
		if (gffType == GffType.EXON) return getGffParentId(true);
		return GffType.TRANSCRIPT + "_" + id;
	}

	public String getTranscriptVersion() {
		return getAttr(FIELD_TRANSCRIPT_VERSION);
	}

	/**
	 * When annotating a VCF file, add fields from this GFF
	 */
	@Override
	public boolean hasAnnotations() {
		return true;
	}

	public boolean hasAttr(String key) {
		key = key.toLowerCase();
		return keyValues.containsKey(key) && (keyValues.get(key) != null);
	}

	/** Does the key have mutiple values */
	public boolean isMultipleValues(String key) {
		return false;
	}

	/**
	 * Is biotType considered 'protein coding'?
	 */
	public boolean isProteingCoding() {
		BioType bioType = getBiotype();
		return bioType != null && bioType.isProteinCoding();
	}

	@Override
	public Iterator<KeyValue<String, String>> iterator() {
		// Sort keys alphabetically
		ArrayList<String> keysSorted = new ArrayList<>();
		keysSorted.addAll(keys);
		Collections.sort(keysSorted);

		// Create an list of (sorted) key-value pairs
		LinkedList<KeyValue<String, String>> iter = new LinkedList<>();
		for (String key : keysSorted) {
			iter.add(new KeyValue<>(key, getAttr(key)));
		}

		return iter.iterator();
	}

	/**
	 * Parse an entry (line) from a GFF file
	 */
	protected void parse(Genome genome, String line) {
		// Split fields
		String fields[] = line.split("\t");

		// Parse chromosome
		String chromo = fields[0];
		parent = genome.getOrCreateChromosome(chromo);

		// Source
		source = fields[1];
		if (source.equals(".")) source = "";

		// Parse type
		gffTypeStr = fields[2];
		if (gffTypeStr.isEmpty() || gffTypeStr.equals(".")) gffType = null;
		gffType = GffType.parse(gffTypeStr);

		// Coordinates: closed, one-based
		start = Gpr.parseIntSafe(fields[3]) - 1;
		end = Gpr.parseIntSafe(fields[4]) - 1;

		// Feature strand
		strandMinus = fields[6].equals("-");

		// Frame
		frame = (fields[7].equals(".") ? -1 : Gpr.parseIntSafe(fields[7]));
		frame = FrameType.GFF.convertFrame(frame);

		// Parse attributes
		if (fields.length >= 8) parseAttributes(fields[8]);
		else parseAttributes(null);

		// Parse some special fields
		id = parseId();
	}

	/**
	 * Parse attributes (key-value pairs) from a line in a GFF file
	 */
	protected void parseAttributes(String attrStr) {
		keyValues = new HashMap<>();
		keys = new HashSet<>();

		// Add some column fields
		add("source", source);
		add("type", gffTypeStr);

		// Parse and add all key-value pairs
		if (attrStr != null) {
			if (attrStr.length() > 0) {
				String attrs[] = attrStr.split(";");
				for (int i = 0; i < attrs.length; i++) {
					// Split key value pair
					String kv[] = attrs[i].split("=");
					if (kv.length > 1) {
						String key = kv[0].trim();
						String value = kv[1].trim();

						if (!hasAttr(key)) add(key, value);
					}
				}
			}
		}
	}

	/**
	 * Obtain or create an ID
	 */
	protected String parseId() {
		String id = "";

		if (hasAttr(FIELD_ID)) id = getAttr(FIELD_ID);
		else if (gffType == GffType.GENE && hasAttr(FIELD_GENE_ID)) id = getAttr(FIELD_GENE_ID);
		else if (gffType == GffType.TRANSCRIPT && hasAttr(FIELD_TRANSCRIPT_ID)) id = getAttr(FIELD_TRANSCRIPT_ID);
		else if (gffType == GffType.EXON && hasAttr(FIELD_EXON_ID)) id = getAttr(FIELD_EXON_ID);
		else if (hasAttr(FIELD_DB_XREF)) id = getAttr(FIELD_DB_XREF);
		else if (hasAttr(FIELD_NAMEU)) id = getAttr(FIELD_NAMEU);
		else id = gffType + "_" + getChromosomeName() + "_" + (start + 1) + "_" + (end + 1); // No ID => create one

		return id.trim(); // Sometimes names or IDs may have spaces, we have to get rid of them
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getChromosomeName() + "\t" + source //
				+ "\t" + gffType //
				+ "\t" + start //
				+ "\t" + end //
				+ "\t" + (strandMinus ? "-" : "+") //
				+ "\n" //
		);

		// Show key value pairs
		ArrayList<String> keys = new ArrayList<>();
		keys.addAll(keyValues.keySet());
		Collections.sort(keys);
		for (String key : keys)
			sb.append("\t" + key + " : " + getAttr(key) + "\n");

		return sb.toString();
	}
}
