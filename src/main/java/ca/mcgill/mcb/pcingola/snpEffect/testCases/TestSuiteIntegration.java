package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesAnn;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegratioAnnInteract;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegratioBuildPdb;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationApply;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationCancer;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationCircularGenome;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationCodingTag;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationConfig;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationCutsomIntervals;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationDelEtc;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationEmbl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationExonFrame;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationFilterTranscripts;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationGenomicSequences;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationGff3;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationGtf22;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsDel;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsDnaDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsFrameShift;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsHard;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsIns;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsLarge;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsMnps;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsUpDownStream;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHugeDeletions;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationInsEtc;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationInsVep;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationLof;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationMissenseSilentRatio;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationMixedVariants;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationMnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationMotif;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationNextProt;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationNmd;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationNoChange;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationProtein;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationRefSeq;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationRegulation;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSequenceOntology;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnpEffMultiThread;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnpEnsembl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationTranscript;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationTranscriptError;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationVariant;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationVcfs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIntegrationErrors;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIntegrationMarkerSeq;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCasesAnn.class, //
		TestCasesIntegratioAnnInteract.class, //
		TestCasesIntegrationApply.class, //
		TestCasesIntegratioBuildPdb.class, //
		TestCasesIntegrationCancer.class, //
		TestCasesIntegrationCircularGenome.class, //
		TestCasesIntegrationCodingTag.class, //
		TestCasesIntegrationConfig.class, //
		TestCasesIntegrationCutsomIntervals.class, //
		TestCasesIntegrationDelEtc.class, //
		TestCasesIntegrationEff.class, //
		TestCasesIntegrationEmbl.class, //
		TestCasesIntegrationExonFrame.class, //
		TestCasesIntegrationErrors.class, //
		TestCasesIntegrationFilterTranscripts.class, //
		TestCasesIntegrationGenomicSequences.class, //
		TestCasesIntegrationGff3.class, //
		TestCasesIntegrationGtf22.class, //
		TestCasesIntegrationHgvs.class, //
		TestCasesIntegrationHgvsDel.class, //
		TestCasesIntegrationHgvsDnaDup.class, //
		TestCasesIntegrationHgvsFrameShift.class, //
		TestCasesIntegrationHgvsHard.class, //
		TestCasesIntegrationHgvsLarge.class, //
		TestCasesIntegrationHgvsMnps.class, //
		TestCasesIntegrationHgvsIns.class, //
		TestCasesIntegrationHgvsUpDownStream.class, //
		TestCasesIntegrationHugeDeletions.class, //
		TestCasesIntegrationInsEtc.class, //
		TestCasesIntegrationInsVep.class, //
		TestCasesIntegrationLof.class, //
		TestCasesIntegrationMarkerSeq.class, //
		TestCasesIntegrationMissenseSilentRatio.class, //
		TestCasesIntegrationMixedVariants.class, //
		TestCasesIntegrationMnp.class, //
		TestCasesIntegrationMotif.class, //
		TestCasesIntegrationNextProt.class, //
		TestCasesIntegrationNmd.class, //
		TestCasesIntegrationNoChange.class, //
		TestCasesIntegrationProtein.class, //
		TestCasesIntegrationRefSeq.class, //
		TestCasesIntegrationRegulation.class, //
		TestCasesIntegrationSequenceOntology.class, //
		TestCasesIntegrationSnpEnsembl.class, //
		TestCasesIntegrationSnp.class, //
		TestCasesIntegrationTranscriptError.class, //
		TestCasesIntegrationTranscript.class, //
		TestCasesIntegrationVariant.class, //
		TestCasesIntegrationVcfs.class, //
		// Long test cases
		TestCasesIntegrationSnpEff.class, //
		TestCasesIntegrationSnpEffMultiThread.class, //
})

public class TestSuiteIntegration {
}
