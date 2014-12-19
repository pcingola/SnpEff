package ca.mcgill.mcb.pcingola.vcf;

/**
 * VcfFields in SnpEff version 2.X have a different format than 3.X
 * As of version 4.1 we switch to a standard annotation format
 */
public enum EffFormatVersion {
	FORMAT_EFF_2 //
	, FORMAT_EFF_3 // Added: AA_length
	, FORMAT_EFF_4 // Added: Exon/Intron rank and 'genotype' (Cancer samples)
	, FORMAT_ANN_5 // Standard annotation format (2014-12)
	;

	public static final EffFormatVersion DEFAULT_FORMAT_VERSION = FORMAT_ANN_5;

	/**
	 * Get INF field name for corresponding format
	 */
	public String infoFieldName() {
		switch (this) {
		case FORMAT_ANN_5:
			return VcfEffect.VCF_INFO_ANN_NAME;

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
		case FORMAT_ANN_5:
			return true;

		case FORMAT_EFF_4:
		case FORMAT_EFF_3:
		case FORMAT_EFF_2:
			return false;

		default:
			throw new RuntimeException("Unknown format: " + this);
		}
	}
}