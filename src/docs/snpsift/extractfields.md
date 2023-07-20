# SnpSift Extract Fields

Extract fields from a VCF file to a TXT, tab separated format, that you can easily load in R, XLS, etc.

### Typical usage

You can also use sub-fields and genotype fields / sub-fields such as:

* Standard VCF fields:
    * `CHROM`
    * `POS`
    * `ID`
    * `REF`
    * `ALT`
    * `FILTER`
* INFO fields:
    * `AF`
    * `AC`
    * `DP`
    * `MQ`
    * etc. (any info field available)
* SnpEff 'ANN' fields:
    * `ANN[*].ALLELE` (alias GENOTYPE)
    * `ANN[*].EFFECT` (alias ANNOTATION): Effect in Sequence ontology terms (e.g. 'missense_variant', 'synonymous_variant', 'stop_gained', etc.)
    * `ANN[*].IMPACT:` { HIGH, MODERATE, LOW, MODIFIER }
    * `ANN[*].GENE:` Gene name (e.g. 'PSD3')
    * `ANN[*].GENEID:` Gene ID
    * `ANN[*].FEATURE`
    * `ANN[*].FEATUREID` (alias TRID: Transcript ID)
    * `ANN[*].BIOTYPE:` Biotype, as described by the annotations (e.g. 'protein_coding')
    * `ANN[*].RANK:` Exon or Intron rank (i.e. exon number in a transcript)
    * `ANN[*].HGVS_C` (alias HGVS_DNA, CODON): Variant in HGVS (DNA) notation
    * `ANN[*].HGVS_P` (alias HGVS, HGVS_PROT, AA): Variant in HGVS (protein) notation
    * `ANN[*].CDNA_POS` (alias POS_CDNA)
    * `ANN[*].CDNA_LEN` (alias LEN_CDNA)
    * `ANN[*].CDS_POS` (alias POS_CDS)
    * `ANN[*].CDS_LEN` (alias LEN_CDS)
    * `ANN[*].AA_POS` (alias POS_AA)
    * `ANN[*].AA_LEN` (alias LEN_AA)
    * `ANN[*].DISTANCE`
    * `ANN[*].ERRORS` (alias WARNING, INFOS)
* SnpEff 'EFF' fields (this is for older SnpEff/SnpSift versions, new version use 'ANN' field):
    * `EFF[*].EFFECT`
    * `EFF[*].IMPACT`
    * `EFF[*].FUNCLASS`
    * `EFF[*].CODON`
    * `EFF[*].AA`
    * `EFF[*].AA_LEN`
    * `EFF[*].GENE`
    * `EFF[*].BIOTYPE`
    * `EFF[*].CODING`
    * `EFF[*].TRID`
    * `EFF[*].RANK`

    !!! info
        You can combine `vcfEffOnePerLine.pl` script with `SnpSift extractFields` if you want to have each effect in a separate line.

* SnpEff 'LOF' fields:
    * `LOF[*].GENE`
    * `LOF[*].GENEID`
    * `LOF[*].NUMTR`
    * `LOF[*].PERC`
* SnpEff' NMD' fields:
    * `NMD[*].GENE`
    * `NMD[*].GENEID`
    * `NMD[*].NUMTR`
    * `NMD[*].PERC`

!!! warning
    When using multiple indexes you must remember to use quotes and escape the character in the command line (e.g. `"ANN[*].EFFECT"`).
    Otherwise, the shell would parse the asterisk changing the expression and producing unexpected results.

!!! info
    You can use command line option `-s` to specify multiple field separator and `-e` to specify how to represent empty fields.


### Example 1: Extracting chromosome, position, ID and allele frequency

```
$ java -jar SnpSift.jar extractFields s.vcf CHROM POS ID AF | head
#CHROM        POS        ID            AF
1             69134                    0.086
1             69496      rs150690004   0.001
1             69511      rs75062661    0.983
1             69569                    0.538
1             721559                   0.001
1             721757                   0.011
1             846854     rs111957712   0.003
1             865584     rs148711625   0.001
1             865625     rs146327803   0.001
```

### Example 2: Extracting genotype fields

    $ java -jar SnpSift.jar extractFields file.vcf "CHROM" "POS" "ID" "THETA" "GEN[0].GL[1]" "GEN[1].GL" "GEN[3].GL[*]" "GEN[*].GT"

This means to extract:

* `CHROM` `POS` `ID`: regular fields (as in the previous example)
* `THETA` : This one is from INFO
* `GEN[0].GL[1]` : Second likelihood from first genotype
* `GEN[1].GL` : The whole GL fields (all entries without separating them)
* `GEN[3].GL[*]` : All likelihoods form genotype 3 (this time they will be tab separated, as opposed to the previous one).
* `GEN[*].GT` : Genotype subfields (GT) from ALL samples (tab separated).

The result will look something like:
```
#CHROM  POS     ID              THETA   GEN[0].GL[1]    GEN[1].GL               GEN[3].GL[*]            GEN[*].GT
1       10583   rs58108140      0.0046  -0.47           -0.24,-0.44,-1.16       -0.48   -0.48   -0.48   0|0     0|0     0|0     0|1     0|0     0|1     0|0     0|0     0|1
1       10611   rs189107123     0.0077  -0.48           -0.24,-0.44,-1.16       -0.48   -0.48   -0.48   0|0     0|1     0|0     0|0     0|0     0|0     0|0     0|0     0|0
1       13302   rs180734498     0.0048  -0.58           -2.45,-0.00,-5.00       -0.48   -0.48   -0.48   0|0     0|1     0|0     0|0     0|0     1|0     0|0     0|1     0|0
1       13327   rs144762171     0.0204  -1.11           -1.97,-0.01,-2.51       -0.48   -0.48   -0.48   0|0     0|1     0|0     0|0     0|0     1|0     0|0     0|0     0|0
1       13957   rs201747181     0.0100  0               0,0,0                   0       0       0       0|0     0|1     0|0     0|0     0|0     0|0     0|0     0|0     0|0
1       13980   rs151276478     0.0139  -0.48           -0.48,-0.48,-0.48       -0.48   -0.48   -0.48   0|0     0|0     0|0     0|0     0|0     0|0     0|0     0|0     0|0
1       30923   rs140337953     0.0162  -0.61           -0.10,-0.69,-2.81       -0.48   -0.48   -0.48   1|1     0|0     0|0     1|1     1|0     0|0     1|1     1|0     1|1
1       46402   rs199681827     0.0121  0               0,0,0                   0       0       0       0|0     0|0     0|0     0|0     0|0     0|0     0|0     0|0     0|0
1       47190   rs200430748     0.0153  0               0,0,0                   0       0       0       0|0     0|0     0|0     0|0     0|0     0|0     0|0     0|0     0|0
```

### Example 3: Extracting fields with multiple values in a friendlier format

You can use command line option `-s` to specify multiple field separator and `-e` to specify how to represent empty fields.

    $ java -jar SnpSift.jar extractFields -s "," -e "." test.chr22.ann.vcf CHROM POS REF ALT "EFF[*].EFFECT" "EFF[*].AA"

Notice how we separate same fields using "," instead of the default tab using the option `-s ","`, and we use "." for empty fields (option `-e "."`).

The results is:
```
$ java -jar SnpSift.jar extractFields -s "," -e "." examples/test.chr22.ann.vcf CHROM POS REF ALT "ANN[*].EFFECT" "ANN[*].HGVS_P"
#CHROM	POS	REF	ALT	ANN[*].EFFECT	ANN[*].HGVS_P
22	17071756	T	C	3_prime_UTR_variant,downstream_gene_variant	.,.
22	17072035	C	T	missense_variant,downstream_gene_variant	p.Gly469Glu,.
22	17072258	C	A	missense_variant,downstream_gene_variant	p.Gly395Cys,.
22	17072674	G	A	missense_variant,downstream_gene_variant	p.Pro256Leu,.
22	17072747	T	C	missense_variant,downstream_gene_variant	p.Met232Val,.
22	17072781	C	T	synonymous_variant,downstream_gene_variant	p.Pro220Pro,.
22	17073043	C	T	missense_variant,downstream_gene_variant	p.Arg133Gln,.
22	17073066	A	G	synonymous_variant,downstream_gene_variant	p.Ala125Ala,.
22	17073119	C	T	missense_variant,downstream_gene_variant	p.Val108Met,.
```

### Example 4: Extracting effects, one per line

In order to extract effects, you can simply do something like this (notice that there are multiple columns per line because there are multiple effects per variant):
```
$ java -jar SnpSift.jar extractFields examples/test.chr22.ann.vcf CHROM POS REF ALT "ANN[*].EFFECT"
#CHROM	POS	REF	ALT	ANN[*].EFFECT
22	17071756	T	C	3_prime_UTR_variant	downstream_gene_variant
22	17072035	C	T	missense_variant	downstream_gene_variant
22	17072258	C	A	missense_variant	downstream_gene_variant
22	17072674	G	A	missense_variant	downstream_gene_variant
22	17072747	T	C	missense_variant	downstream_gene_variant
22	17072781	C	T	synonymous_variant	downstream_gene_variant
22	17073043	C	T	missense_variant	downstream_gene_variant
22	17073066	A	G	synonymous_variant	downstream_gene_variant
22	17073119	C	T	missense_variant	downstream_gene_variant
```
Note that since some variant have more than one effect, there can be more than one "EFFECT" column.

If we prefer to have one effect per line, then we can use the `vcfEffOnePerLine.pl` provided with SnpEff distribution
```
$ cat examples/test.chr22.ann.vcf \
    | ./scripts/vcfEffOnePerLine.pl \
    | java -jar SnpSift.jar extractFields - CHROM POS REF ALT "ANN[*].EFFECT" \

#CHROM	POS	REF	ALT	ANN[*].EFFECT
22	17071756	T	C	3_prime_UTR_variant
22	17071756	T	C	downstream_gene_variant
22	17072035	C	T	missense_variant
22	17072035	C	T	downstream_gene_variant
22	17072258	C	A	missense_variant
22	17072258	C	A	downstream_gene_variant
22	17072674	G	A	missense_variant
22	17072674	G	A	downstream_gene_variant
22	17072747	T	C	missense_variant
```
Now we obtain one effect per line, while all other parameters in the line are repeated across mutiple lines (e.g. there are two chr22:17071756 lines, one for each variant annotation).

!!! info
    Note that in SnpSift, we used `-` as input file name, which denotes STDIN.

### Example 5: Extracting genotype using genotype name instead of genotype number

As of SnpSift version 4.1A, you can use the genotype name in expressions:

```
$ java -jar SnpSift.jar extractFields examples/1kg.head_chr1.vcf.gz CHROM POS REF ALT "GEN[HG00096].DS" "GEN[HG00097].DS"
#CHROM    POS      REF  ALT    GEN[HG00096].DS    GEN[HG00097].DS
1         10583    G    A      0.2                 0.15
1         10611    C    G      0.05                0.75
1         13302    C    T      0.05                1.0
1         13327    G    C      0.0                 0.95
1         13957    TC   T      0.05                0.65
1         13980    T    C      0.05                0.6
1         30923    G    T      1.75                0.35
1         46402    C    CTGT   0.05                0.15
1         47190    G    GA     0.15                0.0
```

### Example 6: Extracting non alphanumeric field names

!!! warning
    `SnpSift extractFields` can get confused if the VCF field has non-alphanumeric charaters in the name (e.g. `dbNSFP_GERP++_RS` has two "+" signs).
    A quick fix, it so is to change the field names in the VCF file.

Here is an example:
```
# Change field names in VCF
$ cat kath.vcf | sed "s/dbNSFP_GERP++/dbNSFP_GERP/g" > kath.gerp.vcf

# Use new names to extract fields
$ java -jar SnpSift.jar  extractFields kath.gerp.vcf CHROM POS REF ALT dbNSFP_GERP_RS dbNSFP_GERP_NS
#CHROM	POS	REF	ALT	dbNSFP_GERP_RS
1	142827044	G	A		
2	132914561	G	A		
7	151933217	C	A		
7	151933251	T	C		
7	151933302	T	C		
7	151945101	G	C	-0.892
7	151945167	G	T		
7	151962176	T	A		
7	151970672	A	T		
7	151970856	T	A	3.71
18	14183638	G	C		
18	14183710	A	G		
18	14542909	G	A		
18	14543039	T	C	-0.942
```
