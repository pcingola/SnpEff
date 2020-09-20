#1. Introduction

SnpSift is a toolbox that allows you to filter and manipulate annotated files.

For older version of this page, see: [Manual page for SnpSift version 4.1](https://pcingola.github.io/SnpEff/SnpSift.version_4_1.html)

Once your genomic variants have been annotated, you need to filter them out in order to find the "interesting / relevant variants".
Given the large data files, this is not a trivial task (e.g. you cannot load all the variants into XLS spreadsheet).
SnpSift helps to perform this VCF file manipulation and filtering required at this stage in data processing pipelines.

### Download and install

SnpSift is part of SnpEff main distribution, so please click on [here](download.md) and follow the instructions on how to download and install SnpEff.

### SnpSift utilities

SnpSift is a collection of tools to manipulate [VCF](http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41) (variant call format) files.

Some examples of what you can do:

Operation              | Meaning
---------------------- | --------
**Filter**             | You can filter using arbitrary expressions, for instance `"(QUAL > 30) | (exists INDEL) | ( countHet() < 2 )"`. The actual expressions can be quite complex, so it allows for a lot of flexibility.
**Annotate**           | You can add 'ID' and INFO fields from another "VCF database" (e.g.  typically dbSnp database in VCF format).
**CaseControl**        | You can compare how many variants are in 'case' and in 'control' groups. Also calculates p-values (Fisher exact test).
**Intervals**          | Filter variants that intersect with intervals.
**Intervals (intidx)** | Filter variants that intersect with intervals. Index the VCF file using memory mapped I/O to speed up the search. This is intended for huge VCF files and a small number of intervals to retrieve.
**Join**               | Join by generic genomic regions (intersecting or closest).
**RmRefGen**           | Remove reference genotype (i.e. replace '0/0' genotypes by '.')
**TsTv**               | Calculate transition to transversion ratio.
**Extract fields**     | Extract fields from a VCF file to a TXT (tab separated) format.
**Variant type**       | Adds SNP/MNP/INS/DEL to info field. It also adds "HOM/HET" if there is only one sample.
**GWAS Catalog**       | Annotate using GWAS Catalog.
**DbNSFP**             | Annotate using dbNSFP: The dbNSFP is an integrated database of functional predictions from multiple algorithms (SIFT, Polyphen2, LRT and MutationTaster, PhyloP and GERP++, etc.)
**SplitChr**           | Split a VCF file by chromosome

### Citing SnpSift

In order to cite SnpSift, please use the following [example](citing.md#citing-snpsift).

### Source code

The project is hosted at SourceForge.

Here is the SVN command to check out the development version of the code:

    svn co https://snpeff.svn.sourceforge.net/svnroot/snpeff/SnpSift/trunk
