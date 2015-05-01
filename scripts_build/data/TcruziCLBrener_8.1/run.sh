#!/bin/sh

WGET="wget -N"

#---
# Download files
#---

# $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerEsmeraldo-like/gff/data/TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like.gff
# $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerEsmeraldo-like/fasta/data/TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like_AnnotatedCDSs.fasta
# $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerEsmeraldo-like/fasta/data/TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like_AnnotatedProteins.fasta
# $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerEsmeraldo-like/fasta/data/TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like_Genome.fasta

# $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerNon-Esmeraldo-like/gff/data/TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like.gff
# $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerNon-Esmeraldo-like/fasta/data/TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like_AnnotatedCDSs.fasta
# $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerNon-Esmeraldo-like/fasta/data/TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like_AnnotatedProteins.fasta
# # $WGET http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerNon-Esmeraldo-like/fasta/data/TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like_Genome.fasta

#---
# Create joint GFF
#---

# rm -vf genes.gff genesDef.gff
# 
# for gff in TriTrypDB-8.1_TcruziCL*.gff
# do
# 	echo $gff
# 	FASTA_DELIMITER=`grep -n "^##FASTA" $gff | cut -f 1 -d :`
# 	GFF_LINES=$(($FASTA_DELIMITER-1))
# 
# 	head -n $GFF_LINES $gff >> genesDef.gff
# done
# 
# # Join files and append genomic fasta
# cat genesDef.gff > genes.gff
# echo "##FASTA" >> genes.gff
# cat TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like_Genome.fasta TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like_Genome.fasta \
# 	| sed "s/>\(.*\) | organism=.*/>\1/" \
# 	>> genes.gff

#---
# Create CDS fasta
#---

echo Building CDS fasta
cat TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like_AnnotatedCDSs.fasta TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like_AnnotatedCDSs.fasta \
	| sed "s/>\(.*\) | organism=.*/>rna_\1-1/" \
	> cds.fa

echo Building protein fasta
cat TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like_AnnotatedProteins.fasta TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like_AnnotatedProteins.fasta \
	| sed "s/>\(.*\) | organism=.*/>rna_\1-1/" \
	> protein.fa

