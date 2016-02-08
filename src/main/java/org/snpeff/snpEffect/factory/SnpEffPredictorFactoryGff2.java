package org.snpeff.snpEffect.factory;

import org.snpeff.snpEffect.Config;

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

}
