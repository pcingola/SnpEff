#!/bin/sh

cat NC_002516.2.gff | sed "s/CDS/exon/" > genes.gff 
echo "##FASTA" >> genes.gff
cat NC_002516.2.fna >> genes.gff 

