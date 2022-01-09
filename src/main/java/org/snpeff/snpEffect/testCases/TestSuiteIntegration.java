package org.snpeff.snpEffect.testCases;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
//@RunWith(Suite.class)
//@SuiteClasses({ TestCasesAnn.class, //
//		TestCasesIntegrationApply.class, //
//		TestCasesIntegrationBuildPdb.class, //
//		TestCasesIntegrationCancer.class, //
//		TestCasesIntegrationCanonical.class, //
//		TestCasesIntegrationCircularGenome.class, //
//		TestCasesIntegrationCodingTag.class, //
//		TestCasesIntegrationConfig.class, //
//		TestCasesIntegrationCovid19.class, //
//		TestCasesIntegrationCutsomIntervals.class, //
//		TestCasesIntegrationDelEtc.class, //
//		TestCasesIntegrationDup.class, //
//		TestCasesIntegrationEff.class, //
//		TestCasesIntegrationEmbl.class, //
//		TestCasesIntegrationExonFrame.class, //
//		TestCasesIntegrationErrors.class, //
//		TestCasesIntegrationFilterTranscripts.class, //
//		TestCasesIntegrationGenBank.class, //
//		TestCasesIntegrationGenomicSequences.class, //
//		TestCasesIntegrationGff3.class, //
//		TestCasesIntegrationGtf22.class, //
//		TestCasesIntegrationHgvs.class, //
//		TestCasesIntegrationHgvsDel.class, //
//		TestCasesIntegrationHgvsDnaDup.class, //
//		TestCasesIntegrationHgvsFrameShift.class, //
//		TestCasesIntegrationHgvsHard.class, //
//		TestCasesIntegrationHgvsLarge.class, //
//		TestCasesIntegrationHgvsMnps.class, //
//		TestCasesIntegrationHgvsIns.class, //
//		TestCasesIntegrationHgvsUpDownStream.class, //
//		TestCasesIntegrationLargeDeletion.class, //
//		TestCasesIntegrationInsEtc.class, //
//		TestCasesIntegrationInsVep.class, //
//		TestCasesIntegrationLof.class, //
//		TestCasesIntegrationMarkerSeq.class, //
//		TestCasesIntegrationMissenseSilentRatio.class, //
//		TestCasesIntegrationMixedVariants.class, //
//		TestCasesIntegrationMnp.class, //
//		TestCasesIntegrationMotif.class, //
//		TestCasesIntegrationNextProt.class, //
//		TestCasesIntegrationNmd.class, //
//		TestCasesIntegrationNoChange.class, //
//		TestCasesIntegrationProtein.class, //
//		TestCasesIntegrationProteinInteraction.class, //
//		TestCasesIntegrationRefSeq.class, //
//		TestCasesIntegrationRegulation.class, //
//		TestCasesIntegrationSequenceOntology.class, //
//		TestCasesIntegrationSnpEnsembl.class, //
//		TestCasesIntegrationSnp.class, //
//		TestCasesIntegrationSpliceRegion.class, //
//		TestCasesIntegrationStructural.class, //
//		TestCasesIntegrationTranscriptError.class, //
//		TestCasesIntegrationTranscript.class, //
//		TestCasesIntegrationVariant.class, //
//		TestCasesIntegrationVcfs.class, //
//		// Long test cases
//		TestCasesIntegrationSnpEff.class, //
//		TestCasesIntegrationSnpEffMultiThread.class, //
//})

@Suite
@SuiteDisplayName("Unit test cases")
@SelectPackages({"org.snpeff.snpEffect.testCases.integration"})
public class TestSuiteIntegration {
}
