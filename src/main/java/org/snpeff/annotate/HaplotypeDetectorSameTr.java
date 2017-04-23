package org.snpeff.annotate;

import java.util.HashSet;

import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;

/**
 * Detects variants / vcfEntries in the same transcript
 *
 * Note: This is ONLY used for test cases (no practical application so far)
 *
 * @author pcingola
 */
public class HaplotypeDetectorSameTr extends HaplotypeDetectorBase {

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
			, EffectType.CODON_CHANGE_PLUS_CODON_DELETION //
			, EffectType.CODON_CHANGE_PLUS_CODON_INSERTION //
			, EffectType.CODON_DELETION //
			, EffectType.CODON_INSERTION //
			, EffectType.FRAME_SHIFT //
	};

	public HaplotypeDetectorSameTr() {
		super();
	}

	@Override
	protected boolean checkTranscript(VcfTuple vht1, VcfTuple vht2) {
		return vht1.sameTr(vht2);
	}

	@Override
	void initSUpportedEffectTypes() {
		supportedEffectTypes = new HashSet<>();
		for (EffectType et : HaplotypeDetectorSameTr.SUPPORTED_EFFECTS)
			supportedEffectTypes.add(et);
	}

	@Override
	protected boolean isValidVariant(Variant variant) {
		return variant.isSnp() || variant.isMnp() || variant.isInDel() || variant.isMixed();
	}

}
