package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String vcfFile = Gpr.HOME + "/snpEff/test_inv.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
			for (SeqChange sc : ve.seqChanges())
				System.out.println("\t" + sc);
		}

		// Stop here....
		if (Math.random() < 2) return;

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

	@Override
	public boolean run() {
		for (Marker m : config.getSnpEffectPredictor().getMarkers()) {
			if (m instanceof NextProt) {
				System.out.println(m);
			}
		}
		return true;
	}
}
