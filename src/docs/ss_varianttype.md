# SnpSift Variant type

Adds an INFO field denoting variant type.

It adds "SNP/MNP/INS/DEL/MIXED" in the INFO field.
It also adds "HOM/HET", but this last one works if there is only one sample (otherwise it doesn't make any sense).
```
$ java -jar SnpSift.jar varType  test.vcf | grep -v "^#" | head
20	10469	.	C	G	100.0	PASS	SNP;HOM	GT:AP	0|0:0.075,0.060
20	10492	.	C	T	100.0	PASS	SNP;HET	GT:AP	0|1:0.180,0.345
20	10575	.	C	CG	100.0	PASS	DEL;HET	GT:AP	0|1:0.000,0.000
20	10611	.	CG	C	100.0	PASS	INS;HET	GT:AP	0|1:0.000,0.010
20	10618	.	GT	TA	100.0	PASS	MNP;HET	GT:AP	0|1:0.020,0.030
```
