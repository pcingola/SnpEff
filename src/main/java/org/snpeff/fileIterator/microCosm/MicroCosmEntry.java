package org.snpeff.fileIterator.microCosm;

import org.snpeff.interval.Genome;
import org.snpeff.interval.MicroRnaBindingSite;

/**
 * Entry in a MicroCosm (miRNA target prediction) file
 * 
 * @author pablocingolani
 */
public class MicroCosmEntry {
	public String group;
	public String miRnaName;
	public String method;
	public String feature;
	public String chr;
	public int start;
	public int end;
	public String strand;
	public String phase;
	public double score;
	public double pValue;
	public String transcriptId;
	public String externalName;

	/**
	 * Create a MicroRnaBindingSite marker
	 * @return
	 */
	public MicroRnaBindingSite getMarker(Genome genome) {
		return new MicroRnaBindingSite(genome.getOrCreateChromosome(chr) //
				, start - 1 // One-based coordinates
				, end - 1 // One-based coordinates
				, strand.equals("-") //
				, miRnaName //
				, pValue //
		);
	}

	@Override
	public String toString() {
		return "group        : " + group + "\n" //
				+ "miRnaName    : " + miRnaName + "\n" //
				+ "method       : " + method + "\n" //
				+ "feature      : " + feature + "\n" //
				+ "chr          : " + chr + "\n" //
				+ "start        : " + start + "\n" //
				+ "end          : " + end + "\n" //
				+ "strand       : " + strand + "\n" //
				+ "phase        : " + phase + "\n" //
				+ "score        : " + score + "\n" //
				+ "pvalue       : " + pValue + "\n" //
				+ "transcriptId : " + transcriptId + "\n" //
				+ "externalName : " + externalName + "\n" //
		;
	}
}
