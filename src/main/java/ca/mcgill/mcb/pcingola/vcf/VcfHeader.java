package ca.mcgill.mcb.pcingola.vcf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Represents the header of a vcf file.
 *
 * References: http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
 *
 * @author pablocingolani
 *
 */
public class VcfHeader {

	int numberOfSamples = -1;
	StringBuffer header;
	HashMap<String, VcfHeaderInfo> vcfInfoById;
	HashMap<String, VcfInfoGenotype> vcfInfoGenotypeById;
	ArrayList<String> sampleNames;
	boolean chromLine = false;

	public VcfHeader() {
		header = new StringBuffer();
	}

	/**
	 * Add a VCF INFO header definition
	 * @param vcfInfo
	 */
	public void add(VcfHeaderInfo vcfInfo) {
		parseInfoLines();

		// Not already added?
		if (!vcfInfoById.containsKey(vcfInfo.getId())) {
			addLine(vcfInfo.toString()); // Add line
			resetCache(); // Invalidate cache
		}
	}

	/**
	 * Add a 'FORMAT' meta info
	 * @param vcfGenotypeStr
	 */
	public void addFormat(VcfInfoGenotype vcfInfoGenotype) {
		parseInfoLines();

		// Not already added?
		if (!vcfInfoGenotypeById.containsKey(vcfInfoGenotype.getId())) {
			addLine(vcfInfoGenotype.toString()); // Add line
			resetCache(); // Invalidate cache
		}
	}

	/**
	 * Add line to header (can add many lines)
	 * @return
	 */
	public void addLine(String newHeaderLine) {
		// Nothing to do?
		if (newHeaderLine == null) return;

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
					} else if (line.startsWith("#CHROM")) {
						header.append(newHeaderLine + "\n"); // Add new header right before title line
						added = true;
					}
				}

				if (!line.isEmpty()) header.append(line + "\n"); // Add non-empty lines
			}

			// Not added yet? => Add to the end
			if (!added) header.append(newHeaderLine + "\n"); // Add new header right before title line
		} else {
			// Do we need to append a '\n'
			char lastChar = (header.length() > 0 ? header.charAt(header.length() - 1) : '\n');
			if (lastChar != '\n' && lastChar != '\r') header.append('\n');

			// Append header line
			header.append(newHeaderLine);
			chromLine |= newHeaderLine.startsWith("#CHROM\t");
		}

		// Cache is no longer valid
		resetCache();
	}

	public String[] getLines() {
		return header.toString().split("\n");
	}

	/**
	 * Number of samples
	 * @return
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
	public List<PedigreeEnrty> getPedigree() {
		ArrayList<PedigreeEnrty> list = new ArrayList<PedigreeEnrty>();

		List<String> sampleNames = getSampleNames();

		for (String line : getLines()) {
			if (line.startsWith("##PEDIGREE=")) {
				String l = line.substring("##PEDIGREE=".length());
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

					PedigreeEnrty pe = new PedigreeEnrty(original, derived);
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

					PedigreeEnrty pe = new PedigreeEnrty(father, mother, child);
					pe.sampleNumbers(sampleNames);
					list.add(pe);

				} else throw new RuntimeException("Unable to parse pedigree line:\n\t'" + line + "'");
			}
		}

		return list;
	}

	/**
	 * Get sample names
	 * @return
	 */
	public List<String> getSampleNames() {
		if (sampleNames != null) return sampleNames;

		// Split header
		String headerLines[] = header.toString().split("\n");

		// Find "#CHROM" line in header
		for (String line : headerLines) {
			if (line.startsWith("#CHROM")) {
				chromLine = true;

				// This line contains all the sample names (starting on column 9)
				String titles[] = line.split("\t");

				// Create a list of names
				sampleNames = new ArrayList<String>();
				for (int i = 9; i < titles.length; i++)
					sampleNames.add(titles[i]);

				// Done
				return sampleNames;
			}
		}

		// Not found
		return null;
	}

	/**
	 * Get all VcfInfo entries
	 * @return
	 */
	public Collection<VcfHeaderInfo> getVcfInfo() {
		parseInfoLines();
		return vcfInfoById.values();
	}

	/**
	 * Get Info type for a given ID
	 * @param id
	 * @return
	 */
	public VcfHeaderInfo getVcfInfo(String id) {
		parseInfoLines();
		return vcfInfoById.get(id);
	}

	public HashMap<String, VcfHeaderInfo> getVcfInfoById() {
		parseInfoLines();
		return vcfInfoById;
	}

	public VcfInfoGenotype getVcfInfoGenotype(String id) {
		parseInfoLines();
		return vcfInfoGenotypeById.get(id);
	}

	/**
	 * Parse INFO fields from header
	 */
	public void parseInfoLines() {

		chromLine = header.indexOf("#CHROM") >= 0;

		if (vcfInfoById == null) {
			vcfInfoById = new HashMap<String, VcfHeaderInfo>();

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
			vcfInfoById.put("EFF.EFFECT", new VcfHeaderInfo("EFF.EFFECT", VcfInfoType.String, ".", "SnpEff effect"));
			vcfInfoById.put("EFF.IMPACT", new VcfHeaderInfo("EFF.IMPACT", VcfInfoType.String, ".", "SnpEff impact (HIGH, MODERATE, LOW, MODIFIER)"));
			vcfInfoById.put("EFF.FUNCLASS", new VcfHeaderInfo("EFF.FUNCLASS", VcfInfoType.String, ".", "SnpEff functional class (NONE, SILENT, MISSENSE, NONSENSE)"));
			vcfInfoById.put("EFF.CODON", new VcfHeaderInfo("EFF.CODON", VcfInfoType.String, ".", "SnpEff codon change"));
			vcfInfoById.put("EFF.AA", new VcfHeaderInfo("EFF.AA", VcfInfoType.String, ".", "SnpEff amino acid change"));
			vcfInfoById.put("EFF.AA_LEN", new VcfHeaderInfo("EFF.AA_LEN", VcfInfoType.Integer, ".", "Protein length in amino acids"));
			vcfInfoById.put("EFF.GENE", new VcfHeaderInfo("EFF.GENE", VcfInfoType.String, ".", "SnpEff gene name"));
			vcfInfoById.put("EFF.BIOTYPE", new VcfHeaderInfo("EFF.BIOTYPE", VcfInfoType.String, ".", "SnpEff gene bio-type"));
			vcfInfoById.put("EFF.CODING", new VcfHeaderInfo("EFF.CODING", VcfInfoType.String, ".", "SnpEff gene coding (CODING, NON_CODING)"));
			vcfInfoById.put("EFF.TRID", new VcfHeaderInfo("EFF.TRID", VcfInfoType.String, ".", "SnpEff transcript ID"));
			vcfInfoById.put("EFF.RANK", new VcfHeaderInfo("EFF.RANK", VcfInfoType.String, ".", "SnpEff exon/intron rank"));
			vcfInfoById.put("EFF.EXID", new VcfHeaderInfo("EFF.EXID", VcfInfoType.String, ".", "SnpEff exon ID"));

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
			vcfInfoGenotypeById = new HashMap<String, VcfInfoGenotype>();
			vcfInfoGenotypeById.put("DP", new VcfInfoGenotype("DP", VcfInfoType.Integer, "1", "Read depth"));
			vcfInfoGenotypeById.put("EC", new VcfInfoGenotype("EC", VcfInfoType.Integer, "A", "Expected alternate allele counts"));
			vcfInfoGenotypeById.put("FT", new VcfInfoGenotype("FT", VcfInfoType.String, "1", "Genotype filter"));
			vcfInfoGenotypeById.put("GT", new VcfInfoGenotype("GT", VcfInfoType.String, "1", "Genotype"));
			vcfInfoGenotypeById.put("GP", new VcfInfoGenotype("GP", VcfInfoType.Float, "1", "Genotype phred-scaled genotype posterior probabilities"));
			vcfInfoGenotypeById.put("GQ", new VcfInfoGenotype("GQ", VcfInfoType.Integer, "1", "Genotype conditional genotype quality, encoded as a phred quality"));
			vcfInfoGenotypeById.put("HQ", new VcfInfoGenotype("HQ", VcfInfoType.Integer, "2", "Haplotype qualities"));
			vcfInfoGenotypeById.put("PL", new VcfInfoGenotype("PL", VcfInfoType.String, "G", "Normalized, Phred-scaled likelihoods for genotypes"));
			vcfInfoGenotypeById.put("PQ", new VcfInfoGenotype("PQ", VcfInfoType.Integer, "1", "Phasing quality"));
			vcfInfoGenotypeById.put("PS", new VcfInfoGenotype("PS", VcfInfoType.Integer, "1", "Phase set"));
			vcfInfoGenotypeById.put("MQ", new VcfInfoGenotype("MQ", VcfInfoType.Integer, "1", "RMS mapping quality."));

			// Set all automatically added fields as "implicit"
			for (VcfHeaderInfo vcfInfo : vcfInfoById.values())
				vcfInfo.setImplicit(true);

			for (VcfInfoGenotype vcfInfoGenotype : vcfInfoGenotypeById.values())
				vcfInfoGenotype.setImplicit(true);

			//---
			// Add all INFO fields from header
			//---
			for (String line : getLines()) {
				if (line.startsWith("##INFO=") || line.startsWith("##FORMAT=")) {
					VcfHeaderInfo vcfInfo = VcfHeaderInfo.factory(line);
					if (vcfInfo instanceof VcfInfoGenotype) vcfInfoGenotypeById.put(vcfInfo.getId(), (VcfInfoGenotype) vcfInfo);
					else vcfInfoById.put(vcfInfo.getId(), vcfInfo);
				}
			}
		}
	}

	void resetCache() {
		vcfInfoById = null;
	}

	/**
	 * Get header information
	 * @return
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
