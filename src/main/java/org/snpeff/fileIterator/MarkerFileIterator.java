package org.snpeff.fileIterator;

import java.io.BufferedReader;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Opens a Marker file and iterates over all markers
 *
 * @author pcingola
 */
public abstract class MarkerFileIterator<M extends Marker> extends FileIterator<M> {

	protected boolean createChromos = true; // Create chromosomes if not found
	protected Genome genome;
	protected boolean ignoreChromosomeErrors = true; // If true, do not throw an exception when a chromosome is not found. Just ignore the line
	protected int inOffset;

	public MarkerFileIterator(BufferedReader reader, int inOffset) {
		super(reader);
		this.inOffset = inOffset;
		this.genome = new Genome("genome");
	}

	public MarkerFileIterator(String fileName, Genome genome, int inOffset) {
		super(fileName);
		this.inOffset = inOffset;
		this.genome = (genome != null ? genome : new Genome("genome"));
	}

	public MarkerFileIterator(String fileName, int inOffset) {
		super(fileName);
		this.inOffset = inOffset;
		this.genome = new Genome("genome");
	}

	/**
	 * Find chromosome 'chromoName'. If it does not exists and 'createChromos' is true, the chromosome is created
	 */
	public Chromosome getChromosome(String chromoName) {
		if (createChromos) return genome.getOrCreateChromosome(chromoName);
		return genome.getChromosome(chromoName);
	}

	public Genome getGenome() {
		return genome;
	}

	/**
	 * Initialize
	 * @param fileName : Can be null (no file is opened)
	 */
	@Override
	protected void init(String fileName, int inOffset) {
		line = null;
		lineNum = 0;
		next = null;
		this.fileName = fileName;
		if (fileName != null) reader = Gpr.reader(fileName);
	}

	public boolean isIgnoreChromosomeErrors() {
		return ignoreChromosomeErrors;
	}

	public Markers loadMarkers() {
		Markers list = new Markers();
		list.setName(fileName);
		for (M t : this)
			list.add(t);
		close();
		return list;
	}

	/**
	 * Parse a string as a 'position'.
	 * Note: It subtracts 'inOffset' so that all coordinates are zero-based
	 */
	public int parsePosition(String posStr) {
		return Gpr.parseIntSafe(posStr) - inOffset;
	}

	/**
	 * Sanity check
	 */
	public void sanityCheckChromo(String chromoName, Chromosome chromo) {
		if (chromo == null) {
			if (ignoreChromosomeErrors) {
				if (verbose) Log.info("WARNING: Chromosome '" + chromoName + "' not found. File '" + fileName + "', line " + lineNum);
				return;
			}
			throw new RuntimeException("ERROR: Chromosome '" + chromoName + "' not found! File '" + fileName + "', line " + lineNum);
		}
	}

	public void setCreateChromos(boolean createChromos) {
		this.createChromos = createChromos;
	}

	public void setIgnoreChromosomeErrors(boolean ignoreChromosomeErrors) {
		this.ignoreChromosomeErrors = ignoreChromosomeErrors;
	}

	public void setInOffset(int inOffset) {
		this.inOffset = inOffset;
	}

}
