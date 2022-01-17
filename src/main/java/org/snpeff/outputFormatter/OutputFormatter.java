package org.snpeff.outputFormatter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.snpeff.filter.VariantEffectFilter;
import org.snpeff.interval.Marker;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.VariantEffect;

/**
 * Formats output
 * How is this used:
 *    - newSection();   // Create a new 'section' on the output format (e.g. a new Marker)
 *    - add();			// Add all changes related to this section (i.e. all changes related to this marker)
 *    - endSection();	// Output all changes related to this section (output header if needed), clean up list of changes
 *
 * @author pcingola
 */
public abstract class OutputFormatter {

	boolean supressOutput = false; // Do not print anything (used for testCases)
	boolean showHeader = true; // Show header information
	boolean useHgvs; // Use HGVS notation
	boolean useGeneId; // Use Gene ID instead of gene name
	boolean useSequenceOntology; // Use Sequence Ontology terms
	boolean useOicr; // Use OICR tag
	int sectionNum = 0;
	int outOffset = 1;
	String commandLineStr;
	String version;
	String chrStr;
	String outputFile = null;
	BufferedWriter out;
	Marker section;
	VariantEffectFilter variantEffectResutFilter = null; // Filter prediction results
	List<VariantEffect> variantEffects;
	Config config;

	public OutputFormatter() {
		variantEffects = new ArrayList<>();
	}

	/**
	 * Add effects to list
	 */
	public void add(VariantEffect variantEffect) {
		// Passes the filter? => Add
		if ((variantEffectResutFilter == null) || (!variantEffectResutFilter.filter(variantEffect))) variantEffects.add(variantEffect);
	}

	@Override
	public OutputFormatter clone() {
		OutputFormatter newOutputFormatter = null;
		try {
			// Create a new formatter. We cannot use the same output formatter for all workers
			newOutputFormatter = this.getClass().getConstructor().newInstance();
			newOutputFormatter.supressOutput = supressOutput;
			newOutputFormatter.showHeader = showHeader;
			newOutputFormatter.useHgvs = useHgvs;
			newOutputFormatter.useGeneId = useGeneId;
			newOutputFormatter.useSequenceOntology = useSequenceOntology;
			newOutputFormatter.useOicr = useOicr;
			newOutputFormatter.sectionNum = sectionNum;
			newOutputFormatter.outOffset = outOffset;
			newOutputFormatter.commandLineStr = commandLineStr;
			newOutputFormatter.version = version;
			newOutputFormatter.chrStr = chrStr;
			newOutputFormatter.section = section;
			newOutputFormatter.variantEffectResutFilter = variantEffectResutFilter;
			newOutputFormatter.config = config;

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return newOutputFormatter;
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
	 * Finish up section
	 */
	public String endSection(Marker marker) {
		StringBuilder sb = new StringBuilder();

		// Add header?
		if (showHeader && (sectionNum == 0)) {
			String header = toStringHeader();
			if (!header.isEmpty()) {
				sb.append(header);
				sb.append("\n");
			}
		}

		// Add current line
		sb.append(toString());

		sectionNum++;
		variantEffects.clear();

		return supressOutput ? null : sb.toString();
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
				} else if (!supressOutput) System.out.println(outStr); // Show on STDOUT
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * End this section and print results
	 */
	public void printSection(Marker marker) {
		print(endSection(marker));
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

	public void setOutOffset(int outOffset) {
		this.outOffset = outOffset;
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

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Starts a new section
	 */
	public void startSection(Marker marker) {
		section = marker;
	}

	@Override
	public String toString() {
		throw new RuntimeException("Method toString() must be overridden!");
	}

	/**
	 * Show header
	 */
	protected abstract String toStringHeader();
}
