package ca.mcgill.mcb.pcingola.fileIterator;

import java.io.IOException;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Regulation;

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
public class RegulationGffFileIterator extends RegulationFileIterator {

	public static final int GFF_OFFSET = 1;

	public RegulationGffFileIterator(String fileName) {
		super(fileName, GFF_OFFSET);
	}

	public RegulationGffFileIterator(String fileName, Genome genome) {
		super(fileName, genome, GFF_OFFSET);
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
						// Chromosome
						String chromosome = fields[0].trim();
						Chromosome chromo = getChromosome(chromosome);
						sanityCheckChromo(chromosome, chromo); // Sanity check

						int start = parsePosition(fields[3]);
						int end = parsePosition(fields[4]); // The chromEnd base is not included
						String id = "line_" + lineNum; // ID must be unique

						// Strand
						String strandStr = fields[6];
						boolean strandMinus = strandStr.equals("-");

						// Parse info field, looking for "Name=XXXX"
						String info = fields[8];
						String name = "";
						String cellType = "";
						String infos[] = info.split(";");
						for (String nv : infos) { // Field has "name = value" pairs
							String nameValue[] = nv.split("="); // Get field name
							if (nameValue.length > 0) {
								String attr = nameValue[0].trim();
								String val = nameValue[1].trim();

								// Is name 'Name'? 
								if (attr.equals("Name")) name = val;
								else if (attr.equals("Cell_type")) {
									cellType = val; // Cell type 
								} else if (attr.equals("Alias") && cellType.isEmpty()) {
									cellType = val.split("_")[0]; // Cell type is in 'Alias'
								}
							}
						}

						// Create seqChange
						Regulation reg = new Regulation(chromo, start, end, strandMinus, id, name, cellType);
						return reg;
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
