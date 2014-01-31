package ca.mcgill.mcb.pcingola.genotypes;

import java.io.Serializable;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;

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
	 * @return
	 */
	public boolean loadVcf() {
		//---
		// Create data structure
		//---
		Timer.showStdErr("Counting lines form file: " + vcfFileName);
		int numLines = Gpr.countLines(vcfFileName);
		Timer.showStdErr("Done. Number of lines: " + numLines);

		Timer.showStdErr("Loading file " + vcfFileName);
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		int entryNum = 0;
		for (VcfEntry ve : vcf) {
			if (genotypeVectors == null) {
				long mem = ((long) ve.getVcfGenotypes().size()) * numLines / 4L;
				double memG = mem / (1024.0 * 1024 * 1024);
				Timer.showStdErr(String.format("Initializing data structures. Expected memory consumption (lower bound): %d bytes (%.2f Gb).", mem, memG));

				genotypeVectors = new GenotypeVector[ve.getVcfGenotypes().size()];
				for (int i = 0; i < genotypeVectors.length; i++)
					genotypeVectors[i] = new GenotypeVector(numLines);

				Timer.showStdErr("Done.");
				Timer.showStdErr("Loading: ");
			}

			//System.out.print(ve.getChromosomeName() + ":" + ve.getStart());
			int sampleNum = 0;
			for (VcfGenotype vg : ve) {
				set(entryNum, sampleNum++, vg);
				//System.out.print(String.format("%2d", code));
			}
			//System.out.println("");

			entryNum++;
			Gpr.showMark(entryNum, MARK);
		}

		System.err.println("");
		Timer.showStdErr("Done");
		return true;
	}

	/**
	 * Parse command line arguments
	 * @param args
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
		Timer.showStdErr("Saving to file: " + fileName);
		Gpr.toFileSerializeGz(fileName, this);
	}

	/**
	 * Set an entry
	 * 
	 * @param entryNum
	 * @param sampleNum
	 * @param vg
	 */
	public void set(int entryNum, int sampleNum, VcfGenotype vg) {
		genotypeVectors[sampleNum].set(entryNum, vg);
	}

}
