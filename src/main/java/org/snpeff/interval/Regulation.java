package org.snpeff.interval;

import org.snpeff.serializer.MarkerSerializer;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffects;

/**
 * Regulatory elements
 *
 * @author pablocingolani
 */
public class Regulation extends Marker {

    private static final long serialVersionUID = -5607588295343642199L;

    String regulationType = "";
    String name = "";

    public Regulation() {
        super();
        type = EffectType.REGULATION;
    }

    public Regulation(Marker parent, int start, int end, boolean strandMinus, String id, String name, String regulationType) {
        super(parent, start, end, strandMinus, id);
        type = EffectType.REGULATION;
        this.name = name;
        this.regulationType = regulationType;
    }

    @Override
    public Regulation cloneShallow() {
        Regulation clone = (Regulation) super.cloneShallow();
        clone.regulationType = regulationType;
        clone.name = name;
        return clone;
    }

    public String getName() {
        return name;
    }

    public String getRegulationType() {
        return regulationType;
    }

    /**
     * Parse a line from a serialized file
     */
    @Override
    public void serializeParse(MarkerSerializer markerSerializer) {
        super.serializeParse(markerSerializer);
        regulationType = markerSerializer.getNextField();
        name = markerSerializer.getNextField();
    }

    /**
     * Create a string to serialize to a file
     *
     * @return
     */
    @Override
    public String serializeSave(MarkerSerializer markerSerializer) {
        return super.serializeSave(markerSerializer) //
                + "\t" + regulationType //
                + "\t" + name //
                ;
    }

    @Override
    public String toString() {
        return getChromosomeName() + "\t" + getStart() + "-" + getEndClosed() //
                + " " //
                + type + ((name != null) && (!name.isEmpty()) ? " '" + name + "'" : "");
    }

    /**
     * Calculate the effect of this variant
     */
    @Override
    public boolean variantEffect(Variant variant, VariantEffects variantEffects) {
        if (!intersects(variant)) return false; // Sanity check
        EffectType effType = EffectType.REGULATION;
        variantEffects.add(variant, this, effType, "");
        return true;
    }

}
