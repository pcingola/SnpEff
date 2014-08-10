package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Interval for a UTR (5 prime UTR and 3 prime UTR
 * 
 * @author pcingola
 *
 */
public class Utr3prime extends Utr {

	private static final long serialVersionUID = 5688641008301281991L;

	public Utr3prime() {
		super();
		type = EffectType.UTR_3_PRIME;
	}

	public Utr3prime(Exon parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.UTR_3_PRIME;
	}

	@Override
	public boolean isUtr3prime() {
		return true;
	}

	@Override
	public boolean isUtr5prime() {
		return false;
	}

	@Override
	public boolean variantEffect(Variant seqChange, VariantEffects changeEffects) {
		if (!intersects(seqChange)) return false;

		if (seqChange.includes(this) && (seqChange.getVariantType() == VariantType.DEL)) {
			changeEffects.add(this, EffectType.UTR_3_DELETED, ""); // A UTR was removed entirely
			return true;
		}

		Transcript tr = (Transcript) findParent(Transcript.class);
		int dist = utrDistance(seqChange, tr);
		changeEffects.add(this, type, dist >= 0 ? dist + " bases from CDS" : "");
		if (dist >= 0) changeEffects.setDistance(dist);

		return true;
	}

	/**
	 * Calculate distance from beginning of 3'UTRs
	 * 
	 * @param snp
	 * @param utr
	 * @return
	 */
	@Override
	int utrDistance(Variant seqChange, Transcript tr) {
		int cdsEnd = tr.getCdsEnd();
		if (cdsEnd < 0) return -1;

		if (isStrandPlus()) return seqChange.getStart() - cdsEnd;
		return cdsEnd - seqChange.getEnd();
	}

}
