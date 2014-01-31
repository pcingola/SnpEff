package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Test;
import junit.framework.TestSuite;

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

		suite.addTestSuite(IntegrationSnpEff.class);
		suite.addTestSuite(IntegrationSnpEffMultiThread.class);

		// Integration tets
		//		VCF header + 1 line => Should be OK after printing (check output formatter)
		//
		// 		Multi-threaded version should have exactly the same output as single threaded
		//				- test using 1 line file
		//				- test using 1M lines file
		//
		//		Broad test dataset using all genome version should be roughly the same

		//suite.addTestSuite();

		return suite;
	}
}
