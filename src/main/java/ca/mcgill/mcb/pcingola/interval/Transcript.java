package ca.mcgill.mcb.pcingola.interval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ca.mcgill.mcb.pcingola.interval.codonChange.CodonChange;
import ca.mcgill.mcb.pcingola.serializer.MarkerSerializer;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.EffectType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffect.ErrorWarningType;
import ca.mcgill.mcb.pcingola.snpEffect.VariantEffects;
import ca.mcgill.mcb.pcingola.stats.ObservedOverExpectedCpG;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.GprSeq;

/**
 * Codon position
 * @author pcingola
 */
class CodonPosition {

	public int codonNum = -1;
	public int codonIndex = -1;
}

/**
 * Interval for a transcript, as well as some other information: exons, utrs, cds, etc.
 *
 * @author pcingola
 */
public class Transcript extends IntervalAndSubIntervals<Exon> {

	private static final long serialVersionUID = -2665025617916107311L;

	boolean proteinCoding; // Is this a protein-coding transcript?
	int cdsStart, cdsEnd;
	String bioType = ""; // Transcript biotype
	String cds; // Coding sequence
	String protein; // Protein sequence
	ArrayList<SpliceSiteBranch> spliceBranchSites; // Branch splice sites
	ArrayList<Utr> utrs; // UTRs
	ArrayList<Cds> cdss; // CDS information
	ArrayList<Intron> introns; // Intron markers
	Upstream upstream; // Upstream interval
	Downstream downstream; // Downstream interval
	Exon firstCodingExon; // First coding exon. I.e. where transcription start site (TSS) is.
	int cds2pos[], aa2pos[];
	boolean aaCheck, dnaCheck;
	boolean ribosomalSlippage; // Ribosomal slippage causes changes in reading frames. This might be represented as negative length introns (overlapping exons).

	public Transcript() {
		super();
		spliceBranchSites = new ArrayList<SpliceSiteBranch>();
		utrs = new ArrayList<Utr>();
		cdss = new ArrayList<Cds>();
		type = EffectType.TRANSCRIPT;
	}

	public Transcript(Gene gene, int start, int end, boolean strandMinus, String id) {
		super(gene, start, end, strandMinus, id);
		type = EffectType.TRANSCRIPT;
	}

	/**
	 * Calculate chromosome position as function of Amino Acid number
	 *
	 * @returns An array mapping 'pos[aaNumber] = chromosmalPos'
	 */
	public synchronized int[] aaNumber2Pos() {
		if (aa2pos != null) return aa2pos;

		calcCdsStartEnd();

		aa2pos = new int[protein().length()];
		for (int i = 0; i < aa2pos.length; i++)
			aa2pos[i] = -1;

		int cdsMin = Math.min(cdsStart, cdsEnd);
		int cdsMax = Math.max(cdsStart, cdsEnd);

		// For each exon, add CDS position to array
		int aaBaseNum = 0;
		int step = isStrandPlus() ? 1 : -1;
		int codonBase = 0;
		for (Exon exon : sortedStrand()) {
			int min = isStrandPlus() ? exon.getStart() : exon.getEnd();
			for (int pos = min; exon.intersects(pos) && aaBaseNum < aa2pos.length; pos += step)
				// Is this within a CDS?
				if ((cdsMin <= pos) && (pos <= cdsMax)) {
					// First codon base? Add to map
					if (codonBase == 0) aa2pos[aaBaseNum++] = pos;
					codonBase = (codonBase + 1) % 3;
				}
		}

		return aa2pos;
	}

	/**
	 * Add a CDS
	 * @param cdsInt
	 */
	public void add(Cds cdsInt) {
		cdss.add(cdsInt);
		cds = null;
	}

	/**
	 * Add a SpliceSiteBranchU12
	 * @param branchU12
	 */
	public void add(SpliceSiteBranchU12 branchU12) {
		spliceBranchSites.add(branchU12);
	}

	/**
	 * Add a UTR
	 * @param utr
	 */
	public void add(Utr utr) {
		utrs.add(utr);
		cds = null;
	}

	/**
	 * Add missing UTRs. See utrFromCds() method.
	 * @param missingUtrs
	 */
	boolean addMissingUtrs(Markers missingUtrs, boolean verbose) {
		missingUtrs.sort(false, isStrandMinus());

		// Get min/max CDS positions
		int minCds = Integer.MAX_VALUE;
		int maxCds = 0;
		for (Cds c : cdss) {
			minCds = Math.min(minCds, c.getStart());
			maxCds = Math.max(maxCds, c.getEnd());
		}

		if (verbose) System.out.println("Transcript '" + id + "' has missing UTRs. Strand: " + strandMinus + " (minCds: " + minCds + " , maxCds: " + maxCds + "):");

		// Add intervals
		boolean retVal = false;
		for (Marker mu : missingUtrs) {
			Exon exon = queryExon(mu);
			if (exon == null) throw new RuntimeException("Cannot find exon for UTR: " + mu);
			Utr toAdd = null;

			if (isStrandPlus()) {
				if (mu.getEnd() <= minCds) toAdd = new Utr5prime(exon, mu.getStart(), mu.getEnd(), strandMinus, mu.getId());
				else if (mu.getStart() >= maxCds) toAdd = new Utr3prime(exon, mu.getStart(), mu.getEnd(), strandMinus, mu.getId());
			} else {
				if (mu.getStart() >= maxCds) toAdd = new Utr5prime(exon, mu.getStart(), mu.getEnd(), strandMinus, mu.getId());
				else if (mu.getEnd() <= minCds) toAdd = new Utr3prime(exon, mu.getStart(), mu.getEnd(), strandMinus, mu.getId());
			}

			// OK?
			if (toAdd != null) {
				add(toAdd);
				if (verbose) System.out.println("\tAdding " + toAdd);
				retVal = true;
			}
		}

		return retVal;
	}

	/**
	 * Adjust transcript coordinates
	 * @return
	 */
	public boolean adjust() {
		boolean changed = false;
		int strandSumTr = 0;
		int newStart = Integer.MAX_VALUE;
		int newEnd = Integer.MIN_VALUE;

		int countStrandPlus = 0, countStrandMinus = 0;
		for (Exon exon : sortedStrand()) {
			newStart = Math.min(newStart, exon.getStart());
			newEnd = Math.max(newEnd, exon.getEnd());

			// Common exon strand
			if (exon.isStrandPlus()) countStrandPlus++;
			else countStrandMinus++;
		}

		// UTRs
		for (Utr utr : getUtrs()) {
			newStart = Math.min(newStart, utr.getStart());
			newEnd = Math.max(newEnd, utr.getEnd());
		}

		// Sanity check
		strandSumTr = countStrandPlus - countStrandMinus; // Some exons have incorrect strands, we use the strand indicated by most exons
		boolean newStrandMinus = strandSumTr < 0;
		if ((countStrandPlus > 0) && (countStrandMinus > 0)) Gpr.debug("Transcript '" + id + "' has " + countStrandPlus + " exons on the plus and " + countStrandMinus + " exons on the minus strand! This should never happen!");

		// Change transcript strand?
		if (strandMinus != newStrandMinus) {
			changed = true;
			setStrandMinus(newStrandMinus); // Change strand
		}

		// Changed? Update values
		if (newStart < Integer.MAX_VALUE && newEnd > Integer.MIN_VALUE) {
			// Change start?
			if (start != newStart) {
				setStart(newStart);
				changed = true;
			}

			// Change end?
			if (end != newEnd) {
				setEnd(newEnd);
				changed = true;
			}
		}

		return changed;
	}

	/**
	 * Create a new transcript after applying changes in variant
	 *
	 * Note: If this transcript is unaffected, no new transcript is created (same transcript is returned)
	 *
	 * @param variant
	 * @return
	 */
	@Override
	public Transcript apply(Variant variant) {
		// SeqChange after this marker: No effect
		if (end < variant.getStart()) return this;

		// Create new transcript
		Transcript tr = (Transcript) super.apply(variant);

		// We will change information, so we need a clone
		if (tr == this) tr = (Transcript) clone();
		tr.reset(); // Reset all parameters (we only wanted the coordinate changes)

		// Add changed exons
		for (Exon ex : this) {
			Exon newExon = ex.apply(variant);
			if (newExon != null) tr.add(newExon);
		}

		// Add changed UTRs
		for (Utr utr : utrs) {
			Utr newUtr = (Utr) utr.apply(variant);
			if (newUtr != null) tr.utrs.add(newUtr);
		}

		// Up & Down stream
		if (upstream != null) tr.upstream = (Upstream) upstream.apply(variant);
		if (downstream != null) tr.downstream = (Downstream) downstream.apply(variant);

		// Splice branch
		for (SpliceSiteBranch sbr : spliceBranchSites) {
			SpliceSiteBranch newSbs = (SpliceSiteBranch) sbr.apply(variant);
			if (newSbs != null) tr.spliceBranchSites.add(newSbs);
		}

		return tr;
	}

	/**
	 * Calculate base number in a CDS where 'pos' maps
	 *
	 * @returns Base number or '-1' if it does not map to a coding base
	 */
	public synchronized int baseNumberCds(int pos, boolean usePrevBaseIntron) {
		// Doesn't hit this transcript?
		if (!intersects(pos)) return -1;

		// Is it in UTR instead of CDS?
		if (isUtr(pos)) return -1;

		// Calculate cdsStart and cdsEnd (if not already done)
		calcCdsStartEnd();

		// All exons..
		int firstCdsBaseInExon = 0; // Where the exon maps to the CDS (i.e. which CDS base number does the first base in this exon maps to).
		for (Exon eint : sortedStrand()) {
			if (eint.intersects(pos)) {
				int cdsBaseInExon; // cdsBaseInExon: base number relative to the beginning of the coding part of this exon (i.e. excluding 5'UTRs)
				if (isStrandPlus()) cdsBaseInExon = pos - Math.max(eint.getStart(), cdsStart);
				else cdsBaseInExon = Math.min(eint.getEnd(), cdsStart) - pos;

				cdsBaseInExon = Math.max(0, cdsBaseInExon);

				return firstCdsBaseInExon + cdsBaseInExon;
			} else {
				// Before exon begins?
				if ((isStrandPlus() && (pos < eint.getStart())) // Before exon begins (positive strand)?
						|| (isStrandMinus() && (pos > eint.getEnd()))) // Before exon begins (negative strand)?
					return firstCdsBaseInExon - (usePrevBaseIntron ? 1 : 0);
			}

			if (isStrandPlus()) firstCdsBaseInExon += Math.max(0, eint.getEnd() - Math.max(eint.getStart(), cdsStart) + 1);
			else firstCdsBaseInExon += Math.max(0, Math.min(cdsStart, eint.getEnd()) - eint.getStart() + 1);
		}

		return firstCdsBaseInExon - 1;
	}

	/**
	 * Return a codon that includes 'cdsBaseNumber'
	 */
	public String baseNumberCds2Codon(int cdsBaseNumber) {
		int codonNum = cdsBaseNumber / CodonChange.CODON_SIZE;
		int min = codonNum * CodonChange.CODON_SIZE;
		int max = codonNum * CodonChange.CODON_SIZE + CodonChange.CODON_SIZE;
		if ((min >= 0) && (max <= cds().length())) return cds().substring(min, max).toUpperCase();
		return null;
	}

	/**
	 * Calculate chromosome position as function of CDS number
	 *
	 * @returns An array mapping 'pos[cdsBaseNumber] = chromosmalPos'
	 */
	public synchronized int[] baseNumberCds2Pos() {
		if (cds2pos != null) return cds2pos;

		calcCdsStartEnd();

		cds2pos = new int[cds().length()];
		for (int i = 0; i < cds2pos.length; i++)
			cds2pos[i] = -1;

		int cdsMin = Math.min(cdsStart, cdsEnd);
		int cdsMax = Math.max(cdsStart, cdsEnd);

		// For each exon, add CDS position to array
		int cdsBaseNum = 0;
		for (Exon exon : sortedStrand()) {
			int min = isStrandPlus() ? exon.getStart() : exon.getEnd();
			int step = isStrandPlus() ? 1 : -1;
			for (int pos = min; exon.intersects(pos) && cdsBaseNum < cds2pos.length; pos += step)
				if ((cdsMin <= pos) && (pos <= cdsMax)) cds2pos[cdsBaseNum++] = pos;
		}

		return cds2pos;
	}

	/**
	 * Calculate distance from transcript to a position
	 * @param pos
	 * @return
	 */
	public synchronized int baseNumberPreMRna(int pos) {
		int count = 0;
		for (Exon eint : sortedStrand()) {
			if (eint.intersects(pos)) {
				// Intersect this exon? Calculate the number of bases from the beginning
				int dist = 0;
				if (isStrandPlus()) dist = pos - eint.getStart();
				else dist = eint.getEnd() - pos;

				// Sanity check
				if (dist < 0) throw new RuntimeException("Negative distance for position " + pos + ". This should never happen!\n" + this);

				return count + dist;
			}

			count += eint.size();
		}
		return -1;
	}

	/**
	 * Convert a 'cDNA' base number to a genomic coordinate
	 * @param baseNum
	 * @return
	 */
	public synchronized int baseNumberPreMRna2Pos(int baseNum) {
		for (Exon eint : sortedStrand()) {
			if (eint.size() >= baseNum) return eint.getStart() + baseNum;
			baseNum -= eint.size();
		}

		return -1;
	}

	/**
	 * Calculate CDS start and CDS end
	 */
	synchronized void calcCdsStartEnd() {
		// Do we need to calculate these values?
		if (cdsStart < 0) {
			// Calculate coding start (after 5 prime UTR)

			if (utrs.isEmpty()) {
				// No UTRs => Use all exons
				cdsStart = (isStrandPlus() ? end : start); // cdsStart is the position of the first base in the CDS (i.e. the first base after all 5'UTR)
				cdsEnd = (isStrandPlus() ? start : end); // cdsEnd is the position of the last base in the CDS (i.e. the first base before all 3'UTR)

				for (Exon ex : this) {
					if (isStrandPlus()) {
						cdsStart = Math.min(cdsStart, ex.getStart());
						cdsEnd = Math.max(cdsEnd, ex.getEnd());
					} else {
						cdsStart = Math.max(cdsStart, ex.getEnd());
						cdsEnd = Math.min(cdsEnd, ex.getStart());
					}
				}
			} else {
				// We have to take into account UTRs
				cdsStart = (isStrandPlus() ? start : end); // cdsStart is the position of the first base in the CDS (i.e. the first base after all 5'UTR)
				cdsEnd = (isStrandPlus() ? end : start); // cdsEnd is the position of the last base in the CDS (i.e. the first base before all 3'UTR)
				int cdsStartNotExon = cdsStart;

				for (Utr utr : utrs) {
					if (utr instanceof Utr5prime) {
						if (isStrandPlus()) cdsStart = Math.max(cdsStart, utr.getEnd() + 1);
						else cdsStart = Math.min(cdsStart, utr.getStart() - 1);
					} else if (utr instanceof Utr3prime) {
						if (isStrandPlus()) cdsEnd = Math.min(cdsEnd, utr.getStart() - 1);
						else cdsEnd = Math.max(cdsEnd, utr.getEnd() + 1);
					}
				}

				// Make sure cdsStart and cdsEnd lie within an exon
				if (isStrandPlus()) {
					cdsStart = firstExonPositionAfter(cdsStart);
					cdsEnd = lastExonPositionBefore(cdsEnd);
				} else {
					cdsStart = lastExonPositionBefore(cdsStart);
					cdsEnd = firstExonPositionAfter(cdsEnd);
				}

				// We were not able to find cdsStart & cdsEnd within exon limits.
				// Probably there is something wrong with the database and the transcript does
				// not have a single coding base (e.g. all of it is UTR).
				if (cdsStart < 0 || cdsEnd < 0) cdsStart = cdsEnd = cdsStartNotExon;
			}
		}
	}

	/**
	 * Retrieve coding sequence
	 */
	public synchronized String cds() {
		if (cds != null) return cds;

		// Concatenate all exons
		List<Exon> exons = sortedStrand();
		StringBuilder sequence = new StringBuilder();
		int utr5len = 0, utr3len = 0;

		// 5 prime UTR length
		for (Utr utr : get5primeUtrs())
			utr5len += utr.size();

		// Append all exon sequences
		boolean missingSequence = false;
		for (Exon exon : exons) {
			missingSequence |= !exon.hasSequence(); // If there is no sequence, we are in trouble
			sequence.append(exon.getSequence());
		}

		if (missingSequence) cds = ""; // One or more exons does not have sequence. Nothing to do
		else {
			// OK, all exons have sequences

			// 3 prime UTR length
			for (Utr utr : get3primeUtrs())
				utr3len += utr.size();

			// Cut 5 prime UTR and 3 prime UTR points
			int subEnd = sequence.length() - utr3len;

			if (utr5len > subEnd) cds = "";
			else cds = sequence.substring(utr5len, subEnd);
		}

		return cds;
	}

	/**
	 * Collapses exons having gaps of zero (i.e. exons that followed by other exons).
	 * Does the same for CDSs.
	   Does the same for UTRs.
	 */
	public boolean collapseZeroGap() {
		if (ribosomalSlippage) return false; // Overlapping exons are representing ribosomal slippage, so they are not annotations errors and must not be corrected.

		boolean ret = false;
		introns = null; // These need to be recalculated

		//---
		// Collapse Exons
		//---
		Markers markers = new Markers();
		markers.addAll(subintervals());
		Map<Marker, Marker> collapse = MarkerUtil.collapseZeroGap(markers); // Create a map of collapsed exons

		// Replace exons
		for (Marker exon : collapse.keySet()) {
			Exon collapsedExon = (Exon) collapse.get(exon);

			// Is this exon to be replaced?
			if (exon != collapsedExon) {
				ret = true;

				// Replace exon
				remove((Exon) exon);
				if (!containsId(collapsedExon.getId())) add(collapsedExon); // Add collapsedExon. Make sure we don't add it twice (since many exons can be collapsed into one).

				// Change parent exon in UTRs
				for (Marker m : getUtrs()) {
					Utr utr = (Utr) m;
					if (utr.getParent() == exon) utr.setParent(collapsedExon);
				}
			}
		}

		//---
		// Collapse CDS
		//---
		collapse = MarkerUtil.collapseZeroGap(new Markers(cdss));
		cdss = new ArrayList<Cds>(); // Re-create CDSs list
		Markers uniqCollapsedCds = new Markers(collapse.values()).unique(); // Create a set of unique CDSs and add them to CDSs list
		for (Marker cds : uniqCollapsedCds)
			cdss.add((Cds) cds);

		//---
		// Collapse UTRs
		//---
		collapse = MarkerUtil.collapseZeroGap(new Markers(utrs));
		Markers uniqCollapsedUtrs = new Markers(collapse.values()).unique(); // Create a set of unique UTRs, and add them to the list
		utrs = new ArrayList<Utr>(); // Re-generate UTRs list
		for (Marker utr : uniqCollapsedUtrs)
			utrs.add((Utr) utr);

		return ret;
	}

	/**
	 * Calculate CpG bias: number of CpG / expected[CpG]
	 * @return
	 */
	public double cpgExonBias() {
		ObservedOverExpectedCpG oe = new ObservedOverExpectedCpG();
		return oe.oe(this);
	}

	/**
	 * Count total CpG in this transcript's exons
	 * @return
	 */
	public int cpgExons() {
		ObservedOverExpectedCpG oe = new ObservedOverExpectedCpG();
		return oe.observed(this);
	}

	/**
	 * Find all splice sites.
	 *
	 * @param createIfMissing : If true, create canonical splice sites if they are missing.
	 *
	 * @return
	 */
	public List<SpliceSite> createSpliceSites(int spliceSiteSize, int spliceRegionExonSize, int spliceRegionIntronMin, int spliceRegionIntronMax) {
		List<SpliceSite> list = new LinkedList<SpliceSite>();

		// For each gene, transcript and exon
		ArrayList<Exon> exons = (ArrayList<Exon>) sortedStrand();

		if (exons.size() > 0) {
			for (int i = 0; i < exons.size(); i++) {
				Exon exon = exons.get(i);
				Exon prev = (i >= 1 ? exons.get(i - 1) : null);
				Exon next = (i < exons.size() - 1 ? exons.get(i + 1) : null);

				//---
				// Distance to previous exon
				//---
				if (prev != null) {
					int dist = 0;
					if (isStrandPlus()) dist = exon.getStart() - prev.getEnd() - 1;
					else dist = prev.getStart() - exon.getEnd() - 1;

					// Acceptor splice site: before exon start, but not before first exon
					SpliceSite ss = exon.createSpliceSiteAcceptor(Math.min(spliceSiteSize, dist));
					if (ss != null) list.add(ss);

					// Splice site region at the end
					SpliceSiteRegion ssr = exon.createSpliceSiteRegionStart(spliceRegionExonSize);
					if (ssr != null) list.add(ssr);
				}

				//---
				// Distance to next exon
				//---
				if (next != null) {
					int dist = 0;
					if (isStrandPlus()) dist = next.getStart() - exon.getEnd() - 1;
					else dist = exon.getStart() - next.getEnd() - 1;

					// Donor splice site: after exon end, but not after last exon
					SpliceSite ss = exon.createSpliceSiteDonor(Math.min(spliceSiteSize, dist));
					if (ss != null) list.add(ss);

					// Splice site region at the end
					SpliceSiteRegion ssr = exon.createSpliceSiteRegionEnd(spliceRegionExonSize);
					if (ssr != null) list.add(ssr);
				}

				// Sanity check
				int rank = i + 1;
				if (exon.getRank() != rank) {
					String msg = "Rank numbers do not march: " + rank + " != " + exon.getRank() + "\n\tTranscript: " + this;
					throw new RuntimeException(msg);
				}
			}
		}

		// Add splice site regions (Introns)
		introns();
		if (introns != null) {
			for (int i = 0; i < introns.size(); i++) {
				Intron intron = introns.get(i);

				if (i > 0) {
					SpliceSiteRegion ssrs = intron.createSpliceSiteRegionStart(spliceRegionIntronMin, spliceRegionIntronMax);
					if (ssrs != null) list.add(ssrs);
				}

				if (i < (introns.size() - 1)) {
					SpliceSiteRegion ssre = intron.createSpliceSiteRegionEnd(spliceRegionIntronMin, spliceRegionIntronMax);
					if (ssre != null) list.add(ssre);
				}

			}

		}

		return list;
	}

	/**
	 * Creates a list of UP/DOWN stream regions (for each transcript)
	 * Upstream (downstream) stream is defined as upDownLength before (after) transcript
	 */
	public void createUpDownStream(int upDownLength) {
		Chromosome chr = getChromosome();
		int chrMin = chr.getStart(), chrMax = chr.getEnd();

		// Create up/down stream intervals and add them to the list
		if (isStrandPlus()) {
			upstream = new Upstream(this, Math.max(start - upDownLength, chrMin), Math.max(start - 1, chrMin), false, id);
			downstream = new Downstream(this, Math.min(end + 1, chrMax), Math.min(end + upDownLength, chrMax), false, id);
		} else {
			upstream = new Upstream(this, Math.min(end + 1, chrMax), Math.min(end + upDownLength, chrMax), false, id);
			downstream = new Downstream(this, Math.max(start - upDownLength, chrMin), Math.max(start - 1, chrMin), false, id);
		}
	}

	/**
	 * Deletes redundant exons (i.e. exons that are totally included in other exons).
	 * Does the same for CDSs.
	   Does the same for UTRs.
	 */
	public boolean deleteRedundant() {
		boolean ret = false;
		introns = null; // These need to be recalculated

		//---
		// Delete redundant exons
		//---
		Map<Marker, Marker> includedIn = MarkerUtil.redundant(subintervals());
		for (Marker exon : includedIn.keySet()) {
			ret = true;
			remove((Exon) exon);

			// Change parent exon in UTRs
			for (Marker m : getUtrs()) {
				Utr utr = (Utr) m;
				if (utr.getParent() == exon) utr.setParent(includedIn.get(exon));
			}
		}

		//---
		// Delete redundant CDS
		//---
		includedIn = MarkerUtil.redundant(cdss);
		for (Marker cds : includedIn.keySet())
			cdss.remove(cds);

		//---
		// Delete redundant UTRs
		//---
		includedIn = MarkerUtil.redundant(utrs);
		for (Marker utr : includedIn.keySet())
			utrs.remove(utr);

		return ret;
	}

	/**
	 * Return the Exon that hits position 'pos'
	 * @param pos
	 * @return An exon intersecting 'pos' (null if not found)
	 */
	public Exon findExon(int pos) {
		for (Exon exon : this)
			if (exon.intersects(pos)) return exon;
		return null;
	}

	/**
	 * Find a CDS that matches exactly the exon
	 * @param exon
	 * @return
	 */
	public Cds findMatchingCds(Exon exon) {
		for (Cds cds : cdss)
			if (exon.includes(cds)) return cds;
		return null;
	}

	/**
	 * Return the UTR that hits position 'pos'
	 * @param pos
	 * @return An UTR intersecting 'pos' (null if not found)
	 */
	public Utr findUtr(int pos) {
		// Is it in UTR instead of CDS?
		for (Utr utr : utrs)
			if (utr.intersects(pos)) return utr;
		return null;
	}

	/**
	 * Return the UTR that hits position 'pos'
	 * @param posPrev
	 * @return An UTR intersecting 'pos' (null if not found)
	 */
	public List<Utr> findUtrs(Marker marker) {
		List<Utr> utrs = new LinkedList<Utr>();

		// Is it in UTR instead of CDS?
		for (Utr utr : utrs)
			if (utr.intersects(marker)) utrs.add(utr);

		return utrs.isEmpty() ? null : utrs;
	}

	/**
	 * Find the first position after 'pos' within an exon
	 * @param pos
	 * @return
	 */
	int firstExonPositionAfter(int pos) {
		for (Exon ex : sorted()) {
			if (pos <= ex.getStart()) return ex.getStart();
			if (pos <= ex.getEnd()) return pos;
		}

		System.err.println("WARNING: Cannot find first exonic position after " + pos + " for transcript '" + id + "'");
		return -1;
	}

	/**
	 * Correct exons based on frame information.
	 *
	 * E.g. if the frame information (form a genomic
	 * database file, such as a GTF) does not
	 * match the calculated frame, we correct exon's
	 * boundaries to make them match.
	 *
	 * This is performed in two stages:
	 *    i) First exon is corrected by adding a fake 5'UTR
	 *    ii) Other exons are corrected by changing the start (or end) coordinates.
	 *
	 * @return
	 */
	public synchronized boolean frameCorrection() {
		// Copy frame information form CDSs to Exons (if missing)
		frameFromCds();

		// First exon is corrected by adding a fake 5'UTR
		boolean changedFirst = frameCorrectionFirstCodingExon();

		// Other exons are corrected by changing the start (or end) coordinates.
		// boolean changedNonFirst = false;
		// Gpr.debug("UNCOMMENT!");
		boolean changedNonFirst = frameCorrectionNonFirstCodingExon();

		boolean changed = changedFirst || changedNonFirst;

		// We have to reset cached CDS data if anything changed
		if (changed) resetCdsCache();

		// Return true if there was any adjustment
		return changed;
	}

	/**
	 * Fix transcripts having non-zero frames in first exon
	 *
	 * Transcripts whose first exon has a non-zero frame indicate problems.
	 * We add a 'fake' UTR5 to compensate for reading frame.
	 *
	 * @param showEvery
	 */
	synchronized boolean frameCorrectionFirstCodingExon() {
		List<Exon> exons = sortedStrand();

		// No exons? Nothing to do
		if ((exons == null) || exons.isEmpty()) return false;

		Exon exonFirst = getFirstCodingExon(); // Get first exon
		// Exon exonFirst =  exons.get(0); // Get first exon
		if (exonFirst.getFrame() <= 0) return false; // Frame OK (or missing), nothing to do

		// First exon is not zero? => Create a UTR5 prime to compensate
		Utr5prime utr5 = null;
		int frame = exonFirst.getFrame();

		if (isStrandPlus()) {
			int end = exonFirst.getStart() + (frame - 1);
			utr5 = new Utr5prime(exonFirst, exonFirst.getStart(), end, isStrandMinus(), exonFirst.getId());
		} else {
			int start = exonFirst.getEnd() - (frame - 1);
			utr5 = new Utr5prime(exonFirst, start, exonFirst.getEnd(), isStrandMinus(), exonFirst.getId());
		}

		// Reset frame, since it was already corrected
		exonFirst.setFrame(0);
		Cds cds = findMatchingCds(exonFirst);
		if (cds != null) cds.frameCorrection(cds.getFrame());

		// Add UTR5'
		add(utr5);

		// Update counter
		return true;
	}

	/**
	 * Correct exons according to frame information
	 */
	synchronized boolean frameCorrectionNonFirstCodingExon() {
		boolean corrected = false;

		// Concatenate all exons to create a CDS
		List<Exon> exons = sortedStrand();
		StringBuilder sequence = new StringBuilder();

		// We don't need to correct if there is no sequence! (the problem only exists due to sequence frame)
		for (Exon exon : exons)
			if (!exon.hasSequence()) return false;

		// 5'UTR length
		int utr5Start = Integer.MAX_VALUE, utr5End = -1;
		for (Utr utr : get5primeUtrs()) {
			utr.size();
			utr5Start = Math.min(utr5Start, utr.getStart());
			utr5End = Math.max(utr5End, utr.getEnd());
		}

		// Create UTR
		Marker utr5 = utr5End >= 0 ? new Marker(this, utr5Start, utr5End, strandMinus, "") : null;

		// Append all exon sequences
		for (Exon exon : exons) {
			String seq = "";
			int utrOverlap = 0;

			// Check if exon overlaps UTR
			if (utr5 != null && utr5.includes(exon)) {
				// The whole exon is included => No sequence change
				seq = "";
			} else {
				// Add sequence
				seq = exon.getSequence();
				if (utr5 != null && utr5.intersects(exon)) {
					utrOverlap = utr5.intersectSize(exon);
					if (utrOverlap > 0) {
						if (utrOverlap < seq.length()) seq = seq.substring(utrOverlap);
						else seq = "";
					}
				}
			}

			//---
			// Frame check
			//---
			if (exon.getFrame() < 0) {
				// Nothing to do (assume current frame is right
			} else {
				// Calculate frame
				// References: http://mblab.wustl.edu/GTF22.html
				int frameReal = GprSeq.frameFromLength(sequence.length());

				// Does calculated frame match?
				if (frameReal != exon.getFrame()) {
					if (utrOverlap > 0) {
						throw new RuntimeException("Fatal Error: First exon needs correction: This should never happen!"//
								+ "\n\tThis method is supposed to be called AFTER method"//
								+ "\n\tSnpEffPredictorFactory.frameCorrectionFirstCodingExon(), which"//
								+ "\n\tshould have taken care of this problem." //
								+ "\n\t" + this //
								);
					} else {
						// Find matching CDS
						Cds cdsToCorrect = findMatchingCds(exon);

						// Correct exon until we get the expected frame
						for (boolean ok = true; ok && frameReal != exon.getFrame();) {
							// Correct both Exon and CDS
							ok &= exon.frameCorrection(1);
							if (cdsToCorrect != null) cdsToCorrect.frameCorrection(1);
							corrected = true;
						}

						// Get new exon's sequence
						seq = exon.getSequence();
					}
				}
			}

			// Append sequence
			sequence.append(seq);
		}

		return corrected;
	}

	/**
	 * Copy frame info from CDSs into Exons
	 */
	void frameFromCds() {
		for (Exon ex : this) {
			// No frame info? => try to find matching CDS
			if (ex.getFrame() < 0) {
				// Check a CDS that matches an exon
				for (Cds cds : getCds()) {
					// CDS matches the exon coordinates? => Copy frame info
					if (isStrandPlus() && (ex.getStart() == cds.getStart())) {
						ex.setFrame(cds.getFrame());
						break;
					} else if (isStrandMinus() && (ex.getEnd() == cds.getEnd())) {
						ex.setFrame(cds.getFrame());
						break;
					}
				}
			}
		}

	}

	/**
	 * Create a list of 3 prime UTRs
	 */
	public List<Utr3prime> get3primeUtrs() {
		ArrayList<Utr3prime> list = new ArrayList<Utr3prime>();
		for (Utr utr : utrs)
			if (utr instanceof Utr3prime) list.add((Utr3prime) utr);
		return list;
	}

	public List<Utr3prime> get3primeUtrsSorted() {
		List<Utr3prime> list = get3primeUtrs();
		Collections.sort(list);
		return list;
	}

	/**
	 * Create a list of 5 prime UTRs
	 */
	public List<Utr5prime> get5primeUtrs() {
		ArrayList<Utr5prime> list = new ArrayList<Utr5prime>();
		for (Utr utr : utrs)
			if (utr instanceof Utr5prime) list.add((Utr5prime) utr);
		return list;
	}

	public List<Utr5prime> get5primeUtrsSorted() {
		List<Utr5prime> list = get5primeUtrs();
		Collections.sort(list);
		return list;
	}

	public String getBioType() {
		return bioType;
	}

	/**
	 * Get all CDSs
	 * @return
	 */
	public List<Cds> getCds() {
		return cdss;
	}

	public int getCdsEnd() {
		calcCdsStartEnd();
		return cdsEnd;
	}

	public int getCdsStart() {
		calcCdsStartEnd();
		return cdsStart;
	}

	public Downstream getDownstream() {
		return downstream;
	}

	/**
	 * Get first coding exon
	 * @return
	 */
	public synchronized Exon getFirstCodingExon() {
		if (firstCodingExon == null) {
			// Get transcription start position
			long cstart = getCdsStart();

			// Pick exon intersecting cdsStart (TSS)
			for (Exon exon : sortedStrand())
				if (exon.intersects(cstart)) firstCodingExon = exon;

			// Sanity check
			if (firstCodingExon == null) throw new RuntimeException("Error: Cannot find first coding exon for transcript:\n" + this);
		}
		return firstCodingExon;
	}

	public ArrayList<SpliceSiteBranch> getSpliceBranchSites() {
		return spliceBranchSites;
	}

	/**
	 * Create a TSS marker
	 * @return
	 */
	public Marker getTss() {
		calcCdsStartEnd();
		Marker tss = new Marker(this, start + (isStrandPlus() ? 0 : -1), start + (isStrandPlus() ? 1 : 0), false, "TSS_" + id);
		return tss;
	}

	public Upstream getUpstream() {
		return upstream;
	}

	/**
	 * Get all UTRs
	 */
	public List<Utr> getUtrs() {
		return utrs;
	}

	/**
	 * Does this transcript have any errors?
	 * @return
	 */
	public boolean hasError() {
		return isErrorProteinLength() || isErrorStartCodon() || isErrorStopCodonsInCds();
	}

	/**
	 * Does this transcript have any errors?
	 * @return
	 */
	public boolean hasErrorOrWarning() {
		return isErrorProteinLength() || isErrorStartCodon() || isErrorStopCodonsInCds() // Errors
				|| isWarningStopCodon() // Warnings
				;
	}

	/**
	 * Get all introns (lazy init)
	 * @return
	 */
	public synchronized ArrayList<Intron> introns() {
		if (introns == null) {
			introns = new ArrayList<Intron>();

			Exon exBefore = null;
			for (Exon ex : sortedStrand()) {
				if (exBefore != null) {
					// Create intron
					Intron intron;
					int rank = introns.size() + 1;

					// Find intron start and end
					int start, end;
					if (isStrandPlus()) {
						start = exBefore.getEnd() + 1;
						end = ex.getStart() - 1;
					} else {
						start = ex.getEnd() + 1;
						end = exBefore.getStart() - 1;
					}

					int size = end - start + 1;
					if (size > 0) {
						// Add intron to list
						intron = new Intron(this, start, end, strandMinus, id + "_intron_" + rank, exBefore, ex);

						intron.setRank(rank);
						introns.add(intron);
					}
				}

				exBefore = ex;
			}
		}
		return introns;
	}

	/**
	 * Return the intron size for intron number 'intronNum'
	 *
	 * Note: Intron number 'N' is the intron between exon number N and exon number N+1
	 * Note: Numbering is zero-based (not to be confused with exon 'ranking', which is one-based)
	 *
	 * @param intronNum
	 * @return
	 */
	public int intronSize(int intronNum) {
		if (intronNum >= (numChilds() - 1)) return -1;
		ArrayList<Exon> exons = (ArrayList<Exon>) sortedStrand();
		Exon exon = exons.get(intronNum);
		Exon next = exons.get(intronNum + 1);
		return (isStrandPlus() ? (next.getStart() - exon.getEnd()) : (exon.getStart() - next.getEnd())) - 1;
	}

	public boolean isAaCheck() {
		return aaCheck;
	}

	@Override
	protected boolean isAdjustIfParentDoesNotInclude(Marker parent) {
		return true;
	}

	/**
	 * Is this variant in the CDS part of this transcript?
	 * @param variant
	 * @return
	 */
	boolean isCds(Variant variant) {
		calcCdsStartEnd();

		int cs = cdsStart;
		int ce = cdsEnd;

		if (isStrandMinus()) {
			cs = cdsEnd;
			ce = cdsStart;
		}

		return (variant.getEnd() >= cs) && (variant.getStart() <= ce);
	}

	/**
	 * Has this transcript been checked against CDS/DNA/AA sequences?
	 */
	public boolean isChecked() {
		return aaCheck || dnaCheck;
	}

	public boolean isDnaCheck() {
		return dnaCheck;
	}

	/**
	 * Check if coding length is multiple of 3 in protein coding transcripts
	 * @return true on Error
	 */
	public boolean isErrorProteinLength() {
		if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;
		cds();
		return (cds.length() % 3) != 0;
	}

	/**
	 * Is the first codon a START codon?
	 * @return
	 */
	public boolean isErrorStartCodon() {
		if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;

		// Not even one codon in this protein? Error
		String cds = cds();
		if (cds.length() < 3) return true;

		String codon = cds.substring(0, 3);
		return !codonTable().isStart(codon);
	}

	/**
	 * Check if protein sequence has STOP codons in the middle of the coding sequence
	 * @return true on Error
	 */
	public boolean isErrorStopCodonsInCds() {
		if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;

		// Get protein sequence
		String prot = protein();
		if (prot == null) return false;

		// Any STOP codon before the end?
		char bases[] = prot.toCharArray();
		int max = bases.length - 1;
		int countErrs = 0;
		for (int i = 0; i < max; i++)
			if (bases[i] == '*') {
				countErrs++;
				// We allow up to one STOP codon because it can be a RARE_AMINO_ACID which is coded as a STOP codon.
				// More than one STOP codon is not "normal", so it's probably an error in the genomic annotations (e.g. ENSEMBL or UCSC)
				if (countErrs > 1) return true;
			}

		// OK
		return false;
	}

	public boolean isProteinCoding() {
		return proteinCoding;
	}

	public boolean isRibosomalSlippage() {
		return ribosomalSlippage;
	}

	/**
	 * Does this 'pos' hit a UTR?
	 * @param pos
	 * @return
	 */
	public boolean isUtr(int pos) {
		return findUtr(pos) != null;
	}

	public boolean isUtr(Marker marker) {
		return findUtrs(marker) != null;
	}

	/**
	 * Is the last codon a STOP codon?
	 * @return
	 */
	public boolean isWarningStopCodon() {
		if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) return false;

		// Not even one codon in this protein? Error
		String cds = cds();
		if (cds.length() < 3) return true;

		String codon = cds.substring(cds.length() - 3);
		return !codonTable().isStop(codon);
	}

	/**
	 * Find the last position before 'pos' within an exon
	 * @param pos
	 * @return
	 */
	int lastExonPositionBefore(int pos) {
		int last = -1;
		for (Exon ex : sorted()) {
			if (pos < ex.getStart()) {
				// Nothing found?
				if (last < 0) {
					System.err.println("WARNING: Cannot find last exonic position before " + pos + " for transcript '" + id + "'");
					return -1;
				}
				return last;
			} else if (pos <= ex.getEnd()) return pos;
			last = ex.getEnd();
		}

		if (last < 0) System.err.println("WARNING: Cannot find last exonic position before " + pos + " for transcript '" + id + "'");
		return pos;
	}

	/**
	 * A list of all markers in this transcript
	 * @return
	 */
	@Override
	public Markers markers() {
		Markers markers = new Markers();
		markers.addAll(subIntervals.values());
		markers.addAll(spliceBranchSites);
		markers.addAll(utrs);
		markers.addAll(cdss);
		markers.add(upstream);
		markers.add(downstream);
		markers.addAll(introns());
		return markers;
	}

	/**
	 * Retrieve coding sequence AND the UTRs (mRNA = 5'UTR + CDS + 3'UTR)
	 * I.e. Concatenate all exon sequences
	 */
	public String mRna() {
		List<Exon> exons = sortedStrand();

		// Concatenate all exons
		StringBuilder sequence = new StringBuilder();
		for (Exon ex : exons)
			sequence.append(ex.getSequence());

		return sequence.toString();
	}

	/**
	 * Protein sequence (amino acid sequence produced by this transcripts)
	 * @return
	 */
	public String protein() {
		if (protein == null) {
			if (!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) protein = "";
			else protein = codonTable().aa(cds());
		}
		return protein;
	}

	/**
	 * Query all genomic regions that intersect 'marker'
	 */
	@Override
	public Markers query(Marker marker) {
		Markers markers = new Markers();

		// Add exons
		for (Exon e : this)
			if (e.intersects(marker)) {
				markers.add(e);
				markers.add(e.query(marker));
			}

		// Ad splice sites
		for (SpliceSiteBranch sb : spliceBranchSites)
			if (sb.intersects(marker)) markers.add(sb);

		// Ad UTRs
		for (Utr u : utrs)
			if (u.intersects(marker)) markers.add(u);

		// Add CDSs
		for (Cds m : cdss)
			if (m.intersects(marker)) markers.add(m);

		// Add introns
		for (Intron m : introns())
			if (m.intersects(marker)) markers.add(m);

		// Note: Upstram and Downstream are technically NOT in the
		// transcript. So we should not be returning them
		//
		//		// Add upstream & downstream
		//		if (upstream.intersects(marker)) markers.add(upstream);
		//		if (downstream.intersects(marker)) markers.add(downstream);

		return markers;
	}

	/**
	 * Return the first exon that intersects 'interval' (null if not found)
	 * @param interval
	 * @return
	 */
	public Exon queryExon(Marker interval) {
		for (Exon ei : this)
			if (ei.intersects(interval)) return ei;
		return null;
	}

	/**
	 * Assign ranks to exons
	 */
	public boolean rankExons() {
		boolean changed = false;
		int rank = 1;
		for (Exon exon : sortedStrand()) {
			if (rank != exon.getRank()) {
				exon.setRank(rank);
				changed = true;
			}
			rank++;
		}
		return changed;
	}

	@Override
	public void reset() {
		super.reset();
		sorted = null;
		spliceBranchSites = new ArrayList<SpliceSiteBranch>();
		utrs = new ArrayList<Utr>();
		cdss = new ArrayList<Cds>();
		introns = null;
		upstream = null;
		downstream = null;
		resetCdsCache();
	}

	public void resetCdsCache() {
		cdsStart = -1;
		cdsEnd = -1;
		firstCodingExon = null;
		cds = null;
		cds2pos = null;
	}

	/**
	 * Perfom some baseic chekcs, return error type, if any
	 * @param variant
	 * @return
	 */
	public ErrorWarningType sanityCheck(Variant variant) {
		if (isErrorProteinLength()) return ErrorWarningType.WARNING_TRANSCRIPT_INCOMPLETE;
		else if (isErrorStopCodonsInCds()) return ErrorWarningType.WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS;
		else if (isErrorStartCodon()) return ErrorWarningType.WARNING_TRANSCRIPT_NO_START_CODON;
		return null;
	}

	/**
	 * Parse a line from a serialized file
	 * @param line
	 * @return
	 */
	@Override
	public void serializeParse(MarkerSerializer markerSerializer) {
		super.serializeParse(markerSerializer);
		bioType = markerSerializer.getNextField();
		proteinCoding = markerSerializer.getNextFieldBoolean();
		dnaCheck = markerSerializer.getNextFieldBoolean();
		aaCheck = markerSerializer.getNextFieldBoolean();
		ribosomalSlippage = markerSerializer.getNextFieldBoolean();
		upstream = (Upstream) markerSerializer.getNextFieldMarker();
		downstream = (Downstream) markerSerializer.getNextFieldMarker();

		for (Marker m : markerSerializer.getNextFieldMarkers())
			utrs.add((Utr) m);

		for (Marker m : markerSerializer.getNextFieldMarkers())
			cdss.add((Cds) m);

		for (Marker m : markerSerializer.getNextFieldMarkers())
			spliceBranchSites.add((SpliceSiteBranchU12) m);
	}

	/**
	 * Create a string to serialize to a file
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public String serializeSave(MarkerSerializer markerSerializer) {
		return super.serializeSave(markerSerializer) //
				+ "\t" + bioType //
				+ "\t" + proteinCoding //
				+ "\t" + dnaCheck //
				+ "\t" + aaCheck //
				+ "\t" + ribosomalSlippage //
				+ "\t" + markerSerializer.save(upstream) //
				+ "\t" + markerSerializer.save(downstream) //
				+ "\t" + markerSerializer.save((Iterable) utrs)//
				+ "\t" + markerSerializer.save((Iterable) cdss)//
				+ "\t" + markerSerializer.save((Iterable) spliceBranchSites)//
				;
	}

	public void setAaCheck(boolean aaCheck) {
		this.aaCheck = aaCheck;
	}

	public void setBioType(String bioType) {
		this.bioType = bioType;
	}

	public void setDnaCheck(boolean dnaCheck) {
		this.dnaCheck = dnaCheck;
	}

	public void setProteinCoding(boolean proteinCoding) {
		this.proteinCoding = proteinCoding;
	}

	public void setRibosomalSlippage(boolean ribosomalSlippage) {
		this.ribosomalSlippage = ribosomalSlippage;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getChromosomeName() + ":" + start + "-" + end);
		sb.append(", strand: " + (isStrandPlus() ? "+" : "-"));
		if ((id != null) && (id.length() > 0)) sb.append(", id:" + id);
		if ((bioType != null) && (bioType.length() > 0)) sb.append(", bioType:" + bioType);
		if (isProteinCoding()) sb.append(", Protein");

		if (numChilds() > 0) {
			sb.append("\n");
			for (Utr utr : get5primeUtrsSorted())
				sb.append("\t\t5'UTR   :\t" + utr + "\n");

			sb.append("\t\tExons:\n");
			for (Exon exon : sorted())
				sb.append("\t\t" + exon + "\n");

			for (Utr utr : get3primeUtrsSorted())
				sb.append("\t\t3'UTR   :\t" + utr + "\n");

			// We may show CDS
			if (isProteinCoding()) {
				sb.append("\t\tCDS     :\t" + cds() + "\n");
				sb.append("\t\tProtein :\t" + protein() + "\n");
			}
		}

		return sb.toString();
	}

	/**
	 * Show a transcript as an ASCII Art
	 * @return
	 */
	public String toStringAsciiArt() {
		char art[] = new char[size()];
		for (int i = start, j = 0; i <= end; i++, j++) {
			Utr utr = findUtr(i);
			if (utr != null) art[j] = utr.isUtr5prime() ? '5' : '3';
			else {
				Exon exon = findExon(i);
				if (exon != null) art[j] = exon.isStrandPlus() ? '>' : '<';
				else art[j] = '-';
			}
		}

		return new String(art);
	}

	/**
	 * Calculate UTR regions from CDSs
	 */
	public boolean utrFromCds(boolean verbose) {
		if (cdss.size() <= 0) return false; // Cannot do this if we don't have CDS information

		// All exons minus all UTRs and CDS should give us the missing UTRs
		Markers exons = new Markers();
		Markers minus = new Markers();

		// Add all exons
		for (Exon e : this)
			exons.add(e);

		// Add all UTRs and CDSs to the 'minus' set
		for (Utr uint : getUtrs())
			minus.add(uint);

		for (Cds cint : cdss)
			minus.add(cint);

		Markers missingUtrs = exons.minus(minus); // Perform interval minus
		if (missingUtrs.size() > 0) return addMissingUtrs(missingUtrs, verbose); // Anything left? => There was a missing UTR
		return false;
	}

	/**
	 * Get some details about the effect on this transcript
	 * @param variant
	 * @return
	 */
	@Override
	public boolean variantEffect(Variant variant, VariantEffects variantsEffect) {
		if (!intersects(variant)) return false; // Sanity check

		//---
		// Hits a UTR region?
		//---
		boolean included = false;
		for (Utr utr : utrs)
			if (utr.intersects(variant)) {
				// Calculate the effect
				utr.variantEffect(variant, variantsEffect);
				included |= utr.includes(variant); // Is this variant fully included in the UTR?
			}
		if (included) return true; // SeqChange fully included in the UTR? => We are done.

		//---
		// Hits a SpliceSiteBranch region?
		//---
		included = false;
		for (SpliceSiteBranch ssbranch : spliceBranchSites)
			if (ssbranch.intersects(variant)) {
				// Calculate the effect
				ssbranch.variantEffect(variant, variantsEffect);
				included |= ssbranch.includes(variant); // Is this variant fully included branch site?
			}
		if (included) return true; // SeqChange fully included in the Branch site? => We are done.

		// Does it hit an intron?
		for (Intron intron : introns())
			if (intron.intersects(variant)) {
				variantsEffect.effect(intron, EffectType.INTRON, "");
				included |= intron.includes(variant); // Is this variant fully included in this intron?
			}
		if (included) return true; // SeqChange fully included? => We are done.

		//---
		// Analyze non-coding transcripts (or 'interval' variants)
		//---
		if ((!Config.get().isTreatAllAsProteinCoding() && !isProteinCoding()) || variant.isInterval() || !variant.isVariant()) {
			// Do we have exon information for this transcript?
			if (!subintervals().isEmpty()) {
				// Add all exons
				for (Exon exon : this)
					if (exon.intersects(variant)) variantsEffect.effect(exon, EffectType.EXON, "");
			} else variantsEffect.effect(this, EffectType.TRANSCRIPT, ""); // No exons annotated? Just mark it as hitting a transcript

			// Ok, we are done
			return true;
		}

		//---
		// This is a protein coding transcript.
		// We analyze codon replacement effect
		//---
		if (isCds(variant)) {
			// Get codon change effect
			CodonChange codonChange = new CodonChange(variant, this, variantsEffect);
			codonChange.variantEffect();
			return true;
		}

		return false;
	}

}
