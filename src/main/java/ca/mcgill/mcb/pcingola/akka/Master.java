package ca.mcgill.mcb.pcingola.akka;

import java.util.HashMap;
import java.util.Iterator;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.SmallestMailboxRouter;
import ca.mcgill.mcb.pcingola.akka.msg.Result;
import ca.mcgill.mcb.pcingola.akka.msg.StartMaster;
import ca.mcgill.mcb.pcingola.akka.msg.Work;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Master: Distributes the jobs to all workers, sends the results to 'listener'
 * 
 * @author pablocingolani
 *
 * @param <TI>
 * @param <TO>
 */
public abstract class Master<TI, TO> extends UntypedActor implements Iterable<TI>, Iterator<TI> {

	public static int LOAD_FACTOR = 3; // Number of works distributed per worker

	// Show progress
	public static final int DEFAULT_SHOW_EVERY = 1000;

	// Default number of entries processed in each work
	public static final int DEFAULT_BATCH_SIZE = 10;

	// Debug mode?
	public static boolean debug = false;

	protected int batchSize; // Number of input objects sent to each worker per 'Work' message 
	protected int showEvery; // Show progress (print '.' on STDOUT)
	protected long nextOutput; // Serial number of next output
	protected int countInputObjects; // Count number of input objects
	protected int numWorkers; // Number of workers (threads)
	protected int sentWorks; // Number of workers still working
	protected ActorRef workerRouter;
	protected HashMap<Long, Result<TO>> worksBySerial;

	/**
	 * Constructor
	 * @param numWorkers: How may workers will be used
	 * @param listener : Listener
	 */
	public Master(Props props, int numWorkers) {
		// Initialize 
		sentWorks = 0;
		this.numWorkers = numWorkers;
		worksBySerial = new HashMap<Long, Result<TO>>();
		nextOutput = Work.FIRST_SERIAL_NUMBER;
		batchSize = DEFAULT_BATCH_SIZE;
		showEvery = DEFAULT_SHOW_EVERY;
		countInputObjects = 1;

		// Create router
		SmallestMailboxRouter router = new SmallestMailboxRouter(numWorkers);
		workerRouter = getContext().actorOf(props.withRouter(router), "workerRouter");
	}

	@Override
	public abstract boolean hasNext();

	/**
	 * Return a class that iterates Works (creates works)
	 * @return
	 */
	public Iterable<Work<TI>> iterableWork() {
		final Master<TI, TO> master = this;

		return new Iterable<Work<TI>>() {

			@Override
			public Iterator<Work<TI>> iterator() {
				return new Iterator<Work<TI>>() {

					@Override
					public boolean hasNext() {
						return master.hasNext();
					}

					@SuppressWarnings("unchecked")
					@Override
					public Work<TI> next() {
						// Create data array
						TI data[] = (TI[]) new Object[batchSize];
						for (int i = 0; (i < batchSize) && hasNext(); i++) {
							data[i] = master.next();
							if (debug) Gpr.debug("countInputObjects:" + countInputObjects);
							if (showEvery > 0) Gpr.showMark(countInputObjects++, showEvery);
						}

						// Create work
						return new Work<TI>(data);
					}

					@Override
					public void remove() {
						throw new RuntimeException("Unimplemented method");
					}
				};
			}
		};
	}

	@Override
	public Iterator<TI> iterator() {
		return this;
	}

	/**
	 * Get next item
	 * @return
	 */
	@Override
	public abstract TI next();

	/**
	 * Master-receive: Handle-messages
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(Object message) {

		//---
		// Message specific processing
		//---
		if (message instanceof StartMaster) {
			if (debug) Gpr.debug("Start");

			// Process start message
			StartMaster start = (StartMaster) message;
			startMaster(start);
		} else if (message instanceof Result) {
			// Process results 
			sentWorks--;
			Result<TO> result = (Result<TO>) message;
			if (debug) Gpr.debug("Result: " + result.serialNumber);
			processResults(result);
		} else unhandled(message);

		//---
		// Distribute some work
		//---
		int threshold = LOAD_FACTOR * numWorkers; // We want all the workers to have some work at all times
		if (sentWorks < threshold) {
			// Send all works to workers
			for (Work<TI> work : this.iterableWork()) {
				sentWorks++;
				workerRouter.tell(work, getSelf());
				if (debug) Gpr.debug("Sent: " + work.serialNumber);
				if (sentWorks >= threshold) break; // Enough, wait until we get some results
			}

			// Nothing else to do? => Stop
			if ((sentWorks == 0) && !hasNext()) shutdown(); // Shutdown all agents (including router)
		}
	}

	/**
	 * Output all contents of a result
	 * @param result
	 */
	protected void output(Result<TO> result) {
		for (TO t : result.data)
			if (t != null) output(t);
	}

	/**
	 * Output a result datum
	 * You should probably override this method
	 * 
	 * @param result
	 */
	protected void output(TO output) {
		System.out.println(output);
	}

	/**
	 * Print results in the same order they were sent to the processing queue (i.e. ordered by Work.serialNumber)
	 * @param result
	 */
	protected void processResults(Result<TO> result) {
		worksBySerial.put(result.serialNumber, result);

		// Is the next one ready?
		while (worksBySerial.containsKey(nextOutput)) {
			Result<TO> out = worksBySerial.get(nextOutput);
			output(out); // Output next one
			worksBySerial.remove(nextOutput); // Remove from hash
			nextOutput++; // Update counter
		}
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented method");
	}

	/**
	 * Shut down all agents
	 */
	protected void shutdown() {
		if (debug) Gpr.debug("Shutting down");
		getContext().system().shutdown();
	}

	/**
	 * This is executed when the 'start' message arrives (at the beginning of the processing)
	 * @param startMaster
	 */
	protected void startMaster(StartMaster startMaster) {
		// Initialize parameters
		batchSize = startMaster.batchSize;
		showEvery = startMaster.showEvery;
	}

}
