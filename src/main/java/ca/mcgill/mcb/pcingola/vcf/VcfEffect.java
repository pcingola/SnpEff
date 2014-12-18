package ca.mcgill.mcb.pcingola.vcf;

import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * An 'ANN' or 'EFF' entry in a VCF INFO field
 * Note: 'EFF' is the old version that has been replaced by the standardized 'ANN' field (2014-12)
 * *
 * @author pablocingolani
 */
public class VcfEffect {

	/**
	 * VcfFields in SnpEff version 2.X have a different format than 3.X
	 * As of version 4.1 we switch to a standard annotation format
	 */
	public enum FormatVersion {
		FORMAT_EFF_2 //
		, FORMAT_EFF_3 // Added: AA_length
		, FORMAT_EFF_4 // Added: Exon/Intron rank and 'genotype' (Cancer samples)
		, FORMAT_ANN_5 // Standard annotation format (2014-12)
	}

	public static final String VCF_INFO_EFF_NAME_EFF = "EFF";
	public static final String VCF_INFO_EFF_NAME = "EFF"; // TODO: Change to 'ANN'

	String effectString;
	String effectStrings[];
	FormatVersion formatVersion;
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

	/**
	 * Convert from field name to field number
	 */
	public static int fieldNum(String name, FormatVersion formatVersion) {
		int fieldNum = 0;

		// TODO: ("Implement this as a hash!");

		if (name.equals("EFF.EFFECT")) return fieldNum;
		fieldNum++;

		if (name.equals("EFF.IMPACT")) return fieldNum;
		fieldNum++;

		if (name.equals("EFF.FUNCLASS")) return fieldNum;
		fieldNum++;

		if (name.equals("EFF.CODON")) return fieldNum;
		fieldNum++;

		// This field can be called either AA or HGVS
		if (name.equals("EFF.AA") || name.equals("EFF.HGVS")) return fieldNum;
		fieldNum++;

		if (formatVersion != FormatVersion.FORMAT_EFF_2) {
			if (name.equals("EFF.AA_LEN")) return fieldNum;
			fieldNum++;
		}

		if (name.equals("EFF.GENE")) return fieldNum;
		fieldNum++;

		if (name.equals("EFF.BIOTYPE")) return fieldNum;
		fieldNum++;

		if (name.equals("EFF.CODING")) return fieldNum;
		fieldNum++;

		if (name.equals("EFF.TRID")) return fieldNum;
		fieldNum++;

		if (name.equals("EFF.RANK") || name.equals("EFF.EXID")) return fieldNum; // This one used to be called exonID, now it is used for exon OR intron rank
		fieldNum++;

		if (formatVersion == FormatVersion.FORMAT_EFF_4) {
			if (name.equals("EFF.GT") || name.equals("EFF.GENOTYPE_NUMBER") || name.equals("EFF.GENOTYPE")) return fieldNum;
			fieldNum++;
		}

		return -1;
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
	public VcfEffect(String effectString, FormatVersion formatVersion) {
		this.formatVersion = formatVersion;
		this.effectString = effectString;
		parse();
	}

	public void addEffectType(EffectType effectType) {
		effectTypes.add(effectType);
		this.effectType = null;
	}

	/**
	 * Guess effect format version
	 */
	public FormatVersion formatVersion() {
		// Already set?
		if (formatVersion != null) return formatVersion;

		// OK, guess format version
		// TODO: ("Branch ro ANN/EFF");

		if (effectStrings == null) effectStrings = split(effectString);
		int len = effectStrings.length;

		// Error or Warning string is not added under normal situations
		String lastField = effectStrings[len - 2]; // Actually las array item is after the last ')', so we use the previous one
		if (lastField.startsWith("ERROR") || lastField.startsWith("WARNING")) len--;

		// Guess format
		if (len <= 11) return FormatVersion.FORMAT_EFF_2;
		if (len <= 12) return FormatVersion.FORMAT_EFF_3;

		return FormatVersion.FORMAT_EFF_4;
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
		// TODO: ("How do we introduce this data in 'ANN'?");
		return effectDetails;
	}

	public String getEffectsStr() {
		// TODO: ("Rename to getAnn?");
		StringBuilder sb = new StringBuilder();
		for (EffectType et : effectTypes) {
			if (sb.length() > 0) sb.append("+");
			sb.append(et);
		}
		return sb.toString();
	}

	public String getEffectsStrSo() {
		// TODO: ("Rename to getAnn?");
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

	void parse() {
		effectStrings = split(effectString);

		// Guess format, if not given
		if (formatVersion == null) formatVersion = formatVersion();

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

			if (formatVersion != FormatVersion.FORMAT_EFF_2) {
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

			if (formatVersion == FormatVersion.FORMAT_EFF_4) {
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
		if (eff.indexOf(VariantEffect.EFFECT_TYPE_SEPARATOR_OLD) >= 0) {
			// Old version
			for (String es : eff.split("\\" + VariantEffect.EFFECT_TYPE_SEPARATOR_OLD))
				effs.add(EffectType.parse(es));
		} else {
			// Split effect strings
			for (String es : eff.split(VariantEffect.EFFECT_TYPE_SEPARATOR))
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
