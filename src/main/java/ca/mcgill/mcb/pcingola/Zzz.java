package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz {

	public static void main(String[] args) {

		VcfFileIterator vcf = new VcfFileIterator(Gpr.HOME + "/snpEff/test_tabix.vcf.gz");
		// VcfFileIterator vcf = new VcfFileIterator(Gpr.HOME + "/snpEff/test_tabix.vcf");

		for (VcfEntry ve : vcf) {
			if (vcf.isHeadeSection()) {
				System.out.println(vcf.getVcfHeader());
			}
			System.out.println(ve);
		}
	}

}
