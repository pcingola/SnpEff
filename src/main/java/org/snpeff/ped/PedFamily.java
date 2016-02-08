package org.snpeff.ped;

/**
 * A family: A group of Tfams with the same familyId
 * 
 * @author pcingola
 */
public class PedFamily extends PedPedigree {

	String familyId = null;

	public PedFamily() {
		super();
	}

	/**
	 * Add an entry t this family
	 * @param tfamEntry
	 */
	@Override
	public void add(TfamEntry tfamEntry) {
		if ((familyId != null) && (!familyId.equals(tfamEntry.getFamilyId()))) throw new RuntimeException("Cannot add memeber to family. Family IDs do not match: '" + familyId + "' vs '" + tfamEntry.getFamilyId() + "'");
		tfamEntryById.put(tfamEntry.getId(), tfamEntry);
	}

}
