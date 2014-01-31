package ca.mcgill.mcb.pcingola.snpEffect.factory;

import java.io.BufferedReader;
import java.util.List;

import ca.mcgill.mcb.pcingola.collections.MultivalueHashMap;
import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * This class creates a SnpEffectPredictor from a TXT file dumped using UCSC table browser
 *
 * Fields in this table
 * 
 * Field        Example               SQL type            Info     Description
 * -----        -------               --------            ----     -----------
 * name         uc001aaa.3            varchar(255)        values   Name of gene
 * chrom        chr1                  varchar(255)        values   Reference sequence chromosome or scaffold
 * strand       +                     char(1)             values   + or - for strand
 * txStart      11873                 int(10) unsigned    range    Transcription start position
 * txEnd        14409                 int(10) unsigned    range    Transcription end position
 * cdsStart     11873                 int(10) unsigned    range    Coding region start
 * cdsEnd       11873                 int(10) unsigned    range    Coding region end
 * exonCount    3                     int(10) unsigned    range    Number of exons
 * exonStarts   11873,12612,13220,    longblob                     Exon start positions
 * exonEnds     12227,12721,14409,    longblob                     Exon end positions
 * proteinID                          varchar(40)         values   UniProt display ID for Known Genes, UniProt accession or RefSeq protein ID for UCSC Genes
 * alignID      uc001aaa.3            varchar(255)        values   Unique identifier for each (known gene, alignment position) pair
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryKnownGene extends SnpEffPredictorFactory {

	public static final String CDS_STAT_COMPLETE = "cmpl";

	int ignoredTr = 0;
	MultivalueHashMap<String, Gene> genesByName;

	public SnpEffPredictorFactoryKnownGene(Config config) {
		super(config, 0); // Zero values coordinates
		genesByName = new MultivalueHashMap<String, Gene>();
	}

	@Override
	public SnpEffectPredictor create() {
		// Read gene intervals from a file
		if (fileName == null) fileName = config.getBaseFileNameGenes() + ".txt";

		System.out.println("Reading gene intervals file : '" + fileName + "'");
		readRefSeqFile(); // Read gene info

		beforeExonSequences(); // Some clean-up before readng exon sequences

		if (readSequences) readExonSequences(); // Read chromosome sequences and set exon sequences
		// else adjustChromosomes();

		finishUp(); // Perform adjustments

		// Check that exons have sequences
		System.out.println(config.getGenome());
		boolean error = config.getGenome().isMostExonsHaveSequence();
		System.out.println("# Ignored transcripts        : " + ignoredTr);
		if (error && readSequences) throw new RuntimeException("Most Exons do not have sequences!");

		return snpEffectPredictor;
	}

	/**
	 * Find (or create) a gene for this transcript
	 * @param geneName
	 * @param trId
	 * @param chromo
	 * @param start
	 * @param end
	 * @param strand
	 * @return
	 */
	Gene findOrCreateGene(String geneName, String trId, Chromosome chromo, int start, int end, int strand, boolean isCoding) {
		Marker tr = new Marker(chromo, start, end, strand, trId);
		List<Gene> genes = genesByName.get(geneName);
		int geneIndex = 0;
		if (genes != null) {
			for (Gene gene : genes) {
				if (gene.intersects(tr)) {
					// Do we need to update gene length?
					if (start < gene.getStart()) gene.setStart(start);
					if (gene.getEnd() < end) gene.setEnd(end);

					return gene;
				}
			}

			geneIndex = genes.size() + 1;
		}

		// Need to create a new gene
		String geneId = geneName + (geneIndex > 0 ? "." + geneIndex : "");
		Gene gene = new Gene(chromo, start, end, strand, geneId, geneName, isCoding ? "Protein" : "mRNA");
		genesByName.add(geneName, gene);
		add(gene);

		return gene;
	}

	/**
	 * Read and parse GFF file
	 * @param vcfFileName
	 * @throws Exception
	 */
	protected void readRefSeqFile() {
		try {
			int count = 0;
			BufferedReader reader = Gpr.reader(fileName);
			if (reader == null) return; // Error

			for (lineNum = 1; reader.ready(); lineNum++) {
				line = reader.readLine();

				// Skip headers
				if (!line.startsWith("#")) {
					String fields[] = line.split("\t");

					if (fields.length >= 9) {
						// Parse fields
						int fieldNum = 0;
						String id = fields[fieldNum++];
						String chromoName = fields[fieldNum++];
						int strand = (fields[fieldNum++].equals("-") ? -1 : +1);

						int txstart = parsePosition(fields[fieldNum++]);
						int txend = parsePosition(fields[fieldNum++]) - 1; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )

						int cdsStart = parsePosition(fields[fieldNum++]);
						int cdsEnd = parsePosition(fields[fieldNum++]) - 1; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )

						int exonCount = Gpr.parseIntSafe(fields[fieldNum++]);
						String exonStarts = fields[fieldNum++];
						String exonEnds = fields[fieldNum++]; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )

						String proteinId = fields[fieldNum++];
						// String alignId = fields[fieldNum++]; // Not used

						//---
						// Create
						//----
						Chromosome chromo = getOrCreateChromosome(chromoName);

						// Is it protein coding?
						boolean isCoding = !proteinId.isEmpty(); // Protein ID assigned?

						// Create IDs
						String trId = uniqueTrId(id);

						// Get or create gene
						Gene gene = findOrCreateGene(proteinId, trId, chromo, txstart, txend, strand, isCoding);

						// Create transcript
						Transcript tr = new Transcript(gene, txstart, txend, strand, trId);
						tr.setProteinCoding(isCoding);
						add(tr);

						// Add Exons and CDS
						String exStartStr[] = exonStarts.split(",");
						String exEndStr[] = exonEnds.split(",");
						for (int i = 0; i < exonCount; i++) {
							// Exons
							int exStart = parsePosition(exStartStr[i]);
							int exEnd = parsePosition(exEndStr[i]) - 1; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )
							String exId = trId + ".ex." + (i + 1);
							Exon ex = new Exon(tr, exStart, exEnd, strand, exId, i);
							add(ex);

							// CDS (ony if intersects)
							if ((exStart <= cdsEnd) && (exEnd >= cdsStart)) {
								Cds cds = new Cds(tr, Math.max(cdsStart, exStart), Math.min(cdsEnd, exEnd), strand, exId);
								add(cds);
							}
						}

						count++;
						if (count % MARK == 0) System.out.print('.');
						if (count % (100 * MARK) == 0) System.out.print("\n\t");
					}
				}
			}

			reader.close();
		} catch (Exception e) {
			Gpr.debug("Offending line (lineNum: " + lineNum + "): '" + line + "'");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a new (unique) transcript ID
	 * @param id
	 * @return
	 */
	String uniqueTrId(String id) {
		if (!transcriptsById.containsKey(id)) return id;
		for (int i = 2; true; i++) {
			String trId = id + "." + i;
			if (!transcriptsById.containsKey(trId)) return trId;
		}
	}
}
