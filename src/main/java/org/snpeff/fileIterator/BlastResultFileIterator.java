package org.snpeff.fileIterator;


/**
 * Iterate on each line of a GWAS catalog (TXT format)
 * 
 * @author pcingola
 */
public class BlastResultFileIterator extends LineClassFileIterator<BlastResultEntry> {

	public BlastResultFileIterator(String fileName) {
		super(fileName, BlastResultEntry.class, "qseqid;sseqid;pident;length;mismatch;gapopen;qstart;qend;sstart;send;evalue;bitscore");
	}
}
