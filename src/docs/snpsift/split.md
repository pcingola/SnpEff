# SnpSift Split

Simply split (or join) VCF files.
Allows to create one file per chromosome or one file every N lines.

## Usage

```
java -jar SnpSift.jar split [-j] [-l <num>] file.vcf [file_2.vcf ... file_N.vcf]
```

| Option | Description |
| --- | --- |
| `-j` | Join all files in command line (output goes to STDOUT) |
| `-l <num>` | Split every `num` lines |
| (default) | Split by chromosome (one file per chromosome) |

## Splitting

A typical workflow for this command is to:

1. Split very large VCF files `SnpSift split huge.vcf`
2. Perform some CPU intensive processing in parallel using several computers or cores
3. Join the resulting VCF files `SnpSift split -j huge.000.vcf huge.001.vcf huge.002.vcf ... > huge.out.vcf`

### Split by chromosome

By default, the command splits a VCF file creating one output file per chromosome.

E.g.: Splitting a VCF having human variants:

    java -jar SnpSift.jar split myHugeVcf.vcf.gz

Will create files myHugeVcf.1.vcf, myHugeVcf.2.vcf, ... , myHugeVcf.22.vcf, myHugeVcf.X.vcf, myHugeVcf.Y.vcf

### Split by number of lines

You can specify the `-l` command line option to split the file every N lines.

E.g.: Split a VCF file every 10,000 lines:

    java -jar SnpSift.jar split -l 10000 myHugeVcf.vcf.gz

Will create files myHugeVcf.000.vcf, myHugeVcf.001.vcf, myHugeVcf.002.vcf, ...

!!! info
    VCF header will be added to each file, so resulting files will be more than 10,000 lines.

!!! info
    Output files are always uncompressed `.vcf`, even if the input is a `.vcf.gz` file.

## Joining

You can use `-j` (join) command line option to join a set of VCF files. The header is taken from the first file, and the output is written to STDOUT. At least two input files are required.

    java -jar SnpSift.jar split -j huge.000.vcf huge.001.vcf huge.002.vcf ... > huge.out.vcf
