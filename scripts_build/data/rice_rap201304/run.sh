#!/bin/sh

# #---
# # Download sequences
# #---
#
# wget http://rapdb.dna.affrc.go.jp/download/archive/irgsp1/IRGSP-1.0_genome.fasta.gz
# wget http://rapdb.dna.affrc.go.jp/download/archive/irgsp1/IRGSP-1.0_representative_2013-04-24.tar.gz
# wget http://rapdb.dna.affrc.go.jp/download/archive/irgsp1/IRGSP-1.0_protein_2013-04-24.fasta.gz
# wget http://rapdb.dna.affrc.go.jp/download/archive/irgsp1/IRGSP-1.0_cds_2013-04-24.fasta.gz
# 
# mkdir ORI
# mv IRGSP-1.0_* ORI/
# 
# # Add genome
# cp ORI/IRGSP-1.0_genome.fasta.gz ../genomes/rice_rap201304.fa.gz

# # Genes
# gunzip ORI/IRGSP-1.0_representative_2013-04-24.tar.gz
# tar -xvf ORI/IRGSP-1.0_representative_2013-04-24.tar
# mv IRGSP-1.0_representative_2013-04-24/* ORI/
# rmdir IRGSP-1.0_representative_2013-04-24

# Note: mRnas are missing Parent info (same thing happened in rice5, so I have a script for fixing this)
cat ORI/locus.gff ORI/transcripts.gff ORI/transcripts_exon.gff \
	| $HOME/snpEff/data/rice5/mRnaAddParent.pl \
	| sort -k1,1 -k4n,4n \
	| uniq \
	> genes.gff


cp ORI/IRGSP-1.0_cds_2013-04-24.fasta.gz cds.fa.gz
cp ORI/IRGSP-1.0_protein_2013-04-24.fasta.gz protein.fa.gz


