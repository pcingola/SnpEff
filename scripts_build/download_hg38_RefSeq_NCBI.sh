#!/bin/sh -e

# Genome name (in SnpEff's config file)
GENOME="GRCh38.p2.RefSeq"

# File names
GFF_REF="ref_GRCh38.p2_top_level.gff3.gz"
CHR_IDS="chromosomes.txt"
CHR_IDS_2_NAME="chromosomes2name.txt"

# Path to scripts
SNPEFF_DIR="$HOME/snpEff"
SCRIPTS_DIR="$SNPEFF_DIR/scripts"
SCRIPTS_BUILD_DIR="$SNPEFF_DIR/scripts_build/data/GRCh38.p2.RefSeq"

# #---
# # Download FASTA files
# #---
# 
# # Reference genome
# for chr in alts unlocalized unplaced chr1 chr2 chr3 chr4 chr5 chr6 chr7 chr8 chr9 chr10 chr11 chr12 chr13 chr14 chr15 chr16 chr17 chr18 chr19 chr20 chr21 chr22 chrX chrY chrMT 
# do
# 	echo Downloading chromosome $chr
# 	wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/Assembled_chromosomes/seq/hs_ref_GRCh38.p2_$chr.fa.gz
# done
# 
# # RNA and protein sequneces
# for n in 1 2 3 4 5 6
# do
# 	wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.$n.rna.fna.gz
# 	wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/mRNA_Prot/human.$n.protein.faa.gz
# done
# 
# #---
# # Download GFF data
# #---
# 
# echo "Download GFF files"
# wget -N ftp://ftp.ncbi.nlm.nih.gov/refseq/H_sapiens/H_sapiens/GFF/$GFF_REF
# 
# echo "Copying genes.ORI.gff file"
# cp $GFF_REF genes.ORI.gff.gz
# gunzip genes.ORI.gff.gz
# 
# #---
# # Download chromosome IDs file
# #---
# 
# ASSEMBLY_ID=`zcat genes.ORI.gff.gz | head -n 100 | grep "^#" | grep genome-build-accession | cut -f 2 -d :`
# echo "Assembly ID: $ASSEMBLY_ID"
# 
# echo "Download chromosome IDs map file"
# wget -O - ftp://ftp.ncbi.nlm.nih.gov/genomes/ASSEMBLY_REPORTS/All/$ASSEMBLY_ID.assembly.txt > $CHR_IDS
# 
# echo "Create a file mapping chromosome IDs to names: $CHR_IDS_2_NAME"
# cat $CHR_IDS | cut -f 1,7 | grep -v "^#" > $CHR_IDS_2_NAME

#---
# Process files
#---

echo "Processing GFF file"
$SCRIPTS_BUILD_DIR/hg38_RefSeq_NCBI_fix_GFF.pl genes.ORI.gff chromosomes2name.txt > genes.gff 
gzip genes.gff

# echo "Processing reference FASTA files"
# rm -rf $GENOME || true
# mkdir $GENOME || true
# cd $GENOME
# gunzip -c ../hs_ref*.fa.gz | $SCRIPTS_BUILD_DIR/hg38_RefSeq_NCBI_fix_FASTA.pl ../chromosomes2name.txt | $SCRIPTS_DIR/fastaSplit.pl 
# 
# echo "Creating and compressing FASTA file $GENOME.fa"
# cat chr[1-9].fa chr[1,2][0-9].fa chrX.fa chrY.fa chrMT.fa chr???*.fa | gzip -c > ../$GENOME.fa.gz
# cd -
# 

echo "Processing protein FASTA files"
gunzip -c human.?.protein.faa.gz | sed "s/^>gi|[0-9]*|ref|\(.*\)|.*/>\1/" > protein.ORI.fa 
cat protein.ORI.fa | $SCRIPTS_BUILD_DIR/hg38_RefSeq_NCBI_fix_FASTA_ProCds.pl protein_id.map.txt > protein.fa
gzip protein.fa

echo "Processing RNA FASTA files"
gunzip -c human.?.rna.fna.gz | sed "s/^>gi|[0-9]*|ref|\(.*\)|.*/>\1/" > cds.ORI.fa 
cat cds.ORI.fa | $SCRIPTS_BUILD_DIR/hg38_RefSeq_NCBI_fix_FASTA_ProCds.pl ids.map.txt > cds.fa
gzip cds.fa

# #---
# # Copy to SnpEff dirs
# #---
# 
echo "Copying data to snpEff/data/$GENOME/"
mkdir $SNPEFF_DIR/data/$GENOME || true
cp -v genes.gff.gz cds.fa.gz protein.fa.gz $SNPEFF_DIR/data/$GENOME
cp -v $GENOME.fa.gz $SNPEFF_DIR/data/genomes/

echo "Done."
