package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Timer;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		Timer.showStdErr("Start");
		String genome = "testHg3771Chr1";
		String[] argsSnpEff = { "-v", "-nextProt", genome };

		// Create object and load databases
		Zzz zzz = new Zzz(argsSnpEff);
		zzz.load(genome);
		zzz.run();
		Timer.showStdErr("End");
	}

	public Zzz(String[] args) {
		super(args);
	}

	void load(String genome) {
		setGenomeVer(genome);
		parseArgs(args);
		loadConfig();
		loadDb();
	}

	public boolean run() {
		for (Marker m : config.getSnpEffectPredictor().getMarkers()) {
			if (m instanceof NextProt) {
				System.out.println(m);
			}
		}
		return true;
	}
}
