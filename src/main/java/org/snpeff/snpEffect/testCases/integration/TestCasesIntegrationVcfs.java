package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.SnpEff;
import org.snpeff.fileIterator.VcfFileIterator;
import org.snpeff.interval.Variant;
import org.snpeff.snpEffect.commandLine.SnpEffCmdEff;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.EffFormatVersion;
import org.snpeff.vcf.VcfEntry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VCF annotations test cases
 *
 * @author pcingola
 */
public class TestCasesIntegrationVcfs extends TestCasesIntegrationBase {

    boolean createOutputFile = false;

    public TestCasesIntegrationVcfs() {
        super();
    }

    /**
     * Creates a test file
     */
    public static void create1kgFile() {
        String vcfFile = Gpr.HOME + "/snpEff/1kg.indels.vcf";
        String vcfOutFile = Gpr.HOME + "/workspace/SnpEff/tests/1kg.indels.vcf";

        StringBuilder outvcf = new StringBuilder();

        VcfFileIterator vcf = new VcfFileIterator(vcfFile);
        for (VcfEntry ve : vcf) {
            StringBuilder sb = new StringBuilder();

            for (Variant sc : ve.variants()) {
                if (sb.length() > 0) sb.append(",");
                sb.append(sc.getReference() + "/" + sc.getAlt());
            }

            ve.addInfo("variant", sb.toString());
            outvcf.append(ve + "\n");
        }

        Gpr.toFile(vcfOutFile, outvcf);
    }

    @Test
    public void test_01_vcf_bed_filter() {
        Log.debug("Test");
        String vcfFile = path("test_vcf_filter.vcf");
        String bedFile = path("test_vcf_filter.bed");

        String[] args = {"-classic", "-filterinterval", bedFile, "testHg3771Chr1", vcfFile};
        SnpEff snpeff = new SnpEff(args);

        // Create command and run
        SnpEffCmdEff effcmd = (SnpEffCmdEff) snpeff.cmd();
        effcmd.setVerbose(verbose);
        effcmd.setSupressOutput(!verbose);
        List<VcfEntry> vcfEntries = effcmd.run(true);

        // All VCF entries should be filtered out
        Log.debug("Vcf entries: " + vcfEntries.size());
        assertEquals(0, vcfEntries.size());

        // Nothing should be printed
        for (VcfEntry ve : vcfEntries)
            System.out.println(ve);
    }

    /**
     * Annotating LOF / NMD using a geneName that contains spaces triggers
     * an Exception (it shouldn't happen)
     */
    @Test
    public void test_02_Annotating_LOF_Spaces() {
        String vcfFileName = path("vcf_genes_spaces.vcf");
        String genomeName = "test_ENSG00000158062_spaces";

        // Prepare a command line
        String[] args = {"-noLog", genomeName, vcfFileName};
        SnpEff snpEff = new SnpEff(args);
        snpEff.setSupressOutput(!verbose);
        snpEff.setVerbose(verbose);
        snpEff.setDebug(debug);

        // This should run OK
        boolean ok = snpEff.run();
        assertTrue(ok, "SnpEff run failed!");
    }

    /**
     * Non-variant VCF entries should be skipped (i.e. no annotation should be added)
     */
    @Test
    public void test_03_do_not_annotate_non_variants() {
        String vcfFileName = path("test_non_variants.vcf");
        String genomeName = "testHg3775Chr1";

        // Prepare a command line
        String[] args = {"-noLog", genomeName, vcfFileName};
        SnpEff snpEff = new SnpEff(args);
        snpEff.setSupressOutput(!verbose);
        snpEff.setVerbose(verbose);
        snpEff.setDebug(debug);

        // Run command
        SnpEffCmdEff seff = (SnpEffCmdEff) snpEff.cmd();
        List<VcfEntry> vcfEntries = seff.run(true);
        assertFalse(vcfEntries.isEmpty(), "SnpEff run failed, returned an empty list");

        // Check output
        for (VcfEntry ve : vcfEntries) {
            if (verbose) Log.info(ve);

            if (ve.hasInfo(EffFormatVersion.VCF_INFO_ANN_NAME) || ve.hasInfo(EffFormatVersion.VCF_INFO_EFF_NAME)) //
                throw new RuntimeException("Effect field should not be annotated on non-variant entries!\n" + ve);

        }
    }

}
