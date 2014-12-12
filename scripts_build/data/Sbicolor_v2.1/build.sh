#!/bin/sh

# Create GFF file with sequence
(cat ORI/Sbicolor_255_v2.1.gene_exons.gff3.gz ; echo "#" ; echo "##FASTA"; gunzip -c ORI/Sbicolor_v2.1_255.fa.gz ) > genes.gff 

# Create protein file
gunzip -c ORI/Sbicolor_255_v2.1.protein.gz | sed "s/^>.*ID=\(.*v2.1\) annot.*/>\1/" > protein.fa

# Create CDS file
gunzip -c ORI/Sbicolor_255_v2.1.cds.gz | sed "s/^>.*ID=\(.*v2.1\) annot.*/>\1/" > cds.fa

