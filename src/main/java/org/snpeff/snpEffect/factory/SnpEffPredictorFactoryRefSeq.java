package org.snpeff.snpEffect.factory;

import java.io.BufferedReader;
import java.util.List;

import org.snpeff.collections.MultivalueHashMap;
import org.snpeff.interval.BioType;
import org.snpeff.interval.Cds;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.FrameType;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * This class creates a SnpEffectPredictor from a TXT file dumped using UCSC table browser
 *
 * RefSeq table schema: http://genome.ucsc.edu/cgi-bin/hgTables
 *
 * field           example               SQL type                                 info     description
 * bin             585                   smallint(5)                              range    Indexing field to speed chromosome range queries.
 * name            NR_026818             varchar(255)                             values   Name of gene (usually transcript_id from GTF)
 * chrom           chr1                  varchar(255)                             values   Reference sequence chromosome or scaffold
 * strand          -                     char(1)                                  values   + or - for strand
 * txStart         34610                 int(10)                                  range    Transcription start position
 * txEnd           36081                 int(10)                                  range    Transcription end position
 * cdsStart        36081                 int(10)                                  range    Coding region start
 * cdsEnd          36081                 int(10)                                  range    Coding region end
 * exonCount       3                     int(10)                                  range    Number of exons
 * exonStarts      34610,35276,35720,    longblob                                          Exon start positions
 * exonEnds        35174,35481,36081,    longblob                                          Exon end positions
 * score           0                     int(11)                                  range
 * name2           FAM138A               varchar(255)                             values   Alternate name (e.g. gene_id from GTF)
 * cdsStartStat    unk                   enum('none', 'unk', 'incmpl', 'cmpl')    values   enum('none','unk','incmpl','cmpl')
 * cdsEndStat      unk                   enum('none', 'unk', 'incmpl', 'cmpl')    values   enum('none','unk','incmpl','cmpl')
 * exonFrames      -1,-1,-1,             longblob                                          Exon frame {0,1,2}, or -1 if no frame for exon
 *
 * Refseq Accession format (i.e. NM_ NR_ codes) : http://www.ncbi.nlm.nih.gov/RefSeq/key.html
 *
 * Accession       Molecule   Method       Note
 * AC_123456       Genomic    Mixed        Alternate complete genomic molecule. This prefix is used for records that are provided to reflect an alternate assembly or annotation. Primarily used for viral, prokaryotic records.
 * AP_123456       Protein    Mixed        Protein products; alternate protein record. This prefix is used for records that are provided to reflect an alternate assembly or annotation. The AP_ prefix was originally designated for bacterial proteins but this usage was changed.
 * NC_123456       Genomic    Mixed        Complete genomic molecules including genomes, chromosomes, organelles, plasmids.
 * NG_123456       Genomic    Mixed        Incomplete genomic region; supplied to support the NCBI genome annotation pipeline. Represents either non-transcribed pseudogenes, or larger regions representing a gene cluster that is difficult to annotate via automatic methods.
 * NM_123456789    mRNA       Mixed        Transcript products; mature messenger RNA (mRNA) transcripts.
 * NP_123456789    Protein    Mixed        Protein products; primarily full-length precursor products but may include some partial proteins and mature peptide products.
 * NR_123456       RNA        Mixed        Non-coding transcripts including structural RNAs, transcribed pseudogenes, and others.
 * NT_123456       Genomic    Automated    Intermediate genomic assemblies of BAC and/or Whole Genome Shotgun sequence data.
 * NW_123456789    Genomic    Automated    Intermediate genomic assemblies of BAC or Whole Genome Shotgun sequence data.
 * NZ_ABCD12345678 Genomic    Automated    A collection of whole genome shotgun sequence data for a project. Accessions are not tracked between releases. The first four characters following the underscore (e.g. 'ABCD') identifies a genome project.
 * XM_123456789    mRNA       Automated    Transcript products; model mRNA provided by a genome annotation process; sequence corresponds to the genomic contig.
 * XP_123456789    Protein    Automated    Protein products; model proteins provided by a genome annotation process; sequence corresponds to the genomic contig.
 * XR_123456       RNA        Automated    Transcript products; model non-coding transcripts provided by a genome annotation process; sequence corresponds to the genomic contig.
 * YP_123456789    Protein    Mixed        Protein products; no corresponding transcript record provided. Primarily used for bacterial, viral, and mitochondrial records.
 * ZP_12345678     Protein    Automated    Protein products; annotated on NZ_ accessions (often via computational methods).
 * NS_123456       Genomic    Automated    Genomic records that represent an assembly which does not reflect the structure of a real biological molecule. The assembly may represent an unordered assembly of unplaced scaffolds, or it may represent an assembly of DNA sequences generated from a biological sample that may not represent a single organism.
 *
 * $ zcat genes.txt.gz | cut -f 2 | cut -b 1,2 | sort | uniq -c
 *   34466 NM
 *    6548 NR
 *
 * @author pcingola
 */
public class SnpEffPredictorFactoryRefSeq extends SnpEffPredictorFactory {

	public static final String CDS_STAT_COMPLETE = "cmpl";

	MultivalueHashMap<String, Gene> genesByName;

	public SnpEffPredictorFactoryRefSeq(Config config) {
		super(config, 0); // Zero values coordinates

		genesByName = new MultivalueHashMap<>();

		frameType = FrameType.UCSC;
		frameCorrection = true;
	}

	/**
	 * Get biotype based on ID
	 * Reference http://www.ncbi.nlm.nih.gov/RefSeq/key.html
	 */
	BioType bioType(String id) {
		if (id.length() < 2) return null;

		String key = id.substring(0, 2);
		switch (key) {
		case "NM":
		case "NP":
		case "XM":
		case "XP":
		case "YP":
		case "ZP":
			return BioType.protein_coding;

		case "AC":
		case "AP":
		case "NC":
		case "NG":
		case "NR":
		case "NT":
		case "NW":
		case "NZ":
		case "XR":
		case "NS":
			return BioType.pseudogene;

		default:
			return null;
		}
	}

	@Override
	public SnpEffectPredictor create() {
		try {
			// Read gene intervals from a file
			if (fileName == null) fileName = config.getBaseFileNameGenes() + ".refseq";

			if (verbose) System.out.print("Reading gene intervals file : '" + fileName + "'\n\t\t");
			readRefSeqFile(); // Read gene info

			beforeExonSequences(); // Some clean-up before readng exon sequences

			// Read chromosome sequences and set exon sequences
			if (readSequences) readExonSequences();
			else if (createRandSequences) createRandSequences();

			finishUp(); // Perform adjustments

			if (verbose) Log.info(config.getGenome());
		} catch (Exception e) {
			if (verbose) e.printStackTrace();
			throw new RuntimeException("Error reading file '" + fileName + "'\n" + e);
		}

		return snpEffectPredictor;
	}

	/**
	 * Find (or create) a gene for this transcript
	 */
	Gene findOrCreateGene(String geneName, String trId, Chromosome chromo, int start, int end, boolean strandMinus) {
		Marker tr = new Marker(chromo, start, end, strandMinus, trId);
		List<Gene> genes = genesByName.get(geneName);
		int geneIndex = 0;
		if (genes != null) {
			for (Gene gene : genes) {
				if (gene.intersects(tr)) {
					// Do we need to update gene length?
					if (start < gene.getStart()) gene.setStart(start);
					if (gene.getEndClosed() < end) gene.setEndClosed(end);

					return gene;
				}
			}

			geneIndex = genes.size() + 1;
		}

		// Need to create a new gene
		String geneId = geneName + (geneIndex > 0 ? "." + geneIndex : "");
		Gene gene = new Gene(chromo, start, end, strandMinus, geneId, geneName, bioType(trId));
		genesByName.add(geneName, gene);
		add(gene);

		return gene;
	}

	/**
	 * Is this protein coding?
	 */
	boolean isProteinCoding(String id) {
		BioType biotype = bioType(id);
		return biotype != null && biotype.isProteinCoding();
	}

	/**
	 * Read and parse RefSeq file
	 */
	protected void readRefSeqFile() {
		try {
			int count = 0;
			BufferedReader reader = Gpr.reader(fileName);
			if (reader == null) return; // Error

			for (lineNum = 1; reader.ready(); lineNum++) {
				line = reader.readLine();

				// Skip headers
				if ((lineNum == 1) || line.startsWith("#")) continue;

				String fields[] = line.split("\t");

				if (fields.length >= 16) {
					// Parse fields

					// String bin= fields[0]; // Not used
					String id = fields[1];
					String chromoName = fields[2];
					boolean strandMinus = fields[3].equals("-");

					int txstart = parsePosition(fields[4]);
					int txend = parsePosition(fields[5]) - 1; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )

					int cdsStart = parsePosition(fields[6]);
					int cdsEnd = parsePosition(fields[7]) - 1; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )

					int exonCount = Gpr.parseIntSafe(fields[8]);
					String exonStarts = fields[9];
					String exonEnds = fields[10]; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )

					// String score= fields[11]; // Not used
					String geneName = fields[12];
					String cdsStartStat = fields[13];
					String cdsEndStat = fields[14];
					String exonFrames = fields[15];

					//---
					// Create
					//----
					Chromosome chromo = getOrCreateChromosome(chromoName);

					// Create IDs
					String trId = uniqueTrId(id);

					// Get or create gene
					Gene gene = findOrCreateGene(geneName, trId, chromo, txstart, txend, strandMinus);

					// Create transcript
					Transcript tr = new Transcript(gene, txstart, txend, strandMinus, trId);
					boolean markAsCoding = isProteinCoding(trId) && cdsStartStat.equals(CDS_STAT_COMPLETE) && cdsEndStat.equals(CDS_STAT_COMPLETE); // If CDS start or end are not 'complete', don't mark as protein coding
					tr.setProteinCoding(markAsCoding);
					add(tr);

					// Add Exons and CDS
					String exStartStr[] = exonStarts.split(",");
					String exEndStr[] = exonEnds.split(",");
					String exFrameStr[] = exonFrames.split(",");
					for (int i = 0; i < exonCount; i++) {
						// Exons
						int exStart = parsePosition(exStartStr[i]);
						int exEnd = parsePosition(exEndStr[i]) - 1; // Our internal database representations of coordinates always have a zero-based start and a one-based end (Reference: http://genome.ucsc.edu/FAQ/FAQtracks.html#tracks1 )
						int exFrame = Gpr.parseIntSafe(exFrameStr[i]);
						String exId = trId + ".ex." + (i + 1);
						Exon ex = new Exon(tr, exStart, exEnd, strandMinus, exId, i);

						exFrame = frameType.convertFrame(exFrame);
						ex.setFrame(exFrame);
						ex = add(ex);

						// CDS (ony if intersects)
						if ((exStart <= cdsEnd) && (exEnd >= cdsStart)) {
							Cds cds = new Cds(tr, Math.max(cdsStart, exStart), Math.min(cdsEnd, exEnd), strandMinus, exId);
							add(cds);
						}
					}

					count++;
					if (verbose) Gpr.showMark(count, MARK, "\t\t");
				}
			}

			reader.close();
		} catch (Exception e) {
			Log.debug("Offending line (lineNum: " + lineNum + "): '" + line + "'");
			throw new RuntimeException(e);
		}
	}

	/**
	 * Create a new (unique) transcript ID
	 */
	String uniqueTrId(String id) {
		if (!transcriptsById.containsKey(id)) return id;
		for (int i = 2; true; i++) {
			String trId = id + "." + i;
			if (!transcriptsById.containsKey(trId)) return trId;
		}
	}
}
