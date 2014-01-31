package ca.mcgill.mcb.pcingola.akka.vcf;

import ca.mcgill.mcb.pcingola.akka.Worker;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

public class WorkerVcf extends Worker<VcfEntry, VcfEntry> {

	public WorkerVcf() {
		super();
	}

	@Override
	public VcfEntry calculate(VcfEntry vcfEntry) {
		return vcfEntry;
	}
}
