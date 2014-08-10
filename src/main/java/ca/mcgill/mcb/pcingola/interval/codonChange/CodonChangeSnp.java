package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Calculate codon changes produced by a SNP
 * @author pcingola
 */
public class CodonChangeSnp extends CodonChange {

	public CodonChangeSnp(Variant seqChange, Transcript transcript, VariantEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = true; // A SNP can only affect one exon
	}

	/**
	 * Analyze SNPs in this transcript.
	 * Add changeEffect to 'changeEffect'
	 */
	@Override
	protected boolean codonChangeSingle(Exon exon) {
		// Get old and new codons
		codonsOld = codonsOld();
		if (codonsOld.isEmpty()) variantEffects.addErrorWarning(ErrorWarningType.ERROR_MISSING_CDS_SEQUENCE);

		codonsNew = codonsNew();
		variantEffects.add(exon, EffectType.CODON_CHANGE, ""); // Use a generic low priority variant, this allows 'setCodons' to override it
		variantEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);

		return true;
	}

	/**
	 * Get new (modified) codons
	 */
	@Override
	public String codonsNew() {
		// Was there a problem getting 'codonsOld'? => We cannot do anything
		if (codonsOld.isEmpty()) return "";

		char codonChars[] = codonsOld.toLowerCase().toCharArray();
		char snpBase = variant.netChange(transcript.isStrandMinus()).charAt(0);
		codonChars[codonIndex] = Character.toUpperCase(snpBase);

		String codonsNew = new String(codonChars);
		return codonsNew;
	}

	/**
	 * Get original codons in CDS
	 */
	@Override
	public String codonsOld() {
		int numCodons = 1;

		// Get CDS
		String cdsStr = transcript.cds();
		int cdsLen = cdsStr.length();

		// Calculate minBase (first codon base in the CDS)
		int minBase = codonNum * CodonChange.CODON_SIZE;
		if (minBase < 0) minBase = 0;

		// Calculate maxBase (last codon base in the CDS)
		int maxBase = codonNum * CodonChange.CODON_SIZE + numCodons * CodonChange.CODON_SIZE;
		if (maxBase > cdsLen) maxBase = cdsLen;

		// Sanity checks
		if (cdsStr.isEmpty() // Empty CDS => Cannot get codon (e.g. one or more exons are missing their sequences
				|| (cdsLen <= minBase) // Codon past CDS sequence => Cannot get codon
		) return "";

		// Create codon sequence
		char codonChars[] = cdsStr.substring(minBase, maxBase).toLowerCase().toCharArray();
		codonChars[codonIndex] = Character.toUpperCase(codonChars[codonIndex]);
		String codon = new String(codonChars);
		return codon;
	}
}
