package ca.mcgill.mcb.pcingola.akka.vcfStr;

import akka.actor.Props;
import ca.mcgill.mcb.pcingola.akka.vcf.MasterVcf;

/**
 * A simple demo of a master process
 * 
 * @author pablocingolani
 */
public class MasterVcfStr extends MasterVcf<String> {

	public MasterVcfStr(int numWorkers) {
		super(new Props(WorkerVcfStr.class), numWorkers);
	}

}
