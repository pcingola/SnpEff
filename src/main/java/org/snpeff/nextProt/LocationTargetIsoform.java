package org.snpeff.nextProt;

/**
 * A location respect to an isoform
 */
public class LocationTargetIsoform extends Location {
    public String accession;

    public LocationTargetIsoform(String accession) {
        super();
        this.accession = accession;
    }

    public LocationTargetIsoform(String accession, int begin, int end) {
        super("", begin, end);
        this.accession = accession;
    }

    public boolean isIsoform() {
        return true;
    }

    @Override
    public String toString() {
        return "LocationTargetIsoform(" + accession + ", " + begin + ", " + end + ")";
    }
}

