package org.snpeff.annotate;

import java.util.Set;

import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.vcf.VcfEntry;

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
	 * Find a set of variants creating a haplotype with 'var' to
	 * be annotated respect to transcript 'tr'
	 */
	public abstract Set<VcfTupleSet> haplotypes(Variant var, Transcript tr);

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

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
