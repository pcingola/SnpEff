package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Genome;
import ca.mcgill.mcb.pcingola.interval.SeqChange;
import ca.mcgill.mcb.pcingola.outputFormatter.VcfOutputFormatter;
import ca.mcgill.mcb.pcingola.snpEffect.Config;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.util.Timer;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect.FormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import ca.mcgill.mcb.pcingola.vcf.VcfGenotype;

/**
 * VCF parsing test cases 
 * 
 * @author pcingola
 */
public class TestCasesVcf extends TestCase {

	boolean verbose = false;
	boolean createOutputFile = false;
	Random rand;
	Config config;
	Genome genome;

	/**
	 * Creates a test file
	 * @throws IOException
	 */
	public static void create1kgFile() throws IOException {
		String vcfFile = Gpr.HOME + "/snpEff/1kg.indels.vcf";
		String vcfOutFile = Gpr.HOME + "/workspace/SnpEff/tests/1kg.indels.vcf";

		StringBuilder outvcf = new StringBuilder();

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			StringBuilder sb = new StringBuilder();

			for (SeqChange sc : ve.seqChanges()) {
				if (sb.length() > 0) sb.append(",");
				sb.append(sc.getReference() + "/" + sc.getChange());
			}

			ve.addInfo("SEQCHANGE", sb.toString());
			outvcf.append(ve + "\n");
		}

		Gpr.toFile(vcfOutFile, outvcf);
	}

	public TestCasesVcf() {
		super();
		initRand();
		config = new Config("testCase", Config.DEFAULT_CONFIG_FILE);
	}

	/**
	 * Get file's format version
	 * @param vcfFileName
	 * @return
	 */
	FormatVersion formatVersion(String vcfFileName) {
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		VcfEntry ve = vcf.next();
		List<VcfEffect> effs = ve.parseEffects();
		VcfEffect eff = effs.get(0);
		return eff.formatVersion();
	}

	void initRand() {
		rand = new Random(20100629);
	}

	void initSnpEffPredictor() {
		initSnpEffPredictor("testCase");
	}

	void initSnpEffPredictor(String genomeName) {
		// Create a config and force out snpPredictor for hg37 chromosome Y
		config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
		config.loadSnpEffectPredictor();
		genome = config.getGenome();
		config.getSnpEffectPredictor().buildForest();
	}

	/**
	 * Check that the size is correct (at least for SNPs)
	 * 
	 * Louis Letourneau discovered this horrendous bug. This is 
	 * my first attempt to fix it....
	 */
	public void test_00() {
		String fileName = "./tests/1kg_head.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName);

		for (VcfEntry ve : vcf) {
			Assert.assertEquals(1, ve.size());
		}
	}

	/**
	 * Basic parsing
	 */
	public void test_01() {
		initSnpEffPredictor("testCase");

		String fileName = "./tests/vcf.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName, genome);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (SeqChange seqChange : vcfEntry.seqChanges()) {
				System.out.println(seqChange);
				String seqChangeStr = "chr" + seqChange.getChromosomeName() + ":" + seqChange.getStart() + "_" + seqChange.getReference() + "/" + seqChange.getChange();
				Assert.assertEquals(seqChange.getId(), seqChangeStr);
			}
		}
	}

	/**
	 * All variants are heterozygous
	 */
	public void test_02_hetero() {
		initSnpEffPredictor("testCase");

		String fileName = "./tests/vcf_hetero.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName, genome);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (SeqChange seqChange : vcfEntry.seqChanges()) {
				if (!seqChange.isHeterozygous()) throw new RuntimeException("All VCF entries in this file should be heterozygous!\n\t" + seqChange);
			}
		}
	}

	/**
	 * All variants are neider homozugous nor heterozygous
	 */
	public void test_02_homhet() {
		initSnpEffPredictor("testCase");

		String fileName = "./tests/vcf_homhet.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName, genome);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (SeqChange seqChange : vcfEntry.seqChanges()) {
				if (seqChange.isHomozygous()) throw new RuntimeException("NO multi-sample VCF entry should be homozygous!\n\t" + seqChange);
				if (seqChange.isHeterozygous()) throw new RuntimeException("NO multi-sample VCF entry should be heterozygous!\n\t" + seqChange);
			}
		}
	}

	/**
	 * All variants are homozygous
	 */
	public void test_03_homo() {
		initSnpEffPredictor("testCase");

		String fileName = "./tests/vcf_homo.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName, genome);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (SeqChange seqChange : vcfEntry.seqChanges()) {
				Gpr.debug(seqChange.isHeterozygous() + "\t" + seqChange);
				if (!seqChange.isHomozygous()) throw new RuntimeException("All VCF entries in this file should be homozygous!\n\t" + seqChange);
			}
		}
	}

	/**
	 * Deletions
	 */
	public void test_04_del() {
		initSnpEffPredictor("testCase");

		String fileName = "./tests/vcf_04_del.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName, genome);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (SeqChange seqChange : vcfEntry.seqChanges()) {
				if (!seqChange.isDel()) throw new RuntimeException("All VCF entries in this file should be deletions!\n\t" + seqChange);
			}
		}
	}

	/**
	 * Problems parsing
	 */
	public void test_05_choking_on_dot_slash_dot() {
		initSnpEffPredictor("testCase");

		String fileName = "./tests/choking_on_dot_slash_dot.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName, genome);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (VcfGenotype gen : vcfEntry) {
				boolean var = gen.isVariant(); // This used to cause an exception
				System.out.println("\t" + var + "\t" + gen);
			}
		}
		System.out.println("");
	}

	/**
	 * Problems creating seqChanges
	 * 
	 * The problem is when creating a seqChange from this line:
	 * Chr1    223919  .   CTCGACCACTGGAA  CTCACATCCATACAT,CATGACCACTGGAA
	 * 
	 * There are two changes:
	 * 			CTCGACCACTGGAA   
	 * 			CTCACATCCATACAT
	 *          => GACCACTGGAA / ACATCCATACAT  (Mixed change?)
	 * 
	 * 			CTCGACCACTGGAA
	 * 			CATGACCACTGGAA  
	 * 			 ^^
	 *          => CG / TG  (MNP)
	 *  
	 */
	public void test_06_mixed_change() {
		// WARNING: This test is expected to fail, because this functionality is unimplemented
		initSnpEffPredictor("testCase");

		String file = "./tests/array_out_of_bounds.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		vcf.setCreateChromos(true);

		for (VcfEntry vcfEntry : vcf) {
			System.out.println(vcfEntry);

			// Compare seqChanges to what we expect
			List<SeqChange> seqChanges = vcfEntry.seqChanges();

			Assert.assertEquals("chr1:223921_GACCACTGGAA/ACATCCATACAT", seqChanges.get(0).toString()); // FIXME: What the hell do I actually expect here?			
			Assert.assertEquals("chr1:223919_TC/AT", seqChanges.get(1).toString());
		}
	}

	/**
	 * Extremely weird long lines in a VCF file (thousands of bases long)
	 */
	public void test_07_long_lines() {
		initSnpEffPredictor("testCase");

		String file = "./tests/long.vcf";

		Timer t = new Timer();
		t.start();

		VcfFileIterator vcf = new VcfFileIterator(file);
		vcf.setCreateChromos(true);

		// They are so long that they may produce 'Out of memory' errors
		for (VcfEntry vcfEntry : vcf) {
			System.out.println(vcfEntry.getChromosomeName() + ":" + vcfEntry.getStart());
			for (VcfGenotype vg : vcfEntry)
				System.out.println("\t" + vg);
		}

		// Too much time? we are doing something wrong...
		if (t.elapsed() > 1000) throw new RuntimeException("It should not take this long to process a few lines!!!");
	}

	/**
	 * Test for "<DEL>" in ALT field
	 */
	public void test_08_alt_del() {
		initSnpEffPredictor("testCase");

		String file = "./tests/alt_del.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		vcf.setCreateChromos(true);

		// They are so long that they may produce 'Out of memory' errors
		for (VcfEntry vcfEntry : vcf) {
			System.out.println(vcfEntry);

			boolean hasDel = false;
			for (SeqChange sc : vcfEntry.seqChanges()) {
				hasDel |= sc.isDel();
				System.out.println("\t" + sc + "\t" + sc.isDel());
			}

			Assert.assertEquals(true, hasDel);
		}
	}

	/**
	 * Empty ALT: Not a variant
	 */
	public void test_09_empty_ALT() {
		String file = "./tests/empty.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			System.out.println(vcfEntry);
			Assert.assertEquals(false, vcfEntry.isVariant());
		}
	}

	/**
	 * Empty Quality: Not a variant
	 */
	public void test_10_empty_QUAL() {
		String file = "./tests/empty.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			System.out.println(vcfEntry);
			Assert.assertEquals(0.0, vcfEntry.getQuality());
		}
	}

	/**
	 * Empty fields should show '.' when printed
	 */
	public void test_11_empty() {
		String file = "./tests/empty.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			System.out.println(vcfEntry);
			Assert.assertEquals("1\t11169327\t.\tT\t.\t.\tPASS\tAC=0;AF=0.00;AN=176;DP=7756;MQ0=0;set=ReferenceInAll\tGT:DP\t0/0:115", vcfEntry.toString());
		}
	}

	public void test_12_readHeader() {
		String file = "./tests/test.chr1.1line.vcf";

		VcfFileIterator vcfFile = new VcfFileIterator(file);
		vcfFile.readHeader();

		int numLines = 0;
		for (VcfEntry vcfEntry : vcfFile) {
			System.out.println(vcfEntry);
			numLines++;
		}

		Assert.assertEquals(1, numLines);
	}

	/**
	 * Header should NOT have a trailing '\n'
	 */
	public void test_12_readHeader_NL() {
		String file = "./tests/test.chr1.1line.vcf";

		VcfFileIterator vcfFile = new VcfFileIterator(file);
		String header = vcfFile.readHeader().toString();

		Assert.assertEquals(false, header.charAt(header.length() - 1) == '\n');
	}

	public void test_13_chrOri() {
		String file = "./tests/test.chr1.1line.vcf";

		VcfFileIterator vcfFile = new VcfFileIterator(file);
		vcfFile.readHeader();

		String chr = null;
		for (VcfEntry vcfEntry : vcfFile)
			chr = vcfEntry.getChromosomeNameOri();

		Assert.assertEquals("chr1", chr);
	}

	public void test_14_OutputFormatter_AddInfo() {
		VcfOutputFormatter vof = new VcfOutputFormatter((List<VcfEntry>) null);
		String testIn[] = { "Hi ", "Hi how;", "Hi how;are|", "Hi how;are|you,", "Hi how;are|you,doing=", "Hi how;are|you,doing=today(.)" };
		String testOut[] = { "Hi_", "Hi_how_", "Hi_how_are_", "Hi_how_are_you_", "Hi_how_are_you_doing_", "Hi_how_are_you_doing_today_._" };
		for (int i = 0; i < testIn.length; i++) {
			String safe = vof.vcfInfoSafeString(testIn[i]);
			System.out.println("'" + testIn[i] + "'\t'" + safe + "'\t'" + testOut[i] + "'");
			Assert.assertEquals(testOut[i], safe);
		}
	}

	public void test_15_Eff_format_version_guess() {
		String vcfFileName = "./tests/test.EFF_V2.vcf";
		FormatVersion formatVersion = formatVersion(vcfFileName);
		Assert.assertEquals(FormatVersion.FORMAT_SNPEFF_2, formatVersion);

		vcfFileName = "./tests/test.EFF_V3.vcf";
		formatVersion = formatVersion(vcfFileName);
		Assert.assertEquals(FormatVersion.FORMAT_SNPEFF_3, formatVersion);

	}

	public void test_16_indels() {
		String vcfFile = "tests/1kg.indels.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			StringBuilder seqChangeResult = new StringBuilder();

			for (SeqChange sc : ve.seqChanges()) {
				if (seqChangeResult.length() > 0) seqChangeResult.append(",");
				seqChangeResult.append(sc.getReference() + "/" + sc.getChange());
			}

			String seqChangeExpected = ve.getInfo("SEQCHANGE");

			Assert.assertEquals(seqChangeExpected, seqChangeResult.toString());
		}
	}

	public void test_17_vcf_bed_filter() {
		String vcfFile = "tests/test_vcf_filter.vcf";
		String bedFile = "tests/test_vcf_filter.bed";

		String args[] = { "-v", "-filterinterval", bedFile, "testHg3771Chr1", vcfFile };
		SnpEff snpeff = new SnpEff(args);

		// Create command and run
		SnpEffCmdEff effcmd = (SnpEffCmdEff) snpeff.snpEffCmd();
		List<VcfEntry> vcfEntries = effcmd.run(true);

		// All VCF entries should be filtered out
		Gpr.debug("Vcf entries: " + vcfEntries.size());
		Assert.assertEquals(0, vcfEntries.size());

		// Nothing should be printed
		for (VcfEntry ve : vcfEntries)
			System.out.println(ve);
	}

	public void test_18_vcf_tabix() {
		VcfFileIterator vcf = new VcfFileIterator("./tests/test_tabix.vcf.gz");

		String chrpos = "";
		for (VcfEntry ve : vcf) {
			System.out.println(ve);
			chrpos += ve.getChromosomeName() + ":" + ve.getStart() + " ";
		}

		// Make sure both lines appear
		Assert.assertEquals("1:249211906 2:41612", chrpos.trim());
	}

}
