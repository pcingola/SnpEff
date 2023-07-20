# SnpSift GT

Compress genotype calls, reducing the overall size of the VCF file.

This is intended for compressing very large VCF in very large sequencing projects (e.g. thousands of samples).

!!! info
    For instance, we've reduced 1Tb (1,000 Gb) VCF file to roughly 1Gb in a project that has over 10,000 samples.

The underlying idea is quite simple.
In large re-sequencing projects most of the variants are singletons.
This means that most variants are present in only one of the samples.
For those variants, you have thousands of samples that are homozygous reference (i.e. genotype entry is "0/0") and one that is a variant (e.g. '0/1' or '1/1').

A trivial way to compress these VCF entries is just to state which sample has non-reference information.
Intuitively, this is similar to the way used to represent sparse matrices (only store non-zero elements).

`SnpSift gt` creates three INFO fields. These three files are composed of comma separated indexes of samples having:

* HE: Indicated heterozygous variants (i.e. '0/1').
* HO: Indicated homozygous variants (i.e. '1/1').
* NA: Indicated missing genotype data (i.e. './.').

You can use `-u` command line option to uncompress.

E.g.:
```
$ cat test.vcf
#CHROM  POS     ID  REF  ALT  QUAL  FILTER  INFO  FORMAT  Sample_1  Sample_2  Sample_3  Sample_4  Sample_5  Sample_6  Sample_7  Sample_8  Sample_9  Sample_10  Sample_11  Sample_12  Sample_13  Sample_14  Sample_15
1       861276  .   A    G    .     PASS    AC=1  GT      0/0       1/1       0/0       0/0       0/0       0/0       0/0       0/0       0/0       0/0        0/0        0/0        0/0        0/0        0/0

#---
# Compress genotypes
#---
$ java -jar SnpSift.jar gt test.vcf | tee test.gt.vcf
##INFO=<ID=HO,Number=.,Type=Integer,Description="List of sample indexes having homozygous ALT genotypes">
##INFO=<ID=HE,Number=.,Type=Integer,Description="List of sample indexes having heterozygous ALT genotypes">
##INFO=<ID=NA,Number=.,Type=Integer,Description="List of sample indexes having missing genotypes">
#CHROM  POS     ID  REF  ALT  QUAL  FILTER  INFO        FORMAT  Sample_1  Sample_2  Sample_3  Sample_4  Sample_5  Sample_6  Sample_7  Sample_8  Sample_9  Sample_10  Sample_11  Sample_12  Sample_13  Sample_14  Sample_15
1       861276  .   A    G    .     PASS    AC=1;HO=1

#---
# Uncompress genotypes (command line option '-u')
#---
$ java -jar SnpSift.jar gt -u test.gt.vcf
#CHROM  POS     ID  REF  ALT  QUAL  FILTER  INFO  FORMAT  Sample_1  Sample_2  Sample_3  Sample_4  Sample_5  Sample_6  Sample_7  Sample_8  Sample_9  Sample_10  Sample_11  Sample_12  Sample_13  Sample_14  Sample_15
1       861276  .   A    G    .     PASS    AC=1  GT      0/0       1/1       0/0       0/0       0/0       0/0       0/0       0/0       0/0       0/0        0/0        0/0        0/0        0/0        0/0
```

!!! warning
    This is lossy compression. Note that only GT informations is compressed, all other information in genotype field is lost.
