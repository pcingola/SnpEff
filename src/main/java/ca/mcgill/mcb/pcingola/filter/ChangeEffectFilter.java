package ca.mcgill.mcb.pcingola.filter;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;

/**
 * A Generic ChangeEffect filter
 * 
 * @author pcingola
 */
public class ChangeEffectFilter implements Filter<ChangeEffect> {

	boolean downstream = false;
	boolean frameShift = false;
	boolean intergenic = false;
	boolean intron = false;
	boolean upstream = false;
	boolean utr = false;

	public ChangeEffectFilter() {}

	/** 
	 * Is any of the options set?
	 * @return
	 */
	@Override
	public boolean anythingSet() {
		return downstream || frameShift || intergenic || intron || upstream || utr;
	}

	/**
	 * Filter a sequence change
	 * @return true if it passes the filter
	 */
	@Override
	public boolean filter(ChangeEffect changeEffectResut) {
		if( downstream && changeEffectResut.isDownstream() ) return true;
		if( frameShift && changeEffectResut.isFrameShift() ) return true;
		if( intergenic && changeEffectResut.isIntergenic() ) return true;
		if( intron && changeEffectResut.isIntron() ) return true;
		if( upstream && changeEffectResut.isUpstream() ) return true;
		if( utr && changeEffectResut.isUtr() ) return true;
		return false;
	}

	public boolean isDownstream() {
		return downstream;
	}

	public boolean isFrameShift() {
		return frameShift;
	}

	public boolean isIntergenic() {
		return intergenic;
	}

	public boolean isIntron() {
		return intron;
	}

	public boolean isUpstream() {
		return upstream;
	}

	public boolean isUtr() {
		return utr;
	}

	public void setDownstream(boolean downstream) {
		this.downstream = downstream;
	}

	public void setFrameShift(boolean frameShift) {
		this.frameShift = frameShift;
	}

	public void setIntergenic(boolean intergenic) {
		this.intergenic = intergenic;
	}

	public void setIntron(boolean intron) {
		this.intron = intron;
	}

	public void setUpstream(boolean upstream) {
		this.upstream = upstream;
	}

	public void setUtr(boolean utr) {
		this.utr = utr;
	}
}
