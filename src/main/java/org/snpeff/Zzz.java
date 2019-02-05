package org.snpeff;

import java.util.HashSet;
import java.util.Set;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;

public class Zzz {

	public static void main(String[] args) {
		boolean verbose = false;
		String genome = "GRCh38.92";

		System.out.println("Loading config");
		Config conf = new Config(genome);
		System.out.println("Loading genome");
		SnpEffectPredictor sep = conf.loadSnpEffectPredictor();

		// Selected chromosomes
		Set<String> chrs = new HashSet<>();
		for (int i = 1; i < 23; i++)
			chrs.add("" + i);
		chrs.add("X");
		chrs.add("Y");
		chrs.add("M");
		chrs.add("MT");

		// Count lengths
		int gcount = 0, tcount = 0;
		long glength = 0, tlength = 0, elength = 0, ilength = 0;
		for (Gene g : sep.getGenome().getGenes()) {
			//			if (!g.isProteinCoding()) continue;
			if (!chrs.contains(g.getChromosomeName())) {
				System.err.println("Gene '" + g.getGeneName() + "', not in main chromosomes '" + g.getChromosomeName() + "', skipping");
				continue;
			}

			gcount++;
			int glen = g.size();
			glength += glen;
			if (verbose) System.out.println("Gene: " + g.getGeneName() + "\tsize: " + glen);

			for (Transcript t : g) {
				tcount++;
				int tlen = t.size();
				int elen = 0;
				for (Exon e : t)
					elen += e.size();
				int ilen = tlen - elen;
				if (verbose) System.out.println("\t" + t.getId() + "\ttr size: " + tlen + "\texon length: " + elen + "\tintron length: " + ilen);

				tlength += tlen;
				ilength += ilen;
				elength += elen;
			}
		}

		int chrCount = 0;
		long chrLength = 0;
		for (Chromosome chr : sep.getGenome().getChromosomes()) {
			if (!chrs.contains(chr.getChromosomeName())) continue;
			System.out.println("Chr: " + chr.getChromosomeName() + "\tlen: " + chr.size());
			chrCount++;
			chrLength += chr.size();
		}

		System.out.println("Totals:" //
				+ "\nChromsomes\t" + chrCount//
				+ "\nChromsomes length\t" + chrLength//
				+ "\nNumber of genes\t" + gcount//
				+ "\nTotal genes length\t" + glength //
				+ "\nNumber of transcripts\t" + tcount//
				+ "\nTotal transcript length\t" + tlength //
				+ "\nTotal intron length\t" + ilength //
				+ "\nTotal exon length\t" + elength //
		);
	}

}