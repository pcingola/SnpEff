package org.snpeff.sam;

import org.snpeff.util.Gpr;
import org.snpeff.util.Log;

/**
 * SQ header: Reference sequence dictionary. The order of @SQ lines defines the alignment sorting order.
 *
 * @author pcingola
 */
public class SamHeaderRecordSq extends SamHeaderRecord {

	String sequenceName, assembly, md5, species, uri;
	int length;

	public SamHeaderRecordSq(String line) {
		super(line, "SQ");
	}

	public String getAssembly() {
		return assembly;
	}

	public int getLength() {
		return length;
	}

	public String getMd5() {
		return md5;
	}

	public String getSequenceName() {
		return sequenceName;
	}

	public String getSpecies() {
		return species;
	}

	public String getUri() {
		return uri;
	}

	@Override
	protected void init() {
		sequenceName = assembly = md5 = species = uri = "";
		length = 0;
	}

	@Override
	protected void parseField(String tag, String value) {
		if (tag.equals("SN")) sequenceName = value;
		else if (tag.equals("LN")) length = Gpr.parseIntSafe(value);
		else if (tag.equals("AS")) assembly = value;
		else if (tag.equals("M5")) md5 = value;
		else if (tag.equals("SP")) species = value;
		else if (tag.equals("UR")) uri = value;
		else Log.debug("Unknown tag '" + tag + "' for header record type '" + recordTypeCode + "'.");
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('@' + recordTypeCode);

		if (!sequenceName.isEmpty()) sb.append("\tSN:" + sequenceName);
		if (length > 0) sb.append("\tLN:" + length);
		if (!assembly.isEmpty()) sb.append("\tAS:" + sequenceName);
		if (!md5.isEmpty()) sb.append("\tM5:" + sequenceName);
		if (!species.isEmpty()) sb.append("\tSP:" + sequenceName);
		if (!uri.isEmpty()) sb.append("\tUR:" + sequenceName);

		return sb.toString();
	}

}
