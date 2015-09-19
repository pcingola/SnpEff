package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String header = ">ENSTTRT00000007616 ensembl_projection:known_by_projection scaffold:turTru1:scaffold_113855:40787:62938:1 gene:ENSTTRG00000007618 gene_biotype:protein_coding transcript_biotype:protein_coding";

		String l[] = header.substring(1).split("[ \t:;,]");
		for (String s : l)
			System.out.println("'" + s + "'");

	}
}
