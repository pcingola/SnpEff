package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesAnn;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesApply;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCancer;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCircularGenome;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCodingTag;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesConfig;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCutsomIntervals;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesEmbl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesExonFrame;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesFilterTranscripts;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGenomicSequences;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGff3;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGtf22;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsDnaDupIntegration;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsHard;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsLarge;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsMnps;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHugeDeletions;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesInsEtc;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesInsVep;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnpEffMultiThread;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesLof;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMissenseSilentRatio;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMixedVariants;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMotif;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesNextProt;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesNmd;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesNoChange;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesProtein;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesRefSeq;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesRegulation;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSequenceOntology;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSnpEnsembl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesTranscript;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesTranscriptError;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesVariant;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesVcfs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesErrors;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesMarkerSeq;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCasesAnn.class, //
		TestCasesApply.class, //
		TestCasesCancer.class, //
		TestCasesCircularGenome.class, //
		TestCasesCodingTag.class, //
		TestCasesConfig.class, //
		TestCasesCutsomIntervals.class, //
		TestCasesEff.class, //
		TestCasesEmbl.class, //
		TestCasesExonFrame.class, //
		TestCasesErrors.class, //
		TestCasesFilterTranscripts.class, //
		TestCasesGenomicSequences.class, //
		TestCasesGff3.class, //
		TestCasesGtf22.class, //
		TestCasesHgvs.class, //
		TestCasesHgvsDnaDupIntegration.class, //
		TestCasesHgvsHard.class, //
		TestCasesHgvsLarge.class, //
		TestCasesHgvsMnps.class, //
		TestCasesHugeDeletions.class, //
		TestCasesInsEtc.class, //
		TestCasesInsVep.class, //
		TestCasesLof.class, //
		TestCasesMarkerSeq.class, //
		TestCasesMissenseSilentRatio.class, //
		TestCasesMixedVariants.class, //
		TestCasesMnp.class, //
		TestCasesMotif.class, //
		TestCasesNextProt.class, //
		TestCasesNmd.class, //
		TestCasesNoChange.class, //
		TestCasesProtein.class, //
		TestCasesRefSeq.class, //
		TestCasesRegulation.class, //
		TestCasesSequenceOntology.class, //
		TestCasesSnpEnsembl.class, //
		TestCasesSnp.class, //
		TestCasesTranscriptError.class, //
		TestCasesTranscript.class, //
		TestCasesVariant.class, //
		TestCasesVcfs.class, //
		TestCasesIntegrationSnpEff.class, //
		TestCasesIntegrationSnpEffMultiThread.class, //
})
public class TestSuiteIntegration {
}
