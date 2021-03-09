package org.snpeff.spliceSites;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.snpeff.collections.AutoHashMap;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Log;

/**
 * A set of transcripts
 *
 * @author pablocingolani
 */
public class TranscriptSet implements Iterable<Transcript> {

	public static final int MAX_TRANSCRIPTS_PER_GENE = 1000;

	boolean debug;
	boolean verbose;

	Genome genome;

	HashSet<Transcript> transcripts;

	AutoHashMap<String, ArrayList<Transcript>> transcriptsByChromo;

	public TranscriptSet(Genome genome) {
		this.genome = genome;
		transcripts = new HashSet<Transcript>();
		transcriptsByChromo = new AutoHashMap<String, ArrayList<Transcript>>(new ArrayList<Transcript>());
	}

	/**
	 * Filter out "bad transcripts"
	 */
	public void filter() {
		if (verbose) Log.info("Filtering transcripts: Removing non-coding and transcripts with errors");

		int total = 0, kept = 0;
		for (Gene gene : genome.getGenes()) {
			if (gene.numChilds() > MAX_TRANSCRIPTS_PER_GENE) {
				System.err.println("Ignoring gene '" + gene.getGeneName() + "', too many transcripts (" + gene.numChilds() + ")");
				continue;
			}

			for (Transcript tr : gene) {
				total++;

				if (!tr.isProteinCoding()) {
					if (debug) System.err.println("Ignoring transcript '" + tr.getId() + "', non-coding.");
					continue;
				}
				if (tr.hasError()) {
					if (debug) System.err.println("Ignoring transcript '" + tr.getId() + "', it has errors.");
					continue;
				}

				transcripts.add(tr);
				transcriptsByChromo.getOrCreate(tr.getChromosomeName()).add(tr);
				kept++;
			}
		}

		if (verbose) Log.info("Done. Kept / total: " + kept + " / " + total);

	}

	public List<Transcript> getByChromo(String chrName) {
		return transcriptsByChromo.getOrCreate(Chromosome.simpleName(chrName));
	}

	@Override
	public Iterator<Transcript> iterator() {
		return transcripts.iterator();
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
}
