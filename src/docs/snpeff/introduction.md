# SnpEff

**SnpEff** is a variant annotation and effect prediction tool.
It annotates and predicts the effects of genetic variants (such as amino acid changes).

### Download &amp; Install

Download and installing SnpEff it pretty easy, take a look at the [download page](../download.md).

### Building from source

Take a look at the ["Source code"](../download.md#source-code) section.

### SnpEff Summary

A typical SnpEff use case would be:

* Input:
    The inputs are predicted variants (SNPs, insertions, deletions and MNPs).
    The input file is usually obtained as a result of a sequencing experiment, and it is usually in variant call format (VCF).
* Output:
    SnpEff analyzes the input variants.
    It annotates the variants and calculates the effects they produce on known genes (e.g. amino acid changes).
    A list of effects and annotations that SnpEff can calculate can be found [here](inputoutput.md#effect-prediction-details).

**Variants**

By genetic variant we mean difference between a genome and a "reference" genome.
As an example, imagine we are sequencing a "sample".
Here "sample" can mean anything that you are interested in studying, from a cell culture, to a mouse or a cancer patient.

It is a standard procedure to compare your sample sequences against the corresponding "reference genome".
For instance you may compare the cancer patient genome against the "reference genome".

In a typical sequencing experiment, you will find many places in the genome where your sample differs from the reference genome.
These are called "genomic variants" or just "variants".

Typically, variants are categorized as follows:

Type  | What is means                    | Example
----- | -------------------------------- | -----------------------------
SNP   | Single-Nucleotide Polymorphism   | Reference = 'A', Sample = 'C'
Ins   | Insertion                        | Reference = 'A', Sample = 'AGT'
Del   | Deletion                         | Reference = 'AC', Sample = 'C'
MNP   | Multiple-nucleotide polymorphism | Reference = 'ATA', Sample = 'GTC'
MIXED | Multiple-nucleotide and an InDel | Reference = 'ATA', Sample = 'GTCAGT'

This is not a comprehensive list, it is just to give you an idea.

**Annotations**

So, you have a huge file describing all the differences between your sample and the reference genome.
But you want to know more about these variants than just their genetic coordinates.
E.g.: Are they in a gene? In an exon? Do they change protein coding? Do they cause premature stop codons?

SnpEff can help you answer all these questions.
The process of adding this information about the variants is called "Annotation".

SnpEff provides several degrees of annotations, from simple (e.g. which gene is each variant affecting) to extremely complex annotations
(e.g. will this non-coding variant affect the expression of a gene?).
It should be noted that the more complex the annotations, the more it relies in computational predictions.
Such computational predictions can be incorrect, so results from SnpEff (or any prediction algorithm) cannot be trusted blindly,
they must be analyzed and independently validated by corresponding wet-lab experiments.

### Citing

If you are using SnpEff or SnpSift, please cite our work as shown [here](../index.md#citing-snpeff). Thank you!

### SnpEff Features

The following table shows the main SnpEff features:

Feature                  | Comment
------------------------ | ----------------------
Local install            | SnpEff can be installed in your local computer or servers. <br> Local installations are preferred for processing genomic data. <br> As opposed to remote web-based services, running a program locally has many advantages: <ul><li> There no need to upload huge genomic dataset. </li><li> Processing doesn't depend on availability or processing capacity of remote servers. </li><li> Service continuity: no need to worry if a remote service will be maintained in the future. </li><li> Security and confidentiality issues of uploading data to third party servers are not a problem. </li><li> Avoid legal problems of processing clinical data on "outside" servers. </li></ul>
Multi platform           | SnpEff is written in Java. It runs on Unix / Linux, OS.X and Windows.
Simple installation      | Installation is as simple as downloading a ZIP file and double clicking on it.
Genomes                  | Human genome, as well as all model organisms are supported. <br>Over 2,500 genomes are supported, which includes most mammalian, plant, bacterial and fungal genomes with published genomic data.
Speed                    | SnpEff is really fast. It can annotate up to 1,000,000 variants per minute.
GATK&Galaxy integration  | SnpEff can be easily integrated with [GATK](http://www.broadinstitute.org/gatk/) and [Galaxy](http://galaxyproject.org/) pipelines.
GUI                      | Web based user interface via Galaxy project
Input and Output formats | SnpEff accepts input files in the following format:<ul><li> `VCF` format, which is the de-facto standard for sequencing variants.</li><li>`BED` format: To annotate enrichment experiments (e.g. ChIP-Seq peaks) or other genomic data.</li></ul>
Variants supported       | SnpEff can annotate SNPs, MNPs, insertions and deletions. Support for mixed variants and structural variants is available (although sometimes limited).
Effect supported         | Many effects are calculated: such as SYNONYMOUS_CODING, NON_SYNONYMOUS_CODING, FRAME_SHIFT, STOP_GAINED just to name a few.
Variant impact           | SnpEff provides a simple assessment of the putative impact of the variant (e.g. HIGH, MODERATE or LOW impact).
Cancer tissue analysis   | Somatic vs Germline mutations can be calculated on the fly. This is very useful for the cancer researcher community.
Loss of Function (LOF) assessment       | SnpEff can estimate if a variant is deemed to have a loss of function on the protein.
Nonsense mediate decay (NMD) assessment | Some mutations may cause mRNA to be degraded thus not translated into a protein. <br>NMD analysis marks mutations that are estimated to trigger nonsense mediated decay.
HGVS notation            | SnpEff can provide output in HGVS notation, which is quite popular in clinical and translation research environments.
User annotations         | A user can provide custom annotations (by means of BED files).
Public databases         | SnpEff can annotate using publicly available data from well known databases, for instance:<ul><li>**ENCODE** datasets are supported by SnpEff (by means of BigWig files provided by ENCODE project).</li><li>**Epigenome Roadmap** provides data-sets that can be used with SnpEff.</li><li>**TFBS** Transcription factor binding site predictions can be annotated. Motif data used in this annotations is generates by `Jaspar` and `ENSEBML` projects</li><li>**NextProt** database can be used to annotate protein domains as well as important functional sites in a protein (e.g. phosphorilation site)</li></ul>
Common variants (dbSnp)  | Annotating "common" variants from **dbSnp** and **1,000 Genomes** can be easily done (see `SnpSift annotate`).
Gwas catalog             | Support for GWAS catalog annotations (see `SnpSift gwasCat`)
Conservation scores      | PhastCons conservation score annotations support (see `SnpSift phastCons`)
DbNsfp                   | A comprehensive database providing many annotations and scores, such as: `SIFT`, `Polyphen2` ,`GERP++`, `PhyloP`, `MutationTaster`, `SiPhy`, `Interpro`, `Haploinsufficiency`, etc. (via SnpSift).<br>See `SnpSift dbnsfp` for details.
Non-coding annotations   | Regulatory and non-coding annotations are supported for different tissues and cell lines. Annotations supported include PolII,H3K27ac, H3K4me2, H3K4me3, H3K27me3, CTCF, H3K36me3, just to name a few.
Gene Sets annotations    | Gene sets (MSigDb, GO, BioCarta, KEGG, Reactome, etc.) can be used to annotate via `SnpSift geneSets` command.


###  Databases

In order to produce the annotations, SnpEff requires a database.
We build these databases using information from trusted resources.

!!! info
    By default SnpEff downloads and installs databases automatically (since version 4.0)

Currently, there are pre-built database for over 20,000 reference genomes.
This means that most cases are covered.

In some very rare occasions, people need to build a database for an organism not currently supported (e.g. the genome is not publicly available).
In most cases, this can be done and there is a [section](build_db.md) of this manual teaching how to build your own SnpEff database.

Which databases are supported? You can find out all the supported databases by running the `databases` command:

```
java -jar snpEff.jar databases | less
```

This command shows the database name, genome name and source data (where was the genome reference data obtained from).
Keep in mind that many times I use ENSEMBL reference genomes, so the name would be `GRCh37` instead of `hg19`, or `GRCm38` instead
of `mm10`, and so on.

**Example: Finding a database:** So, let's say you want to find out the name of the latest mouse (Mus.Musculus) database.
You can runs something like this:

```
java -jar snpEff.jar databases | grep -i musculus

129S1_SvImJ_v1.99                                           	Mus_musculus_129s1svimj                                     	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_129S1_SvImJ_v1.99.zip
AKR_J_v1.99                                                 	Mus_musculus_akrj                                           	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_AKR_J_v1.99.zip
A_J_v1.99                                                   	Mus_musculus_aj                                             	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_A_J_v1.99.zip
BALB_cJ_v1.99                                               	Mus_musculus_balbcj                                         	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_BALB_cJ_v1.99.zip
C3H_HeJ_v1.99                                               	Mus_musculus_c3hhej                                         	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_C3H_HeJ_v1.99.zip
C57BL_6NJ_v1.99                                             	Mus_musculus_c57bl6nj                                       	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_C57BL_6NJ_v1.99.zip
CAST_EiJ_v1.99                                              	Mus_musculus_casteij                                        	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_CAST_EiJ_v1.99.zip
CBA_J_v1.99                                                 	Mus_musculus_cbaj                                           	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_CBA_J_v1.99.zip
DBA_2J_v1.99                                                	Mus_musculus_dba2j                                          	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_DBA_2J_v1.99.zip
FVB_NJ_v1.99                                                	Mus_musculus_fvbnj                                          	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_FVB_NJ_v1.99.zip
GRCm38.75                                                   	Mus_musculus                                                	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_GRCm38.75.zip
GRCm38.99                                                   	Mus_musculus                                                	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_GRCm38.99.zip
LP_J_v1.99                                                  	Mus_musculus_lpj                                            	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_LP_J_v1.99.zip
NOD_ShiLtJ_v1.99                                            	Mus_musculus_nodshiltj                                      	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_NOD_ShiLtJ_v1.99.zip
NZO_HlLtJ_v1.99                                             	Mus_musculus_nzohlltj                                       	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_NZO_HlLtJ_v1.99.zip
PWK_PhJ_v1.99                                               	Mus_musculus_pwkphj                                         	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_PWK_PhJ_v1.99.zip
WSB_EiJ_v1.99                                               	Mus_musculus_wsbeij                                         	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_WSB_EiJ_v1.99.zip
testMm37.61                                                 	Mus_musculus                                                	          	                              	https://snpeff.blob.core.windows.net/databases/v5_0/snpEff_v5_0_testMm37.61.zip
```

At the time of writing this, you have 10 options (obviously this will change in the future).
Some are databases are GRCm version 37 (i.e. mm9) and some are version 38 (i.e. mm10).
Since it is generally better to use the latest release, you should probably pick `GRCm38.74`.
Again, this is an example of the version numbers at the time of writing this paragraph, in the future there will be other releases and you
should update to the corresponding version.

**Unsupported reference genomes:** If your reference genome of interest is not supported yet (i.e. there is no database available),
you can build a database yourself (see [Building databases](build_db.md)).
If you have problems adding you own organism, send the issue to SnpEff repository and I'll do my best to help you out.
