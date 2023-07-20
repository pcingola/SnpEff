# SnpSift Join

Join files by genomic regions (i.e. chr:start-end).

Files can be generic TXT (tab separated), VCF or BED.

Usage example:
```
Usage: java -jar SnpSift.jar join [options] file1 file2
Note: It is assumed that both files fit in memory.
Options:
    -if1 <num>       : Offset for file1 (e.g. 1 if coordinates are one-based. Default: 1
    -if2 <num>       : Offset for file2 (e.g. 2 if coordinates are one-based. Default: 1
    -cols1 <colDef>  : Column definition for file 1. Format: chrCol,startCol,endCol (e.g. '1,2,3').
                       Shortcuts 'bed' or 'vcf' are allowed. Default: 'vcf
    -cols2 <colDef>  : Column definition for file 2. Format: chrCol,startCol,endCol (e.g. '1,2,3').
                       Shortcuts 'bed' or 'vcf' are allowed. Default: 'vcf
    -all             : For each interval, show all intersecting.
                       Default: show only one (the largest intersection)
    -closest         : Show closest intervals in file2 if none intersect.
                       Default: off
    -empty           : Show intervals in file1 even if they do not intersect with any other interval.
                       Default: off
```

Example: Join two bed files, showing intersecting or closest intervals

    java -Xmx2G -jar SnpSift.jar join -v -cols1 bed -cols2 bed -closest file1.bed file2.bed

Example: Join one bed file and another file having chr:start-end in columns 7,8 and 9 respectively. Showing intervals form file1 that do not intersect any interval from file2

    java -Xmx2G -jar SnpSift.jar join -v -cols1 bed -cols2 7,8,9 -empty file.bed my_weird_file.txt
