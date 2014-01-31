package ca.mcgill.mcb.pcingola.fileIterator;

/**
 * Iterate on each line of a file, creating a MatrixEntry
 * 
 * @author pcingola
 */
public class MatrixEntryFileIterator extends LineClassFileIterator<MatrixEntry> {

	public MatrixEntryFileIterator(String fileName) {
		super(fileName, MatrixEntry.class, "chr;pos;id;ref;alt;matrix");
	}

}
