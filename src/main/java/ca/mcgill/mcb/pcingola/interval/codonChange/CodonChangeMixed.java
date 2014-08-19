package ca.mcgill.mcb.pcingola.interval.codonChange;

import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Calculate codon changes produced by a 'mixed' variant
 *
 * Essetially every 'mixed' variant can be represented as a concatenation of a SNP/MNP + an INS/DEL
 *
 * @author pcingola
 */
public class CodonChangeMixed extends CodonChangeMnp {

	public static boolean debug = false;

	int oldCodonCdsStart = -1;
	int oldCodonCdsEnd = -1;

	Variant mnp;
	Variant indel;
	CodonChange codonChangeMnp;
	CodonChange codonChangeIndel;
	VariantEffects variantEffectsOri;

	public CodonChangeMixed(Variant variant, Transcript transcript, VariantEffects variantEffects) {
		super(variant, transcript, variantEffects);
		returnNow = false;
		requireNetCdsChange = false;

		// Decompose mixed variant into 'SNP/MNP' + 'INS/DEL'
		int minLen = Math.min(variant.getReference().length(), variant.getAlt().length());

		String ref = variant.getReference();
		String refMnp = ref.substring(0, minLen);
		String refIndel = ref.substring(minLen);

		String alt = variant.getAlt();
		String altMnp = alt.substring(0, minLen);
		String altIndel = alt.substring(minLen);

		mnp = new Variant(variant.getChromosome(), variant.getStart(), refMnp, altMnp, variant.getId());
		indel = new Variant(variant.getChromosome(), variant.getStart() + minLen, refIndel, altIndel, variant.getId());

		// Create codon changes
		variantEffectsOri = variantEffects;
		this.variantEffects = new VariantEffects(variant);
		codonChangeMnp = CodonChange.factory(mnp, transcript, this.variantEffects);
		codonChangeIndel = CodonChange.factory(indel, transcript, this.variantEffects);
	}

	@Override
	public void codonChange() {
		codonOldNew();

		codonChangeMnp.codonChange();
		codonChangeIndel.codonChange();

		// Set highest impact variant effect
		if (variantEffects.isEmpty()) return; // Nothing to do

		variantEffects.sort();
		VariantEffect varEff = variantEffects.get(0);

		if (debug) {
			Gpr.debug("Mixed variant:" + variant + "\n\t\tSNP/MNP : " + mnp + "\n\t\tInDel   : " + indel + "\n\t\tEffects : ");
			for (VariantEffect ve : variantEffects)
				System.err.println("\t\t\t" + ve.toStringSimple(true));
		}

		// Add main effect
		varEff = effect(varEff.getMarker(), varEff.getEffectType(), "", codonsOld, codonsNew, codonStartNum, codonStartIndex, false);

		// Add 'additional' effects
		for (int i = 0; i < variantEffects.size(); i++) {
			List<EffectType> effTypes = variantEffects.get(i).getEffectTypes();
			for (int j = 0; j < effTypes.size(); j++) {
				EffectType effType = effTypes.get(j);
				if (!varEff.hasEffectType(effType)) varEff.addEffectType(effType);
			}
		}

		variantEffectsOri.addEffect(varEff);
	}

	void codonNum() {
		if (transcript.isStrandPlus()) {
			codonStartNum = codonChangeMnp.codonStartNum;
			codonStartIndex = codonChangeMnp.codonStartIndex;
		} else {
			codonStartNum = codonChangeIndel.codonStartNum;
			codonStartIndex = codonChangeIndel.codonStartIndex;
		}
	}

	//	/**
	//	 * Get new (modified) codons
	//	 */
	//	@Override
	//	public String codonsNew() {
	//		if (transcript.isStrandPlus()) return codonChangeMnp.codonsNew() + codonChangeIndel.codonsNew();
	//		return codonChangeIndel.codonsNew() + codonChangeMnp.codonsNew();
	//	}
	//
	//	/**
	//	 * Get original codons in CDS
	//	 */
	//	@Override
	//	public String codonsOld() {
	//		if (transcript.isStrandPlus()) return codonChangeMnp.codonsOld() + codonChangeIndel.codonsOld();
	//		return codonChangeIndel.codonsOld() + codonChangeMnp.codonsOld();
	//	}

}
