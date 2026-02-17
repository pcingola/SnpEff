# SnpSift Join

Join two files by genomic regions (i.e. chr:start-end).

For each interval in file1, the command finds intersecting intervals in file2. Both files are loaded entirely into memory.

### Usage

```
java -jar SnpSift.jar join [options] file1 file2
Options:
    -cols1 <colDef>  : Column definition for file1. Format: chrCol,startCol,endCol (1-based column numbers).
                       Shortcuts 'bed' or 'vcf' are allowed.
    -cols2 <colDef>  : Column definition for file2. Same format as -cols1.
    -if1 <num>       : Coordinate offset for file1 (0 for zero-based, 1 for one-based). Default: 0
    -if2 <num>       : Coordinate offset for file2. Default: 0
    -all             : Show all intersecting intervals from file2.
                       Default: show only the one with the largest overlap.
    -closest         : If no intersection found, show the closest interval from file2.
    -empty           : Show intervals from file1 even if no match is found.
```

!!! warning
    You should always specify `-cols1` and `-cols2` explicitly. The `bed` shortcut sets columns to 1,2,3 with 0-based coordinates. The `vcf` shortcut sets columns to 1,2,2 with 1-based coordinates (treating each entry as a single-position interval).

### How it works

File2 is loaded into an interval forest. For each interval in file1, the command queries this forest for overlapping intervals.

By default, only the single best match (largest overlap) from file2 is reported for each interval in file1. With `-all`, all intersecting intervals are reported.

When `-closest` is used and no intersection is found, the command searches for the nearest interval in file2 by progressively expanding the search window. If both `-closest` and `-empty` are used, intervals with no intersection show the closest match if one is found, or are output with a "NONE" label. Without `-empty`, intervals with no match at all are silently dropped.

When `-empty` is used without `-closest`, all non-intersecting intervals from file1 are output with a "NONE" label.

Input files are parsed as whitespace-separated (tabs or spaces). Lines starting with `#` are treated as comments and skipped.

### Output format

Tab-separated lines with a label prefix indicating the match type:

```
INTERSECT    <line_from_file1>    <line_from_file2>
CLOSEST      <line_from_file1>    <line_from_file2>
NONE         <line_from_file1>
```

`INTERSECT` lines appear for intervals with overlapping matches. `CLOSEST` lines appear when `-closest` is used and no overlap was found but a nearby interval exists. `NONE` lines appear when `-empty` is used and no match was found.

### Examples

Join two BED files, showing intersecting or closest intervals:
```
java -Xmx2G -jar SnpSift.jar join -cols1 bed -cols2 bed -closest file1.bed file2.bed
```

Join a BED file with a custom file having chr:start:end in columns 7, 8, and 9, showing non-intersecting intervals:
```
java -Xmx2G -jar SnpSift.jar join -cols1 bed -cols2 7,8,9 -empty file.bed my_file.txt
```
