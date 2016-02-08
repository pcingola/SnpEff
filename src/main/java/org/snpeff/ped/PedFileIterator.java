package org.snpeff.ped;

import java.io.IOException;

import org.snpeff.fileIterator.FileIterator;

/**
 * PED file iterator (PED file from PLINK)
 * 
 * Reference: http://pngu.mgh.harvard.edu/~purcell/plink/data.shtml
 * 
 * @author pcingola
 */
public class PedFileIterator extends FileIterator<PedEntry> {

	PlinkMap plinkMap;

	public PedFileIterator(String fileName, String mapFileName) {
		super(fileName);
		plinkMap = new PlinkMap();
		plinkMap.read(mapFileName);
	}

	public PlinkMap getPlinkMap() {
		return plinkMap;
	}

	/**
	 * Parse one line
	 * @param line
	 * @return
	 */
	PedEntry parseLine(String line) {
		return new PedEntry(plinkMap, line);
	}

	@Override
	protected PedEntry readNext() {
		try {
			if (reader.ready()) {
				line = reader.readLine(); // Read a line (only if needed)
				if (line != null) {
					lineNum++;
					try {
						PedEntry pedEntry = parseLine(line);
						if (pedEntry != null) return pedEntry;
					} catch (Throwable t) {
						throw new RuntimeException("Error parsing line from PED/TFAM file:\n\tFile name : '" + fileName + "'\n\tLine number : " + lineNum + "\n\tLine: '" + line + "'", t);
					};

				}
			}
		} catch (IOException e) {
			return null;
		}

		return null;
	}
}
