package org.snpeff.snpEffect.testCases;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Invoke all integration test cases
 *
 * @author pcingola
 */
@Suite
@SuiteDisplayName("Integration test cases")
@SelectPackages({"org.snpeff.snpEffect.testCases.integration"})
public class TestSuiteIntegration {
}
