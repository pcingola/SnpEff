#!/bin/sh

zcat Araly1_GeneModels_FilteredModels6.gff.gz \
	| sed "s/name/gene_id/" \
	| sed "s/transcriptId \(.*\)/transcript_id \"\1\";/" \
	| sed "s/exonNumber \(.*\)/exon_number \"\1\";/" \
	> g1

./convert.pl g1 > genes.gtf
rm -vf g1

