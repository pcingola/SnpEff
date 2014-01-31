package ca.mcgill.mcb.pcingola.interval;

import java.util.List;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect;
import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;

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
	public List<ChangeEffect> seqChangeEffect(SeqChange seqChange, ChangeEffect changeEffect) {
		if (!intersects(seqChange)) return ChangeEffect.emptyResults(); // Sanity check
		changeEffect.set(this, EffectType.MICRO_RNA, "" + pValue);
		return changeEffect.newList();
	}

}
