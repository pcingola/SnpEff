package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		String z = "aaa&bbb+ccc|ddd,eee,fff;ggg(hhh)iii[jjj]kkk+&;,zzz";
		String zs[] = z.split("[\\&\\+\\|,;:\\(\\)\\[\\]]+");
		for (String s : zs)
			System.out.println("'" + s + "'");
	}
}
