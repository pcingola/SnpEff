#!/bin/sh

gunzip -c ORI/parastrongyloides_trichosuri.PRJEB515.WBPS3.annotations.gff3.gz \
	| sed "s/gene://" \
	| sed "s/transcript://" \
	| sed "s/exon://" \
	| sed "s/cds://" \
	> genes.gff

echo "##FASTA" >> genes.gff

gunzip -c ORI/parastrongyloides_trichosuri.PRJEB515.WBPS3.genomic.fa.gz >> genes.gff

cp ORI/parastrongyloides_trichosuri.PRJEB515.WBPS3.CDS_transcripts.fa.gz cds.fa.gz
cp ORI/parastrongyloides_trichosuri.PRJEB515.WBPS3.protein.fa.gz protein.fa.gz
