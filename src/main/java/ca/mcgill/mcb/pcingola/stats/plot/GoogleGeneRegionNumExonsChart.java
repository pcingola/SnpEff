package ca.mcgill.mcb.pcingola.stats.plot;

import java.util.ArrayList;

import ca.mcgill.mcb.pcingola.interval.Exon;
import ca.mcgill.mcb.pcingola.interval.Intergenic;
import ca.mcgill.mcb.pcingola.interval.Intron;
import ca.mcgill.mcb.pcingola.stats.CoverageByType;

public class GoogleGeneRegionNumExonsChart extends GoogleGeneRegionChart {

	int exons;

	public GoogleGeneRegionNumExonsChart(CoverageByType coverageByType, String name, int exons) {
		super(coverageByType, name);
		this.exons = exons;
	}

	void initTypes() {
		ArrayList<String> typeList = new ArrayList<String>();

		boolean exonsAdded = false;
		for (String type : DEFAULT_TYPES) {
			// Add exons or introns?
			if (type.equals(Exon.class.getSimpleName()) || type.equals(Intron.class.getSimpleName())) {
				if (!exonsAdded) {
					// Add all exons and introns
					for (int ex = 1; ex <= exons; ex++) {
						typeList.add(Exon.class.getSimpleName() + ":" + ex + ":" + exons);
						if (ex < exons) typeList.add(Intron.class.getSimpleName() + ":" + ex + ":" + exons);
					}

					exonsAdded = true;
				}
			} else if (type.equals(Intergenic.class.getSimpleName())) {
				// Do not add intergenic
			} else typeList.add(type);
		}

		// convert to array
		types = typeList.toArray(new String[0]);
	}

}
