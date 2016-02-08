package org.snpeff.genBank;

/**
 * A file containing one or more set of features (e.g. multiple chromosomes concatenated in a single file)
 * @author pcingola
 *
 */
public class EmblFile extends FeaturesFile {

	public EmblFile(String fileName) {
		super(fileName);
	}

	@Override
	Embl readNext() {
		return new Embl(lineFileIterator);
	}

}
