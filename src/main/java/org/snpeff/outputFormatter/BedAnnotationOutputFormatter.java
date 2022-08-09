package org.snpeff.outputFormatter;

import java.util.HashSet;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Regulation;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.VariantEffect;

/**
 * Formats: Show all annotations that intersect the BED input file.
 *
 * WARNING: In this format, the output are annotations (instead of input intervals)
 *
 * @author pcingola
 */
public class BedAnnotationOutputFormatter extends BedOutputFormatter {

	public BedAnnotationOutputFormatter() {
		super();
		outOffset = 0; // Bed format is zero-based
	}

	/**
	 * Show all effects
	 */
	@Override
	public String toString() {
		Variant variant = (Variant) section;
		String variantName = variant.getChromosomeName() + ":" + (variant.getStart() + outOffset);

		// Show results
		HashSet<String> chEffs = new HashSet<>();
		for (VariantEffect changeEffect : variantEffects) {
			// If it is not filtered out by changeEffectResutFilter  => Show it
			if ((variantEffectResutFilter == null) || (!variantEffectResutFilter.filter(changeEffect))) {
				String ann = null;

				Marker m = changeEffect.getMarker();
				if (m != null) {
					// Get gene name (if any)
					String geneName = null;
					Gene gene = changeEffect.getGene();
					if (gene != null) geneName = (useGeneId ? gene.getId() : gene.getGeneName());

					// Get annotation type
					String type = m.getType().toString();

					// Show complete regulation info
					if (m instanceof Regulation) {
						Regulation r = (Regulation) m;
						type += "|" + r.getName() + "|" + r.getRegulationType();
					}

					// Add BED line
					ann = m.getChromosomeName() + "\t" //
							+ "\t" + (m.getStart() + outOffset) //
							+ "\t" + (m.getEndClosed() + outOffset + 1) //
							+ "\t" + variantName + ";" + type //
							+ (geneName != null ? ":" + geneName : "") //
					;
				}

				if (ann != null) chEffs.add(ann);
			}

		}

		// Show all
		StringBuilder sb = new StringBuilder();
		for (String chEff : chEffs)
			sb.append(chEff + "\n");

		return sb.toString();
	}

	/**
	 * Show header
	 */
	@Override
	public String toStringHeader() {
		return "# SnpEff version " + version + "\n" //
				+ "# Command line: " + commandLineStr + "\n" //
				+ "# Chromo\tStart\tEnd\tVariant;Annotation\tScore";
	}
}
