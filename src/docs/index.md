# SnpEff & SnpSift

Genomic variant annotations, and functional effect prediction toolbox.

[Download](https://snpeff.blob.core.windows.net/versions/snpEff_latest_core.zip){: .md-button .md-button--primary }
[SnpEff Documentation](snpeff/introduction.md){: .md-button .md-button--primary }
[SnpSift Documentation](snpsift/introduction.md){: .md-button .md-button--primary }

Latest version 5.2c (2024-04-09)

## SnpEff

Genetic variant annotation, and functional effect prediction toolbox.
It annotates and predicts the effects of genetic variants on genes and proteins (such as amino acid changes).

Features:

* Supports over **38,000 genomes**.
* Standard **ANN** annotation format
* **Cancer** variants analysis
* **GATK** compatible (`-o gatk`)
* **HGVS** notation
* **Sequence Ontology** standardized terms
* Implements [VCF annotation standard `ANN` field](adds/VCFannotationformat_v1.0.pdf).

[SnpEff Documentation](snpeff/introduction.md){: .md-button .md-button--primary }

##  SnpSift

SnpSift annotates genomic variants using databases, filters, and manipulates genomic annotated variants.

Once you annotated your files using SnpEff, you can use SnpSift to help you filter large genomic datasets in order to find the most significant variants for your experiment.



## Citing SnpEff & SnpSift

If you are using SnpEff or SnpSift in an research or academic environment, please cite our [paper](adds/SnpEff_paper.pdf).

### Citing SnpEff

You can find the paper [here](adds/SnpEff_paper.pdf).

In order to cite SnpEff, please use the following reference:

!!! SnpEff
    "A program for annotating and predicting the effects of single nucleotide polymorphisms, SnpEff: SNPs in the genome of Drosophila melanogaster strain w1118; iso-2; iso-3.", Cingolani P, Platts A, Wang le L, Coon M, Nguyen T, Wang L, Land SJ, Lu X, Ruden DM. Fly (Austin). 2012 Apr-Jun;6(2):80-92.  PMID: 22728672

BibTex entry:
```
@article{cingolani2012program,
title={A program for annotating and predicting the effects of single nucleotide polymorphisms, SnpEff: SNPs in the genome of Drosophila melanogaster strain w1118; iso-2; iso-3},
author={Cingolani, P. and Platts, A. and Coon, M. and Nguyen, T. and Wang, L. and Land, S.J. and Lu, X. and Ruden, D.M.},
    journal={Fly},
    volume={6},
    number={2},
    pages={80-92},
    year={2012}
}
```

### Citing SnpSift

You can find the paper [here](adds/SnpSift_paper.pdf).

In order to cite SnpSift, please use the following reference:

!!! SnpSift
    "Using Drosophila melanogaster as a model for genotoxic chemical mutational studies with a new program, SnpSift", Cingolani, P., et. al., Frontiers in Genetics, 3, 2012.

BibTex entry:
```
@article{cingolani2012using,
    title={Using Drosophila melanogaster as a model for genotoxic chemical mutational studies with a new program, SnpSift},
    author={Cingolani, P. and Patel, V.M. and Coon, M. and Nguyen, T. and Land, S.J. and Ruden, D.M. and Lu, X.},
    journal={Frontiers in Genetics},
    volume={3},
    year={2012},
    publisher={Frontiers Media SA}
}
```


## Microsoft Genomics 

<img width=108 src="https://img-prod-cms-rt-microsoft-com.akamaized.net/cms/api/am/imageFileData/RE1Mu3b?ver=5c31">

All SnpEff & SnpSift genomic databases are kindly hosted by Microsoft Genomics and Azure

## Galaxy & GATK

SnpEff is integrated with other tools commonly used in sequencing data analysis pipelines.

Most notably [Galaxy](http://galaxyproject.org/) and [GATK](http://www.broadinstitute.org/gatk/) projects support SnpEff.

[View details](snpeff/integration.md){: .button}

##  In memory of Dr. Xiangyi Lu

[Please donate](xiangyi_lu_donate.md)

![](images/xiangy_small.jpg){: .right}

On October 22, 2017, Xiangyi Lu, a co-author on the SnpEff and SnpSift papers, died of ovarian cancer after a three year struggle.

Douglas Ruden, Xiangyi's husband and senior author on the papers, has requested that a non-mandatory gift of at least $10 for using
SnpEff or SnpSift be donated to WSU to honor Xiangyi Lu. All gifts will go to a fund named "The Xiangyi Lu Graduate Student Fellowship in Bioinformatics Fund."
with the goal of raising $1 million, in order to permanently endow one graduate student research position in bioinformatics every year.
