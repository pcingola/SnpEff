# SnpSift RmRefGen

Remove reference genotypes.

Replaces genotype information for non-variant samples.

E.g. If you have this file:
```
#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT       M1                 M2                X1              X2              
2L      426906  .       C       G       53.30   .       DP=169  GT:PL:GQ     0/1:7,0,255:4      0/1:7,0,255:4     0/0:0,0,0:6     0/0:0,30,255:35
2L      601171  .       C       A       999.00  .       DP=154  GT:PL:GQ     0/1:81,0,141:78    0/1:42,0,251:39   0/0:0,0,0:4     0/0:0,33,255:36
2L      648611  .       A       T       999.00  .       DP=225  GT:PL:GQ     0/1:52,0,42:47     1/1:75,21,0:14    0/0:0,0,0:3     0/0:0,60,255:61
2L      807373  .       A       G       106.00  .       DP=349  GT:PL:GQ     0/1:14,0,65:12     0/1:60,0,42:50    0/0:0,0,0:4     0/0:0,69,255:72
2L      816766  .       G       T       999.00  .       DP=411  GT:PL:GQ     0/1:108,0,45:53    0/1:7,0,255:6     0/0:0,0,0:4     0/0:0,57,255:59
```

You can run:

     cat file.vcf | java -jar SnpSift.jar rmRefGen > file_noref.vcf

and you get this (notice the last two columns, that had '0/0' genotype):
```
#CHROM  POS     ID      REF     ALT     QUAL    FILTER  INFO    FORMAT       M1                 M2                X1    X2              
2L      426906  .       C       G       53.30   .       DP=169  GT:PL:GQ     0/1:7,0,255:4      0/1:7,0,255:4     .     .
2L      601171  .       C       A       999.00  .       DP=154  GT:PL:GQ     0/1:81,0,141:78    0/1:42,0,251:39   .     .
2L      648611  .       A       T       999.00  .       DP=225  GT:PL:GQ     0/1:52,0,42:47     1/1:75,21,0:14    .     .
2L      807373  .       A       G       106.00  .       DP=349  GT:PL:GQ     0/1:14,0,65:12     0/1:60,0,42:50    .     .
2L      816766  .       G       T       999.00  .       DP=411  GT:PL:GQ     0/1:108,0,45:53    0/1:7,0,255:6     .     .
```
