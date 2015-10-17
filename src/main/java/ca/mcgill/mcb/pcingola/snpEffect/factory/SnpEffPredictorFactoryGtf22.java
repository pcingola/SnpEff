package ca.mcgill.mcb.pcingola.snpEffect.factory;

import ca.mcgill.mcb.pcingola.interval.Gtf2Marker;
import ca.mcgill.mcb.pcingola.snpEffect.Config;

/**
 * This class creates a SnpEffectPredictor from a GTF 2.2 file
 *
 * References: http://mblab.wustl.edu/GTF22.html
 *
 * @author pcingola
 */
public class SnpEffPredictorFactoryGtf22 extends SnpEffPredictorFactoryGff {

	public SnpEffPredictorFactoryGtf22(Config config) {
		super(config);
		version = "GTF22";
		fileName = config.getBaseFileNameGenes() + ".gtf";
	}

	//	/**
	//	 * Add a new interval to SnpEffect predictor
	//	 */
	//	void addInterval(String id, String type, String chromo, int start, int end, boolean strandMinus, String geneId, String geneName, String transcriptId, boolean proteinCoding, String geneBioType, String trBioType, int frame, String transcriptSupportLevel) {
	//		// Get chromosome
	//		Chromosome chromosome = getOrCreateChromosome(chromo);
	//
	//		Gene gene = null;
	//		Transcript tr = null;
	//
	//		// Get (or create) gene
	//		if (!geneId.isEmpty()) gene = findGene(geneId);
	//		else {
	//			tr = findTranscript(transcriptId);
	//			gene = (Gene) tr.getParent();
	//		}
	//
	//		// Gene not found? => Create new gene
	//		if (gene == null) {
	//			if (geneName == null) geneName = geneId;
	//			if ((geneBioType == null) || (geneBioType.isEmpty())) geneBioType = "mRNA"; // No bioType? Create a default one
	//			gene = new Gene(chromosome, start, end, strandMinus, geneId, geneName, geneBioType);
	//			add(gene);
	//		}
	//
	//		// Check that they are in the same chromosome
	//		if (!gene.getChromosomeName().equals(chromosome.getId())) error("Gene chromosome does not match !" + "\n\tPosition    : " + chromo + ":" + start + "-" + end + "\n\t" + gene);
	//
	//		// No transcript ID => Create only gene. We are done.
	//		if (transcriptId.isEmpty()) return;
	//
	//		// Get (or create) transcript
	//		tr = findTranscript(transcriptId);
	//		if (tr == null) {
	//			tr = new Transcript(gene, start, end, strandMinus, transcriptId);
	//
	//			// No bioType? Create a default one
	//			if ((trBioType == null) || trBioType.isEmpty()) trBioType = "mRNA";
	//			tr.setBioType(trBioType);
	//
	//			// Add transcript support level information
	//			if (transcriptSupportLevel != null) {
	//				TranscriptSupportLevel tsl = TranscriptSupportLevel.parse(transcriptSupportLevel);
	//				tr.setTranscriptSupportLevel(tsl);
	//			}
	//
	//			add(tr);
	//		}
	//
	//		if (proteinCoding) tr.setProteinCoding(proteinCoding); // Set protein coding (if available)
	//
	//		// Check that they are in the same chromosome
	//		if (!tr.getChromosomeName().equals(chromosome.getId())) error("Transcript chromosome does not match !" + "\n\tPosition    : " + chromo + ":" + start + "-" + end + "\n\t" + gene);
	//
	//		if (is(type, EXON)) {
	//			// This can be added in different ways
	//			if (type.equals("exon")) {
	//				int rank = 0; // Rank information will be added later
	//				Exon exon = new Exon(tr, start, end, strandMinus, id, rank);
	//				exon.setFrame(frame);
	//				add(exon);
	//			} else if (type.equals("CDS")) {
	//				Cds cds = new Cds(tr, start, end, strandMinus, id);
	//				cds.setFrame(frame);
	//				add(cds);
	//			} else if (type.equals("stop_codon")) {
	//				// According to the norm: "Unlike Genbank annotation, the stop codon is not included in the CDS for the terminal exon"
	//				// So we add it as another 'CDS' (it might be contiguous to the previous CDS)
	//				Cds cds = new Cds(tr, start, end, strandMinus, id);
	//				cds.setFrame(frame);
	//				add(cds);
	//			} else if (type.equals("start_codon")) {
	//				// Nothing to do
	//			} else if (type.equals("intron_CNS")) {
	//				IntronConserved intronConserved = new IntronConserved(gene, start, end, strandMinus, id);
	//				add(intronConserved);
	//				snpEffectPredictor.add(intronConserved);
	//			}
	//		} else if (is(type, UTR5) || is(type, UTR3)) {
	//			// Find exon
	//			Marker utr = new Marker(tr, start, end, strandMinus, id);
	//			Exon exon = tr.queryExon(utr);
	//			if (exon == null) { // No exon? => Create one
	//				int rank = 0; // We don't have rank information
	//				String exonId = "Exon_" + chromo + "_" + (start + 1) + "_" + (end + 1);
	//				exon = new Exon(tr, start, end, strandMinus, exonId, rank);
	//				exon.setFrame(frame);
	//				add(exon);
	//			}
	//
	//			// Add UTR
	//			if (is(type, UTR5)) {
	//				Utr5prime u5int = new Utr5prime(exon, start, end, strandMinus, id);
	//				tr.add(u5int);
	//				add(u5int);
	//			} else if (is(type, UTR3)) {
	//				Utr3prime u3int = new Utr3prime(exon, start, end, strandMinus, id);
	//				tr.add(u3int);
	//				add(u3int);
	//			}
	//		} else if (is(type, INTERGENIC_CONSERVED)) {
	//			IntergenicConserved intergenicConserved = new IntergenicConserved(chromosome, start, end, strandMinus, id);
	//			snpEffectPredictor.add(intergenicConserved);
	//			add(intergenicConserved);
	//		}
	//	}
	//
	//	/**
	//	 * Read and parse GTF file
	//	 */
	//	@Override
	//	protected boolean parse(String line, GffType typeToRead) {
	//		// Omit headers
	//		if (!GffMarker.canParseLine(line)) return false;
	//
	//		// Filter by type
	//		String type = fields[2];
	//		if (!is(type, typeToRead)) return false;
	//
	//		// Parse fields
	//		String chromo = fields[0];
	//		String source = fields[1];
	//		int start = parsePosition(fields[3]);
	//		int end = parsePosition(fields[4]);
	//		boolean strandMinus = fields[6].equals("-");
	//		int frame = (fields[7].equals(".") ? -1 : Gpr.parseIntSafe(fields[7]));
	//		frame = frameType.convertFrame(frame);
	//		String geneId = "", transcriptId = "";
	//		String geneName = null;
	//		String transcriptSupportLevel = null;
	//
	//		// Is it protein coding?
	//		String geneBioType = "", trBioType = "";
	//
	//		// Parse attributes
	//		if (fields.length >= 8) {
	//			HashMap<String, String> attrMap = parseAttributes(fields[8]);
	//
	//			// Get gene and transcript ID
	//			geneId = attrMap.get("gene_id");
	//			transcriptId = attrMap.get("transcript_id");
	//			geneName = attrMap.get("gene_name");
	//
	//			// Try to get biotype from different fields
	//			geneBioType = attrMap.get("gene_biotype"); // Note: This is ENSEMBL specific
	//			if (geneBioType == null) geneBioType = attrMap.get("gene_type"); // Note: This is GENCODE specific
	//			if (geneBioType == null) geneBioType = attrMap.get("biotype"); // Try using 'biotype' field
	//
	//			trBioType = attrMap.get("transcript_biotype"); // Transcript biotype
	//			if (trBioType == null) trBioType = attrMap.get("transcript_type"); // Note: This is GENCODE specific
	//
	//			transcriptSupportLevel = attrMap.get("transcript_support_level");
	//		}
	//
	//		// Use 'source' as bioType (Old ENSEMBL GTF files use this field)
	//		if ((trBioType == null) || trBioType.isEmpty()) trBioType = source;
	//		boolean proteinCoding = isProteingCoding(trBioType);
	//
	//		// Transform null to empty
	//		if (geneId == null) geneId = "";
	//		if (transcriptId == null) transcriptId = "";
	//
	//		// Sometimes names or IDs may have spaces, we have to get rid of them
	//		geneId = geneId.trim();
	//		transcriptId = transcriptId.trim();
	//
	//		String id = type + "_" + chromo + "_" + (start + 1) + "_" + (end + 1); // Create ID
	//		if (transcriptId.isEmpty() && geneId.isEmpty()) {
	//			warning("Empty gene_id and transcript_id. This should never happen (see GTF norm)");
	//		} else addInterval(id, type, chromo, start, end, strandMinus, geneId, geneName, transcriptId, proteinCoding, geneBioType, trBioType, frame, transcriptSupportLevel); // Add interval
	//
	//		return true;
	//	}
	//
	//	/**
	//	 * Parse attributes
	//	 */
	//	HashMap<String, String> parseAttributes(String attrStr) {
	//		HashMap<String, String> keyValues = new HashMap<String, String>();
	//
	//		if (attrStr.length() > 0) {
	//			Matcher matcher = ATTRIBUTE_PATTERN.matcher(attrStr);
	//			while (matcher.find()) {
	//				if (matcher.groupCount() >= 2) {
	//					String key = matcher.group(1).toLowerCase();
	//					String value = matcher.group(2);
	//					keyValues.put(key, value);
	//				}
	//			}
	//		}
	//
	//		return keyValues;
	//	}

	@Override
	protected boolean parse(String line) {
		Gtf2Marker gffMarker = new Gtf2Marker(genome, line);
		return addInterval(gffMarker);
	}
}
