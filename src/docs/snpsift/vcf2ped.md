# SnpSift Vcf2Tped

Convert from VCF to PLINK's TPED file format.

The `vcf2tped` command uses a VCF and a TFAM file as input, creating a TPED and a consolidated TFAM as outputs.

## Usage

```
java -jar SnpSift.jar vcf2tped [options] file.tfam file.vcf outputName
```

| Option | Description | Default |
| --- | --- | --- |
| `-f` | Force. Overwrite new files if they exist | false |
| `-num` | Use only numbers {1, 2, 3, 4} instead of bases {A, C, G, T} | false |
| `-onlySnp` | Use only SNPs when converting VCF to TPED | false |
| `-onlyBiAllelic` | Use only bi-allelic variants | false |
| `-useMissing` | Use entries with missing genotypes (otherwise they are filtered out) | true |
| `-useMissingRef` | Use entries with missing genotypes marking them as 'reference' instead of 'missing' | false |

| Parameter | Description |
| --- | --- |
| `file.tfam` | File with genotypes and groups information (in PLINK's TFAM format) |
| `file.vcf` | A VCF file (variants and genotype data) |
| `outputName` | Base name for the new TPED and TFAM files |

## Features

`vcf2tped` command supports the following features:

* Output a TPED file:
    * Only samples present in both the input TFAM and the input VCF files are in the output TPED.
    * Bi-allelic filter: `-onlyBiAllelic` option filters out non bi-allelic variants.
    * Non SNP variants (InDels, MNPs, etc):
        * InDels and other non-SNP variants are converted to "fake" SNPs (some programs have problems handling non-SNP variants).
        * `-onlySnp` option filters out non SNP variants.
    * Missing variants:
        * By default, entries with missing genotypes are included and marked as missing in the TPED file.
        * `-useMissing` uses missing variants in TPED file.
        * `-useMissingRef` Converts missing variants to reference genotype.
* Output TFAM file:
    * Only samples present in both the input TFAM and the input VCF files are in the output TFAM.
    * Samples are re-ordered to have the same order as the VCF file
