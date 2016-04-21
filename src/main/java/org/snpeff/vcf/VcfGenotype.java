package org.snpeff.vcf;

import java.util.HashMap;

import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.util.Gpr;

/**
 * A VCF genotype field
 * There is one genotype per sample in each VCF entry
 *
 * @author pablocingolani
 */
public class VcfGenotype {

	public static final String GT_FIELD_DEPTH_OF_COVERAGE = "DP"; // Approximate read depth (reads with MQ=255 or with bad mates are filtered)
	public static final String GT_FIELD_ALLELIC_DEPTH_OF_COVERAGE = "AD"; // Allelic depths for the ref and alt alleles in the order listed
	String values;
	int genotype[];
	int ploidy;
	boolean phased;
	double gQuality;
	int depth;
	int genotypeLikelihoodPhred[];

	HashMap<String, String> fields;

	VcfEntry vcfEntry;

	public VcfGenotype(VcfEntry vcfEntry, String format, String values) {
		this.vcfEntry = vcfEntry;
		this.values = values;
	}

	/**
	 * Add a name=value pair
	 * WARNING: This method does NOT change the FORMAT field. Use VcfEntry.addFormat() method
	 */
	public void add(String name, String value) {
		// Sanity check value
		if ((value.indexOf(' ') >= 0) //
				|| (value.indexOf('\t') >= 0) //
				|| (value.indexOf('=') >= 0) //
				|| (value.indexOf(':') >= 0) //
		) throw new RuntimeException("Error: Attempt to add a value containin illegal characters: no white-space, semicolons, colons, or equals-signs permitted\n\tname : '" + name + "'\n\tvalue : '" + value + "'");

		// Sanity check format
		if (vcfEntry.getFormat().indexOf(name) < 0) throw new RuntimeException("Error Attempt to add a field (name=" + name + ") that is not present in FORMAT field. Use VcfEntry.addFormat() method first!");

		// Finally, add the values
		values += (values.endsWith(":") ? "" : ":") + value; // Add to value string
		if (fields != null) fields.put(name, value); // Add value to hash (if needed)
	}

	/**
	 * Depth of coverage
	 * @return -1 if not found
	 */
	public int depth() {
		// Try DP field
		String dp = get(GT_FIELD_DEPTH_OF_COVERAGE);
		if (dp != null && !dp.isEmpty()) return Gpr.parseIntSafe(dp);

		String ads = get(GT_FIELD_ALLELIC_DEPTH_OF_COVERAGE);
		if (ads == null || ads.isEmpty()) return -1;

		int depth = 0;
		for (String ad : ads.split(","))
			depth += Gpr.parseIntSafe(ad);

		return depth;
	}

	/**
	 * Get a genotype field
	 */
	public String get(String fieldName) {
		parseFields();
		return fields.get(fieldName);
	}

	/**
	 * Get genotype numbers as an array
	 * E.g.
	 * 		'0/1' -> {0, 1}
	 *
	 * WARNING: If the genotype is missing, the numeric value is '-1'.
	 * E.g.:
	 * 			 './.' -> {-1, -1}
	 *
	 * @return
	 */
	public int[] getGenotype() {
		parseFields(); // Lazy parse
		return genotype;
	}

	/**
	 * Get genotype string by index
	 *
	 * WARNING: If the genotype is missing, it returns an empty string.
	 * E.g.:
	 * 			 './.' -> getGenotype(0) = ""
	 *
	 * @return
	 */
	public String getGenotype(int idx) {
		parseFields(); // Lazy parse

		if (genotype == null) return ""; // Missing genotype

		int num = genotype[idx];
		if (num < 0) return ""; // Missing genotype

		return num == 0 ? vcfEntry.getRef() : vcfEntry.getAlts()[num - 1];
	}

	/**
	 * Return as a genotype SNP code:
	 * 		-1: if missing data of more than one ALT
	 * 		0:	if aa (0/0)
	 * 		1:	if Aa (0/1 or 1/0)
	 * 		2:	if AA (1/1)
	 *
	 * WARNING: In multi-allelic case, any non-ref is treated as ALT
	 *
	 * @return
	 */
	public int getGenotypeCode() {
		parseFields(); // Lazy parse

		// No genotype info?
		if (genotype == null) return -1; // Missing genotype

		int code = 0;
		for (int i = 0; i < genotype.length; i++) {
			if (genotype[i] < 0) return -1; // Missing genotype
			code += (genotype[i] > 0 ? 1 : 0); // Any variant is '1', reference is '0'
		}

		return code;
	}

	/**
	 * Return as a genotype SNP code:
	 * 		0:	if aa (0/0) or any missing value
	 * 		1:	if Aa (0/1 or 1/0)
	 * 		2:	if AA (1/1)
	 *
	 * @return
	 */
	public int getGenotypeCodeIgnoreMissing() {
		parseFields(); // Lazy parse

		// No genotype info?
		if (genotype == null) return -1;

		int code = 0;
		for (int i = 0; i < genotype.length; i++)
			code += (genotype[i] > 0 ? 1 : 0); // Any variant is '1', reference or missing is '0'

		return code;
	}

	/**
	 * Return genotypes as string (e.g. "A/C")
	 * @return
	 */
	public String getGenotypeStr() {
		parseFields(); // Lazy parse

		StringBuilder sb = new StringBuilder();

		if (genotype != null) {
			for (int i = 0; i < genotype.length; i++) {
				int num = genotype[i];

				String gen = ".";
				if (num == 0) gen = vcfEntry.getRef();
				else if (num > 0) gen = vcfEntry.getAlts()[num - 1];

				sb.append(gen);
				if (i < (genotype.length - 1)) {
					if (isPhased()) sb.append("|");
					else sb.append("/");
				}
			}
		}

		return sb.toString();
	}

	public VcfEntry getVcfEntry() {
		return vcfEntry;
	}

	/**
	 * Create a missing genotype string according to organism plodity
	 */
	String gtMissing() {
		// Most cases are covered here
		int len = plodity();
		if (len <= 1) return ".";
		if (len == 2) return "./.";

		// Higher plodity organisms
		StringBuilder sb = new StringBuilder();

		sb.append('.');
		for (int i = 1; i < len; i++)
			sb.append("/.");

		return sb.toString();
	}

	/**
	 * Is the most likely genotype heterozygous?
	 * @return
	 */
	public boolean isHeterozygous() {
		return !isHomozygous();
	}

	/**
	 * Is this genotype homozygous? (either REF or ALT)
	 */
	public boolean isHomozygous() {
		parseFields(); // Lazy parse

		if (genotype != null) {
			// Any genotype is different? => not homozygous
			for (int i = 1; i < genotype.length; i++)
				if (genotype[i] != genotype[i - 1]) return false;

			return true; // Homozygous
		}

		return vcfEntry.isBiAllelic();
	}

	/**
	 * Is this genotype homozygous ALT?
	 */
	public boolean isHomozygousAlt() {
		parseFields(); // Lazy parse

		if (genotype != null) {
			// Any genotype is different? => not homozygous
			for (int i = 1; i < genotype.length; i++)
				if (genotype[i] != genotype[i - 1]) return false;

			return true; // Homozygous
		}

		return vcfEntry.isBiAllelic();
	}

	/**
	 * Is genotpye missing (e.g. "GT=./.")
	 * @return
	 */
	public boolean isMissing() {
		parseFields();

		// The field is missing?
		if (genotype == null) return true;

		// Any genotype missing?
		for (int i = 0; i < genotype.length; i++)
			if (genotype[i] < 0) return true;

		return false;
	}

	public boolean isPhased() {
		parseFields();
		return phased;
	}

	/**
	 * Is any genotype different than REF?
	 * Note: This is calculated for the most likely genotype (GT field)
	 */
	public boolean isRef() {
		if (values.isEmpty()) return false;
		parseFields(); // Lazy parse

		if (genotype != null) {
			// Any genotype is different than REF? => This is a variant
			for (int i = 0; i < genotype.length; i++)
				if (genotype[i] > 0) return false;
			return true;
		}

		return !vcfEntry.isVariant();
	}

	/**
	 * Is any genotype different than REF?
	 * Note: This is calculated for the most likely genotype (GT field)
	 */
	public boolean isVariant() {
		if (values.isEmpty()) return false;
		parseFields(); // Lazy parse

		if (genotype != null) {
			// Any genotype is different than REF? => This is a variant
			for (int i = 0; i < genotype.length; i++)
				if (genotype[i] > 0) return true;

			return false;
		}

		return vcfEntry.isVariant();
	}

	/**
	 * Parse fields
	 */
	void parseFields() {
		if (fields != null) return;

		try {
			fields = new HashMap<String, String>();

			if (values.isEmpty()) return; // Values are missing? Nothing to do

			String format[] = vcfEntry.getFormatFields();
			String fieldValues[] = values.split(":");

			int min = Math.min(fieldValues.length, format.length);

			for (int i = 0; i < min; i++) {
				String name = format[i];
				String value = fieldValues[i];

				fields.put(name, value);
				if (name.equals("GT")) parseGt(value);
				else if (name.equals("PL")) parsePl(value);
				else if (name.equals("GQ")) gQuality = Gpr.parseDoubleSafe(value);
			}

		} catch (Exception e) {
			throw new RuntimeException("Error parsing fields on line:" //
					+ "\n\tFormat   : '" + vcfEntry.getFormat() + "'" //
					+ "\n\tValues   : '" + values + "'" //
					+ "\n\tVcf line : " + vcfEntry //
					, e);
		}
	}

	/**
	 * Parse GT field
	 */
	void parseGt(String value) {
		String gtStr[] = null;
		if (value.indexOf('|') >= 0) {
			gtStr = value.split("\\|");
			phased = true; // Phased
		} else {
			gtStr = value.split("/");
			phased = false; // Not phased
		}

		// Create fields
		genotype = new int[gtStr.length];
		for (int i = 0; i < genotype.length; i++)
			if (gtStr[i].isEmpty() || gtStr[i].equals(".")) genotype[i] = -1; // Genotype '-1' means missing values
			else {
				genotype[i] = Gpr.parseIntSafe(gtStr[i]);

				// Sanity check
				if ((genotype[i] - 1) >= vcfEntry.getAlts().length) {
					boolean plural = vcfEntry.getAlts().length > 1;
					throw new RuntimeException("Error: Bad genotype field '" + value + "'. Genotype says '" + genotype[i] + "' but there " + (plural ? "are" : "is") + " only '" + vcfEntry.getAlts().length + "' allele" + (plural ? "s" : "") + " ('" + vcfEntry.getAltsStr() + "').");
				}
			}
	}

	/**
	 * Parse PL field
	 */
	void parsePl(String value) {
		String plStr[] = value.split(",");
		genotypeLikelihoodPhred = new int[plStr.length];
		for (int i = 0; i < plStr.length; i++)
			genotypeLikelihoodPhred[i] = Gpr.parseIntSafe(plStr[i]);
	}

	/**
	 * Genotype plodity (i.e. how many copies of the chromosome does it have)
	 */
	public int plodity() {
		int gt[] = getGenotype();
		return gt == null ? 0 : gt.length;
	}

	/**
	 * Set a genotype field value
	 */
	public void set(String gtFieldName, String gtValue) {
		String ffields[] = vcfEntry.getFormatFields();
		if (ffields.length < 1) return; // No fields, nothing to do

		// Trying to set "missing genotype"? Make sure we got the plodity right
		if (gtFieldName.equals("GT") && gtValue.equals(VcfFileIterator.MISSING)) gtValue = gtMissing();

		// Rebuild values
		StringBuilder gtsb = new StringBuilder();
		for (String fieldName : ffields) {
			String value = get(fieldName);

			if (fieldName.equals(gtFieldName)) value = gtValue; // Is this the field to replace?
			if (value == null) value = "."; // Missing value?

			gtsb.append((gtsb.length() > 0 ? ":" : "") + value); // Append field value
		}
		values = gtsb.toString();

		// Invalidate previous parsing
		fields = null;
	}

	/**
	 * Set genotype value
	 */
	public void setGenotype(String gtValue) {
		set("GT", gtValue);
	}

	@Override
	public String toString() {
		return (values.isEmpty() ? "." : values);
	}
}
