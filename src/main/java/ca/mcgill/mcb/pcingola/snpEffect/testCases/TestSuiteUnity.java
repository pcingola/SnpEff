package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Test;
import junit.framework.TestSuite;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesAlign;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBinomial;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesChiSquare;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesCochranArmitage;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesCodonTable;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaNSequence;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaOverlap;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaSequence;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaSequenceByte;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesFasta;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesFileIndexChrPos;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesFisherExactTest;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesGenePvalueList;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesGenotypeVector;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsDnaDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsProtDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHypergeometric;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIntStats;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIntervals;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesJaspar;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesMarkerUtils;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesNmers;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesOverlap;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesReactome;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesSeekableReader;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesSequenceIndexer;

/**
 * Invoke all test cases for SnpEff
 *
 * @author pcingola
 */
public class TestSuiteUnity {

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		suite.addTestSuite(TestCasesAlign.class);
		suite.addTestSuite(TestCasesBinomial.class);
		suite.addTestSuite(TestCasesChiSquare.class);
		suite.addTestSuite(TestCasesCochranArmitage.class);
		suite.addTestSuite(TestCasesCodonTable.class);
		suite.addTestSuite(TestCasesDnaNSequence.class);
		suite.addTestSuite(TestCasesDnaOverlap.class);
		suite.addTestSuite(TestCasesDnaSequenceByte.class);
		suite.addTestSuite(TestCasesDnaSequence.class);
		suite.addTestSuite(TestCasesFasta.class);
		suite.addTestSuite(TestCasesFileIndexChrPos.class);
		suite.addTestSuite(TestCasesFisherExactTest.class);
		suite.addTestSuite(TestCasesGenePvalueList.class);
		suite.addTestSuite(TestCasesGenotypeVector.class);
		suite.addTestSuite(TestCasesHgvsDnaDup.class);
		suite.addTestSuite(TestCasesHgvsProtDup.class);
		suite.addTestSuite(TestCasesHypergeometric.class);
		suite.addTestSuite(TestCasesIntervals.class);
		suite.addTestSuite(TestCasesIntStats.class);
		suite.addTestSuite(TestCasesJaspar.class);
		suite.addTestSuite(TestCasesMarkerUtils.class);
		suite.addTestSuite(TestCasesNmers.class);
		suite.addTestSuite(TestCasesOverlap.class);
		suite.addTestSuite(TestCasesReactome.class);
		suite.addTestSuite(TestCasesSeekableReader.class);
		suite.addTestSuite(TestCasesSequenceIndexer.class);

		return suite;
	}
}
