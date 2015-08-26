package ca.mcgill.mcb.pcingola.spliceSites;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import ca.mcgill.mcb.pcingola.collections.AutoHashMap;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.util.Timer;

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
		if (verbose) Timer.showStdErr("Filtering transcripts: Removing non-coding and transcripts with errors");

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

		if (verbose) Timer.showStdErr("Done. Kept / total: " + kept + " / " + total);

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
