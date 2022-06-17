#!/bin/sh -e

#---
# Convert chromosome names for FASTA files
#
# Original files downloaded from
#   http://ftp.ensembl.org/pub/rapid-release/species/Sciurus_carolinensis/GCA_902686445.2/
#---

# Fixing GFF file. Remove 'gene:' from IDs to make them consistent with CDS and protein fasta files
gunzip -c ORI/Sciurus_carolinensis-GCA_902686445.2-2020_12-genes.gff3.gz \
  | sed 's/gene:\(.*\)$/\1/' \
  | sed 's/transcript:\(.*\)$/\1/' \
  | sed 's/CDS:\(.*\)$/\1/' \
  > genes.gff

# Changing labels in protein fasta file (use transcript ID)
gunzip -c ORI/Sciurus_carolinensis-GCA_902686445.2-2020_12-pep.fa.gz \
  | sed "s/^>.*transcript:\(.*\)*$/>\1/" \
  > protein.fa

# Copy files that do not need modification
cp ORI/Sciurus_carolinensis-GCA_902686445.2-softmasked.fa.gz sequences.fa.gz
cp ORI/Sciurus_carolinensis-GCA_902686445.2-2020_12-cds.fa.gz cds.fa.gz

