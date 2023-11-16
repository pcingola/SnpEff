package org.snpeff.fileIterator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.snpeff.interval.Chromosome;

/**
 * Opens a fasta file and iterates over all fasta sequences in the file
 *
 * @author pcingola
 */
public class FastaFileIterator extends FileIterator<String> {

	private static enum State {
		DESCRIPTION, // Description parser state.
		SEQUENCE, // Sequence parser state.
		COMPLETE; // Complete parser state.
	}

	public static String TRANSCRIPT_ID_SEPARATORS_REGEX = "[ \t:,.=;]";
	public static char TRANSCRIPT_ID_SEPARATORS[] = TRANSCRIPT_ID_SEPARATORS_REGEX.substring(1, TRANSCRIPT_ID_SEPARATORS_REGEX.length() - 1).toCharArray();

	Pattern transcriptPattern = Pattern.compile("transcript:(\\S*)");
	String header = null;
	String nextHeader = null;

	public FastaFileIterator(String fastaFileName) {
		super(fastaFileName);
	}

	/**
	 * Try to parse IDs from a fasta header
	 */
	public List<String> fastaHeader2Ids() {
		String fastaHeaderLine = getName();
		List<String> list = new LinkedList<>();

		// Try using some separators individually
		for (char sep : TRANSCRIPT_ID_SEPARATORS) {
			String ids[] = fastaHeaderLine.split(sep + "");
			for (String id : ids)
				list.add(id);
		}

		// Try using some separators together
		String ids[] = fastaHeaderLine.split(TRANSCRIPT_ID_SEPARATORS_REGEX);
		for (String id : ids)
			list.add(id);

		return list;
	}

	/**
	 * Current sequence header
	 */
	public String getHeader() {
		return header;
	}

	/**
	 * Sequence name (first 'word')
	 *
	 * It extracts the characters after the leading '>' and before the first space, then
	 * removes leading 'chr', 'chr:', etc.
	 */
	public String getName() {
		if (header == null) return "";
		String name = header.split("\\s+")[0];
		return Chromosome.simpleName(name);
	}

	/**
	 * Get transcript name from FASTA header (ENSEMBL protein files)
	 *
	 * Format example:
	 *     '>ENSP00000356130 pep:known chromosome:GRCh37:1:205111633:205180694:-1 gene:ENSG00000133059 transcript:ENST00000367162'
	 */
	public String getIdFromFastaHeader() {
		Matcher mmatcher = transcriptPattern.matcher(getHeader());
		if (mmatcher.find()) return mmatcher.group(1);
		return getName();  // Nothing found? just return the name
	}

	/**
	 * Handle header
	 */
	void header(String line) {
		if (header == null) header = line.substring(1).trim();
		else header = nextHeader;
		nextHeader = line.substring(1).trim(); // Remove starting '>' as well as leading and trailing spaces
	}

	/**
	 * Read a sequence from the file
	 */
	@Override
	protected String readNext() {
		StringBuffer sb = new StringBuffer();
		try {
			State state = State.DESCRIPTION;
			while (ready()) {
				if (line == null) line = readLine();

				switch (state) {
				case DESCRIPTION:
					if (line.startsWith(">")) {
						state = State.SEQUENCE;
						header(line);
					}
					break;

				case SEQUENCE:
					if (line.startsWith(">")) {
						state = State.COMPLETE;
						header(line);
					} else sb.append(line.trim());
					state = State.COMPLETE;
					break;

				case COMPLETE:
					if (line.startsWith(">")) {
						header(line);
						state = State.COMPLETE;
						return sb.toString(); // We finished reading this sequence
					} else sb.append(line.trim());
					break;

				default:
					throw new RuntimeException("Unkown state '" + state + "'");
				}

				line = null;
			}

			if (state == State.COMPLETE) return sb.toString();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// Not finished reading a sequence
		return null;
	}
}
