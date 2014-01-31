package ca.mcgill.mcb.pcingola.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.interval.SeqChange.ChangeType;

/**
 * Some stats about seqChange objects
 */
public class SeqChangeStats implements SamplingStats<SeqChange> {

	public static final String CHANGE_SEPARATOR = "\t";
	static final char bases[] = { 'A', 'C', 'G', 'T' };

	Genome genome;
	IntStats qualityStats;
	IntStats coverageStats;
	IntStats indelLen;
	HashMap<String, ChrPosStats> chrPosStatsbyName;
	long countSeqChanges = 0;
	long countVariants = 0;
	long countNonVariants;
	long countNonEmptyId;
	CountByType countByChangeType, countByChangeTypeHet, countByChangeTypeHom, baseChangesCount;

	public SeqChangeStats(Genome genome) {
		this.genome = genome;
		qualityStats = new IntStats();
		coverageStats = new IntStats();
		indelLen = new IntStats();
		chrPosStatsbyName = new HashMap<String, ChrPosStats>();
		countByChangeType = new CountByType();
		countByChangeTypeHom = new CountByType();
		countByChangeTypeHet = new CountByType();
		baseChangesCount = new CountByType();
	}

	/**
	 * How to code an 'item' change (e.g. codon change, AA change, etc.)
	 * @param oldItem
	 * @param newItem
	 * @return
	 */
	private String changeKey(String oldItem, String newItem) {
		return oldItem + CHANGE_SEPARATOR + newItem;
	}

	public char[] getBases() {
		return bases;
	}

	/**
	 * Background color used for base change table
	 * @param oldBase
	 * @param newBase
	 * @return
	 */
	public String getBasesChangesColor(String oldBase, String newBase) {
		return baseChangesCount.getColorHtml(changeKey(oldBase, newBase));
	}

	public long getBasesChangesCount(String oldBase, String newBase) {
		return baseChangesCount.get(changeKey(oldBase, newBase));
	}

	public ChangeType[] getChangeType() {
		return ChangeType.values();
	}

	public int getChangeTypeLength() {
		return ChangeType.values().length;
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
		return chrPosStatsbyName.get(chrName).toStringHistoPlot("Changes histogram: " + chrName, "Position", "Changes");
	}

	/**
	 * Total number of seqChanges
	 * @return
	 */
	public long getCount() {
		return countSeqChanges;
	}

	/**
	 * Number of seqChanges by type
	 * @return
	 */
	public CountByType getCountByChangeType() {
		return countByChangeType;
	}

	/**
	 * Number of heterozygous seqChanges by type
	 * @return
	 */
	public CountByType getCountByChangeTypeHet() {
		return countByChangeTypeHet;
	}

	/**
	 * Number of homozygous seqChanges by type
	 * @return
	 */
	public CountByType getCountByChangeTypeHom() {
		return countByChangeTypeHom;
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

	public String getCoverageHistoUrl() {
		return coverageStats.toStringPlot("Coverage histogram", "Coverage", true);
	}

	public IntStats getCoverageStats() {
		return coverageStats;
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
			len += ch.getEnd() - ch.getStart() + 1;
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
		double tot = countSeqChanges;
		double known = countNonEmptyId;
		return tot > 0 ? known / tot : 0;
	}

	public String getQualityHistoUrl() {
		return qualityStats.toStringPlot("Quality histogram", "Quality", true);
	}

	public IntStats getQualityStats() {
		return qualityStats;
	}

	/**
	 * Rate of change
	 * @return
	 */
	public long getRateOfChange() {
		return countSeqChanges > 0 ? getGenomeLenEffective() / countSeqChanges : 0;
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

	public long getTransitions() {
		return baseChangesCount.get(changeKey("A", "G")) //
				+ baseChangesCount.get(changeKey("G", "A")) //
				+ baseChangesCount.get(changeKey("C", "T")) //
				+ baseChangesCount.get(changeKey("T", "C")) //
		;
	}

	public long getTransversions() {
		return baseChangesCount.get(changeKey("A", "C")) //
				+ baseChangesCount.get(changeKey("C", "A")) //
				+ baseChangesCount.get(changeKey("A", "T")) //
				+ baseChangesCount.get(changeKey("T", "A")) //
				+ baseChangesCount.get(changeKey("G", "C")) //
				+ baseChangesCount.get(changeKey("C", "G")) //
				+ baseChangesCount.get(changeKey("G", "T")) //
				+ baseChangesCount.get(changeKey("T", "G")) //
		;
	}

	/**
	 * Transitions / transverions ratio
	 * 
	 * WARNING: I removed the '2.0' factor because it mostly confused people. 
	 * I clarify that the ratio is a 'raw' ratio in the summary page
	 * 
	 * ------------------------------------------------------------------------
	 * Comments that follow are out-dated. I leave it here just for reference.
	 * 
	 * Note: Why is there a '2' in the ratio and not just "number of transitions / number of transverions"?
	 * 
	 * From Casey Bergman (Manchester Univ.)
	 * 		Ts:Tv ratio is a ratio of rates, not observed events. Imagine observing 100 sites with 
	 * 		transitions and 100 sites with transversions. Your method would say that the Ts:Tv rate 
	 * 		ratio is 1. But since there are 4 possible Tv mutation types and only 2 possible Ts 
	 * 		mutation types, in this example there is actually a 2-fold higher rate of Ts mutations 
	 * 		that Tv mutations per site. Thus, the Ts:Tv (rate) ratio is 2:1
	 * 
	 * References: 
	 * 		http://www.mun.ca/biology/scarr/Transitions_vs_Transversions.html
	 * 		http://biostar.stackexchange.com/questions/4759/ti-tv-ratio-confirms-snp-discovery-is-this-a-general-rule/
	 * 
	 * @return
	 */
	public double getTsTvRatio() {
		double ts = getTransitions();
		double tv = getTransversions();
		return tv > 0 ? ts / tv : 0;
	}

	@Override
	public boolean hasData() {
		return countSeqChanges != 0;
	}

	/**
	 * Perform starts on an InDel
	 * @param seqChange
	 */
	void indelSample(SeqChange seqChange) {
		// InDel length histogram
		int len = (seqChange.isDel() ? -1 : 1) * (seqChange.getChangeOption(0).length() - 1);
		indelLen.sample(len);
	}

	/**
	 * Use this sample to perform statistics
	 */
	@Override
	public void sample(SeqChange seqChange) {
		// Not a real change => Ignore
		if (!seqChange.isChange()) {
			countNonVariants++;
			return;
		}

		countSeqChanges++;

		// Count non-empty IDs
		if ((seqChange.getId() != null) && !seqChange.getId().isEmpty()) countNonEmptyId++;

		// Count by change type
		String changeType = seqChange.getChangeType().toString();
		countByChangeType.inc(changeType); // Each type of changes

		// Hom or Het 
		if (seqChange.isHomozygous()) countByChangeTypeHom.inc(changeType);
		if (seqChange.isHeterozygous()) countByChangeTypeHet.inc(changeType);

		// Quality histogram
		if (seqChange.getQuality() >= 0) qualityStats.sample((int) seqChange.getQuality()); // Quality < 0 means 'not available'

		// Coverage histogram
		if (seqChange.getCoverage() >= 0) coverageStats.sample(seqChange.getCoverage()); // Coverage < 0 means 'not available'

		// SNP stats or InDel stats
		if (seqChange.isSnp()) snpSample(seqChange);
		else if (seqChange.isInDel()) indelSample(seqChange);

		// Coverage by chromosome (hot spot) stats
		Chromosome chr = seqChange.getChromosome();
		if (chr != null) {
			// Get stats for this chromosome
			String chrName = chr.getId();
			ChrPosStats chrPosStats = chrPosStatsbyName.get(chrName);

			// No stats? => Create a new one
			if (chrPosStats == null) {
				chrPosStats = new ChrPosStats(chrName, chr.size());
				chrPosStatsbyName.put(chrName, chrPosStats);
			}

			// Perform stats
			chrPosStats.sample(seqChange.getStart());
		}
	}

	/**
	 * Perform stats on a SNP 
	 * @param seqChange
	 */
	void snpSample(SeqChange seqChange) {
		// Increment change matrix counters
		String ref = seqChange.getReference();
		int numOpts = seqChange.getChangeOptionCount();

		for (int i = 0; i < numOpts; i++) {
			String snp = seqChange.getChangeOption(i);
			if (ref != snp) baseChangesCount.inc(changeKey(ref, snp)); // Some case might be the same base (e.g. heterozygous SNP change "A => W", where 'W' means 'A' or 'T')
		}
	}

}
