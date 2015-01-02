package ca.mcgill.mcb.pcingola.vcf;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Custom;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.outputFormatter.VcfOutputFormatter;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.FunctionalClass;
import ca.mcgill.mcb.pcingola.util.Gpr;

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

	public static final String EFFECT_TYPE_SEPARATOR = "&"; // Separator between mutiple effectTypes
	public static final String EFFECT_TYPE_SEPARATOR_OLD = "+"; // Old separator between mutiple effectTypes

	private static HashMap<EffFormatVersion, HashMap<String, Integer>> fieldName2Num;

	String effectString;
	String effectStrings[];
	EffFormatVersion formatVersion;
	String effString;
	EffectType effectType;
	String effectTypesStr;
	List<EffectType> effectTypes;
	String effectDetails;
	VariantEffect.EffectImpact impact;
	VariantEffect.FunctionalClass funClass;
	String codon;
	String aa;
	int aaLen;
	String gene;
	String bioType;
	VariantEffect.Coding coding;
	String transcriptId;
	String exonId;
	String genotype;
	String errorsOrWarning;
	VariantEffect variantEffect;
	boolean useSequenceOntology = true, useHgvs = true, useGeneId = false;

	/**
	 * Convert from field name to field number
	 */
	public static int fieldNum(String name, EffFormatVersion formatVersion) {
		if (formatVersion == null) formatVersion = EffFormatVersion.DEFAULT_FORMAT_VERSION;

		HashMap<String, Integer> f2n = fieldName2Num.get(formatVersion);

		// Not created yet?
		if (fieldName2Num == null) {
			if (formatVersion.isAnn()) f2n = mapAnn2Num(name, formatVersion);
			else f2n = mapEff2Num(name, formatVersion);

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
	static HashMap<String, Integer> mapAnn2Num(String name, EffFormatVersion formatVersion) {
		HashMap<String, Integer> f2n = new HashMap<String, Integer>();
		int fieldNum = 0;

		f2n.put("ANN.EFFECT", fieldNum++);
		f2n.put("ANN.IMPACT", fieldNum++);
		f2n.put("ANN.FUNCLASS", fieldNum++);
		f2n.put("ANN.CODON", fieldNum++);

		// This field can be called either AA or HGVS
		f2n.put("ANN.AA", fieldNum);
		f2n.put("ANN.HGVS", fieldNum);
		fieldNum++;

		if (formatVersion != EffFormatVersion.FORMAT_EFF_2) {
			f2n.put("ANN.AA_LEN", fieldNum++);
		}

		f2n.put("ANN.GENE", fieldNum++);
		f2n.put("ANN.BIOTYPE", fieldNum++);
		f2n.put("ANN.CODING", fieldNum++);
		f2n.put("ANN.TRID", fieldNum++);

		// This one used to be called exonID, now it is used for exon OR intron rank
		f2n.put("ANN.RANK", fieldNum);
		f2n.put("ANN.EXID", fieldNum);
		fieldNum++;

		if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
			// This one can be called  in different ways
			f2n.put("ANN.GT", fieldNum);
			f2n.put("ANN.GENOTYPE_NUMBER", fieldNum);
			f2n.put("ANN.GENOTYPE", fieldNum);
			fieldNum++;
		}

		return f2n;
	}

	/**
	 * Create a hash to map names to field numbers on 'EFF' fields
	 */
	static HashMap<String, Integer> mapEff2Num(String name, EffFormatVersion formatVersion) {
		HashMap<String, Integer> f2n = new HashMap<String, Integer>();
		int fieldNum = 0;

		f2n.put("EFF.EFFECT", fieldNum++);
		f2n.put("EFF.IMPACT", fieldNum++);
		f2n.put("EFF.FUNCLASS", fieldNum++);
		f2n.put("EFF.CODON", fieldNum++);

		// This field can be called either AA or HGVS
		f2n.put("EFF.AA", fieldNum);
		f2n.put("EFF.HGVS", fieldNum);
		fieldNum++;

		if (formatVersion != EffFormatVersion.FORMAT_EFF_2) {
			f2n.put("EFF.AA_LEN", fieldNum++);
		}

		f2n.put("EFF.GENE", fieldNum++);
		f2n.put("EFF.BIOTYPE", fieldNum++);
		f2n.put("EFF.CODING", fieldNum++);
		f2n.put("EFF.TRID", fieldNum++);

		// This one used to be called exonID, now it is used for exon OR intron rank
		f2n.put("EFF.RANK", fieldNum);
		f2n.put("EFF.EXID", fieldNum);
		fieldNum++;

		if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
			// This one can be called  in different ways
			f2n.put("EFF.GT", fieldNum);
			f2n.put("EFF.GENOTYPE_NUMBER", fieldNum);
			f2n.put("EFF.GENOTYPE", fieldNum);
			fieldNum++;
		}

		return f2n;
	}

	/**
	 * Split a 'effect' string to an array of strings
	 */
	public static String[] split(String eff) {
		// TODO: Parsing should be version dependent and provably implemented on different methods using a dispatcher

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

	/**
	 * Return a string safe to be used in an 'EFF' info field (VCF file)
	 */
	public static String vcfEffSafe(String str) {
		// TODO: ("Parenthesis and square brakets are no longer needed!");

		return str.replaceAll("(\\s|\\(|\\)|\\[|\\]|;|,|\\|)+", "_");
	}

	/**
	 * Constructor: Guess format version
	 */
	public VcfEffect(String effectString) {
		formatVersion = null; // Force guess
		this.effectString = effectString;
		parse();
	}

	/**
	 * Constructor: Force format version
	 * @param effStr
	 * @param formatVersion : If null, will try to guess it
	 */
	public VcfEffect(String effectString, EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion;
		this.effectString = effectString;
		parse();
	}

	public VcfEffect(VariantEffect variantEffect, EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion; // Force guess
		this.variantEffect = variantEffect;
	}

	public void addEffectType(EffectType effectType) {
		effectTypes.add(effectType);
		this.effectType = null;
	}

	/**
	 * Add subfield to a buffer
	 */
	void add(StringBuilder sb, Object obj) {
		sb.append(VcfEntry.vcfInfoSafe(obj.toString()));
		sb.append("|");
	}

	/**
	 * Create 'ANN' field
	 */
	String createAnnField() {
		StringBuilder effBuff = new StringBuilder();

		// Allele
		Variant var = variantEffect.getVariant();
		if (!var.isVariant()) add(effBuff, ".");
		else if (var.isNonRef()) add(effBuff, var.getGenotype());
		else add(effBuff, var.getAlt());

		// Add main annotation in Sequence Ontology terms
		add(effBuff, variantEffect.getEffectTypeString(true));

		// Add effect impact
		add(effBuff, variantEffect.getEffectImpact());

		// Gene name
		Gene gene = variantEffect.getGene();
		if (gene != null) add(effBuff, VcfOutputFormatter.vcfInfoSafeString(gene.getGeneName()));
		else effBuff.append("|");

		// Gene ID
		if (gene != null) add(effBuff, VcfOutputFormatter.vcfInfoSafeString(gene.getId()));
		else effBuff.append("|");

		// Feature type
		Marker marker = variantEffect.getMarker();
		Transcript tr = variantEffect.getTranscript();
		if (tr != null) add(effBuff, EffectType.TRANSCRIPT.toSequenceOntology());
		else if (marker != null) {
			if (marker instanceof Custom) add(effBuff, marker.getType() + VcfEffect.EFFECT_TYPE_SEPARATOR + ((Custom) marker).getLabel()); // Custom
			else if (marker instanceof Regulation) add(effBuff, marker.getType() + ":" + ((Regulation) marker).getCellType()); // Regulation includes cell type
			else add(effBuff, marker.getType().toSequenceOntology()); // Other markers
		} else effBuff.append("|");

		// Feature ID
		if (tr != null) add(effBuff, VcfEntry.vcfInfoSafe(tr.getId()));
		else if (marker != null) add(effBuff, VcfEntry.vcfInfoSafe(marker.getId()));
		else effBuff.append("|");

		// Transcript biotype
		if (tr != null) {
			if ((tr.getBioType() != null) && !tr.getBioType().isEmpty()) add(effBuff, tr.getBioType());
			else add(effBuff, tr.isProteinCoding() ? "Coding" : "Noncoding"); // No biotype? Add protein_coding of we know it is.
		} else effBuff.append("|");

		// Add exon (or intron) rank info
		Exon ex = variantEffect.getExon();
		int rank = -1;
		if (ex != null) rank = ex.getRank();
		else {
			// Do we have an intron?
			Intron intron = variantEffect.getIntron();
			if (intron != null) rank = intron.getRank();
		}
		if (rank >= 0) add(effBuff, rank);
		else effBuff.append("|");

		// HGVS
		add(effBuff, variantEffect.getHgvsDna());
		add(effBuff, variantEffect.getHgvsProt());

		// cDNA position / length
		if (tr != null) {
			int cDnaPos = variantEffect.getCodonNum() * 3 + variantEffect.getCodonIndex();
			add(effBuff, cDnaPos + "/" + variantEffect.getCdsLength());
		} else effBuff.append("|");

		// CDS position / length
		if (tr != null) {
			int cdsPos = variantEffect.getCodonNum() * 3 + variantEffect.getCodonIndex();
			add(effBuff, cdsPos + "/" + variantEffect.getCdsLength());
		} else effBuff.append("|");

		////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////

		// Add functional class
		FunctionalClass fc = variantEffect.getFunctionalClass();
		effBuff.append(fc == FunctionalClass.NONE ? "" : fc.toString()); // Show only if it is not empty
		effBuff.append("|");

		// Codon change
		String codonChange = variantEffect.getCodonChangeMax();
		if (!codonChange.isEmpty()) effBuff.append(codonChange);
		else if (variantEffect.getDistance() >= 0) effBuff.append(variantEffect.getDistance());
		effBuff.append("|");

		// Add HGVS (amino acid change)
		if (useHgvs) effBuff.append(variantEffect.getHgvs());
		else effBuff.append(variantEffect.getAaChange());
		effBuff.append("|");

		// Add amino acid length
		if (formatVersion != EffFormatVersion.FORMAT_EFF_2) { // This field is not in format version 2
			int aalen = variantEffect.getAaLength();
			effBuff.append(aalen >= 0 ? aalen : "");
			effBuff.append("|");
		}

		// Add gene info
		if (gene != null) {
			// Gene name
			effBuff.append(VcfOutputFormatter.vcfInfoSafeString(useGeneId ? gene.getId() : gene.getGeneName()));
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
		if (tr != null) effBuff.append(VcfOutputFormatter.vcfInfoSafeString(tr.getId()));
		effBuff.append("|");

		// Add genotype (or genotype difference) for this effect
		if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
			effBuff.append("|");
			effBuff.append(variantEffect.getGenotype());
		}

		//---
		// Errors or warnings (this is the last thing in the list)
		//---
		if (variantEffect.hasError() || variantEffect.hasWarning()) {
			StringBuilder err = new StringBuilder();

			// Add warnings
			if (!variantEffect.getWarning().isEmpty()) err.append(variantEffect.getWarning());

			// Add errors
			if (!variantEffect.getError().isEmpty()) {
				if (err.length() > 0) err.append("+");
				err.append(variantEffect.getError());
			}

			effBuff.append("|");
			effBuff.append(err);
		}
		effBuff.append(")");

		return effBuff.toString();

	}

	/**
	 * Create 'EFF' field
	 */
	String createEffField() {
		StringBuilder effBuff = new StringBuilder();

		// Add effect
		effBuff.append(variantEffect.effect(true, false, false, useSequenceOntology));
		effBuff.append("(");

		// Add effect impact
		effBuff.append(variantEffect.getEffectImpact());
		effBuff.append("|");

		// Add functional class
		FunctionalClass fc = variantEffect.getFunctionalClass();
		effBuff.append(fc == FunctionalClass.NONE ? "" : fc.toString()); // Show only if it is not empty
		effBuff.append("|");

		// Codon change
		String codonChange = variantEffect.getCodonChangeMax();
		if (!codonChange.isEmpty()) effBuff.append(codonChange);
		else if (variantEffect.getDistance() >= 0) effBuff.append(variantEffect.getDistance());
		effBuff.append("|");

		// Add HGVS (amino acid change)
		if (useHgvs) effBuff.append(variantEffect.getHgvs());
		else effBuff.append(variantEffect.getAaChange());
		effBuff.append("|");

		// Add amino acid length
		if (formatVersion != EffFormatVersion.FORMAT_EFF_2) { // This field is not in format version 2
			int aalen = variantEffect.getAaLength();
			effBuff.append(aalen >= 0 ? aalen : "");
			effBuff.append("|");
		}

		// Add gene info
		Gene gene = variantEffect.getGene();
		Transcript tr = variantEffect.getTranscript();
		if (gene != null) {
			// Gene name
			effBuff.append(VcfOutputFormatter.vcfInfoSafeString(useGeneId ? gene.getId() : gene.getGeneName()));
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
		if (tr != null) effBuff.append(VcfOutputFormatter.vcfInfoSafeString(tr.getId()));
		effBuff.append("|");

		// Add exon (or intron) rank info
		Exon ex = variantEffect.getExon();
		int rank = -1;
		if (ex != null) rank = ex.getRank();
		else {
			// Do we have an intron?
			Intron intron = variantEffect.getIntron();
			if (intron != null) rank = intron.getRank();
		}
		effBuff.append(rank >= 0 ? rank : "");

		// Add genotype (or genotype difference) for this effect
		if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
			effBuff.append("|");
			effBuff.append(variantEffect.getGenotype());
		}

		//---
		// Errors or warnings (this is the last thing in the list)
		//---
		if (variantEffect.hasError() || variantEffect.hasWarning()) {
			StringBuilder err = new StringBuilder();

			// Add warnings
			if (!variantEffect.getWarning().isEmpty()) err.append(variantEffect.getWarning());

			// Add errors
			if (!variantEffect.getError().isEmpty()) {
				if (err.length() > 0) err.append("+");
				err.append(variantEffect.getError());
			}

			effBuff.append("|");
			effBuff.append(err);
		}
		effBuff.append(")");

		return effBuff.toString();
	}

	/**
	 * Create INFO field using either 'ANN' or 'EFF' depending on format version
	 */
	String createInfoField() {
		if (isAnn()) return createAnnField();
		return createEffField();
	}

	/**
	 * Guess effect format version
	 */
	public EffFormatVersion formatVersion() {
		// Already set?
		if (formatVersion != null && formatVersion.isFullVersion()) return formatVersion;

		// OK, guess format version

		if (effectStrings == null) effectStrings = split(effectString);
		int len = effectStrings.length;

		// Error or Warning string is not added under normal situations
		String lastField = effectStrings[len - 2]; // Actually las array item is after the last ')', so we use the previous one
		if (lastField.startsWith("ERROR") || lastField.startsWith("WARNING")) len--;

		// Guess format
		if (len <= 11) formatVersion = EffFormatVersion.FORMAT_EFF_2;
		else if (len <= 12) formatVersion = EffFormatVersion.FORMAT_EFF_3;
		else formatVersion = EffFormatVersion.FORMAT_EFF_4;

		return formatVersion;
	}

	/**
	 * Get a subfield as an index
	 */
	public String get(int index) {
		if (index >= effectStrings.length) return null;
		return effectStrings[index];
	}

	public String getAa() {
		return aa;
	}

	public int getAaLen() {
		return aaLen;
	}

	public String getBioType() {
		return bioType;
	}

	public VariantEffect.Coding getCoding() {
		return coding;
	}

	public String getCodon() {
		return codon;
	}

	public String getEffectDetails() {
		return effectDetails;
	}

	public String getEffectsStr() {
		StringBuilder sb = new StringBuilder();
		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append("+");
			sb.append(et);
		}
		return sb.toString();
	}

	public String getEffectsStrSo() {
		StringBuilder sb = new StringBuilder();
		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append("+");
			sb.append(et.toSequenceOntology());
		}
		return sb.toString();
	}

	public String getEffectString() {
		// TODO: ("Rename to getAnn?");
		return effectString;
	}

	public EffectType getEffectType() {
		// TODO: ("Rename to getAnn?");
		if (effectType != null) return effectType;
		if (effectTypes == null || effectTypes.isEmpty()) return EffectType.NONE;

		// Pick highest effect type
		effectType = EffectType.NONE;
		for (EffectType et : effectTypes)
			if (et.compareTo(effectType) < 0) effectType = et;

		return effectType;
	}

	public List<EffectType> getEffectTypes() {
		// TODO: ("Rename to getAnn?");
		return effectTypes;
	}

	public String getEffectTypesStr() {
		return effectTypesStr;
	}

	public String getEffString() {
		return effString;
	}

	public String getErrorsOrWarning() {
		return errorsOrWarning;
	}

	public String getExonId() {
		return exonId;
	}

	public VariantEffect.FunctionalClass getFunClass() {
		return funClass;
	}

	public String getGene() {
		return gene;
	}

	public String getGenotype() {
		return genotype;
	}

	public String getHgvsDna() {
		if (aa == null) return null;
		if (aa.indexOf('/') > 0) {
			String f[] = aa.split("/");
			if (f.length > 1 && (f[1].startsWith("c.") || f[1].startsWith("n."))) return f[1];
		} else if (aa.startsWith("c.") || aa.startsWith("n.")) return aa;

		return null;
	}

	public String getHgvsProt() {
		if (aa == null) return null;
		if (aa.indexOf('/') > 0) {
			String f[] = aa.split("/");
			if (f.length > 0 && f[0].startsWith("p.")) return f[0];
		} else if (aa.startsWith("p.")) return aa;

		return null;
	}

	public VariantEffect.EffectImpact getImpact() {
		return impact;
	}

	public String getTranscriptId() {
		return transcriptId;
	}

	public boolean hasEffectType(EffectType effType) {
		if (effectTypes == null) return false;
		for (EffectType et : effectTypes)
			if (et == effType) return true;
		return false;

	}

	/**
	 * Should we use 'ANN' (or 'EFF')
	 */
	public boolean isAnn() {
		return formatVersion == null || formatVersion == EffFormatVersion.FORMAT_ANN_1;
	}

	/**
	 * Parse annotations either in 'ANN' or 'EFF' INFO field
	 */
	void parse() {
		if (isAnn()) parseAnn();
		else parseEff();
	}

	/**
	 * Parse 'ANN' field
	 */
	void parseAnn() {
		throw new RuntimeException("Unimplemented!");
	}

	/**
	 * Parse 'EFF' field
	 */
	void parseEff() {
		effectStrings = split(effectString);

		// Guess format, if not given
		if (formatVersion == null || !formatVersion.isFullVersion()) formatVersion = formatVersion();

		try {
			// Parse each sub field
			int index = 0;

			// TODO: ("Branch for ANN / EFF?");

			// Effect
			effString = effectStrings[index];
			effectTypesStr = effectStrings[index];
			effectTypes = parseEffect(effectStrings[index]);
			effectDetails = parseEffectDetails(effectStrings[index]); // Effect details: everything between '['  and ']' (e.g. Regulation, Custom, Motif, etc.)
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) impact = VariantEffect.EffectImpact.valueOf(effectStrings[index]);
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) funClass = VariantEffect.FunctionalClass.valueOf(effectStrings[index]);
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) codon = effectStrings[index];
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) aa = effectStrings[index];
			index++;

			if (formatVersion != EffFormatVersion.FORMAT_EFF_2) {
				if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) aaLen = Gpr.parseIntSafe(effectStrings[index]);
				else aaLen = 0;
				index++;
			}

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) gene = effectStrings[index];
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) bioType = effectStrings[index];
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) coding = VariantEffect.Coding.valueOf(effectStrings[index]);
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) transcriptId = effectStrings[index];
			index++;

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) exonId = effectStrings[index];
			index++;

			if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
				if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) genotype = effectStrings[index];
				else genotype = "";
				index++;
			}

			if ((effectStrings.length > index) && !effectStrings[index].isEmpty()) errorsOrWarning = effectStrings[index];
			index++;

		} catch (Exception e) {
			String fields = "";
			for (int i = 0; i < effectStrings.length; i++)
				fields += "\t" + i + " : '" + effectStrings[i] + "'\n";
			throw new RuntimeException("Error parsing:\n\t'" + effectString + "'\n\t EFF formatVersion : " + formatVersion + "\n" + fields, e);
		}
	}

	List<EffectType> parseEffect(String eff) {
		int idx = eff.indexOf('[');
		if (idx > 0) eff = eff.substring(0, idx);

		List<EffectType> effs = new LinkedList<EffectType>();
		if (eff.isEmpty()) return effs;

		// Split multiple effectTypes
		if (eff.indexOf(VcfEffect.EFFECT_TYPE_SEPARATOR_OLD) >= 0) {
			// Old version
			for (String es : eff.split("\\" + VcfEffect.EFFECT_TYPE_SEPARATOR_OLD))
				effs.add(EffectType.parse(es));
		} else {
			// Split effect strings
			for (String es : eff.split(VcfEffect.EFFECT_TYPE_SEPARATOR))
				effs.add(EffectType.parse(es));
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

	public void setFunClass(VariantEffect.FunctionalClass funClass) {
		this.funClass = funClass;
	}

	public void setGene(String gene) {
		this.gene = gene;
	}

	public void setImpact(VariantEffect.EffectImpact impact) {
		this.impact = impact;
	}

	public void setTranscriptId(String transcriptId) {
		this.transcriptId = transcriptId;
	}

	@Override
	public String toString() {
		// Create from variant?
		if (variantEffect != null) return createInfoField();

		// Create from parsed fields
		StringBuilder sb = new StringBuilder();

		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append("+");
			sb.append(et);
		}

		// TODO: ("Branch ANN/EFF");

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

		if (gene != null) sb.append(gene);
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

		if (errorsOrWarning != null) sb.append("|" + errorsOrWarning);

		sb.append(")");

		return sb.toString();
	}
}
