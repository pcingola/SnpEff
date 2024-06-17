# SnpSift dbNSFP

The [dbNSFP](https://sites.google.com/site/jpopgen/dbNSFP) is an integrated database of functional predictions from multiple algorithms (SIFT, Polyphen2, LRT and MutationTaster, PhyloP and GERP++, etc.).

### Typical usage

One of the main advantages is that you can annotate using multiple prediction tools with just one command.
This allows for faster annotations.
[Here](https://sites.google.com/site/jpopgen/dbNSFP) is the link to dbNSFP database website for more details.

**Database:** In order to annotate using dbNSFP, you need to download the dbNSFP database and the index file.
dbNSFP is large (several GB) so it might take a while to download it.
The database is compressed (block-gzip) and tabix-indexed, so two files are required (the data *.gz file and the *.gz.tbi index file).

### Doenloading

You can download the files from SnpEff's site.

WARNING: Remember that you need both the database and the index files

DbNSFP Version 4.5:
* GRCh37 / hg19:
    * [Database](https://snpeff.blob.core.windows.net/databases/db/GRCh37/dbNSFP/dbNSFP4.5c.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff.blob.core.windows.net/databases/db/GRCh37/dbNSFP/dbNSFP4.5c.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`
* GRCh38 / hg38:
    * [Database](https://snpeff.blob.core.windows.net/databases/db/GRCh38/dbNSFP/dbNSFP4.5c.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff.blob.core.windows.net/databases/db/GRCh38/dbNSFP/dbNSFP4.5c.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`

DbNSFP Version 4.1
* GRCh37 / hg19 (dbNSFP Academic):
    * [Database](https://snpeff.blob.core.windows.net/databases/dbs/GRCh37/dbNSFP_4.1a/dbNSFP4.1a.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff.blob.core.windows.net/databases/dbs/GRCh37/dbNSFP_4.1a/dbNSFP4.1a.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`
* GRCh38 / hg38 (dbNSFP Academic):
    * [Database](https://snpeff.blob.core.windows.net/databases/dbs/GRCh38/dbNSFP_4.1a/dbNSFP4.1a.txt.gz). Save file as `dbNSFP.txt.gz`
    * [Index](https://snpeff.blob.core.windows.net/databases/dbs/GRCh38/dbNSFP_4.1a/dbNSFP4.1a.txt.gz.tbi). Save file as `dbNSFP.txt.gz.tbi`

### dbNSFP Annotation example

Here is a full example how to perform annotations:

```
# Annotate using dbNSFP
# Note that the first time you run the command, it will attempt to download the dbNSFP database.
java -jar SnpSift.jar dbnsfp -v myFile.vcf > myFile.annotated.vcf
```

!!! info
    You can now specify which fields you want to use for annotation using the `-f` command line option followed by a comma separated list of field names.
    Defaults fields are shown when running the command without any arguments `java -jar SnpSift.jar dbNSFP`

If your dbNSFP file is not in the 'default' path (where SnpEff expects it), you can specify the path to your dbNSFP file using the `-db` command line option:

```
# Annotate using dbNSFP
java -jar SnpSift.jar dbnsfp -v -db path/to/my/dbNSFP2.9.txt.gz myFile.vcf > myFile.annotated.vcf
```

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
# Download dbNSFP database
$ wget http://dbnsfp.houstonbioinformatics.org/dbNSFPzip/dbNSFP2.9.zip

# Uncompress
$ unzip dbNSFP2.9.zip

# Create a single file version
$ (head -n 1 dbNSFP2.9_variant.chr1 ; cat dbNSFP2.9_variant.chr* | grep -v "^#" ) > dbNSFP2.9.txt

# Compress using block-gzip algorithm
bgzip dbNSFP2.9.txt

# Create tabix index
tabix -s 1 -b 2 -e 2 dbNSFP2.9.txt.gz
```

**Building dbNSFP for hg19/GRCh37 using dbNSFP 3.X:**

Latest dbNSFP versions (3.X) are based on GRCh38/hg38 genomic coordinates.
In order to use the latest dbNSFP databses with GRCh37/hg19 genome versions you need to create a new dbNSFP file with the right coordinates.
Fortunately, dbNSFP 3.X provides GRCh37/hg19 coordinates, so we only need to swap coordinates and sort by genomic position.
You can easily do this by using the `dbNSFP_sort.pl` script ([you can find it here](https://raw.githubusercontent.com/pcingola/SnpEff/master/scripts_build/dbNSFP_sort.pl)) by running something like the following command lines:
```
# Set to your downloaded dbNSFP version
version="3.2a"

# Replace coordinates by columns 7 and 8 (hg19 coordinates) and sort by those coordinates
cat dbNSFP${version}_variant.chr* \
    | $HOME/snpEff/scripts_build/dbNSFP_sort.pl 7 8 \
    > dbNSFP${version}_hg19.txt

# Compress and index
bgzip dbNSFP${version}_hg19.txt
tabix -s 1 -b 2 -e 2 dbNSFP${version}_hg19.txt.gz
```
