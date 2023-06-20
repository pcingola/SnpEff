package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.interval.*;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.snpEffect.SnpEffectPredictor;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffects;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEffect;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test case
 */
public class TestCasesIntegrationCircularGenome extends TestCasesIntegrationBase {

    public TestCasesIntegrationCircularGenome() {
        super();
    }

    @Test
    public void testCase_01_CircularGenome() {
        Log.debug("Test");

        // Create database & build interval forest
        String genomeName = "testCase";
        String genBankFile = path("genes_circular.gbk");
        SnpEffectPredictor sep = buildGeneBank(genomeName, genBankFile);
        sep.buildForest();

        // Create variant
        Genome genome = sep.getGenome();
        Variant var = new Variant(genome.getChromosome("chr"), 2, "", "TATTTTTCAG", "");

        // Calculate effect
        // This should NOT throw an exception ("Interval has negative coordinates.")
        VariantEffects varEffs = sep.variantEffect(var);
        for (VariantEffect varEff : varEffs) {
            VcfEffect vcfEff = new VcfEffect(varEff, EffFormatVersion.FORMAT_ANN_1);
            if (verbose) Log.info("\t" + vcfEff);
        }
    }

    @Test
    public void testCase_02_CircularGenome() {
        Log.debug("Test");

        Log.silenceWarning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR);
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);

        //---
        // Create database & build interval forest
        //---
        String genomeName = "test_circular_GCA_000210475.1.22";
        SnpEffectPredictor sep = build(genomeName);
        sep.buildForest();

        if (verbose) {
            Genome genome = sep.getGenome();
            for (Chromosome chr : genome.getChromosomes())
                System.out.println(chr);
        }

        // Check if chr is circular
        Chromosome chr = sep.getGenome().getChromosome("p948");
        if (verbose) Log.info("Chromosome:" + chr);
        assertTrue(chr.isCircular(), "Chromosome is not circular");

        // Check protien sequence
        String protein = "MDTSLAHENARLRALLQTQQDTIRQMAEYNRLLSQRVAAYASEINRLKALVAKLQRMQFGKSSEKLRAKTERQIQEAQERISALQEEMAETLGEQYDPVLPSALRQSSARKPLPASLPRETRVIRPEEECCPACGGELSSLGCDVSEQLELISSAFKVIETQRPKQACCRCDHIVQAPVPSKPIARSYAGAGLLAHVVTGKYADHLPLYRQSEIYRRQGVELSRATLGRWTGAVAELLEPLYDVLRQYVLMPGKVHADDIPVPVQEPGSGKTRTARLWVYVRDDRNAGSQMPPAVWFAYSPDRKGIHPQNHLAGYSGVLQADAYGGYRALYESGRITEAACMAHARRKIHDVHARAPTYITTEALQRIGELYAIEAEVRGCSAEQRLAARKARAAPLMQSLYDWIQQQMKTLSRHSDTAKAFAYLLKQWDALNVYCSNGWVEIDNNIAENALRGVAVGRKNWMFAGSDSGGEHAAVLYSLIGTCRLNNVEPEKWLRYVIEHIQDWPANRVRDLLPWKVDLSSQ*";
        for (Gene g : sep.getGenome().getGenes()) {
            for (Transcript tr : g) {
                assertEquals(protein, tr.protein());
                if (verbose) Log.info(tr);
            }
        }

        //---
        // Check variants in zero or negative coordiantes
        //---
        checkAnnotations(sep, "p948", 0, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
        checkAnnotations(sep, "p948", -3, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
        checkAnnotations(sep, "p948", -885, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");

        //---
        // Check variant after chromosome end (same variants as before)
        //---
        checkAnnotations(sep, "p948", 94797, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
        checkAnnotations(sep, "p948", 94794, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
        checkAnnotations(sep, "p948", 93912, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");
    }

    @Test
    public void testCase_02_CircularGenome_end() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR);
        Log.silenceWarning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND);

        //---
        // Create database & build interval forest
        //---
        String genomeName = "test_circular_GCA_000210475.1.22_end";
        SnpEffectPredictor sep = build(genomeName);
        sep.buildForest();

        if (verbose) {
            Genome genome = sep.getGenome();
            for (Chromosome chr : genome.getChromosomes())
                Log.info(chr);
        }

        //---
        // Check variants in zero or negative coordinates
        //---
        checkAnnotations(sep, "p948", 0, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
        checkAnnotations(sep, "p948", -3, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
        checkAnnotations(sep, "p948", -885, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");

        //---
        // Check variant after chromosome end (same variants as before)
        //---
        checkAnnotations(sep, "p948", 94797, "T", "A", "p.Phe297Ile", "c.889T>A", "missense_variant");
        checkAnnotations(sep, "p948", 94794, "T", "A", "p.Trp296Arg", "c.886T>A", "missense_variant");
        checkAnnotations(sep, "p948", 93912, "G", "T", "p.Asp2Tyr", "c.4G>T", "missense_variant");
    }

    @Test
    public void testCase_03_CircularGenome() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR);

        String prot = "MQTECSAGAYEFPASCGRRVVARFDGGRMSSDGGVILVKQADDILGLSRRF" //
                + "AACFRDKRHPGFVEYIPQSRDAAYRENRQQSGG*" //
                ;

        String cds = "ATGCAGACAGAGTGTAGCGCAGGCGCGTATGAGTTTCCAGCCTCCTGTGGAC" //
                + "GGCGTGTTGTGGCCCGTTTTGACGGGGGTCGCATGAGTTCGGATGGGGGCGTCAT" //
                + "TCTGGTGAAGCAGGCTGATGACATTCTGGGTCTCAGCCGCCGCTTTGCTGCCTGT" //
                + "TTTCGCGATAAGCGGCATCCCGGCTTTGTGGAATATATTCCACAAAGCCGGGATG" //
                + "CCGCTTATCGCGAAAACAGGCAGCAAAGCGGCGGCTGA" //
                ;

        // Create database & build interval forest
        String genomeName = "test_Acetobacter_pasteurianus";
        SnpEffectPredictor sep = build(genomeName);
        Gene g = sep.getGene("DB34_00005");
        Transcript tr = g.subIntervals().iterator().next();
        assertEquals(prot, tr.protein(), "Protein sequence differs");
        assertEquals(cds, tr.cds().toUpperCase(), "CDS sequence differs");
    }

    @Test
    public void testCase_04_CircularGenome() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR);

        String prot = "MTNNIVIAGRLVADAELFFTNNGSAICNFTLANNKRYKDIEKSTFIEASIFGNYAESMNK" //
                + "YLKKGVSIDVIGELVQESWSKDGKIYYKHKIKVKEIDFRTPKDNISEANFENEDTPSNHL" //
                + "LYLVEDNMRAVTIPIIISEQTPNIAKFSNVISKECKLSLAICSMVLLLSSIIFNHIQPSY" //
                + "SKSSIQIFVKTTPNKNAITYIARNPTNSSIINNSLLVLICKLCILWQFIIYYYIHSL*" //
                ;

        String cds = "ATGACAAATAATATAGTAATTGCAGGAAGATTGGTGGCAGACGCTGAACTATTTTTTACA" //
                + "AATAATGGCTCTGCTATTTGTAATTTTACTTTGGCGAATAATAAAAGATACAAAGACATA" //
                + "GAAAAAAGCACTTTTATAGAAGCTAGTATTTTTGGCAACTATGCAGAATCTATGAATAAG" //
                + "TATCTAAAAAAAGGCGTATCAATTGATGTAATAGGAGAGCTGGTTCAAGAAAGCTGGAGC" //
                + "AAAGATGGAAAAATATATTATAAACATAAAATCAAAGTCAAAGAGATTGATTTTAGAACA" //
                + "CCAAAAGATAATATTTCAGAAGCAAACTTTGAAAATGAAGATACACCCTCAAATCATCTG" //
                + "CTTTATCTGGTGGAAGACAATATGAGAGCTGTAACTATTCCTATTATCATAAGTGAGCAA" //
                + "ACGCCAAATATAGCAAAATTCTCAAATGTCATTTCTAAAGAATGCAAACTTTCTCTTGCT" //
                + "ATTTGCTCTATGGTTTTATTACTTTCTTCTATCATTTTCAATCATATTCAACCAAGTTAT" //
                + "TCAAAAAGCTCAATCCAAATTTTTGTAAAAACAACACCTAATAAGAATGCAATAACGTAC" //
                + "ATTGCAAGAAATCCTACTAACTCGTCCATAATCAATAATTCCTTATTAGTCTTAATTTGT" //
                + "AAGCTCTGTATTTTATGGCAATTTATTATTTATTATTATATCCATTCTCTATGA" //
                ;

        // Create database & build interval forest
        String genomeName = "test_Campylobacter_fetus_subsp_venerealis_nctc_10354";
        SnpEffectPredictor sep = build(genomeName);
        Gene g = sep.getGene("CFV354_1968");
        Transcript tr = g.subIntervals().iterator().next();
        assertEquals(prot, tr.protein(), "Protein sequence differs");
        assertEquals(cds, tr.cds().toUpperCase(), "CDS sequence differs");
    }

    @Test
    public void testCase_05_CircularGenome_ExonsOrder() {
        Log.debug("Test");
        Log.silenceWarning(ErrorWarningType.WARNING_CHROMOSOME_CIRCULAR);

        String expectedProtein = "MGSLEMVPMGAGPPSPGGDPDGYDGGNNSQYPSASGSSGNTPTP" //
                + "PNDEERESNEEPPPPYEDPYWGNGDRHSDYQPLGTQDQSLYLGLQHDGNDGLPPPPYS" //
                + "PRDDSSQHIYEEAGRGSMNPVCLPVIVAPYLFWLAAIAASCFTASVSTVVTATGLALS" //
                + "LLLLAAVASSYAAAQRKLLTPVTVLTAVVTFFAICLTWRIEDPPFNSLLFALLAAAGG" //
                + "LQGIYVLVMLVLLILAYRRRWRRLTVCGGIMFLACVLVLIVDAVLQLSPLLGAVTVVS" //
                + "MTLLLLAFVLWLSSPGGLGTLGAALLTLAAALALLASLILGTLNLTTMFLLMLLWTLV" //
                + "VLLICSSCSSCPLSKILLARLFLYALALLLLASALIAGGSILQTNFKSLSSTEFIPNL" //
                + "FCMLLLIVAGILFILAILTEWGSGNRTYGPVFMCLGGLLTMVAGAVWLTVMSNTLLSA" //
                + "WILTAGFLIFLIGFALFGVIRCCRYCCYYCLTLESEERPPTPYRNTV*";

        // Create database & build interval forest
        String genomeName = "testCase";
        String genBankFile = path("Human_herpesvirus_4_uid14413.gbk.gz");
        SnpEffectPredictor sep = buildGeneBank(genomeName, genBankFile, true);
        sep.buildForest();

        // Create variant
        Genome genome = sep.getGenome();
        Gene gene = genome.getGenes().getGeneByName("LMP2");

        // Get transcript
        Transcript tr = gene.get("YP_401631.1");
        assertNotNull(tr, "Transcript ID not found");

        if (verbose) Log.debug("Transcript: " + tr);
        String prot = tr.protein();
        assertEquals(expectedProtein, prot, "Protein sequence deas not match");
    }

    @Test
    public void testCase_06_CircularGenome_ExonsOrder() {
        Log.debug("Test");

        String expectedProtein = "MALQTDTQAWRVEIGTRGLMFSNCVPLHLPEGQYHKLRLPVSAY" //
                + "EALAVARYGLVGSLWEVPAVNSALQCLAAAAPCKDVKIYPSCIFQVHAPMFVTIKTSL" //
                + "RCLNPHDLCLCLICVGAAILDIPLLCAPRDGAGARAAEGQAAAAQGGKLRVWGRLSPS" //
                + "SPTSLSLAFPYAGPPPVAWYRHSINLTRSEGVGIGKDCAQDHACPVPPQGHASSAADQ" //
                + "AGVPERGRKRAHEGPGAGEAASAGRGDVALSQSRALLWRGLGWDTGRGRLAPGLAMSR" //
                + "DAASGSVHLDIQVDRAEEGWVCDVLLEPGPPTAREGCSLSMDPGLVTLKDAWTLFPLH" //
                + "PEHDAVVPPKEEIHVMAQGHLQGGTPSLWGFTFQEAACDQWVLRPRVWTAHSPIKMTV" //
                + "YNCGHKPLHIGPSTRLGLALFWPAERSDNLDAGRIFYQLTSGELYWGRTVARPPTLTL" //
                + "PVDELRPWPKLTPEEPMQH*" //
                ;

        // Create database & build interval forest
        String genomeName = "testCase";
        String genBankFile = path("Human_herpesvirus_4_uid14413.gbk.gz");
        SnpEffectPredictor sep = buildGeneBank(genomeName, genBankFile);
        sep.buildForest();

        // Create variant
        Genome genome = sep.getGenome();
        Gene gene = genome.getGenes().getGeneByName("LF1");
        Transcript tr = gene.iterator().next();
        String prot = tr.protein();

        if (verbose) Log.debug("Transcript: " + tr);
        assertEquals(expectedProtein, prot, "Protein sequence does not match");
    }
}
