package org.snpeff.annotate;

import java.util.List;
import java.util.Set;

import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;

/**
 * Detect haplotype annotations.
 * Note that there are different levels of detection:
 *
 * 	- Does a variant have "any" haplotype annotation?
 *
 * 	- Could a variant have "any" haplotype annotation (in the future)?
 *
 *  - Which variants conform the haplotype for a specific transcript?
 *
 * @author pcingola
 */
public abstract class HaplotypeAnnotationDetector {

	boolean verbose;

	public abstract void add(VcfEntry ve, Variant variant, VariantEffect variantEffect);

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
			int geno1[] = gt1.getGenotype();
			int geno2[] = gt1.getGenotype();
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

	/**
	 * Find a set of variants creating a haplotype with 'var' to
	 * be annotated respect to transcript 'tr'
	 */
	public abstract Set<Variant> haplotype(Variant var, Transcript tr);

	/**
	 * Analyze if a VcfEntry has a haplotype annotation
	 *
	 * Note: Even when returning 'false', a new VcfEntry entry
	 *       could create a haplotype annotation for this entry
	 *
	 * @return true if the VcfEntry does have a haplotype annotation
	 */
	public abstract boolean hasHaplotypeAnnotation(VcfEntry ve);

	/**
	 * Is there any kind of phasing information?
	 */
	protected boolean hasPhase(VcfEntry vcfEntry) {
		List<VcfGenotype> gts = vcfEntry.getVcfGenotypes();
		for (VcfGenotype gt : gts) {
			if (gt.isPhased() || gt.isHomozygousAlt()) { // Homozygous ALT means implicit phasing
				return true;
			}
		}
		return false;
	}

	/**
	 * Analyze if a VcfEntry has or could have in the future any
	 * haplotype annotation for this detector.
	 *
	 * @return true if the VcfEntry will not have any (more) haplotype annotations
	 */
	public abstract boolean isFree(VcfEntry ve);

	/**
	 * Remove entry and free associated resources
	 */
	public abstract void remove(VcfEntry ve);

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

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
