package org.snpeff.akka.vcf;

import org.snpeff.akka.Worker;
import org.snpeff.vcf.VcfEntry;

public class WorkerVcf extends Worker<VcfEntry, VcfEntry> {

	public WorkerVcf() {
		super();
	}

	@Override
	public VcfEntry calculate(VcfEntry vcfEntry) {
		return vcfEntry;
	}
}
