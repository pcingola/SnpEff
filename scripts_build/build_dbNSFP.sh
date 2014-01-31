#!/bin/sh

db=dbNSFP2.3.txt

head -n 1 dbNSFPv2.3/dbNSFP2.3_variant.chr1 > $db

cat dbNSFPv2.3/dbNSFP2.3_variant.chr1 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr2 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr3 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr4 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr5 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr6 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr7 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr8 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr9 \
		dbNSFPv2.3/dbNSFP2.3_variant.chrX \
		dbNSFPv2.3/dbNSFP2.3_variant.chrY \
		dbNSFPv2.3/dbNSFP2.3_variant.chr10 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr11 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr12 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr13 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr14 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr15 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr16 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr17 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr18 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr19 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr20 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr21 \
		dbNSFPv2.3/dbNSFP2.3_variant.chr22 \
	| grep -v "^#" \
	>> $db

echo BGZIP
bgzip $db
tabix -s 1 -b 2 -e 2 $db.gz

