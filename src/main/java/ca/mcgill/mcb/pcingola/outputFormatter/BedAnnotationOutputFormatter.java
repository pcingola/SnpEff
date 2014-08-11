package ca.mcgill.mcb.pcingola.outputFormatter;

import java.util.HashSet;

import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;

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
		Variant seqChange = (Variant) section;
		String variantName = seqChange.getChromosomeName() + ":" + (seqChange.getStart() + outOffset);

		// Show results
		HashSet<String> chEffs = new HashSet<String>();
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
						type += "|" + r.getName() + "|" + r.getCellType();
					}

					// Add BED line
					ann = m.getChromosomeName() + "\t" // 
							+ "\t" + (m.getStart() + outOffset) //
							+ "\t" + (m.getEnd() + outOffset + 1) //
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
