#!/bin/sh -e

# Convert chromosome names for FASTA files

# zcat ORI/ref_Macaca_fascicularis_5.0_top_level.gff3.gz \
# 	| sed "s/NC_022272.1/chr1/" \
# 	| sed "s/NC_022273.1/chr2/" \
# 	| sed "s/NC_022274.1/chr3/" \
# 	| sed "s/NC_022275.1/chr4/" \
# 	| sed "s/NC_022276.1/chr5/" \
# 	| sed "s/NC_022277.1/chr6/" \
# 	| sed "s/NC_022278.1/chr7/" \
# 	| sed "s/NC_022279.1/chr8/" \
# 	| sed "s/NC_022280.1/chr9/" \
# 	| sed "s/NC_022281.1/chr10/" \
# 	| sed "s/NC_022282.1/chr11/" \
# 	| sed "s/NC_022283.1/chr12/" \
# 	| sed "s/NC_022284.1/chr13/" \
# 	| sed "s/NC_022285.1/chr14/" \
# 	| sed "s/NC_022286.1/chr15/" \
# 	| sed "s/NC_022287.1/chr16/" \
# 	| sed "s/NC_022288.1/chr17/" \
# 	| sed "s/NC_022289.1/chr18/" \
# 	| sed "s/NC_022290.1/chr19/" \
# 	| sed "s/NC_022291.1/chr20/" \
# 	| sed "s/NC_022292.1/chrX/" \
# 	| sed "s/NC_012670.1/chrMT/" \
#  	> genes.ORI.gff

grep "Parent=rna" genes.ORI.gff | cut -f 3- | grep ^CDS | cut -f 7 | tr ";" "\t" | cut -f 2,3 | tr "=" "\t" | cut -f 2,4 | uniq > replace_list.txt
cat genes.ORI.gff | ./replace.GFF_ID_Parent.pl > genes.gff

# gzip genes.gff

# Convert chromosome names for GFF file
#
# zcat  \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr1.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr2.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr3.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr4.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr5.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr6.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr7.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr8.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr9.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr10.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr11.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr12.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr13.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr14.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr15.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr16.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr17.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr18.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr19.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chr20.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chrX.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_chrMT.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_unlocalized.fa.gz \
# 	ORI/mfa_ref_Macaca_fascicularis_5.0_unplaced.fa.gz \
# 	| sed "s/^>gi|.*|ref|\(.._.*\)|.*/>\1/" \
# 	| sed "s/NC_022272.1/chr1/" \
# 	| sed "s/NC_022273.1/chr2/" \
# 	| sed "s/NC_022274.1/chr3/" \
# 	| sed "s/NC_022275.1/chr4/" \
# 	| sed "s/NC_022276.1/chr5/" \
# 	| sed "s/NC_022277.1/chr6/" \
# 	| sed "s/NC_022278.1/chr7/" \
# 	| sed "s/NC_022279.1/chr8/" \
# 	| sed "s/NC_022280.1/chr9/" \
# 	| sed "s/NC_022281.1/chr10/" \
# 	| sed "s/NC_022282.1/chr11/" \
# 	| sed "s/NC_022283.1/chr12/" \
# 	| sed "s/NC_022284.1/chr13/" \
# 	| sed "s/NC_022285.1/chr14/" \
# 	| sed "s/NC_022286.1/chr15/" \
# 	| sed "s/NC_022287.1/chr16/" \
# 	| sed "s/NC_022288.1/chr17/" \
# 	| sed "s/NC_022289.1/chr18/" \
# 	| sed "s/NC_022290.1/chr19/" \
# 	| sed "s/NC_022291.1/chr20/" \
# 	| sed "s/NC_022292.1/chrX/" \
# 	| sed "s/NC_012670.1/chrMT/" \
# 	> Macaca_fascicularis.fa
#
# gzip Macaca_fascicularis.fa

# Convert headers in FASTA protein and rna


# zcat ORI/protein.fa.gz | sed "s/^>gi|.*|ref|\(.._.*\)|.*/>\1/" > protein.fa
# zcat ORI/rna.fa.gz | sed "s/^>gi|.*|ref|\(.._.*\)|.*/>\1/" > cds.fa
# gzip -v cds.fa protein.fa
