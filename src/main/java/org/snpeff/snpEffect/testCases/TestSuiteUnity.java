package org.snpeff.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.snpeff.snpEffect.testCases.integration.TestCasesHgvsExon;
import org.snpeff.snpEffect.testCases.integration.TestCasesHgvsIntron;
import org.snpeff.snpEffect.testCases.unity.TestCasesAlign;
import org.snpeff.snpEffect.testCases.unity.TestCasesAnnParse;
import org.snpeff.snpEffect.testCases.unity.TestCasesApplyDel;
import org.snpeff.snpEffect.testCases.unity.TestCasesApplyIns;
import org.snpeff.snpEffect.testCases.unity.TestCasesApplyMixed;
import org.snpeff.snpEffect.testCases.unity.TestCasesApplyMnp;
import org.snpeff.snpEffect.testCases.unity.TestCasesApplySnp;
import org.snpeff.snpEffect.testCases.unity.TestCasesBinomial;
import org.snpeff.snpEffect.testCases.unity.TestCasesBuild;
import org.snpeff.snpEffect.testCases.unity.TestCasesCds;
import org.snpeff.snpEffect.testCases.unity.TestCasesChiSquare;
import org.snpeff.snpEffect.testCases.unity.TestCasesCircular;
import org.snpeff.snpEffect.testCases.unity.TestCasesCochranArmitage;
import org.snpeff.snpEffect.testCases.unity.TestCasesCodonTable;
import org.snpeff.snpEffect.testCases.unity.TestCasesCytoBands;
import org.snpeff.snpEffect.testCases.unity.TestCasesDel;
import org.snpeff.snpEffect.testCases.unity.TestCasesDels;
import org.snpeff.snpEffect.testCases.unity.TestCasesDnaNSequence;
import org.snpeff.snpEffect.testCases.unity.TestCasesDnaOverlap;
import org.snpeff.snpEffect.testCases.unity.TestCasesDnaSequence;
import org.snpeff.snpEffect.testCases.unity.TestCasesDnaSequenceByte;
import org.snpeff.snpEffect.testCases.unity.TestCasesEffectCollapse;
import org.snpeff.snpEffect.testCases.unity.TestCasesEffectCollapse2;
import org.snpeff.snpEffect.testCases.unity.TestCasesFasta;
import org.snpeff.snpEffect.testCases.unity.TestCasesFileIndexChrPos;
import org.snpeff.snpEffect.testCases.unity.TestCasesFisherExactTest;
import org.snpeff.snpEffect.testCases.unity.TestCasesGenePvalueList;
import org.snpeff.snpEffect.testCases.unity.TestCasesGenomicSequences;
import org.snpeff.snpEffect.testCases.unity.TestCasesGenotypeVector;
import org.snpeff.snpEffect.testCases.unity.TestCasesHgvs;
import org.snpeff.snpEffect.testCases.unity.TestCasesHgvsDnaDup;
import org.snpeff.snpEffect.testCases.unity.TestCasesHgvsDnaDupNegative;
import org.snpeff.snpEffect.testCases.unity.TestCasesHgvsProtDup;
import org.snpeff.snpEffect.testCases.unity.TestCasesHypergeometric;
import org.snpeff.snpEffect.testCases.unity.TestCasesIns;
import org.snpeff.snpEffect.testCases.unity.TestCasesIntStats;
import org.snpeff.snpEffect.testCases.unity.TestCasesIntergenic;
import org.snpeff.snpEffect.testCases.unity.TestCasesIntervalTree;
import org.snpeff.snpEffect.testCases.unity.TestCasesIntervalTreeArray;
import org.snpeff.snpEffect.testCases.unity.TestCasesIntervalTreeOri;
import org.snpeff.snpEffect.testCases.unity.TestCasesIntervalVariant;
import org.snpeff.snpEffect.testCases.unity.TestCasesIntervals;
import org.snpeff.snpEffect.testCases.unity.TestCasesIubString;
import org.snpeff.snpEffect.testCases.unity.TestCasesJaspar;
import org.snpeff.snpEffect.testCases.unity.TestCasesMarkerUtils;
import org.snpeff.snpEffect.testCases.unity.TestCasesMnps;
import org.snpeff.snpEffect.testCases.unity.TestCasesNmers;
import org.snpeff.snpEffect.testCases.unity.TestCasesOverlap;
import org.snpeff.snpEffect.testCases.unity.TestCasesProteinInteraction;
import org.snpeff.snpEffect.testCases.unity.TestCasesReactome;
import org.snpeff.snpEffect.testCases.unity.TestCasesSeekableReader;
import org.snpeff.snpEffect.testCases.unity.TestCasesSequenceIndexer;
import org.snpeff.snpEffect.testCases.unity.TestCasesSnps;
import org.snpeff.snpEffect.testCases.unity.TestCasesSpliceRegion;
import org.snpeff.snpEffect.testCases.unity.TestCasesSpliceSite;
import org.snpeff.snpEffect.testCases.unity.TestCasesStructuralDel;
import org.snpeff.snpEffect.testCases.unity.TestCasesStructuralDup;
import org.snpeff.snpEffect.testCases.unity.TestCasesStructuralInv;
import org.snpeff.snpEffect.testCases.unity.TestCasesStructuralTranslocations;
import org.snpeff.snpEffect.testCases.unity.TestCasesVariantDecompose;
import org.snpeff.snpEffect.testCases.unity.TestCasesVariantRealignment;
import org.snpeff.snpEffect.testCases.unity.TestCasesVcf;

/**
 * Invoke all test cases for SnpEff
 *
 * @author pcingola
 */
@RunWith(Suite.class)
@SuiteClasses({ TestCasesAlign.class, //
		TestCasesAnnParse.class, //
		TestCasesApplyDel.class, //
		TestCasesApplyIns.class, //
		TestCasesApplyMixed.class, //
		TestCasesApplyMnp.class, //
		TestCasesApplySnp.class, //
		TestCasesBinomial.class, //
		TestCasesBuild.class, //
		TestCasesChiSquare.class, //
		TestCasesCircular.class, //
		TestCasesCds.class, //
		TestCasesCochranArmitage.class, //
		TestCasesCodonTable.class, //
		TestCasesCytoBands.class, //
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
		TestCasesGenomicSequences.class, //
		TestCasesGenotypeVector.class, //
		TestCasesIntergenic.class, //
		TestCasesIntervalTree.class, //
		TestCasesIntervalTreeOri.class, //
		TestCasesIntervalTreeArray.class, //
		TestCasesIubString.class, //
		TestCasesHgvs.class, //
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
		TestCasesProteinInteraction.class, //
		TestCasesReactome.class, //
		TestCasesSeekableReader.class, //
		TestCasesSequenceIndexer.class, //
		TestCasesSnps.class, //
		TestCasesSpliceSite.class, //
		TestCasesSpliceRegion.class, //
		TestCasesStructuralDel.class, //
		TestCasesStructuralDup.class, //
		TestCasesStructuralInv.class, //
		TestCasesStructuralTranslocations.class, //
		TestCasesVariantDecompose.class, //
		TestCasesVariantRealignment.class, //
		TestCasesVcf.class //

})

public class TestSuiteUnity {

}
