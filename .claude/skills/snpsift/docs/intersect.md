# SnpSift Intersect

This command intersects several interval files and produces a result of all intersections.

A typical usage example is to create a consensus of peaks from several ChIP-Seq experiments.

### Supported file formats

Input files can be: BED, VCF, GFF, or TXT (assumed to be `chr \t start \t end`).
Gzipped files (`.gz`) are also supported.

### Usage

```
java -jar SnpSift.jar intersect [options] file_1.bed file_2.bed ... file_N.bed
Options:
    -minOverlap <num> : Minimum number of bases that two intervals have to overlap. Default: 1
    -cluster <num>    : Total number of intersecting intervals required (including the query interval itself). Default: 2
    -intersect        : Report the intersection (narrowest overlap) of all intervals. Default: true
    -union            : Report the union (widest span) of all intervals. Default: false
    -not <file>       : Filter out results that intersect with any interval in this file. Can be used multiple times.
```

The command requires at least two input files to produce meaningful results (with a single file, all intervals pass through unchanged).

### Algorithm

For every interval in all input files, the command queries all other interval forests for overlapping intervals.
Two intervals must share at least `minOverlap` bases to be considered overlapping.
If the total number of overlapping intervals (including the query interval itself) is at least `cluster`, a consensus interval is produced.

With `-intersect` (the default), the consensus is the **narrowest** region shared by all overlapping intervals.
With `-union`, the consensus is the **widest** span covering all overlapping intervals.

Duplicate consensus intervals (same chromosome, start, and end) are deduplicated in the output.

### Output format

Tab-separated BED-like format: `chr \t start \t end \t id`

Coordinates are 0-based with exclusive end (BED convention). The `id` field is the concatenation of all contributing interval IDs joined by `+`.

Results that overlap with intervals from a `-not` file are filtered out.
