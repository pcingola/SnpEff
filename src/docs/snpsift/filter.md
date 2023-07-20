# SnpSift filter

SnpSift filter is one of the most useful SnpSift commands.
Using SnpSift filter you can filter VCF files using arbitrary expressions, for instance `"(QUAL > 30) | (exists INDEL) | ( countHet() > 2 )"`. The actual expressions can be quite complex, so it allows for a lot of flexibility.

### Typical usage

Some examples for the impatient:

* I want to filter out samples with quality less than 30:

        cat variants.vcf | java -jar SnpSift.jar filter " ( QUAL >= 30 )" > filtered.vcf

* ...but we also want InDels that have quality 20 or more:

        cat variants.vcf | java -jar SnpSift.jar filter "(( exists INDEL ) & (QUAL >= 20)) | (QUAL >= 30 )" > filtered.vcf

* ...or any homozygous variant present in more than 3 samples:

        cat variants.vcf | java -jar SnpSift.jar filter "(countHom() > 3) | (( exists INDEL ) & (QUAL >= 20)) | (QUAL >= 30 )" > filtered.vcf

* ...or any heterozygous sample with coverage 25 or more:

        cat variants.vcf | java -jar SnpSift.jar filter "((countHet() > 0) && (DP >= 25)) | (countHom() > 3) | (( exists INDEL ) & (QUAL >= 20)) | (QUAL >= 30 )" > filtered.vcf

* I want to keep samples where the genotype for the first sample is homozygous variant and the genotype for the second sample is reference:

        cat variants.vcf | java -jar SnpSift.jar filter "isHom( GEN[0] ) & isVariant( GEN[0] ) & isRef( GEN[1] )" > filtered.vcf

* I want to keep samples where the ID matches a set defined in a file:

        cat variants.vcf | java -jar SnpSift.jar filter --set my_rs.txt "ID in SET[0]" > filtered.vcf

    and the file my_rs.txt has one string per line, e.g.:

        rs58108140
        rs71262674
        rs71262673

You can combine any conditions you want using boolean operators.

### Command line options
```
Usage: java -jar SnpSift.jar filter [options] 'expression' [input.vcf]
Options:
	-a|--addFilter   : Add a string to FILTER VCF field if 'expression' is true. Default: '' (none)
	-e|--exprFile    : Read expression from a file
	-f|--file        : VCF input file. Default: STDIN
	-i|--filterId    : ID for this filter (##FILTER tag in header and FILTER VCF field). Default: 'SnpSift'
	-n|--inverse     : Inverse. Show lines that do not match filter expression
	-p|--pass        : Use 'PASS' field instead of filtering out VCF entries
	-r|--rmFilter    : Remove a string from FILTER VCF field if 'expression' is true (and 'str' is in the field). Default: '' (none)
	-s|--set         : Create a SET using 'file'
	--errMissing     : Error is a field is missing. Default: false
	--format         : SnpEff format version: {2, 3}. Default: Auto
	--galaxy         : Used from Galaxy (expressions have been sanitized).
```

### Variables

All VCF fields can be used as variables names, as long as they are declared in the VCF header OR they are "standard" VCF fields (as defined by the VCF 4.1 specification).

* **Fields** names: "CHROM, POS, ID, REF, ALT, QUAL or FILTER". Examples:

    * Any variant in chromosome 1:

            "( CHROM = 'chr1' )"

    * Variants between two positions:

            "( POS > 123456 ) & ( POS < 654321 )"

    * Has an ID and it matches the regulat expression 'rs':

            "(exists ID) & ( ID =~ 'rs' )"

    * The reference is 'A':

            "( REF = 'A' )"

    * The alternative is 'T':

            "( ALT = 'T' )"

    * Quality over 30:

            "( QUAL > 30 )"

    * Filter value is either 'PASS' or it is missing:

            "( na FILTER ) | (FILTER = 'PASS')"

* **INFO field** names in the INFO field. E.g. if the info field has "DP=48;AF1=0;..." you can use something like:

        "( DP > 10 ) & ( AF1 = 0 )"

### Multiple valued fields and variables

When variables have multiple values, you can access individual values as if it was an array.

* **Multiple value** info fields (comma separated) can be accessed using an index. E.g. If the INFO field has "CI95=0.04167,0.5417" you can use an expression such as:

        "( CI95[0] > 0.1 ) & (CI95[1] <= 0.3)"

* **Multiple indexes** You may test multiple indexed fields using 'ANY' or 'ALL' as index. In the examples we assume the INFO field has "CI95=0.04167,0.5417"

    **ANY** or **\***: If you use 'ANY' as index, the expression will be true if any field satisfies the expression.

    So, for instance, the following expressions:

        "( CI95[ANY] > 0.1 )"
    or:

        "( CI95[*] > 0.1 )"

    are equivalent to (in this case, there are only two values in the array):

        "( CI95[0] > 0.1 ) | ( CI95[1] > 0.1 )"

    **ALL** or **?**: If you use 'ALL' as index, the expression will be true if all field satisfy the expression.

    So, for instance, the following expressions:

        "( CI95[ALL] > 0.1 )"
        "( CI95[?] > 0.1 )"

    are equivalent to (in this case, there are only two values in the array):

        "( CI95[0] > 0.1 ) & ( CI95[1] > 0.1 )"


### Genotype fields

Vcf genotype fields can be accessed individually using array notation.

* **Genotype fields** are accessed using an index (sample number) followed by a variable name. E.g. If the genotypes are `GT:PL:GQ    1/1:255,66,0:63    0/1:245,0,255:99`
    You can write something like:

        "( GEN[0].GQ > 60 ) & ( GEN[1].GQ > 90 )"

    You may use an asterisk to represent 'ANY' field:

        "( GEN[*].GQ > 60 )"

* **Genotype multiple fields** are accessed using an index (sample number) followed by a variable name and then another index. E.g. If the genotypes are `GT:PL:GQ    1/1:255,66,0:63    0/1:245,0,255:99`
    You can write something like:

        "( GEN[0].PL[2] = 0 )"
    You may use an asterisk to represent 'ANY' field:

        "( GEN[0].PL[*] = 0 )"
    ...or even:

        "( GEN[*].PL[*] = 0 )"

!!! info
    You can create an expression using sample names instead of genotype numbers.
    E.g.

        $ java -jar SnpSift.jar filter "( GEN[HG00096].DS > 0.2 ) & ( GEN[HG00097].DS > 0.5 )" examples/1kg.head_chr1.vcf.gz


### Sets

Sets are defined by the '-s' (or '--set') command line option. Each file must have one string per line.
They are named based on the order used in the command line (e.g. the first one is `SET[0]`, the second one is `SET[1]`, etc.)

Example: You can write something like (assuming your command line was "-s set1.txt -s set2.txt -s set3.txt"):

    "( ID in SET[2] )"

### SnpEff 'ANN' fields

SnpEff annotations are parsed, so you can access individual sub-fields:

Effect fields (from SnpEff) are accessed using an index (effect number) followed by a sub-field name.

Available `ANN` sub-fields are (for details, take a look at the [specification](../adds/VCFannotationformat_v1.0.pdf)):

* `ALLELE` (alias GENOTYPE)
* `EFFECT` (alias ANNOTATION): Effect in Sequence ontology terms (e.g. 'missense_variant', 'synonymous_variant', 'stop_gained', etc.)
* `IMPACT`: { HIGH, MODERATE, LOW, MODIFIER }
* `GENE`: Gene name (e.g. 'PSD3')
* `GENEID`: Gene ID
* `FEATURE`
* `FEATUREID` (alias TRID: Transcript ID)
* `BIOTYPE`: Biotype, as described by the annotations (e.g. 'protein_coding')
* `RANK`: Exon or Intron rank (i.e. exon number in a transcript)
* `HGVS_C` (alias HGVS_DNA, CODON): Variant in HGVS (DNA) notation
* `HGVS_P` (alias HGVS, HGVS_PROT, AA): Variant in HGVS (protein) notation
* `CDNA_POS` (alias POS_CDNA)
* `CDNA_LEN` (alias LEN_CDNA)
* `CDS_POS` (alias POS_CDS)
* `CDS_LEN` (alias LEN_CDS)
* `AA_POS` (alias POS_AA)
* `AA_LEN` (alias LEN_AA)
* `DISTANCE`
* `ERRORS` (alias WARNING, INFOS)

For example, you may want only the lines where the **first** annotation has `missense_variant` variant:

**Important** According to the specification, there can be more than one EFFECT separated by &amp; (e.g. 'missense_variant&amp;splice_region_variant', thus using `has` operator is better than using equality operator (`=`).
For instance `'missense_variant&splice_region_variant' = 'missense_variant'` is false, whereas  `'missense_variant&splice_region_variant' has 'missense_variant'` is true.

```
$ java -jar SnpSift.jar filter "ANN[0].EFFECT has 'missense_variant'" examples/test.chr22.ann.vcf > test.chr22.ann.filter_missense_first.vcf

# Output example (edited for readability)
$ cat test.chr22.ann.filter_missense_first.vcf
22    17072035    .    C    T    .    .    ANN=T|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.1406G>A|p.Gly469Glu|1666/2034|1406/1674|469/557||,T|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397G>A|||||3944|
22    17072258    .    C    A    .    .    ANN=A|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.1183G>T|p.Gly395Cys|1443/2034|1183/1674|395/557||,A|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397G>T|||||3721|
22    17072674    .    G    A    .    .    ANN=A|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.767C>T|p.Pro256Leu|1027/2034|767/1674|256/557||,A|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397C>T|||||3305|
22    17072747    .    T    C    .    .    ANN=C|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.694A>G|p.Met232Val|954/2034|694/1674|232/557||,C|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397A>G|||||3232|
22    17073043    .    C    T    .    .    ANN=T|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.398G>A|p.Arg133Gln|658/2034|398/1674|133/557||,T|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397G>A|||||2936|
22    17073119    .    C    T    .    .    ANN=T|missense_variant|MODERATE|CCT8L2|ENSG00000198445|transcript|ENST00000359963|protein_coding|1/1|c.322G>A|p.Val108Met|582/2034|322/1674|108/557||,T|downstream_gene_variant|MODIFIER|FABP5P11|ENSG00000240122|transcript|ENST00000430910|processed_pseudogene||n.*397G>A|||||2860|
```

...but this probably doesn't make much sense. What you may really want are lines where **ANY** effect to be `missense_variant`:
```
$ java -jar SnpSift.jar filter "ANN[*].EFFECT has 'missense_variant'" examples/test.chr22.ann.vcf > test.chr22.ann.filter_missense_any.vcf

# Output example (edited for readability)
$ cat test.chr22.ann.filter_missense_any.vcf
...
22    24891462    .    G    A    .    .    ANN=A|stop_gained|HIGH|UPB1|ENSG00000100024|transcript|ENST00000413389|protein_coding|2/10|c.59G>A|p.Trp20*|1652/3418|59/951|20/316||
                                              ,A|missense_variant|MODERATE|UPB1|ENSG00000100024|transcript|ENST00000326010|protein_coding|1/10|c.91G>A|p.Gly31Ser|435/2290|91/1155|31/384||
                                              ,A|missense_variant|MODERATE|UPB1|ENSG00000100024|transcript|ENST00000382760|protein_coding|1/4|c.91G>A|p.Gly31Ser|253/1928|91/561|31/186||

22    24896158    .    A    T    .    .    ANN=T|missense_variant|MODERATE|UPB1|ENSG00000100024|transcript|ENST00000326010|protein_coding|2/10|c.188A>T|p.Glu63Val|532/2290|188/1155|63/384||
                                              ,T|missense_variant|MODERATE|UPB1|ENSG00000100024|transcript|ENST00000382760|protein_coding|2/4|c.188A>T|p.Glu63Val|350/1928|188/561|63/186||
```

May be you want only the ones that affect **gene 'TRMT2A'**:
```
$ java -jar SnpSift.jar filter "(ANN[*].EFFECT has 'missense_variant') && (ANN[*].GENE = 'TRMT2A')" examples/test.chr22.ann.vcf > test.chr22.ann.filter_missense_any_TRMT2A.vcf

$ cat test.chr22.ann.filter_missense_any_TRMT2A.vcf
22    20103915    .    C    T    .    .    ANN=T|stop_gained|HIGH|RANBP1|ENSG00000099901|transcript|ENST00000432879|protein_coding|1/3|c.208C>T|p.Arg70*|455/744|208/497|70/164||WARNING_TRANSCRIPT_INCOMPLETE
                                              ,T|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000439169|protein_coding|2/12|c.245G>A|p.Arg82His|561/2473|245/1932|82/643||
                                              ,T|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000252136|protein_coding|2/12|c.245G>A|p.Arg82His|634/2964|245/1878|82/625||
                                              ,T|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000403707|protein_coding|3/13|c.245G>A|p.Arg82His|607/2928|245/1878|82/625||
                                              ,T|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000404751|protein_coding|2/12|c.245G>A|p.Arg82His|584/2498|245/1689|82/562||
                                              ,T|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000445045|protein_coding|2/2|c.209G>A|p.Arg70His|432/582|209/359|70/118||WARNING_TRANSCRIPT_INCOMPLETE
                                              ,...

22    20103925    .    T    C    .    .    ANN=C|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000439169|protein_coding|2/12|c.235A>G|p.Asn79Asp|551/2473|235/1932|79/643||
                                              ,C|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000252136|protein_coding|2/12|c.235A>G|p.Asn79Asp|624/2964|235/1878|79/625||
                                              ,C|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000403707|protein_coding|3/13|c.235A>G|p.Asn79Asp|597/2928|235/1878|79/625||
                                              ,C|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000404751|protein_coding|2/12|c.235A>G|p.Asn79Asp|574/2498|235/1689|79/562||
                                              ,C|missense_variant|MODERATE|RANBP1|ENSG00000099901|transcript|ENST00000432879|protein_coding|1/3|c.218T>C|p.Phe73Ser|465/744|218/497|73/164||WARNING_TRANSCRIPT_INCOMPLETE
                                              ,C|missense_variant|MODERATE|TRMT2A|ENSG00000099899|transcript|ENST00000445045|protein_coding|2/2|c.199A>G|p.Asn67Asp|422/582|199/359|67/118||WARNING_TRANSCRIPT_INCOMPLETE
                                              ,C|splice_region_variant&intron_variant|LOW|RANBP1|ENSG00000099901|transcript|ENST00000430524|protein_coding|1/5|c.-374+7T>C||||||
                                              ,...
```

### SnpEff 'EFF' fields

!!! warning
    This section documents older SnpEff/SnpSift which used 'EFF' INFO field (as opposed to 'ANN' field) or files annotated using SnpEff's `-classic` or `-formatEff` command line options.

SnpEff annotations are parsed, so you can access individual sub-fields:

Effect fields (from SnpEff) are accessed using an index (effect number) followed by a sub-field name.

Available `EFF` sub-fields are:

* `EFFECT`: Effect (e.g. SYNONYMOUS_CODING, NON_SYNONYMOUS_CODING, FRAME_SHIFT, etc.)
* `IMPACT`: { HIGH, MODERATE, LOW, MODIFIER }
* `FUNCLASS`: { NONE, SILENT, MISSENSE, NONSENSE }
* `CODON`: Codon change (e.g. 'ggT/ggG')
* `AA`: Amino acid change (e.g. 'G156')
* `GENE`: Gene name (e.g. 'PSD3')
* `BIOTYPE`: Gene biotype, as described by the annotations (e.g. 'protein_coding')
* `CODING`: Gene is { CODING, NON_CODING }
* `TRID`: Transcript ID
* `RANK`: Exon or Intron rank (i.e. exon number in a transcript)

For example, you may want only the lines where the first effect is a NON_SYNONYMOUS variants:

    "( EFF[0].EFFECT = 'NON_SYNONYMOUS_CODING' )"

...but this probably doesn't make much sense. What you may really want are lines where ANY effect is NON_SYNONYMOUS:

    "( EFF[*].EFFECT = 'NON_SYNONYMOUS_CODING' )"

May be you want only the ones that affect gene 'TCF7L2':

    "( EFF[*].EFFECT = 'NON_SYNONYMOUS_CODING' ) &  ( EFF[*].GENE = 'TCF7L2' )"

### SnpEff 'LOF' and 'NMD' fields

Similarly `LOF` and `NMD` sub-fields are available:

* `LOF.GENE` and NMD.GENE
* `LOF.GENEID` and NMD.GENEID
* `LOF.NUMTR` and NMD.NUMTR
* `LOF.PERC` and NMD.PERC

For instance, if we want to obtain genes having a Loss of Function effect in more than 90% of the transcripts, you can do this:

    $cat test.snpeff.vcf | java -Xmx1G -jar SnpSift.jar filter "(exists LOF[*].PERC) & (LOF[*].PERC > 0.9)"


!!! warning
    We assume that 'test.snpeff.vcf' was annotated with SnpEff using '-lof' command line option.


### Available operands and functions

The following operators and functions are interpreted by `SnpSift filter`:

Operand | Description   | Data type            | Example
------- | ------------- | -------------------- | --------------
 =      | Equality test                     | FLOAT, INT or STRING | (REF **=** 'A')
 &gt;   | Greater than                      | FLOAT or INT         | (DP **&gt;** 20)
 &ge;   | Greater or equal than             | FLOAT or INT | (DP **&ge;** 20)
 &lt;   | Less than                         | FLOAT or INT | (DP **&lt;** 20)
 &le;   | Less or equal than                | FLOAT or INT | (DP **&le;** 20)
 =~     | Match regular expression          | STRING  | (REL **=~** 'AC')
 !~     | Does not match regular expression | STRING  | (REL **!~** 'AC')
 &amp;  | AND operator                      | Boolean | (DP &gt; 20) **&amp;** (REF = 'A')
 \|     | OR operator                       | Boolean | (DP &gt; 20) **\|** (REF = 'A')
 !      | NOT operator                      | Boolean | **!** (DP &gt; 20)
 exists | The variable exists (not missing) | Any     | (**exists** INDEL)
 has    | The right hand side expression is equalt to any of the items in a list consisting of separating the left hand side expression using delimiters: `&`, `+`, `;`, `,`, `:`, `(', ')` ,`[', ']`. <br> Example: If the expression is: ANN\[\*].EFFECT **has** 'missense_variant'.<br> If left hand side (ANN\[\*].EFFECT) has value 'missense_variant&splice_region_variant', then it is transformed to a list: `['missense_variant', 'splice_region_variant']`<br> Since the right hand side ('missense_variant') is in the list, the expression evaluates to 'true' | Any | (ANN\[\*].EFFECT **has** 'missense_variant')

Function   | Description | Data type | Example
---------- | ----------- | --------- | --------
`countHom()` | Count number of homozygous genotypes   | No arguments | (**countHom()** &gt; 0)
`countHet()` | Count number of heterozygous genotypes | No arguments | (**countHet()** &gt; 2)
`countVariant()` | Count number of genotypes that are variants (i.e. not reference 0/0) | No arguments | (**countVariant()** &gt; 5)
`countRef()`     | Count number of genotypes that are NOT variants (i.e. reference 0/0) | No arguments | (**countRef()** &lt; 1)

Genotype <br> Function | Description | Data type | Example
---------------------- | ----------- | --------- | --------
`isHom`     | Is homozygous genotype?   | Genotype | **isHom( GEN\[0] )**
`isHet`     | Is heterozygous genotype? | Genotype | **isHet( GEN\[0] )**
`isVariant` | Is genotype a variant? (i.e. not reference 0/0) | Genotype | **isVariant( GEN\[0] )**
`isRef`     | Is genotype a reference? (i.e. 0/0)             | Genotype | **isRef( GEN\[0] )**

### Using sample names instead of sample numbers

As of version 4.1A, SnpSift allows to use sample names instead of sample numbers.
This allows to create more readable expressions.

Example:

```
$ cat cancer.vcf | java -jar SnpSift.jar filter "GEN[Somatic].GT = '2/1'"
#CHROM  POS    ID   REF  ALT    QUAL  FILTER    INFO    FORMAT    Germline    Somatic
1       69091  .    A    C,G    .     PASS      AC=1    GT        1/0         2/1
```

Note that we used `GEN[Somatic]` instead of `GEN[1]`.
