package org.snpeff.util;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    public static final int DEFAULT_PROXY_PORT = 80;
    private static final int BUFFER_SIZE = 102400;
    boolean debug = false;
    boolean verbose = false;
    boolean update; // Are we updating SnpEff itself?

    boolean maskDownloadException = false;

    public Download() {
    }

    /**
     * File name from URL (i.e. anything after the last '/')
     */
    public static String urlBaseName(String url) {
        String[] f = url.split("/");
        String base = f[f.length - 1];

        int qidx = base.indexOf('?');
        if (qidx > 0) base = base.substring(0, qidx);

        return base;
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
            sslSetup(); // Set up SSL for websites having issues with certificates (e.g. Sourceforge)

            if (verbose) Log.info("Connecting to " + url);

            URLConnection connection = openConnection(url);

            // Follow redirect? (only for http connections)
            if (connection instanceof HttpURLConnection) {
                for (boolean followRedirect = true; followRedirect; ) {
                    HttpURLConnection httpConnection = (HttpURLConnection) connection;
                    if (verbose) Log.info("Connecting to " + url + ", using proxy: " + httpConnection.usingProxy());
                    int code = httpConnection.getResponseCode();

                    if (code == 200) {
                        followRedirect = false; // We are done
                    } else if (code == 302) {
                        String newUrl = connection.getHeaderField("Location");
                        if (verbose) Log.info("Following redirect: " + newUrl);
                        url = new URL(newUrl);
                        connection = openConnection(url);
                    } else if (code == 404) {
                        throw new RuntimeException("File not found on the server. Make sure the database name is correct.");
                    } else throw new RuntimeException("Error code from server: " + code);
                }
            }

            // Copy resource to local file, use remote file if no local file name specified
            InputStream is = connection.getInputStream();

            // Print info about resource
            Date date = new Date(connection.getLastModified());
            if (debug) Log.debug("Copying file (type: " + connection.getContentType() + ", modified on: " + date + ")");

            // Open local file
            if (verbose) Log.info("Local file name: '" + localFile + "'");

            // Create local directory if it doesn't exists
            File file = new File(localFile);
            if (file != null && file.getParent() != null) {
                File path = new File(file.getParent());
                if (!path.exists()) {
                    if (verbose) Log.info("Local path '" + path + "' doesn't exist, creating.");
                    path.mkdirs();
                }
            }

            FileOutputStream os = null;
            os = new FileOutputStream(localFile);

            // Copy to file
            int count = 0, total = 0, lastShown = 0;
            byte[] data = new byte[BUFFER_SIZE];
            while ((count = is.read(data, 0, BUFFER_SIZE)) != -1) {
                os.write(data, 0, count);
                total += count;

                // Show every MB
                if ((total - lastShown) > (1024 * 1024)) {
                    if (verbose) System.err.print(".");
                    lastShown = total;
                }
            }
            if (verbose) Log.info("");

            // Close streams
            is.close();
            os.close();
            if (verbose) Log.info("Download finished. Total " + total + " bytes.");

            res = true;
        } catch (Exception e) {
            res = false;
            if (verbose) Log.info("ERROR while connecting to " + url);
            if (!maskDownloadException) throw new RuntimeException(e);
        }

        return res;
    }

    /**
     * Open a connection
     */
    URLConnection openConnection(URL url) throws IOException {
        Proxy proxy = proxy();
        return (proxy == null ? url.openConnection() : url.openConnection(proxy));
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
            String[] entryPath = entryName.split("/"); // Entry name should be something like 'data/genomeVer/file';
            String dataName = entryPath[entryPath.length - 2] + "/" + entryPath[entryPath.length - 1]; // remove the 'data/' part
            entryName = dataDir + "/" + dataName; // Ad local 'data' dir
            if (debug) Log.debug("Local file name: '" + entryName + "'");
        }

        return entryName;
    }

    /**
     * Parse proxy value from environment
     *
     * @param envVarName: Environment variable name
     * @return A Tuple with host and port, null if not found or could not be parsed
     */
    Tuple<String, Integer> parseProxyEnv(String envVarName) {
        String envProxy = System.getenv(envVarName);
        if (envProxy == null || envProxy.isBlank()) return null;

        // Parse URL from environment variable
        if (verbose) Log.info("Using proxy from environment variable '" + envVarName + "', value '" + envProxy + "'");

        String proxyHost = null;
        int port = DEFAULT_PROXY_PORT;

        try {
            URL url;
            url = new URL(envProxy);
            proxyHost = url.getHost();
            port = url.getPort();
        } catch (MalformedURLException e) {
            // Could not parse URL

            if (envProxy.indexOf(':') > 0) {
                // Try "host:port" format
                String[] hp = envProxy.split(":");
                proxyHost = hp[0];
                port = Gpr.parseIntSafe(hp[1]);
            } else {
                // Use just host (leave port as default)
                proxyHost = envVarName;
            }
        }

        if (verbose)
            Log.info("Parsing proxy value '" + envProxy + "', host: '" + proxyHost + "', port: '" + port + "'");
        return new Tuple<>(proxyHost, port);
    }

    /**
     * Parse proxy from Java propperties
     *
     * @return A Tuple with host and port, null if not found or could not be parsed
     */
    Tuple<String, Integer> parseProxyJavaPropperty() {
        // Try java properties, i.e. '-D' command line argument
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");

        // Java property not found
        if (proxyHost == null || proxyHost.isBlank()) return null;

        if (verbose)
            Log.info("Using proxy from Java properties: http.proxyHost: '" + proxyHost + "', http.proxyPort: '" + proxyPort + "'");
        int port = (proxyPort != null && !proxyPort.isBlank() ? Gpr.parseIntSafe(proxyPort) : DEFAULT_PROXY_PORT);

        if (verbose)
            Log.info("Parsing proxy value from Java propperties, host: '" + proxyHost + "', port: '" + port + "'");
        return new Tuple<>(proxyHost, port);
    }

    /**
     * Create a proxy if the system properties are set
     * I.e.: If java is run using something like
     * java -Dhttp.proxyHost=$PROXY -Dhttp.proxyPort=$PROXY_PORT -jar ...
     *
     * @return A proxy object if system properties were defined, null otherwise
     */
    Proxy proxy() {
        // Try environment variable
        Tuple<String, Integer> proxyHostPort = parseProxyEnv("http_proxy");

        // Try another environment variable
        if (proxyHostPort == null) proxyHostPort = parseProxyEnv("HTTP_PROXY");

        // Not found in environment? Try java properties
        if (proxyHostPort == null) proxyHostPort = parseProxyJavaPropperty();

        if (proxyHostPort == null) return null;

        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHostPort.getFirst(), proxyHostPort.getSecond()));
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setMaskDownloadException(boolean maskDownloadException) {
        this.maskDownloadException = maskDownloadException;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Sourceforge certificates throw exceptions if we don't add this SSL setup
     * Reference: http://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient
     */
    void sslSetup() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
        SSLContext.setDefault(ctx);
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
                if (verbose) Log.info("Creating backup file '" + backupFile + "'");
                zipBackup = new ZipOutputStream(new FileOutputStream(backupFile));
            }

            //---
            // Extract ZIP file
            //---
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String localEntryName = parseEntryPath(entry.getName(), mainDir, dataDir);
                    if (debug) Log.debug("Extracting file '" + entry.getName() + "' to '" + localEntryName + "'");
                    else if (verbose) Log.info("Extracting file '" + entry.getName() + "'");

                    // Backup entry
                    if (zipBackup != null) backupFile(zipBackup, localEntryName);

                    //---
                    // Does directory exists?
                    //---
                    String dirName = Gpr.dirName(localEntryName);
                    File dir = new File(dirName);
                    if (!dir.exists()) {
                        // Create local dir
                        if (verbose) Log.info("Creating local directory: '" + dir + "'");
                        if (!dir.mkdirs())
                            throw new RuntimeException("Cannot create directory '" + dir.getCanonicalPath() + "'");
                    }

                    //---
                    // Extract entry
                    //---
                    FileOutputStream fos = new FileOutputStream(localEntryName);
                    BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER_SIZE);

                    int count = 0;
                    byte[] data = new byte[BUFFER_SIZE];
                    while ((count = zipIn.read(data, 0, BUFFER_SIZE)) != -1)
                        dest.write(data, 0, count);

                    dest.flush();
                    dest.close();
                } else if (entry.isDirectory()) {
                    String dir = parseEntryPath(entry.getName(), mainDir, dataDir);
                    // Create local dir
                    if (verbose) Log.info("Creating local directory: '" + dir + "'");
                    if (!(new File(dir)).mkdirs()) throw new RuntimeException("Cannot create directory '" + dir + "'");
                }
            }

            // Close zip files
            zipIn.close();
            if (zipBackup != null) {
                zipBackup.close();
                Log.info("Backup file created: '" + backupFile + "'");
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * Used to get rid of some SSL Certificateproblems
     * Ref: http://stackoverflow.com/questions/1828775/how-to-handle-invalid-ssl-certificates-with-apache-httpclient
     */
    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
