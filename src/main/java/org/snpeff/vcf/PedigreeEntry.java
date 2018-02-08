package org.snpeff.vcf;

import java.util.List;

/**
 * Pedigree entry in a VCF file header
 *
 * E.g.:
 * 		##PEDIGREE=<Derived=Patient_01_Somatic,Original=Patient_01_Germline>
 *
 * or
 * 		##PEDIGREE=<Child=CHILD-GENOME-ID,Mother=MOTHER-GENOME-ID,Father=FATHER-GENOME-ID>
 *
 *
 * @author pcingola
 *
 */
public class PedigreeEntry {

	public String father;
	public String mother;
	public String child;
	public int fatherNum = -1, motherNum = -1, childNum = -1; // Sample number in VCF file (column number after 'FORMAT')

	public PedigreeEntry(String original, String derived) {
		child = derived;
		father = original;
		mother = null;
	}

	public PedigreeEntry(String father, String mother, String child) {
		this.child = child;
		this.father = father;
		this.mother = mother;
	}

	public String getDerived() {
		return child;
	}

	public int getDerivedNum() {
		return childNum;
	}

	public String getOriginal() {
		return father;
	}

	public int getOriginalNum() {
		return fatherNum;
	}

	/**
	 * Is this an 'Original-Derived' entry type?
	 * @return
	 */
	public boolean isDerived() {
		return (mother == null) && (father != null);
	}

	/**
	 * Find sample numbers in a list of sample names
	 * @param sampleNames
	 */
	public void sampleNumbers(List<String> sampleNames) {
		int i = 0;
		for (String sn : sampleNames) {
			if (sn.equals(father)) fatherNum = i;
			if (sn.equals(child)) childNum = i;
			if ((mother != null) && sn.equals(mother)) motherNum = i;
			i++;
		}

		if (fatherNum < 0) throw new RuntimeException("Cannot find pedigree Father/Original sample name '" + father + "'");
		if (childNum < 0) throw new RuntimeException("Cannot find pedigree Child/Derived sample name '" + child + "'");
	}

	@Override
	public String toString() {
		if (mother == null) return "Derived=" + getDerived() + ", Original=" + getOriginal();
		return "Child=" + child + ", Mother=" + mother + ", Father=" + father;
	}
}
