package org.snpeff.vcf;

import org.snpeff.align.VcfRefAltAlign;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.*;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.LossOfFunction;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A VCF entry (a line) in a VCF file
 *
 * @author pablocingolani
 */
public class VcfEntry extends Marker implements Iterable<VcfGenotype> {

    public static final String FILTER_PASS = "PASS";
    public static final String SUB_FIELD_SEP = ";";
    public static final String[] EMPTY_STRING_ARRAY = new String[0];
    public static final double ALLELE_FEQUENCY_COMMON = 0.05;
    public static final double ALLELE_FEQUENCY_LOW = 0.01;
    public static final Pattern INFO_KEY_PATTERN = Pattern.compile("[\\p{Alpha}_][\\p{Alnum}._]*");
    public static final String VCF_INFO_END = "END"; // Imprecise variants
    // In order to report sequencing data evidence for both variant and non-variant positions in the genome, the VCF
    // specification allows to represent blocks of reference-only calls in a single record using the END INFO tag, an idea
    // originally introduced by the gVCF file format. The convention adopted here is to represent reference evidence as
    // likelihoods against an unknown alternate allele. Think of this as the likelihood for reference as compared to any
    // other possible alternate allele (both SNP, indel, or otherwise). A symbolic alternate allele <*> is used to represent
    // this unspecified alternate allele
    public static final String VCF_ALT_NON_REF = "<*>"; // See VCF 4.2 section "5.5 Representing unspecified alleles and REF-only blocks (gVCF)"
    public static final String VCF_ALT_NON_REF_gVCF = "<NON_REF>"; // NON_REF tag for ALT field (only in gVCF fields)
    public static final String VCF_ALT_MISSING_REF = "*"; // The '*' allele is reserved to indicate that the allele is missing due to a upstream deletion (see VCF 4.3 spec., ALT definition)
    public static final String[] VCF_ALT_NON_REF_gVCF_ARRAY = {VCF_ALT_NON_REF_gVCF};
    public static final String[] VCF_ALT_NON_REF_ARRAY = {VCF_ALT_NON_REF};
    public static final String[] VCF_ALT_MISSING_REF_ARRAY = {VCF_ALT_MISSING_REF};
    public static final String VCF_INFO_HOMS = "HO";
    public static final String VCF_INFO_HETS = "HE";
    public static final String VCF_INFO_NAS = "NA";
    private static final Map<String, String> INFO_VALUE_ENCODE;
    private static final long serialVersionUID = 4226374412681243433L;

    static {
        // Initialize VCF value encoding table
        INFO_VALUE_ENCODE = new HashMap<>();
        // INFO_VALUE_ENCODE.put("%3A", ":"); // This is used in genotype entries, not INFO entries.
        INFO_VALUE_ENCODE.put("%3B", ";");
        INFO_VALUE_ENCODE.put("%3D", "=");
        //		INFO_VALUE_ENCODE.put("%25", "%");
        INFO_VALUE_ENCODE.put("%2C", ",");
        INFO_VALUE_ENCODE.put("%0D", "\n");
        INFO_VALUE_ENCODE.put("%0A", "\r");
        INFO_VALUE_ENCODE.put("%09", "\t");
    }

    protected String[] alts;
    protected String altStr;
    protected String chromosomeName; // Original chromosome name
    protected String filter;
    protected String format;
    protected String[] formatFields;
    protected String[] genotypeFields; // Raw fields from VCF file
    protected String genotypeFieldsStr; // Raw fields from VCF file (one string, tab separated)
    protected byte[] genotypeScores;
    protected HashMap<String, String> info;
    protected String infoStr = "";
    protected String line; // Line from VCF file
    protected int lineNum; // Line number
    protected Double quality;
    protected String ref;
    protected LinkedList<Variant> variants;
    protected List<VcfEffect> vcfEffects;
    protected VcfFileIterator vcfFileIterator; // Iterator where this entry was red from
    protected ArrayList<VcfGenotype> vcfGenotypes = null;

    public VcfEntry(VcfFileIterator vcfFileIterator, Marker parent, String chromosomeName, int start, String id, String ref, String altsStr, double quality, String filterPass, String infoStr, String format) {
        super(parent, start, start + ref.length() - 1, false, id);
        this.vcfFileIterator = vcfFileIterator;
        this.chromosomeName = chromosomeName;
        this.ref = ref;
        parseAlts(altsStr);
        this.quality = quality;
        filter = filterPass;
        this.infoStr = infoStr;
        parseInfo();
        this.format = format;
        parseEnd(altsStr);
    }

    /**
     * Create a line form a file iterator
     */
    public VcfEntry(VcfFileIterator vcfFileIterator, String line, int lineNum, boolean parseNow) {
        super(null, 0, 0, false, "");
        this.vcfFileIterator = vcfFileIterator;
        this.lineNum = lineNum;
        this.line = line;

        if (parseNow) parse();
    }

    /**
     * Return a string without leading, trailing and duplicated underscores
     */
    public static String cleanUnderscores(String s) {
        if (s == null || s.isEmpty()) return s;
        var sb = new StringBuilder();
        var schars = s.toCharArray();
        var first = true;
        var previusIsUnderscore = false;
        for (char c : schars) {
            if (c == '_') {
                if (!first) previusIsUnderscore = true;
                continue;
            }

            if (previusIsUnderscore) {
                previusIsUnderscore = false;
                sb.append('_');
            }

            sb.append(c);
            first = false; // First character must not be an underscore
        }
        return sb.toString();
    }

    /**
     * Does 'value' represent an EMPTY / MISSING value in a VCF field?
     * (or multiple MISSING comma-separated values)
     */
    public static boolean isEmpty(String value) {
        if (value == null || value.isEmpty() || value.equals(VcfFileIterator.MISSING)) return true;

        if (value.indexOf(',') >= 0) {
            // Multiple values, all of them MISSING?
            String[] values = value.split(",");
            for (String val : values)
                if (!(val.isEmpty() || val.equals(VcfFileIterator.MISSING))) return false;

            return true;
        }

        return false;
    }

    /**
     * Make sure the INFO key matches the regular
     * expression (as specified in VCF spec 4.3)
     */
    public static boolean isValidInfoKey(String key) {
        Matcher m = INFO_KEY_PATTERN.matcher(key);
        return m.matches();
    }

    /**
     * Check that this value can be added to an INFO field
     *
     * @return true if OK, false if invalid value
     */
    public static boolean isValidInfoValue(String value) {
        boolean invalid = ((value != null) // Null?
                && ((value.indexOf(' ') >= 0) // No spaces allowed
                || (value.indexOf(';') >= 0) // No semicolons allowed
                || (value.indexOf('=') >= 0) // No equal signs allowed
                || (value.indexOf('\t') >= 0) // No tabs allowed
                || (value.indexOf('\n') >= 0)) // No new lines allowed
        );
        return !invalid;
    }

    /**
     * Decode INFO value
     */
    public static String vcfInfoDecode(String str) {
        if (str == null || str.isEmpty() || str.equals(".")) return str;

        for (String encoded : INFO_VALUE_ENCODE.keySet())
            str = str.replace(encoded, INFO_VALUE_ENCODE.get(encoded));

        return str;
    }

    /**
     * Encode a string to be used in an 'INFO' field value
     * From the VCF 4.3 specification
     * Characters with special meaning (such as field delimiters ';' in INFO or ':' FORMAT
     * fields) must be represented using the capitalized percent encoding:
     * %3A : (colon)
     * %3B ; (semicolon)
     * %3D = (equal sign)
     * %25 % (percent sign)
     * %2C , (comma)
     * %0D CR
     * %0A LF
     * %09 TAB
     */
    public static String vcfInfoEncode(String str) {
        if (str == null || str.isEmpty() || str.equals(".")) return str;

        for (String encoded : INFO_VALUE_ENCODE.keySet())
            str = str.replace(INFO_VALUE_ENCODE.get(encoded), encoded);

        return str.replaceAll(" ", "_"); // Transform spaces, if any
    }

    /**
     * Return a string safe to be used in an 'INFO' field key
     */
    public static String vcfInfoKeySafe(String str) {
        if (str == null) return str;
        str = str.replaceAll("[^a-zA-Z0-9_.]", "_");
        char c0 = str.charAt(0);
        if (c0 != '_' && !Character.isAlphabetic(c0)) str = '_' + str;
        return str;
    }

    /**
     * Return a string safe to be used in an 'INFO' field value
     */
    public static String vcfInfoValueSafe(String str) {
        if (str == null) return str;
        return str.replaceAll("[ ,;|=()\t]", "_");
    }

    /**
     * Add string to FILTER  field
     */
    public void addFilter(String filterStr) {
        // Get current value
        if (filter.equals(".") || filter.equals(VcfEntry.FILTER_PASS)) filter = ""; // Empty?

        // Append new value
        filter += (!filter.isEmpty() ? ";" : "") + filterStr; // Add this filter to the not-passed list
    }

    /**
     * Add a 'FORMAT' field
     */
    public void addFormat(String formatName) {
        if (format == null) format = "";
        if (format.indexOf(formatName) >= 0) throw new RuntimeException("Format field '" + formatName + "' already exists!");

        // Add to format
        format += (format.endsWith(":") ? "" : ":") + formatName;
    }

    /**
     * Add a genotype as a string
     */
    public void addGenotype(String vcfGenotypeStr) {
        if (vcfGenotypes == null) vcfGenotypes = new ArrayList<>();
        if (format == null) format = "";
        vcfGenotypes.add(new VcfGenotype(this, format, vcfGenotypeStr));

        genotypeScores = null; // Reset or invalidate scores
    }

    /**
     * Add a "key=value" tuple the info field
     *
     * @param key   : INFO key name
     * @param value : Can be null if it is a boolean field.
     */
    public void addInfo(String key, String value) {
        if (!isValidInfoKey(key)) Log.warning(ErrorWarningType.WARNING_INVALID_INFO_KEY, "Illegal INFO key / name. Key: \"" + key + "\" does not match regular expression ^[A-Za-z_][0-9A-Za-z_.]*$");
        if (!isValidInfoValue(value)) Log.warning(ErrorWarningType.WARNING_INVALID_INFO_VALUE, "No white-space, semi-colons, or equals-signs are permitted in INFO field values. Name:\"" + key + "\" Value:\"" + value + "\"");

        // Remove previous 'key' for INFO field?
        removeInfo(key);

        // Is this a 'flag'?
        boolean isFlag = false;
        VcfHeader vcfHeader = vcfFileIterator.getVcfHeader();
        if (vcfHeader != null) {
            VcfHeaderInfo vcfHeaderInfo = vcfFileIterator.getVcfHeader().getVcfHeaderInfo(key);
            isFlag = (vcfHeaderInfo != null) && (vcfHeaderInfo.getVcfInfoType() == VcfInfoType.Flag);
        }

        // Add to info hash (if available)
        if (info != null) info.put(key, value);

        // Append value to infoStr
        String addInfoStr = key + (value != null && !isFlag ? "=" + value : ""); // String to append
        if ((infoStr == null) || infoStr.isEmpty()) infoStr = addInfoStr;
        else {
            if (!infoStr.endsWith(SUB_FIELD_SEP)) infoStr += SUB_FIELD_SEP; // Do we need to add a semicolon?
            infoStr += addInfoStr; // Add info string
        }

    }

    /**
     * Categorization by allele frequency
     */
    public AlleleFrequencyType alleleFrequencyType() {
        double maf = maf();
        if (maf <= ALLELE_FEQUENCY_LOW) return AlleleFrequencyType.Rare;
        if (maf <= ALLELE_FEQUENCY_COMMON) return AlleleFrequencyType.LowFrequency;
        return AlleleFrequencyType.Common;
    }

    /**
     * Is this entry heterozygous?
     * <p>
     * Infer Hom/Her if there is only one sample in the file.
     * Ohtherwise the field is null.
     */
    public Boolean calcHetero() {
        // No genotyping information? => Use number of ALT field
        if (genotypeFieldsStr == null) return isMultiallelic();

        Boolean isHetero = null;

        // No genotype fields => Parse fields (we only parse them if there is only one GT field)
        if (genotypeFields == null) {

            // Are there more than two tabs? (i.e. more than one format field + one genotype field)
            int countFields, fromIndex;
            for (countFields = 0, fromIndex = 0; (fromIndex >= 0) && (countFields < 1); countFields++, fromIndex++)
                fromIndex = genotypeFieldsStr.indexOf('\t', fromIndex);

            // OK only one genotype field => Parse it in order to extract homo info.
            if (countFields == 1) parseGenotypes();
        }

        // OK only one genotype field => calculate if it is heterozygous
        if ((genotypeFields != null) && (genotypeFields.length == 1)) isHetero = getVcfGenotype(0).isHeterozygous();

        return isHetero;
    }

    /**
     * Perform several simple checks and report problems (if any).
     */
    public String check() {
        StringBuilder sb = new StringBuilder();

        // Check REF
        if (ref.indexOf(",") >= 0) sb.append("REF field has multiple entries (this is not allowed)\n");

        // Check INFO fields
        for (String infoName : getInfoKeys()) {
            String err = checkInfo(infoName);
            if (!err.isEmpty()) sb.append(err + "\n");
        }

        // Check genotypes
        sb.append(checkGenotypes());

        return sb.toString();
    }

    /**
     * Check genotypes
     */
    String checkGenotypes() {
        StringBuilder err = new StringBuilder();

        if (getVcfFileIterator() != null && getVcfFileIterator().getVcfHeader() != null) {
            int numGt = getVcfGenotypes().size();
            int numSamples = getVcfFileIterator().getVcfHeader().getNumberOfSamples();
            if (numGt != numSamples) err.append("Number of genotypes (" + numGt + ") differs form the number of samples (" + numSamples + ")\n");
        }

        // Check that each genotype matches the number of alleles
        int numAlts = getAlts().length;
        int gtNum = 1;
        for (VcfGenotype vgt : getVcfGenotypes()) {
            int gts[] = vgt.getGenotype();
            if (gts != null) {
                for (int i = 0; i < gts.length; i++)
                    if (gts[i] > numAlts) err.append("Genotype number " + gtNum + " has genotype number '" + gts[i] + "', but there are only '" + numAlts + "' ALTs.\n");
            }
            gtNum++;
        }

        return err.toString();
    }

    /**
     * Check info field
     * Note: We report the first error we find
     */
    String checkInfo(String infoName) {
        if (infoName.isEmpty()) return "";

        VcfHeaderInfo vcfInfo = getVcfInfo(infoName);
        if (vcfInfo == null) return "Cannot find header for INFO field '" + infoName + "'";

        // Split INFO value and match it to allele
        String valsStr = getInfo(infoName);
        if (valsStr == null) return ""; // INFO field not present, nothing to do

        // Check values
        String values[] = valsStr.split(",");
        for (String val : values)
            if (!VcfEntry.isValidInfoValue(val)) return "INFO field '" + infoName + "' has an invalid value '" + val + "' (no spaces, tabs, '=' or ';' are allowed)";

        // Check number of INFO elements
        if (vcfInfo.isNumberNumber() && vcfInfo.getNumber() != values.length) {
            VcfInfoType type = vcfInfo.getVcfInfoType();
            if (type == VcfInfoType.Flag && values.length == 1) ; // OK, flags must have one or zero values
            else return "INFO filed '" + infoName + "' has 'Number=" + vcfInfo.getNumber() + "' in header, but it contains '" + values.length + "' elements.";
        }
        if (vcfInfo.isNumberAllAlleles() && values.length != (alts.length + 1)) return "INFO filed '" + infoName + "' has 'Number=R' in header, but it contains '" + values.length + "' elements when there are '" + alts.length + "' alleles (it should have '" + (alts.length + 1) + "' elements).";
        if (vcfInfo.isNumberAllAlleles() && values.length != alts.length) return "INFO filed '" + infoName + "' has 'Number=A' in header, but it contains '" + values.length + "' elements when there are '" + alts.length + "' alleles.";

        return "";
    }

    @Override
    public Cds cloneShallow() {
        throw new RuntimeException("Unimplemented!");
    }

    /**
     * Compress genotypes into "HO/HE/NA" INFO fields
     */
    public boolean compressGenotypes() {
        if (getAlts().length > 1) return false;

        StringBuilder homs = new StringBuilder();
        StringBuilder hets = new StringBuilder();
        StringBuilder nas = new StringBuilder();

        // Add all genotype codes
        int idx = 0;
        for (VcfGenotype gen : getVcfGenotypes()) {
            int score = gen.getGenotypeCode();

            if (score == 0) {
                ; //Nothing to do
            } else if (score < 0) nas.append((nas.length() > 0 ? "," : "") + idx);
            else if (score == 1) hets.append((hets.length() > 0 ? "," : "") + idx);
            else if (score == 2) homs.append((homs.length() > 0 ? "," : "") + idx);
            else return false; // Cannot compress

            idx++;
        }

        // Update INFO fields
        if (homs.length() > 0) addInfo(VCF_INFO_HOMS, homs.toString());
        if (hets.length() > 0) addInfo(VCF_INFO_HETS, hets.toString());
        if (nas.length() > 0) addInfo(VCF_INFO_NAS, nas.toString());

        // Nothing added? Add 'NAS' (as an indicator that it was compressed
        if ((homs.length() == 0) && (hets.length() == 0) && (nas.length() == 0)) addInfo(VCF_INFO_NAS, null);

        return true;
    }

    /**
     * Remove a string from FILTER field
     *
     * @returns true if the value is removed
     */
    public boolean delFilter(String filterStr) {
        // Get current value
        StringBuilder sbFilter = new StringBuilder();

        // Split by semicolon and filter out the undesired values
        boolean removed = false;
        for (String f : filter.split(";")) {
            if (!f.equals(filterStr)) sbFilter.append((sbFilter.length() > 0 ? ";" : "") + f); // Append if it does not match filterStr
            else removed = true;
        }

        // Changed? Set new value
        if (removed) filter = sbFilter.toString();
        return removed;
    }

    /**
     * Get index of matching ALT entry
     *
     * @return -1 if not found
     */
    public int getAltIndex(String alt) {
        for (int i = 0; i < alts.length; i++)
            if (alts[i].equalsIgnoreCase(alt)) return i;
        return -1;
    }

    public String[] getAlts() {
        return alts;
    }

    /**
     * Create a comma separated ALTS string
     */
    public String getAltsStr() {
        if (altStr != null) {
            if (altStr.isEmpty()) return ".";
            return altStr;
        }

        if (alts == null) return "";

        StringBuilder sb = new StringBuilder();
        for (String alt : alts)
            sb.append(alt + " ");
        altStr = sb.toString().trim().replace(' ', ',');

        return altStr;
    }

    /**
     * Original chromosome name (as it appeared in the VCF file)
     */
    @Override
    public String getChromosomeNameOri() {
        return chromosomeName;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String[] getFormatFields() {
        if (formatFields == null) {
            if (format == null) formatFields = new String[0];
            else formatFields = format.split(":");
        }
        return formatFields;
    }

    /**
     * Return genotypes parsed as an array of codes
     */
    public synchronized byte[] getGenotypesScores() {
        if (genotypeScores != null) return genotypeScores;

        // Not compressed? Parse codes
        if (!isCompressedGenotypes()) {
            List<VcfGenotype> vcfGts = getVcfGenotypes();
            int numSamples = vcfGts.size();
            genotypeScores = new byte[numSamples];

            int idx = 0;
            for (VcfGenotype vcfGt : vcfGts)
                genotypeScores[idx++] = (byte) vcfGt.getGenotypeCode();

            return genotypeScores;
        }

        //---
        // Uncompress (HO/HE/NA in info fields)
        //---

        // Get 'sparse' matrix entries
        String hoStr = getInfo(VCF_INFO_HOMS);
        String heStr = getInfo(VCF_INFO_HETS);
        String naStr = getInfo(VCF_INFO_NAS);

        // Parse 'sparse' entries
        int numSamples = getNumberOfSamples();
        genotypeScores = new byte[numSamples];
        parseSparseGt(naStr, genotypeScores, -1);
        parseSparseGt(heStr, genotypeScores, 1);
        parseSparseGt(hoStr, genotypeScores, 2);

        return genotypeScores;
    }

    /**
     * Get info string
     */
    public String getInfo(String key) {
        if (info == null) parseInfo();
        return info.get(key);
    }

    /**
     * Get info string for a specific allele
     */
    public String getInfo(String key, String allele) {
        if (info == null) parseInfo();

        // Get INFO value
        String infoStr = info.get(key);
        if (infoStr == null) return null;

        // Split INFO value and match it to allele
        String infos[] = infoStr.split(",");

        // INFO fields having number type 'R' (all alleles) should have one value for reference as well.
        // So in those cases we must skip the first value
        int firstAltIndex = 0;
        VcfHeaderInfo vcfInfo = getVcfInfo(key);
        if (vcfInfo != null && vcfInfo.isNumberAllAlleles()) {
            firstAltIndex = 1;

            // Are we looking for 'REF' information?
            if (ref.equalsIgnoreCase(allele)) return infos[0];
        }

        // Find ALT matching allele
        for (int i = 0, j = firstAltIndex; (i < alts.length) && (j < infos.length); i++, j++)
            if (alts[i].equalsIgnoreCase(allele)) return infos[j];

        return null;
    }

    /**
     * Get an INFO field matching a variant
     *
     * @returns Field value (string) or null if there is no match
     */
    public String getInfo(String key, Variant var) {
        if (info == null) parseInfo();

        // Get INFO value
        String infoStr = info.get(key);
        if (infoStr == null) return null;

        // Split INFO value and match it to allele
        String infos[] = infoStr.split(",");

        // INFO fields having number type 'R' (all alleles) should have one value for reference as well.
        // So in those cases we must skip the first value
        int firstAltIndex = 0;
        VcfHeaderInfo vcfInfo = getVcfInfo(key);
        if (vcfInfo != null && vcfInfo.isNumberAllAlleles()) {
            firstAltIndex = 1;

            // Are we looking for 'REF' information? I.e. variant is not a variant)
            if (!var.isVariant()) return infos[0];
        }

        // Look for genotype matching 'var'
        int i = firstAltIndex;
        String gtPrev = "";
        for (Variant v : variants()) {
            if (i >= infos.length) break;
            if (var.equals(v)) return infos[i];
            if (!v.getGenotype().equals(gtPrev)) i++; // Advance genotype counter
        }

        return null;
    }

    /**
     * Does the entry exists?
     */
    public boolean getInfoFlag(String key) {
        if (info == null) parseInfo();
        return info.containsKey(key);
    }

    /**
     * Get info field as a 'double' number
     * The norm specifies data type as 'FLOAT', that is why the name of this method might be not intuitive
     */
    public double getInfoFloat(String key) {
        if (info == null) parseInfo();
        String f = info.get(key);
        if (f == null) return Double.NaN;
        return Gpr.parseDoubleSafe(f);
    }

    /**
     * Get info field as an long number
     * The norm specifies data type as 'INT', that is why the name of this method might be not intuitive
     */
    public long getInfoInt(String key) {
        if (info == null) parseInfo();
        String i = info.get(key);
        if (i == null) return 0;
        return Gpr.parseLongSafe(i);
    }

    /**
     * Get all keys available in the info field
     */
    public Set<String> getInfoKeys() {
        if (info == null) parseInfo();
        return info.keySet();
    }

    /**
     * Get the full (unparsed) INFO field
     */
    public String getInfoStr() {
        return infoStr;
    }

    /**
     * Original VCF line (from file)
     */
    public String getLine() {
        return line;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    /**
     * number of samples in this VCF file
     */
    public int getNumberOfSamples() {
        if (vcfFileIterator == null) return 0;
        VcfHeader vh = vcfFileIterator.getVcfHeader();
        if (vh == null) return 0;
        return vh.getNumberOfSamples();
    }

    public double getQuality() {
        return (quality != null ? quality : 0);
    }

    public String getRef() {
        return ref;
    }

    public String getStr() {
        return getChromosomeName() //
                + ":" + (getStart() + 1) //
                + "_" + ref //
                + "/" + getAltsStr();
    }

    public List<VcfEffect> getVcfEffects() {
        return getVcfEffects(null);
    }

    /**
     * Parse 'EFF' info field and get a list of effects
     */
    public synchronized List<VcfEffect> getVcfEffects(EffFormatVersion formatVersion) {
        if (vcfEffects != null) return vcfEffects;

        String effStr = null;
        if (formatVersion == null) {
            // Guess which INFO field could be
            effStr = getInfo(EffFormatVersion.VCF_INFO_ANN_NAME);
            if (effStr != null) {
                formatVersion = EffFormatVersion.FORMAT_ANN; // Unspecied 'ANN' version
            } else {
                effStr = getInfo(EffFormatVersion.VCF_INFO_EFF_NAME);
                if (effStr != null) formatVersion = EffFormatVersion.FORMAT_EFF; // Unspecied 'EFF' version
            }
        } else {
            // Use corresponding INFO field
            String effFieldName = VcfEffect.infoFieldName(formatVersion);
            effStr = getInfo(effFieldName); // Get effect string from INFO field
        }

        // Create a list of effect
        vcfEffects = new ArrayList<>();

        // Note: An empty "EFF" string can be viewed as a FLAG type and transformed to a "true" value
        if ((effStr == null) || effStr.isEmpty() || effStr.equals("true")) return vcfEffects;

        // Add each effect
        String effs[] = effStr.split(",");
        for (String eff : effs) {
            VcfEffect veff = new VcfEffect(eff, formatVersion); // Create and parse this effect
            vcfEffects.add(veff);
        }

        return vcfEffects;
    }

    public VcfFileIterator getVcfFileIterator() {
        return vcfFileIterator;
    }

    public VcfGenotype getVcfGenotype(int index) {
        return getVcfGenotypes().get(index);
    }

    public List<VcfGenotype> getVcfGenotypes() {
        if (vcfGenotypes == null) parseGenotypes();
        return vcfGenotypes;
    }

    /**
     * Get VcfInfo type for a given ID
     */
    public VcfHeaderInfo getVcfInfo(String id) {
        return vcfFileIterator.getVcfHeader().getVcfHeaderInfo(id);
    }

    /**
     * Get Info number for a given ID
     */
    public VcfInfoType getVcfInfoNumber(String id) {
        VcfHeaderInfo vcfInfo = vcfFileIterator.getVcfHeader().getVcfHeaderInfo(id);
        if (vcfInfo == null) return null;
        return vcfInfo.getVcfInfoType();
    }

    public boolean hasField(String filedName) {
        return vcfFileIterator.getVcfHeader().getVcfHeaderInfo(filedName) != null;
    }

    public boolean hasGenotypes() {
        return ((vcfGenotypes != null) && (vcfGenotypes.size() > 0)) || (genotypeFieldsStr != null);
    }

    public boolean hasInfo(String infoFieldName) {
        if (info == null) parseInfo();
        return info.containsKey(infoFieldName);
    }

    public boolean hasQuality() {
        return quality != null;
    }

    /**
     * Is this bi-allelic (based ONLY on the number of ALTs)
     * WARINIG: You should use 'calcHetero()' method for a more precise calculation.
     */
    public boolean isBiAllelic() {
        if (alts == null) return false;
        return alts.length == 1; // Only one ALT option? => homozygous
    }

    /**
     * Do we have compressed genotypes in "HO,HE,NA" INFO fields?
     */
    public boolean isCompressedGenotypes() {
        return !hasGenotypes() && (getNumberOfSamples() > 0) && (hasInfo(VCF_INFO_HOMS) || hasInfo(VCF_INFO_HETS) || hasInfo(VCF_INFO_NAS));
    }

    public boolean isFilterPass() {
        return filter.equals("PASS");
    }

    /**
     * Is this multi-allelic (based ONLY on the number of ALTs)
     * WARINIG: You should use 'calcHetero()' method for a more precise calculation.
     */
    public boolean isMultiallelic() {
        if (alts == null) return false;
        return alts.length > 1; // More than one ALT option? => not homozygous
    }

    @Override
    protected boolean isShowWarningIfParentDoesNotInclude() {
        return false;
    }

    /**
     * Is thins a VCF entry with a single SNP?
     */
    public boolean isSingleSnp() {
        return ref != null //
                && altStr != null //
                && ref.length() == 1 //
                && altStr.length() == 1 //
                && !ref.equalsIgnoreCase(altStr) //
                ;
    }

    /**
     * Is this variant a singleton (appears only in one genotype)
     */
    public boolean isSingleton() {
        int count = 0;
        for (VcfGenotype gen : this) {
            if (gen.isVariant()) count++;
            if (count > 1) return false;
        }
        return count == 1;
    }

    /**
     * Is this a change or are the ALTs actually the same as the reference
     */
    public boolean isVariant() {
        if (alts == null || alts.length == 0) return false;

        // Is any ALT is variant?
        for (String alt : alts)
            if (isVariant(alt)) return true;

        return false;
    }

    /**
     * Is this ALT string a variant?
     */
    public boolean isVariant(String alt) {
        return alt != null //
                && !alt.isEmpty() //
                && !alt.equals(VcfFileIterator.MISSING) // Missing ALT (".")?
                && !alt.equals(VCF_ALT_NON_REF) // '*'
                && !alt.equals(VCF_ALT_NON_REF_gVCF) // '<NON_REF>'
                && !alt.equals(VCF_ALT_MISSING_REF) // '<*>'
                && !alt.equals(ref) // Is ALT different than REF?
                ;
    }

    @Override
    public Iterator<VcfGenotype> iterator() {
        return getVcfGenotypes().iterator();
    }

    /**
     * Calculate Minor allele count
     */
    public int mac() {
        long ac = -1;

        // Do we have it annotated as AF or MAF?
        if (hasField("MAC")) return (int) getInfoInt("MAC");
        else if (hasField("AC")) ac = getInfoInt("AC");

        // AC not found (or doesn't make sense)
        if (ac <= 0) {
            // We have to calculate
            ac = 0;
            for (byte genCode : getGenotypesScores())
                if (genCode > 0) ac += genCode; // Don't count '-1' (i.e. missing genotypes)
        }

        // How many samples (alleles) do we have?
        int numSamples = 0;
        List<String> sampleNames = vcfFileIterator.getVcfHeader().getSampleNames();
        if (sampleNames != null) numSamples = sampleNames.size();
        else numSamples = getVcfGenotypes().size();

        // Always use the Minor Allele Count
        if ((numSamples > 1) && (ac > numSamples)) ac = 2 * numSamples - ac;

        return (int) ac;
    }

    /**
     * Calculate Minor allele frequency
     */
    public double maf() {
        double maf = -1;

        // Do we have it annotated as AF or MAF?
        if (hasField("AF")) maf = getInfoFloat("AF");
        else if (hasField("MAF")) maf = getInfoFloat("MAF");
        else {
            // No annotations, we have to calculate
            int ac = 0, count = 0;
            for (VcfGenotype gen : this) {
                count += 2;
                int genCode = gen.getGenotypeCode();
                if (genCode > 0) ac += genCode;
            }
            maf = ((double) ac) / count;
        }

        // Always use the Minor Allele Frequency
        if (maf > 0.5) maf = 1.0 - maf;

        return maf;
    }

    /**
     * Parse a 'line' from a 'vcfFileIterator'
     */
    public void parse() {
        // Parse line
        String fields[] = line.split("\t", 10); // Only pare the fist 9 fields (i.e. do not parse genotypes)

        // Is line OK?
        if (fields.length >= 4) {
            // Chromosome and position. VCF files are one-base, so inOffset should be 1.
            chromosomeName = fields[0].trim();

            // Chromosome
            Chromosome chromo = vcfFileIterator.getChromosome(chromosomeName);
            parent = chromo;
            vcfFileIterator.sanityCheckChromo(chromosomeName, chromo); // Sanity check

            // Start
            setStart(vcfFileIterator.parsePosition(vcfFileIterator.readField(fields, 1)));

            // ID (e.g. might indicate dbSnp)
            id = vcfFileIterator.readField(fields, 2);

            // REF
            ref = vcfFileIterator.readField(fields, 3).toUpperCase(); // Reference and change
            strandMinus = false; // Strand is always positive (defined in VCF spec.)

            // ALT
            altStr = vcfFileIterator.readField(fields, 4).toUpperCase();
            parseAlts(altStr);

            // Quality
            String qStr = vcfFileIterator.readField(fields, 5);
            if (!qStr.isEmpty()) quality = Gpr.parseDoubleSafe(qStr);
            else quality = null;

            // Filter
            filter = vcfFileIterator.readField(fields, 6); // Filter parameters

            // INFO fields
            infoStr = vcfFileIterator.readField(fields, 7);
            info = null;

            // Start & End coordinates are anchored to the reference genome, thus based on REF field (ALT is not taken into account)
            parseEnd(altStr);

            // Genotype format
            format = null;
            if (fields.length > 8) format = vcfFileIterator.readField(fields, 8); // This field is optional, So it can be null or EMPTY ('.')

            // Add genotype fields (lazy parse)
            if (fields.length > 9) genotypeFieldsStr = fields[9];
        } else throw new RuntimeException("Impropper VCF entry: Not enough fields (missing tab separators?).\n" + line);
    }

    /**
     * Parse ALT field
     */
    void parseAlts(String altsStr) {
        if (altsStr.length() == 1 || altsStr.indexOf(',') < 0) {
            // SNP or single field (no commas)
            alts = parseAltSingle(altsStr);
            if (alts == null) alts = new String[0];
        } else {
            // Multiple fields (comma separated)
            List<String> altsList = new ArrayList<>();

            // Parse each one
            String altsSplit[] = altsStr.split(",");
            for (String altSingle : altsSplit) {
                String altsTmp[] = parseAltSingle(altSingle);

                // Append all to list
                if (altsTmp != null) {
                    for (String alt : altsTmp)
                        altsList.add(alt);
                }
            }

            alts = altsList.toArray(EMPTY_STRING_ARRAY);
        }
    }

    /**
     * Parse single ALT record, return parsed ALTS
     */
    String[] parseAltSingle(String altsStr) {
        if (altsStr.length() != 1 // Not a SNP? Do not expand
                || !isVariant(altsStr) // Not a variant? Do not expand
                || !vcfFileIterator.isExpandIub() // Do not expand IUB option?
        ) {
            String alts[] = {altsStr};
            return alts;
        }

        // Not a variant?
        if (altsStr.equals(VCF_ALT_NON_REF)) {
            return VCF_ALT_NON_REF_ARRAY;
        }
        if (altsStr.equals(VCF_ALT_MISSING_REF)) {
            return VCF_ALT_MISSING_REF_ARRAY;
        }
        if (altsStr.equals(VCF_ALT_NON_REF_gVCF)) {
            return VCF_ALT_NON_REF_gVCF_ARRAY;
        }

        // SNP IUB conversion table
        String alts[];
        switch (altsStr) {
            case "A":
            case "C":
            case "G":
            case "T":
            case "*":
            case ".":
                alts = new String[1];
                alts[0] = altsStr;
                break;

            case "N": // aNy base
                alts = new String[4];
                alts[0] = "A";
                alts[1] = "C";
                alts[2] = "G";
                alts[3] = "T";
                break;

            case "B": // B: not A
                alts = new String[3];
                alts[0] = "C";
                alts[1] = "G";
                alts[2] = "T";
                break;

            case "D": // D: not C
                alts = new String[3];
                alts[0] = "A";
                alts[1] = "G";
                alts[2] = "T";
                break;

            case "H": // H: not G
                alts = new String[3];
                alts[0] = "A";
                alts[1] = "C";
                alts[2] = "T";
                break;

            case "V": // V: not T
                alts = new String[3];
                alts[0] = "A";
                alts[1] = "C";
                alts[2] = "G";
                break;

            case "M":
                alts = new String[2];
                alts[0] = "A";
                alts[1] = "C";
                break;

            case "R":
                alts = new String[2];
                alts[0] = "A";
                alts[1] = "G";
                break;

            case "W": // Weak
                alts = new String[2];
                alts[0] = "A";
                alts[1] = "T";
                break;

            case "S": // Strong
                alts = new String[2];
                alts[0] = "C";
                alts[1] = "G";
                break;

            case "Y":
                alts = new String[2];
                alts[0] = "C";
                alts[1] = "T";
                break;

            case "K":
                alts = new String[2];
                alts[0] = "G";
                alts[1] = "T";
                break;

            default:
                throw new RuntimeException("WARNING: Unkown IUB code for SNP '" + altsStr + "'");
        }

        return alts;
    }

    /**
     * Parse 'end' coordinate
     */
    void parseEnd(String altStr) {
        setEnd(getStart() + ref.length());

        // Imprecise variants are indicated by an angle brackets '<...>'
        if (altStr.indexOf('<') >= 0) {
            // If there is an 'END' tag, we should use it
            if ((getInfo(VCF_INFO_END) != null)) {
                // Get 'END' field and do some sanity check
                setEnd((int) getInfoInt(VCF_INFO_END));
                if (!isValid()) {
                    throw new RuntimeException("INFO field 'END' is before variant's 'POS'\n\tEND : " + getEnd() + "\n\tPOS : " + getStart());
                }
            }
        }
    }

    /**
     * Parse GENOTPYE entries
     */
    void parseGenotypes() {
        if (isCompressedGenotypes()) {
            uncompressGenotypes();
        } else {
            vcfGenotypes = new ArrayList<>();

            // No genotype string? => Nothing to do
            if (genotypeFieldsStr == null) return;

            // Split genotypes and parse them
            genotypeFields = genotypeFieldsStr.split("\t");
            for (int i = 0; i < genotypeFields.length; i++) {
                String gen = genotypeFields[i];
                if (gen.equals(VcfFileIterator.MISSING)) gen = "";
                addGenotype(gen);
            }
        }
    }

    /**
     * Parse INFO fields
     */
    void parseInfo() {
        // Parse info entries
        info = new HashMap<>();
        for (String inf : infoStr.split(SUB_FIELD_SEP)) {
            String vp[] = inf.split("=", 2);

            if (vp.length > 1) info.put(vp[0], vp[1]); // Key = Value pair
            else info.put(vp[0], "true"); // A property that is present, but has no value (e.g. "INDEL")
        }
    }

    /**
     * Parse LOF from VcfEntry
     */
    public List<VcfLof> parseLof() {
        String lofStr = getInfo(LossOfFunction.VCF_INFO_LOF_NAME);

        ArrayList<VcfLof> lofList = new ArrayList<>();
        if (lofStr == null || lofStr.isEmpty()) return lofList;

        // Split comma separated list
        String lofs[] = lofStr.split(",");
        for (String lof : lofs)
            lofList.add(new VcfLof(this, lof));

        return lofList;
    }

    /**
     * Parse NMD from VcfEntry
     */
    public List<VcfNmd> parseNmd() {
        String nmdStr = getInfo(LossOfFunction.VCF_INFO_NMD_NAME);

        ArrayList<VcfNmd> nmdList = new ArrayList<>();
        if (nmdStr == null || nmdStr.isEmpty()) return nmdList;

        // Split comma separated list
        String nmds[] = nmdStr.split(",");
        for (String nmd : nmds)
            nmdList.add(new VcfNmd(nmd));

        return nmdList;
    }

    /**
     * Parse genotype string (sparse matrix) and set all entries using 'value'
     */
    void parseSparseGt(String str, byte gt[], int valueInt) {
        if ((str == null) || (str.isEmpty()) || (str.equals("true"))) return;

        // Split comma separated indeces
        String idxs[] = str.split(",");
        byte value = (byte) valueInt;

        // Set all entries
        for (String idx : idxs) {
            int i = Gpr.parseIntSafe(idx);
            gt[i] = value;
        }

    }

    /**
     * Remove INFO field
     */
    public void removeInfo(String key) {
        // Not in info field? => Nothing to do
        if (!infoStr.contains(key)) return;

        StringBuilder infoStrNew = new StringBuilder();
        for (String infoEntry : infoStr.split(SUB_FIELD_SEP)) {
            String keyValuePair[] = infoEntry.split("=", 2);

            // Not the key we want to remove? => Add it
            if (!keyValuePair[0].equals(key)) {
                if (infoStrNew.length() > 0) infoStrNew.append(';');
                infoStrNew.append(infoEntry);
            }
        }

        // Create new string
        infoStr = infoStrNew.toString();

        // Update info hash
        if (info != null) info.remove(key);

        // Is this an 'EFF/ANN' field? Remove parsed effects
        if (EffFormatVersion.isEffectVcfInfoField(key)) vcfEffects = null; // Reset field
    }

    /**
     * Parse INFO fields
     */
    public boolean rmInfo(String info) {
        boolean deleted = false;
        StringBuilder infoSb = new StringBuilder();

        // Parse info entries
        for (String inf : infoStr.split(SUB_FIELD_SEP)) {
            String vp[] = inf.split("=");

            if (vp[0].equals(info)) {
                // Delete this field
                deleted = true;
            } else {
                if (infoSb.length() > 0) infoSb.append(SUB_FIELD_SEP);

                infoSb.append(vp[0]);
                if (vp.length > 1) { // Flags don't need '=' sign
                    infoSb.append("=");
                    infoSb.append(vp[1]);
                }
            }
        }

        if (deleted) {
            infoStr = infoSb.toString();
        }
        return deleted;
    }

    public void setGenotypeStr(String genotypeFieldsStr) {
        this.genotypeFieldsStr = genotypeFieldsStr;
    }

    /**
     * To string as a simple "CHR:START_REF/ALTs" format
     */
    @Override
    public String toStr() {
        return getClass().getSimpleName() //
                + "_" + getChromosomeName() //
                + ":" + (getStart() + 1) //
                + "_" + ref //
                + "/" + getAltsStr();
    }

    @Override
    public String toString() {
        boolean deleteLastTab = true;

        StringBuilder sb = new StringBuilder(toStringNoGt());
        sb.append("\t");

        // Is there any 'format' field? It is optional, so it could be 'null'
        if (format != null) {
            sb.append((format.isEmpty() ? "." : format) + "\t");

            // If we have vcfGenotypes parsed, use them
            if ((vcfGenotypes != null) && !vcfGenotypes.isEmpty()) {
                for (VcfGenotype vg : vcfGenotypes)
                    sb.append(vg + "\t");
            } else if (genotypeFieldsStr != null) { // If vcfGenotypes have not been parsed, use raw fields
                sb.append(genotypeFieldsStr);
                deleteLastTab = false;
            }
        }

        if (deleteLastTab) sb.deleteCharAt(sb.length() - 1); // Delete last tab

        return sb.toString();
    }

    /**
     * Show only first eight fields (no genotype entries)
     */
    public String toStringNoGt() {
        // Use original chromosome name or named from chromosome object
        String chr = null;
        if (chromosomeName != null) chr = chromosomeName;
        else if ((parent != null) && (parent instanceof Chromosome)) chr = parent.getId();
        else if (parent != null) chr = getChromosomeName();
        else chr = ".";

        StringBuilder sb = new StringBuilder(chr //
                + "\t" + (getStart() + 1) //
                + "\t" + (id.isEmpty() ? "." : id) //
        );

        // REF and ALT
        String refStr = (ref == null || ref.isEmpty() ? "." : ref);
        sb.append("\t" + refStr);
        sb.append("\t" + getAltsStr());

        // Quality, filter, info, format...
        sb.append("\t" + (quality != null ? quality + "" : "."));
        sb.append("\t" + ((filter == null) || filter.isEmpty() ? "." : filter));
        sb.append("\t" + ((infoStr == null) || infoStr.isEmpty() ? "." : infoStr));

        return sb.toString();
    }

    /**
     * Uncompress VCF entry having genotypes in "HO,HE,NA" fields
     */
    public VcfEntry uncompressGenotypes() {
        // Not compressed? Nothing to do
        if (!isCompressedGenotypes()) return this;

        // Get 'sparse' matrix entries
        String hoStr = getInfo(VCF_INFO_HOMS);
        String heStr = getInfo(VCF_INFO_HETS);
        String naStr = getInfo(VCF_INFO_NAS);

        // Parse 'sparse' entries
        List<String> sampleNames = getVcfFileIterator().getVcfHeader().getSampleNames();
        if (sampleNames == null) throw new RuntimeException("Cannot find sample names in VCF header. Unable to uncompress genotypes.");
        int numSamples = sampleNames.size();
        byte gt[] = new byte[numSamples];
        parseSparseGt(naStr, gt, -1);
        parseSparseGt(heStr, gt, 1);
        parseSparseGt(hoStr, gt, 2);

        // Remove info fields
        if (hoStr != null) rmInfo(VCF_INFO_HOMS);
        if (heStr != null) rmInfo(VCF_INFO_HETS);
        if (naStr != null) rmInfo(VCF_INFO_NAS);
        setFormat("GT");

        // Create output string
        for (int i = 0; i < gt.length; i++) {
            String gtStr;
            switch (gt[i]) {
                case -1:
                    gtStr = "./.";
                    break;

                case 0:
                    gtStr = "0/0";
                    break;

                case 1:
                    gtStr = "0/1";
                    break;

                case 2:
                    gtStr = "1/1";
                    break;

                default:
                    throw new RuntimeException("Unknown code '" + gt[i] + "'");
            }

            addGenotype(gtStr);
        }

        return this;
    }

    /**
     * Create a list of variants from this VcfEntry
     */
    public List<Variant> variants() {
        if (variants != null) return variants;

        // Create list of variants
        variants = new LinkedList<>();

        // Create one Variant for each ALT
        Chromosome chr = (Chromosome) parent;

        if (!isVariant()) {
            // Not a variant?
            List<Variant> vars = variants(chr, getStart(), ref, null, id);
            String alt = ".";

            // Add original 'ALT' field as genotype
            for (Variant variant : vars)
                variant.setGenotype(alt);

            variants.addAll(vars);
        } else {
            // At least one variant
            for (String alt : alts) {
                if (!isVariant(alt)) alt = null;
                List<Variant> vars = variants(chr, getStart(), ref, alt, id);
                variants.addAll(vars);
            }
        }

        return variants;
    }

    /**
     * Create a variant
     */
    List<Variant> variants(Chromosome chromo, int start, String reference, String alt, String id) {
        List<Variant> list = null;
        if (alt != null) alt = alt.toUpperCase();

        if (alt == null || alt.isEmpty() || alt.equals(reference)) {
            // Non-variant
            list = Variant.factory(chromo, start, reference, null, id, false);
        } else if (alt.charAt(0) == '<') {
            // Structural variants
            if (alt.startsWith("<DEL")) {
                // Case: Deletion
                // 2 321682    .  T   <DEL>         6     PASS    IMPRECISE;SVTYPE=DEL;END=321887;SVLEN=-105;CIPOS=-56,20;CIEND=-10,62
                String ch = ref;
                int startNew = start;

                if (getEndClosed() > start) {
                    startNew = start + reference.length();
                    int size = getEndClosed() - startNew + 1;
                    char change[] = new char[size];
                    for (int i = 0; i < change.length; i++)
                        change[i] = reference.length() > i ? reference.charAt(i) : 'N';
                    ch = new String(change);
                }
                list = Variant.factory(chromo, startNew, ch, "", id, false);
            } else if (alt.startsWith("<INV")) {
                // Inversion
                int startNew = start + reference.length();
                Variant var = new Variant(chromo, startNew, getEndClosed(), id);
                var.setVariantType(VariantType.INV);
                list = new LinkedList<>();
                list.add(var);
            } else if (alt.startsWith("<DUP")) {
                // Duplication
                int startNew = start + reference.length();
                Variant var = new Variant(chromo, startNew, getEndClosed(), id);
                var.setVariantType(VariantType.DUP);
                list = new LinkedList<>();
                list.add(var);
            }
        } else if ((alt.indexOf('[') >= 0) || (alt.indexOf(']') >= 0)) {
            // Translocations

            // Parse ALT string
            boolean left = alt.indexOf(']') >= 0;
            String sep = (left ? "\\]" : "\\[");
            String tpos[] = alt.split(sep);
            String pos = tpos[1];
            boolean before = (alt.indexOf(']') == 0) || (alt.indexOf('[') == 0);
            String altBases = (before ? tpos[2] : tpos[0]);

            // Parse 'chr:start'
            String posSplit[] = pos.split(":");
            String trChrName = posSplit[0];
            Chromosome trChr = chromo.getGenome().getOrCreateChromosome(trChrName);
            int trStart = Gpr.parseIntSafe(posSplit[1]) - 1;

            VariantBnd var = new VariantBnd(chromo, start, ref, altBases, trChr, trStart, left, before);
            list = new LinkedList<>();
            list.add(var);
        } else if (reference.length() == alt.length()) {
            // Case: SNP, MNP
            if (reference.length() == 1) {
                // SNPs
                // 20     3 .         C      G       .   PASS  DP=100
                list = Variant.factory(chromo, start, reference, alt, id, vcfFileIterator.isExpandIub());
            } else {
                // MNPs
                // 20     3 .         TC     AT      .   PASS  DP=100
                // Sometimes the first bases are the same and we can trim them
                int startDiff = Integer.MAX_VALUE;
                for (int i = 0; i < reference.length(); i++)
                    if (reference.charAt(i) != alt.charAt(i)) startDiff = Math.min(startDiff, i);

                // MNPs
                // Sometimes the last bases are the same and we can trim them
                int endDiff = 0;
                for (int i = reference.length() - 1; i >= 0; i--)
                    if (reference.charAt(i) != alt.charAt(i)) endDiff = Math.max(endDiff, i);

                String newRef = reference.substring(startDiff, endDiff + 1);
                String newAlt = alt.substring(startDiff, endDiff + 1);
                list = Variant.factory(chromo, start + startDiff, newRef, newAlt, id, vcfFileIterator.isExpandIub());
            }
        } else {
            // Short Insertions, Deletions or Mixed Variants (substitutions)
            VcfRefAltAlign align = new VcfRefAltAlign(alt, reference);
            align.align();
            int startDiff = align.getOffset();

            switch (align.getVariantType()) {
                case DEL:
                    // Case: Deletion
                    // 20     2 .         TC      T      .   PASS  DP=100
                    // 20     2 .         AGAC    AAC    .   PASS  DP=100
                    String ref = "";
                    String ch = align.getAlignment();
                    if (!ch.startsWith("-")) throw new RuntimeException("Deletion '" + ch + "' does not start with '-'. This should never happen!");
                    list = Variant.factory(chromo, start + startDiff, ref, ch, id, vcfFileIterator.isExpandIub());
                    break;

                case INS:
                    // Case: Insertion of A { tC ; tCA } tC is the reference allele
                    // 20     2 .         TC      TCA    .   PASS  DP=100
                    ch = align.getAlignment();
                    ref = "";
                    if (!ch.startsWith("+")) throw new RuntimeException("Insertion '" + ch + "' does not start with '+'. This should never happen!");
                    list = Variant.factory(chromo, start + startDiff, ref, ch, id, vcfFileIterator.isExpandIub());
                    break;

                case MIXED:
                    // Case: Mixed variant (substitution)
                    reference = reference.substring(startDiff);
                    alt = alt.substring(startDiff);
                    list = Variant.factory(chromo, start + startDiff, reference, alt, id, vcfFileIterator.isExpandIub());
                    break;

                default:
                    // Other change type?
                    throw new RuntimeException("Unsupported VCF change type '" + align.getVariantType() + "'\n\tRef: " + reference + "'\n\tAlt: '" + alt + "'\n\tVcfEntry: " + this);
            }
        }

        //---
        // Add original 'ALT' field as genotype
        //---
        if (list == null) list = new LinkedList<>();
        for (Variant variant : list)
            variant.setGenotype(alt);

        return list;
    }

    public enum AlleleFrequencyType {
        Common, LowFrequency, Rare
    }
}
