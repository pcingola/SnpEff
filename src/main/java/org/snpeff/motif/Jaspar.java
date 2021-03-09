package org.snpeff.motif;

import java.util.HashMap;
import java.util.Iterator;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Load PWM matrices from a Jaspar file
 *
 * @author pablocingolani
 */
public class Jaspar implements Iterable<Pwm> {

	boolean verbose = false;
	HashMap<String, Pwm> pwms;

	public Jaspar() {
	}

	public Pwm getPwm(String pwmId) {
		return pwms.get(pwmId);
	}

	@Override
	public Iterator<Pwm> iterator() {
		return pwms.values().iterator();
	}

	/**
	 * Load matrix file
	 * @param jasparMatrixFile
	 */
	public void load(String jasparMatrixFile) {
		pwms = new HashMap<String, Pwm>();

		if (verbose) Log.info("Loading jaspar matrix from file " + jasparMatrixFile);

		String lines[] = Gpr.readFile(jasparMatrixFile, true).split("\n");

		for (int i = 0; i < lines.length;) {
			String name = lines[i++];
			String id = "";

			if (name.startsWith(">")) {
				String nameSplit[] = name.substring(1).split("\\s");
				id = nameSplit[0];
				name = nameSplit[1];
			} else throw new RuntimeException("Error: Name line does not start with '>' : " + name);

			int weightsA[] = parseWeights(lines[i++], "A");
			int weightsC[] = parseWeights(lines[i++], "C");
			int weightsG[] = parseWeights(lines[i++], "G");
			int weightsT[] = parseWeights(lines[i++], "T");

			// Sanity check
			if (weightsA.length != weightsC.length) throw new RuntimeException("Weight lengths differ: " + weightsA.length + ", " + weightsC.length + ", " + weightsG.length + ", " + weightsT.length);

			// Create PWM
			Pwm pwm = new Pwm(weightsA.length);
			pwm.setName(name);
			pwm.setId(id);
			pwm.setCounts('A', weightsA);
			pwm.setCounts('C', weightsC);
			pwm.setCounts('G', weightsG);
			pwm.setCounts('T', weightsT);
			pwm.calcLogOddsWeight();

			// Add to pwms hash
			pwms.put(id, pwm);
		}
	}

	/**
	 * Parse a line into weights
	 * @param line
	 * @param lineType
	 * @return
	 */
	int[] parseWeights(String line, String lineType) {
		// Sanity check
		if (line.startsWith(lineType)) line = line.substring(lineType.length());

		// line = line.replace('[', ' ').replace(']', ' ');
		line = line.replace('[', ' ').replace(']', ' ').trim();

		// Split weights
		String weightStr[] = line.split("\\s+");
		// Populate weight array
		int weight[] = new int[weightStr.length];
		for (int i = 0; i < weight.length; i++)
			weight[i] = Gpr.parseIntSafe(weightStr[i]);

		return weight;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
