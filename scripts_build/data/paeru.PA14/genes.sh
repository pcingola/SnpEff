#!/bin/sh

cat NC_008463.1.gff | sed "s/CDS/exon/" > genes.gff 
echo "##FASTA" >> genes.gff
cat NC_008463.1.fna >> genes.gff 

