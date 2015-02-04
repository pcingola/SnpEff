package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.EffFormatVersion;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Test case
 */
public class TestCasesZzz {

	boolean debug = false;
	boolean verbose = true || debug;

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
		cmdEff.setFormatVersion(EffFormatVersion.FORMAT_EFF_4);

		// Run command
		List<VcfEntry> list = cmdEff.run(true);
		return list;
	}

	/**
	 * GATK does not annotate this 
	 */
	@Test
	public void test_05() {
		Gpr.debug("Test");

		String genomeName = "Mycobacterium_tuberculosis_CCDC5079_uid203790";
		String vcf = "tests/test_gatk_no_annotations.vcf";
		String args[] = { "-noLog", "-o", "gatk" };
		List<VcfEntry> vcfEntries = snpEffect(genomeName, vcf, args);

		for (VcfEntry ve : vcfEntries) {
			if (verbose) System.out.println(ve);

			for (VcfEffect veff : ve.parseEffects()) {
				if (verbose) System.out.println("\t'" + veff.getEffectsStr() + "'\t" + veff);
			}
		}
	}

}
