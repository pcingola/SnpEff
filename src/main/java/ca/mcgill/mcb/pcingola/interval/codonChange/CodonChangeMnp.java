package ca.mcgill.mcb.pcingola.interval.codonChange;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Calculate codon changes produced by a MNP
 * @author pcingola
 */
public class CodonChangeMnp extends CodonChange {

	int cdsStart;
	int cdsEnd;

	public CodonChangeMnp(SeqChange seqChange, Transcript transcript, ChangeEffect changeEffect) {
		super(seqChange, transcript, changeEffect);
		returnNow = false;
		requireNetCdsChange = true;
	}

	int cdsBaseNumber(int pos, boolean usePrevBaseIntron) {
		if (pos < cdsStart) return transcript.isStrandPlus() ? 0 : transcript.cds().length() - 1;
		if (pos > cdsEnd) return transcript.isStrandPlus() ? transcript.cds().length() - 1 : 0;
		return transcript.cdsBaseNumber(pos, usePrevBaseIntron);
	}

	/**
	 * Calculate a list of codon changes
	 * @return
	 */
	@Override
	List<ChangeEffect> codonChange() {
		ArrayList<ChangeEffect> changes = new ArrayList<ChangeEffect>();
		if (!transcript.intersects(seqChange)) return changes;

		// CDS coordinates
		cdsStart = transcript.isStrandPlus() ? transcript.getCdsStart() : transcript.getCdsEnd();
		cdsEnd = transcript.isStrandPlus() ? transcript.getCdsEnd() : transcript.getCdsStart();

		// Does it intersect CDS?
		if (cdsStart > seqChange.getEnd()) return changes;
		if (cdsEnd < seqChange.getStart()) return changes;

		// Base number relative to CDS start
		int scStart, scEnd;
		if (transcript.isStrandPlus()) {
			scStart = cdsBaseNumber(seqChange.getStart(), false);
			scEnd = cdsBaseNumber(seqChange.getEnd(), true);
		} else {
			scEnd = cdsBaseNumber(seqChange.getStart(), true);
			scStart = cdsBaseNumber(seqChange.getEnd(), false);
		}

		// Update coordinates
		codonNum = scStart / CODON_SIZE;
		codonIndex = scStart % CODON_SIZE;

		// MNP overlap in coding part 
		int scLen = scEnd - scStart;
		if (scLen < 0) return changes;

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
		ChangeEffect changeEffectNew = changeEffect.clone();
		changeEffectNew.set(transcript, EffectType.CODON_CHANGE, "");
		changeEffectNew.setCodons(codonsOld, codonsNew, codonNum, codonIndex);
		changes.add(changeEffectNew);

		return changes;
	}

	/**
	 * We may have to calculate 'netCdsChange', which is the effect on the CDS
	 * Note: A deletion or a MNP might affect several exons
	 * @return
	 */
	@Override
	protected String netCdsChange() {
		if (seqChange.size() > 1) {
			StringBuilder sb = new StringBuilder();
			for (Exon exon : transcript.sortedStrand()) {
				String seq = seqChange.netChange(exon);
				sb.append(exon.isStrandPlus() ? seq : GprSeq.reverseWc(seq));
			}
			return sb.toString();
		}

		return seqChange.netChange(transcript.getStrand());
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
