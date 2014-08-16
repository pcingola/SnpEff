#!/bin/sh -e

# Update dbSnp
cd db/dbSnp/
wget ftp://ftp.ncbi.nih.gov/snp/organisms/human_9606/VCF/00-All.vcf.gz
gunzip 00-All.vcf.gz
mv 00-All.vcf dbSnp.vcf
cd -

