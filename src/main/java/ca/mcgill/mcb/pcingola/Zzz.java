package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.CombinatorialIterator;

public class Zzz extends SnpEff {

	public static void main(String[] args) {

		//		CombinatorialIterator ci = new CombinatorialIterator(1);
		//		ci.set(0, 1, 3);
		//		for (int[] ii : ci) {
		//			System.out.println("CI: " + ci);
		//		}

		CombinatorialIterator ci = new CombinatorialIterator(3);
		ci.set(0, 1, 3);
		ci.set(1, 1, 5);
		ci.set(2, 0, 1);
		for (int[] ii : ci) {
			System.out.println("CI: " + ci);
		}

		//		String s = "NN";
		//		System.out.println("in: " + s);
		//		for (String ss : Variant.iub(s))
		//			System.out.println("\tout: " + ss);

		//		String vcfFileName = Gpr.HOME + "/z.vcf";
		//
		//		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		//		for (VcfEntry ve : vcf) {
		//			System.out.println(ve);
		//
		//			for (Variant var : ve.variants())
		//				System.out.println("\t" + var);
		//		}
	}
}
