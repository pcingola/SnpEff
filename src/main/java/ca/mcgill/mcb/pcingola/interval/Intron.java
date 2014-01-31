package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

/**
 * Intron
 * 
 * @author pcingola
 */
public class Intron extends Marker {

	private static final long serialVersionUID = -8283322526157264389L;

	int rank; // Exon rank in transcript
	Exon exonBefore; // Exon before this intron
	Exon exonAfter; // Exon after this intron

	public Intron(Transcript parent, int start, int end, int strand, String id, Exon exonBefore, Exon exonAfter) {
		super(parent, start, end, strand, id);
		type = EffectType.INTRON;
		this.exonAfter = exonAfter;
		this.exonBefore = exonBefore;
	}

	public Exon getExonAfter() {
		return exonAfter;
	}

	public Exon getExonBefore() {
		return exonBefore;
	}

	public int getRank() {
		return rank;
	}

	public String getSpliceType() {
		return (exonBefore != null ? exonBefore.getSpliceType() : "") //
				+ "-" //
				+ (exonAfter != null ? exonAfter.getSpliceType() : "") //
		;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

}
