#!/bin/sh

cat NC_012660.1.gff | sed "s/CDS/exon/" > genes.gff 
echo "##FASTA" >> genes.gff
cat NC_012660.1.fna >> genes.gff 

