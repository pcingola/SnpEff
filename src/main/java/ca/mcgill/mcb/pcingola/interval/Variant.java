package ca.mcgill.mcb.pcingola.interval;

import java.util.LinkedList;

import ca.mcgill.mcb.pcingola.align.VariantRealign;
import ca.mcgill.mcb.pcingola.binseq.GenomicSequences;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.util.GprSeq;
import ca.mcgill.mcb.pcingola.util.IubString;

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
 * @author pcingola
 */
public class Variant extends Marker {

	public enum VariantType {
		SNP// Single nucleotide polymorphism (i.e. 1 base is changed)
		, MNP // Multiple nucleotide polymorphism (i.e. several bases are changed)
		, INS // Insertion (i.e. some bases added)
		, DEL // Deletion (some bases removed)
		, MIXED // A mixture of insertion, deletions, SNPs and or MNPs (a.k.a. substitution)
		, INTERVAL
		// Just analyze interval hits. Not a variant (e.g. BED input format)
	}

	// Not a variant (ref=alt)
	public static final Variant NO_VARIANT = new Variant(null, 0, 0, "");

	private static final long serialVersionUID = -2928105165111400441L;

	VariantType variantType; // Variant type
	String ref; // Reference (i.e. original bases in the genome)
	String alt; // Changed bases
	String genotype; // Genotype 'ALT' (e.g. A VCF entry may encode multiple ALTs).
	boolean imprecise = false; // Imprecise variant: coordinates are not exact (E.g. see section "Encoding Structural Variants in VCF" from VCF spec. 4.1)

	/**
	 * Create variants from ALT (which can be multiple values)
	 */
	public static LinkedList<Variant> factory(Chromosome chromo, int start, String ref, String altStr, String id, boolean expand) {
		LinkedList<Variant> list = new LinkedList<Variant>();

		// No alt? It's an interval
		if (altStr == null) {
			Variant var = new Variant(chromo, start, ref, null, id);
			list.add(var);
			return list;
		}

		// Split alts
		String alts[];
		if (altStr.indexOf(',') >= 0) alts = altStr.split(",");
		else alts = altStr.split("/");

		// Special case, two ALTs are the same
		if (alts.length == 2 && alts[0].equals(alts[1])) {
			Variant var = new Variant(chromo, start, ref, alts[0], id);
			list.add(var);
			return list;
		}

		// Add each alt
		for (String alt : alts) {
			// Note: We use 'hasIUBMax()' instead of 'hasIUB()' because large InDels may
			// have tons of 'N' bases. In such cases, it is impractical (and useless) to
			// produce all possible combinations
			boolean refIub = expand && IubString.hasIUBMax(ref);
			boolean altIub = expand && IubString.hasIUBMax(alt);

			// Expand all possible REF / ALT combinations
			if (!refIub && !altIub) {
				// Non-IUB expansion needed
				Variant var = new Variant(chromo, start, ref, alt, id);
				list.add(var);
			} else if (altIub && !refIub) {
				// ALT has IUB characters
				IubString iubsAlt = new IubString(alt);
				for (String seqAlt : iubsAlt) {
					Variant var = new Variant(chromo, start, ref, seqAlt, id);
					list.add(var);
				}
			} else if (!altIub && refIub) {
				// REF has IUB characters
				IubString iubsRef = new IubString(alt);
				for (String seqRef : iubsRef) {
					Variant var = new Variant(chromo, start, seqRef, alt, id);
					list.add(var);
				}
			} else if (altIub && refIub) {
				// Both REF and ALT have IUB characters
				IubString iubsRef = new IubString(alt);
				for (String seqRef : iubsRef) {
					IubString iubsAlt = new IubString(alt);
					for (String seqAlt : iubsAlt) {
						Variant var = new Variant(chromo, start, seqRef, seqAlt, id);
						list.add(var);
					}
				}
			}
		}

		return list;
	}

	/**
	 * This constructor is used when we only have interval data (e.g. when reading a BED file)
	 */
	public Variant(Marker parent, int start, int end, String id) {
		super(parent, start, end, false, id);
		ref = alt = "";
		variantType = VariantType.INTERVAL;
	}

	public Variant(Marker parent, int position, String referenceStr, String altStr) {
		this(parent, position, referenceStr, altStr, "");
	}

	public Variant(Marker parent, int position, String referenceStr, String altStr, String id) {
		super(parent, position, position, false, id);
		init(parent, position, referenceStr, altStr, null, id);
	}

	/**
	 * Return the change (always in positive strand)
	 */
	public String change() {
		return isStrandPlus() ? alt : GprSeq.reverseWc(alt);
	}

	@Override
	public Variant clone() {
		return (Variant) super.clone();
	}

	public String getAlt() {
		return alt;
	}

	public String getGenotype() {
		return genotype;
	}

	public String getReference() {
		return ref;
	}

	public VariantType getVariantType() {
		return variantType;
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

	void init(Marker parent, int position, String referenceStr, String altStr, VariantType variantType, String id) {
		if (altStr == null) altStr = referenceStr; // Not a variant (this is an interval). Set ref = alt
		ref = referenceStr.toUpperCase();
		alt = altStr.toUpperCase();

		// Sanity check
		if (altStr.indexOf(',') >= 0 || altStr.indexOf('/') >= 0) throw new RuntimeException("Variants with multiple ALTs are not allowed (ALT: '" + altStr + "')");

		// Remove leading char (we still have some test cases using old TXT format)
		if (ref.equals("*")) ref = "";

		if (alt.startsWith("+")) {
			// Insertion
			alt = ref + alt.substring(1);
		} else if (alt.startsWith("-")) {
			// Deletion
			ref = alt.substring(1);
			alt = "";
		} else if (alt.startsWith("=")) {
			// Mixed variant
			alt = altStr.substring(1);
		}

		//---
		// Calculate variant type
		//---
		if (variantType == null) {
			if (ref.equals(alt)) this.variantType = VariantType.INTERVAL;
			else if (ref.length() == 1 && alt.length() == 1) this.variantType = VariantType.SNP;
			else if (ref.length() == alt.length()) this.variantType = VariantType.MNP;
			else if (ref.length() < alt.length() && alt.startsWith(ref)) this.variantType = VariantType.INS;
			else if (ref.length() > alt.length() && ref.startsWith(alt)) this.variantType = VariantType.DEL;
			else this.variantType = VariantType.MIXED;
		} else this.variantType = variantType;

		//---
		// Start and end position
		// 	- Start is always the leftmost base
		//	- End is always the rightmost affected base in the reference genome
		//---
		start = position;
		if (isIns() || isSnp()) {
			// These changes only affect one position in the reference genome
			end = start;
		} else { // if (isDel() || isMnp()) {
			// Update 'end' position
			if (ref.length() > 1) end = start + ref.length() - 1;
		}

		// Effect type
		type = EffectType.NONE;
		this.id = id;
	}

	public boolean isDel() {
		return (variantType == VariantType.DEL);
	}

	public boolean isElongation() {
		return lengthChange() > 0;
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
		return (variantType == VariantType.INTERVAL);
	}

	public boolean isMixed() {
		return (variantType == VariantType.MIXED);
	}

	public boolean isMnp() {
		return variantType == VariantType.MNP;
	}

	public boolean isNonRef() {
		return false;
	}

	@Override
	protected boolean isShowWarningIfParentDoesNotInclude() {
		return false;
	}

	public boolean isSnp() {
		return variantType == VariantType.SNP;
	}

	public boolean isTruncation() {
		return lengthChange() < 0;
	}

	/**
	 * Is this a change or is ALT actually the same as the reference
	 */
	public boolean isVariant() {
		return !ref.equals(alt);
	}

	/**
	 * Calculate the number of bases of change in length
	 */
	public int lengthChange() {
		if (isSnp() || isMnp()) return 0;

		// This is a length changing SeqChange (i.e. Insertions, deletion, or mixed change)
		// Calculate the number of bases of change in length
		return alt.length() - ref.length();
	}

	/**
	 * Return the change (always compared to 'referenceStrand') without any '+' or '-' leading characters
	 */
	public String netChange(boolean reverseStrand) {
		if (isDel()) return reverseStrand ? GprSeq.reverseWc(ref) : ref; // Deleteion have empty 'alt'
		return reverseStrand ? GprSeq.reverseWc(alt) : alt; // Need reverse-WC?
	}

	/**
	 * Only the part of the change that overlaps with a marker
	 * Return the change (always in positive strand) without any '+' or '-' leading characters
	 */
	public String netChange(Marker marker) {
		String netChange = alt;
		if (isDel()) netChange = ref; // In deletions 'alt' is empty

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
	 * Create a new variant realigning it towards the leftmost position
	 */
	public Variant realignLeft() {
		GenomicSequences gs = getGenome().getGenomicSequences();
		if (gs == null) return this;

		VariantRealign vr = new VariantRealign(gs, this);
		if (!vr.realign()) return this;
		return vr.getVariantRealigned();
	}

	/**
	 * Return the reference (always in positive strand)
	 */
	public String reference() {
		return isStrandPlus() ? ref : GprSeq.reverseWc(ref);
	}

	public void setGenotype(String genotype) {
		this.genotype = genotype;
	}

	public void setImprecise(boolean imprecise) {
		this.imprecise = imprecise;
	}

	public void setVariantType(VariantType variantType) {
		this.variantType = variantType;
	}

	@Override
	public String toString() {
		if (variantType == VariantType.INTERVAL) return "chr" + getChromosomeName() + ":" + start + "-" + end;

		return "chr" + getChromosomeName() //
				+ ":" + start //
				+ "_" + getReference() //
				+ "/" + getAlt() //
				+ ((id != null) && (id.length() > 0) ? " '" + id + "'" : "");
	}

	/**
	 * Show variant in ENSEMBL's VEP format
	 */
	public String toStringEnsembl() {
		return getChromosomeName() + "\t" + start + "\t" + end + "\t" + ref + "/" + alt + "\t+";
	}

	/**
	 * Old format, used for some test cases
	 */
	public String toStringOld() {
		if (isIns()) return getChromosomeName() + ":" + getStart() + "_*" + "/+" + getAlt();
		else if (isDel()) return getChromosomeName() + ":" + getStart() + "_*" + "/-" + getReference();
		return getChromosomeName() + ":" + getStart() + "_" + getReference() + "/" + getAlt();
	}
}
