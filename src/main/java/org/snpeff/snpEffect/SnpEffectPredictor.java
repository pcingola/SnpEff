package org.snpeff.snpEffect;

import org.snpeff.binseq.GenomicSequences;
import org.snpeff.interval.*;
import org.snpeff.interval.tree.IntervalForest;
import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.util.Gpr;

import java.io.Serializable;
import java.util.*;

/**
 * Predicts effects of SNPs
 * <p>
 * Note: Actually tries to predict any kind of SeqChange, not only SNPs . It is called SnpEffectPredictor for 'historical reasons'.
 *
 * @author pcingola
 */
public class SnpEffectPredictor implements Serializable {
    public static final int DEFAULT_UP_DOWN_LENGTH = 5000;
    public static final int SMALL_VARIANT_SIZE_THRESHOLD = 10; // Number of bases for a variant to be considered 'small'
    private static final long serialVersionUID = 4519418862303325081L;
    boolean useChromosomes = true;

    boolean debug;
    int upDownStreamLength = DEFAULT_UP_DOWN_LENGTH;
    int spliceSiteSize = SpliceSite.CORE_SPLICE_SITE_SIZE;
    int spliceRegionExonSize = SpliceSite.SPLICE_REGION_EXON_SIZE;
    int spliceRegionIntronMin = SpliceSite.SPLICE_REGION_INTRON_MIN;
    int spliceRegionIntronMax = SpliceSite.SPLICE_REGION_INTRON_MAX;
    Genome genome;
    Markers markers; // All other markers are stored here (e.g. custom markers, intergenic, etc.)
    IntervalForest intervalForest; // Interval forest by chromosome name

    public SnpEffectPredictor(Genome genome) {
        this.genome = genome;
        markers = new Markers();
    }

    /**
     * Load predictor from a binary file
     */
    public static SnpEffectPredictor load(Config config) {
        String snpEffPredFile = config.getFileSnpEffectPredictor();

        // Sanity check
        if (!Gpr.canRead(snpEffPredFile))
            throw new RuntimeException("\tERROR: Cannot read file '" + snpEffPredFile + "'.\n\tYou can try to download the database by running the following command:\n\t\tjava -jar snpEff.jar download " + config.getGenome().getVersion() + "\n");

        // Load markers from file
        MarkerSerializer ms = new MarkerSerializer(config.getGenome());
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

    /**
     * Add a gene interval
     */
    public void add(Gene gene) {
        genome.getGenes().add(gene);
    }

    /**
     * Add a marker
     * <p>
     * Note: Markers have to be added BEFORE building the interval trees.
     * Interval trees are built the first time you call snpEffect(snp) method.
     */
    public void add(Marker marker) {
        markers.add(marker);
    }

    /**
     * Add a set of markers
     */
    public void addAll(Markers markersToAdd) {
        markers.addAll(markersToAdd);
    }

    /**
     * Create interval trees (forest)
     */
    public void buildForest() {
        intervalForest = new IntervalForest();
        intervalForest.setDebug(debug);

        // Add all chromosomes to forest
        if (useChromosomes) {
            for (Chromosome chr : genome)
                intervalForest.add(chr);
        }

        // In a circular genome, a gene can have negative coordinates or crosses
        // over chromosome end. These genes are mirrored to the opposite end of
        // the chromosome so that they can be referenced by both circular coordinates.
        genome.getGenes().createCircularGenes();

        // Add all genes to forest
        for (Gene gene : genome.getGenes())
            intervalForest.add(gene);

        //---
        // Create (and add) up-down stream, splice sites, intergenic, etc
        //---
        markers.add(createGenomicRegions());

        // Mark canonical transcripts
        canonical();

        // Add all 'markers' to forest (includes custom intervals)
        intervalForest.add(markers);

        // Build interval forest
        intervalForest.build();

        // Build gene-dependent
        buildPerGene();
    }

    /**
     * Build 'per gene' information
     */
    void buildPerGene() {
        for (Gene gene : genome.getGenes())
            gene.buildPerGene();
    }

    /**
     * Make sure all genes have canonical transcripts
     */
    void canonical() {
        for (Gene g : genome.getGenes())
            g.canonical();
    }

    /**
     * Count number of transcripts
     */
    public int countTranscripts() {
        int total = 0;
        for (Gene g : genome.getGenes())
            total += g.numChilds();
        return total;
    }

    /**
     * Create (and add) up-down stream, splice sites, intergenic, etc
     */
    public Markers createGenomicRegions() {
        Markers markers = new Markers();

        // Add up-down stream intervals
        markers.addAll(genome.getGenes().createUpDownStream(upDownStreamLength));

        // Add splice site intervals
        genome.getGenes().createSpliceSites(spliceSiteSize, spliceRegionExonSize, spliceRegionIntronMin, spliceRegionIntronMax);

        // Intergenic markers
        markers.addAll(genome.getGenes().createIntergenic());

        return markers;
    }

    /**
     * Filter transcripts by TSL
     */
    public void filterTranscriptSupportLevel(TranscriptSupportLevel maxTsl) {
        for (Gene g : genome.getGenes())
            g.filterTranscriptSupportLevel(maxTsl);
    }

    /**
     * Obtain a gene by geneId
     */
    public Gene getGene(String geneId) {
        return genome.getGenes().get(geneId);
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

    public void setSpliceRegionExonSize(int spliceRegionExonSize) {
        this.spliceRegionExonSize = spliceRegionExonSize;
    }

    public int getSpliceRegionIntronMax() {
        return spliceRegionIntronMax;
    }

    public void setSpliceRegionIntronMax(int spliceRegionIntronMax) {
        this.spliceRegionIntronMax = spliceRegionIntronMax;
    }

    public int getSpliceRegionIntronMin() {
        return spliceRegionIntronMin;
    }

    public void setSpliceRegionIntronMin(int spliceRegionIntronMin) {
        this.spliceRegionIntronMin = spliceRegionIntronMin;
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

    public void setUpDownStreamLength(int upDownStreamLength) {
        this.upDownStreamLength = upDownStreamLength;
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
        return !intervalForest.hasTree(chrName);

        // OK, we have the chromosome
    }

    /**
     * Remove all transcripts that are NOT in the list
     *
     * @return : Number of transcripts removed
     */
    public int keepTranscriptsProteinCoding() {
        int total = 0;
        for (Gene g : genome.getGenes())
            total += g.keepTranscriptsProteinCoding();
        return total;
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
        return marker.query(intervalForest);
    }

    /**
     * Find closest gene to this marker
     * <p>
     * In case more than one 'closest' gene is
     * found (e.g. two or more genes at the
     * same distance). The following rules
     * apply:
     * <p>
     * i) If many genes have the same 'closest
     * distance', coding genes are preferred.
     * <p>
     * ii) If more than one coding gene has the
     * same 'closet distance', a random gene
     * is returned.
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
                int end = inputInterval.getEndClosed() + extend;
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
     */
    public Markers queryDeep(Marker marker) {
        if (Config.get().isErrorOnMissingChromo() && isChromosomeMissing(marker))
            throw new RuntimeException("Chromosome missing for marker: " + marker);

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

        if (!hitChromo && Config.get().isErrorChromoHit())
            throw new RuntimeException("ERROR: Out of chromosome range. " + marker);
        return hits;
    }

    /**
     * Name of the regions hit by a marker
     *
     * @return A set of region names
     */
    public Set<String> regions(Marker marker, boolean showGeneDetails, boolean compareTemplate) {
        return regions(marker, showGeneDetails, compareTemplate, null);
    }

    /**
     * Name of the regions hit by a marker
     *
     * @param id : Only use genes or transcripts matching this ID (null for any)
     */
    public Set<String> regions(Marker marker, boolean showGeneDetails, boolean compareTemplate, String id) {
        if (Config.get().isErrorOnMissingChromo() && isChromosomeMissing(marker))
            throw new RuntimeException("Chromosome missing for marker: " + marker);

        boolean hitChromo = false;
        HashSet<String> hits = new HashSet<>();

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
                                    if (utr.intersects(marker))
                                        regionsAddHit(hits, utr, marker, showGeneDetails, compareTemplate);

                                // Does it intersect an exon?
                                for (Exon ex : tr)
                                    if (ex.intersects(marker))
                                        regionsAddHit(hits, ex, marker, showGeneDetails, compareTemplate);

                                // Does it intersect an intron?
                                for (Intron intron : tr.introns())
                                    if (intron.intersects(marker))
                                        regionsAddHit(hits, intron, marker, showGeneDetails, compareTemplate);
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
                            if ((gene != null) && (gene.getId().equals(id)))
                                regionsAddHit(hits, markerInt, marker, showGeneDetails, compareTemplate); // Gene ID matches => count
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
     */
    void regionsAddHit(HashSet<String> hits, Marker hit2add, Marker marker, boolean showGeneDetails, boolean compareTemplate) {
        String hitStr = hit2add.getClass().getSimpleName();

        if (compareTemplate) {
            Gene gene = (Gene) hit2add.findParent(Gene.class);
            if (gene != null)
                hitStr += (hit2add.isStrandPlus() == marker.isStrandPlus()) ? "_TEMPLATE_STRAND" : "_NON_TEMPLATE_STRAND";
        }

        if (showGeneDetails && (hit2add instanceof Gene)) {
            Gene gene = (Gene) hit2add;
            hitStr += "[" + gene.getBioType() + ", " + gene.getGeneName() + ", " + (gene.isProteinCoding() ? "protein" : "not-protein") + "]";
        }

        hits.add(hitStr); // Add marker name to the list
    }

    /**
     * Remove all non-canonical transcripts
     * If a file is provided, read "Gene => canonical_transcript" mapping
     * from file
     */
    public void removeNonCanonical(String canonFile) {
        Map<String, String> geneCanonTr = new HashMap<>();

        // Any gene mapping file?
        if (canonFile != null && !canonFile.isEmpty()) {
            // Read "gene -> trId" map form file
            String lines = Gpr.readFile(canonFile).trim();
            if (lines.isEmpty()) throw new RuntimeException("Empty or missing file '" + canonFile + "'");

            // Parse lines and store in hash
            for (String line : lines.split("\n")) {
                String[] fields = line.split("\t");
                String geneId = fields[0].trim();
                String trId = fields[1].trim();
                geneCanonTr.put(geneId, trId);
            }
        }

        // Remove non-canonical transcripts
        Set<String> done = new HashSet<>(geneCanonTr.keySet());
        for (Gene g : genome.getGenes()) {
            String geneId = g.getId();
            String trId = geneCanonTr.get(geneId);
            g.removeNonCanonical(trId);
            done.remove(geneId);
        }

        // Check that all genes have been added
        if (done.size() > 0) {
            StringBuilder sb = new StringBuilder();
            done.forEach(g -> sb.append(g + " "));
            throw new RuntimeException("Canonical gene list file '" + canonFile + "' has gene Ids that do not match any gene: " + sb);
        }
    }

    /**
     * Remove all unverified transcripts
     *
     * @return true if ALL genes had ALL transcripts removed (i.e. something
     * went wrong, like in cases where no transcript was checked during the
     * building process)
     */
    public boolean removeUnverified() {
        boolean allRemoved = true;
        for (Gene g : genome.getGenes())
            allRemoved &= g.removeUnverified();

        return allRemoved;
    }

    /**
     * Remove all transcripts that are NOT in the list
     *
     * @return : Number of transcripts removed
     */
    public int retainAllTranscripts(Set<String> trIds) {
        int total = 0;
        for (Gene g : genome.getGenes())
            total += g.keepTranscripts(trIds);
        return total;
    }

    /**
     * Save predictor to a binary file (specified by the configuration)
     */
    public void save(Config config) {
        // Save genome and markers
        String databaseFile = config.getFileSnpEffectPredictor();
        save(databaseFile);

        // Save genomic sequences
        GenomicSequences gs = genome.getGenomicSequences();
        gs.setVerbose(config.isVerbose());
        gs.save(config);
    }

    /**
     * Save predictor to a binary file
     */
    public void save(String fileName) {
        // Add al markers
        Markers markersToSave = new Markers();
        markersToSave.add(genome);

        for (Chromosome chr : genome)
            markersToSave.add(chr);

        for (Gene g : genome.getGenes())
            markersToSave.add(g);

        markersToSave.add(getMarkers());

        // Save markers to file
        markersToSave.save(fileName);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setSpliceSiteSize(int spliceSiteSize) {
        this.spliceSiteSize = spliceSiteSize;
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
        VariantEffects variantEffects = new VariantEffects();

        // Chromosome missing?
        if (Config.get().isErrorOnMissingChromo() && isChromosomeMissing(variant)) {
            variantEffects.addErrorWarning(variant, ErrorWarningType.ERROR_CHROMOSOME_NOT_FOUND);
            return variantEffects;
        }

        // Translocations require special treatment
        // (e.g. they have two intersections points instead of one)
        if (variant.isBnd()) {
            Markers intersects = query(variant);
            variantEffectBnd(variant, variantEffects, intersects);
            return variantEffects;
        }

        // Is this a structural variant? Large structural variants (e.g. involving more than
        // one gene) may require to calculate effects by using all involved genes
        // For some variants we require to be 'N' bases apart (translocations
        // are assumed always involve large genomic regions)
        boolean structuralVariant = variant.isStructural() && (variant.size() > SMALL_VARIANT_SIZE_THRESHOLD);

        Markers intersects = null;

        // Structural variants?
        boolean structuralHuge = structuralVariant && variant.isStructuralHuge();
        if (structuralHuge) {
            // Large variants could make the query results huge and slow down
            // the algorithm, so we stop here
            // Note: Translocations (BND) only intercept two loci, so this
            //       issue does not apply.
            intersects = variantEffectStructuralLarge(variant, variantEffects);
        } else {
            // Query interval tree: Which intervals does variant intersect?
            intersects = query(variant);
        }

        // In case of large structural variants, we need to check the number of genes
        // involved. If more than one, then we need a different approach (e.g. taking
        // into account all genes involved to calculate fusions)");
        if (structuralVariant) {
            // Are we done?
            if (variantEffectStructural(variant, variantEffects, intersects)) return variantEffects;
        }

        // Calculate variant effect for each query result
        variantEffect(variant, variantEffects, intersects);

        return variantEffects;
    }

    /**
     * Calculate variant effect for each marker in 'intersect'
     */
    protected void variantEffect(Variant variant, VariantEffects variantEffects, Markers intersects) {
        // Analyze effects for all markers that intercept
        boolean hitChromo = false, hitSomething = false;
        List<Marker> deferredMarkers = null;
        for (Marker marker : intersects) {
            if (marker instanceof Chromosome) hitChromo = true; // Do we hit any chromosome?
            else if (marker.isDeferredAnalysis()) {
                // Deferred analysis markers must be analyzed after 'standard' ones because their impact depends on other results
                // For instance, a NextProt marker's impact would be different if the variant is synonymous or non-synonymous
                if (deferredMarkers == null) deferredMarkers = new ArrayList<>();
                deferredMarkers.add(marker);
            } else {
                // Analyze all markers
                if (variant.isNonRef()) marker.variantEffectNonRef(variant, variantEffects);
                else marker.variantEffect(variant, variantEffects);

                hitSomething = true;
            }
        }

        // Any 'deferred analysis' markers must be analyzed after the 'standard' (i.e. non-deferred) ones
        if (deferredMarkers != null) {
            for (Marker marker : deferredMarkers)
                marker.variantEffect(variant, variantEffects);
        }

        // Any errors or intergenic (i.e. did not hit any gene)
        if (!hitChromo) {
            // Special case: Insertion right after chromosome's last base
            Chromosome chr = genome.getChromosome(variant.getChromosomeName());
            if (variant.isIns() && variant.getStart() == (chr.getEndClosed() + 1)) {
                // This is a chromosome extension
                variantEffects.add(variant, null, EffectType.CHROMOSOME_ELONGATION, "");
            } else if (Config.get().isErrorChromoHit()) {
                variantEffects.addErrorWarning(variant, ErrorWarningType.ERROR_OUT_OF_CHROMOSOME_RANGE);
            }
        } else if (!hitSomething) {
            if (Config.get().isOnlyRegulation()) {
                variantEffects.add(variant, null, EffectType.NONE, "");
            } else {
                variantEffects.add(variant, null, EffectType.INTERGENIC, "");
            }
        }
    }

    /**
     * Calculate translocations variant effects
     */
    void variantEffectBnd(Variant variant, VariantEffects variantEffects, Markers intersects) {
        // Create a new variant effect for structural variants, then calculate all transcript fusions
        VariantEffectStructural veff = new VariantEffectStructural(variant, intersects);

        // Do we have a fusion event?
        List<VariantEffect> veffFusions = veff.fusions();
        if (veffFusions != null) {
            for (VariantEffect veffFusion : veffFusions)
                variantEffects.add(veffFusion);
        }
    }

    /**
     * Calculate structural variant effects taking into account all involved genes
     *
     * @return A list of intervals that need to be further analyzed
     * or 'null' if no further gene-by-gene analysis is required
     */
    boolean variantEffectStructural(Variant variant, VariantEffects variantEffects, Markers intersects) {
        // Any variant effects added?
        boolean added = false;

        // Create a new variant effect for structural variants, add effect (if any)
        VariantEffectStructural veff = new VariantEffectStructural(variant, intersects);
        if (veff.getEffectType() != EffectType.NONE) {
            variantEffects.add(veff);
            added = true;
        }

        // Do we have a fusion event?
        List<VariantEffect> veffFusions = veff.fusions();
        if (veffFusions != null && !veffFusions.isEmpty()) {
            for (VariantEffect veffFusion : veffFusions) {
                added = true;
                variantEffects.add(veffFusion);
            }
        }

        // In some cases we want to annotate all overlapping genes
        if (variant.isDup() || variant.isDel()) return false;

        // If variant effects were added, there is no need for further analysis
        return added;
    }

    /**
     * Add large structural variant effects
     */
    Markers variantEffectStructuralLarge(Variant variant, VariantEffects variantEffects) {
        EffectType eff, effGene, effTr, effExon, effExonPartial;

        switch (variant.getVariantType()) {
            case DEL:
                eff = EffectType.CHROMOSOME_LARGE_DELETION;
                effGene = EffectType.GENE_DELETED;
                effTr = EffectType.TRANSCRIPT_DELETED;
                effExon = EffectType.EXON_DELETED;
                effExonPartial = EffectType.EXON_DELETED_PARTIAL;
                break;

            case DUP:
                eff = EffectType.CHROMOSOME_LARGE_DUPLICATION;
                effGene = EffectType.GENE_DUPLICATION;
                effTr = EffectType.TRANSCRIPT_DUPLICATION;
                effExon = EffectType.EXON_DUPLICATION;
                effExonPartial = EffectType.EXON_DUPLICATION_PARTIAL;
                break;

            case INV:
                eff = EffectType.CHROMOSOME_LARGE_INVERSION;
                effGene = EffectType.GENE_INVERSION;
                effTr = EffectType.TRANSCRIPT_INVERSION;
                effExon = EffectType.EXON_INVERSION;
                effExonPartial = EffectType.EXON_INVERSION_PARTIAL;
                break;

            default:
                throw new RuntimeException("Unimplemented option for variant type " + variant.getVariantType());
        }

        // Add effect
        variantEffects.add(variant, variant.getChromosome(), eff, "");

        // Add detailed effects for genes & transcripts
        return variantEffectStructuralLargeGenes(variant, variantEffects, effGene, effTr, effExon, effExonPartial);
    }

    /**
     * Add large structural variant effects: Genes and transcripts
     */
    Markers variantEffectStructuralLargeGenes(Variant variant, VariantEffects variantEffects, EffectType effGene, EffectType effTr, EffectType effExon, EffectType effExonPartial) {
        Markers intersect = new Markers();

        // Check all genes in the genome
        for (Gene g : genome.getGenes()) {
            // Does the variant affect the gene?
            if (variant.intersects(g)) {
                intersect.add(g);
                variantEffects.add(variant, g, effGene, "");

                // Does the variant affect this transcript?
                for (Transcript tr : g) {
                    // Variant affects the whole transcript?
                    if (variant.includes(tr)) {
                        intersect.add(tr);
                        variantEffects.add(variant, tr, effTr, "");
                    } else if (variant.intersects(tr)) {
                        intersect.add(tr);

                        // Variant affects part of the transcript
                        // Add effects for each exon
                        for (Exon ex : tr) {
                            if (variant.includes(ex)) {
                                variantEffects.add(variant, ex, effExon, "");
                            } else if (variant.intersects(ex)) {
                                variantEffects.add(variant, ex, effExonPartial, "");
                            }
                        }
                    }
                }
            }
        }

        return intersect;
    }
}
