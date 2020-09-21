# SnpSift GeneSets

Annotating GeneSets, such as Gene Ontology (GO), KEGG, Reactome, etc.; can be quite useful to find significant variants.

Gene set annotations can be added to a SnpEff annotated file using `SnpSift geneSets` command.
The VCF file must be annotated using `SnpEff` before performing Gene Sets annotations.
This is because we must know which gene the variant affects).

!!! info
    You can download MSigDb from [Broad Institute](http://www.broadinstitute.org/gsea/msigdb)

Usage example:
```
$ java -jar SnpSift.jar geneSets -v db/msigDb/msigdb.v3.1.symbols.gmt test.ann.vcf > test.eff.geneSets.vcf
00:00:00.000	Reading MSigDb from file: 'db/msigDb/msigdb.v3.1.symbols.gmt'
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

$ cat test.eff.geneSets.vcf
## INFO=<ID=MSigDb,Number=.,Type=String,Description="Gene set from MSigDB database (GSEA)">
1	69849	.	G	A	454.73	PASS	AC=33;EFF=STOP_GAINED(HIGH|NONSENSE|tgG/tgA|W253*|305|OR4F5|protein_coding|CODING|ENST00000335137|1|1);MSigDb=ACEVEDO_METHYLATED_IN_LIVER_CANCER_DN,CHR1P36,KEGG_OLFACTORY_TRANSDUCTION,REACTOME_GPCR_DOWNSTREAM_SIGNALING,REACTOME_OLFACTORY_SIGNALING_PATHWAY,REACTOME_SIGNALING_BY_GPCR
```
