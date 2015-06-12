package ca.mcgill.mcb.pcingola.vcf;

public class VcfHeaderInfoGenotype extends VcfHeaderInfo {

	/**
	 * Constructor using a "##FORMAT" line from a VCF file
	 */
	public VcfHeaderInfoGenotype(String line) {
		super(line);
		genotype = true;
	}

	public VcfHeaderInfoGenotype(String id, VcfInfoType vcfInfoType, String number, String description) {
		super(id, vcfInfoType, number, description);
		genotype = true;
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
