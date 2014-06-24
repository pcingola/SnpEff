package ca.mcgill.mcb.pcingola.fileIterator;

import java.io.IOException;
import java.util.LinkedList;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.GffMarker;
import ca.mcgill.mcb.pcingola.util.KeyValue;

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
	 * @param line
	 * @return
	 */
	GffMarker parse(String line) {
		// Ignore empty lines and comment lines
		if (line.isEmpty()) return null;
		if (line.startsWith("#")) return null;

		String fields[] = line.split("\t");
		if (fields.length < 9) return null;

		// Parse fields
		String chromo = fields[0];
		String source = fields[1];
		String type = fields[2];
		int start = parsePosition(fields[3]);
		int end = parsePosition(fields[4]);

		// Parse strand
		boolean strandMinus = false;
		if (fields[6].equals("+")) strandMinus = false;
		else if (fields[6].equals("-")) strandMinus = true;

		// Parse attributes
		String attrStr = fields[8];

		String id = null;
		LinkedList<KeyValue<String, String>> kvs = new LinkedList<KeyValue<String, String>>();
		if (attrStr.length() > 0) {
			String attrs[] = attrStr.split(";");
			for (int i = 0; i < attrs.length; i++) {
				// Split key value pair
				String kv[] = attrs[i].split("=");
				if (kv.length > 1) {
					String key = kv[0].trim();
					String value = kv[1].trim();

					if (key.equalsIgnoreCase("ID")) id = value;
					else if ((id == null) && key.equalsIgnoreCase("Name")) id = value;
					else kvs.add(new KeyValue<String, String>(key, value));
				}
			}
		}

		// ID
		if (id == null) id = type + "_" + chromo + "_" + (start + 1) + "_" + (end + 1); // No ID => create one
		id = id.trim(); // Sometimes names or IDs may have spaces, we have to get rid of them

		// Create marker
		Chromosome chromosome = getChromosome(chromo);
		GffMarker gffMarker = new GffMarker(chromosome, start, end, strandMinus, id);

		// Add all key value pairs
		if ((!type.isEmpty()) && (!type.equals("."))) gffMarker.add("type", type);
		if ((!source.isEmpty()) && (!source.equals("."))) gffMarker.add("source", source);
		for (KeyValue<String, String> kv : kvs)
			gffMarker.add(kv);

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
				GffMarker seqChange = parse(line);
				if (seqChange != null) return seqChange;

			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}

}
