package org.snpeff.fileIterator;

import java.io.IOException;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Motif;
import org.snpeff.motif.Jaspar;
import org.snpeff.util.Log;

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
						boolean strandMinus = strandStr.equals("-");

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
								} else if (attr.equals("binding_matrix")) {
									pwmId = val;
								} else if (attr.equals("motif_feature_type")) {
									name = val;
								}
							}
						}

						// Create marker
						if (name.isEmpty()) {
							if (verbose) Log.info("Warning: Name not found, line " + lineNum + "\t" + line);
						} else if (pwmId.isEmpty()) {
							if (verbose) Log.info("Warning: PWM ID not found, line " + lineNum + "\t" + line);
						} else if (jaspar.getPwm(pwmId) == null) {
							if (verbose) Log.info("Warning: PWM '" + pwmId + "' not found, line " + lineNum + "\t" + line);
						} else {
							Motif motif = new Motif(chromo, start, end, strandMinus, id, name, pwmId);
							motif.setPwm(jaspar.getPwm(pwmId));
							return motif;
						}
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
