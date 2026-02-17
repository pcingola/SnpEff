# SnpSift Extract Fields

Extract fields from a VCF file to a TXT, tab separated format, that you can easily load in R, XLS, etc.

---

## Command Options

```
java -jar SnpSift.jar extractFields [options] file.vcf fieldName1 fieldName2 ... fieldNameN > tabFile.txt
```

- `-s <separator>` : Separator for multiple values within the same field when using `[*]` wildcards. Default: `\t` (tab).
- `-e <string>` : String to output for empty/missing fields. Default: `''` (empty string).

Use `-` as the file name to read from STDIN.

!!! warning
    The default `-s` separator is tab, which is also the column separator. This means that when using `[*]` wildcards, multiple values appear as extra columns in the output, making it hard to parse programmatically. Use `-s ","` to get comma-separated values within each column instead.

---

## Field expressions

### Standard VCF fields

* `CHROM`
* `POS`
* `ID`
* `REF`
* `ALT`
* `QUAL`
* `FILTER`

### INFO fields

Any INFO field available in the VCF header can be extracted by name (e.g., `AF`, `AC`, `DP`, `MQ`).

For comma-separated INFO values (e.g., per-allele fields), you can use indexing:

* `AF` : Returns the raw comma-separated string (e.g., `0.1,0.2`)
* `AF[0]` : Returns only the first value (e.g., `0.1`)
* `AF[*]` : Iterates over all values, separating them with the `-s` separator

### Genotype fields

* `GEN[0].GT` : GT sub-field of the first sample (0-based index)
* `GEN[0].AD[0]` : First value of the AD sub-field of the first sample (ref allele depth)
* `GEN[0].AD[1]` : Second value of the AD sub-field (alt allele depth)
* `GEN[0].AD` : The full AD field as a raw comma-separated string
* `GEN[HG00096].DS` : Genotype sub-field by sample name (instead of numeric index)
* `GEN[*].GT` : GT from ALL samples (iterated with the `-s` separator)

### SnpEff 'ANN' fields

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

### SnpEff 'EFF' fields (older format, new versions use 'ANN')

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

### SnpEff 'LOF' and 'NMD' fields

* `LOF[*].GENE`, `LOF[*].GENEID`, `LOF[*].NUMTR`, `LOF[*].PERC`
* `NMD[*].GENE`, `NMD[*].GENEID`, `NMD[*].NUMTR`, `NMD[*].PERC`

---

## Wildcard indexing

`[*]` (or `[ANY]`) iterates over all values of a multi-valued field. When multiple `[*]` wildcards appear in different fields, the iteration produces a cartesian product: for each value of the outer iterator, all values of the inner iterator are enumerated.

The iteration priority order (inner to outer) is: genotype sub-values > genotypes > NMD > LOF > effects > variants.

!!! warning
    When using wildcards, remember to quote the expression on the command line (e.g. `"ANN[*].EFFECT"`) to prevent shell glob expansion.

!!! warning
    Field names containing non-alphanumeric characters (like `+` in `dbNSFP_GERP++_RS`) cannot be used directly because the expression parser interprets them as operators.
    As a workaround, rename the fields in the VCF file first (e.g., using `sed`).

---

## Examples

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
* `GEN[1].GL` : The whole GL field as a raw comma-separated string (not split)
* `GEN[3].GL[*]` : All likelihoods from genotype 3, separated by the `-s` separator (default: tab)
* `GEN[*].GT` : Genotype subfields (GT) from ALL samples, separated by the `-s` separator

The result will look something like:
```
#CHROM  POS     ID              THETA   GEN[0].GL[1]    GEN[1].GL               GEN[3].GL[*]            GEN[*].GT
1       10583   rs58108140      0.0046  -0.47           -0.24,-0.44,-1.16       -0.48   -0.48   -0.48   0|0     0|0     0|0     0|1     0|0     0|1     0|0     0|0     0|1
1       10611   rs189107123     0.0077  -0.48           -0.24,-0.44,-1.16       -0.48   -0.48   -0.48   0|0     0|1     0|0     0|0     0|0     0|0     0|0     0|0     0|0
1       13302   rs180734498     0.0048  -0.58           -2.45,-0.00,-5.00       -0.48   -0.48   -0.48   0|0     0|1     0|0     0|0     0|0     1|0     0|0     0|1     0|0
```

Note that `GEN[1].GL` returns the raw comma-separated string `-0.24,-0.44,-1.16`, while `GEN[3].GL[*]` splits the values and separates them with tabs.

### Example 3: Using a custom separator for multi-valued fields

Using `-s ","` to get comma-separated values within each column, and `-e "."` for empty fields:

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

Notice how the effects are comma-separated within one column, making the output easy to parse.

### Example 4: Extracting effects with default separator vs one-per-line

Without `-s`, each effect value becomes a separate tab-separated column (since the default separator is tab):
```
$ java -jar SnpSift.jar extractFields examples/test.chr22.ann.vcf CHROM POS REF ALT "ANN[*].EFFECT"
#CHROM	POS	REF	ALT	ANN[*].EFFECT
22	17071756	T	C	3_prime_UTR_variant	downstream_gene_variant
22	17072035	C	T	missense_variant	downstream_gene_variant
```
Note that since some variants have more than one effect, there can be more than one "EFFECT" column. This makes the output harder to parse programmatically since the number of columns varies per row.

If you prefer one effect per line, use the `vcfEffOnePerLine.pl` script provided with SnpEff:
```
$ cat examples/test.chr22.ann.vcf \
    | ./scripts/vcfEffOnePerLine.pl \
    | java -jar SnpSift.jar extractFields - CHROM POS REF ALT "ANN[*].EFFECT"

#CHROM	POS	REF	ALT	ANN[*].EFFECT
22	17071756	T	C	3_prime_UTR_variant
22	17071756	T	C	downstream_gene_variant
22	17072035	C	T	missense_variant
22	17072035	C	T	downstream_gene_variant
```
Now each effect is on its own line, with variant fields repeated.

!!! info
    `-` as input file name denotes STDIN, enabling piping.

### Example 5: Extracting genotype using sample name instead of index

You can use sample names (from the VCF header) instead of numeric indices:

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
