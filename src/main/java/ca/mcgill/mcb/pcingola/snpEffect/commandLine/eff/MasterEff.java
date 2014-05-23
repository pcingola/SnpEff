package ca.mcgill.mcb.pcingola.snpEffect.commandLine.eff;

import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;
import ca.mcgill.mcb.pcingola.akka.vcf.MasterVcf;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.outputFormatter.OutputFormatter;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;

/**
 * Master agent for SnpEff 'eff' command
 * 
 * @author pablocingolani
 */
public class MasterEff extends MasterVcf<String> {

	public MasterEff(int numWorkers, final SnpEffCmdEff snpEffCmdEff, final SnpEffectPredictor snpEffectPredictor, final OutputFormatter outputFormatter, final IntervalForest filterIntervals) {
		super(new Props( //
				// Create a factory
				new UntypedActorFactory() {

					@Override
					public Actor create() {
						return new WorkerEff(snpEffCmdEff, snpEffectPredictor, outputFormatter.clone(), filterIntervals);
					}

				}) //
				, numWorkers);
		parseNow = false;
	}
}
