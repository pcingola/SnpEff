package org.snpeff.snpEffect.testCases.unity;

import org.snpeff.interval.*;
import org.snpeff.snpEffect.Config;
import org.snpeff.snpEffect.SnpEffectPredictor;

/**
 * Test case
 * <p>
 * Transcript:
 * 1:0-999, strand: +, id:transcript1, Protein
 * Exons:
 * 1:100-199 'exon1', rank: 1, frame: ., sequence: atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg
 * 1:300-399 'exon2', rank: 2, frame: ., sequence: tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg
 * 1:900-999 'exon3', rank: 0, frame: ., sequence: ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg
 * CDS     :	atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatggtgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacgggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg
 * Protein :	MSAGEGIHAARILMLPRWILSEIWCPGREGHIMVFGNSRARFCSKLNWQLGINPDPIVTHGSIHPQRVEEKHLTPIEQDLFRNTLYRLPIYLIPHIYFIG
 * <p>
 * <p>
 * Transcript (full coordinates):
 * 0
 * 0                                                                                                   1                                                                                                   2                                                                                                   3                                                                                                   4                                                                                                   5                                                                                                   6                                                                                                   7                                                                                                   8                                                                                                   9
 * 0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9         0         1         2         3         4         5         6         7         8         9
 * 0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789
 * ....................................................................................................atgtccgcaggtgaaggcatacacgctgcgcgtatactgatgttacctcgatggattttgtcagaaatatggtgcccaggacgcgaagggcatattatgg....................................................................................................tgtttgggaattcacgggcacggttctgcagcaagctgaattggcagctcggcataaatcccgaccccatcgtcacgcacggatcaattcatcctcaacg....................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................................ggtagaggaaaagcacctaacccccattgagcaggatctctttcgtaatactctgtatcgattaccgatttatttgattccccacatttatttcatcggg
 * M  S  A  G  E  G  I  H  A  A  R  I  L  M  L  P  R  W  I  L  S  E  I  W  C  P  G  R  E  G  H  I  M                                                                                                      V  F  G  N  S  R  A  R  F  C  S  K  L  N  W  Q  L  G  I  N  P  D  P  I  V  T  H  G  S  I  H  P  Q                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      R  V  E  E  K  H  L  T  P  I  E  Q  D  L  F  R  N  T  L  Y  R  L  P  I  Y  L  I  P  H  I  Y  F  I  G
 * 0120120120120120120120120120120120120120120120120120120120120120120120120120120120120120120120120120                                                                                                    1201201201201201201201201201201201201201201201201201201201201201201201201201201201201201201201201201                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    2012012012012012012012012012012012012012012012012012012012012012012012012012012012012012012012012012
 * ---------------------------------------------------------------------------------------------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>---------------------------------------------------------------------------------------------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------->>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
 * |                                                                                                  |                                                                                                    |                                                                                                  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |                                                                                                  |
 * |                                                                                                  |                                                                                                    |                                                                                                  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |                                                                                                  |^999
 * |                                                                                                  |                                                                                                    |                                                                                                  |                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |^900
 * |                                                                                                  |                                                                                                    |                                                                                                  |^399
 * |                                                                                                  |                                                                                                    |^300
 * |                                                                                                  |^199
 * |^100
 */
public class TestCasesBaseApply extends TestCasesBase {

    public TestCasesBaseApply() {
        super();
    }

    @Override
    protected void initSnpEffPredictor() {
        // Create a config and force out snpPredictor
        if (config == null || config.getGenome() == null || !config.getGenome().getGenomeName().equals(genomeName)) {
            config = new Config(genomeName, Config.DEFAULT_CONFIG_FILE);
        }

        // Create predictor
        genome = config.getGenome();
        snpEffectPredictor = new SnpEffectPredictor(genome);

        // Chromosome sequence
        chromosome = new Chromosome(genome, 0, 1000, "1");
        chromoSequence = "ATTGGCTCGACGCTCATTCACTCCAACAGCCCGGGACCCCCGCTCAATTATTTCACTCACCGGGAAAATTGTACCGATTGTCCGTGCCTTACTTCAAATGATGTCCGCAGGTGAAGGCATACACGCTGCGCGTATACTGATGTTACCTCGATGGATTTTGTCAGAAATATGGTGCCCAGGACGCGAAGGGCATATTATGGTTGCGCGAAGACATCATTTTGGAACTAACTACTAGAACTAATCAGTAAACATCCTACTGGACGGCTTGCCCCGCGATTCAAACCGCTAACTTTATCGTCCTGTTTGGGAATTCACGGGCACGGTTCTGCAGCAAGCTGAATTGGCAGCTCGGCATAAATCCCGACCCCATCGTCACGCACGGATCAATTCATCCTCAACGAAAGGGAGCTAGCGCTGTACGGCCACGGGAGGGTGTGCACCATATTCAACGACTTCTTAACCCGACCTTAAACCAATCTTCTTACGAATGTGCCGTCGAGCGGCACCTTTCAGATTTCAGTGTTGCAACTCTTACCAGTGCACTTAAACACTCCCTCAAATCACAGGCCTTGTTCTATAGCGCTCAGCACGTCGCCAGGATGCTGGTACGCCGGACTGTCCAGATACCGTTAGCACGGCATAGGGAGGATCGCGCAGGCTATACCCGATATCGGTTGGGCATCCTTTAATTCTTTGCGGATGCGAATACCCGTCACCCCTTGCGATTCTGTTTAACGCAGACTCAACCTAACGATTGACCTACATAGTAATGAGTTTTGTTGGTCCGTAAGACTTCGCCCAAAACCGCGCATGGTGGTCGTAGAAACGTACTCACAGGCCACTAAATCCGCTTAGGACATACAGTCTCCTTCGGTCACATTAACCCCATATGATTAGTACGGTAGAGGAAAAGCACCTAACCCCCATTGAGCAGGATCTCTTTCGTAATACTCTGTATCGATTACCGATTTATTTGATTCCCCACATTTATTTCATCGGG"; // A";
        chromoBases = chromoSequence.toCharArray();

        codonTable = genome.codonTable();

        // Create gene, trancript and exons
        gene = new Gene(chromosome, 0, 999, false, "gene1", "gene1", BioType.protein_coding);
        transcript = new Transcript(gene, gene.getStart(), gene.getEndClosed(), gene.isStrandMinus(), "transcript1");
        transcript.setProteinCoding(true);

        Exon exons[] = new Exon[3];
        exons[0] = new Exon(transcript, 100, 199, false, "exon1", 0);
        exons[1] = new Exon(transcript, 300, 399, false, "exon2", 0);
        exons[2] = new Exon(transcript, 900, 999, false, "exon3", 0);

        for (Exon ex : exons) {
            ex.setSequence(chromoSequence.substring(ex.getStart(), ex.getEndClosed() + 1));
            transcript.add(ex);
        }

        // Create genomic sequences
        genome.getGenomicSequences().addGeneSequences(chromosome.getId(), chromoSequence);

        // Set predictor parameters
        snpEffectPredictor.setSpliceRegionExonSize(spliceRegionExonSize);
        snpEffectPredictor.setSpliceRegionIntronMin(spliceRegionIntronMin);
        snpEffectPredictor.setSpliceRegionIntronMax(spliceRegionIntronMax);
        snpEffectPredictor.setUpDownStreamLength(0);

        // Update config
        config.setSnpEffectPredictor(snpEffectPredictor);
        config.setHgvsShift(shiftHgvs);

        // Build forest
        snpEffectPredictor.buildForest();
    }
}
