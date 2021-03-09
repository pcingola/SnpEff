package org.snpeff.fileIterator;

import java.io.IOException;
import java.util.LinkedList;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Variant;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Opens a sequence change file and iterates over all sequence changes
 *
 * TXT Format: Tab-separated format, containing five columns that correspond to:
 *			chr \t position \t refSeq \t newSeq \t strand \t quality \t coverage \t id \n
 * Fields strand, quality, coverage and id are optional
 *
 * E.g.
 * 		5   140532    T		C   +	31
 * 		12  1017956   T		A   +
 * 		2   946507    G		C   +	21	16	Very_important_Snp
 * 		14  19584687  C		T   -
 * 		19  66520     G		A   +	27
 * 		8   150029    A		T   +	31
 *
 * @author pcingola
 */
public class VariantTxtFileIterator extends VariantFileIterator {

	LinkedList<Variant> fifo = new LinkedList<Variant>();

	public VariantTxtFileIterator(String fileName) {
		super(fileName);
	}

	public VariantTxtFileIterator(String fileName, Genome genome) {
		super(fileName, genome);
	}

	@Override
	public boolean hasNext() {
		if (!fifo.isEmpty()) return true;
		return super.hasNext();
	}

	@Override
	public Variant next() {
		if (!fifo.isEmpty()) return fifo.removeFirst();
		return super.next();
	}

	@Override
	protected Variant readNext() {
		try {
			while (ready()) {
				// Try to read a line
				String line = readLine();

				if (line == null) return null; // End of file?

				// Parse SNP
				line = line.trim(); // Remove spaces

				// Ignore empty lines and comment lines
				if ((line.length() > 0) && (!line.startsWith("#"))) {
					// Parse line
					String fields[] = line.split("\t");

					// Is line OK?
					if (fields.length >= 4) {
						String chromosome = fields[0].trim();
						Chromosome chromo = getChromosome(chromosome);
						sanityCheckChromo(chromosome, chromo); // Sanity check

						int start = parsePosition(fields[1]);
						String reference = fields[2];
						String change = fields[3];

						// Strand field is optional. Default is plus strand
						int strand = 1;
						if (fields.length >= 5) {
							String strandStr = fields[4];
							if (strandStr.charAt(0) == '+') strand = 1;
							else if (strandStr.charAt(0) == '-') strand = -1;
						}

						if (strand < 0) {
							reference = GprSeq.reverseWc(reference);
							change = GprSeq.reverseWc(change);
						}

						String id = "";
						if (fields.length >= 8) id = fields[7];

						fifo = (LinkedList<Variant>) Variant.factory(chromo, start, reference, change, id, true);
						return fifo.removeFirst();
					} else throw new RuntimeException("Error reading file '" + fileName + "' line " + lineNum + " (number of fields is " + fields.length + "):\t" + line);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	/**
	 * Used for debugging
	 */
	void showFifo() {
		Log.debug("Fifo:" + fifo.size());
		for (Variant v : fifo)
			System.err.println("\t\t" + v);
	}
}
