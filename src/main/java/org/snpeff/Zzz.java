package org.snpeff;

import org.snpeff.vcf.VcfEntry;

/**
 * 
 */
public class Zzz {

	public static void main(String[] args) {

		String str = "hi;hello;bye;\nadios=chau\tbye\nhi=hello\thola";

		String enc = VcfEntry.vcfInfoEncode(str);
		System.out.println(enc);

		String dec = VcfEntry.vcfInfoDecode(enc);
		System.out.println(dec);

		//		String vcfFileName = Gpr.HOME + "/snpEff/z.ann.vcf";
		//
		//		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		//
		//		// Each VCF line is a VcfEntry
		//		for (VcfEntry vcfEntry : vcf) {
		//			System.out.println(vcfEntry);
		//
		//			// Parse annotations 'ANN' (formerly known as effects 'EFF')
		//			for (VcfEffect veff : vcfEntry.getVcfEffects()) {
		//				System.out.println("\t" + veff);
		//				System.out.println("\t\tAlele: " + veff.getAllele());
		//				System.out.println("\t\tEffects: " + veff.getEffectTypesStr());
		//				System.out.println("\t\tTranscript ID: " + veff.getFeatureId());
		//				System.out.println("\t\tHGVS.c: " + veff.getHgvsC());
		//				System.out.println("\t\tHGVS.p: " + veff.getHgvsP());
		//			}

		// TODO: Add 'auto curate'
		// Format:
		//		Reasons why the variant is filtered: {NON_CODING, AF_1KG, ...., AF_EXAC, ... } => Mapped to some codes 
		//      One auto-curate code per variant annotation entry (VcfEffect)
		//      AUTO_CURATE_CODE=5,2,4,0,...,12 
		// vcfEntry.getInfo("AUTO_CURATE");

		//		}
	}

}
