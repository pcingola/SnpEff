package org.snpeff.gtex;

import gnu.trove.list.array.TDoubleArrayList;

/**
 * A 'column' in a GTEx file (values from one experiment
 * @author pcingola
 */
public class GtexExperiment {

	Gtex gtex;
	String id, tissueType, tissueTypeDetail;
	TDoubleArrayList values;

	public GtexExperiment(Gtex gtex, String line) {
		this.gtex = gtex;
		parse(line);
		values = new TDoubleArrayList();
	}

	public GtexExperiment(Gtex gtex, String id, String tissueType, String tissueTypeDetail) {
		this.gtex = gtex;
		this.id = id;
		this.tissueType = tissueType;
		this.tissueTypeDetail = tissueTypeDetail;
		values = new TDoubleArrayList();
	}

	public void add(double value) {
		values.add(value);
	}

	public Gtex getGtex() {
		return gtex;
	}

	public String getId() {
		return id;
	}

	public String getTissueType() {
		return tissueType;
	}

	public String getTissueTypeDetail() {
		return tissueTypeDetail;
	}

	public double getValue(int idx) {
		return values.get(idx);
	}

	/**
	 * Get value by geneID
	 * @param geneId
	 * @return
	 */
	public double getValue(String geneId) {
		int idx = gtex.getIndex(geneId);
		return values.get(idx);
	}

	protected void parse(String line) {
		String fields[] = line.split("\t");
		id = fields[0];
		tissueType = fields[5];
		tissueTypeDetail = fields[6];
	}

	public int size() {
		return values.size();
	}

	@Override
	public String toString() {
		return id + "\t" + tissueType + "\t" + tissueTypeDetail;
	}

	public String toStringAll() {
		StringBuilder sb = new StringBuilder();
		sb.append(this);

		for (int i = 0; i < values.size(); i++)
			sb.append(values.get(i) + "\t");

		return sb.toString();
	}

}
