package ca.mcgill.mcb.pcingola.snpEffect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.codons.CodonTable;
import ca.mcgill.mcb.pcingola.interval.Custom;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;

/**
 * Effect of a variant.
 *
 * @author pcingola
 */
public class VariantEffect implements Cloneable, Comparable<VariantEffect> {

	public enum Coding {
		CODING, NON_CODING
	}

	public enum EffectImpact {
		HIGH, MODERATE, LOW, MODIFIER
	}

	public enum EffectType {
		NONE //
		, CHROMOSOME //
		, CHROMOSOME_LARGE_DELETION //
		, INTERGENIC //
		, UPSTREAM //
		, UTR_5_PRIME //
		, UTR_5_DELETED //
		, START_GAINED //
		, SPLICE_SITE_ACCEPTOR //
		, SPLICE_SITE_BRANCH //
		, SPLICE_SITE_BRANCH_U12 //
		, SPLICE_SITE_DONOR //
		, SPLICE_SITE_REGION //
		, START_LOST //
		, SYNONYMOUS_START //
		, NON_SYNONYMOUS_START //
		, CDS //
		, GENE //
		, GENOME //
		, TRANSCRIPT //
		, EXON //
		, EXON_DELETED //
		, NON_SYNONYMOUS_CODING //
		, SYNONYMOUS_CODING //
		, FRAME_SHIFT //
		, CODON_CHANGE //
		, CODON_INSERTION //
		, CODON_CHANGE_PLUS_CODON_INSERTION //
		, CODON_DELETION //
		, CODON_CHANGE_PLUS_CODON_DELETION //
		, RARE_AMINO_ACID //
		, STOP_GAINED //
		, SYNONYMOUS_STOP //
		, NON_SYNONYMOUS_STOP //
		, STOP_LOST //
		, INTRON //
		, UTR_3_PRIME //
		, UTR_3_DELETED //
		, DOWNSTREAM //
		, INTRON_CONSERVED //
		, INTERGENIC_CONSERVED //
		, INTRAGENIC //
		, REGULATION //
		, MOTIF //
		, MICRO_RNA //
		, CUSTOM //
		, NEXT_PROT //
		;

		static HashMap<String, EffectType> so2efftype = new HashMap<String, VariantEffect.EffectType>();

		/**
		 * Parse a string to an EffectType
		 */
		public static EffectType parse(String str) {
			try {
				return EffectType.valueOf(str);
			} catch (Exception e) {
				// OK, the value does not exits.
			}

			// Try an SO term
			if (so2efftype.isEmpty()) so2efftype();
			if (so2efftype.containsKey(str)) return so2efftype.get(str);

			throw new RuntimeException("Cannot parse EffectType '" + str + "'");
		}

		/**
		 * Create a map between SO terms and EffectType
		 */
		static void so2efftype() {
			for (EffectType efftype : EffectType.values()) {
				String so = efftype.toSequenceOntology();
				so2efftype.put(so, efftype);
			}
		}

		public String toSequenceOntology() {
			switch (this) {

			case CDS:
				return "coding_sequence_variant";

			case CHROMOSOME_LARGE_DELETION:
			case CHROMOSOME:
				return "chromosome";

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
				return "exon_variant";

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
				return "splice_region_variant";

			case SPLICE_SITE_BRANCH_U12:
				return "splice_region_variant";

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
				return "initiator_codon_variant+non_canonical_start_codon";

			case TRANSCRIPT:
				return "transcript_variant";

			case UPSTREAM:
				return "upstream_gene_variant";

			case UTR_3_PRIME:
				return "3_prime_UTR_variant";

			case UTR_3_DELETED:
				return "3_prime_UTR_truncation+exon_loss";

			case UTR_5_PRIME:
				return "5_prime_UTR_variant";

			case UTR_5_DELETED:
				return "5_prime_UTR_truncation+exon_loss_variant";

			case NONE:
			case GENOME:
			case CUSTOM:
			default:
				return this.toString().toLowerCase(); // Just a wild guess ... this should probably throw an Exception
			}
		}
	};

	/**
	 * Errors for change effect
	 *
	 */
	public enum ErrorWarningType {
		WARNING_SEQUENCE_NOT_AVAILABLE //
		, WARNING_REF_DOES_NOT_MATCH_GENOME //
		, WARNING_TRANSCRIPT_INCOMPLETE //
		, WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS //
		, WARNING_TRANSCRIPT_NO_START_CODON //
		, ERROR_CHROMOSOME_NOT_FOUND //
		, ERROR_OUT_OF_CHROMOSOME_RANGE //
		, ERROR_OUT_OF_EXON //
		, ERROR_MISSING_CDS_SEQUENCE //
		;

		public boolean isError() {
			return toString().startsWith("ERROR");
		}

		public boolean isWarning() {
			return toString().startsWith("WARNING");
		}
	}

	/**
	 * This class is only getFused for SNPs
	 */
	public enum FunctionalClass {
		NONE, SILENT, MISSENSE, NONSENSE
	}

	static final boolean COMPATIBLE_v1_8 = true; // Activate this in order to get the same out as version 1.8. This is only for testing & debugging

	Variant variant = null;
	Variant variantRef = null;
	// EffectType effectType = EffectType.NONE;
	List<EffectType> effectTypes;
	EffectImpact effectImpact = null;
	Marker marker = null;
	String error = "", warning = "", message = ""; // Any message, warning or error?
	String codonsOld = "", codonsNew = ""; // Codon change information
	String codonsAroundOld = "", codonsAroundNew = ""; // Codons around
	int distance = -1; // Distance metric
	int codonNum = -1; // Codon number (negative number mens 'information not available')
	int codonIndex = -1; // Index within a codon (negative number mens 'information not available')
	int codonDegeneracy = -1; // Codon degeneracy (negative number mens 'information not available')
	String aaOld = "", aaNew = ""; // Amino acid changes
	String aasAroundOld = "", aasAroundNew = ""; // Amino acids around

	public VariantEffect(Variant variant) {
		this.variant = variant;
	}

	public VariantEffect(Variant variant, Variant variantRef) {
		this.variant = variant;
		this.variantRef = variantRef;
	}

	public void addEffectType(EffectType effectType) {
		effectTypes.add(effectType);
	}

	/**
	 * Add an error or warning
	 */
	public void addErrorWarning(ErrorWarningType errwarn) {
		if (errwarn == null) return;

		if (errwarn.isError()) {
			if (error.indexOf(errwarn.toString()) < 0) error += (error.isEmpty() ? "" : "+") + errwarn;
		} else {
			if (warning.indexOf(errwarn.toString()) < 0) warning += (warning.isEmpty() ? "" : "+") + errwarn;
		}
	}

	@Override
	public VariantEffect clone() {
		try {
			return (VariantEffect) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a string for codon effect
	 * @param showAaChange : If true, include codon change, biotype, etc.
	 * @return
	 */
	String codonEffect(boolean showAaChange, boolean showBioType, boolean useSeqOntology) {
		String codonEffect = "";
		if ((marker == null) || (codonNum < 0)) return codonEffect;

		// Add codon effect
		codonEffect += getEffectTypeString(useSeqOntology);

		// Append codon change
		if (showAaChange) codonEffect += "(" + getAaChange() + ")";

		return codonEffect;
	}

	@Override
	public int compareTo(VariantEffect variantEffect) {
		// Sort by impact
		int comp = getEffectImpact().compareTo(variantEffect.getEffectImpact());
		if (comp != 0) return comp;

		// Sort by effect
		comp = getEffectType().compareTo(variantEffect.getEffectType());
		if (comp != 0) return comp;

		// Sort by genomic coordinate of affected 'marker'
		if ((getMarker() != null) && (variantEffect.getMarker() != null)) return getMarker().compareTo(variantEffect.getMarker());

		// Sort by variant (most of the time this is equal)
		return variant.compareTo(variantEffect.getVariant());
	}

	/**
	 * Show a string with overall effect
	 */
	public String effect(boolean shortFormat, boolean showAaChange, boolean showBioType, boolean useSeqOntology) {
		String e = "";
		String codonEffect = codonEffect(showAaChange, showBioType, useSeqOntology); // Codon effect

		// Create effect string
		if (!codonEffect.isEmpty()) e = codonEffect;
		else if (isRegulation()) return getEffectTypeString(useSeqOntology) + "[" + ((Regulation) marker).getName() + "]";
		else if (isNextProt()) return getEffectTypeString(useSeqOntology) + "[" + VcfEffect.vcfEffSafe(((NextProt) marker).getId()) + "]"; // Make sure this 'id' is not dangerous in a VCF 'EFF' field
		else if (isMotif()) return getEffectTypeString(useSeqOntology) + "[" + ((Motif) marker).getPwmId() + ":" + ((Motif) marker).getPwmName() + "]";
		else if (isCustom()) {
			// Custom interval
			String label = ((Custom) marker).getLabel();
			double score = ((Custom) marker).getScore();
			if (Double.isNaN(score)) label = label + ":" + score;
			if (!label.isEmpty()) label = "[" + label + "]";
			return getEffectTypeString(useSeqOntology) + label;
		} else if (isIntergenic() || isIntron() || isSpliceSite()) e = getEffectTypeString(useSeqOntology);
		else if (!message.isEmpty()) e = getEffectTypeString(useSeqOntology) + ": " + message;
		else if (marker == null) e = getEffectTypeString(useSeqOntology); // There are cases when no marker is associated (e.g. "Out of chromosome", "No such chromosome", etc.)
		else e = getEffectTypeString(useSeqOntology) + ": " + marker.getId();

		if (shortFormat) e = e.split(":")[0];

		return e;
	}

	/**
	 * Amino acid change string
	 */
	public String getAaChange() {
		if (aaOld.isEmpty() && aaNew.isEmpty()) return "";
		if (aaOld.equals(aaNew)) return aaNew;
		return (aaOld.isEmpty() ? "-" : aaOld) + "/" + (aaNew.isEmpty() ? "-" : aaNew);
	}

	/**
	 * Amino acid change string (HGVS style)
	 */
	public String getAaChangeHgsv() {
		if (aaOld.isEmpty() && aaNew.isEmpty()) {
			if (codonNum >= 0) return "" + (codonNum + 1);
			return "";
		}

		if (aaOld.equals(aaNew)) return aaNew + (codonNum + 1);
		return aaOld + (codonNum + 1) + aaNew;
	}

	/**
	 * Amino acid length (negative if there is none)
	 * @return Amino acid length (CDS length / 3 ) or '-1' if there is no CDS length
	 */
	public int getAaLength() {
		int cdsLen = getCdsLength();
		if (cdsLen < 0) return -1;

		int lenNoStop = Math.max(0, cdsLen - 3); // Do not include the STOP codon
		return lenNoStop / 3;
	}

	public String getAaNew() {
		return aaNew;
	}

	public String getAaOld() {
		return aaOld;
	}

	/**
	 * Get biotype
	 * @return
	 */
	public String getBiotype() {
		Gene gene = getGene();
		if (gene == null) return "";

		Transcript tr = getTranscript();
		if (tr != null) return tr.getBioType();
		else if (gene.getGenome().hasCodingInfo()) return (gene.isProteinCoding() ? "coding" : "non-coding");

		return "";
	}

	/**
	 * CDS length (negative if there is none)
	 * @return
	 */
	public int getCdsLength() {
		// CDS size info
		Transcript tr = getTranscript();
		if ((tr != null) && tr.isProteinCoding()) return tr.cds().length();
		return -1;
	}

	/**
	 * Codon change string
	 * @return
	 */
	public String getCodonChange() {
		if (codonsOld.isEmpty() && codonsNew.isEmpty()) return "";
		// if (codonsOld.equals(codonsNew)) return codonsNew;
		return codonsOld + "/" + codonsNew;
	}

	public int getCodonIndex() {
		return codonIndex;
	}

	public int getCodonNum() {
		return codonNum;
	}

	public String getCodonsNew() {
		return codonsNew;
	}

	public String getCodonsOld() {
		return codonsOld;
	}

	public int getDistance() {
		return distance;
	}

	/**
	 * Return impact of this effect
	 * @return
	 */
	public EffectImpact getEffectImpact() {

		if (effectImpact == null) {
			if ((variant != null) && (!variant.isVariant())) {
				// Not a change? => Modifier
				effectImpact = EffectImpact.MODIFIER;
			} else {
				// TODO: Refactor.This code should be in each marker class, not here...
				switch (getEffectType()) {
				case EXON_DELETED:
				case FRAME_SHIFT:
				case SPLICE_SITE_ACCEPTOR:
				case SPLICE_SITE_DONOR:
				case START_LOST:
				case STOP_GAINED:
				case STOP_LOST:
				case RARE_AMINO_ACID:
				case CHROMOSOME_LARGE_DELETION:
					effectImpact = EffectImpact.HIGH;
					break;

				case CODON_CHANGE:
				case CODON_CHANGE_PLUS_CODON_DELETION:
				case CODON_CHANGE_PLUS_CODON_INSERTION:
				case CODON_DELETION:
				case CODON_INSERTION:
				case NON_SYNONYMOUS_CODING:
				case SPLICE_SITE_BRANCH_U12:
				case UTR_3_DELETED:
				case UTR_5_DELETED:
					effectImpact = EffectImpact.MODERATE;
					break;

				case SPLICE_SITE_REGION:
				case SPLICE_SITE_BRANCH:
				case NON_SYNONYMOUS_START:
				case NON_SYNONYMOUS_STOP:
				case START_GAINED:
				case SYNONYMOUS_CODING:
				case SYNONYMOUS_START:
				case SYNONYMOUS_STOP:
					effectImpact = EffectImpact.LOW;
					break;

				case CDS:
				case CHROMOSOME:
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
				case TRANSCRIPT:
				case UPSTREAM:
				case UTR_3_PRIME:
				case UTR_5_PRIME:
					effectImpact = EffectImpact.MODIFIER;
					break;

				case MOTIF:
					effectImpact = EffectImpact.LOW;

				case NEXT_PROT:
					// TODO: Refactor.This code should be in NextProt marker, not here
					if (marker == null) effectImpact = EffectImpact.MODIFIER;
					else if (((NextProt) marker).isHighlyConservedAaSequence()) effectImpact = EffectImpact.MODERATE;
					else effectImpact = EffectImpact.LOW;
					break;

				default:
					throw new RuntimeException("Unknown impact for effect type: '" + effectTypes + "'");
				}
			}
		}

		return effectImpact;
	}

	public EffectType getEffectType() {
		if (effectTypes.isEmpty()) return EffectType.NONE;
		return effectTypes.get(0);
	}

	/**
	 * Get Effect Type as a string
	 */
	public String getEffectTypeString(boolean useSeqOntology) {
		if (effectTypes == null) return "";

		StringBuilder sb = new StringBuilder();
		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append("+");
			if (useSeqOntology) sb.append(et.toSequenceOntology());
			else sb.append(et.toString());
		}

		return sb.toString();
	}

	public String getError() {
		return error;
	}

	/**
	 * Get exon (if any)
	 */
	public Exon getExon() {
		if (marker != null) {
			if (marker instanceof Exon) return (Exon) marker;
			return (Exon) marker.findParent(Exon.class);
		}
		return null;
	}

	/**
	 * Return functional class of this effect (i.e.  NONSENSE, MISSENSE, SILENT or NONE)
	 */
	public FunctionalClass getFunctionalClass() {
		if (variant.isSnp()) {
			if (!aaNew.equals(aaOld)) {
				CodonTable codonTable = marker.codonTable();
				if (codonTable.isStop(codonsNew)) return FunctionalClass.NONSENSE;

				return FunctionalClass.MISSENSE;
			}
			if (!codonsNew.equals(codonsOld)) return FunctionalClass.SILENT;
		}

		return FunctionalClass.NONE;
	}

	public Gene getGene() {
		if (marker != null) {
			if (marker instanceof Gene) return (Gene) marker;
			return (Gene) marker.findParent(Gene.class);
		}
		return null;
	}

	public String getGeneRegion() {
		switch (getEffectType()) {
		case NONE:
		case CHROMOSOME:
		case CHROMOSOME_LARGE_DELETION:
		case CUSTOM:
		case CDS:
			return EffectType.NONE.toString();
		case INTERGENIC:
		case INTERGENIC_CONSERVED:
			return EffectType.INTERGENIC.toString();
		case UPSTREAM:
			return EffectType.UPSTREAM.toString();
		case UTR_5_PRIME:
		case UTR_5_DELETED:
		case START_GAINED:
			return EffectType.UTR_5_PRIME.toString();
		case SPLICE_SITE_ACCEPTOR:
			return EffectType.SPLICE_SITE_ACCEPTOR.toString();
		case SPLICE_SITE_BRANCH_U12:
		case SPLICE_SITE_BRANCH:
			return EffectType.SPLICE_SITE_BRANCH.toString();
		case SPLICE_SITE_DONOR:
			return EffectType.SPLICE_SITE_DONOR.toString();
		case SPLICE_SITE_REGION:
			return EffectType.SPLICE_SITE_REGION.toString();
		case INTRAGENIC:
		case START_LOST:
		case SYNONYMOUS_START:
		case NON_SYNONYMOUS_START:
		case GENE:
		case NEXT_PROT:
		case TRANSCRIPT:
			if (isExon()) return EffectType.EXON.toString();
			return EffectType.NONE.toString();
		case EXON:
		case EXON_DELETED:
		case NON_SYNONYMOUS_CODING:
		case SYNONYMOUS_CODING:
		case FRAME_SHIFT:
		case CODON_CHANGE:
		case CODON_INSERTION:
		case CODON_CHANGE_PLUS_CODON_INSERTION:
		case CODON_DELETION:
		case CODON_CHANGE_PLUS_CODON_DELETION:
		case STOP_GAINED:
		case SYNONYMOUS_STOP:
		case NON_SYNONYMOUS_STOP:
		case STOP_LOST:
		case RARE_AMINO_ACID:
			return EffectType.EXON.toString();
		case INTRON:
		case INTRON_CONSERVED:
			return EffectType.INTRON.toString();
		case UTR_3_PRIME:
		case UTR_3_DELETED:
			return EffectType.UTR_3_PRIME.toString();
		case DOWNSTREAM:
			return EffectType.DOWNSTREAM.toString();
		case REGULATION:
			return EffectType.REGULATION.toString();
		case MOTIF:
			return EffectType.MOTIF.toString();
		default:
			throw new RuntimeException("Unknown gene region for effect type: '" + effectTypes + "'");
		}
	}

	/**
	 * Get genotype string
	 * @return
	 */
	public String getGenotype() {
		if (variant == null) return "";
		if (variantRef != null) return variant.getGenotype() + "-" + variantRef.getGenotype();
		return variant.getGenotype();
	}

	/**
	 * Change in HGVS notation
	 * @return
	 */
	public String getHgvs() {
		// Calculate protein level and dna level changes
		HgvsProtein hgsvProtein = new HgvsProtein(this);
		HgvsDna hgsvDna = new HgvsDna(this);
		String hgvsProt = hgsvProtein.toString();
		String hgvsDna = hgsvDna.toString();

		// Build output
		StringBuilder hgsv = new StringBuilder();
		if (hgvsProt != null) hgsv.append(hgsvProtein);
		if (hgvsDna != null) {
			if (hgsv.length() > 0) hgsv.append('/');
			hgsv.append(hgvsDna);
		}

		return hgsv.toString();
	}

	/**
	 * Get intron (if any)
	 * @return
	 */
	public Intron getIntron() {
		if (marker != null) {
			if (marker instanceof Intron) return (Intron) marker;
			return (Intron) marker.findParent(Intron.class);
		}
		return null;
	}

	public Marker getMarker() {
		return marker;
	}

	public Transcript getTranscript() {
		if (marker != null) {
			if (marker instanceof Transcript) return (Transcript) marker;
			return (Transcript) marker.findParent(Transcript.class);
		}
		return null;
	}

	public Variant getVariant() {
		return variant;
	}

	public String getWarning() {
		return warning;
	}

	/**
	 * Do we have an associated marker with additional annotations?
	 * @return
	 */
	public boolean hasAdditionalAnnotations() {
		return getMarker() != null // Do we have a marker?
				&& (getMarker() instanceof Custom) // Is it 'custom'?
				&& ((Custom) getMarker()).hasAnnotations() // Does it have additional annotations?
				;
	}

	public boolean hasError() {
		return (error != null) && (!error.isEmpty());
	}

	public boolean hasWarning() {
		return (warning != null) && (!warning.isEmpty());
	}

	/**
	 * Show data header
	 * @return
	 */
	public String header() {
		return "Warnings\t" //
				+ "Gene_ID\t" //
				+ "Gene_name\t" //
				+ "Bio_type\t" //
				+ "Trancript_ID\t" //
				+ "Exon_ID\t" //
				+ "Exon_Rank\t" //
				+ "Effect\t" //
				+ "old_AA/new_AA\t" //
				+ "Old_codon/New_codon\t" //
				+ "Codon_Num(CDS)\t" //
				+ "Codon_Degeneracy\t" //
				+ "CDS_size\t" //
				+ "Codons_around\t" //
				+ "AAs_around\t" //
				+ "Custom_interval_ID";
	}

	public boolean isCustom() {
		return getEffectType() == EffectType.CUSTOM;
	}

	public boolean isDownstream() {
		return getEffectType() == EffectType.DOWNSTREAM;
	}

	public boolean isExon() {
		return (marker instanceof Exon) || (getEffectType() == EffectType.EXON_DELETED);
	}

	public boolean isFrameShift() {
		return (getEffectType() == EffectType.FRAME_SHIFT);
	}

	public boolean isIntergenic() {
		return (getEffectType() == EffectType.INTERGENIC) || (getEffectType() == EffectType.INTERGENIC_CONSERVED);
	}

	public boolean isIntron() {
		return (getEffectType() == EffectType.INTRON) || (getEffectType() == EffectType.INTRON_CONSERVED);
	}

	public boolean isMotif() {
		return (getEffectType() == EffectType.MOTIF);
	}

	public boolean isNextProt() {
		return (getEffectType() == EffectType.NEXT_PROT);
	}

	public boolean isRegulation() {
		return (getEffectType() == EffectType.REGULATION);
	}

	public boolean isSpliceSite() {
		return (getEffectType() == EffectType.SPLICE_SITE_DONOR) //
				|| (getEffectType() == EffectType.SPLICE_SITE_ACCEPTOR) //
				|| (getEffectType() == EffectType.SPLICE_SITE_REGION) //
				|| (getEffectType() == EffectType.SPLICE_SITE_BRANCH) //
				|| (getEffectType() == EffectType.SPLICE_SITE_BRANCH_U12) //
				;
	}

	public boolean isStartGained() {
		return getEffectType() == EffectType.START_GAINED;
	}

	public boolean isUpstream() {
		return (getEffectType() == EffectType.UPSTREAM) || (getEffectType() == EffectType.START_GAINED);
	}

	public boolean isUtr() {
		return (getEffectType() == EffectType.UTR_5_PRIME) //
				|| (getEffectType() == EffectType.UTR_3_PRIME) //
				|| (getEffectType() == EffectType.UTR_5_DELETED) //
				|| (getEffectType() == EffectType.UTR_3_DELETED) //
				;
	}

	public void set(Marker marker, EffectType effectType, String message) {
		setMarker(marker); // Use setter because it takes care of warnings
		effectTypes = new LinkedList<VariantEffect.EffectType>();
		effectTypes.add(effectType);
		this.message = message;
	}

	/**
	 * Set codon change. Calculate effect type based on codon changes (for SNPs ans MNPs)
	 */
	public EffectType setCodons(String codonsOld, String codonsNew, int codonNum, int codonIndex) {
		EffectType newEffectType = null;

		this.codonsOld = codonsOld;
		this.codonsNew = codonsNew;
		this.codonNum = codonNum;
		this.codonIndex = codonIndex;

		CodonTable codonTable = marker.codonTable();
		// boolean indel = variant.isInDel();

		// Calculate amino acids
		if (codonsOld.isEmpty()) aaOld = "";
		else {
			aaOld = codonTable.aa(codonsOld);
			codonDegeneracy = codonTable.degenerate(codonsOld, codonIndex); // Calculate codon degeneracy
		}

		if (codonsNew.isEmpty()) aaNew = "";
		else aaNew = codonTable.aa(codonsNew);

		if (variant.isSnp() || variant.isMnp()) {
			// SNM and MNP effects
			if (aaOld.equals(aaNew)) {
				// Same AA: Synonymous coding
				if ((codonNum == 0) && codonTable.isStartFirst(codonsOld)) {
					// It is in the first codon (which also is a start codon)
					if (codonTable.isStartFirst(codonsNew)) addEffectType(EffectType.SYNONYMOUS_START); // The new codon is also a start codon => SYNONYMOUS_START
					else setEffectType(EffectType.START_LOST); // The AA is the same, but the codon is not a start codon => start lost
				} else if (codonTable.isStop(codonsOld)) {
					// Stop codon
					if (codonTable.isStop(codonsNew)) addEffectType(EffectType.SYNONYMOUS_STOP); // New codon is also a stop => SYNONYMOUS_STOP
					else addEffectType(EffectType.STOP_LOST); // New codon is not a stop, the we've lost a stop
				} else {
					// All other cases are just SYNONYMOUS_CODING
					addEffectType(EffectType.SYNONYMOUS_CODING);
				}
			} else {
				// Different AA: Non-synonymous coding
				if ((codonNum == 0) && codonTable.isStartFirst(codonsOld)) {
					// It is in the first codon (which also is a start codon)
					if (codonTable.isStartFirst(codonsNew)) addEffectType(EffectType.NON_SYNONYMOUS_START); // Non-synonymous mutation on first codon => start lost
					else addEffectType(EffectType.START_LOST); // Non-synonymous mutation on first codon => start lost
				} else if (codonTable.isStop(codonsOld)) {
					// Stop codon
					if (codonTable.isStop(codonsNew)) addEffectType(EffectType.NON_SYNONYMOUS_STOP); // Notice: This should never happen for SNPs! (for some reason I removed this comment at some point and that create some confusion): http://www.biostars.org/post/show/51352/in-snpeff-impact-what-is-difference-between-stop_gained-and-non-synonymous_stop/
					else addEffectType(EffectType.STOP_LOST);
				} else if (codonTable.isStop(codonsNew)) addEffectType(EffectType.STOP_GAINED);
				else {
					// All other cases are just NON_SYN
					addEffectType(EffectType.NON_SYNONYMOUS_CODING);
				}
			}
		} else {
			// Add a new effect in some cases
			if ((codonNum == 0) && codonTable.isStartFirst(codonsOld) && !codonTable.isStartFirst(codonsNew)) newEffectType = EffectType.START_LOST;
			else if (codonTable.isStop(codonsOld) && !codonTable.isStop(codonsNew)) newEffectType = EffectType.STOP_LOST;
			else if (!codonTable.isStop(codonsOld) && codonTable.isStop(codonsNew)) newEffectType = EffectType.STOP_GAINED;
		}

		return newEffectType;
	}

	/**
	 * Set values for codons around change.
	 * @param codonsLeft
	 * @param codonsRight
	 */
	public void setCodonsAround(String codonsLeft, String codonsRight) {
		codonsAroundOld = codonsLeft.toLowerCase() + codonsOld.toUpperCase() + codonsRight.toLowerCase();
		codonsAroundNew = codonsLeft.toLowerCase() + codonsNew.toUpperCase() + codonsRight.toLowerCase();

		// Amino acids surrounding the ones changed
		CodonTable codonTable = marker.codonTable();
		String aasLeft = codonTable.aa(codonsLeft);
		String aasRigt = codonTable.aa(codonsRight);
		aasAroundOld = aasLeft.toLowerCase() + aaOld.toUpperCase() + aasRigt.toLowerCase();
		aasAroundNew = aasLeft.toLowerCase() + aaNew.toUpperCase() + aasRigt.toLowerCase();
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public void setEffectImpact(EffectImpact effectImpact) {
		this.effectImpact = effectImpact;
	}

	public void setEffectType(EffectType effectType) {
		effectTypes = new LinkedList<VariantEffect.EffectType>();
		effectTypes.add(effectType);
	}

	/**
	 * Set marker. Add some warnings if the marker relates to incomplete transcripts
	 * @param marker
	 */
	public void setMarker(Marker marker) {
		this.marker = marker;

		Transcript transcript = getTranscript();
		if (transcript != null) {
			// Transcript level errors or warnings
			addErrorWarning(transcript.sanityCheck(variant));

			// Exon level errors or warnings
			Exon exon = getExon();
			if (exon != null) addErrorWarning(exon.sanityCheck(variant));
		}
	}

	@Override
	public String toString() {
		return toString(false, false);
	}

	public String toString(boolean useSeqOntology, boolean useHgvs) {
		// Get data to show
		String geneId = "", geneName = "", bioType = "", transcriptId = "", exonId = "", customId = "";
		int exonRank = -1;

		if (marker != null) {
			// Gene Id, name and biotype
			Gene gene = getGene();
			Transcript tr = getTranscript();

			// CDS size info
			if (gene != null) {
				geneId = gene.getId();
				geneName = gene.getGeneName();
				bioType = getBiotype();
			}

			// Update trId
			if (tr != null) transcriptId = tr.getId();

			// Exon rank information
			Exon exon = getExon();
			if (exon != null) {
				exonId = exon.getId();
				exonRank = exon.getRank();
			}

			// Regulation
			if (isRegulation()) bioType = ((Regulation) marker).getCellType();
		}

		// Add seqChage's ID
		if (!variant.getId().isEmpty()) customId += variant.getId();

		// Add custom markers
		if ((marker != null) && (marker instanceof Custom)) customId += (customId.isEmpty() ? "" : ";") + marker.getId();

		// CDS length
		int cdsSize = getCdsLength();

		String errWarn = error + (error.isEmpty() ? "" : "|") + warning;

		String aaChange = "";
		if (useHgvs) aaChange = getHgvs();
		else aaChange = ((aaOld.length() + aaNew.length()) > 0 ? aaOld + "/" + aaNew : "");

		return errWarn //
				+ "\t" + geneId //
				+ "\t" + geneName //
				+ "\t" + bioType //
				+ "\t" + transcriptId //
				+ "\t" + exonId //
				+ "\t" + (exonRank >= 0 ? exonRank : "") //
				+ "\t" + effect(false, false, false, useSeqOntology) //
				+ "\t" + aaChange //
				+ "\t" + ((codonsOld.length() + codonsNew.length()) > 0 ? codonsOld + "/" + codonsNew : "") //
				+ "\t" + (codonNum >= 0 ? (codonNum + 1) : "") //
				+ "\t" + (codonDegeneracy >= 0 ? codonDegeneracy + "" : "") //
				+ "\t" + (cdsSize >= 0 ? cdsSize : "") //
				+ "\t" + (codonsAroundOld.length() > 0 ? codonsAroundOld + " / " + codonsAroundNew : "") //
				+ "\t" + (aasAroundOld.length() > 0 ? aasAroundOld + " / " + aasAroundNew : "") //
				+ "\t" + customId //
				;
	}

	/**
	 * Get the simplest string describing the effect (this is mostly used for testcases)
	 */
	public String toStringSimple(boolean shortFormat) {
		String transcriptId = "";
		Transcript tr = getTranscript();
		if (tr != null) transcriptId = tr.getId();

		String exonId = "";
		Exon exon = getExon();
		if (exon != null) exonId = exon.getId();

		String eff = effect(shortFormat, true, true, false);
		if (!eff.isEmpty()) return eff;
		if (!exonId.isEmpty()) return exonId;
		if (!transcriptId.isEmpty()) return transcriptId;

		return "NO EFFECT";
	}

	/**
	 * Return a string safe enough to be used in a VCF file
	 */
	String vcfSafe(String str) {
		return str;
	}

}
