package org.snpeff.annotate;

import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.vcf.VcfEntry;

public class VcfHaplotypeTuple {

	VcfEntry vcfEntry;
	Variant variant;
	VariantEffect variantEffect;
	Transcript transcript;

	public VcfHaplotypeTuple(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		vcfEntry = ve;
		this.variant = variant;
		this.variantEffect = variantEffect;
		transcript = variantEffect.getTranscript();
	}

	public int getCodonNum() {
		return variantEffect.getCodonNum();
	}

	public Transcript getTranscript() {
		return variantEffect.getTranscript();
	}

	public String getTrCodonKey() {
		return getTranscript().getId() + "\t" + getCodonNum();
	}

	public Variant getVariant() {
		return variant;
	}

	public VariantEffect getVariantEffect() {
		return variantEffect;
	}

	public VcfEntry getVcfEntry() {
		return vcfEntry;
	}

	public boolean hasTranscript() {
		return transcript != null;
	}

	@Override
	public String toString() {
		return vcfEntry.toStr() //
				+ "," + variant //
				+ "," + (hasTranscript() ? transcript.getId() : "null")//
				+ ",codon:" + variantEffect.getCodonNum() //
		;
	}

}
