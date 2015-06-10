package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String vcfFileName = Gpr.HOME + "/z.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			System.out.println(ve);

			for (Variant var : ve.variants())
				System.out.println("\t" + var);
		}
	}
}
