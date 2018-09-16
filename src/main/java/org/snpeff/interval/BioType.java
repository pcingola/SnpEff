package org.snpeff.interval;

/**
 * BioTypes: Gene or transcript bioType annotation
 *
 * References: http://www.ensembl.org/info/genome/genebuild/biotypes.html
 *
 * Biotypes classifies genes and transcripts into groups including: protein coding, pseudogene
 * , processed pseudogene, miRNA, rRNA, scRNA, snoRNA, snRNA. The biotypes can be grouped into
 * protein coding, pseudogene, long noncoding and short noncoding. Examples of biotypes in each
 * group are as follows:
 * 		Protein coding: IGC gene, IGD gene, IG gene, IGJ gene, IGLV gene, IGM gene, IGV gene
 * 						, IGZ gene, nonsense mediated decay, nontranslating CDS, non stop decay
 * 						, polymorphic pseudogene, TRC gene, TRD gene, TRJ gene.
 *
 * 		Pseudogene: disrupted domain, IGC pseudogene, IGJ pseudogene, IG pseudogene, IGV pseudogene
 * 						, processed pseudogene, transcribed processed pseudogene, transcribed unitary pseudogene
 * 						, transcribed unprocessed pseudogene, translated processed pseudogene
 * 						, TRJ pseudogene, unprocessed pseudogene
 *
 * 		Long noncoding: 3prime overlapping ncrna, ambiguous orf, antisense, antisense RNA, lincRNA
 * 						, ncrna host, processed transcript, sense intronic, sense overlapping
 * 		Short noncoding: miRNA, miRNA_pseudogene, miscRNA, miscRNA pseudogene, Mt rRNA, Mt tRNA, rRNA
 * 						, scRNA, snlRNA, snoRNA, snRNA, tRNA, tRNA_pseudogene
 *
 * @author pcingola
 *
 */
public enum BioType {
	prime3_overlapping_ncrna //  WARNING: This one is actually called '3prime_overlapping_ncrna' but identifiers cannot start with a number in Java
	, antisense //
	, IG_C_gene //
	, IG_C_pseudogene //
	, IG_D_gene //
	, IG_J_gene //
	, IG_J_pseudogene //
	, IG_V_gene //
	, IG_V_pseudogene //
	, lincRNA //
	, macro_lncRNA //
	, miRNA //
	, misc_RNA //
	, Mt_rRNA //
	, Mt_tRNA //
	, non_coding //
	, nonsense_mediated_decay //
	, non_stop_decay //
	, polymorphic_pseudogene //
	, processed_pseudogene //
	, processed_transcript //
	, protein_coding //
	, pseudogene //
	, retained_intron //
	, ribozyme //
	, rRNA //
	, scaRNA //
	, scRNA //
	, sense_intronic //
	, sense_overlapping //
	, snoRNA //
	, snRNA //
	, sRNA //
	, TEC //
	, transcribed_processed_pseudogene //
	, transcribed_unitary_pseudogene //
	, transcribed_unprocessed_pseudogene //
	, translated_unprocessed_pseudogene //
	, TR_C_gene //
	, TR_D_gene //
	, TR_J_gene //
	, TR_J_pseudogene //
	, TR_V_gene //
	, TR_V_pseudogene //
	, unitary_pseudogene //
	, unprocessed_pseudogene //
	, vaultRNA //
	;

	/**
	 * Basic bioTypes for coding / non-coding genes
	 */
	public static BioType coding(boolean isCoding) {
		return isCoding ? BioType.protein_coding : pseudogene;
	}

	/**
	 * Parse a BioType
	 */
	public static BioType parse(String str) {
		try {
			if (str == null) return null;

			return BioType.valueOf(str);
		} catch (Exception e) {
			if (str != null) {
				switch (str.toLowerCase()) {
				case "mrna":
				case "protein":
				case "cds":
				case "trna":
				case "start_codon":
				case "stop_codon":
				case "five_prime_utr":
				case "5'-utr":
				case "5'utr":
				case "5utr":
				case "three_prime_utr":
				case "3'-utr":
				case "3'utr":
				case "3utr":
					return protein_coding;

				case "pseudogenic_transcript":
				case "pseudogenic_exon":
					return transcribed_processed_pseudogene;

				case "ncrna":
					return lincRNA;

				case "rrna":
					return rRNA;

				case "mirna":
					return miRNA;

				case "snrna":
					return snRNA;

				case "snorna":
					return snoRNA;

				case "3prime_overlapping_ncrna":
					//  WARNING: This one is actually called '3prime_overlapping_ncrna' but identifiers cannot start with a number in Java
					return prime3_overlapping_ncrna;

				default:
					return null;
				}
			}

			return null;
		}
	}

	public boolean isProteinCoding() {
		return this == protein_coding //
				|| this == IG_C_gene //
				|| this == IG_D_gene //
				|| this == IG_J_gene //
				|| this == IG_V_gene //
				|| this == TR_C_gene //
				|| this == TR_D_gene //
				|| this == TR_J_gene //
				|| this == TR_V_gene //
		;
	}
}
