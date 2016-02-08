package org.snpeff.fileIterator;

import java.io.IOException;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.GenericMarker;

/**
 * Opens a file and creates generic markers (one per line)
 *     
 * @author pcingola
 */
public class GenericMarkerFileIterator extends MarkerFileIterator<GenericMarker> {

	int colChr = 0;
	int colStart = 1;
	int colEnd = 2;

	public GenericMarkerFileIterator(String fileName, int colChr, int colStart, int colEnd, int inOffset) {
		super(fileName, inOffset);
		this.colChr = colChr;
		this.colStart = colStart;
		this.colEnd = colEnd;
	}

	@Override
	protected GenericMarker readNext() {
		// Try to read a line
		try {
			while (ready()) {
				line = readLine();

				if (line == null) return null; // End of file?

				// Ignore empty lines and comment lines
				if ((line.length() > 0) && (!line.startsWith("#"))) {
					// Parse line
					String fields[] = line.split("\\s");

					// Is line OK?
					if (fields.length >= 2) {
						// Fields 
						String chromosome = fields[colChr].trim();
						Chromosome chromo = getChromosome(chromosome);
						sanityCheckChromo(chromosome, chromo); // Sanity check

						// Start
						int start = 0;
						if (fields.length > colStart) start = parsePosition(fields[colStart]);

						// End
						int end = start;
						if (fields.length > colEnd) end = parsePosition(fields[colEnd]);

						// ID
						String id = "line_" + lineNum;
						if ((fields.length > 3) && (!fields[3].isEmpty())) id = fields[3];

						// Create genericMarker
						GenericMarker genericMarker = new GenericMarker(chromo, start, end, id);
						genericMarker.setLine(line); // Store original line
						return genericMarker;
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
