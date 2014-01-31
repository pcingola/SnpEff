package ca.mcgill.mcb.pcingola;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import ca.mcgill.mcb.pcingola.fastq.FastqVariant;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Convert qseq file to fastq
 * 
 * @author pcingola
 */
public class Qseq2FastqSplit {

	public static final int SHOW_EVERY = 100000;

	FastqVariant fastqVariant = FastqVariant.FASTQ_ILLUMINA;
	Options options;
	String inPe1, inPe2, inIndex;
	String outBase;
	HashSet<String> seqs = new HashSet<String>();
	HashMap<String, BufferedWriter> outsPe1 = new HashMap<String, BufferedWriter>();
	HashMap<String, BufferedWriter> outsPe2 = new HashMap<String, BufferedWriter>();

	public static void main(String[] args) {
		Qseq2FastqSplit qseq2FastqSplit = new Qseq2FastqSplit();
		qseq2FastqSplit.parseCmdLineOptions(args);
		qseq2FastqSplit.run();
	}

	@SuppressWarnings("static-access")
	void parseCmdLineOptions(String[] args) {
		//---
		// Create command line options
		//---
		Option help = OptionBuilder.withArgName("help").withDescription("Show this help message").create("help");
		Option inPe1Opt = OptionBuilder.withArgName("file").hasArg().withDescription("Qseq file for pair end 1").create("1");
		Option inPe2Opt = OptionBuilder.withArgName("file").hasArg().withDescription("Qseq file for pair end 2").create("2");
		Option indexOpt = OptionBuilder.withArgName("file").hasArg().withDescription("Qseq index ").create("i");
		Option seqsOpt = OptionBuilder.withArgName("seqs").hasArg().withDescription("Comma separated list of sequences").create("s");
		Option outOpt = OptionBuilder.withArgName("base").hasArg().withDescription("Base name for output files").create("o");

		options = new Options();
		options.addOption(help).addOption(inPe1Opt).addOption(inPe2Opt).addOption(indexOpt).addOption(seqsOpt).addOption(outOpt);

		//---
		// Parse command line args
		//---
		CommandLineParser parser = new PosixParser();
		try {
			CommandLine line = parser.parse(options, args);

			// Show help message?
			if( line.hasOption("help") ) {
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp(Qseq2FastqSplit.class.getSimpleName(), options);
				System.exit(-1);
			}

			if( line.hasOption("1") ) inPe1 = line.getOptionValue("1");
			else parsingError("Missing required parameter '-1'");

			if( line.hasOption("2") ) inPe2 = line.getOptionValue("2");
			else parsingError("Missing required parameter '-2'");

			if( line.hasOption("i") ) inIndex = line.getOptionValue("i");
			else parsingError("Missing required parameter '-i'");

			if( line.hasOption("o") ) outBase = line.getOptionValue("o");
			else parsingError("Missing required parameter '-o'");

			if( line.hasOption("s") ) {
				String seqsStr = line.getOptionValue("s");
				for( String s : seqsStr.split(",") )
					seqs.add(s.toUpperCase());
			} else parsingError("Missing required parameter '-s'");

		} catch(ParseException e) {
			parsingError(e.getMessage());
		}
	}

	void parsingError(String msg) {
		if( msg != null ) System.out.println("Error:" + msg);
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(Qseq2FastqSplit.class.getSimpleName(), options);
		System.exit(-1);
	}

	void run() {
		long countUnknown = 0;
		long lineNum = 1;

		Timer.showStdErr("Converting lines from QSEQ to FASTQ (Sanger)");

		// Process file
		try {
			String linePe1, linePe2, lineIdx;

			// Open inputs
			BufferedReader inPe1Buff = new BufferedReader(new InputStreamReader(new FileInputStream(inPe1)));
			BufferedReader inPe2Buff = new BufferedReader(new InputStreamReader(new FileInputStream(inPe2)));
			BufferedReader indexBuff = new BufferedReader(new InputStreamReader(new FileInputStream(inIndex)));

			// Open outputs
			for( String seq : seqs ) {
				String fileName = outBase + "_1_" + seq + ".fastq";
				BufferedWriter outBuff = new BufferedWriter(new FileWriter(fileName));
				outsPe1.put(seq, outBuff);

				fileName = outBase + "_2_" + seq + ".fastq";
				outBuff = new BufferedWriter(new FileWriter(fileName));
				outsPe2.put(seq, outBuff);
			}

			// Read inputs
			for( ; ((linePe1 = inPe1Buff.readLine()) != null) & ((linePe2 = inPe2Buff.readLine()) != null) & ((lineIdx = indexBuff.readLine()) != null); lineNum++ ) {
				// Get index sequence
				String t[] = lineIdx.split("\t");
				String seqIdx = t[8].toUpperCase();
				if( seqs.contains(seqIdx) ) { // Is it one of the sequences from command line?
					writeFastq(linePe1, outsPe1.get(seqIdx), lineNum);
					writeFastq(linePe2, outsPe2.get(seqIdx), lineNum);
				} else {
					// Gpr.debug("Line: " + lineNum + "\t" + seqIdx);
					countUnknown++;
				}

				if( lineNum % SHOW_EVERY == 0 ) Timer.showStdErr(lineNum + " lines, " + countUnknown + " unknown.");
			}

			// Close outputs
			for( String seq : seqs ) {
				outsPe1.get(seq).close();
				outsPe2.get(seq).close();
			}

			// Close inputs
			inPe1Buff.close();
			inPe2Buff.close();
			indexBuff.close();
		} catch(Exception e) {
			throw new RuntimeException(e);
		}

		lineNum--;
		Timer.showStdErr(lineNum + " lines, " + countUnknown + " unknown.");
	}

	/**
	 * Write a qseq as a fastq
	 * @param qseqLine
	 * @param outBuff
	 * @param seqNum
	 * @throws IOException
	 */
	void writeFastq(String qseqLine, BufferedWriter outBuff, long seqNum) throws IOException {
		String t[] = qseqLine.split("\t");
		outBuff.write("@seq_" + seqNum + "\n");
		outBuff.write(t[8] + "\n");
		outBuff.write("+\n");
		outBuff.write(GprSeq.changeQuality(t[9], fastqVariant, FastqVariant.FASTQ_SANGER) + "\n"); // Convert quality to Sanger
	}

}
