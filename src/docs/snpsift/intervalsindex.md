# SnpSift Intervals Index

This is used to extract variants that intersect any interval.

!!! warning
    This is similar to "SnpSift intervals", but intended for huge VCF files, and relatively small number of intervals.

This command indexes the VCF file, thus is optimized for huge VCF files.
You must provide intervals as BED files.
BED format is tab separated zero-based coordinates "chr \t start \t end " (for this application, all other fields in the BED file are ignored).
You can use command line option '-if 1' if you want one-based coordinates.

E.g.:

    java -jar SnpSift.jar intidx variants.vcf my_intervals.bed > variants_intersecting_intervals.vcf

You can also have genomic coordinate in the command line.
Note that in this case, coordinates are assumed to be one-based (instead of zero-based, like in BED files):

    java -jar SnpSift.jar intidx -c variants.vcf chr1:12345-23456 chr2:3456789-4567890  > variants_intersecting_intervals.vcf


!!! warning
    BED file format is tab separated zero-based coordinates "chr \t start \t end " (for this application, all other fields in the BED file are ignored).

!!! warning
    If BED file has header lines, they must start with a '#'
