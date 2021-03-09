package org.snpeff.fileIterator;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * Load a table from a file. The table is loaded into an arbitrary object having
 * arrays to hold the data.
 *
 * @author pcingola
 */
public class TableFile implements Serializable {

	private static final long serialVersionUID = 9180460090637138107L;

	protected static final char FIELD_NAME_SEPARATOR = ';';

	protected int size;
	protected boolean verbose;
	protected boolean hasTitle;
	protected String fileName;
	protected String separator = "\t";
	protected String fieldStr;
	protected String fieldNames[];
	protected HashMap<String, Field> fieldByName;

	public TableFile(int size) {
		this.size = size;
		initArrays(size);
	}

	public TableFile(String fileName) {
		init(fileName, guessFields());
	}

	public TableFile(String fileName, String fieldStr) {
		init(fileName, fieldStr);
	}

	/**
	 * Assign values parse from 'line' into array elements indexed by 'idx'
	 *
	 * @param line
	 * @param idx
	 */
	protected void assign(String line, int idx) {
		String vals[] = line.split(separator);

		// Populate values
		int maxFieldNum = Math.min(vals.length, fieldNames.length);
		for (int i = 0; i < maxFieldNum; i++) {
			// If the name of the field is empty => Skip this field
			if (!fieldNames[i].isEmpty()) {
				try {
					// Get array
					Field field = fieldByName.get(fieldNames[i]);
					Object array = field.get(this);

					// Get type
					String typeName = field.getType().getCanonicalName();

					// Get value
					Object value = null;
					String valOri = vals[i];
					if (typeName.equals("java.lang.String[]")) value = valOri;
					else {
						// Numeric values have to be trimmed
						String valTrim = valOri.trim();

						if (typeName.equals("int[]")) value = Gpr.parseIntSafe(valTrim);
						else if (typeName.equals("long[]")) value = Gpr.parseLongSafe(valTrim);
						else if (typeName.equals("double[]")) value = Gpr.parseDoubleSafe(valTrim);
						else if (typeName.equals("float[]")) value = Gpr.parseFloatSafe(valTrim);
						else if (typeName.equals("boolean[]")) value = Gpr.parseBoolSafe(valTrim);
						else throw new RuntimeException("Unsoported parsing for object type '" + typeName + "' cannot be parsed.");
					}

					// Assign: array[idx] = value
					Array.set(array, idx, value);

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	/**
	 * Try to guess field order
	 *
	 * @return
	 */
	String guessFields() {
		StringBuilder sb = new StringBuilder();

		Field fields[] = getClass().getFields();
		for (Field f : fields)
			sb.append(f.getName() + FIELD_NAME_SEPARATOR);

		// Remove last ';'
		if (sb.charAt(sb.length() - 1) == FIELD_NAME_SEPARATOR) sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	/**
	 * Initialize object
	 *
	 * @param fileName
	 * @param fieldStr
	 */
	void init(String fileName, String fieldStr) {
		this.fileName = fileName;
		this.fieldStr = fieldStr;

		verbose = false;
		hasTitle = true;

		if (fileName != null) load();
	}

	/**
	 * Initialize arrays
	 *
	 * @param size
	 */
	@SuppressWarnings("rawtypes")
	void initArrays(int size) {
		initFields();

		// For each field...
		for (Field f : fieldByName.values()) {
			// Get type name
			String canName = f.getType().getCanonicalName();
			int idx = canName.indexOf('[');
			if (idx < 0) throw new RuntimeException("Cannot parse aarray class '" + canName + "'");
			String baseClassName = canName.substring(0, idx);

			try {
				// Find class based on type name
				Class clazz = null;

				if (baseClassName.equals("int")) clazz = int.class;
				else if (baseClassName.equals("byte")) clazz = byte.class;
				else if (baseClassName.equals("short")) clazz = short.class;
				else if (baseClassName.equals("long")) clazz = long.class;
				else if (baseClassName.equals("double")) clazz = double.class;
				else if (baseClassName.equals("float")) clazz = float.class;
				else if (baseClassName.equals("boolean")) clazz = boolean.class;
				else if (baseClassName.equals("char")) clazz = char.class;
				else clazz = Class.forName(baseClassName);

				// Create array and set field to the newly constructed array
				Object array = Array.newInstance(clazz, size);
				f.set(this, array);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	/**
	 * Map field names to Field objects
	 */
	protected void initFields() {
		if (fieldByName == null) {
			if (fieldStr == null) fieldStr = guessFields(); // No fields data? guess them.

			fieldNames = fieldStr.split("" + FIELD_NAME_SEPARATOR);
			fieldByName = new HashMap<String, Field>();

			for (String fname : fieldNames) {
				try {
					if (!fname.isEmpty()) {
						Field field = this.getClass().getField(fname);
						fieldByName.put(fname, field);
					}
				} catch (NoSuchFieldException e) {
					throw new RuntimeException("Error: Field '" + fname + "' not found for class '" + this.getClass().getCanonicalName() + "' (may be the fields is not public?)");
				}
			}
		}
	}

	/**
	 * Load all data into memory
	 */
	public void load() {
		initFields();

		if (verbose) Log.info("Counting lines from '" + fileName + "'");
		size = Gpr.countLines(fileName);
		if (hasTitle) size--;
		if (verbose) Log.info("Done. " + size + " data lines.");

		// Initialize all array objects
		initArrays(size);

		// Read file
		if (verbose) Log.info("Loading file '" + fileName + "'");
		int idx = 0;
		LineFileIterator lfi = new LineFileIterator(fileName);
		for (String line : lfi)
			if ((lfi.lineNum > 1) || (!hasTitle)) assign(line, idx++);

		if (verbose) Log.info("Done. " + idx + " lines loaded.");

		// Clean up
		lfi.close();
	}

	/**
	 * Save this object to a binary file (using Java serialization)
	 *
	 * @param binaryFile
	 */
	public void save(String binaryFile) {
		fieldByName = null; // This one is not serializable (and we don't really need it anyway)

		if (verbose) Log.info("Saving to file '" + binaryFile + "'");
		Gpr.toFileSerialize(binaryFile, this);
		if (verbose) Log.info("Done.");
	}

	/**
	 * Array size
	 *
	 * @return
	 */
	public int size() {
		return size;
	}

	/**
	 * Return line number 'index' as a string
	 *
	 * @param index
	 * @return
	 */
	public String toString(int index) {
		StringBuilder sb = new StringBuilder();

		initFields();

		// For each field
		for (String fname : fieldNames) {
			if (!fname.isEmpty()) { // Non empty field?
				try {
					// Get field, array and value = array[index]
					Field f = fieldByName.get(fname);
					Object array = f.get(this);
					Object value = Array.get(array, index);

					// Append value to this line
					sb.append(value + "\t");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			}
		}

		return sb.toString();
	}
}
