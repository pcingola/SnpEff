# SnpSift

**SnpSift** is a toolbox that allows you to filter and manipulate annotated files.

Once your genomic variants have been annotated, you need to filter them out in order to find the "interesting / relevant variants".
Given the large data files, this is not a trivial task (e.g. you cannot load all the variants into XLS spreadsheet).
SnpSift helps to perform this VCF file manipulation and filtering required at this stage in data processing pipelines.

### Download and install

SnpSift is part of SnpEff main distribution, so please click on [here](../download.md) and follow the instructions on how to download and install SnpEff.

### SnpSift utilities

SnpSift is a collection of tools to manipulate VCF (variant call format) files.

Some examples of what you can do:

Operation              | Meaning
---------------------- | --------
**[Annotate](annotate.md)**           | Add 'ID' and INFO fields from another VCF database (e.g. dbSnp). Assumes entries are sorted.
**[Annotate (mem)](annotate_mem.md)** | Annotate from a database created from a VCF file, loaded into memory.
**[CaseControl](casecontrol.md)**     | Compare how many variants are in 'case' and in 'control' groups; calculate p-values (Fisher exact test).
**[Concordance](concordance.md)**     | Concordance metrics between two VCF files.
**[DbNSFP](dbnsfp.md)**              | Annotate using dbNSFP, an integrated database of functional predictions from multiple algorithms (SIFT, Polyphen2, LRT, MutationTaster, PhyloP, GERP++, etc.).
**[Extract fields](extractfields.md)** | Extract fields from a VCF file into tab-separated format.
**[Filter](filter.md)**               | Filter using arbitrary expressions, e.g. `"(QUAL > 30) | (exists INDEL) | ( countHet() < 2 )"`.
**[GeneSets](genesets.md)**           | Annotate using MSigDb gene sets (GO, KEGG, Reactome, BioCarta, etc.).
**[GT](gt.md)**                       | Compress genotype fields to reduce VCF file size in large sequencing projects.
**[GWAS Catalog](gwascatalog.md)**    | Annotate using GWAS Catalog.
**[Intersect](intersect.md)**         | Intersect intervals from multiple files to find consensus regions (e.g. ChIP-Seq peaks).
**[Intervals](intervals.md)**         | Filter variants that intersect with intervals defined in BED files.
**[Intervals Index](intervalsindex.md)** | Filter variants that intersect with intervals. Uses file indexing for fast random access; intended for huge VCF files and a small number of intervals.
**[Join](join.md)**                   | Join files by genomic region (intersecting or closest).
**[PhastCons](phastcons.md)**         | Annotate using conservation scores (phastCons).
**[Private](private.md)**             | Annotate if a variant is private to a family or group.
**[RmInfo](rminfo.md)**               | Remove INFO fields from a VCF file.
**[RmRefGen](rmrefgen.md)**           | Remove reference genotypes (replace '0/0' genotypes by '.').
**[Split](split.md)**                 | Split a VCF file by chromosome.
**[TsTv](tstv.md)**                   | Calculate transition to transversion ratio.
**[Variant type](varianttype.md)**    | Annotate variant type (SNP, MNP, INS, DEL, or MIXED). Also adds HOM/HET if there is only one sample.
**[VcfCheck](vcfcheck.md)**           | Check that a VCF file is well formed.
**[Vcf2Tped](vcf2ped.md)**           | Convert VCF to TPED format.

### Citing SnpSift

In order to cite SnpSift, please use the following [example](../index.md#citing-snpsift).

### Source code

The project is hosted at [GitHub](https://github.com/pcingola/SnpEff).
