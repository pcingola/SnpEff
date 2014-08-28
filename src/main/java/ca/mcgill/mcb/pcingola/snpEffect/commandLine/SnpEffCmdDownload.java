package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.net.MalformedURLException;
import java.net.URL;

import ca.mcgill.mcb.pcingola.logStatsServer.VersionCheck;
import ca.mcgill.mcb.pcingola.util.Download;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Command line program: Build database
 *
 * @author pcingola
 */
public class SnpEffCmdDownload extends SnpEff {

	boolean update; // Are we updating SnpEff?

	public SnpEffCmdDownload() {
		super();
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {

			// Argument starts with '-'?
			if (args[i].startsWith("-")) usage("Unknow option '" + args[i] + "'"); // Options (config, verbose, etc.) are parsed at SnpEff level
			else if (genomeVer.length() <= 0) genomeVer = args[i];
			else usage("Unknow parameter '" + args[i] + "'");
		}

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");
	}

	/**
	 * Download database from server
	 */
	@Override
	public boolean run() {
		if (genomeVer.equals("snpeff")) {
			// Download SnpEff latest version
			update = true;
			return runDownloadSnpEff();
		} else {
			// Download a genome
			return runDownloadGenome();
		}
	}

	/**
	 * Download a genome file
	 */
	boolean runDownloadGenome() {
		loadConfig(); // Read config file

		if (verbose) Timer.showStdErr("Downloading database for '" + genomeVer + "'");

		URL url = config.downloadUrl(genomeVer);
		String localFile = System.getProperty("java.io.tmpdir") + "/" + Download.urlBaseName(url.toString());

		// Download and UnZIP
		Download download = new Download();
		download.setVerbose(verbose);
		download.setDebug(debug);
		download.setUpdate(update);
		if (download.download(url, localFile)) {
			if (download.unzip(localFile, config.getDirMain(), config.getDirData()) && verbose) Timer.showStdErr("Unzip: OK");
		}

		if (verbose) Timer.showStdErr("Done");
		return true;
	}

	/**
	 * Download SnpEff
	 */
	boolean runDownloadSnpEff() {
		genomeVer = ""; // No genome version
		loadConfig(); // Read config file

		//---
		// Get latest version data from server
		//---
		VersionCheck versionCheck = VersionCheck.version(SnpEff.SOFTWARE_NAME, SnpEff.VERSION_SHORT, config.getVersionsUrl(), verbose);
		if (versionCheck.isNewVersion()) {
			Timer.showStdErr("New version: " //
					+ "\n\tNew version  : " + versionCheck.getLatestVersion() //
					+ "\n\tRelease date : " + versionCheck.getLatestReleaseDate() //
					+ "\n\tDownload URL : " + versionCheck.getLatestUrl() //
			);
		} else {
			// Already updated?
			Timer.showStdErr("No new version found. This seems to be the latest version (" + versionCheck.getLatestVersion() + ") or server could not be contacted. Nothing done.");
			return false;
		}

		// OK, download
		if (verbose) Timer.showStdErr("Downloading SnpEff");

		URL url;
		try {
			url = new URL(versionCheck.getLatestUrl());
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		String localFile = Gpr.baseName(url.toString());

		// Download and unzip
		Download download = new Download();
		download.setVerbose(verbose);
		download.setDebug(debug);
		download.setUpdate(update);
		if (download.download(url, localFile)) {
			if (download.unzip(localFile, config.getDirMain(), config.getDirData()) && verbose) Timer.showStdErr("Unzip: OK");
		}

		if (verbose) Timer.showStdErr("Done");
		return true;
	}

	/**
	 * Show 'usage;' message and exit with an error code '-1'
	 * @param message
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + VERSION);
		System.err.println("Usage: snpEff download [options] {snpeff | genome_version}");
		System.exit(-1);
	}
}
