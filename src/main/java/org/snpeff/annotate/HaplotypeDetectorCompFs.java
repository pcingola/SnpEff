package org.snpeff.annotate;

import java.util.HashSet;

import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;

/**
 * Detects variants / vcfEntries creating compensating
 * frame-shifts (within the same transcript)
 *
 * @author pcingola
 */
public class HaplotypeDetectorCompFs extends HaplotypeDetectorBase {

	public static final EffectType SUPPORTED_EFFECTS[] = { //
			EffectType.CODON_CHANGE_PLUS_CODON_DELETION //
			, EffectType.CODON_CHANGE_PLUS_CODON_INSERTION //
			, EffectType.CODON_DELETION //
			, EffectType.CODON_INSERTION //
			, EffectType.FRAME_SHIFT //
	};

	public HaplotypeDetectorCompFs() {
		super();
	}

	@Override
	protected boolean checkTranscript(VcfTuple vht1, VcfTuple vht2) {
		return vht1.sameTr(vht2);
	}

	@Override
	void initSUpportedEffectTypes() {
		supportedEffectTypes = new HashSet<>();
		for (EffectType et : HaplotypeDetectorCompFs.SUPPORTED_EFFECTS)
			supportedEffectTypes.add(et);
	}

	@Override
	protected boolean isValidVariant(Variant variant) {
		return variant.isInDel() || variant.isMixed();
	}

}
