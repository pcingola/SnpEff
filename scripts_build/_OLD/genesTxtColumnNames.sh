#!/bin/sh

#-------------------------------------------------------------------------------
# Convert the gene names in order to be used in an R script
#
# Usage: cat snpEff_genes.txt | ./scripts/genesTxtColumnNames.sh > genes.txt
#
# Once in R, you can:
#	- Load this table:
#			data <- read.csv("genes.txt", sep= "\t", header=TRUE);
#
#	- Access the data:
#			data$countINTRON
#
#	- Add missing or empty columns:
#			if( is.null(data$countINTRON ) { data$countINTRON <- 0 * (1:length(data$geneId) ); }
#
#																Pablo Cingolani
#-------------------------------------------------------------------------------

cat  \
    | grep -v "^# The following"\
    | sed "s/Bases affected (/bases/g" \
    | sed "s/Length (/len/g"  \
    | sed "s/Count (/count/g" \
    | sed "s/Total score (/score/g" \
    | sed "s/)//g" \
    | sed "s/#GeneId/geneId/" \
    | sed "s/GeneName/geneName/" \
    | sed "s/BioType/bioType/" \
    | sed "s/_PRIME//g" \
    | sed "s/SPLICE_SITE_//g" \
    | sed "s/SYNONYMOUS_CODING/SYN/g" \
