package org.snpeff.nextProt;

import org.snpeff.snpEffect.ErrorWarningType;
import org.snpeff.util.Gpr;
import org.snpeff.util.Log;
import org.snpeff.vcf.VcfEntry;
import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Mimics the 'annotation' tag in a NextProt XML file
 *
 * @author Pablo Cingolani
 */
public class NextProtXmlAnnotation extends NextProtXmlNode {

    CvTerm cvTerm;
    String description;
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

    /**
     * Create all markers for this annotation
     */
    public void addMarkers(NextProtMarkerFactory markersFactory) {
        // Add markers for each location
        for (var l : locations) {
            // Note: This cast may not be always possible in future NextProt version, or when adding new categories
            var loc = (LocationTargetIsoform) l;

            // Get Isoform
            var iso = entry.getIsoform(loc.accession);
            if (iso == null) {
                Log.warning(ErrorWarningType.WARNING_TRANSCRIPT_NOT_FOUND, "Isoform '" + loc.accession + "' not found for entry '" + entry.getAccession() + "'");
                continue;
            }

            // Create markers
            for (var trId : iso.getTranscriptIds()) {
                markersFactory.addMarkers(entry, iso, this, loc, trId);
            }
        }
    }

    public String description() {
        // Some annotations have control-vocabulary terms (e.g. "modified-residue")
        if (cvTerm != null) return cvTerm.description;

        // Some annotations do not have controlled vocabularies, but have a "desription" (e.g. "active-site")
        if (description != null) {
            // Sometimes a description can be split at ';'
            if (description.indexOf(';') > 0) return description.split(";")[0];
            return description;
        }

        return null;
    }

    public String getCategory() {
        return category;
    }

    public CvTerm getCvTerm() {
        return cvTerm;
    }

    public void setCvTerm(CvTerm cvTerm) {
        this.cvTerm = cvTerm;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Location> getLocations() {
        return locations;
    }

    public boolean hasCvTerm() {
        return this.cvTerm != null;
    }

    void init() {
        if (isIgnore(category)) return;
        locations = new ArrayList<>();
    }

    /**
     * Should we annotate this category?
     */
    boolean isAnnotate(String category) {
        switch (category) {
            // Only store locations for these types of annotations
            case "active-site":
            case "binding-site":
            case "cleavage-site":
            case "cysteines":
            case "disulfide-bond":
            case "glycosylation-site":
            case "lipidation-site":
            case "modified-residue":
            case "metal-binding-site":
            case "selenocysteine":
                return true;

            default:
                return false;
        }
    }

    public boolean isEmpty() {
        return locations == null || locations.isEmpty();
    }

    /**
     * Should we ignore this category?
     */
    boolean isIgnore(String category) {
        switch (category) {
            // Ignore these types of annotations
            case "antibody-mapping":
            case "beta-strand":
            case "calcium-binding-region":
            case "coiled-coil-region":
            case "compositionally-biased-region":
            case "cross-link":
            case "dna-binding-region":
            case "domain":
            case "expression-info":
            case "expression-profile":
            case "function-info":
            case "go-biological-process":
            case "go-molecular-function":
            case "go-cellular-component":
            case "helix":
            case "interacting-region":
            case "initiator-methionine":
            case "interaction-info":
            case "interaction-mapping":
            case "intramembrane-region":
            case "mature-protein":
            case "miscellaneous-region":
            case "miscellaneous-site":
            case "mitochondrial-transit-peptide":
            case "mutagenesis":
            case "non-terminal-residue":
            case "nucleotide-phosphate-binding-region":
            case "pathway":
            case "pdb-mapping":
            case "peptide-mapping":
            case "peroxisome-transit-peptide":
            case "propeptide":
            case "repeat":
            case "sequence-conflict":
            case "short-sequence-motif":
            case "signal-peptide":
            case "srm-peptide-mapping":
            case "subcellular-location":
            case "topological-domain":
            case "transmembrane-region":
            case "turn":
            case "uniprot-keyword":
            case "variant":
            case "zinc-finger-region":
                return true;

            default:
                return false;
        }
    }

    boolean isIntreaction(String category) {
        return category.equals("disulfide-bond");
    }

    public void locationBeginPos(Attributes attributes) {
        if (location != null)
            location.begin = Gpr.parseIntSafe(attributes.getValue("position")) - 1; // Transform to zero-based
    }

    /**
     * End of location tag
     */
    public void locationEnd() {
        if (locations != null) {
            if (isIntreaction(category)) {
                // In these cases, the "interval" [start, end] but in reality it is
                // an interaction from position start to position end
                // We convert to an interation location before adding it
                location = new LocationTargetIsoformInteraction((LocationTargetIsoform) location);
            }
            locations.add(location);

            // Check: This category should be added to 'isAnnotate'?
            if (!isAnnotate(category)) {
                entry.getHandler().countMissingCategory(category);
            }
        }
        location = null;
    }

    public void locationEndPos(Attributes attributes) {
        if (location != null)
            location.end = Gpr.parseIntSafe(attributes.getValue("position")) - 1; // Transform to zero-based
    }

    public void locationIsoformStart(String accession) {
        location = new LocationTargetIsoform(accession);
    }

    public void locationStart(Attributes attributes) {
        if (location == null) {
            String type = attributes.getValue("type");
            location = new Location(type);
        }
    }

    /**
     * Return an annotation "name"
     * Clean up characters to make them compatible with VCF annotations
     */
    public String name() {
        var descr = description();
        var name = category + (descr != null ? " " + descr : "");
        name = name.replace('.', '_');
        return VcfEntry.cleanUnderscores(VcfEntry.vcfInfoValueSafe(name));
    }

    @Override
    public String toString() {
        return toString("");

    }

    public String toString(String prefix) {
        var sb = new StringBuilder();
        sb.append("Annotation '" + name() + "'\n");
        sb.append("\tDescription: " + description + "\n");
        sb.append("\t" + cvTerm + "\n");
        if (locations != null) {
            for (Location l : locations)
                sb.append(prefix + "\t" + l + "\n");
        }
        return sb.toString();
    }
}
