package ca.mcgill.mcb.pcingola.snpEffect.testCases;

import java.io.File;

import ca.mcgill.mcb.pcingola.snpEffect.testCases.integration.TestCasesIntegrationBase;
import ca.mcgill.mcb.pcingola.util.Gpr;

/**
 * Test case
 *
 */
public class TestCasesZzz extends TestCasesIntegrationBase {

	public TestCasesZzz() {
	}

	int countSequenceBinFiles(String dir) {
		int count = 0;
		for (String fn : (new File(dir)).list()) {
			if (fn.startsWith("sequence") && fn.endsWith(".bin")) {
				count++;
				if (verbose) Gpr.debug("Found file (" + count + "): " + fn);
			}
		}

		return count;
	}

	void deleteAllBinFiles(String dir) {
		for (File f : (new File(dir)).listFiles()) {
			String fn = f.getName();
			if (fn.startsWith("sequence") && fn.endsWith(".bin")) {
				if (verbose) Gpr.debug("Deleting file: " + f.getAbsolutePath());
				f.delete();
			}
		}
	}

}
