package ca.mcgill.mcb.pcingola.interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import ca.mcgill.mcb.pcingola.binseq.GenomicSequences;
import ca.mcgill.mcb.pcingola.fileIterator.FastaFileIterator;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 *
 * This is just used for the Interval class.
 * It is NOT a representation of an entire genome.
 *
 * @author pcingola
 */
public class Genome extends Marker implements Serializable, Iterable<Chromosome> {

	private static final long serialVersionUID = -330362012383572257L;

	private static int genomeIdCounter = 0;

	int genomeId;
	long length = -1;
	String species;
	String version;
	String fastaDir;
	ArrayList<String> chromosomeNames;
	ArrayList<String> chromosomeNamesSorted = null;
	String chromoFastaFiles[];
	HashMap<String, Chromosome> chromosomes;
	Genes genes; // All genes, transcripts, exons, UTRs, CDS, etc.
	Boolean codingInfo = null; // Do we have coding info from genes?
	GenomicSequences genomicSequences; // Store all genomic sequences (of interest) here

	/**
	 * Create a genome from a faidx file.
	 * See "samtools faidx" command (reference http://samtools.sourceforge.net/samtools.shtml)
	 *
	 * @param genomeName : Genome's name (version)
	 * @param faidxFile : FAI file used to create all chromosomes
	 * @return
	 */
	public static Genome createFromFaidx(String genomeName, String faidxFile) {
		Genome genome = new Genome(genomeName);

		// Read the whole file
		String lines[] = Gpr.readFile(faidxFile).split("\n");
		for (String line : lines) {
			String vals[] = line.split("\t");
			String chrName = vals[0];
			int len = Gpr.parseIntSafe(vals[1]);

			// Create chromo
			Chromosome chromosome = new Chromosome(genome, 0, len, chrName);
			genome.add(chromosome);
		}
		return genome;

	}

	public Genome() {
		super();
		id = version = "";
		type = EffectType.GENOME;
		chromosomeNames = new ArrayList<String>();
		chromosomes = new HashMap<String, Chromosome>();
		genes = new Genes(this);
		genomicSequences = new GenomicSequences(this);
		setGenomeId();
	}

	public Genome(String version) {
		super(null, 0, Integer.MAX_VALUE, false, version);
		this.version = version;
		type = EffectType.GENOME;
		chromosomeNames = new ArrayList<String>();
		chromosomes = new HashMap<String, Chromosome>();
		genes = new Genes(this);
		genomicSequences = new GenomicSequences(this);
		setGenomeId();
	}

	public Genome(String version, Properties properties) {
		super(null, 0, Integer.MAX_VALUE, false, version);
		this.version = version;
		type = EffectType.GENOME;
		genes = new Genes(this);
		genomicSequences = new GenomicSequences(this);

		species = properties.getProperty(version + ".genome");
		if (species == null) throw new RuntimeException("Property: '" + version + ".genome' not found");
		species = species.trim();

		chromosomeNames = new ArrayList<String>();
		String[] chromosomeNames = propertyToStringArray(properties, version + ".chromosomes");

		// Fasta file & dir (optional)
		if (properties.getProperty(version + ".fasta_dir") != null) fastaDir = properties.getProperty(version + ".fasta_dir").trim();
		else fastaDir = "";
		if (properties.getProperty(version + ".chromo_fasta_files") != null) chromoFastaFiles = propertyToStringArray(properties, version + ".chromo_fasta_files");
		else chromoFastaFiles = new String[0];

		chromosomes = new HashMap<String, Chromosome>();
		for (String chName : chromosomeNames)
			add(new Chromosome(this, 0, 0, chName));

		setGenomeId();
	}

	/**
	 * Add a chromosome
	 */
	public synchronized void add(Chromosome chromo) {
		chromosomeNames.add(chromo.getId());
		chromosomes.put(chromo.getId(), chromo);
		chromo.setParent(this);
	}

	/**
	 * Get a sorted list of chromosomes
	 */
	public List<String> chromosomeNamesSorted() {
		if (chromosomeNamesSorted != null) return chromosomeNamesSorted; // Already done? => return previous result

		// Sort chromosomes by name
		ArrayList<Chromosome> chromosArr = new ArrayList<Chromosome>(chromosomes.size());
		chromosArr.addAll(chromosomes.values());
		Collections.sort(chromosArr);

		// Create a list and add all names to list
		chromosomeNamesSorted = new ArrayList<String>();
		for (int i = 0; i < chromosArr.size(); i++)
			chromosomeNamesSorted.add(chromosArr.get(i).getId());

		return chromosomeNamesSorted;
	}

	/**
	 * Create a chromosome named 'chromoName'
	 */
	synchronized Chromosome createChromosome(String chromoName) {
		Chromosome chr = getChromosome(chromoName);
		if (chr != null) return chr; // Already created => Nothing done (some race condition might get you here)

		String ch = Chromosome.simpleName(chromoName);
		chr = new Chromosome(this, 0, 0, ch);
		add(chr);
		return chr;
	}

	public String[] getChromoFastaFiles() {
		return chromoFastaFiles;
	}

	/**
	 * Find chromosome 'chromoName'
	 */
	public Chromosome getChromosome(String chromoName) {
		String ch = Chromosome.simpleName(chromoName);
		return chromosomes.get(ch);
	}

	public String[] getChromosomeNames() {
		return chromosomeNames.toArray(new String[0]);
	}

	public Collection<Chromosome> getChromosomes() {
		return chromosomes.values();
	}

	/**
	 * Return chromosomes sorted by size (largest chromosomes first)
	 */
	public List<Chromosome> getChromosomesSortedSize() {
		ArrayList<Chromosome> chrs = new ArrayList<Chromosome>();
		chrs.addAll(chromosomes.values());

		// Sort by size (and then by name)
		Collections.sort(chrs, new Comparator<Chromosome>() {

			@Override
			public int compare(Chromosome chr1, Chromosome chr2) {
				int cmp = chr2.size() - chr1.size();
				if (cmp != 0) return cmp;
				return chr1.getId().compareTo(chr2.getId());
			}
		});

		return chrs;
	}

	public String getFastaDir() {
		return fastaDir;
	}

	public Genes getGenes() {
		return genes;
	}

	/**
	 * Create a sorted list of genes (sorted by gene Id)
	 */
	public List<Gene> getGenesSorted() {
		ArrayList<Gene> genesSorted = new ArrayList<Gene>();
		genesSorted.addAll(genes.values());
		Collections.sort(genesSorted, new Comparator<Gene>() {

			@Override
			public int compare(Gene gene1, Gene gene2) {
				return gene1.getId().compareTo(gene2.getId());
			}
		});

		return genesSorted;
	}

	/**
	 * Create a sorted list of genes (sorted by genomic position)
	 */
	public List<Gene> getGenesSortedPos() {
		ArrayList<Gene> genesSorted = new ArrayList<Gene>();
		genesSorted.addAll(genes.values());
		Collections.sort(genesSorted);
		return genesSorted;
	}

	public String getGenomeId() {
		return id + "[" + genomeId + "]";
	}

	public GenomicSequences getGenomicSequences() {
		return genomicSequences;
	}

	/**
	 * Get or create a chromosome
	 */
	public Chromosome getOrCreateChromosome(String chromoName) {
		Chromosome chr = getChromosome(chromoName);
		if (chr == null) chr = createChromosome(chromoName);
		return chr;
	}

	public String getSpecies() {
		return species;
	}

	public String getVersion() {
		return version;
	}

	/**
	 * Is this chromosome in this genome?
	 */
	public boolean hasChromosome(String chromo) {
		for (String ch : chromosomeNames)
			if (ch.equals(chromo)) return true;
		return false;
	}

	/**
	 * Do we have coding info from genes?
	 */
	public boolean hasCodingInfo() {
		// Is this already calculated?
		if (codingInfo == null) {
			int countCoding = 0;

			for (Gene gene : genes)
				if (gene.isProteinCoding()) countCoding++;

			codingInfo = (countCoding != 0);
		}

		return codingInfo;
	}

	/**
	 * Do most exons have sequence?
	 * This is an indicator that something went really bad building the database.
	 *
	 * @return Check if most exons have sequence assigned.
	 */
	public boolean isMostExonsHaveSequence() {
		int exonSeq = 0, exonNoSeq = 0;

		// For each gene, transcript and exon, count the ones having sequences
		for (Gene g : getGenes())
			for (Transcript tr : g)
				for (Exon e : tr)
					if (e.getSequence().isEmpty()) exonNoSeq++;
					else exonSeq++;

		return exonNoSeq < exonSeq;
	}

	@Override
	public Iterator<Chromosome> iterator() {
		return chromosomes.values().iterator();
	}

	/**
	 * Total genome length: add all chromosomes
	 */
	public long length() {
		if (length <= 0) {
			length = 0;
			for (Chromosome chr : chromosomes.values())
				length += chr.getEnd() - chr.getStart() + 1;
		}

		return length;
	}

	/**
	 * Parse a comma separated property as a string array
	 */
	String[] propertyToStringArray(Properties properties, String attr) {
		String value = properties.getProperty(attr);
		if (value == null) return new String[0];

		String values[] = value.split("[\\s+,]");
		LinkedList<String> list = new LinkedList<String>();
		for (String val : values)
			if (val.length() > 0) list.add(val);

		return list.toArray(new String[0]);
	}

	/**
	 * Read the whole genome sequence into memory
	 * @param fastaFile : Path to a Fasta file
	 * @return true if it was successful
	 */
	public boolean readGenomeSequence(String fastaFile) {
		// Read fasta sequence
		FastaFileIterator ffi = new FastaFileIterator(fastaFile);
		for (String seq : ffi) {
			String chrName = ffi.getName();
			Chromosome chromosome = getChromosome(chrName);
			if (chromosome != null) {
				chromosome.setSequence(seq);
			} else {
				// Chromosome not found, create a new one
				chromosome = new Chromosome(this, 0, seq.length(), chrName);
				chromosome.setSequence(seq);
				add(chromosome);
			}
		}

		return true;
	}

	/**
	 * Remove a chromosome
	 * WARINIG: Doesn't check any dependencies!
	 */
	public void remove(Chromosome chromo) {
		chromosomeNames.remove(chromo.getId());
		chromosomes.remove(chromo.getId());
	}

	/**
	 * Save genome to file
	 */
	public void save(String fileName) {
		// Create a list of 'markers' to save
		Markers markers = new Markers();
		markers.add(this);

		for (Chromosome chr : this)
			markers.add(chr);

		for (Gene g : this.getGenes())
			markers.add(g);

		// Save markers to file
		markers.save(fileName);
	}

	/**
	 * Parse a line from a serialized file
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		version = markerSerializer.getNextField();
		species = markerSerializer.getNextField();

		for (Marker m : markerSerializer.getNextFieldMarkers())
			add((Chromosome) m);

	}

	/**
	 * Create a string to serialize to a file
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + version //
				+ "\t" + species //
				+ "\t" + markerSerializer.save((Iterable) chromosomes.values()) //
				;
	}

	private void setGenomeId() {
		genomeId = genomeIdCounter++;
	}

	/**
	 * Show number of genes, transcripts & exons
	 * @return true : If there is an error condition (most exons do not have sequences)
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// Initialize counters
		int exonSeq = 0, exonNoSeq = 0;
		int countGenes = 0, countGenesProteinCoding = 0;
		int countTranscripts = 0, countTranscriptsProteinCoding = 0;
		int countExons = 0, countCds = 0;
		int countCheckAa = 0, countCheckDna = 0;
		int countUtrs = 0;
		int errorProteinLength = 0;
		int errorProteinStopCodons = 0;
		int warningStopCodon = 0;
		int errorStartCodon = 0;
		int errorTr = 0;
		Genes genes = getGenes();

		// For each gene
		for (Gene g : genes) {
			countGenes++;
			if (g.isProteinCoding()) countGenesProteinCoding++;

			for (Transcript tr : g) {
				if (tr.isProteinCoding()) countTranscriptsProteinCoding++;
				if (tr.isAaCheck()) countCheckAa++;
				if (tr.isDnaCheck()) countCheckDna++;

				int numCds = tr.getCds().size();
				int numExons = tr.subIntervals().size();

				countTranscripts++;
				countExons += numExons;
				countCds += numCds;

				for (Exon e : tr) {
					if (e.getSequence().isEmpty()) exonNoSeq++;
					else exonSeq++;
				}

				//---
				// Transcript sanity check: Check if there are any common errors in protein coding transcript
				//---
				if (tr.isProteinCoding()) {
					boolean hasError = false;

					if (!tr.getUtrs().isEmpty()) countUtrs++;

					if (tr.isErrorProteinLength()) {
						hasError = true;
						errorProteinLength++; // Protein length error
					}

					if (tr.isErrorStopCodonsInCds()) {
						hasError = true;
						errorProteinStopCodons++; // Protein has STOP codons in CDS
					}

					if (tr.isWarningStopCodon()) {
						// Note: This is considered a warning, not an error (sometimes the annotations exclude STOP codon on pourpose, although GTF say they should not)
						warningStopCodon++; // Protein does not end with STOP codon
					}

					if (tr.isErrorStartCodon()) {
						hasError = true;
						errorStartCodon++; // Protein does not start with START codon
					}

					if (hasError) errorTr++;
				}
			}
		}

		// Show summary
		double avgTrPerGene = countTranscripts / ((double) countGenes);
		double avgExonPerTr = countExons / ((double) countTranscripts);

		// Genome & Genes
		sb.append("#-----------------------------------------------\n");
		sb.append("# Genome name                : '" + species + "'" + "\n");
		sb.append("# Genome version             : '" + version + "'\n");
		sb.append("# Has protein coding info    : " + hasCodingInfo() + "\n");
		sb.append("# Genes                      : " + countGenes + "\n");
		sb.append("# Protein coding genes       : " + countGenesProteinCoding + "\n");

		// Transcripts
		sb.append("#-----------------------------------------------\n");
		sb.append("# Transcripts                : " + countTranscripts + "\n");
		sb.append(String.format("# Avg. transcripts per gene  : %.2f", avgTrPerGene) + "\n");

		// Checked transcripts
		sb.append("#-----------------------------------------------\n");
		sb.append("# Checked transcripts        : \n");
		if (countTranscriptsProteinCoding > 0) sb.append(String.format("#               AA sequences : %6d ( %.2f%% )\n", countCheckAa, (100.0 * countCheckAa / countTranscriptsProteinCoding)));
		if (countTranscripts > 0) sb.append(String.format("#              DNA sequences : %6d ( %.2f%% )\n", countCheckDna, (100.0 * countCheckDna / countTranscripts)));

		// Coding transcripts
		sb.append("#-----------------------------------------------\n");
		sb.append("# Protein coding transcripts : " + countTranscriptsProteinCoding + "\n");
		if (countTranscriptsProteinCoding > 0) {
			sb.append(String.format("#              Length errors : %6d ( %.2f%% )\n", errorProteinLength, (100.0 * errorProteinLength / countTranscriptsProteinCoding)));
			sb.append(String.format("#  STOP codons in CDS errors : %6d ( %.2f%% )\n", errorProteinStopCodons, (100.0 * errorProteinStopCodons / countTranscriptsProteinCoding)));
			sb.append(String.format("#         START codon errors : %6d ( %.2f%% )\n", errorStartCodon, (100.0 * errorStartCodon / countTranscriptsProteinCoding)));
			sb.append(String.format("#        STOP codon warnings : %6d ( %.2f%% )\n", warningStopCodon, (100.0 * warningStopCodon / countTranscriptsProteinCoding)));
			sb.append(String.format("#              UTR sequences : %6d ( %.2f%% )\n", countUtrs, (100.0 * countUtrs / countTranscripts)));
			sb.append(String.format("#               Total Errors : %6d ( %.2f%% )\n", errorTr, (100.0 * errorTr / countTranscriptsProteinCoding)));
			if (countUtrs <= 0) sb.append("# WARNING                    : No protein coding transcript has UTR\n");
		}

		// Exons & CDS
		sb.append("#-----------------------------------------------\n");
		sb.append("# Cds                        : " + countCds + "\n");
		sb.append("# Exons                      : " + countExons + "\n");
		sb.append("# Exons with sequence        : " + exonSeq + "\n");
		sb.append("# Exons without sequence     : " + exonNoSeq + "\n");
		sb.append(String.format("# Avg. exons per transcript  : %.2f", avgExonPerTr) + "\n");

		// MT check: Only check if number of chromosomes in the genome is more than one
		// Check that MT chromosome has a proper codon table
		ArrayList<Chromosome> mtChrs = new ArrayList<Chromosome>();
		for (Chromosome chr : this)
			if (chr.isMt()) mtChrs.add(chr);

		// It there a mi
		if ((getChromosomes().size() > 1) && (mtChrs.size() <= 0)) {
			sb.append("# WARNING                    : No mitochondrion chromosome found\n");
		} else {
			// Check if mitochondrion chromosome has a mitochondrion codon table
			for (Chromosome chr : mtChrs) {
				String mtCodonTable = chr.codonTable().getName();
				if (mtCodonTable.toUpperCase().indexOf("MITO") < 0) sb.append("# WARNING!                   : Mitochondrion chromosome '" + chr.getId() + "' does not have a mitochondrion codon table (codon table = '" + mtCodonTable + "'). You should update the config file.\n");
			}
		}

		// Chromosomes
		sb.append("#-----------------------------------------------\n");
		sb.append("# Number of chromosomes      : " + getChromosomes().size() + "\n");
		sb.append("# Chromosomes                : Format 'chromo_name size codon_table'\n");
		for (Chromosome chr : getChromosomesSortedSize())
			sb.append("#\t\t'" + chr.getId() + "'\t" + chr.size() + "\t" + chr.getCodonTable().getName() + "\n");

		if (countTranscriptsProteinCoding <= 0) sb.append("\n# WARNING! : No protein coding transcripts found.\n");

		// Done
		sb.append("#-----------------------------------------------\n");

		return sb.toString();
	}
}
