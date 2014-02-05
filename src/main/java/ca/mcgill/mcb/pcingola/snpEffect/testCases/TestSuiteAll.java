package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Invoke all test cases for SnpEff
 * 
 * @author pcingola
 */
public class TestSuiteAll {

	public static void main(String args[]) {
		junit.textui.TestRunner.run(suite());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();

		// Stats
		suite.addTestSuite(TestCasesBinomial.class);
		suite.addTestSuite(TestCasesHypergeometric.class);
		suite.addTestSuite(TestCasesFisherExactTest.class);
		suite.addTestSuite(TestCasesChiSquare.class);
		suite.addTestSuite(TestCasesIntStats.class);
		suite.addTestSuite(TestCochranArmitage.class);
		suite.addTestSuite(TestGenePvalueList.class);

		// Binary sequences
		suite.addTestSuite(TestCasesNmers.class);
		suite.addTestSuite(TestCasesDnaSequence.class);
		suite.addTestSuite(TestCasesDnaSequenceByte.class);
		suite.addTestSuite(TestCasesDnaNSequence.class);
		suite.addTestSuite(TestCaseSequenceIndexer.class);
		suite.addTestSuite(TestCaseOverlap.class);
		suite.addTestSuite(TestCasesDnaOverlap.class);

		// Alignment
		suite.addTestSuite(TestCasesAlign.class);

		// Intervals
		suite.addTestSuite(TestCasesIntervals.class);
		suite.addTestSuite(TestCasesMarkerUtils.class);

		// Codon tables
		suite.addTestSuite(TestCasesCodonTable.class);

		// SeqChange
		suite.addTestSuite(TestCasesSeqChange.class);
		suite.addTestSuite(TestCasesTranscript.class);
		suite.addTestSuite(TestCasesSnp.class);
		suite.addTestSuite(TestCasesMnp.class);
		suite.addTestSuite(TestCasesIns.class);
		suite.addTestSuite(TestCasesDel.class);
		suite.addTestSuite(TestCasesIntervalSeqChange.class);

		suite.addTestSuite(TestCasesSnpEnsembl.class);
		suite.addTestSuite(TestCasesMissenseSilentRatio.class);
		suite.addTestSuite(TestCasesNoChange.class);
		suite.addTestSuite(TestCasesTranscriptError.class);
		suite.addTestSuite(TestCasesHugeDeletions.class);

		// Filter transcripts
		suite.addTestSuite(TestCasesFilterTranscripts.class);

		// Build databases: File format
		suite.addTestSuite(TestCasesFasta.class);
		suite.addTestSuite(TestCasesGff3.class);
		suite.addTestSuite(TestCasesGtf22.class);
		suite.addTestSuite(TestCasesRefSeq.class);
		suite.addTestSuite(TestCasesJaspar.class);
		suite.addTestSuite(TestCasesEmbl.class);

		// File formats
		suite.addTestSuite(TestCasesVcf.class);

		// Build database: Exon frame
		suite.addTestSuite(TestCasesExonFrame.class);

		// File 
		suite.addTestSuite(TestCasesSeekableReader.class);
		suite.addTestSuite(TestCasesFileIndexChrPos.class);

		// Protein coding sequences
		suite.addTestSuite(TestCasesProtein.class);

		// Loss of function and Nonsense mediated decay
		suite.addTestSuite(TestCasesLof.class);
		suite.addTestSuite(TestCasesNmd.class);

		// TestCasesGenotypeVector
		suite.addTestSuite(TestCasesGenotypeVector.class);

		// Apply changes
		suite.addTestSuite(TestCasesApply.class);

		// Cancer effects (Somatic vs Germline)
		suite.addTestSuite(TestCasesCancer.class);

		// HGSV notation
		suite.addTestSuite(TestCasesHgvs.class);

		// Nextprot
		suite.addTestSuite(TestCasesNextProt.class);

		// Motif
		suite.addTestSuite(TestCasesMotif.class);

		// Reactome
		suite.addTestSuite(TestCasesReactome.class);

		// Annotate using custom intervals
		suite.addTestSuite(TestCasesCutsomIntervals.class);

		// Sequence ontology
		suite.addTestSuite(TestCasesSequenceOntology.class);

		return suite;
	}
}
