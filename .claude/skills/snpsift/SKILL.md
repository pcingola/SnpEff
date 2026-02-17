---
name: snpsift
description: Run SnpSift, a toolbox for filtering and manipulating annotated VCF files. Use when the user wants to filter variants, annotate with databases (dbNSFP, dbSnp, GWAS), extract fields, or manipulate VCF files.
allowed-tools: Bash, Read, Grep, Glob
---

# SnpSift

SnpSift is a toolbox for filtering and manipulating annotated VCF files. Once genomic variants have been annotated, SnpSift helps filter and process them to find relevant variants.

Full documentation: https://pcingola.github.io/SnpEff/

## Running

Use the wrapper script at `.claude/skills/snpsift/snpsift.sh`. It handles JVM memory defaults and argument passthrough. The JAR file is expected at `$HOME/snpEff/SnpSift.jar`.

**IMPORTANT: SnpSift output is typically very large (thousands to millions of lines). ALWAYS redirect output to a file. NEVER let output print to stdout, as it will fill the context window and make the conversation unusable.**

```bash
# Correct: redirect to file
.claude/skills/snpsift/snpsift.sh <command> [options] [arguments] > output.vcf 2> snpsift.log

# WRONG: never do this
.claude/skills/snpsift/snpsift.sh <command> [options] [arguments]
```

## Commands

Command | Description | Docs
------- | ----------- | ----
`annotate` | Add ID and INFO fields from a VCF database (e.g. dbSnp) | [annotate](docs/annotate.md)
`annotateMem` | Annotate from a VCF database loaded into memory | [annotate_mem](docs/annotate_mem.md)
`caseControl` | Case vs control variant comparison with p-values | [casecontrol](docs/casecontrol.md)
`concordance` | Concordance metrics between two VCF files | [concordance](docs/concordance.md)
`dbnsfp` | Annotate using dbNSFP (SIFT, Polyphen2, etc.) | [dbnsfp](docs/dbnsfp.md)
`extractFields` | Extract VCF fields to tab-separated format | [extractfields](docs/extractfields.md)
`filter` | Filter using arbitrary expressions | [filter](docs/filter.md)
`geneSets` | Annotate using MSigDb gene sets (GO, KEGG, etc.) | [genesets](docs/genesets.md)
`gt` | Compress genotype fields | [gt](docs/gt.md)
`gwasCat` | Annotate using GWAS Catalog | [gwascatalog](docs/gwascatalog.md)
`intersect` | Intersect intervals from multiple files | [intersect](docs/intersect.md)
`intervals` | Filter variants by BED intervals | [intervals](docs/intervals.md)
`intervalsIndex` | Filter variants by intervals using file indexing | [intervalsindex](docs/intervalsindex.md)
`join` | Join files by genomic region | [join](docs/join.md)
`phastCons` | Annotate using phastCons conservation scores | [phastcons](docs/phastcons.md)
`private` | Annotate private variants (family/group) | [private](docs/private.md)
`rmInfo` | Remove INFO fields from VCF | [rminfo](docs/rminfo.md)
`rmRefGen` | Remove reference genotypes | [rmrefgen](docs/rmrefgen.md)
`split` | Split VCF by chromosome | [split](docs/split.md)
`tstv` | Calculate transition/transversion ratio | [tstv](docs/tstv.md)
`varType` | Annotate variant type (SNP, MNP, INS, DEL, MIXED) | [varianttype](docs/varianttype.md)
`vcfCheck` | Check VCF file format correctness | [vcfcheck](docs/vcfcheck.md)
`vcf2tped` | Convert VCF to TPED format | [vcf2ped](docs/vcf2ped.md)

## Additional Documentation

Topic | File
----- | ----
Introduction | [introduction](docs/introduction.md)
FAQ | [faq](docs/faq.md)
Download & Install | [download](docs/download.md)
Examples | [examples](docs/examples.md)
Help | [help](docs/help.md)
