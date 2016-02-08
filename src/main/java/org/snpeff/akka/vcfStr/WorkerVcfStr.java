package org.snpeff.akka.vcfStr;

import org.snpeff.akka.Worker;
import org.snpeff.vcf.VcfEntry;

/**
 * A trivial calculation on a VCF that returns a String
 * 
 * @author pablocingolani
 */
public class WorkerVcfStr extends Worker<VcfEntry, String> {

	@Override
	public String calculate(VcfEntry vcfEntry) {
		return (vcfEntry != null ? vcfEntry.toString() : null);
	}
}
