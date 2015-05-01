#!/bin/sh

# wget http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerEsmeraldo-like/gff/data/TriTrypDB-8.1_TcruziCLBrenerEsmeraldo-like.gff
# wget http://tritrypdb.org/common/downloads/release-8.1/TcruziCLBrenerNon-Esmeraldo-like/gff/data/TriTrypDB-8.1_TcruziCLBrenerNon-Esmeraldo-like.gff

rm -vf genes.gff sequence.fa geneDef.gff

for gff in TriTrypDB-8.1_TcruziCL*.gff
do
	echo $gff
	FASTA_DELIMITER=`grep -n "^##FASTA" $gff | cut -f 1 -d :`

	GFF_LINES=$(($FASTA_DELIMITER-1))
	FASTA_LINES=$(($FASTA_DELIMITER+1))

	head -n $GFF_LINES $gff >> geneDef.gff
	tail -n +$FASTA_LINES $gff >> sequence.fa
done

# Join files
cat geneDef.gff > genes.gff
echo "##FASTA" >> genes.gff
cat sequence.fa >> genes.gff
