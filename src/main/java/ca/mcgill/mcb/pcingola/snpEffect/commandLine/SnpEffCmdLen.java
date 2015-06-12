package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.SpliceSiteAcceptor;
import ca.mcgill.mcb.pcingola.interval.SpliceSiteDonor;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.stats.ReadsOnMarkersModel;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;

/**
 * Calculate the maximum interval length by type, for all markers in a genome
 *
 *
 * @author pcingola
 */
public class SnpEffCmdLen extends SnpEff {

	int readLength, numIterations, numReads;
	SnpEffectPredictor snpEffectPredictor;
	ReadsOnMarkersModel readsOnMarkersModel;

	public SnpEffCmdLen() {
		super();
	}

	/**
	 * Calculate effective length for all genes
	 */
	void effectiveCodingLength() {
		if (verbose) Timer.showStdErr("Calclating gene effective coding lengths");

		System.out.println("gene.name\tgene.id\tchr\tstart\tend\teffective.length\tmax.cds.length");
		for (Chromosome chr : snpEffectPredictor.getGenome()) {
			if (verbose) Timer.showStdErr("Effective coding lengths for chromosome " + chr.getId());

			for (Gene gene : snpEffectPredictor.getGenome().getGenes()) {
				if (gene.getChromosomeName().equals(chr.getId()) && gene.isProteinCoding()) {
					int efflen = effectiveCodingLength(gene);
					int maxcds = maxcds(gene);

					System.out.println(gene.getGeneName() //
							+ "\t" + gene.getId() //
							+ "\t" + gene.getChromosomeName() //
							+ "\t" + (gene.getStart() + 1) //
							+ "\t" + (gene.getEnd() + 1) //
							+ "\t" + efflen //
							+ "\t" + maxcds //
					);

					// Sanity check
					if (maxcds > efflen) throw new RuntimeException("CDS length is greter then effective length. This should never happen!");
				}
			}
		}
	}

	/**
	 * Calculate effective length for a gene
	 */
	int effectiveCodingLength(Gene gene) {
		// Initialize
		byte coding[] = new byte[gene.size()];
		for (int i = 0; i < coding.length; i++)
			coding[i] = 0;

		// Mark all 'used' bases
		for (Transcript tr : gene) {
			// Ignore non-protein coding
			if (tr.isProteinCoding()) {
				for (Exon ex : tr) {
					// Mark all bases in exon as 'used'
					for (int i = ex.getStart(); i <= ex.getEnd(); i++)
						coding[i - gene.getStart()] = 1;

					// Mark bases in SpliceSiteAcceptor/Donnor as 'used'
					for (SpliceSite ss : ex.getSpliceSites()) {
						if (ss instanceof SpliceSiteAcceptor || ss instanceof SpliceSiteDonor) {
							for (int i = ss.getStart(); i <= ss.getEnd(); i++)
								coding[i - gene.getStart()] = 1;
						}
					}

				}

				// Mark bases in SpliceSites as 'used'
				for (Intron intr : tr.introns()) {
					for (SpliceSite ss : intr.getSpliceSites()) {
						if (ss instanceof SpliceSiteAcceptor || ss instanceof SpliceSiteDonor) {
							for (int i = ss.getStart(); i <= ss.getEnd(); i++)
								coding[i - gene.getStart()] = 1;
						}
					}
				}
			}
		}

		// Count all used bases
		int efflen = 0;
		for (int i = 0; i < coding.length; i++)
			if (coding[i] > 0) efflen++;

		return efflen;
	}

	int maxcds(Gene gene) {
		int max = 0;
		for (Transcript tr : gene)
			if (tr.isProteinCoding()) max = Math.max(max, tr.cds().length());

		return max;

	}

	@Override
	public void parseArgs(String[] args) {
		this.args = args;
		for (int i = 0; i < args.length; i++) {

			// Argument starts with '-'?
			if (args[i].equals("-r")) {
				if ((i + 1) < args.length) readLength = Gpr.parseIntSafe(args[++i]);
				else usage("Missing value for parameter '-r'");
			} else if (args[i].equals("-iter")) {
				if ((i + 1) < args.length) numIterations = Gpr.parseIntSafe(args[++i]);
				else usage("Missing value for parameter '-iter'");
			} else if (args[i].equals("-reads")) {
				if ((i + 1) < args.length) numReads = Gpr.parseIntSafe(args[++i]);
				else usage("Missing value for parameter '-reads'");
			} else if (args[i].equals("-r")) {
				if ((i + 1) < args.length) readLength = Gpr.parseIntSafe(args[++i]);
				else usage("Missing value for parameter '-r'");

			} else if (genomeVer.isEmpty()) genomeVer = args[i];
			else usage("Unknown parameter '" + args[i] + "'");
		}

		// Check: Do we have all required parameters?
		if (genomeVer.isEmpty()) usage("Missing genomer_version parameter");
		if (readLength < 0) usage("Read length should be a non-negative number");
	}

	/**
	 * Run
	 * @return
	 */
	@Override
	public boolean run() {
		// Initialize
		loadConfig();
		loadDb();

		if (verbose) Timer.showStdErr("Building interval forest");
		snpEffectPredictor.buildForest();

		//---
		// Count lengths
		//---
		effectiveCodingLength();

		readsOnMarkersModel = new ReadsOnMarkersModel(snpEffectPredictor);
		readsOnMarkersModel.setVerbose(verbose);

		if (verbose) Timer.showStdErr("Counting bases");
		readsOnMarkersModel.run(); // Count
		if (!quiet) System.out.println(readsOnMarkersModel);

		// Perform some random sampling
		if ((numIterations > 0) && (readLength > 0)) readsOnMarkersModel.randomSampling(numIterations, readLength, numReads);

		return true;
	}

	/**
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff len [options] genome_version");
		System.err.println("Options:");
		System.err.println("\t-r     <num> : Assume a read size of 'num' bases.");
		System.err.println("\t-iter  <num> : Perform 'num' iterations of random sampling.");
		System.err.println("\t-reads <num> : Each random sampling iteration has 'num' reads.");
		System.exit(-1);
	}

}
