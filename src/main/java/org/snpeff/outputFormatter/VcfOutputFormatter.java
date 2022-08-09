package org.snpeff.outputFormatter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Custom;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.LossOfFunction;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.util.KeyValue;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;

/**
 * Formats output as VCF
 *
 * @author pcingola
 */
public class VcfOutputFormatter extends OutputFormatter {

	public static boolean debug = false;
	public static final String VCF_INFO_OICR_NAME = "OICR";

	boolean needAddInfo = false;
	boolean needAddHeader = true;
	boolean lossOfFunction;
	boolean gatk;
	boolean onlyHighestAnn;
	EffFormatVersion formatVersion = EffFormatVersion.DEFAULT_FORMAT_VERSION;
	List<VcfEntry> vcfEntries;

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

		// Sort change effects by impact
		Collections.sort(variantEffects);

		// GATK mode: Picks the first (i.e. highest impact) effect
		if (gatk) variantEffects = variantEffectsHighest(variantEffects);

		//---
		// Calculate all effects and genes
		//---
		HashSet<String> effs = new HashSet<>();
		ArrayList<String> effsSorted = new ArrayList<>();
		HashSet<String> oicr = (useOicr ? new HashSet<>() : null);
		boolean addCustomFields = false;
		for (VariantEffect variantEffect : variantEffects) {

			// If it is not filtered out by variantEffectResutFilter => Show it
			if ((variantEffectResutFilter == null) || (!variantEffectResutFilter.filter(variantEffect))) {
				//---
				// Create INFO field value as a string
				//---
				VcfEffect vcfEffect = new VcfEffect(variantEffect, formatVersion, useSequenceOntology, gatk);
				vcfEffect.setUseGeneId(useGeneId);
				vcfEffect.setUseHgvs(useHgvs);
				String effStr = vcfEffect.toString();

				//---
				// Add effect
				//---
				if (!effs.add(effStr)) {
					if (debug) {
						// Effect has already been added? Something is wrong, the information should be unique for each effect
						StringBuilder sb = new StringBuilder();
						sb.append("--------------------------------------------------------------------------------\n");
						sb.append("VCF Entry   :\t" + vcfEntry + "\n");
						sb.append("REPEAT (VCF):\t" + effStr + "\n");
						sb.append("REPEAT (TXT):\t" + variantEffect + "\n");
						sb.append("All    (VCF):\n");
						for (String ce : effsSorted)
							sb.append("\t" + ce + "\n");
						sb.append("All    (TXT):\n");
						for (VariantEffect ce : variantEffects)
							sb.append("\t" + ce + "\n");
						sb.append("--------------------------------------------------------------------------------\n");
						Log.debug("WARNING: Repeated effect!\n" + sb);
					}
				} else effsSorted.add(effStr);

				//---
				// Add OICR data
				//---
				if (useOicr) {
					Transcript tr = variantEffect.getTranscript();

					if (tr != null) {

						StringBuilder sb = new StringBuilder();
						Variant variant = variantEffect.getVariant();

						// Get cDNA position
						int pos = tr.isStrandMinus() ? variant.getStart() : variant.getEndClosed(); // First base in cDNA
						int cdnaIdx = tr.baseNumber2MRnaPos(pos) + 1; // Which cDNA base number?
						if (cdnaIdx > 0) sb.append("(" + VcfEntry.vcfInfoValueSafe(tr.getId()) + "|" + cdnaIdx + ")");

						oicr.add(sb.toString());
					}
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
		if (!effStr.isEmpty()) vcfEntry.addInfo(VcfEffect.infoFieldName(formatVersion), effStr);

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
			for (VariantEffect variantEffect : variantEffects) {
				if (variantEffect.hasAdditionalAnnotations()) {
					Custom custom = (Custom) variantEffect.getMarker();
					for (KeyValue<String, String> kv : custom) {
						String key = VcfEntry.vcfInfoValueSafe(custom.getLabel() + "_" + kv.key);
						String value = VcfEntry.vcfInfoValueSafe(kv.value);
						vcfEntry.addInfo(key, value);
					}
				}
			}
		}

		needAddInfo = false; // Don't add info twice
	}

	/**
	 * Are all varaint effects having some sort of warning or error?
	 */
	boolean allWarnings(List<VariantEffect> variantEffects) {
		if (variantEffects.size() <= 0) return false; // Emtpy => No warnings

		for (VariantEffect varEff : variantEffects)
			if (!(varEff.hasError() || varEff.hasWarning())) return false;

		return true;
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
			// Ignore other markers
			if (vcfEntries != null) vcfEntries.add((VcfEntry) marker);
			return super.endSection(marker);
		}
		return null;
	}

	/**
	 * New lines to be added to header
	 */
	public List<String> getNewHeaderLines() {
		ArrayList<String> newLines = new ArrayList<>();

		newLines.add("##SnpEffVersion=\"" + version + "\"");
		newLines.add("##SnpEffCmd=\"" + commandLineStr + "\"");

		// Fields changed in different format versions
		newLines.add(formatVersion.vcfHeader());

		if (lossOfFunction) {
			newLines.add("##INFO=<ID=LOF,Number=.,Type=String,Description=\"Predicted loss of function effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected'\">");
			newLines.add("##INFO=<ID=NMD,Number=.,Type=String,Description=\"Predicted nonsense mediated decay effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected'\">");
		}

		if (useOicr) newLines.add("##INFO=<ID=OICR,Number=.,Type=String,Description=\"Format: ( Transcript | Distance from begining cDNA )\">");

		return newLines;
	}

	public void setFormatVersion(EffFormatVersion formatVersion) {
		this.formatVersion = formatVersion;
	}

	public void setGatk(boolean gatk) {
		this.gatk = gatk;
		if (gatk) formatVersion = EffFormatVersion.FORMAT_EFF_2;
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
		// Ignore other markers
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
		if (vcfEntry == null) return "";

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

	/**
	 * GATK mode: Pick the first (i.e. highest impact) effect that has
	 * no error/warning. If all variant effects have warnings or errors, just
	 * pick the first (to avoid having an empty annotation)
	 */
	List<VariantEffect> variantEffectsHighest(List<VariantEffect> variantEffects) {
		if (variantEffects.size() <= 1) return variantEffects;

		// Create a new list of variant effects
		ArrayList<VariantEffect> varEffsHighest = new ArrayList<>();

		// In GATK mode, skip varianrEffects having errors or warnings (unless ALL effects have warnings)
		if (allWarnings(variantEffects)) {
			// Do all effects have warnings or errors?
			// We avoid producing an empty 'EFF' field in GATK mode by just picking the first
			varEffsHighest.add(variantEffects.get(0));
		} else {
			// Pick the first variantEffect that has no error or warning
			for (VariantEffect variantEffect : variantEffects) {
				if (!variantEffect.hasError() && !variantEffect.hasWarning()) {
					varEffsHighest.add(variantEffect);
					return varEffsHighest;
				}
			}
		}

		// Note: This list will always have at most one element
		return varEffsHighest;
	}

}
