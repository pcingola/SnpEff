package org.snpeff.vcf;

import java.util.HashSet;
import java.util.Set;

/**
 * VcfFields in SnpEff version 2.X have a different format than 3.X
 * As of version 4.1 we switch to a standard annotation format
 */
public enum EffFormatVersion {
	FORMAT_EFF // Unspecified 'EFF' version
	, FORMAT_EFF_2 // EFF version 2
	, FORMAT_EFF_3 // EFF version 3. Added: AA_length
	, FORMAT_EFF_4 // EFF version 4. Added: Exon/Intron rank and 'genotype' (Cancer samples)
	, FORMAT_ANN // Unspecified ANN.
	, FORMAT_ANN_1 // ANN version 1. Standard annotation format (2014-12 / 2015-01)
	;

	public static final EffFormatVersion DEFAULT_FORMAT_VERSION = FORMAT_ANN_1;

	public static final String VCF_INFO_EFF_NAME = "EFF";
	public static final String VCF_INFO_ANN_NAME = "ANN";
	public static final String[] VCF_INFO_ANN_NAMES = { VCF_INFO_ANN_NAME, VCF_INFO_EFF_NAME };

	public static final String EFFECT_TYPE_SEPARATOR = "&"; // Separator between mutiple effectTypes
	public static final String EFFECT_TYPE_SEPARATOR_OLD = "+"; // Old separator between mutiple effectTypes

	protected static Set<String> vcfInfoNames;

	/**
	 * Initialize class
	 */
	static {
		vcfInfoNames = new HashSet<>();

		for (String key : VCF_INFO_ANN_NAMES)
			vcfInfoNames.add(key);
	}

	/**
	 * Is this one of the VCF INFO keys?
	 */
	public static boolean isEffectVcfInfoField(String key) {
		return vcfInfoNames.contains(key);
	}

	/**
	 * Get INF field name for corresponding format
	 */
	public String infoFieldName() {
		switch (this) {
		case FORMAT_ANN:
		case FORMAT_ANN_1:
			return VCF_INFO_ANN_NAME;

		case FORMAT_EFF:
		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return VCF_INFO_EFF_NAME;

		default:
			throw new RuntimeException("Unknown format: " + this);
		}
	}

	/**
	 * Is this an 'ANN' format?
	 */
	public boolean isAnn() {
		switch (this) {
		case FORMAT_ANN:
		case FORMAT_ANN_1:
			return true;

		case FORMAT_EFF:
		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return false;

		default:
			throw new RuntimeException("Unknown format: " + this);
		}
	}

	/**
	 * Id this an 'EFF' format?
	 */
	public boolean isEff() {
		switch (this) {
		case FORMAT_ANN:
		case FORMAT_ANN_1:
			return false;

		case FORMAT_EFF:
		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return true;

		default:
			throw new RuntimeException("Unknown format: " + this);
		}
	}

	/**
	 * Is this format fully specified? I.e. does it have version number?
	 */
	public boolean isFullVersion() {
		switch (this) {
		case FORMAT_ANN:
		case FORMAT_EFF:
			return false;

		case FORMAT_ANN_1:
		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return true;

		default:
			throw new RuntimeException("Unknown format: " + this);
		}
	}

	/**
	 * Multiple effect separator
	 */
	public String separator() {
		switch (this) {
		case FORMAT_ANN_1:
		case FORMAT_ANN:
			return "&";

		case FORMAT_EFF:
		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return "+";

		default:
			throw new RuntimeException("Unknown format: " + this);
		}
	}

	/**
	 * Multiple effect separator: Split regex
	 */
	public String separatorSplit() {
		switch (this) {
		case FORMAT_ANN_1:
		case FORMAT_ANN:
			return "\\&";

		case FORMAT_EFF:
		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return "\\+";

		default:
			throw new RuntimeException("Unknown format: " + this);
		}
	}

	/**
	 * VCF header for each format type
	 */
	public String vcfHeader() {
		switch (this) {
		case FORMAT_EFF_2:
			return "##INFO=<ID=EFF,Number=.,Type=String,Description=\"Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_change| Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon [ | ERRORS | WARNINGS ] )' \">";

		case FORMAT_EFF_3:
			return "##INFO=<ID=EFF,Number=.,Type=String,Description=\"Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_change| Amino_Acid_length | Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon [ | ERRORS | WARNINGS ] )' \">";

		case FORMAT_EFF_4:
			return "##INFO=<ID=EFF,Number=.,Type=String,Description=\"Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_Change| Amino_Acid_length | Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon_Rank  | Genotype [ | ERRORS | WARNINGS ] )' \">";

		case FORMAT_ANN_1:
			return "##INFO=<ID=ANN,Number=.,Type=String,Description=\"Functional annotations: 'Allele | Annotation | Annotation_Impact | Gene_Name | Gene_ID | Feature_Type | Feature_ID | Transcript_BioType | Rank | HGVS.c | HGVS.p | cDNA.pos / cDNA.length | CDS.pos / CDS.length | AA.pos / AA.length | Distance | ERRORS / WARNINGS / INFO' \">";

		default:
			throw new RuntimeException("Unimplemented format '" + this + "'");
		}

	}
}
