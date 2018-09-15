package org.snpeff.fileIterator;

import java.io.IOException;

import org.snpeff.interval.Genome;
import org.snpeff.interval.GffMarker;

/**
 * Opens a sequence change file and iterates over all intervals in GFF3 format.
 *
 * @author pcingola
 */
public class Gff3FileIterator extends MarkerFileIterator<GffMarker> {

	public Gff3FileIterator(String fileName) {
		super(fileName, 0);
	}

	public Gff3FileIterator(String fileName, Genome genome) {
		super(fileName, genome, 0);
	}

	public Gff3FileIterator(String fileName, Genome genome, int inOffset) {
		super(fileName, genome, inOffset);
	}

	/**
	 * Parse a line and create a appropriate GFF marker
	 */
	GffMarker parse(String line) {
		// Ignore empty lines and comment lines
		if (!GffMarker.canParseLine(line)) return null;

		// Parse fields
		GffMarker gffMarker = new GffMarker(genome, line);
		return gffMarker;
	}

	@Override
	protected GffMarker readNext() {
		// Try to read a line
		try {
			while (ready()) {
				line = readLine();
				if (line == null) return null; // End of file?

				// Parse line
				GffMarker gffmarker = parse(line);
				if (gffmarker != null) return gffmarker;

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
