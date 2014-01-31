package ca.mcgill.mcb.pcingola.akka.vcf;

import ca.mcgill.mcb.pcingola.akka.msg.StartMaster;

/**
 * A message telling master process to start calculating
 * It also sends the filename to be opened
 * 
 * @author pablocingolani
 */
public class StartMasterVcf extends StartMaster {

	public final String vcfFileName;

	public StartMasterVcf(String vcfFileName, int batchSize, int showEvery) {
		super(batchSize, showEvery);
		this.vcfFileName = vcfFileName;
	}
}
