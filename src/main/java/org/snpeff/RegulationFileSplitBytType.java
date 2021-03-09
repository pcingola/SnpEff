package org.snpeff;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.snpeff.fileIterator.RegulationFileIterator;
import org.snpeff.interval.Regulation;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.util.Timer;

/**
 * Split regulation files into smaller files (one per 'regulation type')
 *
 * Regulation files can be quite large and we cannot read them into
 * memory. Also there might be thousands of different regulation types
 *
 * We read chunks of BLOCK_SIZE lines at a time to avoid running out of
 * memory. Note that we cannot open one file descriptor per 'regulation
 * type' and save each line into it, since we may also run out of file
 * descriptors.
 *
 * @author pcingola
 */
public class RegulationFileSplitBytType {

	static final int BLOCK_SIZE = 1000000; // Read blocks of BLOCK_SIZE lines at a time

	boolean verbose = false;
	String path;
	Set<String> regTypes;
	Set<String> regFileNames;
	Map<String, StringBuilder> lineByRegType;

	public RegulationFileSplitBytType() {
		regTypes = new HashSet<>();
		regFileNames = new HashSet<>();
	}

	/**
	 * Add a line to a 'regulatio type'
	 */
	void add(String regType, String line) {
		StringBuilder sb = lineByRegType.get(regType);
		if (sb == null) {
			sb = new StringBuilder();
			lineByRegType.put(regType, sb);
		}

		sb.append(line + "\n");
		regTypes.add(regType);
	}

	public Set<String> getRegFileNames() {
		return regFileNames;
	}

	public Collection<String> getRegTypes() {
		return regTypes;
	}

	String outputFileName(String regType) {
		String rType = Gpr.sanityzeFileName(regType);
		return path + "/regulation_" + rType + ".gff";
	}

	/**
	 * Read a block of lines an store lines by 'regulation type'
	 * @return true if there are more lines to read, false if we reached the end of file
	 */
	boolean readBlock(RegulationFileIterator regulationFileIterator) {
		if (verbose) Timer.show("\tReading block of lines starting at line " + regulationFileIterator.getLineNum());
		lineByRegType = new HashMap<>();
		for (int i = 0; i < BLOCK_SIZE; i++) {
			if (!regulationFileIterator.hasNext()) return false;

			Regulation reg = regulationFileIterator.next();
			add(reg.getRegulationType(), regulationFileIterator.getLine());
		}

		return true;
	}

	/**
	 * Save current block of lines in different files
	 */
	void saveBlock() {
		if (verbose) Timer.show("\tSaving block of lines");

		for (String regType : lineByRegType.keySet()) {
			String fileName = outputFileName(regType);
			if (verbose) {
				String createAppend = Gpr.exists(fileName) ? "Appending" : "Creating";
				Log.info("\t\t" + createAppend + " file '" + fileName + "'");
			}
			Gpr.toFile(fileName, lineByRegType.get(regType), true);
			regFileNames.add(fileName);
		}
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Read a regulation file and split into one file for each "regulation type"
	 */
	public void splitFile(RegulationFileIterator regulationFileIterator, String path) {
		this.path = path;

		// Read file by blocks of lines (more efficient)
		while (readBlock(regulationFileIterator))
			saveBlock();
		saveBlock();
	}
}
