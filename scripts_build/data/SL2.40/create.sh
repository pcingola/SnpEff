#!/bin/sh

# wget ftp://ftp.solgenomics.net/genomes/Solanum_lycopersicum/wgs/assembly/build_2.40/S_lycopersicum_chromosomes.2.40.fa.gz

# GFF has to be downloaded manually from 
#
# http://solgenomics.net/itag/release/2.3/list_files#
#          File ITAG2.3_gene_models.gff3
#
#

# Create genes.gff
echo Decompressing the file
cp ITAG2.3_gene_models.gff3.gz genes.gff.gz
rm -f genes.gff
gunzip genes.gff.gz 

echo Fixing start-end problem
cat genes.gff | ./fixStartEnd.pl > g
mv g genes.gff

# Append FASTA Sequences
echo "###" >> genes.gff
echo "##FASTA" >> genes.gff
zcat S_lycopersicum_chromosomes.2.40.fa.gz >> genes.gff

