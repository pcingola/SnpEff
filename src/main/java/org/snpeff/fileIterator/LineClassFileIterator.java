package org.snpeff.fileIterator;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;

import org.snpeff.util.Gpr;

/**
 * Iterate on each line. Create and populate objects based on 'fieldNames'
 * definition: a colon separated list of fields.
 * 
 * Note: You can use empty names to skip columns
 * 
 * @author pcingola
 */
@SuppressWarnings("rawtypes")
public class LineClassFileIterator<T> extends FileIterator<T> {

	public static boolean debug = false;

	public static final char FIELD_NAME_SEPARATOR = ';';

	protected String separator = "\t";
	protected String fieldStr;
	protected String fieldNames[];
	protected HashMap<String, Field> fieldByName;
	protected Class clazz;

	public LineClassFileIterator(String fileName, Class clazz) {
		super(fileName);
		line = null;
		lineNum = 0;
		reader = null;
		this.clazz = clazz;
		reader = Gpr.reader(fileName);

		this.fieldStr = guessFields();
		if (fieldStr != null)
			fieldMap();
	}

	public LineClassFileIterator(String fileName, Class clazz, String fieldNames) {
		super(fileName);
		line = null;
		lineNum = 0;
		reader = null;
		this.clazz = clazz;
		reader = Gpr.reader(fileName);

		this.fieldStr = fieldNames;
		if (fieldStr != null)
			fieldMap();
	}

	/**
	 * Create an object using
	 * 
	 * @param line
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected T createObject(String line) {
		String vals[] = line.split(separator);

		// Create a new instance
		T tObj;
		try {
			tObj = (T) clazz.getConstructor().newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// Populate values
		int maxFieldNum = Math.min(vals.length, fieldNames.length);
		for (int i = 0; i < maxFieldNum; i++) {
			// If the name of the field is empty => Skip this field
			if (!fieldNames[i].isEmpty()) {
				Field field = fieldByName.get(fieldNames[i]);

				// Assign each field
				String typeName = field.getType().getCanonicalName();
				try {
					String valOri = vals[i];
					if (typeName.equals("java.lang.String"))
						field.set(tObj, valOri);
					else {
						// Numeric values have to be trimmed
						String valTrim = valOri.trim();

						if (typeName.equals("int"))
							field.setInt(tObj, Gpr.parseIntSafe(valTrim));
						else if (typeName.equals("long"))
							field.setLong(tObj, Gpr.parseLongSafe(valTrim));
						else if (typeName.equals("double"))
							field.setDouble(tObj, Gpr.parseDoubleSafe(valTrim));
						else if (typeName.equals("float"))
							field.setFloat(tObj, Gpr.parseFloatSafe(valTrim));
						else
							throw new RuntimeException(
									"Unsoported parsing for object type '" + typeName + "' cannot be parsed.");
					}
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		return tObj;
	}

	/**
	 * Map field names to Field objects
	 */
	protected void fieldMap() {
		fieldNames = fieldStr.split("" + FIELD_NAME_SEPARATOR);
		fieldByName = new HashMap<String, Field>();

		for (String fname : fieldNames) {
			try {
				if (!fname.isEmpty()) {
					Field field = clazz.getField(fname);
					fieldByName.put(fname, field);
				}
			} catch (NoSuchFieldException e) {
				throw new RuntimeException("Error: Field '" + fname + "' not found for class '"
						+ clazz.getCanonicalName() + "' (may be the fields is not public?)");
			}
		}
	}

	/**
	 * Use field order as returned by 'class.getFields()' WARNING: As far as I know,
	 * the JVM does specify a sort order. So this might not work on all platforms as
	 * expected.
	 * 
	 * @return
	 */
	String guessFields() {
		StringBuilder sb = new StringBuilder();

		Field fields[] = clazz.getFields();
		for (Field f : fields)
			sb.append(f.getName() + FIELD_NAME_SEPARATOR);

		// Remove last ';'
		if (sb.charAt(sb.length() - 1) == FIELD_NAME_SEPARATOR)
			sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	@Override
	public Iterator<T> iterator() {
		return this;
	}

	/**
	 * Read a sequence from the file
	 * 
	 * @return
	 */
	@Override
	protected T readNext() {
		try {
			if (reader.ready()) {
				while ((line = reader.readLine()) != null) { // Read a line (only if needed)
					if (!line.trim().isEmpty()) { // Non empty line? Create object
						lineNum++;
						return createObject(line);
					}
				}
			}
		} catch (IOException e) {
			return null;
		}

		return null;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}
}
