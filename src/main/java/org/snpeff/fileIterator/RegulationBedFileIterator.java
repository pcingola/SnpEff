package org.snpeff.fileIterator;

import java.io.IOException;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Regulation;
import org.snpeff.util.Gpr;

/**
 * Opens a GFF3 file and create regulatory elements.
 * 
 * Example of regulatory GFF file:
 * 			ftp://ftp.ensembl.org/pub/release-63/regulation/homo_sapiens/AnnotatedFeatures.gff.gz
 * 
 * References (GFF3) : http://www.sequenceontology.org/gff3.shtml
 * 
 * @author pcingola
 */
public class RegulationBedFileIterator extends RegulationFileIterator {

	public static final int BED_OFFSET = 0;

	String name = "";
	String cellType = "";

	public RegulationBedFileIterator(String fileName, Genome genome, String name, String cellType) {
		super(fileName, genome, BED_OFFSET);
		this.name = name;
		this.cellType = cellType;
	}

	public RegulationBedFileIterator(String fileName, String name, String cellType) {
		super(fileName, BED_OFFSET);
		this.name = name;
		this.cellType = cellType;
	}

	@Override
	protected Regulation readNext() {
		// Try to read a line
		try {
			while (ready()) {
				line = readLine();

				if (line == null) return null; // End of file?

				// Ignore empty lines and comment lines
				if ((line.length() > 0) && (!line.startsWith("#"))) {
					// Parse line
					String fields[] = line.split("\t");

					// Is line OK?
					if (fields.length >= 3) {
						// Format: CHR \t START \t END \t ID \t SCORE \t ....
						// Fields 
						String chromosome = fields[0].trim();
						Chromosome chromo = getChromosome(chromosome);
						sanityCheckChromo(chromosome, chromo); // Sanity check

						// Start
						int start = parsePosition(fields[1]);

						// End
						int end = start; // Default 'end is same as start (only if value is missing)
						if (fields.length > 2) end = Gpr.parseIntSafe(fields[2]); // The chromEnd base is not included, but zero-based

						// ID
						String id = "line_" + lineNum;
						if ((fields.length > 3) && (!fields[3].isEmpty())) id = fields[3];

						// Score and all following fields are ignored 

						// Create regulation
						return new Regulation(chromo, start, end, false, id, name, cellType);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
