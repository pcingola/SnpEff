package ca.mcgill.mcb.pcingola.vcf;

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

	/**
	 * Get INF field name for corresponding format
	 */
	public String infoFieldName() {
		switch (this) {
		case FORMAT_ANN:
		case FORMAT_ANN_1:
			return VcfEffect.VCF_INFO_ANN_NAME;

		case FORMAT_EFF:
		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return VcfEffect.VCF_INFO_EFF_NAME;

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
}