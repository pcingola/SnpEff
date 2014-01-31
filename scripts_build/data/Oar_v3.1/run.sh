#!/bin/sh -e

#---
# Download files
#---

# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/GFF/ref_Oar_v3.1_top_level.gff3.gz
# 
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr1.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr10.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr11.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr12.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr13.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr14.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr15.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr16.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr17.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr18.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr19.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr2.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr20.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr21.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr22.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr23.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr24.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr25.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr26.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr3.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr4.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr5.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr6.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr7.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr8.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chr9.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chrMT.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_chrX.fa.gz
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/Assembled_chromosomes/seq/oar_ref_Oar_v3.1_unplaced.fa.gz
#
# wget ftp://ftp.ncbi.nlm.nih.gov/genomes/Ovis_aries/protein/protein.fa.gz
#
# mkdir ORI
# mv *.gz ORI

#---
# Create reference genome FASTA file
#---

zcat \
		ORI/oar_ref_Oar_v3.1_chr1.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr2.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr3.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr4.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr5.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr6.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr7.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr8.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr9.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr10.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr11.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr12.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr13.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr14.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr15.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr16.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr17.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr18.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr19.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr20.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr21.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr22.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr23.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr24.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr25.fa.gz \
		ORI/oar_ref_Oar_v3.1_chr26.fa.gz \
		ORI/oar_ref_Oar_v3.1_chrMT.fa.gz \
		ORI/oar_ref_Oar_v3.1_chrX.fa.gz \
		ORI/oar_ref_Oar_v3.1_unplaced.fa.gz \
	| sed "s/^>gi|.*|ref|\(N._.*\)|.*/>\1/" \
	> Oar_v3.1.fa
pigz Oar_v3.1.fa
mv Oar_v3.1.fa.gz ../genomes/


#---
# Create genes list
#---

zcat ORI/ref_Oar_v3.1_top_level.gff3.gz > genes.gff

grep "Parent=rna" genes.gff | grep CDS | cut -f 9 | tr ";" "\t" | cut -f 2,3 | tr "=" "\t" | cut -f 2,4 | uniq > replace_list.txt
cat genes.gff | ./replace.pl > genes.new.gff
mv genes.new.gff genes.gff


#---
# Create proteins list
#---

zcat ORI/protein.fa.gz \
	| sed "s/^>gi|.*|ref|\(.._.*\)|.*/>\1/" \
	> protein.fa


