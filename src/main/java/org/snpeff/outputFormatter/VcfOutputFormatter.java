package org.snpeff.outputFormatter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.filter.VariantEffectFilter;
import org.snpeff.interval.Custom;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.LossOfFunction;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Gpr;
import org.snpeff.util.KeyValue;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfHeaderEntry;
import org.snpeff.vcf.VcfHeaderInfo;
import org.snpeff.vcf.VcfInfoType;

/**
 * Formats output as VCF
 *
 * @author pcingola
 */
public class VcfOutputFormatter {

	public static boolean debug = false;
	public static final String VCF_INFO_OICR_NAME = "OICR";

	Config config;
	boolean gatk;
	boolean lossOfFunction;
	boolean needAddInfo = false;
	boolean needAddHeader = true;
	boolean supressOutput = false; // Do not print anything (used for testCases)
	boolean onlyHighestAnn;
	boolean printed = false;
	boolean showHeader = true; // Show header information
	boolean useHgvs; // Use HGVS notation
	boolean useGeneId; // Use Gene ID instead of gene name
	boolean useSequenceOntology; // Use Sequence Ontology terms
	boolean useOicr; // Use OICR tag
	boolean vcfHeaderProcessed = false;
	int entryNum = 0;
	int outOffset = 1;
	String commandLineStr;
	String version;
	String chrStr;
	String outputFile = null;
	BufferedWriter out;
	VcfEntry vcfEntryCurrent;
	VariantEffectFilter variantEffectResutFilter = null; // Filter prediction results
	List<VariantEffect> variantEffects;
	EffFormatVersion formatVersion = EffFormatVersion.DEFAULT_FORMAT_VERSION;
	List<VcfEntry> vcfEntries;

	public VcfOutputFormatter() {
		variantEffects = new ArrayList<>();
	}

	/**
	 * Add all vcf entries to a list (used only for debugging and test-cases)
	 */
	public VcfOutputFormatter(List<VcfEntry> vcfEntries) {
		this();
		this.vcfEntries = vcfEntries;
	}

	/**
	 * Add effects to list
	 */
	public void add(VariantEffect variantEffect) {
		// Passes the filter? => Add
		if ((variantEffectResutFilter == null) || (!variantEffectResutFilter.filter(variantEffect))) variantEffects.add(variantEffect);
	}

	/**
	 * Add all variant effects
	 */
	public void add(VariantEffects variantEffects) {
		if (variantEffects == null) return;
		for (VariantEffect eff : variantEffects)
			add(eff);
	}

	public boolean addHeaders(VcfFileIterator vcf) {
		// Add new header lines
		for (VcfHeaderEntry vh : headers())
			vcf.getVcfHeader().add(vh);
		return true;
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

			// If it is not filtered out by changeEffectResutFilter => Show it
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
						Gpr.debug("WARNING: Repeated effect!\n" + sb);
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
						int pos = tr.isStrandMinus() ? variant.getStart() : variant.getEnd(); // First base in cDNA
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
	public VcfOutputFormatter clone() {
		try {
			// Create a new formatter. We cannot use the same output formatter for all workers
			VcfOutputFormatter newOutputFormatter = (VcfOutputFormatter) super.clone();
			newOutputFormatter = this.getClass().newInstance();
			newOutputFormatter.formatVersion = formatVersion;
			newOutputFormatter.needAddInfo = needAddInfo;
			newOutputFormatter.needAddHeader = needAddHeader;
			newOutputFormatter.lossOfFunction = lossOfFunction;
			newOutputFormatter.gatk = gatk;
			newOutputFormatter.supressOutput = supressOutput;
			newOutputFormatter.showHeader = showHeader;
			newOutputFormatter.useHgvs = useHgvs;
			newOutputFormatter.useGeneId = useGeneId;
			newOutputFormatter.useSequenceOntology = useSequenceOntology;
			newOutputFormatter.useOicr = useOicr;
			newOutputFormatter.entryNum = entryNum;
			newOutputFormatter.outOffset = outOffset;
			newOutputFormatter.commandLineStr = commandLineStr;
			newOutputFormatter.version = version;
			newOutputFormatter.chrStr = chrStr;
			newOutputFormatter.vcfEntryCurrent = vcfEntryCurrent;
			newOutputFormatter.variantEffectResutFilter = variantEffectResutFilter;
			newOutputFormatter.config = config;
			return newOutputFormatter;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * CLose output files, if any
	 */
	public void close() {
		if (out != null) {
			try {
				out.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * New lines to be added to header
	 */
	public List<VcfHeaderEntry> headers() {
		List<VcfHeaderEntry> newLines = new ArrayList<>();

		newLines.add(new VcfHeaderEntry("##SnpEffVersion=\"" + SnpEff.VERSION_AUTHOR + "\""));
		newLines.add(new VcfHeaderEntry("##SnpEffCmd=\"" + commandLineStr + "\""));

		// Fields changed in different format versions
		newLines.add(new VcfHeaderEntry(formatVersion.vcfHeader()));

		if (lossOfFunction) {
			newLines.add(new VcfHeaderInfo("LOF", VcfInfoType.String, ".", "Predicted loss of function effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected"));
			newLines.add(new VcfHeaderInfo("NMD", VcfInfoType.String, ".", "Predicted nonsense mediated decay effects for this variant. Format: 'Gene_Name | Gene_ID | Number_of_transcripts_in_gene | Percent_of_transcripts_affected'"));
		}

		if (useOicr) {
			newLines.add(new VcfHeaderInfo("OICR", VcfInfoType.String, ".", "Format: ( Transcript | Distance from begining cDNA )"));
		}

		return newLines;
	}

	/**
	 * End this section and print results
	 */
	public void print() {
		if (!printed) print(toString());
		printed = true;
	}

	/**
	 * Print a "raw" string to a file
	 */
	public void print(String outStr) {
		try {
			// Open output file?
			if ((outputFile != null) && (out == null)) out = new BufferedWriter(new FileWriter(outputFile));

			// Write something?
			if ((outStr != null) && (!outStr.isEmpty())) {
				// Write to file?
				if (out != null) {
					out.write(outStr);
					out.write("\n");
				} else if (!supressOutput) {
					System.out.println(outStr); // Show on STDOUT
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Process VCF header related issues
	 */
	protected String processVcfHeader(VcfFileIterator vcf) {
		if (vcfHeaderProcessed // Already processed? Skip
				|| (!vcf.isHeadeSection() && vcf.getLineNum() > 1) // First line is header (when missing)
		) return "";

		// Add lines to header
		addHeaders(vcf);
		vcfHeaderProcessed = true;

		if (showHeader) {
			String headerStr = vcf.getVcfHeader().toString();
			if (!headerStr.isEmpty()) print(headerStr);
			showHeader = false;
			return headerStr;
		}

		return "";
	}

	public void setChrStr(String chrStr) {
		this.chrStr = chrStr;
	}

	public void setCommandLineStr(String commandLineStr) {
		this.commandLineStr = commandLineStr;
	}

	public void setConfig(Config config) {
		this.config = config;
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

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	public void setSupressOutput(boolean supressOutput) {
		this.supressOutput = supressOutput;
	}

	public void setUseGeneId(boolean useGeneId) {
		this.useGeneId = useGeneId;
	}

	public void setUseHgvs(boolean useHgvs) {
		this.useHgvs = useHgvs;
	}

	public void setUseOicr(boolean useOicr) {
		this.useOicr = useOicr;
	}

	public void setUseSequenceOntology(boolean useSequenceOntology) {
		this.useSequenceOntology = useSequenceOntology;
	}

	public void setVariantEffectResutFilter(VariantEffectFilter changeEffectResutFilter) {
		variantEffectResutFilter = changeEffectResutFilter;
	}

	public void setVcfEntry(VcfEntry ve) {
		vcfEntryCurrent = ve;
		needAddInfo = true;
		printed = false;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		if (vcfEntryCurrent == null) return "";

		if (vcfEntries != null) vcfEntries.add(vcfEntryCurrent);

		// Create header?
		String header = null;
		if (showHeader && (entryNum == 0)) {
			header = toStringHeader();
			if (!header.isEmpty()) {
				header += "\n";
			}
		}

		// Current vcf entry
		if (needAddInfo) addInfo(vcfEntryCurrent);
		String line = vcfEntryCurrent.toString();

		// Update
		entryNum++;
		variantEffects.clear();

		return header == null ? line : header + line;
	}

	/**
	 * Show header
	 */
	protected String toStringHeader() {
		VcfEntry vcfEntry = vcfEntryCurrent;
		if (vcfEntry == null) return "";

		VcfFileIterator vcfFile = vcfEntry.getVcfFileIterator();
		if (needAddHeader) addHeaders(vcfFile); // Add header lines
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
