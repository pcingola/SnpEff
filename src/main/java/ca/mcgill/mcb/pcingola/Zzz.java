package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz {

	public static void main(String[] args) {
		String vcfFileName = Gpr.HOME + "/snpEff/miika_EML4_ALK_fusion.vcf";
		// String vcfFileName = Gpr.HOME + "/snpEff/miika_FGFR3_TACC3_fusion.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
		}
	}

}
