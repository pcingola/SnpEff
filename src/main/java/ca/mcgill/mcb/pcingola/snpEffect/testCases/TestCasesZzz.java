package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.Random;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.interval.Chromosome;
import ca.mcgill.mcb.pcingola.interval.Gene;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.Transcript;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.SnpEffectPredictor;

/**
 * 
 * Test case
 * 
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	boolean debug = false;
	boolean verbose = false || debug;

	Random rand;
	Config config;
	Genome genome;
	Chromosome chromosome;
	Gene gene;
	Transcript transcript;
	SnpEffectPredictor snpEffectPredictor;
	String chromoSequence = "";
	char chromoBases[];

	public TestCasesZzz() {
		super();
		//		init();
	}

	//	/**
	//	 * Distance to transcription start site (from a position before CDS start)
	//	 * @param bases
	//	 * @param pos
	//	 * @param direction
	//	 * @return
	//	 */
	//	int codingBase(char bases[], int pos, int direction) {
	//		for (; (pos >= 0) && (pos < bases.length); pos += direction)
	//			if ((bases[pos] == '>') || (bases[pos] == '<')) return pos;
	//
	//		throw new RuntimeException("This should never happen!");
	//	}

	//	/**
	//	 * Distance to UTR
	//	 * @param bases
	//	 * @param pos
	//	 * @param direction
	//	 * @return
	//	 */
	//	int distToUtr5(char bases[], int pos, int direction) {
	//		int count = 0;
	//		for (; (pos >= 0) && (pos < bases.length); pos -= direction) {
	//			if (bases[pos] == '5') return count;
	//			if (bases[pos] != '-') count++;
	//		}
	//		return count;
	//	}
	//
	//	void init() {
	//		initRand();
	//		initSnpEffPredictor(false, true);
	//	}
	//
	//	void initRand() {
	//		rand = new Random(20130708);
	//	}
	//
	//	/**
	//	 * Run SnpEff on VCF file
	//	 * @param vcfFile
	//	 */
	//	public void snpEffect(String vcfFile, String genomeVer) {
	//		// Create command
	//		String args[] = { "-hgvs", "-ud", "0", genomeVer, vcfFile };
	//
	//		SnpEff cmd = new SnpEff(args);
	//		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
	//
	//		// Run command
	//		List<VcfEntry> list = cmdEff.run(true);
	//
	//		// Find HGVS in any 'EFF' field
	//		int entryNum = 1;
	//		for (VcfEntry vcfEntry : list) {
	//			boolean found = false;
	//
	//			// Load hgvs expected annotations into set
	//			String hgvsStr = vcfEntry.getInfo("HGVS");
	//			HashSet<String> hgvsExpected = new HashSet<String>();
	//			for (String h : hgvsStr.split(",")) {
	//				if (h.indexOf(':') > 0) h = h.substring(h.indexOf(':') + 1);
	//				hgvsExpected.add(h);
	//			}
	//
	//			if (debug) System.err.println(entryNum + "\t" + vcfEntry);
	//
	//			// Find if HGVS predicted by SnpEff matches tha expected annotations
	//			for (VcfEffect eff : vcfEntry.parseEffects()) {
	//				String hgvsReal = eff.getAa();
	//				if (debug) System.err.println("\tHGVS: " + hgvsExpected.contains(hgvsReal) + "\t" + hgvsExpected + "\tAA: " + eff.getAa() + "\t" + eff.getGenotype() + "\t" + eff);
	//				if (hgvsExpected.contains(hgvsReal)) found = true;
	//			}
	//
	//			// Not found? Error
	//			if (!found) throw new RuntimeException("HGVS not found in variant\n" + vcfEntry);
	//			entryNum++;
	//		}
	//	}

}
