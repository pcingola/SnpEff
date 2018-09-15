package org.snpeff.fileIterator;

import java.io.IOException;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Regulation;

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

						// Strand
						String strandStr = fields[6];
						boolean strandMinus = strandStr.equals("-");

						// Parse info field, looking for "Name=XXXX"
						String info = fields[8];
						String name = "";
						String type = "";
						String infos[] = info.split(";");
						for (String nv : infos) { // Field has "name = value" pairs
							String nameValue[] = nv.split("="); // Get field name
							if (nameValue.length > 0) {
								String attr = nameValue[0].trim();
								String val = nameValue[1].trim();

								// Is name 'Name'?
								if (attr.equals("Name") || attr.equals("feature_type")) {
									name = val;
								} else if (attr.equals("Cell_type") || attr.equals("description")) {
									type = val.replaceAll(" - ", "-"); // Cell type or tissue type
								} else if (attr.equals("Alias") && type.isEmpty()) {
									type = val.split("_")[0]; // Cell type is in 'Alias'
								}
							}
						}

						// Create unique ID
						String id = type + "_" + name + "_" + lineNum;

						// Create regulation
						Regulation reg = new Regulation(chromo, start, end, strandMinus, id, name, type);
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
