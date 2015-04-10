#!/bin/sh -e

# Genome name (in SnpEff's config file)
GENOME="GRCh38.p2.RefSeq"

# File names
GFF_REF="ref_GRCh38.p2_top_level.gff3.gz"
GFF_ALT="alt_CHM1_1.1_top_level.gff3.gz"
CHR_IDS="chromosomes.txt"
CHR_IDS_2_NAME="chromosomes2name.txt"

# Path to scripts
SNPEFF_DIR="$HOME/snpEff"
SCRIPTS_DIR="$SNPEFF_DIR/scripts"
SCRIPTS_BUILD_DIR="$SNPEFF_DIR/scripts_build"

#---
# Download FASTA files
#---

for chr in alts unlocalized unplaced chr1 chr2 chr3 chr4 chr5 chr6 chr7 chr8 chr9 chr10 chr11 chr12 chr13 chr14 chr15 chr16 chr17 chr18 chr19 chr20 chr21 chr22 chrX chrY chrMT 
do
	echo Downloading chromosome $chr
	wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/Assembled_chromosomes/seq/hs_ref_GRCh38.p2_$chr.fa.gz
done

#---
# Download GFF data
#---

echo "Download GFF files"
wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/GFF/$GFF_REF
wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/GFF/$GFF_ALT

echo "Build genes.gff file"
gunzip -c $GFF_REF $GFF_ALT > genes.ORI.gff
gzip genes.ORI.gff

#---
# Download chromosome IDs file
#---

ASSEMBLY_ID=`zcat genes.ORI.gff.gz | head -n 100 | grep "^#" | grep genome-build-accession | cut -f 2 -d :`
echo "Assembly ID: $ASSEMBLY_ID"

echo "Download chromosome IDs map file"
wget -O - ftp://ftp.ncbi.nlm.nih.gov/genomes/ASSEMBLY_REPORTS/All/$ASSEMBLY_ID.assembly.txt > $CHR_IDS

echo "Create a file mapping chromosome IDs to names: $CHR_IDS_2_NAME"
cat $CHR_IDS | cut -f 1,7 | grep -v "^#" > $CHR_IDS_2_NAME

#---
# Process files
#---

echo "Processing GFF file"
gunzip -c genes.ORI.gff.gz | $SCRIPTS_BUILD_DIR/hg38_RefSeq_NCBI_fix_GFF.pl chromosomes2name.txt > genes.gff 
gzip genes.gff

echo "Processing FASTA files"
rm -rf $GENOME || true
mkdir $GENOME || true
cd $GENOME
gunzip -c ../*.fa.gz | $SCRIPTS_BUILD_DIR/hg38_RefSeq_NCBI_fix_FASTA.pl ../chromosomes2name.txt | $SCRIPTS_DIR/fastaSplit.pl 

echo "Creating and compressing FASTA file $GENOME.fa"
cat chr[1-9].fa chr[1,2][0-9].fa chrX.fa chrY.fa chrMT.fa chr???*.fa | gzip -c > ../$GENOME.fa.gz
cd -

#---
# Copy to SnpEff dirs
#---

echo "Copying data to snpEff/data/$GENOME/"
mkdir $SNPEFF_DIR/data/$GENOME || true
cp genes.gff.gz $SNPEFF_DIR/data/$GENOME
cp $GENOME.fa.gz $SNPEFF_DIR/data/genomes/

echo "Done."
