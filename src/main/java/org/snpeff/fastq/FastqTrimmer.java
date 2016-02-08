package org.snpeff.fastq;

/**
 * Trim fastq sequence when quality drops below a threshold
 * The resulting sequence has to ba at least 'minBases'
 * @author pcingola
 *
 */
public class FastqTrimmer {

	public static boolean debug = false;

	int qualityThreshold; // Quality threshold
	FastqBuilder fastqBuilder;
	int minBases; // How many bases do we need to report a sequence 

	public FastqTrimmer(int qualityThreshold, int minBases) {
		this.qualityThreshold = qualityThreshold;
		this.minBases = minBases;
		fastqBuilder = new FastqBuilder();
	}

	/**
	 * Create a new fastq sequence by trimming the given sequence
	 * @param fastq
	 * @return
	 */
	public Fastq trim(Fastq fastq) {
		int idx = trimIndex(fastq);
		if( idx < minBases ) idx = 0; // At leas 'minBases' long

		fastqBuilder.withDescription(fastq.getDescription());
		fastqBuilder.withVariant(fastq.getVariant());
		fastqBuilder.withSequence(fastq.getSequence().substring(0, idx));
		fastqBuilder.withQuality(fastq.getQuality().substring(0, idx));
		return fastqBuilder.build();
	}

	/**
	 * Return index where the sequence should be trimmed
	 * First time quality drops below 'qualityThreshold'
	 */
	int trimIndex(Fastq fastq) {
		int qual[] = FastqTools.qualtityArray(fastq);
		for( int i = 0; i < qual.length; i++ )
			if( qual[i] < qualityThreshold ) return i;
		return qual.length;
	}
}
