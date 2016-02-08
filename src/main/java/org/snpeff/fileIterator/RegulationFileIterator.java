package org.snpeff.fileIterator;

import org.snpeff.interval.Genome;
import org.snpeff.interval.Regulation;

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
