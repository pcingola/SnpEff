package ca.mcgill.mcb.pcingola.stats;

import ca.mcgill.mcb.pcingola.collections.AutoHashMap;

public class CoverageByType extends AutoHashMap<String, PosStats> {

	private static final long serialVersionUID = 6365021779756255154L;

	public CoverageByType() {
		super(new PosStats());
	}

}
