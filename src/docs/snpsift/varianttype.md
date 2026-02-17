# SnpSift Variant type

Adds variant type annotations to the INFO field of a VCF file.

## Usage

```
java -jar SnpSift.jar varType file.vcf > annotated.vcf
```

Output is written to STDOUT.

## Annotations added

The command adds the following INFO fields:

- Variant type flag: `SNP`, `MNP`, `INS`, `DEL`, or `MIXED`
- Zygosity flag: `HOM` or `HET` (only meaningful for single-sample VCF files)
- `VARTYPE`: Comma-separated list of variant types, one per allele

## Example

```
$ java -jar SnpSift.jar varType test.vcf | grep -v "^#" | head
20	10469	.	C	G	100.0	PASS	SNP;HOM	GT:AP	0|0:0.075,0.060
20	10492	.	C	T	100.0	PASS	SNP;HET	GT:AP	0|1:0.180,0.345
20	10575	.	C	CG	100.0	PASS	INS;HET	GT:AP	0|1:0.000,0.000
20	10611	.	CG	C	100.0	PASS	DEL;HET	GT:AP	0|1:0.000,0.010
20	10618	.	GT	TA	100.0	PASS	MNP;HET	GT:AP	0|1:0.020,0.030
```
