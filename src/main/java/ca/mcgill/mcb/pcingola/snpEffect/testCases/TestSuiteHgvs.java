package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsDnaDupIntegration;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsExon;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsFrameShift;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsHard;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsIntron;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsLarge;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsMnps;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsProtDups;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsUpDownStream;
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
		TestCasesHgvs.class, //
		TestCasesHgvsDnaDupIntegration.class, //
		TestCasesHgvsFrameShift.class, //
		TestCasesHgvsHard.class, //
		TestCasesHgvsLarge.class, //
		TestCasesHgvsMnps.class, //
		TestCasesHgvsProtDups.class, //
		TestCasesHgvsUpDownStream.class, //

})
public class TestSuiteHgvs {

}
