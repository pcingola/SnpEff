package org.snpeff.fileIterator;

/**
 * One line per sequence. Actually it is exactly the same as LineFilteIterator
 * 
 * @author pcingola
 */
public class LineSeqFileIterator extends LineFileIterator {

	public static boolean debug = false;

	public LineSeqFileIterator(String lineSeqFileName) {
		super(lineSeqFileName);
	}

}
