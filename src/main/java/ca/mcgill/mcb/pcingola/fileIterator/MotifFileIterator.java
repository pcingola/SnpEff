package ca.mcgill.mcb.pcingola.fileIterator;

import java.io.IOException;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Motif;
import ca.mcgill.mcb.pcingola.motif.Jaspar;

/**
 * Opens a regulation file and create Motif elements.
 * 
 * @author pcingola
 */
public class MotifFileIterator extends MarkerFileIterator<Motif> {

	public static final int GFF_OFFSET = 1;
	Jaspar jaspar;

	public MotifFileIterator(String fileName, Genome genome, Jaspar jaspar) {
		super(fileName, genome, GFF_OFFSET);
		this.jaspar = jaspar;
	}

	@Override
	protected Motif readNext() {
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
						int strand = strandStr.equals("-") ? -1 : +1;

						// Parse info field, looking for "Name=XXXX"
						String info = fields[8];
						String name = "", pwmId = "";
						String infos[] = info.split(";");
						for (String nv : infos) { // Field has "name = value" pairs
							String nameValue[] = nv.split("="); // Get field name
							if (nameValue.length > 0) {
								String attr = nameValue[0].trim();
								String val = nameValue[1].trim();

								// Is name 'Name'? 
								if (attr.equals("Name")) {
									name = val;
									String names[] = name.split(":");
									name = names[0];
									pwmId = names[names.length - 1];
								}
							}
						}

						// Create seqChange
						Motif motif = new Motif(chromo, start, end, strand, id, name, pwmId);
						motif.setPwm(jaspar.getPwm(pwmId));
						if (motif.getPwm() == null) System.err.println("Warning: Pwm '" + id + "' not found! Name = " + name);

						return motif;
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
