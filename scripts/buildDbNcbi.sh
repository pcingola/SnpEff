#!/bin/sh -e

#-------------------------------------------------------------------------------
#
# Download a GenBank file form NCBI and build the corresponding SnpEff database
#
# Note: It is assumed to be run form SnpEff's directory and that 
#       the 'data' directory is 'data'
#
# Usage example:
#     $ cd ~/snpEff
#     $ ./scripts/buildDbNcbi.sh CP000724.1
#
#															Pablo Cingolani 2015
#-------------------------------------------------------------------------------

#---
# Command line arguments
#---
ID=$1

if [ -z "$ID" ]
then
	echo "Usage: $0 ncbi_genbank_accession"
	exit 1
fi

DIR="data/$ID"
GENE_FILE="$DIR/genes.gbk"

#---
# Download data
#---

# Create db directory
mkdir -p "$DIR" >/dev/null 2>&1 || true

# Download GenBank file
echo "Downloading genome $ID"
curl "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=nucleotide&id=$ID&rettype=gbwithparts" > $GENE_FILE

#---
# Build database
#---

# Add entry to config file
echo "$ID.genome : $ID" >> snpEff.config

# Build database
java -jar snpEff.jar build -v $ID

