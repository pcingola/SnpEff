package org.snpeff.snpEffect.testCases.unity;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.interval.Variant.VariantType;
import org.snpeff.interval.VariantBnd;
import org.snpeff.outputFormatter.VcfOutputFormatter;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Timer;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;
import org.snpeff.vcf.VcfEntry;
import org.snpeff.vcf.VcfGenotype;
import org.snpeff.vcf.VcfHeaderInfo;
import org.snpeff.vcf.VcfHeaderInfo.VcfInfoNumber;
import org.snpeff.vcf.VcfInfoType;

/**
 * VCF parsing test cases
 *
 * @author pcingola
 */
public class TestCasesVcf extends TestCasesBase {

	boolean verbose = false;
	boolean debug = false;
	boolean createOutputFile = false;

	public TestCasesVcf() {
		super();
	}

	/**
	 * Check that the size is correct (at least for SNPs)
	 *
	 * Louis Letourneau discovered this horrendous bug. This is
	 * my first attempt to fix it....
	 */
	@Test
	public void test_00() {
		Gpr.debug("Test");
		String fileName = "./tests/unity/vcf/1kg_head.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName);

		for (VcfEntry ve : vcf) {
			Assert.assertEquals(1, ve.size());
		}
	}

	/**
	 * Basic parsing
	 */
	@Test
	public void test_01() {
		Gpr.debug("Test");
		String fileName = "./tests/unity/vcf/vcf.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (Variant variant : vcfEntry.variants()) {
				String variantStr = "chr" + variant.toStringOld();
				if (verbose) System.out.println(variant + "\t'" + variantStr + "'");
				Assert.assertEquals(variant.getId(), variantStr);
			}
		}
	}

	/**
	 * Deletions
	 */
	@Test
	public void test_04_del() {
		Gpr.debug("Test");
		String fileName = "./tests/unity/vcf/vcf_04_del.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (Variant variant : vcfEntry.variants()) {
				if (!variant.isDel()) throw new RuntimeException("All VCF entries in this file should be deletions!\n\t" + variant);
			}
		}
	}

	/**
	 * Problems parsing
	 */
	@Test
	public void test_05_choking_on_dot_slash_dot() {
		Gpr.debug("Test");
		String fileName = "./tests/unity/vcf/choking_on_dot_slash_dot.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName);
		vcf.setCreateChromos(true);
		for (VcfEntry vcfEntry : vcf) {
			for (VcfGenotype gen : vcfEntry) {
				boolean var = gen.isVariant(); // This used to cause an exception
				if (verbose) System.out.println("\t" + var + "\t" + gen);
			}
		}
		if (verbose) System.out.println("");
	}

	/**
	 * Problems creating variants
	 *
	 * The problem is when creating a variant from this line:
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
	@Test
	public void test_06_mixed_change() {
		// WARNING: This test is expected to fail, because this functionality is unimplemented
		Gpr.debug("Test");
		String file = "./tests/unity/vcf/array_out_of_bounds.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		vcf.setCreateChromos(true);

		for (VcfEntry vcfEntry : vcf) {
			if (verbose) System.out.println(vcfEntry);

			// Compare variants to what we expect
			List<Variant> variants = vcfEntry.variants();

			Assert.assertEquals("chr1:223921_GACCACTGGAA/ACATCCATACAT", variants.get(0).toString());
			Assert.assertEquals("chr1:223919_TC/AT", variants.get(1).toString());
		}
	}

	/**
	 * Extremely weird long lines in a VCF file (thousands of bases long)
	 */
	@Test
	public void test_07_long_lines() {
		Gpr.debug("Test");

		String file = "./tests/unity/vcf/long.vcf";

		Timer t = new Timer();
		t.start();

		VcfFileIterator vcf = new VcfFileIterator(file);
		vcf.setCreateChromos(true);

		// They are so long that they may produce 'Out of memory' errors
		for (VcfEntry vcfEntry : vcf) {
			if (verbose) System.out.println(vcfEntry.getChromosomeName() + ":" + vcfEntry.getStart());
			for (VcfGenotype vg : vcfEntry)
				if (verbose) System.out.println("\t" + vg);
		}

		// Too much time? we are doing something wrong...
		if (t.elapsed() > 1000) throw new RuntimeException("It should not take this long to process a few lines!!!");
	}

	/**
	 * Test for "<DEL>" in ALT field
	 */
	@Test
	public void test_08_alt_del() {
		Gpr.debug("Test");

		String file = "./tests/unity/vcf/alt_del.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		vcf.setCreateChromos(true);

		// They are so long that they may produce 'Out of memory' errors
		for (VcfEntry vcfEntry : vcf) {
			if (verbose) System.out.println(vcfEntry);

			boolean hasDel = false;
			for (Variant sc : vcfEntry.variants()) {
				hasDel |= sc.isDel();
				if (verbose) System.out.println("\t" + sc + "\t" + sc.isDel());
			}

			Assert.assertEquals(true, hasDel);
		}
	}

	/**
	 * Empty ALT: Not a variant
	 */
	@Test
	public void test_09_empty_ALT() {
		Gpr.debug("Test");
		String file = "./tests/unity/vcf/empty.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			if (verbose) System.out.println(vcfEntry);
			Assert.assertEquals(false, vcfEntry.isVariant());
		}
	}

	/**
	 * Empty Quality: Not a variant
	 */
	@Test
	public void test_10_empty_QUAL() {
		Gpr.debug("Test");
		String file = "./tests/unity/vcf/empty.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			if (verbose) System.out.println(vcfEntry);
			Assert.assertEquals(0.0, vcfEntry.getQuality(), 10 ^ -6);
		}
	}

	/**
	 * Empty fields should show '.' when printed
	 */
	@Test
	public void test_11_empty() {
		Gpr.debug("Test");
		String file = "./tests/unity/vcf/empty.vcf";

		VcfFileIterator vcf = new VcfFileIterator(file);
		for (VcfEntry vcfEntry : vcf) {
			if (verbose) System.out.println(vcfEntry);
			Assert.assertEquals("1\t11169327\t.\tT\t.\t.\tPASS\tAC=0;AF=0.00;AN=176;DP=7756;MQ0=0;set=ReferenceInAll\tGT:DP\t0/0:115", vcfEntry.toString());
		}
	}

	@Test
	public void test_12_readHeader() {
		Gpr.debug("Test");
		String file = "./tests/unity/vcf/test.chr1.1line.vcf";

		VcfFileIterator vcfFile = new VcfFileIterator(file);
		vcfFile.readHeader();

		int numLines = 0;
		for (VcfEntry vcfEntry : vcfFile) {
			if (verbose) System.out.println(vcfEntry);
			numLines++;
		}

		Assert.assertEquals(1, numLines);
	}

	/**
	 * Header should NOT have a trailing '\n'
	 */
	@Test
	public void test_12_readHeader_NL() {
		Gpr.debug("Test");
		String file = "./tests/unity/vcf/test.chr1.1line.vcf";

		VcfFileIterator vcfFile = new VcfFileIterator(file);
		String header = vcfFile.readHeader().toString();

		Assert.assertEquals(false, header.charAt(header.length() - 1) == '\n');
	}

	@Test
	public void test_13_chrOri() {
		Gpr.debug("Test");
		String file = "./tests/unity/vcf/test.chr1.1line.vcf";

		VcfFileIterator vcfFile = new VcfFileIterator(file);
		vcfFile.readHeader();

		String chr = null;
		for (VcfEntry vcfEntry : vcfFile)
			chr = vcfEntry.getChromosomeNameOri();

		Assert.assertEquals("chr1", chr);
	}

	@Test
	public void test_14_VcfInfoKey() {
		Gpr.debug("Test");
		new VcfOutputFormatter((List<VcfEntry>) null);
		String testIn[] = { "Hi ", "Hi how;", "Hi how;are|", "Hi how;are|you,", "Hi how;are|you,doing=", "Hi how;are|you,doing=today(.)", ".ann" };
		String testOut[] = { "Hi_", "Hi_how_", "Hi_how_are_", "Hi_how_are_you_", "Hi_how_are_you_doing_", "Hi_how_are_you_doing_today_._", "_.ann" };
		for (int i = 0; i < testIn.length; i++) {
			String safe = VcfEntry.vcfInfoKeySafe(testIn[i]);
			if (verbose) System.out.println("'" + testIn[i] + "'\t'" + safe + "'\t'" + testOut[i] + "'");
			Assert.assertEquals(testOut[i], safe);
		}
	}

	@Test
	public void test_14_VcfInfoValue() {
		Gpr.debug("Test");
		new VcfOutputFormatter((List<VcfEntry>) null);
		String testIn[] = { "Hi ", "Hi how;", "Hi how;are|", "Hi how;are|you,", "Hi how;are|you,doing=", "Hi how;are|you,doing=today(.)" };
		String testOut[] = { "Hi_", "Hi_how_", "Hi_how_are_", "Hi_how_are_you_", "Hi_how_are_you_doing_", "Hi_how_are_you_doing_today_._" };
		for (int i = 0; i < testIn.length; i++) {
			String safe = VcfEntry.vcfInfoValueSafe(testIn[i]);
			if (verbose) System.out.println("'" + testIn[i] + "'\t'" + safe + "'\t'" + testOut[i] + "'");
			Assert.assertEquals(testOut[i], safe);
		}
	}

	@Test
	public void test_15_Eff_format_version_guess() {
		Gpr.debug("Test");
		String vcfFileName = "./tests/unity/vcf/test.EFF_V2.vcf";
		EffFormatVersion formatVersion = formatVersion(vcfFileName);
		Assert.assertEquals(EffFormatVersion.FORMAT_EFF_2, formatVersion);

		vcfFileName = "./tests/unity/vcf/test.EFF_V3.vcf";
		formatVersion = formatVersion(vcfFileName);
		Assert.assertEquals(EffFormatVersion.FORMAT_EFF_3, formatVersion);
	}

	@Test
	public void test_16_indels() {
		Gpr.debug("Test");
		String vcfFile = "tests/unity/vcf/1kg.indels.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			StringBuilder variantResult = new StringBuilder();

			for (Variant v : ve.variants()) {
				if (variantResult.length() > 0) variantResult.append(",");

				String vs = v.toStringOld();
				vs = vs.substring(vs.indexOf('_') + 1);

				if (verbose) System.out.println("\t" + v + "\t" + v.toStringOld() + "\t" + vs);

				variantResult.append(vs);
			}

			String variantExpected = ve.getInfo("SEQCHANGE");

			Assert.assertEquals(variantExpected, variantResult.toString());
		}
	}

	@Test
	public void test_18_vcf_tabix() {
		Gpr.debug("Test");
		VcfFileIterator vcf = new VcfFileIterator("./tests/unity/vcf/test_tabix.vcf.gz");

		String chrpos = "";
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			chrpos += ve.getChromosomeName() + ":" + ve.getStart() + " ";
		}

		// Make sure both lines appear
		Assert.assertEquals("1:249211906 2:41612", chrpos.trim());
	}

	@Test
	public void test_22_huge_headers() {
		Gpr.debug("Test");
		String vcfFile = "tests/unity/vcf/huge_header_slow.vcf.gz";

		Timer timer = new Timer();
		timer.start();

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
		}

		Assert.assertTrue(timer.elapsed() < 1000); // We should be able to iterate the whole file in less than a second
	}

	@Test
	public void test_23_VcfUnsorted() {
		Gpr.debug("Test");
		String vcfFile = "tests/unity/vcf/out_of_order.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		vcf.setErrorIfUnsorted(true);

		boolean errorFound = false;
		String expectedErrorMessage = "VCF file '" + vcfFile + "' is not sorted, genomic position 20:2622038 is before 20:2621729";
		try {
			for (VcfEntry ve : vcf) {
				if (verbose) System.out.println(ve);
			}
		} catch (Throwable e) {
			errorFound = e.getMessage().startsWith(expectedErrorMessage);
			if (verbose) e.printStackTrace();

			if (!errorFound) {
				Gpr.debug("Error messages differ:" //
						+ "\n\tExpected : '" + expectedErrorMessage + "'" //
						+ "\n\tActual   : '" + e.getMessage() + "'" //
				);
			}
		}

		Assert.assertTrue(errorFound);
	}

	/**
	 * Parsing effect that created an exception (going from SO -> Classic)
	 */
	@Test
	public void test_24_VcfEffect_parse_SO() {
		String vcfFileName = "tests/unity/vcf/test_rasmus.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			for (VcfEffect veff : ve.getVcfEffects())
				if (verbose) System.out.println("\t\t" + veff);
		}
	}

	/**
	 * Parsing Genomic VCFs
	 * http://www.broadinstitute.org/gatk/guide/article?id=4017
	 */
	@Test
	public void test_25_Genomic_VCF() {
		String vcfFileName = "tests/unity/vcf/genomic_vcf.gvcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		int start = -1;
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			if (start < 0) start = ve.getStart();

			// Check
			if (start != ve.getStart()) throw new RuntimeException("Start position should be " + start + " instead of " + ve.getStart() + "\n" + ve);

			boolean ok = false;
			for (Variant var : ve.variants()) {
				ok |= var.getVariantType().toString().equals(ve.getInfo("Type"));
				if (verbose) System.out.println(ve + "\n\t\tSize   : " + ve.size() + "\n\t\tVariant: " + ve.isVariant() + "\n\t\tType   : " + var.getVariantType() + "\n");
			}
			if (!ok) throw new RuntimeException("Variant type should be '" + ve.getInfo("Type") + "'\n" + ve);

			start += ve.size();
		}
	}

	/**
	 * Add a new INFO and the respective header
	 */
	@Test
	public void test_26_vcfInfoHeaderAdd() {
		Gpr.debug("Test");
		String vcfFileName = "tests/unity/vcf/example_42.vcf";

		// Create a new INFO field
		String infoFieldName = "NEW_INFO";
		VcfHeaderInfo vhInfo = new VcfHeaderInfo(infoFieldName, VcfInfoType.Integer, VcfInfoNumber.UNLIMITED.toString(), "An arbitrary set of random numbers");
		String expectedHeader = "##INFO=<ID=" + infoFieldName + ",Number=.,Type=Integer,Description=\"An arbitrary set of random numbers\">";

		// Open VCF file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (vcf.isHeadeSection()) {
				// Add INFO field to header
				vcf.getVcfHeader().addInfo(vhInfo);
				if (verbose) System.out.println(vcf.getVcfHeader());
				Assert.assertTrue(vcf.getVcfHeader().toString().contains(expectedHeader));
			}

			// Add INFO field values
			String value = "" + ((int) (1000 * Math.random()));
			ve.addInfo(infoFieldName, value);
			if (verbose) System.out.println(ve);

			// Check that 'info=value' is there
			Assert.assertTrue(ve.toString().contains(infoFieldName + "=" + value));
		}
	}

	/**
	 * Add and replace an INFO header
	 */
	@Test
	public void test_27_vcfInfoHeaderReplace() {
		Gpr.debug("Test");

		String infoFieldName = "NEW_INFO";
		String vcfFileName = "tests/unity/vcf/example_42.vcf";

		// Add this header
		VcfHeaderInfo vhInfo = new VcfHeaderInfo(infoFieldName, VcfInfoType.Integer, VcfInfoNumber.UNLIMITED.toString(), "An arbitrary set of integer random numbers");
		String expectedHeader = "##INFO=<ID=" + infoFieldName + ",Number=.,Type=Integer,Description=\"An arbitrary set of integer random numbers\">";

		// Replace using this header
		VcfHeaderInfo vhInfo2 = new VcfHeaderInfo(infoFieldName, VcfInfoType.Float, "1", "One float random number");
		String expectedHeader2 = "##INFO=<ID=" + infoFieldName + ",Number=1,Type=Float,Description=\"One float random number\">";

		// Open VCF file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (vcf.isHeadeSection()) {
				// Add INFO field to header
				vcf.getVcfHeader().addInfo(vhInfo);
				if (verbose) System.out.println(vcf.getVcfHeader());
				Assert.assertTrue(vcf.getVcfHeader().toString().contains(expectedHeader));

				// Add second INFO field to header (should replace first one)
				vcf.getVcfHeader().addInfo(vhInfo2);
				if (verbose) System.out.println(vcf.getVcfHeader());
				Assert.assertTrue(vcf.getVcfHeader().toString().contains(expectedHeader2)); // New header
				Assert.assertTrue(!vcf.getVcfHeader().toString().contains(expectedHeader)); // Old header should be gone
			}

			// Add INFO field values
			String value = "" + ((int) (1000 * Math.random()));
			ve.addInfo(infoFieldName, value);
			if (verbose) System.out.println(ve);

			// Check that 'info=value' is there
			Assert.assertTrue(ve.toString().contains(infoFieldName + "=" + value));
		}
	}

	/**
	 * Add and replace an INFO header
	 */
	@Test
	public void test_28_vcfInfoReplace() {
		Gpr.debug("Test");

		String vcfFileName = "tests/unity/vcf/example_42.vcf";

		// Replace all 'DP' fields using this value
		String infoKey = "DP";
		String infoValue = "42";

		// Open VCF file
		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			String infoValuePrev = ve.getInfo(infoKey);

			// Check that 'key=value' is in INFO
			String keyValPrev = infoKey + "=" + infoValuePrev;
			Assert.assertTrue("Old key=valu is not present", ve.getInfoStr().contains(keyValPrev));

			// Replace value
			ve.addInfo(infoKey, infoValue);
			if (verbose) System.out.println(ve);

			// Check that new 'key=value' is there
			String keyVal = infoKey + "=" + infoValue;
			Assert.assertTrue("New key=value is present", ve.toString().contains(keyVal));

			// Check that previous 'key=value' is no longer there
			Assert.assertTrue("Old key=value is still in INOF field", !ve.getInfoStr().contains(keyValPrev));
		}
	}

	/**
	 * Test old effect separator '+' instead of '&'
	 */
	@Test
	public void test_29() {
		Gpr.debug("Test");
		String fileName = "./tests/unity/vcf/test_vcf_ann_plus_sign.vcf";
		VcfFileIterator vcf = new VcfFileIterator(fileName);

		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.getVcfEffects()) {
				if (verbose) System.out.println("\t" + veff);

				// Check
				Assert.assertEquals(EffectType.UTR_5_DELETED.toString(), veff.getEffectTypes().get(0).toString());
				Assert.assertEquals(EffectType.EXON_DELETED.toString(), veff.getEffectTypes().get(1).toString());
			}
		}
	}

	/**
	 * Non-variant gVCF entries (i.e. ALT=<NON_REF>)
	 */
	@Test
	public void test_30_gVCF_NON_REF() {
		Gpr.debug("Test");

		String vcfFileName = "tests/unity/vcf/test_gVCF_NON_REF.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFileName);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);

			// Check variants
			// The last variant is "<NON_REF>" which is interpreted as non-variant (it gives no information)
			int countNonVariants = 0;
			for (Variant var : ve.variants()) {
				if (verbose) System.out.println("\t" + var);
				if (!var.isVariant()) countNonVariants++;
			}
			Assert.assertEquals(1, countNonVariants);

			// Check that we can parse genotypes
			for (VcfGenotype vgt : ve.getVcfGenotypes()) {
				if (verbose) System.out.println("\t\tVCF_GT: " + vgt);
			}

			// Check GT score
			for (byte gt : ve.getGenotypesScores()) {
				if (verbose) System.out.println("\t\tGT    : " + gt);
				Assert.assertEquals(1, gt);
			}
		}
	}

	@Test
	public void test_31_MISSING_REF() {
		String vcfFile = "tests/unity/vcf/test_missing_ref.vcf";
		VcfFileIterator vcf = new VcfFileIterator(vcfFile);

		int countVariants = 0;
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			for (Variant v : ve.variants()) {
				if (verbose) System.out.println("\t" + v + "\tis Variant: " + v.isVariant());
				if (v.isVariant()) countVariants++;
			}
		}

		Assert.assertEquals(1, countVariants);
	}

	@Test
	public void test_32_VcfInfoKeyNames() {
		String keysPass[] = { "ANN", "ann9", "a9nn", "ann_", "a_nn", "_ann", "ann.gene" };
		String keysFail[] = { "ann+", "9ann", ".gene" };

		for (String key : keysPass) {
			Assert.assertTrue("String '" + key + "' should be a valid INFO key", VcfEntry.isValidInfoKey(key));
		}

		for (String key : keysFail)
			Assert.assertFalse("String '" + key + "' should be an invalid INFO key", VcfEntry.isValidInfoKey(key));
	}

	@Test
	public void test_33_translocations() {
		Gpr.debug("Test");
		String vcfFile = "tests/unity/vcf/vcf_translocation.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);

			boolean ok = false;
			for (Variant var : ve.variants()) {
				if (verbose) System.out.println("\t" + var.getVariantType() + "\t" + var);
				Assert.assertEquals("Variant type is not 'BND'", VariantType.BND, var.getVariantType());
				ok = true;
			}

			Assert.assertTrue("No variants found!", ok);
		}
	}

	@Test
	public void test_34_vcfInfoEncoding() {
		String str = "hi;hello;bye;\nadios=chau\tbye\nhi=hello\thola";
		String enc = VcfEntry.vcfInfoEncode(str);
		String dec = VcfEntry.vcfInfoDecode(enc);
		Assert.assertEquals("Encoding-Decoding cycle failed", str, dec);
	}

	@Test
	public void test_35_translocations_parsing() {
		Gpr.debug("Test");
		String vcfFile = "tests/unity/vcf/vcf_translocation_parsing.vcf";

		VcfFileIterator vcf = new VcfFileIterator(vcfFile);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve);
			boolean ok = false;
			for (Variant var : ve.variants()) {
				if (verbose) System.out.println("\t" + var.getVariantType() + "\t" + var);
				Assert.assertEquals("Variant type is not 'BND'", VariantType.BND, var.getVariantType());

				VariantBnd vbnd = (VariantBnd) var;
				Assert.assertEquals("Variant BND 'left' does not match", ve.getInfoFlag("LEFT"), vbnd.isLeft());
				Assert.assertEquals("Variant BND 'before' does not match", ve.getInfoFlag("BEFORE"), vbnd.isBefore());
				ok = true;
			}

			Assert.assertTrue("No variants found!", ok);
		}
	}

}
