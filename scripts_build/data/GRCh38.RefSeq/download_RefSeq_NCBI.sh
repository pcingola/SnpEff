#!/bin/bash -eu
set -o pipefail

PROGRAM_DIR=$(cd $(dirname $0); pwd -P)

# Reference
# https://ftp.ncbi.nih.gov/genomes/refseq/vertebrate_mammalian/Homo_sapiens/annotation_releases/109.20200815/

VER="GRCh38"

# Previous version
SUBVER="p13"
ASSEMBLY_ID="GCF_000001405.39"
RELEASE="109.20200815"
RELEASE="109.20211119"

# Latest version
SUBVER="p14"
ASSEMBLY_ID="GCF_000001405.40"
RELEASE="110"

GENOME="$VER.$SUBVER"

# Path to scripts
SNPEFF_DIR="$HOME/snpEff"
SCRIPTS_DIR="$SNPEFF_DIR/scripts"
SCRIPTS_BUILD_DIR="$PROGRAM_DIR"
DB_DIR="$SNPEFF_DIR/data/$GENOME"

# URLs
HTTP_URL="http://ftp.ncbi.nih.gov/"
HTTP_DIR="$HTTP_URL/genomes/refseq/vertebrate_mammalian/Homo_sapiens/all_assembly_versions/${ASSEMBLY_ID}_${VER}.${SUBVER}"

URL_BASE="$HTTP_DIR/${ASSEMBLY_ID}_${VER}.${SUBVER}_"
URL_SEQ="${URL_BASE}genomic.fna.gz"
URL_RNASEQ="${URL_BASE}rna.fna.gz"
URL_PROTSEQ="${URL_BASE}protein.faa.gz"
URL_GTF="${URL_BASE}genomic.gtf.gz"
URL_ASSEMBLY_REPORT="${URL_BASE}assembly_report.txt"

# Files
CHR_IDS_2_NAME="$DB_DIR/ORI/chromosomes2name.txt"
GTF_ORI="$DB_DIR/ORI/${ASSEMBLY_ID}_${VER}.${SUBVER}_genomic.gtf.gz"
FASTQ_ORI="$DB_DIR/ORI/${ASSEMBLY_ID}_${VER}.${SUBVER}_genomic.fna.gz"
PROT_FASTA_ORI="$DB_DIR/ORI/${ASSEMBLY_ID}_${VER}.${SUBVER}_protein.faa.gz"
RNA_FASTA_ORI="$DB_DIR/ORI/${ASSEMBLY_ID}_${VER}.${SUBVER}_rna.fna.gz"

#---
# Download files
#---

# Create dir
mkdir -p $DB_DIR/ORI
cd $DB_DIR/ORI

echo "Download reference genome"
wget -N "$URL_SEQ"

echo Downloading mRNA sequences
wget -N "$URL_RNASEQ"

echo Downloading protein sequences
wget -N "$URL_PROTSEQ"

echo "Download GTF files"
wget -N "$URL_GTF"

echo "Download chromosome IDs map file"
wget -N "$URL_ASSEMBLY_REPORT"

cd ..

#---
# Chromosome IDs file
#---

echo "Create a file mapping chromosome IDs to names: $CHR_IDS_2_NAME"
cat "ORI/${ASSEMBLY_ID}_${VER}.${SUBVER}_assembly_report.txt" | cut -f 1,7 | grep -v "^#" > "$CHR_IDS_2_NAME"

#---
# Process files
#---

echo "Processing GFF file"
$SCRIPTS_BUILD_DIR/fix_gtf.pl "$GTF_ORI" "$CHR_IDS_2_NAME" > "$DB_DIR/genes.gtf"

echo "Processing reference FASTA files"
gunzip -c "$FASTQ_ORI" | $SCRIPTS_BUILD_DIR/fix_fasta.pl "$CHR_IDS_2_NAME" > "$DB_DIR/sequences.fa"

echo "Processing protein FASTA files"
gunzip -c "$PROT_FASTA_ORI" | $SCRIPTS_BUILD_DIR/fix_fasta_protein_cds.pl protein_id.map.txt > "$DB_DIR/protein.fa"

echo "Processing RNA FASTA files"
gunzip -c "$RNA_FASTA_ORI" | perl -pe 's/^>(\S+).*/>$1/' > "$DB_DIR/mrna.fa"

#---
# Compress
#---
pigz -v genes.gtf mrna.fa protein.fa protein_id.map.txt sequences.fa

echo "Done."
