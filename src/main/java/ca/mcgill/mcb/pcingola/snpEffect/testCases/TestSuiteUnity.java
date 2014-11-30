package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

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
@RunWith(Suite.class)
@SuiteClasses({ TestCasesAlign.class, //
	TestCasesBinomial.class, //
	TestCasesChiSquare.class, //
	TestCasesCochranArmitage.class, //
	TestCasesCodonTable.class, //
	TestCasesDnaNSequence.class, //
	TestCasesDnaOverlap.class, //
	TestCasesDnaSequenceByte.class, //
	TestCasesDnaSequence.class, //
	TestCasesFasta.class, //
	TestCasesFileIndexChrPos.class, //
	TestCasesFisherExactTest.class, //
	TestCasesGenePvalueList.class, //
	TestCasesGenotypeVector.class, //
	TestCasesHgvsDnaDup.class, //
	TestCasesHgvsProtDup.class, //
	TestCasesHypergeometric.class, //
	TestCasesIntervals.class, //
	TestCasesIntStats.class, //
	TestCasesJaspar.class, //
	TestCasesMarkerUtils.class, //
	TestCasesNmers.class, //
	TestCasesOverlap.class, //
	TestCasesReactome.class, //
	TestCasesSeekableReader.class, //
	TestCasesSequenceIndexer.class //
})
public class TestSuiteUnity {

}
