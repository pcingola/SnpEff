package org.snpeff.pdb;

import org.biojava.nbio.structure.AminoAcid;
import org.snpeff.interval.Chromosome;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Gpr;

public class DistanceResult {

	public static boolean debug = false;

	// Pdb information
	public String pdbId;
	public String pdbChainId1;
	public String pdbChainId2;
	public int aaPos1, aaPos2;
	public char aa1, aa2;
	public double distance;

	// Genomic information
	public String trId1, trId2;
	public String chr1, chr2;
	public int pos1, pos2;

	public DistanceResult() {
		pdbId = pdbChainId1 = pdbChainId2 = trId1 = trId2 = chr1 = chr2 = "";
		aaPos1 = aaPos2 = pos1 = pos2 = -1;
		distance = -1;
		aa1 = aa2 = '.';
	}

	public DistanceResult(AminoAcid aa1, AminoAcid aa2, Transcript tr1, Transcript tr2, double distance) {
		this();
		setAa1(aa1);
		setAa2(aa2);
		setTr1(tr1);
		setTr2(tr2);
		this.distance = distance;
	}

	public DistanceResult(String line) {
		this();

		try {
			// Parse line
			String fields[] = line.split("\t");
			int n = 0;
			if (fields.length < 7) return;

			pdbId = fields[n++];
			pdbChainId1 = fields[n++];
			pdbChainId2 = fields[n++];
			distance = Gpr.parseDoubleSafe(fields[n++]);
			aa1 = fields[n++].charAt(0);
			aaPos1 = Gpr.parseIntSafe(fields[n++]);
			aa2 = fields[n++].charAt(0);
			aaPos2 = Gpr.parseIntSafe(fields[n++]);

			// Optional fields
			if (fields.length > n) {
				String chrPos1 = fields[n++];
				if (!chrPos1.isEmpty()) {
					String f[] = chrPos1.split(":");
					chr1 = f[0];
					pos1 = Gpr.parseIntSafe(f[1]);
				}
			}

			if (fields.length > n) {
				String chrPos2 = fields[n++];
				if (!chrPos2.isEmpty()) {
					String f[] = chrPos2.split(":");
					chr2 = f[0];
					pos2 = Gpr.parseIntSafe(f[1]);
				}
			}

			if (fields.length > n) trId1 = fields[n++];
			if (fields.length > n) trId2 = fields[n++];
		} catch (Exception e) {
			throw new RuntimeException("Error processing line:\t'" + line + "'", e);
		}
	}

	/**
	 * Convert AA number to genomic coordinate
	 */
	int aaNum2ChrPos(Transcript tr, int aaNum, char aa) {
		if (aaNum < 0) {
			if (debug) Gpr.debug("Invalid AA number:" + aaNum //
					+ "\n\tDistanceResult: " + this //
					+ "\n\tTranscript    : " + tr //
			);
			return -1;
		}

		// Does transcript's AA sequence match the expected AA?
		String protein = tr.protein();
		if (protein == null || protein.length() <= aaNum) {
			if (debug) Gpr.debug("Invalid AA number:" //
					+ "\n\tAA number     : " + aaNum //
					+ "\n\tProtein length: " + protein.length() //
					+ "\n\tDistanceResult: " + this //
					+ "\n\tTranscript    : " + tr //
			);
			return -1;
		}

		// Does transcript's AA sequence match the expected AA?
		if (protein.charAt(aaNum) != aa) {
			if (debug) Gpr.debug("AA not matching the expected sequence:" //
					+ "\n\tAA             :\t" + aa //
					+ "\n\ttr.protein     :\t" + protein //
					+ "\n\ttr.protein[" + aaNum + "]:\t" + protein.charAt(aaNum) //
					+ "\n\tDistanceResult: " + this //
					+ "\n\tTranscript    : " + tr //
			);
			return -1;
		}

		// Find genomic position based on AA position
		int aa2pos[] = tr.aaNumber2Pos();
		if (aa2pos.length <= aaNum) {
			if (debug) Gpr.debug("AA number out of range in aa2pos[]: " //
					+ "\n\tAA number        : " + aaNum //
					+ "\n\ttr.aa2pos.length : " + aa2pos.length //
					+ "\n\tDistanceResult: " + this //
					+ "\n\tTranscript       : " + tr //
			);
			return -1;
		}

		// Convert to genomic positions
		return aa2pos[aaNum];
	}

	/**
	 * Compare by genomic position
	 */
	public int compareByPos(DistanceResult d) {
		// Compare first position
		int comp = Chromosome.compare(chr1, d.chr1);
		if (comp != 0) return comp;

		comp = pos1 - d.pos1;
		if (comp != 0) return comp;

		// Compare second position
		comp = Chromosome.number(chr2) - Chromosome.number(d.chr2);
		if (comp != 0) return comp;

		comp = Chromosome.compare(chr2, d.chr2);
		if (comp != 0) return comp;

		comp = pos2 - d.pos2;
		if (comp != 0) return comp;

		// Compare distances
		return (int) Math.signum(distance - d.distance);
	}

	/**
	 * Same genomic positions
	 */
	public boolean equalPos(DistanceResult d) {
		return chr1.equals(d.chr1) //
				&& chr2.equals(d.chr2) //
				&& pos1 == d.pos1 //
				&& pos2 == d.pos2 //
		;
	}

	public String getId() {
		return pdbId // PDB ID
				+ ":" + pdbChainId1 // PDB chain name
				+ "_" + (aaPos1 + 1) // AA number (one-based)
				+ "-" + pdbChainId2 // PDB chain name
				+ "_" + (aaPos2 + 1) // AA number (one-based)
				+ ":" + trId1 //
				+ (!trId2.equals(trId1) ? "-" + trId2 : "") // Ommit if it's the same transcript
		;
	}

	public boolean hasValidCoords() {
		return !chr1.isEmpty() && !trId1.isEmpty() && pos1 >= 0 //
				&& !chr2.isEmpty() && !trId2.isEmpty() && pos2 >= 0 //
		;
	}

	public void setAa1(AminoAcid aa) {
		setPdbId(aa.getChain().getStructure().getPDBCode());
		pdbChainId1 = aa.getChainId();
		aaPos1 = aa.getResidueNumber().getSeqNum() - 1;
		aa1 = aa.getChemComp().getOne_letter_code().charAt(0);
	}

	public void setAa2(AminoAcid aa) {
		setPdbId(aa.getChain().getStructure().getPDBCode());
		pdbChainId2 = aa.getChainId();
		aaPos2 = aa.getResidueNumber().getSeqNum() - 1;
		aa2 = aa.getChemComp().getOne_letter_code().charAt(0);
	}

	void setPdbId(String pdbId) {
		if (this.pdbId == null || this.pdbId.isEmpty()) {
			this.pdbId = pdbId;
		} else if (!this.pdbId.equals(pdbId)) {
			// Cannot change to different Pdb ID
			throw new RuntimeException("New pdbId does not match old pdbId: " + this.pdbId + " != " + pdbId);
		}
	}

	public void setTr1(Transcript tr) {
		trId1 = tr.getId();
		chr1 = tr.getChromosomeName();
		pos1 = aaNum2ChrPos(tr, aaPos1, aa1);
	}

	public void setTr2(Transcript tr) {
		trId2 = tr.getId();
		chr2 = tr.getChromosomeName();
		pos2 = aaNum2ChrPos(tr, aaPos2, aa2);
	}

	@Override
	public String toString() {
		return pdbId //
				+ "\t" + pdbChainId1 //
				+ "\t" + pdbChainId2 //
				+ "\t" + distance //
				+ "\t" + aa1 //
				+ "\t" + aaPos1 //
				+ "\t" + aa2 //
				+ "\t" + aaPos2 //
				+ "\t" + (!chr1.isEmpty() ? chr1 + ":" + pos1 : "") //
				+ "\t" + (!chr2.isEmpty() ? chr2 + ":" + pos2 : "") //
				+ "\t" + trId1 //
				+ "\t" + trId2 //
		;
	}

	/**
	 * Show genomic positions only
	 */
	public String toStringPos() {
		return "" //
				+ (chr1 != null ? "\t" + chr1 + ":" + pos1 : "") //
				+ (chr2 != null ? "\t" + chr2 + ":" + pos2 : "") //
		;
	}
}
