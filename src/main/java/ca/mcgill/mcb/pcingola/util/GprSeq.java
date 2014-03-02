package ca.mcgill.mcb.pcingola.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

import ca.mcgill.mcb.pcingola.fastq.Fastq;
import ca.mcgill.mcb.pcingola.fastq.FastqVariant;

public class GprSeq {

	public static final char FASTQ_SANGER_ZERO = '!';

	public static final char BASES[] = { 'A', 'C', 'G', 'T' };
	public static final char AMINO_ACIDS[] = { 'A', 'R', 'N', 'D', 'C', 'E', 'Q', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V' };

	public static byte AA_TO_CODE[];
	public static char CODE_TO_AA[];
	public static byte DNA_TO_CODE[];
	public static byte CODE_TO_DNA[];

	public static final String KNOWN_FILE_EXTENSIONS[] = { ".fa", ".fasta", ".fq", ".fastq", ".sai", ".sam", ".bam", ".bcf", ".vcf", "pileup", "mpileup" };

	static {
		AA_TO_CODE = new byte[256];
		DNA_TO_CODE = new byte[256];

		// Initialize everything as 'missing'
		for (int i = 0; i < AA_TO_CODE.length; i++)
			AA_TO_CODE[i] = DNA_TO_CODE[i] = -1;

		// Initialize forward mapping: AA -> Code
		AA_TO_CODE['-'] = 0; // Gap
		AA_TO_CODE['A'] = 1;
		AA_TO_CODE['B'] = 2;
		AA_TO_CODE['C'] = 3;
		AA_TO_CODE['D'] = 4;
		AA_TO_CODE['E'] = 5;
		AA_TO_CODE['F'] = 6;
		AA_TO_CODE['G'] = 7;
		AA_TO_CODE['H'] = 8;
		AA_TO_CODE['I'] = 9;
		AA_TO_CODE['K'] = 10;
		AA_TO_CODE['L'] = 11;
		AA_TO_CODE['M'] = 12;
		AA_TO_CODE['N'] = 13;
		AA_TO_CODE['P'] = 14;
		AA_TO_CODE['Q'] = 15;
		AA_TO_CODE['R'] = 16;
		AA_TO_CODE['S'] = 17;
		AA_TO_CODE['T'] = 18;
		AA_TO_CODE['V'] = 19;
		AA_TO_CODE['W'] = 20;
		AA_TO_CODE['X'] = 21;
		AA_TO_CODE['Y'] = 22;
		AA_TO_CODE['Z'] = 23;
		AA_TO_CODE['*'] = 24; // Stop Codon
		AA_TO_CODE['U'] = 25; // Selenocysteine (Rare amino acid)

		// Initialize reverse mapping: Codes -> AA
		CODE_TO_AA = new char[26];
		CODE_TO_AA[0] = '-'; // Gap
		CODE_TO_AA[1] = 'A';
		CODE_TO_AA[2] = 'B';
		CODE_TO_AA[3] = 'C';
		CODE_TO_AA[4] = 'D';
		CODE_TO_AA[5] = 'E';
		CODE_TO_AA[6] = 'F';
		CODE_TO_AA[7] = 'G';
		CODE_TO_AA[8] = 'H';
		CODE_TO_AA[9] = 'I';
		CODE_TO_AA[10] = 'K';
		CODE_TO_AA[11] = 'L';
		CODE_TO_AA[12] = 'M';
		CODE_TO_AA[13] = 'N';
		CODE_TO_AA[14] = 'P';
		CODE_TO_AA[15] = 'Q';
		CODE_TO_AA[16] = 'R';
		CODE_TO_AA[17] = 'S';
		CODE_TO_AA[18] = 'T';
		CODE_TO_AA[19] = 'V';
		CODE_TO_AA[20] = 'W';
		CODE_TO_AA[21] = 'X';
		CODE_TO_AA[22] = 'Y';
		CODE_TO_AA[23] = 'Z';
		CODE_TO_AA[24] = '*'; // Stop Codon
		CODE_TO_AA[25] = 'U'; // Selenocysteine (Rare amino acid)
	}

	public static byte aa2Code(char aa) {
		byte c = AA_TO_CODE[(byte) aa];
		if (c < 0) throw new RuntimeException("Unknown code for amino acid '" + aa + "' (ord: " + ((int) aa) + " )");
		return c;
	}

	/**
	 * Change a fastQ encoding in a quality sequence
	 * @param qualityStr
	 * @return
	 */
	public static String changeQuality(String qualityStr, FastqVariant fqSrc, FastqVariant fqDst) {
		if (fqSrc == fqDst) return qualityStr; // Nothing to do

		// Source & destination offset
		char src, dst;
		switch (fqSrc) {
		case FASTQ_SOLEXA:
		case FASTQ_ILLUMINA:
			src = 64;
			break;
		case FASTQ_SANGER:
			src = 33;
			break;
		default:
			throw new RuntimeException("Unimplemented fastq variant '" + fqSrc + "'");
		}

		switch (fqDst) {
		case FASTQ_SOLEXA:
		case FASTQ_ILLUMINA:
			dst = 64;
			break;
		case FASTQ_SANGER:
			dst = 33;
			break;
		default:
			throw new RuntimeException("Unimplemented fastq variant '" + fqDst + "'");
		}
		int diff = dst - src;
		if (diff == 0) return qualityStr; // Nothing to do

		// Change each quality
		char oldQ[] = qualityStr.toCharArray();
		char newQ[] = new char[oldQ.length];
		for (int i = 0; i < oldQ.length; i++) {
			int q = oldQ[i] - src;

			// Sanity check
			if (q < -5) throw new RuntimeException("Invalid quality char '" + oldQ[i] + "' (quality = " + q + "). This doesn't look like a valid '" + fqSrc + "' format");

			newQ[i] = (char) (Math.max(0, q) + dst);
		}

		return new String(newQ);
	}

	public static char code2aa(byte aacode) {
		return CODE_TO_AA[aacode];

	}

	/**
	 * Read a fasta file containing one (and only one) sequence
	 * @param chrName
	 * @return
	 */
	public static String fastaSimpleRead(String fastaFile) {
		StringBuilder sb = new StringBuilder();

		BufferedReader inFile;
		try {
			// Open file (either 'regular' or gzipped)
			inFile = Gpr.reader(fastaFile);
			if (inFile == null) return ""; // Error opening file

			String line = inFile.readLine(); // Discard first line
			while (inFile.ready()) {
				line = inFile.readLine().trim(); // Read a line
				sb.append(line);
			}
			inFile.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return sb.toString();
	}

	/**
	 * Get an ID from a fastq 
	 * @param fastq
	 * @return Fastq's id
	 */
	public static String fastqId(Fastq fastq) {
		return readId(fastq.getDescription().substring(1)); // Remove leading '@' (or '+') and create a read ID
	}

	/**
	 * Calculate frame (as specified in GTF / GFF) using sequence length
	 * References: http://mblab.wustl.edu/GTF22.html
	 * 
	 * What frame means:
	 * 		'0' indicates that the specified region is in frame, i.e. that 
	 * 			its first base corresponds to the first base of a codon.
	 *  
	 * 		'1' indicates that there is one extra base, i.e. that the 
	 * 			second base of the region corresponds to the first base of a codon
	 * 
	 * 		'2' means that the third base of the region is the first base of a 
	 * 			codon. 
	 * 
	 * If the strand is '-', then the first base of the region is value of 'end', because 
	 * the corresponding coding region will run from <end> to <start> on the reverse strand.
	 * 
	 * Frame is calculated as (3 - ((length-frame) mod 3)) mod 3:
	 * Here is why:
	 * 		(length-frame) is the length of the previous feature starting at the first whole codon (and thus the frame subtracted out).
	 * 		(length-frame) mod 3 is the number of bases on the 3' end beyond the last whole codon of the previous feature.
	 * 		3-((length-frame) mod 3) is the number of bases left in the codon after removing those that are represented at the 3' end of the feature.
	 * 		(3-((length-frame) mod 3)) mod 3 changes a 3 to a 0, since three bases makes a whole codon, and 1 and 2 are left unchanged.
	 * 
	 * @param length
	 * @return
	 */
	public static int frameFromLength(int length) {
		return (3 - (length % 3)) % 3;
	}

	/**
	 * Are there any ambiguous bases in this sequence?
	 * @param sequence
	 * @return
	 */
	public static boolean isAmbiguous(String sequence) {
		char seq[] = sequence.toLowerCase().toCharArray();
		for (int i = 0; i < seq.length; i++) {
			char c = seq[i];
			if ((c != 'a') && (c != 'c') && (c != 'g') && (c != 't')) return true;
		}
		return false;
	}

	/**
	 * Random base
	 * @return
	 */
	public static char randBase(Random random) {
		switch (random.nextInt(4)) {
		case 0:
			return 'A';
		case 1:
			return 'C';
		case 2:
			return 'G';
		case 3:
			return 'T';
		default:
			throw new RuntimeException("This should never happen!");
		}
	}

	/**
	 * Random sequence
	 * @param len
	 * @return
	 */
	public static String randSequence(Random random, int len) {
		char bases[] = new char[len];
		for (int i = 0; i < len; i++)
			bases[i] = randBase(random);
		return new String(bases);
	}

	/**
	 * Create an ID: Remove everything after the first space char. 
	 * Remove trailing '/1' or '/2' (if any)
	 * 
	 * @param line
	 * @return
	 */
	public static String readId(String line) {
		String id = line.split("\\s")[0]; // Remove everything after the first space character
		if (id.endsWith("/1")) return id.substring(0, id.length() - 2);
		if (id.endsWith("/2")) return id.substring(0, id.length() - 2);
		return id;
	}

	public static String removeExt(String fileName) {
		return Gpr.removeExt(fileName, KNOWN_FILE_EXTENSIONS);
	}

	/**
	 * Reverse of a string (sequence)
	 * @param seq
	 * @return
	 */
	public static String reverse(String seq) {
		char reverse[] = new char[seq.length()];
		int i = reverse.length - 1;
		char bases[] = seq.toCharArray();
		for (char base : bases)
			reverse[i--] = base;
		return new String(reverse);
	}

	/**
	 * Reverse Watson-Cricks complement
	 * @param seq
	 * @return
	 */
	public static String reverseWc(String seq) {
		char rwc[] = new char[seq.length()];
		int i = rwc.length - 1;
		char bases[] = seq.toCharArray();
		for (char base : bases)
			rwc[i--] = wc(base);

		return new String(rwc);
	}

	/**
	 * Transform into a FASTA formatted string
	 * @param name
	 * @param sequence
	 * @return
	 */
	public static String string2fasta(String name, String sequence) {
		StringBuffer sb = new StringBuffer();
		sb.append(">" + name + "\n");

		int lineLen = 80;
		for (int i = 0; i < sequence.length(); i += lineLen) {
			int max = Math.min(i + lineLen, sequence.length());
			sb.append(sequence.substring(i, max) + "\n");
		}

		return sb.toString();
	}

	/**
	 * Watson-Cricks complement
	 * @param sequenceBits
	 * @return
	 */
	public static char wc(char base) {
		switch (base) {
		case 'A':
		case 'a':
			return 'T';
		case 'C':
		case 'c':
			return 'G';
		case 'G':
		case 'g':
			return 'C';
		case 'T':
		case 't':
		case 'U':
		case 'u':
			return 'A';
		case 'n':
		case 'N':
			return 'N';
		default:
			return base;
			// throw new RuntimeException("Unknown base '" + base + "'");
		}
	}

	/**
	 * Watson-Cricks complement
	 * @param seq
	 * @return
	 */
	public static String wc(String seq) {
		char rwc[] = new char[seq.length()];
		char bases[] = seq.toCharArray();
		for (int i = 0; i < bases.length; i++)
			rwc[i] = wc(bases[i]);
		return new String(rwc);
	}

}
