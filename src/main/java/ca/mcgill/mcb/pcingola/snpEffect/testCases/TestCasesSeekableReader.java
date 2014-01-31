package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.SeekableBufferedReader;

/**
 * Seekable file reader test case
 * 
 * @author pcingola
 */
public class TestCasesSeekableReader extends TestCase {

	/**
	 * Calculate a simple hash using SeekableBufferedReader
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	long calcHash(String fileName) throws IOException {
		SeekableBufferedReader sbr = new SeekableBufferedReader(fileName);
		String line;
		long hash = 0;
		while ((line = sbr.readLine()) != null) {
			hash += line.hashCode();
		}

		sbr.close();
		return hash;
	}

	/**
	 * Same as calc hash, but using BufferedReader
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	long calcHashBufferedReader(String fileName) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(fileName)));
		String line;
		long hash = 0;
		while ((line = br.readLine()) != null) {
			hash += line.hashCode();
		}
		br.close();
		return hash;
	}

	public void test_00() throws IOException {
		String fileName = "tests/testLukas.vcf";
		long hashExp = calcHashBufferedReader(fileName);
		long hash = calcHash(fileName);
		System.out.println(String.format("%016x\t%016x\t%s", hashExp, hash, fileName));

		Assert.assertEquals(hashExp, hash);
	}

	/**
	 * Basic parsing
	 * @throws IOException 
	 */
	public void test_01() throws IOException {
		String dirName = "./tests/";
		File dir = new File(dirName);

		for (String fileName : dir.list()) {
			if (fileName.endsWith(".txt") || fileName.endsWith(".vcf")) {
				if (fileName.equals("testLukas.vcf")) {
					fileName = dirName + fileName;
					long hashExp = calcHashBufferedReader(fileName);
					long hash = calcHash(fileName);
					System.out.println(String.format("%016x\t%016x\t%s", hashExp, hash, fileName));

					Assert.assertEquals(hashExp, hash);
				}
			}
		}
	}
}
