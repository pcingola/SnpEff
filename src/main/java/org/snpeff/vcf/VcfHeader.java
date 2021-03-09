package org.snpeff.vcf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snpeff.util.Log;

/**
 * Represents the header of a vcf file.
 *
 * References: http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
 *
 * @author pablocingolani
 */
public class VcfHeader {

	public static final String INFO_PREFIX = "##INFO=";
	public static final String FORMAT_PREFIX = "##FORMAT=";
	public static final String PEDIGREE_PREFIX = "##PEDIGREE=";
	public static final String CHROM_PREFIX = "#CHROM\t";

	int numberOfSamples = -1;
	StringBuffer header;
	Map<String, VcfHeaderInfo> vcfInfoById;
	Map<String, VcfHeaderFormat> vcfFormatById;
	ArrayList<String> sampleNames;
	Map<String, Integer> sampleName2Num;
	boolean chromLine = false;

	public static boolean isFormatLine(String line) {
		return line.startsWith(FORMAT_PREFIX);
	}

	public static boolean isInfoLine(String line) {
		return line.startsWith(INFO_PREFIX);
	}

	public VcfHeader() {
		header = new StringBuffer();
	}

	/**
	 * Add all missing lines from 'vcfHeader'
	 */
	public synchronized void add(VcfHeader newVcfHeader) {
		// Add missing INFO header lines
		for (VcfHeaderInfo vhi : newVcfHeader.getVcfHeaderInfo())
			if (!vhi.isImplicit() && !hasInfo(vhi)) addInfo(vhi);

		// Add missing FORMAT header lines
		for (VcfHeaderFormat vf : newVcfHeader.getVcfHeaderFormat())
			if (!vf.isImplicit() && !hasFormat(vf)) addFormat(vf);

		// Add other lines
		for (String line : newVcfHeader.getLines())
			if (!VcfHeader.isInfoLine(line) && !VcfHeader.isFormatLine(line)) addLine(line);
	}

	/**
	 * Add header line
	 */
	public synchronized void add(VcfHeaderEntry vcfHeader) {
		if (vcfHeader.isInfo()) addInfo((VcfHeaderInfo) vcfHeader);
		else if (vcfHeader.isFormat()) addFormat((VcfHeaderFormat) vcfHeader);
		else {
			addLine(vcfHeader.toString());
			resetCache(); // Invalidate cache
		}
	}

	/**
	 * Add a 'FORMAT' meta info
	 */
	public synchronized void addFormat(VcfHeaderFormat vcfFormat) {
		parse();

		// Not already added?
		if (!vcfFormatById.containsKey(vcfFormat.getId())) {
			addLine(vcfFormat.toString()); // Add line
			resetCache(); // Invalidate cache
		}
	}

	/**
	 * Add a VCF INFO header definition
	 */
	public synchronized void addInfo(VcfHeaderInfo vcfInfo) {
		parse();

		// Already added? Remove old entry
		if (hasInfo(vcfInfo)) removeInfo(vcfInfo.getId());

		// Add line
		addLine(vcfInfo.toString());
		resetCache(); // Invalidate cache
	}

	/**
	 * Add line to header (can add many lines)
	 */
	public synchronized void addLine(String newHeaderLine) {
		// Nothing to do?
		if (newHeaderLine == null || newHeaderLine.isEmpty()) return;

		// We should insert this line before '#CHROM' line
		if (chromLine) {
			// Split header
			String headerLines[] = header.toString().split("\n");
			header = new StringBuffer();

			// Find "#CHROM" line in header (should always be the last one)
			boolean added = false;
			for (String line : headerLines) {

				if (!added) { // Anything to add?
					if (line.equals(newHeaderLine)) { // Header already added?
						added = true; // Line already present? => Don't add
					} else if (line.startsWith(CHROM_PREFIX)) {
						header.append(newHeaderLine + "\n"); // Add new header right before title line
						added = true;
					}
				}

				if (!line.isEmpty()) header.append(line + "\n"); // Add non-empty lines
			}

			// Not added yet? => Add to the end
			if (!added) header.append(newHeaderLine + "\n"); // Add new header right before title line
		} else {
			appendNewLineToHeader();

			// Append header line
			header.append(newHeaderLine);
			chromLine |= newHeaderLine.startsWith(CHROM_PREFIX);
		}

		// Cache is no longer valid
		resetCache();
	}

	/**
	 * Header should end with a newline
	 */
	void appendNewLineToHeader() {
		// Do we need to append a '\n'
		char lastChar = (header.length() > 0 ? header.charAt(header.length() - 1) : '\n');
		if (lastChar != '\n' && lastChar != '\r') header.append('\n');
	}

	String[] getHeaderLines() {
		if (header == null) return new String[0];
		return header.toString().split("\n");
	}

	public String[] getLines() {
		return header.toString().split("\n");
	}

	/**
	 * Number of samples
	 */
	public int getNumberOfSamples() {
		if (numberOfSamples < 0) {
			getSampleNames();
			numberOfSamples = (sampleNames != null ? sampleNames.size() : 0);
		}
		return numberOfSamples;
	}

	/**
	 * Get pedigree (if any)
	 */
	public synchronized List<PedigreeEntry> getPedigree() {
		ArrayList<PedigreeEntry> list = new ArrayList<>();

		List<String> sampleNames = getSampleNames();
		if (sampleNames.isEmpty()) {
			Log.debug("Error: Could not get sample names");
			return list;
		}
		for (String line : getLines()) {
			if (line.startsWith(PEDIGREE_PREFIX)) {
				String l = line.substring(PEDIGREE_PREFIX.length());
				l = l.replace('<', ' ');
				l = l.replace('>', ' ');
				l = l.trim();

				String records[] = l.split(",");

				if (records.length == 2) {
					// Format:
					//     ##PEDIGREE=<Derived=Patient_01_Somatic,Original=Patient_01_Germline>
					String derived = null, original = null;
					for (String r : records) {
						String nv[] = r.split("=");
						String name = nv[0];
						String value = nv[1];

						if (name.equalsIgnoreCase("Derived")) derived = value;
						else if (name.equalsIgnoreCase("Original")) original = value;
						else throw new RuntimeException("Cannot parse PEDIGREE heade line. Field name: '" + name + "'\n\tLine: '" + line + "'");
					}

					if (derived == null) throw new RuntimeException("Cannot parse PEDIGREE heade line. Missing 'Derived' name-value pair");
					if (original == null) throw new RuntimeException("Cannot parse PEDIGREE heade line. Missing 'Original' name-value pair");

					PedigreeEntry pe = new PedigreeEntry(original, derived);
					pe.sampleNumbers(sampleNames);

					list.add(pe);
				} else if (records.length == 2) {
					// Format:
					//     ##PEDIGREE=<Child=CHILD-GENOME-ID,Mother=MOTHER-GENOME-ID,Father=FATHER-GENOME-ID>

					String father = null, mother = null, child = null;
					for (String r : records) {
						String nv[] = r.split("=");
						String name = nv[0];
						String value = nv[1];

						if (name.equalsIgnoreCase("Father")) father = value;
						else if (name.equalsIgnoreCase("Mother")) mother = value;
						else if (name.equalsIgnoreCase("Child")) child = value;
						else throw new RuntimeException("Cannot parse PEDIGREE heade line. Field name: '" + name + "'\n\tLine: '" + line + "'");
					}

					if (father == null) throw new RuntimeException("Cannot parse PEDIGREE heade line. Missing 'Father' name-value pair");
					if (mother == null) throw new RuntimeException("Cannot parse PEDIGREE heade line. Missing 'Mother' name-value pair");
					if (child == null) throw new RuntimeException("Cannot parse PEDIGREE heade line. Missing 'Child' name-value pair");

					PedigreeEntry pe = new PedigreeEntry(father, mother, child);
					pe.sampleNumbers(sampleNames);
					list.add(pe);

				} else throw new RuntimeException("Unable to parse pedigree line:\n\t'" + line + "'");
			}
		}

		return list;
	}

	/**
	 * Get sample names
	 */
	public List<String> getSampleNames() {
		if (sampleNames != null) return sampleNames;
		return parseSampleNames();
	}

	/**
	 * Sample number (position in "#CHROM" line)
	 * @return -1 if not found
	 */
	public int getSampleNum(String sameplName) {
		if (sampleName2Num == null) parseSampleNum();

		// Get sample number
		Integer num = sampleName2Num.get(sameplName);
		return num != null ? num : -1;
	}

	public synchronized Collection<VcfHeaderFormat> getVcfHeaderFormat() {
		parse();
		return vcfFormatById.values();
	}

	public synchronized VcfHeaderFormat getVcfHeaderFormat(String id) {
		parse();
		return vcfFormatById.get(id);
	}

	/**
	 * Get all VcfInfo entries
	 */
	public synchronized Collection<VcfHeaderInfo> getVcfHeaderInfo() {
		parse();
		return vcfInfoById.values();
	}

	/**
	 * Get Info type for a given ID
	 */
	public synchronized VcfHeaderInfo getVcfHeaderInfo(String id) {
		parse();
		return vcfInfoById.get(id);
	}

	/**
	 * Do we already have this 'format' entry?
	 */
	public synchronized boolean hasFormat(VcfHeaderFormat vcfFormat) {
		parse();
		return vcfFormatById.containsKey(vcfFormat.getId());
	}

	/**
	 * Do we already have this 'info' header?
	 */
	public synchronized boolean hasInfo(VcfHeaderInfo vcfInfo) {
		parse();
		return vcfInfoById.containsKey(vcfInfo.getId());
	}

	/**
	 * Parse header lines
	 */
	public synchronized void parse() {
		if (vcfInfoById != null) return;

		chromLine = header.indexOf(CHROM_PREFIX) >= 0;

		vcfInfoById = new HashMap<>();
		vcfFormatById = new HashMap<>();

		// Add standard fields
		vcfInfoById.put("CHROM", new VcfHeaderInfo("CHROM", VcfInfoType.String, "1", "Chromosome name"));
		vcfInfoById.put("POS", new VcfHeaderInfo("POS", VcfInfoType.Integer, "1", "Position in chromosome"));
		vcfInfoById.put("ID", new VcfHeaderInfo("ID", VcfInfoType.String, "1", "Variant ID"));
		vcfInfoById.put("REF", new VcfHeaderInfo("REF", VcfInfoType.String, "1", "Reference sequence"));
		vcfInfoById.put("ALT", new VcfHeaderInfo("ALT", VcfInfoType.String, "A", "Alternative sequence/s"));
		vcfInfoById.put("QUAL", new VcfHeaderInfo("QUAL", VcfInfoType.Float, "1", "Mapping quality"));
		vcfInfoById.put("FILTER", new VcfHeaderInfo("FILTER", VcfInfoType.String, "1", "Filter status"));
		vcfInfoById.put("FORMAT", new VcfHeaderInfo("FORMAT", VcfInfoType.String, "1", "Format in genotype fields"));

		// Add well known fields
		// Reference: http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
		vcfInfoById.put("AA", new VcfHeaderInfo("AA", VcfInfoType.String, "1", "Ancestral allele"));
		vcfInfoById.put("AC", new VcfHeaderInfo("AC", VcfInfoType.Integer, "A", "Allele Frequency"));
		vcfInfoById.put("AF", new VcfHeaderInfo("AF", VcfInfoType.Float, "1", "Allele Frequency"));
		vcfInfoById.put("AN", new VcfHeaderInfo("AN", VcfInfoType.Integer, "1", "Total number of alleles"));
		vcfInfoById.put("BQ", new VcfHeaderInfo("BQ", VcfInfoType.Float, "1", "RMS base quality"));
		vcfInfoById.put("CIGAR", new VcfHeaderInfo("CIGAR", VcfInfoType.String, "1", "Cigar string describing how to align an alternate allele to the reference allele"));
		vcfInfoById.put("DB", new VcfHeaderInfo("DB", VcfInfoType.Flag, "1", "dbSNP membership"));
		vcfInfoById.put("DP", new VcfHeaderInfo("DP", VcfInfoType.Integer, "1", "Combined depth across samples"));
		vcfInfoById.put("END", new VcfHeaderInfo("END", VcfInfoType.String, "1", "End position of the variant described in this record"));
		vcfInfoById.put("H2", new VcfHeaderInfo("H2", VcfInfoType.Flag, "1", "Membership in hapmap 2"));
		vcfInfoById.put("H3", new VcfHeaderInfo("H3", VcfInfoType.Flag, "1", "Membership in hapmap 3"));
		vcfInfoById.put("MQ", new VcfHeaderInfo("MQ", VcfInfoType.Float, "1", "RMS mapping quality"));
		vcfInfoById.put("MQ0", new VcfHeaderInfo("MQ0", VcfInfoType.Integer, "1", "Number of MAPQ == 0 reads covering this record"));
		vcfInfoById.put("NS", new VcfHeaderInfo("NS", VcfInfoType.Integer, "1", "Number of samples with data"));
		vcfInfoById.put("SB", new VcfHeaderInfo("SB", VcfInfoType.Float, "1", "Strand bias at this position"));
		vcfInfoById.put("SOMATIC", new VcfHeaderInfo("SOMATIC", VcfInfoType.Flag, "1", "Indicates that the record is a somatic mutation, for cancer genomics"));
		vcfInfoById.put("VALIDATED", new VcfHeaderInfo("VALIDATED", VcfInfoType.Flag, "1", "Validated by follow-up experiment"));
		vcfInfoById.put("1000G", new VcfHeaderInfo("1000G", VcfInfoType.Flag, "1", "Membership in 1000 Genomes"));

		// Structural variants
		vcfInfoById.put("IMPRECISE", new VcfHeaderInfo("IMPRECISE", VcfInfoType.Flag, "0", "Imprecise structural variation"));
		vcfInfoById.put("NOVEL", new VcfHeaderInfo("NOVEL", VcfInfoType.Flag, "0", "Indicates a novel structural variation"));
		vcfInfoById.put("END", new VcfHeaderInfo("END", VcfInfoType.Integer, "1", "End position of the variant described in this record"));
		vcfInfoById.put("SVTYPE", new VcfHeaderInfo("SVTYPE", VcfInfoType.String, "1", "Type of structural variant"));
		vcfInfoById.put("SVLEN", new VcfHeaderInfo("SVLEN", VcfInfoType.Integer, ".", "Difference in length between REF and ALT alleles"));
		vcfInfoById.put("CIPOS", new VcfHeaderInfo("CIPOS", VcfInfoType.Integer, "2", "Confidence interval around POS for imprecise variants"));
		vcfInfoById.put("CIEND", new VcfHeaderInfo("CIEND", VcfInfoType.Integer, "2", "Confidence interval around END for imprecise variants"));
		vcfInfoById.put("HOMLEN", new VcfHeaderInfo("HOMLEN", VcfInfoType.Integer, ".", "Length of base pair identical micro-homology at event breakpoints"));
		vcfInfoById.put("HOMSEQ", new VcfHeaderInfo("HOMSEQ", VcfInfoType.String, ".", "Sequence of base pair identical micro-homology at event breakpoints"));
		vcfInfoById.put("BKPTID", new VcfHeaderInfo("BKPTID", VcfInfoType.String, ".", "ID of the assembled alternate allele in the assembly file"));
		vcfInfoById.put("MEINFO", new VcfHeaderInfo("MEINFO", VcfInfoType.String, "4", "Mobile element info of the form NAME,START,END,POLARITY"));
		vcfInfoById.put("METRANS", new VcfHeaderInfo("METRANS", VcfInfoType.String, "4", "Mobile element transduction info of the form CHR,START,END,POLARITY"));
		vcfInfoById.put("DGVID", new VcfHeaderInfo("DGVID", VcfInfoType.String, "1", "ID of this element in Database of Genomic Variation"));
		vcfInfoById.put("DBVARID", new VcfHeaderInfo("DBVARID", VcfInfoType.String, "1", "ID of this element in DBVAR"));
		vcfInfoById.put("DBRIPID", new VcfHeaderInfo("DBRIPID", VcfInfoType.String, "1", "ID of this element in DBRIP"));
		vcfInfoById.put("MATEID", new VcfHeaderInfo("MATEID", VcfInfoType.String, ".", "ID of mate breakends"));
		vcfInfoById.put("PARID", new VcfHeaderInfo("PARID", VcfInfoType.String, "1", "ID of partner breakend"));
		vcfInfoById.put("EVENT", new VcfHeaderInfo("EVENT", VcfInfoType.String, "1", "ID of event associated to breakend"));
		vcfInfoById.put("CILEN", new VcfHeaderInfo("CILEN", VcfInfoType.Integer, "2", "Confidence interval around the length of the inserted material between breakends"));
		vcfInfoById.put("DP", new VcfHeaderInfo("DP", VcfInfoType.Integer, "1", "Read Depth of segment containing breakend"));
		vcfInfoById.put("DPADJ", new VcfHeaderInfo("DPADJ", VcfInfoType.Integer, ".", "Read Depth of adjacency"));
		vcfInfoById.put("CN", new VcfHeaderInfo("CN", VcfInfoType.Integer, "1", "Copy number of segment containing breakend"));
		vcfInfoById.put("CNADJ", new VcfHeaderInfo("CNADJ", VcfInfoType.Integer, ".", "Copy number of adjacency"));
		vcfInfoById.put("CICN", new VcfHeaderInfo("CICN", VcfInfoType.Integer, "2", "Confidence interval around copy number for the segment"));
		vcfInfoById.put("CICNADJ", new VcfHeaderInfo("CICNADJ", VcfInfoType.Integer, ".", "Confidence interval around copy number for the adjacency"));

		// Add SnpEff 'EFF' fields
		String eff = EffFormatVersion.VCF_INFO_EFF_NAME;
		vcfInfoById.put(eff + ".EFFECT", new VcfHeaderInfo(eff + ".EFFECT", VcfInfoType.String, ".", "SnpEff effect"));
		vcfInfoById.put(eff + ".IMPACT", new VcfHeaderInfo(eff + ".IMPACT", VcfInfoType.String, ".", "SnpEff impact (HIGH, MODERATE, LOW, MODIFIER)"));
		vcfInfoById.put(eff + ".FUNCLASS", new VcfHeaderInfo(eff + ".FUNCLASS", VcfInfoType.String, ".", "SnpEff functional class (NONE, SILENT, MISSENSE, NONSENSE)"));
		vcfInfoById.put(eff + ".CODON", new VcfHeaderInfo(eff + ".CODON", VcfInfoType.String, ".", "SnpEff codon change"));
		vcfInfoById.put(eff + ".AA", new VcfHeaderInfo(eff + ".AA", VcfInfoType.String, ".", "SnpEff amino acid change"));
		vcfInfoById.put(eff + ".AA_LEN", new VcfHeaderInfo(eff + ".AA_LEN", VcfInfoType.Integer, ".", "Protein length in amino acids"));
		vcfInfoById.put(eff + ".GENE", new VcfHeaderInfo(eff + ".GENE", VcfInfoType.String, ".", "SnpEff gene name"));
		vcfInfoById.put(eff + ".BIOTYPE", new VcfHeaderInfo(eff + ".BIOTYPE", VcfInfoType.String, ".", "SnpEff gene bio-type"));
		vcfInfoById.put(eff + ".CODING", new VcfHeaderInfo(eff + ".CODING", VcfInfoType.String, ".", "SnpEff gene coding (CODING, NON_CODING)"));
		vcfInfoById.put(eff + ".TRID", new VcfHeaderInfo(eff + ".TRID", VcfInfoType.String, ".", "SnpEff transcript ID"));
		vcfInfoById.put(eff + ".RANK", new VcfHeaderInfo(eff + ".RANK", VcfInfoType.String, ".", "SnpEff exon/intron rank"));
		vcfInfoById.put(eff + ".EXID", new VcfHeaderInfo(eff + ".EXID", VcfInfoType.String, ".", "SnpEff exon ID"));
		vcfInfoById.put(eff + ".GENOTYPE", new VcfHeaderInfo(eff + ".GENOTYPE", VcfInfoType.String, ".", "SnpEff genotype"));
		vcfInfoById.put(eff + ".GT", new VcfHeaderInfo(eff + ".GT", VcfInfoType.String, ".", "SnpEff genotype"));
		vcfInfoById.put(eff + ".GENOTYPE_NUMBER", new VcfHeaderInfo(eff + ".GENOTYPE_NUMBER", VcfInfoType.String, ".", "SnpEff genotype"));
		vcfInfoById.put(eff + ".ERRORS", new VcfHeaderInfo(eff + ".ERRORS", VcfInfoType.String, ".", "Errors, Warnings or additional Information"));
		vcfInfoById.put(eff + ".WARNINGS", new VcfHeaderInfo(eff + ".WARNINGS", VcfInfoType.String, ".", "Errors, Warnings or additional Information"));
		vcfInfoById.put(eff + ".INFOS", new VcfHeaderInfo(eff + ".INFO", VcfInfoType.String, ".", "Errors, Warnings or additional Information"));

		for (String ann : EffFormatVersion.VCF_INFO_ANN_NAMES) {
			vcfInfoById.put(ann + ".ALLELE", new VcfHeaderInfo(ann + ".ALLELE", VcfInfoType.String, ".", "SnpEff genotype"));
			vcfInfoById.put(ann + ".GENOTYPE", new VcfHeaderInfo(ann + ".GENOTYPE", VcfInfoType.String, ".", "SnpEff genotype"));
			vcfInfoById.put(ann + ".GT", new VcfHeaderInfo(ann + ".GT", VcfInfoType.String, ".", "SnpEff genotype"));
			vcfInfoById.put(ann + ".EFFECT", new VcfHeaderInfo(ann + ".EFFECT", VcfInfoType.String, ".", "SnpEff effect"));
			vcfInfoById.put(ann + ".ANNOTATION", new VcfHeaderInfo(ann + ".ANNOTATION", VcfInfoType.String, ".", "SnpEff annotation"));
			vcfInfoById.put(ann + ".IMPACT", new VcfHeaderInfo(ann + ".IMPACT", VcfInfoType.String, ".", "SnpEff impact (HIGH, MODERATE, LOW, MODIFIER)"));
			vcfInfoById.put(ann + ".GENE", new VcfHeaderInfo(ann + ".GENE", VcfInfoType.String, ".", "SnpEff gene name"));
			vcfInfoById.put(ann + ".GENEID", new VcfHeaderInfo(ann + ".GENEID", VcfInfoType.String, ".", "SnpEff gene ID"));
			vcfInfoById.put(ann + ".FEATURE", new VcfHeaderInfo(ann + ".FEATURE", VcfInfoType.String, ".", "SnpEff feature type"));
			vcfInfoById.put(ann + ".FEATUREID", new VcfHeaderInfo(ann + ".FEATUREID", VcfInfoType.String, ".", "SnpEff feature ID"));
			vcfInfoById.put(ann + ".TRID", new VcfHeaderInfo(ann + ".TRID", VcfInfoType.String, ".", "SnpEff feature ID"));
			vcfInfoById.put(ann + ".BIOTYPE", new VcfHeaderInfo(ann + ".BIOTYPE", VcfInfoType.String, ".", "SnpEff gene bio-type"));
			vcfInfoById.put(ann + ".RANK", new VcfHeaderInfo(ann + ".RANK", VcfInfoType.String, ".", "SnpEff exon/intron rank"));
			vcfInfoById.put(ann + ".EXID", new VcfHeaderInfo(ann + ".EXID", VcfInfoType.String, ".", "SnpEff exon ID"));
			vcfInfoById.put(ann + ".CODON", new VcfHeaderInfo(ann + ".CODON", VcfInfoType.String, ".", "SnpEff codon change"));
			vcfInfoById.put(ann + ".HGVS_DNA", new VcfHeaderInfo(ann + ".HGVS_DNA", VcfInfoType.String, ".", "SnpEff HGVS DNA"));
			vcfInfoById.put(ann + ".HGVS_C", new VcfHeaderInfo(ann + ".HGVS_C", VcfInfoType.String, ".", "SnpEff HGVS DNA"));
			vcfInfoById.put(ann + ".AA", new VcfHeaderInfo(ann + ".AA", VcfInfoType.String, ".", "SnpEff amino acid change"));
			vcfInfoById.put(ann + ".HGVS", new VcfHeaderInfo(ann + ".HGVS", VcfInfoType.String, ".", "SnpEff HGVS protein notation"));
			vcfInfoById.put(ann + ".HGVS_P", new VcfHeaderInfo(ann + ".HGVS_P", VcfInfoType.String, ".", "SnpEff HGVS protein notation"));
			vcfInfoById.put(ann + ".HGVS_PROT", new VcfHeaderInfo(ann + ".HGVS_PROT", VcfInfoType.String, ".", "SnpEff HGVS protein notation"));
			vcfInfoById.put(ann + ".AA_LEN", new VcfHeaderInfo(ann + ".AA_LEN", VcfInfoType.Integer, ".", "Protein length in amino acids"));
			vcfInfoById.put(ann + ".LEN_AA", new VcfHeaderInfo(ann + ".LEN_AA", VcfInfoType.Integer, ".", "Protein length in amino acids"));
			vcfInfoById.put(ann + ".POS_AA", new VcfHeaderInfo(ann + ".POS_AA", VcfInfoType.Integer, ".", "Variant's position within protein"));
			vcfInfoById.put(ann + ".AA_POS", new VcfHeaderInfo(ann + ".AA_POS", VcfInfoType.Integer, ".", "Variant's position within protein"));
			vcfInfoById.put(ann + ".CDNA_LEN", new VcfHeaderInfo(ann + ".CDNA_LEN", VcfInfoType.Integer, ".", "cDNA length"));
			vcfInfoById.put(ann + ".LEN_CDNA", new VcfHeaderInfo(ann + ".LEN_CDNA", VcfInfoType.Integer, ".", "cDNA length"));
			vcfInfoById.put(ann + ".POS_CDNA", new VcfHeaderInfo(ann + ".POS_CDNA", VcfInfoType.Integer, ".", "Variant's position within cDNA"));
			vcfInfoById.put(ann + ".CDNA_POS", new VcfHeaderInfo(ann + ".CDNA_POS", VcfInfoType.Integer, ".", "Variant's position within cDNA"));
			vcfInfoById.put(ann + ".CDS_LEN", new VcfHeaderInfo(ann + ".CDS_LEN", VcfInfoType.Integer, ".", "CDS length"));
			vcfInfoById.put(ann + ".LEN_CDS", new VcfHeaderInfo(ann + ".LEN_CDS", VcfInfoType.Integer, ".", "CSD length"));
			vcfInfoById.put(ann + ".POS_CDS", new VcfHeaderInfo(ann + ".POS_CDS", VcfInfoType.Integer, ".", "Variant's position within CDS"));
			vcfInfoById.put(ann + ".CDS_POS", new VcfHeaderInfo(ann + ".CDS_POS", VcfInfoType.Integer, ".", "Variant's position within CDS"));
			vcfInfoById.put(ann + ".DISTANCE", new VcfHeaderInfo(ann + ".DISTANCE", VcfInfoType.Integer, ".", "Distance"));
			vcfInfoById.put(ann + ".ERRORS", new VcfHeaderInfo(ann + ".ERRORS", VcfInfoType.String, ".", "Errors, Warnings or additional Information"));
			vcfInfoById.put(ann + ".WARNINGS", new VcfHeaderInfo(ann + ".WARNINGS", VcfInfoType.String, ".", "Errors, Warnings or additional Information"));
			vcfInfoById.put(ann + ".INFOS", new VcfHeaderInfo(ann + ".INFO", VcfInfoType.String, ".", "Errors, Warnings or additional Information"));
		}

		// Add SnpEff 'LOF' fields
		vcfInfoById.put("LOF.GENE", new VcfHeaderInfo("LOF.GENE", VcfInfoType.String, ".", "SnpEff LOF gene name"));
		vcfInfoById.put("LOF.GENEID", new VcfHeaderInfo("LOF.GENEID", VcfInfoType.String, ".", "SnpEff LOF gene ID"));
		vcfInfoById.put("LOF.NUMTR", new VcfHeaderInfo("LOF.NUMTR", VcfInfoType.Integer, ".", "SnpEff LOF number of transcripts in gene"));
		vcfInfoById.put("LOF.PERC", new VcfHeaderInfo("LOF.PERC", VcfInfoType.Float, ".", "SnpEff LOF percentage of transcripts in this gene that are affected"));

		// Add SnpEff 'NMD' fields
		vcfInfoById.put("NMD.GENE", new VcfHeaderInfo("NMD.GENE", VcfInfoType.String, ".", "SnpEff NMD gene name"));
		vcfInfoById.put("NMD.GENEID", new VcfHeaderInfo("NMD.GENEID", VcfInfoType.String, ".", "SnpEff NMD gene ID"));
		vcfInfoById.put("NMD.NUMTR", new VcfHeaderInfo("NMD.NUMTR", VcfInfoType.Integer, ".", "SnpEff NMD number of transcripts in gene"));
		vcfInfoById.put("NMD.PERC", new VcfHeaderInfo("NMD.PERC", VcfInfoType.Float, ".", "SnpEff NMD percentage of transcripts in this gene that are affected"));

		// Genotype fields
		vcfFormatById.put("DP", new VcfHeaderFormat("DP", VcfInfoType.Integer, "1", "Read depth"));
		vcfFormatById.put("EC", new VcfHeaderFormat("EC", VcfInfoType.Integer, "A", "Expected alternate allele counts"));
		vcfFormatById.put("FT", new VcfHeaderFormat("FT", VcfInfoType.String, "1", "Genotype filter"));
		vcfFormatById.put("GT", new VcfHeaderFormat("GT", VcfInfoType.String, "1", "Genotype"));
		vcfFormatById.put("GP", new VcfHeaderFormat("GP", VcfInfoType.Float, "1", "Genotype phred-scaled genotype posterior probabilities"));
		vcfFormatById.put("GQ", new VcfHeaderFormat("GQ", VcfInfoType.Integer, "1", "Genotype conditional genotype quality, encoded as a phred quality"));
		vcfFormatById.put("HQ", new VcfHeaderFormat("HQ", VcfInfoType.Integer, "2", "Haplotype qualities"));
		vcfFormatById.put("PL", new VcfHeaderFormat("PL", VcfInfoType.String, "G", "Normalized, Phred-scaled likelihoods for genotypes"));
		vcfFormatById.put("PQ", new VcfHeaderFormat("PQ", VcfInfoType.Integer, "1", "Phasing quality"));
		vcfFormatById.put("PS", new VcfHeaderFormat("PS", VcfInfoType.Integer, "1", "Phase set"));
		vcfFormatById.put("MQ", new VcfHeaderFormat("MQ", VcfInfoType.Integer, "1", "RMS mapping quality."));

		// Set all automatically added fields as "implicit"
		for (VcfHeaderInfo vcfInfo : vcfInfoById.values())
			vcfInfo.setImplicit(true);

		for (VcfHeaderFormat vcfInfoGenotype : vcfFormatById.values())
			vcfInfoGenotype.setImplicit(true);

		//---
		// Add all header lines
		//---
		for (String line : getLines()) {
			if (isInfoLine(line) || isFormatLine(line)) {
				VcfHeaderInfo vh = (VcfHeaderInfo) VcfHeaderEntry.factory(line);
				if (vh.isFormat()) vcfFormatById.put(vh.getId(), (VcfHeaderFormat) vh);
				else vcfInfoById.put(vh.getId(), vh);
			}
		}
	}

	// Split header
	protected synchronized List<String> parseSampleNames() {
		String headerLines[] = header.toString().split("\n");

		// Find "#CHROM" line in header
		sampleNames = new ArrayList<>();
		for (String line : headerLines) {
			if (line.startsWith("#CHROM")) {
				chromLine = true;

				// This line contains all the sample names (starting on column 9)
				String titles[] = line.split("\t");

				// Create a list of names
				for (int i = 9; i < titles.length; i++)
					sampleNames.add(titles[i]);

				// Done
				return sampleNames;
			}
		}

		// Not found
		return sampleNames;
	}

	protected synchronized void parseSampleNum() {
		sampleName2Num = new HashMap<>();

		// Create mapping
		int count = 0;
		if (getSampleNames() != null) {
			for (String name : getSampleNames()) {
				sampleName2Num.put(name, count++);
			}
		}
	}

	/**
	 * Remove header line starting with a prefix
	 */
	public synchronized void remove(String linePrefix) {
		// We should insert this line before '#CHROM' line
		// Split header
		String headerLines[] = header.toString().split("\n");
		header = new StringBuffer();

		// Find "#CHROM" line in header (should always be the last one)
		for (String line : headerLines) {
			if (line.startsWith(linePrefix)) continue; // Skip this line
			if (!line.isEmpty()) header.append(line + "\n"); // Add non-empty lines
		}

		appendNewLineToHeader();
	}

	/**
	 * Remove header line matching an INFO field
	 */
	public synchronized void removeInfo(String infoId) {
		// We should insert this line before '#CHROM' line
		// Split header
		String headerLines[] = header.toString().split("\n");
		header = new StringBuffer();

		// Find "#CHROM" line in header (should always be the last one)
		for (String line : headerLines) {
			if (isInfoLine(line)) {
				VcfHeaderInfo vhinfo = (VcfHeaderInfo) VcfHeaderEntry.factory(line); // Parse INFO line
				if (vhinfo.getId().equals(infoId)) continue; // Skip this line
			}

			if (!line.isEmpty()) header.append(line + "\n"); // Add non-empty lines
		}

		appendNewLineToHeader();
	}

	void resetCache() {
		vcfInfoById = null;
		vcfFormatById = null;
	}

	/**
	 * Get header information
	 */
	@Override
	public String toString() {
		if (header.length() <= 0) return "";

		// Delete last character, if it's a '\n' or a '\r'
		for (char c = header.charAt(header.length() - 1); (c == '\n') || (c == '\r'); c = header.charAt(header.length() - 1))
			header.deleteCharAt(header.length() - 1);

		return header.toString();
	}

}
