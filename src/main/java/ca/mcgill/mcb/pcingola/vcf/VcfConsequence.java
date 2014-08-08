package ca.mcgill.mcb.pcingola.vcf;

import java.util.ArrayList;
import java.util.List;

/**
 * An 'CSQ' entry in a vcf line ('Consequence' from ENSEMBL's VEP)
 *
 * Format:
 * ##INFO=<ID=CSQ,Number=.,Type=String,Description="Consequence type as predicted by VEP. Format: Allele|Gene|Feature|Feature_type|Consequence|cDNA_position|CDS_position|Protein_position|Amino_acids|Codons|Existing_variation|DISTANCE|STRAND|SYMBOL|SYMBOL_SOURCE|HGNC_ID|BIOTYPE|SIFT|PolyPhen|DOMAINS|HGVSc|HGVSp|GMAF|AFR_MAF|AMR_MAF|ASN_MAF|EUR_MAF|AA_MAF|EA_MAF|CLIN_SIG|SOMATIC|MOTIF_NAME|MOTIF_POS|HIGH_INF_POS|MOTIF_SCORE_CHANGE">
 *
 * Fields:
 *      1   Allele
 *      2   Gene
 *      3   Feature
 *      4   Feature_type
 *      5   Consequence
 *      6   cDNA_position
 *      7   CDS_position
 *      8   Protein_position
 *      9   Amino_acids
 *      10  Codons
 *      11  Existing_variation
 *      12  DISTANCE
 *      13  STRAND
 *      14  SYMBOL
 *      15  SYMBOL_SOURCE
 *      16  HGNC_ID
 *      17  BIOTYPE
 *      18  SIFT
 *      19  PolyPhen
 *      20  DOMAINS
 *      21  HGVSc
 *      22  HGVSp
 *      23  GMAF
 *      24  AFR_MAF
 *      25  AMR_MAF
 *      26  ASN_MAF
 *      27  EUR_MAF
 *      28  AA_MAF
 *      29  EA_MAF
 *      30  CLIN_SIG
 *      31  SOMATIC
 *      32  MOTIF_NAME
 *      33  MOTIF_POS
 *      34  HIGH_INF_POS
 *      35  MOTIF_SCORE_CHANGE

 *
 * @author pablocingolani
 */
public class VcfConsequence {

	public static final String VCF_INFO_CSQ_NAME = "CSQ";

	public String allele;
	public String gene;
	public String feature;
	public String featureType;
	public String consequence;
	public String cdnaPosition;
	public String cdsPosition;
	public String proteinPosition;
	public String aminoAcids;
	public String codons;
	public String existingVariation;
	public String distance;
	public String strand;
	public String symbol;
	public String symbolSource;
	public String hgncId;
	public String biotype;
	public String sift;
	public String polyphen;
	public String domains;
	public String hgvsc;
	public String hgvsp;
	public String gmaf;
	public String afrMaf;
	public String amrMaf;
	public String asnMaf;
	public String eurMaf;
	public String aaMaf;
	public String eaMaf;
	public String clinSig;
	public String somatic;
	public String motifName;
	public String motifPos;
	public String highInfPos;
	public String motifScoreChange;

	public static List<VcfConsequence> parse(String csqString) {
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
			list.add(new VcfConsequence(csq));

		return list;
	}

	public static List<VcfConsequence> parse(VcfEntry ve) {
		return parse(ve.getInfo(VCF_INFO_CSQ_NAME));
	}

	/**
	 * Constructor: Guess format version
	 */
	public VcfConsequence(String csqString) {
		parseSingle(csqString);
	}

	/**
	 * Parse a single entry in a CSQ (usually CSQs have multiple entries)
	 */
	void parseSingle(String csqString) {
		String fields[] = csqString.split("\\|", -1);
		int index = 0;

		allele = fields[index++];
		gene = fields[index++];
		feature = fields[index++];
		featureType = fields[index++];
		consequence = fields[index++];
		cdnaPosition = fields[index++];
		cdsPosition = fields[index++];
		proteinPosition = fields[index++];
		aminoAcids = fields[index++];
		codons = fields[index++];
		existingVariation = fields[index++];
		distance = fields[index++];
		strand = fields[index++];
		symbol = fields[index++];
		symbolSource = fields[index++];
		hgncId = fields[index++];
		biotype = fields[index++];
		sift = fields[index++];
		polyphen = fields[index++];
		domains = fields[index++];
		hgvsc = fields[index++];
		hgvsp = fields[index++];
		gmaf = fields[index++];
		afrMaf = fields[index++];
		amrMaf = fields[index++];
		asnMaf = fields[index++];
		eurMaf = fields[index++];
		aaMaf = fields[index++];
		eaMaf = fields[index++];
		clinSig = fields[index++];
		somatic = fields[index++];
		motifName = fields[index++];
		motifPos = fields[index++];
		highInfPos = fields[index++];
		motifScoreChange = fields[index++];
	}

	@Override
	public String toString() {
		return allele + "|" + gene + "|" + feature + "|" + featureType + "|" + consequence + "|" + cdnaPosition + "|" + cdsPosition + "|" + proteinPosition + "|" + aminoAcids + "|" + codons + "|" + existingVariation + "|" + distance + "|" + strand + "|" + symbol + "|" + symbolSource + "|" + hgncId + "|" + biotype + "|" + sift + "|" + polyphen + "|" + domains + "|" + hgvsc + "|" + hgvsp + "|" + gmaf + "|" + afrMaf + "|" + amrMaf + "|" + asnMaf + "|" + eurMaf + "|" + aaMaf + "|" + eaMaf + "|" + clinSig + "|" + somatic + "|" + motifName + "|" + motifPos + "|" + highInfPos + "|" + motifScoreChange;
	}
}
