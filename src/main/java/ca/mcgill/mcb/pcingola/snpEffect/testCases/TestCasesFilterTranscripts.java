package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEff;
import ca.mcgill.mcb.pcingola.snpEffect.commandLine.SnpEffCmdEff;
import ca.mcgill.mcb.pcingola.vcf.VcfEffect;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 *
 * Filter transcripts 
 * 
 * @author pcingola
 */
public class TestCasesFilterTranscripts extends TestCase {

	boolean verbose = false;

	public TestCasesFilterTranscripts() {
		super();
	}

	/**
	 * Filter transcripts from a file
	 */
	public void test_01() {
		String args[] = { "-v" //
				, "-noStats" // 
				, "-i", "vcf", "-o", "vcf" //
				, "-classic" //
				, "-onlyTr", "tests/filterTranscripts_01.txt"//
				, "testHg3765Chr22" //
				, "tests/test_filter_transcripts_001.vcf" //
		};

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		List<VcfEntry> vcfEntries = cmdEff.run(true);
		for (VcfEntry ve : vcfEntries) {
			System.out.println(ve);

			// Get effect string
			String effs = ve.getInfo(VcfEffect.VCF_INFO_EFF_NAME);
			for (String effStr : effs.split(",")) {
				VcfEffect veff = new VcfEffect(effStr);
				System.out.println("\ttrId:" + veff.getTranscriptId() + "\t" + veff);

				Assert.assertEquals("ENST00000400573", veff.getTranscriptId());
			}
		}
	}

	/**
	 * Filter transcripts from a file
	 */
	public void test_02() {
		String args[] = { "-v"//
				, "-noStats" // 
				, "-i", "vcf", "-o", "vcf" //
				, "-classic" //
				, "-onlyTr", "tests/filterTranscripts_02.txt"//
				, "testHg3765Chr22" //
				, "tests/test_filter_transcripts_001.vcf" //
		};

		SnpEff cmd = new SnpEff(args);
		SnpEffCmdEff cmdEff = (SnpEffCmdEff) cmd.snpEffCmd();
		List<VcfEntry> vcfEntries = cmdEff.run(true);
		for (VcfEntry ve : vcfEntries) {
			System.out.println(ve);

			// Get effect string
			String effs = ve.getInfo(VcfEffect.VCF_INFO_EFF_NAME);
			for (String effStr : effs.split(",")) {
				VcfEffect veff = new VcfEffect(effStr);
				System.out.println("\ttrId:" + veff.getTranscriptId() + "\t" + veff);

				if (veff.getTranscriptId().equals("ENST00000400573") || veff.getTranscriptId().equals("ENST00000262608")) {
					// OK
				} else throw new RuntimeException("This transcript should not be here! " + veff);
			}
		}
	}

}
