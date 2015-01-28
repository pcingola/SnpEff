package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String vcfFile = Gpr.HOME + "/workspace/SnpEff/tests/test.chr1.eff.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			// Show header (you may want to save it to a file instead)
			if (vcf.isHeadeSection()) System.out.println(vcf.getVcfHeader());

			//			// Query database
			//
			//			// Add annotations
			//			VariantEffect variantEffect = new VariantEffect(variant, marker, effectType, effectImpact, message, codonsOld, codonsNew, codonNum, codonIndex, cDnaPos);
			//			EffFormatVersion formatVersion = EffFormatVersion.FORMAT_ANN_1;
			//			VcfEffect veff = new VcfEffect(variantEffect, formatVersion, true);
			//			ve.addInfo(formatVersion.infoFieldName(), veff.toString());

			// Show VCF line (you may want to save it to a file instead) 
			System.out.println(ve);
		}

	}
}
