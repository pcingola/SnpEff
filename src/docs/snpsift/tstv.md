# SnpSift TsTv

Calculate transition/transversion ratios and other variant statistics per sample.

## Usage

```
java -jar SnpSift.jar tstv file.vcf
```

!!! warning
    Only SNPs are used for Ts/Tv calculations.

## Output

The command outputs four statistics sections, all broken down per sample:

- **TS/TV stats**: Transition and transversion counts and ratios per sample
- **Hom/Het stats**: Homozygous reference, one ALT, two ALTs, and missing counts
- **Variant type stats**: Counts by variant type (SNP, MNP, INS, DEL, MIXED, Multiallelic)
- **Allele count stats**: Minor allele count distribution

Example:

```
java -jar SnpSift.jar tstv s.vcf

TS/TV stats:
Sample ,1,2,3,4,5,6,7,8,9,10,11,12,Total
Transitions ,150488,150464,158752,156674,152936,160356,152276,155314,156484,149276,151182,153468,1847670
Transversions ,70878,70358,73688,72434,70828,76150,72030,71958,72960,69348,70180,71688,862500
Ts/Tv ,2.123,2.139,2.154,2.163,2.159,2.106,2.114,2.158,2.145,2.153,2.154,2.141,2.142

Hom/Het stats:
...

Variant type stats:
...

Allele count stats:
...
```
