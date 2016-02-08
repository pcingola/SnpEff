package org.snpeff.vcf;

/**
 * Represents a info elements in a VCF file's header
 *
 * References: http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
 *
 * @author pablocingolani
 */
public class VcfHeaderEntry {

	protected String line;
	protected String id;

	public static VcfHeaderEntry factory(String line) {
		if (line.startsWith("##FORMAT=")) return new VcfHeaderInfoGenotype(line);
		if (line.startsWith("##INFO=")) return new VcfHeaderInfo(line);
		return new VcfHeaderEntry(line);
	}

	/**
	 * Constructor
	 */
	public VcfHeaderEntry(String line) {
		this.line = line;
		id = line;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return line;
	}
}
