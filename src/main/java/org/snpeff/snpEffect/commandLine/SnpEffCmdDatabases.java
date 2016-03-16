package org.snpeff.snpEffect.commandLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.snpeff.SnpEff;
import org.snpeff.util.Gpr;

/**
 * Show all databases configures in snpEff.config
 *
 * Create an HTML 'download' table based on the config file
 * Also creates a list of genome for Galaxy menu
 *
 * @author pablocingolani
 */
public class SnpEffCmdDatabases extends SnpEff {

	public static final String DARK_ROW = "bgcolor=#CCCCCC";
	public static final String LIGHT_ROW = "bgcolor=#EEEEEE";

	public static final String HTTP_PROTOCOL = "http://";
	public static final String FTP_PROTOCOL = "ftp://";

	boolean galaxy = false;
	boolean html = false;
	HashMap<String, String> nameByGenomeVer;
	ArrayList<String> namesSorted;
	ArrayList<String> genVerSorted;

	public SnpEffCmdDatabases() {
	}

	/**
	 * Galaxy config genome list
	 */
	void galaxyConfig() {
		System.out.println("\t<param name=\"genomeVersion\" type=\"select\" label=\"Genome\">");

		for (String name : namesSorted) {
			for (String genVer : genVerSorted) {
				String n = config.getName(genVer);

				// In this group?
				if (name.equals(n)) {
					System.out.println("\t\t<option value=\"" + genVer + "\">" + name.replace('_', ' ') + " : " + genVer + "</option>");
				}
			}
		}

		System.out.println("\t</param>");
	}

	/**
	 * Create html table
	 */
	void htmlTable() {
		// Create an HTML table
		boolean dark = false;
		String bg = "";

		System.out.println("\t<table> <tr " + DARK_ROW + "> <td> <b> Genome </b> </td>  <td> <b> Version </b> </td>  <td> <b> Reference </b> </td> </tr>");
		for (String name : namesSorted) {

			// Color
			if (dark) bg = DARK_ROW;
			else bg = LIGHT_ROW;
			dark = !dark;

			boolean showName = true;
			for (String genVer : genVerSorted) {
				String n = config.getName(genVer);
				// In this group?
				if (name.equals(n)) {
					System.out.println("\t\t<tr " + bg + ">");

					// Show name
					String name2show = showName ? name.replace('_', ' ') : "&nbsp;";
					System.out.println("\t\t\t<td> " + name2show + " </td>");
					showName = false;

					// Download link
					String url = "http://sourceforge.net/projects/snpeff/files/databases/v" + SnpEff.VERSION_MAJOR + "/snpEff_v" + SnpEff.VERSION_MAJOR + "_" + genVer + ".zip";
					System.out.println("\t\t\t<td> <a class=\"body\" href=\"" + url + "\"> " + genVer + " </a> </td>");

					// Reference
					String ref = config.getReference(genVer);
					String link = "";
					if (ref != null) {
						if (ref.indexOf(',') > 0) ref = ref.substring(0, ref.indexOf(',')); // Many references? Use the first one
						link = ref;

						// Remove everything after slash
						int idx = ref.indexOf('/', HTTP_PROTOCOL.length());
						if (idx > 0) ref = ref.substring(0, idx);

						// Remove protocol
						if (ref.startsWith(HTTP_PROTOCOL)) ref = ref.substring(HTTP_PROTOCOL.length());
						if (ref.startsWith(FTP_PROTOCOL)) ref = ref.substring(FTP_PROTOCOL.length());

					} else ref = "";
					System.out.println("\t\t\t<td> <a class=\"body\" href=\"" + link + "\">" + ref + "</a> </td>");

					System.out.println("\t\t</tr>");
				}
			}
		}
		System.out.println("\t</table>");
	}

	@Override
	public void parseArgs(String[] args) {
		if (args.length > 1) usage(null);

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];

			if (isOpt(arg)) {
				usage("Unknown option '" + arg + "'");
			} else {
				// Command line
				if (arg.equals("galaxy")) {
					galaxy = true;
					html = false;
				} else if (arg.equals("html")) {
					html = true;
					galaxy = false;
				}
			}
		}
	}

	@Override
	public boolean run() {
		// Read config (it doesn't matter which genome)
		genomeVer = "hg19";
		loadConfig(); // Read config file

		// Get all genome names and sort them
		nameByGenomeVer = new HashMap<String, String>();
		for (String genVer : config)
			nameByGenomeVer.put(genVer, config.getName(genVer));

		namesSorted = new ArrayList<String>();
		namesSorted.addAll(nameByGenomeVer.values());
		Collections.sort(namesSorted);

		// Sort genome versions
		genVerSorted = new ArrayList<String>();
		for (String genVer : config)
			genVerSorted.add(genVer);
		Collections.sort(genVerSorted);

		if (galaxy) galaxyConfig();
		else if (html) htmlTable();
		else txtTable();

		return true;
	}

	/**
	 * Create TXT table
	 */
	void txtTable() {
		System.out.println(String.format("%-60s\t%-60s\t%-10s\t%-30s\t%s", "Genome", "Organism", "Status", "Bundle", "Database download link"));
		System.out.println(String.format("%-60s\t%-60s\t%-10s\t%-30s\t%s", "------", "--------", "------", "------", "----------------------"));

		for (String genomeVer : genVerSorted) {
			String name = nameByGenomeVer.get(genomeVer);

			// Download link
			String url = config.downloadUrl(genomeVer).toString();

			// Bundle name
			String bundle = config.getBundleName(genomeVer);
			if (bundle == null) bundle = "";

			// Check database file
			String database = config.getDirData() + "/" + genomeVer + "/snpEffectPredictor.bin";
			String status = "";
			if (Gpr.canRead(database)) status = "OK";

			// Show
			System.out.println(String.format("%-60s\t%-60s\t%-10s\t%-30s\t%s", genomeVer, name, status, bundle, url));
		}
	}

	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("Usage: snpEff databases [galaxy|html]");
		System.err.println("\nOptions");
		System.err.println("\tgalaxy  : Show databases in a galaxy menu format.");
		System.err.println("\thtml    : Show databases in a HTML format.");
		System.exit(-1);
	}
}
