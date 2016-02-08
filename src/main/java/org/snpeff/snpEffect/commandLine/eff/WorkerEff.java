package org.snpeff.snpEffect.commandLine.eff;

import org.snpeff.akka.vcfStr.WorkerVcfStr;
import org.snpeff.interval.Variant;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.outputFormatter.OutputFormatter;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.vcf.VcfEntry;

/**
 * Worker agent for SnpEff 'eff' command
 *
 * @author pablocingolani
 */
public class WorkerEff extends WorkerVcfStr {

	SnpEffCmdEff snpEffCmdEff; // Used only to show errors
	SnpEffectPredictor snpEffectPredictor; // Predictor
	OutputFormatter outputFormatter; // Output format
	IntervalForest filterIntervals; // Filter only seqChanges that match these intervals

	public WorkerEff(SnpEffCmdEff snpEffCmdEff, SnpEffectPredictor snpEffectPredictor, OutputFormatter outputFormatter, IntervalForest filterIntervals) {
		super();
		this.snpEffCmdEff = snpEffCmdEff;
		this.snpEffectPredictor = snpEffectPredictor;
		this.outputFormatter = outputFormatter;
		this.filterIntervals = filterIntervals;
	}

	@Override
	public String calculate(VcfEntry vcfEntry) {
		if (vcfEntry == null) return null;

		try {
			// We set the master (and VcfFileIterator) 'parseNow=false', so the VcfEntry should be 'unparsed' at this moment.
			// The idea is that the workers can do this, relieving the master from this task.
			vcfEntry.parse();

			// Skip if there are filter intervals and they are not matched
			if ((filterIntervals != null) && (filterIntervals.query(vcfEntry).isEmpty())) return null;

			// Create new 'section'
			outputFormatter.startSection(vcfEntry);

			for (Variant variant : vcfEntry.variants()) {
				if (variant.isVariant()) {
					// Calculate effects
					VariantEffects variantEffects = snpEffectPredictor.variantEffect(variant);

					// Create new 'section'
					outputFormatter.startSection(variant);

					// Show results
					for (VariantEffect changeEffect : variantEffects)
						outputFormatter.add(changeEffect);

					// Finish up this section
					outputFormatter.endSection(variant);
				}
			}

			// Finish up this section
			outputFormatter.endSection(vcfEntry);

		} catch (Throwable t) {
			snpEffCmdEff.error(t, "Error while processing VCF entry (line " + vcfEntry.getLineNum() + ") :\n\t" + vcfEntry);
			t.printStackTrace();
		}

		return vcfEntry.toString();
	}

}
