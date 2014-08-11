package ca.mcgill.mcb.pcingola.interval.codonChange;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Calculate codon changes produced by a MNP
 * @author pcingola
 */
public class CodonChangeMnp extends CodonChange {

	int cdsStart;
	int cdsEnd;

	public CodonChangeMnp(Variant seqChange, Transcript transcript, VariantEffects changeEffects) {
		super(seqChange, transcript, changeEffects);
		returnNow = false;
		requireNetCdsChange = true;
	}

	int cdsBaseNumber(int pos, boolean usePrevBaseIntron) {
		if (pos < cdsStart) return transcript.isStrandPlus() ? 0 : transcript.cds().length() - 1;
		if (pos > cdsEnd) return transcript.isStrandPlus() ? transcript.cds().length() - 1 : 0;
		return transcript.baseNumberCds(pos, usePrevBaseIntron);
	}

	/**
	 * Calculate a list of codon changes
	 */
	@Override
	protected void codonChange() {
		if (!transcript.intersects(variant)) return;

		// CDS coordinates
		cdsStart = transcript.isStrandPlus() ? transcript.getCdsStart() : transcript.getCdsEnd();
		cdsEnd = transcript.isStrandPlus() ? transcript.getCdsEnd() : transcript.getCdsStart();

		// Does it intersect CDS?
		if (cdsStart > variant.getEnd()) return;
		if (cdsEnd < variant.getStart()) return;

		// Base number relative to CDS start
		int scStart, scEnd;
		if (transcript.isStrandPlus()) {
			scStart = cdsBaseNumber(variant.getStart(), false);
			scEnd = cdsBaseNumber(variant.getEnd(), true);
		} else {
			scEnd = cdsBaseNumber(variant.getStart(), true);
			scStart = cdsBaseNumber(variant.getEnd(), false);
		}

		// Update coordinates
		codonNum = scStart / CODON_SIZE;
		codonIndex = scStart % CODON_SIZE;

		// MNP overlap in coding part
		int scLen = scEnd - scStart;
		if (scLen < 0) return;

		// Round to codon position
		int scStart3 = round3(scStart, false);
		int scEnd3 = round3(scEnd, true);
		if (scEnd3 == scStart3) scEnd3 += 3; // At least one codon

		// Append 'N'
		String padN = "";
		int diff = scEnd3 - (transcript.cds().length() - 1);
		if (diff > 0) {
			scEnd3 = transcript.cds().length() - 1;
			if (diff == 1) padN = "N";
			else if (diff == 2) padN = "NN";
			else throw new RuntimeException("Sanity check failed. Number of 'N' pading is :" + diff + ". This should not happen!");
		}

		// Get old codon (reference)
		codonsOld = transcript.cds().substring(scStart3, scEnd3 + 1);

		// Get new codon (change)
		String prepend = codonsOld.substring(0, scStart - scStart3);
		String append = "";
		if (scEnd3 > scEnd) append = codonsOld.substring(codonsOld.length() - (scEnd3 - scEnd));
		codonsNew = prepend + netCdsChange() + append;

		// Pad codons with 'N' if required
		codonsOld += padN;
		codonsNew += padN;

		// Create change effect
		effect(transcript, EffectType.CODON_CHANGE, "", codonsOld, codonsNew, codonNum, codonIndex); // Use a generic low priority variant, this allows 'setCodons' to override it

		return;
	}

	@Override
	public String codonsNew() {
		return codonsNew;
	}

	/**
	 * Calculate old codons
	 */
	@Override
	public String codonsOld() {
		return codonsOld;
	}

	/**
	 * We may have to calculate 'netCdsChange', which is the effect on the CDS
	 * Note: A deletion or a MNP might affect several exons
	 * @return
	 */
	@Override
	protected String netCdsChange() {
		if (variant.size() > 1) {
			StringBuilder sb = new StringBuilder();
			for (Exon exon : transcript.sortedStrand()) {
				String seq = variant.netChange(exon);
				sb.append(exon.isStrandPlus() ? seq : GprSeq.reverseWc(seq));
			}
			return sb.toString();
		}

		return variant.netChange(transcript.isStrandMinus());
	}

	int round3(int num, boolean end) {
		if (end) {
			if (num % 3 == 2) return num;
			return (num / 3) * 3 + 2;
		}

		if (num % 3 == 0) return num;
		return (num / 3) * 3;
	}

}
