# SnpSift Intervals

Filter VCF entries that intersect (or don't intersect) genomic intervals defined in BED files.

### Usage

```
java -jar SnpSift.jar intervals [options] file_1.bed [file_2.bed ... file_N.bed]
Options:
    -i <file.vcf> : VCF input file. Default: STDIN
    -x            : Exclude mode: output VCF entries that do NOT intersect any interval
```

!!! warning
    The VCF input must be provided via `-i` or STDIN. All positional arguments are treated as BED files.
    If you pass a VCF file as a positional argument, it will be incorrectly loaded as a BED file.

### How it works

All intervals from all BED files are loaded into a single interval forest.
Each VCF entry is tested for position overlap against this interval set.

By default, VCF entries that overlap any interval are output.
With `-x`, VCF entries that do NOT overlap any interval are output.
Output is in standard VCF format (with header).

### Example

Keep only variants in target regions:
```
cat variants.vcf | java -jar SnpSift.jar intervals target_regions.bed > variants_on_target.vcf
```

Exclude variants in problematic regions:
```
java -jar SnpSift.jar intervals -x -i variants.vcf blacklist.bed > variants_filtered.vcf
```

Multiple BED files can be provided and their intervals are combined:
```
java -jar SnpSift.jar intervals -i variants.vcf peaks_rep1.bed peaks_rep2.bed peaks_rep3.bed > variants_in_peaks.vcf
```

### BED file format

BED files are tab-separated with 0-based coordinates (end position is exclusive).
Only the first three columns are used (`chr`, `start`, `end`); additional columns are ignored for filtering purposes.
A minimum of two columns (`chr`, `start`) is required; if `end` is missing, it defaults to `start`.
Header lines must start with `#`.
