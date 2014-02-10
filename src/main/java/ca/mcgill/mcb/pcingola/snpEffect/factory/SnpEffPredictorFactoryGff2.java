package ca.mcgill.mcb.pcingola.snpEffect.factory;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;

/**
 * This class creates a SnpEffectPredictor from a GFF2 file.
 * 
 * WARNING: GFF2 support is VERY limited! It was only done for amel (honey bee) genome.
 * 
 * Note: GFF2 is an obsolete format. Take a look at this quote from Gmod (http://gmod.org/wiki/GFF2)
 * 
 * "Why GFF2 is harmful to your health
 * 		One of GFF2's problems is that it is only able to represent one level of nesting of features. This 
 * 		is mainly a problem when dealing with genes that have multiple alternatively-spliced transcripts. GFF2 
 * 		is unable to deal with the three-level hierarchy of gene transcript exon. Most people get 
 * 		around this by declaring a series of transcripts and giving them similar names to indicate that they 
 * 		come from the same gene. The second limitation is that while GFF2 allows you to create two-level hierarchies, such 
 * 		as transcript exon, it doesn't have any concept of the direction of the hierarchy. So it doesn't know 
 * 		whether the exon is a subfeature of the transcript, or vice-versa. This means you have to use "aggregators" to sort 
 * 		out the relationships. This is a major pain in the neck. For this reason, GFF2 format has been deprecated in 
 * 		favor of GFF2 format databases." 
 * 
 * We are only adding this format in order to read old amel2 (Honey bee) genome annotations
 * 
 * Refereces: http://gmod.org/wiki/GFF2
 * 
 * @author pcingola
 */
public class SnpEffPredictorFactoryGff2 extends SnpEffPredictorFactoryGff {

	public SnpEffPredictorFactoryGff2(Config config) {
		super(config);
		version = "GFF2";
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
	void addInterval(String id, String type, String chromo, int start, int end, int strand, String geneId, String trId) {
		// Get chromosome
		Chromosome chromosome = getOrCreateChromosome(chromo);

		// Add gene?
		if (is(type, TRANSCRIPT)) {
			// Add gene if needed
			Gene gene = findGene(geneId);
			if (gene == null) {
				gene = new Gene(chromosome, start, end, strand, geneId, geneId, "mRNA");
				add(gene);
			}

			// Add transcript
			Transcript tint = findTranscript(trId);
			if (tint == null) {
				tint = new Transcript(gene, start, end, strand, trId);
				add(tint);
			}
		} else if (is(type, EXON)) {
			// Get transcript
			Transcript tint = findTranscript(trId);
			if (tint == null) {
				System.err.println("Cannot find transcript '" + trId + "'");
				return;
			}

			// Create and add exon
			int rank = 0; // Rank info not available in GFF2
			Exon exon = new Exon(tint, start, end, strand, id, rank);
			add(exon);
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
		String type = fields[2];

		// Is it the type that we want to read?
		if (!is(type, typeToRead)) return false;

		// Parse fields
		String chromo = fields[0];
		int start = parsePosition(fields[3]);
		int end = parsePosition(fields[4]);
		int strand = (fields[6].equals("-") ? -1 : +1);
		String geneId = "", trId = "";

		// Parse attributes
		if (fields.length >= 8) {
			String attrStr = fields[8];

			if (attrStr.length() > 0) {
				String attrs[] = attrStr.split(";");
				for (int i = 0; i < attrs.length; i++) {
					attrs[i] = attrs[i].trim();
					String kv[] = attrs[i].split("\\s+");
					String key = kv[0];
					String value = unquote(kv[1]);

					if (key.equalsIgnoreCase("gene_id")) geneId = value;
					else if (key.equalsIgnoreCase("transcript_id")) trId = value;
				}
			}
		}

		// Add interval
		String id = typeToRead + "_" + chromo + "_" + (start + 1) + "_" + (end + 1);

		// Sometimes names or IDs may have spaces, we have to get rid of them
		id = id.trim();
		geneId = geneId.trim();

		addInterval(id, type, chromo, start, end, strand, geneId, trId);

		return true;
	}
}
