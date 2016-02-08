package org.snpeff.akka.vcf;

import org.snpeff.snpEffect.Config;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

/**
 * A work queue that processes a VCF file
 * 
 * Sends batches of VcfEntries to each worker.
 * 
 * @author pablocingolani
 */
public class VcfWorkQueue {

	String fileName;
	int batchSize, showEvery;
	Class<? extends Actor> masterClazz;
	Props masterProps;
	Config config;

	public VcfWorkQueue(String fileName, Config config, int batchSize, int showEvery, Props masterProps) {
		this.fileName = fileName;
		this.batchSize = batchSize;
		this.showEvery = showEvery;
		this.masterProps = masterProps;
		this.config = config;
	}

	public void run(boolean wait) {
		// Create an Akka system
		ActorSystem workQueue = ActorSystem.create("vcfWorkQueue");

		// Create the master
		ActorRef master;
		if (masterClazz != null) master = workQueue.actorOf(new Props(masterClazz), "masterVcf");
		else master = workQueue.actorOf(masterProps, "masterVcf");

		// Start processing
		master.tell(new StartMasterVcf(fileName, config, batchSize, showEvery));

		// Wait until completion
		if (wait) workQueue.awaitTermination();
	}
}
