package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import java.util.HashSet;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.binseq.DnaAndQualitySequence;
import ca.mcgill.mcb.pcingola.binseq.DnaSequence;
import ca.mcgill.mcb.pcingola.binseq.DnaSequencePe;
import ca.mcgill.mcb.pcingola.binseq.coder.DnaCoder;
import ca.mcgill.mcb.pcingola.fastq.Fastq;
import ca.mcgill.mcb.pcingola.fastq.FastqVariant;
import ca.mcgill.mcb.pcingola.fileIterator.FastqFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;

public class TestCasesDnaSequence extends TestCase {

	public static boolean verbose = false;

	/**
	 * Create random changes in a sequence
	 * @param overlap
	 * @param overlapChanges
	 * @return
	 */
	String change(String sequence, int numChanges, Random rand) {
		HashSet<Integer> changedPos = new HashSet<Integer>();
		char chars[] = sequence.toCharArray();

		for( int i = 0; i < numChanges; ) {
			int pos = rand.nextInt(chars.length);

			if( !changedPos.contains(pos) ) { // Already changed?
				int newCode = rand.nextInt() & 0x03;
				char newBase = DnaCoder.get().toBase(newCode);

				if( chars[pos] != newBase ) { // Base is different?
					chars[pos] = newBase;
					changedPos.add(pos);
					i++;
				}
			}
		}

		return new String(chars);
	}

	/**
	 * Create random sequences and store them in a DnaSequence. 
	 * Compare getting a few random bases from the original and DnaSequence sequences.
	 * @param numTests
	 * @param lenMask
	 * @param seed
	 */
	public void randDnaSeqGetBasesTest(int numTests, int numTestsPerSeq, int lenMask, long seed) {
		Random rand = new Random(seed);

		for( int t = 0; t < numTests; t++ ) {
			String seq = "";
			int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
			seq = randSeq(len, rand); // Create a random sequence

			DnaSequence bseq = new DnaSequence(seq);

			// Retrieve numTestsPerSeq random bases from the sequence
			for( int i = 0; i < numTestsPerSeq; i++ ) {
				int randPos = rand.nextInt(len);
				int randLen = rand.nextInt(len - randPos);
				String basesOri = seq.substring(randPos, randPos + randLen);
				String basesBin = bseq.getBases(randPos, randLen);
				Assert.assertEquals(basesOri, basesBin);
				if( verbose ) System.out.println("randDnaSeqGetBasesTest:\tPos: " + randPos + "\t" + "Len: " + randLen + "\t'" + basesOri + "'\t=\t'" + basesBin + "'");
			}
		}
	}

	/**
	 * Create random sequences and store them in a DnaSequence. 
	 * Compare getting a single random base from the original and DnaSequence sequences.
	 * @param numTests
	 * @param lenMask
	 * @param seed
	 */
	public void randDnaSeqGetBaseTest(int numTests, int numTestsPerSeq, int lenMask, long seed) {
		Random rand = new Random(seed);

		for( int t = 0; t < numTests; t++ ) {
			String seq = "";
			int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
			seq = randSeq(len, rand); // Create a random sequence

			if( verbose ) System.out.println("DnaSequence test:" + t + "\tlen:" + len + "\t" + seq);
			DnaSequence bseq = new DnaSequence(seq);

			// Retrieve numTestsPerSeq random bases from the sequence
			for( int i = 0; i < numTestsPerSeq; i++ ) {
				int randPos = rand.nextInt(len);
				char baseOri = seq.charAt(randPos);
				char baseBin = bseq.getBase(randPos);
				Assert.assertEquals(baseOri, baseBin);
			}
		}
	}

	/**
	 * Create random sequences and compare to storing them in a DnaSequence
	 * @param numTests
	 * @param lenMask
	 * @param seed
	 */
	public void randDnaSeqTest(int numTests, int lenMask, long seed) {
		Random rand = new Random(seed);

		for( int t = 0; t < numTests; t++ ) {
			String seq = "";
			int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
			seq = randSeq(len, rand); // Create a random sequence

			if( verbose ) System.out.println("DnaSequence test:" + t + "\tlen:" + len + "\t" + seq);
			DnaSequence bseq = new DnaSequence(seq);
			Assert.assertEquals(seq, bseq.toString());
		}
	}

	/**
	 * Create a random quality sequence of length 'len'
	 */
	String randQual(int len, Random rand) {
		StringBuilder sb = new StringBuilder();
		// Create a random sequence
		for( int i = 0; i < len; i++ ) {
			int r = rand.nextInt() & 40;
			char qchar = (char) ('!' + r);
			sb.append(qchar);
		}
		return sb.toString();
	}

	/**
	 * Create random sequences and store them in a DnaSequence. 
	 * Compare after replacing random bases from the original and DnaSequence sequences.
	 * @param numTests
	 * @param lenMask
	 * @param seed
	 */
	public void randReplaceBaseTest(int numTests, int numTestsPerSeq, int lenMask, long seed) {
		Random rand = new Random(seed);

		for( int t = 0; t < numTests; t++ ) {
			String seq = "";
			int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
			seq = randSeq(len, rand); // Create a random sequence

			DnaSequence bseq = new DnaSequence(seq);

			// Replace numTestsPerSeq random bases from the sequence
			if( verbose ) System.out.println("randReplaceBaseTest\nOri    :\t" + seq);
			for( int i = 0; i < numTestsPerSeq; i++ ) {
				// Random position
				int randPos = rand.nextInt(len);
				char baseOri = seq.charAt(randPos);

				// Random base (different than baseOri)
				char randBase = baseOri;
				while(randBase == baseOri) {
					int r = rand.nextInt() & 0x03;
					randBase = DnaCoder.get().toBase(r);
				}

				// Replace base in sequence (string)
				char seqChars[] = seq.toCharArray();
				seqChars[randPos] = randBase;
				seq = new String(seqChars);

				// Replace i DnaSequence
				bseq.setBase(randPos, randBase);
				if( verbose ) System.out.println("Changed:\t" + seq + "\tpos: " + randPos + "\trandbase: " + randBase + "\n\t\t" + bseq);

				// Compare results
				Assert.assertEquals(seq, bseq.toString());
			}
		}
	}

	/**
	 * Create a random sequence of length 'len'
	 * @param len
	 * @param rand
	 * @return
	 */
	String randSeq(int len, Random rand) {
		StringBuilder sb = new StringBuilder();
		// Create a random sequence
		for( int i = 0; i < len; i++ ) {
			int r = rand.nextInt() & 0x03;
			sb.append(DnaCoder.get().toBase(r));
		}
		return sb.toString();
	}

	/**
	 * Create random sequences (and qualities) and compare to storing them in a DnaAndQuality
	 * @param numTests
	 * @param lenMask
	 * @param seed
	 */
	public void randTestQual(int numTests, int lenMask, long seed) {
		Random rand = new Random(seed);

		for( int t = 0; t < numTests; t++ ) {
			String seq = "", qual = "";
			int len = (rand.nextInt() & lenMask) + 10; // Randomly select sequence length
			seq = randSeq(len, rand); // Create a random sequence
			qual = randQual(len, rand); // Create a random quality
			String fullSeq = seq + "\t" + qual;

			if( verbose ) System.out.println("DnaAndQualitySequence test:" + t + "\tlen:" + len + "\t" + seq);
			DnaAndQualitySequence bseq = new DnaAndQualitySequence(seq, qual, FastqVariant.FASTQ_SANGER);
			if( !fullSeq.equals(bseq.toString()) ) throw new RuntimeException("Sequences do not match:\n\tOriginal:\t" + fullSeq + "\n\tDnaAndQSeq:\t" + bseq);
		}
	}

	public void test_01_short() {
		Gpr.debug("Test");
		long seed = 20100615;
		int lenMask = 0xff;
		int numTests = 1000;
		randDnaSeqTest(numTests, lenMask, seed);
	}

	public void test_01_short_getBase() {
		Gpr.debug("Test");
		long seed = 20110217;
		int lenMask = 0xff;
		int numTests = 1000;
		int numTestsPerSeq = 100;
		randDnaSeqGetBaseTest(numTests, numTestsPerSeq, lenMask, seed);
	}

	public void test_01_short_getBases() {
		Gpr.debug("Test");
		long seed = 20110218;
		int lenMask = 0xff;
		int numTests = 1000;
		int numTestsPerSeq = 100;
		randDnaSeqGetBasesTest(numTests, numTestsPerSeq, lenMask, seed);
	}

	public void test_01_short_replaceBase() {
		Gpr.debug("Test");
		long seed = 20110218;
		int lenMask = 0xff;
		int numTests = 1000;
		int numTestsPerSeq = 100;
		randReplaceBaseTest(numTests, numTestsPerSeq, lenMask, seed);
	}

	public void test_02_long() {
		Gpr.debug("Test");
		long seed = 20100614;
		int lenMask = 0xffff;
		int numTests = 10;
		randDnaSeqTest(numTests, lenMask, seed);
	}

	public void test_02_long_getBase() {
		Gpr.debug("Test");
		long seed = 20110217;
		int lenMask = 0xffff;
		int numTests = 10;
		int numTestsPerSeq = 1000;
		randDnaSeqGetBaseTest(numTests, numTestsPerSeq, lenMask, seed);
	}

	public void test_02_long_getBases() {
		Gpr.debug("Test");
		long seed = 20110218;
		int lenMask = 0xffff;
		int numTests = 10;
		int numTestsPerSeq = 1000;
		randDnaSeqGetBasesTest(numTests, numTestsPerSeq, lenMask, seed);
	}

	public void test_02_long_replaceBase() {
		Gpr.debug("Test");
		long seed = 20110217;
		int lenMask = 0xffff;
		int numTests = 10;
		int numTestsPerSeq = 1000;
		randReplaceBaseTest(numTests, numTestsPerSeq, lenMask, seed);
	}

	public void test_04_Pe() {
		Gpr.debug("Test");
		int numTests = 1000;
		Random rand = new Random(20100617);

		for( int t = 0; t < numTests; t++ ) {

			// Create gap
			String gapStr = "";
			int gap = rand.nextInt(50) + 1;
			for( int i = 0; i < gap; i++ )
				gapStr += "N";

			// Sequence 2
			int len1 = rand.nextInt(100) + 1;
			String seq1 = randSeq(len1, rand);

			// Sequence 2
			int len2 = rand.nextInt(100) + 1;
			String seq2 = randSeq(len2, rand);

			// Final sequence
			String seq = seq1 + gapStr + seq2;

			DnaSequencePe bseqpe = new DnaSequencePe(seq1, seq2, gap);
			if( verbose ) System.out.println("PE test: " + t + "\t" + bseqpe);

			if( !bseqpe.toString().equals(seq) ) throw new RuntimeException("Sequences do not match:\n\t" + seq + "\n\t" + bseqpe);
		}
	}

	public void test_05_fastqReader() {
		Gpr.debug("Test");
		String fastqFileName = "tests/fastq_test.fastq";
		String txtFileName = "tests/fastq_test.txt";

		// Read fastq file
		StringBuilder sb = new StringBuilder();
		for( Fastq fq : new FastqFileIterator(fastqFileName, FastqVariant.FASTQ_ILLUMINA) )
			sb.append(fq.getSequence() + "\t" + fq.getQuality() + "\n");
		if( verbose ) System.out.println("Fastq test:\n" + sb);

		// Read txt file
		String txt = Gpr.readFile(txtFileName);

		// Compare
		if( txt.equals(sb.toString()) ) throw new RuntimeException("Sequences from fastq file does not match expected results:\n----- Fastq file -----" + sb + "\n----- Txt file -----" + txt + "-----");
	}

	public void test_05_quality_short() {
		Gpr.debug("Test");
		long seed = 20100804;
		int lenMask = 0xff;
		int numTests = 1000;
		randTestQual(numTests, lenMask, seed);
	}

	public void test_06_quality_long() {
		Gpr.debug("Test");
		long seed = 20100804;
		int lenMask = 0xffff;
		int numTests = 10;
		randTestQual(numTests, lenMask, seed);
	}

}
