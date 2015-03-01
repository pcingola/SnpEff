package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String vcfFile = Gpr.HOME + "/snpEff/t.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {

			// Show VCF line (you may want to save it to a file instead) 
			System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				System.out.println("\t" + veff);
				System.out.println("\tcds:\tpos:" + veff.getCdsPos() + "\tlen: " + veff.getCdsLen());
				// System.out.println("\tcds:\tpos:" + veff.getVcfFieldString("CDS_POS") + "\tlen: " + veff.getVcfFieldString("CDS_LEN"));
				System.out.println("\tcDna:\tpos:" + veff.getcDnaPos() + "\tlen: " + veff.getcDnaLen());
				System.out.println("\tprot:\tpos:" + veff.getAaPos() + "\tlen: " + veff.getAaLen());
			}
		}

	}
}
