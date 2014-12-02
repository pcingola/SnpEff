package ca.mcgill.mcb.pcingola.interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.stats.ObservedOverExpectedCpG;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Interval for a gene, as well as transcripts
 *
 * @author pcingola
 *
 */
public class Gene extends IntervalAndSubIntervals<Transcript> implements Serializable {

	public enum GeneType {
		CODING, NON_CODING, UNKNOWN
	}

	private static final long serialVersionUID = 8419206759034068147L;

	String geneName;
	String bioType;

	public Gene() {
		super();
		geneName = "";
		bioType = "";
		type = EffectType.GENE;
	}

	public Gene(Marker parent, int start, int end, boolean strandMinus, String id, String geneName, String bioType) {
		super(parent, start, end, strandMinus, id);
		this.geneName = geneName;
		this.bioType = bioType;
		type = EffectType.GENE;
	}

	/**
	 * Adjust start, end and strand values
	 * @return true if any adjustment was done
	 */
	public boolean adjust() {
		boolean changed = false;
		int strandSumGene = 0;
		int newStart = start, newEnd = start;

		if (newStart == 0 && newEnd == 0) {
			newStart = Integer.MAX_VALUE;
			newEnd = Integer.MIN_VALUE;
		}

		for (Transcript tr : this) {
			newStart = Math.min(newStart, tr.getStart());
			newEnd = Math.max(newEnd, tr.getEnd());

			for (Exon exon : tr.sortedStrand()) {
				newStart = Math.min(newStart, exon.getStart());
				newEnd = Math.max(newEnd, exon.getEnd());
				strandSumGene += exon.isStrandMinus() ? -1 : 1; // Some exons have incorrect strands, we use the strand indicated by most exons
			}

			for (Utr utr : tr.getUtrs()) {
				newStart = Math.min(newStart, utr.getStart());
				newEnd = Math.max(newEnd, utr.getEnd());
			}
		}

		// Change gene strand?
		boolean newStrandMinus = strandSumGene < 0;
		if (strandMinus != newStrandMinus) {
			strandMinus = newStrandMinus;
			changed = true;
		}

		// Change start?
		if (start != newStart) {
			start = newStart;
			changed = true;
		}

		// Change end?
		if (end != newEnd) {
			end = newEnd;
			changed = true;
		}

		return changed;
	}

	/**
	 * Get canonical transcript
	 * Canonical transcripts are defined as the longest CDS of amongst the protein coding transcripts.
	 * If none of the transcripts is protein coding, then it is the longest cDNA.
	 */
	public Transcript canonical() {
		Transcript canonical = null;
		int canonicalLen = 0;

		if (isProteinCoding()) {
			// Find canonical transcript in protein coding gene (longest CDS)
			for (Transcript t : this) {
				int tlen = t.cds().length();

				// Compare coding length. If both lengths are equal, compare IDs
				if (t.isProteinCoding() //
						&& ((canonical == null) // No canonical selected so far? => Select this one
								|| (canonicalLen < tlen) // Longer? => Update
						|| ((canonicalLen == tlen) && (t.getId().compareTo(canonical.getId()) < 0)) // Same length? Compare IDs
						) //
				) {
					canonical = t;
					canonicalLen = tlen;
				}
			}
		} else {
			// Find canonical transcript in non-protein coding gene (longest mRNA)
			for (Transcript t : this) {
				int tlen = t.mRna().length();

				if (canonicalLen <= tlen //
						&& ((canonical == null) // No canonical selected so far? => Select this one
								|| (canonicalLen < tlen) // Longer? => Update
						|| ((canonicalLen == tlen) && (t.getId().compareTo(canonical.getId()) < 0)) // Same length? Compare IDs
						) //
				) {
					canonical = t;
					canonicalLen = tlen;
				}
			}
		}

		// Found canonincal transcript? Set canonical flag
		if (canonical != null) canonical.setCanonical(true);

		return canonical;
	}

	/**
	 * Calculate CpG bias: number of CpG / expected[CpG]
	 */
	public double cpgExonBias() {
		ObservedOverExpectedCpG oe = new ObservedOverExpectedCpG();
		return oe.oe(this);
	}

	public GeneType geneType() {
		if (bioType.length() > 0) {
			// Is it 'protein_coding'or a 'mRNA'?
			if (bioType.equalsIgnoreCase("protein_coding") || bioType.equalsIgnoreCase("mRNA")) return GeneType.CODING;
			return GeneType.NON_CODING;
		}

		return GeneType.UNKNOWN;
	}

	public String getBioType() {
		return bioType;
	}

	public String getGeneName() {
		return geneName;
	}

	/**
	 * Is any of the transcripts protein coding?
	 */
	public boolean isProteinCoding() {
		for (Transcript tr : this)
			if (tr.isProteinCoding()) return true;
		return false;
	}

	@Override
	protected boolean isShowWarningIfParentDoesNotInclude() {
		return false;
	}

	/**
	 * Remove all transcripts in trIds
	 * @return : Number of transcripts removed
	 */
	public int keepTranscripts(Set<String> trIds) {
		// Find transcripts in trIds
		ArrayList<Transcript> toDelete = new ArrayList<Transcript>();
		for (Transcript t : this)
			if (!trIds.contains(t.getId())) toDelete.add(t);

		// Remove them
		for (Transcript t : toDelete)
			remove(t);

		return toDelete.size();
	}

	/**
	 * Remove only protein coding transcripts
	 * @return : Number of transcripts removed
	 */
	public int keepTranscriptsProtein() {
		// Find transcripts in trIds
		ArrayList<Transcript> toDelete = new ArrayList<Transcript>();
		for (Transcript t : this)
			if (!t.isProteinCoding()) toDelete.add(t);

		// Remove them
		for (Transcript t : toDelete)
			remove(t);

		return toDelete.size();
	}

	@Override
	public Markers markers() {
		Markers markers = new Markers();
		for (Transcript tr : this) {
			markers.add(tr);
			markers.add(tr.markers());
		}
		return markers;
	}

	/**
	 * Remove all non-canonical transcripts
	 */
	public void removeNonCanonical() {
		Transcript canonical = canonical();

		// Found canonical? => Remove all others
		if (canonical != null) {
			// Remove all other transcripts
			ArrayList<Transcript> toDelete = new ArrayList<Transcript>();
			toDelete.addAll(subIntervals.values());
			toDelete.remove(canonical); // Do not remove canonical transcript.

			// Remove all other transcripts
			for (Transcript t : toDelete)
				remove(t);
		}
	}

	/**
	 * Remove unverified or corrected transcripts
	 * @return : True if ALL transcripts have been removed
	 */
	public boolean removeUnverified() {
		// Mark unchecked transcripts for deletion
		ArrayList<Transcript> toDelete = new ArrayList<Transcript>();

		int countRemoved = 0;
		for (Transcript t : this)
			if (!t.isChecked() || t.isCorrected()) {
				toDelete.add(t);
				countRemoved++;
			}

		if (Config.get().isDebug()) Gpr.debug("Gene '', removing " + countRemoved + " / " + numChilds() + " unchecked transcript.");

		// Remove
		for (Transcript t : toDelete)
			remove(t);

		return numChilds() <= 0;
	}

	/**
	 * Parse a line from a serialized file
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		geneName = markerSerializer.getNextField();
		bioType = markerSerializer.getNextField();
	}

	/**
	 * Create a string to serialize to a file
	 */
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + geneName //
				+ "\t" + bioType;
	}

	public void setBioType(String bioType) {
		this.bioType = bioType;
	}

	/**
	 * Size of a genetic region for a given gene
	 */
	public int sizeof(String type) {
		// Calculate size
		EffectType eff = EffectType.valueOf(type.toUpperCase());
		Markers all = new Markers();
		int len = 0;

		switch (eff) {
		case GENE:
			return size();

		case EXON:
			// Add all exons
			for (Transcript tr : this)
				for (Exon ex : tr)
					all.add(ex);
			break;

		case CDS:
			// Add all cds
			for (Transcript tr : this)
				for (Cds cds : tr.getCds())
					all.add(cds);
			break;

		case TRANSCRIPT:
			// Add all transcripts
			for (Transcript tr : this)
				all.add(tr);
			break;

		case INTRON:
			return Math.max(0, sizeof("TRANSCRIPT") - sizeof("EXON"));

		case UTR_3_PRIME:
			// Add all Utr3prime
			for (Transcript tr : this)
				for (Utr3prime utr : tr.get3primeUtrs())
					all.add(utr);
			break;

		case UTR_5_PRIME:
			// Add all Utr3prime
			for (Transcript tr : this)
				for (Utr5prime utr : tr.get5primeUtrs())
					all.add(utr);
			break;

		case UPSTREAM:
			for (Transcript tr : this)
				all.add(tr.getUpstream());

			break;

		case DOWNSTREAM:
			for (Transcript tr : this)
				all.add(tr.getDownstream());
			break;

		case SPLICE_SITE_ACCEPTOR:
			// Add all exons
			for (Transcript tr : this)
				for (Exon ex : tr)
					if (ex.getSpliceSiteAcceptor() != null) all.add(ex.getSpliceSiteAcceptor());
			break;

		case SPLICE_SITE_BRANCH:
			// Add all exons
			for (Transcript tr : this)
				for (SpliceSiteBranch ssb : tr.getSpliceBranchSites())
					all.add(ssb);
			break;

		case SPLICE_SITE_DONOR:
			// Add all exons
			for (Transcript tr : this)
				for (Exon ex : tr)
					if (ex.getSpliceSiteDonor() != null) all.add(ex.getSpliceSiteDonor());
			break;

		case SPLICE_SITE_REGION:
			// Add all exons
			for (Transcript tr : this) {
				for (Exon ex : tr) {
					if (ex.getSpliceSiteRegionStart() != null) all.add(ex.getSpliceSiteRegionStart());
					if (ex.getSpliceSiteRegionEnd() != null) all.add(ex.getSpliceSiteRegionEnd());
				}
				for (Intron intron : tr.introns()) {
					if (intron.getSpliceSiteRegionStart() != null) all.add(intron.getSpliceSiteRegionStart());
					if (intron.getSpliceSiteRegionEnd() != null) all.add(intron.getSpliceSiteRegionEnd());
				}
			}

		case INTRAGENIC:
			// We have to perform a set minus operation between this gene and all the transcripts
			Markers gene = new Markers();
			gene.add(this);

			// Create transcripts
			Markers trans = new Markers();
			for (Transcript tr : this)
				trans.add(tr);

			all = gene.minus(trans);
			break;

		case NONE:
			return 0;

		default:
			throw new RuntimeException("Unimplemented sizeof('" + type + "')");
		}

		// Merge and calculate total length
		Markers merged = all.merge();
		for (Marker m : merged)
			len += m.size();

		return len;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getChromosomeName() + ":" + start + "-" + end);
		sb.append(", strand:" + (strandMinus ? "-1" : "1"));
		if ((id != null) && (id.length() > 0)) sb.append(", id:" + id);
		if ((geneName != null) && (geneName.length() > 0)) sb.append(", name:" + geneName);
		if ((bioType != null) && (bioType.length() > 0)) sb.append(", bioType:" + bioType);

		sb.append("\n");

		if (numChilds() > 0) {
			sb.append("Transcipts:\n");
			for (Transcript tint : sorted())
				sb.append("\t" + tint + "\n");
		}

		return sb.toString();
	}

	/**
	 * Get some details about the effect on this gene
	 */
	@Override
	public boolean variantEffect(Variant variant, Variant variantRef, VariantEffects variantEffects) {
		if (!intersects(variant)) return false; // Sanity check

		// Keep track of the original variants, just in case it is changed
		Variant variantOri = variant;
		Variant variantRefOri = variantRef;

		// Do we need to 'walk and roll'? I.e. alignt the variant towards the most 3-prime
		// end of the transcript? Note that VCF request variants to be aligned towards
		// the 'leftmost' coordinate, so this re-alignment is only required for variants
		// within transcripts on the positive strand.
		boolean shifted3prime = false;
		if (variant.isInDel() && Config.get().isShiftHgvs() && isStrandPlus()) {
			Gpr.debug("SHIFT USING HGVS");
			// Get sequence information. Might have to load sequences from database
			variant = variant.shiftLeft();
			variantRef = variantRef.shiftLeft();
			shifted3prime = (variant != variantOri); // Created a new variant? => It was shifted towards the left (i.e. 3-prime)
		}

		// Find effect for each transcript
		boolean hitTranscript = false;
		for (Transcript tr : this) {
			// Apply sequence change to create new 'reference'?
			if (variantRef != null) tr = tr.apply(variantRef);

			// Calculate effects
			hitTranscript |= tr.variantEffect(variant, variantEffects);
		}

		// May be none of the transcripts are actually hit
		if (!hitTranscript) {
			variantEffects.addEffect(this, EffectType.INTRAGENIC, "");
			return true;
		}

		// Add INFO_SHIFT_3_PRIME warning message
		if (shifted3prime) {
			for (VariantEffect ve : variantEffects) {
				if (ve.getVariant() == variant) { // Is this effect using the shifted variant?
					ve.addErrorWarningInfo(ErrorWarningType.INFO_REALIGN_3_PRIME); // Mark as shifted
				}
			}
		}

		return true;
	}
}
