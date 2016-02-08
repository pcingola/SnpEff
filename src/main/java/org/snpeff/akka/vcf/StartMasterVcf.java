package org.snpeff.akka.vcf;

import org.snpeff.akka.msg.StartMaster;
import org.snpeff.snpEffect.Config;

/**
 * A message telling master process to start calculating
 * It also sends the filename to be opened
 * 
 * @author pablocingolani
 */
public class StartMasterVcf extends StartMaster {

	public final String vcfFileName;
	public final Config config;

	public StartMasterVcf(String vcfFileName, Config config, int batchSize, int showEvery) {
		super(batchSize, showEvery);
		this.vcfFileName = vcfFileName;
		this.config = config;
	}
}
