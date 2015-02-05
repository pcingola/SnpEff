package ca.mcgill.mcb.pcingola.vcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ca.mcgill.mcb.pcingola.align.VcfRefAltAlign;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.LossOfFunction;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * A VCF entry (a line) in a VCF file

 * @author pablocingolani
 */
public class VcfEntry extends Marker implements Iterable<VcfGenotype> {

	public enum AlleleFrequencyType {
		Common, LowFrequency, Rare
	}

	public static final String[] EMPTY_STRING_ARRAY = new String[0];

	public static final double ALLELE_FEQUENCY_COMMON = 0.05;
	public static final double ALLELE_FEQUENCY_LOW = 0.01;

	public static final String VCF_INFO_END = "END"; // Imprecise variants
	public static final String VCF_ALT_NON_REF = "<NON_REF>"; // NON_REF tag for ALT field (only in gVCF fiels)

	public static final String VCF_INFO_HOMS = "HO";
	public static final String VCF_INFO_HETS = "HE";
	public static final String VCF_INFO_NAS = "NA";

	public static final String VCF_INFO_PRIVATE = "Private";

	public static boolean useNumericGenotype = false;

	private static final long serialVersionUID = 4226374412681243433L;

	protected String line; // Line from VCF file
	protected int lineNum; // Line number
	protected VcfFileIterator vcfFileIterator; // Iterator where this entry was red from
	protected String chromosomeName; // Original chromosome name
	protected String ref;
	protected String[] alts;
	protected Double quality;
	protected String filterPass;
	protected String infoStr = "";
	protected HashMap<String, String> info;
	protected String format;
	protected String formatFields[];
	protected ArrayList<VcfGenotype> vcfGenotypes = null;
	protected VariantType variantType;
	protected String genotypeFields[]; // Raw fields from VCF file
	protected String genotypeFieldsStr; // Raw fields from VCF file (one string, tab separated)

	/**
	 * Check that this value can be added to an INFO field
	 * @return true if OK, false if invalid value
	 */
	public static boolean isValidInfoValue(String value) {
		boolean invalid = ((value != null) && ((value.indexOf(' ') >= 0) || (value.indexOf(';') >= 0) || (value.indexOf('=') >= 0) || (value.indexOf('\t') >= 0) || (value.indexOf('\n') >= 0)));
		return !invalid;
	}

	/**
	 * Return a string safe to be used in an 'INFO' field (VCF file)
	 */
	public static String vcfInfoSafe(String str) {
		//			return str.replaceAll("(\\s|;|,)+", "_");
		if (str == null) return str;
		return str.replaceAll("[ ,;|=()]", "_");
	}

	public VcfEntry(VcfFileIterator vcfFileIterator, Marker parent, String chromosomeName, int start, String id, String ref, String altsStr, double quality, String filterPass, String infoStr, String format) {
		super(parent, start, start + ref.length() - 1, false, id);
		this.chromosomeName = chromosomeName;
		this.ref = ref;
		parseAlts(altsStr);
		this.quality = quality;
		this.filterPass = filterPass;
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
		if (vcfGenotypes == null) vcfGenotypes = new ArrayList<VcfGenotype>();
		if (format == null) format = "";
		vcfGenotypes.add(new VcfGenotype(this, format, vcfGenotypeStr));
	}

	/**
	 * Add a "key=value" tuple the info field
	 *
	 * @param name : INFO key name
	 * @param value : Can be null if it is a boolean field.
	 */
	public void addInfo(String name, String value) {
		if (!isValidInfoValue(name) || !isValidInfoValue(value)) throw new RuntimeException("No white-space, semi-colons, or equals-signs are permitted in INFO field. Name:\"" + name + "\" Value:\"" + value + "\"");

		// Remove previous 'key' for INFO field?
		removeInfo(name);

		// Add to info hash (if available)
		if (info != null) info.put(name, value);

		// Append value to infoStr
		String addInfoStr = name + (value != null ? "=" + value : ""); // String to append
		if ((infoStr == null) || infoStr.isEmpty()) infoStr = addInfoStr;
		else {
			if (!infoStr.endsWith(";")) infoStr += ";"; // Do we need to add a semicolon?
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
			if (!VcfEntry.isValidInfoValue(val)) return "INFO filed '" + infoName + "' has an invalid value '" + val + "' (no spaces, tabs, '=' or ';' are allowed)";

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

	public String[] getAlts() {
		return alts;
	}

	/**
	 * Create a comma separated ALTS string
	 */
	public String getAltsStr() {
		if (alts == null) return "";

		String altsStr = "";
		for (String alt : alts)
			altsStr += alt + " ";
		return altsStr.trim().replace(' ', ',');
	}

	/**
	 * Original chromosome name (as it appeared in the VCF file)
	 */
	@Override
	public String getChromosomeNameOri() {
		return chromosomeName;
	}

	public String getFilterPass() {
		return filterPass;
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
	public byte[] getGenotypesScores() {
		int numSamples = getVcfFileIterator().getVcfHeader().getSampleNames().size();

		// Not compressed? Parse codes
		if (!isCompressedGenotypes()) {
			byte gt[] = new byte[numSamples];

			int idx = 0;
			for (VcfGenotype vgt : getVcfGenotypes())
				gt[idx++] = (byte) vgt.getGenotypeCode();

			return gt;
		}

		//---
		// Uncompress (HO/HE/NA in info fields)
		//---

		// Get 'sparse' matrix entries
		String hoStr = getInfo(VCF_INFO_HOMS);
		String heStr = getInfo(VCF_INFO_HETS);
		String naStr = getInfo(VCF_INFO_NAS);

		// Parse 'sparse' entries
		byte gt[] = new byte[numSamples];
		parseSparseGt(naStr, gt, -1);
		parseSparseGt(heStr, gt, 1);
		parseSparseGt(hoStr, gt, 2);

		return gt;
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

	public VariantType getVariantType() {
		return variantType;
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
		return vcfFileIterator.getVcfHeader().getVcfInfo(id);
	}

	/**
	 * Get Info number for a given ID
	 */
	public VcfInfoType getVcfInfoNumber(String id) {
		VcfHeaderInfo vcfInfo = vcfFileIterator.getVcfHeader().getVcfInfo(id);
		if (vcfInfo == null) return null;
		return vcfInfo.getVcfInfoType();
	}

	public boolean hasField(String filedName) {
		return vcfFileIterator.getVcfHeader().getVcfInfo(filedName) != null;
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

	public boolean isDel() {
		return (variantType == VariantType.DEL);
	}

	public boolean isFilterPass() {
		return filterPass.equals("PASS");
	}

	public boolean isInDel() {
		return (variantType == VariantType.INS) || (variantType == VariantType.DEL);
	}

	public boolean isIns() {
		return (variantType == VariantType.INS);
	}

	public boolean isInterval() {
		return (variantType == VariantType.INTERVAL);
	}

	public boolean isMixedInDel() {
		return variantType == VariantType.MIXED;
	}

	public boolean isMnp() {
		return variantType == VariantType.MNP;
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

	public boolean isSnp() {
		return variantType == VariantType.SNP;
	}

	/**
	 * Is this a change or are the ALTs actually the same as the reference
	 */
	public boolean isVariant() {
		if (alts == null) return false;

		for (String alt : alts)
			if (!alt.isEmpty() && !alt.equals(".") && !ref.equals(alt)) return true; // Any change option is different? => true

		return false;
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
		else {
			// No annotations, we have to calculate
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

			// REF and ALT
			ref = vcfFileIterator.readField(fields, 3).toUpperCase(); // Reference and change
			strandMinus = false; // Strand is always positive (defined in VCF spec.)
			String altsStr = vcfFileIterator.readField(fields, 4).toUpperCase();
			parseAlts(altsStr);

			// Quality
			String qStr = vcfFileIterator.readField(fields, 5);
			if (!qStr.isEmpty()) quality = Gpr.parseDoubleSafe(qStr);
			else quality = null;

			// Filter
			filterPass = vcfFileIterator.readField(fields, 6); // Filter parameters

			// INFO fields
			infoStr = vcfFileIterator.readField(fields, 7);
			info = null;

			// Start & End coordinates are anchored to the reference genome, thus based on REF field (ALT is not taken into account)
			parseEnd(altsStr);

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
		//---
		// Parse altsStr
		//---
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

		//---
		// What type of variant do we have?
		//---
		int maxAltLen = Integer.MIN_VALUE, minAltLen = Integer.MAX_VALUE;

		for (String alt : alts) {
			maxAltLen = Math.max(maxAltLen, alt.length());
			minAltLen = Math.min(minAltLen, alt.length());

			if (alt.startsWith("<DEL")) variantType = VariantType.DEL;
		}

		// Infer change type
		if (variantType == null) {
			if (alts == null // No alts
					|| (alts.length == 0) // Zero ALTs
					|| (alts.length == 1 && (alts[0].isEmpty() || alts[0].equals("."))) // One ALT, but it's empty
			) {
				variantType = VariantType.INTERVAL;
			} else if ((ref.length() == maxAltLen) && (ref.length() == minAltLen)) {
				if (ref.length() == 1) variantType = VariantType.SNP;
				else variantType = VariantType.MNP;
			} else if (ref.length() > minAltLen) variantType = VariantType.DEL;
			else if (ref.length() < maxAltLen) variantType = VariantType.INS;
			else variantType = VariantType.MIXED;
		}
	}

	/**
	 * Parse single ALT record, return parsed ALTS
	 */
	String[] parseAltSingle(String altsStr) {
		String alts[];

		// If ALT is '<NON_REF>', it means that there are no alts
		if (altsStr.equals(VCF_ALT_NON_REF)) return null;

		if (altsStr.length() == 1) {
			if (altsStr.equals("A") || altsStr.equals("C") || altsStr.equals("G") || altsStr.equals("T") || altsStr.equals(".")) {
				alts = new String[1];
				alts[0] = altsStr;
			} else if (altsStr.equals("N")) { // aNy base
				alts = new String[4];
				alts[0] = "A";
				alts[1] = "C";
				alts[2] = "G";
				alts[3] = "T";
			} else if (altsStr.equals("B")) { // B: not A
				alts = new String[3];
				alts[0] = "C";
				alts[1] = "G";
				alts[2] = "T";
			} else if (altsStr.equals("D")) { // D: not C
				alts = new String[3];
				alts[0] = "A";
				alts[1] = "G";
				alts[2] = "T";
			} else if (altsStr.equals("H")) { // H: not G
				alts = new String[3];
				alts[0] = "A";
				alts[1] = "C";
				alts[2] = "T";
			} else if (altsStr.equals("V")) { // V: not T
				alts = new String[3];
				alts[0] = "A";
				alts[1] = "C";
				alts[2] = "G";
			} else if (altsStr.equals("M")) {
				alts = new String[2];
				alts[0] = "A";
				alts[1] = "C";
			} else if (altsStr.equals("R")) {
				alts = new String[2];
				alts[0] = "A";
				alts[1] = "G";
			} else if (altsStr.equals("W")) { // Weak
				alts = new String[2];
				alts[0] = "A";
				alts[1] = "T";
			} else if (altsStr.equals("S")) { // Strong
				alts = new String[2];
				alts[0] = "C";
				alts[1] = "G";
			} else if (altsStr.equals("Y")) {
				alts = new String[2];
				alts[0] = "C";
				alts[1] = "T";
			} else if (altsStr.equals("K")) {
				alts = new String[2];
				alts[0] = "G";
				alts[1] = "T";
			} else if (altsStr.equals(".")) { // No alternative (same as reference)
				alts = new String[1];
				alts[0] = ref;
			} else {
				throw new RuntimeException("WARNING: Unkown IUB code for SNP '" + altsStr + "'");
			}
		} else {
			alts = new String[1];
			alts[0] = altsStr;
		}

		return alts;
	}

	public List<VcfEffect> parseEffects() {
		return parseEffects(null);
	}

	/**
	 * Parse 'EFF' info field and get a list of effects
	 */
	public List<VcfEffect> parseEffects(EffFormatVersion formatVersion) {
		String effStr = null;
		if (formatVersion == null) {
			// Guess which INFO field could be
			effStr = getInfo(VcfEffect.VCF_INFO_ANN_NAME);
			if (effStr != null) {
				formatVersion = EffFormatVersion.FORMAT_ANN; // Unspecied 'ANN' version
			} else {
				effStr = getInfo(VcfEffect.VCF_INFO_EFF_NAME);
				if (effStr != null) formatVersion = EffFormatVersion.FORMAT_EFF; // Unspecied 'EFF' version
			}
		} else {
			// Use corresponding INFO field
			String effFieldName = VcfEffect.infoFieldName(formatVersion);
			effStr = getInfo(effFieldName); // Get effect string from INFO field
		}

		// Create a list of effect
		ArrayList<VcfEffect> effList = new ArrayList<VcfEffect>();
		if ((effStr == null) || effStr.isEmpty() || effStr.equals("true")) return effList; // Note: An empty "EFF" string can be viewed as a FLAG type and transformed to a "true" value

		// Add each effect
		String effs[] = effStr.split(",");
		for (String eff : effs) {
			VcfEffect veff = new VcfEffect(eff, formatVersion); // Create and parse this effect
			effList.add(veff);
		}
		return effList;
	}

	/**
	 * Parse 'end' coordinate
	 */
	void parseEnd(String altStr) {
		end = start + ref.length() - 1;

		// Imprecise variants are indicated by an angle brackets '<...>'
		if (altStr.indexOf('<') >= 0) {
			// If there is an 'END' tag, we should use it
			if ((getInfo(VCF_INFO_END) != null)) {
				// Get 'END' field and do some sanity check
				end = (int) getInfoInt("END") - 1;
				if (end < start) throw new RuntimeException("INFO field 'END' is before varaint's 'POS'\n\tEND : " + end + "\n\tPOS : " + start);
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
			vcfGenotypes = new ArrayList<VcfGenotype>();

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
		info = new HashMap<String, String>();
		for (String inf : infoStr.split(";")) {
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

		ArrayList<VcfLof> lofList = new ArrayList<VcfLof>();
		if (lofStr == null || lofStr.isEmpty()) return lofList;

		// Split comma separated list
		String lofs[] = lofStr.split(",");
		for (String lof : lofs)
			lofList.add(new VcfLof(lof));

		return lofList;
	}

	/**
	 * Parse NMD from VcfEntry
	 */
	public List<VcfNmd> parseNmd() {
		String nmdStr = getInfo(LossOfFunction.VCF_INFO_NMD_NAME);

		ArrayList<VcfNmd> nmdList = new ArrayList<VcfNmd>();
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
		for (String infoEntry : infoStr.split(";")) {
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
	}

	/**
	 * Parse INFO fields
	 */
	public boolean rmInfo(String info) {
		boolean deleted = false;
		StringBuilder infoSb = new StringBuilder();

		// Parse info entries
		for (String inf : infoStr.split(";")) {
			String vp[] = inf.split("=");

			if (vp[0].equals(info)) {
				// Delete this field
				deleted = true;
			} else {
				if (infoSb.length() > 0) infoSb.append(";");

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

	public void setFilterPass(String filterPass) {
		this.filterPass = filterPass;
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
	 * To string as a simple "chr:start-end" format
	 */
	@Override
	public String toStr() {
		return getClass().getSimpleName() + "_" + getChromosomeName() + ":" + (start + 1) + "_" + ref + "/" + getAltsStr();
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
				+ "\t" + ref //
				+ "\t");

		// ALTs
		if (alts != null) {
			for (int i = 0; i < alts.length; i++) {
				String altStr = (alts[i].isEmpty() ? "." : alts[i]);
				sb.append(altStr + ",");
			}
			sb.deleteCharAt(sb.length() - 1); // Delete last colon
		}

		// Quality, filter, info, format...
		sb.append("\t" + (quality != null ? quality + "" : "."));
		sb.append("\t" + ((filterPass == null) || filterPass.isEmpty() ? "." : filterPass));
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
		LinkedList<Variant> list = new LinkedList<Variant>();

		// Create one SeqChange for each ALT
		Chromosome chr = (Chromosome) parent;
		int genotypeNumber = 1;
		if (alts == null) {
			// No ALTs, then it's not a change
			List<Variant> variants = variants(chr, start, ref, null, id);
			String alt = ".";

			for (Variant variant : variants) {
				if (useNumericGenotype) variant.setGenotype(Integer.toString(genotypeNumber));
				else variant.setGenotype(alt);
			}

			list.addAll(variants);
		} else {
			for (String alt : alts) {
				List<Variant> variants = variants(chr, start, ref, alt, id);

				// Set corresponding genotype
				for (Variant variant : variants) {
					if (useNumericGenotype) variant.setGenotype(Integer.toString(genotypeNumber));
					else variant.setGenotype(alt);
				}

				list.addAll(variants);
				genotypeNumber++;
			}
		}

		return list;
	}

	/**
	 * Create a variant
	 */
	List<Variant> variants(Chromosome chromo, int start, String reference, String alt, String id) {
		// No change?
		if (alt == null || alt.isEmpty() || alt.equals(reference)) return Variant.factory(chromo, start, reference, null, id);

		alt = alt.toUpperCase();

		// Case: Structural variant
		// 2 321682    .  T   <DEL>         6     PASS    IMPRECISE;SVTYPE=DEL;END=321887;SVLEN=-105;CIPOS=-56,20;CIEND=-10,62
		if (alt.startsWith("<DEL")) {
			// Create deletion string
			// May be we should be using "imprecise" for these variants
			String ch = ref;
			int startNew = start;

			if (end > start) {
				startNew = start + reference.length();
				int size = end - startNew + 1;
				char change[] = new char[size];
				for (int i = 0; i < change.length; i++)
					change[i] = reference.length() > i ? reference.charAt(i) : 'N';
				ch = new String(change);
			}

			// Create SeqChange
			return Variant.factory(chromo, startNew, ch, "", id);
		}

		// Case: SNP, MNP
		// 20     3 .         C      G       .   PASS  DP=100
		// 20     3 .         TC     AT      .   PASS  DP=100
		if (reference.length() == alt.length()) {
			// SNPs
			if (reference.length() == 1) return Variant.factory(chromo, start, reference, alt, id);

			// MNPs
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
			return Variant.factory(chromo, start + startDiff, newRef, newAlt, id);
		}

		//---
		// Simple Insertions, Deletions or Mixed Variants (substitutions)
		//---
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
			return Variant.factory(chromo, start + startDiff, ref, ch, id);

		case INS:
			// Case: Insertion of A { tC ; tCA } tC is the reference allele
			// 20     2 .         TC      TCA    .   PASS  DP=100
			ch = align.getAlignment();
			ref = "";
			if (!ch.startsWith("+")) throw new RuntimeException("Insertion '" + ch + "' does not start with '+'. This should never happen!");
			return Variant.factory(chromo, start + startDiff, ref, ch, id);

		case MIXED:
			// Case: Mixed variant (substitution)
			reference = reference.substring(startDiff);
			alt = alt.substring(startDiff);
			return Variant.factory(chromo, start + startDiff, reference, alt, id);

		default:
			// Other change type?
			throw new RuntimeException("Unsupported VCF change type '" + align.getVariantType() + "'\n\tRef: " + reference + "'\n\tAlt: '" + alt + "'\n\tVcfEntry: " + this);
		}
	}

}
