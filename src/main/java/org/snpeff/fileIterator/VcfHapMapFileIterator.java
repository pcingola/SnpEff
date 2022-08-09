package org.snpeff.fileIterator;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;

import java.io.IOException;

/**
 * Opens a Hapmap phased file and iterates over all entries, returning VcfEntries for each line
 * Note: Each HapMap file has one chromosome. The reference sequence for the chromosome is read from a fasta file
 *
 * @author pcingola
 */
public class VcfHapMapFileIterator extends MarkerFileIterator<VcfEntry> {

    static final String format = "GT"; // VCF genotype format

    protected Genome genome;
    String fatsaFileName;
    String seq = ""; // Reference sequence
    VcfFileIterator vcfFile;
    Chromosome chr;

    public VcfHapMapFileIterator(String hapMapFileName, String fatsaFileName, Genome genome) {
        super(hapMapFileName, 1); // Positions are one based
        this.fatsaFileName = fatsaFileName;
        this.genome = genome;
    }

    /**
     * Parse a line from a hapMap file and create a VCF entry.
     *
     * @param line
     * @return
     */
    VcfEntry parseHapMapLine(String line) {
        String recs[] = line.split("\t");
        String id = recs[0];
        int start = parsePosition(recs[1]);

        // Get reference sequence
        if ((start >= seq.length()) || (start < 0)) throw new RuntimeException("Position out of chromosome range " + start + ".\n\tLine number: " + lineNum + "\t" + line);
        String ref = seq.substring(start, start + 1);

        // Create ALTS
        int count[] = new int[4]; // Count how many from each base
        for (int i = 2; i < recs.length; i++) {
            // Parse each phased entry
            String alts[] = recs[i].toUpperCase().split(" ");

            for (String alt : alts) {
                if (alt.equals("A")) count[0]++;
                else if (alt.equals("C")) count[1]++;
                else if (alt.equals("G")) count[2]++;
                else if (alt.equals("T")) count[3]++;
            }
        }

        // Remove counts for REF base
        if (ref.equals("A")) count[0] = 0;
        else if (ref.equals("C")) count[1] = 0;
        else if (ref.equals("G")) count[2] = 0;
        else if (ref.equals("T")) count[3] = 0;

        // Create ALT string
        String altStr = "";
        if (count[0] > 0) altStr += "A";
        if (count[1] > 0) altStr += (altStr.isEmpty() ? "" : ",") + "C";
        if (count[2] > 0) altStr += (altStr.isEmpty() ? "" : ",") + "G";
        if (count[3] > 0) altStr += (altStr.isEmpty() ? "" : ",") + "T";

        VcfEntry vcfEntry = null;
        if (altStr.length() <= 3) {
            vcfEntry = new VcfEntry(vcfFile, genome, chr.getId(), start, id, ref, altStr, 0.0, "", "", format);

            // Create all genotype entries
            for (int i = 2; i < recs.length; i++) {
                String alts[] = recs[i].split(" ");

                String gen = "";

                if (alts[0].equals(alts[1])) {
                    // Homozygous
                    if (alts[0].equals(ref)) gen = "0|0";
                    else gen = "1|1";
                } else {
                    // Heterozygous
                    if (alts[0].equals(ref)) gen = "0|1";
                    else gen = "1|0";
                }

                vcfEntry.addGenotype(gen);
            }

            // System.out.println(line + "\n" + vcfEntry + "\n");
        } else Log.debug("WARNING! Skipping entry:\t" + line);

        return vcfEntry;
    }

    @Override
    protected VcfEntry readNext() {
        // Read sequence from file (if needed)
        if ((seq == null) || seq.isEmpty()) readSequence();

        // Read another line from the file
        try {
            while (ready()) {
                line = readLine();
                if (line == null) return null; // End of file?

                if (lineNum > 1) { // Ignore title line
                    VcfEntry vcfEntry = parseHapMapLine(line);
                    if (vcfEntry != null) return vcfEntry;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file '" + fileName + "'. Line ignored:\n\tLine (" + lineNum + "):\t'" + line + "'");
        }
        return null;
    }

    /**
     * Read sequence from fasta file. Only one sequence per file, so we just read the first sequence.
     * WARING: Chromosome is created if it doesn't exist
     */
    void readSequence() {
        FastaFileIterator ffi = new FastaFileIterator(fatsaFileName);
        seq = ffi.next().toUpperCase();
        String chrName = ffi.getName();

        // Is the chromosome avaialbe?
        if (genome.getChromosome(chrName) == null) {
            // Create chromosome and add it to genome
            chr = new Chromosome(genome, 0, seq.length(), chrName);
            genome.add(chr);
        } else chr = genome.getChromosome(chrName);

        ffi.close();
    }
}
