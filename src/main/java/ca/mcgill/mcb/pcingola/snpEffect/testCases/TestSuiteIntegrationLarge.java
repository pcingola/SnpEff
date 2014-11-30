package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsLarge;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHugeDeletions;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCasesHgvsLarge.class, //
		TestCasesHugeDeletions.class, //
})
public class TestSuiteIntegrationLarge {
}
