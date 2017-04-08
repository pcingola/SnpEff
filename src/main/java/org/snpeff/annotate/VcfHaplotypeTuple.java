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
	int aaStart, aaEnd;

	public VcfHaplotypeTuple(VcfEntry ve, Variant variant, VariantEffect variantEffect) {
		vcfEntry = ve;
		this.variant = variant;
		this.variantEffect = variantEffect;
		transcript = variantEffect.getTranscript();

		if (hasTranscript()) {
			aaStart = variantEffect.getCodonNum();
			if (variant.isSnp()) aaEnd = aaStart;
			else {
				String aaRef = variantEffect.getAaRef();
				aaEnd = aaStart + aaRef.length() - 1;
			}
		} else {
			aaStart = aaEnd = -1;
		}
	}

	/**
	 * Do the amino acid numbers intersect
	 */
	public boolean aaIntersect(VcfHaplotypeTuple vht) {
		return getTrId().equals(vht.getTrId()) // Same transcript
				&& (aaStart <= vht.aaEnd) && (aaEnd >= vht.aaStart) // AA intervals intersect?
		;
	}

	public int getCodonNum() {
		return variantEffect.getCodonNum();
	}

	public Transcript getTranscript() {
		return variantEffect.getTranscript();
	}

	public String getTrId() {
		return transcript.getId();
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
				+ ",codon" + (aaStart == aaEnd ? ": " + aaStart : "s : [" + aaStart + ", " + aaEnd + "]") //
		;
	}

}
