package org.snpeff.snpEffect.testCases;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.snpeff.snpEffect.testCases.unity.*;

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
		TestCasesNextProt.class, //
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
