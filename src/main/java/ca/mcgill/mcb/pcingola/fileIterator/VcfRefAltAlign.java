package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.interval.Variant.VariantType;

/**
 * Needleman-Wunsch (global sequence alignment) algorithm for sequence  alignment (short strings, since it's not memory optimized)
 * 
 * @author pcingola
 */
public class VcfRefAltAlign extends NeedlemanWunsch {

	public static final int MAX_SIZE = 10000; // 1024 * 1024;

	String stringA, stringB;
	VariantType changeType;

	public VcfRefAltAlign(String a, String b) {
		super(a, b);
		stringA = a;
		stringB = b;
	}

	@Override
	public String align() {
		try {
			if (simpleAlign()) {
				// OK Nothing else to do 
			} else {
				// Perform alignment only of sequences are not too long (we don't want an 'out of memory' issue)
				long size = ((long) stringA.length()) * stringB.length();
				if ((size > 0) && (size < MAX_SIZE)) {
					scoreMatrix();
					calcAlignment();

					if (stringB.length() > stringA.length()) {
						if (alignment.startsWith("-")) {
							changeType = VariantType.DEL;
							return alignment;
						}
					} else if (stringB.length() < stringA.length()) {
						if (alignment.startsWith("+")) {
							changeType = VariantType.INS;
							return alignment;
						}
					}
				}

				// Not an InDel? Then it's a substitution
				substitution();
			}
		} catch (Throwable t) {
			throw new RuntimeException("Error aligning sequences:\n\tSequence 1: " + new String(a) + "\n\tSequence 2: " + new String(b), t);
		}

		return alignment;
	}

	public VariantType getChangeType() {
		return changeType;
	}

	public void setChangeType(VariantType changeType) {
		this.changeType = changeType;
	}

	/**
	 * Trim bases that are equal at the end of stringA / stringB
	 * @param stringA
	 * @param stringB
	 */
	void trimCommonBasesEnd() {
		int ia = stringA.length() - 1;
		int ib = stringB.length() - 1;
		int count = 0;
		for (; ia >= offset && ib >= offset; ia--, ib--, count++)
			if (stringA.charAt(ia) != stringB.charAt(ib)) break;

		// Trim last bases (they are equal)
		if (count > 0) {
			stringA = stringA.substring(0, ia + 1);
			stringB = stringB.substring(0, ib + 1);
		}
	}

	/**
	 * Simplified alignment
	 * @return
	 */
	boolean simpleAlign() {

		if (stringA.length() == stringB.length()) {
			offset = 0;
			if (stringA.equals(stringB)) {
				// No change
				changeType = VariantType.INTERVAL;
				return true;
			} else if (stringA.length() == 1) {
				// SNP
				changeType = VariantType.SNP;
				return true;
			} else {
				// MNP
				offset = minCommonBase();
				changeType = VariantType.MNP;
				return true;
			}
		}

		offset = minCommonBase();
		trimCommonBasesEnd();

		if (stringA.length() < stringB.length()) {
			// A has a deletion respect to B
			int idx = stringB.indexOf(stringA);
			if (idx >= 0) {
				changeType = VariantType.DEL;
				offset = stringA.length();
				alignment = "-" + stringB.substring(stringA.length(), stringB.length());
				return true;
			}

			changeType = VariantType.MIXED;
			return true;
		} else if (stringA.length() > stringB.length()) {
			// A has an insertion respect to B
			int idx = stringA.indexOf(stringB);
			if (idx >= 0) {
				changeType = VariantType.INS;
				offset = stringB.length();
				alignment = "+" + stringA.substring(stringB.length(), stringA.length());
				return true;
			}

			changeType = VariantType.MIXED;
			return true;
		}

		return false;
	}

	/**
	 * Min position with a common base between stringA and stringB
	 * @param stringA
	 * @param stringB
	 * @return
	 */
	int minCommonBase() {
		int min = Math.min(stringA.length(), stringB.length());
		int i;
		for (i = 0; i < min; i++)
			if (stringA.charAt(i) != stringB.charAt(i)) return i;

		return i;
	}

	/**
	 * If it is not a trivial alignment, then it's a mixed variant (a.k.a subtitution)
	 */
	void substitution() {
		changeType = VariantType.MIXED;

		// Offset
		// Note: There must be a difference, otherwise this would be an InDel, captured in 'simpleAlign() method
		int min = Math.min(stringA.length(), stringB.length());
		for (int i = 0; i < min; i++)
			if (stringA.charAt(i) == stringB.charAt(i)) offset = i;
			else break;
	}
}
