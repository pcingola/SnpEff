package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.binseq.DnaSequence;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Interval for the whole chromosome
 * If a SNP has no 'ChromosomeInterval' => it is outside the chromosome => Invalid
 *
 * @author pcingola
 *
 */
public class Chromosome extends Marker {

	private static final long serialVersionUID = 1636197649250882952L;

	/**
	 * Compare chromosome names
	 */
	public static int compare(String chr1, String chr2) {
		// Try to compare numbers
		int chr1Num = number(chr1);
		int chr2Num = number(chr2);
		if (chr1Num > 0 && chr2Num > 0) return chr1Num - chr2Num;
		if (chr1Num > 0 && chr2Num <= 0) return -1;
		if (chr1Num <= 0 && chr2Num > 0) return 1;

		// Numbers did not work, compare strings
		return simpleName(chr1).compareTo(simpleName(chr2));
	}

	/**
	 * Convert to chromosome number (return '0' if it cannot be converted)
	 */
	public static int number(String chrName) {
		return Gpr.parseIntSafe(ChromosomeSimpleName.get(chrName));
	}

	/**
	 * Simplify chromosome name
	 */
	public static String simpleName(String chrName) {
		return ChromosomeSimpleName.get(chrName);
	}

	double chromosomeNum;

	DnaSequence sequence = null;

	public Chromosome() {
		super();
	}

	public Chromosome(Genome parent, int start, int end, String id) {
		super(null, start, end, false, id); // Parent = null to avoid sanity check (it will always fail for chromosomes)
		this.parent = parent;
		type = EffectType.CHROMOSOME;
		setChromosomeName(id);
	}

	/**
	 * Compare only chromosome's name
	 * @param i2
	 * @return
	 */
	public int compareChromoName(Interval interval) {
		Chromosome i2 = (Chromosome) interval;

		// Both of them are non-numeric: Compare as string
		if ((chromosomeNum == 0) && (i2.chromosomeNum == 0)) return id.compareTo(i2.id);

		// One of them is a number? => the number goes first
		if ((chromosomeNum != 0) && (i2.chromosomeNum == 0)) return -1;
		if ((chromosomeNum == 0) && (i2.chromosomeNum != 0)) return 1;

		// Use numeric comparison
		if (chromosomeNum - i2.chromosomeNum < 0) return -1;
		if (chromosomeNum - i2.chromosomeNum > 0) return 1;
		return 0;
	}

	public DnaSequence getDnaSequence() {
		return sequence;
	}

	public String getSequence() {
		return sequence.toString();
	}

	/**
	 * Is this a mitochondrial chromosome?
	 * Note: This is a wild guess just by looking at the name
	 */
	public boolean isMt() {
		String iduc = id.toUpperCase();
		return iduc.equals("M") //
				|| iduc.startsWith("MT") //
				|| (iduc.indexOf("MITO") >= 0) //
		;
	}

	@Override
	protected boolean isShowWarningIfParentDoesNotInclude() {
		return false;
	}

	/**
	 * Parse a line from a serialized file
	 * @param line
	 * @return
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		setChromosomeName(id);
	}

	/**
	 * Set chromosome name
	 * Note: Removes prefixes (such as 'chr') and parse numeric version.
	 * E.g. 'chr2' becomes '2'. Also numeric '2' is assigned to 'chromosomeNum' to facilitate order by number (so that '2' is ordered before '21')
	 * @param chromo
	 */
	private void setChromosomeName(String chromo) {
		id = simpleName(chromo);
		chromosomeNum = Gpr.parseIntSafe(id); // Try to parse a numeric string
	}

	public void setLength(int len) {
		end = len - 1; // Remember that intervals are zero-based
	}

	/**
	 * Set sequence for this chromosome
	 * @param sequenceStr
	 */
	public void setSequence(String sequenceStr) {
		sequence = new DnaSequence(sequenceStr, true);
		setLength(sequenceStr.length()); // Update chromosome length

	}

}
