# SnpSift GeneSets

Annotating GeneSets, such as Gene Ontology (GO), KEGG, Reactome, etc.; can be quite useful to find significant variants.

Gene set annotations can be added to a SnpEff annotated file using `SnpSift geneSets` command.
The VCF file must be annotated using `SnpEff` before performing Gene Sets annotations, because we must know which gene the variant affects.

### Usage

```
java -jar SnpSift.jar geneSets [-v] msigdb.gmt file.vcf > output.vcf
```

The command takes two positional arguments: the GMT file and the VCF file. Unlike most SnpSift commands, STDIN is not supported for the VCF input.

!!! info
    You can download MSigDb from [Broad Institute](https://www.gsea-msigdb.org/gsea/msigdb/)

### Input format

The gene sets file must be in GMT (Gene Matrix Transposed) format: a tab-separated file where each line defines one gene set.
Each line has the format: `gene_set_name <TAB> description <TAB> gene1 <TAB> gene2 <TAB> ...`

MSigDb GMT files follow this format and can be used directly.

### How it works

For each variant, the command looks at the gene names from SnpEff annotations (the `GENE` field in `ANN`).
For each gene, it finds all gene sets that contain that gene and adds them to the `MSigDb` INFO field (comma-separated, sorted alphabetically).

!!! warning
    Gene names in the GMT file must exactly match the gene names used by SnpEff.
    MSigDb typically uses HGNC gene symbols (e.g. `BRCA1`).
    If your SnpEff database uses different gene identifiers (e.g. Ensembl gene IDs), the lookup will fail silently.
    Make sure your SnpEff annotations and GMT file use the same gene naming convention.

### Example

```
$ java -jar SnpSift.jar geneSets -v db/msigDb/msigdb.v5.1.symbols.gmt test.ann.vcf > test.geneSets.vcf
00:00:00.000	Reading MSigDb from file: 'db/msigDb/msigdb.v5.1.symbols.gmt'
00:00:01.168	Done. Total:
        8513 gene sets
        31847 genes
00:00:01.168	Annotating variants from: 'test.ann.vcf'
00:00:01.298	Done.
# Summary
#	    gene_set	gene_set_size	variants
#	    ACEVEDO_METHYLATED_IN_LIVER_CANCER_DN	940	8
#	    CHR1P36	504	281
#	    KEGG_OLFACTORY_TRANSDUCTION	389	8
#	    REACTOME_GPCR_DOWNSTREAM_SIGNALING	805	8
#	    REACTOME_OLFACTORY_SIGNALING_PATHWAY	328	8
...
#	    REACTOME_SIGNALING_BY_GPCR	920	8

$ cat test.geneSets.vcf
## INFO=<ID=MSigDb,Number=.,Type=String,Description="Gene set from MSigDB database (GSEA)">
1	69849	.	G	A	454.73	PASS	AC=33;ANN=...;MSigDb=ACEVEDO_METHYLATED_IN_LIVER_CANCER_DN,CHR1P36,KEGG_OLFACTORY_TRANSDUCTION,REACTOME_GPCR_DOWNSTREAM_SIGNALING,REACTOME_OLFACTORY_SIGNALING_PATHWAY,REACTOME_SIGNALING_BY_GPCR
```

The summary (gene set name, size, and number of annotated variants) is printed to STDERR when using `-v`.
