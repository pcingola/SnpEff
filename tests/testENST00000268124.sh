#!/bin/sh

tr="ENST00000268124"
base="test$tr.SNP.ORI"
out="test$tr.SNP"

# Sort by position
cat $base.vcf |  sort -k 1n,1n -k 2n,2n | cut -f 1-8 > tmp.vcf
cat $base.vcf.txt | grep $tr | cut -f 2- | sed "s/:/\t/" | sort -k 1n,1n -k 2n,2n | tr ";" "\t" | cut -f 7,11,12,14,15 | tr "\t" ";" > tmp.txt

# Combine
paste tmp.vcf tmp.txt > $out.vcf
rm tmp.vcf tmp.txt

