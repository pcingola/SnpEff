#!/bin/sh

for chr in chr10.fa chr11.fa chr12.fa chr13.fa chr14.fa chr15.fa chr16.fa chr17.fa chr18.fa chr19.fa chr1.fa chr20.fa chr21.fa chr22.fa chr2.fa chr3.fa chr4.fa chr5.fa chr6.fa chr7.fa chr8.fa chr9.fa chrMT.fa chrX.fa chrY.fa
do
	MDGR=`cat GRCh37.65/$chr | grep -v "^>" | tr -d "\n" | tr "[a-z]" "[A-Z]" | md5sum`
	MDHG=`cat hg19/chr$chr   | grep -v "^>" | tr -d "\n" | tr "[a-z]" "[A-Z]" | md5sum`
	echo -e "$chr\n\t$MDGR\n\t$MDHG"
done
