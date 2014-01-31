#!/bin/sh -e

echo "Processing GFF file"
gunzip -c Gmax_109_gene.gff3.gz > genes.gff
#	| sed "s/.five_prime_UTR.1//" \
#	| sed "s/.three_prime_UTR.1//" \
#	> genes.gff

echo "Adding FASTA sequence"
( echo "###" ; echo "##FASTA" ; gunzip -c Gmax_109.fa.gz ) >> genes.gff

ehco "Create CDS file"
gunzip -c Gmax_109_cds.fa.gz \
	| sed "s/|PACid:/|PAC:/" \
	| sed "s/Glyma.*|//" \
	> cds.fa 

echo "Create protein file"
gunzip -c Gmax_109_peptide.fa.gz \
	| sed "s/|PACid:/|PAC:/" \
	| sed "s/Glyma.*|//" \
	> protein.fa

# Build databse
cd $HOME/snpEff

./scripts/snpEffM.sh build -v -gff3 gmax1.09v8 2>&1 | tee gmax1.09v8.build

# CDS test
./scripts/snpEffM.sh cds -v gmax1.09v8 data/gmax1.09v8/cds.fa 2>&1 | tee gmax1.09v8.cds

cd -

echo Done!

