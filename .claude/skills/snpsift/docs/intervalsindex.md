# SnpSift Intervals Index

Extract variants that intersect genomic intervals, optimized for very large VCF files with a relatively small number of query intervals.

!!! info
    This is similar to [SnpSift Intervals](intervals.md), but uses a file-indexing approach instead of loading the entire VCF into memory. Use this when your VCF file is too large to process with `SnpSift intervals`.

### Usage

```
java -jar SnpSift.jar intidx [options] file.vcf file.bed > output.vcf
java -jar SnpSift.jar intidx [options] -i file.vcf chr:start-end [chr:start-end ...] > output.vcf
Options:
    -i        : Provide genomic intervals on the command line instead of a BED file.
                Coordinates are assumed to be 1-based.
    -if <N>   : Input coordinate offset for command-line intervals. Default: 0 (zero-based).
                When -i is used, this is automatically set to 1 (one-based).
                Has no effect on BED file parsing (BED is always 0-based).
```

!!! warning
    The VCF file must be:

    - An **uncompressed file on disk** (not STDIN, not gzipped). The command uses random file access for binary search, which requires a seekable uncompressed file.
    - **Sorted by position** within each chromosome. The binary search algorithm assumes monotonically increasing positions. Unsorted files produce silently wrong results.

### How it works

Instead of reading every line of the VCF, the command builds a byte-offset index of the file by chromosome using binary search on the raw file bytes. For each query interval, it seeks directly to the matching file region and dumps the overlapping VCF lines.

This makes it very fast for extracting a small number of intervals from huge VCF files, since most of the file is never read.

Only one BED file is accepted. If you need to combine multiple BED files, concatenate them beforehand.

Output is VCF format (with header) written to STDOUT.
If two query intervals overlap, the same VCF lines may appear multiple times in the output (no deduplication).
If an interval references a chromosome not present in the VCF, an error is printed to STDERR and the command continues with remaining intervals.

### Examples

Extract variants in target regions:
```
java -jar SnpSift.jar intidx variants.vcf target_regions.bed > variants_on_target.vcf
```

Extract variants at specific genomic positions (1-based coordinates):
```
java -jar SnpSift.jar intidx -i variants.vcf chr1:12345-23456 chr2:3456789-4567890 > selected_variants.vcf
```

### BED file format

BED files are tab-separated with 0-based coordinates (end position is exclusive).
Only the first three columns are used (`chr`, `start`, `end`); additional columns are ignored.
A minimum of two columns (`chr`, `start`) is required; if `end` is missing, it defaults to `start`.
Header lines must start with `#`.
