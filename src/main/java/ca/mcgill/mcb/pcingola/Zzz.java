package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz extends SnpEff {

	StringBuilder sb = new StringBuilder();

	public static void main(String[] args) {
		Timer.showStdErr("Start");

		// Create an input file iterator
		String vcfFileName = Gpr.HOME + "/snpEff/sri/db.bad.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		vcf.setDebug(true);
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
		}

		Timer.showStdErr("End");
	}

}
