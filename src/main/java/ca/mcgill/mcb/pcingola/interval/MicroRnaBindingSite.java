package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffects;

/**
 * miRna binding site (usually this was predicted by some algorithm)
 * 
 * @author pcingola
 */
public class MicroRnaBindingSite extends Marker {

	private static final long serialVersionUID = -9089500641817245554L;

	double pValue;

	public MicroRnaBindingSite(Marker parent, int start, int end, int strand, String id, double pValue) {
		super(parent, start, end, strand, id);
		this.pValue = pValue;
		type = EffectType.MICRO_RNA;
	}

	@Override
	public boolean seqChangeEffect(SeqChange seqChange, ChangeEffects changeEffects) {
		if (!intersects(seqChange)) return false; // Sanity check
		changeEffects.add(this, EffectType.MICRO_RNA, "" + pValue);
		return true;
	}

}
