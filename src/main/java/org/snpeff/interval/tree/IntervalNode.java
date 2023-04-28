package org.snpeff.interval.tree;

import org.snpeff.interval.Interval;
import org.snpeff.interval.Marker;
import org.snpeff.interval.Markers;

import java.io.Serializable;

/**
 * Node for interval tree structure
 *
 * @author pcingola
 */
public class IntervalNode implements Serializable {

    private static final long serialVersionUID = -444656480302906048L;

    protected int center; // Center point
    protected IntervalNode leftNode; // All intervals absolutely to the left respect to 'center'
    protected IntervalNode rightNode; // All intervals absolutely to the right respect to 'center'
    protected Marker[] intervalsCenter; // Intervals intersecting 'center'

    public IntervalNode() {
    }

    public IntervalNode(Markers markers) {
        build(markers);
    }

    /**
     * Build interval tree
     */
    protected void build(Markers markers) {
        // Empty markers?
        if (markers.size() == 0) {
            center = 0;
            return;
        }

        // Calculate median point
        center = markers.getMedian();

        // Split markers to the left, to the right and intersecting 'center'
        Markers left = new Markers();
        Markers right = new Markers();
        Markers intersecting = new Markers();

        for (Marker interval : markers) {
            if (interval.getEndClosed() < center) left.add(interval);
            else if (interval.getStart() > center) right.add(interval);
            else intersecting.add(interval);
        }

        // Convert markers to array
        if (intersecting.isEmpty()) intervalsCenter = null;
        else intervalsCenter = intersecting.toArray();

        // Recurse
        if (left.size() > 0) leftNode = newNode(left);
        if (right.size() > 0) rightNode = newNode(right);
    }

    public Integer getCenter() {
        return center;
    }

    public IntervalNode getLeft() {
        return leftNode;
    }

    public IntervalNode getRight() {
        return rightNode;
    }

    /**
     * Create a new node
     */
    protected IntervalNode newNode(Markers markers) {
        return new IntervalNode(markers);
    }

    /**
     * Perform an interval intersection query on the node
     *
     * @param queryMarker: The interval to intersect
     * @return All intervals containing 'target'
     */
    public Markers query(Interval queryInterval) {
        Markers results = new Markers();

        if (intervalsCenter != null) {
            for (Marker marker : intervalsCenter)
                if (marker.intersects(queryInterval)) results.add(marker);
        }

        if (queryInterval.getStart() < center && leftNode != null) results.add(leftNode.query(queryInterval));
        if (queryInterval.getEndClosed() > center && rightNode != null) results.add(rightNode.query(queryInterval));

        return results;
    }

    /**
     * Perform a stabbing query on the node
     *
     * @param point the time to query at
     * @return All intervals containing time
     */
    public Markers stab(Integer point) {
        Markers result = new Markers();

        if (intervalsCenter != null) {
            for (Marker marker : intervalsCenter)
                if (marker.intersects(point)) result.add(marker);
        }

        if (point < center && leftNode != null) result.add(leftNode.stab(point));
        else if (point > center && rightNode != null) result.add(rightNode.stab(point));
        return result;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(center + ":\n");

        if (intervalsCenter != null) {
            for (Marker marker : intervalsCenter)
                sb.append("\t" + marker + "\n");
        }

        return sb.toString();
    }
}
