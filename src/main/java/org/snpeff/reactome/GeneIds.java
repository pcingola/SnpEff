package org.snpeff.reactome;

import org.snpeff.fileIterator.LineFileIterator;
import org.snpeff.gtex.IdMap;
import org.snpeff.util.Gpr;

/**
 * Maps different Gene IDs:
 * 		- ENSEMBL Gene ID to transcript ID
 * 		- ENSEMBL Gene ID to Gene Name
 * 		- ENSEMBL Gene ID to Refseq Gene ID
 * 		- ENSEMBL Gene ID to Refseq Protein ID
 * 
 * @author pcingola
 */
public class GeneIds {

	IdMap id2tr = new IdMap();
	IdMap id2geneName = new IdMap();
	IdMap id2refseqId = new IdMap();
	IdMap id2refseqProtId = new IdMap();

	public GeneIds(String fileName) {
		load(fileName);
	}

	public IdMap getId2geneName() {
		return id2geneName;
	}

	public IdMap getId2refseqId() {
		return id2refseqId;
	}

	public IdMap getId2refseqProtId() {
		return id2refseqProtId;
	}

	public IdMap getId2tr() {
		return id2tr;
	}

	/**
	 * Load data from file
	 * @param fileName
	 */
	protected void load(String fileName) {
		if (!Gpr.canRead(fileName)) throw new RuntimeException("Cannot read file '" + fileName + "'");

		LineFileIterator lfi = new LineFileIterator(fileName);
		for (String line : lfi) {
			if (lfi.getLineNum() <= 1) continue;
			String fields[] = line.split("\t", -1); // Split , don't trim trailing entries

			// Parse field
			String geneId = fields[0];
			String trId = fields[1];
			String geneName = fields[2];
			String refseqId = fields[3];
			String refseqProId = fields[4];

			// Add mappings
			id2tr.add(geneId, trId);
			id2geneName.add(geneId, geneName);
			id2refseqId.add(geneId, refseqId);
			id2refseqProtId.add(geneId, refseqProId);
		}
	}

}
