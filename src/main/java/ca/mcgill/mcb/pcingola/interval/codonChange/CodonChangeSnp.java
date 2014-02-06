package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;

/**
 * Calculate codon changes produced by a SNP
 * @author pcingola
 */
public class CodonChangeSnp extends CodonChange {

	public CodonChangeSnp(SeqChange seqChange, Transcript transcript, ChangeEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = true; // A SNP can only affect one exon
	}

	/**
	 * Analyze SNPs in this transcript.
	 * Add changeEffect to 'changeEffect'
	 */
	@Override
	boolean codonChangeSingle(Exon exon) {
		// Get old and new codons
		codonsOld = codonsOld();
		if (codonsOld.isEmpty()) changeEffects.addErrorWarning(ErrorWarningType.ERROR_MISSING_CDS_SEQUENCE);

		codonsNew = codonsNew();
		changeEffects.add(exon, EffectType.CODON_CHANGE, "");
		changeEffects.setCodons(codonsOld, codonsNew, codonNum, codonIndex);

		return true;
	}

	/**
	 * Get new (modified) codons 
	 * @return
	 */
	@Override
	String codonsNew() {
		// Was there a problem getting 'codonsOld'? => We cannot do anything 
		if (codonsOld.isEmpty()) return "";

		char codonChars[] = codonsOld.toLowerCase().toCharArray();
		char snpBase = seqChange.netChange(transcript.getStrand()).charAt(0);
		codonChars[codonIndex] = Character.toUpperCase(snpBase);

		String codonsNew = new String(codonChars);
		return codonsNew;
	}

	/**
	 * Get original codons in CDS
	 * @param codonNum
	 * @return
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
