package org.snpeff.vcf;

import org.snpeff.interval.*;
import org.snpeff.snpEffect.EffectType;
import org.snpeff.snpEffect.VariantEffect;
import org.snpeff.snpEffect.VariantEffect.FunctionalClass;
import org.snpeff.util.Gpr;
import org.snpeff.util.Tuple;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * An 'ANN' or 'EFF' entry in a VCF INFO field
 * Note: 'EFF' is the old version that has been replaced by the standardized 'ANN' field (2014-12)
 * *
 *
 * @author pablocingolani
 */
public class VcfEffect {

    public static boolean debug = false;

    public static String ANN_FIELD_NAMES[] = { //
            "ALLELE", "GT", "GENOTYPE", //
            "EFFECT", "ANNOTATION", //
            "IMPACT", //
            "GENE", //
            "GENEID", //
            "FEATURE", //
            "FEATUREID", "TRID", //
            "BIOTYPE", //
            "RANK", "EXID", //
            "HGVS_C", "HGVS_DNA", "CODON", //
            "HGVS", "HGVS_P", "HGVS_PROT", "AA", //
            "POS_CDNA", "CDNA_POS", //
            "LEN_CDNA", "CDNA_LEN", //
            "POS_CDS", "CDS_POS", //
            "LEN_CDS", "CDS_LEN", //
            "POS_AA", "AA_POS", //
            "LEN_AA", "AA_LEN", //
            "DISTANCE", //
            "ERRORS", "WARNINGS", "INFOS", //
    };

    public static String EFF_FIELD_NAMES[] = { //
            "EFFECT", "IMPACT", "FUNCLASS", "CODON", //
            "AA", //
            "HGVS", //
            "AA_LEN", //
            "GENE", //
            "BIOTYPE", //
            "CODING", //
            "TRID", //
            "RANK", "EXID", //
            "GT", "GENOTYPE_NUMBER", "GENOTYPE", //
            "ERRORS", "WARNINGS", "INFOS", //
    };

    EffFormatVersion formatVersion;
    String vcfFieldString; // Original 'raw' string from VCF Info field
    String vcfFieldStrings[]; // Original 'raw' strings from VCF info field: effectString.split()
    String effString;
    EffectType effectType;
    String effectTypesStr;
    List<EffectType> effectTypes;
    String effectDetails;
    int aaLen, aaPos;
    int cdsLen, cdsPos;
    int cDnaLen, cDnaPos;
    int distance;
    int rank, rankMax;
    BioType bioType;
    String codon, aa, hgvsC, hgvsP;
    VariantEffect.Coding coding;
    String genotype;
    String errorsWarnings;
    String geneName, geneId, featureType, featureId, transcriptId, exonId;
    VariantEffect.EffectImpact impact;
    VariantEffect.FunctionalClass funClass;
    VariantEffect variantEffect;
    boolean useSequenceOntology;
    boolean useHgvs;
    boolean useGeneId;
    boolean useFirstEffect;

    /**
     * Constructor: Guess format version
     */
    public VcfEffect(String effectString) {
        init();
        formatVersion = null; // Force guess
        vcfFieldString = effectString;
        parse();
    }

    /**
     * Constructor: Force format version
     *
     * @param formatVersion : If null, will try to guess it
     */
    public VcfEffect(String effectString, EffFormatVersion formatVersion) {
        init();
        this.formatVersion = formatVersion;
        vcfFieldString = effectString;
        parse();
    }

    public VcfEffect(VariantEffect variantEffect, EffFormatVersion formatVersion) {
        this(variantEffect, formatVersion, true, false);
    }

    public VcfEffect(VariantEffect variantEffect, EffFormatVersion formatVersion, boolean useSequenceOntology, boolean useFirstEffect) {
        init();
        this.formatVersion = formatVersion;
        this.variantEffect = variantEffect;
        this.useSequenceOntology = useSequenceOntology;
        this.useFirstEffect = useFirstEffect;
        set(variantEffect);
    }

    /**
     * Get info field name based on format version
     */
    public static String infoFieldName(EffFormatVersion formatVersion) {
        if (formatVersion == null) return EffFormatVersion.VCF_INFO_ANN_NAME;
        return formatVersion.infoFieldName();
    }

    /**
     * Add subfield to a buffer
     */
    void add(StringBuilder sb, Object obj) {
        if (obj != null) sb.append(VcfEntry.vcfInfoEncode(obj.toString()));
        sb.append("|");
    }

    public void addEffectType(EffectType effectType) {
        effectTypes.add(effectType);
        this.effectType = null;
    }

    /**
     * Create 'ANN' field
     */
    String createAnnField() {
        StringBuilder effBuff = new StringBuilder();

        // Allele
        add(effBuff, genotype);

        // Add main annotation in Sequence Ontology terms
        add(effBuff, effectTypesStr);

        // Add effect impact
        add(effBuff, impact);

        // Gene name
        add(effBuff, geneName);

        // Gene ID
        add(effBuff, geneId);

        // Feature type
        add(effBuff, featureType);

        // Feature ID
        add(effBuff, featureId);

        // Transcript biotype
        add(effBuff, bioType);

        // Add exon (or intron) rank info
        if (rank >= 0) add(effBuff, rank + "/" + rankMax);
        else effBuff.append("|");

        // HGVS
        add(effBuff, hgvsC);
        add(effBuff, hgvsP);

        // cDNA position / length
        if (cDnaPos >= 0) {
            add(effBuff, cDnaPos + "/" + cDnaLen);
        } else effBuff.append("|");

        // CDS position / length
        if (cdsPos >= 0) {
            add(effBuff, cdsPos + "/" + cdsLen);
        } else effBuff.append("|");

        // Protein position / protein length
        if (aaPos >= 0) {
            add(effBuff, aaPos + "/" + aaLen);
        } else effBuff.append("|");

        // Distance: Mostly used for non-coding variants
        if (distance >= 0) add(effBuff, distance);
        else effBuff.append("|");

        // Errors or warnings (this is the last thing in the list)
        effBuff.append(errorsWarnings);

        return effBuff.toString();

    }

    /**
     * Create 'EFF' field
     */
    String createEffField() {
        StringBuilder effBuff = new StringBuilder();

        // Add effect
        effBuff.append(effectTypesStr);
        effBuff.append("(");

        // Add effect impact
        effBuff.append(impact);
        effBuff.append("|");

        // Add functional class
        effBuff.append(funClass == FunctionalClass.NONE ? "" : funClass.toString()); // Show only if it is not empty
        effBuff.append("|");

        // Codon change
        if (!codon.isEmpty()) effBuff.append(codon);
        else if (distance >= 0) effBuff.append(distance);
        effBuff.append("|");

        // Add HGVS (amino acid change)
        if (useHgvs) {
            StringBuilder hgvs = new StringBuilder();
            if (hgvsP != null) hgvs.append(VcfEntry.vcfInfoEncode(hgvsP));
            if (hgvsC != null) {
                if (hgvs.length() > 0) hgvs.append('/');
                hgvs.append(VcfEntry.vcfInfoEncode(hgvsC));
            }

            effBuff.append(hgvs.toString());
        } else effBuff.append(aa);
        effBuff.append("|");

        // Add amino acid length
        if (formatVersion != EffFormatVersion.FORMAT_EFF_2) { // This field is not in format version 2
            effBuff.append(aaLen >= 0 ? aaLen : "");
            effBuff.append("|");
        }

        // Add gene info
        if (variantEffect != null) {
            Gene gene = variantEffect.getGene();
            Transcript tr = variantEffect.getTranscript();
            if (gene != null) {
                // Gene name
                effBuff.append(VcfEntry.vcfInfoValueSafe(useGeneId ? geneId : geneName));
                effBuff.append("|");

                // Transcript biotype
                if (tr != null) {
                    if ((tr.getBioType() != null) && (tr.getBioType() != null)) effBuff.append(tr.getBioType());
                    else
                        effBuff.append(tr.isProteinCoding() ? "protein_coding" : ""); // No biotype? Add protein_coding of we know it is.
                }
                effBuff.append("|");

                // Protein coding gene?
                String coding = "";
                if (gene.getGenome().hasCodingInfo())
                    coding = (gene.isProteinCoding() ? VariantEffect.Coding.CODING.toString() : VariantEffect.Coding.NON_CODING.toString());
                effBuff.append(coding);
                effBuff.append("|");
            } else if (variantEffect.isRegulation()) {
                Regulation reg = (Regulation) variantEffect.getMarker();
                effBuff.append("|" + reg.getRegulationType() + "||");
            } else if (variantEffect.isCustom()) {
                Marker m = variantEffect.getMarker();
                if (m != null) effBuff.append("|" + VcfEntry.vcfInfoValueSafe(m.getId()) + "||");
                else effBuff.append("|||");
            } else effBuff.append("|||");
        } else {
            // No variantEffect? Use parsed information
            effBuff.append(VcfEntry.vcfInfoValueSafe(useGeneId ? geneId : geneName));
            effBuff.append("|");
            effBuff.append(bioType);
            effBuff.append("|");
            effBuff.append(coding);
            effBuff.append("|");
        }

        // Add transcript info
        effBuff.append(VcfEntry.vcfInfoValueSafe(transcriptId));
        effBuff.append("|");

        // Add exon (or intron) rank info
        effBuff.append(rank >= 0 ? rank : "");

        // Add genotype (or genotype difference) for this effect
        if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
            effBuff.append("|");
            effBuff.append(genotype);
        }

        //---
        // Errors or warnings (this is the last thing in the list)
        //---
        if (!errorsWarnings.isEmpty()) {
            effBuff.append("|");
            effBuff.append(errorsWarnings);
        }
        effBuff.append(")");

        return effBuff.toString();
    }

    /**
     * Create INFO field using either 'ANN' or 'EFF' depending on format version
     */
    String createInfoField() {
        if (formatVersion == null || formatVersion.isAnn()) return createAnnField();
        return createEffField();
    }

    /**
     * Guess effect format version
     */
    public EffFormatVersion formatVersion() {
        // Already set?
        if (formatVersion != null && formatVersion.isFullVersion()) return formatVersion;

        // Try to guess format
        if (formatVersion == null) formatVersion = formatVersion(vcfFieldString);

        // Split strings
        if (vcfFieldStrings == null) vcfFieldStrings = split(vcfFieldString);

        // Now we can guess specific sub-version within each format
        if (formatVersion.isAnn()) {
            // Easy guess: So far there is only one version
            formatVersion = EffFormatVersion.FORMAT_ANN_1;
        } else if (formatVersion.isEff()) {
            // On of the 'EFF' formats

            int len = vcfFieldStrings.length;

            // Error or Warning string is not added under normal situations
            String lastField = vcfFieldStrings[len - 2]; // Actually last array item is after the last ')', so we use the previous one
            if (lastField.startsWith("ERROR") //
                    || lastField.startsWith("WARNING") //
                    || lastField.startsWith("INFO") //
            ) len--;

            // Guess format
            if (len <= 11) formatVersion = EffFormatVersion.FORMAT_EFF_2;
            else if (len <= 12) formatVersion = EffFormatVersion.FORMAT_EFF_3;
            else formatVersion = EffFormatVersion.FORMAT_EFF_4;
        } else {
            throw new RuntimeException("Unimplemented formatVersion '" + formatVersion + "'");
        }

        return formatVersion;
    }

    /**
     * Guess format 'main' version (either 'ANN' of 'EFF') without trying to guess sub-version
     */
    protected EffFormatVersion formatVersion(String effectString) {
        // Extract string between left and right parenthesis
        int idxLp = effectString.indexOf('(');

        // No parenthesis at all? Definitively 'ANN'
        if (idxLp < 0) return EffFormatVersion.FORMAT_ANN;

        // Probably 'EFF': how many sub fields between parenthesis?
        int idxRp = effectString.lastIndexOf(')');
        if (idxLp < idxRp) {
            String paren = effectString.substring(idxLp + 1, idxRp);
            String fields[] = paren.split("\\|", -1);
            if (fields.length >= 9) return EffFormatVersion.FORMAT_EFF;
        }

        // Too few sub-fields: It cannot be 'EFF'
        return EffFormatVersion.FORMAT_ANN;
    }

    public String getAa() {
        return aa;
    }

    public void setAa(String aa) {
        this.aa = aa;
    }

    public int getAaLen() {
        return aaLen;
    }

    public void setAaLen(int aaLen) {
        this.aaLen = aaLen;
    }

    public int getAaPos() {
        return aaPos;
    }

    public String getAllele() {
        return genotype;
    }

    public BioType getBioType() {
        return bioType;
    }

    public void setBioType(BioType bioType) {
        this.bioType = bioType;
    }

    public int getcDnaLen() {
        return cDnaLen;
    }

    public int getcDnaPos() {
        return cDnaPos;
    }

    public int getCdsLen() {
        return cdsLen;
    }

    public int getCdsPos() {
        return cdsPos;
    }

    public VariantEffect.Coding getCoding() {
        return coding;
    }

    public void setCoding(VariantEffect.Coding coding) {
        this.coding = coding;
    }

    public String getCodon() {
        return codon;
    }

    public void setCodon(String codon) {
        this.codon = codon;
    }

    public int getDistance() {
        return distance;
    }

    public String getEffectDetails() {
        return effectDetails;
    }

    public void setEffectDetails(String effectDetails) {
        this.effectDetails = effectDetails;
    }

    public String getEffectsStr() {
        StringBuilder sb = new StringBuilder();
        for (EffectType et : effectTypes) {
            if (sb.length() > 0) sb.append(formatVersion.separator());
            sb.append(et);
        }
        return sb.toString();
    }

    public String getEffectsStrSo() {
        StringBuilder sb = new StringBuilder();
        for (EffectType et : effectTypes) {
            if (sb.length() > 0) sb.append(formatVersion.separator());
            sb.append(et.toSequenceOntology(formatVersion, null));
        }
        return sb.toString();
    }

    public EffectType getEffectType() {
        if (effectType != null) return effectType;
        if (effectTypes == null || effectTypes.isEmpty()) return EffectType.NONE;

        // Pick highest effect type
        effectType = EffectType.NONE;
        for (EffectType et : effectTypes)
            if (et.compareTo(effectType) < 0) effectType = et;

        return effectType;
    }

    public void setEffectType(EffectType effect) {
        effectTypes = new LinkedList<>();
        addEffectType(effect);
    }

    public List<EffectType> getEffectTypes() {
        return effectTypes;
    }

    public String getEffectTypesStr() {
        return effectTypesStr;
    }

    public String getEffString() {
        return effString;
    }

    public String getErrorsWarning() {
        return errorsWarnings;
    }

    public String getExonId() {
        return exonId;
    }

    public void setExonId(String exonId) {
        this.exonId = exonId;
    }

    public String getFeatureId() {
        return featureId;
    }

    public String getFeatureType() {
        return featureType;
    }

    /**
     * Get a subfield by name
     */
    public String getFieldByName(String fieldName) {
        switch (fieldName) {

            case "ALLELE":
            case "GT":
            case "GENOTYPE":
            case "GENOTYPE_NUMBER":
                return genotype;

            case "EFFECT":
            case "ANNOTATION":
                return effString;

            case "IMPACT":
                return impact != null ? impact.toString() : "";

            case "FUNCLASS":
                return funClass != null ? funClass.toString() : "";

            case "GENE":
                return geneName;

            case "GENEID":
                return geneId;

            case "FEATURE":
            case "FEATURE_TYPE":
                return featureType;

            case "FEATUREID":
                return featureId;

            case "TRID":
                return transcriptId;

            case "BIOTYPE":
                return (bioType == null ? "" : bioType.toString());

            case "RANK":
                return Integer.toString(rank);

            case "EXID":
                return exonId;

            case "RANK_MAX":
                return Integer.toString(rankMax);

            case "HGVS_C":
            case "HGVS_DNA":
                return hgvsC;

            case "CODON":
                return codon;

            case "HGVS":
            case "HGVS_P":
            case "HGVS_PROT":
                return hgvsP;

            case "AA":
                return aa;

            case "POS_CDNA":
            case "CDNA_POS":
                return Integer.toString(cDnaPos);

            case "LEN_CDNA":
            case "CDNA_LEN":
                return Integer.toString(cDnaLen);

            case "POS_CDS":
            case "CDS_POS":
                return Integer.toString(cdsPos);

            case "LEN_CDS":
            case "CDS_LEN":
                return Integer.toString(cdsLen);

            case "POS_AA":
            case "AA_POS":
                return Integer.toString(aaPos);

            case "LEN_AA":
            case "AA_LEN":
                return Integer.toString(aaLen);

            case "CODING":
                return coding != null ? coding.toString() : "";

            case "DISTANCE":
                return Integer.toString(distance);

            case "ERRORS":
            case "WARNINGS":
            case "INFOS":
                return errorsWarnings;

            default:
                throw new RuntimeException("Field '" + fieldName + "' not found.");
        }
    }

    public EffFormatVersion getFormatVersion() {
        return formatVersion;
    }

    public void setFormatVersion(EffFormatVersion formatVersion) {
        this.formatVersion = formatVersion;
    }

    public VariantEffect.FunctionalClass getFunClass() {
        return funClass;
    }

    public void setFunClass(VariantEffect.FunctionalClass funClass) {
        this.funClass = funClass;
    }

    public String getGeneId() {
        return geneId;
    }

    public void setGeneId(String geneId) {
        this.geneId = geneId;
    }

    public String getGeneName() {
        return geneName;
    }

    public void setGeneName(String geneName) {
        this.geneName = geneName;
    }

    public String getGenotype() {
        return genotype;
    }

    public void setGenotype(String genotype) {
        this.genotype = genotype;
    }

    public String getHgvsC() {
        return hgvsC;
    }

    public String getHgvsDna() {
        return hgvsC;
    }

    public String getHgvsP() {
        return hgvsP;
    }

    public String getHgvsProt() {
        return hgvsP;
    }

    public VariantEffect.EffectImpact getImpact() {
        return impact;
    }

    public void setImpact(VariantEffect.EffectImpact impact) {
        this.impact = impact;
    }

    public int getRank() {
        return rank;
    }

    public int getRankMax() {
        return rankMax;
    }

    public String getTranscriptId() {
        return transcriptId;
    }

    public void setTranscriptId(String transcriptId) {
        this.transcriptId = transcriptId;
    }

    /**
     * String from VCF file (original, unparsed, string)
     */
    public String getVcfFieldString() {
        return vcfFieldString;
    }

    /**
     * Get a subfield as an index
     */
    public String getVcfFieldString(int index) {
        if (index >= vcfFieldStrings.length) return null;
        return vcfFieldStrings[index];
    }

    /**
     * Does it have 'effType' ?
     */
    public boolean hasEffectType(EffectType effType) {
        if (effectTypes == null) return false;
        for (EffectType et : effectTypes)
            if (et == effType) return true;
        return false;

    }

    void init() {
        aaLen = aaPos = cdsLen = cdsPos = cDnaLen = cDnaPos = distance = rank = rankMax = -1;
        vcfFieldString = effString = effectTypesStr = effectDetails = codon = aa = hgvsC = hgvsP = genotype = errorsWarnings = geneName = geneId = featureType = featureId = transcriptId = exonId = errorsWarnings = "";
        bioType = null;
        impact = null;
        funClass = FunctionalClass.NONE;
        useSequenceOntology = true;
        useHgvs = true;
        useGeneId = false;
    }

    /**
     * Parse annotations either in 'ANN' or 'EFF' INFO field
     */
    void parse() {
        // Guess format, if not given
        if (formatVersion == null || !formatVersion.isFullVersion()) formatVersion = formatVersion();

        // Split strings
        vcfFieldStrings = split(vcfFieldString);

        // Parse
        if (formatVersion.isAnn()) parseAnn();
        else parseEff();
    }

    /**
     * Parse 'ANN' field
     */
    void parseAnn() {
        int index = 0;

        // Gentype
        genotype = vcfFieldStrings[index++];

        // Annotation
        effString = vcfFieldStrings[index];
        effectTypesStr = vcfFieldStrings[index];
        effectTypes = parseEffect(vcfFieldStrings[index]);
        index++;

        // Impact
        impact = VariantEffect.EffectImpact.valueOf(vcfFieldStrings[index++]);

        // Gene name
        geneName = vcfFieldStrings[index++];

        // Gene ID
        geneId = vcfFieldStrings[index++];

        // Feature type
        featureType = vcfFieldStrings[index++];

        // Feature ID
        featureId = vcfFieldStrings[index++];
        if (featureType.equals("transcript")) transcriptId = featureId;

        // Biotype
        bioType = BioType.parse(vcfFieldStrings[index++]);

        // Rank '/' rankMax
        Tuple<Integer, Integer> ints = parseSlash(vcfFieldStrings[index++]);
        rank = ints.first;
        rankMax = ints.second;

        // HGVS
        hgvsC = VcfEntry.vcfInfoDecode(vcfFieldStrings[index++]);
        codon = hgvsC;

        hgvsP = VcfEntry.vcfInfoDecode(vcfFieldStrings[index++]);
        aa = hgvsP;

        // cDna: 'pos / len'
        ints = parseSlash(vcfFieldStrings[index++]);
        cDnaPos = ints.first;
        cDnaLen = ints.second;

        // CDS: 'pos / len'
        ints = parseSlash(vcfFieldStrings[index++]);
        cdsPos = ints.first;
        cdsLen = ints.second;

        // AA: 'pos / len'
        ints = parseSlash(vcfFieldStrings[index++]);
        aaPos = ints.first;
        aaLen = ints.second;

        // Distance
        distance = Gpr.parseIntSafe(vcfFieldStrings[index++]);

        // Errors , warnings, info
        errorsWarnings = vcfFieldStrings[index++];
    }

    /**
     * Parse 'EFF' field
     */
    void parseEff() {
        try {
            // Parse each sub field
            int index = 0;

            // Effect
            effString = vcfFieldStrings[index];
            effectTypesStr = vcfFieldStrings[index];
            effectTypes = parseEffect(vcfFieldStrings[index]);
            effectDetails = parseEffectDetails(vcfFieldStrings[index]); // Effect details: everything between '['  and ']' (e.g. Regulation, Custom, Motif, etc.)
            index++;

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                impact = VariantEffect.EffectImpact.valueOf(vcfFieldStrings[index]);
            index++;

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                funClass = VariantEffect.FunctionalClass.valueOf(vcfFieldStrings[index]);
            index++;

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) codon = vcfFieldStrings[index];
            index++;

            // Parse 'AA' and HGVS
            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) aa = vcfFieldStrings[index];
            if (aa.indexOf('/') > 0) {
                String f[] = aa.split("/");

                // HGVS Protein
                if (f.length > 0 && f[0].startsWith("p.")) {
                    hgvsP = VcfEntry.vcfInfoDecode(f[0]);
                }

                // HGVS DNA
                if ((f.length > 1) && (f[1].startsWith("c.") || f[1].startsWith("n.")))
                    hgvsC = VcfEntry.vcfInfoDecode(f[1]);
            } else if (aa.startsWith("c.") || aa.startsWith("n.")) {
                hgvsC = aa = VcfEntry.vcfInfoDecode(aa); // Only HGVS DNA
            }

            index++;

            if (formatVersion != EffFormatVersion.FORMAT_EFF_2) {
                if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                    aaLen = Gpr.parseIntSafe(vcfFieldStrings[index]);
                else aaLen = 0;
                index++;
            }

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                geneName = vcfFieldStrings[index];
            index++;

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                bioType = BioType.parse(vcfFieldStrings[index]);
            index++;

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                coding = VariantEffect.Coding.valueOf(vcfFieldStrings[index]);
            index++;

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                transcriptId = vcfFieldStrings[index];
            index++;

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty()) exonId = vcfFieldStrings[index];
            index++;

            if (formatVersion == EffFormatVersion.FORMAT_EFF_4) {
                if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                    genotype = vcfFieldStrings[index];
                else genotype = "";
                index++;
            }

            if ((vcfFieldStrings.length > index) && !vcfFieldStrings[index].isEmpty())
                errorsWarnings = vcfFieldStrings[index];
            index++;

        } catch (Exception e) {
            String fields = "";
            for (int i = 0; i < vcfFieldStrings.length; i++)
                fields += "\t" + i + " : '" + vcfFieldStrings[i] + "'\n";
            throw new RuntimeException("Error parsing:\n\t'" + vcfFieldString + "'\n\t EFF formatVersion : " + formatVersion + "\n" + fields, e);
        }
    }

    /**
     * Parse effect sub-field
     */
    List<EffectType> parseEffect(String eff) {
        int idx = eff.indexOf('[');
        if (idx > 0) eff = eff.substring(0, idx);

        List<EffectType> effs = new LinkedList<>();
        if (eff.isEmpty()) return effs;

        // Split multiple effectTypes
        if (eff.indexOf(EffFormatVersion.EFFECT_TYPE_SEPARATOR_OLD) >= 0) {
            // Old version
            for (String es : eff.split("\\" + EffFormatVersion.EFFECT_TYPE_SEPARATOR_OLD))
                effs.add(EffectType.parse(formatVersion, es));
        } else {
            // Split effect strings
            for (String es : eff.split(formatVersion.separatorSplit()))
                effs.add(EffectType.parse(formatVersion, es));
        }

        return effs;
    }

    /**
     * Parse effect details.
     * E.g. NEXT_PROT[amino_acid_modification:Phosphoserine]  returns "amino_acid_modification:Phosphoserine"
     */
    String parseEffectDetails(String eff) {
        int idx = eff.indexOf('[');
        if (idx < 0) return "";
        return eff.substring(idx + 1, eff.length() - 1);
    }

    /**
     * Parse two integers separated by a slash
     */
    Tuple<Integer, Integer> parseSlash(String str) {
        int i1 = -1, i2 = -1;

        if (str != null && !str.isEmpty()) {
            String fields[] = str.split("/");

            if (fields.length >= 2) {
                // Two numbers separated by a slash
                i1 = Gpr.parseIntSafe(fields[0]);
                i2 = Gpr.parseIntSafe(fields[1]);
            } else {
                // Only one number?
                i1 = Gpr.parseIntSafe(fields[0]);
            }
        }

        return new Tuple<>(i1, i2);
    }

    /**
     * Set all fields form 'variantEffect'
     */
    void set(VariantEffect variantEffect) {
        // Allele
        Variant variant = variantEffect.getVariant();
        Gene gene = variantEffect.getGene();
        Marker marker = variantEffect.getMarker();
        Transcript tr = variantEffect.getTranscript();

        // Genotype
        if (variant.getGenotype() != null) genotype = variant.getGenotype();
        else if (!variant.isVariant()) genotype = variant.getReference();
        else genotype = variant.getAlt();
        // else if (var.isNonRef()) genotype = var.getGenotype();

        // Effect
        effectType = variantEffect.getEffectType();
        effectTypes = variantEffect.getEffectTypes();

        if (formatVersion.isAnn()) {
            effectTypesStr = variantEffect.getEffectTypeString(true, useFirstEffect, formatVersion);
        } else {
            effectTypesStr = variantEffect.effect(true, false, false, useSequenceOntology, useFirstEffect);
        }

        // Impact
        impact = variantEffect.getEffectImpact();

        // Functional class
        funClass = variantEffect.getFunctionalClass();

        // Gene
        List<Gene> genes = variantEffect.isMultipleGenes() ? variantEffect.getGenes() : null;
        if (genes != null) { // Multiple genes
            setGeneNameIdMultiple(variantEffect);
        } else if (gene != null) {
            geneName = gene.getGeneName();
            geneId = gene.getId();

            // Transcript ID
            if (tr != null) transcriptId = tr.getId();
        } else if (marker instanceof Intergenic) {
            geneName = ((Intergenic) marker).getName();
            geneId = marker.getId();
        } else {
            geneName = geneId = "";
        }


        // Feature type & ID
        featureType = featureId = "";
        if (marker != null) {
            if (marker instanceof Custom) {
                // Custom
                featureType = marker.getType() + formatVersion.separator() + ((Custom) marker).getLabel();
                featureId = marker.getId();
            } else if (marker instanceof Regulation) {
                // Regulation includes cell type
                Regulation reg = (Regulation) marker;
                featureType = reg.getType() + formatVersion.separator() + reg.getName() + ":" + reg.getRegulationType();
                featureId = marker.getId();
            } else if (marker instanceof NextProt) {
                featureType = ((NextProt) marker).getName();
                featureId = ((NextProt) marker).getTranscriptId();
            } else if (marker instanceof Motif) {
                Motif motif = (Motif) marker;
                featureType = motif.getPwmName();
                featureId = motif.getPwmId();
            } else if (marker instanceof ProteinStructuralInteractionLocus) {
                featureType = "interaction";
                featureId = marker.getId();
            } else if (marker instanceof ProteinProteinInteractionLocus) {
                featureType = "interaction";
                featureId = marker.getId();
            } else if (tr != null) {
                featureType = "transcript";
                featureId = tr.getId();
                // Append version number (this is recommended by HGVS specification)
                if (tr.getVersion() != null && !tr.getVersion().isEmpty()) featureId += "." + tr.getVersion();
            } else {
                featureType = marker.getType().toSequenceOntology(formatVersion, null);
                featureId = marker.getId();
            }
        }

        // Biotype
        if (tr != null) {
            if (tr.getBioType() != null) {
                bioType = tr.getBioType();
            } else {
                // No biotype? Add protein_coding of we know it is.
                bioType = BioType.coding(tr.isProteinCoding());
            }
        } else {
            bioType = null;
        }

        // Find and set rank and rankMax
        setRank();

        // Codon change
        codon = variantEffect.getCodonChangeMax();

        // AA change
        aa = variantEffect.getAaChange();

        // HGVS notation
        hgvsC = variantEffect.getHgvsDna();
        hgvsP = variantEffect.getHgvsProt();

        // cDna position & len (cDNA is the DNA version of mRNA)
        if (tr != null) {
            cDnaPos = variantEffect.getcDnaPos();
            if (cDnaPos >= 0 && formatVersion.isAnn()) cDnaPos++; // 1-based position;
            cDnaLen = tr.mRna().length();
        } else {
            cDnaPos = cDnaLen = -1;
        }

        // CDS position / length
        if (tr != null) {
            cdsPos = variantEffect.getCodonNum() * 3 + variantEffect.getCodonIndex();
            if (cdsPos >= 0 && formatVersion.isAnn()) cdsPos++; // 1-based position;
            cdsLen = variantEffect.getCdsLength();
        } else {
            cdsPos = cdsLen = -1;
        }

        // Protein position / protein length
        if (tr != null) {
            aaPos = variantEffect.getCodonNum();
            if (aaPos >= 0 && formatVersion.isAnn()) aaPos++; // 1-based position;
            aaLen = variantEffect.getAaLength();
        } else {
            aaPos = aaLen = -1;
        }

        // Distance: Mostly used for non-coding variants
        distance = variantEffect.getDistance();

        if (variantEffect.hasError() || variantEffect.hasWarning()) {
            StringBuilder err = new StringBuilder();
            // Add errors
            if (!variantEffect.getError().isEmpty()) {
                err.append(variantEffect.getError());
            }

            // Add warnings
            if (!variantEffect.getWarning().isEmpty()) {
                if (err.length() > 0) err.append(formatVersion.separator());
                err.append(variantEffect.getWarning());
            }

            errorsWarnings = err.toString();
        }
    }

    /**
     * Structural variant having multiple genes: Set all geneNames and geneIds
     */
    void setGeneNameIdMultiple(VariantEffect variantEffect) {
        // Get all genes
        List<Gene> genes = variantEffect.getGenes();

        // Sort by geneName
        Collections.sort(genes, new Comparator<Gene>() {
            @Override
            public int compare(Gene g1, Gene g2) {
                return g1.getGeneName().compareTo(g2.getGeneName());
            }
        });

        // Create gene name and geneId strings
        StringBuilder geneNames = new StringBuilder();
        StringBuilder geneIds = new StringBuilder();

        String sep = "";
        for (Gene g : genes) {
            geneNames.append(sep + g.getGeneName());
            geneIds.append(sep + g.getId());
            if (sep.isEmpty()) sep = formatVersion.separator();
        }

        geneName = geneNames.toString();
        geneId = geneIds.toString();
    }

    /**
     * Find and set rank and rank max
     */
    void setRank() {
        Transcript tr = variantEffect.getTranscript();

        // Rank
        Exon ex = variantEffect.getExon();
        rank = -1;
        if (ex != null) {
            rank = ex.getRank();
            rankMax = tr.numChilds();
            return;
        }
        // Do we have an intron?
        Intron intron = variantEffect.getIntron();
        if (intron != null) {
            rank = intron.getRank();
            rankMax = Math.max(0, tr.numChilds() - 1);
            return;
        }

        if (tr == null) return;

        // Exon not explicitly set. Try to find it
        Variant variant = variantEffect.getVariant();
        for (Exon e : tr)
            if (e.intersects(variant)) {
                rank = e.getRank();
                rankMax = tr.numChilds();
                return;
            }

        // Intron not explicitly set. Try to find it.
        List<Intron> introns = tr.introns();
        for (Intron in : introns) {
            if (in.intersects(variant)) {
                rank = in.getRank();
                rankMax = introns.size();
                return;
            }
        }

        // Nothing found
        rank = rankMax = -1;
    }

    public void setUseFirstEffect(boolean useFirstEffect) {
        this.useFirstEffect = useFirstEffect;
    }

    public void setUseGeneId(boolean useGeneId) {
        this.useGeneId = useGeneId;
    }

    public void setUseHgvs(boolean useHgvs) {
        this.useHgvs = useHgvs;
    }

    /**
     * Split a 'effect' string to an array of strings
     */
    public String[] split(String eff) {
        // ANN format versions
        if (formatVersion.isAnn()) {
            // Negative number means "use trailing empty as well"
            return eff.replace('|', '\t').split("\t", -1);
        }

        // EFF format version
        if (formatVersion.isEff()) {
            int idxBr = eff.indexOf('[');
            int idxParen = eff.indexOf('(');

            String eff0 = null;
            if ((idxBr >= 0) && (idxBr < idxParen)) {
                int idxRbr = eff.indexOf(']');
                eff0 = eff.substring(0, idxRbr + 1);
                eff = eff.substring(idxRbr);
            }

            eff = eff.replace('(', '\t'); // Replace all chars by spaces
            eff = eff.replace('|', '\t');
            eff = eff.replace(')', '\t');
            String effs[] = eff.split("\t", -1); // Negative number means "use trailing empty as well"

            if (eff0 != null) effs[0] = eff0;

            return effs;
        }

        throw new RuntimeException("Unimplemented format version '" + formatVersion + "'");
    }

    @Override
    public String toString() {
        if (formatVersion == null || formatVersion.isAnn()) return createAnnField();
        return createEffField();
    }

}
