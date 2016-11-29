#!/bin/sh -e

# Genome name (in SnpEff's config file)
VER="GRCh37"
SUBVER="p13"
ASSEMBLY_ID=""
GENOME="$VER.$SUBVER.RefSeq"

# Path to scripts
SNPEFF_DIR="$HOME/snpEff"
SCRIPTS_DIR="$SNPEFF_DIR/scripts"
SCRIPTS_BUILD_DIR="$SNPEFF_DIR/scripts_build/data/$VER.RefSeq"
DB_DIR="$SNPEFF_DIR/data/$GENOME"

# File names
GFF_REF="ref_$VER.$SUBVER""_top_level.gff3.gz"
CHR_IDS="$DB_DIR/chromosomes.txt"
CHR_IDS_2_NAME="$DB_DIR/chromosomes2name.txt"

# Create dir
mkdir -p $DB_DIR/ORI
cd $DB_DIR/ORI

#---
# Download FASTA files
#---

# echo Downloading reference seuquences
# wget -N "ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/Assembled_chromosomes/seq/hs_ref_$VER.$SUBVER\_*.fa.gz"
# 
# echo Downloading mRNA sequences
# wget -N "ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.*.rna.fna.gz"
# 
# echo Downloading protein sequences
# wget -N "ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.*.protein.faa.gz"
# 
# #---
# # Download GFF data
# #---
# 
# echo "Download GFF files"
# wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/GFF/$GFF_REF
# 
echo "Copying genes.ORI.gff file"
cp $GFF_REF genes.ORI.gff.gz
gunzip -c genes.ORI.gff.gz > genes.ORI.gff

#---
# Download chromosome IDs file
#---

if [ -z "$ASSEMBLY_ID" ]
then
	ASSEMBLY_ID=`cat genes.ORI.gff | head -n 100 | grep "^#" | grep genome-build-accession | cut -f 2 -d :`
	echo "Assembly ID: $ASSEMBLY_ID"
fi

echo "Download chromosome IDs map file"
wget -O - ftp://ftp.ncbi.nlm.nih.gov/genomes/ASSEMBLY_REPORTS/All/$ASSEMBLY_ID.assembly.txt > $CHR_IDS

echo "Create a file mapping chromosome IDs to names: $CHR_IDS_2_NAME"
cat $CHR_IDS | cut -f 1,7 | grep -v "^#" > $CHR_IDS_2_NAME

#---
# Process files
#---

echo "Processing GFF file"
$SCRIPTS_BUILD_DIR/fix_gff.pl genes.ORI.gff $CHR_IDS_2_NAME > $DB_DIR/genes.gff 
gzip $DB_DIR/genes.gff

echo "Processing reference FASTA files"
gunzip -c hs_ref*.fa.gz | $SCRIPTS_BUILD_DIR/fix_fasta.pl $CHR_IDS_2_NAME | $SCRIPTS_DIR/fastaSplit.pl 

echo "Creating and compressing FASTA file $GENOME.fa"
cat chr[1-9].fa chr[1,2][0-9].fa chrX.fa chrY.fa chrMT.fa chr???*.fa | gzip -c > $DB_DIR/$GENOME.fa.gz

echo "Processing protein FASTA files"
gunzip -c human.?.protein.faa.gz | sed "s/^>gi|[0-9]*|ref|\(.*\)|.*/>\1/" > protein.ORI.fa 
cat protein.ORI.fa | $SCRIPTS_BUILD_DIR/fix_fasta_protein_cds.pl protein_id.map.txt > $DB_DIR/protein.fa
gzip $DB_DIR/protein.fa

echo "Processing RNA FASTA files"
gunzip -c human.?.rna.fna.gz | sed "s/^>gi|[0-9]*|ref|\(.*\)|.*/>\1/" > cds.ORI.fa 
cat cds.ORI.fa | $SCRIPTS_BUILD_DIR/fix_fasta_protein_cds.pl ids.map.txt > $DB_DIR/cds.fa
gzip $DB_DIR/cds.fa

echo "Done."
