package org.snpeff.nextProt;

import java.util.ArrayList;
import java.util.List;

import org.snpeff.interval.Markers;
import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.xml.sax.Attributes;

/**
 * A location
 */
class Location {
	public int begin, end;

	Location() {
		this(-1, -1);
	}

	Location(int begin, int end) {
		this.begin = begin;
		this.end = end;
	}

	public boolean isValid() {
		return begin >= 0 && end >= 0;
	}

	@Override
	public String toString() {
		return "begin: " + begin + ", end: " + end;
	}
}

/**
 * A location respect to an isoform
 */
class LocationTargetIsoform extends Location {
	public String accession;

	LocationTargetIsoform(String accession) {
		super();
		this.accession = accession;
	}

	@Override
	public String toString() {
		return "IsoformId: '" + accession + "', begin: " + begin + ", end: " + end;
	}

}

/**
 * Mimics the 'annotation' tag in a NextProt XML file
 *
 * @author Pablo Cingolani
 *
 */
public class NextProtXmlAnnotation extends NextProtXmlNode {

	NextProtXmlEntry entry;
	String category; // Annotation category
	List<Location> locations; // Locations associated with current annotation
	Location location; // Current location

	public NextProtXmlAnnotation(NextProtXmlEntry entry, String category) {
		super(null);
		this.entry = entry;
		this.category = category;
		init();
	}

	public String getCategory() {
		return category;
	}

	public List<Location> getLocations() {
		return locations;
	}

	void init() {
		// Only store locations for these types of annotations
		switch (category) {
		case "active-site":
		case "binding-site":
		case "cleavage-site":
		case "cross-link":
		case "cysteines":
		case "glycosylation-site":
		case "lipidation-site":
		case "metal-binding-site":
		case "non-terminal-residue":
		case "nucleotide-phosphate-binding-region":
		case "selenocysteine":
			locations = new ArrayList<>();
			break;
		}
	}

	public boolean isEmpty() {
		return locations == null || locations.isEmpty();
	}

	public void locationBeginPos(Attributes attributes) {
		if (location != null) location.begin = Gpr.parseIntSafe(attributes.getValue("position")) - 1; // Transform to zero-based
	}

	/**
	 * End of location tag
	 */
	public void locationEnd() {
		if (locations != null) locations.add(location);
		location = null;
	}

	public void locationEndPos(Attributes attributes) {
		if (location != null) location.end = Gpr.parseIntSafe(attributes.getValue("position")) - 1; // Transform to zero-based
	}

	public void locationIsoformStart(String accession) {
		location = new LocationTargetIsoform(accession);
	}

	public void locationStart() {
		if (location == null) location = new Location();
	}

	public Markers markers(NextProtMarkerFactory markersFactory) {
		for (var l : locations) {
			// TODO: This cast may not be always possible in future NextProt version, or when adding new categories
			var loc = (LocationTargetIsoform) l;

			// Get Isoform
			var iso = entry.getIsoform(loc.accession);
			if (iso == null) {
				Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Isoform '" + loc.accession + "' not found for entry '" + entry.getAccession() + "'");
				continue;
			}

			// Create markers
			for (var trId : iso.getTranscriptIds())
				markersFactory.markers(entry, iso, this, loc, trId);
		}
		return null;
	}

	@Override
	public String toString() {
		return toString("");

	}

	public String toString(String prefix) {
		var sb = new StringBuilder();
		sb.append("Annotation '" + category + "'\n");
		if (locations != null) {
			for (Location l : locations)
				sb.append(prefix + "\t" + l + "\n");
		}
		return sb.toString();
	}
}
