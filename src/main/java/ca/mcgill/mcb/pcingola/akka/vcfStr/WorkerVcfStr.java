package ca.mcgill.mcb.pcingola.akka.vcfStr;

import ca.mcgill.mcb.pcingola.akka.Worker;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

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
