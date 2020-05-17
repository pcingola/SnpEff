# SnpEff&SnpSift

Genomic variant annotations and functional effect prediction toolbox.

[Download SnpEff](https://sourceforge.net/projects/snpeff/files/snpEff_latest_core.zip/download){: .button}

Important: This version implements the [VCF annotation standard 'ANN' field](adds/VCFannotationformat_v1.0.pdf).

Latest version 4.3T (2017-11-24)

Requires Java 1.8


## [ClinEff](http://www.dnaminer.com/)

[Professional version](http://www.dnaminer.com/) of SnpEff & SnpSift suites. [ClinEff](http://www.dnaminer.com/) is considered more stable thus suitable for Clinical and Production operations, whereas SnpEff/SnpSfit is designed for Research and Academic usage.

Features:

* Compliance support (CLIA and CAP)
* Long Term Support
* Prioritized bug fixes and feature development
* Customized databases and annotation pipelines
* Integration with open, private and proprietary databases
* Privacy: Tickets, issues, pipeline-specific analysis


## SnpEff

Genetic variant annotation and functional effect prediction toolbox. It annotates and predicts the effects of genetic variants on genes and proteins (such as amino acid changes).

Features:

* Supports over **38,000 genomes**.
* Standard **ANN** annotation format
* **Cancer** variants analysis
* **GATK** compatible (`-o gatk`)
* **HGVS** notation
*  **Sequence Ontology** standardized terms

[View details](se_annfield.md){: .button}

##  SnpSift

SnpSift annotates genomic variants using databases, filters, and manipulates genomic annotated variants.

Once you annotated your files using SnpEff, you can use SnpSift to help you filter large genomic datasets in order to find the most significant variants for your experiment.

[View details](ss_introduction.md){: .button}

##  Version 4.3

Features:

* Significant improvements in translocations annotations
* Improvements in large structural variant annotations
* Protein-Protein interaction loci annotations (from PDB)

[View details](features.md){: .button}

## Paper & Citing

If you are using SnpEff or SnpSift in an research or academic environment, please cite our [papers](adds/SnpEff_paper.pdf).

[View details](citing.md){: .button}

## Who uses SnpEff?

Users of SnpEff include most major research an academic institutions, as well as pharmaceutical companies and clinical sequencing projects.

[View details](users_of_snpeff.md){: .button}

## Galaxy & GATK

SnpEff is integrated with other tools commonly used in sequencing data analysis pipelines.

Most notably [Galaxy](http://galaxyproject.org/) and [GATK](http://www.broadinstitute.org/gatk/) projects support SnpEff.

[View details](se_integration.md){: .button}

##  In memory of Dr. Xiangyi Lu: [Please donate](xiangyi_lu_donate.md)
![](images/xiangy_small.jpg){: .right}
On October 22, 2017, Xiangyi Lu, a co-author on the SnpEff and SnpSift papers, died of ovarian cancer after a three year struggle.
Douglas Ruden, Xiangyi's husband and senior author on the papers, has requested that a non-mandatory gift of at least $10 for using
SnpEff or SnpSift be donated to WSU to honor Xiangyi Lu. All gifts will go to a newly named fund, the "Xiangyi Lu Graduate Student Fellowship in Bioinformatics Fund."
with the goal of raising $1 million, in order to permanently endow one graduate student research position in bioinformatics every year.
