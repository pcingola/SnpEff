package ca.mcgill.mcb.pcingola.vcf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Custom;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intergenic;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.interval.NextProt;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.FunctionalClass;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Tuple;

/**
 * An 'ANN' or 'EFF' entry in a VCF INFO field
 * Note: 'EFF' is the old version that has been replaced by the standardized 'ANN' field (2014-12)
 * *
 * @author pablocingolani
 */
public class VcfEffect {

	public static boolean debug = false;

	public static final String VCF_INFO_EFF_NAME = "EFF";
	public static final String VCF_INFO_ANN_NAME = "ANN";
	public static final String[] VCF_INFO_ANN_NAMES = { VCF_INFO_ANN_NAME, VCF_INFO_EFF_NAME };

	public static final String EFFECT_TYPE_SEPARATOR = "&"; // Separator between mutiple effectTypes
	public static final String EFFECT_TYPE_SEPARATOR_OLD = "+"; // Old separator between mutiple effectTypes

	private static HashMap<EffFormatVersion, HashMap<String, Integer>> fieldName2Num = new HashMap<EffFormatVersion, HashMap<String, Integer>>();

	EffFormatVersion formatVersion;
	String vcfFieldString; // Original 'raw' string from VCF Info field
	String vcfFieldStrings[]; // Original 'raw' strings from VCF info field: effectString.split()
	String effString;
	EffectType effectType;
	String effectTypesStr;
	List<EffectType> effectTypes;
	String effectDetails;
	int aaLen, aaPos;
	int cdsLen, cdsPos;
	int cDnaLen, cDnaPos;
	int distance;
	int rank, rankMax;
	String bioType;
	String codon, aa, hgvsC, hgvsP;
	VariantEffect.Coding coding;
	String genotype;
	String errorsWarnings;
	String geneName, geneId, featureType, featureId, transcriptId, exonId;
	VariantEffect.EffectImpact impact;
	VariantEffect.FunctionalClass funClass;
	VariantEffect variantEffect;
	boolean useSequenceOntology = true, useHgvs = true, useGeneId = false;

	/**
	 * Convert from field name to field number
	 */
	public static int fieldNum(String name, EffFormatVersion formatVersion) {
		if (formatVersion == null) formatVersion = EffFormatVersion.DEFAULT_FORMAT_VERSION;

		HashMap<String, Integer> f2n = fieldName2Num.get(formatVersion);

		// Not created yet?
		if (f2n == null) {
			if (formatVersion.isAnn()) f2n = mapAnn2Num(formatVersion);
			else f2n = mapEff2Num(formatVersion);

			fieldName2Num.put(formatVersion, f2n);
		}

		// Query by name
		Integer num = f2n.get(name);
		if (num == null) return -1; // Not found?

		return num;
	}

	/**
	 * Get info field name based on format version
	 */
	public static String infoFieldName(EffFormatVersion formatVersion) {
		if (formatVersion == null) return VCF_INFO_ANN_NAME;
		return formatVersion.infoFieldName();
	}

	/**
	 * Create a hash to map names to field numbers on 'ANN' fields
	 */
	public static HashMap<String, Integer> mapAnn2Num(EffFormatVersion formatVersion) {
		HashMap<String, Integer> f2n = new HashMap<String, Integer>();

		// Use both names
		for (String annFieldName : VCF_INFO_ANN_NAMES) {
			int fieldNum = 0;

			f2n.put(annFieldName + ".ALLELE", fieldNum);
			f2n.put(annFieldName + ".GT", fieldNum);
			f2n.put(annFieldName + ".GENOTYPE", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".EFFECT", fieldNum);
			f2n.put(annFieldName + ".ANNOTATION", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".IMPACT", fieldNum++);

			f2n.put(annFieldName + ".GENE", fieldNum++);

			f2n.put(annFieldName + ".GENEID", fieldNum++);

			f2n.put(annFieldName + ".FEATURE", fieldNum++);

			// Feature ID or transcript ID
			f2n.put(annFieldName + ".FEATUREID", fieldNum);
			f2n.put(annFieldName + ".TRID", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".BIOTYPE", fieldNum++);

			// This one used to be called exonID, now it is used for exon OR intron rank
			f2n.put(annFieldName + ".RANK", fieldNum);
			f2n.put(annFieldName + ".EXID", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".HGVS_C", fieldNum);
			f2n.put(annFieldName + ".HGVS_DNA", fieldNum);
			f2n.put(annFieldName + ".CODON", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".HGVS", fieldNum);
			f2n.put(annFieldName + ".HGVS_P", fieldNum);
			f2n.put(annFieldName + ".HGVS_PROT", fieldNum);
			f2n.put(annFieldName + ".AA", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".POS_CDNA", fieldNum);
			f2n.put(annFieldName + ".CDNA_POS", fieldNum);
			f2n.put(annFieldName + ".LEN_CDNA", fieldNum);
			f2n.put(annFieldName + ".CDNA_LEN", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".POS_CDS", fieldNum);
			f2n.put(annFieldName + ".CDS_POS", fieldNum);
			f2n.put(annFieldName + ".LEN_CDS", fieldNum);
			f2n.put(annFieldName + ".CDS_LEN", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".POS_AA", fieldNum);
			f2n.put(annFieldName + ".AA_POS", fieldNum);
			f2n.put(annFieldName + ".LEN_AA", fieldNum);
			f2n.put(annFieldName + ".AA_LEN", fieldNum);
			fieldNum++;

			f2n.put(annFieldName + ".DISTANCE", fieldNum++);

			f2n.put(annFieldName + ".ERRORS", fieldNum);
			f2n.put(annFieldName + ".WARNINGS", fieldNum);
			f2n.put(annFieldName + ".INFOS", fieldNum);
			fieldNum++;
		}

		return f2n;
	}

	/**
	 * Create a hash to map names to field numbers on 'EFF' fields
	 */
	public static HashMap<String, Integer> mapEff2Num(EffFormatVersion formatVersion) {
		HashMap<String, Integer> f2n = new HashMap<String, Integer>();

		for (String annFieldName : VCF_INFO_ANN_NAMES) {
			int fieldNum = 0;

			f2n.put(annFieldName + ".EFFECT", fieldNum++);
			f2n.put(annFieldName + ".IMPACT", fieldNum++);
			f2n.put(annFieldName + ".FUNCLASS", fieldNum++);
			f2n.put(annFieldName + ".CODON", fieldNum++);

			// This field can be called either AA or HGVS
			f2n.put(annFieldName + ".AA", fieldNum);
			f2n.put(annFieldName + ".HGVS", fieldNum);
			fieldNum++;

			if (formatVersion != EffFormatVersion.FORMAT_EFF_2) {
				f2n.put(annFieldName + ".AA_LEN", fieldNum++);
			}

			f2n.put(annFieldName + ".GENE", fieldNum++);
			f2n.put(annFieldName + ".BIOTYPE", fieldNum++);
			f2n.put(annFieldName + ".CODING", fieldNum++);
			f2n.put(annFieldName + ".TRID", fieldNum++);

			// This one used to be called exonID, now it is used for exon OR intron rank
			f2n.put(annFieldName + ".RANK", fieldNum);
			f2n.put(annFieldName + ".EXID", fieldNum);
			fieldNum++;

			if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
				// This one can be called  in different ways
				f2n.put(annFieldName + ".GT", fieldNum);
				f2n.put(annFieldName + ".GENOTYPE_NUMBER", fieldNum);
				f2n.put(annFieldName + ".GENOTYPE", fieldNum);
				fieldNum++;
			}

			f2n.put(annFieldName + ".ERRORS", fieldNum);
			f2n.put(annFieldName + ".WARNINGS", fieldNum);
			f2n.put(annFieldName + ".INFO", fieldNum);
			fieldNum++;
		}

		return f2n;
	}

	/**
	 * Return a string safe to be used in an 'EFF' info field (VCF file)
	 */
	public static String vcfEffSafe(String str) {
		return str.replaceAll("(\\s|\\(|\\)|\\[|\\]|;|,|\\|)+", "_");
	}

	/**
	 * Constructor: Guess format version
	 */
	public VcfEffect(String effectString) {
		formatVersion = null; // Force guess
		vcfFieldString = effectString;
		init();
		parse();
	}

	/**
	 * Constructor: Force format version
	 * @param effStr
	 * @param formatVersion : If null, will try to guess it
	 */
	public VcfEffect(String effectString, EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion;
		init();
		vcfFieldString = effectString;
		parse();
	}

	public VcfEffect(VariantEffect variantEffect, EffFormatVersion formatVersion, boolean useSequenceOntology) {
		this.formatVersion = formatVersion;
		this.variantEffect = variantEffect;
		this.useSequenceOntology = useSequenceOntology;
		init();
		set(variantEffect);
	}

	/**
	 * Add subfield to a buffer
	 */
	void add(StringBuilder sb, Object obj) {
		sb.append(VcfEntry.vcfInfoSafe(obj.toString()));
		sb.append("|");
	}

	public void addEffectType(EffectType effectType) {
		effectTypes.add(effectType);
		this.effectType = null;
	}

	/**
	 * Create 'ANN' field
	 */
	String createAnnField() {
		StringBuilder effBuff = new StringBuilder();

		// Allele
		add(effBuff, genotype);

		// Add main annotation in Sequence Ontology terms
		add(effBuff, effectTypesStr);

		// Add effect impact
		add(effBuff, impact);

		// Gene name
		add(effBuff, geneName);

		// Gene ID
		add(effBuff, geneId);

		// Feature type
		add(effBuff, featureType);

		// Feature ID
		add(effBuff, featureId);

		// Transcript biotype
		add(effBuff, bioType);

		// Add exon (or intron) rank info
		if (rank >= 0) add(effBuff, rank + "/" + rankMax);
		else effBuff.append("|");

		// HGVS
		add(effBuff, hgvsC);
		add(effBuff, hgvsP);

		// cDNA position / length
		if (cDnaPos >= 0) {
			add(effBuff, cDnaPos + "/" + cDnaLen);
		} else effBuff.append("|");

		// CDS position / length
		if (cdsPos >= 0) {
			add(effBuff, cdsPos + "/" + cdsLen);
		} else effBuff.append("|");

		// Protein position / protein length
		if (aaPos >= 0) {
			add(effBuff, aaPos + "/" + aaLen);
		} else effBuff.append("|");

		// Distance: Mostly used for non-coding variants
		if (distance >= 0) add(effBuff, distance);
		else effBuff.append("|");

		// Errors or warnings (this is the last thing in the list)
		effBuff.append(errorsWarnings);

		return effBuff.toString();

	}

	/**
	 * Create 'EFF' field
	 */
	String createEffField() {
		StringBuilder effBuff = new StringBuilder();

		// Add effect
		effBuff.append(effectTypesStr);
		effBuff.append("(");

		// Add effect impact
		effBuff.append(impact);
		effBuff.append("|");

		// Add functional class
		effBuff.append(funClass == FunctionalClass.NONE ? "" : funClass.toString()); // Show only if it is not empty
		effBuff.append("|");

		// Codon change
		if (!codon.isEmpty()) effBuff.append(codon);
		else if (distance >= 0) effBuff.append(distance);
		effBuff.append("|");

		// Add HGVS (amino acid change)
		if (useHgvs) {
			StringBuilder hgvs = new StringBuilder();
			if (hgvsP != null) hgvs.append(hgvsP);
			if (hgvsC != null) {
				if (hgvs.length() > 0) hgvs.append('/');
				hgvs.append(hgvsC);
			}

			effBuff.append(hgvs.toString());
		} else effBuff.append(aa);
		effBuff.append("|");

		// Add amino acid length
		if (formatVersion != EffFormatVersion.FORMAT_EFF_2) { // This field is not in format version 2
			effBuff.append(aaLen >= 0 ? aaLen : "");
			effBuff.append("|");
		}

		// Add gene info
		// TODO: Code here should not be using 'variantEffect'
		Gene gene = variantEffect.getGene();
		Transcript tr = variantEffect.getTranscript();
		if (gene != null) {
			// Gene name
			effBuff.append(VcfEntry.vcfInfoSafe(useGeneId ? geneId : geneName));
			effBuff.append("|");

			// Transcript biotype
			if (tr != null) {
				if ((tr.getBioType() != null) && !tr.getBioType().isEmpty()) effBuff.append(tr.getBioType());
				else effBuff.append(tr.isProteinCoding() ? "protein_coding" : ""); // No biotype? Add protein_coding of we know it is.
			}
			effBuff.append("|");

			// Protein coding gene?
			String coding = "";
			if (gene.getGenome().hasCodingInfo()) coding = (gene.isProteinCoding() ? VariantEffect.Coding.CODING.toString() : VariantEffect.Coding.NON_CODING.toString());
			effBuff.append(coding);
			effBuff.append("|");
		} else if (variantEffect.isRegulation()) {
			Regulation reg = (Regulation) variantEffect.getMarker();
			effBuff.append("|" + reg.getCellType() + "||");
		} else if (variantEffect.isCustom()) {
			Marker m = variantEffect.getMarker();
			if (m != null) effBuff.append("|" + VcfEntry.vcfInfoSafe(m.getId()) + "||");
			else effBuff.append("|||");
		} else effBuff.append("|||");

		// Add transcript info
		effBuff.append(VcfEntry.vcfInfoSafe(transcriptId));
		effBuff.append("|");

		// Add exon (or intron) rank info
		effBuff.append(rank >= 0 ? rank : "");

		// Add genotype (or genotype difference) for this effect
		if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
			effBuff.append("|");
			effBuff.append(genotype);
		}

		//---
		// Errors or warnings (this is the last thing in the list)
		//---
		if (!errorsWarnings.isEmpty()) {
			effBuff.append("|");
			effBuff.append(errorsWarnings);
		}
		effBuff.append(")");

		return effBuff.toString();
	}

	/**
	 * Create INFO field using either 'ANN' or 'EFF' depending on format version
	 */
	String createInfoField() {
		if (formatVersion == null || formatVersion.isAnn()) return createAnnField();
		return createEffField();
	}

	/**
	 * Guess effect format version
	 */
	public EffFormatVersion formatVersion() {
		// Already set?
		if (formatVersion != null && formatVersion.isFullVersion()) return formatVersion;

		// Try to guess format
		if (formatVersion == null) formatVersion = formatVersion(vcfFieldString);

		// Split strings
		if (vcfFieldStrings == null) vcfFieldStrings = split(vcfFieldString);

		// Now we can guess specific sub-version within each format
		if (formatVersion.isAnn()) {
			// Easy guess: So far there is only one version
			formatVersion = EffFormatVersion.FORMAT_ANN_1;
		} else if (formatVersion.isEff()) {
			// On of the 'EFF' formats

			int len = vcfFieldStrings.length;

			// Error or Warning string is not added under normal situations
			String lastField = vcfFieldStrings[len - 2]; // Actually last array item is after the last ')', so we use the previous one
			if (lastField.startsWith("ERROR") //
					|| lastField.startsWith("WARNING") //
					|| lastField.startsWith("INFO") //
			) len--;

			// Guess format
			if (len <= 11) formatVersion = EffFormatVersion.FORMAT_EFF_2;
			else if (len <= 12) formatVersion = EffFormatVersion.FORMAT_EFF_3;
			else formatVersion = EffFormatVersion.FORMAT_EFF_4;
		} else {
			throw new RuntimeException("Unimplemented formatVersion '" + formatVersion + "'");
		}

		return formatVersion;
	}

	/**
	 * Guess format 'main' version (either 'ANN' of 'EFF') without trying to guess sub-version
	 */
	protected EffFormatVersion formatVersion(String effectString) {
		// Extract string between left and right parenthesis
		int idxLp = effectString.indexOf('(');

		// No parenthesis at all? Definitively 'ANN'
		if (idxLp < 0) return EffFormatVersion.FORMAT_ANN;

		// Probably 'EFF': how many sub fields between parenthesis?
		int idxRp = effectString.lastIndexOf(')');
		if (idxLp < idxRp) {
			String paren = effectString.substring(idxLp + 1, idxRp);
			String fields[] = paren.split("\\|", -1);
			if (fields.length >= 9) return EffFormatVersion.FORMAT_EFF;
		}

		// Too few sub-fields: It cannot be 'EFF'
		return EffFormatVersion.FORMAT_ANN;
	}

	public String getAa() {
		return aa;
	}

	public int getAaLen() {
		return aaLen;
	}

	public int getAaPos() {
		return aaPos;
	}

	public String getAllele() {
		return genotype;
	}

	public String getBioType() {
		return bioType;
	}

	public int getcDnaLen() {
		return cDnaLen;
	}

	public int getcDnaPos() {
		return cDnaPos;
	}

	public int getCdsLen() {
		return cdsLen;
	}

	public int getCdsPos() {
		return cdsPos;
	}

	public VariantEffect.Coding getCoding() {
		return coding;
	}

	public String getCodon() {
		return codon;
	}

	public int getDistance() {
		return distance;
	}

	public String getEffectDetails() {
		return effectDetails;
	}

	public String getEffectsStr() {
		StringBuilder sb = new StringBuilder();
		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append(formatVersion.separator());
			sb.append(et);
		}
		return sb.toString();
	}

	public String getEffectsStrSo() {
		StringBuilder sb = new StringBuilder();
		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append(formatVersion.separator());
			sb.append(et.toSequenceOntology(formatVersion));
		}
		return sb.toString();
	}

	public EffectType getEffectType() {
		if (effectType != null) return effectType;
		if (effectTypes == null || effectTypes.isEmpty()) return EffectType.NONE;

		// Pick highest effect type
		effectType = EffectType.NONE;
		for (EffectType et : effectTypes)
			if (et.compareTo(effectType) < 0) effectType = et;

		return effectType;
	}

	public List<EffectType> getEffectTypes() {
		return effectTypes;
	}

	public String getEffectTypesStr() {
		return effectTypesStr;
	}

	public String getEffString() {
		return effString;
	}

	public String getErrorsWarning() {
		return errorsWarnings;
	}

	public String getExonId() {
		return exonId;
	}

	public String getFeatureId() {
		return featureId;
	}

	public String getFeatureType() {
		return featureType;
	}

	public EffFormatVersion getFormatVersion() {
		return formatVersion;
	}

	public VariantEffect.FunctionalClass getFunClass() {
		return funClass;
	}

	public String getGeneId() {
		return geneId;
	}

	public String getGeneName() {
		return geneName;
	}

	public String getGenotype() {
		return genotype;
	}

	public String getHgvsC() {
		return hgvsC;
	}

	public String getHgvsDna() {
		return hgvsC;
	}

	public String getHgvsP() {
		return hgvsP;
	}

	public String getHgvsProt() {
		return hgvsP;
	}

	public VariantEffect.EffectImpact getImpact() {
		return impact;
	}

	public int getRank() {
		return rank;
	}

	public int getRankMax() {
		return rankMax;
	}

	public String getTranscriptId() {
		return transcriptId;
	}

	/**
	 * String from VCF file (original, unparsed, string)
	 */
	public String getVcfFieldString() {
		return vcfFieldString;
	}

	/**
	 * Get a subfield as an index
	 */
	public String getVcfFieldString(int index) {
		if (index >= vcfFieldStrings.length) return null;
		return vcfFieldStrings[index];
	}

	public boolean hasEffectType(EffectType effType) {
		if (effectTypes == null) return false;
		for (EffectType et : effectTypes)
			if (et == effType) return true;
		return false;

	}

	void init() {
		aaLen = aaPos = cdsLen = cdsPos = cDnaLen = cDnaPos = distance = rank = rankMax = -1;
		vcfFieldString = effString = effectTypesStr = effectDetails = bioType = codon = aa = hgvsC = hgvsP = genotype = errorsWarnings = geneName = geneId = featureType = featureId = transcriptId = exonId = errorsWarnings = "";
		impact = null;
		funClass = FunctionalClass.NONE;
	}

	/**
	 * Parse annotations either in 'ANN' or 'EFF' INFO field
	 */
	void parse() {
		// Guess format, if not given
		if (formatVersion == null || !formatVersion.isFullVersion()) formatVersion = formatVersion();

		// Split strings
		vcfFieldStrings = split(vcfFieldString);

		// Parse
		if (formatVersion.isAnn()) parseAnn();
		else parseEff();
	}

	/**
	 * Parse 'ANN' field
	 */
	void parseAnn() {
		int index = 0;

		// Gentype
		genotype = vcfFieldStrings[index++];

		// Annotation
		effString = vcfFieldStrings[index];
		effectTypesStr = vcfFieldStrings[index];
		effectTypes = parseEffect(vcfFieldStrings[index]);
		index++;

		// Impact
		impact = VariantEffect.EffectImpact.valueOf(vcfFieldStrings[index++]);

		// Gene name
		geneName = vcfFieldStrings[index++];

		// Gene ID
		geneId = vcfFieldStrings[index++];

		// Feature type
		featureType = vcfFieldStrings[index++];

		// Feature ID
		featureId = vcfFieldStrings[index++];
		if (featureType.equals("transcript")) transcriptId = featureId;

		// Biotype
		bioType = vcfFieldStrings[index++];

		// Rank '/' rankMax
		Tuple<Integer, Integer> ints = parseSlash(vcfFieldStrings[index++]);
		rank = ints.first;
		rankMax = ints.second;

		// HGVS
		hgvsC = vcfFieldStrings[index++];
		hgvsP = vcfFieldStrings[index++];

		// cDna: 'pos / len'
		ints = parseSlash(vcfFieldStrings[index++]);
		cDnaPos = ints.first;
		cDnaLen = ints.second;

		// CDS: 'pos / len'
		ints = parseSlash(vcfFieldStrings[index++]);
		cdsPos = ints.first;
		cdsLen = ints.second;

		// AA: 'pos / len'
		ints = parseSlash(vcfFieldStrings[index++]);
		aaPos = ints.first;
		aaLen = ints.second;

		// Distance
		distance = Gpr.parseIntSafe(vcfFieldStrings[index++]);

		// Errors , warnings, info
		errorsWarnings = vcfFieldStrings[index++];
	}

	/**
	 * Parse 'EFF' field
	 */
	void parseEff() {
		try {
			// Parse each sub field
			int index = 0;

			// Effect
			effString = vcfFieldStrings[index];
			effectTypesStr = vcfFieldStrings[index];
			effectTypes = parseEffect(vcfFieldStrings[index]);
			effectDetails = parseEffectDetails(vcfFieldStrings[index]); // Effect details: everything between '['  and ']' (e.g. Regulation, Custom, Motif, etc.)
			index++;

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) impact = VariantEffect.EffectImpact.valueOf(vcfFieldStrings[index]);
			index++;

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) funClass = VariantEffect.FunctionalClass.valueOf(vcfFieldStrings[index]);
			index++;

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) codon = vcfFieldStrings[index];
			index++;

			// Parse 'AA' and HGVS
			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) aa = vcfFieldStrings[index];
			if (aa.indexOf('/') > 0) {
				String f[] = aa.split("/");

				// HGVS Protein
				if (f.length > 0 && f[0].startsWith("p.")) hgvsP = f[0];

				// HGVS DNA
				if ((f.length > 1) && (f[1].startsWith("c.") || f[1].startsWith("n."))) hgvsC = f[1];
			} else if (aa.startsWith("c.") || aa.startsWith("n.")) hgvsC = aa; // Only HGVS DNA

			index++;

			if (formatVersion != EffFormatVersion.FORMAT_EFF_2) {
				if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) aaLen = Gpr.parseIntSafe(vcfFieldStrings[index]);
				else aaLen = 0;
				index++;
			}

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) geneName = vcfFieldStrings[index];
			index++;

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) bioType = vcfFieldStrings[index];
			index++;

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) coding = VariantEffect.Coding.valueOf(vcfFieldStrings[index]);
			index++;

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) transcriptId = vcfFieldStrings[index];
			index++;

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) exonId = vcfFieldStrings[index];
			index++;

			if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
				if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) genotype = vcfFieldStrings[index];
				else genotype = "";
				index++;
			}

			if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) errorsWarnings = vcfFieldStrings[index];
			index++;

		} catch (Exception e) {
			String fields = "";
			for (int i = 0; i < vcfFieldStrings.length; i++)
				fields += "\t" + i + " : '" + vcfFieldStrings[i] + "'\n";
			throw new RuntimeException("Error parsing:\n\t'" + vcfFieldString + "'\n\t EFF formatVersion : " + formatVersion + "\n" + fields, e);
		}
	}

	List<EffectType> parseEffect(String eff) {
		int idx = eff.indexOf('[');
		if (idx > 0) eff = eff.substring(0, idx);

		List<EffectType> effs = new LinkedList<EffectType>();
		if (eff.isEmpty()) return effs;

		// Split multiple effectTypes
		if (eff.indexOf(formatVersion.separator()) >= 0) {
			// Old version
			for (String es : eff.split(formatVersion.separatorSplit()))
				effs.add(EffectType.parse(formatVersion, es));
		} else {
			// Split effect strings
			for (String es : eff.split(formatVersion.separatorSplit()))
				effs.add(EffectType.parse(formatVersion, es));
		}

		return effs;
	}

	/**
	 * Parse effect details.
	 * E.g. NEXT_PROT[amino_acid_modification:Phosphoserine]  returns "amino_acid_modification:Phosphoserine"
	 */
	String parseEffectDetails(String eff) {
		// TODO: ("Check that nextprot doesn't include '&' in the names");

		int idx = eff.indexOf('[');
		if (idx < 0) return "";
		return eff.substring(idx + 1, eff.length() - 1);
	}

	/**
	 * Parse two integers separated by a slash
	 */
	Tuple<Integer, Integer> parseSlash(String str) {
		int i1 = -1, i2 = -1;

		if (str != null && !str.isEmpty()) {
			String fields[] = str.split("/");

			if (fields.length >= 2) {
				// Two numbers separated by a slash
				i1 = Gpr.parseIntSafe(fields[0]);
				i2 = Gpr.parseIntSafe(fields[1]);
			} else {
				// Only one number?
				i1 = Gpr.parseIntSafe(fields[0]);
			}
		}

		return new Tuple<Integer, Integer>(i1, i2);
	}

	/**
	 * Set all fields form 'variantEffect'
	 */
	void set(VariantEffect variantEffect) {
		// Allele
		Variant var = variantEffect.getVariant();
		Gene g = variantEffect.getGene();
		Marker marker = variantEffect.getMarker();
		Transcript tr = variantEffect.getTranscript();

		// Genotype
		if (!var.isVariant()) genotype = "";
		else if (var.isNonRef()) genotype = var.getGenotype();
		else genotype = var.getAlt();

		// Effect
		effectType = variantEffect.getEffectType();
		effectTypes = variantEffect.getEffectTypes();

		if (formatVersion.isAnn()) {
			effectTypesStr = variantEffect.getEffectTypeString(true, formatVersion.separator());
		} else {
			effectTypesStr = variantEffect.effect(true, false, false, useSequenceOntology);
		}

		// Impact
		impact = variantEffect.getEffectImpact();

		// Functional class
		funClass = variantEffect.getFunctionalClass();

		// Gene
		if (g != null) {
			geneName = g.getGeneName();
			geneId = g.getId();
		} else if (marker instanceof Intergenic) {
			geneName = ((Intergenic) marker).getName();
			geneId = marker.getId();
		} else {
			geneName = geneId = "";
		}

		// Feature type & ID
		featureType = featureId = "";
		if (marker != null) {
			if (marker instanceof Custom) {
				// Custom
				featureType = marker.getType() + formatVersion.separator() + ((Custom) marker).getLabel();
				featureId = marker.getId();
			} else if (marker instanceof Regulation) {
				// Regulation includes cell type
				Regulation reg = (Regulation) marker;
				featureType = reg.getType() + formatVersion.separator() + reg.getName() + ":" + reg.getCellType();
				featureId = marker.getId();
			} else if (marker instanceof NextProt) {
				featureType = marker.getId();
				featureId = ((NextProt) marker).getTranscriptId();
			} else if (marker instanceof Motif) {
				Motif motif = (Motif) marker;
				featureType = motif.getPwmName();
				featureId = motif.getPwmId();
			} else if (tr != null) {
				featureType = "transcript";
				featureId = transcriptId = tr.getId();
			} else {
				featureType = marker.getType().toSequenceOntology(formatVersion);
				featureId = marker.getId();
			}
		}

		// Biotype
		if (tr != null) {
			if ((tr.getBioType() != null) && !tr.getBioType().isEmpty()) {
				bioType = tr.getBioType();
			} else {
				// No biotype? Add protein_coding of we know it is.
				bioType = tr.isProteinCoding() ? "Coding" : "Noncoding";
			}
		} else {
			bioType = "";
		}

		// Rank
		Exon ex = variantEffect.getExon();
		rank = -1;
		if (ex != null) {
			rank = ex.getRank();
			rankMax = tr.numChilds();
		} else {
			// Do we have an intron?
			Intron intron = variantEffect.getIntron();
			if (intron != null) {
				rank = intron.getRank();
				rankMax = Math.max(0, tr.numChilds() - 1);
			}
		}

		// Codon change
		codon = variantEffect.getCodonChangeMax();

		// AA change
		aa = variantEffect.getAaChange();

		// HGVS notation
		hgvsC = variantEffect.getHgvsDna();
		hgvsP = variantEffect.getHgvsProt();

		// cDna position & len (cDNA is the DNA version of mRNA)
		if (tr != null) {
			cDnaPos = variantEffect.getcDnaPos();
			if (cDnaPos >= 0 && formatVersion.isAnn()) cDnaPos++; // 1-based position;
			cDnaLen = tr.mRna().length();
		} else {
			cDnaPos = cDnaLen = -1;
		}

		// CDS position / length
		if (tr != null) {
			cdsPos = variantEffect.getCodonNum() * 3 + variantEffect.getCodonIndex();
			if (cdsPos >= 0 && formatVersion.isAnn()) cdsPos++; // 1-based position;
			cdsLen = variantEffect.getCdsLength();
		} else {
			cdsPos = cdsLen = -1;
		}

		// Protein position / protein length
		if (tr != null) {
			aaPos = variantEffect.getCodonNum();
			if (aaPos >= 0 && formatVersion.isAnn()) aaPos++; // 1-based position;
			aaLen = variantEffect.getAaLength();
		} else {
			aaPos = aaLen = -1;
		}

		// Distance: Mostly used for non-coding variants
		distance = variantEffect.getDistance();

		if (variantEffect.hasError() || variantEffect.hasWarning()) {
			StringBuilder err = new StringBuilder();
			// Add errors
			if (!variantEffect.getError().isEmpty()) {
				err.append(variantEffect.getError());
			}

			// Add warnings
			if (!variantEffect.getWarning().isEmpty()) {
				if (err.length() > 0) err.append(formatVersion.separator());
				err.append(variantEffect.getWarning());
			}

			errorsWarnings = err.toString();
		}

	}

	public void setAa(String aa) {
		this.aa = aa;
	}

	public void setAaLen(int aaLen) {
		this.aaLen = aaLen;
	}

	public void setBioType(String bioType) {
		this.bioType = bioType;
	}

	public void setCoding(VariantEffect.Coding coding) {
		this.coding = coding;
	}

	public void setCodon(String codon) {
		this.codon = codon;
	}

	public void setEffectDetails(String effectDetails) {
		this.effectDetails = effectDetails;
	}

	public void setEffectType(EffectType effect) {
		effectTypes = new LinkedList<EffectType>();
		addEffectType(effect);
	}

	public void setExonId(String exonId) {
		this.exonId = exonId;
	}

	public void setFormatVersion(EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion;
	}

	public void setFunClass(VariantEffect.FunctionalClass funClass) {
		this.funClass = funClass;
	}

	public void setGeneId(String geneId) {
		this.geneId = geneId;
	}

	public void setGeneName(String geneName) {
		this.geneName = geneName;
	}

	public void setGenotype(String genotype) {
		this.genotype = genotype;
	}

	public void setImpact(VariantEffect.EffectImpact impact) {
		this.impact = impact;
	}

	public void setTranscriptId(String transcriptId) {
		this.transcriptId = transcriptId;
	}

	public void setUseGeneId(boolean useGeneId) {
		this.useGeneId = useGeneId;
	}

	public void setUseHgvs(boolean useHgvs) {
		this.useHgvs = useHgvs;
	}

	/**
	 * Split a 'effect' string to an array of strings
	 */
	public String[] split(String eff) {
		// ANN format versions
		if (formatVersion.isAnn()) {
			// Negative number means "use trailing empty as well"
			return eff.replace('|', '\t').split("\t", -1);
		}

		// EFF format version
		if (formatVersion.isEff()) {
			int idxBr = eff.indexOf('[');
			int idxParen = eff.indexOf('(');

			String eff0 = null;
			if ((idxBr >= 0) && (idxBr < idxParen)) {
				int idxRbr = eff.indexOf(']');
				eff0 = eff.substring(0, idxRbr + 1);
				eff = eff.substring(idxRbr);
			}

			eff = eff.replace('(', '\t'); // Replace all chars by spaces
			eff = eff.replace('|', '\t');
			eff = eff.replace(')', '\t');
			String effs[] = eff.split("\t", -1); // Negative number means "use trailing empty as well"

			if (eff0 != null) effs[0] = eff0;

			return effs;
		}

		throw new RuntimeException("Unimplemented format version '" + formatVersion + "'");
	}

	@Override
	public String toString() {
		// Create from variant?
		if (variantEffect != null) return createInfoField();

		// Create from parsed fields
		StringBuilder sb = new StringBuilder();

		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append(formatVersion.separator());
			sb.append(et);
		}

		if ((effectDetails != null) && !effectDetails.isEmpty()) sb.append("[" + effectDetails + "]");
		sb.append("(");

		if (impact != null) sb.append(impact);
		sb.append("|");

		if (funClass != null) sb.append(funClass);
		sb.append("|");

		if (codon != null) sb.append(codon);
		sb.append("|");

		if (aa != null) sb.append(aa);
		sb.append("|");

		if (aaLen > 0) sb.append(aaLen);
		sb.append("|");

		if (geneName != null) sb.append(geneName);
		sb.append("|");

		if (bioType != null) sb.append(bioType);
		sb.append("|");

		if (coding != null) sb.append(coding);
		sb.append("|");

		if (transcriptId != null) sb.append(transcriptId);
		sb.append("|");

		if (exonId != null) sb.append(exonId);
		sb.append("|");

		if (genotype != null) sb.append(genotype);

		if (errorsWarnings != null) sb.append("|" + errorsWarnings);

		sb.append(")");

		return sb.toString();
	}

}
