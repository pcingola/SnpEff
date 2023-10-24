package org.snpeff.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;

/**
 * Variants statistics: These are statisticas at Variant level.
 * For example in a VCF file, there can be more than one variant per VCF entry (i.e. VCF line)
 */
public class VariantStats implements SamplingStats<Variant> {

	public static final String CHANGE_SEPARATOR = "\t";
	static final char bases[] = { 'A', 'C', 'G', 'T' };

	Genome genome;
	IntStats indelLen;
	HashMap<String, ChrPosStats> chrPosStatsbyName;
	long countVariants = 0;
	long countNonVariants;
	long countNonEmptyId;
	CountByType countByChangeType, baseChangesCount;

	public VariantStats(Genome genome) {
		this.genome = genome;
		indelLen = new IntStats();
		chrPosStatsbyName = new HashMap<String, ChrPosStats>();
		countByChangeType = new CountByType();
		baseChangesCount = new CountByType();
	}

	/**
	 * How to code an 'item' change (e.g. codon change, AA change, etc.)
	 */
	private String changeKey(String oldItem, String newItem) {
		return oldItem + CHANGE_SEPARATOR + newItem;
	}

	void chromoStats(Variant variant) {
		String chrName = variant.getChromosomeName();
		ChrPosStats chrPosStats = chrPosStatsbyName.get(chrName);

		// No stats? => Create a new one
		if (chrPosStats == null) {
			Chromosome chr = genome.getChromosome(chrName);
			if (chr != null) {
				chrPosStats = new ChrPosStats(chrName, chr.size());
				chrPosStatsbyName.put(chrName, chrPosStats);
			}
		}

		// Perform stats
		if (chrPosStats != null) chrPosStats.sample(variant.getStart());
	}

	public char[] getBases() {
		return bases;
	}

	/**
	 * Background color used for base change table
	 */
	public String getBasesChangesColor(String oldBase, String newBase) {
		return baseChangesCount.getColorHtml(changeKey(oldBase, newBase));
	}

	public long getBasesChangesCount(String oldBase, String newBase) {
		return baseChangesCount.get(changeKey(oldBase, newBase));
	}

	public VariantType[] getChangeType() {
		return VariantType.values();
	}

	public int getChangeTypeLength() {
		return VariantType.values().length;
	}

	/**
	 * Choromosome length
	 * @param chromoName
	 * @return
	 */
	public int getChromosomeLength(String chromoName) {
		Chromosome chr = genome.getChromosome(chromoName);
		if (chr != null) return chr.size();
		return 0;
	}

	/**
	 * A list of chromosomes that had at least one change
	 * Note: Chromosome names are sorted.
	 *
	 * @return
	 */
	public List<String> getChromosomeNamesEffective() {
		// Add all chromosomes to the list and sort them
		ArrayList<Chromosome> chrsEffective = new ArrayList<Chromosome>();
		for (String chrName : chrPosStatsbyName.keySet())
			chrsEffective.add(genome.getChromosome(chrName));
		Collections.sort(chrsEffective);

		// Create a list of chromosome names
		ArrayList<String> chrNames = new ArrayList<String>();
		for (Chromosome chr : chrsEffective)
			chrNames.add(chr.getId());

		return chrNames;
	}

	public ChrPosStats getChrPosStats(String chrName) {
		return chrPosStatsbyName.get(chrName);
	}

	public String getChrPosStatsChartUrl(String chrName) {
		return chrPosStatsbyName.get(chrName).toStringHistoPlot("Variants histogram: " + chrName, "Position", "Variants");
	}

	/**
	 * Total number of variants
	 * @return
	 */
	public long getCount() {
		return countVariants;
	}

	/**
	 * Number of variants by type
	 * @return
	 */
	public CountByType getCountByChangeType() {
		return countByChangeType;
	}

	/**
	 * Number of changes by chromosome
	 * @param chromoName
	 * @return
	 */
	public int getCountByChromosome(String chromoName) {
		ChrPosStats chrStats = chrPosStatsbyName.get(chromoName);
		if (chrStats == null) return 0;
		return chrStats.getTotal();
	}

	public long getCountNonEmptyId() {
		return countNonEmptyId;
	}

	public long getCountNonVariants() {
		return countNonVariants;
	}

	/**
	 * Genome length
	 * @return
	 */
	public long getGenomeLen() {
		return genome.length();
	}

	/**
	 * Genome effective length: The sum of length of every chromosome that had a change
	 * (e.g. If there was no SNP in chromosome Y, then it doesn't count in the effective length)
	 *
	 * @return
	 */
	public long getGenomeLenEffective() {
		long len = 0;
		for (String chrName : chrPosStatsbyName.keySet()) {
			Chromosome ch = genome.getChromosome(chrName);
			len += ch.size();
		}

		return len;
	}

	public IntStats getIndelLen() {
		return indelLen;
	}

	public String getIndelLenHistoUrl() {
		return indelLen.toStringPlot("Insertion deletion length histogram", "Length", true);
	}

	/**
	 * Ratio of known variants (the one with a non-empty ID) and total variants
	 */
	public double getKnownRatio() {
		double tot = countVariants;
		double known = countNonEmptyId;
		return tot > 0 ? known / tot : 0;
	}

	/**
	 * Rate of change
	 * @return
	 */
	public long getRateOfChange() {
		return countVariants > 0 ? getGenomeLenEffective() / countVariants : 0;
	}

	/**
	 * Rate of change by chromosome
	 * @param chromoName
	 * @return
	 */
	public int getRateOfChangeByChromosome(String chromoName) {
		int rate = 0;
		int len = getChromosomeLength(chromoName);
		int count = getCountByChromosome(chromoName);
		if (count > 0) rate = len / count;
		return rate;
	}

	@Override
	public boolean hasData() {
		return countVariants != 0;
	}

	/**
	 * Perform starts on an InDel
	 */
	void indelSample(Variant variant) {
		// InDel length histogram
		int len = (variant.isDel() ? -1 : 1) * (variant.getAlt().length() - 1);
		indelLen.sample(len);
	}

	/**
	 * Use this sample to perform statistics
	 */
	@Override
	public void sample(Variant variant) {
		// Not a real change => Ignore
		if (!variant.isVariant()) {
			countNonVariants++;
			return;
		}

		countVariants++;

		// Count non-empty IDs
		if ((variant.getId() != null) && !variant.getId().isEmpty()) countNonEmptyId++;

		// Count by change type
		String variantType = variant.getVariantType().toString();
		countByChangeType.inc(variantType); // Each type of changes

		// SNP stats or InDel stats
		if (variant.isVariant()) {
			if (variant.isSnp()) snpSample(variant);
			else if (variant.isInDel()) indelSample(variant);
		}

		// Coverage by chromosome (hot spot) stats
		chromoStats(variant);
	}

	/**
	 * Perform stats on a SNP
	 */
	void snpSample(Variant variant) {
		baseChangesCount.inc(changeKey(variant.getReference(), variant.getAlt())); // Some case might be the same base (e.g. heterozygous SNP change "A => W", where 'W' means 'A' or 'T')
	}

}
