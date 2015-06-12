package ca.mcgill.mcb.pcingola.akka;

import akka.actor.UntypedActor;
import ca.mcgill.mcb.pcingola.akka.msg.Result;
import ca.mcgill.mcb.pcingola.akka.msg.Work;

/**
 * Worker: Performs a simple work and get the data back
 * 
 * TI: Data type in (input for this calculation)
 * TO: Data type out (result form the calculation)
 * 
 * @author pablocingolani
 */
public class Worker<TI, TO> extends UntypedActor {

	private static int WORKER_ID = 0;
	private static String WORKER_ID_MUTEX = "WORKER_ID_MUTEX";

	protected int id;

	public static int getId() {
		synchronized (WORKER_ID_MUTEX) {
			return WORKER_ID++;
		}
	}

	public Worker() {
		id = getId();
	}

	/**
	 * Perform main calculation
	 * You must override this method to perform whatever calculation you want to implement
	 */
	public TO calculate(TI data) {
		throw new RuntimeException("Method calculate() not implemented in class " + this.getClass().getSimpleName());
	}

	/**
	 * Perform some useful action
	 */
	@SuppressWarnings("unchecked")
	public Result<TO> calculate(Work<TI> work) {
		// Create array
		TO resultData[] = (TO[]) new Object[work.data.length];

		// Calculate each result
		int i = 0;
		for (TI datIn : work.data)
			resultData[i++] = calculate(datIn);

		// Build Result
		return new Result<TO>(work, resultData);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object message) {
		if (message instanceof Work) {
			// Perform some kind of calculation
			Result<TO> result = calculate((Work<TI>) message);
			getSender().tell(result, getSelf());
		} else unhandled(message);
	}
}