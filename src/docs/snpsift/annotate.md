# SnpSift Annotate

Annotate using fields from another VCF file (e.g. dbSnp, 1000 Genomes projects, ClinVar, ExAC, etc.).

### Typical usage

This is typically used to annotate IDs and INFO fields from a 'database' VCF file (e.g. dbSnp).
Here is an example:

    java -jar SnpSift.jar annotate dbSnp132.vcf variants.vcf > variants_annotated.vcf

Important: `SnpSift annotate` command has different strategies depending on the input VCF file:

* **Uncomressed VCF** If the file is not compressed, it created an index in memory to optimize search. This assumes that both the database and the input VCF files are sorted by position, since it is required by the VCF standard (chromosome sort order can differ between files).
* **Compressed, Tabix indexed** It uses the tabix index to speed up annotations.
* **Compressed, NOT Tabix indexed** It loads the entire 'database' VCF file into memory, which may be slow or even impractical for large 'database' VCF files.  This allows to annotate using unsorted VCF files.

Note:

* By default it adds ALL database INFO fields.
* You can use the `-info` command line option if you only want select only a subset of fields from db.vcf file.
* You can use the `-id` command line option if you only want to add ID fields (no INFO fields will be added).
* Using the `-exists` command line option, you can annotate entries that exists in the 'database' file.

!!! info
    DbSnp in VCF format can be downloaded [here](ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/00-All.vcf.gz) (GRCh38 coordinates).
    For other versions, check [this link](ftp://ftp.ncbi.nih.gov/snp/organisms/).

### Example 1: Annotating `ID` from dbSnp

```
$ cat test.chr22.vcf
#CHROM  POS         ID           REF  ALT  QUAL   FILTER  INFO
22      16157571    .            T    G    0.0    FAIL    NS=53
22      16346045    .            T    C    0.0    FAIL    NS=244
22      16350245    .            C    A    0.0    FAIL    NS=192
22      17054103    .            G    A    0.0    PASS    NS=404
22      17071906    .            A    T    0.0    PASS    NS=464
22      17072347    .            C    T    0.0    PASS    NS=464
22      17072394    .            C    G    0.0    PASS    NS=463
22      17072411    .            G    T    0.0    PASS    NS=464

$ java -jar SnpSift.jar annotate -id db/dbSnp/dbSnp137.20120616.vcf test.chr22.vcf
#CHROM  POS         ID           REF  ALT  QUAL   FILTER  INFO
22      16157571    .            T    G    0.0    FAIL    NS=53
22      16346045    rs56234788   T    C    0.0    FAIL    NS=244
22      16350245    rs2905295    C    A    0.0    FAIL    NS=192
22      17054103    rs4008588    G    A    0.0    PASS    NS=404
22      17071906    .            A    T    0.0    PASS    NS=464
22      17072347    rs139948519  C    T    0.0    PASS    NS=464
22      17072394    .            C    G    0.0    PASS    NS=463
22      17072411    rs41277596   G    T    0.0    PASS    NS=464
```

### Example 2: Annotating `ID` and all `INFO` fields from dbSnp
(VCF headers not shown for brevity):
```
$ cat test.chr22.vcf
#CHROM  POS         ID           REF  ALT  QUAL   FILTER  INFO
22      16157571    .            T    G    0.0    FAIL    NS=53
22      16346045    .            T    C    0.0    FAIL    NS=244
22      16350245    .            C    A    0.0    FAIL    NS=192
22      17054103    .            G    A    0.0    PASS    NS=404
22      17071906    .            A    T    0.0    PASS    NS=464
22      17072347    .            C    T    0.0    PASS    NS=464
22      17072394    .            C    G    0.0    PASS    NS=463
22      17072411    .            G    T    0.0    PASS    NS=464

$ java -jar SnpSift.jar annotate db/dbSnp/dbSnp137.20120616.vcf test.chr22.vcf
#CHROM  POS         ID           REF  ALT  QUAL   FILTER  INFO
22      16157571    .            T    G    0.0    FAIL    NS=53
22      16346045    rs56234788   T    C    0.0    FAIL    NS=244;RSPOS=16346045;GMAF=0.162248628884826;dbSNPBuildID=129;SSR=0;SAO=0;VP=050100000000000100000100;WGT=0;VC=SNV;SLO;GNO
22      16350245    rs2905295    C    A    0.0    FAIL    NS=192;RSPOS=16350245;GMAF=0.230804387568556;dbSNPBuildID=101;SSR=1;SAO=0;VP=050000000000000100000140;WGT=0;VC=SNV;GNO
22      17054103    rs4008588    G    A    0.0    PASS    NS=404;RSPOS=17054103;GMAF=0.123400365630713;dbSNPBuildID=108;SSR=0;SAO=0;VP=050100000000070010000100;WGT=0;VC=SNV;SLO;VLD;G5A;G5;KGPilot123
22      17071906    .            A    T    0.0    PASS    NS=464
22      17072347    rs139948519  C    T    0.0    PASS    NS=464;RSPOS=17072347;dbSNPBuildID=134;SSR=0;SAO=0;VP=050200000004040010000100;WGT=0;VC=SNV;S3D;ASP;VLD;KGPilot123
22      17072394    .            C    G    0.0    PASS    NS=463
22      17072411    rs41277596   G    T    0.0    PASS    NS=464;RSPOS=17072411;GMAF=0.00411334552102377;dbSNPBuildID=127;SSR=0;SAO=0;VP=050200000008040010000100;GENEINFO=CCT8L2:150160;WGT=0;VC=SNV;S3D;CFL;VLD;KGPilot123
```
