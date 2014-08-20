package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Timer;

public class Zzz extends SnpEff {

	StringBuilder sb = new StringBuilder();

	public static void main(String[] args) {
		Timer.showStdErr("Start");

		for (EffectType et : EffectType.values()) {
			System.out.println(et.toSequenceOntology());
		}

		Timer.showStdErr("End");
	}

}
