package org.snpeff.interval;

/**
 * Transcript level support
 * Reference: http://useast.ensembl.org/Help/Glossary?id=492;redirect=no
 *
 * @author pcingola
 */
public enum TranscriptSupportLevel {

	TSL_1 // All splice junctions of the transcript are supported by at least one non-suspect mRNA
	, TSL_2 // The best supporting mRNA is flagged as suspect or the support is from multiple ESTs
	, TSL_3 // The only support is from a single EST
	, TSL_4 // The best supporting EST is flagged as suspect
	, TSL_5 // No single transcript supports the model structure
	,
	/**
	The transcript was not analyzed for one of the following reasons:
	pseudo-gene annotation, including transcribed pseudo-genes
	human leukocyte antigen (HLA) transcript
	immunoglobin gene transcript
	T-cell receptor transcript
	single-exon transcript (will be included in a future version)
	**/
	TSL_NA;

	public static final int TSL_NULL_VALUE = 9; // This number should be higher than any other TSL number

	public static TranscriptSupportLevel parse(String str) {
		if (str.startsWith("TSL_")) return TranscriptSupportLevel.valueOf(str);

		// Sometimes GTF files have strings like this one:
		//      transcript_support_level "4 (assigned to previous version 3)"
		// So we have to remove the part in parenthesis
		if (str.length() > 2) str = str.substring(0, 2).trim();

		// Safely parse TSL
		try {
			return TranscriptSupportLevel.valueOf("TSL_" + str);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Return TSL level (-1 if not available / invalid)
	 */
	public static int tsl(TranscriptSupportLevel tsl) {
		if (tsl == null) return TSL_NULL_VALUE;

		switch (tsl) {
		case TSL_1:
			return 1;

		case TSL_2:
			return 2;

		case TSL_3:
			return 3;

		case TSL_4:
			return 4;

		case TSL_5:
			return 5;

		case TSL_NA:
			return 6;

		default:
			throw new RuntimeException("Unimplemented TSL level " + tsl);
		}
	}

}
