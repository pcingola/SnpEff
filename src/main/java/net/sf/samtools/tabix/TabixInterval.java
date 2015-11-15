package net.sf.samtools.tabix;

public class TabixInterval {

	int tid, beg, end;

	public TabixInterval(TabixReader tabixReader, String s) {
		int col = 0, end = 0, beg = 0;
		while ((end = s.indexOf('\t', beg)) >= 0 || end == -1) {
			++col;
			if (col == tabixReader.getmSc()) {
				tid = tabixReader.chr2tid(s.substring(beg, end));
			} else if (col == tabixReader.getmBc()) {
				this.beg = this.end = Integer.parseInt(s.substring(beg, end == -1 ? s.length() : end));
				if ((tabixReader.getmPreset() & 0x10000) != 0) ++this.end;
				else--this.beg;
				if (this.beg < 0) this.beg = 0;
				if (this.end < 1) this.end = 1;
			} else {
				// SAM supports are not tested yet
				if ((tabixReader.getmPreset() & 0xffff) == 0) { // generic
					if (col == tabixReader.getmEc()) this.end = Integer.parseInt(s.substring(beg, end));
				} else if ((tabixReader.getmPreset() & 0xffff) == 1) { // SAM
					if (col == 6) { // CIGAR
						int l = 0, i, j;
						String cigar = s.substring(beg, end);
						for (i = j = 0; i < cigar.length(); ++i) {
							if (cigar.charAt(i) > '9') {
								int op = cigar.charAt(i);
								if (op == 'M' || op == 'D' || op == 'N') l += Integer.parseInt(cigar.substring(j, i));
							}
						}
						this.end = this.beg + l;
					}
				} else if ((tabixReader.getmPreset() & 0xffff) == 2) {
					// VCF
					String alt;
					alt = end >= 0 ? s.substring(beg, end) : s.substring(beg);
					if (col == 4) { // REF
						if (alt.length() > 0) this.end = this.beg + alt.length();
					} else if (col == 8) { // INFO
						int e_off = -1, i = alt.indexOf("END=");
						if (i == 0) e_off = 4;
						else if (i > 0) {
							i = alt.indexOf(";END=");
							if (i >= 0) e_off = i + 5;
						}
						if (e_off > 0) {
							i = alt.indexOf(";", e_off);
							this.end = Integer.parseInt(i > e_off ? alt.substring(e_off, i) : alt.substring(e_off));
						}
					}
				}
			}
			if (end == -1) break;
			beg = end + 1;
		}
	}

	@Override
	public String toString() {
		return "tid: " + tid //
				+ ", start: " + beg //
				+ ", end: " + end //
				;
	}
}
