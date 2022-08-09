package org.snpeff.interval;

import org.snpeff.interval.tree.IntervalForest;

/**
 * A 'BND' variant (i.e. translocation) consisting of two endpoints
 *
 * @author pcingola
 */
public class VariantBnd extends Variant {

    private static final long serialVersionUID = 1L;
    Marker endPoint;
    boolean left; // Is endPoint oriented to the left?
    boolean before; // Is endPoint before ALT?

    public VariantBnd() {
        super();
    }

    public VariantBnd(Marker parent, int start, String ref, String alt, Chromosome chrTr, int startTr, boolean left, boolean before) {
        super(parent, start, ref, alt);
        endPoint = new Marker(chrTr, startTr, startTr);
        endPoint.setStrandMinus(left);
        this.left = left;
        this.before = before;
        variantType = VariantType.BND;
    }

    public Marker getEndPoint() {
        return endPoint;
    }

    public boolean isBefore() {
        return before;
    }

    @Override
    public boolean isBnd() {
        return true;
    }

    public boolean isLeft() {
        return left;
    }

    @Override
    public boolean isStructural() {
        return true;
    }

    /**
     * Return a collection of intervals that intersect both ends of this variant
     */
    @Override
    public Markers query(IntervalForest intervalForest) {
        Markers res1 = intervalForest.query(this);
        Markers res2 = intervalForest.query(endPoint);
        res1.add(res2);
        return res1;
    }

    @Override
    public Variant realignLeft() {
        // Do not realign translocations
        return this;
    }

    @Override
    public String toString() {
        String sep = left ? "]" : "[";
        String trPos = sep + endPoint.getChromosomeName() + ":" + endPoint.getStart() + sep;

        return "chr" + getChromosomeName() //
                + ":" + getStart() //
                + "_" + getReference() //
                + "/" //
                + (before ? trPos + getAlt() : getAlt() + trPos) //
                ;
    }

}
