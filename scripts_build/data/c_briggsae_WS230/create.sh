#!/bin/sh -e

zcat c_briggsae.WS230.annotations.gff3.gz \
	| grep -v SNP \
	| grep -v translated_nucleotide_match \
	| grep -v repeat_region \
	| grep -v inverted_repeat \
	| grep -v tandem_repeat \
	| grep -v nucleotide_match \
	> genes.gff

# Add fasta sequence
echo "###" >> genes.gff
echo "##FASTA" >> genes.gff
zcat c_briggsae.WS230.genomic.fa.gz >> genes.gff
