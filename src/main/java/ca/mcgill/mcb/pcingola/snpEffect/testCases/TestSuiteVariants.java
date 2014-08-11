package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Invoke all test cases for SnpEff
 *
 * @author pcingola
 */
public class TestSuiteVariants {

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Quick test
		TestCasesTranscript.N = TestCasesSnp.N = TestCasesMnp.N = TestCasesIns.N = TestCasesDel.N = 50;

		// Variants
		suite.addTestSuite(TestCasesVariant.class);
		suite.addTestSuite(TestCasesTranscript.class);
		suite.addTestSuite(TestCasesSnp.class);
		suite.addTestSuite(TestCasesMnp.class);
		suite.addTestSuite(TestCasesIns.class);
		suite.addTestSuite(TestCasesDel.class);
		suite.addTestSuite(TestCasesIntervalVariant.class);
		suite.addTestSuite(TestCasesMixedVariants.class);

		suite.addTestSuite(TestCasesSnpEnsembl.class);

		// Other SnpEff 'eff' tests
		suite.addTestSuite(TestCasesEff.class);

		return suite;
	}
}
