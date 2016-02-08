package org.snpeff.fileIterator;

import java.util.HashMap;

import org.snpeff.vcf.VcfInfoType;

/**
 * Given a table in a TXT file, try to guess the value types for each column
 *
 * @author pcingola
 */
public class GuessTableTypes {

	public static boolean debug = true;
	public static int MIN_LINES = 100 * 1000; // Analyze at least this many lines (because some types might change)

	String fileName;
	String headerPrefix = "#";
	String columnSeparator = "\t";
	String subfieldSeparator = ";";
	String fieldNames[] = null;
	VcfInfoType types[] = null;
	boolean multipleValues[] = null;
	HashMap<String, Integer> names2index;

	public GuessTableTypes(String fileName) {
		this.fileName = fileName;
	}

	public boolean foundAllTypes() {
		for (int i = 0; i < types.length; i++)
			if (types[i] == null) return false;
		return true;
	}

	public String[] getFieldNames() {
		return fieldNames;
	}

	public String getFileName() {
		return fileName;
	}

	public boolean[] getMultipleValues() {
		return multipleValues;
	}

	public HashMap<String, Integer> getNames2index() {
		return names2index;
	}

	/**
	 * Get field type
	 * @param fieldName
	 * @return
	 */
	public VcfInfoType getType(String fieldName) {
		Integer idx = names2index.get(fieldName);
		if (idx == null) return null;
		return types[idx];
	}

	/**
	 * Get an array of types (ordered by column)
	 * @return
	 */
	public VcfInfoType[] getTypes() {
		return types;
	}

	/**
	 * Guess data type for this value
	 * @param value
	 * @return
	 */
	public VcfInfoType guessType(String value) {
		// Empty? Nothing to do
		if (value == null || value.isEmpty() || value.equals(".")) return null;

		//---
		// Do we have multiple valued field? Split it
		//---
		if (isMultiple(value)) {
			String values[] = value.split(subfieldSeparator);

			VcfInfoType type = null;
			for (String val : values) {
				VcfInfoType valType = guessType(val);
				if (type == null) type = valType;
				else if (valType == null) continue; // We cannot infer this sub-field's data type. No problem
				else if (type != valType) return null; // There is no consensus on the data type of each sub-field => null
			}

			return type;
		}

		//---
		// There is only one value. Let's try to guess what it is
		//---
		try {
			Long.parseLong(value);
			return VcfInfoType.Integer;
		} catch (Exception e) {
			// OK, it was not an integer
		}

		try {
			Double.parseDouble(value);
			return VcfInfoType.Float;
		} catch (Exception e) {
			// OK, it was not a float
		}

		// Is it a character?
		if (value.length() == 1) return VcfInfoType.Character;

		// Is it a flag?
		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false") || value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("no")) return VcfInfoType.Flag;

		// OK, it's a string then

		return VcfInfoType.String;
	}

	/**
	 * Find column names form header and guess data types from values
	 *
	 * @return true of OK, false if there was an error parsing header or data
	 */
	public boolean guessTypes() {
		boolean header = true;
		if (headerPrefix == null) headerPrefix = "";

		// Iterate parsing lines until we guessed all data types (lines can have empty values, so we may not be able to guess them in the first line).
		LineFileIterator lfi = new LineFileIterator(fileName);
		for (String line : lfi) {
			if (header) {
				// Parse header
				header = false;

				// Make sure this is the header
				if (headerPrefix.isEmpty() || line.startsWith(headerPrefix)) {
					line = line.substring(headerPrefix.length());
					fieldNames = line.split(columnSeparator);
					types = new VcfInfoType[fieldNames.length];
					multipleValues = new boolean[fieldNames.length];

					names2index = new HashMap<String, Integer>();
					for (int i = 0; i < fieldNames.length; i++)
						names2index.put(fieldNames[i], i);
				} else {
					// Cannot parse header!
					return false;
				}
			} else {
				if (multipleValues == null) throw new RuntimeException("Cannot parse file '" + fileName + "'. Missing header?");

				boolean done = true;
				String values[] = line.split(columnSeparator);
				for (int i = 0; i < fieldNames.length; i++) {
					// We don't know the type yet? Try to guess it
					VcfInfoType type = guessType(values[i]);

					if (fieldNames[i].equals("1000Gp1_AMR_AF") //
							&& values[i] != null //
					// && !values[i].equals(".") //
					) System.err.println("line: " + lfi.getLineNum() + "\tfield[" + i + "]: '" + fieldNames[i] + "'\tfield_type: " + types[i] + "'\tvalue_type: " + type + "\tdata: '" + values[i] + "'");

					if (types[i] == null) {
						types[i] = type;
					} else {
						// Some types can 'change'
						if (types[i] == VcfInfoType.Integer && type == VcfInfoType.Float) types[i] = type;
						else if (type == VcfInfoType.String) types[i] = type;
					}

					// Do we have multiple values per field?
					multipleValues[i] |= isMultiple(values[i]);
					done &= (types[i] != null);
				}

				// Have we guessed all types? => We are done
				if (done && lfi.getLineNum() > MIN_LINES) {
					lfi.close();
					return true;
				}
			}
		}

		lfi.close();
		return false;
	}

	/**
	 * Do we have multiple values separated by 'subfieldSeparator'?
	 * @param value
	 * @return
	 */
	boolean isMultiple(String value) {
		return value.indexOf(subfieldSeparator) >= 0;
	}

	/**
	 * Has this field multiple values
	 * @param fieldName
	 * @return
	 */
	public Boolean isMultipleValues(String fieldName) {
		Integer idx = names2index.get(fieldName);
		if (idx == null) return null;
		return multipleValues[idx];
	}

	public boolean parsedHeader() {
		return fieldNames != null;
	}

	public void setColumnSeparator(String columnSeparator) {
		this.columnSeparator = columnSeparator;
	}

	public void setSubfieldSeparator(String subfieldSeparator) {
		this.subfieldSeparator = subfieldSeparator;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fieldNames.length; i++)
			sb.append(fieldNames[i] + "\t" + types[i] + (multipleValues[i] ? "\tmultiple" : "") + "\n");
		return sb.toString();
	}
}
