#!/bin/sh

rm genes.gtf* protein.fa* cds.fa*

# GTF
zcat ORI/Anopheles-gambiae-PEST_BASEFEATURES_AgamP4.2.gtf.gz \
	| sed "s/Parent=\(.*\);/transcript_id \"\1\";/" \
	> genes.gtf

# Chromosomes FASTA
cp ORI/Anopheles-gambiae-PEST_CHROMOSOMES_AgamP4.fa.gz  ../genomes/agam4.2.fa.gz

# CDS
cp ORI/Anopheles-gambiae-PEST_TRANSCRIPTS_AgamP4.2.fa.gz cds.fa.gz

# Proteins
zcat ORI/Anopheles-gambiae-PEST_PEPTIDES_AgamP4.2.fa.gz | sed "s/-P/-R/" > protein.fa

gzip -v protein.fa genes.gtf

