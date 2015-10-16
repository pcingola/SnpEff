package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesAnn;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsExon;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvsIntron;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesAlign;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesApplyDel;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesApplyIns;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesApplyMixed;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesApplyMnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesApplySnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBinomial;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesBuild;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesCds;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesChiSquare;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesCochranArmitage;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesCodonTable;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDel;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDels;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaNSequence;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaOverlap;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaSequence;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesDnaSequenceByte;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesEffectCollapse;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesEffectCollapse2;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesFasta;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesFileIndexChrPos;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesFisherExactTest;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesGenePvalueList;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesGenotypeVector;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsDnaDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsDnaDupNegative;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHgvsProtDup;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesHypergeometric;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIns;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIntStats;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIntervalVariant;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesIntervals;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesJaspar;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesMarkerUtils;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesMnps;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesNmers;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesOverlap;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesReactome;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesSeekableReader;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesSequenceIndexer;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesSnps;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesSpliceRegion;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesSpliceSite;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesVariantDecompose;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesVariantRealignment;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.unity.TestCasesVcf;

/**
 * Invoke all test cases for SnpEff
 *
 * @author pcingola
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCasesAlign.class, //
		TestCasesAnn.class, //
		TestCasesApplyDel.class, //
		TestCasesApplyIns.class, //
		TestCasesApplyMixed.class, //
		TestCasesApplyMnp.class, //
		TestCasesApplySnp.class, //
		TestCasesBinomial.class, //
		TestCasesBuild.class, //
		TestCasesChiSquare.class, //
		TestCasesCds.class, //
		TestCasesCochranArmitage.class, //
		TestCasesCodonTable.class, //
		TestCasesDels.class, //
		TestCasesDel.class, //
		TestCasesDnaNSequence.class, //
		TestCasesDnaOverlap.class, //
		TestCasesDnaSequenceByte.class, //
		TestCasesDnaSequence.class, //
		TestCasesEffectCollapse.class, //
		TestCasesEffectCollapse2.class, //
		TestCasesFasta.class, //
		TestCasesFileIndexChrPos.class, //
		TestCasesFisherExactTest.class, //
		TestCasesGenePvalueList.class, //
		TestCasesGenotypeVector.class, //
		TestCasesIubString.class, //
		TestCasesHgvsExon.class, //
		TestCasesHgvsIntron.class, //
		TestCasesHgvsDnaDup.class, //
		TestCasesHgvsDnaDupNegative.class, //
		TestCasesHgvsProtDup.class, //
		TestCasesHypergeometric.class, //
		TestCasesIntervals.class, //
		TestCasesIntervalVariant.class, //
		TestCasesIns.class, //
		TestCasesIntStats.class, //
		TestCasesJaspar.class, //
		TestCasesMarkerUtils.class, //
		TestCasesMnps.class, //
		TestCasesNmers.class, //
		TestCasesOverlap.class, //
		TestCasesReactome.class, //
		TestCasesSeekableReader.class, //
		TestCasesSequenceIndexer.class, //
		TestCasesSnps.class, //
		TestCasesSpliceSite.class, //
		TestCasesSpliceRegion.class, //
		TestCasesVariantDecompose.class, //
		TestCasesVariantRealignment.class, //
		TestCasesVcf.class //

})

public class TestSuiteUnity {

}
