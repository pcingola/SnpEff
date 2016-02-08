package org.snpeff.akka.vcfStr;

import org.snpeff.akka.vcf.MasterVcf;

import akka.actor.Props;

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
