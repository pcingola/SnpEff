package org.snpeff.annotate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;

/**
 * Detects variants / vcfEntries affecting the same codon (in the same transcript)
 *
 * @author pcingola
 */
public abstract class HaplotypeDetectorBase extends HaplotypeAnnotationDetector {

	Set<EffectType> supportedEffectTypes;
	VcfEntry latestVcfEntry;
	AutoHashMap<String, HashSet<VcfHaplotypeTuple>> tuplesByTr;
	AutoHashMap<VcfEntry, HashSet<VcfHaplotypeTuple>> tuplesByVcfentry;

	public HaplotypeDetectorBase() {
		reset();
	}

	/**
	 * Add a <vcfEntry, variant, veff> tuple
	 */
	@Override
	public void add(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		latestVcfEntry = ve;

		// Valid variant & varintEffect?
		if (!isValidVariant(variant) || !isValidVariantEffect(variantEffect)) return;

		// Add
		VcfHaplotypeTuple vht = new VcfHaplotypeTuple(ve, variant, variantEffect);
		add(vht);
	}

	protected void add(VcfHaplotypeTuple vht) {
		tuplesByTr.getOrCreate(vht.getTrId()).add(vht);
		tuplesByVcfentry.getOrCreate(vht.getVcfEntry()).add(vht);
	}

	/**
	 * Check if both transcripts can be in the same haplotype annotation (only check transcript/codon level information)
	 */
	abstract protected boolean checkTranscript(VcfHaplotypeTuple vht1, VcfHaplotypeTuple vht2);

	/**
	 * Return the variants within the same haplotype (for this detector)
	 */
	@Override
	public Set<Variant> haplotype(Variant var, Transcript tr) {
		return null;
	}

	/**
	 * Does this set have a 'haplotype annotation' variant?
	 */
	protected boolean hasHaplotypeAnnotation(Set<VcfHaplotypeTuple> tupleSetVe, Set<VcfHaplotypeTuple> tupleSetTr) {
		if (tupleSetTr == null || tupleSetVe == null) return false;

		for (VcfHaplotypeTuple vht1 : tupleSetVe) {
			VcfEntry ve1 = vht1.getVcfEntry();
			for (VcfHaplotypeTuple vht2 : tupleSetTr) {
				VcfEntry ve2 = vht2.getVcfEntry();
				if (ve1.compareTo(ve2) == 0) continue; // Don't compare to self
				if (hasHaplotypeAnnotation(vht1, vht2)) return true;
			}
		}

		return false;
	}

	/**
	 * Does this vcfEntry have any 'haplotype annotation' variants associated?
	 */
	@Override
	public boolean hasHaplotypeAnnotation(VcfEntry ve) {
		Set<VcfHaplotypeTuple> tupleSetVe = tuplesByVcfentry.get(ve);
		if (tupleSetVe == null) return false;

		// Look tuples by codon
		for (VcfHaplotypeTuple vht : tupleSetVe) {
			String key = vht.getTrId();
			Set<VcfHaplotypeTuple> tupleSetTr = tuplesByTr.get(key);
			if (hasHaplotypeAnnotation(tupleSetVe, tupleSetTr)) return true;
		}

		return false;
	}

	/**
	 * Do these haplotype tuples have a 'haplotype annotation'?
	 */
	protected boolean hasHaplotypeAnnotation(VcfHaplotypeTuple vht1, VcfHaplotypeTuple vht2) {
		// Check transcript level information
		if (!checkTranscript(vht1, vht2)) return false;

		// Check haplotype phasing
		VcfEntry ve1 = vht1.getVcfEntry();
		VcfEntry ve2 = vht2.getVcfEntry();
		List<VcfGenotype> gts1 = ve1.getVcfGenotypes();
		List<VcfGenotype> gts2 = ve2.getVcfGenotypes();

		int len = Math.min(gts1.size(), gts2.size());
		for (int i = 0; i < len; i++) {
			if (arePhased(gts1.get(i), gts2.get(i))) return true;
		}

		return false;
	}

	abstract void initSUpportedEffectTypes();

	@Override
	public boolean isFree(VcfEntry ve) {
		if (latestVcfEntry == null //
				|| latestVcfEntry == ve //
		) return false;

		// The vcfEntry does not have any tuples? Then there's nothing interesting about it
		Set<VcfHaplotypeTuple> tupleSet = tuplesByVcfentry.get(ve);
		if (tupleSet == null) return true;

		// The latestVcfEntry does not have any tuples?
		// Then we don't know whether it's in the same codon or not
		// e.g. latestVcfEntry is in an intron but the next vcfEntry is
		// in the an exon within the same codon as 've'
		Set<VcfHaplotypeTuple> tupleSetLatest = tuplesByVcfentry.get(latestVcfEntry);
		if (tupleSetLatest == null) return false;

		// Does 've' share any transcript & codon with the latest vcfEntry?
		for (VcfHaplotypeTuple vht1 : tupleSetLatest)
			for (VcfHaplotypeTuple vht2 : tupleSet)
				if (vht1.aaIntersect(vht2)) return false; // Same codon on at least one transcript: 've' is nor free

		return true;
	}

	abstract protected boolean isValidVariant(Variant variant);

	/**
	 * Can this a variant effect be analyzed in this detector?
	 */
	protected boolean isValidVariantEffect(VariantEffect variantEffect) {
		// Does it have a transcript and a codon number?
		if (variantEffect.getTranscript() == null || variantEffect.getCodonNum() < 0) return false;

		// Effect type supported?
		EffectType effType = variantEffect.getEffectType();
		return supportedEffectTypes.contains(effType);
	}

	/**
	 * Remove a VcfEntry
	 */
	@Override
	public void remove(VcfEntry ve) {
		if (ve == latestVcfEntry) latestVcfEntry = null;

		// Remove from 'tuplesByVcfentry
		Set<VcfHaplotypeTuple> tupleSet = tuplesByVcfentry.remove(ve);
		if (tupleSet == null) return;

		// Remove from 'tuplesByTrCodon'
		// Remove all VcfHaplotypeTuple associated with this 'vcfEntry'
		for (VcfHaplotypeTuple vht : tupleSet) {
			String key = vht.getTrId();

			// Remove vht from this set
			Set<VcfHaplotypeTuple> tset = tuplesByTr.get(key);
			if (tset != null) {
				tset.remove(vht);
				if (tset.isEmpty()) { // No more entries in set? Remove it
					tuplesByTr.remove(key);
				}
			}
		}
	}

	/**
	 * Reset all internal data structures
	 */
	void reset() {
		tuplesByTr = new AutoHashMap<>(new HashSet<VcfHaplotypeTuple>());
		tuplesByVcfentry = new AutoHashMap<>(new HashSet<VcfHaplotypeTuple>());
		initSUpportedEffectTypes();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() + ":\n");

		sb.append("\ttuplesByTrCodon.size:" + tuplesByTr.size() + ":\n");
		for (String key : tuplesByTr.keySet()) {
			sb.append("\t\t'" + key + "': ");
			sb.append("[ ");

			HashSet<VcfHaplotypeTuple> tupleSet = tuplesByTr.get(key);
			for (VcfHaplotypeTuple vht : tupleSet)
				sb.append("'" + vht + "' ");

			sb.append("]\n");
		}

		sb.append("\ttuplesByVcfentry.size:" + tuplesByVcfentry.size() + ":\n");
		for (VcfEntry ve : tuplesByVcfentry.keySet()) {
			sb.append("\t\t" + ve.toStr() + ": ");
			sb.append("[ ");

			HashSet<VcfHaplotypeTuple> tupleSet = tuplesByVcfentry.get(ve);
			for (VcfHaplotypeTuple vht : tupleSet)
				sb.append("'" + vht + "' ");

			sb.append("]\n");
		}

		return sb.toString();
	}

}
