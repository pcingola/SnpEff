package org.snpeff.snpEffect.testCases.unity;

import org.snpeff.interval.*;
import org.snpeff.snpEffect.*;
import org.snpeff.util.GprSeq;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Creates a simple "genome" for testing:
 * <p>
 * Forward strand:
 * 1:1000-2000, strand: +, id:tr1, Protein
 * Exons:
 * 1:1000-1049 'exon1000', rank: 0, frame: ., sequence: tcccccccatcatagcctgaaggccccatgaataagcgtagaagacatgc
 * 1:1100-1149 'exon1100', rank: 1, frame: ., sequence: ggctcatatgtatcgttttcgtggcttcgtacgtccgcgagtatccattg
 * 1:1200-1249 'exon1200', rank: 2, frame: ., sequence: ctcctgaacttgtgtacctagaatacgtcggaaacaagcaatcaaccagc
 * 1:1300-1349 'exon1300', rank: 3, frame: ., sequence: ctcccgtctccgcaggagttttcttcgtatctcacgcttctgctaatcta
 * 1:1400-1449 'exon1400', rank: 4, frame: ., sequence: gtacggcgaatattgacaagaacggtggtatttattagaatttgcatatg
 * 1:1500-1549 'exon1500', rank: 5, frame: ., sequence: ggtactaaagtcggaggcgggtaacgtaatcgccgcgggacgggttcagc
 * 1:1600-1649 'exon1600', rank: 6, frame: ., sequence: tcgtaaacctaaagctataattttcacgacgctcgcaccgttcaaaaatc
 * 1:1700-1749 'exon1700', rank: 7, frame: ., sequence: tatcgcgggttgcgtaagtttaggtatactcctggttctggattccacat
 * 1:1800-1849 'exon1800', rank: 8, frame: ., sequence: cctaataaccagtggctccgatctggccttcttgcggcgaatttggagga
 * 1:1900-1949 'exon1900', rank: 9, frame: ., sequence: cggctgcagcgtaaatatgtcgtttaccggggacagaaggacggaagcta
 * CDS     :	tcccccccatcatagcctgaaggccccatgaataagcgtagaagacatgcggctcatatgtatcgttttcgtggcttcgtacgtccgcgagtatccattgctcctgaacttgtgtacctagaatacgtcggaaacaagcaatcaaccagcctcccgtctccgcaggagttttcttcgtatctcacgcttctgctaatctagtacggcgaatattgacaagaacggtggtatttattagaatttgcatatgggtactaaagtcggaggcgggtaacgtaatcgccgcgggacgggttcagctcgtaaacctaaagctataattttcacgacgctcgcaccgttcaaaaatctatcgcgggttgcgtaagtttaggtatactcctggttctggattccacatcctaataaccagtggctccgatctggccttcttgcggcgaatttggaggacggctgcagcgtaaatatgtcgtttaccggggacagaaggacggaagcta
 * Protein :	SPPS*PEGPMNKRRRHAAHMYRFRGFVRPRVSIAPELVYLEYVGNKQSTSLPSPQEFSSYLTLLLI*YGEY*QERWYLLEFAYGY*SRRRVT*SPRDGFSS*T*SYNFHDARTVQKSIAGCVSLGILLVLDSTS**PVAPIWPSCGEFGGRLQRKYVVYRGQKDGS?
 * <p>
 * Reverse strand:
 * 1:1000-2000, strand: -, id:tr1, Protein
 * Exons:
 * 1:1000-1049 'exon1000', rank: 1000, frame: ., sequence: tcccccccatcatagcctgaaggccccatgaataagcgtagaagacatgc
 * 1:1100-1149 'exon1100', rank: 1100, frame: ., sequence: ggctcatatgtatcgttttcgtggcttcgtacgtccgcgagtatccattg
 * 1:1200-1249 'exon1200', rank: 1200, frame: ., sequence: ctcctgaacttgtgtacctagaatacgtcggaaacaagcaatcaaccagc
 * 1:1300-1349 'exon1300', rank: 1300, frame: ., sequence: ctcccgtctccgcaggagttttcttcgtatctcacgcttctgctaatcta
 * 1:1400-1449 'exon1400', rank: 1400, frame: ., sequence: gtacggcgaatattgacaagaacggtggtatttattagaatttgcatatg
 * 1:1500-1549 'exon1500', rank: 1500, frame: ., sequence: ggtactaaagtcggaggcgggtaacgtaatcgccgcgggacgggttcagc
 * 1:1600-1649 'exon1600', rank: 1600, frame: ., sequence: tcgtaaacctaaagctataattttcacgacgctcgcaccgttcaaaaatc
 * 1:1700-1749 'exon1700', rank: 1700, frame: ., sequence: tatcgcgggttgcgtaagtttaggtatactcctggttctggattccacat
 * 1:1800-1849 'exon1800', rank: 1800, frame: ., sequence: cctaataaccagtggctccgatctggccttcttgcggcgaatttggagga
 * 1:1900-1949 'exon1900', rank: 1900, frame: ., sequence: cggctgcagcgtaaatatgtcgtttaccggggacagaaggacggaagcta
 * CDS     :	cggctgcagcgtaaatatgtcgtttaccggggacagaaggacggaagctacctaataaccagtggctccgatctggccttcttgcggcgaatttggaggatatcgcgggttgcgtaagtttaggtatactcctggttctggattccacattcgtaaacctaaagctataattttcacgacgctcgcaccgttcaaaaatcggtactaaagtcggaggcgggtaacgtaatcgccgcgggacgggttcagcgtacggcgaatattgacaagaacggtggtatttattagaatttgcatatgctcccgtctccgcaggagttttcttcgtatctcacgcttctgctaatctactcctgaacttgtgtacctagaatacgtcggaaacaagcaatcaaccagcggctcatatgtatcgttttcgtggcttcgtacgtccgcgagtatccattgtcccccccatcatagcctgaaggccccatgaataagcgtagaagacatgc
 * Protein :	RLQRKYVVYRGQKDGSYLITSGSDLAFLRRIWRISRVA*V*VYSWFWIPHS*T*SYNFHDARTVQKSVLKSEAGNVIAAGRVQRTANIDKNGGIY*NLHMLPSPQEFSSYLTLLLIYS*TCVPRIRRKQAINQRLICIVFVASYVREYPLSPPS*PEGPMNKRRRH?
 */
public class TestGenome {

    public boolean strandMinus;
    public boolean verbose;
    public Config config;
    public String genomeName;
    public Genome genome;
    public Chromosome chr;
    public Gene gene;
    public Transcript tr;
    public SnpEffectPredictor snpEffectPredictor;
    public Markers markers; // Markers to add

    public TestGenome(boolean strandMinus) {
        this.strandMinus = strandMinus;
        genomeName = "test_genome";
        markers = new Markers();
        createGenome();
        config = new Config(genome);
    }

    /**
     * Add markers to SnpEffPredictor
     */
    public void add(Marker marker) {
        markers.add(marker);
    }

    /**
     * Apply a predictor to a variant and check whether the expected number of matches are found
     */
    public void checkEffect(Variant variant, EffectType effectTypeExpected, VariantEffect.EffectImpact effectImpactExpected, int countMatchExpected) {
        createSnpEffectPredictor();
        VariantEffects veffs = snpEffectPredictor.variantEffect(variant);

        if (verbose)
            System.out.printf("Check Effect: Number of effects %d\n", veffs.size());

        int countMatch = 0;
        for (VariantEffect veff : veffs) {
            if ((veff.hasEffectType(effectTypeExpected)) && (veff.getEffectImpact() == effectImpactExpected))
                countMatch++;

            if (verbose) //
                System.out.println("Effect\t: '" + veff.toString(true, true) + "'" //
                        + "\n\t\tEffect type   : " + veff.getEffectType() //
                        + "\n\t\tEffect impact : '" + veff.getEffectImpact() + "'" //
                        + "\n\t\tCount matches : " + countMatch //
                        + "\n\t\thasEffectType : " + veff.hasEffectType(effectTypeExpected) //
                        + "\n");
        }

        assertEquals(countMatchExpected, countMatch);
    }

    /**
     * Create a simple "genome"
     */
    void createGenome() {
        Random random = new Random(20211222);
        genome = new Genome(genomeName);
        chr = new Chromosome(genome, 1, 10000000, "chr1");
        gene = new Gene(chr, 1000, 2000, strandMinus, "gene1", "gene1", BioType.protein_coding);

        // Create a transcript with exons
        tr = new Transcript(gene, gene.getStart(), gene.getEndClosed(), strandMinus, "tr1");
        tr.setProteinCoding(true);
        for (int i = gene.getStart(), rank = 1; i < gene.getEndClosed(); i += 100, rank++) {
            Exon exon = new Exon(tr, i, i + 49, strandMinus, "exon_" + rank, rank); // Exon is 50 bases (so that there are amino acids spanning across introns)
            exon.setSequence(GprSeq.randSequence(random, exon.size()));
            tr.add(exon);
        }

        gene.add(tr);
    }

    /**
     * Create a snpEff predictor and build the forest
     */
    public SnpEffectPredictor createSnpEffectPredictor() {
        snpEffectPredictor = new SnpEffectPredictor(genome);
        snpEffectPredictor.add(chr);
        snpEffectPredictor.add(gene);
        snpEffectPredictor.addAll(markers);
        snpEffectPredictor.buildForest();
        return snpEffectPredictor;
    }

}
