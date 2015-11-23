package ca.mcgill.mcb.pcingola.pdb;

import java.util.ArrayList;
import java.util.List;

import org.biojava.bio.structure.AminoAcid;

import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.util.Gpr;

public class DistanceResult {

	// Pdb information
	public String pdbId;
	public String pdbChainId;
	public int aaPos1, aaPos2;
	public char aa1, aa2;
	public double distance;

	// Genomic information
	public String transcriptId;
	public String chr1, chr2;
	public int pos1, pos2;

	public DistanceResult() {
		pdbId = pdbChainId = transcriptId = chr1 = chr2 = "";
		aaPos1 = aaPos2 = pos1 = pos2 = -1;
		distance = -1;
		aa1 = aa2 = '.';
	}

	public DistanceResult(AminoAcid aa1, AminoAcid aa2, double distance) {
		this();
		setAa1(aa1);
		setAa2(aa2);
		this.distance = distance;
	}

	public DistanceResult(String line) {
		this();

		// Parse line
		String fields[] = line.split("\t");
		int n = 0;
		pdbId = fields[n++];
		pdbChainId = fields[n++];
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
				//				chr1Num = Chromosome.number(chr1);
			}
		}

		if (fields.length > n) {
			String chrPos2 = fields[n++];
			if (!chrPos2.isEmpty()) {
				String f[] = chrPos2.split(":");
				chr2 = f[0];
				pos2 = Gpr.parseIntSafe(f[1]);
				//				chr2Num = Chromosome.number(chr1);
			}
		}

		if (fields.length > n) transcriptId = fields[n++];
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

	/**
	 * Return amino acid pair (sorted)
	 */
	public String getAaPair() {
		return aa1 <= aa2 ? aa1 + "-" + aa2 : aa2 + "-" + aa1;
	}

	/**
	 * Return amino acid pair (sorted) + all combinations of annotations
	 */
	public List<String> getAaPairAnnotations() {
		new ArrayList<>();

		throw new RuntimeException("");
		//		Arrays.stream(annotations1.split(";")) //
		//				.forEach( //
		//						ann1 -> Arrays.stream(annotations2.split(";")) //
		//								.forEach( //
		//										ann2 -> anns.add(aaPair + "\t" //
		//												+ (reversed ? ann2 + "\t" + ann1 : ann1 + "\t" + ann2) //
		//		) //
		//		) //
		//		);
		//
		//		return anns;
	}

	/**
	 * Return transcript ID without sub-version
	 * E.g.  If trId is 'NM_176795.3'  => return 'NM_176795'
	 */
	public String getTrIdNoSub() {
		int n = transcriptId.indexOf('.');
		return n > 0 ? transcriptId.substring(0, n) : transcriptId;
	}

	public void setAa1(AminoAcid aa) {
		pdbId = aa.getChain().getParent().getPDBCode();
		pdbChainId = aa.getChainId();
		aaPos1 = aa.getResidueNumber().getSeqNum() - 1;
		aa1 = aa.getChemComp().getOne_letter_code().charAt(0);
	}

	public void setAa2(AminoAcid aa) {
		pdbId = aa.getChain().getParent().getPDBCode();
		pdbChainId = aa.getChainId();
		aaPos2 = aa.getResidueNumber().getSeqNum() - 1;
		aa2 = aa.getChemComp().getOne_letter_code().charAt(0);
	}

	@Override
	public String toString() {
		return pdbId //
				+ "\t" + pdbChainId //
				+ "\t" + distance //
				+ "\t" + aa1 //
				+ "\t" + aaPos1 //
				+ "\t" + aa2 //
				+ "\t" + aaPos2 //
				+ "\t" + (!chr1.isEmpty() ? chr1 + ":" + pos1 : "") //
				+ "\t" + (!chr2.isEmpty() ? chr2 + ":" + pos2 : "") //
				+ "\t" + transcriptId //
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
