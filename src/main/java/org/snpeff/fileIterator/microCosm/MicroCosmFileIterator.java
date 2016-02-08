package org.snpeff.fileIterator.microCosm;

import org.snpeff.fileIterator.LineClassFileIterator;

/**
 * Iterate on each line of a MicroCosm predictions
 * 
 * References: 
 * 		http://www.ebi.ac.uk/enright-srv/microcosm/
 * 
 * @author pcingola
 */
public class MicroCosmFileIterator extends LineClassFileIterator<MicroCosmEntry> {

	public MicroCosmFileIterator(String fileName) {
		super(fileName, MicroCosmEntry.class, "group;miRnaName;method;feature;chr;start;end;strand;phase;score;pValue;transcriptId;externalName;");
	}
}
