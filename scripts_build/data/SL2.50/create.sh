#!/bin/sh

# Download  FASTA
#wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG2.4_release/S_lycopersicum_chromosomes.2.50.fa.gz
#wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG2.4_release/ITAG2.4_cds.fasta
#wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG2.4_release/ITAG2.4_proteins.fasta

# Download GFF3
# wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG2.4_release/ITAG2.4_gene_models.gff3

# Create genes.gff
echo Decompressing the file
cp ITAG2.4_gene_models.gff3 genes.gff

echo Fixing start-end problem
cat genes.gff | ./fixStartEnd.pl | ./removeExonFrame.pl > g
mv g genes.gff

# Append FASTA Sequences
echo Append FASTA
echo "###" >> genes.gff
echo "##FASTA" >> genes.gff
zcat S_lycopersicum_chromosomes.2.50.fa.gz >> genes.gff

echo Create CDS file
cat ITAG2.4_cds.fasta | sed "s/^>/>mRNA:/" > cds.fa

echo Create Proteins file
cat ITAG2.4_proteins.fasta | sed "s/^>/>mRNA:/" > protein.fa

