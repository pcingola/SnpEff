package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Test case
 */
public class TestCasesZzz extends TestCase {

	boolean debug = true;
	boolean verbose = false || debug;

	public TestCasesZzz() {
		super();
	}

	/**
	 * Calculate snp effect for an input VCF file
	 */
	public List<VcfEntry> snpEffect(String genome, String vcfFile, String otherArgs[]) {
		// Arguments
		ArrayList<String> args = new ArrayList<String>();
		if (otherArgs != null) {
			for (String a : otherArgs)
				args.add(a);
		}
		args.add(genome);
		args.add(vcfFile);

		SnpEff cmd = new SnpEff(args.toArray(new String[0]));
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		cmdEff.setVerbose(verbose);
		cmdEff.setSupressOutput(!verbose);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		return list;
	}

	public void test_03() {
		Gpr.debug("Test");
		String genomeName = "testHg3775Chr1";
		String vcf = "tests/gatk_NO_splice_regions.vcf";
		String args[] = { "eff", "-noLog", "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect(genomeName, vcf, args);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println("\t'" + veff.getEffectsStr() + "'\t" + veff);
				if (veff.getEffectsStr().indexOf("SPLICE_SITE_REGION") >= 0) throw new RuntimeException("Splice region effects should not present in GATK compatible mode");
			}
		}

	}

}
