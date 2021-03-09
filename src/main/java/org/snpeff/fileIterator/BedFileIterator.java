package org.snpeff.fileIterator;

import java.io.IOException;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Markers;
import org.snpeff.interval.Variant;
import org.snpeff.interval.VariantWithScore;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Opens a sequence change file and iterates over all intervals in BED format.
 * Reference: http://genome.ucsc.edu/FAQ/FAQformat.html#format1
 *
 * BED lines have three required fields and nine additional optional fields.
 * The number of fields per line must be consistent throughout any single set of data in an annotation track.
 *
 * The first three required BED fields are:
 * 		1. chrom - The name of the chromosome (e.g. chr3, chrY, chr2_random) or scaffold (e.g. scaffold10671).
 * 		2. chromStart - The starting position of the feature in the chromosome or scaffold. The first base in a chromosome is numbered 0.
 * 		3. chromEnd - The ending position of the feature in the chromosome or scaffold. The chromEnd base is not included in the display of the feature. For example, the first 100 bases of a chromosome are defined as chromStart=0, chromEnd=100, and span the bases numbered 0-99.
 *
 * There are 9 additional optional BED fields, but we only use one:
 * 		4. name - Defines the name of the BED line. This label is displayed to the left of the BED line in the Genome Browser window when the track is open to full display mode or directly to the left of the item in pack mode.
 * 		5. score - A score used for that interval
 *
 * @author pcingola
 */
public class BedFileIterator extends VariantFileIterator {

	public static Markers load(String bedFileName, boolean verbose) {
		if (verbose) Log.info("Reading intervals from BED file '" + bedFileName + "'");

		BedFileIterator bed = new BedFileIterator(bedFileName);
		Markers markers = new Markers();

		for (Variant var : bed)
			markers.add(var);

		// Check that file is not empty
		if (markers.isEmpty()) throw new RuntimeException("No intervals found in BED file " + bedFileName);

		return markers;
	}

	public BedFileIterator(String fileName) {
		super(fileName);
		inOffset = 0;
	}

	public BedFileIterator(String fileName, Genome genome) {
		super(fileName, genome);
		inOffset = 0;
	}

	@Override
	protected Variant readNext() {
		// Try to read a line
		try {
			while (ready()) {
				line = readLine();

				if (line == null) return null; // End of file?

				// Ignore empty lines and comment lines
				if ((line.length() > 0) && (!line.startsWith("#"))) {
					// Parse line
					String fields[] = line.split("\\t");

					// Is line OK?
					if (fields.length >= 2) {
						// Format: CHR \t START \t END \t ID \t SCORE \t ....
						// Fields
						String chromosome = fields[0].trim();
						Chromosome chromo = getChromosome(chromosome);
						sanityCheckChromo(chromosome, chromo); // Sanity check

						// Start
						int start = parsePosition(fields[1]);

						// End
						int end = start;
						if (fields.length > 2) end = parsePosition(fields[2]) - 1; // The chromEnd base is not included
						if (end < start) end = start;

						// ID
						String id = "line_" + lineNum;
						if ((fields.length > 3) && (!fields[3].isEmpty())) id = fields[3];

						// Score
						double score = 0;
						if (fields.length > 4) score = Gpr.parseDoubleSafe(fields[4]);

						// Create variant
						Variant variant = new VariantWithScore(chromo, start, end, id, score);
						variant.setChromosomeNameOri(chromosome);
						return variant;
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
