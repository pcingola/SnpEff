package org.snpeff.snpEffect.commandLine;

import org.snpeff.vcf.VcfEntry;

/**
 * Command line program: Predict variant effects
 *
 * @author Pablo Cingolani
 */
public class AnnotateVcfHaplotypes extends AnnotateVcf {

	public AnnotateVcfHaplotypes() {
		super();
	}

	/**
	 * Annotate a VCF entry
	 */
	@Override
	public boolean annotate(VcfEntry vcfEntry) {
		super.annotate(vcfEntry);
		return true;
	}

}
