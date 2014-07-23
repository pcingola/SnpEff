package ca.mcgill.mcb.pcingola.util;

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

/**
 * Command line program: Build database
 *
 * @author pcingola
 */
public class Download {

	private static int BUFFER_SIZE = 102400;

	boolean debug = false;

	boolean verbose = false;
	boolean update; // Are we updating ?

	/**
	 * File name from URL (i.e. anything after the last '/')
	 */
	public static String urlBaseName(String url) {
		String f[] = url.toString().split("/");
		return f[f.length - 1];
	}

	public Download() {
	}

	/**
	 * Add files to 'backup' ZIP file
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

	public boolean download(String urlString, String localFile) {
		try {
			URL url = new URL(urlString);
			return download(url, localFile);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Download a file
	 */
	public boolean download(URL url, String localFile) {
		boolean res = false;
		try {
			if (verbose) Timer.showStdErr("Connecting to " + url);
			URLConnection connection = url.openConnection();

			// Follow redirect? (only for http connections)
			if (connection instanceof HttpURLConnection) {
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
			}

			// Copy resource to local file, use remote file if no local file name specified
			InputStream is = url.openStream();

			// Print info about resource
			Date date = new Date(connection.getLastModified());
			if (verbose) Timer.showStdErr("Copying file (type: " + connection.getContentType() + ", modified on: " + date + ")");

			// Open local file
			if (verbose) Timer.showStdErr("Local file name: '" + localFile + "'");

			// Create local directory if it doesn't exists
			File file = new File(localFile);
			File path = new File(file.getParent());
			if (!path.exists()) {
				if (verbose) Timer.showStdErr("Local path '" + path + "' doesn't exist, creating.");
				path.mkdirs();
			}

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
					if (verbose) System.err.print(".");
					lastShown = total;
				}
			}
			if (verbose) System.err.println("");

			// Close streams
			is.close();
			os.close();
			if (verbose) Timer.showStdErr("Donwload finished. Total " + total + " bytes.");

			res = true;
		} catch (Exception e) {
			Timer.showStdErr("ERROR while connecting to " + url);
			throw new RuntimeException(e);
		}

		return res;
	}

	/**
	 * Parse an entry path from a ZIP file
	 */
	String parseEntryPath(String entryName, String mainDir, String dataDir) {
		if (update) {
			// Software update: Entry name should be something like 'snpEff_vXX/dir/file';
			int idx = entryName.indexOf('/');
			if (idx > 0) entryName = mainDir + entryName.substring(idx);
			else throw new RuntimeException("Expecting at least one directory in path '" + entryName + "'");
		} else {
			// Database download
			String entryPath[] = entryName.split("/"); // Entry name should be something like 'data/genomeVer/file';
			String dataName = entryPath[entryPath.length - 2] + "/" + entryPath[entryPath.length - 1]; // remove the 'data/' part
			entryName = dataDir + "/" + dataName; // Ad local 'data' dir
			if (verbose) Timer.showStdErr("Local file name: '" + entryName + "'");
		}

		return entryName;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * UnZIP all files
	 */
	public boolean unzip(String zipFile, String mainDir, String dataDir) {
		try {
			FileInputStream fis = new FileInputStream(zipFile);
			ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(fis));
			ZipOutputStream zipBackup = null;
			String backupFile = "";

			// Create a ZIP backup file (only if we are updating)
			if (update) {
				backupFile = String.format("%s/backup_%2$tY-%2$tm-%2$td_%2$tH:%2$tM:%2$tS.zip", mainDir, new GregorianCalendar());
				if (verbose) Timer.showStdErr("Creating backup file '" + backupFile + "'");
				zipBackup = new ZipOutputStream(new FileOutputStream(backupFile));
			}

			//---
			// Extract ZIP file
			//---
			ZipEntry entry;
			while ((entry = zipIn.getNextEntry()) != null) {
				if (!entry.isDirectory()) {
					String localEntryName = parseEntryPath(entry.getName(), mainDir, dataDir);
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
					String dir = parseEntryPath(entry.getName(), mainDir, dataDir);
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

}
