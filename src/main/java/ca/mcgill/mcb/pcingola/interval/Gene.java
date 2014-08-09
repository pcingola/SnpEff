package ca.mcgill.mcb.pcingola.interval;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;

import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.stats.ObservedOverExpectedCpG;

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

		if (isProteinCoding()) {
			// Find canonical transcript (longest CDS)
			for (Transcript t : this)
				if (t.isProteinCoding() && ((canonical == null) || (canonical.cds().length() < t.cds().length()))) canonical = t;
		} else {
			// Find canonical transcript (longest cDNA)
			for (Transcript t : this)
				if ((canonical == null) || (canonical.cds().length() < t.cds().length())) canonical = t;
		}

		return canonical;
	}

	/**
	 * Calculate CpG bias: number of CpG / expected[CpG]
	 * @return
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
	 * @return
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
	 * Remove unverified transcripts
	 */
	public void removeUnverified() {
		// Mark unchecked transcripts for deletion
		ArrayList<Transcript> toDelete = new ArrayList<Transcript>();
		for (Transcript t : this)
			if (!t.isChecked()) toDelete.add(t);

		// Remove
		for (Transcript t : toDelete)
			remove(t);
	}

	/**
	 * Get some details about the effect on this gene
	 * @param seqChange
	 * @return
	 */
	@Override
	public boolean variantEffect(Variant seqChange, VariantEffects changeEffects, Variant seqChangerRef) {
		if (!intersects(seqChange)) return false; // Sanity check

		boolean hitTranscript = false;
		for (Transcript tr : this) {
			// Apply sequence change to create new 'reference'?
			if (seqChangerRef != null) tr = tr.apply(seqChangerRef);

			// Calculate effects
			hitTranscript |= tr.variantEffect(seqChange, changeEffects);
		}

		// May be none of the transcripts are actually hit
		if (!hitTranscript) {
			changeEffects.add(this, EffectType.INTRAGENIC, "");
			return true;
		}

		return true;
	}

	/**
	 * Parse a line from a serialized file
	 * @param line
	 * @return
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		geneName = markerSerializer.getNextField();
		bioType = markerSerializer.getNextField();
	}

	/**
	 * Create a string to serialize to a file
	 * @return
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
	 * @param type
	 * @return
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

}
