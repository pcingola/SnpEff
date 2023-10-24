# Input &amp; output files

Files used as input to SnpEff must comply with standard formats.
Here we describe supported input data formats.

### VCF files

As we mentioned before, [Variant Call Format (VCF)](http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41) is the recommended format
for input files.
This is the format used by the "1000 Genomes Project", and is currently considered the de facto standard for genomic variants.
It is also the default format used in SnpEff.

In a nutshell, VCF format is tab-separated text file having the following columns:

1. Chromosome name
2. Position
3. Variant's ID
4. Reference genome
5. Alternative (i.e. variant)
6. Quality score
7. Filter (whether or not the variant passed quality filters)
8. INFO : Generic information about this variant. SnpEff adds annotation information in this column.

Here is an example of a few lines in a VCF file:
```
#CHROM POS     ID        REF    ALT     QUAL FILTER INFO                    
20     14370   rs6054257 G      A       29   PASS   NS=3;DP=14;AF=0.5;DB;H2
20     17330   .         T      A       3    q10    NS=3;DP=11;AF=0.017   
```
Note that the first line is header information. Header lines start with '#'

### VCF output

As we mentioned in the previous chapter, VCF is SnpEff's default input and output format.
It is highly recommended to use VCF as input and output format, since it is a standard format that can be also used by other tools and software packages.
Thus VCF makes it much easier to integrate genomic data processing pipelines.

SnpEff adds annotation information to the INFO field of a VCF file.
The INFO field is the eight column of a VCF file, see previous section for a quick example or take a look at the
[VCF specification](http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41) for details.

Here is an example of a file before and after being annotated using SnpEff:
VCF file before annotations
```
#CHROM POS     ID        REF    ALT     QUAL FILTER INFO                    
1	889455	.	G	A	100.0	PASS	AF=0.0005
1	897062	.	C	T	100.0	PASS	AF=0.0005
```

VCF file after being annotated using SnpEff
```
#CHROM POS     ID        REF    ALT     QUAL FILTER INFO                    
1	889455	.	G	A	100.0	PASS	AF=0.0005;EFF=STOP_GAINED(HIGH|NONSENSE|Cag/Tag|Q236*|749|NOC2L||CODING|NM_015658|)
1	897062	.	C	T	100.0	PASS	AF=0.0005;EFF=STOP_GAINED(HIGH|NONSENSE|Cag/Tag|Q141*|642|KLHL17||CODING|NM_198317|)
```
A you can see, SnpEff added an 'EFF' tag to the INFO field (eight column).

### VCF Header lines

SnpEff updates the header of the VCF file to reflect additional fields.
This is required by the VCF specification.
SnpEff also adds the command line options used to annotate the file as well as SnpEff's version, so you can keep track of what exactly was done.

Here is an example of some header lines added to an annotated file:
```
##SnpEffVersion="SnpEff 3.1m (build 2013-02-08)"
##SnpEffCmd="SnpEff  hg19 demo.1kg.vcf "
##INFO=<ID=EFF,Number=.,Type=String,Description="Predicted effects for this variant.Format: 'Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_change| Amino_Acid_length | Gene_Name | Gene_BioType | Coding | Transcript | Exon [ | ERRORS | WARNINGS ] )' \">
```
### ANN field (VCF output files)

Functional annotations information is added to the INFO field using an `ANN` tag.

**NOTE:** field. SnpEff implements the [**VCF annotation standard 'ANN' field.**](../adds/VCFannotationformat_v1.0.pdf)

This format specification has been created by the developers of the most widely used variant annotation programs (SnpEff, ANNOVAR and ENSEMBL's VEP)
and attempts to:

* provide a common framework for variant annotation,
* make pipeline development easier,
* facilitate benchmarking, and
* improve some known problems in variant annotations.

Obviously this 'ANN' field broke compatibility with the old 'EFF' field from old SnpEff versions. In order to use the old 'EFF' field, you can use the `-formatEff` command line option.

The annotation 'ANN' field looks like this (the full annotation standard specification can be found [here](../adds/VCFannotationformat_v1.0.pdf)).

```
ANN=T|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.1406G>A|p.Gly469Glu|1666/2034|1406/1674|469/557||,T|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397G>A|||||3944|
```

A variant can have (and usually has) more than one annotation.
Multiple annotations are separated by commas.
In the previous example there were two annotations corresponding to different genes (CCT8L2 and FABP5P11).

Each annotation consists of multiple sub-fields separated by the pipe character "|" (fields 15 and 16 are empty in this example):
```
Annotation      : T|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.1406G>A|p.Gly469Glu|1666/2034|1406/1674|469/557|  |
SubField number : 1|       2        |    3   |  4   |       5       |    6     |      7        |      8       | 9 |    10   |    11     |   12    |   13    |   14  |15| 16
```

Here is a description of the meaning of each sub-field:

1. **Allele (or ALT):** In case of multiple ALT fields, this helps to identify which ALT we are referring to.
    E.g.:

        # CHROM  POS     ID  REF  ALT    QUAL  FILTER  INFO
        chr1    123456  .   C    A      .     .       ANN=A|...
        chr1    234567  .   A    G,T    .     .       ANN=G|... , T|...

    In case of cancer sample, when comparing somatic versus germline using a non-standard reference (e.g. one of the ALTs is the reference) the format should be ALT-REFERENCE. E.g.:

        #CHROM  POS     ID  REF  ALT  QUAL  FILTER  INFO
        chr1    123456  .   A    C,G  .     .       ANN=G-C|...

    Compound variants: two or more variants affecting the annotations (e.g. two consecutive SNPs conforming a MNP, two consecutive frame_shift variants that "recover" the frame).
    In this case, the Allele field should include a reference to the other variant/s included in the annotation:

        #CHROM  POS     ID  REF  ALT  QUAL  FILTER  INFO
        chr1    123456  .   A    T    .     .       ANN=T|...
        chr1    123457  .   C    G    .     .       ANN=C-chr1:123456_A>T|...

2. **Annotation (a.k.a. effect):** Annotated using Sequence Ontology terms. Multiple effects can be concatenated using '&amp;'.

        #CHROM  POS     ID  REF  ALT  QUAL  FILTER  INFO
        chr1    123456  .   C    A    .     .      ANN=A|intron_variant&nc_transcript_variant|...

3. **Putative_impact:** A simple estimation of putative impact / deleteriousness : `{HIGH, MODERATE, LOW, MODIFIER}`
4. **Gene Name:** Common gene name (HGNC). Optional: use closest gene when the variant is "intergenic".
5. **Gene ID:** Gene ID
6. **Feature type:** Which type of feature is in the next field (e.g. transcript, motif, miRNA, etc.). It is preferred to use Sequence Ontology (SO) terms, but 'custom' (user defined) are allowed.

        ANN=A|stop_gained|HIGH|||transcript|...
    Tissue specific features may include cell type / tissue information separated by semicolon e.g.:

        ANN=A|histone_binding_site|LOW|||H3K4me3:HeLa-S3|

7. **Feature ID:** Depending on the annotation, this may be: Transcript ID (preferably using version number), Motif ID, miRNA, ChipSeq peak, Histone mark, etc.
Note: Some features may not have ID (e.g. histone marks from custom Chip-Seq experiments may not have a unique ID).
8. **Transcript biotype:** The bare minimum is at least a description on whether the transcript is {"Coding", "Noncoding"}. Whenever possible, use ENSEMBL biotypes.
9. **Rank / total:** Exon or Intron rank / total number of exons or introns.
10. **HGVS.c:** Variant using HGVS notation (DNA level)
11. **HGVS.p:** If variant is coding, this field describes the variant using HGVS notation (Protein level). Since transcript ID is already mentioned in 'feature ID', it may be omitted here.
12. **cDNA_position / cDNA_len:** Position in cDNA and trancript's cDNA length (one based).
13. **CDS_position / CDS_len:** Position and number of coding bases (one based includes START and STOP codons).
14. **Protein_position / Protein_len:** Position and number of AA (one based, including START, but not STOP).
15. **Distance to feature:** All items in this field are options, so the field could be empty.
    * Up/Downstream: Distance to first / last codon
    * Intergenic: Distance to closest gene
    * Distance to closest Intron boundary in exon (+/- up/downstream). If same, use positive number.
    * Distance to closest exon boundary in Intron (+/- up/downstream)
    * Distance to first base in MOTIF
    * Distance to first base in miRNA
    * Distance to exon-intron boundary in splice_site or splice _region
    * ChipSeq peak: Distance to summit (or peak center)
    * Histone mark / Histone state: Distance to summit (or peak center)
16. **Errors, Warnings or Information messages:** Add errors, warnings or informative message that can affect annotation accuracy. [See details here](#errors-and-warnings)

**Consistency between HGVS and functional annotations:**

In some cases there might be inconsistent reporting between 'annotation' and HGVS.
This is due to the fact that VCF recommends aligning to the leftmost coordinate, whereas HGSV recommends aligning to the "most 3-prime coordinate".
For instance, an InDel on the edge of an exon, which has an 'intronic' annotation according to VCF alignment recommendation, can lead to a 'stop_gained' when aligned using HGVS's recommendation (using the most 3-prime possible alignment).
So the 'annotation' sub-field will report 'intron' whereas HGVS sub-field will report a 'stop_gained'.
This is obviously inconsistent and must be avoided.
In order to report annotations that are consistent with HGVS notation, variants must be re-aligned according to each transcript's strand (i.e. align the variant according to the transcript's most 3-prime coordinate).
Then annotations are calculated, thus the reported annotations will be consistent with HGVS notation.
Annotation software should have a command line option to override this behaviour (e.g. `-no_shift_hgvs`)

### EFF field (VCF output files)

Effects information is added to the INFO field using an 'EFF' tag.

!!!warning
    This section refers the obsolete annotation format using the 'EFF' tag which can be activated using the `-formatEff` command line option.
    As of version 4.1 SnpEff uses the 'ANN' field by default.

Notes:

* As of version 4.0, the default output uses Sequence Ontology for 'Effect' names. You can output "old" style effect names by using the `-classic` command line option.
* When multiple effects are available, they are sorted first by "Effect_Impact", then by "Effect" and finally by "marker's genomic coordinates" (e.g. affected transcript's genomic coordinates).
* Staring from version 4.0, SnpEff outputs HGVS notation in the 'AA' sub-field by default.

There can be multiple effects separated by comma. The format for each effect is:
```
EFF= Effect ( Effect_Impact | Functional_Class | Codon_Change | Amino_Acid_Change| Amino_Acid_Length | Gene_Name | Transcript_BioType | Gene_Coding | Transcript_ID | Exon_Rank  | Genotype_Number [ | ERRORS | WARNINGS ] )
```

| EFF Sub-field           | Meaning                                                                                             |
|-------------------------|-----------------------------------------------------------------------------------------------------|
| Effect                  | Effect of this variant. See details [here](#eff-field-vcf-output-files).                            |
| Effect impact           | Effect impact {High, Moderate, Low, Modifier}. See details [here](#eff-field-vcf-output-files).     |
| Functional Class        | Functional class {NONE, SILENT, MISSENSE, NONSENSE}.                                                |
| Codon_Change / Distance | Codon change: old_codon/new_codon OR distance to transcript (in case of upstream / downstream)      |
| Amino_Acid_Change       | Amino acid change: old_AA AA_position/new_AA (e.g. 'E30K')                                          |
| Amino_Acid_Length       | Length of protein in amino acids (actually, transcription length divided by 3).                     |
| Gene_Name               | Gene name                                                                                           |
| Transcript_BioType      | Transcript bioType, if available.                                                                   |
| Gene_Coding             | `[CODING                                                                                            | NON_CODING]`. This field is 'CODING' if any transcript of the gene is marked as protein coding.
| Transcript_ID           | Transcript ID (usually ENSEMBL IDs)                                                                 |
| Exon/Intron Rank        | Exon rank or Intron rank (e.g. '1' for the first exon, '2' for the second exon, etc.)               |
| Genotype_Number         | Genotype number corresponding to this effect (e.g. '2' if the effect corresponds to the second ALT) |
| Warnings / Errors       | Any warnings or errors (not shown if empty).                                                        |

### Multiple annotations per VCF line

Usually there is more than one annotation reported in each `ANN` (or `EFF`) field.

There are several reasons for this:

* A variant can affect multiple genes. E.g a variant can be DOWNSTREAM from one gene and UPSTREAM from another gene. E.g.:
* In complex organisms, genes usually have multiple transcripts. So SnpEff reports the effect of a variant on each transcript.
    E.g.:

        #CHROM  POS       ID   REF  ALT    QUAL  FILTER  INFO
        1       889455    .    G    A      .     .       .

    In this case SnpEff will report the effect of each variant on each gene and each transcript (output edited for readability):

        #CHROM  POS     ID   REF  ALT  QUAL FILTER   INFO
        1       889455  .    G    A    .    .        ANN=A|stop_gained|HIGH|NOC2L|ENSG00000188976|transcript|ENST00000327044|protein_coding|7/19|c.706C>T|p.Gln236*|756/2790|706/2250|236/749||
                                                        ,A|downstream_gene_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000487214|processed_transcript||n.*865C>T|||||351|
                                                        ,A|downstream_gene_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000469563|retained_intron||n.*878C>T|||||4171|
                                                        ,A|non_coding_exon_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000477976|retained_intron|5/17|n.2153C>T||||||;LOF=(NOC2L|ENSG00000188976|6|0.17);NMD=(NOC2L|ENSG00000188976|6|0.17)

  * A VCF line can have more then one variant.
      E.g. If reference genome is 'G', but the sample has either 'A' or 'T' (non-biallelic variant), then this will be reported as one VCF line, having multiple alternative variants (notice that there are two ALTs):

          #CHROM  POS       ID   REF  ALT      QUAL  FILTER  INFO
          1       889455    .    G    A,T      .     .       .

      In this case SnpEff will report the effect of each ALT on each gene and each transcript.
      Notice that ENST00000327044 has a `stop_gained` variant (ALT = 'A') and a `missense_variant` (ALT = 'T')

          #CHROM  POS      ID    REF  ALT    QUAL FILTER    INFO
          1       889455   .     G    A,T    .    .         ANN=A|stop_gained|HIGH|NOC2L|ENSG00000188976|transcript|ENST00000327044|protein_coding|7/19|c.706C>T|p.Gln236*|756/2790|706/2250|236/749||
                                                               ,T|missense_variant|MODERATE|NOC2L|ENSG00000188976|transcript|ENST00000327044|protein_coding|7/19|c.706C>A|p.Gln236Lys|756/2790|706/2250|236/749||
                                                               ,A|downstream_gene_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000487214|processed_transcript||n.*865C>T|||||351|
                                                               ,T|downstream_gene_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000487214|processed_transcript||n.*865C>A|||||351|
                                                               ,A|downstream_gene_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000469563|retained_intron||n.*878C>T|||||4171|
                                                               ,T|downstream_gene_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000469563|retained_intron||n.*878C>A|||||4171|
                                                               ,A|non_coding_exon_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000477976|retained_intron|5/17|n.2153C>T||||||
                                                               ,T|non_coding_exon_variant|MODIFIER|NOC2L|ENSG00000188976|transcript|ENST00000477976|retained_intron|5/17|n.2153C>A||||||;LOF=(NOC2L|ENSG00000188976|6|0.17);NMD=(NOC2L|ENSG00000188976|6|0.


**Effect sort order**. When multiple effects are reported, SnpEff sorts the effects the following way:

* Putative impact: Effects having higher putative impact are first.
* Effect type: Effects assumed to be more deleterious effects first.
* Canonical transcript before non-canonical.
* Marker genomic coordinates (e.g. genes starting before first).

### Variant annotaiton details

Detailed description of the variant's functional annotation predicted by SnpEff in the `Effect` and `Effect_Impact` sub-fields.

Notes:

* **Effect (Sequence Ontology)**
Sequence ontology ([SO](http://www.sequenceontology.org/)) allows to standardize terminology used for assessing sequence changes and impact.
This allows for a common language across all variant annotation programs and makes it easier to communicate using a uniform terminology.
Starting from version 4.0 VCF output uses SO terms by default.
* **Effect (Classic)** This are the "classic" effect names usd by SnpEff, these can be accessed using the `-classic` command line option.
* **Effect impact** Effects are categorized by 'impact': {High, Moderate, Low, Modifier}. This are pre-defined categories to help users find more significant variants.

    !!! warning
        Impact categories must be used with care, they were created only to help and simplify the filtering process.
        Obviously, there is no way to predict whether a "high impact" or a "low impact" variant is the one producing a phenotype of interest.

Here is a list of effects and some brief explanations:

| Effect<br>Seq. Ontology                                                                                                                                                                  | Effect<br>Classic                    | Note &amp; Example                                                                                                                                                  | Impact   |
|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------|
| [coding_sequence_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001580)                                                                                           | `CDS`                                  | The variant hits a CDS.                                                                                                                                             | `MODIFIER` |
| [chromosome](http://www.sequenceontology.org/browser/current_svn/term/SO:0001580)                                                                                                        | `CHROMOSOME_LARGE_DELETION`            | A large parte (over 1%) of the chromosome was deleted.                                                                                                              | `HIGH`     |
| [duplication](http://www.sequenceontology.org/browser/current_svn/term/SO:1000035)                                                                                                       | `CHROMOSOME_LARGE_DUPLICATION`         | Duplication of a large chromosome segment (over 1% or 1,000,000 bases)                                                                                              | `HIGH`     |
| [inversion](http://www.sequenceontology.org/browser/current_svn/term/SO:1000036)                                                                                                         | `CHROMOSOME_LARGE_INVERSION`           | Inversion of a large chromosome segment (over 1% or 1,000,000 bases).                                                                                               | `HIGH`     |
| [coding_sequence_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001580)                                                                                           | `CODON_CHANGE`                         | One or many codons are changed <br>e.g.:  An MNP of size multiple of 3                                                                                              | `LOW`      |
| [inframe_insertion](http://www.sequenceontology.org/browser/current_svn/term/SO:0001821)                                                                                                 | `CODON_INSERTION`                      | One or many codons are inserted <br>e.g.:  An insert multiple of three in a codon boundary                                                                          | `MODERATE` |
| [disruptive_inframe_insertion](http://www.sequenceontology.org/browser/current_svn/term/SO:0001824)                                                                                      | `CODON_CHANGE_PLUS` `CODON_INSERTION`  | One codon is changed and one or many codons are inserted <br>e.g.:  An insert of size multiple of three, not at codon boundary                                      | `MODERATE` |
| [inframe_deletion](http://www.sequenceontology.org/browser/current_svn/term/SO:0001822)                                                                                                  | `CODON_DELETION`                       | One or many codons are deleted <br>e.g.:  A deletion multiple of three at codon boundary                                                                            | `MODERATE` |
| [disruptive_inframe_deletion](http://www.sequenceontology.org/browser/current_svn/term/SO:0001826)                                                                                       | `CODON_CHANGE_PLUS` `CODON_DELETION`   | One codon is changed and one or more codons are deleted <br>e.g.:  A deletion of size multiple of three, not at codon boundary                                      | `MODERATE` |
| [downstream_gene_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001632)                                                                                           | `DOWNSTREAM`                           | Downstream of a gene (default length: 5K bases)                                                                                                                     | `MODIFIER` |
| [exon_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001791)                                                                                                      | `EXON`                                 | The variant hits an exon (from a non-coding transcript) or a retained intron.                                                                                       | `MODIFIER` |
| [exon_loss_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001572)                                                                                                 | `EXON_DELETED`                         | A deletion removes the whole exon.                                                                                                                                  | `HIGH`     |
| [exon_loss_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001572)                                                                                                 | `EXON_DELETED_PARTIAL`                 | Deletion affecting part of an exon.                                                                                                                                 | `HIGH`     |
| [duplication](http://www.sequenceontology.org/browser/current_svn/term/SO:1000035)                                                                                                       | `EXON_DUPLICATION`                     | Duplication of an exon.                                                                                                                                             | `HIGH`     |
| [duplication](http://www.sequenceontology.org/browser/current_svn/term/SO:1000035)                                                                                                       | `EXON_DUPLICATION_PARTIAL`             | Duplication affecting part of an exon.                                                                                                                              | `HIGH`     |
| [inversion](http://www.sequenceontology.org/browser/current_svn/term/SO:1000036)                                                                                                         | `EXON_INVERSION`                       | Inversion of an exon.                                                                                                                                               | `HIGH`     |
| [inversion](http://www.sequenceontology.org/browser/current_svn/term/SO:1000036)                                                                                                         | `EXON_INVERSION_PARTIAL`               | Inversion affecting part of an exon.                                                                                                                                | `HIGH`     |
| [frameshift_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001589)                                                                                                | `FRAME_SHIFT`                          | Insertion or deletion causes a frame shift <br>e.g.:  An indel size is not multple of 3                                                                             | `HIGH`     |
| [gene_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001564)                                                                                                      | `GENE`                                 | The variant hits a gene.                                                                                                                                            | `MODIFIER` |
| [feature_ablation](http://www.sequenceontology.org/browser/current_svn/term/SO:0001879)                                                                                                  | `GENE_DELETED`                         | Deletion of a gene.                                                                                                                                                 | `HIGH`     |
| [duplication](http://www.sequenceontology.org/browser/current_svn/term/SO:1000035)                                                                                                       | `GENE_DUPLICATION`                     | Duplication of a gene.                                                                                                                                              | `MODIFIER` |
| [gene_fusion](http://www.sequenceontology.org/browser/current_svn/term/SO:0001565)                                                                                                       | `GENE_FUSION`                          | Fusion of two genes.                                                                                                                                                | `HIGH`     |
| [gene_fusion](http://www.sequenceontology.org/browser/current_svn/term/SO:0001565)                                                                                                       | `GENE_FUSION_HALF`                     | Fusion of one gene and an intergenic region.                                                                                                                        | `HIGH`     |
| [bidirectional_gene_fusion](http://www.sequenceontology.org/browser/current_svn/term/SO:0002086)                                                                                         | `GENE_FUSION_REVERSE`                  | Fusion of two genes in opposite directions.                                                                                                                         | `HIGH`     |
| [rearranged_at_DNA_level](http://www.sequenceontology.org/browser/current_svn/term/SO:0000904)                                                                                           | `GENE_REARRANGEMENT`                   | Rearrangement affecting one or more genes.                                                                                                                          | `HIGH`     |
| [intergenic_region](http://www.sequenceontology.org/browser/current_svn/term/SO:0000605)                                                                                                 | `INTERGENIC`                           | The variant is in an intergenic region                                                                                                                              | `MODIFIER` |
| [conserved_intergenic_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0002017)                                                                                      | `INTERGENIC_CONSERVED`                 | The variant is in a highly conserved intergenic region                                                                                                              | `MODIFIER` |
| [intragenic_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0002011)                                                                                                | `INTRAGENIC`                           | The variant hits a gene, but no transcripts within the gene                                                                                                         | `MODIFIER` |
| [intron_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001627)                                                                                                    | `INTRON`                               | Variant hits and intron. Technically, hits no exon in the transcript.                                                                                               | `MODIFIER` |
| [conserved_intron_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0002018)                                                                                          | `INTRON_CONSERVED`                     | The variant is in a highly conserved intronic region                                                                                                                | `MODIFIER` |
| [miRNA](http://www.sequenceontology.org/browser/current_svn/term/SO:0000276)                                                                                                             | `MICRO_RNA`                            | Variant affects an miRNA                                                                                                                                            | `MODIFIER` |
| [missense_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001583)                                                                                                  | `NON_SYNONYMOUS_CODING`                | Variant causes a codon that produces a different amino acid <br>e.g.:  Tgg/Cgg, W/R                                                                                 | `MODERATE` |
| [initiator_codon_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001582)                                                                                           | `NON_SYNONYMOUS_START`                 | Variant causes start codon to be mutated into another start codon (the new codon produces a different AA). <br>e.g.: Atg/Ctg, M/L (ATG and CTG can be START codons) | `LOW`      |
| [stop_retained_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001567)                                                                                             | `NON_SYNONYMOUS_STOP`                  | Variant causes stop codon to be mutated into another stop codon (the new codon produces a different AA). <br>e.g.: Atg/Ctg, M/L (ATG and CTG can be START codons)   | `LOW`      |
| [protein_protein_contact](http://www.sequenceontology.org/browser/current_svn/term/SO:0001093)                                                                                           | `PROTEIN_PROTEIN_INTERACTION_LOCUS`    | Protein-Protein interaction loci.                                                                                                                                   | `HIGH`     |
| [structural_interaction_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0002093)                                                                                    | `PROTEIN_STRUCTURAL_INTERACTION_LOCUS` | Within protein interacion loci (e.g. two AA that are in contact within the same protein, prossibly helping structural conformation).                                | `HIGH`     |
| [rare_amino_acid_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0002008)                                                                                           | `RARE_AMINO_ACID`                      | The variant hits a rare amino acid thus is likely to produce protein loss of function                                                                               | `HIGH`     |
| [splice_acceptor_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001574)                                                                                           | `SPLICE_SITE_ACCEPTOR`                 | The variant hits a splice acceptor site (defined as two bases before exon start, except for the first exon).                                                        | `HIGH`     |
| [splice_donor_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001575 )                                                                                             | `SPLICE_SITE_DONOR`                    | The variant hits a Splice donor site (defined as two bases after coding exon end, except for the last exon).                                                        | `HIGH`     |
| [splice_region_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001630)                                                                                             | `SPLICE_SITE_REGION`                   | A sequence variant in which a change has occurred within the region of the splice site, either within 1-3 bases of the exon or 3-8 bases of the intron.             | `LOW`      |
| [splice_region_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001630)                                                                                             | `SPLICE_SITE_BRANCH`                   | A varaint affective putative (Lariat) branch point, located in the intron.                                                                                          | `LOW`      |
| [splice_region_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001630)                                                                                             | `SPLICE_SITE_BRANCH_U12`               | A varaint affective putative (Lariat) branch point from U12 splicing machinery, located in the intron.                                                              | `MODERATE` |
| [stop_lost](http://www.sequenceontology.org/browser/current_svn/term/SO:0001578)                                                                                                         | `STOP_LOST`                            | Variant causes stop codon to be mutated into a non-stop codon <br>e.g.: Tga/Cga, \*/R                                                                               | `HIGH`     |
| [5_prime_UTR_premature_<br>start_codon_gain_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001988)                                                                | `START_GAINED`                         | A variant in 5'UTR region produces a three base sequence that can be a START codon.                                                                                 | `LOW`      |
| [start_lost](http://www.sequenceontology.org/browser/current_svn/term/SO:0002012)                                                                                                        | `START_LOST`                           | Variant causes start codon to be mutated into a non-start codon. <br> e.g.: aTg/aGg, M/R                                                                            | `HIGH`     |
| [stop_gained](http://www.sequenceontology.org/browser/current_svn/term/SO:0001587)                                                                                                       | `STOP_GAINED`                          | Variant causes a STOP codon <br>e.g.: Cag/Tag, Q/\*                                                                                                                 | `HIGH`     |
| [synonymous_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001819)                                                                                                | `SYNONYMOUS_CODING`                    | Variant causes a codon that produces the same amino acid <br>e.g.:  Ttg/Ctg, L/L                                                                                    | `LOW`      |
| [start_retained](http://www.sequenceontology.org/browser/current_svn/term/SO:0002019)                                                                                                    | `SYNONYMOUS_START`                     | Variant causes start codon to be mutated into another start codon. <br>e.g.:  Ttg/Ctg, L/L (TTG and CTG can be START codons)                                        | `LOW`      |
| [stop_retained_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001567)                                                                                             | `SYNONYMOUS_STOP`                      | Variant causes stop codon to be mutated into another stop codon. <br>e.g.: taA/taG, \*/\*                                                                           | `LOW`      |
| [transcript_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001576)                                                                                                | `TRANSCRIPT`                           | The variant hits a transcript.                                                                                                                                      | `MODIFIER` |
| [feature_ablation](http://www.sequenceontology.org/browser/current_svn/term/SO:0001879)                                                                                                  | `TRANSCRIPT_DELETED`                   | Deletion of a transcript.                                                                                                                                           | `HIGH`     |
| [regulatory_region_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001566)                                                                                         | `REGULATION`                           | The variant hits a known regulatory feature (non-coding).                                                                                                           | `MODIFIER` |
| [upstream_gene_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001631)                                                                                             | `UPSTREAM`                             | Upstream of a gene (default length: 5K bases)                                                                                                                       | `MODIFIER` |
| [3_prime_UTR_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001624)                                                                                               | `UTR_3_PRIME`                          | Variant hits 3'UTR region                                                                                                                                           | `MODIFIER` |
| [3_prime_UTR_truncation](http://www.sequenceontology.org/browser/current_svn/term/SO:0002015) + [exon_loss](http://www.sequenceontology.org/browser/current_svn/term/SO:0001572)         | `UTR_3_DELETED`                        | The variant deletes an exon which is in the 3'UTR of the transcript                                                                                                 | `MODERATE` |
| [5_prime_UTR_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001623)                                                                                               | `UTR_5_PRIME`                          | Variant hits 5'UTR region                                                                                                                                           | `MODIFIER` |
| [5_prime_UTR_truncation](http://www.sequenceontology.org/browser/current_svn/term/SO:0002013) + [exon_loss_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001572) | `UTR_5_DELETED`                        | The variant deletes an exon which is in the 5'UTR of the transcript                                                                                                 | `MODERATE` |
| [sequence_feature](http://www.sequenceontology.org/browser/current_svn/term/SO:0002013) + [exon_loss_variant](http://www.sequenceontology.org/browser/current_svn/term/SO:0001572)       | `NEXT_PROT`                            | A 'NextProt' based annotation. Details are provided in the 'feature type' sub-field (ANN), or in the effect details (EFF).                                          | `MODERATE` |

### Details about Rare amino acid annotaitons

These are amino acids that occurs very rarely in an organism. For instance, humans are supposed to use 20 amino acids, but
there is also one rare AA. Selenocysteine, single letter code 'U', appears roughly 100 times in the whole genome.
The amino acid is so rare that usually it does not appear in codon translation tables. It is encoded as UGA, which usually
means a STOP codon. Secondary RNA structures are assumed to enable this special translation.

A variant in one of these sites is likely to cause a loss of function in the protein. E.g. in case of a Selenocysteine, a
loss of a selenium molecule is likely to cause loss of function. Put it simply, the assumption is that there is a great deal of trouble
to get that non-standard amino acid there, so it must be important. RARE_AMINO_ACID mark is used to show that special attention should
be paid in these cases.

!!! warning
    When the variant hits a RARE_AMINO_ACID mark, it is likely that the 'old_AA/new_AA' field will be incorrect. This may happen because
    the amino acid is not predictable using a codon table.

### Details about Protein interaction annotaitons

Protein interactions are calculated from [PDB](http://www.rcsb.org/) or [AlphaFold](https://alphafold.ebi.ac.uk/download). There are two main types of interactions:

* `protein_protein_contact:` These are "protein-protein" interaction loci. They are calculated from PDB's co-crystalized structures by inferring pairs of amino acids
  in different proteins that have atoms closer than 3 Angstrom from each other.
* `structural_interaction_variant:` These are "within protein" interaction loci, which are likely to be supporting the protein structure.
  They are calculated from single protein PDB entries, by selecting amino acids that are:
  a) atom within 3 Angstrom of each other; and b) are far away in the AA sequence (over 20 AA distance).
  The assumption is that, since they are very close in distance, they must be "interacting" and thus important for protein structure.

### Impact prediction

SnpEff reports putative variant impact in order to make it easier quickly to categorize and prioritize variants.

!!! warning
    Impact categories must be used with care, they were created only to help and simplify the filtering process.
    Obviously, there is no way to predict whether a `HIGH` impact or a `LOW` impact variant is the one producing a phenotype of interest.

| Impact   | Meaning                                                                                                                                                              | Example                               |
|----------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------|
| `HIGH`	    | The variant is assumed to have high (disruptive) impact in the protein, probably causing protein truncation, loss of function or triggering nonsense mediated decay. | `stop_gained`, `frameshift_variant`       |
| `MODERATE` | A non-disruptive variant that might change protein effectiveness.                                                                                                    | `missense_variant`, `inframe_deletion`    |
| `LOW`      | Assumed to be mostly harmless or unlikely to change protein behavior.                                                                                                | `synonymous_variant`                    |
| `MODIFIER` | Usually non-coding variants or variants affecting non-coding genes, where predictions are difficult or there is no evidence of impact.                               | `exon_variant`, `downstream_gene_variant` |


### Functional class

When a variant is a single nucleotide (SNV) in a protein coding transcript, SnpEff will inferr the "Functional class".

Functional class can be, which is inferred as:

| Functional Class | Meaning                                              |
|------------------|------------------------------------------------------|
| `SILENT`           | The condon remains the same after the variant change |
| `MISSENSE`         | The codon changes after the variant change           |
| `NONSENSE`         | The codon changed into a STOP codon                  |


### Loss of function (LOF) and nonsense-mediated decay (NMD) predictions

Loss of function ('LOF') and nonsense-mediated decay ('NMD') predictions.
In older versions, this prediction was activated using command line option `-lof`, but as of version 4.0, it is activated by default.
Some details on how these variants work, can be found in [these slides](../adds/snpEff_lof_nmd.pdf).

!!! info
    Starting from version 4.0, this option is activated by default.

Analyze if a set of effects are can create a "Loss Of Function" and "Nonsense mediated decays" effects.

Needless to say, this is a prediction based on analysis of groups of "putative effects". Proper wet-lab validation is required to infer "real" LOF.

**References:** I used the LOF definition used in the following paper [A Systematic Survey of Loss-of-Function Variants in Human Protein-Coding Genes](http://www.sciencemag.org/content/335/6070/823.abstract).

!!! info
    From the paper:

    *We adopted a definition for LoF variants expected to correlate with complete loss of function
    of the affected transcripts: stop codon-introducing (nonsense) or splice site-disrupting single-nucleotide
    variants (SNVs), insertion/deletion (indel) variants predicted to disrupt a transcript's reading frame, or
    larger deletions removing either the first exon or more than 50% of the protein-coding sequence of the affected transcript.*

    *Both nonsense SNVs and frameshift indels are enriched toward the 3' end of the affected gene, consistent with a greater tolerance to truncation
    close to the end of the coding sequence; putative LoF variants identified in the last 5% of the coding region were thus systematically
    removed from our high-confidence set.*

Other parameters used for LOF/NMD calculations:

* Number of bases before last exon-exon junction that nonsense mediated decay is supposed to occur: 50
* It is assumed that even with a protein coding change at the last 5% of the protein, the protein could still be functional.
* It is assumed that even with a protein coding change at the first 5% of the protein: "..suggesting some disrupted transcripts are rescued by transcriptional reinitiation at an alternative start codon."
* Larger deletions removing either the first exon or more than 50% of the protein-coding sequence of the affected transcript

Usage example:
```
# Note: Form version 4.0 onwards, the '-lof' command line option is not required
java -Xmx8g -jar snpEff.jar -v \
    -lof \
    GRCh37.75 \
    test.chr22.vcf > test.chr22.ann.vcf
```

SnpEff adds 'LOF' and 'NMD' tags to INFO fields (column 8 in VCF format).  LOF and NMD tags have the following format:

    Gene | ID | num_transcripts | percent_affected

Where:

| Field            | Description                                         |
|------------------|-----------------------------------------------------|
| Gene             | Gene name                                           |
| ID               | Gene ID (usually ENSEMBL)                           |
| Num_transcripts  | Number of transcripts in this gene                  |
| percent_affected | Percentage of transcripts affected by this variant. |

Example: If we have this effect

    EFF=stop_gained(LOW|NONSENSE|Gga/Tga|p.Gly163*/c.487G>T|574|GAB4|protein_coding|CODING|ENST00000400588|3|1),...

and the corresponding LOF and NMD tags are

    LOF=(GAB4|ENSG00000215568|4|0.25);NMD=(GAB4|ENSG00000215568|4|0.25)

The meaning of the LOF tag is:

| Field            | Description                                      |
|------------------|--------------------------------------------------|
| Gene             | GAB4                                             |
| ID               | ENSG00000215568                                  |
| Num_transcripts  | There are 4 transcripts in this gene             |
| percent_affected | 25% of transcripts are affected by this variant. |

### Errors and Warnings

As mentioned int the previous section, the last sub-field in EFF field shows errors or warnings (if any).
Here is a description of the errors and warnings:

| Error                           | Meaning and possible solutions                                                                                                           |
|---------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `ERROR_CHROMOSOME_NOT_FOUND`    | Chromosome does not exits in reference database. Typically indicates a mismatch between the chromosome names in the input file and the chromosome names used in the reference. See this [FAQ](faq.md#error-chromosome-not-found) for more details.                  |
| `ERROR_OUT_OF_CHROMOSOME_RANGE` | This means that the position is higher than chromosome's length. Probably an indicator that your data is not from this reference genome. |
| `ERROR_OUT_OF_EXON`             | Exonic information not matching the coordinates. Indicates a problem (or even a bug?) in the database                                    |
| `ERROR_MISSING_CDS_SEQUENCE`    | Transcript has no CDS info. Indicates a problem (or even a bug?) in the database                                                         |

| Warning | Meaning and possible solutions |
|---------|--------------------------------|
| `WARNING_REF_DOES_NOT_MATCH_GENOME`       | This means that the `REF` field does not match the reference genome.<br> **Warning!** This warning probably indicated there is something really wrong with your data! <br> This happens when your data was aligned to a different reference genome than the one used to create SnpEff's database. If there are many of these warnings, it's a strong indicator that the data doesn't match and all the annotations will be garbage (because you are using the wrong database). <br>**Solution:** Use the right database to annotate! <br>Due to performance and memory optimizations, SnpEff only checks reference sequence on Exons. |
| `WARNING_SEQUENCE_NOT_AVAILABLE`          | For some reason the exon sequence is not available, so we cannot calculate effects.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 |
| `WARNING_TRANSCRIPT_INCOMPLETE`           | A protein coding transcript whose length is non-multiple of 3. This means that information is missing for one or more amino acids.<br> This is usually due to errors in the genomic information (e.g. the genomic databases provided by UCSC or ENSEMBL). Genomic information databases are constantly being improved and are getting more accurate, but some errors still remain.                                                                                                                                                                                                                                                  |
| `WARNING_TRANSCRIPT_MULTIPLE_STOP_CODONS` | A protein coding transcript has two or more STOP codons in the middle of the coding sequence (CDS). This should not happen and it usually means the genomic information may have an error in this transcript. <br> This is usually due to errors in the genomic information (e.g. the genomic databases provided by UCSC or ENSEMBL). Genomic information databases are constantly being improved and are getting more accurate, but some errors still remain.                                                                                                                                                                      |
| `WARNING_TRANSCRIPT_NO_START_CODON`       | A protein coding transcript does not have a proper START codon. It is rare that a real transcript does not have a START codon, so this probably indicates errors in genomic information for this transcript (e.g. the genomic databases provided by UCSC or ENSEMBL). <br> Genomic information databases are constantly being improved and are getting more accurate, but some errors still remain.                                                                                                                                                                                                                                 |


| Info | Meaning |
|------|---------|
| `INFO_REALIGN_3_PRIME`                    | Variant has been realigned to the most 3-prime position within the transcript. This is usually done to to comply with HGVS specification to always report the most 3-prime annotation.                                                                         |
| `INFO_COMPOUND_ANNOTATION`                | This effect is a result of combining more than one variants (e.g. two consecutive SNPs that conform an MNP, or two consecutive frame_shift variants that compensate frame).                                                                                    |
| `INFO_NON_REFERENCE_ANNOTATION`           | An alternative reference sequence was used to calculate this annotation (e.g. cancer sample comparing somatic vs. germline).                                                                                            

### BED files

In an enrichment experiment, such as ChIP-Seq, the results are enrichment regions, usually called "peaks".
It is common for "peak callers" (algorithms that detect enrichment), write the results in a BED file.
SnpEff can annotate BED files in order to facilitate interpretation of enrichment experiments.

!!! warning
    Column fifth onwards are ignored when using BED file format and they will be lost in the output file.

SnpEff can annotate BED files in order to facilitate interpretation of enrichment experiments.
Annotations are added to the fourth column of the BED file.

E.g.:
```
$ java -Xmx8g -jar snpEff.jar -i bed BDGP5.69 chipSeq_peaks.bed

# SnpEff version 3.3 (build 2013-05-15), by Pablo Cingolani
# Command line: SnpEff  -i bed BDGP5.69 /home/pcingola/fly_pvuseq/chipSeq/Sample_w1118_IP_w_5hmC/w1118_IP_w_5hmC_peaks.bed
# Chromo  Start     End       Name;Effect|Gene|BioType        Score
2L        189463    190154    MACS_peak_1;Exon|exon_6_12_RETAINED|FBtr0078122|protein_coding|spen|protein_coding;Exon|exon_5_10_RETAINED|FBtr0078123|protein_coding|spen|protein_coding;Exon|exon_7_13_RETAINED|FBtr0306341|protein_coding|spen|protein_coding;Exon|exon_6_11_RETAINED|FBtr0078121|protein_coding|spen|protein_coding 245.41
2L        195607    196120    MACS_peak_2;Exon|exon_6_12_RETAINED|FBtr0078122|protein_coding|spen|protein_coding;Exon|exon_5_10_RETAINED|FBtr0078123|protein_coding|spen|protein_coding;Exon|exon_7_13_RETAINED|FBtr0306341|protein_coding|spen|protein_coding;Exon|exon_6_11_RETAINED|FBtr0078121|protein_coding|spen|protein_coding 51.22
2L        527253    527972    MACS_peak_3;Intron|intron_2_RETAINED-RETAINED|FBtr0078063|protein_coding|ush|protein_coding     55.97
2L        711439    711764    MACS_peak_4;Intron|intron_1_RETAINED-RETAINED|FBtr0078045|protein_coding|ds|protein_coding      61.16
2L        1365255   1365556   MACS_peak_5;Upstream|FBtr0077927|protein_coding|CG14346|protein_coding;Upstream|FBtr0077926|protein_coding|CG14346|protein_coding;Intergenic|NLaz...CG14346;Upstream|FBtr0077942|protein_coding|NLaz|protein_coding     62.78
2L        1970199   1970405   MACS_peak_6;Upstream|FBtr0077813|protein_coding|Der-1|protein_coding;Intergenic|tRNA:CR31942...Der-1;Downstream|FBtr0077812|tRNA|tRNA:CR31942|tRNA      110.34
2L        3345637   3346152   MACS_peak_7;Intron|intron_2_ALTTENATIVE_3SS-ALTTENATIVE_3SS|FBtr0089979|protein_coding|E23|protein_coding;Intron|intron_3_ALTTENATIVE_3SS-ALTTENATIVE_3SS|FBtr0089981|protein_coding|E23|protein_coding 65.49
2L        4154734   4155027   MACS_peak_8;Intergenic|CG2955...Or24a;Downstream|FBtr0077468|protein_coding|CG2955|protein_coding       76.92
2L        4643232   4643531   MACS_peak_9;Downstream|FBtr0110769|protein_coding|BG642163|protein_coding;Exon|exon_2_2_RETAINED|FBtr0300354|protein_coding|CG15635|protein_coding      76.92

```
When a peak intersects multiple transcripts or even multiple genes, each annotation is separated by a semicolon.
So if you look into the previous results in more detail, the first line looks like this (format edited for readability purposes):
```
2L  189463  190154  MACS_peak_1;Exon|exon_6_12_RETAINED|FBtr0078122|protein_coding|spen|protein_coding
                                ;Exon|exon_5_10_RETAINED|FBtr0078123|protein_coding|spen|protein_coding
                                ;Exon|exon_7_13_RETAINED|FBtr0306341|protein_coding|spen|protein_coding
                                ;Exon|exon_6_11_RETAINED|FBtr0078121|protein_coding|spen|protein_coding
```
This peak is hitting four transcripts (FBtr0078122, FBtr0078123, FBtr0306341, FBtr0078121) in gene 'spen'.

### Exon naming convention

The format for the exon identifier is `exon_Rank_Total_Type`, where:

* `rank` is the exon rank in the transcript (position in the transcript)
* `total` is the total number of exons in that transcript
* `type` is the exon splice type.

For instance `exon_5_10_RETAINED` would be the fifth exon in a 10 exon transcript.
This exon is type "RETAINED", which means it is not spliced out.

Exons are categorized by splicing as follows:

* `NONE` : Not spliced
* `RETAINED` : All transcripts have this exon
* `SKIPPED` : Some transcripts skip it
* `ALTTENATIVE_3SS` : Some transcripts have and alternative 3' exon start
* `ALTTENATIVE_5SS` : Some transcripts have and alternative 5' exon end
* `MUTUALLY_EXCLUSIVE` : Mutually exclusive (respect to other exon)
* `ALTTENATIVE_PROMOMOTER` : The first exon is different in some transcripts.
* `ALTTENATIVE_POLY_A` : The last exon.

See [this Wikipedia](http://en.wikipedia.org/wiki/Alternative_splicing#Modes) entry for more information on exon splice types.

### Intron naming convention

Similarly to exons, introns are named as `intron_Rank_ExonTypeBefore-ExonTypeAfter`, where:

* `Rank` :  the rank number for this intron in the transcript
* `ExonTypeBefore` :  the splicing type of the exon preceding this intron (see exon naming convention for details).
* `ExonTypeAfter` :  the splicing type of the after this intron (see exon naming convention for details).

For instance `intron_9_SKIPPED-RETAINED` would be the ninth intron of the transcript.
The intron is preceded by a `SKIPPED` exon and followed by a `RETAINED` exon.
