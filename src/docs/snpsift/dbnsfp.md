# SnpSift dbNSFP

The [dbNSFP](https://sites.google.com/site/jpopgen/dbNSFP) is an integrated database of functional predictions from multiple algorithms (SIFT, Polyphen2, LRT and MutationTaster, PhyloP and GERP++, etc.).

### Typical usage

One of the main advantages is that you can annotate using multiple prediction tools with just one command.
This allows for faster annotations.
[Here](https://sites.google.com/site/jpopgen/dbNSFP) is the link to dbNSFP database website for more details.

**Database:** In order to annotate using dbNSFP, you need to download the dbNSFP database and the index file.
dbNSFP is large (several GB) so it might take a while to download it.
The database is compressed (block-gzip) and tabix-indexed, so two files are required (the data *.gz file and the *.gz.tbi index file).

!!! warning
    dbNSFP only contains data for SNPs (single nucleotide polymorphisms). Indels and other variant types are silently skipped during annotation.

!!! warning
    The input VCF file must be sorted by chromosome and position. The command will fail with an error if unsorted entries are detected.

### Downloading

You can download the files from SnpEff's site.

WARNING: Remember that you need both the database and the index files

DbNSFP Version 4.5:
* GRCh37 / hg19:
    * [Database](https://snpeff-public.s3.amazonaws.com/databases/db/GRCh37/dbNSFP/dbNSFP4.5c.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff-public.s3.amazonaws.com/databases/db/GRCh37/dbNSFP/dbNSFP4.5c.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`
* GRCh38 / hg38:
    * [Database](https://snpeff-public.s3.amazonaws.com/databases/db/GRCh38/dbNSFP/dbNSFP4.5c.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff-public.s3.amazonaws.com/databases/db/GRCh38/dbNSFP/dbNSFP4.5c.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`

DbNSFP Version 4.1
* GRCh37 / hg19 (dbNSFP Academic):
    * [Database](https://snpeff-public.s3.amazonaws.com/databases/dbs/GRCh37/dbNSFP_4.1a/dbNSFP4.1a.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff-public.s3.amazonaws.com/databases/dbs/GRCh37/dbNSFP_4.1a/dbNSFP4.1a.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`
* GRCh38 / hg38 (dbNSFP Academic):
    * [Database](https://snpeff-public.s3.amazonaws.com/databases/dbs/GRCh38/dbNSFP_4.1a/dbNSFP4.1a.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff-public.s3.amazonaws.com/databases/dbs/GRCh38/dbNSFP_4.1a/dbNSFP4.1a.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`

---

## Command Options

- `-f <field_list>` : Comma-separated list of dbNSFP field names to annotate. Default fields are shown when running the command without arguments.
- `-n` : Invert field selection. Use all fields EXCEPT the ones specified with `-f`.
- `-db <file>` : Path to dbNSFP database file (bgzip + tabix).
- `-a` : Annotate fields even if the database has an empty value (uses '.' for missing). By default, empty fields are skipped.
- `-m` : Annotate fields even when there is no database entry for a variant (uses '.' for all fields). By default, variants not found in dbNSFP are left unchanged.
- `-collapse` : Collapse (deduplicate) repeated values when multiple dbNSFP entries match a variant (e.g., multiple transcripts). Values are comma-separated.
- `-nocollapse` : Disable collapsing of repeated values.
- `-g <genome>` : Genome version (used to locate the database in the config file).

---

### Output fields

All annotated fields are added to the VCF INFO column with the prefix `dbNSFP_`. For example, the dbNSFP field `SIFT_pred` becomes `dbNSFP_SIFT_pred` in the output VCF.

Special characters in field names are sanitized for VCF compatibility (e.g., `GERP++_RS` becomes `dbNSFP_GERP___RS` because `+` is not valid in VCF INFO keys).

When a variant matches multiple dbNSFP entries (common for genes with multiple transcripts), the values are comma-separated. Use `-collapse` to deduplicate repeated values across entries.

---

### Annotation examples

Annotate using default fields:
```
java -jar SnpSift.jar dbnsfp -v myFile.vcf > myFile.annotated.vcf
```

Annotate specific fields only:
```
java -jar SnpSift.jar dbnsfp -f SIFT_pred,Polyphen2_HDIV_pred,CADD_phred myFile.vcf > myFile.annotated.vcf
```

Annotate all fields EXCEPT specific ones:
```
java -jar SnpSift.jar dbnsfp -n -f Interpro_domain,Uniprot_acc myFile.vcf > myFile.annotated.vcf
```

Specify a custom database path:
```
java -jar SnpSift.jar dbnsfp -db path/to/dbNSFP4.5c.txt.gz myFile.vcf > myFile.annotated.vcf
```

Annotate even when values are missing in the database:
```
java -jar SnpSift.jar dbnsfp -a -m myFile.vcf > myFile.annotated.vcf
```

---

### Building dbNSFP (for developers)

!!! info
    Users do **NOT** need to do this, since a pre-indexed database can be downloaded from SnpSift's site (see previous sub-section).
    These instructions are mostly for developers.

You can also create dbNSFP files yourself, downloading the files from [DbNsfp](https://sites.google.com/site/jpopgen/dbNSFP) site.
Two files are required:

* A block-gzipped database file
* The corresponding tabix index for the database file.

Creating a file that SnpSift can use is simple, just follow this guideline:
```
# Download dbNSFP database (adjust version as needed)
$ wget http://dbnsfp.houstonbioinformatics.org/dbNSFPzip/dbNSFP4.5c.zip

# Uncompress
$ unzip dbNSFP4.5c.zip

# Create a single file version
$ (head -n 1 dbNSFP4.5c_variant.chr1 ; cat dbNSFP4.5c_variant.chr* | grep -v "^#" ) > dbNSFP4.5c.txt

# Compress using block-gzip algorithm
bgzip dbNSFP4.5c.txt

# Create tabix index
tabix -s 1 -b 2 -e 2 dbNSFP4.5c.txt.gz
```

**Building dbNSFP for hg19/GRCh37 using dbNSFP 4.X:**

Latest dbNSFP versions are based on GRCh38/hg38 genomic coordinates.
In order to use the latest dbNSFP databases with GRCh37/hg19 genome versions you need to create a new dbNSFP file with the right coordinates.
Fortunately, dbNSFP provides GRCh37/hg19 coordinates, so we only need to swap coordinates and sort by genomic position.
You can easily do this by using the `dbNSFP_sort.pl` script ([you can find it here](https://raw.githubusercontent.com/pcingola/SnpEff/master/scripts_build/dbNSFP_sort.pl)) by running something like the following command lines:
```
# Set to your downloaded dbNSFP version
version="4.5c"

# Replace coordinates by columns 7 and 8 (hg19 coordinates) and sort by those coordinates
cat dbNSFP${version}_variant.chr* \
    | $HOME/snpEff/scripts_build/dbNSFP_sort.pl 7 8 \
    > dbNSFP${version}_hg19.txt

# Compress and index
bgzip dbNSFP${version}_hg19.txt
tabix -s 1 -b 2 -e 2 dbNSFP${version}_hg19.txt.gz
```
