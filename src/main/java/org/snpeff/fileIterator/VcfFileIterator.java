package org.snpeff.fileIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.snpeff.fileIterator.parser.Parser;
import org.snpeff.interval.Genome;
import org.snpeff.snpEffect.Config;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfHeader;

/**
 * Opens a VCF file and iterates over all entries (i.e. VCF lines in the file)
 *
 * Format: VCF 4.1
 *
 * Reference: 	http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
 * 				Old 4.0 format: http://www.1000genomes.org/wiki/doku.php?id=1000_genomes:analysis:vcf4.0
 *
 * 1. CHROM chromosome: an identifier from the reference genome. All entries for a specific CHROM should form a contiguous block within the VCF file.(Alphanumeric String, Required)
 * 2. POS position: The reference position, with the 1st base having position 1. Positions are sorted numerically, in increasing order, within each reference sequence CHROM. (Integer, Required)
 * 3. ID semi-colon separated list of unique identifiers where available. If this is a dbSNP variant it is encouraged to use the rs number(s). No identifier should be present in more than one data record. If there is no identifier available, then the missing value should be used. (Alphanumeric String)
 * 4. REF reference base(s): Each base must be one of A,C,G,T,N. Bases should be in uppercase. Multiple bases are permitted. The value in the POS field refers to the position of the first base in the String. For InDels, the reference String must include the base before the event (which must be reflected in the POS field). (String, Required).
 * 5. ALT comma separated list of alternate non-reference alleles called on at least one of the samples. Options are base Strings made up of the bases A,C,G,T,N, or an angle-bracketed ID String (”<ID>”). If there are no alternative alleles, then the missing value should be used. Bases should be in uppercase. (Alphanumeric String; no whitespace, commas, or angle-brackets are permitted in the ID String itself)
 * 6. QUAL phred-scaled quality score for the assertion made in ALT. i.e. give -10log_10 prob(call in ALT is wrong). If ALT is 0/0 (no variant) then this is -10log_10 p(variant), and if ALT is not ”.” this is -10log_10 p(no variant). High QUAL scores indicate high confidence calls. Although traditionally people use integer phred scores, this field is permitted to be a floating point to enable higher resolution for low confidence calls if desired. (Numeric)
 * 7. FILTER filter: PASS if this position has passed all filters, i.e. a call is made at this position. Otherwise, if the site has not passed all filters, a semicolon-separated list of codes for filters that fail. e.g. “q10;s50” might indicate that at this site the quality is below 10 and the number of samples with data is below 50% of the total number of samples. “0” is reserved and should not be used as a filter String. If filters have not been applied, then this field should be set to the missing value. (Alphanumeric String)
 * 8. INFO additional information: (Alphanumeric String) INFO fields are encoded as a semicolon-separated series of short keys with optional values in the format: <key>=<data>[,data]. Arbitrary keys are permitted, although the following sub-fields are reserved (albeit optional):
 *        - AA ancestral allele
 *        - AC allele count in genotypes, for each ALT allele, in the same order as listed
 *        - AF allele frequency for each ALT allele in the same order as listed: use this when estimated from primary data, not called genotypes
 *        - AN total number of alleles in called genotypes
 *        - BQ RMS base quality at this position
 *        - CIGAR cigar string describing how to align an alternate allele to the reference allele
 *        - DB dbSNP membership
 *        - DP combined depth across samples, e.g. DP=154
 *        - END end position of the variant described in this record (esp. for CNVs)
 *        - H2 membership in hapmap2
 *        - MQ RMS mapping quality, e.g. MQ=52
 *        - MQ0 Number of MAPQ == 0 reads covering this record
 *        - NS Number of samples with data
 *        - SB strand bias at this position
 *        - SOMATIC indicates that the record is a somatic mutation, for cancer genomics
 *        - VALIDATED validated by follow-up experiment
 *
 * Warning: You can have more than one variant (and variant type) per VCF line (i.e. VCfEntry), e.g.:
 *          	TTG	->	TTGTG,T					Insertion of 'TG' and deletion of 'TG'
 *          	TA	->	T,TT					Deletion of 'A' and SNP (A replaced by T)
 *				T	->	TTTTGTG,TTTTG,TTGTG		Insertion of 'TTTGTG', insertion of 'TTTG' and insertion of 'TGTG'
 * 
 *
 * @author pcingola
 */
public class VcfFileIterator extends MarkerFileIterator<VcfEntry> implements Parser<VcfEntry> {

	public static final String MISSING = "."; // Missing value
	private static final String EMPTY = "";

	boolean parseNow = true;
	boolean headeSection = false;
	boolean errorIfUnsorted = false;
	boolean expandIub;
	VcfHeader header = new VcfHeader();
	String chrPrev = "";
	int posPrev = -1;

	public VcfFileIterator() {
		super((String) null, 1);
		init();
	}

	public VcfFileIterator(BufferedReader reader) {
		super(reader, 1);
		init();
	}

	public VcfFileIterator(String fileName) {
		super(fileName, 1);
		init();
	}

	public VcfFileIterator(String fileName, Genome genome) {
		super(fileName, genome, 1);
		init();
	}

	/**
	 * Get sample names
	 */
	public List<String> getSampleNames() {
		return header.getSampleNames();
	}

	/**
	 * Get VcfHeader
	 */
	public VcfHeader getVcfHeader() {
		return header;
	}

	protected void init() {
		expandIub = (Config.get() != null ? Config.get().isExpandIub() : true);
	}

	public boolean isExpandIub() {
		return expandIub;
	}

	public boolean isHeadeSection() {
		return headeSection;
	}

	@Override
	public Collection<VcfEntry> parse(String str) {
		LinkedList<VcfEntry> list = new LinkedList<>();
		list.add(parseVcfLine(str));
		return list;
	}

	/**
	 * Parse a line from a VCF file
	 */
	public VcfEntry parseVcfLine(String line) {
		try {
			if (line.startsWith("#")) { // Header?
				header.addLine(line);
			} else if ((line.length() > 0) && (!line.startsWith("#"))) { // Vcf entry?
				return new VcfEntry(this, line, lineNum, parseNow);
			}
		} catch (Throwable t) {
			Log.debug("Fatal error reading file '" + fileName + "' (line: " + lineNum + "):\n" + line);
			throw new RuntimeException(t);
		}
		// Could not create a VcfEntry from this line (e.g. header line)
		return null;
	}

	/**
	 * Read a field an return a value
	 */
	public String readField(String fields[], int fieldNum) {
		if (fields.length > fieldNum) {
			if (fields[fieldNum].equals(MISSING)) return EMPTY;
			return fields[fieldNum];
		}
		return EMPTY;
	}

	/**
	 * Read only header info
	 */
	public VcfHeader readHeader() {
		// No more header to read?
		if ((nextLine != null) && !nextLine.startsWith("#")) return header;

		try {
			while (ready()) {
				line = readLine();
				if (line == null) return null; // End of file?
				if (!line.startsWith("#")) {
					nextLine = line;
					return header; // End of header?
				}

				header.addLine(line);
			}
		} catch (IOException e) {
			throw new RuntimeException("Error reading file '" + fileName + "'. Line ignored:\n\tLine (" + lineNum + "):\t'" + line + "'");
		}

		return header;
	}

	@Override
	protected VcfEntry readNext() {
		// Read another line from the file
		try {
			headeSection = (lineNum == 0); // First line is header (even if missing)
			while (ready()) {
				line = readLine();
				if (line == null) return null; // End of file?

				VcfEntry vcfEntry = parseVcfLine(line);
				if (vcfEntry != null) {
					// Check that lies are sorted by position
					if (errorIfUnsorted //
							&& vcfEntry.getChromosomeName().equals(chrPrev) //
							&& vcfEntry.getStart() < posPrev) { //
						throw new RuntimeException("VCF file " //
								+ (fileName != null ? "'" + fileName + "'" : "") //
								+ " is not sorted, genomic position " //
								+ chrPrev + ":" + (posPrev + 1) //
								+ " is before " //
								+ chrPrev + ":" + (vcfEntry.getStart() + 1) //
						);
					}

					// Debug mode? Perform some sanity checks
					if (debug) {
						String err = vcfEntry.check();
						// Check that file is sorted
						if (vcfEntry.getChromosomeName().equals(chrPrev) && vcfEntry.getStart() < posPrev) err += "File is not sorted: Position '" + vcfEntry.getChromosomeName() + ":" + (vcfEntry.getStart() + 1) + "' after position '" + chrPrev + ":" + (posPrev + 1) + "'";
						// Any errors? Report them
						if (!err.isEmpty()) System.err.println("WARNING: Malformed VCF entry" + (fileName != null ? "file '" + fileName + "'" : "") + ", line " + lineNum + ":\n" + "\tEntry  : " + vcfEntry + "\n" + "\tErrors :\n" + Gpr.prependEachLine("\t\t", err));
					}

					chrPrev = vcfEntry.getChromosomeName();
					posPrev = vcfEntry.getStart();

					// Return new entry
					return vcfEntry;
				} else headeSection = true;
			}
		} catch (IOException e) {
			throw new RuntimeException("Error reading file '" + fileName + "'. Line ignored:\n\tLine (" + lineNum + "):\t'" + line + "'");
		}
		return null;
	}

	@Override
	public void setCreateChromos(boolean createChromos) {
		this.createChromos = createChromos;
	}

	public void setErrorIfUnsorted(boolean errorIfUnsorted) {
		this.errorIfUnsorted = errorIfUnsorted;
	}

	public void setExpandIub(boolean expandIub) {
		this.expandIub = expandIub;
	}

	@Override
	public void setInOffset(int inOffset) {
		throw new RuntimeException("Cannot set input offset on VCF file!");
	}

	/**
	 * Should we parse vcfEntries later? (lazy parsing)
	 */
	public void setParseNow(boolean parseNow) {
		this.parseNow = parseNow;
	}

	/**
	 * Set header
	 */
	public void setVcfHeader(VcfHeader header) {
		this.header = header;
	}

}
