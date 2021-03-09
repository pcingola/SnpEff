package org.snpeff;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.snpeff.fastq.FastqVariant;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Convert qseq file to fastq
 *
 * @author pcingola
 */
public class Qseq2Fastq {

	public static final int SHOW_EVERY = 100000;

	public static void main(String[] args) {

		// Parse argument
		FastqVariant fastqVariant = FastqVariant.FASTQ_ILLUMINA;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("-phred33")) fastqVariant = FastqVariant.FASTQ_SANGER;
			else if (args[0].equalsIgnoreCase("-phred64")) fastqVariant = FastqVariant.FASTQ_ILLUMINA;
			else {
				// Error? Show usage and exit
				System.err.println("Usage: cat in.qseq | Qseq2Fastq [-phred33 | -phred64] > out.fastq");
				System.err.println("Options:");
				System.err.println("\t-phred33:\t Input qseq file is in phred33 format (Sanger)");
				System.err.println("\t-phred64:\t Input qseq file is in phred64 format (Illumina). This is the default.");
				System.exit(-1);
			}
		}

		Log.info("Converting lines from QSEQ to FASTQ (Sanger)");

		// Process file
		try {
			// Convert stdin
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			for (long i = 1; (line = in.readLine()) != null; i++) {
				String t[] = line.split("\t");
				System.out.println("@seq_" + i);
				System.out.println(t[8]);
				System.out.println("+");
				System.out.println(GprSeq.changeQuality(t[9], fastqVariant, FastqVariant.FASTQ_SANGER)); // Convert quality to Sanger

				if (i % SHOW_EVERY == 0) Log.info(i + " lines.");
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
