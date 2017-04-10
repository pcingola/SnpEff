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
	VcfHaplotypeTuple latestVcfHaplotypeTuple;
	AutoHashMap<String, HashSet<VcfHaplotypeTuple>> tuplesByTr;
	AutoHashMap<VcfEntry, HashSet<VcfHaplotypeTuple>> tuplesByVcfentry;
	AutoHashMap<VcfEntry, HashSet<String>> trIdsByVcfentry;

	public HaplotypeDetectorBase() {
		reset();
	}

	/**
	 * Add a <vcfEntry, variant, veff> tuple
	 */
	@Override
	public void add(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		latestVcfHaplotypeTuple = new VcfHaplotypeTuple(ve, variant, variantEffect);

		// Always add transcript Ids
		addTrIds(ve, variantEffect);

		// Only add if it's a valid variant & varintEffect
		if (isValidVariant(variant) && isValidVariantEffect(variantEffect)) {
			add(latestVcfHaplotypeTuple);
		}
	}

	protected void add(VcfHaplotypeTuple vht) {
		tuplesByTr.getOrCreate(vht.getTrId()).add(vht);
		tuplesByVcfentry.getOrCreate(vht.getVcfEntry()).add(vht);
	}

	/**
	 * Add transcript ID by VcfEntry
	 */
	protected void addTrIds(VcfEntry ve, VariantEffect variantEffect) {
		// Always store transcripts
		Transcript tr = variantEffect.getTranscript();
		if (tr != null) trIdsByVcfentry.getOrCreate(ve).add(tr.getId());
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

		// Look tuples by transcript
		for (VcfHaplotypeTuple vht : tupleSetVe) {
			String trid = vht.getTrId();
			Set<VcfHaplotypeTuple> tupleSetTr = tuplesByTr.get(trid);
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

	/**
	 * Do these VcfEntries share any transcript ID?
	 */
	protected boolean hasSameTrIdAsLatest(VcfEntry ve) {
		Set<String> trIdsLatest = trIdsByVcfentry.get(latestVcfHaplotypeTuple.getVcfEntry());
		if (trIdsLatest == null) return false; // Latest entry deosn't have any transcript IDs

		Set<String> trIdsVe = trIdsByVcfentry.get(ve);
		if (trIdsVe == null) return false; // Something went wrong here. Normally we should not have a missing set for this

		for (String trid : trIdsLatest)
			if (trIdsVe.contains(trid)) return true;

		return false;
	}

	abstract void initSUpportedEffectTypes();

	@Override
	public boolean isFree(VcfEntry ve) {
		// Latest entry is not free, we don't know if the next entry might add a
		if (latestVcfHaplotypeTuple == null //
				|| latestVcfHaplotypeTuple.getVcfEntry() == ve //
		) return false;

		// The vcfEntry does not have any tuples? Then there's nothing interesting about it
		Set<VcfHaplotypeTuple> tupleSet = tuplesByVcfentry.get(ve);
		if (tupleSet == null) return true;

		// The latestVcfEntry does not have any transcript IDs?
		// Then it is not in the same haplotype
		if (!hasSameTrIdAsLatest(ve)) return true;

		// The latestVcfEntry does not have any tuples?
		// Then we don't know whether it's in the same haplotype or not
		// e.g. latestVcfEntry is in an intron but the next vcfEntry is
		// in the an exon within the same codon as 've'
		Set<VcfHaplotypeTuple> tupleSetLatest = tuplesByVcfentry.get(latestVcfHaplotypeTuple.getVcfEntry());
		if (tupleSetLatest == null) return false;

		// Does 've' share any transcript with the latest vcfEntry?
		for (VcfHaplotypeTuple vht1 : tupleSetLatest)
			for (VcfHaplotypeTuple vht2 : tupleSet)
				if (checkTranscript(vht1, vht2)) return false; // Condition checks, then 've' is nor free

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
		if (latestVcfHaplotypeTuple != null && ve == latestVcfHaplotypeTuple.getVcfEntry()) latestVcfHaplotypeTuple = null;

		// Remove from 'transcript Ids'
		trIdsByVcfentry.remove(ve);

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
		trIdsByVcfentry = new AutoHashMap<>(new HashSet<String>());
		initSUpportedEffectTypes();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() + ":\n");

		sb.append("\ttuplesByTr.size:" + tuplesByTr.size() + ":\n");
		for (String key : tuplesByTr.keySet()) {
			sb.append("\t\t'" + key + "': ");
			sb.append("[ ");

			Set<VcfHaplotypeTuple> tupleSet = tuplesByTr.get(key);
			for (VcfHaplotypeTuple vht : tupleSet)
				sb.append("'" + vht + "' ");

			sb.append("]\n");
		}

		sb.append("\ttuplesByVcfentry.size:" + trIdsByVcfentry.size() + ":\n");
		for (VcfEntry ve : trIdsByVcfentry.keySet()) {
			sb.append("\t\t" + ve.toStr() + ": ");
			sb.append("[ ");

			Set<VcfHaplotypeTuple> tupleSet = tuplesByVcfentry.get(ve);
			for (VcfHaplotypeTuple vht : tupleSet)
				sb.append("'" + vht + "' ");

			sb.append("]\n");
		}

		sb.append("\ttrIdsByVcfentry.size:" + trIdsByVcfentry.size() + ":\n");
		for (VcfEntry ve : trIdsByVcfentry.keySet()) {
			sb.append("\t\t'" + ve.toStr() + "': ");
			sb.append("[ ");

			Set<String> tupleSet = trIdsByVcfentry.get(ve);
			for (String trId : tupleSet)
				sb.append("'" + trId + "' ");

			sb.append("]\n");
		}

		return sb.toString();
	}

}
