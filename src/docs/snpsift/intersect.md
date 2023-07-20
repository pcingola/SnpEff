# SnpSift Intersect

This command intersects several intervals files (e.g. BED, BigBed, TXT) and produces a result of all intersections.

A typical usage example is to create a consensus of peaks from several Chip-Seq experiments.

Algorithm: This command creates one interval forest for each input file.
For every interval in all input files, finds all intervals that intersect at least `minOverlap` bases (default 1 base).
If there are at least `cluster` number of intersecting intervals it creates a consensus interval from the intersections (or `union`) of all intervals found.
The consensus interval, if any, is shown as result.

Command line options:
```
$ java -jar SnpSift.jar intersect
SnpSift version 1.9d (build 2013-04-26), by Pablo Cingolani
Usage: java -jar SnpSift.jar [options] file_1.bed file_2.bed ... file_N.bed
Options:
        -minOverlap <num> : Minimum number of bases that two intervals have to overlap. Default : 0
        -cluster <num>    : An interval has to intersect at least 'num' intervals (from other files) to be considered. Default: 0
        -intersect        : Report the intersection of all intervals. Default: false
        -union            : Report the union of all intervals. Default: true
```
