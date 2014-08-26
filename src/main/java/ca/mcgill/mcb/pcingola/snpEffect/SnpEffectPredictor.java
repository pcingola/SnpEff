package ca.mcgill.mcb.pcingola.snpEffect;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import net.sf.samtools.util.RuntimeEOFException;
import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Intergenic;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.SpliceSite;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Utr;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalForest;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Predicts effects of SNPs
 *
 * Note: Actually tries to predict any kind of SeqChange, not only SNPs . It is called SnpEffectPredictor for 'historical reasons'.
 *
 * @author pcingola
 *
 */
public class SnpEffectPredictor implements Serializable {
	private static final long serialVersionUID = 4519418862303325081L;

	public static final int DEFAULT_UP_DOWN_LENGTH = 5000;
	public static final int HUGE_DELETION_SIZE_THRESHOLD = 1000000; // Number of bases
	public static final double HUGE_DELETION_RATIO_THRESHOLD = 0.01; // Percentage of bases

	boolean useChromosomes = true;

	int upDownStreamLength = DEFAULT_UP_DOWN_LENGTH;
	int spliceSiteSize = SpliceSite.CORE_SPLICE_SITE_SIZE;
	int spliceRegionExonSize = SpliceSite.SPLICE_REGION_EXON_SIZE;
	int spliceRegionIntronMin = SpliceSite.SPLICE_REGION_INTRON_MIN;
	int spliceRegionIntronMax = SpliceSite.SPLICE_REGION_INTRON_MAX;

	Genome genome;
	Markers markers; // All other markers are stored here (e.g. custom markers, intergenic, etc.)
	IntervalForest intervalForest;

	/**
	 * Load predictor from a binary file
	 */
	public static SnpEffectPredictor load(Config config) {
		String snpEffPredFile = config.getFileSnpEffectPredictor();

		// Sanity check
		if (!Gpr.canRead(snpEffPredFile)) throw new RuntimeException("\tERROR: Cannot read file '" + snpEffPredFile + "'.\n\tYou can try to download the database by running the following command:\n\t\tjava -jar snpEff.jar download " + config.getGenome().getVersion() + "\n");

		// Load markers from file
		MarkerSerializer ms = new MarkerSerializer();
		Markers markers = ms.load(snpEffPredFile);

		// Find genome
		Genome genome = null;
		for (Marker m : markers)
			if (m instanceof Genome) genome = (Genome) m;
		if (genome == null) throw new RuntimeException("Genome not found. This should never happen!");

		// Create predictor
		SnpEffectPredictor snpEffectPredictor = new SnpEffectPredictor(genome);

		// Add genes
		for (Marker m : markers)
			if (m instanceof Gene) {
				Gene gene = (Gene) m;
				snpEffectPredictor.add(gene);
			}

		// Add 'other' markers
		for (Marker m : markers)
			if (!(m instanceof Genome) //
					&& !(m instanceof Chromosome) //
					&& !(m instanceof Gene) //
					&& !(m instanceof Transcript) //
					&& !(m instanceof Exon) //
					&& !(m instanceof Cds) //
					&& !(m instanceof Utr) //
					&& !(m instanceof SpliceSite) //
					) snpEffectPredictor.add(m);

		return snpEffectPredictor;
	}

	public SnpEffectPredictor(Genome genome) {
		this.genome = genome;
		markers = new Markers();
	}

	/**
	 * Add a gene interval
	 */
	public void add(Gene gene) {
		genome.getGenes().add(gene);
	}

	/**
	 * Add a marker
	 *
	 * Note: Markers have to be added BEFORE building the interval trees.
	 *       Interval trees are built the first time you call snpEffect(snp) method.
	 */
	public void add(Marker marker) {
		markers.add(marker);
	}

	/**
	 * Add a set of markers
	 * @param markersToAdd
	 */
	public void addAll(Markers markersToAdd) {
		for (Marker marker : markersToAdd)
			markers.add(marker);
	}

	/**
	 * Create interval trees (forest)
	 */
	public void buildForest() {
		intervalForest = new IntervalForest();

		// Add all chromosomes to forest
		if (useChromosomes) {
			for (Chromosome chr : genome)
				intervalForest.add(chr);
		}

		// Add all genes to forest
		for (Gene gene : genome.getGenes())
			intervalForest.add(gene);

		//---
		// Create (and add) up-down stream, splice sites, intergenic, etc
		//---
		markers.add(createGenomicRegions());

		// Add all 'markers' to forest (includes custom intervals)
		intervalForest.add(markers);

		// Build interval forest
		intervalForest.build();
	}

	/**
	 * Create (and add) up-down stream, splice sites, intergenic, etc
	 */
	public Markers createGenomicRegions() {
		Markers markers = new Markers();

		// Add up-down stream intervals
		for (Marker upDownStream : genome.getGenes().createUpDownStream(upDownStreamLength))
			markers.add(upDownStream);

		// Add splice site intervals
		for (Marker spliceSite : genome.getGenes().createSpliceSites(spliceSiteSize, spliceRegionExonSize, spliceRegionIntronMin, spliceRegionIntronMax))
			markers.add(spliceSite);

		// Intergenic markers
		for (Intergenic intergenic : genome.getGenes().createIntergenic())
			markers.add(intergenic);

		return markers;
	}

	/**
	 * Obtain a gene interval
	 */
	public Gene getGene(String geneIntervalId) {
		return genome.getGenes().get(geneIntervalId);
	}

	public Genome getGenome() {
		return genome;
	}

	public IntervalForest getIntervalForest() {
		return intervalForest;
	}

	public Markers getMarkers() {
		return markers;
	}

	public int getSpliceRegionExonSize() {
		return spliceRegionExonSize;
	}

	public int getSpliceRegionIntronMax() {
		return spliceRegionIntronMax;
	}

	public int getSpliceRegionIntronMin() {
		return spliceRegionIntronMin;
	}

	public Transcript getTranscript(String trId) {
		for (Gene g : genome.getGenes())
			for (Transcript tr : g)
				if (tr.getId().equals(trId)) return tr;

		return null;
	}

	public int getUpDownStreamLength() {
		return upDownStreamLength;
	}

	/**
	 * Is the chromosome missing in this marker?
	 */
	boolean isChromosomeMissing(Marker marker) {
		// Missing chromosome in marker?
		if (marker.getChromosome() == null) return true;

		// Missing chromosome in genome?
		String chrName = marker.getChromosomeName();
		Chromosome chr = genome.getChromosome(chrName);
		if (chr == null) return true;

		// Chromosome length is 1 or less?
		if (chr.size() < 1) return true;

		// Tree not found in interval forest?
		if (!intervalForest.hasTree(chrName)) return true;

		// OK, we have the chromosome
		return false;
	}

	/**
	 * Dump to sdtout
	 */
	public void print() {
		System.out.println(genome);

		// Show genes
		for (Gene gene : genome.getGenes().sorted())
			System.out.println(gene);

		// Show other inervals
		for (Marker marker : markers)
			System.out.println(marker);
	}

	/**
	 * Return a collection of intervals that intersect 'marker'
	 */
	public Markers query(Marker marker) {
		return intervalForest.query(marker);
	}

	/**
	 * Find closest gene to this marker
	 *
	 * In case more than one 'closest' gene is
	 * found (e.g. two or more genes at the
	 * same distance). The following rules
	 * apply:
	 *
	 * 		i) If many genes have the same 'closest
	 * 		   distance', coding genes are preferred.
	 *
	 * 		ii) If more than one coding gene has the
	 * 		    same 'closet distance', a random gene
	 *			is returned.
	 *
	 * @param inputInterval
	 */
	public Gene queryClosestGene(Marker inputInterval) {
		int initialExtension = 1000;

		String chrName = inputInterval.getChromosomeName();
		Chromosome chr = genome.getChromosome(chrName);
		if (chr == null) return null;

		if (chr.size() > 0) {
			// Extend interval to capture 'close' genes
			for (int extend = initialExtension; extend < chr.size(); extend *= 2) {
				int start = Math.max(inputInterval.getStart() - extend, 0);
				int end = inputInterval.getEnd() + extend;
				Marker extended = new Marker(chr, start, end, false, "");

				// Find all genes that intersect with the interval
				Markers markers = query(extended);
				Markers genes = new Markers();
				int minDist = Integer.MAX_VALUE;
				for (Marker m : markers) {
					if (m instanceof Gene) {
						int dist = m.distance(inputInterval);
						if (dist < minDist) {
							genes.add(m);
							minDist = dist;
						}
					}
				}

				// Found something?
				if (genes.size() > 0) {
					// Find a gene having distance 'minDist'. Prefer coding genes
					Gene minDistGene = null;

					for (Marker m : genes) {
						int dist = m.distance(inputInterval);
						if (dist == minDist) {
							Gene gene = (Gene) m;
							if (minDistGene == null) minDistGene = gene;
							else if (!minDistGene.isProteinCoding() && gene.isProteinCoding()) minDistGene = gene;
						}
					}

					return minDistGene;
				}

			}
		}

		// Nothing found
		return null;
	}

	/**
	 * Return a collection of intervals that intersect 'marker'
	 * Query resulting genes, transcripts and exons to get ALL types of intervals possible
	 *
	 * @return
	 */
	public Markers queryDeep(Marker marker) {
		if (Config.get().isErrorOnMissingChromo() && isChromosomeMissing(marker)) throw new RuntimeEOFException("Chromosome missing for marker: " + marker);

		boolean hitChromo = false;
		Markers hits = new Markers();
		Markers intersects = query(marker);

		if (intersects.size() > 0) {
			for (Marker m : intersects) {
				hits.add(m);

				if (m instanceof Chromosome) {
					hitChromo = true; // OK (we have to hit a chromosome, otherwise it's an error
				} else if (m instanceof Gene) {
					// Analyze Genes
					Gene gene = (Gene) m;
					hits.addAll(gene.query(marker));
				}
			}
		}

		if (!hitChromo && Config.get().isErrorChromoHit()) throw new RuntimeException("ERROR: Out of chromosome range. " + marker);
		return hits;
	}

	/**
	 * Name of the regions hit by a marker
	 * @param marker
	 * @return A set of region names
	 */
	public Set<String> regions(Marker marker, boolean showGeneDetails, boolean compareTemplate) {
		return regions(marker, showGeneDetails, compareTemplate, null);
	}

	/**
	 * Name of the regions hit by a marker
	 * @param marker
	 * @param showGeneDetails
	 * @param compareTemplate
	 * @param id : Only use genes or transcripts matching this ID
	 * @return
	 */
	public Set<String> regions(Marker marker, boolean showGeneDetails, boolean compareTemplate, String id) {
		if (Config.get().isErrorOnMissingChromo() && isChromosomeMissing(marker)) throw new RuntimeEOFException("Chromosome missing for marker: " + marker);

		boolean hitChromo = false;
		HashSet<String> hits = new HashSet<String>();

		Markers intersects = query(marker);
		if (intersects.size() > 0) {
			for (Marker markerInt : intersects) {

				if (markerInt instanceof Chromosome) {
					hitChromo = true; // OK (we have to hit a chromosome, otherwise it's an error
					hits.add(markerInt.getClass().getSimpleName()); // Add marker name to the list
				} else if (markerInt instanceof Gene) {
					// Analyze Genes
					Gene gene = (Gene) markerInt;
					regionsAddHit(hits, gene, marker, showGeneDetails, compareTemplate);

					// For all transcripts...
					for (Transcript tr : gene) {
						if ((id == null) || gene.getId().equals(id) || tr.getId().equals(id)) { // Mathes ID? (...or no ID to match)

							// Does it intersect this transcript?
							if (tr.intersects(marker)) {
								regionsAddHit(hits, tr, marker, showGeneDetails, compareTemplate);

								// Does it intersect a UTR?
								for (Utr utr : tr.getUtrs())
									if (utr.intersects(marker)) regionsAddHit(hits, utr, marker, showGeneDetails, compareTemplate);

								// Does it intersect an exon?
								for (Exon ex : tr)
									if (ex.intersects(marker)) regionsAddHit(hits, ex, marker, showGeneDetails, compareTemplate);

								// Does it intersect an intron?
								for (Intron intron : tr.introns())
									if (intron.intersects(marker)) regionsAddHit(hits, intron, marker, showGeneDetails, compareTemplate);
							}
						}
					}
				} else {
					// No ID to match?
					if (id == null) regionsAddHit(hits, markerInt, marker, showGeneDetails, compareTemplate);
					else {
						// Is ID from transcript?
						Transcript tr = (Transcript) markerInt.findParent(Transcript.class);
						if ((tr != null) && (tr.getId().equals(id))) {
							regionsAddHit(hits, markerInt, marker, showGeneDetails, compareTemplate); // Transcript ID matches => count
						} else {
							// Is ID from gene?
							Gene gene = (Gene) markerInt.findParent(Gene.class);
							if ((gene != null) && (gene.getId().equals(id))) regionsAddHit(hits, markerInt, marker, showGeneDetails, compareTemplate); // Gene ID matches => count
						}
					}
				}
			}
		}

		if (!hitChromo) throw new RuntimeException("ERROR: Out of chromosome range. " + marker);
		return hits;
	}

	/**
	 * Add into to a hash
	 * @param hits
	 * @param marker
	 * @param hit2add
	 * @param showGeneDetails
	 * @param compareTemplate
	 */
	void regionsAddHit(HashSet<String> hits, Marker hit2add, Marker marker, boolean showGeneDetails, boolean compareTemplate) {
		String hitStr = hit2add.getClass().getSimpleName();

		if (compareTemplate) {
			Gene gene = (Gene) hit2add.findParent(Gene.class);
			if (gene != null) hitStr += (hit2add.isStrandPlus() == marker.isStrandPlus()) ? "_TEMPLATE_STRAND" : "_NON_TEMPLATE_STRAND";
		}

		if (showGeneDetails && (hit2add instanceof Gene)) {
			Gene gene = (Gene) hit2add;
			hitStr += "[" + gene.getBioType() + ", " + gene.getGeneName() + ", " + (gene.isProteinCoding() ? "protein" : "not-protein") + "]";
		}

		hits.add(hitStr); // Add marker name to the list
	}

	/**
	 * Remove all non-canonical transcripts
	 */
	public void removeNonCanonical() {
		for (Gene g : genome.getGenes())
			g.removeNonCanonical();
	}

	/**
	 * Remove all unverified transcripts
	 */
	public void removeUnverified() {
		for (Gene g : genome.getGenes())
			g.removeUnverified();
	}

	/**
	 * Remove all transcripts that are NOT in the list
	 * @return : Number of transcripts removed
	 */
	public int retainAllTranscripts(Set<String> trIds) {
		int total = 0;
		for (Gene g : genome.getGenes())
			total += g.keepTranscripts(trIds);
		return total;
	}

	/**
	 * Remove all transcripts that are NOT in the list
	 * @return : Number of transcripts removed
	 */
	public int retainTranscriptsProtein() {
		int total = 0;
		for (Gene g : genome.getGenes())
			total += g.keepTranscriptsProtein();
		return total;
	}

	/**
	 * Save predictor to a binary file (specified by the configuration)
	 */
	public void save(Config config) {
		String databaseFile = config.getFileSnpEffectPredictor();
		MarkerSerializer markerSerializer = new MarkerSerializer();
		markerSerializer.save(databaseFile, this);
	}

	public void setSpliceRegionExonSize(int spliceRegionExonSize) {
		this.spliceRegionExonSize = spliceRegionExonSize;
	}

	public void setSpliceRegionIntronMax(int spliceRegionIntronMax) {
		this.spliceRegionIntronMax = spliceRegionIntronMax;
	}

	public void setSpliceRegionIntronMin(int spliceRegionIntronMin) {
		this.spliceRegionIntronMin = spliceRegionIntronMin;
	}

	public void setSpliceSiteSize(int spliceSiteSize) {
		this.spliceSiteSize = spliceSiteSize;
	}

	public void setUpDownStreamLength(int upDownStreamLength) {
		this.upDownStreamLength = upDownStreamLength;
	}

	public void setUseChromosomes(boolean useChromosomes) {
		this.useChromosomes = useChromosomes;
	}

	public int size() {
		if (intervalForest == null) return 0;
		return intervalForest.size();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(genome.getVersion() + "\n");
		for (Chromosome chr : genome)
			sb.append(chr + "\n");
		sb.append(genome.getGenes());
		return sb.toString();
	}

	/**
	 * Predict the effect of a variant
	 */
	public VariantEffects variantEffect(Variant variant) {
		return variantEffect(variant, null);
	}

	/**
	 * Predict the effect of a variant
	 * @param variant : Sequence change
	 * @param variantRef : Before analyzing results, we have to change markers using variantrRef to create a new reference 'on the fly'
	 */
	public VariantEffects variantEffect(Variant variant, Variant variantRef) {
		VariantEffects variantEffects = new VariantEffects(variant, variantRef);

		//---
		// Chromosome missing?
		//---
		if (Config.get().isErrorOnMissingChromo() && isChromosomeMissing(variant)) {
			variantEffects.addErrorWarning(ErrorWarningType.ERROR_CHROMOSOME_NOT_FOUND);
			return variantEffects;
		}

		//---
		// Check that this is not a huge deletion.
		// Huge deletions would crash the rest of the algorithm, so we need to stop them here.
		//---
		// Get chromosome
		if (variant.isDel()) {
			String chromoName = variant.getChromosomeName();
			Chromosome chr = genome.getChromosome(chromoName);
			double ratio = (chr.size() > 0 ? variant.size() / ((double) chr.size()) : 0);
			if (variant.size() > HUGE_DELETION_SIZE_THRESHOLD || ratio > HUGE_DELETION_RATIO_THRESHOLD) {
				variantEffects.addEffect(chr, EffectType.CHROMOSOME_LARGE_DELETION, "");
				return variantEffects;
			}
		}

		//---
		// Query interval tree: Which intervals does variant intersect?
		//---
		Markers intersects = query(variant);

		// Show all results
		boolean hitChromo = false, hitSomething = false;
		if (intersects.size() > 0) {
			for (Marker marker : intersects) {
				if (marker instanceof Chromosome) hitChromo = true; // Do we hit any chromosome?
				else { // Analyze all markers
					marker.variantEffect(variant, variantRef, variantEffects);
					hitSomething = true;
				}
			}
		}

		// Any errors or intergenic (i.e. did not hit any gene)
		if (!hitChromo) {
			if (Config.get().isErrorChromoHit()) variantEffects.addErrorWarning(ErrorWarningType.ERROR_OUT_OF_CHROMOSOME_RANGE);
		} else if (!hitSomething) {
			if (Config.get().isOnlyRegulation()) {
				variantEffects.addEffect(null, EffectType.NONE, "");
			} else {
				variantEffects.addEffect(null, EffectType.INTERGENIC, "");
			}
		}

		return variantEffects;
	}
}
