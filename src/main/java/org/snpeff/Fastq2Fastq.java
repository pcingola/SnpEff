package org.snpeff;

import org.snpeff.fastq.Fastq;
import org.snpeff.fastq.FastqVariant;
import org.snpeff.fileIterator.FastqFileIterator;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Convert FASTQ (phred64) file to FASTQ (phred33)
 *
 * @author pcingola
 */
public class Fastq2Fastq {

	public static final int SHOW_EVERY = 100000;

	public static void main(String[] args) {

		FastqVariant fastqVariantIn = FastqVariant.FASTQ_ILLUMINA;
		FastqVariant fastqVariantOut = FastqVariant.FASTQ_SANGER;

		if (args.length != 1) {
			System.err.println("Usage: Fastq2Fastq inFile.fastq > outFile.fastq");
			System.exit(-1);
		}

		// Parse command lien argument
		String inFile = args[0];
		Log.info("Converting lines from FASTQ (Illumina) to FASTQ (Sanger). Input file '" + inFile + "'");

		// Process file
		try {
			// Convert stdin
			int i = 1;
			FastqFileIterator ffi = new FastqFileIterator(inFile, fastqVariantIn);
			for (Fastq fastq : ffi) {
				System.out.println(fastq.getDescription());
				System.out.println(fastq.getSequence());
				System.out.println("+");
				System.out.println(GprSeq.changeQuality(fastq.getQuality(), fastqVariantIn, fastqVariantOut)); // Convert quality to Sanger

				if (i % SHOW_EVERY == 0) Log.info(i + " sequences.");
				i++;
			}
			ffi.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
