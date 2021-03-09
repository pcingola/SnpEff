package org.snpeff.snpEffect;

import java.util.HashMap;

import org.snpeff.codons.CodonTable;
import org.snpeff.codons.CodonTables;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Analize purity changes in codons and amino acids
 * @author pcingola
 */
public class PurityChange {

	public static final int CODONS_AROUND = 5;
	public static final int CODONS_SIZE = 3;
	public static final int MIN_COUNT_CODON = 2;
	public static final String KEY_SEPARATOR = "\t";
	public static final boolean debug = false;

	CodonTable codonTable;

	public static void main(String[] args) {
		if (args.length <= 0) {
			System.err.println("Usage: PurityChange snpEff_results_file");
			System.exit(-1);
		}

		// Create object and process file
		CodonTable codonTable = CodonTables.getInstance().getTable(CodonTables.STANDARD_TABLE_NAME); // We need a codon table (for humans, use default)
		PurityChange purityChange = new PurityChange(codonTable);
		purityChange.load(args[0]);
	}

	public PurityChange(CodonTable codonTable) {
		this.codonTable = codonTable;
	}

	/**
	 * Add to a counter in a hashmap
	 * @param count
	 * @param codon
	 */
	void add(HashMap<String, Integer> count, String key, int add) {
		Integer c = count.get(key);
		if (c == null) c = 1;
		else c += add;
		count.put(key, c);
	}

	/**
	 * Add to a counter in a hashmap (creating a key: "AA\tCodon")
	 * @param count
	 * @param codon
	 */
	void addAa(HashMap<String, Integer> count, String codon, int add) {
		String aa = codonTable.aa(codon);
		String key = aa + KEY_SEPARATOR + codon.toUpperCase();
		add(count, key, add);
	}

	String getCodon(String str, int codonNum) {
		int pos = codonNum * CODONS_SIZE;
		return str.substring(pos, pos + CODONS_SIZE);
	}

	void load(String resFileName) {
		StringBuffer out = new StringBuffer();

		//---
		// Read snpEffect results files
		//---
		Log.debug("Reading file: " + resFileName);
		String resFile = Gpr.readFile(resFileName);
		String resLines[] = resFile.split("\n");

		int countIgnored = 0, countOk = 0;
		for (String resLine : resLines) {
			String recs[] = resLine.split("\t");
			if (recs.length > 21) {
				String chr = recs[0];
				String pos = recs[1];
				String ref = recs[2];
				String change = recs[3];

				if (change.length() / CODONS_SIZE == 1) {
					String codonsAround[] = recs[20].split("/");
					if (codonsAround.length > 1) {
						// Analyze codon purity
						String codonsAroundOld = codonsAround[0].trim();
						String codonsAroundNew = codonsAround[1].trim();
						String codonsChange[] = recs[17].split("/");
						String codonsOld = codonsChange[0].trim();
						String codonsNew = codonsChange[1].trim();
						double purityChange = purityChange(codonsOld, codonsAroundOld, codonsNew, codonsAroundNew);

						// Analyze amino acid purity
						String aastr[] = recs[21].split("/");
						String aaAroundOld = aastr[0];
						String aaAroundNew = aastr[1];

						countOk++;
						String lineOut = chr + "\t" + pos + "\t" + ref + "\t" + change //
								+ "\t" + codonsAroundOld + "/" + codonsAroundNew //
								+ "\t" + aaAroundOld + "/" + aaAroundNew //
								+ "\t" + String.format("%.2f", purityChange);

						System.out.println(lineOut);
						out.append(lineOut + "\n");
					} else {
						countIgnored++;
						if (debug) Log.debug("Change is no one codon: ignored");
					}
				} else {
					countIgnored++;
					if (debug) Log.debug("Change '" + change + "' ignored");
				}
			} else {
				countIgnored++;
				if (debug) Log.debug("Line has not enough fields: ignored");
			}
		}

		System.out.println("OK:" + countOk + "\tIgnored:" + countIgnored + "\tTotal:" + (countOk + countIgnored));
		Gpr.toFile("/tmp/purity.txt", out);
	}

	double maxPurity(HashMap<String, Integer> count) {
		//---
		// Find the amino acid with max count
		//---

		// Count all amino acid
		HashMap<String, Integer> countAa = new HashMap<String, Integer>();
		for (String key : count.keySet()) {
			int cnt = count.get(key);
			String aa = key.split(KEY_SEPARATOR)[0];
			add(countAa, aa, cnt);
		}

		// Find AA with largest count
		int maxCountAa = 0;
		String maxAa = "";
		for (String aa : countAa.keySet()) {
			int cntAa = countAa.get(aa);
			if (maxCountAa < cntAa) {
				maxCountAa = cntAa;
				maxAa = aa;
			}
		}

		//---
		// Now find max and total count values for maxAa
		//---
		int totalMaxAa = 0, maxCountCodon = 0;
		String maxCodon = "";
		for (String key : count.keySet()) {
			String aa = key.split(KEY_SEPARATOR)[0];
			if (aa.equals(maxAa)) { // Is this count related to maxAa?
				int cnt = count.get(key);
				totalMaxAa += cnt;
				if (maxCountCodon < cnt) { // New maximum for a codon?
					maxCountCodon = cnt;
					maxCodon = key.split(KEY_SEPARATOR)[1];
				}
			}
		}

		double purity = 0;
		if (maxCountCodon >= MIN_COUNT_CODON) purity = ((double) maxCountCodon) / ((double) totalMaxAa);
		if (debug) Log.debug("MaxAa: '" + maxAa + "'\ttotal: " + totalMaxAa + "\tmaxCodon: '" + maxCodon + "'\tcountCodon: " + maxCountCodon + "\t=> " + purity);
		return purity;
	}

	/**
	 * Calculate 'purity' in this string
	 * @param str
	 * @param around
	 * @param CODONS_SIZE
	 * @return
	 */
	double purity(String codon, String around) {
		String pre = around.substring(0, CODONS_SIZE * CODONS_AROUND);
		String post = around.substring(around.length() - CODONS_SIZE * CODONS_AROUND);
		if (debug) Log.debug("codon: '" + codon + "'\tAround: '" + around + "'\tpre: '" + pre + "'\tpost: '" + post + "'");

		// Count all codons
		HashMap<String, Integer> count = new HashMap<String, Integer>();

		// First use 'codons'
		int maxSyms = codon.length() / CODONS_SIZE;
		for (int i = 0; i < maxSyms; i++) {
			String sym = getCodon(codon, i);
			addAa(count, sym, 1);
		}

		// Now count codons 'around' (pre and post)
		for (int i = 0; i < CODONS_AROUND; i++) {
			String cod = getCodon(pre, i);
			addAa(count, cod, 1);

			cod = getCodon(post, i);
			addAa(count, cod, 1);
		}

		// Find AA-codon purity
		return maxPurity(count);
	}

	/**
	 * Calculate purity change
	 * @return
	 */
	double purityChange(String oldcodon, String aroundOld, String newcodon, String aroundNew) {
		double purityOld = purity(oldcodon, aroundOld);
		double purityNew = purity(newcodon, aroundNew);

		double purityChange = purityNew - purityOld;
		if (debug) Log.debug("Purity: " + purityChange + "\tPurity OLD: " + purityOld + "\tPurity NEW: " + purityNew);

		return purityChange;
	}

}
