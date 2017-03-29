package org.snpeff.vcf;

public class VcfHeaderFormat extends VcfHeaderInfo {

	/**
	 * Constructor using a "##FORMAT" line from a VCF file
	 */
	public VcfHeaderFormat(String line) {
		super(line);
	}

	public VcfHeaderFormat(String id, VcfInfoType vcfInfoType, String number, String description) {
		super(id, vcfInfoType, number, description);
	}

	@Override
	public boolean isFormat() {
		return true;
	}

	@Override
	public String toString() {
		if (line != null) return line;

		return VcfHeader.FORMAT_PREFIX //
				+ "<ID=" + id//
				+ ",Number=" + (number >= 0 ? number : vcfInfoNumber) //
				+ ",Type=" + vcfInfoType //
				+ ",Description=\"" + description + "\"" //
				+ ">" //
				;
	}

}
