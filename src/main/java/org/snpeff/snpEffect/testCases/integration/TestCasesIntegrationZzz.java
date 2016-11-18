package org.snpeff.snpEffect.testCases.integration;

/**
 *
 * Test cases for variants
 *
 * @author pcingola
 */
public class TestCasesIntegrationZzz extends TestCasesIntegrationBase {

	public TestCasesIntegrationZzz() {
		super();
	}

	//	@Test
	//	public void testCase_05_CircularGenome_ExonsOrder() {
	//		Gpr.debug("Test");
	//		String expectedProtein = "MGSLEMVPMGAGPPSPGGDPDGYDGGNNSQYPSASGSSGNTPTP" //
	//				+ "PNDEERESNEEPPPPYEDPYWGNGDRHSDYQPLGTQDQSLYLGLQHDGNDGLPPPPYS" //
	//				+ "PRDDSSQHIYEEAGRGSMNPVCLPVIVAPYLFWLAAIAASCFTASVSTVVTATGLALS" //
	//				+ "LLLLAAVASSYAAAQRKLLTPVTVLTAVVTFFAICLTWRIEDPPFNSLLFALLAAAGG" //
	//				+ "LQGIYVLVMLVLLILAYRRRWRRLTVCGGIMFLACVLVLIVDAVLQLSPLLGAVTVVS" //
	//				+ "MTLLLLAFVLWLSSPGGLGTLGAALLTLAAALALLASLILGTLNLTTMFLLMLLWTLV" //
	//				+ "VLLICSSCSSCPLSKILLARLFLYALALLLLASALIAGGSILQTNFKSLSSTEFIPNL" //
	//				+ "FCMLLLIVAGILFILAILTEWGSGNRTYGPVFMCLGGLLTMVAGAVWLTVMSNTLLSA" //
	//				+ "WILTAGFLIFLIGFALFGVIRCCRYCCYYCLTLESEERPPTPYRNTV*";
	//
	//		// Create database & build interval forest
	//		String genomeName = "testCase";
	//		String genBankFile = "tests/Human_herpesvirus_4_uid14413.gbk.gz";
	//		SnpEffectPredictor sep = buildGeneBank(genomeName, genBankFile);
	//		sep.buildForest();
	//
	//		// Create variant
	//		Genome genome = sep.getGenome();
	//		Gene gene = genome.getGenes().getGeneByName("LMP2");
	//
	//		// Get transcript
	//		Transcript tr = gene.get("YP_401631.1");
	//		Assert.assertTrue("Transcript ID not found", tr != null);
	//
	//		if (verbose) Gpr.debug("Transcript: " + tr);
	//		String prot = tr.protein();
	//		Assert.assertEquals("Protein sequence deas not match", expectedProtein, prot);
	//	}
	//
	//	@Test
	//	public void testCase_06_CircularGenome_ExonsOrder() {
	//		Gpr.debug("Test");
	//
	//		String expectedProtein = "MALQTDTQAWRVEIGTRGLMFSNCVPLHLPEGQYHKLRLPVSAY" // 
	//				+ "EALAVARYGLVGSLWEVPAVNSALQCLAAAAPCKDVKIYPSCIFQVHAPMFVTIKTSL" // 
	//				+ "RCLNPHDLCLCLICVGAAILDIPLLCAPRDGAGARAAEGQAAAAQGGKLRVWGRLSPS" // 
	//				+ "SPTSLSLAFPYAGPPPVAWYRHSINLTRSEGVGIGKDCAQDHACPVPPQGHASSAADQ" // 
	//				+ "AGVPERGRKRAHEGPGAGEAASAGRGDVALSQSRALLWRGLGWDTGRGRLAPGLAMSR" // 
	//				+ "DAASGSVHLDIQVDRAEEGWVCDVLLEPGPPTAREGCSLSMDPGLVTLKDAWTLFPLH" // 
	//				+ "PEHDAVVPPKEEIHVMAQGHLQGGTPSLWGFTFQEAACDQWVLRPRVWTAHSPIKMTV" // 
	//				+ "YNCGHKPLHIGPSTRLGLALFWPAERSDNLDAGRIFYQLTSGELYWGRTVARPPTLTL" // 
	//				+ "PVDELRPWPKLTPEEPMQH*" //
	//		;
	//
	//		// Create database & build interval forest
	//		String genomeName = "testCase";
	//		String genBankFile = "tests/Human_herpesvirus_4_uid14413.gbk.gz";
	//		SnpEffectPredictor sep = buildGeneBank(genomeName, genBankFile);
	//		sep.buildForest();
	//
	//		// Create variant
	//		Genome genome = sep.getGenome();
	//		Gene gene = genome.getGenes().getGeneByName("LF1");
	//		Transcript tr = gene.iterator().next();
	//		String prot = tr.protein();
	//
	//		if (verbose) Gpr.debug("Transcript: " + tr);
	//		Assert.assertEquals("Protein sequence does not match", expectedProtein, prot);
	//	}

}
