package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;

/**
 * Rare amino acid annotation:
 * 
 * These are amino acids that occurs very rarely in an organism. For instance, humans 
 * are supposed to use 20 amino acids, but there is also one rare AA. Selenocysteine, 
 * single letter code 'U', appears roughly 100 times in the whole genome. The amino 
 * acid is so rare that usually it does not appear in codon translation tables. It 
 * is encoded as UGA, which , under normal conditions, is a STOP codon. Secondary 
 * RNA structures are assumed to enable this special translation.
 * 
 * @author pcingola
 */
public class RareAminoAcid extends Marker {

	private static final long serialVersionUID = -1926572865764543849L;

	public RareAminoAcid() {
		super();
		type = EffectType.RARE_AMINO_ACID;
	}

	public RareAminoAcid(Marker parent, int start, int end, String id) {
		super(parent, start, end, false, id);
		type = EffectType.RARE_AMINO_ACID;
	}
}
