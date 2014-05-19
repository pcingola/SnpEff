#!/bin/sh

# echo Creating reference genome fasta file
# zcat ORI/cfe_ref_CB1_chrMT.fa.gz | sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/>\1/" > ../genomes/camelus_ferus.fa
# zcat ORI/cfe_ref_CB1_chrUn.fa.gz | sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/>\1/" >> ../genomes/camelus_ferus.fa
# 
# echo Creating proteins fasta file
# zcat ORI/protein.fa.gz | sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/>\1/" > protein.fa
# 
# echo Creating RNA fasta file
# zcat ORI/rna.fa.gz | sed "s/^>gi|[0-9]*|ref|\(.*\..*\)|.*/>\1/" > cds.fa
# 
echo Creating GFF
zcat ORI/ref_CB1_top_level.gff3.gz | grep "Parent=rna" | cut -f 3- | grep ^CDS | cut -f 7 | tr ";" "\t" | cut -f 2,3 | tr "=" "\t" | cut -f 2,4 | uniq > replace_list.txt
zcat ORI/ref_CB1_top_level.gff3.gz | ./replace.GFF_ID_Parent.pl > genes.gff


echo Done
