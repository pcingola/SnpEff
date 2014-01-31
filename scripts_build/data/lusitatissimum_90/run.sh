#!/bin/sh

#cp ORI/Lusitatissimum_200.fa ../genomes/lusitatissimum_90.fa
#cp ORI/Lusitatissimum_200_gene_exons.gff3 ../genes.gff

cat ORI/Lusitatissimum_200_protein.fa \
	| sed "s/>.*PACid:/>PAC:/" \
	> protein.fa

cat ORI/Lusitatissimum_200_cds.fa \
	| sed "s/>.*PACid:/>PAC:/" \
	> cds.fa

