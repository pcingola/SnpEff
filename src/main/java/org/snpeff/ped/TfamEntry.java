package org.snpeff.ped;

import org.snpeff.util.Gpr;

/**
 * An entry in a TFAM table.
 * I.e. a line in a PLINK's TFAM file
 * 
 * @author pcingola
 */
public class TfamEntry implements Comparable<TfamEntry> {

	/**
	 *  From PLINK's manual: Affection status, by default, should be coded:
	 *  		-9 missing 
	 *  		0 missing
	 *  		1 unaffected
	 *  		2 affected
	 */
	public static final int PHENOTYPE_CASE = 2;
	public static final int PHENOTYPE_CONTROL = 1;
	public static final int PHENOTYPE_MISSING = -9; // We define anything <= 0 as missing

	String familyId, id, fatherId, motherId;
	Sex sex;
	double phenotype;

	public TfamEntry(String line) {
		parse(line);
	}

	public TfamEntry(String familyId, String id, String fatherId, String motherId, Sex sex, double phenotype) {
		this.familyId = familyId;
		this.id = id;
		this.fatherId = fatherId;
		this.motherId = motherId;
		this.sex = sex;
		this.phenotype = phenotype;
	}

	@Override
	public int compareTo(TfamEntry ind) {
		return id.compareTo(ind.getId());
	}

	public String getFamilyId() {
		return familyId;
	}

	public String getFatherId() {
		return fatherId;
	}

	public String getId() {
		return id;
	}

	public String getMotherId() {
		return motherId;
	}

	public double getPhenotype() {
		return phenotype;
	}

	public Sex getSex() {
		return sex;
	}

	/**
	 * Is phenotype 'Case'?
	 * @return
	 */
	public boolean isCase() {
		return phenotype == PHENOTYPE_CASE;
	}

	/**
	 * Is phenotype 'Control'?
	 * @return
	 */
	public boolean isControl() {
		return phenotype == PHENOTYPE_CONTROL;
	}

	/**
	 * Is phenotype 'Missing'?
	 * @return
	 */
	public boolean isMissing() {
		return phenotype == PHENOTYPE_MISSING;
	}

	/**
	 * Parse a line form a TFAM file
	 * @param line
	 */
	protected void parse(String line) {
		String fields[] = line.split("\\s", 7);
		parse(fields);
	}

	/**
	 * Parse fields form a line
	 * @param fields
	 */
	protected int parse(String fields[]) {
		int fieldNum = 0;
		familyId = fields[fieldNum++];
		id = fields[fieldNum++];
		fatherId = fields[fieldNum++];
		motherId = fields[fieldNum++];

		if (fatherId.equals("0") || fatherId.equals("NA")) fatherId = null;
		if (motherId.equals("0") || motherId.equals("NA")) motherId = null;

		// Parse sex field
		int sexnum = Gpr.parseIntSafe(fields[fieldNum++]);
		sex = Sex.Unknown;
		if (sexnum == 1) sex = Sex.Male;
		else if (sexnum == 2) sex = Sex.Female;

		// Parse phenotype field
		String phenotypeStr = fields[fieldNum++];
		if (phenotypeStr.equals("0") || phenotypeStr.equals("-9")) phenotype = PHENOTYPE_MISSING; // These means 'missing'
		else phenotype = Gpr.parseDoubleSafe(phenotypeStr);

		return fieldNum;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		String sexStr;
		if (sex == Sex.Male) sexStr = "1";
		else if (sex == Sex.Female) sexStr = "2";
		else sexStr = "0";

		sb.append(familyId + "\t");
		sb.append(id + "\t");
		sb.append(fatherId + "\t");
		sb.append(motherId + "\t");
		sb.append(sexStr + "\t");
		sb.append(phenotype);

		return sb.toString();
	}
}
