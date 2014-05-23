package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Variant;

/**
 * Opens a sequence change file and iterates over all sequence changes
 * 
 * @author pcingola
 */
public abstract class VariantFileIterator extends MarkerFileIterator<Variant> {

	public VariantFileIterator(String fileName, Genome genome) {
		super(fileName, genome, 1);
	}

	public VariantFileIterator(String fileName) {
		super(fileName, 1);
	}
}
