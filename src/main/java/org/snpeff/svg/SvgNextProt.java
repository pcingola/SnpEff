package org.snpeff.svg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;
import org.snpeff.interval.NextProt;
import org.snpeff.interval.Transcript;
import org.snpeff.util.Log;

/**
 * Create an SVG representation of a NextProt annotation tracks
 */
public class SvgNextProt extends Svg {

	Transcript tr;
	List<NextProt> nextprotMarkers;
	Map<String, List<NextProt>> nextprotMarkersByCategory;

	public SvgNextProt(Transcript tr, Svg svg, Markers nextprotMarkers) {
		super(tr, svg);
		this.tr = tr;
		rectColorStroke = "#ffffff";

		add(nextprotMarkers);

		if (svg != null) baseY = svg.nextBaseY;
		int numCat = nextprotMarkersByCategory.keySet().size();
		nextBaseY = baseY + 2 * (numCat + 1) * RECT_HEIGHT;
	}

	/**
	 * Add NextProt markers
	 */
	void add(Markers nextprotMarkers) {
		this.nextprotMarkers = new ArrayList<>();
		nextprotMarkersByCategory = new HashMap<>();

		for (Marker m : nextprotMarkers) {
			NextProt np = (NextProt) m;
			this.nextprotMarkers.add(np);

			// Get category name
			String category = np.getId();
			if (category.indexOf(':') > 0) category = category.split(":")[0];

			// Add to 'by category' list
			List<NextProt> byCat = nextprotMarkersByCategory.get(category);
			if (byCat == null) {
				byCat = new ArrayList<>();
				nextprotMarkersByCategory.put(category, byCat);
			}
			byCat.add(np);
		}
		if (debug) Log.debug("Added " + nextprotMarkers.size() + " markers");
	}

	/**
	 * Show a nextprot category
	 */
	String plotCategory(String category, int order) {
		int baseYori = baseY;
		int nextBaseYori = nextBaseY;

		baseY = baseY + 2 * order * RECT_HEIGHT;
		nextBaseY = baseY;

		if (debug) Log.debug("Plotting NextProt category:" + category);
		StringBuilder sb = new StringBuilder();
		sb.append(hline(rectHeight / 2));

		for (NextProt np : nextprotMarkersByCategory.get(category)) {
			Svg svg = factory(np, this);
			sb.append("\t" + svg);
		}

		// Show chromosome label
		sb.append(text(0, baseY + TEXT_SIZE, category));

		// Restore originla parameters
		baseY = baseYori;
		nextBaseY = nextBaseYori;

		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		// Sort categories alphabetically
		List<String> categories = new ArrayList<>();
		categories.addAll(nextprotMarkersByCategory.keySet());
		Collections.sort(categories);

		// Show each category
		int i = 0;
		for (String category : categories)
			sb.append(plotCategory(category, i++));

		return sb.toString();
	}

}
