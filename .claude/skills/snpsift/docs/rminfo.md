# SnpSift RmInfo

This command removes INFO fields from a VCF file (i.e. removes annotations)

Removing INFO fields is usually done because you want to re-annotate the VCF file, thus removing old INFO fields in order to add new ones later.

SnpEff SnpSift only add annotations and do not change current ones.
So, in order to re-annotate a file, you should first remove the old annotations and then re-annotate.

The reason for this behavior is simply because replacing annotation values is considered a bad practice.
Imagine that you have a VCF entry  in your re-annotated file having the value "AA=1": How do you know if this is from the old annotations or from the new ones?
This confusion often leads to problems in downstream steps of your pipelines, so it's better to avoid the problem by first removing all the previous annotations and then adding the new ones.

Usage example:
```
$ cat test.snpeff.vcf
#CHROM	POS 	ID	    REF	ALT	QUAL	FILTER	INFO
1	    734462	1032	G	A	.	    s50	    AC=348;EFF=DOWNSTREAM(MODIFIER|||||RP11-206L10.8|processed_transcript|NON_CODING|ENST00000447500||1),INTRON(MODIFIER|||||RP11-206L10.6|processed_transcript|NON_CODING|ENST00000429505|1|1)

$ java -jar SnpSift.jar rmInfo test.snpeff.vcf EFF
#CHROM	POS	    ID	    REF	ALT	QUAL	FILTER	INFO
1	    734462	1032	G	A	.	    s50	    AC=348
```
