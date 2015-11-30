package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz {

	public static void main(String[] args) {
		String vcfFileName = Gpr.HOME + "/snpEff/z.vcf";
		// String vcfFileName = Gpr.HOME + "/snpEff/miika_FGFR3_TACC3_fusion.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
			for (Variant v : ve.variants())
				System.out.println("\t" + v.isVariant() + "\t" + v);
		}
	}

}
