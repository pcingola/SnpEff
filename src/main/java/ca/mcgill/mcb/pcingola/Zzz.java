package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		Timer.showStdErr("Start");

		String vcfFile = Gpr.HOME + "/snpEff/slow.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
		}

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
