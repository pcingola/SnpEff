package org.snpeff.akka.msg;

/**
 * A message telling master process to start calculating
 * 
 * @author pablocingolani
 */
public class StartMaster {

	public final int batchSize;
	public final int showEvery;

	public StartMaster(int batchSize, int showEvery) {
		this.batchSize = batchSize;
		this.showEvery = showEvery;
	}
}