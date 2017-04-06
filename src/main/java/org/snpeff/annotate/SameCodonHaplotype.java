package org.snpeff.annotate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.snpeff.collections.AutoHashMap;
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
public class SameCodonHaplotype {

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

	AutoHashMap<String, HashSet<VcfHaplotypeTuple>> tuplesByTrCodon;
	AutoHashMap<VcfEntry, HashSet<VcfHaplotypeTuple>> tuplesByVcfentry;
	boolean keepAll;

	public SameCodonHaplotype() {
		reset();
	}

	/**
	 * Add a <vcfEntry, variant, veff> tuple
	 */
	public void add(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
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

	/**
	 * Are these genotypes phased?
	 */
	boolean arePhased(VcfGenotype gt1, VcfGenotype gt2) {
		// Are the variants phased?
		if (!isPhased(gt1) || !isPhased(gt2)) return false;

		// Do they have phase groups?
		if (!samePhaseGroup(gt1, gt2)) return false;

		// Check that both are ALT at the same chromosome (maternal / paternal)
		int geno1[] = gt1.getGenotype();
		int geno2[] = gt1.getGenotype();
		int min = Math.min(geno1.length, geno2.length);
		for (int i = 0; i < min; i++) {
			if (geno1[i] > 0 && geno2[i] > 0) return true;
		}

		return false;
	}

	/**
	 * Is there any kind of phasing information?
	 */
	public boolean hasPhase(VcfEntry vcfEntry) {
		List<VcfGenotype> gts = vcfEntry.getVcfGenotypes();
		for (VcfGenotype gt : gts) {
			if (gt.isPhased() || gt.isHomozygousAlt()) { // Homozygous ALT means implicit phasing
				return true;
			}
		}
		return false;
	}

	/**
	 * Does this set have a 'same codon' variant?
	 */
	boolean hasSameCodon(Set<VcfHaplotypeTuple> tupleSetVe, Set<VcfHaplotypeTuple> tupleSetCodon) {
		if (tupleSetCodon == null || tupleSetCodon.size() <= 1) return false;

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

	/**
	 * Does this vcfEntry have any 'same codon' variants associated?
	 */
	public boolean hasSameCodon(VcfEntry ve) {
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

	/**
	 * Are these genotypes phased?
	 */
	boolean isPhased(VcfGenotype gt) {
		return gt.isPhased() || gt.isHomozygousAlt();
	}

	/**
	 * Remove a VcfEntry
	 */
	public void remove(VcfEntry ve) {
		if (keepAll) return; // Keep all entries (used for test cases and debugging)

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

	/**
	 * Are these genotypes in the same phase group?
	 */
	boolean samePhaseGroup(VcfGenotype gt1, VcfGenotype gt2) {
		String ps1 = gt1.get(VcfGenotype.GT_FIELD_PHASE_GROUP);
		String ps2 = gt2.get(VcfGenotype.GT_FIELD_PHASE_GROUP);
		if (ps1 == null && ps2 == null) return true; // Both of them are empty? Consider them as match
		if (ps1 == null || ps2 == null) return false; // Only one of them is empty? Consider them as NO match
		return ps1.equals(ps2);
	}

	public void setKeepAll(boolean keepAll) {
		this.keepAll = keepAll;
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
