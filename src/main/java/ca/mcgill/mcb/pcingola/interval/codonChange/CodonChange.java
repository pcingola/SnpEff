package ca.mcgill.mcb.pcingola.interval.codonChange;

import java.util.List;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.interval.Variant;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;

/**
 * Analyze codon changes based on a variant and a Transcript
 *
 * @author pcingola
 */
public class CodonChange {

	public static boolean showCodonChange = true; // This is disabled in some specific test cases
	public static final int CODON_SIZE = 3; // I'll be extremely surprised if you ever need to change this parameter...

	boolean returnNow = false; // Can we return immediately after calculating the first 'codonChangeSingle()'?
	boolean requireNetCdsChange = false;
	Variant variant;
	Transcript transcript;
	Exon exon = null;
	VariantEffects changeEffects;
	int codonNum = -1;
	int codonIndex = -1;
	String codonsOld = ""; // Old codons (before change)
	String codonsNew = ""; // New codons (after change)
	String aaOld = ""; // Old amino acids (before change)
	String aaNew = ""; // New amino acids (after change)
	String netCdsChange = "";

	public CodonChange(Variant variant, Transcript transcript, VariantEffects changeEffects) {
		this.variant = variant;
		this.transcript = transcript;
		this.changeEffects = changeEffects;
	}

	/**
	 * Calculate all possible codon changes
	 */
	public void calculate() {
		// Split each variant into it's multiple options
		for (int i = 0; i < variant.getChangeOptionCount(); i++) {
			// Create a new variant for this option, calculate codonChange for this variantOption and add result to the list
			Variant variantNew = variant.getSeqAltOption(i);
			if (variantNew != null) {
				// Create a specific codon change and calculate changes
				CodonChange codonChange = factory(variantNew, transcript, changeEffects);
				codonChange.codonChange(); // Calculate codon change and add them to the list
			}
		}

		return;
	}

	/**
	 * Calculate base number in a cds where 'pos' is
	 */
	int cdsBaseNumber(int pos) {
		int cdsbn = transcript.baseNumberCds(pos, true);

		// Does not intersect the transcript?
		if (cdsbn < 0) {
			// 'pos' before transcript start
			if (pos <= transcript.getCdsStart()) {
				if (transcript.isStrandPlus()) return 0;
				return transcript.cds().length();
			}

			// 'pos' is after CDS end
			if (transcript.isStrandPlus()) return transcript.cds().length();
			return 0;
		}

		return cdsbn;
	}

	/**
	 * Calculate a list of codon changes
	 */
	void codonChange() {
		if (!transcript.intersects(variant)) return;

		// Get coding start (after 5 prime UTR)
		int cdsStart = transcript.getCdsStart();

		// We may have to calculate 'netCdsChange', which is the effect on the CDS
		netCdsChange = netCdsChange();

		//---
		// Concatenate all exons
		//---
		int firstCdsBaseInExon = 0; // Where the exon maps to the CDS (i.e. which CDS base number does the first base in this exon maps to).
		List<Exon> exons = transcript.sortedStrand();
		for (Exon exon : exons) {
			this.exon = exon;
			if (exon.intersects(variant)) {
				int cdsBaseInExon; // cdsBaseInExon: base number relative to the beginning of the coding part of this exon (i.e. excluding 5'UTRs)

				if (transcript.isStrandPlus()) {
					int firstvariantBaseInExon = Math.max(variant.getStart(), Math.max(exon.getStart(), cdsStart));
					cdsBaseInExon = firstvariantBaseInExon - Math.max(exon.getStart(), cdsStart);
				} else {
					int lastvariantBaseInExon = Math.min(variant.getEnd(), Math.min(exon.getEnd(), cdsStart));
					cdsBaseInExon = Math.min(exon.getEnd(), cdsStart) - lastvariantBaseInExon;
				}

				if (cdsBaseInExon < 0) cdsBaseInExon = 0;

				// Get codon number and index within codon (where seqChage is pointing)
				codonNum = (firstCdsBaseInExon + cdsBaseInExon) / CODON_SIZE;
				codonIndex = (firstCdsBaseInExon + cdsBaseInExon) % CODON_SIZE;

				// Use appropriate method to calculate codon change
				boolean hasChanged = false; // Was there any change?
				hasChanged = codonChangeSingle(exon);

				// Any change? => Add change to list
				if (hasChanged) changeEffects.setMarker(exon); // It is affecting this exon, so we set the marker

				// Can we return immediately?
				if (returnNow) return;
			}

			if (transcript.isStrandPlus()) firstCdsBaseInExon += Math.max(0, exon.getEnd() - Math.max(exon.getStart(), cdsStart) + 1);
			else firstCdsBaseInExon += Math.max(0, Math.min(cdsStart, exon.getEnd()) - exon.getStart() + 1);
		}

		return;
	}

	/**
	 * Calculate the effect of a single change type: SNP, MNP, INS, DEL
	 * @return
	 */
	boolean codonChangeSingle(Exon exon) {
		throw new RuntimeException("Unimplemented method for this thype of variant: " + variant.getType());
	}

	/**
	 * Calculate new codons
	 * @return
	 */
	String codonsNew() {
		throw new RuntimeException("Unimplemented method for this thype of CodonChange: " + this.getClass().getSimpleName());
	}

	/**
	 * Calculate old codons
	 * @return
	 */
	public String codonsOld() {
		return codonsOld(1);
	}

	/**
	 * Calculate old codons
	 * @return
	 */
	protected String codonsOld(int numCodons) {
		String cds = transcript.cds();
		String codon = "";

		int start = codonNum * CodonChange.CODON_SIZE;
		int end = start + numCodons * CodonChange.CODON_SIZE;

		int len = cds.length();
		if (start >= cds.length()) start = len;
		if (end >= cds.length()) end = len;

		// Capitalize
		codon = cds.substring(start, end);

		// Codon not multiple of three? Add missing bases as 'N'
		if (codon.length() % 3 == 1) codon += "NN";
		else if (codon.length() % 3 == 2) codon += "N";

		return codon;
	}

	/**
	 * Create a specific codon change for a variant
	 */
	CodonChange factory(Variant variant, Transcript transcript, VariantEffects changeEffects) {
		switch (variant.getVariantType()) {
		case SNP:
			return new CodonChangeSnp(variant, transcript, changeEffects);
		case INS:
			return new CodonChangeIns(variant, transcript, changeEffects);
		case DEL:
			return new CodonChangeDel(variant, transcript, changeEffects);
		case MNP:
			return new CodonChangeMnp(variant, transcript, changeEffects);
		case MIXED:
			return new CodonChangeInterval(variant, transcript, changeEffects);
		case INTERVAL:
			return new CodonChangeInterval(variant, transcript, changeEffects);
		default:
			throw new RuntimeException("Unimplemented factory for variant: " + variant);
		}
	}

	/**
	 * We may have to calculate 'netCdsChange', which is the effect on the CDS
	 * Note: A deletion or a MNP might affect several exons
	 */
	protected String netCdsChange() {
		if (!requireNetCdsChange) return "";

		if (variant.size() > 1) {
			StringBuilder sb = new StringBuilder();
			for (Exon exon : transcript.sortedStrand())
				sb.append(variant.netChange(exon));
			return sb.toString();
		}

		return variant.netChange(transcript.isStrandMinus());
	}

}
