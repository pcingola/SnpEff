package org.snpeff.genotypes;

import java.io.Serializable;

import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;

/**
 * Simple test program
 * @author pcingola
 */
public class Genotypes implements Serializable {

	private static final long serialVersionUID = 5417498450863908076L;

	public static int MARK = 100;

	String vcfFileName;
	GenotypeVector genotypeVectors[];

	/**
	 * Read from a file
	 * @param fileName
	 */
	public static Genotypes load(String fileName) {
		return (Genotypes) Gpr.readFileSerializedGz(fileName);
	}

	public static void main(String[] args) {
		Genotypes genotypes = new Genotypes();
		genotypes.parse(args);
		genotypes.loadVcf();
		genotypes.save("/tmp/geno.bin");
	}

	public Genotypes() {
	}

	/**
	 * Load data
	 */
	public boolean loadVcf() {
		// Create data structure
		Log.info("Counting lines form file: " + vcfFileName);
		int numLines = Gpr.countLines(vcfFileName);
		Log.info("Done. Number of lines: " + numLines);

		Log.info("Loading file " + vcfFileName);
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		int entryNum = 0;
		for (VcfEntry ve : vcf) {
			if (genotypeVectors == null) {
				long mem = ((long) ve.getVcfGenotypes().size()) * numLines / 4L;
				double memG = mem / (1024.0 * 1024 * 1024);
				Log.info(String.format("Initializing data structures. Expected memory consumption (lower bound): %d bytes (%.2f Gb).", mem, memG));

				genotypeVectors = new GenotypeVector[ve.getVcfGenotypes().size()];
				for (int i = 0; i < genotypeVectors.length; i++)
					genotypeVectors[i] = new GenotypeVector(numLines);

				Log.info("Done.");
				Log.info("Loading: ");
			}

			int sampleNum = 0;
			for (VcfGenotype vg : ve)
				set(entryNum, sampleNum++, vg);

			entryNum++;
			Gpr.showMark(entryNum, MARK);
		}

		System.err.println("");
		Log.info("Done");
		return true;
	}

	/**
	 * Parse command line arguments
	 */
	public void parse(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: " + Genotypes.class.getSimpleName() + " vcfFile");
			System.exit(-1);
		}

		vcfFileName = args[0];
	}

	/**
	 * Save to file
	 * @param fileName
	 */
	public void save(String fileName) {
		Log.info("Saving to file: " + fileName);
		Gpr.toFileSerializeGz(fileName, this);
	}

	/**
	 * Set an entry
	 */
	public void set(int entryNum, int sampleNum, VcfGenotype vg) {
		genotypeVectors[sampleNum].set(entryNum, vg);
	}

}
