package ca.mcgill.mcb.pcingola.snpEffect.factory;

import java.io.BufferedReader;
import java.util.HashMap;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.FrameType;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.IntergenicConserved;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Utr3prime;
import ca.mcgill.mcb.pcingola.interval.Utr5prime;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * This class creates a SnpEffectPredictor from a GFF file.
 * This includes derived formats as GTF.
 *
 * References: http://gmod.org/wiki/GFF3
 *
 * @author pcingola
 */
public abstract class SnpEffPredictorFactoryGff extends SnpEffPredictorFactory {

	public static final HashMap<String, String> typeMap;
	public static final String GENE = Gene.class.getSimpleName();
	public static final String TRANSCRIPT = Transcript.class.getSimpleName();
	public static final String EXON = Exon.class.getSimpleName();
	public static final String UTR5 = Utr5prime.class.getSimpleName();
	public static final String UTR3 = Utr3prime.class.getSimpleName();
	public static final String INTERGENIC_CONSERVED = IntergenicConserved.class.getSimpleName();

	public static final String FASTA_DELIMITER = "##FASTA";

	String version = "";
	boolean mainFileHasFasta = false; // Are sequences in the GFF file or in a separate FASTA file?

	static {
		// Initialize typeMap
		typeMap = new HashMap<String, String>();

		addTypeMap("gene", GENE);
		addTypeMap("pseudogene", TRANSCRIPT);

		//		addTypeMap("transcript", TRANSCRIPT);
		addTypeMap("mRNA", TRANSCRIPT);
		addTypeMap("tRNA", TRANSCRIPT);
		addTypeMap("snoRNA", TRANSCRIPT);
		addTypeMap("rRNA", TRANSCRIPT);
		addTypeMap("ncRNA", TRANSCRIPT);
		addTypeMap("miRNA", TRANSCRIPT);
		addTypeMap("snRNA", TRANSCRIPT);
		addTypeMap("pseudogenic_transcript", TRANSCRIPT);

		addTypeMap("exon", EXON);
		addTypeMap("pseudogenic_exon", EXON);
		addTypeMap("CDS", EXON);
		addTypeMap("start_codon", EXON);
		addTypeMap("stop_codon", EXON);
		addTypeMap("intron_CNS", EXON);

		addTypeMap("five_prime_UTR", UTR5);
		addTypeMap("5'-UTR", UTR5);
		addTypeMap("5UTR", UTR5);

		addTypeMap("three_prime_UTR", UTR3);
		addTypeMap("3'-UTR", UTR3);
		addTypeMap("3UTR", UTR3);

		addTypeMap("inter_CNS", INTERGENIC_CONSERVED);
	}

	static void addTypeMap(String typeAliasStr, String type) {
		typeMap.put(typeAliasStr.toUpperCase(), type);
	}

	public SnpEffPredictorFactoryGff(Config config) {
		super(config, 1);
		markersById = new HashMap<String, Marker>();
		genesById = new HashMap<String, Gene>();
		transcriptsById = new HashMap<String, Transcript>();
		fileName = config.getBaseFileNameGenes() + ".gff";

		frameCorrection = true;
		frameType = FrameType.GFF;
	}

	@Override
	public SnpEffectPredictor create() {
		// Read gene intervals from a file
		if (verbose) System.out.println("Reading " + version + " data file  : '" + fileName + "'");
		try {
			// We have to read the file a few times because we want to have all genes, then all transcripts, then all exons, etc.
			if (verbose) System.out.print("\tReading genes       : ");
			readGff(GENE);

			if (verbose) System.out.print("\tReading transcripts : ");
			readGff(TRANSCRIPT);

			if (verbose) System.out.print("\tReading exons       : ");
			readGff(EXON);

			// This features are not present in GFF2 and are optional in GTF 2.2
			if (!version.equals("GFF2")) {
				exonsFromCds(); // We need to create exons from CDSs before UTRs are added, since UTR require exons as parents

				if (verbose) System.out.print("\tReading UTRs (5)    : ");
				readGff(UTR5);

				if (verbose) System.out.print("\tReading UTRs (3)    : ");
				readGff(UTR3);
			}

			// Some clean-up before reading exon sequences
			beforeExonSequences();

			if (readSequences) readExonSequences();
			else if (createRandSequences) createRandSequences();

			if (verbose) System.out.println("\tTotal: " + totalSeqsAdded + " sequences added, " + totalSeqsIgnored + " sequences ignored.");

			// Finish up (fix problems, add missing info, etc.)
			finishUp();

			if (verbose) System.out.println(config.getGenome());
		} catch (Exception e) {
			if (verbose) e.printStackTrace();
			throw new RuntimeException("Error reading file '" + fileName + "'\n" + e);
		}

		return snpEffectPredictor;
	}

	/**
	 * Is 'term' a 'type'?
	 */
	boolean is(String term, String type) {
		String inType = typeMap.get(term.toUpperCase());
		if (inType == null) return false;
		return inType.equals(type);
	}

	/**
	 * Is this protein coding according to the source
	 *
	 * References: http://vega.sanger.ac.uk/info/about/gene_and_transcript_types.html
	 */
	protected boolean isProteingCoding(String biotype) {
		return biotype.equals("protein_coding") //
				|| biotype.equals("IG_C_gene") //
				|| biotype.equals("IG_D_gene") //
				|| biotype.equals("IG_J_gene") //
				|| biotype.equals("IG_V_gene") //
				|| biotype.equals("TR_C_gene") //
				|| biotype.equals("TR_D_gene") //
				|| biotype.equals("TR_J_gene") //
				|| biotype.equals("TR_V_gene") //
		;
	}

	/**
	 * Parse a line
	 * @return true if a line was parsed
	 */
	protected abstract boolean parse(String line, String typeToRead);

	@Override
	protected void readExonSequences() {
		// Read chromosome sequences and set exon sequences
		if (verbose) System.out.print("\tReading sequences   :\n");
		if (mainFileHasFasta) readExonSequencesGff(fileName); // Read from GFF file (it has a '##FASTA' delimiter)
		else super.readExonSequences(); // Read them from FASTA file
	}

	/**
	 * Read chromosome sequence from GFF3 file and extract exons' sequences
	 */
	protected void readExonSequencesGff(String gffFileName) {
		try {
			BufferedReader reader = Gpr.reader(gffFileName);

			// Get to fasta part of the file
			for (lineNum = 1; reader.ready(); lineNum++) {
				line = reader.readLine();
				if (line.equals(FASTA_DELIMITER)) {
					mainFileHasFasta = true;
					break;
				}
			}

			// Read fasta sequence
			String chromoName = null;
			StringBuffer chromoSb = new StringBuffer();
			for (; reader.ready(); lineNum++) {
				line = reader.readLine();
				if (line.startsWith(">")) { // New fasta sequence
					// Set chromosome sequences and length (create it if it doesn't exist)
					if (chromoName != null) addSequences(chromoName, chromoSb.toString()); // Add all sequences

					// Get sequence name
					int idxSpace = line.indexOf(' ');
					if (idxSpace > 0) line = line.substring(0, idxSpace);
					chromoName = Chromosome.simpleName(line.substring(1).trim()); // New chromosome name
					chromoNamesReference.add(chromoName);

					// Initialize buffer
					chromoSb = new StringBuffer();
					if (verbose) System.out.println("\t\tReading sequence '" + chromoName + "'");
				} else chromoSb.append(line.trim());
			}

			// Last chromosome
			// Set chromosome sequneces and length (create it if it doesn't exist)
			if (chromoName != null) {
				chromoLen(chromoName, chromoSb.length());
				addSequences(chromoName, chromoSb.toString()); // Add all sequences
			} else warning("Ignoring sequences for '" + chromoName + "'. Cannot find chromosome"); // Chromosome not found

			reader.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Read GFF file from the beginning looking for 'typeToRead' elements
	 */
	protected void readGff(String typeToRead) throws Exception {
		int count = 0;
		BufferedReader reader = Gpr.reader(fileName);
		if (reader == null) return; // Error

		// Parsing GFF3 (reference: http://gmod.org/wiki/GFF3)
		try {
			for (lineNum = 1; reader.ready(); lineNum++) {
				line = reader.readLine();

				// Are we done?
				if (line.equals(FASTA_DELIMITER)) {
					mainFileHasFasta = true;
					break;
				} else if (line.startsWith("#")) {
					// Ignore this line
				} else if (parse(line, typeToRead)) {
					count++;
					if (verbose) Gpr.showMark(count, MARK, "\t\t");
				}
			}
		} catch (Exception e) {
			error("Offending line (lineNum: " + lineNum + "): '" + line + "'", e);
		}

		reader.close();
		if (verbose) System.out.println((count > 0 ? "\n" : "") + "\tTotal: " + count + " " + typeToRead + "s added.");
	}
}
