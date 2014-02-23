package ca.mcgill.mcb.pcingola.snpEffect.commandLine;

import java.util.HashSet;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.LossOfFunction;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.stats.CountByType;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfLof;
import ca.mcgill.mcb.pcingola.vcf.VcfNmd;

/**
 * Command line: Test
 * 
 * Note: Used for testing weird stuff
 * 
 * @author pcingola
 */
public class SnpEffCmdTest2 extends SnpEff {

	public static final String VARIANTS_IN_GENES = "_VARAINTS_IN_GENES";
	public static final String VARIANTS = "_VARAINTS";
	public static final String BIOTYPE_SKIPPED = "_BIOTYPE_SKIPPED";
	public static final double MIN_PERCENT_TRANSCRIPTS_AFFECTED = 0.0;

	public static final int SHOW_EVERY = 10000;

	boolean onlyProteinCodingTranscripts; // Use only protein coding transcripts
	boolean useClosestGene; // Use closest gene (if gene is not found in EFF entry)
	boolean doNotUseAF50; // Filter out VCF entries if AF > 50%
	boolean keyMafCategory; // Add MAF category to key { COMMON, LOW< RARE }
	boolean keyPrivate; // Add "PRIVATE" flag to info
	boolean keyId; // Add "ID" flag to info

	SnpEffectPredictor snpEffectPredictor;
	String genesFile;
	String vcfFile;
	CountByType count; // Counter
	CountByType countByEffect; // Count raw number of effects
	CountByType countByVariant; // Count each effect once per variant
	CountByType countByEffByGene; // Count number of effects for each gene
	CountByType countByGene;// Count each effect once per gene
	HashSet<String> genes; // Only select effect in these genes

	public SnpEffCmdTest2() {
		super();
		countByEffByGene = new CountByType();
		countByVariant = new CountByType();
		countByGene = new CountByType();
		countByEffect = new CountByType();
		count = new CountByType();
	}

	/**
	 * Analyze vcf entry
	 * @param ve
	 */
	void analyze(VcfEntry ve) {
		boolean inGenes = false;
		HashSet<String> effectsByVariant = new HashSet<String>();
		HashSet<String> effectsByGene = new HashSet<String>();

		// We might ignore AF > 0.5 because 
		// Some algorithms are based on minor allele frequencies. So when 
		// calculated, REF and ALT are swapped. This means that the EFF field 
		// which describes REF->ALT change is not describing the change 
		// used by the algorithm (ALT->REF)
		if (doNotUseAF50 && (ve.getInfoFloat("AF") > 0.5)) return;

		// Add this info after all keys
		String keyPost = "";
		if (keyMafCategory) keyPost += "\t" + ve.alleleFrequencyType().toString();
		if (keyPrivate) keyPost += "\t" + (ve.getInfoFlag(VcfEntry.VCF_INFO_PRIVATE) ? VcfEntry.VCF_INFO_PRIVATE : "");
		if (keyId) keyPost += "\t" + ((ve.getId() == null) || ve.getId().isEmpty() ? "" : "ID");

		String geneClosest = null;

		//---
		// Parse Effect
		//---
		for (VcfEffect veff : ve.parseEffects()) {

			// Do not process is there are errors or warnings
			if (veff.getErrorsOrWarning() != null) {
				count.inc("ERRORS_OR_WARNINGS" + keyPost);
				continue;
			}

			String gene = veff.getGene();
			if (genes != null) {
				// No gene info? Nothing to do
				if (gene == null || gene.isEmpty()) {
					count.inc("NO_GENE" + keyPost);
					continue;
				}

				// Gene Info does not match? Nothing to do
				if (!genes.contains(gene)) {
					count.inc("NO_GENE_SET" + keyPost);
					continue;
				}

				// Not a protein coding transcript? Skip
				if (onlyProteinCodingTranscripts && ((veff.getBioType() == null) || !veff.getBioType().equals("protein_coding"))) {
					count.inc(BIOTYPE_SKIPPED + "_" + veff.getBioType() + keyPost);
					continue;
				}

				inGenes = true;
			} else if (gene == null || gene.isEmpty()) {
				geneClosest = useClosestGene ? findClosestGene(ve) : "";
				if (!geneClosest.isEmpty()) count.inc("GENE_CLOSEST" + keyPost);
				gene = geneClosest; // Use closest gene 
			}

			// Count by effect
			String key = veff.getEffect().toString();
			if (veff.getEffect() == EffectType.REGULATION) key += "[" + veff.getBioType() + ":" + veff.getEffectDetails() + "]";
			else if (veff.getEffectDetails() != null && !veff.getEffectDetails().isEmpty()) key += "[" + veff.getEffectDetails() + "]";
			effectsByVariant.add(key + keyPost);
			effectsByGene.add(gene + "\t" + key + keyPost);
			countByEffect.inc(key + keyPost);
		}

		//---
		// Parse LOF 
		//---
		for (VcfLof lof : ve.parseLof()) {
			// No gene info? Nothing to do
			String gene = lof.getGeneName();
			if (gene == null || gene.isEmpty()) continue;

			// Gene Info does not match? Nothing to do
			if ((genes != null) && !genes.contains(gene)) continue;

			inGenes = true;
			if (lof.getPercentAffected() >= MIN_PERCENT_TRANSCRIPTS_AFFECTED) {
				effectsByGene.add(gene + "\t" + LossOfFunction.VCF_INFO_LOF_NAME + keyPost);
				effectsByVariant.add(LossOfFunction.VCF_INFO_LOF_NAME + keyPost);
				countByEffect.inc(LossOfFunction.VCF_INFO_LOF_NAME + keyPost);
			}
		}
		//---
		// Parse NMD 
		//---
		for (VcfNmd nmd : ve.parseNmd()) {
			// No gene info? Nothing to do
			String gene = nmd.getGeneName();
			if (gene == null || gene.isEmpty()) continue;

			// Gene Info does not match? Nothing to do
			if ((genes != null) && !genes.contains(gene)) continue;

			inGenes = true;
			if (nmd.getPercentAffected() >= MIN_PERCENT_TRANSCRIPTS_AFFECTED) {
				effectsByGene.add(gene + "\t" + LossOfFunction.VCF_INFO_NMD_NAME + keyPost);
				effectsByVariant.add(LossOfFunction.VCF_INFO_NMD_NAME + keyPost);
				countByEffect.inc(LossOfFunction.VCF_INFO_NMD_NAME + keyPost);
			}
		}

		// ACAT & NCCAT Scores (they appear once per variant)
		String acat = ve.getInfo(SnpEffCmdAcat.ACAT);
		if (acat != null) {
			String acatFields[] = acat.split(",");
			for (String af : acatFields) {
				String afs[] = af.split(":");
				String gene = afs[0];
				String acatScore = afs[2];

				effectsByGene.add(gene + "\t_" + SnpEffCmdAcat.ACAT + "_" + acatScore + keyPost);
				effectsByVariant.add("_" + SnpEffCmdAcat.ACAT + "_" + acatScore + keyPost);
			}
		}

		// NCCAT is just once per variant
		String nccat = ve.getInfo(SnpEffCmdAcat.NCCAT);
		if (nccat != null) {
			countByVariant.inc("_" + SnpEffCmdAcat.NCCAT + "_" + nccat + keyPost);

			// These variants don't have gene information
			if (useClosestGene) {
				if (geneClosest == null) geneClosest = findClosestGene(ve);
				effectsByGene.add(geneClosest + "\t_" + SnpEffCmdAcat.NCCAT + "_" + nccat + keyPost);
			}
		}

		// Count once per variant
		for (String eff : effectsByVariant)
			countByVariant.inc(eff);

		// Count effects by gene
		for (String eff : effectsByGene)
			countByEffByGene.inc(eff);

		// Count total number of variants
		count.inc(VARIANTS + keyPost);
		if (inGenes) count.inc(VARIANTS_IN_GENES + keyPost); // Count if it is in genes
	}

	/**
	 * Find closes gene name
	 * @param queryMarker
	 * @return
	 */
	String findClosestGene(Marker queryMarker) {
		Gene gene = snpEffectPredictor.queryClosestGene(queryMarker);
		return gene != null ? gene.getGeneName() : "";
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args.length < 2) usage(null);

		for (int idx = 0; idx < args.length; idx++) {
			String arg = args[idx];

			if (isOpt(arg)) {
				if (arg.equals("-id")) keyId = true;
				else if (arg.equals("-maf")) keyMafCategory = true;
				else if (arg.equals("-private")) keyPrivate = true;
				else if (arg.equals("-closest")) useClosestGene = true;
				else if (arg.equals("-noAf50")) doNotUseAF50 = true;
				else if (arg.equals("-prot")) onlyProteinCodingTranscripts = true;
				else usage("Unknown opton '" + arg + "'");
			} else if ((genomeVer == null) || genomeVer.isEmpty()) genomeVer = args[idx];
			else if (vcfFile == null) vcfFile = args[idx];
			else if (genesFile == null) genesFile = args[idx];
		}

		if (genomeVer == null) usage("Missing genome version");
		if (vcfFile == null) usage("Missing VCF file");
	}

	/**
	 * Print a counter using a label on each line
	 * @param label
	 * @param countByType
	 */
	void print(String label, CountByType countByType) {
		System.out.println(label + "\teff\tcount");
		for (String type : countByType.keysSorted())
			System.out.println(label + "\t" + type + "\t" + countByType.get(type));

	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		//---
		// Load database, build tree
		//---
		loadConfig(); // Read config file

		if (verbose) Timer.showStdErr("Loading predictor...");
		config.loadSnpEffectPredictor();
		if (verbose) Timer.showStdErr("done");

		if (verbose) Timer.showStdErr("Building interval forest...");
		snpEffectPredictor = config.getSnpEffectPredictor();
		snpEffectPredictor.buildForest();
		if (verbose) Timer.showStdErr("done");

		//---
		// Load genes
		//---
		if (genesFile != null) {
			genes = new HashSet<String>();
			if (verbose) Timer.showStdErr("Loading genes from '" + genesFile + "'");
			for (String gene : Gpr.readFile(genesFile).split("\n"))
				genes.add(gene.trim());
			if (verbose) Timer.showStdErr("Done. Genes added : " + genes.size());
		}

		//---
		// Process input file
		//---
		if (verbose) Timer.showStdErr("Counting effect on input: " + vcfFile);
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		int i = 1;
		for (VcfEntry ve : vcf) {
			analyze(ve);

			if (verbose) Gpr.showMark(i++, SHOW_EVERY);
		}

		//---
		// Calculate 'once per gene' counters
		//---
		for (String key : countByEffByGene.keySet()) {
			if (countByEffByGene.get(key) > 0) { // This should always be true
				String keySplit[] = key.split("\t", 2);

				if (keySplit.length > 1) {
					String eff = keySplit[1];
					countByGene.inc(eff);
				}
			} else throw new RuntimeException("This should never happen!");
		}

		//---
		// Show output
		//---
		System.out.println("# General Numbers");
		if (genes != null) System.out.println("GENES\t" + genes.size());
		print("", count);
		System.out.println("#");
		System.out.println("# Number of effects (raw counts)");
		print("count_effect", countByEffect);
		System.out.println("#");
		System.out.println("# Number of effects per gene (number of effects for each gene)");
		print("count_effect_by_gene", countByEffByGene);
		System.out.println("#");
		System.out.println("# Number of effects per variant (i.e. each effect is counted only once per variant)");
		print("count_by_variant", countByVariant);
		System.out.println("#");
		System.out.println("# Number of genes for each effect (i.e. each effect is counted only once per gene)");
		print("count_by_gene", countByGene);
		return true;
	}

	/**
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff test [options] genomeVer file.vcf [genes.txt]");
		System.err.println("Options:");
		System.err.println("\t-closest  : Use closest gene (if gene is not found in EFF entry)");
		System.err.println("\t-id       : Add ID flag to info");
		System.err.println("\t-maf      : Add MAF category to info { COMMON, LOW, RARE }");
		System.err.println("\t-noAf50   : Filter out VCF entries if AF > 50%");
		System.err.println("\t-private  : Add PRIVATE flag to info");
		System.err.println("\t-prot     : Use only protein coding transcripts");
		System.exit(-1);
	}

}
