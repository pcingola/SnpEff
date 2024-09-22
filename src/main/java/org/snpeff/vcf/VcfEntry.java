package org.snpeff.vcf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snpeff.align.VcfRefAltAlign;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.interval.VariantBnd;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.LossOfFunction;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * A VCF entry is a line in a VCF file
 * A VCF line can have multiple variants, and multiple genotypes
 * 
 * The VcfEntry represents the VCF line, NOT the variant itself. for example, if the `START` field
 * in the VCF line may differ from the `start` possition of the variant because the first base of
 * the `REF` field is used as an "anchor".
 * 
 * 
 * @author pablocingolani
 */
public class VcfEntry extends Marker implements Iterable<VcfGenotype> {

	public enum AlleleFrequencyType {
		Common, LowFrequency, Rare
	}

	public static final String FILTER_PASS = "PASS";
	public static final char WITHIN_FIELD_SEP = ',';
	public static final String SUB_FIELD_SEP = ";";
	public static final String[] EMPTY_STRING_ARRAY = new String[0];
	public static final double ALLELE_FEQUENCY_COMMON = 0.05;
	public static final double ALLELE_FEQUENCY_LOW = 0.01;
	public static final int MAX_PADN = 1000;

	public static final Pattern INFO_KEY_PATTERN = Pattern.compile("[\\p{Alpha}_][\\p{Alnum}._]*");

	// In order to report sequencing data evidence for both variant and non-variant positions in the genome, the VCF
	// specification allows to represent blocks of reference-only calls in a single record using the END INFO tag, an idea
	// originally introduced by the gVCF file format. The convention adopted here is to represent reference evidence as
	// likelihoods against an unknown alternate allele. Think of this as the likelihood for reference as compared to any
	// other possible alternate allele (both SNP, indel, or otherwise). A symbolic alternate allele <*> is used to represent
	// this unspecified alternate allele
	//
	// From the VCF Spec: "To retain backwards compatibility with with gVCF, the symbolic allele <NON REF> should be treated as an alias of <*>"
	//
	public static final String VCF_ALT_NON_REF = "<*>"; // See VCF 4.2 section "5.5 Representing unspecified alleles and REF-only blocks (gVCF)"
	public static final String VCF_ALT_NON_REF_OLD = "<NON_REF>"; // NON_REF tag for ALT field (only in gVCF fields (olf version of '<*>')
	public static final String VCF_ALT_MISSING_REF = "*"; // The '*' allele is reserved to indicate that the allele is missing due to a upstream deletion (see VCF 4.3 spec., ALT definition)
	
	public static final String VCF_ALT_INV = "<INV>"; // Inversion

	public static final String[] VCF_ALT_A_ARRAY = { "A" };
	public static final String[] VCF_ALT_C_ARRAY = { "C" };
	public static final String[] VCF_ALT_G_ARRAY = { "G" };
	public static final String[] VCF_ALT_T_ARRAY = { "T" };
	public static final String[] VCF_ALT_N_ARRAY = { "A", "C", "G", "T" }; // aNy base
	public static final String[] VCF_ALT_B_ARRAY = {      "C", "G", "T" }; // B: Not 'A'
	public static final String[] VCF_ALT_D_ARRAY = { "A",      "G", "T" }; // D: not 'C'
	public static final String[] VCF_ALT_H_ARRAY = { "A", "C",      "T" }; // H: No 'G'
	public static final String[] VCF_ALT_V_ARRAY = { "A", "C", "G"      }; // V: Not 'T'
	public static final String[] VCF_ALT_M_ARRAY = { "A", "C"           }; 
	public static final String[] VCF_ALT_R_ARRAY = { "A",      "G"      }; 
	public static final String[] VCF_ALT_W_ARRAY = { "A",           "T" }; // Weak
	public static final String[] VCF_ALT_S_ARRAY = {      "C", "G"      }; // Strong
	public static final String[] VCF_ALT_Y_ARRAY = {      "C",      "T" }; 
	public static final String[] VCF_ALT_K_ARRAY = {           "G", "T" }; 
	public static final String[] VCF_ALT_ASTERISK_ARRAY = { "*" };
	public static final String[] VCF_ALT_MISSING_ARRAY = { "." };

	public static final String[] VCF_ALT_NON_REF_OLD_ARRAY = { VCF_ALT_NON_REF_OLD };
	public static final String[] VCF_ALT_NON_REF_ARRAY = { VCF_ALT_NON_REF };
	// public static final String[] VCF_ALT_MISSING_REF_ARRAY = { VCF_ALT_MISSING_REF };
	public static final String[] VCF_ALT_INV_ARRAY = { VCF_ALT_INV };

	public static final String VCF_INFO_END = "END"; // Imprecise variants. Deprecated: "END has been deprecated in favour of INFO SVLEN and FORMAT LEN."
	public static final String VCF_INFO_SVLEN = "SVLEN"; // "SVLEN must be specified for symbolic structural variant alleles"
	public static final String VCF_INFO_IMPRECISE = "IMPRECISE"; // IMPRECISE must be specified for imprecise structural variant alleles
	public static final String VCF_INFO_HOMS = "HO";
	public static final String VCF_INFO_HETS = "HE";
	public static final String VCF_INFO_NAS = "NA";
	public static final String VCF_INFO_PRIVATE = "Private"; // Private variant

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
	protected String formatFields[];
	protected String genotypeFields[]; // Raw fields from VCF file
	protected String genotypeFieldsStr; // Raw fields from VCF file (one string, tab separated)
	protected byte genotypeScores[];
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
			String values[] = value.split(",");
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
	 * @return true if OK, false if invalid value
	 */
	public static boolean isValidInfoValue(String value) {
		boolean invalid = ((value != null) && ((value.indexOf(' ') >= 0) || (value.indexOf(';') >= 0) || (value.indexOf('=') >= 0) || (value.indexOf('\t') >= 0) || (value.indexOf('\n') >= 0)));
		return !invalid;
	}

	/**
	 * Pad with 'N' characters up length 'len'
	 */
	String padNs(String ref, int len) {
		if(len > MAX_PADN) len = MAX_PADN;
		if(ref == null || ref.isEmpty()) {
			// No reference, create a string of 'N' characters
			char[] bases = new char[len];
			Arrays.fill(bases, 'N');
			return new String(bases);
		} else {
			// Extend reference
			if( ref.length() >= len ) return ref;
			char[] bases = new char[len];
			for( int i = 0; i < len; i++ ) bases[i] = (i < ref.length() ? ref.charAt(i) : 'N');
			return new String(bases);
		}
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
	 * 		%3A : (colon)
	 * 		%3B ; (semicolon)
	 * 		%3D = (equal sign)
	 * 		%25 % (percent sign)
	 * 		%2C , (comma)
	 * 		%0D CR
	 * 		%0A LF
	 * 		%09 TAB
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

	public VcfEntry(VcfFileIterator vcfFileIterator, Marker parent, String chromosomeName, int start, String id, String ref, String altsStr, double quality, String filterPass, String infoStr, String format) {
		super(parent, start, start + ref.length() - 1, false, id);
		this.chromosomeName = chromosomeName;
		this.ref = ref;
		this.alts = parseAlts(altsStr);
		this.quality = quality;
		filter = filterPass;
		this.infoStr = infoStr;
		parseInfo();
		this.format = format;
		this.end = parseEnd();
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
	 * @param key : INFO key name
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
	 *
	 * 		Infer Hom/Her if there is only one sample in the file.
	 * 		Ohtherwise the field is null.
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
			if (type == VcfInfoType.Flag && values.length == 1); // OK, flags must have one or zero values
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

	public String getFormat() {
		return format;
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
				+ ":" + (start + 1) //
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

	public boolean hasAltNonRef() {
		if (alts == null || alts.length == 0) return false;

		// Is any ALT is variant?
		for (String alt : alts)
			if (isAltNonRef(alt)) return true;

		return false;
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
	 * Is this ALT string a NON_REF? (gVCF)
	 */
	public boolean isAltNonRef(String alt) {
		return alt != null && (alt.equals(VCF_ALT_NON_REF) || alt.equals(VCF_ALT_NON_REF_OLD));
	}

	/**
	 * Is this bi-allelic (based ONLY on the number of ALTs)
	 * WARINIG: You should use 'calcHetero()' method for a more precise calculation.
	 */
	public boolean isBiAllelic() {
		if (alts == null) return false;
		return alts.length == 1; // Only one ALT option?
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
		return alts.length > 1; // More than one ALT option?
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
				&& !alt.equals(VCF_ALT_NON_REF) // '<*>'
				&& !alt.equals(VCF_ALT_NON_REF_OLD) // '<NON_REF>'
				&& !alt.equals(VCF_ALT_MISSING_REF) // '*'
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
			start = vcfFileIterator.parsePosition(vcfFileIterator.readField(fields, 1));

			// ID (e.g. might indicate dbSnp)
			id = vcfFileIterator.readField(fields, 2);

			// REF
			ref = vcfFileIterator.readField(fields, 3).toUpperCase(); // Reference and change
			strandMinus = false; // Strand is always positive (defined in VCF spec.)

			// ALT
			altStr = vcfFileIterator.readField(fields, 4).toUpperCase();
			this.alts = parseAlts(altStr);

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
			// But INFO fields can affect Start & End coordinates, for example in cases of imprecise variants
			this.end = parseEnd();

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
	String[] parseAlts(String altsStr) {
		if (altsStr.length() == 1) {
			// Probably a SNP (single character)
			return parseAltSingleChar(altsStr);
		} else if (altsStr.indexOf(',') < 0) {
			// Single field (no commas)
			alts = parseAltSingle(altsStr);
			return alts != null ? alts : new String[0];
		} else {
			// Multiple fields (comma separated)
			List<String> altsList = new ArrayList<>();

			// Parse each one
			String altsSplit[] = altsStr.split(",");
			for (String altSingle : altsSplit) {
				// Each `altSingle` can be expanded into multiple ALTs because of IUB codes
				String altsTmp[] = parseAltSingle(altSingle);

				// Append all to list
				if (altsTmp != null) {
					for (String alt : altsTmp)
						altsList.add(alt);
				}
			}

			return altsList.toArray(EMPTY_STRING_ARRAY);
		}
	}

	/**
	 * Parse single ALT record which is NOT a SNP, return parsed ALTS
	 * Return null if it is not a variant
	 */
	String[] parseAltSingle(String altsStr) {
		if (altsStr.length() == 1) return parseAltSingleChar(altsStr);
		// All other cases
		return new String[] { altsStr };
	}

	/**
	 * Parse single ALT record which is a SNP, expand IUB codes
	 * Return parsed ALTS
	 */
	String[] parseAltSingleChar(String altsStr) {
		// Standard SNPs, no IUB expansion
		switch (altsStr) {
			case "A": return VCF_ALT_A_ARRAY;
			case "C": return VCF_ALT_C_ARRAY;
			case "G": return VCF_ALT_G_ARRAY;
			case "T": return VCF_ALT_T_ARRAY;
			case "*": return VCF_ALT_ASTERISK_ARRAY;
			case ".": return VCF_ALT_MISSING_ARRAY;
		}

		// Is IUB expantion enabled?
		if(!vcfFileIterator.isExpandIub() ) return new String[] { altsStr };

		// SNP IUB conversion table
		switch (altsStr) {
			case "N": return VCF_ALT_N_ARRAY;
			case "B": return VCF_ALT_B_ARRAY;
			case "D": return VCF_ALT_D_ARRAY;
			case "H": return VCF_ALT_H_ARRAY;
			case "V": return VCF_ALT_V_ARRAY;
			case "M": return VCF_ALT_M_ARRAY;
			case "R": return VCF_ALT_R_ARRAY;
			case "W": return VCF_ALT_W_ARRAY;
			case "S": return VCF_ALT_S_ARRAY;
			case "Y": return VCF_ALT_Y_ARRAY;
			case "K": return VCF_ALT_K_ARRAY;
			default:
				throw new RuntimeException("WARNING: Unkown IUB code for SNP '" + altsStr + "'");
		}
	}

	/**
	 * Parse 'end' coordinate
	 */
	int parseEnd() {
		// SNP or single char variant?
		if( altStr.length() == 1 ) return start;

		// Imprecise variants are indicated by an angle brackets '<...>'
		if (altStr.indexOf('<') >= 0 ) {
			if( altStr.indexOf("<INS") >= 0 ) return start; // Insertions happen at one position (right after POS)
			
			// For other imprecise variants, we need to check INFO fields
			if ( hasInfo(VCF_INFO_END) ) {
				// From VCF specification:
				//	```
				// 	For backwards compatibility, a missing SVLEN should be inferred from the END field.
				//	...
				//	This is a computed field that, when present, must be set to the maximum end reference position (1-based) of: 
				//		the position of the final base of the REF allele, 
				//		the end position corresponding to the SVLEN of a symbolic SV allele, 
				//		and the end positions calculated from FORMAT LEN for the <*> symbolic allele.
				//	```
				// This means that the END possition includes the last base of the REF allele, i.e. `[start, end]` is a 
				// closed interval (not half-open as in BED format).

				// Get 'END' field and do some sanity check
				end = (int) getInfoInt(VCF_INFO_END) - 1;	// END is closed 1-based (includes the last base from REF)
				if (end < start) throw new RuntimeException("INFO field 'END' is before varaint's 'POS'\n\tEND : " + end + "\n\tPOS : " + start);
				return end;
			} else if ( hasInfo(VCF_INFO_SVLEN) ) {
				// From VCF specification:
				//   ```
				//   SVLEN must be specified for symbolic structural variant alleles. SVLEN is defined for INS, DUP, 
				//   INV, and DEL symbolic alleles as the number of the inserted, duplicated, inverted, and deleted
				//   bases respectively. SVLEN is defined for CNV symbolic alleles as the length of the segment over
				//   which the copy number variant is defined. The missing value . should be used for all other ALT
				//   alleles, including ALT alleles using breakend notation.
				//   ```
				var svlen = (int) getInfoInt(VCF_INFO_SVLEN);
				// 	"For backwards compatibility, the absolute value of SVLEN should be taken and a negative SVLEN
				//	should be treated as positive values."
				if (svlen < 0) svlen = -svlen;
				return start + (svlen - 1);	// END is closed interval (i.e. the last base from REF is included)
			} else {
				// If neither SVLEN nor END fields are present, we can't infer the end position, we'll use 'ref' length
				return start + ref.length() - 1;
			}
		}
		
		// Normal variants (i.e. not imprecise)
		// We use 'ref' length to calculate 'end' position
		return start + ref.length() - 1;
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

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setGenotypeStr(String genotypeFieldsStr) {
		this.genotypeFieldsStr = genotypeFieldsStr;
	}

	public void setLineNum(int lineNum) {
		this.lineNum = lineNum;
	}

	/**
	 * To string as a simple "CHR:START_REF/ALTs" format
	 */
	@Override
	public String toStr() {
		return getClass().getSimpleName() //
				+ "_" + getChromosomeName() //
				+ ":" + (start + 1) //
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
				+ "\t" + (start + 1) //
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
			List<Variant> vars = variants(chr, start, ref, null, id);
			String alt = ".";

			// Add original 'ALT' field as genotype
			for (Variant variant : vars)
				variant.setGenotype(alt);

			variants.addAll(vars);
		} else {
			// At least one variant
			for (String alt : alts) {
				if (!isVariant(alt)) alt = null;
				List<Variant> vars = variants(chr, start, ref, alt, id);
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
		} else if (reference.length() == 1 && alt.length() == 1) {
			// The most common case: SNPs
			// VCF entry example:
			//		20     3 .         C      G       .   PASS  DP=100
			list = Variant.factory(chromo, start, reference, alt, id, vcfFileIterator.isExpandIub());
		} else if (alt.charAt(0) == '<') {
			list = variantsStructural(chromo, start, reference, alt, id);
		} else if ((alt.indexOf('[') >= 0) || (alt.indexOf(']') >= 0)) {
			list = variantsTranslocation(chromo, start, reference, alt, id);
		} else if (reference.length() == alt.length()) {
			list = variantsMnp(chromo, start, reference, alt, id);
		} else {
			list = variantsInDelMixed(chromo, start, reference, alt, id);
		}

		if (list == null) list = new LinkedList<>();
		else {
			// Assign the original 'ALT' to the genotype field, so we can trace it back even if we changed the 'ALT' when creating the variant
			for (Variant variant : list) {
				variant.setGenotype(alt);
			}
		}
		return list;
	}

	/**
	 * Create a list of Ins/Del/Mixed variants
	 */
	List<Variant> variantsInDelMixed(Chromosome chromo, int start, String reference, String alt, String id) {
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
				return Variant.factory(chromo, start + startDiff, ref, ch, id, vcfFileIterator.isExpandIub());

			case INS:
				// Case: Insertion of A { tC ; tCA } tC is the reference allele
				// 20     2 .         TC      TCA    .   PASS  DP=100
				ch = align.getAlignment();
				ref = "";
				if (!ch.startsWith("+")) throw new RuntimeException("Insertion '" + ch + "' does not start with '+'. This should never happen!");
				return Variant.factory(chromo, start + startDiff, ref, ch, id, vcfFileIterator.isExpandIub());

			case MIXED:
				// Case: Mixed variant (substitution)
				reference = reference.substring(startDiff);
				alt = alt.substring(startDiff);
				return Variant.factory(chromo, start + startDiff, reference, alt, id, vcfFileIterator.isExpandIub());
			
			default:
				throw new RuntimeException("Expecting either 'INS', 'DEL', or 'MIXED'. Unsupported type '" + align.getVariantType() + "'\n\tRef: " + reference + "'\n\tAlt: '" + alt + "'\n\tVcfEntry: " + this);
		}
	}
	
	/**
	 * Create a list of MNP variants
	 * Example of VCF MNP entry:
	 * 		20     3 .         TC     AT      .   PASS  DP=100
	 */
	List<Variant> variantsMnp(Chromosome chromo, int start, String reference, String alt, String id) {
		// Sometimes the first bases are the same and we can trim them
		int startDiff = Integer.MAX_VALUE;
		for (int i = 0; i < reference.length(); i++)
			if (reference.charAt(i) != alt.charAt(i)) startDiff = Math.min(startDiff, i);

		// Sometimes the last bases are the same and we can trim them
		int endDiff = 0;
		for (int i = reference.length() - 1; i >= 0; i--)
			if (reference.charAt(i) != alt.charAt(i)) endDiff = Math.max(endDiff, i);

		String newRef = reference.substring(startDiff, endDiff + 1);
		String newAlt = alt.substring(startDiff, endDiff + 1);
		List<Variant> list = Variant.factory(chromo, start + startDiff, newRef, newAlt, id, vcfFileIterator.isExpandIub());
		return list;
	}

	/**
	 * Create a list of structural variants
	 * See VCF specification sections 3 "INFO keys used for structural variants", and 4 "FORMAT keys used for structural variants"
	 * 
	 * From section 3:
	 * 		```
	 * 		The following INFO keys are reserved for encoding structural variants. In general, when these keys are used by imprecise
	 * 		variants, the values should be best estimates
	 * 		
	 * 		##INFO=<ID=IMPRECISE,Number=0,Type=Flag,Description="Imprecise structural variation">
	 * 		Indicates that this record contains an imprecise structural variant ALT allele...
	 * 
	 * 		##INFO=<ID=END,Number=1,Type=Integer,Description="Deprecated. Present for backwards compatibility with earlier versions of VCF.">
	 * 		END has been deprecated in favour of INFO SVLEN and FORMAT LEN.
	 * 
	 * 		##INFO=<ID=SVTYPE,Number=1,Type=String,Description="Type of structural variant">
	 * 		This field has been deprecated due to redundancy with ALT. Refer to section 1.4.5 for the set of 
	 * 		valid ALT field symbolic structural variant alleles.
	 * 
	 * 		##INFO=<ID=SVLEN,Number=A,Type=Integer,Description="Length of structural variant">
	 * 		SVLEN must be specified for symbolic structural variant alleles. SVLEN is defined for INS, DUP, INV, 
	 * 		and DEL symbolic alleles as the number of the inserted, duplicated, inverted, and deleted bases
	 * 		respectively. SVLEN is defined for CNV symbolic alleles as the length of the segment over which the
	 * 		copy number variant is defined.
	 * 		For backwards compatibility, a missing SVLEN should be inferred from the END field.
	 * 		For backwards compatibility, the absolute value of SVLEN should be taken and a negative SVLEN should
	 * 		be treated as positive values.
	 * 		```
	 */
	List<Variant> variantsStructural(Chromosome chromo, int start, String reference, String alt, String id) {
		List<Variant> list = null;

		// Update start position
		// from VCF spec.: "Note that for structural variant symbolic alleles, POS corresponds to the base immediately preceding the variant."
		var startVariant = Math.min(start + 1, end);

		// Variant's reference
		var refVariant = reference;
		// If the reference allele is longer than the variant, pad with Ns
		int svlen = (end >= startVariant ? end - startVariant + 1 : 0);
		// Variant's reference: Remove anchor base ("... POS corresponds to the base immediately preceding the variant")
		if( refVariant.length() > 1 ) {
			refVariant = reference.substring(1); // Remove the first base
			refVariant = padNs(refVariant, svlen); // Pad with Ns if necessary
		} else {
			// If it's one base, we need to remove the anchor base and pad with Ns, which is the same as creating a new string with Ns
			refVariant = padNs(null, svlen);
		}

		// Structural variants
		VariantType vaType = null;
		boolean hasImprecise = hasInfo(VCF_INFO_IMPRECISE);
		if (alt.startsWith("<DEL")) {
			list = Variant.factory(chromo, startVariant, refVariant, "", id, false);
			vaType = VariantType.DEL;
		} else if (alt.startsWith("<DUP")) {
			list = variantsStructuralDup(chromo, startVariant, refVariant, alt, id);
			vaType = VariantType.DUP;
		} else if (alt.startsWith("<INV")) {
			list = variantsStructuralInv(chromo, startVariant, refVariant, alt, id);
			vaType = VariantType.INV;
		} else if (alt.startsWith("<CNV")) {
			list = variantsStructuralCnv(chromo, startVariant, refVariant, alt, id);
			vaType = VariantType.CNV;
		} else if (alt.startsWith("<INS")) {
			// Note that we use the original 'start' allele instead of 'startVariant' because the <INS> happens right after the anchor base
			list = variantsStructuralIns(chromo, start, alt, id);
			vaType = VariantType.INS;
		} else {
			// Unknown structural variants?
			throw new RuntimeException("Unsupported structural variant type '" + alt + "'");
		}

		// Adjust variants: Set variant type, adjust 'end' coordinate, set 'imprecise' flag
		for (Variant var : list) {
			// Set variant type
			var.setVariantType(vaType);
			// Make sure the `end` coordinate is correct
			// Why is this necessary? 
			// 	When padding with Ns, `padNs` function will add at most `MAX_PADNS` Ns, since the `end` coordinate 
			// 	is based on the length of the reference allele, we may need to adjust it
			var.setEnd(end);
			// Set 'imprecise' flag if INFO field 'IMPRECISE' is present
			if( hasImprecise ) var.setImprecise(true);
		}

		return list;
	}

	List<Variant> variantsStructuralCnv(Chromosome chromo, int start, String reference, String alt, String id) {
		// Create the variant and set the type
		Variant var = new Variant(chromo, start, end, id);
		var.setVariantType(VariantType.CNV);

		// Create a list of varinats
		List<Variant> list = new LinkedList<>();
		list.add(var);
		return list;
	}

	/**
	 * Create a list of <DUP> variants
	 * VCF Specification (version 4.5) FORMAT keys used for structural variants
	 */
	List<Variant> variantsStructuralDup(Chromosome chromo, int start, String reference, String alt, String id) {
		int svlen = end - start + 1;
		var altDup = padNs(reference + reference, svlen);

		// Create the variant and set the type
		Variant var = new Variant(chromo, start, ref, altDup, id);
		var.setVariantType(VariantType.DUP);
		
		// Create a list of varinats
		List<Variant> list = new LinkedList<>();
		list.add(var);
		return list;
	}

	List<Variant> variantsStructuralIns(Chromosome chromo, int start, String alt, String id) {
		// Create the variant and set the type
		Variant var = new Variant(chromo, start, "", alt, id);
		var.setVariantType(VariantType.INS);

		// Create a list of varinats
		List<Variant> list = new LinkedList<>();
		list.add(var);
		return list;
	}

	/**
	 * Create a list of <INV> variants
	 */
	List<Variant> variantsStructuralInv(Chromosome chromo, int start, String reference, String alt, String id) {
		// Calculate the 'ALT' sequence
		// If 'REF' is more than a single character, then 'ALT' is 'REF' reversed
		var altInv = alt;
		if(reference.length() > 1 ) {
			altInv = (new StringBuffer(reference)).reverse().toString();
			int svlen = end - start + 1;
			altInv = padNs(altInv, svlen); // Pad with Ns if necessary
		}

		// Create the variant and set the type
		Variant var = new Variant(chromo, start, reference, altInv, id);
		
		// Create a list of varinats
		List<Variant> list = new LinkedList<>();
		list.add(var);
		return list;
	}

	/**
	 * Create a list of translocation variants
	 */
	List<Variant>  variantsTranslocation(Chromosome chromo, int start, String reference, String alt, String id) {
		List<Variant> list = null;
			
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

		return list;
	}
}
