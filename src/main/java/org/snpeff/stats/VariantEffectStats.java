package org.snpeff.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffect.FunctionalClass;
import org.snpeff.stats.plot.GoogleGenePercentBar;

/**
 * Variants annotation statistics.
 * 
 * These are statistics about each variant annotation / effect. There can be
 * multiple annotaitons per variant (e.g. multiple transcripts in a gene
 * produce multiple annotations in a single variant).
 */
public class VariantEffectStats implements SamplingStats<VariantEffect> {

	public static final String CHANGE_SEPARATOR = "\t";

	boolean useSequenceOntology = false; // Use Sequence Ontology terms
	int aaChangeCountMax = Integer.MIN_VALUE;
	int codonChangeCountMax = Integer.MIN_VALUE;
	int countWarnings = 0;
	int countErrors = 0;
	Genome genome;
	CountByType countByEffect;
	CountByType countByCodon;
	CountByType countByAa;
	CountByType countByGeneRegion;
	CountByType countByImpact;
	CountByType countByFunctionalClass;
	CountByType countByBioType;
	HashSet<String> codonSet, aaSet, geneSet;
	GeneCountByTypeTable geneCountByRegionTable;
	GeneCountByTypeTable geneCountByImpactTable;
	GeneCountByTypeTable geneCountByEffectTable;

	public VariantEffectStats(Genome genome) {
		this.genome = genome;
		countByEffect = new CountByType();
		countByCodon = new CountByType();
		countByAa = new CountByType();
		countByGeneRegion = new CountByType();
		countByImpact = new CountByType();
		countByFunctionalClass = new CountByType();
		codonSet = new HashSet<>();
		aaSet = new HashSet<>();
		geneSet = new HashSet<>();
		geneCountByRegionTable = new GeneCountByTypeTable();
		geneCountByEffectTable = new GeneCountByTypeTable();
		geneCountByImpactTable = new GeneCountByTypeTable();
	}

	/**
	 * How to code an 'item' change (e.g. codon change, AA change, etc.)
	 */
	private String changeKey(String oldItem, String newItem) {
		return oldItem + CHANGE_SEPARATOR + newItem;
	}

	/**
	 * Background color used for AA change table
	 */
	public String getAaChangeColor(String oldAa, String newAa) {
		return countByAa.getColorHtml(changeKey(oldAa, newAa));
	}

	/**
	 * How many changes from oldAa to newAa do we have?
	 */
	public long getAaChangeCount(String oldAa, String newAa) {
		return countByAa.get(changeKey(oldAa, newAa));
	}

	/**
	 * Get list of all amino acisd involved
	 */
	public List<String> getAaList() {
		ArrayList<String> aas = new ArrayList<>();
		aas.addAll(aaSet);
		Collections.sort(aas);
		return aas;
	}

	public String getCodonChangeColor(String oldCodon, String newCodon) {
		return countByCodon.getColorHtml(changeKey(oldCodon, newCodon));
	}

	/**
	 * How many changes from oldCodo to newCodon do we have?
	 */
	public long getCodonChangeCount(String oldCodon, String newCodon) {
		return countByCodon.get(changeKey(oldCodon, newCodon));
	}

	/**
	 * Get a list of all codons involved
	 */
	public List<String> getCodonList() {
		ArrayList<String> codons = new ArrayList<>();
		codons.addAll(codonSet);
		Collections.sort(codons);
		return codons;
	}

	public CountByType getCountByEffect() {
		return countByEffect;
	}

	public CountByType getCountByFunctionalClass() {
		return countByFunctionalClass;
	}

	public CountByType getCountByGeneRegion() {
		return countByGeneRegion;
	}

	public CountByType getCountByImpact() {
		return countByImpact;
	}

	public int getCountErrors() {
		return countErrors;
	}

	public int getCountWarnings() {
		return countWarnings;
	}

	public GeneCountByTypeTable getGeneCountByEffectTable() {
		return geneCountByEffectTable;
	}

	public GeneCountByTypeTable getGeneCountByImpactTable() {
		return geneCountByImpactTable;
	}

	public GeneCountByTypeTable getGeneCountByRegionTable() {
		return geneCountByRegionTable;
	}

	/**
	 * Barplot of different gene regions
	 */
	public String getPlotGene() {
		GoogleGenePercentBar gb = new GoogleGenePercentBar("Variations", "", "%" //
				, 100 * countByGeneRegion.percent("" + EffectType.INTERGENIC) //
				, 100 * countByGeneRegion.percent("" + EffectType.UPSTREAM) //
				, 100 * countByGeneRegion.percent("" + EffectType.UTR_5_PRIME) //
				, 100 * countByGeneRegion.percent("" + EffectType.EXON) //
				, 100 * countByGeneRegion.percent("" + EffectType.SPLICE_SITE_DONOR) //
				, 100 * countByGeneRegion.percent("" + EffectType.INTRON) //
				, 100 * countByGeneRegion.percent("" + EffectType.SPLICE_SITE_ACCEPTOR) //
				, 100 * countByGeneRegion.percent("" + EffectType.UTR_3_PRIME) //
				, 100 * countByGeneRegion.percent("" + EffectType.DOWNSTREAM) //
		);
		return gb.toURLString();
	}

	public double getSilentRatio() {
		long mis = countByFunctionalClass.get(FunctionalClass.MISSENSE.toString());
		long silent = countByFunctionalClass.get(FunctionalClass.SILENT.toString());
		if (silent == 0) return 0.0;
		return ((double) mis) / ((double) silent);
	}

	@Override
	public boolean hasData() {
		return countByEffect.hasData();
	}

	@Override
	public void sample(VariantEffect variantEffect) {
		// Any warnings?
		if (variantEffect.hasWarning()) countWarnings++;
		if (variantEffect.hasError()) countErrors++;

		// Count by effect
		String effect = variantEffect.getEffectTypeString(useSequenceOntology);
		if (effect == null || effect.isEmpty()) return; // No effect? Nothing to do

		// Split effects
		String effects[] = effect.split("[\\+&]");
		for (String eff : effects)
			countByEffect.inc(eff);

		// Count by gene region
		String geneRegion = variantEffect.getGeneRegion();
		countByGeneRegion.inc(geneRegion);

		// Count by impact
		String impact = variantEffect.getEffectImpact().toString();
		countByImpact.inc(impact);

		// Count by functional class
		FunctionalClass fc = variantEffect.getFunctionalClass();
		if (fc != FunctionalClass.NONE) countByFunctionalClass.inc(fc.toString());

		// Count gene and gene region
		Marker marker = variantEffect.getMarker();
		if (marker != null) { // E.g. Intergenic is not associated with a marker
			Gene gene = variantEffect.getGene();
			Transcript tr = variantEffect.getTranscript();
			if (tr != null && gene != null) {
				// Count by effect by transcript
				for (String eff : effects)
					geneCountByEffectTable.sample(gene, tr, eff, variantEffect);

				// Count by region by transcript
				geneCountByRegionTable.sample(gene, tr, geneRegion, variantEffect);

				// Count by impact
				geneCountByImpactTable.sample(gene, tr, variantEffect.getEffectImpact().toString(), variantEffect);
			}
		}

		//---
		// Count codon changes
		//---
		if ((variantEffect.getCodonsRef() != null) && (variantEffect.getCodonsRef().length() > 0)) {
			// Note: There might be many codons changing
			String oldCodons[] = split(variantEffect.getCodonsRef(), 3);
			String newCodons[] = split(variantEffect.getCodonsAlt(), 3);
			int max = Math.max(oldCodons.length, newCodons.length);

			for (int i = 0; i < max; i++) {
				String oldCodon = "-", newCodon = "-";

				if (i < oldCodons.length) oldCodon = oldCodons[i].toUpperCase();
				if (i < newCodons.length) newCodon = newCodons[i].toUpperCase();

				String codonChangeKey = changeKey(oldCodon, newCodon);
				codonSet.add(oldCodon);
				codonSet.add(newCodon);
				countByCodon.inc(codonChangeKey);
			}
		}

		//---
		// Count amino acid changes
		//---
		if ((variantEffect.getAaRef() != null) && (variantEffect.getAaRef().length() > 0)) {
			// Note: There might be many AAs changing
			String oldAas[] = split(variantEffect.getAaRef(), 1);
			String newAas[] = split(variantEffect.getAaAlt(), 1);
			int max = Math.max(oldAas.length, newAas.length);

			for (int i = 0; i < max; i++) {
				String oldAa = "-", newAa = "-";

				if (i < oldAas.length) oldAa = oldAas[i].toUpperCase();
				if (i < newAas.length) newAa = newAas[i].toUpperCase();

				String aaChangeKey = changeKey(oldAa, newAa);
				aaSet.add(oldAa);
				aaSet.add(newAa);
				countByAa.inc(aaChangeKey);
			}
		}
	}

	public void setUseSequenceOntology(boolean useSequenceOntology) {
		this.useSequenceOntology = useSequenceOntology;
	}

	/**
	 * Split a string into fixed size parts
	 */
	String[] split(String str, int size) {
		int numStr = str.length() / size;
		String splitStr[] = new String[numStr];
		char chars[] = str.toCharArray();

		for (int i = 0, h = 0; (i < chars.length) && (h < splitStr.length); h++) {
			String newStr = "";

			for (int j = 0; (j < size) && (i < chars.length); j++, i++)
				newStr += chars[i];

			splitStr[h] = newStr;
		}

		return splitStr;
	}
}
