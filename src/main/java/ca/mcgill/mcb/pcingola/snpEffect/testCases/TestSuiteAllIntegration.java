package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Test;
import junit.framework.TestSuite;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCancer;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCodingTag;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCutsomIntervals;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesDel;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesEmbl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesExonFrame;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesFilterTranscripts;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGenomicSequences;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGff3;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGtf22;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHugeDeletions;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIns;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationSnpEffMultiThread;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntervalVariant;
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
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSequenceOntology;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSnpEnsembl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesTranscript;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesTranscriptError;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesVariant;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesVcf;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
public class TestSuiteAllIntegration {

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTestSuite(TestCasesCancer.class);
		suite.addTestSuite(TestCasesCodingTag.class);
		suite.addTestSuite(TestCasesCutsomIntervals.class);
		suite.addTestSuite(TestCasesDel.class);
		suite.addTestSuite(TestCasesEff.class);
		suite.addTestSuite(TestCasesEmbl.class);
		suite.addTestSuite(TestCasesExonFrame.class);
		suite.addTestSuite(TestCasesFilterTranscripts.class);
		suite.addTestSuite(TestCasesGenomicSequences.class);
		suite.addTestSuite(TestCasesGff3.class);
		suite.addTestSuite(TestCasesGtf22.class);
		suite.addTestSuite(TestCasesHgvs.class);
		suite.addTestSuite(TestCasesHugeDeletions.class);
		suite.addTestSuite(TestCasesIns.class);
		suite.addTestSuite(TestCasesIntervalVariant.class);
		suite.addTestSuite(TestCasesLof.class);
		suite.addTestSuite(TestCasesMissenseSilentRatio.class);
		suite.addTestSuite(TestCasesMixedVariants.class);
		suite.addTestSuite(TestCasesMnp.class);
		suite.addTestSuite(TestCasesMotif.class);
		suite.addTestSuite(TestCasesNextProt.class);
		suite.addTestSuite(TestCasesNmd.class);
		suite.addTestSuite(TestCasesNoChange.class);
		suite.addTestSuite(TestCasesProtein.class);
		suite.addTestSuite(TestCasesRefSeq.class);
		suite.addTestSuite(TestCasesSequenceOntology.class);
		suite.addTestSuite(TestCasesSnpEnsembl.class);
		suite.addTestSuite(TestCasesSnp.class);
		suite.addTestSuite(TestCasesTranscriptError.class);
		suite.addTestSuite(TestCasesTranscript.class);
		suite.addTestSuite(TestCasesVariant.class);
		suite.addTestSuite(TestCasesVcf.class);

		// Integration tets
		//		VCF header + 1 line => Should be OK after printing (check output formatter)
		//
		// 		Multi-threaded version should have exactly the same output as single threaded
		//				- test using 1 line file
		//				- test using 1M lines file
		//
		//		Broad test dataset using all genome version should be roughly the same
		suite.addTestSuite(TestCasesIntegrationSnpEff.class);
		suite.addTestSuite(TestCasesIntegrationSnpEffMultiThread.class);

		return suite;
	}
}
