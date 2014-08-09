package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a 'mixed' variant
 *
 * Essetially every 'mixed' variant can be represented as a concatenation of a SNP/MNP + an INS/DEL
 *
 * @author pcingola
 */
public class CodonChangeMixed extends CodonChange {

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
	protected void codonChange() {
		codonChangeMnp.codonChange();
		codonChangeIndel.codonChange();

		codonsNew = codonsNew();
		codonsOld = codonsOld();

		codonNum();
		codonNum = codonChangeMnp.codonNum;
		codonIndex = codonChangeMnp.codonIndex;

		// Set highest impact variant effect
		if (variantEffects.isEmpty()) return; // Nothing to do

		variantEffects.sort();
		VariantEffect varEff = variantEffects.get(0);
		variantEffectsOri.add(varEff);
		variantEffectsOri.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
	}

	void codonNum() {
		if (transcript.isStrandPlus()) {
			codonNum = codonChangeMnp.codonNum;
			codonIndex = codonChangeMnp.codonIndex;
		} else {
			codonNum = codonChangeIndel.codonNum;
			codonIndex = codonChangeIndel.codonIndex;
		}
	}

	/**
	 * Get new (modified) codons
	 */
	@Override
	public String codonsNew() {
		if (transcript.isStrandPlus()) return codonChangeMnp.codonsNew() + codonChangeIndel.codonsNew();
		return codonChangeIndel.codonsNew() + codonChangeMnp.codonsNew();
	}

	/**
	 * Get original codons in CDS
	 */
	@Override
	public String codonsOld() {
		if (transcript.isStrandPlus()) return codonChangeMnp.codonsOld() + codonChangeIndel.codonsOld();
		return codonChangeIndel.codonsOld() + codonChangeMnp.codonsOld();
	}

}
