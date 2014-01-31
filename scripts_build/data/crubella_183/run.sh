#!/bin/sh

# cp Crubella_183.fa.gz ../genomes/crubella_183.fa.gz

# gunzip -c ORI/Crubella_183_gene.gff3.gz > genes.gff

gunzip -c ORI/Crubella_183_cds.fa | sed "s/>.*PACid:/>PAC:/" > cds.fa
gunzip -c ORI/Crubella_183_protein.fa | sed "s/>.*PACid:/>PAC:/" > protein.fa


