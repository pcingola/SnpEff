package org.snpeff.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.snpeff.snpEffect.testCases.integration.TestCasesHgvsExon;
import org.snpeff.snpEffect.testCases.integration.TestCasesHgvsIntron;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvs;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsDnaDup;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsFrameShift;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsHard;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsIns;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsLarge;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsMnps;
import org.snpeff.snpEffect.testCases.integration.TestCasesIntegrationHgvsUpDownStream;
import org.snpeff.snpEffect.testCases.unity.TestCasesHgvsDnaDup;
import org.snpeff.snpEffect.testCases.unity.TestCasesHgvsDnaDupNegative;
import org.snpeff.snpEffect.testCases.unity.TestCasesHgvsProtDup;
import org.snpeff.snpEffect.testCases.unity.TestCasesVariantRealignment;

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
