package org.snpeff.codons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import org.snpeff.binseq.coder.DnaCoder;

/**
 * A codon translation table
 * @author pcingola
 */
public class CodonTable {

	public static final String TERMINATION_CODON = "Ter";
	public static final String TERMINATION_CODON_1 = "*";

	private static HashMap<String, String> aa3letter;

	/**
	 * All start codons are translated as "M".
	 *
	 * Reference: https://en.wikipedia.org/wiki/Start_codon
	 * 		Alternative start codons are different from the standard AUG codon and are found in both
	 * 		prokaryotes (bacteria) and eukaryotes. Alternate start codons are still translated as Met
	 * 		when they are at the start of a protein (even if the codon encodes a different amino acid
	 * 		otherwise). This is because a separate transfer RNA (tRNA) is used for initiation.
	 */
	public static final String DEFAULT_START_CODON = "M";

	static {
		aa3letter = new HashMap<>();
		aa3letter.put("A", "Ala");
		aa3letter.put("B", "Asx");
		aa3letter.put("C", "Cys");
		aa3letter.put("D", "Asp");
		aa3letter.put("E", "Glu");
		aa3letter.put("F", "Phe");
		aa3letter.put("G", "Gly");
		aa3letter.put("H", "His");
		aa3letter.put("I", "Ile");
		aa3letter.put("K", "Lys");
		aa3letter.put("L", "Leu");
		aa3letter.put("M", "Met");
		aa3letter.put("N", "Asn");
		aa3letter.put("P", "Pro");
		aa3letter.put("Q", "Gln");
		aa3letter.put("R", "Arg");
		aa3letter.put("S", "Ser");
		aa3letter.put("T", "Thr");
		aa3letter.put("U", "Sec"); // Selenocysteine (Rare amino acid)
		aa3letter.put("V", "Val");
		aa3letter.put("W", "Trp");
		aa3letter.put("X", "X"); // Old stop codon nomenclature
		aa3letter.put("Y", "Tyr");
		aa3letter.put("Z", "Glx");
		aa3letter.put("*", "*");
	}

	String name;
	HashMap<String, String> codon2aa;
	HashMap<String, String> aa2codon;
	HashSet<String> startCodons;
	HashSet<String> stopCodons;
	HashMap<String, Integer> degeneracy;

	public CodonTable(String name, String table) {
		this.name = name;
		codon2aa = new HashMap<>();
		aa2codon = new HashMap<>();
		startCodons = new HashSet<>();
		stopCodons = new HashSet<>();
		parse(table);
		calcDegeneracy();
	}

	/**
	 * Translate codons to an amino acid sequence
	 */
	public String aa(String codons) {
		return aa(codons, false);
	}

	public String aa(String codons, boolean fullProteinSequence) {
		if (codons.isEmpty()) return "";

		char bases[] = codons.toCharArray();
		StringBuilder aas = new StringBuilder();

		int aaNum = 0;
		for (int i = 0; i < bases.length;) {
			// Append bases to codon
			String cod = "";
			for (int j = 0; (j < 3) && (i < bases.length); j++, i++)
				cod += bases[i];

			// Translate codon to amino acid
			String aa = codon2aa.get(cod.toUpperCase());

			if (aa == null) aa = "?";

			// When translating a full protein sequence, start codons are always translated as 'M'
			if (fullProteinSequence && aaNum == 0 && isStart(cod)) aa = DEFAULT_START_CODON;

			aas.append(aa);
			aaNum++;
		}

		return aas.toString();
	}

	public String aaThreeLetterCode(char aa) {
		if (aa == '*') return TERMINATION_CODON; // Termination codon. Used to be "*" (see reference http://www.hgvs.org/mutnomen/standards.html#aalist)
		String aa3 = aa3letter.get(Character.toString(aa).toUpperCase());
		if (aa3 == null) return "???";
		return aa3;
	}

	/**
	 * Convert 1-letter code to 3-letter code (amino acids)
	 *
	 * Reference: http://www.hgvs.org/mutnomen/standards.html#aalist
	 *
	 * @param Amino acid in three letter code
	 */
	public String aaThreeLetterCode(String aa) {
		// Single character?
		if (aa.length() == 1) return aaThreeLetterCode(aa.charAt(0));

		// Empty?
		if (aa.isEmpty()) return "";

		// Convert each character
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < aa.length(); i++)
			sb.append(aaThreeLetterCode(aa.charAt(i)));

		return sb.toString();
	}

	/**
	 * Calculate degeneracy table
	 */
	void calcDegeneracy() {
		degeneracy = new HashMap<>();

		for (char base1 : DnaCoder.TO_BASE)
			for (char base2 : DnaCoder.TO_BASE)
				for (char base3 : DnaCoder.TO_BASE) {

					char c[] = { base1, base2, base3 };
					String codonStrOld = new String(c);
					String aaOld = aa(codonStrOld);

					for (int pos = 0; pos < 3; pos++) {
						int count = 0;
						for (char baseNew : DnaCoder.TO_BASE) {
							char codon[] = { base1, base2, base3 };
							codon[pos] = baseNew;
							String codonStrNew = new String(codon);
							String aaNew = aa(codonStrNew);

							if (aaOld.equals(aaNew)) count++; // Same AA after changing base[pos]? => count (it's a degenerate)
						}

						String key = codonStrOld.toUpperCase() + "_" + pos;
						degeneracy.put(key, count);
					}
				}
	}

	/**
	 * Translate an amino acid into a codon
	 */
	public String codon(String aa) {
		String codon = aa2codon.get(aa.toUpperCase());
		if (codon == null) return "???";
		return codon;
	}

	/**
	 * Degree of "degenerate site"
	 *
	 * What is a "degenerate site"?
	 *
	 * Here is an explanation form Stephen Wright (Univ. Toronto), who requested to add this feature
	 *
	 *    "...a fourfold degenerate site would be a site where any change is synonymous. So the
	 *    third codon position for the arginine CGN, is a fourfold degenerate site, as is the
	 *    third codon position for valine, alanine, etc.
	 *    Similarly, all second positions of a codon are zerofold degenerate, since any change is
	 *    nonsynonymous. Many first codon positions are also zerofold degenerate, however, for
	 *    example, the first codon position of AGG is NOT zerofold, because a shift to CGG is a
	 *    synonymous change."
	 *
	 * @param codon
	 * @param pos
	 * @return Degenracy level, or '-1' if not available
	 */
	public int degenerate(String codon, int pos) {
		// if( codon.length() != 3 ) throw new RuntimeException("Error: Codon does not have three bases '" + codon + "'");
		String key = codon.toUpperCase() + "_" + pos;
		Integer degLevel = degeneracy.get(key);
		return degLevel != null ? degLevel : -1; // Return '-1'
	}

	public String getName() {
		return name;
	}

	/**
	 * Is 'codon' a start codon in this table?
	 */
	public boolean isStart(String codon) {
		if (codon.length() == 3) return startCodons.contains(codon.toUpperCase());

		for (int i = 0; i < codon.length(); i += 3) {
			int max = Math.min(codon.length(), i + 3);
			String codonSigle = codon.substring(i, max);
			if (startCodons.contains(codonSigle.toUpperCase())) return true;
		}

		return false;
	}

	/**
	 * Is the first codon a 'start' codon?
	 */
	public boolean isStartFirst(String codon) {
		if (codon.length() < 3) return false;
		return isStart(codon.substring(0, 3));
	}

	/**
	 * Is 'codon' a stop codon in this table?
	 */
	public boolean isStop(String codon) {
		if (codon.length() == 3) return stopCodons.contains(codon.toUpperCase());

		for (int i = 0; i < codon.length(); i += 3) {
			int max = Math.min(codon.length(), i + 3);
			String codonSigle = codon.substring(i, max);
			if (stopCodons.contains(codonSigle.toUpperCase())) return true;
		}

		return false;
	}

	/**
	 * Is there a stop codon in this amino acid sequence
	 */
	public boolean isStopAa(String aas) {
		return aas.indexOf('*') >= 0;
	}

	/**
	 * Parse a 'table'
	 * Format: comma separated list of CODON/AA
	 * E.g.: "TTT/F, TTC/F, TTA/L, TTG/L, TCT/S, TCC/S, TCA/S, TCG/S, TAT/Y, TAC/Y, TAA/*, TAG/*, TGT/C, ..."
	 *
	 * Note: A '*' indicated stop codon, a '+' indicates start codon
	 * @param table : Codon table
	 */
	void parse(String table) {
		table = table.toUpperCase().trim();

		String entries[] = table.split(",");
		for (String entry : entries) {
			entry = entry.trim();
			if (entry.length() > 0) {
				String t[] = entry.split("/");
				if (t.length == 2) {
					String codon = t[0];
					String aa = t[1];

					// If it contains a '+' then is is a START codon as well
					if (aa.indexOf('+') >= 0) {
						startCodons.add(codon);
						aa = aa.replaceAll("\\+", ""); // Remove all '+' signs
					}

					// If it contains a '*' then is is a STOP codon
					if (aa.indexOf('*') >= 0) stopCodons.add(codon);

					aa2codon.put(aa, codon);
					codon2aa.put(codon, aa);
				} else throw new RuntimeException("Error reading codon table. Cannot parse entry: '" + entry + "'\n\tTable: '" + table + "'");
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("codon." + name + ": ");

		ArrayList<String> codons = new ArrayList<>();
		codons.addAll(codon2aa.keySet());
		Collections.sort(codons);
		for (String codon : codons)
			sb.append(" " + codon + "/" + aa(codon) + (isStart(codon) ? "+" : "") + ",");
		sb.deleteCharAt(sb.length() - 1); // Remove last comma

		return sb.toString();
	}
}
