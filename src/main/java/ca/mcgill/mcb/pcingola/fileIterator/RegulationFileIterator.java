package ca.mcgill.mcb.pcingola.fileIterator;

import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Regulation;

/**
 * Opens a regulation file and create Regulation elements.
 * 
 * @author pcingola
 */
public abstract class RegulationFileIterator extends MarkerFileIterator<Regulation> {

	public RegulationFileIterator(String fileName, Genome genome, int inOffset) {
		super(fileName, genome, inOffset);
	}

	public RegulationFileIterator(String fileName, int inOffset) {
		super(fileName, inOffset);
	}

}
