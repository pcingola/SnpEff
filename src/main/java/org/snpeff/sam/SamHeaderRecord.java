package org.snpeff.sam;

/**
 * Sam header record
 * 
 * @author pcingola
 */
public abstract class SamHeaderRecord {

	String recordTypeCode;

	public SamHeaderRecord(String line, String recordTypeCode) {
		this.recordTypeCode = recordTypeCode;
		init();
		parse(line);
	}

	public String getRecordTypeCode() {
		return recordTypeCode;
	}

	/**
	 * Initialize records before parsing
	 */
	protected void init() {}

	/**
	 * Parse a line for this record type
	 * @param line
	 * @param recordTypeCode
	 */
	public void parse(String line) {
		init();

		// Check record type
		if( !line.startsWith("@" + recordTypeCode) ) throw new RuntimeException("Header line is not type '" + recordTypeCode + "': " + line);

		// Split fields
		String fields[] = line.split("\t");
		recordTypeCode = fields[0].substring(1);

		// Parse each field
		for( int i = 1; i < fields.length; i++ ) {
			String tag = fields[i].substring(0, 2);
			String value = fields[i].substring(3);
			parseField(tag, value);
		}
	}

	/**
	 * Parse a field for this record
	 * @param tag
	 * @param value
	 */
	abstract protected void parseField(String tag, String value);

}
