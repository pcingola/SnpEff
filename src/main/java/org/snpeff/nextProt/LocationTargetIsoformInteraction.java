package org.snpeff.nextProt;

/**
 * A location respect to two locations within an isoform
 * In this case "start" and "end" are not really an interval, but an interaction between
 * two locations (e.g. disulphide bond)
 */
public class LocationTargetIsoformInteraction extends LocationTargetIsoform {
    public LocationTargetIsoformInteraction(LocationTargetIsoform loc) {
        super(loc.accession);
        this.begin = loc.begin;
        this.end = loc.end;
    }

    public LocationTargetIsoformInteraction(String accession, int begin, int end) {
        super(accession, begin, end);
    }

    public boolean isInteraction() {
        return true;
    }

    @Override
    public String toString() {
        return "LocationTargetIsoformInteraction(" + accession + ", " + begin + ", " + end + ")";
    }
}
