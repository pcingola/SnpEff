#!/bin/sh

cat NC_009444.1.gff | sed "s/CDS/exon/" > genes.gff 
echo "##FASTA" >> genes.gff
cat NC_009444.1.fna >> genes.gff 

