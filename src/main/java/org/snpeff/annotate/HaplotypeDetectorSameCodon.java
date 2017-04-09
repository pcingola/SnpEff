package org.snpeff.annotate;

import java.util.HashSet;

import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.EffectType;

/**
 * Detects variants / vcfEntries affecting the same codon (in the same transcript)
 *
 * @author pcingola
 */
public class HaplotypeDetectorSameCodon extends HaplotypeDetectorBase {

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

	public HaplotypeDetectorSameCodon() {
		super();
	}

	@Override
	protected boolean checkTranscript(VcfHaplotypeTuple vht1, VcfHaplotypeTuple vht2) {
		return vht1.aaIntersect(vht2);
	}

	@Override
	void initSUpportedEffectTypes() {
		supportedEffectTypes = new HashSet<>();
		for (EffectType et : HaplotypeDetectorSameCodon.SUPPORTED_EFFECTS)
			supportedEffectTypes.add(et);
	}

	@Override
	protected boolean isValidVariant(Variant variant) {
		return variant.isSnp() || variant.isInDel() || variant.isMnp() || variant.isMixed();
	}

}
