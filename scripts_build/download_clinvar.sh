#!/bin/sh -e

# Update ClinVar
cd db/clinVar
wget ftp://ftp.ncbi.nlm.nih.gov/pub/clinvar/vcf/clinvar_00-latest.vcf.gz
gunzip clinvar_00-latest.vcf.gz 
cd -

