# Troubleshooting

Some common problems and how to solve them. See also the [FAQ](faq.md) for a comprehensive list of errors and warnings.

### Chromosome not found

!!! warning
    This is by far the most common problem.
    It means that the input VCF file has chromosome names that do not match SnpEff's database and don't match the reference genome either, since SnpEff's databases are created using
    reference genome chromosome names.

SnpEff automatically normalizes chromosome names by stripping common prefixes such as `chr`, `chromo`, and `chromosome` (with separators `:`, `_`, `-`). For example, `chr1` and `1` will match automatically. The `ERROR_CHROMOSOME_NOT_FOUND` error only occurs when normalization still fails to find a match, which usually means a completely different naming convention or the wrong reference genome.

If you get this error, the solution is to fix your VCF file to use chromosome names consistent with the database. You can see which chromosome names are used by SnpEff by using the `-v` (verbose) command line option. This shows all chromosome names and their respective lengths. Notice the last line ("Chromosomes names \[sizes\]"):

```
$ java -Xmx8g -jar snpEff.jar -v GRCh37.75 examples/test.chr22.vcf > test.chr22.ann.vcf
00:00:00.000	Reading configuration file 'snpEff.config'. Genome: 'GRCh37.75'
...
# Number of chromosomes      : 297
# Chromosomes names [sizes]  :
#		'HG1292_PATCH' [250051446]
#		'HG1287_PATCH' [249964560]
#		'HG1473_PATCH' [249272860]
#		'HG1471_PATCH' [249269426]
#		'HSCHR1_1_CTG31' [249267852]
#		'HSCHR1_2_CTG31' [249266025]
#		'HSCHR1_3_CTG31' [249262108]
#		'HG999_2_PATCH' [249259300]
#		'HG989_PATCH' [249257867]
#		'HG999_1_PATCH' [249257505]
#		'HG1472_PATCH' [249251918]
#		'1' [249250621]
#		'2' [243199373]
#		'3' [198022430]
#		'4' [191154276]
#		'5' [180915260]
#		'6' [171115067]
#		'7' [159138663]
#		'X' [155270560]
...
```

### Apparent inconsistencies when using UCSC genome browser

!!! warning
    ENSEMBL versioned databases (e.g. `GRCh37.75`) are preferred over UCSC ones (e.g. `hg19`) because ENSEMBL uses clear sub-versioning, which is important for reproducibility.

Reference sequence and annotations are made for an organism version and sub-version.
For example, human genome version 37, sub-version 75 would be called `GRCh37.75`.

UCSC doesn't specify sub-version. They just say `hg19`. This sub-version problem makes it harder to reproduce results, which is why ENSEMBL annotations with clear versioning are preferred. For the human genome GRCh37, the available ENSEMBL databases include `GRCh37.75` and `GRCh37.87`.

###  SnpEff reporting an effect that doesn't match ENSEMBL's web page

Please remember that databases are updated often (e.g. by ENSEMBL), so if you are using an old database, you might get different effects.

For example, this transcript ENST00000487462 changed from protein_coding in GRCh37.63
```
1       protein_coding  exon    1655388 1655458 .       -       .        gene_id "ENSG00000008128"; transcript_id "ENST00000487462"; exon_number "1"; gene_name "CDK11A"; transcript_name "CDK11A-013";
1       protein_coding  exon    1653905 1654270 .       -       .        gene_id "ENSG00000008128"; transcript_id "ENST00000487462"; exon_number "2"; gene_name "CDK11A"; transcript_name "CDK11A-013";
```
...to processed_transcript in GRCh37.64:
```
1       processed_transcript    exon    1655388 1655458 .       -       .        gene_id "ENSG00000008128"; transcript_id "ENST00000487462"; exon_number "1"; gene_name "CDK11A"; gene_biotype "protein_coding"; transcript_name "CDK11A-013";
1       processed_transcript    exon    1653905 1654270 .       -       .        gene_id "ENSG00000008128"; transcript_id "ENST00000487462"; exon_number "2"; gene_name "CDK11A"; gene_biotype "protein_coding"; transcript_name "CDK11A-013";
```

This means that you'll get different results for this transcript using sub-version 63 or 64. Latest versions are generally improved, so always try to use the latest available database.

Sometimes it might even be the case that the latest released database and the one shown on the web interface may be out of sync.

### SnpEff reports a synonymous and a missense effect on the same gene

This is not a bug.
It is not uncommon for a gene to have more than one transcript (e.g. in human most genes have multiple transcripts).
A variant (e.g. a SNP) might affect different transcripts in different ways, as a result of different reading frames.

For instance:
```
chr5	137622242	.	C	T	.	.	ANN=T|missense_variant|MODERATE|CDC25C|ENSG00000158402|transcript|ENST00000514017|protein_coding|5/14|c.163G>A|p.Glu55Lys|...,
                                        T|synonymous_variant|LOW|CDC25C|ENSG00000158402|transcript|ENST00000323760|protein_coding|5/14|c.489G>A|p.Gln163Gln|...,
                                        T|synonymous_variant|LOW|CDC25C|ENSG00000158402|transcript|ENST00000348983|protein_coding|5/14|c.489G>A|p.Gln163Gln|...,
                                        T|synonymous_variant|LOW|CDC25C|ENSG00000158402|transcript|ENST00000356505|protein_coding|5/14|c.489G>A|p.Gln163Gln|...
```
In this example (divided into multiple lines for legibility), the first transcript ENST00000514017 has a `missense_variant` effect, but the other transcripts have a `synonymous_variant` effect.

### Counting total number of effects of a given type

Some people try to count the number of effects in a file by doing (assuming we want to count how many MODIFIER effects we have):

    grep -o MODIFIER output.ann.vcf | wc -l

This is incorrect because a VCF line can have multiple effects (e.g. when there are multiple transcripts in a gene).
A proper way to count effects would be:

```
cat output.ann.vcf \
	| cut -f 8 \
	| tr ";" "\n" \
	| grep ^ANN= \
	| cut -f 2 -d = \
	| tr "," "\n" \
	| grep MODIFIER \
	| wc -l
```

Brief explanation:

Command                 | Meaning
----------------------- | ----------
`cut -f 8`              | Extract INFO fields
`tr ";" "\n"`           | Expand each field into one line
`grep ^ANN=`            | Only keep 'ANN' fields
`cut -f 2 -d =`         | Keep only the annotation data (drop the 'ANN=' part)
`tr "," "\n"`           | Expand annotations to multiple lines
`grep MODIFIER | wc -l` | Count the ones you want (in this example 'MODIFIER')

### OutOfMemory errors

If you get a `java.lang.OutOfMemoryError` exception, you need to increase the memory available to the Java Virtual Machine using the `-Xmx` option. See the [Java memory options](running.md#java-memory-options) section for details. For human genomes, you typically need at least 8 GB: `-Xmx8g`.

### Other common errors and warnings

For a comprehensive list of all errors and warnings that SnpEff can produce (such as `WARNING_REF_DOES_NOT_MATCH_GENOME`, `ERROR_OUT_OF_CHROMOSOME_RANGE`, `WARNING_TRANSCRIPT_INCOMPLETE`, etc.), see the [Errors and Warnings section in the FAQ](faq.md#errors-and-warnings).
