# Troubleshooting

Some common problems

### Chromosome not found

!!! warning
    This is by far the most common problem.
    It means that the input VCF file has chromosome names that do not match SnpEff's database and don't match reference genome either, since SnpEff's database are created using
    reference genome chromosome names.

The solution is simple: fix your VCF file to use standard chromosome names.
You can see which chromosome names are used by SnpEff simply by using the `-v` (verbose) command line option.
This shows all chromosome names and their respective lengths. Notice the last line ("Chromosomes names \[sizes\]"):
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
    Usage of hg19 genome is deprecated and discouraged, you should use GRChXX.YY instead (e.g. the latest version at the time of writing is GRCh37.70)

Reference sequence and annotations are made for an organism version and sub-version.
For examples human genome, version 37, sub-version 70 would be called (GRCh37.70).

UCSC doesn't specify sub-version.
They just say hg19.
This annoying sub-version problem appeared often and, having reproducibility of results in mind, I dropped UCSC annotations in favor of ENSEMBL ones (they have clear versioning).

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

This means that you'll get different results for this transcript using sub-version 63 or 64. I assume that latest versions are improved, so I always encourage to upgrade.

Sometimes it might even be the case that latest released database and the one shown on the web interface may be out of sync.

### SnpEff reports a SYNONYMOUS and a NON_SYNONYMOUS effect on the same gene

This is not a bug.
It is not uncommon for a gene to have more than one transcript (e.g. in human most genes have multiple transcripts).
A variant (e.g. a SNP) might affect different transcripts in different ways, as a result of different reading frames.

For instance:
```
chr5 137622242 . C T . . EFF=NON_SYNONYMOUS_CODING(MODERATE|MISSENSE|Gaa/Aaa|E/K|CDC25C|protein_coding|CODING|ENST00000514017|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000323760|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000348983|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000356505|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000357274|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000415130|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000513970|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000514555|exon_5_137622186_137622319),
                             SYNONYMOUS_CODING(LOW|SILENT|caG/caA|Q|CDC25C|protein_coding|CODING|ENST00000534892|exon_5_137622186_137622319)
```
in this example (it was divided into multiple lines for legibility), the first transcript ENST0000051401 has a NON_SYNONYMOUS effect, but all other transcripts have a SYNONYMOUS effect.

### Counting total number of effects of a given type

Some people try to count the number of effects in a file by doing (assuming we want to count how many MODIFIER effects we have):

    grep -o MODIFIER output.ann.vcf | wc -l

This is incorrect because a VCF line can have multiple effects (e.g. when there are multiple transcripts in a gene).
A proper way to count effects would be:

```
cat output.ann.vcf \
	| cut -f 8 \
	| tr ";" "\n" \
	| grep ^EFF= \
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
`grep ^EFF=`            | Only keep 'EFF' fields
`cut -f 2 -d =`         | Keep only the effect data (drop the 'EFF=' part)
`tr "," "\n"`           | Expand effects to multiple lines
`grep MODIFIER | wc -l` | Count the ones you want (in this example 'MODIFIER')
