package org.snpeff.annotate;

import java.util.List;

import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;

/**
 * 
 * A tuple: <VcfEntry, variant, variantEffect>
 * 
 * @author pcingola
 */
public class VcfTuple {

	public static final byte[][] EMPTY_GT_ARRAY = new byte[0][0];

	VcfEntry vcfEntry;
	Variant variant;
	VariantEffect variantEffect;
	Transcript transcript;
	int aaStart, aaEnd;
	byte gt[][];

	public VcfTuple(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		vcfEntry = ve;
		this.variant = variant;
		this.variantEffect = variantEffect;
		transcript = variantEffect.getTranscript();

		if (hasTranscript()) {
			aaStart = variantEffect.getCodonNum();
			if (variant.isSnp()) aaEnd = aaStart;
			else {
				String aaRef = variantEffect.getAaRef();
				aaEnd = aaStart + aaRef.length() - 1;
			}
		} else {
			aaStart = aaEnd = -1;
		}

		initGt(ve);
	}

	void initGt(VcfEntry ve) {
		int numSamples = ve.getNumberOfSamples();
		if (numSamples == 0) {
			gt = EMPTY_GT_ARRAY;
			return;
		}

	}

	/**
	 * Do the amino acid numbers intersect
	 */
	public boolean aaIntersect(VcfTuple vht) {
		return sameTr(vht) // Same transcript
				&& (aaStart <= vht.aaEnd) && (aaEnd >= vht.aaStart) // Do AA intervals intersect?
		;
	}

	/**
	 * Are these genotypes phased?
	 */
	protected boolean arePhased(VcfGenotype gt1, VcfGenotype gt2) {
		// Are the variants phased?
		if (gt1.isPhased() && gt2.isPhased()) {
			// If phased, do they have phase groups?
			// (or no phase group information at all)
			if (!samePhaseGroup(gt1, gt2)) return false;

			// Check that at least one ALT is on the same chromosome (maternal / paternal)
			byte geno1[] = gt1.getGenotype();
			byte geno2[] = gt2.getGenotype();
			int min = Math.min(geno1.length, geno2.length);
			for (int i = 0; i < min; i++) {
				if (geno1[i] > 0 && geno2[i] > 0) return true;
			}

			return false;
		} else {
			// Not phased? Check for implicit phasing
			// i.e. Is at least one of them homozygous ALT?
			return gt1.isHomozygousAlt() || gt2.isHomozygousAlt();
		}
	}

	public int getCodonNum() {
		return variantEffect.getCodonNum();
	}

	public Transcript getTranscript() {
		return variantEffect.getTranscript();
	}

	public String getTrId() {
		return transcript.getId();
	}

	public Variant getVariant() {
		return variant;
	}

	public VariantEffect getVariantEffect() {
		return variantEffect;
	}

	public VcfEntry getVcfEntry() {
		return vcfEntry;
	}

	public boolean hasTranscript() {
		return transcript != null;
	}

	/**
	 * Do these haplotype tuples have a 'haplotype annotation'?
	 */
	public boolean isHaplotypeWith(VcfTuple vht) {
		// Check haplotype phasing
		VcfEntry ve1 = getVcfEntry();
		VcfEntry ve2 = vht.getVcfEntry();
		List<VcfGenotype> gts1 = ve1.getVcfGenotypes();
		List<VcfGenotype> gts2 = ve2.getVcfGenotypes();

		int len = Math.min(gts1.size(), gts2.size());
		for (int i = 0; i < len; i++) {
			if (arePhased(gts1.get(i), gts2.get(i))) return true;
		}

		return false;
	}

	/**
	 * Are these genotypes in the same phase group?
	 */
	protected boolean samePhaseGroup(VcfGenotype gt1, VcfGenotype gt2) {
		String ps1 = gt1.get(VcfGenotype.GT_FIELD_PHASE_GROUP);
		String ps2 = gt2.get(VcfGenotype.GT_FIELD_PHASE_GROUP);
		if (ps1 == null && ps2 == null) return true; // Both of them are empty? Consider them as match
		if (ps1 == null || ps2 == null) return false; // Only one of them is empty? Consider them as NO match
		return ps1.equals(ps2);
	}

	/**
	 * Same transcript?
	 */
	public boolean sameTr(VcfTuple vht) {
		return getTrId().equals(vht.getTrId());
	}

	@Override
	public String toString() {
		return vcfEntry.toStr() //
				+ "," + variant //
				+ "," + (hasTranscript() ? transcript.getId() : "null")//
				+ ",codon" + (aaStart == aaEnd ? ": " + aaStart : "s : [" + aaStart + ", " + aaEnd + "]") //
		;
	}

}
