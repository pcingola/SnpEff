package org.snpeff.snpEffect.commandLine.eff;

import org.snpeff.akka.vcf.MasterVcf;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.outputFormatter.OutputFormatter;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;

import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.UntypedActorFactory;

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

					private static final long serialVersionUID = 1L;

					@Override
					public Actor create() {
						return new WorkerEff(snpEffCmdEff, snpEffectPredictor, outputFormatter.clone(), filterIntervals);
					}

				}) //
				, numWorkers);
		parseNow = false;
		Gpr.debug("MASTER!!!");
	}
}
