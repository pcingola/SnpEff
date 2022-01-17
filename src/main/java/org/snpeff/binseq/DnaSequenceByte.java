package org.snpeff.binseq;

/**
 * Binary packed DNA sequence.
 * <p>
 * Notes:
 * - This is designed for short sequences (such as "short reads")
 * - Every base is encoded in 2 bits {a, c, g, t} <=> {0, 1, 2, 3}
 * - All bits are stored in an array of 'bytes;
 * - Most significant bits are the first bases in the sequence (makes comparison easier)
 *
 * @author pcingola
 */
public class DnaSequenceByte {

    public static final int BASES_PER_BYTE = 4;
    public static final int BITS_PER_BASE = 2;
    public static final byte[] MASK = {(byte) 0xC0, 0x30, 0x0C, 0x03};
    public static final char[] BASES = {'A', 'C', 'G', 'T'};
    public static final byte[] BASE2CODE = new byte[256];

    /*
     * Static initialization
     */ static {
        BASE2CODE['a'] = 0;
        BASE2CODE['A'] = 0;
        BASE2CODE['c'] = 1;
        BASE2CODE['C'] = 1;
        BASE2CODE['g'] = 2;
        BASE2CODE['G'] = 2;
        BASE2CODE['t'] = 3;
        BASE2CODE['T'] = 3;
    }

    int length;
    byte[] codes;

    public DnaSequenceByte(int length, byte[] codes) {
        this.length = length;
        this.codes = codes;
    }

    public DnaSequenceByte(String seqStr) {
        if (seqStr != null) set(seqStr);
    }

    public char getBase(int index) {
        int idx = index / BASES_PER_BYTE;
        int pos = index % BASES_PER_BYTE;
        int code = 0xff & (codes[idx] & MASK[pos]);
        int rot = ((BASES_PER_BYTE - 1 - pos) * BITS_PER_BASE);
        code = code >>> rot;
        return BASES[code];
    }

    public byte[] getCodes() {
        return codes;
    }

    public int getLength() {
        return length;
    }

    @Override
    public int hashCode() {
        long hash = 0;
        for (int i = 0; i < codes.length; i++)
            hash = hash * 33 + codes[i];

        return (int) hash;
    }

    /**
     * Sequence lenth
     */
    public int length() {
        return length;
    }

    /**
     * Set sequence
     */
    public void set(String seqStr) {
        if (seqStr == null) {
            length = 0;
            codes = null;
        } else {
            length = seqStr.length();
            int ilen = length / BASES_PER_BYTE + (length % BASES_PER_BYTE != 0 ? 1 : 0);
            codes = new byte[ilen];
            char[] seqChar = seqStr.toCharArray();

            // Create binary sequence
            int j = 0, i = 0, k = 0;
            byte s = 0;
            for (; i < seqChar.length; i++) {
                s <<= 2;
                s |= BASE2CODE[seqChar[i]];
                k++;
                if (k >= BASES_PER_BYTE) {
                    codes[j] = s;
                    k = 0;
                    j++;
                    s = 0;
                }
            }

            // Last word: Shift the last bits
            if ((k < BASES_PER_BYTE) && (k != 0)) {
                s <<= BITS_PER_BASE * (BASES_PER_BYTE - k);
                codes[j] = s;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        char[] c = new char[length];
        for (int i = 0; i < length; i++)
            c[i] = getBase(i);

        sb.append(c);
        return sb.toString();
    }

    public String toStringHex() {
        StringBuilder sb = new StringBuilder();

        char[] c = new char[length];
        for (int i = 0; i < length; i++)
            c[i] = getBase(i);

        sb.append(c);
        sb.append("\t");

        for (int i = 0; i < codes.length; i++)
            sb.append(Integer.toHexString(codes[i]));

        return sb.toString();
    }

}
