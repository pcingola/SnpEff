package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ca.mcgill.mcb.pcingola.logStatsServer.VersionCheck;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Command line program: Build database
 * 
 * @author pcingola
 */
public class SnpEffCmdDownload extends SnpEff {

	public static boolean debug = false;
	private static int BUFFER_SIZE = 102400;

	String version = SnpEff.VERSION_MAJOR;
	boolean update; // Are we updating SnpEff?

	public SnpEffCmdDownload() {
		super();
	}

	/**
	 * Add files to 'backup' zip file
	 * @param zos
	 * @param fileName
	 */
	void backupFile(ZipOutputStream zos, String fileName) {
		try {
			FileInputStream fis = new FileInputStream(fileName);

			zos.putNextEntry(new ZipEntry(fileName));
			int len;
			byte[] buf = new byte[BUFFER_SIZE];
			while ((len = fis.read(buf)) > 0)
				zos.write(buf, 0, len);

			zos.closeEntry();
			fis.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * File name from URL (i.e. anything after the last '/')
	 * @param url
	 * @return
	 */
	String baseName(String url) {
		String f[] = url.toString().split("/");
		return f[f.length - 1];
	}

	/**
	 * Build the URL for getting the database file
	 * 
	 * Format  : DatabaseRepository / v VERSION / snpEff_v VERSION _ genomeVersion .zip
	 * Example : http://downloads.sourceforge.net/project/snpeff/databases/v2_0_3/snpEff_v2_0_3_EF3.64.zip
	 * 
	 * @param genomeVer
	 * @return
	 */
	private URL buildUrl() {
		try {
			// Replace '.' by '_' 
			version = version.replace('.', '_');

			String urlRoot = config.getDatabaseRepository();

			StringBuilder urlsb = new StringBuilder();
			urlsb.append(urlRoot);
			if (urlsb.charAt(urlRoot.length() - 1) != '/') urlsb.append("/");
			urlsb.append("v" + version + "/snpEff_v" + version + "_" + genomeVer + ".zip");

			return new URL(urlsb.toString());
		} catch (MalformedURLException e) {
			return null;
		}
	}

	/**
	 * Download a file
	 * @param url
	 * @return
	 */
	boolean download(URL url, String localFile) {
		boolean res = false;
		try {
			if (verbose) Timer.showStdErr("Connecting to " + url);
			URLConnection connection = url.openConnection();

			for (boolean followRedirect = true; followRedirect;) {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				int code = httpConnection.getResponseCode();

				if (code == 200) {
					followRedirect = false; // We are done
				} else if (code == 302) {
					String newUrl = connection.getHeaderField("Location");
					if (verbose) Timer.showStdErr("Following redirect: " + newUrl);
					url = new URL(newUrl);
					connection = url.openConnection();
				} else if (code == 404) {
					throw new RuntimeException("File not found on the server. Make sure the database name is correct.");
				} else throw new RuntimeException("Error code from server: " + code);
			}

			// Copy resource to local file, use remote file if no local file name specified
			InputStream is = url.openStream();

			// Print info about resource
			Date date = new Date(connection.getLastModified());
			if (verbose) Timer.showStdErr("Copying file (type: " + connection.getContentType() + ", modified on: " + date + ")");

			// Open local file 
			if (verbose) Timer.showStdErr("Local file name: '" + localFile + "'");
			FileOutputStream os = null;
			os = new FileOutputStream(localFile);

			// Copy to file
			int count = 0, total = 0, lastShown = 0;
			byte data[] = new byte[BUFFER_SIZE];
			while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
				os.write(data, 0, count);
				total += count;

				// Show every MB
				if ((total - lastShown) > (1024 * 1024)) {
					if (verbose) Timer.showStdErr("Downloaded " + total + " bytes");
					lastShown = total;
				}
			}

			// Close streams
			is.close();
			os.close();
			if (verbose) Timer.showStdErr("Donwload finished. Total " + total + " bytes.");

			res = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return res;
	}

	/**
	 * Parse command line arguments
	 * @param args
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
	 * Parse an entry path from a ZIP file
	 * @param entryName
	 * @return
	 */
	String parseEntryPath(String entryName) {
		if (update) {
			// Software update: Entry name should be something like 'snpEff_vXX/dir/file';
			int idx = entryName.indexOf('/');
			if (idx > 0) entryName = config.getDirMain() + entryName.substring(idx);
			else throw new RuntimeException("Expecting at least one directory in path '" + entryName + "'");
		} else {
			// Database download
			String entryPath[] = entryName.split("/"); // Entry name should be something like 'data/genomeVer/file';
			String dataName = entryPath[entryPath.length - 2] + "/" + entryPath[entryPath.length - 1]; // remove the 'data/' part
			entryName = config.getDirData() + "/" + dataName; // Ad local 'data' dir
			if (verbose) Timer.showStdErr("Local file name: '" + entryName + "'");
		}

		return entryName;
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
	 * @return
	 */
	boolean runDownloadGenome() {
		loadConfig(); // Read config file

		if (verbose) Timer.showStdErr("Downloading database for '" + genomeVer + "'");

		URL url = buildUrl();
		String localFile = baseName(url.toString());

		// Download and unzip
		if (download(url, localFile)) {
			if (unzip(localFile) && verbose) Timer.showStdErr("Unzip: OK");
		}

		if (verbose) Timer.showStdErr("Done");
		return true;
	}

	/**
	 * Download snpEff
	 * @return
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
		String localFile = baseName(url.toString());

		// Download and unzip
		if (download(url, localFile)) {
			if (unzip(localFile) && verbose) Timer.showStdErr("Unzip: OK");
		}

		if (verbose) Timer.showStdErr("Done");
		return true;
	}

	/**
	 * Unzip all files
	 * @return
	 */
	boolean unzip(String zipFile) {
		try {
			FileInputStream fis = new FileInputStream(zipFile);
			ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(fis));
			ZipOutputStream zipBackup = null;
			String backupFile = "";

			// Create a ZIP backup file (only if we are updating)
			if (update) {
				backupFile = String.format("%s/backup_%2$tY-%2$tm-%2$td_%2$tH:%2$tM:%2$tS.zip", config.getDirMain(), new GregorianCalendar());
				if (verbose) Timer.showStdErr("Creating backup file '" + backupFile + "'");
				zipBackup = new ZipOutputStream(new FileOutputStream(backupFile));
			}

			//---
			// Extract zip file
			//---
			ZipEntry entry;
			while ((entry = zipIn.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					String localEntryName = parseEntryPath(entry.getName());
					if (verbose) Timer.showStdErr("Extracting file '" + entry.getName() + "' to '" + localEntryName + "'");

					// Backup entry
					if (zipBackup != null) backupFile(zipBackup, localEntryName);

					//---
					// Does directory exists?
					//---
					String dirName = Gpr.dirName(localEntryName);
					File dir = new File(dirName);
					if (!dir.exists()) {
						if (verbose) Timer.showStdErr("Creating local directory: '" + dir + "'");
						dir.mkdirs(); // Create local dir
					}

					//---
					// Extract entry
					//---
					FileOutputStream fos = new FileOutputStream(localEntryName);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);

					int count = 0;
					byte data[] = new byte[BUFFER_SIZE];
					while ((count = zipIn.read(data, 0, BUFFER_SIZE)) != -1)
						dest.write(data, 0, count);

					dest.flush();
					dest.close();
				} else if (entry.isDirectory()) {
					String dir = parseEntryPath(entry.getName());
					if (verbose) Timer.showStdErr("Creating local directory: '" + dir + "'");
					new File(dir).mkdirs(); // Create local dir
				}
			}

			// Close zip files
			zipIn.close();
			if (zipBackup != null) {
				zipBackup.close();
				Timer.showStdErr("Backup file created: '" + backupFile + "'");
			}

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

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
		// System.err.println("If 'snpeff' is used instead of a genome, SnpEff latest version will be downloaded");
		System.exit(-1);
	}
}
