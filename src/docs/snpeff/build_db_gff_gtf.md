# Building databases: GTF / GFF details

In this section we show some specific details on the GTF and GFF file format required by SnpEff to build databases.

!!! warning
    Most people do NOT need to build a database, and can safely use a pre-built one.
    So unless you are working with a rare, custom, or new genomes you most likely don't need to do it either.

## Summary

As seen in the previous [Building databases](#building-databases), there are three main steps when building a database:

1. **Step 1:** [Configure a new genome](#step-1-configure-a-new-genome) in SnpEff's config file `snpEff.config`.
2. **Step 2:** [Build using gene annotations and reference sequences](#step-2-build-using-gene-annotations-and-reference-sequences)
3. **Step 3:** [Checking the database](#step-3-checking-the-database): SnpEff will check the database by comparing predicted protein sequences and CDS sequences with ones provided by the user.

In this section we'll go into the details of the GTF and GFF format requirements for **Step 2**.
As a general rule, GTF format is preferred over GFF, so if your genome provides both GTF anf GFF, use GTF whenever possible.

## GTF format example

This is a snippet example from a GTF file that fulfills SnpEff's requirements. 
The example (from ENSEMBL's human genome GTF file) shows the definition of one gene, one transcript and it's exons, as well as the trancript's start codon, stop codon, and UTR regions.

```
# Note that tabs have been replaced by spaces for readability
chr1    ensembl  gene          10472288        10630758        .       +       .       gene_id "ENSG00000142655.13"; gene_type "protein_coding"; gene_name "PEX14";
chr1    ensembl  transcript    10474950        10630758        .       +       .       gene_id "ENSG00000142655.13"; transcript_id "ENST00000356607.9"; transcript_type "protein_coding";
chr1    ensembl  exon          10474950        10475002        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10474967        10475002        .       +       0       transcript_id "ENST00000356607.9";
chr1    ensembl  start_codon   10474967        10474969        .       +       0       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10495274        10495321        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10495274        10495321        .       +       0       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10536213        10536297        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10536213        10536297        .       +       0       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10599238        10599366        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10599238        10599366        .       +       2       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10618332        10618417        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10618332        10618417        .       +       2       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10623019        10623121        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10623019        10623121        .       +       0       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10624340        10624437        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10624340        10624437        .       +       2       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10627272        10627363        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10627272        10627363        .       +       0       transcript_id "ENST00000356607.9";
chr1    ensembl  exon          10629531        10630758        .       +       .       transcript_id "ENST00000356607.9";
chr1    ensembl  CDS           10629531        10629984        .       +       1       transcript_id "ENST00000356607.9";
chr1    ensembl  stop_codon    10629985        10629987        .       +       0       transcript_id "ENST00000356607.9";
chr1    ensembl  UTR           10474950        10474966        .       +       .       transcript_id "ENST00000356607.9"; 
chr1    ensembl  UTR           10629985        10630758        .       +       .       transcript_id "ENST00000356607.9"; 
```

For a more detailed example, check ENSEMBL's GTF files, for instance [this one for GRCh38.107](http://ftp.ensembl.org/pub/release-107/gtf/homo_sapiens/Homo_sapiens.GRCh38.107.gtf.gz) (the human genome)
## GTF format details

The full GTF format specification is beyond the scope of this section, and it is assumed you are familiar with it.
It's probably a good idea to take a look at the format specification before reading the rest of this section, here are some links:

- [ENSEMBL GTF format specification](https://useast.ensembl.org/info/website/upload/gff.html)
- [Brent's lab GTF format](https://mblab.wustl.edu/GTF22.html)

### GTF File name

SnpEff expects the GTF file to be located at 

```
$SNPEFF_HOME/data/GENOME_NAME/genes.gtf
```

where:

- `$SNPEFF_HOME` is the directory where SnpEff is installed (usually `$HOME/snpEff`)
- `GENOME_NAME` is the genome name of the genome you are trying to build, which MUST match the name you added in the config file `snpEff.config`

Note: The file name can be `genes.gff.gz` if it's compressed using `gzip`.

### GTF lines

In a nutshell, GTF files are text files and each line is parsed separately.

Lines that start with `#` are treated as comments (i.e. ignored).

Each (non-comment) line is parsed as a tab-separate list of fields, for example:

```
#!genome-build GRCh38.p13
1       protein_coding  gene         69091   70008   .       +       .       gene_id "ENSG00000186092"; gene_name "OR4F5"; gene_source "ensembl_havana"; gene_biotype "protein_coding";
1       protein_coding  transcript   69091   70008   .       +       .       gene_id "ENSG00000186092"; transcript_id "ENST00000335137"; gene_name "OR4F5"; gene_source "ensembl_havana"; gene_biotype "protein_coding"; transcript_name "OR4F5-001"; transcript_source "ensembl_havana"; tag "CCDS"; ccds_id "CCDS30547";
1       protein_coding  exon         69091   70008   .       +       .       gene_id "ENSG00000186092"; transcript_id "ENST00000335137"; exon_number "1"; gene_name "OR4F5"; gene_source "ensembl_havana"; gene_biotype "protein_coding"; transcript_name "OR4F5-001"; transcript_source "ensembl_havana"; tag "CCDS"; ccds_id "CCDS30547"; exon_id "ENSE00002319515";
1       protein_coding  CDS          69091   70005   .       +       0       gene_id "ENSG00000186092"; transcript_id "ENST00000335137"; exon_number "1"; gene_name "OR4F5"; gene_source "ensembl_havana"; gene_biotype "protein_coding"; transcript_name "OR4F5-001"; transcript_source "ensembl_havana"; tag "CCDS"; ccds_id "CCDS30547"; protein_id "ENSP00000334393";
1       protein_coding  start_codon  69091   69093   .       +       0       gene_id "ENSG00000186092"; transcript_id "ENST00000335137"; exon_number "1"; gene_name "OR4F5"; gene_source "ensembl_havana"; gene_biotype "protein_coding"; transcript_name "OR4F5-001"; transcript_source "ensembl_havana"; tag "CCDS"; ccds_id "CCDS30547";
1       protein_coding  stop_codon   70006   70008   .       +       0       gene_id "ENSG00000186092"; transcript_id "ENST00000335137"; exon_number "1"; gene_name "OR4F5"; gene_source "ensembl_havana"; gene_biotype "protein_coding"; transcript_name "OR4F5-001"; transcript_source "ensembl_havana"; tag "CCDS"; ccds_id "CCDS30547";
```

It should be noted that lines do NOT have a specific order.
Usually information defining a gene tends to be together, but this is not required by the GTF format.

### GTF fields

The nine tab-separated fields in each (non-comment) line are:

1. **seqname:** name of the chromosome or scaffold; chromosome names can be given with or without the 'chr' prefix. Important note: the seqname must be one used within Ensembl, i.e. a standard chromosome name or an Ensembl identifier such as a scaffold ID, without any additional content such as species or assembly. See the example GFF output below.
2. **source:** name of the program that generated this feature, or the data source (database or project name)
3. **feature:** feature type name, e.g. Gene, transcript, exon, etc.
4. **start:** Start position* of the feature, with sequence numbering starting at 1.
5. **end:** End position* of the feature, with sequence numbering starting at 1.
6. **score:** A floating point value.
7. **strand:** defined as + (forward), or - (reverse).
8. **frame:** One of '0', '1' or '2'. '0' indicates that the first base of the feature is the first base of a codon, '1' that the second base is the first base of a codon, and so on..
9. **attribute:** A semicolon-separated list of tag-value pairs, providing additional information about each feature.

!!! info
    Also note that `'.'` denotes an empty field, you cannot just use an empty string to denote an empty field.

### GTF field requirements

SnpEff requires that the fields are:

1. **seqname:** Must match the name of the chromosome / scaffold in the reference genome sequence FASTA file (with or without a `chr` prepend).
2. **source:** This field is ignored by SnpEff.
3. **feature:** These feature types, such as `gene`, ` exon`, `cds`, etc. See details in section [GTF Feature](gtf-feature) 
5. **start:** One-based chromosome position of feature start (base included).
6. **end:** One-based chromosome position of feature end (base included).
7. **score:** SnpEff ignores this field.
8. **strand:** Considered negative strand if `'-'`, otherwise interpreted as positive strand.
9. **frame:** Interpreted as 'phase', can be `{0, 1, 2}`. If empty (`'.'`) or `-1` is interpreted as "missing". See ["GTF Frame details"](#gtf-frame-details) section below.
11. **attribute:** Attribute list, see [GTF Attributes](#gtf-attributes) section below 

### GTF Feature

These feature types will be translated to SnpEff entities the following way (case ignored):

| Feature Type         | Feature value                                                                                 |
|----------------------|-----------------------------------------------------------------------------------------------|
| GENE                 | gene, protein                                                                                 |  
| TRANSCRIPT           | pseudogene, transcript, mrna, trna, snorna, rrna, ncrna, mirna, snrna, pseudogenic_transcript |          
| EXON                 | exon, pseudogenic_exon                                                                        |
| CDS                  | cds                                                                                           |          
| START_CODON          | start_codon                                                                                   |          
| STOP_CODON           | stop_codon                                                                                    |          
| UTR5                 | five_prime_utr, 5'-utr, 5'utr, 5utr                                                           |          
| UTR3                 | three_prime_utr, 3'-utr, 3'utr, 3utr                                                          |          
| INTRON_CONSERVED     | intron_CNS, intron_cns                                                                        |          
| INTERGENIC_CONSERVED | inter_cns                                                                                     |

### GTF Frame

The frame field indicates the number of bases that should be **removed** from the beginning	of this feature to reach the first base of the next codon.
This is typically used in `EXON` or `CDS` features within in coding genes.

Possible values are:

- `0`: indicates that the feature begins with a whole codon at the 5' most base.
- `1`: means that there is one extra base (the third base of a codon) before the first whole codon and 
- `2`: means that there are two extra bases (the second and third bases of the codon) before the first codon
- `.`: Missing value, SnpEff will inferr this value from the feature's coordinates

!!! info
    Sometimes this is called 'phase' instead of frame, to distinguish form the *"coding base modulo 3"* definition. 

**Frame correction**
SnpEff performs a "frame correction".
If the frame value calculated using the feature (exon) coordinates differs from the one given in the `start / end` coordinates, the coordinates will be corrected.

This correction is performed in two stages, for each transcript:

i) First exon is corrected by adding a fake 5'UTR
ii) Other exons are corrected by changing the start (or end) coordinates. We drop bases from either the `start` coordinate (if the exon is on the positive strand) or `end` coordinate (if the exon is on the negative strand) until the frame matches the one from the GTF.


** Check zero frames:**
If all frames are zero, there is a high chance that the frame values are incorrectly labeled as zero instead of "missing values" (i.e. '.').
SnpEff will check if **all** frame values are zero. If there are more than `MIN_TOTAL_FRAME_COUNT` frmae values set (by default 10) and all of them are zero, it will show a warning.

### GTF Attributes

The "attributes" field is parsed as a semicolon-separated list of key-value pairs, providing additional information about each feature.

Required attributes are:

| Feature type                       | Required attributes                            | Optional attributes                              | 
|------------------------------------|------------------------------------------------|--------------------------------------------------|
| GENE                               | ID / GeneID, GeneBioType,                      | GeneName                                         |
| TRANSCRIPT                         | ID / TranscriptID, TranscriptBioType, ParentID | `transcript_support_level`, `transcript_version` |
| CDS, EXON, STOP_CODON, START_CODON | ID, ParentID / TranscriptID                    |                                                  |
| UTR, UTR5, UTR3                    | ID, ParentID / TranscriptID                    |                                                  |
| INTRON_CONSERVED                   | ID, TranscriptID / TranscriptID                |                                                  |
| INTERGENIC_CONSERVED               | ID                                             |                                                  |

### GTF Attribute: ID

The attribute name can be (not case sensitive, in search order):

- `id`
- `gene_id` (if feature type is `GENE`)
- `transcript_id` (if feature type is `TRANSCRIPT`)
- `exon_id` (if feature type is `EXON`),
- `db_xref`
- `name`

If none is available, SnpEff will generate an ID as 
```
feature + "_" + chromosomeName + "_" + start + "_" + end
```

where `feature` is the parse "Feature type" .

### GTF Attribute: ParentId

The attribute name can be (not case sensitive, in search order):

- `parent`
- `gene` (if feature type is `TRANSCRIPT` or `INTRON_CONSERVED`)
- same as TranscriptId (if feature type is any of 'EXON', 'CDS', 'START_CODON', 'STOP_CODON', 'UTR3', or 'UTR5')

!!! warning
    The value of `ParentID` must match exactly the `ID` of the parent feature (e.g. the `ParentID` for a transcript, must match the ID of the parent gene).
    It is a common mistake in some GTF / GFF files to add or remove some characters.
    If the IDs don't match, the GTF/GFF file is invalid for SnpEff.


### GTF Attribute: GeneId

The attribute name can be (not case sensitive, in search order):

- `gene_id`
- `id` (if feature type is `GENE`)

`GeneId` value must be a unique ID for each gene in the genome. If the value is repeated, SnpEff will add a dot ('.') followed by an integer number to make is unique.

### GTF Attribute: TranscriptId

The attribute name can be (not case sensitive, in search order):

- `transcript_id`
- `id` (if feature type is `TRANSCRIPT`)
- or the same as `ParentID` if feature type is `EXON`

### GTF Attribute: GeneName

The attribute name can be (not case sensitive, in search order):

- `gene_name`
- `name` (if feature type is `GENE`)

### GTF Attribute: BioType

The attribute name can be (not case sensitive): `biotype`

Possible values are:

| BioType                          | Possible attribute values                                                                                                    |
|----------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| protein_coding                   | mrna, protein, cds, trna, start_codon, stop_codon, five_prime_utr, 5'-utr, 5'utr, 5utr, three_prime_utr, 3'-utr, 3'utr, 3utr |
| transcribed_processed_pseudogene | pseudogenic_transcript, pseudogenic_exon                                                                                     |
| lincRNA                          | ncrna                                                                                                                        |
| rRNA                             | rrna                                                                                                                         |
| miRNA                            | mirna                                                                                                                        |
| snRNA                            | snrna                                                                                                                        |
| snoRNA                           | snorna                                                                                                                       |
| prime3_overlapping_ncrna         | 3prime_overlapping_ncrna                                                                                                     |

If the BioType field is not found, the GTF **source** field will be parsed, otherwise the feature type will be parsed.

### GTF Attribute: GeneBioType

This is similar to BioType, used specifically for feature type `GENE`
The attribute name can be (not case sensitive, in search order):

- `gene_biotype`
- `gene_type`
- `biotype`

Attribute values are parsed the same maner as `BioType`.

If `GeneBioType` is `protein_coding`, then the gene is assumed to be a protein coding (all transcripts within will be also considered protein coding).

### GTF Attribute: TranscriptBioType

This is similar to BioType, used specifically for feature type `TRANSCRIPT`

The attribute name can be (not case sensitive, in search order): 

- `transcript_biotype`
- `transcript_type`
- `biotype`

Attribute values are parsed the same maner as `BioType`.

If `TranscriptBioType` is `protein_coding`, then the transcript is assumed to be a protein coding transcript.

## GFF

SnpEff treats GFF files the same way as GTF files.

The GFF format is more flexible / lax than GTF.
Unfortunately, this extra flexibility also means that it is difficult to find GFF files that fulfill the requirements to build a genomic database, as many people add the information in different ways.

!!! info
    Generally GTF files are preferred to build databases

[GFF3](http://gmod.org/wiki/GFF3) is the currently supported version, the old GFF2 format is [deprecated](http://gmod.org/wiki/GFF2)

### GFF File name

SnpEff expects the GFF file to be located at 

```
$SNPEFF_HOME/data/GENOME_NAME/genes.gff
```
where:
 
- `$SNPEFF_HOME` is the directory where SnpEff is installed (usually `$HOME/snpEff`)
- `GENOME_NAME` is the genome name of the genome you are trying to build, which MUST match the name you added in the config file `snpEff.config`

Note: The file name can be `genes.gff.gz` if it's compressed using `gzip`. 

### GTF lines and fields

GFF lines and fields are very similar to GTF ones.
The main difference is that the attributes field is formatted as semi-colon separated `key=value` pairs (in GTF `key` and `value` are separated by a space instead of an `=` sign).

!!! info
    Other than the minor difference in attributes formatting, SnpEff parses and interprets all the fields and attributes exactly the same way as in GTF files.


### GFF genome sequence

GFF files can have the reference genome sequence in the same file.
After a special comment `##FASTA` you can concatenate the whole genome FASTA file.
For example (see [GFF3 Sequence Section](http://gmod.org/wiki/GFF3#GFF3_Sequence_Section)):

```
##gff-version 3
ctg123 . exon            1300  1500  .  +  .  ID=exon00001
ctg123 . exon            1050  1500  .  +  .  ID=exon00002
ctg123 . exon            3000  3902  .  +  .  ID=exon00003
ctg123 . exon            5000  5500  .  +  .  ID=exon00004
ctg123 . exon            7000  9000  .  +  .  ID=exon00005
##FASTA
>ctg123
cttctgggcgtacccgattctcggagaacttgccgcaccattccgccttg
tgttcattgctgcctgcatgttcattgtctacctcggctacgtgtggcta
tctttcctcggtgccctcgtgcacggagtcgagaaaccaaagaacaaaaa
aagaaattaaaatatttattttgctgtggtttttgatgtgtgttttttat
aatgatttttgatgtgaccaattgtacttttcctttaaatgaaatgtaat
cttaaatgtatttccgacgaattcgaggcctgaaaagtgtgacgccattc
...
```

This makes it easier to distribute the genome reference toghether with the genome annotations in one file.

