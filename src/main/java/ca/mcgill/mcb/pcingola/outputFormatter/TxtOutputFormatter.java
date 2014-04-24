package ca.mcgill.mcb.pcingola.outputFormatter;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.SeqChange.ChangeType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;

/**
 * Formats output as TXT
 * 
 * @author pcingola
 */
public class TxtOutputFormatter extends OutputFormatter {

	public TxtOutputFormatter() {
		super();
	}

	/**
	 * Finish up section
	 * @param marker
	 */
	@Override
	public String endSection(Marker marker) {
		// Ignore other markers (e.g. seqChanges)
		if (marker instanceof SeqChange) return super.endSection(marker);
		return null;
	}

	@Override
	public void startSection(Marker marker) {
		// Ignore other markers (e.g. seqChanges)
		if (marker instanceof SeqChange) super.startSection(marker);
	}

	/**
	 * Show all effects
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		SeqChange seqChange = (SeqChange) section;

		// Show results
		for (ChangeEffect changeEffect : changeEffects) {
			// If it is not filtered out by changeEffectResutFilter  => Show it
			if ((changeEffectResutFilter == null) || (!changeEffectResutFilter.filter(changeEffect))) {
				String hh = "";
				if (seqChange.isHomozygous()) hh = "Hom";
				else if (seqChange.isHeterozygous()) hh = "Het";

				String qual = (seqChange.getQuality() >= 0 ? Double.toString(seqChange.getQuality()) : "");
				String cov = (seqChange.getCoverage() >= 0 ? Integer.toString(seqChange.getCoverage()) : "");

				if (qual.endsWith(".0")) qual = qual.substring(0, qual.length() - 2);

				int start = seqChange.getStart() + outOffset;
				int end = seqChange.getEnd() + outOffset;
				sb.append(chrStr + seqChange.getChromosomeName() //
						+ "\t" + start + ((seqChange.getChangeType() == ChangeType.Interval) ? "-" + end : "") // Only when showing intervals
						+ "\t" + seqChange.reference() //
						+ "\t" + seqChange.change() //  
						+ "\t" + seqChange.getChangeType() //
						+ "\t" + hh // Homo Hetero info
						+ "\t" + qual // Quality
						+ "\t" + cov // Coverage
						+ "\t" + changeEffect.toString(useSequenceOntology, useHgvs) // Sequence change result
						+ "\n" //
				);
			}
		}

		if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1); // Delete trailing '\n'
		return sb.toString();
	}

	/**
	 * Show header
	 */
	@Override
	public String toStringHeader() {
		String header = "# SnpEff version " + version + "\n" //
				+ "# Command line: " + commandLineStr + "\n" //
				+ "# Chromo\tPosition\tReference\tChange\tChange_type\tHomozygous\tQuality\tCoverage\t" + (new ChangeEffect(null)).header();

		return header;
	}
}
