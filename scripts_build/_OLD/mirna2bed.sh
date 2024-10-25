#!/bin/sh

zcat db/miRNA/human_predictions*.txt.gz \
	| ./scripts_build/mirna2bed.pl \
	| sort -k1,1 -k2,2g \
	> db/miRNA/human_predictions.bed

# This is for IGV
bgzip db/miRNA/human_predictions.bed
tabix -p bed db/miRNA/human_predictions.bed.gz

# Uncompress for java programs (they choke on bgzip input)
zcat db/miRNA/human_predictions.bed.gz > db/miRNA/human_predictions.bed
