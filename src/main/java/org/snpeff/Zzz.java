package org.snpeff;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map.Entry;

import org.snpeff.util.Gpr;

public class Zzz {

	public static void main(String[] args) throws Exception {
		String urlstr = "https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_GRCh37.75.zip";
		URL url = new URL(urlstr);
		URLConnection connection = openConnection(url);
		HttpURLConnection httpConnection = (HttpURLConnection) connection;

		System.out.println("Connecting to " + url + ", using proxy: " + httpConnection.usingProxy());

		for (Entry<String, List<String>> e : httpConnection.getHeaderFields().entrySet()) {
			System.out.println("Key: " + e.getKey() + "\nValue:" + e.getValue() + "\n");
		}
	}

	public static URLConnection openConnection(URL url) throws IOException {
		Proxy proxy = proxy();
		return (proxy == null ? url.openConnection() : url.openConnection(proxy));
	}

	public static Proxy proxy() {
		String proxyHost = System.getProperty("http.proxyHost");
		String proxyPort = System.getProperty("http.proxyPort");

		if (proxyHost == null || proxyHost.isBlank()) return null;

		int port = Gpr.parseIntSafe(proxyPort);
		if (port <= 0) port = 80;

		System.out.println("Using proxy host '" + proxyHost + "', port " + port);

		return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, port));
	}

}