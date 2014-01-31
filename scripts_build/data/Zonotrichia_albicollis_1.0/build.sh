#!/bin/sh -e

#---
# Convert chromosome names for FASTA files
#---

zcat ORI/ref_Zonotrichia_albicollis-1.0.1_top_level.gff3.gz > genes.ORI.gff

grep "Parent=rna" genes.ORI.gff | cut -f 3- | grep ^CDS | cut -f 7 | tr ";" "\t" | cut -f 2,3 | tr "=" "\t" | cut -f 2,4 | uniq > replace_list.txt
cat genes.ORI.gff | ./replace.GFF_ID_Parent.pl > genes.gff

gzip genes.gff

#---
# Convert chromosome names for GFF file
#---

zcat ORI/44394_ref_Zonotrichia_albicollis-1.0.1_chrUn.fa.gz \
 	| sed "s/^>gi|.*|ref|\(.._.*\)|.*/>\1/" \
 	> Zonotrichia_albicollis_1.0.fa

gzip Zonotrichia_albicollis_1.0.fa

#---
# Convert headers in FASTA protein and rna
#---

zcat ORI/protein.fa.gz | sed "s/^>gi|.*|ref|\(.._.*\)|.*/>\1/" > protein.fa
zcat ORI/rna.fa.gz | sed "s/^>gi|.*|ref|\(.._.*\)|.*/>\1/" > cds.fa
gzip -v cds.fa protein.fa
