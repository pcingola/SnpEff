package ca.mcgill.mcb.pcingola.interval;

import java.util.List;

import ca.mcgill.mcb.pcingola.interval.SeqChange.ChangeType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

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

	public Utr3prime(Exon parent, int start, int end, int strand, String id) {
		super(parent, start, end, strand, id);
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
	public List<ChangeEffect> seqChangeEffect(SeqChange seqChange, ChangeEffect changeEffect) {
		if (seqChange.includes(this) && (seqChange.getChangeType() == ChangeType.DEL)) {
			changeEffect.set(this, EffectType.UTR_3_DELETED, ""); // A UTR was removed entirely
			return changeEffect.newList();
		}

		Transcript tr = (Transcript) findParent(Transcript.class);
		String utrDistStr = utr5primeDistance(seqChange, tr);
		changeEffect.set(this, type, utrDistStr);

		Exon exon = (Exon) findParent(Exon.class);
		if (exon != null) exon.check(seqChange, changeEffect); // Check that base matches the expected one

		return changeEffect.newList();
	}

	/**
	 * Calculate distance from beginning of 3'UTRs
	 * 
	 * @param snp
	 * @param utr
	 * @return
	 */
	@Override
	String utr5primeDistance(SeqChange snp, Transcript tr) {
		List<Utr3prime> utrs = tr.get3primeUtrs();
		boolean fromEnd = strand < 0; // We want distance from end of transcript (beginning of 3'UTR)
		int dist = snp.distanceBases(utrs, fromEnd) + 1;
		return dist + " bases from CDS";
	}

}
