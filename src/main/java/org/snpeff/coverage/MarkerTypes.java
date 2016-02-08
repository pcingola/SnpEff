package org.snpeff.coverage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.snpeff.interval.Exon;
import org.snpeff.interval.Intron;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Transcript;

/**
 * Create a list of marker types (names or labels for markers)
 * 
 * @author pcingola
 */
public class MarkerTypes {

	HashMap<Marker, String> marker2type;
	HashSet<String> markerTypesClass;

	public MarkerTypes() {
		marker2type = new HashMap<Marker, String>();
		markerTypesClass = new HashSet<String>();
	}

	public void addType(Marker marker, String type) {
		marker2type.put(marker, type);
		markerTypesClass.add(type);
	}

	/**
	 * Some markers have sub-types
	 * @param marker
	 * @return
	 */
	public String getSubType(Marker marker) {
		String subtype = null;

		if (marker instanceof Exon) subtype = "Exon:" + ((Exon) marker).getSpliceType();
		else if (marker instanceof Intron) subtype = "Intron:" + ((Intron) marker).getSpliceType();

		if (subtype != null) markerTypesClass.add(subtype);

		return subtype;
	}

	/**
	 * Get marker type
	 * @param marker
	 * @return
	 */
	public String getType(Marker marker) {
		String type = marker2type.get(marker);
		if (type != null) return type;

		// Marker type based on class name
		type = marker.getClass().getSimpleName();
		markerTypesClass.add(type);

		return type;
	}

	/**
	 * Get marker + rank (in case of exon or intron)
	 * @param marker
	 * @return
	 */
	public String getTypeRank(Marker marker) {
		String typeRank = marker2type.get(marker);
		if (typeRank != null) return typeRank;

		if (marker instanceof Exon) {
			Transcript tr = (Transcript) marker.findParent(Transcript.class);
			typeRank = "Exon:" + ((Exon) marker).getRank() + ":" + tr.numChilds();
		} else if (marker instanceof Intron) {
			Transcript tr = (Transcript) marker.findParent(Transcript.class);
			typeRank = "Intron:" + ((Intron) marker).getRank() + ":" + tr.numChilds();
		} else typeRank = marker.getClass().getSimpleName();

		if (typeRank != null) markerTypesClass.add(typeRank);

		return typeRank;
	}

	public boolean isType(Marker m, String mtype) {
		// Matching type?
		if (getType(m).equals(mtype)) return true;

		// Matching sub-type?
		String msubtype = getSubType(m);
		if ((msubtype != null) && msubtype.equals(mtype)) return true;

		return false;
	}

	/**
	 * Is this marker type based on class name
	 * @param marker
	 * @return
	 */
	boolean isTypeClass(Marker marker) {
		return marker2type.get(marker) == null;
	}

	/**
	 * List of markers whose types are based on class names
	 * @return
	 */
	public List<String> markerTypesClass() {
		// Ad all marker whose types are based on class name
		ArrayList<String> typesList = new ArrayList<String>();
		typesList.addAll(markerTypesClass);
		Collections.sort(typesList); // Sort 
		return typesList;
	}
}
