package org.snpeff.sam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Sam header
 * 
 * @author pcingola
 */
public class SamHeader implements Iterable<String> {

	HashMap<String, ArrayList<SamHeaderRecord>> recordsByType;
	ArrayList<String> lines;

	public SamHeader() {
		recordsByType = new HashMap<String, ArrayList<SamHeaderRecord>>();
		lines = new ArrayList<String>();
	}

	/**
	 * Add a record to the header
	 */
	public void add(SamHeaderRecord samHeaderRecord) {
		ArrayList<SamHeaderRecord> records = getRecords(samHeaderRecord.getRecordTypeCode());
		records.add(samHeaderRecord);
	}

	public void addHeaderRecord(String line) {
		if( !line.startsWith("@") ) throw new RuntimeException("Record type must start with '@'. Header line: " + line);

		// Add line
		lines.add(line);

		// Parse records
		SamHeaderRecord shr = null;
		if( line.startsWith("@SQ") ) shr = new SamHeaderRecordSq(line);

		if( shr != null ) add(shr);
	}

	/**
	 * Get a list of records for this 'recordType'
	 * @param recordType
	 * @return A new empty list is created if no records are available
	 */
	public ArrayList<SamHeaderRecord> getRecords(String recordType) {
		ArrayList<SamHeaderRecord> records = recordsByType.get(recordType);
		if( records == null ) {
			records = new ArrayList<SamHeaderRecord>();
			recordsByType.put(recordType, records);
		}
		return records;
	}

	@Override
	public Iterator<String> iterator() {
		return lines.iterator();
	}

}
