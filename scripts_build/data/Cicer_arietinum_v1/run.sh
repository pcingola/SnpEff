#!/bin/sh

#---
# Download
#---

mkdir ORI
cd ORI
wget http://www.icrisat.org/gt-bt/ICGGC/genomedata.zip
unzip genomedata.zip
cd -

#---
# Get propper chromosome names in FASTA file
#---

cat ORI/genomedata/assembly/Cicer_arietinum_GA_v1.0.masked.fa \
	| sed "s/^>\(\S*\).*/>\1/" 
  > Cicer_arietinum_v1.fa

gzip Cicer_arietinum_v1.fa
mv Cicer_arietinum_v1.fa.gz ~/snpEff/data/genomes/

#---
# Creage GFF file
#---
cat ORI/genomedata/annotation/02.gene/Cicer_arietinum_GA_v1.0.gene.gff \
	ORI/genomedata/annotation/03.ncRNA/Cicer_arietinum_GA_v1.0_ncRNA_annotation_*.gff \
	> genes.gff

cat ORI/genomedata/annotation/02.gene/Cicer_arietinum_GA_v1.0.gene.cds.fa \
	| sed "s/^>\(\S*\).*/>\1/" \
	> cds.fa

cat ORI/genomedata/annotation/02.gene/Cicer_arietinum_GA_v1.0.gene.pep.fa \
	| sed "s/^>\(\S*\).*/>\1/" \
	> protein.fa



