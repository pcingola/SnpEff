package ca.mcgill.mcb.pcingola;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalTreeArray;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;

public class Zzz extends SnpEff {

	public static void main(String[] args) {
		Markers markers = new Markers();

		// Create markers
		Genome genome = new Genome();
		Chromosome chr1 = genome.getOrCreateChromosome("1");

		for (int i = 0; i < 10; i++) {
			int start = 1000 * i;
			int end = start + 100;
			Marker m = new Marker(chr1, start, end);
			markers.add(m);
			System.out.println(m);
		}

		// Build tree
		IntervalTreeArray itree = new IntervalTreeArray(markers);
		itree.build();

		System.out.println(itree.toStringAll());
	}
}
