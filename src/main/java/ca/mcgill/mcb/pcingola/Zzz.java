package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String genome = "testHg3771Chr1";
		String[] argsSnpEff = { "-v", "-nextProt", "-c", Gpr.HOME + "snpEff/snpEff.config", genome };

		// Create object and load databases
		Zzz zzz = new Zzz(argsSnpEff);
		zzz.setGenomeVer(genome);
		zzz.loadConfig();
		zzz.loadDb();
	}

	public Zzz(String[] args) {
		super(args);
	}
}
