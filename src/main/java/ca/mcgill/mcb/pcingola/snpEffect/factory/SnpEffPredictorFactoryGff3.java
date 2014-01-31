package ca.mcgill.mcb.pcingola.snpEffect.factory;

import ca.mcgill.mcb.pcingola.interval.Cds;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Utr3prime;
import ca.mcgill.mcb.pcingola.interval.Utr5prime;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * This class creates a SnpEffectPredictor from a GFF3 file
 * 
 * References: 
 * 		- http://www.sequenceontology.org/gff3.shtml
 * 		- http://gmod.org/wiki/GFF3
 * 		- http://www.eu-sol.net/science/bioinformatics/standards-documents/gff3-format-description
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryGff3 extends SnpEffPredictorFactoryGff {

	public SnpEffPredictorFactoryGff3(Config config, int inOffset) {
		super(config, inOffset);
		version = "GFF3";
	}

	/**
	 * Add a new interval to SnpEffect predictor
	 * @param id
	 * @param type
	 * @param chromo
	 * @param start
	 * @param end
	 * @param strand
	 * @param name
	 * @param parent
	 */
	void addInterval(String id, String type, String chromo, int start, int end, int strand, String name, boolean proteinCoding, String parent, int frame) {
		// Get chromosome
		Chromosome chromosome = getOrCreateChromosome(chromo);

		if (is(type, GENE)) {
			// Have we already added this one?
			if ((id != null) && (findGene(id) != null)) {
				warning(type + " '" + id + "' already added");
				return;
			}

			// Create and add gene
			Gene gene = new Gene(chromosome, start, end, strand, id, name, "");
			add(gene);
		} else if (is(type, TRANSCRIPT)) {
			// Sanity check: Have we already added this one?
			if ((id != null) && (findTranscript(id) != null)) {
				warning(type + " '" + id + "' already added");
				return;
			}

			// Get gene ID (or create a fake one if it's not available
			String geneId = parent;
			if (geneId.isEmpty()) geneId = "Gene_" + id; // Create fake ID

			// Get (or create) gene
			Gene gene = findGene(geneId, id);
			if (gene == null) {
				// Create and add 'fake' gene
				gene = new Gene(chromosome, start, end, strand, geneId, geneId, "mRNA");
				add(gene);
				warning("Cannot find gene '" + geneId + "'. Created gene '" + gene.getId() + "' for transcript '" + id + "'");
			}

			// Update bio-type (if needed)
			if ((gene.getBioType() == null) || (!gene.getBioType().equals(type))) gene.setBioType(type);

			// Check that they are in the same chromosome
			if (!gene.getChromosomeName().equals(chromosome.getId())) error("Trying to assign Transcript to a gene in a different chromosome!" + "\n\tPosition    : " + chromo + ":" + start + "-" + end + "\n\t" + gene);

			// Create and add transcript
			Transcript tr = new Transcript(gene, start, end, strand, id);
			if (proteinCoding) tr.setProteinCoding(proteinCoding); // Set protein coding (if available)
			add(tr);
		} else if (is(type, EXON)) {
			// Add exon to each parent (can belong to more than one transcript)
			String parents[] = parent.split(",");
			for (String par : parents) {
				par = par.trim();

				// Exon's parent (should be a transcript)
				Transcript tr = findTranscript(par, id);
				Gene gene = findGene(par, id);

				// Is exon's parent a gene instead of a transcript?
				if ((tr == null) && (gene != null)) {
					// Create a transcript from the gene
					String trId = "Transcript_" + gene.getId(); // Transcript ID
					tr = new Transcript(gene, start, end, strand, trId);
					if (proteinCoding) tr.setProteinCoding(proteinCoding); // Set protein coding (if available)

					// Add new transcript
					add(tr);
					warning("Cannot find transcript '" + par + "'. Created transcript '" + tr.getId() + "' for this exon");
				}

				// No transcript found? => Try creating one
				if (tr == null) {
					// No gene? Create one
					if (gene == null) {
						// Create and add gene
						String gId = "Gene_" + (par.isEmpty() ? id : par); // Gene ID
						gene = new Gene(chromosome, start, end, strand, gId, name, "");
					}

					// Create transcript
					String trId = par.isEmpty() ? "Transcript_" + id : par; // Transcript ID
					tr = new Transcript(gene, start, end, strand, trId);
					if (proteinCoding) tr.setProteinCoding(proteinCoding); // Set protein coding (if available)

					// Add gene & transcript
					add(gene);
					add(tr);
					warning("Cannot find transcript '" + par + "'. Created transcript '" + tr.getId() + "' and gene '" + gene.getId() + "' for this exon");
				}

				// Check that they are in the same chromosome
				if (!tr.getChromosomeName().equals(chromosome.getId())) {
					warning("Trying to assign Exon or CDS to a transcript in a different chromosome!" + "\n\tPosition    : " + chromo + ":" + start + "-" + end + "\n\t" + tr);
					return;
				}

				// This can be added in different ways
				if (type.equalsIgnoreCase("exon")) {
					int rank = 0; // Rank information will be added later
					Exon ex = new Exon(tr, start, end, strand, id, rank);
					ex.setFrame(frame);
					add(ex);
				} else if (type.equalsIgnoreCase("CDS")) {
					Cds cds = new Cds(tr, start, end, strand, id);
					cds.setFrame(frame);
					add(cds);
				} else if (type.equalsIgnoreCase("stop_codon") || type.equalsIgnoreCase("start_codon")) {
					int rank = 0; // Rank information will be added later
					Exon ex = new Exon(tr, start, end, strand, id, rank);
					ex.setFrame(frame);
					add(ex);
					Cds cds = new Cds(tr, start, end, strand, type + "_" + id);
					cds.setFrame(frame);
					add(cds);
				}
			}
		} else if (is(type, UTR5) || is(type, UTR3)) {
			// Add to each parent (can belong to more than one transcript)
			String parents[] = parent.split(",");
			for (String par : parents) {

				// Get transcript
				Transcript tr = findTranscript(par);
				if (tr == null) {
					warning("Cannot find transcript '" + par + "'");
					return;
				}

				// Find exon
				Marker utr = new Marker(tr, start, end, strand, id);
				Exon exon = tr.queryExon(utr);
				if (exon == null) {
					exon = new Exon(tr, start, end, strand, id, 0);
					exon.setFrame(frame);
					add(exon);
					warning("Cannot find exon for UTR: '" + utr.getId() + "'. Creating exon '" + id + "'");
				}

				// Add UTR
				if (is(type, UTR5)) {
					Utr5prime u5 = new Utr5prime(exon, start, end, strand, id);
					tr.add(u5);
					add(u5);
				} else if (is(type, UTR3)) {
					Utr3prime u3 = new Utr3prime(exon, start, end, strand, id);
					tr.add(u3);
					add(u3);
				}
			}
		}
	}

	/**
	 * Read and parse GFF file
	 * @param vcfFileName
	 * @throws Exception
	 */
	@Override
	protected boolean parse(String line, String typeToRead) {
		String fields[] = line.split("\t");

		// Ommit headers
		if (fields.length <= 6) return false;

		// Is it the type that we want to read?
		String type = fields[2];
		if (!is(type, typeToRead)) return false;

		// Parse fields
		String chromo = fields[0];
		String source = fields[1];
		int start = parsePosition(fields[3]);
		int end = parsePosition(fields[4]);

		// Parse strand
		int strand = 0;
		if (fields[6].equals("+")) strand = +1;
		else if (fields[6].equals("-")) strand = -1;

		int frame = (fields[7].equals(".") ? -1 : Gpr.parseIntSafe(fields[7]));
		String name = null;
		String parent = "";
		String id = null;

		// Is it protein coding?
		boolean proteinCoding = isProteingCoding(source);

		// Parse attributes
		if (fields.length >= 8) {
			String attrStr = fields[8];

			if (attrStr.length() > 0) {
				String attrs[] = attrStr.split(";");
				for (int i = 0; i < attrs.length; i++) {
					// Split key value pair
					String kv[] = attrs[i].split("=");
					if (kv.length > 1) {
						String key = kv[0];
						String value = kv[1];

						if (key.equalsIgnoreCase("ID")) id = value;
						else if (key.equalsIgnoreCase("Parent")) parent = value;
						else if (key.equalsIgnoreCase("Name") || key.equalsIgnoreCase("gene_name")) name = value;
						else if (key.equalsIgnoreCase("db_xref") && (id == null)) id = value;
					}
				}
			}
		}

		if (id == null) id = typeToRead + "_" + chromo + "_" + (start + 1) + "_" + (end + 1); // No ID => create one
		if (name == null) name = id; // No name? => Use ID

		// Sometimes names or IDs may have spaces, we have to get rid of them
		id = id.trim();
		name = name.trim();

		// Add interval
		addInterval(id, type, chromo, start, end, strand, name, proteinCoding, parent, frame);

		return true;
	}
}
