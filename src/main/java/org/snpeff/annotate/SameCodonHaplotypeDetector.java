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
public class SameCodonHaplotypeDetector extends HaplotypeAnnotationDetector {

	public static final EffectType SUPPORTED_EFFECTS[] = { //
			EffectType.CODON_CHANGE //
			, EffectType.CODON_CHANGE_PLUS_CODON_DELETION //
			, EffectType.CODON_CHANGE_PLUS_CODON_INSERTION //
			, EffectType.CODON_DELETION //
			, EffectType.CODON_INSERTION //
			, EffectType.FRAME_SHIFT //
			, EffectType.NON_SYNONYMOUS_CODING //
			, EffectType.NON_SYNONYMOUS_START //
			, EffectType.NON_SYNONYMOUS_STOP //
			, EffectType.START_LOST //
			, EffectType.STOP_GAINED //
			, EffectType.STOP_LOST //
			, EffectType.SYNONYMOUS_CODING //
			, EffectType.SYNONYMOUS_START //
			, EffectType.SYNONYMOUS_STOP //
	};

	private static final Set<EffectType> supportedEffectTypes;

	static {
		supportedEffectTypes = new HashSet<>();
		for (EffectType et : SUPPORTED_EFFECTS)
			supportedEffectTypes.add(et);
	}

	VcfEntry latestVcfEntry;
	AutoHashMap<String, HashSet<VcfHaplotypeTuple>> tuplesByTrCodon;
	AutoHashMap<VcfEntry, HashSet<VcfHaplotypeTuple>> tuplesByVcfentry;

	public SameCodonHaplotypeDetector() {
		reset();
	}

	/**
	 * Add a <vcfEntry, variant, veff> tuple
	 */
	@Override
	public void add(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		latestVcfEntry = ve;

		// Does it have a transcript and a codon number?
		if (variantEffect.getTranscript() == null || variantEffect.getCodonNum() < 0) return;

		// Effect type supported?
		EffectType effType = variantEffect.getEffectType();
		if (!supportedEffectTypes.contains(effType)) return;

		// Add
		VcfHaplotypeTuple vht = new VcfHaplotypeTuple(ve, variant, variantEffect);
		add(vht);
	}

	void add(VcfHaplotypeTuple vht) {
		tuplesByTrCodon.getOrCreate(vht.getTrCodonKey()).add(vht);
		tuplesByVcfentry.getOrCreate(vht.getVcfEntry()).add(vht);
	}

	@Override
	public Set<Variant> haplotype(Variant var, Transcript tr) {
		return null;
	}

	/**
	 * Does this vcfEntry have any 'same codon' variants associated?
	 */
	@Override
	public boolean hasHaplotypeAnnotation(VcfEntry ve) {
		Set<VcfHaplotypeTuple> tupleSetVe = tuplesByVcfentry.get(ve);
		if (tupleSetVe == null) return false;

		// Look tuples by codon
		Set<String> keysAnalyzed = new HashSet<>();
		for (VcfHaplotypeTuple vht : tupleSetVe) {
			String key = vht.getTrCodonKey();

			// If a transcript/codon has already been analyzed, we should skip it in the next iteration
			if (!keysAnalyzed.add(key)) continue;

			Set<VcfHaplotypeTuple> tupleSetCodon = tuplesByTrCodon.get(key);
			if (hasSameCodon(tupleSetVe, tupleSetCodon)) return true;
		}

		return false;
	}

	/**
	 * Does this set have a 'same codon' variant?
	 */
	boolean hasSameCodon(Set<VcfHaplotypeTuple> tupleSetVe, Set<VcfHaplotypeTuple> tupleSetCodon) {
		if (tupleSetCodon == null || tupleSetVe == null) return false;

		for (VcfHaplotypeTuple vht1 : tupleSetVe) {
			VcfEntry ve1 = vht1.getVcfEntry();
			for (VcfHaplotypeTuple vht2 : tupleSetCodon) {
				VcfEntry ve2 = vht2.getVcfEntry();
				if (ve1.compareTo(ve2) == 0) continue; // Don't compare to self
				if (hasSameCodon(vht1, vht2)) return true;
			}
		}

		return false;
	}

	boolean hasSameCodon(VcfHaplotypeTuple vht1, VcfHaplotypeTuple vht2) {
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

		Set<String> keysLatest = new HashSet<>();
		for (VcfHaplotypeTuple vht : tupleSetLatest)
			keysLatest.add(vht.getTrCodonKey());

		// Does 've' share any 'transcript / codon" with the latest vcfEntry?
		for (VcfHaplotypeTuple vht : tupleSet)
			if (keysLatest.contains(vht.getTrCodonKey())) {
				// Same codon on at least one transcript: 've' is nor free
				return false;
			}

		return true;
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
			String key = vht.getTrCodonKey();

			// Remove vht from this set
			Set<VcfHaplotypeTuple> tset = tuplesByTrCodon.get(key);
			if (tset != null) {
				tset.remove(vht);
				if (tset.isEmpty()) { // No more entries in set? Remove it
					tuplesByTrCodon.remove(key);
				}
			}
		}
	}

	void reset() {
		tuplesByTrCodon = new AutoHashMap<>(new HashSet<VcfHaplotypeTuple>());
		tuplesByVcfentry = new AutoHashMap<>(new HashSet<VcfHaplotypeTuple>());
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName() + ":\n");

		sb.append("\ttuplesByTrCodon.size:" + tuplesByTrCodon.size() + ":\n");
		for (String key : tuplesByTrCodon.keySet()) {
			sb.append("\t\t'" + key + "': ");
			sb.append("[ ");

			HashSet<VcfHaplotypeTuple> tupleSet = tuplesByTrCodon.get(key);
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
