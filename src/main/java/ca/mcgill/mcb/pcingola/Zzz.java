package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz {

	public static void main(String[] args) {

		VcfFileIterator vcf = new VcfFileIterator(Gpr.HOME + "/snpEff/test_tabix.vcf.gz");

		for (VcfEntry ve : vcf) {
			System.out.println(ve);
		}
	}

}
