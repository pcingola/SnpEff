package org.snpeff.overlap;

import org.snpeff.binseq.DnaSequence;
import org.snpeff.binseq.coder.Coder;

/**
 * Rotates a binary packed sequence
 * 
 * WARNING: We only rotate up to Coder.basesPerWord() because after that the sequences are the same (with an integer offset)
 * 
 * NOTE: Left rotation 'n' is the same as a right rotation 'Coder.basesPerWord() - n'
 * 
 * @author pcingola
 */
public class SequenceRotator {

	DnaSequence sequence;
	DnaSequence rotations[]; // Right rotations
	Coder coder;

	public SequenceRotator(DnaSequence sequence) {
		this.sequence = sequence;
		coder = sequence.getCoder();

		rotations = new DnaSequence[coder.basesPerWord()];
		for (int i = 0; i < rotations.length; i++)
			rotations[i] = null;
		rotations[0] = sequence;
	}

	public DnaSequence calcRor(int rotate) {
		if ((rotate <= 0) && (rotate >= coder.basesPerWord())) throw new RuntimeException("Invalid parameter rotate=" + rotate + ". It should be a number between 1 and " + coder.basesPerWord());

		long bits[] = sequence.getCodes();
		long newBits[] = new long[bits.length + 1];
		int ror = 2 * rotate;
		int rol = 2 * (coder.basesPerWord() - rotate);
		for (int i = 0; i < newBits.length; i++) {
			if (i < bits.length) newBits[i] = bits[i] >>> ror;
			if (i > 0) newBits[i] |= bits[i - 1] << rol;
		}

		DnaSequence binSeq = new DnaSequence(sequence.length() + rotate, newBits);
		return binSeq;
	}

	/**
	 * Rotate right
	 * @param rotate : A number of rotations to the right
	 * 
	 * @return
	 */
	DnaSequence ror(int rotate) {
		if (rotations[rotate] == null) rotations[rotate] = calcRor(rotate);
		return rotations[rotate];
	}

	/**
	 * Rotate left
	 * @param rotate : A number of rotations to the right (if positive) or to the left (if negative)
	 * 
	 * WARNING: We only rotate up to BinCoder.BASES_PER_WORD because after that the sequences are the same (with an integer offset)
	 * 
	 * @return A rotated sequence
	 */
	public DnaSequence rotate(int rotate) {
		if ((rotate <= -coder.basesPerWord()) || (rotate >= coder.basesPerWord())) throw new RuntimeException("Parameter 'rotate' should be in the range [" + (-coder.basesPerWord() + 1) + " , " + (coder.basesPerWord() - 1) + ")");
		if (rotate < 0) return ror(coder.basesPerWord() + rotate);
		if (rotate > 0) return ror(rotate);
		return sequence;
	}
}
