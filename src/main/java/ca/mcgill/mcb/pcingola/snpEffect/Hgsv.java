package ca.mcgill.mcb.pcingola.snpEffect;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * HGSV notation
 * 
 * References: http://www.hgvs.org/
 * 
 * @author pcingola
 */
public class Hgsv {

	protected ChangeEffect changeEffect;
	SeqChange seqChange;
	Marker marker;
	Transcript tr;

	public Hgsv(ChangeEffect changeEffect) {
		this.changeEffect = changeEffect;
		seqChange = changeEffect.getSeqChange();
		marker = changeEffect.getMarker();
		tr = changeEffect.getTranscript();
	}

	protected String baseChange() {
		if (marker.isStrandPlus()) return seqChange.getReference() + ">" + seqChange.getChange();
		return GprSeq.wc(seqChange.getReference()) + ">" + GprSeq.wc(seqChange.getChange());
	}

	protected String codingPrefix() {
		return (tr.isProteinCoding() ? "c." : "n.");
	}

}
