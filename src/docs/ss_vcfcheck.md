# SnpSift VcfCheck

Perform some basic check ups on VCF files to spot common problems.

`SnpSift vcfCheck` checks for some common problems where VCF files are not following the specification.
Given that many common VCF problems cause analysis tools and pipelines to behave unexpectedly, this command is intended as a simple debugging tool.

E.g.:
```
$ java -jar SnpSift.jar vcfCheck bad.vcf

WARNING: Malformed VCF entryfile 'bad.vcf', line 7:
        Entry  : 3	148885779	.	A	ATT,AT	999.0	PASS	UK10KWES_AC=0,0;MDV=94
        Errors :
                INFO filed 'UK10KWES_AC' has 'Number=1' in header, but it contains '2' elements.
                Cannot find header for INFO field 'MDV'

WARNING: Malformed VCF entryfile 'bad.vcf', line 14:
        Entry  : 3	148890104	.	TCA	T	.	.	.
        Errors :
                File is not sorted: Position '3:148890104' after position '3:148890105'

```
