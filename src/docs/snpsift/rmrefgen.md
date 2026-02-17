# SnpSift RmRefGen

Remove reference genotypes from a VCF file.

For each VCF entry, samples with homozygous reference genotypes (e.g. `0/0`) have their entire genotype column replaced by `.` (missing). This removes all genotype sub-fields (GT, PL, GQ, etc.), not just the GT field.

### Usage

```
java -jar SnpSift.jar rmRefGen [file.vcf] > output.vcf
```

Default input is STDIN.

### Example

```
$ cat file.vcf
#CHROM  POS     ID  REF  ALT  QUAL    FILTER  INFO    FORMAT    M1               M2               X1             X2
2L      426906  .   C    G    53.30   .       DP=169  GT:PL:GQ  0/1:7,0,255:4    0/1:7,0,255:4    0/0:0,0,0:6    0/0:0,30,255:35
2L      648611  .   A    T    999.00  .       DP=225  GT:PL:GQ  0/1:52,0,42:47   1/1:75,21,0:14   0/0:0,0,0:3    0/0:0,60,255:61

$ java -jar SnpSift.jar rmRefGen file.vcf
#CHROM  POS     ID  REF  ALT  QUAL    FILTER  INFO    FORMAT    M1               M2               X1  X2
2L      426906  .   C    G    53.30   .       DP=169  GT:PL:GQ  0/1:7,0,255:4    0/1:7,0,255:4    .   .
2L      648611  .   A    T    999.00  .       DP=225  GT:PL:GQ  0/1:52,0,42:47   1/1:75,21,0:14   .   .
```

Notice that the last two columns (X1, X2) had `0/0` genotypes and were replaced by `.`, removing all sub-fields.
