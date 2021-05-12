package org.snpeff.snpEffect;

/**
 * Errors and warnings
 */
public enum ErrorWarningType {
	//
	// Variant effect errors and warnings
	//
	INFO_REALIGN_3_PRIME // 		Variant has been realigned to the most 3-prime position within the transcript. This is usually done to to comply with HGVS specification to always report the most 3-prime annotation.
	, WARNING_SEQUENCE_NOT_AVAILABLE // The exon does not have reference sequence information
	, WARNING_REF_DOES_NOT_MATCH_GENOME // Sequence reference does not match variant's reference (alignment problem?)
	, WARNING_TRANSCRIPT_INCOMPLETE // Number of coding bases is NOT multiple of 3, so there is missing information for at least one codon.
	, WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS // Multiple STOP codons found in a CDS (usually indicates frame errors un one or more exons)
	, WARNING_TRANSCRIPT_NO_START_CODON // Start codon does not match any 'start' codon in the CodonTable
	, WARNING_TRANSCRIPT_NO_STOP_CODON // Stop codon does not match any 'stop' codon in the CodonTable
	, ERROR_CHROMOSOME_NOT_FOUND // Chromosome name not found. Typically due to mismatch in chromosome naming conventions between variants file and database, but can be a more severa problem (different reference genome)
	, ERROR_OUT_OF_CHROMOSOME_RANGE // Variant is outside chromosome
	, ERROR_OUT_OF_EXON //
	, ERROR_MISSING_CDS_SEQUENCE // Missing coding sequence information
	//
	// Build errors and warnings
	//
	, WARNING_CDS_TOO_SHORT // CDS is too short and cannot be corrected
	, WARNING_CHROMOSOME_NOT_FOUND // Chromosome not found
	, WARNING_CHROMOSOME_CIRCULAR // Chromosome might be circular
	, WARNING_CHROMOSOME_LENGTH // Chromosome length might be incorrect (e.g. genes have boundaries outside chromosome coordiantes)
	, WARNING_GENE_COORDINATES // Gene coordinates are adjusted
	, WARNING_GENE_ID_DUPLICATE // Gene ID already exists
	, WARNING_GENE_NOT_FOUND // Gene not found
	, WARNING_EXON_NOT_FOUND // Could not find exon
	, WARNING_EXON_SEQUENCE_LENGTH // Exon sequence doesn't match exon size
	, WARNING_EXON_TOO_SHORT // Exon is too short, cannot be corrected
	, WARNING_TRANSCRIPT_NOT_FOUND // Transcript not found
	, WARNING_TRANSCRIPT_ID_DUPLICATE // Transcript ID already exists
	, WARNING_CANNOT_ADD_UTR // UTR cannot be added
	, WARNING_FRAMES_ZERO // Frames have zero values
	, WARNING_RARE_AA_POSSITION_NOT_FOUND // The genomic position for a rare amino acid was not found
	//
	// VCF
	//
	, WARNING_INVALID_INFO_KEY // INFO name has invalid characters
	, WARNING_INVALID_INFO_VALUE // INFO value has invalid characters

	//
	// Other
	//
	, ERROR_FILE_NOT_FOUND // File not found, or cannot read it
	, WARNING_FILE_NOT_FOUND // File not found, or cannot read it
	, WARNING_DUPLICATE_PRIMARY_KEY // A primary key is duplicated (e.g. a hash key)
	;

	public boolean isError() {
		return toString().startsWith("ERROR");
	}

	public boolean isWarning() {
		return toString().startsWith("WARNING");
	}
}