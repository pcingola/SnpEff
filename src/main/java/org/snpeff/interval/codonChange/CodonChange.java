package org.snpeff.interval.codonChange;

import java.util.List;

import org.snpeff.codons.CodonTable;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Analyze codon changes based on a variant and a Transcript
 *
 * @author pcingola
 */
public class CodonChange {

	public static boolean showCodonChange = true; // This is disabled in some specific test cases
	public static final int CODON_SIZE = 3; // I'll be extremely surprised if you ever need to change this parameter...

	boolean returnNow = false; // Can we return immediately after calculating the first 'codonChangeSingle()'?
	boolean requireNetCdsChange = false;
	Variant variant;
	Transcript transcript;
	Exon exon = null;
	VariantEffects variantEffects;
	int codonStartNum = -1;
	int codonStartIndex = -1;
	String codonsRef = ""; // REF codons (without variant)
	String codonsAlt = ""; // ALT codons (after variant is applied)
	String netCdsChange = "";

	/**
	 * Create a specific codon change for a variant
	 */
	public static CodonChange factory(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		switch (variant.getVariantType()) {
		case SNP:
			return new CodonChangeSnp(variant, transcript, variantEffects);
		case INS:
			return new CodonChangeIns(variant, transcript, variantEffects);
		case DEL:
			return new CodonChangeDel(variant, transcript, variantEffects);
		case MNP:
			return new CodonChangeMnp(variant, transcript, variantEffects);
		case MIXED:
			return new CodonChangeMixed(variant, transcript, variantEffects);
		case DUP:
			return new CodonChangeDup(variant, transcript, variantEffects);
		case INV:
			return new CodonChangeInv(variant, transcript, variantEffects);
		case INTERVAL:
			return new CodonChangeInterval(variant, transcript, variantEffects);
		default:
			throw new RuntimeException("Unimplemented factory for variant type '" + variant.getVariantType() + "', variant: " + variant);
		}
	}

	protected CodonChange(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		this.transcript = transcript;
		this.variantEffects = variantEffects;
		this.variant = variant;
	}

	/**
	 * Calculate additional effect due to codon changes
	 * E.g. A frame-shift that also affects a stop codon
	 */
	protected EffectType additionalEffect(String codonsOld, String codonsNew, int codonNum, int codonIndex, String aaOld, String aaNew) {
		EffectType newEffectType = null;

		CodonTable codonTable = transcript.codonTable();

		if (variant.isSnp() || variant.isMnp()) {
			// SNM and MNP effects
			if (aaOld.equals(aaNew)) {

				// Same AA: Synonymous coding
				if ((codonNum == 0) && codonTable.isStartFirst(codonsOld)) {
					// It is in the first codon (which also is a start codon)
					if (codonTable.isStartFirst(codonsNew)) newEffectType = EffectType.SYNONYMOUS_START; // The new codon is also a start codon => SYNONYMOUS_START
					else newEffectType = EffectType.START_LOST; // The AA is the same, but the codon is not a start codon => start lost
				} else if (codonTable.isStop(codonsOld)) {
					// Stop codon
					if (codonTable.isStop(codonsNew)) newEffectType = EffectType.SYNONYMOUS_STOP; // New codon is also a stop => SYNONYMOUS_STOP
					else newEffectType = EffectType.STOP_LOST; // New codon is not a stop, the we've lost a stop
				} else newEffectType = EffectType.SYNONYMOUS_CODING; // All other cases are just SYNONYMOUS_CODING

			} else {

				// Different AA: Non-synonymous coding
				if ((codonNum == 0) && codonTable.isStartFirst(codonsOld)) {
					// It is in the first codon (which also is a start codon)
					if (codonTable.isStartFirst(codonsNew)) newEffectType = EffectType.NON_SYNONYMOUS_START; // Non-synonymous mutation on first codon => start lost
					else newEffectType = EffectType.START_LOST; // Non-synonymous mutation on first codon => start lost
				} else if (codonTable.isStop(codonsOld)) {
					// Stop codon
					if (codonTable.isStop(codonsNew)) newEffectType = EffectType.NON_SYNONYMOUS_STOP; // Notice: This should never happen for SNPs! (for some reason I removed this comment at some point and that create some confusion): http://www.biostars.org/post/show/51352/in-snpeff-impact-what-is-difference-between-stop_gained-and-non-synonymous_stop/
					else newEffectType = EffectType.STOP_LOST;
				} else if (codonTable.isStop(codonsNew)) newEffectType = EffectType.STOP_GAINED;
				else newEffectType = EffectType.NON_SYNONYMOUS_CODING; // All other cases are just NON_SYN

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
	 * Calculate base number in a cds where 'pos' is
	 */
	protected int cdsBaseNumber(int pos) {
		int cdsbn = transcript.baseNumberCds(pos, true);

		// Does not intersect the transcript?
		if (cdsbn < 0) {
			// 'pos' before transcript start
			if (pos <= transcript.getCdsStart()) {
				if (transcript.isStrandPlus()) return 0;
				return transcript.cds().length();
			}

			// 'pos' is after CDS end
			if (transcript.isStrandPlus()) return transcript.cds().length();
			return 0;
		}

		return cdsbn;
	}

	/**
	 * Calculate a list of codon changes
	 */
	public void codonChange() {
		if (!transcript.intersects(variant)) return;

		// Get coding start (after 5 prime UTR)
		int cdsStart = transcript.getCdsStart();

		// We may have to calculate 'netCdsChange', which is the effect on the CDS
		netCdsChange = netCdsChange();
		if (requireNetCdsChange && netCdsChange.isEmpty()) { // This can happen on mixed changes where the 'InDel' part lies outside the transcript's exons
			codonsRef = codonsAlt = "";
			return;
		}

		//---
		// Concatenate all exons
		//---
		int firstCdsBaseInExon = 0; // Where the exon maps to the CDS (i.e. which CDS base number does the first base in this exon maps to).
		List<Exon> exons = transcript.sortedStrand();
		for (Exon exon : exons) {
			this.exon = exon;
			if (exon.intersects(variant)) {
				int cdsBaseInExon = -1; // cdsBaseInExon: base number relative to the beginning of the coding part of this exon (i.e. excluding 5'UTRs)

				if (transcript.isStrandPlus()) {
					int firstvariantBaseInExon = Math.max(variant.getStart(), Math.max(exon.getStart(), cdsStart));
					cdsBaseInExon = firstvariantBaseInExon - Math.max(exon.getStart(), cdsStart);
				} else {
					int lastvariantBaseInExon = Math.min(variant.getEnd(), Math.min(exon.getEnd(), cdsStart));
					cdsBaseInExon = Math.min(exon.getEnd(), cdsStart) - lastvariantBaseInExon;
				}

				if (cdsBaseInExon < 0) cdsBaseInExon = 0;

				// Get codon number and index within codon
				if (codonStartNum < 0) {
					codonStartNum = (firstCdsBaseInExon + cdsBaseInExon) / CODON_SIZE;
					codonStartIndex = (firstCdsBaseInExon + cdsBaseInExon) % CODON_SIZE;
				}

				// Use appropriate method to calculate codon change
				boolean hasChanged = false; // Was there any change?
				hasChanged = codonChange(exon);

				// Any change? => Add change to list
				if (hasChanged && !variantEffects.hasMarker()) variantEffects.setMarker(exon); // It is affecting this exon, so we set the marker

				// Can we finish after effect of first exon is added?
				if (returnNow) return;
			}

			if (transcript.isStrandPlus()) firstCdsBaseInExon += Math.max(0, exon.getEnd() - Math.max(exon.getStart(), cdsStart) + 1);
			else firstCdsBaseInExon += Math.max(0, Math.min(cdsStart, exon.getEnd()) - exon.getStart() + 1);
		}

		return;
	}

	/**
	 * Calculate the effect on an exon
	 */
	protected boolean codonChange(Exon exon) {
		throw new RuntimeException("Unimplemented method codonChangeSingle() for\n\t\tVariant type : " + variant.getType() + "\n\t\tClass        : " + getClass().getSimpleName() + "\n\t\tVariant      : " + variant);
	}

	/**
	 * Calculate new codons
	 */
	protected String codonsAlt() {
		throw new RuntimeException("Unimplemented method for this thype of CodonChange: " + this.getClass().getSimpleName());
	}

	/**
	 * Calculate 'reference' codons
	 */
	protected String codonsRef() {
		return codonsRef(1);
	}

	/**
	 * Calculate 'reference' codons
	 */
	protected String codonsRef(int numCodons) {
		String cds = transcript.cds();
		String codon = "";

		int start = codonStartNum * CodonChange.CODON_SIZE;
		int end = start + numCodons * CodonChange.CODON_SIZE;

		int len = cds.length();
		if (start >= cds.length()) start = len;
		if (end >= cds.length()) end = len;

		// Capitalize
		codon = cds.substring(start, end);

		// Codon not multiple of three? Add missing bases as 'N'
		if (codon.length() % 3 == 1) codon += "NN";
		else if (codon.length() % 3 == 2) codon += "N";

		return codon;
	}

	/**
	 * Calculate variant effect
	 * @param marker: Genomic marker affected by this variant (e.g. exon, transcript, etc)
	 * @param effectType: Effect type
	 * @param allowReplace: Can another variant effect replace this one?
	 * @return A new VariantEffect object
	 */
	protected VariantEffect effect(Marker marker, EffectType effectType, boolean allowReplace) {
		return effect(marker, effectType, effectType.effectImpact(), codonsRef, codonsAlt, codonStartNum, codonStartIndex, allowReplace);
	}

	/**
	 * Add an effect
	 */
	private VariantEffect effect(Marker marker, EffectType effectType, EffectImpact effectImpact, String codonsOld, String codonsNew, int codonNum, int codonIndex, boolean allowReplace) {
		// Create and add variant affect
		int cDnaPos = transcript.baseNumber2MRnaPos(variant.getStart());
		VariantEffect varEff = new VariantEffect(variant, marker, effectType, effectImpact, codonsOld, codonsNew, codonNum, codonIndex, cDnaPos);
		variantEffects.add(varEff);

		// Are there any additional effects? Sometimes a new effect arises from setting codons (e.g. FRAME_SHIFT disrupts a STOP codon)
		EffectType addEffType = additionalEffect(codonsOld, codonsNew, codonNum, codonIndex, varEff.getAaRef(), varEff.getAaAlt());
		if (addEffType != null && addEffType != effectType) {
			if (allowReplace && addEffType.compareTo(effectType) < 0) {
				// Replace main effect (using default impact)
				varEff.setEffect(addEffType);
			} else {
				// Add effect to list (using default impact)
				varEff.addEffect(addEffType);
			}
		}

		return varEff;
	}

	protected VariantEffect effectNoCodon(Marker marker, EffectType effectType) {
		return effect(marker, effectType, effectType.effectImpact(), "", "", -1, -1, false);
	}

	protected VariantEffect effectNoCodon(Marker marker, EffectType effectType, EffectImpact effectImpact) {
		return effect(marker, effectType, effectImpact, "", "", -1, -1, false);
	}

	/**
	 * Does the variant intersect any exons?
	 */
	protected boolean intersectsExons() {
		for (Exon ex : transcript)
			if (variant.intersects(ex)) return true;
		return false;
	}

	/**
	 * We may have to calculate 'netCdsChange', which is the effect on the CDS
	 * Note: A deletion or a MNP might affect several exons
	 */
	protected String netCdsChange() {
		if (!requireNetCdsChange) return "";

		if (variant.size() > 1) {
			StringBuilder sb = new StringBuilder();
			for (Exon exon : transcript.sortedStrand())
				sb.append(variant.netChange(exon));
			return sb.toString();
		}

		return variant.netChange(transcript.isStrandMinus());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Transcript : " + transcript.getId() + "\n");
		sb.append("Variant    : " + variant + "\n");
		sb.append("Codons     : " + codonsRef + "/" + codonsAlt + "\tnum: " + codonStartNum + "\tidx: " + codonStartIndex + "\n");
		sb.append("Effects    :\n");
		for (VariantEffect veff : variantEffects)
			sb.append("\t" + veff.getEffectTypeString(false) + "\t" + veff.getCodonsRef() + "/" + veff.getCodonsAlt() + "\t" + veff.getAaRef() + "/" + veff.getAaAlt() + "\n");

		return sb.toString();
	}
}
