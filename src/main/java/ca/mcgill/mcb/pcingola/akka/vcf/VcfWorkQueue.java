package ca.mcgill.mcb.pcingola.akka.vcf;

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

	public VcfWorkQueue(String fileName, int batchSize, int showEvery, Class<? extends Actor> masterClazz) {
		this.fileName = fileName;
		this.batchSize = batchSize;
		this.showEvery = showEvery;
		this.masterClazz = masterClazz;
	}

	public VcfWorkQueue(String fileName, int batchSize, int showEvery, Props masterProps) {
		this.fileName = fileName;
		this.batchSize = batchSize;
		this.showEvery = showEvery;
		this.masterProps = masterProps;
	}

	public void run(boolean wait) {
		// Create an Akka system
		ActorSystem workQueue = ActorSystem.create("vcfWorkQueue");

		// Create the master
		ActorRef master;
		if (masterClazz != null) master = workQueue.actorOf(new Props(masterClazz), "masterVcf");
		else master = workQueue.actorOf(masterProps, "masterVcf");

		// Start processing
		master.tell(new StartMasterVcf(fileName, batchSize, showEvery));

		// Wait until completion
		if (wait) workQueue.awaitTermination();
	}
}
