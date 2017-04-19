package org.snpeff.annotate;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Detects variants / vcfEntries affecting the same codon (in the same transcript)
 *
 * @author pcingola
 */
public abstract class HaplotypeDetectorBase extends HaplotypeAnnotationDetector {

	Set<EffectType> supportedEffectTypes;
	VcfTuple latestVcfHaplotypeTuple;
	AutoHashMap<String, VcfTupleSet> tuplesByTr;
	AutoHashMap<VcfEntry, VcfTupleSet> tuplesByVcfentry;
	AutoHashMap<VcfEntry, Set<String>> trIdsByVcfentry;

	public HaplotypeDetectorBase() {
		reset();
	}

	/**
	 * Add a <vcfEntry, variant, veff> tuple
	 */
	@Override
	public void add(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		latestVcfHaplotypeTuple = new VcfTuple(ve, variant, variantEffect);

		// Always add transcript Ids
		addTrIds(ve, variantEffect);

		// Only add if it's a valid variant & varintEffect
		if (isValidVariant(variant) && isValidVariantEffect(variantEffect)) {
			add(latestVcfHaplotypeTuple);
		}
	}

	protected void add(VcfTuple vht) {
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
	abstract protected boolean checkTranscript(VcfTuple vht1, VcfTuple vht2);

	/**
	 * Return the variants within the same haplotype (for this detector)
	 */
	@Override
	public Set<Variant> haplotype(Variant var, Transcript tr) {
		VcfTupleSet tuples = tuplesByTr.get(tr.getId());
		if (tuples == null) return null;

		return tuples.stream() //
				.peek(vht -> System.out.println("vht:" + vht)) //
				.filter(vht -> vht != null) //
				//				.map(vht -> vht.getVcfEntry()) //
				.map(vht -> vht.getVariant()) //
				.collect(Collectors.toSet()) //
		;
	}

	/**
	 * Does this vcfEntry have any 'haplotype annotation' variants associated?
	 */
	@Override
	public boolean hasHaplotypeAnnotation(VcfEntry ve) {
		VcfTupleSet tupleSetVe = tuplesByVcfentry.get(ve);
		if (tupleSetVe == null) return false;

		// Look tuples by transcript
		for (VcfTuple vht : tupleSetVe) {
			String trid = vht.getTrId();
			VcfTupleSet tupleSetTr = tuplesByTr.get(trid);
			if (hasHaplotypeAnnotation(tupleSetVe, tupleSetTr)) return true;
		}

		return false;
	}

	/**
	 * Does this set have a 'haplotype annotation' variant?
	 */
	protected boolean hasHaplotypeAnnotation(VcfTupleSet tupleSetVe, VcfTupleSet tupleSetTr) {
		if (tupleSetTr == null || tupleSetVe == null) return false;

		for (VcfTuple vht1 : tupleSetVe) {
			VcfEntry ve1 = vht1.getVcfEntry();
			for (VcfTuple vht2 : tupleSetTr) {
				VcfEntry ve2 = vht2.getVcfEntry();
				if (ve1.compareTo(ve2) == 0) continue; // Don't compare to self
				if (checkTranscript(vht1, vht2) && vht1.isHaplotypeWith(vht2)) return true;
			}
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
		VcfTupleSet tupleSet = tuplesByVcfentry.get(ve);
		if (tupleSet == null) return true;

		// The latestVcfEntry does not have any transcript IDs?
		// Then it is not in the same haplotype
		if (!hasSameTrIdAsLatest(ve)) return true;

		// The latestVcfEntry does not have any tuples?
		// Then we don't know whether it's in the same haplotype or not
		// e.g. latestVcfEntry is in an intron but the next vcfEntry is
		// in the an exon within the same codon as 've'
		VcfTupleSet tupleSetLatest = tuplesByVcfentry.get(latestVcfHaplotypeTuple.getVcfEntry());
		if (tupleSetLatest == null) return false;

		// Does 've' share any transcript with the latest vcfEntry?
		for (VcfTuple vht1 : tupleSetLatest)
			for (VcfTuple vht2 : tupleSet)
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
		VcfTupleSet tupleSet = tuplesByVcfentry.remove(ve);
		if (tupleSet == null) return;

		// Remove from 'tuplesByTrCodon'
		// Remove all VcfHaplotypeTuple associated with this 'vcfEntry'
		for (VcfTuple vht : tupleSet) {
			String key = vht.getTrId();

			// Remove vht from this set
			VcfTupleSet tset = tuplesByTr.get(key);
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
		tuplesByTr = new AutoHashMap<>(new VcfTupleSet());
		tuplesByVcfentry = new AutoHashMap<>(new VcfTupleSet());
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

			VcfTupleSet tupleSet = tuplesByTr.get(key);
			for (VcfTuple vht : tupleSet)
				sb.append("'" + vht + "' ");

			sb.append("]\n");
		}

		sb.append("\ttuplesByVcfentry.size:" + trIdsByVcfentry.size() + ":\n");
		for (VcfEntry ve : trIdsByVcfentry.keySet()) {
			sb.append("\t\t" + ve.toStr() + ": ");
			sb.append("[ ");

			VcfTupleSet tupleSet = tuplesByVcfentry.get(ve);
			for (VcfTuple vht : tupleSet)
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
