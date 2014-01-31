#!/bin/sh

REF=hg19

#---
# Download latest datasets
#---

# # Genome sequence
# wget http://hgdownload.cse.ucsc.edu/goldenPath/$REF/bigZips/chromFa.tar.gz
# 
# # Protein sequences
# wget ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.protein.faa.gz
# 
# # CDS sequences
# wget ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.rna.fna.gz
# 
# # RefLink
# wget http://hgdownload.cse.ucsc.edu/goldenPath/$REF/database/refLink.txt.gz
# 
# #---
# # Create files
# #---
# gunzip refLink.txt.gz
# 
# # Protein fasta
# zcat human.protein.faa.gz | ../../scripts/proteinFasta2NM.pl refLink.txt > protein.fa
# gzip protein.fa
# 
# # CDS fasta
# zcat human.rna.fna.gz | sed "s/^>gi|[0-9]*|ref|\(.*\)\..*|.*/>\1/" > cds.fa 
# gzip cds.fa

# Chromosome fasta
#rm -rvf chr
#mkdir chr 
cd chr
#tar -xvzf ../chromFa.tar.gz

FASTA=../$REF.fa
echo Creating FASTA file
rm -vf $FASTA
cat chr[1-9].fa	>> $FASTA
cat chr??.fa	>> $FASTA
cat chr[A-Z].fa	>> $FASTA
cat chr???*.fa	>> $FASTA

cd -

# # Compress genome file
# $HOME/tools/pigz/pigz hg19.fa
# cp hg19.fa.gz $HOME/snpEff/data/genomes/
