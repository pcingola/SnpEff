package org.snpeff.snpEffect.testCases.integration;

import org.junit.jupiter.api.Test;
import org.snpeff.util.Log;

/**
 * Test case where VCF entries has no sequence change (either REF=ALT or ALT=".")
 *
 * @author pcingola
 */
public class TestCasesIntegrationNoChange extends TestCasesIntegrationBase {

    public TestCasesIntegrationNoChange() {
        super();
    }

    @Test
    public void test_01() {
        Log.debug("Test");
        String[] args = {"-classic", "testHg3766Chr1", path("test.no_change.vcf")};
        checkNoChange(args);
    }

    @Test
    public void test_02() {
        Log.debug("Test");
        String[] args = {"-classic", "testHg3766Chr1", path("test.no_change_02.vcf")};
        checkNoChange(args);
    }

}
