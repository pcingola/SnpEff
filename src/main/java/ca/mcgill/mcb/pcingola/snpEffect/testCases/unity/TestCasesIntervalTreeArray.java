package ca.mcgill.mcb.pcingola.snpEffect.testCases.unity;

import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.interval.tree.IntervalTreeArray;
import ca.mcgill.mcb.pcingola.interval.tree.Itree;

/**
 * Test case for interval tree structure
 */
public class TestCasesIntervalTreeArray extends TestCasesIntervalTree {

	@Override
	protected Itree newItree(Markers markers) {
		IntervalTreeArray ita = new IntervalTreeArray(markers);
		ita.setVerbose(verbose);
		ita.setDebug(debug);
		return ita;
	}

}
