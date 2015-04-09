package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.align.SmithWaterman;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String a = "tgtcctagctgtcctaggagctgtgatgtgtaggaggaagagctcaggtggaaaaggagggagctgct";
		String b = "tgtcctagctgtccttggagctgtggtcaccgctatgatgtgtaggaggaagagctcaggtggaaaaggagggagctgct";
		SmithWaterman sw = new SmithWaterman(a, b);
		sw.align();
		System.out.println(sw);
	}
}
