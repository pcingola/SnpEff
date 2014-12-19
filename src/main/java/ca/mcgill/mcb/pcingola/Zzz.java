package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String emptyStr = "";
		String[] list = emptyStr.split(" ");
		System.out.println("list.length: " + list.length);

		for (int i = 0; i < list.length; i++)
			System.out.println("\titem " + i + ":\t'" + list[i] + "'");
	}
}
