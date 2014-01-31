package ca.mcgill.mcb.pcingola.fileIterator;

import java.io.BufferedReader;
import java.io.IOException;

import net.sf.samtools.tabix.TabixReader;
import net.sf.samtools.tabix.TabixReader.TabixIterator;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Opens a Marker file and iterates over all markers
 * 
 * @author pcingola
 */
public abstract class MarkerFileIterator<M extends Marker> extends FileIterator<M> {

	protected boolean createChromos = false; // Create chromosomes if not found
	protected Genome genome;
	protected boolean ignoreChromosomeErrors = true; // If true, do not throw an exception when a chromosome is not found. Just ignore the line
	protected int inOffset;
	protected TabixReader tabixReader;
	protected TabixIterator tabixIterator;

	public MarkerFileIterator(BufferedReader reader, int inOffset) {
		super(reader);
		this.inOffset = inOffset;
		this.genome = new Genome("genome");
		this.createChromos = true;
	}

	public MarkerFileIterator(String fileName, Genome genome, int inOffset) {
		super(fileName);
		this.inOffset = inOffset;
		this.genome = (genome != null ? genome : new Genome("genome"));
		initTabix(fileName);
	}

	public MarkerFileIterator(String fileName, int inOffset) {
		super(fileName);
		this.inOffset = inOffset;
		this.genome = new Genome("genome");
		this.createChromos = true;
		initTabix(fileName);
	}

	/**
	 * Find chromosome 'chromoName'. If it does not exists and 'createChromos' is true, the chromosome is created
	 * @param chromoName
	 * @return
	 */
	public Chromosome getChromosome(String chromoName) {
		if (createChromos) return genome.getOrCreateChromosome(chromoName);
		return genome.getChromosome(chromoName);
	}

	public Genome getGenome() {
		return genome;
	}

	@Override
	public boolean hasNext() {
		if (tabixReader == null) return super.hasNext();

		if (tabixIterator == null) return false;

		if (next == null) {
			next = readNext(); // Try reading next item.
			if ((next == null) && autoClose) close(); // End of file or any problem? => Close file
		}

		return (next != null);
	}

	protected void initTabix(String fileName) {
		try {
			// Do we have a tabix file?
			if (!Gpr.exists(fileName + ".tbi")) return; // No index, cannot open in 'tabix' mode

			// Open tabix reader
			tabixReader = new TabixReader(fileName);
			tabixIterator = tabixReader.iterator();

			// We wo'nt be using the reader
			reader.close();
			reader = null;
		} catch (IOException e) {
			throw new RuntimeException("Error opening tabix file '" + fileName + "'", e);
		}
	}

	public boolean isIgnoreChromosomeErrors() {
		return ignoreChromosomeErrors;
	}

	public Markers loadMarkers() {
		Markers list = new Markers();
		for (M t : this)
			list.add(t);
		close();
		return list;
	}

	/**
	 * Parse a string as a 'position'.
	 * Note: It subtracts 'inOffset' so that all coordinates are zero-based
	 * 
	 * @param posStr
	 * @return
	 */
	public int parsePosition(String posStr) {
		return Gpr.parseIntSafe(posStr) - inOffset;
	}

	@Override
	protected String readLine() throws IOException {
		if (tabixReader == null) return super.readLine(); // No tabix => Do 'normal' readline()

		// Use tabix
		if (nextLine != null) {
			String nl = nextLine;
			nextLine = null;
			return nl;
		}

		if (tabixIterator != null) nextLine = tabixIterator.next(); // Tabix? => Use tabix iterator

		// Remove trailing '\r'
		if ((nextLine != null) && (nextLine.length() > 0) && nextLine.charAt(nextLine.length() - 1) == '\r') nextLine = nextLine.substring(0, nextLine.length() - 1);

		if (nextLine != null) lineNum++;
		return nextLine;
	}

	@Override
	protected boolean ready() throws IOException {
		if ((reader == null) && (tabixReader == null)) return false; // No reader? then we are not ready
		if (tabixReader == null) return super.ready();

		if (nextLine != null) return true; // Next line is null? then we have to try to read a line (to see if one is available)
		readLine();
		return nextLine != null; // Line was read from the file? Then we are ready.
	}

	/**
	 * Sanity check
	 */
	public void sanityCheckChromo(String chromoName, Chromosome chromo) {
		if (chromo == null) {
			if (ignoreChromosomeErrors) {
				System.err.println("WARNING: Chromosome '" + chromoName + "' not found. File '" + fileName + "', line " + lineNum);
				return;
			}
			throw new RuntimeException("ERROR: Chromosome '" + chromoName + "' not found! File '" + fileName + "', line " + lineNum);
		}
	}

	public void seek(String chr) {
		tabixReader.query(chr + ":1");
		tabixIterator = tabixReader.iterator();
	}

	/**
	 * Seek to a chr:pos region
	 * @param chr
	 * @param pos
	 */
	public void seek(String chr, int pos) {
		tabixReader.query(chr + ":" + pos);
		tabixIterator = tabixReader.iterator();
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
