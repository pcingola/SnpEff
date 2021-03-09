package org.snpeff.snpEffect.commandLine;

import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.snpEffect.VariantEffect.EffectImpact;
import org.snpeff.stats.CountByType;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfHeaderInfo;
import org.snpeff.vcf.VcfInfoType;

/**
 * ACAT: Create ACAT score for T2D project
 *
 * Note: This is just used to compile 'ACAT' score in T2D-GENES project, not
 * useful at all for general audience.
 *
 * @author pcingola
 */
public class SnpEffCmdAcat extends SnpEff {

	// Coding score category
	public static final String ACAT = "ACAT";

	// Non-coding INFO fields
	public static final String NCCAT = "NCCAT";
	public static final String NCACTIVITY = "NCACTIVITY"; // We don't have this information at the moment
	public static final String NCMARK = "NCMARK";
	public static final String NCFACTOR = "NCFACTOR";
	public static final String NCELEMENT = "NCELEMENT";
	public static final String NCCONS = "NCCONS";

	// Is this variant exclusive from T2D_GENES (i.e. not in dbSnp, 1000 Genomes or ESP)
	public static final String T2D_GENES = "T2D_GENES";

	// Conservation threshold
	public static final double CONSERVATION_THRESHOLD = 0.99;

	String vcfFile;
	int testNum;
	CountByType countByAcat, countByEff;

	public SnpEffCmdAcat() {
		super();
	}

	/**
	 * Calculate and add ACAT score on this entry
	 * @param ve
	 */
	void acat(VcfEntry ve) {
		boolean hasMotif = false, hasReg = false;
		int minAcatScore = Integer.MAX_VALUE; // Lower values mean more deleterious
		StringBuilder acat = new StringBuilder();
		String acatKey = null;
		int nccat = Integer.MAX_VALUE;
		StringBuilder ncmark = new StringBuilder();
		StringBuilder ncfactor = new StringBuilder();
		StringBuilder ncelement = new StringBuilder();

		// Get conservation score
		double cons = ve.getInfoFloat("PhastCons");
		if (Double.isNaN(cons)) cons = 0; // Not found?

		// Parse all effects
		for (VcfEffect veff : ve.getVcfEffects()) {

			if (verbose) {
				// Count by effect
				countByEff.inc(veff.getEffectType().toString());
				if (veff.getEffectDetails() != null && !veff.getEffectDetails().isEmpty()) countByEff.inc(veff.getEffectType() + "[" + veff.getEffectDetails() + "]");
			}

			// Do not process is there are errors or warnings
			if (veff.getErrorsWarning() != null) continue;

			// Impact andACAT score
			EffectImpact impact = veff.getImpact();
			int acatScore = impact.ordinal() + 1;

			String key = null;

			if (acatScore < 4) {
				//---
				// Coding scores (ACAT)
				//---

				// Is this a better score? => Remove old data
				if (acatScore < minAcatScore) {
					if (acat.length() > 0) acat = new StringBuilder();
				}

				// Append to ACAT score
				if (acatScore <= minAcatScore) {
					minAcatScore = acatScore;

					// Add as ACAT format
					String gene = veff.getGeneName() != null ? veff.getGeneName() : "";
					String trId = veff.getTranscriptId() != null ? veff.getTranscriptId() : "";
					if (acat.length() > 0) acat.append(",");
					acat.append(gene + ":" + trId + ":" + acatScore);

					acatKey = "CODING:" + veff.getImpact() + ":" + veff.getEffectType();
				}

			} else {
				//---
				// NonCoding scores (NCCAT)
				//---
				switch (veff.getEffectType()) {
				case REGULATION:
					// Add NCMARK
					if (ncmark.length() > 0) ncmark.append(",");
					ncmark.append(veff.getBioType() + ":" + veff.getEffectDetails());

					// Add NCELEMENT
					if (ncelement.length() > 0) ncelement.append(",");
					ncelement.append("CHROM");

					key = "REGULATION:" + veff.getBioType() + ":" + veff.getEffectDetails();
					hasReg = true;
					break;

				case MOTIF:
					// Add NCFACTOR
					if (ncfactor.length() > 0) ncfactor.append(",");
					ncfactor.append(veff.getEffectDetails());

					// Add NCELEMENT
					if (ncelement.length() > 0) ncelement.append(",");
					ncelement.append("TFBS");

					key = "MOTIF:" + veff.getEffectDetails();
					hasMotif = true;
					break;

				default:
				}

				// Count non-coding annotations
				if (verbose && key != null) countByAcat.inc(key);
			}

		}

		// Annotate
		if (minAcatScore < 4) {
			// If we have a coding annotation, we don't need to add non-coding annotations
			countByAcat.inc(acatKey);
		} else if (hasMotif && hasReg) nccat = 1;
		else if (hasMotif) nccat = 2;
		else if (hasReg) nccat = 3;
		else if (cons > CONSERVATION_THRESHOLD) nccat = 4;

		// Anything found? Add INFO
		if (acat.length() > 0) ve.addInfo(ACAT, acat.toString());
		if (minAcatScore >= 4) {
			// If there is no coding annotation, add non-coding ones
			if (ncmark.length() > 0) ve.addInfo(NCMARK, ncmark.toString());
			if (ncfactor.length() > 0) ve.addInfo(NCFACTOR, ncfactor.toString());
			if (nccat <= 4) ve.addInfo(NCCAT, nccat + "");
			if (cons >= CONSERVATION_THRESHOLD) ve.addInfo(NCCONS, String.format("%.2f", cons));
		}

		// Count categories
		if (minAcatScore <= 4) countByAcat.inc("CODING_CATEGORY:" + minAcatScore);
		if (nccat <= 4) countByAcat.inc("NON_CODING_CATEGORY:" + nccat);
	}

	/**
	 * Add header lines
	 * @param vcf
	 */
	void addHeader(VcfFileIterator vcf) {
		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(ACAT, VcfInfoType.Integer, ".", "Variant coding impact category {1, 2, 3, 4} correspond to {HIGH, MODERATE, LOW, MODIFIER}. Most deleterious effect is selected."));

		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(NCCAT, VcfInfoType.Integer, ".", "Variant non-coding impact category {1, 2, 3, 4} correspond to {MOTIF + REGULATORY, MOTIF, REGULATORY, CONSERVED }"));
		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(NCACTIVITY, VcfInfoType.String, ".", "Chromatin state predicted activity. Format is Tissue:Activity (e.g. Liver:Enhancer, Adipose:Insulator, etc.)"));
		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(NCMARK, VcfInfoType.String, ".", "Chromatin mark predicted. Format is Tissue:Mark (e.g. Liver:H3K4me1, Adipose:H3K27me3, etc.)"));
		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(NCFACTOR, VcfInfoType.String, ".", "Transcription factor mark predicted. Format is Tissue:FactorId:FactorName (e.g. Liver:MA0139.1:CTCF, Adipose:MA0003.1:Ap2alpha, etc.)"));
		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(NCELEMENT, VcfInfoType.String, ".", "Types of non-coding elements predicted {STATE, MARK, TFBS, etc.}"));
		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(NCCONS, VcfInfoType.Float, ".", "Conservation score above threshold (threshold is " + CONSERVATION_THRESHOLD + "). Score used: PhastCons."));

		vcf.getVcfHeader().addInfo(new VcfHeaderInfo(T2D_GENES, VcfInfoType.Flag, ".", "Variant exclusive from T2D_GENES project (i.e. neither in dbSnp, 1000 Genomes nor ESP)"));
	}

	/**
	 * Parse command line arguments
	 */
	@Override
	public void parseArgs(String[] args) {
		if (args.length != 1) usage("Missing file.vcf");
		else vcfFile = args[0];
	}

	/**
	 * Run command
	 */
	@Override
	public boolean run() {
		if (verbose) Log.info("Calculating ACAT score on input: " + vcfFile);
		countByAcat = new CountByType();
		countByEff = new CountByType();

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		vcf.setDebug(debug);

		for (VcfEntry ve : vcf) {
			if (vcf.isHeadeSection()) {
				addHeader(vcf); // Add header lines
				System.out.println(vcf.getVcfHeader()); // Show header
			}

			acat(ve); // Annotate ACAT
			t2dGenes(ve); // Annotate T2D-Genes

			// Show line
			if (!quiet) System.out.println(ve);
		}

		if (verbose) {
			System.err.println(countByAcat);
			System.err.println(countByEff);
			Log.info("Done.");
		}

		return true;
	}

	/**
	 * Annotate if this variant exclusive from T2D_GENES (i.e. not in dbSnp, 1000 Genomes or ESP)
	 */
	void t2dGenes(VcfEntry ve) {
		if ((ve.getId() == null) || ve.getId().isEmpty()) ve.addInfo(T2D_GENES, null);
	}

	/**
	 * Show usage and exit
	 */
	@Override
	public void usage(String message) {
		if (message != null) System.err.println("Error: " + message + "\n");
		System.err.println("snpEff version " + SnpEff.VERSION);
		System.err.println("Usage: snpEff acat file.vcf");
		System.exit(-1);
	}
}
