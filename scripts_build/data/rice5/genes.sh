#!/bin/sh

cat ORI/build5_locus.gff3 > genes.gff
cat ORI/build5_genes.gff3 | ./mRnaAddParent.pl >> genes.gff
echo "###" >> genes.gff
echo "##FASTA" >> genes.gff
cat ORI/IRGSPb5.fa >> genes.gff
