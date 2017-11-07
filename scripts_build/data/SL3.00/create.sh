#!/bin/sh

zcat="gunzip -c"

# Download  FASTA
#wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG3.2_release/S_lycopersicum_chromosomes.3.00.fa.gz
#wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG3.2_release/ITAG3.2_cds.fasta
#wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG3.2_release/ITAG3.2_proteins.fasta

# Download GFF3
# wget ftp://ftp.solgenomics.net/tomato_genome/annotation/ITAG3.2_release/ITAG3.2_gene_models.gff

# Create genes.gff
echo Fixing start-end problem
$zcat ORI/ITAG3.2_gene_models.gff.gz | ./fixStartEnd.pl | ./removeExonFrame.pl > genes.gff

# Append FASTA Sequences
echo Sequence FASTA
cp ORI/S_lycopersicum_chromosomes.3.00.fa.gz sequences.fa.gz

echo Create CDS file
$zcat ORI/ITAG3.2_cds.fasta.gz | sed "s/^>/>mRNA:/" > cds.fa

echo Create Proteins file
$zcat ORI/ITAG3.2_proteins.fasta.gz | sed "s/^>/>mRNA:/" > protein.fa

gzip cds.fa protein.fa genes.gff
