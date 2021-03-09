package org.snpeff.vcf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snpeff.util.Log;

/**
 * An 'CSQ' entry in a vcf line ('Consequence' from ENSEMBL's VEP)
 *
 * Format:
 * ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND|SYMBOL|SYMBOL_SOURCE|HGNC_ID|BIOTYPE|SIFT|PolyPhen|DOMAINS|HGVSc|HGVSp|GMAF|AFR_MAF|AMR_MAF|ASN_MAF|EUR_MAF|AA_MAF|EA_MAF|CLIN_SIG|SOMATIC|MOTIF_NAME|MOTIF_POS|HIGH_INF_POS|MOTIF_SCORE_CHANGE">
 *
 * @author pablocingolani
 */
public class VcfConsequence {

	public static final String VCF_INFO_CSQ_NAME = "CSQ";

	VcfConsequenceHeader vcfConsequenceHeader;
	Map<String, String> name2value;

	public static List<VcfConsequence> parse(VcfConsequenceHeader vcfConsequenceHeader, String csqString) {
		List<VcfConsequence> list = new ArrayList<VcfConsequence>();

		// Sanity check
		if (csqString == null || csqString.isEmpty()) return list;

		// Remove name, keep value
		int idx = csqString.indexOf('=');
		if (idx >= 0) {
			if (csqString.startsWith("CSQ=")) throw new RuntimeException("String does not start with 'CSQ=', this is not a CSQ field: " + csqString);
			csqString = csqString.substring(idx);
		}

		// Add all CSQ entries
		for (String csq : csqString.split(","))
			list.add(new VcfConsequence(vcfConsequenceHeader, csq));

		return list;
	}

	public static List<VcfConsequence> parse(VcfConsequenceHeader vcfConsequenceHeader, VcfEntry ve) {
		return parse(vcfConsequenceHeader, ve.getInfo(VCF_INFO_CSQ_NAME));
	}

	public VcfConsequence(VcfConsequenceHeader vcfConsequenceHeader, String csqString) {
		this.vcfConsequenceHeader = vcfConsequenceHeader;
		parseSingle(csqString);
	}

	public String get(String fieldName) {
		return name2value.get(fieldName);
	}

	public String getConsequence() {
		return get("Consequence");
	}

	public String getFeature() {
		return get("Feature");
	}

	public String getHgvsDna() {
		return get("HGVSc");
	}

	public String getHgvsProt() {
		return get("HGVSp");
	}

	/**
	 * Parse a single entry in a CSQ (usually CSQs have multiple entries)
	 */
	void parseSingle(String csqString) {
		try {
			String fieldNames[] = vcfConsequenceHeader.getFieldNames();
			name2value = new HashMap<>();

			String fieldValues[] = csqString.split("\\|", -1);
			for (int i = 0; i < fieldValues.length; i++)
				name2value.put(fieldNames[i], fieldValues[i]);

		} catch (Exception e) {
			Log.debug("Error parsing\n\tCSQ string: " + csqString);
			throw new RuntimeException(e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String fieldNames[] = vcfConsequenceHeader.getFieldNames();
		for (int i = 0; i < fieldNames.length; i++) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(fieldNames[i] + ": " + get(fieldNames[i]));
		}
		return sb.toString();
	}
}
