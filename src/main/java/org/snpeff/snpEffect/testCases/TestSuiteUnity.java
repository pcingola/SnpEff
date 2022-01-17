package org.snpeff.snpEffect.testCases;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Invoke all Unit test cases for SnpEff
 * @author pcingola
 */
@Suite
@SuiteDisplayName("Unit test cases")
@SelectPackages({"org.snpeff.snpEffect.testCases.unity"})
public class TestSuiteUnity {
}
