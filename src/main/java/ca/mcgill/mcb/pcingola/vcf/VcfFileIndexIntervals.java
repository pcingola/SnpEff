package ca.mcgill.mcb.pcingola.vcf;

/**
 * DEPRECATED! Use FileIndexChrPos instead
 * Note: This class was slow and had problems with VCF files over 1TB so it was replaced by FileIndexChrPos
 * 
 * 
 * Extract intervals from an uncompressed VCF file using indexing.
 * 
 * WARNING: It is assumed that the file is ordered by position (chromosome order does not matter)
 * 
 * 
 * @author pcingola
 */
public class VcfFileIndexIntervals {

	//	/**
	//	 * A part of a file
	//	 * @author pcingola
	//	 *
	//	 */
	//	class FileRegion {
	//		long start, end;
	//		String lineStart, lineEnd;
	//	}
	//
	//	/**
	//	 * A line and the position on the file where it begins
	//	 * @author pcingola
	//	 *
	//	 */
	//	class LineAndPos {
	//		String line;
	//		long position;
	//
	//		@Override
	//		public String toString() {
	//			String str = "";
	//			if (line != null) {
	//				if (line.length() > 50) str = line.substring(0, 49) + "...";
	//				else str = line;
	//			}
	//			return position + "\t" + str;
	//		}
	//	}
	//
	//	public static final int POS_OFFSET = 1; // VCF files are one-based
	//	private static final long PAGE_SIZE = Integer.MAX_VALUE;
	//	private static final int BUFF_SIZE = 1024 * 1024;
	//
	//	boolean verbose = false;
	//	boolean debug = false;
	//	String fileName;
	//	long size = 0;
	//	FileChannel fileChannel;
	//	ArrayList<MappedByteBuffer> maps = new ArrayList<MappedByteBuffer>();
	//	ArrayList<ByteBuffer> buffers = new ArrayList<ByteBuffer>();
	//	HashMap<String, FileRegion> fileRegions = new HashMap<String, VcfFileIndexIntervals.FileRegion>(); // Store file regions by chromosome
	//
	//	public VcfFileIndexIntervals(String fileName) {
	//		this.fileName = fileName;
	//	}
	//
	//	/**
	//	 * Get chromosome info
	//	 * @param line
	//	 * @return
	//	 */
	//	String chromo(String line) {
	//		if (line.startsWith("#")) return null;
	//		return line.split("\\t")[0];
	//	}
	//
	//	public void close() {
	//		try {
	//			fileChannel.close();
	//			fileChannel = null;
	//			maps = null;
	//			buffers = null;
	//		} catch (IOException e) {
	//			System.err.println("I/O problem while closing file '" + fileName + "'");
	//			throw new RuntimeException(e);
	//		}
	//	}
	//
	//	/**
	//	 * Dump a region of the file to STDOUT
	//	 * @param start
	//	 * @param end
	//	 */
	//	void dump(long start, long end) {
	//		int pageStart = (int) (start / PAGE_SIZE);
	//		int indexStart = (int) (start % PAGE_SIZE);
	//		int pageEnd = (int) (end / PAGE_SIZE);
	//		int indexEnd = (int) (end % PAGE_SIZE);
	//
	//		byte buff[] = new byte[BUFF_SIZE];
	//		for (int page = pageStart; page <= pageEnd; page++) {
	//			long max = PAGE_SIZE;
	//			if (page == pageEnd) max = indexEnd;
	//
	//			ByteBuffer bf = buffers.get(page);
	//			bf.position(indexStart);
	//
	//			for (long pos = indexStart; pos < max; pos += buff.length) {
	//				int len = (int) Math.min(max - pos, buff.length);
	//				bf.get(buff, 0, len);
	//
	//				String out = new String(buff, 0, len);
	//				System.out.print(out);
	//			}
	//			indexStart = 0;
	//		}
	//	}
	//
	//	/**
	//	 * Dump all lines in the interval chr:posStart-posEnd
	//	 * 
	//	 * @param chr
	//	 * @param posStart
	//	 * @param posEnd
	//	 */
	//	public void dump(String chr, int posStart, int posEnd) {
	//		long fileStart = find(chr, posStart, false);
	//		long fileEnd = find(chr, posEnd, true);
	//
	//		dump(fileStart, fileEnd);
	//	}
	//
	//	/**
	//	 * Find the position in the file for the first character of the first line whose genomic position is less or equal than 'chrPos'
	//	 * @param chrPos
	//	 * @param start
	//	 * @param lineStart
	//	 * @param end
	//	 * @param lineEnd
	//	 * @return
	//	 */
	//	long find(int chrPos, long start, String lineStart, long end, String lineEnd, boolean lessEq) {
	//		int posStart = pos(lineStart);
	//		if (chrPos == posStart) return start;
	//
	//		int posEnd = pos(lineEnd);
	//		if (chrPos == posEnd) return end + lineEnd.length() + 1;
	//
	//		if (debug) Gpr.debug("Find:\t" + chrPos + "\t[" + posStart + ", " + posEnd + "]\tFile: [" + start + " , " + end + "]\tsize: " + (end - start));
	//
	//		// Break conditions
	//		if (lessEq) {
	//			if (posStart >= chrPos) return start;
	//			if ((start + lineStart.length() + 1) >= end) return start;
	//		} else {
	//			if (posEnd <= chrPos) return end + lineEnd.length() + 1;
	//			if ((start + lineStart.length() + 1) >= end) return end + lineEnd.length() + 1;
	//		}
	//
	//		// Sanity check
	//		if (posStart >= posEnd) throw new RuntimeException("This should never happen! Is the file sorted by position?");
	//
	//		long mid = (start + end) / 2;
	//		String lineMid = getLine(mid).line;
	//		long posMid = pos(lineMid);
	//
	//		if (lessEq) {
	//			if (chrPos <= posMid) return find(chrPos, start, lineStart, mid, lineMid, lessEq);
	//		} else {
	//			if (chrPos < posMid) return find(chrPos, start, lineStart, mid, lineMid, lessEq);
	//		}
	//		return find(chrPos, mid, lineMid, end, lineEnd, lessEq);
	//	}
	//
	//	/**
	//	 * Find the position in the file for the first character of the first line equal or less than a specific chr:pos
	//	 * @param chr
	//	 * @param pos
	//	 * @return
	//	 */
	//	long find(String chr, int pos, boolean lessEq) {
	//		chr = Chromosome.simpleName(chr);
	//		FileRegion fr = fileRegions.get(chr);
	//		if (fr == null) throw new RuntimeException("No such chromosome: '" + chr + "'");
	//		long posFound = find(pos, fr.start, fr.lineStart, fr.end, fr.lineEnd, lessEq);
	//		return getLine(posFound).position;
	//	}
	//
	//	public byte get(long bytePosition) {
	//		int page = (int) (bytePosition / PAGE_SIZE);
	//		int index = (int) (bytePosition % PAGE_SIZE);
	//		byte b = buffers.get(page).get(index);
	//		if (debug) Gpr.debug("page: " + page + "\t" + index + "\t" + b + "\t'" + ((char) b) + "'");
	//		return b;
	//	}
	//
	//	/**
	//	 * Available chromosomes
	//	 * @return
	//	 */
	//	public Set<String> getChromos() {
	//		return fileRegions.keySet();
	//	}
	//
	//	/**
	//	 * Get position where 'chr' ends
	//	 * @param chr
	//	 * @return -1 if 'chr' is not in the index
	//	 */
	//	public long getEnd(String chr) {
	//		chr = Chromosome.simpleName(chr);
	//		FileRegion fr = fileRegions.get(chr);
	//		if (fr == null) return -1;
	//		return fr.end;
	//	}
	//
	//	/**
	//	 * Get file region for a given chrosmome
	//	 * @param chr
	//	 * @return
	//	 */
	//	FileRegion getFileRegion(String chr) {
	//		chr = Chromosome.simpleName(chr);
	//		FileRegion fr = fileRegions.get(chr);
	//		if (fr == null) {
	//			fr = new FileRegion();
	//			fileRegions.put(chr, fr);
	//		}
	//		return fr;
	//	}
	//
	//	/**
	//	 * Get the line where 'pos' hits
	//	 * 
	//	 * TODO: This is really slow for huge files and huge lines. I should optimize this.
	//	 * 
	//	 * @param pos
	//	 * @return A string with the line that 'pos' hits, null if it's out of boundaries
	//	 */
	//	public LineAndPos getLine(long pos) {
	//		long size = size();
	//		if ((pos >= size) || (pos < 0)) return null;
	//
	//		LineAndPos linePos = new LineAndPos();
	//		StringBuffer sb = new StringBuffer();
	//
	//		// Get bytes before 'pos'
	//		long position;
	//		for (position = pos - 1; position >= 0; position--) {
	//			byte b = get(position);
	//			if (b == '\n') break;
	//			sb.insert(0, (char) b);
	//		}
	//		linePos.position = position + 1;
	//
	//		// Get bytes after 'pos'
	//		for (position = pos; position < size; position++) {
	//			byte b = get(position);
	//			if (b == '\n') break;
	//			sb.append((char) b);
	//		}
	//		linePos.line = sb.toString();
	//
	//		if (debug) Gpr.debug("Line & Position: " + linePos);
	//		return linePos;
	//	}
	//
	//	/**
	//	 * Get position where 'chr' starts
	//	 * @param chr
	//	 * @return -1 if 'chr' is not in the index
	//	 */
	//	public long getStart(String chr) {
	//		chr = Chromosome.simpleName(chr);
	//		FileRegion fr = fileRegions.get(chr);
	//		if (fr == null) return -1;
	//		return fr.start;
	//	}
	//
	//	/**
	//	 * Index chromosomes in the whole file 
	//	 */
	//	public void index() {
	//		// Last line (minus '\n' character, minus one)
	//		long end = size() - 1;
	//		String lineEnd = getLine(end).line;
	//		String chrEnd = chromo(lineEnd);
	//
	//		// Add fileRegion.end for last chromsome in the file
	//		FileRegion fr = getFileRegion(chrEnd);
	//		fr.end = end;
	//		fr.lineEnd = lineEnd;
	//		if (verbose) System.err.println("\tindex:\t" + chrEnd + "\t" + end);
	//
	//		// Find first non-comment line
	//		long start = 0;
	//		String lineStart = "";
	//		for (start = 0; start < size; start += lineStart.length() + 1) {
	//			lineStart = getLine(start).line;
	//			if (chromo(lineStart) != null) break;
	//		}
	//
	//		String chrStart = chromo(lineStart);
	//
	//		// Add fileRegion.start for first chromsome in the file
	//		fr = getFileRegion(chrStart);
	//		fr.start = start;
	//		fr.lineStart = lineStart;
	//		if (verbose) System.err.println("\tindex:\t" + chrStart + "\t" + start);
	//
	//		// Index the rest of the file
	//		indexChromos(start, lineStart, end, lineEnd);
	//	}
	//
	//	/**
	//	 * Index chromosomes in a region of a file
	//	 * @param start
	//	 * @param lineStart
	//	 * @param end
	//	 * @param lineEnd
	//	 */
	//	void indexChromos(long start, String lineStart, long end, String lineEnd) {
	//		if (debug) Gpr.debug("Index:"//
	//				+ "\n\t" + start + "(" + (((double) start) / size()) + ") :\t" + s(lineStart) //
	//				+ "\n\t" + end + "(" + (((double) end) / size()) + ") :\t" + s(lineEnd));
	//
	//		if (start > end) throw new RuntimeException("This should never happen! Start: " + start + "\tEnd: " + end);
	//
	//		String chrStart = chromo(lineStart);
	//		String chrEnd = chromo(lineEnd);
	//
	//		if (chrStart.equals(chrEnd)) {
	//			if (debug) Gpr.debug("Chromo:\tlineStart: " + chrStart + "\tlineEnd: " + chrEnd + "\t==> Back!");
	//			return;
	//		}
	//		if (debug) Gpr.debug("Chromo:\tlineStart: " + chrStart + "\tlineEnd: " + chrEnd);
	//
	//		if ((start + lineStart.length() + 1) >= end) {
	//			if (verbose) System.err.println("\tStart + 1 line = End\t==>Done!\t" + chrStart + " / " + chrEnd + "\t" + start + " / " + end);
	//
	//			// Add index where chromosome starts
	//			getFileRegion(chrEnd).start = getLine(end).position;
	//			getFileRegion(chrEnd).lineStart = lineEnd;
	//
	//			// Add index where chromosome ends
	//			getFileRegion(chrStart).end = getLine(start).position;
	//			getFileRegion(chrStart).lineEnd = lineStart;
	//			return;
	//		}
	//
	//		long mid = (start + end) / 2;
	//		String lineMid = getLine(mid).line;
	//		if (debug) Gpr.debug("Mid: " + mid + "\t" + s(lineMid));
	//
	//		if (debug) Gpr.debug("First half recustion:");
	//		indexChromos(start, lineStart, mid, lineMid);
	//
	//		if (debug) Gpr.debug("Second half recustion:");
	//		indexChromos(mid, lineMid, end, lineEnd);
	//	}
	//
	//	void init(FileChannel channel) throws IOException {
	//	}
	//
	//	/**
	//	 * Open file and initiate mappings
	//	 */
	//	public void open() {
	//		try {
	//			fileChannel = new FileInputStream(fileName).getChannel();
	//			size = fileChannel.size();
	//
	//			// Create all mapped files required
	//			long start = 0, length = 0;
	//			for (int index = 0; start + length < fileChannel.size(); index++) {
	//				if ((fileChannel.size() / PAGE_SIZE) == index) length = (fileChannel.size() - index * PAGE_SIZE);
	//				else length = PAGE_SIZE;
	//				start = index * PAGE_SIZE;
	//
	//				// Create map and add it to the array
	//				MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, start, length);
	//				maps.add(index, map);
	//
	//				ByteBuffer buff = map.asReadOnlyBuffer();
	//				buffers.add(index, buff);
	//			}
	//
	//		} catch (FileNotFoundException e) {
	//			System.err.println("File not found '" + fileName + "'");
	//			throw new RuntimeException(e);
	//		} catch (IOException e) {
	//			System.err.println("I/O problem while mapping file '" + fileName + "'");
	//			throw new RuntimeException(e);
	//		}
	//	}
	//
	//	/**
	//	 * The position argument of a line, or zero if not found
	//	 * @param line
	//	 * @return
	//	 */
	//	int pos(String line) {
	//		if (line.startsWith("#")) return 0; // In VCF, positions are one-based, so zero denotes an error
	//		return Gpr.parseIntSafe(line.split("\\t")[1]) - POS_OFFSET;
	//	}
	//
	//	String s(String s) {
	//		if (s == null) return "null";
	//		return s.length() <= 50 ? s : s.substring(0, 50) + "...";
	//	}
	//
	//	public void setDebug(boolean debug) {
	//		this.debug = debug;
	//	}
	//
	//	public void setVerbose(boolean verbose) {
	//		this.verbose = verbose;
	//	}
	//
	//	/**
	//	 * File size
	//	 * @return
	//	 */
	//	public long size() {
	//		return size;
	//	}
}
