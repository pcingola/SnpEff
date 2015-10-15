package ca.mcgill.mcb.pcingola;

import java.io.File;
import java.io.IOException;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Gpr;

public class Zzz extends SnpEff {

	public static void main(String[] args) {

		String fileName = Gpr.HOME + "/workspace";
		File file = new File(fileName);
		try {
			System.out.println("Canonical : " + file.getCanonicalPath());
			System.out.println("Absolute  : " + file.getAbsolutePath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
