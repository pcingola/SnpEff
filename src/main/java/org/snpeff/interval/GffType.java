package org.snpeff.interval;

public enum GffType {

	GENE //
	, TRANSCRIPT //
	, EXON //
	, CDS //
	, START_CODON //
	, STOP_CODON //
	, UTR5 //
	, UTR3 //
	, INTRON_CONSERVED //
	, INTERGENIC_CONSERVED //
	, UNKNOWN //
	;

	public static GffType parse(String str) {
		switch (str.toLowerCase()) {
		case "gene":
		case "protein":
			return GENE;

		case "pseudogene":
		case "transcript":
		case "mrna":
		case "trna":
		case "snorna":
		case "rrna":
		case "ncrna":
		case "mirna":
		case "snrna":
		case "pseudogenic_transcript":
			return TRANSCRIPT;

		case "exon":
		case "pseudogenic_exon":
			return EXON;

		case "cds":
			return CDS;

		case "start_codon":
			return START_CODON;

		case "stop_codon":
			return STOP_CODON;

		case "five_prime_utr":
		case "5'-utr":
		case "5'utr":
		case "5utr":
			return UTR5;

		case "three_prime_utr":
		case "3'-utr":
		case "3'utr":
		case "3utr":
			return UTR3;

		case "intron_CNS":
		case "intron_cns":
			return INTRON_CONSERVED;

		case "inter_cns":
			return INTERGENIC_CONSERVED;

		default:
			return UNKNOWN;
		}
	}
}
