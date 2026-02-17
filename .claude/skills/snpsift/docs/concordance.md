# SnpSift Concordance

Calculate concordance between two VCF files.

### Typical usage

This is typically used when you want to calculate concordance between a genotyping experiment and a sequencing experiment.

For instance, you sequenced several samples and, as part of a related experiment or just as quality control, you also genotype the same samples using a genotyping array.
Now you want to compare the two experiments.
Ideally there would be no difference between the variants from genotyping and sequencing, but this is hardly the case in real world.

You can use `SnpSift concordance` to measure the differences between the two experiments.

!!! warning
    Both VCF files must be sorted by chromosome and position.

!!! warning
    Sample names are defined in '#CHROM' line of the header section.
    Concordance is calculated only if a sample label matches in both files.

!!! info
    The first VCF file is indexed for fast seeking, so it should be the smaller of the two files for best performance.

---

## Command Options

- `-s <file>` : Only use sample IDs listed in this file (one sample ID per line). Samples not in this file are ignored.
- `-v` : Verbose mode. Shows progress and summary messages on STDERR.

Usage:
```
java -jar SnpSift.jar concordance [options] reference.vcf sequencing.vcf
```

---

### Output

SnpSift's concordance output is written to STDOUT and two files.
For instance the command `java -jar SnpSift.jar concordance genotype.vcf sequencing.vcf` will write:

* Concordance by variant: Written to STDOUT
* Concordance by sample: Written to `concordance_genotype_sequencing.by_sample.txt`
* Summary: Written to `concordance_genotype_sequencing.summary.txt`

The output file names are derived from the base names of the input VCF files (without extension).

---

#### Concordance by variant

This is a table (written to STDOUT) showing concordance details for every entry (chr:position).
Each row represents a variant position, with columns counting how many samples had each genotype combination between the two files.

Column names follow the pattern `<genotype_in_file1>/<genotype_in_file2>`, where genotype values are:

* `REF` : Homozygous reference (0/0)
* `ALT_1` : Heterozygous or homozygous for the first alternate allele (0/1, 1/0, or 1/1)
* `ALT_2` : Genotype involving a second alternate allele (for multi-allelic sites)
* `MISSING_GT_<filename>` : The sample has a missing genotype (./.) in that file
* `MISSING_ENTRY_<filename>` : The variant position does not exist in that file

For example, the column `REF/ALT_1` counts samples that are homozygous reference in the first file but have the first ALT allele in the second file.
The column `ALT_1/ALT_1` counts samples where both files agree on the first ALT allele.

The diagonal columns (`REF/REF`, `ALT_1/ALT_1`, `ALT_2/ALT_2`) represent concordant genotypes.
Off-diagonal columns represent discordant genotypes.

An `ERROR` column counts samples where comparison was not possible (e.g., REF or ALT mismatch between files).

**Matching rules:**

* If a variant exists at a position in only one file, it is still tracked using the `MISSING_ENTRY` columns, so you can see how many samples had genotype calls for positions absent in the other file.
* If both entries are bi-allelic and their ALT fields differ, the entry is counted as an error.
* If REF fields differ between the two files, the entry is counted as an error.
* Multi-allelic variants are processed normally (genotype codes `ALT_1`, `ALT_2` distinguish the alleles).

#### Concordance by sample

This file has the same column format as the by-variant output, but counts are aggregated per sample (one row per sample, sorted alphabetically).

#### Summary

Summary file contains overall information and errors.
Here is an example of a summary file:
```
$ cat concordance_genotype_sequencing.summary.txt
Number of samples:
    929    File genotype.vcf
    583    File sequencing.vcf
    514    Both files
Errors:
        ALT field does not match	19
```
The header indicates that one file ('genotype.vcf') has 929 samples, the other file has 583 and there are 514 matching sample IDs in both files.

The errors section shows the count of each error type encountered.
In this case there were 19 ALT fields that did not match between 'genotype.vcf' and 'sequencing.vcf'.
This can happen, for instance, when there are INDELs, which cannot be detected by genotyping arrays.

!!! info
    Summary messages are also shown to STDERR if you use verbose mode (command line option `-v`).
