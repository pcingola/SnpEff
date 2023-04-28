package org.snpeff.interval;

import org.snpeff.util.Gpr;
import org.snpeff.util.KeyValue;

import java.util.*;

/**
 * An interval intended as a mark
 *
 * @author pcingola
 */
public class GffMarker extends Custom {

    // Field number
    public static final int GFF_FIELD_CHROMO = 0;
    public static final int GFF_FIELD_SOURCE = 1;
    public static final int GFF_FIELD_TYPE = 2;
    public static final int GFF_FIELD_START = 3;
    public static final int GFF_FIELD_END = 4;
    public static final int GFF_FIELD_SCORE = 5;
    public static final int GFF_FIELD_STRAND = 6;
    public static final int GFF_FIELD_FRAME = 7;
    public static final int GFF_FIELD_ATTRIBUTES = 8;
    public static final int GFF_NUMBER_OF_FIELDS = 9; // Total fields in GFF line

    /**
     * source = fields[GFF_FIELD_SOURCE];
     * if (source.equals(".")) source = "";
     * <p>
     * // Parse type
     * gffTypeStr = fields[GFF_FIELD_TYPE];
     * if (gffTypeStr.isEmpty() || gffTypeStr.equals(".")) gffType = null;
     * else gffType = GffType.parse(gffTypeStr);
     * <p>
     * // Coordinates: closed, one-based
     * setStart(Gpr.parseIntSafe(fields[GFF_FIELD_START]) - 1);
     * setEndClosed(Gpr.parseIntSafe(fields[GFF_FIELD_END]) - 1);
     * <p>
     * // Feature strand
     * strandMinus = fields[GFF_FIELD_STRAND].equals("-");
     * <p>
     * // Frame
     * frame = (fields[7].equals(".") ? -1 : Gpr.parseIntSafe(fields[GFF_FIELD_FRAME]));
     */


    private static final long serialVersionUID = -164502778854644537L;

    String source;
    int frame;
    GffType gffType;
    String gffTypeStr;
    Map<String, String> keyValues;
    Set<String> keys;

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
     * Can this line (form a GFF file) be parsed?
     * Note: Returns false for headers, empty lines and lines
     * having less number of fields than expected
     */
    public static boolean canParseLine(String line) {
        // Ignore empty lines and comment lines
        if (line == null || line.isEmpty() || line.startsWith("#")) return false;
        return line.split("\t").length >= GFF_NUMBER_OF_FIELDS;
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

    public BioType getBiotype() {
        if (gffType == GffType.GENE) return getGeneBiotype();
        if (gffType == GffType.TRANSCRIPT) return getTranscriptBiotype();
        return getBiotypeGeneric();
    }

    protected String getBioType() {
        return getAttr("biotype");
    }

    protected BioType getBiotypeGeneric() {
        BioType bioType;

        // Use generic biotype field
        if (hasAttr("biotype")) return BioType.parse(getAttr("biotype"));

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
        String[] keys = {"gene_biotype", "gene_type", "biotype"};

        for (String key : keys)
            if (hasAttr(key)) return BioType.parse(getAttr(key));

        return getBiotypeGeneric();
    }

    public String getGeneId() {
        String key = "gene_id";
        if (hasAttr(key)) return getAttr(key);
        if (gffType == GffType.GENE) return id;
        if (gffType == GffType.TRANSCRIPT) return getGffParentId(true);

        return GffType.GENE + "_" + id;
    }

    public String getGeneName() {
        if (hasAttr("gene_name")) return getAttr("gene_name");
        if (gffType == GffType.GENE && hasAttr("name")) return getAttr("name");
        if (gffType == GffType.TRANSCRIPT) {
            String pid = getGffParentId(true);
            if (pid != null) return pid;
        }
        return id;
    }

    public String getGffParentId(boolean doNotRecurse) {
        if (hasAttr("Parent")) return getAttr("Parent");

        switch (gffType) {
            case TRANSCRIPT:
            case INTRON_CONSERVED:
                if (hasAttr("gene")) return getAttr("gene");
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
        String ids = getAttr("Parent");
        if (ids != null) return ids.split(",");

        // Nothing found? Try to find parentId
        String pid = getGffParentId(false);
        if (pid == null) return null; // Nothing found? Give up

        // Pack parentId into a String[]
        String[] pids = {pid};
        return pids;
    }

    public GffType getGffType() {
        return gffType;
    }

    public BioType getTranscriptBiotype() {
        // Note: Different data providers use different keys
        // E.g.: ENSEMBL uses "transcript_biotype" and GenCode uses "transcript_type"
        String[] keys = {"transcript_biotype", "transcript_type", "biotype"};

        for (String key : keys)
            if (hasAttr(key)) return BioType.parse(getAttr(key));

        return getBiotypeGeneric();
    }

    public String getTranscriptId() {
        String key = "transcript_id";
        if (hasAttr(key)) return getAttr(key);
        if (gffType == GffType.TRANSCRIPT) return id;
        if (gffType == GffType.EXON) return getGffParentId(true);
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
        String[] fields = line.split("\t");

        // Parse chromosome
        String chromo = fields[GFF_FIELD_CHROMO];
        parent = genome.getOrCreateChromosome(chromo);

        // Source
        source = fields[GFF_FIELD_SOURCE];
        if (source.equals(".")) source = "";

        // Parse type
        gffTypeStr = fields[GFF_FIELD_TYPE];
        if (gffTypeStr.isEmpty() || gffTypeStr.equals(".")) gffType = null;
        else gffType = GffType.parse(gffTypeStr);

        // Coordinates: closed, one-based
        setStart(Gpr.parseIntSafe(fields[GFF_FIELD_START]) - 1);
        setEndClosed(Gpr.parseIntSafe(fields[GFF_FIELD_END]) - 1);

        // Feature strand
        strandMinus = fields[GFF_FIELD_STRAND].equals("-");

        // Frame
        frame = (fields[GFF_FIELD_FRAME].equals(".") ? -1 : Gpr.parseIntSafe(fields[GFF_FIELD_FRAME]));
        frame = FrameType.GFF.convertFrame(frame);

        // Parse attributes
        if (fields.length >= GFF_FIELD_ATTRIBUTES) parseAttributes(fields[GFF_FIELD_ATTRIBUTES]);
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

        if (hasAttr("ID")) id = getAttr("ID");
        else if (gffType == GffType.GENE && hasAttr("gene_id")) id = getAttr("gene_id");
        else if (gffType == GffType.TRANSCRIPT && hasAttr("transcript_id")) id = getAttr("transcript_id");
        else if (gffType == GffType.EXON && hasAttr("exon_id")) id = getAttr("exon_id");
        else if (hasAttr("db_xref")) id = getAttr("db_xref");
        else if (hasAttr("Name")) id = getAttr("Name");
        else id = gffType + "_" + getChromosomeName() + "_" + (getStart() + 1) + "_" + (getEndClosed() + 1); // No ID => create one

        return id.trim(); // Sometimes names or IDs may have spaces, we have to get rid of them
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getChromosomeName() + "\t" + source //
                + "\t" + gffType //
                + "\t" + getStart() //
                + "\t" + getEndClosed() //
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
