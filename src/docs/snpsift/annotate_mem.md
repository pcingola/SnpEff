# SnpSift `annotateMem` Command Documentation

The `annotateMem` command is a, high-performance tool for annotating VCF files using pre-built “databases” such as dbSnp, ClinVar, GnomAD, Cosmic, and more. It is optimized to handle large VCF files—annotating over 1 million VCF lines per minute in many cases. This is achieved by converting database VCF files into memory-optimized dataframes indexed by chromosome and variant type.

---

## Overview

The annotation process is divided into **two steps**:

1. **Create the Database:**
   - **Purpose:** Convert one or more VCF files (e.g., from ClinVar, dbSnp, Cosmic, etc.) into a database.
   - **Note:** Although the database creation step can take a long time, it only needs to be performed once per database. Subsequent annotations will leverage the pre-built databases, making the overall process very efficient.

2. **Annotate VCF Files:**
   - **Purpose:** Annotate your input VCF file(s) by querying the databases created in step 1.
   - **Performance:** During annotation, only the relevant dataframes for the current chromosome are loaded into memory, allowing for quick, in-memory searches for each VCF record.

---

## How It Works

- **Database Creation:**  
  The INFO fields from the provided VCF file are extracted and stored in a memory-optimized dataframe. The dataframe is indexed by chromosome and variant type, which facilitates rapid lookups during the annotation step.

- **Annotation:**  
  During annotation, each VCF line is enriched with the fields from the corresponding database entries by performing a fast in-memory search of the pre-built dataframes.

---

## Command Line Usage

### Creating a Database

When creating a database, specify the `-create` option along with one or more `-dbfile` parameters and the corresponding `-fields` that you want to include in the database.

**Example:**  
Create a database using ClinVar VCF, incorporating the INFO fields `CLNSIG`, `CLNDN`, and `ID`:

```bash
java -Xmx16G -jar SnpSift.jar \
    annmem \
    -create \
    -dbfile 'db/clinvar.2024-11-03.vcf' \
    -fields 'CLNSIG,CLNDN,ID'
```


When a database is created, it is stored in a dedicated directory named after the original VCF file, with the suffix `.snpsift.vardb` appended. For example, if your input VCF file is named `clinvar.vcf`, the resulting database will be saved in a directory called `clinvar.vcf.snpsift.vardb`.

Within this directory, the database is partitioned by chromosome. Each chromosome has its own file named following the pattern `{chromosomeName}.snpsift.df`. These files contain the serialized dataframes that store the selected INFO fields for that specific chromosome, enabling fast and efficient in-memory lookups during the annotation step.

Example: When creating a database for `clinvar.2024-11-03.vcf`, the following directory is created
```
# ls clinvar.2024-11-03.vcf.snpsift.vardb/
10.snpsift.df
11.snpsift.df
12.snpsift.df
13.snpsift.df
14.snpsift.df
15.snpsift.df
16.snpsift.df
    ...
MT.snpsift.df
X.snpsift.df
Y.snpsift.df
```


### Annotating a VCF File

Once the database(s) have been created, use the `annmem` command to annotate your input VCF file. You can specify multiple databases to annotate the VCF simultaneously.

**Example:**  
Annotate an input VCF file using multiple databases:

```bash
java -Xmx16G -jar SnpSift.jar \
   annmem \
   -dbfile 'db/clinvar.vcf.gz' \
   -dbfile 'db/dbSnp.151.vcf.gz' \
   -dbfile 'db/cosmic-v92.vcf.gz' \
   input.vcf \
   > input.ann.vcf
```

During this annotation step, the required dataframes are loaded into memory on a per-chromosome basis, ensuring efficient processing.

**Note:** If no `fields` parameter is used in the annotation command, all field in the database are used.

**Note:** If a variant from the input VCF file does not have an entry the database/s, then no INFO field is added.

**Note:** You can specify `-addAnnotated` to add the `ANNOTATED` flag to every VCF entry, so downstream processes know the VCF entry was annotated.

---

## Command Options

Below is a summary of the available command options for `annotateMem`:

- `-addAnnotated`  
  When annotating, add an `ANNOTATED` flag to every INFO field, this is added even if there are no annotations from the database/s added (e.g. because the variant doesn't have an entry in the databases).

- `-create`  
  Create one or more databases from the provided VCF file(s) using specific INFO field(s).

- `-dbfile file.vcf`  
  Use the specified VCF file. This file is either used to create a database or to provide annotation data.

- `-fields field_1,field_2,...,field_N`  
  Specify the comma-separated list of VCF INFO fields (without spaces) to use when creating or annotating.

- `-prefix prefix_db`  
  When annotating, prepend the given prefix to each annotated field name. This is useful when using multiple databases to avoid naming conflicts.

### Usage summary

**Create Databases**
```bash
java -jar SnpSift.jar annmem \
  -create \
  -dbfile database_1.vcf -fields field_1,field_2,...,field_N \
  -dbfile database_2.vcf -fields field_1,field_2,...,field_N \
  ... \
  -dbfile database_N.vcf -fields field_1,field_2,...,field_N
```

**Annotate VCF File**
```bash
java -jar SnpSift.jar annmem \
  [-addAnnotated] \
  -dbfile database_1.vcf -fields field_1,field_2,...,field_N [-prefix prefix_db_1] \
  -dbfile database_2.vcf -fields field_1,field_2,...,field_N [-prefix prefix_db_2] \
  ... \
  -dbfile database_N.vcf -fields field_1,field_2,...,field_N [-prefix prefix_db_N] \
  [input.vcf] > output.vcf
```

**Notes:**

- If `input.vcf` is not provided, `annotateMem` reads from standard input (STDIN).
- VCF files can be compressed with Gzip or Bgzip (if so, the file name must have a `.gz` extension)

---

## Summary

The SnpSift `annotateMem` command offers a fast and scalable solution for annotating large VCF files with data from multiple external databases. By leveraging memory-optimized dataframes and per-chromosome indexing, it delivers high annotation throughput—making it an essential tool for genomic variant analysis workflows.
