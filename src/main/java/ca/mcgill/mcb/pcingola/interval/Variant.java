package ca.mcgill.mcb.pcingola.interval;

import ca.mcgill.mcb.pcingola.snpEffect.ChangeEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * A variant represents a change in a reference sequence
 *
 * Notes:
 * 		 This class was previously known as SeqChange.
 *
 *       As of version 4.0, variants in the negative strand
 *       are NOT allowed any more (they just complicate the
 *       code and bring no real benefit).
 *
 *       We are also storing much less information fields like quality,
 *       score, coverage, etc. have been removed.
 *
 * TODO: Refactor some functionality into different classes (VariantWithScore and VariantCancer)
 *
 * @author pcingola
 */
public class Variant extends Marker {

	public enum VariantType {
		SNP// Single nucleotide polymorphism (i.e. 1 base is changed)
		, MNP // Multiple nucleotide polymorphism (i.e. several bases are changed)
		, INS // Insertion (i.e. some bases added)
		, DEL // Deletion (some bases removed)
		, MIXED // A mixture of insertion, deletions, SNPs and or MNPs (a.k.a. subtitution)
		, Interval
		// Just analyze interval hits. Not a variant (e.g. BED input format)
	}

	private static final long serialVersionUID = -2928105165111400441L;;

	VariantType variantType;
	String ref; // Reference (i.e. original bases in the genome)
	String alt; // Changed bases
	String[] alts; // Available change options (when multiple ALT)
	boolean imprecise = false; // Imprecise variant: coordinates are not exact (E.g. see section "Encoding Structural Variants in VCF" from VCF spec. 4.1)
	String genotype; // Genotype order number (in case there are multiple changes per entry (e.g. A VCF entry may encode multiple ALTs). Note: Genotype differences are coded as "2-1" meaning genotype 1 is used as reference and genotype 2 is used as ALT (e.g. somatic vs germline samples)

	/**
	 * This constructor is used when we only have interval data (e.g. when reading a BED file)
	 */
	public Variant(Marker parent, int start, int end, String id) {
		super(parent, start, end, false, id);
		ref = alt = "";
		variantType = VariantType.Interval;
		alts = new String[1];
		alts[0] = "";
	}

	public Variant(Marker parent, int position, String referenceStr, String altStr) {
		this(parent, position, referenceStr, altStr, "");
	}

	/**
	 * Create a variant
	 */
	public Variant(Marker parent, int position, String referenceStr, String altStr, String id) {
		super(parent, position, position, false, id);
		init(parent, position, referenceStr, altStr, id);
	}

	/**
	 * Return the change (always in positive strand)
	 * @return
	 */
	public String change() {
		return isStrandPlus() ? alt : GprSeq.reverseWc(alt);
	}

	public String getChange() {
		return alt;
	}

	public String getChangeOption(int i) {
		return alts[i];
	}

	public int getChangeOptionCount() {
		return alts.length;
	}

	public VariantType getChangeType() {
		return variantType;
	}

	public String getGenotype() {
		return genotype;
	}

	public String getReference() {
		return ref;
	}

	/**
	 * Create a new SeqChange for this option
	 * @param i
	 * @return
	 */
	public Variant getSeqAltOption(int i) {
		// Just an interval (i.e. no changes)? => return the 'this' object
		if (variantType == VariantType.Interval) return this;

		// Not a real change? return null
		// This might happen if ref=alt
		// E.g.: "A -> T,A"
		if (ref.equalsIgnoreCase(alts[i])) return null;

		// Create new change
		return new Variant((Marker) parent, start, ref, alts[i], id);
	}

	@Override
	public int hashCode() {
		int hashCode = getChromosomeName().hashCode();
		hashCode = hashCode * 31 + start;
		hashCode = hashCode * 31 + end;
		hashCode = hashCode * 31 + (strandMinus ? -1 : 1);
		hashCode = hashCode * 31 + id.hashCode();
		hashCode = hashCode * 31 + ref.hashCode();
		hashCode = hashCode * 31 + alt.hashCode();
		return hashCode;
	}

	void init(Marker parent, int position, String referenceStr, String altStr, String id) {
		ref = referenceStr.toUpperCase();
		alt = altStr.toUpperCase();

		// Change type
		variantType = VariantType.MNP;
		if (alt.length() == 1) { // Is it a SNP?
			variantType = VariantType.SNP;
			if (alt.equals("A") || alt.equals("C") || alt.equals("G") || alt.equals("T")) {
				alts = new String[1];
				alts[0] = alt;
			} else {

				// Reference http://sourceforge.net/apps/mediawiki/samtools/index.php?title=SAM_FAQ#I_do_not_understand_the_columns_in_the_pileup_output.
				// IUB codes: M=A/C, R=A/G, W=A/T, S=C/G, Y=C/T, K=G/T and N=A/C/G/T
				if (alt.length() == 1) {
					if (alt.equals("N")) { // aNy base
						alts = new String[4];
						alts[0] = "A";
						alts[1] = "C";
						alts[2] = "G";
						alts[3] = "T";
					} else if (alt.equals("B")) { // B: not A
						alts = new String[3];
						alts[0] = "C";
						alts[1] = "G";
						alts[2] = "T";
					} else if (alt.equals("D")) { // D: not C
						alts = new String[3];
						alts[0] = "A";
						alts[1] = "G";
						alts[2] = "T";
					} else if (alt.equals("H")) { // H: not G
						alts = new String[3];
						alts[0] = "A";
						alts[1] = "C";
						alts[2] = "T";
					} else if (alt.equals("V")) { // V: not T
						alts = new String[3];
						alts[0] = "A";
						alts[1] = "C";
						alts[2] = "G";
					} else if (alt.equals("M")) {
						alts = new String[2];
						alts[0] = "A";
						alts[1] = "C";
					} else if (alt.equals("R")) {
						alts = new String[2];
						alts[0] = "A";
						alts[1] = "G";
					} else if (alt.equals("W")) { // Weak
						alts = new String[2];
						alts[0] = "A";
						alts[1] = "T";
					} else if (alt.equals("S")) { // Strong
						alts = new String[2];
						alts[0] = "C";
						alts[1] = "G";
					} else if (alt.equals("Y")) {
						alts = new String[2];
						alts[0] = "C";
						alts[1] = "T";
					} else if (alt.equals("K")) {
						alts = new String[2];
						alts[0] = "G";
						alts[1] = "T";
					} else {
						throw new RuntimeException("WARNING: Unkown IUB code for SNP '" + alt + "'");
					}
				}
			}
		} else {
			// Split, if multiple ALTS
			if (alt.indexOf(',') >= 0) alts = alt.split(",");
			else alts = alt.split("/");

			if (alt.startsWith("+")) {
				// Insertions
				variantType = VariantType.INS;
			} else if (alt.startsWith("-")) {
				// Deletions
				variantType = VariantType.DEL;
				if (alts[0].length() > 1) end = position + alts[0].length() - 2; // Update 'end' position
			} else if (alt.startsWith("=")) {
				// Mixed variant (substitution)
				variantType = VariantType.MIXED;
				if (alts[0].length() > 1) end = position + alts[0].length() - 2; // Update 'end' position
			}

			// Insertions and deletions always have '*' as reference
			if ((variantType == VariantType.INS) || (variantType == VariantType.DEL)) ref = "*";
		}

		type = EffectType.NONE;

		// Start and end position
		// 	- Start is always the leftmost base
		//	- End is always the rightmost affected base in the reference genome
		start = position;
		if (isIns() || isSnp()) {
			// These changes only affect one position in the reference genome
			end = start;
		} else {
			for (int i = 0; i < alts.length; i++) {
				String ch = alts[i];
				if (ch.startsWith("+") || ch.startsWith("-") || ch.startsWith("=")) ch = ch.substring(1);
				end = Math.max(end, start + ch.length() - 1);
			}
		}
	}

	public boolean isDel() {
		return (variantType == VariantType.DEL);
	}

	public boolean isImprecise() {
		return imprecise;
	}

	public boolean isInDel() {
		return (variantType == VariantType.INS) || (variantType == VariantType.DEL);
	}

	public boolean isIns() {
		return (variantType == VariantType.INS);
	}

	public boolean isInterval() {
		return (variantType == VariantType.Interval);
	}

	public boolean isMnp() {
		return variantType == VariantType.MNP;
	}

	@Override
	protected boolean isShowWarningIfParentDoesNotInclude() {
		return false;
	}

	public boolean isSnp() {
		return variantType == VariantType.SNP;
	}

	/**
	 * Is this a change or are the changes actually the same as the reference
	 * @return
	 */
	public boolean isVariant() {
		for (String chg : alts)
			if (!ref.equals(chg)) return true; // Any change option is different? => true
		return false;
	}

	public boolean isVariantMultiple() {
		return alts.length > 1;
	}

	/**
	 * Calculate the number of bases of change in length
	 * @return
	 */
	public int lengthChange() {
		if (isVariantMultiple()) throw new RuntimeException("Cannot ask for lengthChange on multiple changes!\n\tSeqChange : " + this);

		if (isVariantMultiple()) throw new RuntimeException("Cannot ask for lengthChange onSeqChange modifies transcript length: Unimplemented!"); // We'll focus on SNPs & MNPs now, we'll do other changes later
		if (isSnp() || isMnp()) return 0;

		// This is a length changing SeqChange (i.e. Insertions, deletion, or mixed change)
		// Calculate the number of bases of change in length
		return alts[0].length() - ref.length();
	}

	/**
	 * Return the change (always compared to 'referenceStrand') without any '+' or '-' leading characters
	 * @return
	 */
	public String netChange(boolean reverseStrand) {
		String netChange = alt;
		if (alt.startsWith("+") || alt.startsWith("-")) netChange = alt.substring(1); // Remove leading char

		// Need reverse-WC?
		return reverseStrand ? GprSeq.reverseWc(netChange) : netChange;
	}

	/**
	 * Only the part of the change that overlaps with a marker
	 * Return the change (always in positive strand) without any '+' or '-' leading characters
	 * @return
	 */
	public String netChange(Marker marker) {
		String netChange = alt;
		if (alt.startsWith("+") || alt.startsWith("-")) netChange = alt.substring(1); // Remove leading char

		int removeBefore = marker.getStart() - start;
		if (removeBefore > 0) {
			if (removeBefore >= netChange.length()) return ""; // Nothing left
		} else removeBefore = 0;

		int removeAfter = end - marker.getEnd();
		if (removeAfter > 0) {
			if ((removeBefore + removeAfter) >= netChange.length()) return ""; // Nothing left
		} else removeAfter = 0;

		// Use reverse-WC?
		if (isStrandMinus()) netChange = GprSeq.reverseWc(netChange);

		// Remove leading and trailing parts
		netChange = netChange.substring(removeBefore, netChange.length() - removeAfter);

		return netChange;
	}

	/**
	 * Return the reference (always in positive strand)
	 * @return
	 */
	public String reference() {
		return isStrandPlus() ? ref : GprSeq.reverseWc(ref);
	}

	public void setChangeType(VariantType changeType) {
		variantType = changeType;
	}

	public void setGenotype(String genotype) {
		this.genotype = genotype;
	}

	public void setImprecise(boolean imprecise) {
		this.imprecise = imprecise;
	}

	@Override
	public String toString() {
		if (variantType == VariantType.Interval) return "chr" + getChromosomeName() + ":" + start + "-" + end;

		return "chr" + getChromosomeName() //
				+ ":" + start //
				+ "_" + getReference() //
				+ "/" + getChange() //
				+ ((id != null) && (id.length() > 0) ? " '" + id + "'" : "");
	}

	/**
	 * Show it required by ENSEMBL's SnpEffectPredictor
	 * @return
	 */
	public String toStringEnsembl() {
		return getChromosomeName() + "\t" + start + "\t" + end + "\t" + ref + "/" + alt + "\t+";
	}

}
