#!/bin/sh

# Delete old databases
rm -vf *.bin

# Create genes file
gunzip -c ../hg19/genes.refseq.gz \
	| grep -w \
			-e binNum \
			-e NM_001269 -e NM_001048194 -e NM_001048195 -e NM_001048199 \
			-e NM_001242466 -e NM_006218 -e NM_181504 -e NM_181523 -e NM_181524 \
			-e NM_001048194 -e NM_001048195 -e NM_001048199 -e NM_001269 \
	> genes.refseq

# Build chromosome reference sequence
cd ../genomes
rm -vf testHg19Pdb.fa.gz
for chr in `cat ../testHg19Pdb/genes.refseq | cut -f 3 | sort | uniq `
do
	echo chr=$chr
	cat hg19/$chr.fa.gz >> testHg19Pdb.fa.gz
done

# Build database
cd $HOME/snpEff
java -Xmx4g -jar snpEff.jar build -v testHg19Pdb

# Build PDB test database
java -Xmx4g -jar snpEff.jar pdb -v -pdbDir $HOME/snpEff/tests_snpeff/pdb testHg19Pdb 
