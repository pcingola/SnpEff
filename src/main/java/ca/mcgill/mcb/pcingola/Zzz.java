package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.util.Download;
import ca.mcgill.mcb.pcingola.util.Timer;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		Timer.showStdErr("Start");

		Download download = new Download();
		download.setVerbose(true);

		String zipFile = "/home/pcingola/zzz/file.zip";
		String mainDir = "/home/pcingola/zzz/main";
		String dataDir = "/home/pcingola/zzz/main/data";
		download.unzip(zipFile, mainDir, dataDir);

		Timer.showStdErr("End");
	}

}
