package org.snpeff.snpEffect;

import java.util.HashMap;
import java.util.Map;

import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.vcf.EffFormatVersion;

/**
 * Effect type:
 * Note that effects are sorted (declared) by impact (highest to lowest putative impact).
 * The idea is to be able to report only one effect per variant/transcript
 *
 * @author pcingola
 */
public enum EffectType {
	// High impact
	// Order: Highest impact first
	CHROMOSOME_LARGE_DELETION(EffectImpact.HIGH) //
	, CHROMOSOME_LARGE_INVERSION(EffectImpact.HIGH) //
	, CHROMOSOME_LARGE_DUPLICATION(EffectImpact.HIGH) //
	, GENE_REARRANGEMENT(EffectImpact.HIGH) //
	, GENE_DELETED(EffectImpact.HIGH) //
	, TRANSCRIPT_DELETED(EffectImpact.HIGH) //
	, EXON_DELETED(EffectImpact.HIGH) //
	, EXON_DELETED_PARTIAL(EffectImpact.HIGH) //
	, GENE_FUSION(EffectImpact.HIGH) //
	, GENE_FUSION_REVERESE(EffectImpact.HIGH) //
	, GENE_FUSION_HALF(EffectImpact.HIGH) //
	, FRAME_SHIFT(EffectImpact.HIGH) //
	, STOP_GAINED(EffectImpact.HIGH) //
	, STOP_LOST(EffectImpact.HIGH) //
	, START_LOST(EffectImpact.HIGH) //
	, SPLICE_SITE_ACCEPTOR(EffectImpact.HIGH) //
	, SPLICE_SITE_DONOR(EffectImpact.HIGH) //
	, RARE_AMINO_ACID(EffectImpact.HIGH) //
	, EXON_DUPLICATION(EffectImpact.HIGH) //
	, EXON_DUPLICATION_PARTIAL(EffectImpact.HIGH) //
	, EXON_INVERSION(EffectImpact.HIGH) //
	, EXON_INVERSION_PARTIAL(EffectImpact.HIGH) //
	, PROTEIN_PROTEIN_INTERACTION_LOCUS(EffectImpact.HIGH) //
	, PROTEIN_STRUCTURAL_INTERACTION_LOCUS(EffectImpact.HIGH) //

	// Moderate impact
	// Order: Highest impact first
	// Note: Method Codon.effect() relies on this order for effect
	//       replacement (when 'allowReplace = true')
	, NON_SYNONYMOUS_CODING(EffectImpact.MODERATE) //
	, GENE_DUPLICATION(EffectImpact.MODERATE) //
	, TRANSCRIPT_DUPLICATION(EffectImpact.MODERATE) //
	, UTR_5_DELETED(EffectImpact.MODERATE) //
	, UTR_3_DELETED(EffectImpact.MODERATE) //
	, SPLICE_SITE_BRANCH_U12(EffectImpact.MODERATE) //
	, GENE_INVERSION(EffectImpact.MODERATE) //
	, TRANSCRIPT_INVERSION(EffectImpact.MODERATE) //
	, CODON_INSERTION(EffectImpact.MODERATE) //
	, CODON_CHANGE_PLUS_CODON_INSERTION(EffectImpact.MODERATE) //
	, CODON_DELETION(EffectImpact.MODERATE) //
	, CODON_CHANGE_PLUS_CODON_DELETION(EffectImpact.MODERATE) //

	// Low impact
	// Order: Highest impact first
	, NON_SYNONYMOUS_STOP(EffectImpact.LOW) //
	, NON_SYNONYMOUS_START(EffectImpact.LOW) //
	, SPLICE_SITE_REGION(EffectImpact.LOW) //
	, SPLICE_SITE_BRANCH(EffectImpact.LOW) //
	, SYNONYMOUS_CODING(EffectImpact.LOW) //
	, SYNONYMOUS_START(EffectImpact.LOW) //
	, SYNONYMOUS_STOP(EffectImpact.LOW) //
	, CODON_CHANGE(EffectImpact.LOW) //
	, START_GAINED(EffectImpact.LOW) //
	, MOTIF(EffectImpact.LOW) //
	, MOTIF_DELETED(EffectImpact.LOW) //
	, FEATURE_FUSION(EffectImpact.LOW) //

	// Modifiers
	// Order: Highest impact first
	, FRAME_SHIFT_BEFORE_CDS_START(EffectImpact.MODIFIER) //
	, FRAME_SHIFT_AFTER_CDS_END(EffectImpact.MODIFIER) //
	, UTR_5_PRIME(EffectImpact.MODIFIER) //
	, UTR_3_PRIME(EffectImpact.MODIFIER) //
	, REGULATION(EffectImpact.MODIFIER) //
	, MICRO_RNA(EffectImpact.MODIFIER) //
	, UPSTREAM(EffectImpact.MODIFIER) //
	, DOWNSTREAM(EffectImpact.MODIFIER) //
	, NEXT_PROT(EffectImpact.MODIFIER) //
	, INTRON_CONSERVED(EffectImpact.MODIFIER) //
	, INTRON(EffectImpact.MODIFIER) //
	, INTRAGENIC(EffectImpact.MODIFIER) //
	, INTERGENIC_CONSERVED(EffectImpact.MODIFIER) //
	, INTERGENIC(EffectImpact.MODIFIER) //
	, CDS(EffectImpact.MODIFIER) //
	, EXON(EffectImpact.MODIFIER) //
	, TRANSCRIPT(EffectImpact.MODIFIER) //
	, GENE(EffectImpact.MODIFIER) //
	, SEQUENCE(EffectImpact.MODIFIER) //
	, CHROMOSOME_ELONGATION(EffectImpact.MODIFIER) //
	, CUSTOM(EffectImpact.MODIFIER) //
	, CHROMOSOME(EffectImpact.MODIFIER) //
	, GENOME(EffectImpact.MODIFIER) //
	, NONE(EffectImpact.MODIFIER) //
	;

	static Map<String, EffectType> so2efftype = new HashMap<>();

	private final EffectImpact effectImpact;

	/**
	 * Parse a string to an EffectType
	 */
	public static EffectType parse(EffFormatVersion formatVersion, String str) {
		try {
			return EffectType.valueOf(str);
		} catch (Exception e) {
			// OK, the value does not exits. Try Sequence Ontology
		}

		// Populate S.O. terms map
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

		//---
		// Add old terms for backwards compatibility
		//---
		Map<String, EffectType> oldSo2efftype = new HashMap<>();
		oldSo2efftype.put("non_coding_exon_variant", EffectType.EXON);
		oldSo2efftype.put("inframe_insertion", EffectType.CODON_INSERTION);
		oldSo2efftype.put("inframe_deletion", EffectType.CODON_DELETION);
		oldSo2efftype.put("transcript", EffectType.TRANSCRIPT);
		oldSo2efftype.put("non_canonical_start_codon", EffectType.SYNONYMOUS_START);

		// Add terms if not already in the map
		for (String so : oldSo2efftype.keySet()) {
			if (!so2efftype.containsKey(so)) so2efftype.put(so, oldSo2efftype.get(so));
		}

	}

	private EffectType(EffectImpact effectImpact) {
		this.effectImpact = effectImpact;
	}

	/**
	 * Return effect impact
	 */
	public EffectImpact effectImpact() {
		return effectImpact;
	}

	public EffectType getGeneRegion() {
		switch (this) {
		case NONE:
		case CHROMOSOME:
		case CHROMOSOME_LARGE_DELETION:
		case CHROMOSOME_LARGE_DUPLICATION:
		case CHROMOSOME_LARGE_INVERSION:
		case CHROMOSOME_ELONGATION:
		case CUSTOM:
		case SEQUENCE:
			return EffectType.CHROMOSOME;

		case INTERGENIC:
		case INTERGENIC_CONSERVED:
		case FEATURE_FUSION:
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
		case TRANSCRIPT_DUPLICATION:
		case TRANSCRIPT_INVERSION:
		case INTRAGENIC:
		case NEXT_PROT:
		case TRANSCRIPT:
		case CDS:
			return EffectType.TRANSCRIPT;

		case GENE:
		case GENE_DELETED:
		case GENE_DUPLICATION:
		case GENE_FUSION:
		case GENE_FUSION_HALF:
		case GENE_FUSION_REVERESE:
		case GENE_INVERSION:
		case GENE_REARRANGEMENT:
			return EffectType.GENE;

		case EXON:
		case EXON_DELETED:
		case EXON_DELETED_PARTIAL:
		case EXON_DUPLICATION:
		case EXON_DUPLICATION_PARTIAL:
		case EXON_INVERSION:
		case EXON_INVERSION_PARTIAL:
		case NON_SYNONYMOUS_START:
		case NON_SYNONYMOUS_CODING:
		case SYNONYMOUS_CODING:
		case SYNONYMOUS_START:
		case FRAME_SHIFT:
		case FRAME_SHIFT_AFTER_CDS_END:
		case FRAME_SHIFT_BEFORE_CDS_START:
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
		case PROTEIN_PROTEIN_INTERACTION_LOCUS:
		case PROTEIN_STRUCTURAL_INTERACTION_LOCUS:
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

	public boolean isFusion() {
		return this == GENE_FUSION //
				|| this == GENE_FUSION_REVERESE //
				|| this == GENE_FUSION_HALF //
				|| this == FEATURE_FUSION //
		;
	}

	public String toSequenceOntology(EffFormatVersion formatVersion, Variant variant) {
		switch (this) {

		case CDS:
			return "coding_sequence_variant";

		case CHROMOSOME_LARGE_DELETION:
			return "chromosome_number_variation";

		case CHROMOSOME_LARGE_DUPLICATION:
			return "duplication";

		case CHROMOSOME_LARGE_INVERSION:
			return "inversion";

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
			return "conservative_inframe_deletion";

		case CODON_INSERTION:
			return "conservative_inframe_insertion";

		case DOWNSTREAM:
			return "downstream_gene_variant";

		case EXON:
			if (variant != null && (!variant.isVariant() || variant.isInterval())) return "exon_region";
			return "non_coding_transcript_exon_variant";

		case EXON_DELETED:
			return "exon_loss_variant";

		case EXON_DELETED_PARTIAL:
			return "exon_loss_variant";

		case EXON_DUPLICATION:
			return "duplication";

		case EXON_DUPLICATION_PARTIAL:
			return "duplication";

		case EXON_INVERSION:
			return "inversion";

		case EXON_INVERSION_PARTIAL:
			return "inversion";

		case FEATURE_FUSION:
			return "feature_fusion";

		case FRAME_SHIFT:
			return "frameshift_variant";

		case FRAME_SHIFT_BEFORE_CDS_START:
			return "start_retained_variant";

		case FRAME_SHIFT_AFTER_CDS_END:
			return "stop_retained_variant";

		case GENE:
			return "gene_variant";

		case GENE_INVERSION:
			return "inversion";

		case GENE_DELETED:
			return "feature_ablation";

		case GENE_DUPLICATION:
			return "duplication";

		case GENE_FUSION:
			return "gene_fusion";

		case GENE_FUSION_HALF:
			return "transcript_ablation";

		case GENE_FUSION_REVERESE:
			return "bidirectional_gene_fusion";

		case GENE_REARRANGEMENT:
			return "rearranged_at_DNA_level";

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

		case PROTEIN_PROTEIN_INTERACTION_LOCUS:
			return "protein_protein_contact";

		case PROTEIN_STRUCTURAL_INTERACTION_LOCUS:
			return "structural_interaction_variant";

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
			return "start_retained_variant";

		case TRANSCRIPT:
			return "non_coding_transcript_variant";

		case TRANSCRIPT_DELETED:
			return "transcript_ablation";

		case TRANSCRIPT_DUPLICATION:
			return "duplication";

		case TRANSCRIPT_INVERSION:
			return "inversion";

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
