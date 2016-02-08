package net.sf.samtools.tabix;

import java.util.Iterator;

import org.snpeff.util.Gpr;

/**
 * Iterate on a result from TabixReader.query()
 */
public class TabixIterator implements Iterator<String>, Iterable<String> {

	boolean debug;
	TabixReader tabixReader;
	private int i;
	private int tid, beg, end;
	private TPair64[] offsets;
	private long currentOffset;
	private boolean isEof;
	private String next = null;
	private boolean showHeader = false; // By default, do not return header lines
	private boolean readBlock = false; // Read the whole compression block

	public TabixIterator(TabixReader tabixReader, int _tid, int _beg, int _end, TPair64[] _off) {
		i = -1;
		currentOffset = 0;
		isEof = false;
		offsets = _off;
		tid = _tid;
		beg = _beg;
		end = _end;
		this.tabixReader = tabixReader;
	}

	@Override
	public boolean hasNext() {
		if (next == null) next = readNext(); // Try reading next item.
		return (next != null);
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}

	@Override
	public String next() {
		if (hasNext()) {
			String ret = next;
			next = null;
			return ret;
		}
		return null;

	}

	/**
	 * Read next line
	 */
	private String readNext() {
		try {
			if (isEof) {
				if (debug) Gpr.debug("readNext return: EOF");
				return null;
			}

			for (;;) {
				if (currentOffset == 0 || !TPair64.less64(currentOffset, offsets[i].v)) { // then jump to the next chunk
					if (i == offsets.length - 1) {
						if (debug) Gpr.debug("readNext break: No more chunks");
						break; // no more chunks
					}
					if (i >= 0) assert(currentOffset == offsets[i].v); // Otherwise bug
					if (i < 0 || offsets[i].v != offsets[i + 1].u) { // Not adjacent chunks; then seek
						long pos = offsets[i + 1].u;
						tabixReader.seek(pos);
						currentOffset = tabixReader.getFilePointer();
						if (debug) Gpr.debug("readNext seek: " + offsets[i + 1].u + "\tcurr_off: " + currentOffset);
					}
					++i;
				}

				String s;
				if ((s = tabixReader.readLine()) != null) {
					if (debug) Gpr.debug("reding line: " + s);

					// TIntv intv;
					char[] str = s.toCharArray();
					currentOffset = tabixReader.getFilePointer();
					if (str.length == 0) {
						if (debug) Gpr.debug("readNext continue, empty line");
						continue;
					}

					// Check header
					if (str[0] == tabixReader.getmMeta()) {
						if (!showHeader) {
							if (debug) Gpr.debug("readNext continue, header line: " + s);
							continue;
						}

						if (debug) Gpr.debug("readNext return, line: " + s);
						return s;
					}

					// Any line we read is OK when reading the whole compression block
					if (readBlock) {
						if (debug) Gpr.debug("readNext return (block), line: " + s);
						return s;
					}

					// Check range
					TabixInterval latestIntv = new TabixInterval(tabixReader, s);
					if (((tid >= 0) && (latestIntv.tid != tid)) || latestIntv.beg >= end) { // Note: tid < 0 means any-chromosome (i.e. no-limits)
						// Past end coordinate, no need to proceed.
						if (debug) Gpr.debug("readNext break: Interval from file after query:" //
								+ "\n\tQuery        :\t" + "tid: " + tid + ", start: " + beg + ", end: " + end //
								+ "\n\tFile interval:\t" + latestIntv //
						);
						break;
					} else if (latestIntv.end > beg && latestIntv.beg < end) {
						if (debug) Gpr.debug("readNext return, line: " + s);
						return s; // overlap; return
					}
				} else {
					if (debug) Gpr.debug("readNext break: End of file");
					break; // end of file
				}
			}

			isEof = true;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		if (debug) Gpr.debug("readNext return, line: null");
		return null;
	}

	@Override
	public void remove() {
		throw new RuntimeException("Unimplemented!");
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setReadBlock(boolean readBlock) {
		this.readBlock = readBlock;
	}

	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("tid:" + tid + "\tbeg:" + beg + "\tend:" + end);
		for (TPair64 tp : offsets)
			sb.append("\t" + tp + "\n");
		return sb.toString();
	}

	public String toStringBlocks() {
		StringBuilder sb = new StringBuilder();
		for (TPair64 tp : offsets)
			sb.append(tp.toStringRor16() + "\n");
		return sb.toString();
	}
}
