package ca.mcgill.mcb.pcingola.snpEffect.commandLine.eff;

import ca.mcgill.mcb.pcingola.akka.vcfStr.WorkerVcfStr;
import ca.mcgill.mcb.pcingola.filter.SeqChangeFilter;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.outputFormatter.OutputFormatter;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

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
	SeqChangeFilter seqChangeFilter; // Filter each seqChange

	public WorkerEff(SnpEffCmdEff snpEffCmdEff, SnpEffectPredictor snpEffectPredictor, OutputFormatter outputFormatter, IntervalForest filterIntervals, SeqChangeFilter seqChangeFilter) {
		super();
		this.snpEffCmdEff = snpEffCmdEff;
		this.snpEffectPredictor = snpEffectPredictor;
		this.outputFormatter = outputFormatter;
		this.filterIntervals = filterIntervals;
		this.seqChangeFilter = seqChangeFilter;
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

			for (SeqChange seqChange : vcfEntry.seqChanges()) {
				// Does it pass the filter? => Analyze
				if ((seqChangeFilter == null) || seqChangeFilter.filter(seqChange)) {
					// Calculate effects
					ChangeEffects changeEffects = snpEffectPredictor.seqChangeEffect(seqChange);

					// Create new 'section'
					outputFormatter.startSection(seqChange);

					// Show results
					for (ChangeEffect changeEffect : changeEffects)
						outputFormatter.add(changeEffect);

					// Finish up this section
					outputFormatter.endSection(seqChange);

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
