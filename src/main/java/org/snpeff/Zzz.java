package org.snpeff;

import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * 
 */
public class Zzz {

	public static void main(String[] args) {

		String vcfFileName = Gpr.HOME + "/snpEff/z.ann.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);

		// Each VCF line is a VcfEntry
		for (VcfEntry vcfEntry : vcf) {
			System.out.println(vcfEntry);

			// Parse annotations 'ANN' (formerly known as effects 'EFF')
			for (VcfEffect veff : vcfEntry.getVcfEffects()) {
				System.out.println("\t" + veff);
				System.out.println("\t\tAlele: " + veff.getAllele());
				System.out.println("\t\tEffects: " + veff.getEffectTypesStr());
				System.out.println("\t\tTranscript ID: " + veff.getFeatureId());
				System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
				System.out.println("\t\tHGVS.p: " + veff.getHgvsP());
			}
		}
	}

}
