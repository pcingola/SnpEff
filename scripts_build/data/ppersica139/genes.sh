#!/bin/sh

# Download files
#wget ftp://ftp.plantgdb.org/download/Genomes/PeGDB/Ppersica_139.fa.gz
#wget ftp://ftp.plantgdb.org/download/Genomes/PeGDB/Ppersica_139_gene.gff3.gz

( zcat Ppersica_139_gene.gff3.gz ; echo "###" ; echo "##FASTA" ; zcat Ppersica_139.fa.gz ) > genes.gff

