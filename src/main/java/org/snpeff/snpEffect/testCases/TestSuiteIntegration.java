package org.snpeff.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.snpeff.snpEffect.testCases.integration.TestCasesAnn;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegratioBuildPdb;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationApply;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationCancer;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationCircularGenome;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationCodingTag;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationConfig;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationCutsomIntervals;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationDelEtc;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationDup;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationEff;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationEmbl;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationErrors;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationExonFrame;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationFilterTranscripts;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationGenomicSequences;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationGff3;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationGtf22;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvs;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsDel;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsDnaDup;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsFrameShift;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsHard;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsIns;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsLarge;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsMnps;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsUpDownStream;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHugeDeletions;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationInsEtc;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationInsVep;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationLof;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationMarkerSeq;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationMissenseSilentRatio;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationMixedVariants;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationMnp;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationMotif;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationNextProt;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationNmd;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationNoChange;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationProtein;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationProteinInteraction;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationRefSeq;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationRegulation;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationSequenceOntology;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationSnp;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationSnpEff;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationSnpEffMultiThread;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationSnpEnsembl;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationStructural;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationTranscript;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationTranscriptError;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationVariant;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationVcfs;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCasesAnn.class, //
		TestCasesIntegrationApply.class, //
		TestCasesIntegratioBuildPdb.class, //
		TestCasesIntegrationCancer.class, //
		TestCasesIntegrationCircularGenome.class, //
		TestCasesIntegrationCodingTag.class, //
		TestCasesIntegrationConfig.class, //
		TestCasesIntegrationCutsomIntervals.class, //
		TestCasesIntegrationDelEtc.class, //
		TestCasesIntegrationDup.class, //
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
		TestCasesIntegrationProteinInteraction.class, //
		TestCasesIntegrationRefSeq.class, //
		TestCasesIntegrationRegulation.class, //
		TestCasesIntegrationSequenceOntology.class, //
		TestCasesIntegrationSnpEnsembl.class, //
		TestCasesIntegrationSnp.class, //
		TestCasesIntegrationStructural.class, //
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
