package org.snpeff.annotate;

/**
 * A VcfTupleSet conforming a haplotype
 *
 * @author pcingola
 */
public class VcfHaplotypeTupleSet extends VcfTupleSet {

	int genotype[];

	public VcfHaplotypeTupleSet() {
		super();
	}

	/**
	 * Can a VcfTuple be added to this haplotype?
	 */
	public boolean canAdd(VcfTuple vt) {
		if (isEmpty()) return true;
		for (VcfTuple v : this) {
			if (!v.isHaplotypeWith(vt)) return false;
		}
		return true;
	}

}
