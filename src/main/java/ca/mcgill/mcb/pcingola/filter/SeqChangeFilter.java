package ca.mcgill.mcb.pcingola.filter;

import ca.mcgill.mcb.pcingola.interval.SeqChange;

/**
 * A Generic SeqChenge filter
 * 
 * @author pcingola
 */
public class SeqChangeFilter implements Filter<SeqChange> {

	Integer minQuality, maxQuality;
	Integer minCoverage, maxCoverage;
	Boolean heterozygous;
	SeqChange.ChangeType changeType;

	public SeqChangeFilter() {
		minCoverage = maxCoverage = minQuality = maxQuality = null;
		heterozygous = null;
		changeType = null;
	}

	public SeqChangeFilter(Integer minQuality, Integer maxQuality, Integer minCoverage, Integer maxCoverage) {
		this.minCoverage = minCoverage;
		this.maxCoverage = maxCoverage;
		this.minQuality = minQuality;
		this.maxQuality = maxQuality;
	}

	@Override
	public boolean anythingSet() {
		return (minQuality != null) || (maxQuality != null) //
				|| (minCoverage != null) || (maxCoverage != null) //
				|| (heterozygous != null) //
				|| (changeType != null);
	}

	/**
	 * Filter a sequence change
	 * @return true if it passes the filter
	 */
	@Override
	public boolean filter(SeqChange seqChange) {
		// Quality
		if( (minQuality != null) && (seqChange.getQuality() < minQuality) ) return false;
		if( (maxQuality != null) && (seqChange.getQuality() > maxQuality) ) return false;

		// Coverage
		if( (minCoverage != null) && (seqChange.getCoverage() < minCoverage) ) return false;
		if( (maxCoverage != null) && (seqChange.getCoverage() > maxCoverage) ) return false;

		// Homo-Hetero
		if( (heterozygous != null) && (seqChange.isHeterozygous() != heterozygous) ) return false;

		// Change type
		if( (changeType != null) && (seqChange.getChangeType() != changeType) ) return false;

		//		// Filter pass data
		//		if( (filterPass != null) ) return filterPass == seqChange.getFilterPass();

		return true;
	}

	public SeqChange.ChangeType getChangeType() {
		return changeType;
	}

	public void setChangeType(SeqChange.ChangeType changeType) {
		this.changeType = changeType;
	}

	public void setHeterozygous(Boolean heterozygous) {
		this.heterozygous = heterozygous;
	}

	public void setMaxCoverage(Integer maxCoverage) {
		this.maxCoverage = maxCoverage;
	}

	public void setMaxQuality(Integer maxQuality) {
		this.maxQuality = maxQuality;
	}

	public void setMinCoverage(Integer minCoverage) {
		this.minCoverage = minCoverage;
	}

	public void setMinQuality(Integer minQuality) {
		this.minQuality = minQuality;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// Quality
		if( minQuality != null ) sb.append((sb.length() > 0 ? " AND " : "") + "( Quality >= " + minQuality + ")");
		if( maxQuality != null ) sb.append((sb.length() > 0 ? " AND " : "") + "( Quality < " + maxQuality + ")");

		// Coverage
		if( minCoverage != null ) sb.append((sb.length() > 0 ? " AND " : "") + "( Coverage >= " + minCoverage + ")");
		if( maxCoverage != null ) sb.append((sb.length() > 0 ? " AND " : "") + "( Coverage >= " + maxCoverage + ")");

		// Homo-Hetero
		if( heterozygous != null ) sb.append((sb.length() > 0 ? " AND " : "") + "( Heterozygous = true ) ");

		// Change type
		if( changeType != null ) sb.append((sb.length() > 0 ? " AND " : "") + "( ChangeType >= " + changeType + ")");

		return sb.toString();
	}
}
