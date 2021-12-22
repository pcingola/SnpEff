package org.snpeff.util;

import org.snpeff.fastq.Fastq;
import org.snpeff.fastq.FastqVariant;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Random;

public class GprSeq {

    public static final char FASTQ_SANGER_ZERO = '!';

    public static final char[] BASES = {'A', 'C', 'G', 'T'};
    public static final char[] AMINO_ACIDS = {'A', 'R', 'N', 'D', 'C', 'E', 'Q', 'G', 'H', 'I', 'L', 'K', 'M', 'F', 'P', 'S', 'T', 'W', 'Y', 'V'};
    public static final byte GAP_CODE = (byte) -1;
    public static final String[] KNOWN_FILE_EXTENSIONS = {".fa", ".fasta", ".fq", ".fastq", ".sai", ".sam", ".bam", ".bcf", ".vcf", "pileup", "mpileup"};
    public static byte[] AA_TO_CODE;
    public static char[] CODE_TO_AA;
    public static byte[] DNA_TO_CODE;
    public static char[] CODE_TO_DNA;

    static {
        AA_TO_CODE = new byte[256];
        DNA_TO_CODE = new byte[256];

        // Initialize everything as 'missing'
        for (int i = 0; i < AA_TO_CODE.length; i++)
            AA_TO_CODE[i] = DNA_TO_CODE[i] = -1;

        // Initialize forward mapping: AA -> Code
        AA_TO_CODE['-'] = -1; // Gap
        AA_TO_CODE['A'] = 0;
        AA_TO_CODE['R'] = 1;
        AA_TO_CODE['N'] = 2;
        AA_TO_CODE['D'] = 3;
        AA_TO_CODE['C'] = 4;
        AA_TO_CODE['E'] = 5;
        AA_TO_CODE['Q'] = 6;
        AA_TO_CODE['G'] = 7;
        AA_TO_CODE['H'] = 8;
        AA_TO_CODE['I'] = 9;
        AA_TO_CODE['L'] = 10;
        AA_TO_CODE['K'] = 11;
        AA_TO_CODE['M'] = 12;
        AA_TO_CODE['F'] = 13;
        AA_TO_CODE['P'] = 14;
        AA_TO_CODE['S'] = 15;
        AA_TO_CODE['T'] = 16;
        AA_TO_CODE['W'] = 17;
        AA_TO_CODE['Y'] = 18;
        AA_TO_CODE['V'] = 19;
        AA_TO_CODE['*'] = 20; // Stop Codon
        AA_TO_CODE['U'] = 21; // Selenocysteine (Rare amino acid)
        AA_TO_CODE['O'] = 22; // Pyrrolysine (Rare amino acid)

        // Initialize reverse mapping: Codes -> AA
        CODE_TO_AA = new char[23];
        CODE_TO_AA[0] = 'A';
        CODE_TO_AA[1] = 'R';
        CODE_TO_AA[2] = 'N';
        CODE_TO_AA[3] = 'D';
        CODE_TO_AA[4] = 'C';
        CODE_TO_AA[5] = 'E';
        CODE_TO_AA[6] = 'Q';
        CODE_TO_AA[7] = 'G';
        CODE_TO_AA[8] = 'H';
        CODE_TO_AA[9] = 'I';
        CODE_TO_AA[10] = 'L';
        CODE_TO_AA[11] = 'K';
        CODE_TO_AA[12] = 'M';
        CODE_TO_AA[13] = 'F';
        CODE_TO_AA[14] = 'P';
        CODE_TO_AA[15] = 'S';
        CODE_TO_AA[16] = 'T';
        CODE_TO_AA[17] = 'W';
        CODE_TO_AA[18] = 'Y';
        CODE_TO_AA[19] = 'V';
        CODE_TO_AA[20] = '*'; // Stop Codon
        CODE_TO_AA[21] = 'U'; // Selenocysteine (Rare amino acid)
        CODE_TO_AA[22] = 'O'; // Pyrrolysine (Rare amino acid)

        // DNA
        DNA_TO_CODE['A'] = 0;
        DNA_TO_CODE['C'] = 1;
        DNA_TO_CODE['G'] = 2;
        DNA_TO_CODE['T'] = 3;

        CODE_TO_DNA = new char[4];
        CODE_TO_DNA[0] = 'A';
        CODE_TO_DNA[1] = 'C';
        CODE_TO_DNA[2] = 'G';
        CODE_TO_DNA[3] = 'T';
    }

    /**
     * Convert from AA char to code
     */
    public static byte aa2Code(char aa) {
        if (aa == '-') return GAP_CODE;
        byte c = AA_TO_CODE[(byte) aa];
        if (c < 0) throw new RuntimeException("Unknown code for amino acid '" + aa + "' (ord: " + ((int) aa) + " )");
        return c;
    }

    /**
     * Convert from AA sequence to codes
     */
    public static byte[] aa2Code(String aa) {
        byte[] codes = new byte[aa.length()];
        for (int i = 0; i < codes.length; i++)
            codes[i] = aa2Code(aa.charAt(i));
        return codes;
    }

    /**
     * Code an AA-pair
     */
    public static int aaPairCode(byte aaCode1, byte aaCode2) {
        if (aaCode1 < 0 || aaCode2 < 0) return -1;
        return aaCode1 * GprSeq.AMINO_ACIDS.length + aaCode2;
    }

    /**
     * Code an AA-pair
     */
    public static int aaPairCode(char aa1, char aa2) {
        if (aa1 == '-' || aa2 == '-') return -1;
        return aa2Code(aa1) * GprSeq.AMINO_ACIDS.length + aa2Code(aa2);
    }

    /**
     * Change a fastQ encoding in a quality sequence
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
        char[] oldQ = qualityStr.toCharArray();
        char[] newQ = new char[oldQ.length];
        for (int i = 0; i < oldQ.length; i++) {
            int q = oldQ[i] - src;

            // Sanity check
            if (q < -5)
                throw new RuntimeException("Invalid quality char '" + oldQ[i] + "' (quality = " + q + "). This doesn't look like a valid '" + fqSrc + "' format");

            newQ[i] = (char) (Math.max(0, q) + dst);
        }

        return new String(newQ);
    }

    /**
     * Convert from AA_code to AA letter
     */
    public static char code2aa(byte aacode) {
        if (aacode < 0) return '-';
        return CODE_TO_AA[aacode];
    }

    /**
     * Convert from AA_code to AA letter
     */
    public static String code2aa(byte[] aacodes) {
        char[] c = new char[aacodes.length];

        for (int i = 0; i < aacodes.length; i++)
            c[i] = code2aa(aacodes[i]);

        return new String(c);
    }

    /**
     * Convert from AA_code to AA letter
     */
    public static String code2aa(int[] aacodes) {
        char[] c = new char[aacodes.length];

        for (int i = 0; i < aacodes.length; i++) {
            if (aacodes[i] < 0) c[i] = '-';
            else c[i] = CODE_TO_AA[aacodes[i]];
        }

        return new String(c);
    }

    public static String code2aaPair(int code) {
        int aaCode1 = code / GprSeq.AMINO_ACIDS.length;
        int aaCode2 = code % GprSeq.AMINO_ACIDS.length;
        return "" + GprSeq.code2aa((byte) aaCode1) + GprSeq.code2aa((byte) aaCode2);
    }

    /**
     * Convert from DNA_code to DNA letter
     */
    public static char code2dna(byte dnacode) {
        if (dnacode < 0) return '-';
        return CODE_TO_DNA[dnacode];
    }

    /**
     * Convert from DNA letter to code
     */
    public static byte dna2Code(char base) {
        if (base == '-') return -1;
        byte c = DNA_TO_CODE[(byte) base];
        if (c < 0)
            throw new RuntimeException("Unknown code for amino acid '" + base + "' (ord: " + ((int) base) + " )");
        return c;
    }

    /**
     * Read a fasta file containing one (and only one) sequence
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
     *
     * @param fastq
     * @return Fastq's id
     */
    public static String fastqId(Fastq fastq) {
        return readId(fastq.getDescription().substring(1)); // Remove leading '@' (or '+') and create a read ID
    }

    /**
     * Are there any ambiguous bases in this sequence?
     */
    public static boolean isAmbiguous(String sequence) {
        char[] seq = sequence.toLowerCase().toCharArray();
        for (int i = 0; i < seq.length; i++) {
            char c = seq[i];
            if ((c != 'a') && (c != 'c') && (c != 'g') && (c != 't')) return true;
        }
        return false;
    }

    public static String padN(String seq, int size) {
        if (seq.length() >= size) return seq;

        StringBuilder sb = new StringBuilder(seq);
        for (int i = seq.length(); i < size; i++)
            sb.append('N');

        return sb.toString();
    }

    /**
     * Random base
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
     */
    public static String randSequence(Random random, int len) {
        char[] bases = new char[len];
        for (int i = 0; i < len; i++)
            bases[i] = randBase(random);
        return new String(bases);
    }


    /**
     * Create an ID: Remove everything after the first space char.
     * Remove trailing '/1' or '/2' (if any)
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
     */
    public static String reverse(String seq) {
        char[] reverse = new char[seq.length()];
        int i = reverse.length - 1;
        char[] bases = seq.toCharArray();
        for (char base : bases)
            reverse[i--] = base;
        return new String(reverse);
    }

    /**
     * Reverse Watson-Cricks complement
     */
    public static String reverseWc(String seq) {
        char[] rwc = new char[seq.length()];
        int i = rwc.length - 1;
        char[] bases = seq.toCharArray();
        for (char base : bases)
            rwc[i--] = wc(base);

        return new String(rwc);
    }

    /**
     * Show differences between two sequences
     */
    public static String showMismatch(String seq1, String seq2, String prefix) {
        var sb = new StringBuilder();
        sb.append(prefix + seq1 + "\n");

        sb.append(prefix);
        var len = Math.min(seq1.length(), seq2.length());
        for (int i = 0; i < len; i++)
            sb.append(seq1.charAt(i) == seq2.charAt(i) ? '|' : '*');
        sb.append('\n');

        sb.append(prefix + seq2 + "\n");
        return sb.toString();
    }

    /**
     * Transform into a FASTA formatted string
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
     */
    public static char wc(char base) {
        switch (base) {
            case 'A':
                return 'T';
            case 'a':
                return 't';

            case 'C':
                return 'G';
            case 'c':
                return 'g';

            case 'G':
                return 'C';
            case 'g':
                return 'c';

            case 'T':
            case 'U':
                return 'A';
            case 't':
            case 'u':
                return 'a';

            case 'N':
                return 'N';
            case 'n':
                return 'n';

            default:
                return base;
        }
    }

    /**
     * Watson-Cricks complement
     */
    public static String wc(String seq) {
        char[] rwc = new char[seq.length()];
        char[] bases = seq.toCharArray();
        for (int i = 0; i < bases.length; i++)
            rwc[i] = wc(bases[i]);
        return new String(rwc);
    }
}
