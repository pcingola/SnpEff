package org.snpeff.snpEffect.commandLine;

public class Zzz {

	public static void main(String[] args) {
		SnpEffCmdEff cmd = new SnpEffCmdEff();
		AnnotateVcf ann = new AnnotateVcf();
		ValuesCopy vc = new ValuesCopy(cmd, ann);
		vc.copy();
	}

}
