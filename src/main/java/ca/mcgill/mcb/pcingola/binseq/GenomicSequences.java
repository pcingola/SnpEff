package ca.mcgill.mcb.pcingola.binseq;

import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;

/**
 * This class stores all "relevant" sequences in a genome
 * 
 * This class is able to:
 * 		i) Add all regions of interest
 * 		ii) Store genomic sequences for those regions of interest
 * 		iii) Retrieve genomic sequences by interval
 * 
 * 
 * @author pcingola
 */
public class GenomicSequences {

	Genome genome; // Reference genome
	IntervalForest intervalForest; // This is an interval forest of 'MarkerSeq' (genomic markers that have sequences)

	public GenomicSequences(Genome genome) {
		this.genome = genome;
	}

	/**
	 * Find genomic sequences for all genes
	 */
	public void findGenes() {

	}
}
