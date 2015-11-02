package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.HashMap;

import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectImpact;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;

/**
 * Effect type:
 * Note that effects are sorted (declared) by impact (highest to lowest putative impact).
 * The idea is to be able to report only one effect per variant/transcript
 *
 * @author pcingola
 */
public enum EffectType {
	// High impact
	CHROMOSOME_LARGE_DELETION //
	, TRANSCRIPT_DELETED //
	, EXON_DELETED //
	, FRAME_SHIFT //
	, STOP_GAINED //
	, STOP_LOST //
	, START_LOST //
	, SPLICE_SITE_ACCEPTOR //
	, SPLICE_SITE_DONOR //
	, RARE_AMINO_ACID //

	// Moderate impact
	, NON_SYNONYMOUS_CODING //
	, CODON_INSERTION //
	, CODON_CHANGE_PLUS_CODON_INSERTION //
	, CODON_DELETION //
	, CODON_CHANGE_PLUS_CODON_DELETION //
	, UTR_5_DELETED //
	, UTR_3_DELETED //
	, SPLICE_SITE_BRANCH_U12 //
	, SPLICE_SITE_REGION //
	, SPLICE_SITE_BRANCH //
	, NON_SYNONYMOUS_STOP //
	, NON_SYNONYMOUS_START //
	, SYNONYMOUS_CODING //
	, SYNONYMOUS_START //
	, SYNONYMOUS_STOP //
	, CODON_CHANGE //

	// Low impact
	, UTR_5_PRIME //
	, UTR_3_PRIME //
	, START_GAINED //
	, UPSTREAM //
	, DOWNSTREAM //
	, MOTIF //
	, MOTIF_DELETED //
	, REGULATION //
	, MICRO_RNA //

	// Modifiers
	, CUSTOM //
	, NEXT_PROT //
	, INTRON_CONSERVED //
	, INTRON //
	, INTRAGENIC //
	, INTERGENIC_CONSERVED //
	, INTERGENIC //
	, CDS //
	, EXON //
	, TRANSCRIPT //
	, GENE //
	, SEQUENCE //
	, CHROMOSOME_ELONGATION //
	, CHROMOSOME //
	, GENOME //
	, NONE //
	;

	static HashMap<String, EffectType> so2efftype = new HashMap<String, EffectType>();

	/**
	 * Parse a string to an EffectType
	 */
	public static EffectType parse(EffFormatVersion formatVersion, String str) {
		try {
			return EffectType.valueOf(str);
		} catch (Exception e) {
			// OK, the value does not exits. Try Sequence ontology
		}

		// Try an SO term
		if (so2efftype.isEmpty()) {
			// In some cases a 'non-variant' has different effect (e.g. 'exon_region'), so we need to call this twice
			so2efftype(formatVersion, null);
			so2efftype(formatVersion, Variant.NO_VARIANT);
		}

		// Look up S.O. term
		if (so2efftype.containsKey(str)) return so2efftype.get(str);

		throw new RuntimeException("Cannot parse EffectType '" + str + "'");
	}

	/**
	 * Create a map between SO terms and EffectType
	 */
	static void so2efftype(EffFormatVersion formatVersion, Variant variant) {
		for (EffectType efftype : EffectType.values()) {
			String so = efftype.toSequenceOntology(formatVersion, variant);

			for (String soSingle : so.split(formatVersion.separatorSplit()))
				if (!so2efftype.containsKey(soSingle)) so2efftype.put(soSingle, efftype);
		}
	}

	/**
	 * Return effect impact
	 */
	public EffectImpact effectImpact() {
		switch (this) {
		case TRANSCRIPT_DELETED:
		case EXON_DELETED:
		case FRAME_SHIFT:
		case SPLICE_SITE_ACCEPTOR:
		case SPLICE_SITE_DONOR:
		case START_LOST:
		case STOP_GAINED:
		case STOP_LOST:
		case RARE_AMINO_ACID:
		case CHROMOSOME_LARGE_DELETION:
			return EffectImpact.HIGH;

		case CODON_CHANGE_PLUS_CODON_DELETION:
		case CODON_CHANGE_PLUS_CODON_INSERTION:
		case CODON_DELETION:
		case CODON_INSERTION:
		case NON_SYNONYMOUS_CODING:
		case SPLICE_SITE_BRANCH_U12:
		case UTR_3_DELETED:
		case UTR_5_DELETED:
			return EffectImpact.MODERATE;

		case SPLICE_SITE_REGION:
		case SPLICE_SITE_BRANCH:
		case NON_SYNONYMOUS_START:
		case NON_SYNONYMOUS_STOP:
		case START_GAINED:
		case SYNONYMOUS_CODING:
		case SYNONYMOUS_START:
		case SYNONYMOUS_STOP:
		case CODON_CHANGE:
			return EffectImpact.LOW;

		case CDS:
		case CHROMOSOME:
		case CHROMOSOME_ELONGATION:
		case CUSTOM:
		case DOWNSTREAM:
		case EXON:
		case GENE:
		case GENOME:
		case INTRAGENIC:
		case INTERGENIC:
		case INTERGENIC_CONSERVED:
		case INTRON:
		case INTRON_CONSERVED:
		case MICRO_RNA:
		case NONE:
		case REGULATION:
		case SEQUENCE:
		case TRANSCRIPT:
		case UPSTREAM:
		case UTR_3_PRIME:
		case UTR_5_PRIME:
			return EffectImpact.MODIFIER;

		case MOTIF:
		case MOTIF_DELETED:
			return EffectImpact.LOW;

		case NEXT_PROT:
			return EffectImpact.MODIFIER;

		default:
			throw new RuntimeException("Unknown impact for effect type: '" + this + "'");
		}
	}

	public EffectType getGeneRegion() {
		switch (this) {
		case NONE:
		case CHROMOSOME:
		case CHROMOSOME_LARGE_DELETION:
		case CHROMOSOME_ELONGATION:
		case CUSTOM:
		case CDS:
		case SEQUENCE:
			return EffectType.NONE;

		case INTERGENIC:
		case INTERGENIC_CONSERVED:
			return EffectType.INTERGENIC;

		case UPSTREAM:
			return EffectType.UPSTREAM;

		case UTR_5_PRIME:
		case UTR_5_DELETED:
		case START_GAINED:
			return EffectType.UTR_5_PRIME;

		case SPLICE_SITE_ACCEPTOR:
			return EffectType.SPLICE_SITE_ACCEPTOR;

		case SPLICE_SITE_BRANCH_U12:
		case SPLICE_SITE_BRANCH:
			return EffectType.SPLICE_SITE_BRANCH;

		case SPLICE_SITE_DONOR:
			return EffectType.SPLICE_SITE_DONOR;

		case SPLICE_SITE_REGION:
			return EffectType.SPLICE_SITE_REGION;

		case TRANSCRIPT_DELETED:
		case INTRAGENIC:
		case GENE:
		case NEXT_PROT:
		case TRANSCRIPT:
			return EffectType.TRANSCRIPT;

		case EXON:
		case EXON_DELETED:
		case NON_SYNONYMOUS_START:
		case NON_SYNONYMOUS_CODING:
		case SYNONYMOUS_CODING:
		case SYNONYMOUS_START:
		case FRAME_SHIFT:
		case CODON_CHANGE:
		case CODON_INSERTION:
		case CODON_CHANGE_PLUS_CODON_INSERTION:
		case CODON_DELETION:
		case CODON_CHANGE_PLUS_CODON_DELETION:
		case START_LOST:
		case STOP_GAINED:
		case SYNONYMOUS_STOP:
		case NON_SYNONYMOUS_STOP:
		case STOP_LOST:
		case RARE_AMINO_ACID:
			return EffectType.EXON;

		case INTRON:
		case INTRON_CONSERVED:
			return EffectType.INTRON;

		case UTR_3_PRIME:
		case UTR_3_DELETED:
			return EffectType.UTR_3_PRIME;

		case DOWNSTREAM:
			return EffectType.DOWNSTREAM;

		case REGULATION:
			return EffectType.REGULATION;

		case MOTIF:
		case MOTIF_DELETED:
			return EffectType.MOTIF;

		case MICRO_RNA:
			return EffectType.MICRO_RNA;

		case GENOME:
			return EffectType.GENOME;

		default:
			throw new RuntimeException("Unknown gene region for effect type: '" + this + "'");
		}
	}

	public String toSequenceOntology(EffFormatVersion formatVersion, Variant variant) {
		switch (this) {

		case CDS:
			return "coding_sequence_variant";

		case CHROMOSOME_LARGE_DELETION:
			return "chromosome_number_variation";

		case CHROMOSOME:
			return "chromosome";

		case CHROMOSOME_ELONGATION:
			return "feature_elongation";

		case CODON_CHANGE:
			return "coding_sequence_variant";

		case CODON_CHANGE_PLUS_CODON_INSERTION:
			return "disruptive_inframe_insertion";

		case CODON_CHANGE_PLUS_CODON_DELETION:
			return "disruptive_inframe_deletion";

		case CODON_DELETION:
			return "inframe_deletion";

		case CODON_INSERTION:
			return "inframe_insertion";

		case DOWNSTREAM:
			return "downstream_gene_variant";

		case EXON:
			if (variant != null && (!variant.isVariant() || variant.isInterval())) return "exon_region";
			return "non_coding_exon_variant";

		case EXON_DELETED:
			return "exon_loss_variant";

		case FRAME_SHIFT:
			return "frameshift_variant";

		case GENE:
			return "gene_variant";

		case INTERGENIC:
			return "intergenic_region";

		case INTERGENIC_CONSERVED:
			return "conserved_intergenic_variant";

		case INTRON:
			return "intron_variant";

		case INTRON_CONSERVED:
			return "conserved_intron_variant";

		case INTRAGENIC:
			return "intragenic_variant";

		case MICRO_RNA:
			return "miRNA";

		case MOTIF:
			return "TF_binding_site_variant";

		case MOTIF_DELETED:
			return "TFBS_ablation";

		case NEXT_PROT:
			return "sequence_feature";

		case NON_SYNONYMOUS_CODING:
			return "missense_variant";

		case NON_SYNONYMOUS_START:
			return "initiator_codon_variant";

		case NON_SYNONYMOUS_STOP:
			return "stop_retained_variant";

		case RARE_AMINO_ACID:
			return "rare_amino_acid_variant";

		case REGULATION:
			return "regulatory_region_variant";

		case SPLICE_SITE_ACCEPTOR:
			return "splice_acceptor_variant";

		case SPLICE_SITE_DONOR:
			return "splice_donor_variant";

		case SPLICE_SITE_REGION:
			return "splice_region_variant";

		case SPLICE_SITE_BRANCH:
			return "splice_branch_variant";

		case SPLICE_SITE_BRANCH_U12:
			return "splice_branch_variant";

		case START_LOST:
			return "start_lost";

		case START_GAINED:
			return "5_prime_UTR_premature_start_codon_gain_variant";

		case STOP_GAINED:
			return "stop_gained";

		case STOP_LOST:
			return "stop_lost";

		case SYNONYMOUS_CODING:
			return "synonymous_variant";

		case SYNONYMOUS_STOP:
			return "stop_retained_variant";

		case SYNONYMOUS_START:
			return "initiator_codon_variant" + formatVersion.separator() + "non_canonical_start_codon";

		case TRANSCRIPT:
			//	return "transcript";
			return "non_coding_transcript_variant";

		case TRANSCRIPT_DELETED:
			return "transcript_ablation";

		case UPSTREAM:
			return "upstream_gene_variant";

		case UTR_3_PRIME:
			return "3_prime_UTR_variant";

		case UTR_3_DELETED:
			return "3_prime_UTR_truncation" + formatVersion.separator() + "exon_loss_variant";

		case UTR_5_PRIME:
			return "5_prime_UTR_variant";

		case UTR_5_DELETED:
			return "5_prime_UTR_truncation" + formatVersion.separator() + "exon_loss_variant";

		case CUSTOM:
			return "custom";

		case NONE:
		case GENOME:
		case SEQUENCE:
			return "";

		default:
			throw new RuntimeException("Unknown SO term for " + this.toString());
		}
	}
}
