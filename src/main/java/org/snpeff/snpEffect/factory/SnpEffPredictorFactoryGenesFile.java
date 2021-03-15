package org.snpeff.snpEffect.factory;

import java.io.File;

import org.snpeff.interval.BioType;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Exon;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Transcript;
import org.snpeff.interval.Utr3prime;
import org.snpeff.interval.Utr5prime;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * This class creates a SnpEffectPredictor from a file (or a set of files) and a configuration
 * The files used are:
 * 		- genes.txt : Biomart query from Ensembl (see scripts/genes_dataset.xml)
 * 		- Fasta files: One per chromosome (as described in the config file)
 *
 * @author pcingola
 */
public class SnpEffPredictorFactoryGenesFile extends SnpEffPredictorFactory {

	public SnpEffPredictorFactoryGenesFile(Config config) {
		super(config, 1);
	}

	@Override
	public SnpEffectPredictor create() {
		try {
			// Sanity check
			if ((genome.getChromoFastaFiles().length > 0) && (genome.getChromosomeCount() != genome.getChromoFastaFiles().length)) throw new RuntimeException("Number of chromosomes does not match number of fasta files (there must be one fasta files per chromosome)\n" + genome);

			// Read gene annotations from a file
			fileName = config.getBaseFileNameGenes() + ".biomart";
			System.out.println("Reading gene intervals file : '" + fileName + "'");
			readGenesFile(); // Read gene info

			beforeExonSequences(); // Some clean-up before reading exon sequences

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
	 * Parse a line form a file
	 */
	void parseGenesFile(String line) {
		// Split fields and trim them
		String fields[] = line.split("\t");
		for (int i = 0; i < fields.length; i++)
			fields[i] = fields[i].trim();

		// Obtain or create gene interval
		String geneId = fields[1];
		String chromoName = fields[2];
		Chromosome chromo = getOrCreateChromosome(chromoName);

		Gene gint = snpEffectPredictor.getGene(geneId);
		if (gint == null) {
			gint = new Gene(chromo, parsePosition(fields[3]), parsePosition(fields[4]), Gpr.parseIntSafe(fields[5]) < 0, geneId, fields[0], BioType.parse(fields[6]));
			snpEffectPredictor.add(gint);
		}

		// Obtain or create transcript
		String transcriptId = fields[7];
		Transcript tr = gint.get(transcriptId);
		if (tr == null) {
			tr = new Transcript(gint, parsePosition(fields[8]), parsePosition(fields[9]), gint.isStrandMinus(), transcriptId);
			gint.add(tr);
		}

		// Obtain or create exon
		String exonId = fields[10];
		Exon exon = tr.get(exonId);
		if (exon == null) {
			exon = new Exon(tr, parsePosition(fields[11]), parsePosition(fields[12]), gint.isStrandMinus(), exonId, Gpr.parseIntSafe(fields[13]));
			tr.add(exon);
		}

		// Any 5 prime UTRs?
		if (fields.length >= 16) {
			if ((fields[14].length() > 0) && (fields[15].length() > 0)) {
				Utr5prime utrInterval = new Utr5prime(exon, parsePosition(fields[14]), parsePosition(fields[15]), gint.isStrandMinus(), exonId);
				tr.add(utrInterval);
			}
		}

		// Any 3 prime UTRs?
		if (fields.length >= 18) {
			if ((fields[16].length() > 0) && (fields[17].length() > 0)) {
				Utr3prime utrInterval = new Utr3prime(exon, parsePosition(fields[16]), parsePosition(fields[17]), gint.isStrandMinus(), exonId);
				tr.add(utrInterval);
			}
		}
	}

	/**
	 * Read genes from a file
	 *
	 * Format: "external_gene_id \t ensembl_gene_id \t chromosome_name \t start_position \t end_position \t strand \t gene_biotype \t ensembl_transcript_id \t transcript_start \t transcript_end \t ensembl_exon_id \t exon_chrom_start \t exon_chrom_end \t rank \t 5_utr_start \t 5_utr_end \t 3_utr_start \t 3_utr_end \n"
	 * See scripts/genes.xml (biomart query)
	 */
	void readGenesFile() {
		String file = ""; // File contents

		// Regular file
		File f = new File(fileName);
		if (f.exists()) file = Gpr.readFile(fileName);
		else {
			// Compressed file (adding '.gz')
			String fileNameGz = fileName + ".gz";
			f = new File(fileNameGz);
			if (f.exists()) file = Gpr.readFile(fileNameGz);
			else throw new RuntimeException("Cannot find file '" + fileName + "' or '" + fileNameGz + "'");
		}

		String lines[] = file.split("\n");
		for (int i = 0; i < lines.length; i++) {
			lineNum = i + 1;
			line = lines[i];
			parseGenesFile(line);
		}
	}

}
