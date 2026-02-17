# SnpSift Annotate

Annotate using fields from another VCF file (e.g. dbSnp, ClinVar, ExAC, etc.).

### Typical usage

This is typically used to annotate IDs and INFO fields from a 'database' VCF file (e.g. dbSnp).
Here is an example:

    java -jar SnpSift.jar annotate dbSnp.vcf variants.vcf > variants_annotated.vcf

### Annotation strategies

`SnpSift annotate` uses different strategies depending on the database VCF file format:

* **Uncompressed VCF (plain text):** SnpSift creates a file-based index (`*.sidx`) for the database. The index is saved next to the database file and reused on subsequent runs if the database has not changed. Both the database and the input VCF files should be sorted by position.
* **Compressed with Tabix index (bgzip + .tbi):** SnpSift uses the tabix index for fast random-access lookups. The database must be compressed with `bgzip` and indexed with `tabix`.

**Note:** Compressed VCF files without a tabix index are not supported and will produce an error. You must either provide a tabix index or use an uncompressed VCF.

### Default behavior

* By default, both the ID field and ALL INFO fields from the database are added to matching entries.
* By default, variant matching requires chromosome, position, REF, and ALT to match (case-insensitive for REF/ALT).
* If a variant from the input VCF has no match in the database, it is left unchanged.
* Database IDs are appended to the input entry's ID field (duplicates are removed).
* For multi-allelic sites, each ALT allele is queried independently. Per-allele INFO fields (Number=A in the VCF header) get the correct value for each specific allele.

---

## Command Options

**Database selection:**

- `database.vcf` : Use this VCF file as the annotation database (positional argument). Can be bgzipped and tabix-indexed.
- `-dbsnp` : Use the DbSnp database (downloaded automatically if configured).
- `-clinvar` : Use the ClinVar database (downloaded automatically if configured).

**Annotation control:**

- `-id` : Only annotate the ID field (no INFO fields). Default: true.
- `-info <list>` : Annotate using only the specified comma-separated list of INFO fields. Default: ALL.
- `-noId` : Do not annotate the ID field.
- `-noInfo` : Do not annotate INFO fields.
- `-exists <tag>` : Add a FLAG INFO field named `<tag>` if the variant exists in the database. No INFO values are transferred, only the presence/absence flag.
- `-a` : Annotate fields even when the database has an empty value (uses '.' for missing). By default, empty fields are skipped.
- `-noAlt` : Match variants by coordinates only (chromosome, position), ignoring REF and ALT alleles.
- `-name <str>` : Prepend `<str>` to all annotated INFO field names. Useful when annotating from multiple databases to avoid field name collisions.

**Method selection (usually auto-detected):**

- `-sorted` : Force the sorted/uncompressed VCF strategy (creates `.sidx` index).
- `-tabix` : Force the tabix strategy.
- `-maxBlockSize <int>` : Set the maximum block size when creating the `.sidx` index (used with `-sorted`).

---

## Examples

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

$ java -jar SnpSift.jar annotate -id dbSnp.vcf test.chr22.vcf
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

$ java -jar SnpSift.jar annotate dbSnp.vcf test.chr22.vcf
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

### Example 3: Annotating specific INFO fields

Annotate only `CLNSIG` and `CLNDN` from ClinVar:

```
java -jar SnpSift.jar annotate -info CLNSIG,CLNDN clinvar.vcf.gz input.vcf > output.vcf
```

### Example 4: Using `-name` prefix with multiple databases

When annotating from multiple databases, use `-name` to avoid field name collisions:

```
java -jar SnpSift.jar annotate -name CLINVAR_ clinvar.vcf.gz input.vcf \
    | java -jar SnpSift.jar annotate -name DBSNP_ dbSnp.vcf.gz > output.vcf
```

### Example 5: Checking variant existence

Add a `DB` flag to variants found in dbSnp (without adding any INFO fields):

```
java -jar SnpSift.jar annotate -exists DB -noInfo dbSnp.vcf.gz input.vcf > output.vcf
```

### Example 6: Position-only matching

Match by coordinates only, ignoring REF and ALT alleles:

```
java -jar SnpSift.jar annotate -noAlt database.vcf input.vcf > output.vcf
```
