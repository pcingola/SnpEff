package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsDnaDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsExon;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsFrameShift;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsHard;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsIntron;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsLarge;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsMnps;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsIns;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationHgvsUpDownStream;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsDnaDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsDnaDupNegative;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsProtDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesVariantRealignment;

/**
 * Invoke all test cases for SnpEff
 *
 * @author pcingola
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCasesVariantRealignment.class, //
		// TestSuiteUnity		
		TestCasesHgvsExon.class, //
		TestCasesHgvsIntron.class, //
		TestCasesHgvsDnaDup.class, //
		TestCasesHgvsDnaDupNegative.class, //
		TestCasesHgvsProtDup.class, //
		// TestSuiteIntegration
		TestCasesIntegrationHgvs.class, //
		TestCasesIntegrationHgvsDnaDup.class, //
		TestCasesIntegrationHgvsFrameShift.class, //
		TestCasesIntegrationHgvsHard.class, //
		TestCasesIntegrationHgvsLarge.class, //
		TestCasesIntegrationHgvsMnps.class, //
		TestCasesIntegrationHgvsIns.class, //
		TestCasesIntegrationHgvsUpDownStream.class, //

})
public class TestSuiteHgvs {

}
