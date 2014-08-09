package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
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

	public CodonChangeMixed(Variant variant, Transcript transcript, VariantEffects changeEffects) {
		super(variant, transcript, changeEffects);
		returnNow = false;
		requireNetCdsChange = false;

		// Decompose mixed variant into 'SNP/MNP' + 'INS/DEL'
		int minLen = Math.min(variant.getReference().length(), variant.getAlt().length());
		mnp = new Variant(variant.getChromosome(), variant.getStart(), variant.getReference().substring(0, minLen), variant.getAlt().substring(0, minLen), variant.getId());
		indel = new Variant(variant.getChromosome(), variant.getStart() + minLen, variant.getReference().substring(minLen), variant.getAlt().substring(minLen), variant.getId());

		// Create codon changes
		codonChangeMnp = CodonChange.factory(mnp, transcript, variantEffects);
		codonChangeIndel = CodonChange.factory(indel, transcript, variantEffects);
	}

	@Override
	protected void codonChange() {
		codonChangeMnp.codonChange();
		codonChangeIndel.codonChange();
	}

	/**
	 * Get new (modified) codons
	 */
	@Override
	String codonsNew() {
		String codons = codonChangeMnp.codonsNew();
		codons += codonChangeIndel.codonsNew();
		return codons;
	}

	/**
	 * Get original codons in CDS
	 */
	@Override
	public String codonsOld() {
		String codons = codonChangeMnp.codonsOld();
		codons += codonChangeIndel.codonsOld();
		return codons;
	}

}
