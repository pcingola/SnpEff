package org.snpeff.annotate;

import java.util.OptionalInt;

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
	public static final byte MISSING_GT_VALUE = -1;

	VcfEntry vcfEntry;
	Variant variant;
	VariantEffect variantEffect;
	Transcript transcript;
	int aaStart, aaEnd;
	int numSamples = -1;
	int maxGtPerSample = -1;
	byte gt[][];
	boolean gtPhased[];
	boolean gtAlt[];
	String gtPhaseGroup[];

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
	protected boolean arePhased(VcfTuple vt, int sampelNum) {
		// Are the variants phased?
		if (isPhased(sampelNum) && vt.isPhased(sampelNum)) {
			// If phased, do they have phase groups?
			// (or no phase group information at all)
			if (!samePhaseGroup(vt, sampelNum)) return false;

			// Check that at least one ALT is on the same chromosome (maternal / paternal)
			byte geno1[] = gt[sampelNum];
			byte geno2[] = vt.getGt(sampelNum);
			int min = Math.min(geno1.length, geno2.length);
			for (int i = 0; i < min; i++) {
				if (geno1[i] > 0 && geno2[i] > 0) return true;
			}

			return false;
		}

		// Not phased? Check for implicit phasing
		// i.e. Is at least one of them homozygous ALT?
		return isHomozygousAlt(sampelNum) || vt.isHomozygousAlt(sampelNum);
	}

	public int getCodonNum() {
		return variantEffect.getCodonNum();
	}

	public byte[][] getGt() {
		parseGt();
		return gt;
	}

	public byte[] getGt(int idx) {
		return gt[idx];
	}

	public String getGtPhaseGroup(int i) {
		return gtPhaseGroup[i];
	}

	public int getMaxGtPerSample() {
		return maxGtPerSample;
	}

	public int getNumSamples() {
		return numSamples;
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

	public boolean isGtAlt(int i) {
		return gtAlt[i];
	}

	/**
	 * Do these haplotype tuples have a 'haplotype annotation'?
	 */
	public boolean isHaplotypeWith(VcfTuple vt) {
		parseGt();
		vt.parseGt();

		// Sanity checks
		if (numSamples != vt.getNumSamples())//
			throw new RuntimeException("Cannot check haplotype involving VCF entries having different number of samples:" //
					+ "\n\tVcf tuple 1: " + this //
					+ "\n\tVcf tuple 2: " + vt //
			);

		if (maxGtPerSample != vt.getMaxGtPerSample())//
			throw new RuntimeException("Cannot check haplotype involving VCF entries having different 'max number of genotypes per sample':" //
					+ "\n\tVcf tuple 1 maxGtPerSample: " + maxGtPerSample //
					+ "\n\tVcf tuple 2 maxGtPerSample: " + vt.getMaxGtPerSample() //
			);

		// Is any sample phased?
		for (int sampelNum = 0; sampelNum < numSamples; sampelNum++)
			if (arePhased(vt, sampelNum)) return true;

		return false;
	}

	/**
	 * Is this sample Homozygous ALT
	 *
	 * FIXME: This would fail to handle the case where a samples has a missing parental chromosome (e.g. chrY)
	 */
	protected boolean isHomozygousAlt(int sampelNum) {
		byte gts[] = gt[sampelNum];

		// Missing
		if (gts[0] <= 0) return false;

		// Any genotype is different? => not homozygous
		for (int i = 1; i < gts.length; i++)
			if (gts[i] != gts[i - 1]) return false;

		return true; // Homozygous
	}

	public boolean isPhased(int i) {
		return gtPhased[i];
	}

	/**
	 * Initialize genotype relate fields
	 */
	protected void parseGt() {
		if (gt != null) return;

		// No samples?
		numSamples = vcfEntry.getNumberOfSamples();
		if (numSamples == 0) {
			gt = EMPTY_GT_ARRAY;
			return;
		}

		// Get max number of genotypes in this VcfEntry
		OptionalInt maxGts = vcfEntry.getVcfGenotypes().stream() //
				.mapToInt(vg -> vg.getGenotypeLen()) //
				.max();

		// No genotypes?
		if (!maxGts.isPresent()) {
			gt = EMPTY_GT_ARRAY;
			return;
		}

		// Create an array of geotypes, copy values
		maxGtPerSample = maxGts.getAsInt();
		gt = new byte[numSamples][maxGtPerSample];
		gtPhased = new boolean[numSamples];
		gtAlt = new boolean[numSamples];
		gtPhaseGroup = new String[numSamples];
		int sampleNum = 0;
		for (VcfGenotype vgt : vcfEntry.getVcfGenotypes()) {
			boolean isAlt = false;
			byte sampelGt[] = vgt.getGenotype();
			for (int i = 0; i < maxGtPerSample; i++) {
				gt[sampleNum][i] = (i < sampelGt.length ? sampelGt[i] : MISSING_GT_VALUE); // Check for missing elements
				isAlt |= (gt[sampleNum][i] > 0);
			}

			gtPhased[sampleNum] = vgt.isPhased();
			gtAlt[sampleNum] = isAlt;
			gtPhaseGroup[sampleNum] = vgt.get(VcfGenotype.GT_FIELD_PHASE_GROUP);

			sampleNum++;
		}
	}

	/**
	 * Are these genotypes in the same phase group?
	 */
	protected boolean samePhaseGroup(VcfTuple vt, int index) {
		String ps1 = gtPhaseGroup[index];
		String ps2 = vt.getGtPhaseGroup(index);
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
