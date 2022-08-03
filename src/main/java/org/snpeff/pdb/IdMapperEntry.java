package org.snpeff.pdb;

/**
 * An entry in a ID mapping file
 *
 * @author pcingola
 */
public class IdMapperEntry implements Cloneable, Comparable<IdMapperEntry> {

	public String trId;
	public String proteinId, pdbChainId;
	public int pdbAaLen, trAaLen;

	public IdMapperEntry(String proteinId, String trId) {
		this.trId = trId;
		this.proteinId = proteinId;
		pdbChainId = "";
		pdbAaLen = trAaLen = 0;
	}

	/**
	 * Clone the object and return a copy having a different chain ID
	 */
	public IdMapperEntry cloneAndSet(String chainId, int pdbAaLen, int trAaLen) {
		try {
			IdMapperEntry cloned = (IdMapperEntry) this.clone();
			cloned.pdbChainId = chainId;
			cloned.pdbAaLen = pdbAaLen;
			cloned.trAaLen = trAaLen;
			return cloned;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int compareTo(IdMapperEntry o) {
		int cmp = trId.compareTo(o.trId);
		if (cmp != 0) return cmp;

		cmp = proteinId.compareTo(o.proteinId);
		if (cmp != 0) return cmp;

		cmp = pdbChainId.compareTo(o.pdbChainId);
		return cmp;
	}

	@Override
	public String toString() {
		return proteinId //
				+ "\t" + trId //
				+ "\t" + pdbChainId //
				+ "\t" + pdbAaLen //
				+ "\t" + trAaLen //
				;
	}
}
