#!/bin/sh

# # Test, only first columns
# #zcat GTEx_Analysis_RNA-seq_RNA-SeQCv1.1.8_gene_rpkm__Pilot_2013_01_31.gct.gz \
# #	| cut -f 1-20 \
# #	| grep -e ^Name -e ^ENS \
# #	> gtex.txt

cat GTEx_Analysis_2014-01-17_RNA-seq_RNA-SeQCv1.1.8_gene_rpkm.gct \
	| grep -e ^Name -e ^ENS \
	> gtex.txt
