package org.snpeff.genBank;

/**
 * A file containing one or more set of features (e.g. multiple chromosomes concatenated in a single file)
 * Ref:
 * 		http://www.insdc.org/documents/feature-table
 *
 * @author pcingola
 */
public class GenBankFile extends FeaturesFile {

	public GenBankFile(String fileName) {
		super(fileName);
	}

	@Override
	GenBank readNext() {
		return new GenBank(lineFileIterator);
	}

}
