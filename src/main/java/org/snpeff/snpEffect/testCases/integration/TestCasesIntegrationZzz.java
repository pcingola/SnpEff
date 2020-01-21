package org.snpeff.snpEffect.testCases.integration;

/**
 * Test case
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	long randSeed = 20100629;
	String genomeName = "testCase";

	public TestCasesIntegrationZzz() {
		super();
	}

	//	/**
	//	 * Test improved exon frame correction in UCSC references
	//	 */
	//	@Test
	//	public void test_02() {
	//		Gpr.debug("Test");
	//		//---
	//		/// Build SnpEffectPredictor using a RefSeq file
	//		//---
	//		String genome = "testNM_015296";
	//		String args[] = { "build", genome };
	//		SnpEff snpeff = new SnpEff(args);
	//		snpeff.setDebug(debug);
	//		snpeff.setVerbose(verbose);
	//
	//		// Build database
	//		SnpEffCmdBuild snpeffBuild = (SnpEffCmdBuild) snpeff.cmd();
	//		snpeffBuild.setStoreAlignments(true);
	//		snpeffBuild.setCheckNumOk(false);
	//		snpeffBuild.run();
	//
	//		//---
	//		// Make sure the alignment matches on most bases after exon rank 49
	//		//---
	//		HashMap<String, SmithWaterman> alignmentByTrId = snpeffBuild.getSnpEffCmdProtein().getAlignmentByTrId();
	//		SmithWaterman sw = alignmentByTrId.get("NM_015296.2");
	//		if (debug) Gpr.debug(sw.getAlignmentScore() + "\n" + sw);
	//		Assert.assertTrue(sw.getAlignmentScore() >= 2061);
	//	}
	//
	//	@Test
	//	public void test_02_ann() {
	//		Gpr.debug("Test");
	//		// Note: Normally this EffectImpact should be 'HIGH' impact, but
	//		// since the database we build in test_01_build is small, there
	//		// are not enough stats.
	//		checkNextProt("testHg3770Chr22" //
	//				, path("test_nextProt_02.vcf")//
	//				, "amino_acid_modification:N-acetylglycine"//
	//				, EffectImpact.LOW //
	//				, true //
	//		);
	//	}
	//
	//	@Test
	//	public void test_03_ann() {
	//		Gpr.debug("Test");
	//		// Note: Normally this EffectImpact should be 'MODERATE' impact, but
	//		// since the database we build in test_01_build is small, there are
	//		// not enough stats.
	//		checkNextProt("testHg3770Chr22" //
	//				, path("test_nextProt_03.vcf") //
	//				, "amino_acid_modification:Phosphoserine" //
	//				, EffectImpact.MODERATE //
	//				, true //
	//		);
	//	}
	//
	//	@Test
	//	public void test_04_Vep() throws IOException {
	//		Gpr.debug("Test");
	//		compareVepSO("testENST00000398332", path("testENST00000398332.Ins.04.vcf"), "ENST00000398332");
	//	}
	//
	//	/**
	//	 * Deletion creates a gene fusion
	//	 */
	//	@Test
	//	public void test_05_fusion() {
	//		Gpr.debug("Test");
	//		String genome = "hg19";
	//		String vcf = path("test_fusion_ROS1-SLC34A2.vcf");
	//
	//		String args[] = { "-noLog", "-ud", "0", genome, vcf };
	//		SnpEff snpEff = new SnpEff(args);
	//		snpEff.setVerbose(verbose);
	//		snpEff.setSupressOutput(!verbose);
	//		snpEff.setDebug(debug);
	//
	//		SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
	//		boolean checked = false;
	//		List<VcfEntry> vcfEntries = seff.run(true);
	//		for (VcfEntry ve : vcfEntries) {
	//			if (verbose) System.out.println(ve);
	//			for (VcfEffect veff : ve.getVcfEffects()) {
	//				if (verbose) System.out.println("\t\t" + veff);
	//				if (veff.getEffectType() == EffectType.GENE_FUSION) {
	//					Assert.assertEquals("Impact does not match", EffectImpact.HIGH, veff.getImpact());
	//					Assert.assertEquals("Affected genes do not match", "ROS1&SLC34A2", veff.getGeneId());
	//					checked = true;
	//				}
	//			}
	//		}
	//		Assert.assertTrue("No translocation found", checked);
	//	}
	//
	//	/**
	//	 * Test SNP effect predictor for a transcript (Insertions)
	//	 */
	//	@Test
	//	public void test_21() {
	//		Gpr.debug("Test");
	//		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
	//		String trId = "ENST00000250823";
	//		comp.snpEffect(path(trId + "_InDels.out"), trId, true);
	//	}
	//
	//	/**
	//	 * Test SNP effect predictor for a transcript (Insertions)
	//	 */
	//	@Test
	//	public void test_21_2() {
	//		Gpr.debug("Test");
	//		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
	//		String trId = "ENST00000250823";
	//		comp.snpEffect(path(trId + "_InDels_2.out"), trId, true);
	//	}
	//
	//	/**
	//	 * Test SNP effect predictor for a transcript (Insertions)
	//	 */
	//	@Test
	//	public void test_21_3() {
	//		Gpr.debug("Test");
	//		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
	//		String trId = "ENST00000250823";
	//		comp.setUseAaNoNum(true);
	//		comp.snpEffect(path(trId + "_InDels_3.out"), trId, true);
	//	}
	//
	//	/**
	//	 * Test SNP effect predictor for a transcript (Insertions)
	//	 */
	//	@Test
	//	public void test_23_MNP_on_exon_edge() {
	//		Gpr.debug("Test");
	//		CompareEffects comp = new CompareEffects(genomeName, randSeed, verbose);
	//		String trId = "ENST00000250823";
	//		comp.setUseAaNoNum(true);
	//		comp.snpEffect(path(trId + "_mnp_out_of_exon.txt"), trId, true);
	//	}

}
