package ca.mcgill.mcb.pcingola.fileIterator;

import java.io.IOException;
import java.util.Iterator;

import org.broad.igv.bbfile.BBFileHeader;
import org.broad.igv.bbfile.BBFileReader;
import org.broad.igv.bbfile.BedFeature;

import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * FileIterator for BigBed features
 * 
 * Note: I use Broad's IGV code to do all the work, this is just a wrapper
 * 
 * @author pablocingolani
 */
public class BigBedFileIterator extends SeqChangeFileIterator {

	BBFileReader readerBb;
	BBFileHeader bbFileHdr;
	Iterator<BedFeature> iterator;
	String label;

	public BigBedFileIterator(String fileName) {
		super(null, 0);
		open(fileName);
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public SeqChange next() {
		return readNext();
	}

	/**
	 * Open file
	 * @param fileName
	 */
	void open(String fileName) {
		try {
			label = Gpr.removeExt(Gpr.baseName(fileName));

			// Open file
			BBFileReader readerBb = new BBFileReader(fileName);
			BBFileHeader bbFileHdr = readerBb.getBBFileHeader(); // Get header
			if (!bbFileHdr.isHeaderOK()) throw new IOException("Bad header for file '" + fileName + "'"); // Sanity check
			if (!bbFileHdr.isBigBed()) throw new RuntimeException("Unrecognized header type for file '" + fileName + "' (expecting BigBed)"); // Get file type

			iterator = readerBb.getBigBedIterator();
		} catch (Exception e) {
			throw new RuntimeException("Error loading file '" + fileName + "'.", e);
		}
	}

	@Override
	protected SeqChange readNext() {
		// Get next item
		BedFeature f = iterator.next();
		if (f == null) return null;

		// Create an ID
		String id = label + ":" + (f.getStartBase() + 1) + "_" + f.getEndBase(); // Show as one-based coordinates

		// Get score
		String restOfFields[] = f.getRestOfFields();
		String score = restOfFields[1];

		// Create seqChange
		SeqChange seqChange = new SeqChange(getChromosome(f.getChromosome()), f.getStartBase(), f.getEndBase() - 1, id);
		seqChange.setScore(Gpr.parseDoubleSafe(score));

		//		Gpr.debug(seqChange);

		return seqChange;
	}

}
