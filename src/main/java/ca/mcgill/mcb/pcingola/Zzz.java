package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz {

	public static void main(String[] args) {

		VcfFileIterator vcf = new VcfFileIterator(Gpr.HOME + "/snpEff/aa.vcf");

		for (VcfEntry ve : vcf) {
			System.out.println(ve);

			for (SeqChange sc : ve.seqChanges()) {
				System.out.println("\t" + sc);
			}
		}
	}

}
