#!/bin/sh -e

# # Get files and create one file for all chromosomes
# echo Download files
# cd ~/snpEff/data/spombe
# wget ftp://ftp.sanger.ac.uk/pub/yeast/pombe/Chromosome_contigs/chromosome1.contig.embl
# wget ftp://ftp.sanger.ac.uk/pub/yeast/pombe/Chromosome_contigs/chromosome2.contig.embl
# wget ftp://ftp.sanger.ac.uk/pub/yeast/pombe/Chromosome_contigs/chromosome3.contig.embl
# cat chromosome?.contig.embl > genes.embl
# cd -

# Build
echo Build database
java -Xmx1g -jar snpEff.jar build -embl -v spombe 2>&1 | tee spombe.build

# Dump
echo Dump database
java -Xmx1g -jar snpEff.jar dump spombe > spombe.dump
