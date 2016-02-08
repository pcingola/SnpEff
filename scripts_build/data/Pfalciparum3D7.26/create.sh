#!/bin/sh

# cp ORI/PlasmoDB-26_Pfalciparum3D7.gff genes.gff
# cp ORI/PlasmoDB-26_Pfalciparum3D7_Genome.fasta sequences.fa

cat ORI/PlasmoDB-26_Pfalciparum3D7_AnnotatedCDSs.fasta \
	| perl -pe 's|>(.*?) .*|>rna_\1-1|' \
	> cds.fa

cat ORI/PlasmoDB-26_Pfalciparum3D7_AnnotatedProteins.fasta \
	| perl -pe 's|>(.*?) .*|>rna_\1-1|' \
	> protein.fa
