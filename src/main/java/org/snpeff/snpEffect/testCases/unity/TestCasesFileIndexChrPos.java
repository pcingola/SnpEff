package org.snpeff.snpEffect.testCases.unity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.junit.Test;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.util.Gpr;
import org.snpeff.vcf.FileIndexChrPos;
import org.snpeff.vcf.FileIndexChrPos.LineAndPos;
import org.snpeff.vcf.VcfEntry;

import junit.framework.Assert;

/**
 * Test cases for file index (chr:pos index on files)
 *
 * @author pcingola
 */
public class TestCasesFileIndexChrPos {

	boolean verbose = false;
	boolean debug = false;

	void readLinesCheck(String vcf, int numTests) {
		Random random = new Random(20130218);

		if (verbose) System.out.println("Opening file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.open();

		// Get file size
		long size = (new File(vcf)).length();
		for (int i = 1; i < numTests; i++) {
			long randPos = random.nextInt((int) size);

			// Compare methods
			LineAndPos lineSlow = idx.getLineSlow(randPos); // This method we trust
			LineAndPos line = idx.getLine(randPos); // This method we test

			// Check and show differences
			if (!line.line.equals(lineSlow.line)) {
				System.err.println("Length: " + lineSlow.line.length() + "\t" + line.line.length());
				System.err.println("Lines:\n\t" + lineSlow.line + "\n\t" + line.line);
				int shown = 0;
				for (int j = 0; j < line.line.length(); j++) {
					System.err.print(j + "\t'" + lineSlow.line.charAt(j) + "'\t'" + line.line.charAt(j) + "'");
					if (lineSlow.line.charAt(j) != line.line.charAt(j)) {
						System.err.print("\t<---");
						if (shown++ > 20) break;
					}
					System.err.println("");
				}
			}

			Assert.assertEquals(lineSlow.line, line.line);
			Assert.assertEquals(lineSlow.position, line.position);

			Gpr.showMark(i, 1);
		}
		System.err.println("");
	}

	/**
	 * Test getting random lines from a file
	 * @throws IOException
	 */
	@Test
	public void test_00_long_file() throws IOException {
		Gpr.debug("Test");
		readLinesCheck("tests/unity/fileIndexChrPos/test.chr1.vcf", 1000);
	}

	/**
	 * Test getting random lines from a file
	 * @throws IOException
	 */
	@Test
	public void test_00_short_file() throws IOException {
		Gpr.debug("Test");
		readLinesCheck("tests/unity/fileIndexChrPos/test_filter_transcripts_001.ori.vcf", 1000);
	}

	/**
	 * Test : Find beginning of a chromosome
	 * @throws IOException
	 */
	@Test
	public void test_01() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.open();
		idx.index();

		long pos = idx.getStart("1");
		if (verbose) System.out.println("\tChr 1 start: " + pos);
		Assert.assertEquals(82703, pos);

		idx.close();
	}

	/**
	 * Test : Find a line
	 * @throws IOException
	 */
	@Test
	public void test_02() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";
		String line = "1	861275	.	C	T	764.18	PASS	AC=1;AF=0.00061;AN=1644;DS;set=Intersection";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.open();
		idx.index();

		long pos = idx.getStart("1");
		LineAndPos lp = idx.getLine(pos);
		if (verbose) System.out.println("\tChr 1 start: " + pos + "\tLine: '" + lp.line + "'");
		Assert.assertEquals(line, lp.line);
		idx.close();
	}

	/**
	 * Test : Find a chr:pos
	 * @throws IOException
	 */
	@Test
	public void test_03() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.open();
		idx.index();

		int chrPos = 861275 - 1; // Zero based coordinate of position in first VCF line

		// Beginning of line
		long pos = idx.find("1", chrPos, true);
		Assert.assertEquals(82703, pos);

		// End of line
		pos = idx.find("1", chrPos, false);
		Assert.assertEquals(82774, pos);

		idx.close();
	}

	/**
	 * Test : Find a chr:pos
	 * @throws IOException
	 */
	@Test
	public void test_04() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// We'll try to find this chr:pos = 1:1019717
		int chrPos = 1019717 - 1; // Zero based coordinate of VCF line

		// Find chr:pos
		long pos = idx.find("1", chrPos, true);
		LineAndPos lp = idx.getLine(pos);
		int chrPosLp = idx.pos(lp.line);
		Assert.assertEquals(chrPos, chrPosLp);
		Assert.assertEquals(129869, pos);

		idx.close();
	}

	/**
	 * Test : Find a chr:pos that does not exists
	 * @throws IOException
	 */
	@Test
	public void test_05() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// We'll try to find this chr:pos = 1:1019716 (the coordinate that is in the VCF file is 1:1019717)
		int chrPosReal = 1019717; // Zero based coordinate of VCF line
		int chrPos = chrPosReal - 1; // Zero based coordinate of VCF line

		// Find chr:pos
		long pos = idx.find("1", chrPos, false);
		LineAndPos lp = idx.getLine(pos);
		int chrPosLp = idx.pos(lp.line);
		Assert.assertEquals(chrPosReal, chrPosLp);

		idx.close();
	}

	/**
	 * Test : Find a chr:pos that does not exists
	 * @throws IOException
	 */
	@Test
	public void test_06() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// We'll try to find this chr:pos = 1:1019716 (the coordinate that is in the VCF file is 1:1019717)
		int chrPosReal = 1019717; // Zero based coordinate of VCF line
		int chrPos = chrPosReal - 1; // Zero based coordinate of VCF line

		// Find chr:pos
		long pos = idx.find("1", chrPos, true);
		LineAndPos lp = idx.getLine(pos);
		int chrPosLp = idx.pos(lp.line);
		Assert.assertEquals(chrPos, chrPosLp); // We expect to find the next coordinate in VCF file (zero-based)

		idx.close();
	}

	/**
	 * Test : Find a chr:pos that does not exists
	 * @throws IOException
	 */
	@Test
	public void test_07() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// We'll try to find this chr:pos = 1:1019716 (the coordinate that is in the VCF file is 1:1019717)
		int chrPosReal = 865488; // Zero based coordinate of VCF line
		int chrPos = chrPosReal - 1; // Zero based coordinate of VCF line

		// Find chr:pos
		long pos = idx.find("1", chrPos, true);
		LineAndPos lp = idx.getLine(pos);
		int chrPosLp = idx.pos(lp.line);
		Assert.assertEquals(chrPos, chrPosLp); // We expect to find the next coordinate in VCF file (zero-based)

		idx.close();
	}

	@Test
	public void test_10() throws IOException {
		Gpr.debug("Test");
		String vcfFileName = "tests/unity/fileIndexChrPos/test.chr1.vcf";
		Random random = new Random(20130216);

		// Index file
		if (verbose) if (verbose) System.out.println("Indexing file '" + vcfFileName + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcfFileName);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// Iterate over vcf file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (random.nextInt(1000) < 20) {
				//System.out.println("\t" + ve);
				int chrPos = ve.getStart();

				long pos = idx.find("1", chrPos, true);
				LineAndPos lp = idx.getLine(pos);
				int chrPosLp = idx.pos(lp.line);
				Assert.assertEquals(chrPos, chrPosLp); // We expect to find the next coordinate in VCF file (zero-based)
			}
		}

	}

	/**
	 * Test : Find a chr:pos that does not exists
	 * @throws IOException
	 */
	@Test
	public void test_11() throws IOException {
		Gpr.debug("Test");
		String vcfFileName = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		Random random = new Random(20130217);

		// Index file
		if (verbose) if (verbose) System.out.println("Indexing file '" + vcfFileName + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcfFileName);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// Iterate over vcf file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		int chrPosPrev = 0;
		for (VcfEntry ve : vcf) {
			int chrPos = ve.getStart();
			if (chrPosPrev == 0) chrPosPrev = chrPos - 100;

			// Only perform some tests otherwise it's too long
			if (random.nextInt(1000) < 2) {
				if (verbose) System.out.println("\tFind: " + chrPosPrev + " - " + chrPos);

				// Find all positions from previous to current
				int step = Math.max((chrPos - chrPosPrev) / 10, 1);
				for (int cp = chrPosPrev; cp <= chrPos; cp += step) {
					// Find chr:pos
					long pos = idx.find("1", cp, true);
					LineAndPos lp = idx.getLine(pos);
					int chrPosLp = idx.pos(lp.line);
					if (debug) Gpr.debug("Find: " + cp + "\t" + lp.line);
					Assert.assertEquals(chrPos, chrPosLp); // We expect to find the next coordinate in VCF file (zero-based)
				}
			}

			chrPosPrev = chrPos + 1;
		}

		idx.close();
	}

	/**
	 * Test : Find a chr:pos that does not exists
	 * @throws IOException
	 */
	@Test
	public void test_20() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		String dump = idx.dump("1", 861292 - 1, 861315 - 1, true);
		String expected = "1\t861292\t.\tC\tG\t2971.31\tPASS\tAC=3;AF=0.00182;AN=1644;DS;set=Intersection\n1\t861315\t.\tG\tA\t837.18\tPASS\tAC=1;AF=0.00061;AN=1644;DS;set=Intersection\n";
		Assert.assertEquals(expected, dump);

		idx.close();
	}

	/**
	 * Test : Find a chr:pos that does not exists
	 * @throws IOException
	 */
	@Test
	public void test_21() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		String dump = idx.dump("1", 861291 - 1, 861315 - 1, true);
		String expected = "1\t861292\t.\tC\tG\t2971.31\tPASS\tAC=3;AF=0.00182;AN=1644;DS;set=Intersection\n1\t861315\t.\tG\tA\t837.18\tPASS\tAC=1;AF=0.00061;AN=1644;DS;set=Intersection\n";
		Assert.assertEquals(expected, dump);

		idx.close();
	}

	/**
	 * Test : Find a chr:pos that does not exists
	 * @throws IOException
	 */
	@Test
	public void test_22() throws IOException {
		Gpr.debug("Test");
		String vcf = "tests/unity/fileIndexChrPos/test.chr1.vcf";

		if (verbose) System.out.println("Indexing file '" + vcf + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcf);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		String dump = idx.dump("1", 861292 - 1, 861316 - 1, true);
		String expected = "1\t861292\t.\tC\tG\t2971.31\tPASS\tAC=3;AF=0.00182;AN=1644;DS;set=Intersection\n1\t861315\t.\tG\tA\t837.18\tPASS\tAC=1;AF=0.00061;AN=1644;DS;set=Intersection\n";
		Assert.assertEquals(expected, dump);

		idx.close();
	}

	/**
	 * Test : Dump small portions of a file
	 * @throws IOException
	 */
	@Test
	public void test_23() throws IOException {
		Gpr.debug("Test");
		String vcfFileName = "tests/unity/fileIndexChrPos/test.chr1.vcf";
		int MAX_TEST = 1000;
		Random random = new Random(20130217);

		// Index file
		if (verbose) System.out.println("Indexing file '" + vcfFileName + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcfFileName);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// Read VCF file
		int minPos = Integer.MAX_VALUE, maxPos = Integer.MIN_VALUE, count = 0;
		ArrayList<VcfEntry> vcfEntries = new ArrayList<>();
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			vcfEntries.add(ve);
			minPos = Math.min(minPos, ve.getStart());
			maxPos = Math.max(maxPos, ve.getStart());
			count++;
		}

		// Add some slack
		int dist = (maxPos - minPos) / count;
		minPos -= 1000;
		maxPos += 1000;

		// Dump random parts of the file
		if (verbose) System.out.println("\tDump test (short): ");
		for (int testNum = 1; testNum < MAX_TEST; testNum++) {
			// Random interval
			int start = random.nextInt(maxPos - minPos) + minPos;
			int end = start + random.nextInt(10) * dist; // Distance between a few lines

			// Dump file
			String dump = idx.dump("1", start, end, true);

			// Calculate expected result
			StringBuilder expected = new StringBuilder();
			for (VcfEntry ve : vcfEntries) {
				if ((start <= ve.getStart()) && (ve.getStart() <= end)) {
					// Append to expected output
					if (expected.length() > 0) expected.append("\n");
					expected.append(ve.getLine());
				}
			}
			if (expected.length() > 0) expected.append("\n");

			// Does it match?
			Assert.assertEquals(expected.toString(), dump);

			Gpr.showMark(testNum, 1);
		}
		System.err.println("");
		idx.close();
	}

	/**
	 * Test : Dump large portions of a file
	 * @throws IOException
	 */
	@Test
	public void test_24() throws IOException {
		Gpr.debug("Test");
		int MAX_TEST = 100;
		String vcfFileName = "tests/unity/fileIndexChrPos/test.chr1.vcf";
		Random random = new Random(20130217);

		// Index file
		if (verbose) System.out.println("Indexing file '" + vcfFileName + "'");
		FileIndexChrPos idx = new FileIndexChrPos(vcfFileName);
		idx.setVerbose(verbose);
		idx.setDebug(debug);
		idx.open();
		idx.index();

		// Read VCF file
		int minPos = Integer.MAX_VALUE, maxPos = Integer.MIN_VALUE;
		ArrayList<VcfEntry> vcfEntries = new ArrayList<>();
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			vcfEntries.add(ve);
			minPos = Math.min(minPos, ve.getStart());
			maxPos = Math.max(maxPos, ve.getStart());
		}

		// Add some slack
		minPos -= 1000;
		maxPos += 1000;

		// Dump random parts of the file
		for (int testNum = 0; testNum < MAX_TEST;) {
			// Random interval
			int start = random.nextInt(maxPos - minPos) + minPos;
			int end = random.nextInt(maxPos - minPos) + minPos;

			if (end > start) {
				if (verbose) System.out.println("Dump test (long) " + testNum + "/" + MAX_TEST + "\tchr1:" + start + "\tchr1:" + end);
				// Dump file
				String dump = idx.dump("1", start, end, true);

				// Calculate expected result
				StringBuilder expected = new StringBuilder();
				for (VcfEntry ve : vcfEntries) {
					if ((start <= ve.getStart()) && (ve.getStart() <= end)) {
						// Append to expected output
						if (expected.length() > 0) expected.append("\n");
						expected.append(ve.getLine());
					}
				}
				if (expected.length() > 0) expected.append("\n");

				// Does it match?
				Assert.assertEquals(expected.toString(), dump);

				testNum++;
				Gpr.showMark(testNum, 1);
			}
		}

		System.err.println("");
		idx.close();
	}

}
