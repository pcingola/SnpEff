package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class Zzz {

	public static void main(String[] args) {
		String keysPass[] = { "ANN", "ann9", "a9nn", "ann_", "a_nn" };
		String keysFail[] = { "ann+", "9ann", "_ann" };

		for (String key : keysPass)
			System.out.println(key + "\t" + VcfEntry.isValidInfoKey(key));

		for (String key : keysFail)
			System.out.println(key + "\t" + VcfEntry.isValidInfoKey(key));
	}

}
