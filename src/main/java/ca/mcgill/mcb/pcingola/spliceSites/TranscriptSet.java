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

/**
 * A set of transcripts
 * 
 * @author pablocingolani
 */
public class TranscriptSet implements Iterable<Transcript> {

	public static final int MAX_TRANSCRIPTS_PER_GENE = 1000;

	HashSet<Transcript> transcripts;
	AutoHashMap<String, ArrayList<Transcript>> transcriptsByChromo;

	public TranscriptSet(Genome genome) {
		transcripts = new HashSet<Transcript>();
		transcriptsByChromo = new AutoHashMap<String, ArrayList<Transcript>>(new ArrayList<Transcript>());

		for (Gene gene : genome.getGenes()) {
			if (gene.numChilds() > MAX_TRANSCRIPTS_PER_GENE) continue;

			for (Transcript tr : gene) {
				if (!tr.isProteinCoding()) continue;
				if (tr.hasError()) continue;

				transcripts.add(tr);
				transcriptsByChromo.getOrCreate(tr.getChromosomeName()).add(tr);
			}
		}
	}

	public List<Transcript> getByChromo(String chrName) {
		return transcriptsByChromo.getOrCreate(Chromosome.simpleName(chrName));
	}

	@Override
	public Iterator<Transcript> iterator() {
		return transcripts.iterator();
	}
}
