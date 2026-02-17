# SnpSift GWAS Catalog

Annotate variants with information from the [GWAS Catalog](https://www.ebi.ac.uk/gwas/).

### Usage

```
java -jar SnpSift.jar gwasCat -genome GRCh37 [-db path/to/gwascatalog.txt] [file.vcf] > output.vcf
```

The command requires a genome version (e.g. `-genome GRCh37`) and a GWAS Catalog database file.
The database can be specified via `-db` or configured in `snpEff.config`.
Default VCF input is STDIN.

### Database

You need the GWAS Catalog file in the "alternative" tab-delimited format, which can be downloaded from [EBI](https://www.ebi.ac.uk/gwas/api/search/downloads/alternative).

If the database path is configured in `snpEff.config` (e.g. `database.gwascatalog.GRCh37`), SnpSift will find it automatically.
Otherwise, specify the path with `-db`.

### How it works

The GWAS Catalog is loaded into memory and indexed by chromosomal position using an interval tree.
For each variant in the VCF file, the command matches by **exact genomic position** (chromosome and position), not by rsID.

Chromosome names are normalized (e.g. 'chr1' and '1' are treated the same).
Large structural variants are skipped during annotation.

### Output fields

The command adds up to five INFO fields to matching VCF entries:

Field | Description
----- | -----------
`GWASCAT_TRAIT` | Associated trait (e.g. "Body_mass_index")
`GWASCAT_P_VALUE` | p-value from the association study
`GWASCAT_OR_BETA` | Odds ratio or Beta coefficient (only added when > 0)
`GWASCAT_REPORTED_GENE` | Gene reported in the original study
`GWASCAT_PUBMED_ID` | PubMed ID of the original paper

When multiple GWAS Catalog entries match a variant, values are comma-separated.
All VCF entries are output (both annotated and non-annotated).

### Example

```
$ java -jar SnpSift.jar gwasCat -genome GRCh37 -db gwascatalog.txt test.vcf > test.gwas.vcf

$ grep GWASCAT test.gwas.vcf | head -5
1   1005806 rs3934834   C   T   .   PASS    AF=0.091;GWASCAT_TRAIT=Body_mass_index;GWASCAT_P_VALUE=1.0E-6;GWASCAT_REPORTED_GENE=AGRN;GWASCAT_PUBMED_ID=25673413
1   2069172 rs425277    C   T   .   PASS    AF=0.400;GWASCAT_TRAIT=Height;GWASCAT_P_VALUE=2.0E-8;GWASCAT_REPORTED_GENE=PRKCZ;GWASCAT_PUBMED_ID=20881960
1   2392648 rs2477686   G   C   .   PASS    AF=0.745;GWASCAT_TRAIT=Non_obstructive_azoospermia;GWASCAT_P_VALUE=3.0E-7;GWASCAT_REPORTED_GENE=MORN1;GWASCAT_PUBMED_ID=24532628
1   2513216 rs734999    C   T   .   PASS    AF=0.547;GWASCAT_TRAIT=Ulcerative_colitis;GWASCAT_P_VALUE=5.0E-13;GWASCAT_REPORTED_GENE=TNFRSF14;GWASCAT_PUBMED_ID=21297633
1   3083712 rs2651899   T   C   .   PASS    AF=0.467;GWASCAT_TRAIT=Migraine;GWASCAT_P_VALUE=1.0E-11;GWASCAT_REPORTED_GENE=PRDM16;GWASCAT_PUBMED_ID=23793025
```
