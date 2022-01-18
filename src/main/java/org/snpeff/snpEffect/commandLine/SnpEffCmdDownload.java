package org.snpeff.snpEffect.commandLine;

import org.snpeff.SnpEff;
import org.snpeff.logStatsServer.VersionCheck;
import org.snpeff.util.Download;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Command line program: Download and install a (pre built) database
 * <p>
 * Update: Allow multiple "compatible" databases per version
 * For instance, SnpEff version 5.1 might use databases from SnpEff 5.0
 *
 * @author pcingola
 */
public class SnpEffCmdDownload extends SnpEff {

    boolean update; // Are we updating SnpEff?

    public SnpEffCmdDownload() {
        super();
    }

    boolean downloadAndInstall(URL url, String localFile, boolean maskDownloadExceptions) {
        // Download and UnZIP
        Download download = new Download();
        download.setVerbose(verbose);
        download.setDebug(debug);
        download.setUpdate(update);
        download.setMaskDownloadException(maskDownloadExceptions);

        if (verbose) Log.info("Downloading from '" + url + "' to local file '" + localFile + "'");

        if (download.download(url, localFile)) {
            if (download.unzip(localFile, config.getDirMain(), config.getDirData())) {
                if (verbose) Log.info("Unzip: OK");
                if ((new File(localFile)).delete()) {
                    if (verbose) Log.info("Deleted local file '" + localFile + "'");
                }
                return true; // Successfully downloaded and unzipped
            }
        }

        return false; // Failed to download and install
    }

    /**
     * Attempt to download from (possibly more than one) URLs
     */
    boolean downloadAndInstall(List<URL> urls) {
        // Download and UnZIP
        var maskExceptions = (urls.size() > 1);
        for (URL url : urls) {
            String localFile = System.getProperty("java.io.tmpdir") + "/" + Download.urlBaseName(url.toString());
            if (downloadAndInstall(url, localFile, maskExceptions)) return true;
        }
        Log.fatalError("Failed to download database from " + urls);
        return false;
    }

    /**
     * Parse command line arguments
     */
    @Override
    public void parseArgs(String[] args) {
        this.args = args;
        for (String arg : args) {

            // Argument starts with '-'?
            if (isOpt(arg))
                usage("Unknown option '" + arg + "'"); // Options (config, verbose, etc.) are parsed at SnpEff level
            else if (genomeVer.length() <= 0) genomeVer = arg;
            else usage("Unknown parameter '" + arg + "'");
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

        if (verbose) Log.info("Downloading database for '" + genomeVer + "'");

        List<URL> urls = config.downloadUrl(genomeVer);
        if (downloadAndInstall(urls)) {
            if (verbose) Log.info("Done");
            return true;
        }
        return false;
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
            Log.info("New version: " //
                    + "\n\tNew version  : " + versionCheck.getLatestVersion() //
                    + "\n\tRelease date : " + versionCheck.getLatestReleaseDate() //
                    + "\n\tDownload URL : " + versionCheck.getLatestUrl() //
            );
        } else {
            // Already updated?
            Log.info("No new version found. This seems to be the latest version (" + versionCheck.getLatestVersion() + ") or server could not be contacted. Nothing done.");
            return false;
        }

        // OK, download
        if (verbose) Log.info("Downloading SnpEff");

        URL url;
        try {
            url = new URL(versionCheck.getLatestUrl());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        String localFile = System.getProperty("java.io.tmpdir") + "/" + Gpr.baseName(url.toString());
        downloadAndInstall(url, localFile, false); // Download and unzip

        if (verbose) Log.info("Done");
        return true;
    }

    /**
     * Show 'usage;' message and exit with an error code '-1'
     */
    @Override
    public void usage(String message) {
        if (message != null) System.err.println("Error: " + message + "\n");
        System.err.println("snpEff version " + VERSION);
        System.err.println("Usage: snpEff download [options] {snpeff | genome_version}");
        System.exit(-1);
    }
}
