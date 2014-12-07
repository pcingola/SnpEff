package ca.mcgill.mcb.pcingola.vcf;

public class VcfInfoGenotype extends VcfHeaderInfo {

	/**
	 * Constructor using a "##INFO" line from a VCF file
	 */
	public VcfInfoGenotype(String line) {
		super(line);
		genotype = true;
	}

	public VcfInfoGenotype(String id, VcfInfoType vcfInfoType, String number, String description) {
		super(id, vcfInfoType, number, description);
		genotype = true;
	}

	@Override
	public String toString() {
		if (line != null) return line;

		return "##FORMAT=<ID=" + id//
				+ ",Number=" + (number >= 0 ? number : vcfInfoNumber) //
				+ ",Type=" + vcfInfoType //
				+ ",Description=\"" + description + "\"" //
				+ ">" //
				;
	}

}
