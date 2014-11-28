package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import junit.framework.Test;
import junit.framework.TestSuite;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesApply;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCancer;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCodingTag;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesCutsomIntervals;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesDel;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesEff;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesEmbl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesExonFrame;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesFilterTranscripts;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGenomicSequences;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGff3;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesGtf22;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHgvs;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesHugeDeletions;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIns;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntervalVariant;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesLof;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMissenseSilentRatio;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMixedVariants;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesMotif;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesNextProt;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesNmd;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesNoChange;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesProtein;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesRefSeq;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSequenceOntology;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSnp;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesSnpEnsembl;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesTranscript;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesTranscriptError;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesVariant;
import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesVcf;
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
 * Invoke all test cases for SnpEff (unity and integration ones)
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
		suite.addTestSuite(TestCasesCochranArmitage.class);
		suite.addTestSuite(TestCasesGenePvalueList.class);

		// Binary sequences
		suite.addTestSuite(TestCasesNmers.class);
		suite.addTestSuite(TestCasesDnaSequence.class);
		suite.addTestSuite(TestCasesDnaSequenceByte.class);
		suite.addTestSuite(TestCasesDnaNSequence.class);
		suite.addTestSuite(TestCasesSequenceIndexer.class);
		suite.addTestSuite(TestCasesOverlap.class);
		suite.addTestSuite(TestCasesDnaOverlap.class);

		// Alignment
		suite.addTestSuite(TestCasesAlign.class);

		// Intervals
		suite.addTestSuite(TestCasesIntervals.class);
		suite.addTestSuite(TestCasesMarkerUtils.class);

		// Codon tables
		suite.addTestSuite(TestCasesCodonTable.class);

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
		suite.addTestSuite(TestCasesMissenseSilentRatio.class);
		suite.addTestSuite(TestCasesNoChange.class);
		suite.addTestSuite(TestCasesTranscriptError.class);
		suite.addTestSuite(TestCasesHugeDeletions.class);
		suite.addTestSuite(TestCasesCodingTag.class);

		// Filter transcripts
		suite.addTestSuite(TestCasesFilterTranscripts.class);

		// Build databases: File format
		suite.addTestSuite(TestCasesFasta.class);
		suite.addTestSuite(TestCasesGff3.class);
		suite.addTestSuite(TestCasesGtf22.class);
		suite.addTestSuite(TestCasesRefSeq.class);
		suite.addTestSuite(TestCasesJaspar.class);
		suite.addTestSuite(TestCasesEmbl.class);

		// File formats: VCF
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
		suite.addTestSuite(TestCasesHgvsDnaDup.class);

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

		// Other SnpEff 'eff' tests
		suite.addTestSuite(TestCasesEff.class);

		// Genomic sequences
		suite.addTestSuite(TestCasesGenomicSequences.class);
		return suite;
	}
}
