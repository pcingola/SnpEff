package org.snpeff;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Get promoter sequences from genes
 *
 * @author pablocingolani
 */
public class PromoterSequences {

	public static String HOME = System.getProperty("user.home");

	public static int LEN_UPSTREAM = 3000;
	public static int LEN_AFTER_TSS = 200;

	SnpEffectPredictor snpEffectPredictor;
	Config config;
	Genome genome;
	HashSet<String> geneIds;
	HashSet<Gene> genes;
	String fastaFile;

	public static void main(String[] args) {

		//---
		//  Parse command line argument
		//---
		if (args.length < 2) {
			System.err.println("Usage: " + PromoterSequences.class.getSimpleName() + " genomeName fastaFile.fa geneId_1 geneId_2 ... geneId_N");
			System.exit(1);
		}

		String genomeName = args[0];
		String fastaFile = args[1];
		ArrayList<String> geneIds = new ArrayList<String>();
		for (int i = 2; i < args.length; i++)
			geneIds.add(args[i]);

		//---
		// Run
		//---
		PromoterSequences promoterSequences = new PromoterSequences(genomeName, fastaFile, geneIds);
		promoterSequences.run();
	}

	public PromoterSequences(String genomeName, String fastaFile, List<String> geneIds) {
		this.fastaFile = fastaFile;

		this.geneIds = new HashSet<String>();
		this.geneIds.addAll(geneIds);

		genes = new HashSet<Gene>();

		Log.info("Loading database");
		String configFile = HOME + "/snpEff/snpEff.config";
		config = new Config(genomeName, configFile);
		config.loadSnpEffectPredictor();
		snpEffectPredictor = config.getSnpEffectPredictor();
		genome = snpEffectPredictor.getGenome();

		//		Log.info("Building forest");
		//		snpEffectPredictor.buildForest();
	}

	public void run() {
		Log.info("Finding genes ");
		for (Gene gene : genome.getGenes()) {
			if (geneIds.contains(gene.getId())) {
				System.err.println("\t" + gene.getId());
				genes.add(gene);
				geneIds.remove(gene.getId());
			}
		}
		if (!geneIds.isEmpty()) Log.info("Not found: " + geneIds);

		//---
		// Read fasta file
		//---
		Log.info("Reading fasta file: " + fastaFile);
		HashSet<Gene> done = new HashSet<Gene>();
		FastaFileIterator ffi = new FastaFileIterator(fastaFile);
		for (String seq : ffi) {
			String chrName = ffi.getName();
			Log.info("Read: " + chrName);

			for (Gene gene : genes) {
				if (gene.getChromosomeName().equals(chrName)) {
					// Extract sequence
					int start = 0, end = 0;

					if (gene.isStrandPlus()) {
						start = gene.getStart() - LEN_UPSTREAM;
						end = gene.getStart() + LEN_AFTER_TSS;
					} else {
						start = gene.getEndClosed() - LEN_AFTER_TSS;
						end = gene.getEndClosed() + LEN_UPSTREAM;
					}

					// Show subsequence
					String subSeq = seq.substring(start, end + 1);
					System.out.println(GprSeq.string2fasta(gene.getId(), subSeq));

					// Remove gene
					done.add(gene);
				}

				// No more genes to find?
				if (done.containsAll(genes)) break;
			}
		}
		ffi.close();

		Log.info("Done");
	}
}
