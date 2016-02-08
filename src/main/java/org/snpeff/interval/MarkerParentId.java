package org.snpeff.interval;

/**
 * This is a marker used as a 'fake' parent during data serialization
 * 
 * @author pcingola
 */
public class MarkerParentId extends Marker {

	int parentId;

	public MarkerParentId(int parentId) {
		this.parentId = parentId;
	}

	public int getParentId() {
		return parentId;
	}
}
