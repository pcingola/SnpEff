package ca.mcgill.mcb.pcingola.akka.msg;

/**
 * A result form a work
 * @author pablocingolani
 *
 * @param <T>
 */
public class Result<T> {
	public final long serialNumber;
	public final T[] data;

	@SuppressWarnings("rawtypes")
	public Result(Work work, T[] data) {
		this.serialNumber = work.serialNumber;
		this.data = data;
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