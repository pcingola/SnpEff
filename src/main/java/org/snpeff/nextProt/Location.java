package org.snpeff.nextProt;

/**
 * A location (i.e. an interval)
 */
public class Location {
    public String type;
    public int begin, end;

    public Location() {
        this(null, -1, -1);
    }

    public Location(String type) {
        this(type, -1, -1);
    }

    public Location(String type, int begin, int end) {
        this.type = type;
        this.begin = begin;
        this.end = end;
    }

    public boolean isValid() {
        return begin >= 0 && end >= 0;
    }

    public boolean isInteraction() {
        return false;
    }

    public boolean isIsoform() {
        return false;
    }

    @Override
    public String toString() {
        return "Location(" + (type != null ? type + ", " : "") + begin + ", " + end + ")";
    }
}
