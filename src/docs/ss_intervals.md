# SnpSift Intervals

This is used to extract variants that intersect any interval.

You must provide intervals as BED files.

Command line options:

* '-x' : Filter out (exclude) VCF entries that match any interval in the BED files.
* '-i file.vcf' : Specify the input VCF file (default is STDIN).

E.g.:

    cat variants.vcf | java -jar SnpSift.jar intervals my_intervals.bed > variants_intersecting_intervals.vcf

!!! warning
    BED file format is tab separated zero-based coordinates "chr \t start \t end " (for this application, all other fields in the BED file are ignored).

!!! warning
    If BED file has header lines, they must start with a '#'
