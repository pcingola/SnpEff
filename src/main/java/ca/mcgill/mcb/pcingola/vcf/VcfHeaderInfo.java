package ca.mcgill.mcb.pcingola.vcf;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Represents a info elements in a VCF file
 *
 * References: http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
 *
 * INFO fields should be described as follows (all keys are required):
 * 		##INFO=<ID=ID,Number=number,Type=type,Description=�description�>
 *
 * 		Possible Types for INFO fields are: Integer, Float, Flag, Character, and String.
 *
 * 		The Number entry is an Integer that describes the number of values that
 * 		can be included with the INFO field. For example, if the INFO field contains
 * 		a single number, then this value should be 1; if the INFO field describes a
 * 		pair of numbers, then this value should be 2 and so on. If the field has one
 * 		value per alternate allele then this value should be 'A'; if the field has
 * 		one value for each possible genotype (more relevant to the FORMAT tags) then
 * 		this value should be 'G'.  If the number of possible values varies, is unknown,
 * 		or is unbounded, then this value should be '.'. The 'Flag' type indicates that
 * 		the INFO field does not contain a Value entry, and hence the Number should be 0 in
 * 		this case. The Description value must be surrounded by double-quotes. Double-quote
 * 		character can be escaped with backslash (\") and backslash as \\.
 *
 * @author pablocingolani
 */
public class VcfHeaderInfo {

	/**
	 * Number of values in an INFO field.
	 * Reference
	 * 		http://samtools.github.io/hts-specs/VCFv4.2.pdf
	 *
	 * Number of items in an INFO field. The Number entry is an Integer that describes the number of values that can be
	 * included with the INFO field. For example, if the INFO field contains
	 * a single number, then this value should be 1; if the INFO field describes a pair of numbers, then this value should
	 * be 2 and so on. There are also certain special characters used to define special cases:
	 * 		- If the field has one value per alternate allele then this value should be `A'.
	 * 		- If the field has one value for each possible allele (including the reference), then this value should be `R'.
	 * 		- If the field has one value for each possible genotype (more relevant to the FORMAT tags) then this value should be `G'.
	 * 		- If the number of possible values varies, is unknown, or is unbounded, then this value should be `.'.
	 * The `Flag' type indicates that the INFO field does not contain a Value entry, and hence the Number should be 0 in this case.
	 */
	public enum VcfInfoNumber {
		NUMBER, UNLIMITED, ALLELE, ALL_ALLELES, GENOTYPE;

		@Override
		public String toString() {
			switch (this) {
			case ALLELE:
				return "A";

			case ALL_ALLELES:
				return "R";

			case GENOTYPE:
				return "G";

			case NUMBER:
				return "";

			case UNLIMITED:
				return ".";

			default:
				throw new RuntimeException("Unimplemented method for type " + this.name());
			}
		}
	}

	String line;
	String id;
	int number;
	boolean implicit; // Is this field implicit? (Added automatically by VcfHeader class)
	boolean genotype; // Is this a 'genotype' field?
	VcfInfoNumber vcfInfoNumber;
	VcfInfoType vcfInfoType;
	String description;

	public static VcfHeaderInfo factory(String line) {
		if (line.startsWith("##FORMAT=")) return new VcfHeaderInfoGenotype(line);
		return new VcfHeaderInfo(line);
	}

	/**
	 * Constructor using a "##INFO" line from a VCF file
	 */
	public VcfHeaderInfo(String line) {
		// Is this an Info line?
		if (line.startsWith("##INFO=") || line.startsWith("##FORMAT=")) {
			genotype = line.startsWith("##FORMAT=");
			// Remove all trailing '\n'
			while (line.endsWith("\n"))
				line = line.substring(0, line.length() - 1);
			this.line = line;

			int start = line.indexOf('<');
			int end = line.lastIndexOf('>');
			String params = line.substring(start + 1, end);

			// Find ID
			Pattern pattern = Pattern.compile("ID=([^,]+),");
			Matcher matcher = pattern.matcher(params);
			if (matcher.find()) id = matcher.group(1);
			else throw new RuntimeException("Cannot find 'ID' in info line: '" + line + "'");

			// Find and parse 'Number'
			number = -1;
			vcfInfoNumber = VcfInfoNumber.UNLIMITED;
			pattern = Pattern.compile("Number=([^,]+),");
			matcher = pattern.matcher(params);
			if (matcher.find()) setNumber(matcher.group(1));
			else throw new RuntimeException("Cannot find 'Number' in info line: '" + line + "'");

			// Find type
			pattern = Pattern.compile("Type=([^,]+),");
			matcher = pattern.matcher(params);
			if (matcher.find()) vcfInfoType = VcfInfoType.parse(matcher.group(1).toUpperCase());
			else throw new RuntimeException("Cannot find 'Type' in info line: '" + line + "'");

			// Find description
			pattern = Pattern.compile("Description=\\\"(.+)\\\"");
			matcher = pattern.matcher(params);
			if (matcher.find()) description = matcher.group(1);
			else throw new RuntimeException("Cannot find 'Description' in info line: '" + line + "'");

		} else throw new RuntimeException("Line provided is not an INFO definition: '" + line + "'");
	}

	public VcfHeaderInfo(String id, VcfInfoType vcfInfoType, String number, String description) {
		this.id = id;
		this.vcfInfoType = vcfInfoType;
		this.description = description;
		setNumber(number);
	}

	public String getDescription() {
		return description;
	}

	public String getId() {
		return id;
	}

	public int getNumber() {
		return number;
	}

	public VcfInfoType getVcfInfoType() {
		return vcfInfoType;
	}

	public boolean isGenotype() {
		return genotype;
	}

	public boolean isImplicit() {
		return implicit;
	}

	public boolean isNumberAllAlleles() {
		return vcfInfoNumber == VcfInfoNumber.ALL_ALLELES;
	}

	public boolean isNumberNumber() {
		return vcfInfoNumber == VcfInfoNumber.NUMBER;
	}

	public boolean isNumberOnePerAllele() {
		return vcfInfoNumber == VcfInfoNumber.ALLELE;
	}

	public boolean isNumberOnePerGenotype() {
		return vcfInfoNumber == VcfInfoNumber.GENOTYPE;
	}

	public boolean isNumberPerAllele() {
		return vcfInfoNumber == VcfInfoNumber.ALLELE || vcfInfoNumber == VcfInfoNumber.ALL_ALLELES;
	}

	public void setGenotype(boolean genotype) {
		this.genotype = genotype;
	}

	public void setImplicit(boolean implicit) {
		this.implicit = implicit;
	}

	public void setNumber(int number) {
		if (number < 0) throw new RuntimeException("Vcf header's INFO field 'number' must be a non-negative integer!");
		vcfInfoNumber = VcfInfoNumber.NUMBER;
		this.number = number;
	}

	public void setNumber(String number) {
		this.number = -1;

		// Parse number field
		if (number.equals("A")) vcfInfoNumber = VcfInfoNumber.ALLELE;
		else if (number.equals("R")) vcfInfoNumber = VcfInfoNumber.ALL_ALLELES;
		else if (number.equals("G")) vcfInfoNumber = VcfInfoNumber.GENOTYPE;
		else if (number.equals(".")) vcfInfoNumber = VcfInfoNumber.UNLIMITED;
		else {
			int num = Gpr.parseIntSafe(number);
			if (num < 0) throw new RuntimeException("Vcf header's INFO field 'number' must be a non-negative integer: '" + number + "'");
			setNumber(num);
		}
	}

	@Override
	public String toString() {
		if (line != null) return line;

		return VcfHeader.INFO_PREFIX //
				+ "<ID=" + id//
				+ ", Number=" + (number >= 0 ? number : vcfInfoNumber) //
				+ ", Type=" + vcfInfoType //
				+ ", Description=\"" + description + "\"" //
				+ ">" //
				;
	}
}
