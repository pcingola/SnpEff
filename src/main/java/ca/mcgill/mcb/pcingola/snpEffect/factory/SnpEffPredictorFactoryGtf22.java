package ca.mcgill.mcb.pcingola.snpEffect.factory;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.IntergenicConserved;
import ca.mcgill.mcb.pcingola.interval.IntronConserved;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Utr3prime;
import ca.mcgill.mcb.pcingola.interval.Utr5prime;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * This class creates a SnpEffectPredictor from a GTF 2.2 file
 *
 * References: http://mblab.wustl.edu/GTF22.html
 *
 * @author pcingola
 */
public class SnpEffPredictorFactoryGtf22 extends SnpEffPredictorFactoryGff {

	static final String ATTRIBUTE_PATTERN_REGEX = "\\s*(\\S+)\\s+\"(.*?)\"\\s*;";
	static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(ATTRIBUTE_PATTERN_REGEX);

	public SnpEffPredictorFactoryGtf22(Config config) {
		super(config);
		version = "GTF22";
		fileName = config.getBaseFileNameGenes() + ".gtf";
	}

	/**
	 * Add a new interval to SnpEffect predictor
	 */
	void addInterval(String id, String type, String chromo, int start, int end, boolean strandMinus, String geneId, String geneName, String transcriptId, boolean proteinCoding, String geneBioType, String trBioType, int frame) {
		// Get chromosome
		Chromosome chromosome = getOrCreateChromosome(chromo);

		// Get (or create) gene
		Gene gene = findGene(geneId);
		if (gene == null) {
			// Create and add  gene
			if (geneName == null) geneName = geneId;
			if ((geneBioType == null) || (geneBioType.isEmpty())) geneBioType = "mRNA"; // No bioType? Create a default one
			gene = new Gene(chromosome, start, end, strandMinus, geneId, geneName, geneBioType);
			add(gene);
		}

		// Check that they are in the same chromosome
		if (!gene.getChromosomeName().equals(chromosome.getId())) error("Gene chromosome does not match !" + "\n\tPosition    : " + chromo + ":" + start + "-" + end + "\n\t" + gene);

		// No transcript ID => Create only gene. We are done.
		if (transcriptId.isEmpty()) return;

		// Get (or create) transcript
		Transcript tr = null;
		tr = findTranscript(transcriptId);
		if (tr == null) {
			tr = new Transcript(gene, start, end, strandMinus, transcriptId);
			if ((trBioType == null) || (trBioType.isEmpty())) trBioType = "mRNA"; // No bioType? Create a default one
			tr.setBioType(trBioType);
			add(tr);
		}

		if (proteinCoding) tr.setProteinCoding(proteinCoding); // Set protein coding (if available)

		// Check that they are in the same chromosome
		if (!tr.getChromosomeName().equals(chromosome.getId())) error("Transcript chromosome does not match !" + "\n\tPosition    : " + chromo + ":" + start + "-" + end + "\n\t" + gene);

		if (is(type, EXON)) {
			// This can be added in different ways
			if (type.equals("exon")) {
				int rank = 0; // Rank information will be added later
				Exon exon = new Exon(tr, start, end, strandMinus, id, rank);
				exon.setFrame(frame);
				add(exon);
			} else if (type.equals("CDS")) {
				Cds cds = new Cds(tr, start, end, strandMinus, id);
				cds.setFrame(frame);
				add(cds);
			} else if (type.equals("stop_codon")) {
				// According to the norm: "Unlike Genbank annotation, the stop codon is not included in the CDS for the terminal exon"
				// So we add it as another 'CDS' (it might be contiguous to the previous CDS)
				Cds cds = new Cds(tr, start, end, strandMinus, id);
				cds.setFrame(frame);
				add(cds);
			} else if (type.equals("start_codon")) {
				// Nothing to do
			} else if (type.equals("intron_CNS")) {
				IntronConserved intronConserved = new IntronConserved(gene, start, end, strandMinus, id);
				add(intronConserved);
				snpEffectPredictor.add(intronConserved);
			}
		} else if (is(type, UTR5) || is(type, UTR3)) {
			// Find exon
			Marker utr = new Marker(tr, start, end, strandMinus, id);
			Exon exon = tr.queryExon(utr);
			if (exon == null) { // No exon? => Create one
				int rank = 0; // We don't have rank information
				String exonId = "Exon_" + chromo + "_" + (start + 1) + "_" + (end + 1);
				exon = new Exon(tr, start, end, strandMinus, exonId, rank);
				exon.setFrame(frame);
				add(exon);
			}

			// Add UTR
			if (is(type, UTR5)) {
				Utr5prime u5int = new Utr5prime(exon, start, end, strandMinus, id);
				tr.add(u5int);
				add(u5int);
			} else if (is(type, UTR3)) {
				Utr3prime u3int = new Utr3prime(exon, start, end, strandMinus, id);
				tr.add(u3int);
				add(u3int);
			}
		} else if (is(type, INTERGENIC_CONSERVED)) {
			IntergenicConserved intergenicConserved = new IntergenicConserved(chromosome, start, end, strandMinus, id);
			snpEffectPredictor.add(intergenicConserved);
			add(intergenicConserved);
		}
	}

	/**
	 * Read and parse GTF file
	 */
	@Override
	protected boolean parse(String line, String typeToRead) {
		String fields[] = line.split("\t");

		// Ommit headers
		if (fields.length <= 6) return false;

		String type = fields[2];

		// Is it the type that we want to read?
		if (!is(type, typeToRead)) return false;

		// Parse fields
		String chromo = fields[0];
		String source = fields[1];
		int start = parsePosition(fields[3]);
		int end = parsePosition(fields[4]);
		boolean strandMinus = fields[6].equals("-");
		int frame = (fields[7].equals(".") ? -1 : Gpr.parseIntSafe(fields[7]));
		String geneId = "", transcriptId = "";
		String geneName = null;

		// Is it protein coding?
		String geneBioType = "", trBioType = "";

		// Parse attributes
		if (fields.length >= 8) {
			HashMap<String, String> attrMap = parseAttributes(fields[8]);

			// Get gene and transcript ID
			geneId = attrMap.get("gene_id");
			transcriptId = attrMap.get("transcript_id");
			geneName = attrMap.get("gene_name");

			// Try to get biotype from different fields
			geneBioType = attrMap.get("gene_biotype"); // Note: This is ENSEMBL specific
			if (geneBioType == null) geneBioType = attrMap.get("gene_type"); // Note: This is GENCODE specific
			if (geneBioType == null) geneBioType = attrMap.get("biotype"); // Try using 'biotype' field

			trBioType = attrMap.get("transcript_type"); // Note: This is GENCODE specific
		}

		// Use 'source' as bioType (ENSEMBL uses this field)
		if ((trBioType == null) || trBioType.isEmpty()) trBioType = source;
		boolean proteinCoding = isProteingCoding(trBioType);

		// Transform null to empty
		if (geneId == null) geneId = "";
		if (transcriptId == null) transcriptId = "";

		// Sometimes names or IDs may have spaces, we have to get rid of them
		geneId = geneId.trim();
		transcriptId = transcriptId.trim();

		String id = type + "_" + chromo + "_" + (start + 1) + "_" + (end + 1); // Create ID
		if (geneId.isEmpty()) warning("Empty gene_id. This should never happen (see norm");
		else addInterval(id, type, chromo, start, end, strandMinus, geneId, geneName, transcriptId, proteinCoding, geneBioType, trBioType, frame); // Add interval

		return true;
	}

	/**
	 * Parse attributes
	 */
	HashMap<String, String> parseAttributes(String attrStr) {
		HashMap<String, String> keyValues = new HashMap<String, String>();

		if (attrStr.length() > 0) {
			Matcher matcher = ATTRIBUTE_PATTERN.matcher(attrStr);
			while (matcher.find()) {
				if (matcher.groupCount() >= 2) {
					String key = matcher.group(1).toLowerCase();
					String value = matcher.group(2);
					keyValues.put(key, value);
				}
			}
		}

		return keyValues;
	}
}
