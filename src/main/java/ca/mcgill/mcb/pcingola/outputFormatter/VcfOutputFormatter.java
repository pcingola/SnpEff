package ca.mcgill.mcb.pcingola.outputFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Custom;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Regulation;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.LossOfFunction;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.FunctionalClass;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.KeyValue;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect.FormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Formats output as VCF
 *
 * @author pcingola
 */
public class VcfOutputFormatter extends OutputFormatter {

	public static final boolean debug = false;
	public static final String VCF_INFO_OICR_NAME = "OICR";

	boolean needAddInfo = false;
	boolean needAddHeader = true;
	boolean lossOfFunction;
	boolean gatk;
	FormatVersion formatVersion = VcfEffect.FormatVersion.FORMAT_EFF_4;
	List<VcfEntry> vcfEntries;

	/**
	 * Create a string that is safe (i.e. valid) to add in an INFO field
	 */
	public static String vcfInfoSafeString(String value) {
		if (value == null) return value;
		value = value.replaceAll("[ ,;|=()]", "_");
		return value;
	}

	public VcfOutputFormatter() {
		super();
	}

	/**
	 * Add all vcf entries to a list (used only for debugging and test-cases)
	 */
	public VcfOutputFormatter(List<VcfEntry> vcfEntries) {
		super();
		this.vcfEntries = vcfEntries;
	}

	/**
	 * Add header
	 */
	protected void addHeader() {
		VcfEntry vcfEntry = (VcfEntry) section;

		// Sanity check
		if (vcfEntry == null) return;

		// Get header
		VcfFileIterator vcfFile = vcfEntry.getVcfFileIterator();

		// Add new lines
		for (String newHeaderLine : getNewHeaderLines())
			vcfFile.getVcfHeader().addLine(newHeaderLine);

		needAddHeader = false;
	}

	/**
	 * Add effects to INFO field
	 */
	protected void addInfo(VcfEntry vcfEntry) {
		// No effects to show?
		if (variantEffects.isEmpty()) return;

		// Do all effects have warnings or errors (this could produce an empty 'EFF' field in GATK mode?
		boolean allWarnings = false;
		if (gatk) {
			allWarnings = variantEffects.size() > 0;
			for (VariantEffect varEff : variantEffects)
				allWarnings &= (varEff.hasError() || varEff.hasWarning());
		}

		// Sort change effects by impact
		Collections.sort(variantEffects);

		// GATK only picks the first (i.e. highest impact) effect
		if (gatk && variantEffects.size() > 1) {
			ArrayList<VariantEffect> varEffsGatk = new ArrayList<VariantEffect>();
			varEffsGatk.add(variantEffects.get(0));
			variantEffects = varEffsGatk;
		}

		//---
		// Calculate all effects and genes
		//---
		HashSet<String> effs = new HashSet<String>();
		ArrayList<String> effsSorted = new ArrayList<String>();
		HashSet<String> oicr = (useOicr ? new HashSet<String>() : null);
		boolean addCustomFields = false;
		for (VariantEffect variantEffect : variantEffects) {
			// In GATK mode, skip changeEffects having errors or warnings (unless ALL effects have warnings)
			if (gatk && !allWarnings && (variantEffect.hasError() || variantEffect.hasWarning())) continue;

			// If it is not filtered out by changeEffectResutFilter  => Show it
			if ((variantEffectResutFilter == null) || (!variantEffectResutFilter.filter(variantEffect))) {
				StringBuilder effBuff = new StringBuilder();

				// Add effect
				effBuff.append(variantEffect.effect(true, false, false, useSequenceOntology));
				effBuff.append("(");

				// Add effect impact
				effBuff.append(variantEffect.getEffectImpact());
				effBuff.append("|");

				// Add functional class
				FunctionalClass fc = variantEffect.getFunctionalClass();
				effBuff.append(fc == FunctionalClass.NONE ? "" : fc.toString()); // Show only if it is not empty
				effBuff.append("|");

				// Codon change
				String codonChange = variantEffect.getCodonChangeMax();
				if (!codonChange.isEmpty()) effBuff.append(codonChange);
				else if (variantEffect.getDistance() >= 0) effBuff.append(variantEffect.getDistance());
				effBuff.append("|");

				// Add HGVS (amino acid change)
				if (useHgvs) effBuff.append(variantEffect.getHgvs());
				else effBuff.append(variantEffect.getAaChange());
				effBuff.append("|");

				// Add amino acid length
				if (formatVersion != FormatVersion.FORMAT_EFF_2) { // This field is not in format version 2
					int aalen = variantEffect.getAaLength();
					effBuff.append(aalen >= 0 ? aalen : "");
					effBuff.append("|");
				}

				// Add gene info
				Gene gene = variantEffect.getGene();
				Transcript tr = variantEffect.getTranscript();
				if (gene != null) {
					// Gene name
					effBuff.append(vcfInfoSafeString(useGeneId ? gene.getId() : gene.getGeneName()));
					effBuff.append("|");

					// Transcript biotype
					if (tr != null) {
						if ((tr.getBioType() != null) && !tr.getBioType().isEmpty()) effBuff.append(tr.getBioType());
						else effBuff.append(tr.isProteinCoding() ? "protein_coding" : ""); // No biotype? Add protein_coding of we know it is.
					}
					effBuff.append("|");

					// Protein coding gene?
					String coding = "";
					if (gene.getGenome().hasCodingInfo()) coding = (gene.isProteinCoding() ? VariantEffect.Coding.CODING.toString() : VariantEffect.Coding.NON_CODING.toString());
					effBuff.append(coding);
					effBuff.append("|");
				} else if (variantEffect.isRegulation()) {
					Regulation reg = (Regulation) variantEffect.getMarker();
					effBuff.append("|" + reg.getCellType() + "||");
				} else if (variantEffect.isCustom()) {
					Marker m = variantEffect.getMarker();
					if (m != null) effBuff.append("|" + VcfEntry.vcfInfoSafe(m.getId()) + "||");
					else effBuff.append("|||");
				} else effBuff.append("|||");

				// Add transcript info
				if (tr != null) effBuff.append(vcfInfoSafeString(tr.getId()));
				effBuff.append("|");

				// Add exon (or intron) rank info
				Exon ex = variantEffect.getExon();
				int rank = -1;
				if (ex != null) rank = ex.getRank();
				else {
					// Do we have an intron?
					Intron intron = variantEffect.getIntron();
					if (intron != null) rank = intron.getRank();
				}
				effBuff.append(rank >= 0 ? rank : "");

				// Add genotype (or genotype difference) for this effect
				if (formatVersion == FormatVersion.FORMAT_EFF_4) {
					effBuff.append("|");
					effBuff.append(variantEffect.getGenotype());
				}

				//---
				// Errors or warnings (this is the last thing in the list)
				//---
				if (variantEffect.hasError() || variantEffect.hasWarning()) {
					StringBuilder err = new StringBuilder();

					// Add warnings
					if (!variantEffect.getWarning().isEmpty()) err.append(variantEffect.getWarning());

					// Add errors
					if (!variantEffect.getError().isEmpty()) {
						if (err.length() > 0) err.append("+");
						err.append(variantEffect.getError());
					}

					effBuff.append("|");
					effBuff.append(err);
				}
				effBuff.append(")");

				//---
				// Add effect
				//---
				if (!effs.add(effBuff.toString())) {
					if (debug) {
						// Effect has already been added? Something is wrong, the information should be unique for each effect
						StringBuilder sb = new StringBuilder();
						sb.append("--------------------------------------------------------------------------------\n");
						sb.append("VCF Entry   :\t" + vcfEntry + "\n");
						sb.append("REPEAT (VCF):\t" + effBuff + "\n");
						sb.append("REPEAT (TXT):\t" + variantEffect + "\n");
						sb.append("All    (VCF):\n");
						for (String ce : effsSorted)
							sb.append("\t" + ce + "\n");
						sb.append("All    (TXT):\n");
						for (VariantEffect ce : variantEffects)
							sb.append("\t" + ce + "\n");
						sb.append("--------------------------------------------------------------------------------\n");
						Gpr.debug("WARNING: Repeated effect!\n" + sb);
					}
				} else effsSorted.add(effBuff.toString());

				//---
				// Add OICR data
				//---
				if (useOicr && (tr != null)) {
					StringBuilder sb = new StringBuilder();
					Variant variant = variantEffect.getVariant();

					// Get cDNA position
					int pos = tr.isStrandMinus() ? variant.getStart() : variant.getEnd(); // First base in cDNA
					int cdnaIdx = tr.baseNumberPreMRna(pos) + 1; // Which cDNA base number?
					if (cdnaIdx > 0) sb.append("(" + vcfInfoSafeString(tr.getId()) + "|" + cdnaIdx + ")");

					oicr.add(sb.toString());
				}

				//---
				// Is this annotated using a 'custom' interval?
				// If so, there might be additional "key=value" pairs
				//---
				addCustomFields |= variantEffect.hasAdditionalAnnotations();
			}
		}

		//---
		// Add data to INFO fields
		//---

		// Add 'EFF' info field
		String effStr = toStringVcfInfo(effsSorted);
		if (!effStr.isEmpty()) vcfEntry.addInfo(VcfEffect.VCF_INFO_EFF_NAME, effStr);

		// Add 'OICR' info field
		if (useOicr && (oicr.size() > 0)) {
			String oicrInfo = toStringVcfInfo(oicr);
			if (!oicrInfo.isEmpty()) vcfEntry.addInfo(VCF_INFO_OICR_NAME, oicrInfo);
		}

		// Add LOF info?
		if (lossOfFunction) {
			// Perform LOF analysis and add annotations
			LossOfFunction lof = new LossOfFunction(config, variantEffects);
			if (lof.isLof()) vcfEntry.addInfo(LossOfFunction.VCF_INFO_LOF_NAME, lof.toStringVcfLof());
			if (lof.isNmd()) vcfEntry.addInfo(LossOfFunction.VCF_INFO_NMD_NAME, lof.toStringVcfNmd());
		}

		// Add custom markers info fields
		if (addCustomFields) {
			for (VariantEffect changeEffect : variantEffects) {
				if (changeEffect.hasAdditionalAnnotations()) {
					Custom custom = (Custom) changeEffect.getMarker();
					for (KeyValue<String, String> kv : custom) {
						String key = VcfEntry.vcfInfoSafe(custom.getLabel() + "_" + kv.key);
						String value = VcfEntry.vcfInfoSafe(kv.value);
						vcfEntry.addInfo(key, value);
					}
				}
			}
		}

		needAddInfo = false; // Don't add info twice
	}

	@Override
	public OutputFormatter clone() {
		try {
			VcfOutputFormatter newOutputFormatter = (VcfOutputFormatter) super.clone();
			newOutputFormatter.formatVersion = formatVersion;
			newOutputFormatter.needAddInfo = needAddInfo;
			newOutputFormatter.needAddHeader = needAddHeader;
			newOutputFormatter.lossOfFunction = lossOfFunction;
			newOutputFormatter.gatk = gatk;
			// newOutputFormatter.genome = genome;
			return newOutputFormatter;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Finish up section
	 */
	@Override
	public String endSection(Marker marker) {
		if (marker == null) {
			return super.endSection(marker);
		} else if (marker instanceof VcfEntry) {
			// Ignore other markers (e.g. seqChanges)
			if (vcfEntries != null) vcfEntries.add((VcfEntry) marker);
			return super.endSection(marker);
		}
		return null;
	}

	/**
	 * New lines to be added to header
	 */
	public List<String> getNewHeaderLines() {
		ArrayList<String> newLines = new ArrayList<String>();

		newLines.add("##SnpEffVersion=\"" + version + "\"");
		newLines.add("##SnpEffCmd=\"" + commandLineStr + "\"");

		// Fields changed in different format versions
		if (formatVersion == FormatVersion.FORMAT_EFF_2) newLines.add("##INFO=<ID=EFF,Number=.,Type=String,Description=\"Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_change| Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon [ | ERRORS | WARNINGS ] )' \">");
		else if (formatVersion == FormatVersion.FORMAT_EFF_3) newLines.add("##INFO=<ID=EFF,Number=.,Type=String,Description=\"Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_change| Amino_Acid_length | Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon [ | ERRORS | WARNINGS ] )' \">");
		else newLines.add("##INFO=<ID=EFF,Number=.,Type=String,Description=\"Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_Change| Amino_Acid_length | Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon_Rank  | Genotype_Number [ | ERRORS | WARNINGS ] )' \">");

		if (lossOfFunction) {
			newLines.add("##INFO=<ID=LOF,Number=.,Type=String,Description=\"Predicted loss of function effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected' \">");
			newLines.add("##INFO=<ID=NMD,Number=.,Type=String,Description=\"Predicted nonsense mediated decay effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected' \">");
		}

		if (useOicr) newLines.add("##INFO=<ID=OICR,Number=.,Type=String,Description=\"Format: ( Transcript | Distance from begining cDNA )\">");

		return newLines;
	}

	/**
	 * Does this
	 */
	boolean hasAnnotations(VariantEffect changeEffect) {
		return changeEffect.getMarker() != null // Do we have a marker?
				&& (changeEffect.getMarker() instanceof Custom) // Is it 'custom'?
				&& ((Custom) changeEffect.getMarker()).hasAnnotations() // Does it have additional annotations?
		;
	}

	public void setGatk(boolean gatk) {
		this.gatk = gatk;
		if (gatk) formatVersion = VcfEffect.FormatVersion.FORMAT_EFF_2;
	}

	public void setLossOfFunction(boolean lossOfFunction) {
		this.lossOfFunction = lossOfFunction;
	}

	@Override
	public void setOutOffset(int outOffset) {
		throw new RuntimeException("Cannot set output offset on '" + this.getClass().getSimpleName() + "' formatter!");
	}

	@Override
	public void startSection(Marker marker) {
		// Ignore other markers (e.g. seqChanges)
		if (marker instanceof VcfEntry) super.startSection(marker);
		needAddInfo = true;
	}

	@Override
	public String toString() {
		if (section == null) return "";
		VcfEntry vcfEntry = (VcfEntry) section;
		if (needAddInfo) addInfo(vcfEntry);
		return vcfEntry.toString();
	}

	/**
	 * Show header
	 */
	@Override
	protected String toStringHeader() {
		if (needAddHeader) addHeader(); // Add header lines

		VcfEntry vcfEntry = (VcfEntry) section;
		VcfFileIterator vcfFile = vcfEntry.getVcfFileIterator();
		return vcfFile.getVcfHeader().toString();
	}

	/**
	 * Convert a collection to a string usable in a VCF INFO field
	 */
	String toStringVcfInfo(Collection<String> strs) {
		// Add the all
		StringBuffer sb = new StringBuffer();
		for (String str : strs)
			if (!str.isEmpty()) sb.append(str + ",");

		if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1); // Remove last comma
		return sb.toString();
	}

}
