# SnpSift Private

Annotate whether a variant is private to a single family or group.

A variant is "private" if all samples carrying the variant (i.e. having a non-reference genotype) belong to the same family. When this is the case, a `Private=<familyId>` INFO field is added to the VCF entry.

### Usage

```
java -jar SnpSift.jar private file.tfam [file.vcf] > output.vcf
```

The first argument is a TFAM file defining the sample-to-family mapping. The second is the VCF file (default: STDIN).

All VCF entries are output, both annotated and non-annotated.

### TFAM file format

The TFAM file is PLINK's family information format: whitespace-separated with 6 columns per line (familyId, sampleId, fatherId, motherId, sex, phenotype). Only the first two columns are used by this command: `familyId` groups samples into families, and `sampleId` must exactly match the sample names in the VCF header. Lines starting with `#` are skipped.

### How it works

For each VCF entry, the command examines all sample genotypes. If every sample that carries a variant allele (non-reference genotype) belongs to the same family, the variant is annotated as `Private=<familyId>`. If variant carriers span two or more families, no annotation is added.

Samples present in the VCF but not found in the TFAM file produce a warning on STDERR and are ignored (their genotypes do not count toward or against the private determination). If all VCF samples are missing from the TFAM file, the command exits with an error.

### Example

```
java -jar SnpSift.jar private phenotypes.tfam variants.vcf > variants.private.vcf
```

An annotated variant may look like:
```
1   1005806 rs3934834   C   T   .   PASS    AF=0.091;Private=Family_47
```

This indicates that the variant is only found in members of `Family_47`, according to the definitions in the TFAM file.
