package ca.mcgill.mcb.pcingola.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.KeyValue;

/**
 * An interval intended as a mark
 *
 * @author pcingola
 *
 */
public class GffMarker extends Custom {

	private static final long serialVersionUID = -164502778854644537L;

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
		keyValues.put(key.toLowerCase(), value);
	}

	public String getAttr(String key) {
		return keyValues.get(key.toLowerCase());
	}

	public String getBiotype() {
		if (gffType == GffType.GENE) return getGeneBiotype();
		if (gffType == GffType.TRANSCRIPT) return getTranscriptBiotype();

		// Default: Use generic biotype
		if (hasAttr("biotype")) return getAttr("biotype");

		// Use 'source' as bioType (Old ENSEMBL GTF files use this field)
		return source;

	}

	protected String getBioType() {
		return getAttr("biotype");
	}

	public int getFrame() {
		return frame;
	}

	public String getGeneBiotype() {
		// Note: Different data providers use different keys
		// E.g.: ENSEMBL uses "gene_biotype" and GenCode uses "gene_type"
		String keys[] = { "gene_biotype", "gene_type", "biotype" };

		for (String key : keys)
			if (hasAttr(key)) return getAttr(key);

		return null;
	}

	public String getGeneId() {
		String key = "gene_id";
		if (hasAttr(key)) return getAttr(key);
		if (gffType == GffType.GENE) return id;
		if (gffType == GffType.TRANSCRIPT) return getGffParentId();

		return GffType.GENE + "_" + id;
	}

	public String getGeneName() {
		String key = "gene_name";
		if (hasAttr(key)) return getAttr(key);
		if (gffType == GffType.GENE) return getAttr("name");
		return GffType.GENE + "_" + id;
	}

	public String getGffParentId() {
		if (hasAttr("Parent")) return getAttr("Parent");

		switch (gffType) {
		case TRANSCRIPT:
		case INTRON_CONSERVED:
			return getGeneId();

		case EXON:
		case CDS:
		case START_CODON:
		case STOP_CODON:
		case UTR3:
		case UTR5:
			return getTranscriptId();

		default:
			return null;
		}
	}

	/**
	 * Parent can have multiple, comma separated entries
	 */
	public String[] getGffParentIds() {
		String ids = getAttr("Parent");
		if (ids != null) return ids.split(",");

		// Nothing found? Try to find parentId
		String pid = getGffParentId();
		if (pid == null) return null; // Nothing found? Give up

		// Pack parentId into a String[]
		String pids[] = { pid };
		return pids;
	}

	public GffType getGffType() {
		return gffType;
	}

	public String getTranscriptBiotype() {
		// Note: Different data providers use different keys
		// E.g.: ENSEMBL uses "transcript_biotype" and GenCode uses "transcript_type"
		String keys[] = { "transcript_biotype", "transcript_type", "biotype" };

		for (String key : keys)
			if (hasAttr(key)) return getAttr(key);

		// Use 'source' as bioType (Old ENSEMBL GTF files use this field)
		return source;
	}

	public String getTranscriptId() {
		String key = "transcript_id";
		if (hasAttr(key)) return getAttr(key);
		if (gffType == GffType.TRANSCRIPT) return id;
		if (gffType == GffType.EXON) return getGffParentId();
		return GffType.TRANSCRIPT + "_" + id;
	}

	public String getTranscriptVersion() {
		return getAttr("transcript_version");
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

	/**
	 * Is biotType considered 'protein coding'?
	 */
	public boolean isProteingCoding() {
		String bioType = getBiotype();
		if (bioType == null) return false;

		return bioType.equals("protein_coding") //
				|| bioType.equals("IG_C_gene") //
				|| bioType.equals("IG_D_gene") //
				|| bioType.equals("IG_J_gene") //
				|| bioType.equals("IG_V_gene") //
				|| bioType.equals("TR_C_gene") //
				|| bioType.equals("TR_D_gene") //
				|| bioType.equals("TR_J_gene") //
				|| bioType.equals("TR_V_gene") //
				;
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
			iter.add(new KeyValue<String, String>(key, getAttr(key)));
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
		keys = new HashSet<String>();

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
						add(key, value);
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

		if (hasAttr("ID")) id = getAttr("ID");
		else if (gffType == GffType.GENE && hasAttr("gene_id")) id = getAttr("gene_id");
		else if (gffType == GffType.TRANSCRIPT && hasAttr("transcript_id")) id = getAttr("transcript_id");
		else if (gffType == GffType.EXON && hasAttr("exon_id")) id = getAttr("exon_id");
		else if (hasAttr("db_xref")) id = getAttr("db_xref");
		else if (hasAttr("Name")) id = getAttr("Name");
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
