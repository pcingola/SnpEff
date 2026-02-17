# SnpSift PhastCons

Annotate variants or intervals with PhastCons conservation scores.

### Usage

```
java -jar SnpSift.jar phastCons [options] path/to/phastCons/dir inputFile
Options:
    -bed              : Input is a BED file (default: VCF).
    -minScore <num>   : Minimum conservation score threshold. Default: 0.0
    -extract <num>    : Extract conserved sub-intervals of at least <num> bases
                        with average score >= minScore. Only works with -bed.
```

The first positional argument is the directory containing the PhastCons wigFix files and the `genome.fai` file.
The second is the input file (VCF or BED).

### Database setup

The command requires two things in the phastCons directory:

**1. PhastCons wigFix files:** Download from UCSC. For example, for hg19:
```
mkdir -p ~/snpEff/db/phastCons && cd ~/snpEff/db/phastCons
wget -r -np -nd -A "*.phastCons100way.wigFix.gz" \
    http://hgdownload.soe.ucsc.edu/goldenPath/hg19/phastCons100way/hg19.100way.phastCons/
```

For other genomes (e.g. hg38), use the corresponding UCSC directory.
Files must match the naming pattern `chr<N>.*.wigFix*` and can be gzipped.
Only `fixedStep` wiggle format is supported.

**2. A chromosome size file named `genome.fai`:** This is a fasta index file (tab-separated: chromosome name, length, ...). Create it with:
```
samtools faidx path/to/genome.fa
cp path/to/genome.fa.fai ~/snpEff/db/phastCons/genome.fai
```

The file must be named exactly `genome.fai` and placed in the phastCons directory.

### VCF mode (default)

Annotates each VCF entry with a `PhastCons` INFO field containing the conservation score (formatted to 3 decimal places).

For single-base variants, the score at that position is used. For multi-base variants, the average score across all bases is used.

The `PhastCons` field is only added when the score exceeds `-minScore` (default 0.0). All VCF entries are output regardless of score.

```
java -Xmx8g -jar SnpSift.jar phastCons ~/snpEff/db/phastCons variants.vcf > variants.phastCons.vcf
```

### BED mode (`-bed`)

With `-bed`, the input is read as a BED file and the output is BED format with the average conservation score in the fifth column.

```
java -jar SnpSift.jar phastCons -bed ~/snpEff/db/phastCons regions.bed > regions.scored.bed
```

The score column is only included for intervals with score above `-minScore`.

**Extracting conserved sub-intervals:** With `-extract <num>`, each input interval is scanned for the longest contiguous sub-intervals that have at least `<num>` bases and an average conservation score >= `-minScore`. Extracted sub-intervals are non-overlapping.

```
java -jar SnpSift.jar phastCons -bed -minScore 0.8 -extract 10 ~/snpEff/db/phastCons regions.bed
```

This extracts all sub-intervals of at least 10 bases with average conservation >= 0.8. The `-extract` option is only available in BED mode; it is silently ignored with VCF input.

### Notes

The input file should ideally be sorted by chromosome. PhastCons data is loaded one chromosome at a time and replaced when a new chromosome is encountered. Unsorted input causes repeated reloading of chromosome data.

If no PhastCons file is found for a chromosome, variants on that chromosome may receive a `PhastCons=0.000` annotation. Use `-minScore` with a small positive value (e.g. 0.001) to avoid false annotations from missing data.

Memory usage can be significant: each chromosome's scores are stored as a per-base array. Use `-Xmx8g` or higher for large genomes.
