#!/bin/bash -eu
set -o pipefail

# ClinVar
mkdir $HOME/snpEff/data/GRCh38/clinvar
cd $HOME/snpEff/data/GRCh38/clinvar
wget https://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf_GRCh38/clinvar.vcf.gz

# DbSnp
mkdir $HOME/snpEff/data/GRCh38/dbSnp
cd $HOME/snpEff/data/GRCh38/dbSnp
wget https://ftp.ncbi.nih.gov/snp/archive/b154/VCF/GCF_000001405.38 -O dbSnp.vcf.gz

# Cosmic (Copied to tmp bucket in AWS)
mkdir $HOME/snpEff/data/GRCh38/cosmic
cd $HOME/snpEff/data/GRCh38/cosmic
aws s3 cp $S3_TMP/cosmic/cosmic-v92.vcf.gz

# GnomAD
mkdir $HOME/snpEff/data/GRCh38/gnomAD
cd $HOME/snpEff/data/GRCh38/gnomAD
aws s3 sync s3://gnomad-public-us-east-1/release/4.1/vcf/joint/ .