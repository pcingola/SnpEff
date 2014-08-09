package ca.mcgill.mcb.pcingola.vcf;

import java.util.LinkedList;
import java.util.List;

import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * An 'EFF' entry in a vcf line
 *
 * @author pablocingolani
 */
public class VcfEffect {

	/**
	 * VcfFields in SnpEff version 2.X have a different format than 3.X
	 */
	public enum FormatVersion {
		FORMAT_SNPEFF_2, FORMAT_SNPEFF_3, FORMAT_SNPEFF_4
	}

	public static final String VCF_INFO_EFF_NAME = "EFF";

	String effectString;
	String effectStrings[];
	FormatVersion formatVersion;
	String effString;
	// VariantEffect.EffectType effect;
	List<VariantEffect.EffectType> effects;
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

		if (formatVersion != FormatVersion.FORMAT_SNPEFF_2) {
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

		if (formatVersion == FormatVersion.FORMAT_SNPEFF_4) {
			if (name.equals("EFF.GT") || name.equals("EFF.GENOTYPE_NUMBER") || name.equals("EFF.GENOTYPE")) return fieldNum;
			fieldNum++;
		}

		return -1;
	}

	/**
	 * Split a 'effect' string to an array of strings
	 */
	public static String[] split(String eff) {
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

	public void addEffect(VariantEffect.EffectType effect) {
		effects.add(effect);
	}

	/**
	 * Guess effect format version
	 * @return
	 */
	public FormatVersion formatVersion() {
		// Already set?
		if (formatVersion != null) return formatVersion;

		// OK, guess format version
		if (effectStrings == null) effectStrings = split(effectString);
		int len = effectStrings.length;

		// Error or Warning string is not added under normal situations
		String lastField = effectStrings[len - 2]; // Actually las array item is after the last ')', so we use the previous one
		if (lastField.startsWith("ERROR") || lastField.startsWith("WARNING")) len--;

		// Guess format
		if (len <= 11) return FormatVersion.FORMAT_SNPEFF_2;
		if (len <= 12) return FormatVersion.FORMAT_SNPEFF_3;

		return FormatVersion.FORMAT_SNPEFF_4;
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

	public VariantEffect.EffectType getEffect() {
		return effects.get(0);
	}

	public String getEffectDetails() {
		return effectDetails;
	}

	public List<VariantEffect.EffectType> getEffects() {
		return effects;
	}

	public String getEffectString() {
		return effectString;
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

	public VariantEffect.EffectImpact getImpact() {
		return impact;
	}

	public String getTranscriptId() {
		return transcriptId;
	}

	void parse() {
		effectStrings = split(effectString);

		// Guess format, if not given
		if (formatVersion == null) formatVersion = formatVersion();

		try {
			// Parse each sub field
			int index = 0;

			// Effect
			effString = effectStrings[index];
			effects = parseEffect(effectStrings[index]);
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

			if (formatVersion != FormatVersion.FORMAT_SNPEFF_2) {
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

			if (formatVersion == FormatVersion.FORMAT_SNPEFF_4) {
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

	List<VariantEffect.EffectType> parseEffect(String eff) {
		int idx = eff.indexOf('[');
		if (idx > 0) eff = eff.substring(0, idx);

		List<VariantEffect.EffectType> effs = new LinkedList<VariantEffect.EffectType>();
		for (String es : eff.split("\\+"))
			effs.add(VariantEffect.EffectType.parse(es));

		return effs;
	}

	/**
	 * Parse effect details.
	 * E.g. NEXT_PROT[amino_acid_modification:Phosphoserine]  returns "amino_acid_modification:Phosphoserine"
	 */
	String parseEffectDetails(String eff) {
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

	public void setEffect(VariantEffect.EffectType effect) {
		effects = new LinkedList<VariantEffect.EffectType>();
		addEffect(effect);
	}

	public void setEffectDetails(String effectDetails) {
		this.effectDetails = effectDetails;
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

		for (EffectType et : effects) {
			if (sb.length() > 0) sb.append("+");
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
