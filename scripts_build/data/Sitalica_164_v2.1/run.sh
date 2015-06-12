#!/bin/sh

cp $HOME/Downloads/Sitalica_164_v2.1.cds cds.fa
cp $HOME/Downloads/Sitalica_164_v2.1.protein protein.fa
cp $HOME/Downloads/Sitalica_164_v2 ../genomes/Sitalica_164_v2.1.fa

# Fix CDS names in GFF file (there are some errors in the GFF)
cat $HOME/Downloads/Sitalica_164_v2.1.gene \
    | sed "s/\.version2.1.CDS.[0-9]*/.CDS/" \
    | sed "s/\.version2.1//" \
    > genes.gff


