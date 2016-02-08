package org.snpeff.sam;

import java.util.HashMap;

import org.snpeff.util.Gpr;
import org.snpeff.util.GprSeq;

/**
 * An entry in a SAM file
 * References: http://samtools.sourceforge.net/SAM-1.3.pdf
 * 
 * @author pcingola
 */
public class SamEntry {

	String line;
	String qname; // Query name (i.e. read ID)
	int flag; // Bitwise FLAGs
	String rname; // Reference name (i.e. chromosome where read maps). '*' if not mapped
	int pos; // 1-based leftmost mapping POSition (0 if not mapped)
	int mapq; // Mapping quality 
	String cigar; // Alignment description in CIGAR format
	String rnext; // Reference sequence name of the NEXT fragment in the template (This field is set as ‘*’ when the information is unavailable, and set as ‘=’ if RNEXT is identical RNAME)
	int pnext; // Position of the NEXT fragment in the template. Set as 0 when the information is unavailable.
	int tlen; // Signed observed Template LENgth.
	String seq; // Sequence 
	String qual; // ASCII of base QUALity plus 33 (same as the quality string in the Sanger FASTQ format)
	String tags[]; // All optional fields are presented in the TAG:TYPE:VALUE format where TAG is a two-character string that matches /[A-Za-z][A-Za-z0-9]/, TYPE is a casesensitive single letter which defines the format of VALUE:

	HashMap<String, String> tagsByName;

	/**
	 * Get an ID from a SAM line
	 */
	public static String samLine2Id(String line) {
		return GprSeq.readId(line.split("\t")[0]);
	}

	/**
	 * Create an entry give a line from a file
	 */
	public SamEntry(String line) {
		this.line = line;
		String recs[] = line.split("\t");
		qname = recs[0];
		flag = Gpr.parseIntSafe(recs[1]);
		rname = recs[2];
		pos = Gpr.parseIntSafe(recs[3]);
		mapq = Gpr.parseIntSafe(recs[4]);
		cigar = recs[5];
		pnext = Gpr.parseIntSafe(recs[6]);
		tlen = Gpr.parseIntSafe(recs[7]);
		rnext = recs[8];
		seq = recs[9];
		qual = recs[10];

		// Copy all other tags
		tags = new String[recs.length - 11];
		for (int i = 0, j = 11; j < recs.length; i++, j++)
			tags[i] = recs[j];
	}

	/**
	 * Does this entry have a tag?
	 */
	public String findTag(String tagName) {
		if (tagsByName == null) parseTags();
		return tagsByName.get(tagName);
	}

	public String getCigar() {
		return cigar;
	}

	public int getFlag() {
		return flag;
	}

	public String getId() {
		return GprSeq.readId(qname);
	}

	public String getLine() {
		return line;
	}

	public int getMapq() {
		return mapq;
	}

	public int getPnext() {
		return pnext;
	}

	public int getPos() {
		return pos;
	}

	public String getQname() {
		return qname;
	}

	public String getQual() {
		return qual;
	}

	public String getRname() {
		return rname;
	}

	public String getRnext() {
		return rnext;
	}

	public String getSeq() {
		return seq;
	}

	public int getTlen() {
		return tlen;
	}

	/**
	 * Some aligners just use '255' in the mapping quality field (bowtie)
	 */
	public boolean hasMapq() {
		return mapq != 255;
	}

	/**
	 * PCR or optical duplicate
	 **/
	public boolean isDuplicate() {
		return (flag & 0x400) != 0;
	}

	/**
	 * The first fragment in the template
	 **/
	public boolean isFirstFragment() {
		return (flag & 0x040) != 0;
	}

	/**
	 * The last fragment in the template
	 **/
	public boolean isLastFragment() {
		return (flag & 0x080) != 0;
	}

	/**
	 * Is this entry mapped to the genome?
	 */
	public boolean isMapped() {
		return !isUnmapped();
	}

	/**
	 * Template having multiple fragments in sequencing
	 **/
	public boolean isMultipleFragments() {
		return (flag & 0x01) != 0;
	}

	/**
	 * Is this read mapped to multiple genomic locations?
	 */
	public boolean isMultipleHits() {
		// These tags are produced by BWA

		// XA: Alternative hits; format: (chr,pos,CIGAR,NM;)*
		String xa = findTag("XA");
		if (xa != null) return true; // It has 'XA' tag? => It is hitting multiple regions

		// XT: Type: Unique/Repeat/N/Mate-sw
		String xt = findTag("XT");
		if ((xt != null) && (xt.equals("R"))) return true; // It is hitting a 'repeat' region? => Multiple hits

		// X0: Number of best hits
		String x0 = findTag("X0");
		if ((x0 != null) && (!x0.equals("1"))) return true; // More than one best hit? => Multiple hits

		return false;
	}

	/**
	 * SEQ of the next fragment in the template being reversed
	 **/
	public boolean isNextReverseWc() {
		return (flag & 0x020) != 0;
	}

	/**
	 * Next fragment in the template unmapped
	 **/
	public boolean isNextUnmapped() {
		return (flag & 0x08) != 0;
	}

	/**
	 * Not passing quality controls
	 **/
	public boolean isNotQualityControl() {
		return (flag & 0x200) != 0;
	}

	/**
	 * Each fragment properly aligned according to the aligner
	 **/
	public boolean isProperlyAligned() {
		return (flag & 0x02) != 0;
	}

	/**
	 * SEQ being reverse complemented
	 **/
	public boolean isReverseWc() {
		return (flag & 0x010) != 0;
	}

	/**
	 * Secondary alignment
	 **/
	public boolean isSecondaryAlignment() {
		return (flag & 0x100) != 0;
	}

	/**
	 * Is this read mapped to only one genomic locations?
	 * @return
	 */
	public boolean isUniqueHit() {
		return !isMultipleHits();
	}

	/**
	 * Fragment unmapped
	 **/
	public boolean isUnmapped() {
		return (flag & 0x04) != 0;
	}

	/**
	 * Parse tags
	 */
	void parseTags() {
		tagsByName = new HashMap<String, String>();
		for (String tag : tags) {
			String nv[] = tag.split(":");
			tagsByName.put(nv[0], nv[2]);
		}
	}

	/**
	 * Replace a sequence
	 * WARNING: Doing this might invalidate the CIGAR field
	 * @param newSeq
	 */
	public void replaceSeq(String newSeq) {
		if (newSeq.length() != seq.length()) throw new RuntimeException("Cannot replace by a sequence with different length: Operation not supported!");
		seq = newSeq;

		// Invalidate some tags
		for (int i = 0; i < tags.length; i++) {
			if (tags[i].startsWith("MD:Z:")) tags[i] = null;
			else if (tags[i].startsWith("NM:i:")) tags[i] = null;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer(qname + "\t" + flag //
				+ "\t" + rname //
				+ "\t" + pos //
				+ "\t" + mapq //
				+ "\t" + cigar //
				+ "\t" + rnext //
				+ "\t" + pnext //
				+ "\t" + tlen //
				+ "\t" + seq //
				+ "\t" + qual);

		// Apend tags
		for (int i = 0; i < tags.length; i++)
			if (tags[i] != null) sb.append("\t" + tags[i]);

		return sb.toString();
	}
}
