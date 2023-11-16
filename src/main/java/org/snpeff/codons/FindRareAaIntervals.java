package org.snpeff.codons;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

import org.snpeff.fileIterator.FastaFileIterator;
import org.snpeff.interval.Gene;
import org.snpeff.interval.Genome;
import org.snpeff.interval.RareAminoAcid;
import org.snpeff.interval.Transcript;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.GprSeq;
import org.snpeff.util.Log;

/**
 * Find intervals where rare amino acids occur
 *
 * @author pablocingolani
 */
public class FindRareAaIntervals {

	public static final double RARE_THRESHOLD = 1e-5;

	boolean verbose = false;
	double rareThreshold = RARE_THRESHOLD;
	Genome genome;
	CodonTable codonTable;
	int count[];
	int countTotal;
	boolean isInTable[];
	HashMap<String, Transcript> trById;
	HashMap<String, RareAminoAcid> rareAaByPos;

	public FindRareAaIntervals(Genome genome) {
		this.genome = genome;
		codonTable = genome.codonTable();
		count = new int['Z']; // It's all upper case
		isInTable = new boolean['Z']; // It's all upper case
		countTotal = 0;
		rareAaByPos = new HashMap<String, RareAminoAcid>();
	}

	/**
	 * Add a rare amino acid (if not already added)
	 */
	void addRareAa(Transcript tr, int start, int end) {
		int s = Math.min(start, end);
		int e = Math.max(start, end);

		String key = tr.getChromosomeName() + ":" + s + "-" + e;
		if (rareAaByPos.containsKey(key)) return; // Nothing to add

		RareAminoAcid raa = new RareAminoAcid(tr, s, e, "");
		rareAaByPos.put(key, raa);
	}

	/**
	 * Find all sites having rare amino acids
	 * @param proteinFastaFile
	 */
	public Collection<RareAminoAcid> findRareAa(String proteinFastaFile) {
		// Find all amino acids in table
		isInTable();

		// Find rare amino acids from protein sequences
		proteingFileStats(proteinFastaFile);

		// Find rare amino acids
		String rare = findRareNames();
		if (rare.isEmpty()) return new LinkedList<RareAminoAcid>(); // Nothing to do

		// Find all sites were rare amino acids are
		findRareAaSites(proteinFastaFile, rare);

		return rareAaByPos.values();
	}

	/**
	 * Find interval AA index in a transcript
	 */
	void findRareAaSites(String id, int aaIdx) {
		// Create index?
		if (trById == null) {
			trById = new HashMap<String, Transcript>();
			for (Gene gene : genome.getGenome().getGenes())
				for (Transcript tr : gene) {
					// Add trascript by transcript_id and protein_id
					trById.put(tr.getId(), tr);
					if(tr.hasProteinId()) trById.put(tr.getProteinId(), tr);
				}
		}

		// Find transcript
		Transcript tr = trById.get(id);
		if (tr == null) {
			Log.warning(ErrorWarningType.WARNING_RARE_AA_POSSITION_NOT_FOUND, "Cannot find transcript '" + id + "'");
			return;
		}
		if( verbose ) Log.info("Found rare amino acid transcript ID '" + tr.getId() + "'" + ( tr.hasProteinId() ? ", protein ID '" + tr.getProteinId() + "'" : ""));

		// Create markers. There might be more than one interval since an intron can be in the middle of the codon.
		int cds2pos[] = tr.baseNumberCds2Pos();
		int pos = 0, posPrev = 0, start = -1;
		int step = tr.isStrandPlus() ? 1 : -1;
		for (int cds = aaIdx * 3; cds < (aaIdx + 1) * 3; cds++) {
			pos = cds2pos[cds];
			if (start < 0) start = pos;
			else if (pos != posPrev + step) {
				// Non-contiguous: Create a new marker and add it to the list
				addRareAa(tr, start, pos);
				start = -1;
			}

			posPrev = pos;
		}

		if (start >= 0) addRareAa(tr, start, pos);
	}

	/**
	 * Calculate AA frequency
	 */
	void findRareAaSites(String proteinFastaFile, String rareAa) {
		char rareAaChr[] = rareAa.toCharArray();
		FastaFileIterator ffi = new FastaFileIterator(proteinFastaFile);

		// For every protein sequence
		for (String seq : ffi) {
			// For every rare AA
			for (char aa : rareAaChr) {
				int aaIdx = seq.indexOf(aa);
				if (aaIdx >= 0) { // Has this protein sequence any rare AA?
					String fastaId = ffi.getIdFromFastaHeader();  // This could be transcript_id or protein_id
					findRareAaSites(fastaId, aaIdx);
				}
			}
		}
	}

	/**
	 * A string containing all rare amino acids
	 * @return
	 */
	String findRareNames() {
		StringBuilder rare = new StringBuilder();
		for (int i = 0; i < count.length; i++) {
			double p = ((double) count[i]) / countTotal;
			if ((count[i] > 0) && ((p < rareThreshold) || !isInTable[i])) rare.append((char) i);
		}
		return rare.toString();
	}

	public double getRareThreshold() {
		return rareThreshold;
	}

	/**
	 * Calculate all AA in a codon table
	 */
	void isInTable() {
		for (char c1 : GprSeq.BASES)
			for (char c2 : GprSeq.BASES)
				for (char c3 : GprSeq.BASES) {
					String codon = "" + c1 + c2 + c3;
					String aa = codonTable.aa(codon);
					isInTable[aa.toUpperCase().charAt(0)] = true;
				}
	}

	/**
	 * Calculate AA frequency
	 * @param proteinFastaFile
	 */
	void proteingFileStats(String proteinFastaFile) {
		FastaFileIterator ffi = new FastaFileIterator(proteinFastaFile);
		for (String seq : ffi) {
			// For each protein sequence, count amino acids
			for (char c : seq.toUpperCase().toCharArray()) {
				// Ignore non-letters (e.g. '*' = stop codon)
				// Ignore unknown amino acids
				if (Character.isLetter(c) && (c != 'X')) {
					count[c]++;
					countTotal++;
				}
			}
		}
	}

	public void setRareThreshold(double rareThreshold) {
		this.rareThreshold = rareThreshold;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < count.length; i++) {
			double p = ((double) count[i]) / countTotal;
			if (count[i] > 0) sb.append(String.format("\t%s\t%d\t%.2e\t%b\n", (char) i, count[i], p, isInTable[i]));
		}

		return sb.toString();
	}
}
