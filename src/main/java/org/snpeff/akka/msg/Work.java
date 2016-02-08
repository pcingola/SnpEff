package org.snpeff.akka.msg;

/**
 * A message in the AKKA system.
 * Message sent from the master to the worker
 * 
 * @author pablocingolani
 *
 * @param <T>
 */
public class Work<T> {

	public static final long FIRST_SERIAL_NUMBER = 1;

	private static final String SERIAL_NUMBER_MUTEX = "SERIAL_NUMBER_MUTEX";
	private static long SERIAL_NUMBER = FIRST_SERIAL_NUMBER;

	public final long serialNumber;
	public final T[] data;

	public static long getSerianlNumber() {
		synchronized (SERIAL_NUMBER_MUTEX) {
			return SERIAL_NUMBER++;
		}
	}

	public Work(T[] data) {
		this.serialNumber = getSerianlNumber();
		this.data = data;
	}

	public int size() {
		return data.length;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// Show serial number
		sb.append("Serial: " + serialNumber + "\t");

		// Show all data
		for (T t : data)
			sb.append(t + "\t");

		sb.deleteCharAt(sb.length() - 1); // Remove last '\t'

		return sb.toString();
	}
}