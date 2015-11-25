package ca.mcgill.mcb.pcingola.pdb;

/**
 * An entry in a ID mapping file
 *
 * @author pcingola
 */
public class IdMapperEntry implements Cloneable, Comparable<IdMapperEntry> {

	//public String geneId, trId, geneName, refSeqId, pdbId, pdbChainId;
	public String trId;
	public String pdbId, pdbChainId;
	public int pdbAaLen, trAaLen;

	public IdMapperEntry(String pdbId, String trId) {
		this.trId = trId;
		this.pdbId = pdbId;
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

		cmp = pdbId.compareTo(o.pdbId);
		if (cmp != 0) return cmp;

		cmp = pdbChainId.compareTo(o.pdbChainId);
		return cmp;
	}

	@Override
	public String toString() {
		return pdbId //
				+ "\t" + trId //
				+ "\t" + pdbChainId //
				+ "\t" + pdbAaLen //
				+ "\t" + trAaLen //
				;
	}
}
