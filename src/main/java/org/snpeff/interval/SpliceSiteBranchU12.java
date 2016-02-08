package org.snpeff.interval;

import org.snpeff.snpEffect.EffectType;

/**
 * A (putative) U12 branch site.
 *
 * @author pablocingolani
 */
public class SpliceSiteBranchU12 extends SpliceSiteBranch {

	private static final long serialVersionUID = 7903892379174750342L;

	public SpliceSiteBranchU12() {
		super();
		type = EffectType.SPLICE_SITE_BRANCH_U12;
	}

	public SpliceSiteBranchU12(Intron parent, int start, int end, boolean strandMinus, String id) {
		super(parent, start, end, strandMinus, id);
		type = EffectType.SPLICE_SITE_BRANCH_U12;
	}

}
