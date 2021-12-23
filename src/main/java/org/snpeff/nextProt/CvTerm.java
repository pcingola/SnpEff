package org.snpeff.nextProt;

import org.xml.sax.Attributes;

/**
 * A controlled vocabulary term
 */
public class CvTerm {
    public String accession, terminology, description;

    public CvTerm(Attributes attributes) {
        accession = attributes.getValue("accession");
        terminology = attributes.getValue("terminology");
    }

    @Override
    public String toString() {
        return "CvTerm(" + accession + ", " + terminology + ", " + description + ")";
    }
}
