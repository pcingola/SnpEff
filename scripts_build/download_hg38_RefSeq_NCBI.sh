#!/bin/sh -e

GFF_REF="ref_GRCh38.p2_top_level.gff3.gz"
GFF_ALT="alt_CHM1_1.1_top_level.gff3.gz"
CHR_IDS="chromosomes.txt"
CHR_IDS_2_NAME="chromosomes2name.txt"

#---
# Download GFF data
#---
echo "Download GFF files"
# wget ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/GFF/$GFF_REF
# wget ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/GFF/$GFF_ALT

echo "Build genes.gff file"
# zcat $GFF_REF $GFF_ALT > genes.ORI.gff

#---
# Download chromosome IDs file
#---
ASSEMBLY_ID=`head -n 100 genes.ORI.gff | grep "^#" | grep genome-build-accession | cut -f 2 -d :`
echo "Assembly ID: $ASSEMBLY_ID"

echo "Download chromosome IDs map file"
# wget -O - ftp://ftp.ncbi.nlm.nih.gov/genomes/ASSEMBLY_REPORTS/All/$ASSEMBLY_ID.assembly.txt > $CHR_IDS

echo "Create a file mapping chromosome IDs to names: $CHR_IDS_2_NAME"
# cat $CHR_IDS | cut -f 1,7 | grep -v "^#" > $CHR_IDS_2_NAME

#---
# Process files
#---
cat genes.ORI.gff | ./x.pl chromosomes2name.txt > genes.gff 

#---
# Build database
#---
